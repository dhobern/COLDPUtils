/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.dhobern.coldp;

import io.github.dhobern.utils.StringUtils;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author stang
 */
public class CoLDPName {
    
    private static final Logger LOG = LoggerFactory.getLogger(CoLDPName.class);

    private Integer ID;
    private Integer basionymID;
    private String scientificName;
    private String authorship;
    private String rank;
    private String uninomial;
    private String genus;
    private String specificEpithet;
    private String infraspecificEpithet;
    private Integer referenceID;
    private String publishedInPage;
    private String publishedInYear;
    private String code;
    private String status;
    private String remarks;
    private String link;
    
    private CoLDPName basionym;
    private Set<CoLDPName> combinations;
    private CoLDPReference reference;
    private Set<CoLDPNameReference> nameReferences;
    private Set<CoLDPNameRelation> nameRelations;
    private Set<CoLDPNameRelation> relatedNameRelations;
    private CoLDPTaxon taxon;
    private Set<CoLDPSynonym> synonyms;

    public CoLDPName() {
    }

    public Integer getID() {
        return ID;
    }

    public void setID(Integer ID) {
        this.ID = ID;
    }

    public Integer getBasionymID() {
        return (basionym == null) ? basionymID : basionym.getID();
    }

    public void setBasionymID(Integer basionymID) {
        if (basionym == null) {
            this.basionymID = basionymID;
        } else {
            LOG.error("Attempted to set basionymID to " + basionymID + " when name associated with basionym " + basionym);
        }
        this.basionymID = basionymID;
    }

    public CoLDPName getBasionym() {
        return basionym;
    }

    public void setBasionym(CoLDPName basionym) {
        if (this.basionym != null && !this.basionym.equals(this)) {
            this.basionym.deregisterCombination(this);
        }
        this.basionym = basionym;
        basionymID = null;
        if (basionym != null && !basionym.equals(this)) {
            basionym.registerCombination(this);
        }
    }

    public Set<CoLDPName> getCombinations() {
        return combinations;
    }

    void registerCombination(CoLDPName combination) {
        if (combination != null) {
            if (combinations == null) {
                combinations = new HashSet<>();
            }
            combinations.add(combination);
        }
    }
 
    void deregisterCombination(CoLDPName combination) {
        if (combination != null && combinations != null) {
            combinations.remove(combination);
        }
    }

    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    public String getAuthorship() {
        return authorship;
    }

    public void setAuthorship(String authorship) {
        this.authorship = authorship;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public String getUninomial() {
        return uninomial;
    }

    public void setUninomial(String uninomial) {
        this.uninomial = uninomial;
    }

    public String getGenus() {
        return genus;
    }

    public void setGenus(String genus) {
        this.genus = genus;
    }

    public String getSpecificEpithet() {
        return specificEpithet;
    }

    public void setSpecificEpithet(String specificEpithet) {
        this.specificEpithet = specificEpithet;
    }

    public String getInfraspecificEpithet() {
        return infraspecificEpithet;
    }

    public void setInfraspecificEpithet(String infraspecificEpithet) {
        this.infraspecificEpithet = infraspecificEpithet;
    }

    public Integer getReferenceID() {
        return reference == null ? referenceID : reference.getID();
    }

    public void setReferenceID(Integer referenceID) {
        if (reference == null) {
            this.referenceID = referenceID;
        } else {
            LOG.error("Attempted to set referenceID to " + referenceID + " when name associated with reference " + reference);
        }
        this.referenceID = referenceID;
    }

    public CoLDPReference getReference() {
        return reference;
    }

    public void setReference(CoLDPReference reference) {
        if (this.reference != null) {
            this.reference.deregisterName(this);
        }
        this.reference = reference;
        referenceID = null;
        if (reference != null) {
            reference.registerName(this);
        }
    }

    public String getPublishedInPage() {
        return publishedInPage;
    }

    public void setPublishedInPage(String publishedInPage) {
        this.publishedInPage = publishedInPage;
    }

    public String getPublishedInYear() {
        return publishedInYear;
    }

    public void setPublishedInYear(String publishedInYear) {
        this.publishedInYear = publishedInYear;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Set<CoLDPNameReference> getNameReferences() {
        return nameReferences;
    }

    void registerNameReference(CoLDPNameReference nameReference) {
        if (nameReference != null) {
            if (nameReferences == null) {
                nameReferences = new HashSet<>();
            }
            nameReferences.add(nameReference);
        }
    }
 
    void deregisterNameReference(CoLDPNameReference nameReference) {
        if (nameReference != null && nameReferences != null) {
            nameReferences.remove(nameReference);
        }
    }
 
    public Set<CoLDPNameRelation> getNameRelations() {
        return nameRelations;
    }

    void registerNameRelation(CoLDPNameRelation nameRelation) {
        if (nameRelation != null) {
            if (nameRelations == null) {
                nameRelations = new HashSet<>();
            }
            nameRelations.add(nameRelation);
        }
    }
 
    void deregisterNameRelation(CoLDPNameRelation nameRelation) {
        if (nameRelation != null && nameRelations != null) {
            nameRelations.remove(nameRelation);
        }
    }
 
    public Set<CoLDPNameRelation> getRelatedNameRelations() {
        return relatedNameRelations;
    }

    void registerRelatedNameRelation(CoLDPNameRelation nameRelation) {
        if (nameRelation != null) {
            if (relatedNameRelations == null) {
                relatedNameRelations = new HashSet<>();
            }
            relatedNameRelations.add(nameRelation);
        }
    }
 
    void deregisterRelatedNameRelation(CoLDPNameRelation nameRelation) {
        if (nameRelation != null && relatedNameRelations != null) {
            relatedNameRelations.remove(nameRelation);
        }
    }

    public CoLDPTaxon getTaxon() {
        return taxon;
    }

    public void setTaxon(CoLDPTaxon taxon) {
        if (this.taxon != null) {
            LOG.error("Attempted to set taxon to " + taxon + " when name associated with taxon " + this.taxon);
        } else {
            this.taxon = taxon;
        }
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
    
    @Override
    public String toString() {
        return "CoLDP_Name{" + "ID=" + ID + ", basionymID=" + getBasionymID() + ", scientificName=" + scientificName + ", authorship=" + authorship + ", rank=" + rank + ", uninomial=" + uninomial + ", genus=" + genus + ", specificEpithet=" + specificEpithet + ", infraspecificEpithet=" + infraspecificEpithet + ", referenceID=" + getReferenceID() + ", publishedInPage=" + publishedInPage + ", publishedInYear=" + publishedInYear + ", code=" + code + ", status=" + status + ", remarks=" + remarks + ", link=" + link + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.ID);
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
        final CoLDPName other = (CoLDPName) obj;
        if (!Objects.equals(this.ID, other.ID)) {
            return false;
        }
        return true;
    }
    
    public static String getCsvHeader() {
        return "ID,basionymID,scientificName,authorship,rank,uninomial,genus,"
                + "specificEpithet,infraspecificEpithet,referenceID,"
                + "publishedInPage,publishedInYear,code,status,remarks,link"; 
    }
    
    public String toCsv() {
        return StringUtils.toCsv(StringUtils.safeString(ID),
                                 StringUtils.safeString(getBasionymID()),
                                 scientificName,
                                 authorship,
                                 rank,
                                 uninomial,
                                 genus,
                                 specificEpithet,
                                 infraspecificEpithet,
                                 StringUtils.safeString(getReferenceID()),
                                 publishedInPage,
                                 publishedInYear,
                                 code,
                                 status,
                                 remarks,
                                 link);
    }
}
