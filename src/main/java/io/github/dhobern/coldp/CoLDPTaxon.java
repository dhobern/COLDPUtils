/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.dhobern.coldp;

import io.github.dhobern.utils.StringUtils;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author stang
 */
public class CoLDPTaxon implements Comparable<CoLDPTaxon> {
    
    private static final Logger LOG = LoggerFactory.getLogger(CoLDPTaxon.class);
   
    private Integer ID;
    private Integer parentID;
    private Integer nameID;
    private String scrutinizer;
    private String scrutinizerDate;
    private Integer referenceID;
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
    
    private Set<CoLDPTaxon> children;
    private CoLDPTaxon parent;
    private CoLDPName name;
    private CoLDPReference reference;
    private Set<CoLDPSynonym> synonyms;
    private Set<CoLDPDistribution> distributions;

    public CoLDPTaxon() {
    }

    public Integer getID() {
        return ID;
    }

    public void setID(Integer ID) {
        this.ID = ID;
    }

    public Integer getParentID() {
        return parent == null ? parentID : parent.getID();
    }

    public void setParentID(Integer parentID) {
        if (parent == null) {
            this.parentID = parentID;
        } else {
            LOG.error("Attempted to set parentID to " + parentID + " when nameRelation associated with parent " + parent);
        }
    }

    public CoLDPTaxon getParent() {
        return parent;
    }

    public void setParent(CoLDPTaxon parent) {
        if (this.parent != null) {
            this.parent.deregisterChild(this);
        }
        this.parent = parent;
        parentID = null;
        if (parent != null) {
            parent.registerChild(this);
        }
    }

    public Set<CoLDPTaxon> getChildren() {
        return children;
    }

    void registerChild(CoLDPTaxon child) {
        if (child != null) {
            if (children == null) {
                children = new HashSet<>();
            }
            children.add(child);
        }
    }
 
    void deregisterChild(CoLDPTaxon child) {
        if (child != null && children != null) {
            children.remove(child);
        }
    }

    public Integer getNameID() {
        return name == null ? nameID : name.getID();
    }

    public void setNameID(Integer nameID) {
        if (name == null) {
            this.nameID = nameID;
        } else {
            LOG.error("Attempted to set nameID to " + nameID + " when nameRelation associated with name " + name);
        }
    }

    public CoLDPName getName() {
        return name;
    }

    public void setName(CoLDPName name) {
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

    public Integer getReferenceID() {
        return reference == null ? referenceID : reference.getID();
    }

    public void setReferenceID(Integer referenceID) {
        if (reference == null) {
            this.referenceID = referenceID;
        } else {
            LOG.error("Attempted to set referenceID to " + referenceID + " when taxon associated with reference " + reference);
        }
    }

    public CoLDPReference getReference() {
        return reference;
    }

    public void setReference(CoLDPReference reference) {
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

    public Set<CoLDPSynonym> getSynonyms() {
        return synonyms;
    }

    void registerSynonym(CoLDPSynonym synonym) {
        if (synonym != null) {
            if (synonyms == null) {
                synonyms = new HashSet<>();
            }
            synonyms.add(synonym);
        }
    }
 
    void deregisterSynonym(CoLDPSynonym synonym) {
        if (synonym != null && synonyms != null) {
            synonyms.remove(synonym);
        }
    }

    public Set<CoLDPDistribution> getDistributions() {
        return distributions;
    }

    void registerDistribution(CoLDPDistribution distribution) {
        if (distribution != null) {
            if (distributions == null) {
                distributions = new HashSet<>();
            }
            distributions.add(distribution);
        }
    }
 
    void deregisterDistribution(CoLDPDistribution distribution) {
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
        final CoLDPTaxon other = (CoLDPTaxon) obj;
        if (!Objects.equals(this.ID, other.ID)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(CoLDPTaxon o) {
        return this.getID().compareTo(o.getID());
    }

    public static class AlphabeticalSort implements Comparator<CoLDPTaxon> { 
        
        private final Map<Integer, CoLDPName> names;
        
        private AlphabeticalSort() {
            names = null;
        }
        
        public AlphabeticalSort(Map<Integer, CoLDPName> names) {
            this.names = names;
        }

        @Override
        public int compare(CoLDPTaxon o1, CoLDPTaxon o2) {
            int comparison;
            
            if (names != null) {
                CoLDPName n1 = names.get(o1.getNameID());
                CoLDPName n2 = names.get(o2.getNameID());
                comparison = n1.getScientificName().compareTo(n2.getScientificName());
            } else {
                comparison = o1.compareTo(o2);
            }
                
            return comparison;
        }
    }

    public static String getCsvHeader() {
        return "ID,parentID,nameID,scrutinizer,scrutinizerDate,referenceID,"
               + "extinct,temporalRangeEnd,lifezone,kingdom,phylum,class,"
               + "order,superfamily,family,subfamily,tribe,genus,species,"
               + "remarks"; 
    }
    
    public String toCsv() {
        return StringUtils.toCsv(StringUtils.safeString(ID),
                                 StringUtils.safeString(getParentID()),
                                 StringUtils.safeString(getNameID()),
                                 scrutinizer,
                                 scrutinizerDate,
                                 StringUtils.safeString(getReferenceID()),
                                 extinct ? "true" : "false",
                                 temporalRangeEnd,
                                 lifezone,
                                 kingdom,
                                 phylum,
                                 clazz,
                                 order,
                                 superfamily,
                                 family,
                                 subfamily,
                                 tribe,
                                 genus,
                                 species,
                                 remarks);
    }
}
