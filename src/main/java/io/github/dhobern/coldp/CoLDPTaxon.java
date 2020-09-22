/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.dhobern.coldp;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author stang
 */
public class CoLDPTaxon implements Comparable<CoLDPTaxon> {
    
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

    public CoLDPTaxon() {
    }

    public Integer getID() {
        return ID;
    }

    public void setID(Integer ID) {
        this.ID = ID;
    }

    public Integer getParentID() {
        return parentID;
    }

    public void setParentID(Integer parentID) {
        this.parentID = parentID;
    }

    public Integer getNameID() {
        return nameID;
    }

    public void setNameID(Integer nameID) {
        this.nameID = nameID;
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
        return referenceID;
    }

    public void setReferenceID(Integer referenceID) {
        this.referenceID = referenceID;
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
}
