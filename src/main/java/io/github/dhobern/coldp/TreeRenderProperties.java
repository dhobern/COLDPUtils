/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.dhobern.coldp;

import io.github.dhobern.utils.StringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 *
 * @author stang
 */
public class TreeRenderProperties {

    public enum TreeRenderType {
        HTML("  ", "&nbsp;", false,
                StringUtils::wrapStrong,
                StringUtils::wrapEmphasis,
                StringUtils::linkURLs,
                (s) -> "<div class=\"" + s + "\">",
                (s, id) -> "<div class=\"" + s + "\"" + (id == null ? "" : " id=\"" + id + "\"") + ">",
                "</div>"),
        TEXT(" ", " ", true,
                null,
                (s) -> "_" + s + "_",
                null, 
                (s) -> s + ": ", 
                (s, id) -> s + (id == null ? "" : " (" + id + ")") + ": ",
                "");
        
        private final String indentUnit;
        private final String nbsp;
        private final boolean spaceTaxa;
        private final Function<String, String> _wrapStrong;
        private final Function<String, String> _wrapEmphasis;
        private final Function<String, String> _linkURLs;
        private final Function<String, String> _openNode;
        private final BiFunction<String, String, String> _openNodeWithID;
        private final String closeNode;
        
        private TreeRenderType() {
            indentUnit =  "";
            nbsp = " ";
            spaceTaxa = false;
            _wrapStrong = null;
            _wrapEmphasis = null;
            _linkURLs = null;
            _openNode = null;
            _openNodeWithID = null;
            closeNode = "";
        }
        
        private TreeRenderType(String indentUnit, 
                String nbsp,
                boolean spaceTaxa,
                Function<String, String> _wrapStrong,
                Function<String, String> _wrapEmphasis,
                Function<String, String> _linkURLs,
                Function<String, String> _openNode,
                BiFunction<String, String, String> _openNodeWithID,
                String closeNode) {
            this.indentUnit = indentUnit;
            this.nbsp = nbsp;
            this.spaceTaxa = spaceTaxa;
            this._wrapStrong = _wrapStrong;
            this._wrapEmphasis = _wrapEmphasis;
            this._linkURLs = _linkURLs;
            this._openNode = _openNode;
            this._openNodeWithID = _openNodeWithID;
            this.closeNode = closeNode;
        }
        
        public String getIndentUnit() {
            return indentUnit;
        }
        
        public String getNBSP() {
            return nbsp;
        }
        
        public boolean spaceTaxa() {
            return spaceTaxa;
        }
        
        public String wrapStrong(String s) {
            if (_wrapStrong != null) {
                s = _wrapStrong.apply(s);
            }
            return s;
        }
        
        public String wrapEmphasis(String s) {
            if (_wrapEmphasis != null) {
                s = _wrapEmphasis.apply(s);
            }
            return s;
        }

        public String linkURLs(String s) {
            if (_linkURLs != null) {
                s = _linkURLs.apply(s);
            }
            return s;
        }

        public String openNode(String s) {
            if (_openNode != null) {
                s = _openNode.apply(s);
            }
            return s;
        }
        
        public String openNodeWithID(String s, String id) {
            if (_openNodeWithID != null) {
                s = _openNodeWithID.apply(s, id);
            }
            return s;
        }
        
        public String closeNode() {
            return closeNode;
        }
    } 
    
    public enum ContextType {
        None, HigherTaxa, Root, Name, NameRelation, References, Reference, NameReference, Taxon, Synonyms, Synonym, Region, Distribution;
    } 
    
    private static class TreeRenderNode {
        private final ContextType contextType;
        private final TreeRenderable treeRenderable;
        
        public TreeRenderNode(ContextType type, TreeRenderable renderable) {
            contextType = type;
            treeRenderable = renderable;
        }

        public ContextType getContextType() {
            return contextType;
        }

        public TreeRenderable getTreeRenderable() {
            return treeRenderable;
        }
    }
    
    private final List<TreeRenderNode> renderStack = new ArrayList<>();
    private final TreeRenderType treeRenderType;
    private final String indentUnit;
    private final int indentCount;
    private final ContextType contextType;
    private final Set<COLDPReference> referenceList; 
    
    public TreeRenderProperties(TreeRenderType treeRenderType, ContextType contextType) {
        this.treeRenderType = treeRenderType;
        this.contextType = contextType;
        this.indentUnit = treeRenderType.getIndentUnit();
        this.indentCount = 0;
        this.referenceList = new TreeSet<>(new COLDPReference.BibliographicSort());
    }

    public TreeRenderProperties(TreeRenderType treeRenderType, ContextType contextType, String indentUnit, int indentCount) {
        this.treeRenderType = treeRenderType;
        this.indentUnit = indentUnit;
        this.indentCount = indentCount;
        this.contextType = contextType;
        this.referenceList = new TreeSet<>(new COLDPReference.BibliographicSort());
    }

    public TreeRenderProperties(TreeRenderProperties parentContext, 
                                TreeRenderable parent, 
                                ContextType contextType) {
        this(parentContext, parent, contextType, false);
    }
    
    public TreeRenderProperties(TreeRenderProperties parentContext, 
                                TreeRenderable parent, 
                                ContextType contextType, 
                                boolean resetReferences) {
        this.treeRenderType = parentContext.treeRenderType;
        this.indentUnit = parentContext.indentUnit;
        this.indentCount = parentContext.indentCount + 1;
        this.contextType = contextType;
        this.renderStack.add(new TreeRenderNode(contextType, parent));
        this.renderStack.addAll(parentContext.renderStack);
        if (!resetReferences) {
            this.referenceList = parentContext.referenceList;
        } else {
            this.referenceList = new TreeSet<>(new COLDPReference.BibliographicSort());
        }
    }

    public TreeRenderType getTreeRenderType() {
        return treeRenderType;
    }

    public String getIndent() {
        return indentUnit.repeat(indentCount);
    }

    public ContextType getContextType() {
        return contextType;
    }   

    void addReference(COLDPReference reference) {
        referenceList.add(reference);
    }
    
    public Set<COLDPReference> getReferenceList() {
        return referenceList;
    }
    
    public COLDPSynonym getCurrentSynonym() {
        for (TreeRenderNode node : renderStack) {
            TreeRenderable renderable = node.getTreeRenderable();
            if (renderable instanceof COLDPSynonym) {
                return (COLDPSynonym) renderable;
            }
        }
        
        return null;
    }
    
    public COLDPName getCurrentName() {
        for (TreeRenderNode node : renderStack) {
            TreeRenderable renderable = node.getTreeRenderable();
            if (renderable instanceof COLDPName) {
                return (COLDPName) renderable;
            }
        }
        
        return null;
    }
    
    public COLDPRegion getCurrentRegion() {
        for (TreeRenderNode node : renderStack) {
            TreeRenderable renderable = node.getTreeRenderable();
            if (renderable instanceof COLDPRegion) {
                return (COLDPRegion) renderable;
            }
        }
        
        return null;
    }
    
    public COLDPTaxon getCurrentTaxon() {
        for (TreeRenderNode node : renderStack) {
            TreeRenderable renderable = node.getTreeRenderable();
            if (renderable instanceof COLDPTaxon) {
                return (COLDPTaxon) renderable;
            }
        }
        
        return null;
    }
}
