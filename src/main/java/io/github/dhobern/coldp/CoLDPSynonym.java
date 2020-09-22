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
public class CoLDPSynonym implements Comparable<CoLDPSynonym> {
    
    private Integer taxonID;
    private Integer nameID; 
    private String status;
    private Integer referenceID; 
    private String remarks;

    public CoLDPSynonym() {
    }

    public Integer getTaxonID() {
        return taxonID;
    }

    public void setTaxonID(Integer taxonID) {
        this.taxonID = taxonID;
    }

    public Integer getNameID() {
        return nameID;
    }

    public void setNameID(Integer nameID) {
        this.nameID = nameID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getReferenceID() {
        return referenceID;
    }

    public void setReferenceID(Integer referenceID) {
        this.referenceID = referenceID;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.taxonID);
        hash = 59 * hash + Objects.hashCode(this.nameID);
        hash = 59 * hash + Objects.hashCode(this.status);
        hash = 59 * hash + Objects.hashCode(this.referenceID);
        hash = 59 * hash + Objects.hashCode(this.remarks);
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
        final CoLDPSynonym other = (CoLDPSynonym) obj;
        if (!Objects.equals(this.status, other.status)) {
            return false;
        }
        if (!Objects.equals(this.taxonID, other.taxonID)) {
            return false;
        }
        if (!Objects.equals(this.nameID, other.nameID)) {
            return false;
        }
        if (!Objects.equals(this.referenceID, other.referenceID)) {
            return false;
        }
        if (!Objects.equals(this.remarks, other.remarks)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "CoLDPSynonym{" + "taxonID=" + taxonID + ", nameID=" + nameID 
                + ", status=" + status + ", referenceID=" + referenceID 
                + ", remarks=" + remarks + '}';
    }

    @Override
    public int compareTo(CoLDPSynonym o) {
        return this.toString().compareTo(o.toString());
    }
}
