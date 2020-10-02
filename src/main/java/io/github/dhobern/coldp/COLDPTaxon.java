/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.dhobern.coldp;

import io.github.dhobern.coldp.COLDPDistribution.RegionSort;
import io.github.dhobern.coldp.TreeRenderProperties.ContextType;
import static io.github.dhobern.utils.StringUtils.*;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author stang
 */
public class COLDPTaxon implements Comparable<COLDPTaxon>, TreeRenderable {
    
    private static final Logger LOG = LoggerFactory.getLogger(COLDPTaxon.class);
   
    private String ID;
    private String parentID;
    private String nameID;
    private String scrutinizer;
    private String scrutinizerDate;
    private String referenceID;
    private boolean extinct;
    private String temporalRangeEnd;
    private String lifezone;
    private String kingdom;
    private String phylum;
    private String clazz;
    private String order;
    private String superfamily;
    private String family;
    private String subfamily;
    private String tribe;
    private String genus;
    private String species;
    private String remarks;
    
    private Set<COLDPTaxon> children;
    private COLDPTaxon parent;
    private COLDPName name;
    private COLDPReference reference;
    private List<COLDPSynonym> synonyms;
    private Set<COLDPDistribution> distributions;

    public COLDPTaxon() {
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getParentID() {
        return parent == null ? parentID : parent.getID();
    }

    public void setParentID(String parentID) {
        if (parent == null) {
            this.parentID = parentID;
        } else {
            LOG.error("Attempted to set parentID to " + parentID + " when nameRelation associated with parent " + parent);
        }
    }

    public COLDPTaxon getParent() {
        return parent;
    }

    public void setParent(COLDPTaxon parent) {
        if (this.parent != null) {
            this.parent.deregisterChild(this);
        }
        this.parent = parent;
        parentID = null;
        if (parent != null) {
            parent.registerChild(this);
        }
    }

    public Set<COLDPTaxon> getChildren() {
        return children;
    }

    void registerChild(COLDPTaxon child) {
        if (child != null) {
            if (children == null) {
                children = new TreeSet<>(new AlphabeticalSortByScientificName());
            }
            children.add(child);
        }
    }
 
    void deregisterChild(COLDPTaxon child) {
        if (child != null && children != null) {
            children.remove(child);
        }
    }

    public String getNameID() {
        return name == null ? nameID : name.getID();
    }

    public void setNameID(String nameID) {
        if (name == null) {
            this.nameID = nameID;
        } else {
            LOG.error("Attempted to set nameID to " + nameID + " when nameRelation associated with name " + name);
        }
    }

    public COLDPName getName() {
        return name;
    }

    public void setName(COLDPName name) {
        this.name = name;
        nameID = null;
        if (name != null) {
            name.setTaxon(this);
        }
    }

    public String getScrutinizer() {
        return scrutinizer;
    }

    public void setScrutinizer(String scrutinizer) {
        this.scrutinizer = scrutinizer;
    }

    public String getScrutinizerDate() {
        return scrutinizerDate;
    }

    public void setScrutinizerDate(String scrutinizerDate) {
        this.scrutinizerDate = scrutinizerDate;
    }

    public String getReferenceID() {
        return reference == null ? referenceID : reference.getID();
    }

    public void setReferenceID(String referenceID) {
        if (reference == null) {
            this.referenceID = referenceID;
        } else {
            LOG.error("Attempted to set referenceID to " + referenceID + " when taxon associated with reference " + reference);
        }
    }

    public COLDPReference getReference() {
        return reference;
    }

    public void setReference(COLDPReference reference) {
        if (this.reference != null) {
            this.reference.deregisterTaxon(this);
        }
        this.reference = reference;
        referenceID = null;
        if (reference != null) {
            reference.registerTaxon(this);
        }
    }

    public boolean isExtinct() {
        return extinct;
    }

    public void setExtinct(boolean extinct) {
        this.extinct = extinct;
    }

    public String getTemporalRangeEnd() {
        return temporalRangeEnd;
    }

    public void setTemporalRangeEnd(String temporalRangeEnd) {
        this.temporalRangeEnd = temporalRangeEnd;
    }

    public String getLifezone() {
        return lifezone;
    }

    public void setLifezone(String lifezone) {
        this.lifezone = lifezone;
    }

    public String getKingdom() {
        return kingdom;
    }

    public void setKingdom(String kingdom) {
        this.kingdom = kingdom;
    }

    public String getPhylum() {
        return phylum;
    }

    public void setPhylum(String phylum) {
        this.phylum = phylum;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String _clazz) {
        this.clazz = _clazz;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getSuperfamily() {
        return superfamily;
    }

    public void setSuperfamily(String superfamily) {
        this.superfamily = superfamily;
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public String getSubfamily() {
        return subfamily;
    }

    public void setSubfamily(String subfamily) {
        this.subfamily = subfamily;
    }

    public String getTribe() {
        return tribe;
    }

    public void setTribe(String tribe) {
        this.tribe = tribe;
    }

    public String getGenus() {
        return genus;
    }

    public void setGenus(String genus) {
        this.genus = genus;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public List<COLDPSynonym> getSynonyms() {
        return synonyms;
    }

    void registerSynonym(COLDPSynonym synonym) {
        if (synonym != null) {
            if (synonyms == null) {
                synonyms = new ArrayList<>();
            }
            synonyms.add(synonym);
        }
    }
 
    void deregisterSynonym(COLDPSynonym synonym) {
        if (synonym != null && synonyms != null) {
            synonyms.remove(synonym);
        }
    }

    public Set<COLDPDistribution> getDistributions() {
        return distributions;
    }

    void registerDistribution(COLDPDistribution distribution) {
        if (distribution != null) {
            if (distributions == null) {
                distributions = new TreeSet<>(new RegionSort());
            }
            distributions.add(distribution);
        }
    }
 
    void deregisterDistribution(COLDPDistribution distribution) {
        if (distribution != null && distributions != null) {
            distributions.remove(distribution);
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + Objects.hashCode(this.ID);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final COLDPTaxon other = (COLDPTaxon) obj;
        if (!Objects.equals(this.ID, other.ID)) {
            return false;
        }
        return true;
    }
    
    private String getNameWithAuthorship() {
        if (name != null) {
            return name.getScientificName() + " " + name.getAuthorship();
        }
        return null;
    }

    @Override
    public int compareTo(COLDPTaxon o) {
        return this.getID().compareTo(o.getID());
    }

    public static class AlphabeticalSort implements Comparator<COLDPTaxon> { 
        
        private final Map<String, COLDPName> names;
        
        private AlphabeticalSort() {
            names = null;
        }
        
        public AlphabeticalSort(Map<String, COLDPName> names) {
            this.names = names;
        }

        @Override
        public int compare(COLDPTaxon o1, COLDPTaxon o2) {
            int comparison;
            
            if (o1.name != null && o2.name != null) {
                comparison = o1.getNameWithAuthorship().compareTo(o2.getNameWithAuthorship());
            } else {
                comparison = o1.compareTo(o2);
            }
                
            return comparison;
        }
    }

    
    public static class AlphabeticalSortByScientificName implements Comparator<COLDPTaxon> { 
        @Override
        public int compare(COLDPTaxon o1, COLDPTaxon o2) {
            return o1.getNameWithAuthorship().compareTo(o2.getNameWithAuthorship());
        }
    }

    public static String getCsvHeader() {
        return "ID,parentID,nameID,scrutinizer,scrutinizerDate,referenceID,"
               + "extinct,temporalRangeEnd,lifezone,kingdom,phylum,class,"
               + "order,superfamily,family,subfamily,tribe,genus,species,"
               + "remarks"; 
    }
    
    public String toCsv() {
        return buildCSV(ID, getParentID(), getNameID(), scrutinizer, 
                        scrutinizerDate, getReferenceID(),
                        extinct ? "true" : "false", temporalRangeEnd,
                        lifezone, kingdom, phylum, clazz, order, superfamily,
                        family, subfamily, tribe, genus, species, remarks);
    }

    @Override
    public void render(PrintWriter writer, TreeRenderProperties context) {
        if (context.getTreeRenderType() == TreeRenderProperties.TreeRenderType.HTML) {
            String divClass;
            ContextType childContextType;
            boolean recursive;
            if (context.getContextType() == ContextType.HigherTaxa) {
                divClass = "HigherTaxon";
                childContextType = ContextType.HigherTaxa;
                recursive = false;
                        
            } else {
                divClass = upperFirst(name.getRank());
                childContextType = ContextType.Taxon;
                recursive = true;
            }
                
            if (reference != null) {
                context.addReference(reference);
            }

            writer.println(context.getIndent() + "<div class=\"" + divClass + "\" id=\"taxon-" + ID + "\">");
            name.render(writer, new TreeRenderProperties(context, this, childContextType));

            if (synonyms != null && synonyms.size() > 0) {
                renderSynonyms(writer, new TreeRenderProperties(context, this, childContextType));
            }

            if (remarks != null) {
                renderNote(writer, new TreeRenderProperties(context, this, childContextType));
            }
            
            if (distributions != null) {
                renderDistributions(writer,  new TreeRenderProperties(context, this, childContextType));
            }

            if (context.getReferenceList().size() > 0) {
                renderReferences(writer, new TreeRenderProperties(context, this, childContextType));
            }

            if (recursive && children != null) {
                for (COLDPTaxon taxon : children) {
                    taxon.render(writer, new TreeRenderProperties(context, this, ContextType.Taxon, true));
                }
            }

            writer.println(context.getIndent() + "</div>");
        }
    }
        
    private void renderSynonyms(PrintWriter writer, TreeRenderProperties context) {
        if (context.getTreeRenderType() == TreeRenderProperties.TreeRenderType.HTML) {
            writer.println(context.getIndent() + "<div class=\"Synonyms\">");

            for (COLDPSynonym synonym : synonyms) {
                synonym.render(writer, new TreeRenderProperties(context, this, ContextType.Synonyms));
            }

            writer.println(context.getIndent() + "</div>");
        }
    }    

    private void renderReferences(PrintWriter writer, TreeRenderProperties context) {
        if (context.getTreeRenderType() == TreeRenderProperties.TreeRenderType.HTML) {
            writer.println(context.getIndent() + "<div class=\"References\">");

            for (COLDPReference reference : context.getReferenceList()) {
                reference.render(writer, new TreeRenderProperties(context, this, ContextType.References));
            }

            writer.println(context.getIndent() + "</div>");
        }
    }    

    private void renderDistributions(PrintWriter writer, TreeRenderProperties context) {
        if (context.getTreeRenderType() == TreeRenderProperties.TreeRenderType.HTML) {
            writer.println(context.getIndent() + "<div class=\"Distribution\">Distribution: ");

            for (COLDPDistribution distribution : distributions) {
                distribution.render(writer, new TreeRenderProperties(context, this, ContextType.Distribution));
            }

            writer.println(context.getIndent() + "</div>");
        }
    }    
    
    private void renderNote(PrintWriter writer, TreeRenderProperties context) {
        if (context.getTreeRenderType() == TreeRenderProperties.TreeRenderType.HTML) {
            writer.println(context.getIndent() + wrapDiv("Note", "Note: " + wrapEmphasis(linkURLs(remarks))));
        }
    }    
}
