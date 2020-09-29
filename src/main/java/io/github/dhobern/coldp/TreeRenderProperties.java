/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.dhobern.coldp;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author stang
 */
public class TreeRenderProperties {

    public enum TreeRenderType {
        HTML("  ");
        
        private final String indentUnit;
        
        private TreeRenderType() {
            indentUnit =  "";
        }
        private TreeRenderType(String indentUnit) {
            this.indentUnit = indentUnit;
        }
        
        public String getIndentUnit() {
            return indentUnit;
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
