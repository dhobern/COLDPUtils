/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.dhobern.coldp;

import java.util.Objects;

/**
 *
 * @author stang
 */
public class CoLDPName {
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

    public CoLDPName() {
    }

    public Integer getID() {
        return ID;
    }

    public void setID(Integer ID) {
        this.ID = ID;
    }

    public Integer getBasionymID() {
        return basionymID;
    }

    public void setBasionymID(Integer basionymID) {
        this.basionymID = basionymID;
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
        return referenceID;
    }

    public void setReferenceID(Integer referenceID) {
        this.referenceID = referenceID;
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

    @Override
    public String toString() {
        return "CoLDP_Name{" + "ID=" + ID + ", basionymID=" + basionymID + ", scientificName=" + scientificName + ", authorship=" + authorship + ", rank=" + rank + ", uninomial=" + uninomial + ", genus=" + genus + ", specificEpithet=" + specificEpithet + ", infraspecificEpithet=" + infraspecificEpithet + ", referenceID=" + referenceID + ", publishedInPage=" + publishedInPage + ", publishedInYear=" + publishedInYear + ", code=" + code + ", status=" + status + ", remarks=" + remarks + ", link=" + link + '}';
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
        
}
