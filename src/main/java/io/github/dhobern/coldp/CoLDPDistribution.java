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
public class CoLDPDistribution implements Comparable<CoLDPDistribution> {
    
    private Integer taxonID;
    private String area;
    private String gazetteer;
    private String status;
    private String referenceID;

    public CoLDPDistribution() {
    }

    public Integer getTaxonID() {
        return taxonID;
    }

    public void setTaxonID(Integer taxonID) {
        this.taxonID = taxonID;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getGazetteer() {
        return gazetteer;
    }

    public void setGazetteer(String gazetteer) {
        this.gazetteer = gazetteer;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReferenceID() {
        return referenceID;
    }

    public void setReferenceID(String referenceID) {
        this.referenceID = referenceID;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + Objects.hashCode(this.taxonID);
        hash = 37 * hash + Objects.hashCode(this.area);
        hash = 37 * hash + Objects.hashCode(this.gazetteer);
        hash = 37 * hash + Objects.hashCode(this.status);
        hash = 37 * hash + Objects.hashCode(this.referenceID);
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
        final CoLDPDistribution other = (CoLDPDistribution) obj;
        if (!Objects.equals(this.area, other.area)) {
            return false;
        }
        if (!Objects.equals(this.gazetteer, other.gazetteer)) {
            return false;
        }
        if (!Objects.equals(this.status, other.status)) {
            return false;
        }
        if (!Objects.equals(this.referenceID, other.referenceID)) {
            return false;
        }
        if (!Objects.equals(this.taxonID, other.taxonID)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "CoLDPDistribution{" + "taxonID=" + taxonID + ", area=" + area + ", gazetteer=" + gazetteer + ", status=" + status + ", referenceID=" + referenceID + '}';
    }

    @Override
    public int compareTo(CoLDPDistribution o) {
        return this.toString().compareTo(o.toString());
    }
}
