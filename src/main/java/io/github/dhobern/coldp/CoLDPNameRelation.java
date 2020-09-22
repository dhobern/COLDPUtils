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
public class CoLDPNameRelation implements Comparable<CoLDPNameRelation> {
    
    private Integer nameID;
    private Integer relatedNameID;
    private String type;
    private Integer referenceID;
    private String remarks;

    public CoLDPNameRelation() {
    }

    public Integer getNameID() {
        return nameID;
    }

    public void setNameID(Integer nameID) {
        this.nameID = nameID;
    }

    public Integer getRelatedNameID() {
        return relatedNameID;
    }

    public void setRelatedNameID(Integer relatedNameID) {
        this.relatedNameID = relatedNameID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
        int hash = 3;
        hash = 89 * hash + Objects.hashCode(this.nameID);
        hash = 89 * hash + Objects.hashCode(this.relatedNameID);
        hash = 89 * hash + Objects.hashCode(this.type);
        hash = 89 * hash + Objects.hashCode(this.referenceID);
        hash = 89 * hash + Objects.hashCode(this.remarks);
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
        final CoLDPNameRelation other = (CoLDPNameRelation) obj;
        if (!Objects.equals(this.type, other.type)) {
            return false;
        }
        if (!Objects.equals(this.remarks, other.remarks)) {
            return false;
        }
        if (!Objects.equals(this.nameID, other.nameID)) {
            return false;
        }
        if (!Objects.equals(this.relatedNameID, other.relatedNameID)) {
            return false;
        }
        if (!Objects.equals(this.referenceID, other.referenceID)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "CoLDPNameRelation{" + "nameID=" + nameID + ", relatedNameID=" + relatedNameID + ", type=" + type + ", referenceID=" + referenceID + ", remarks=" + remarks + '}';
    }

    @Override
    public int compareTo(CoLDPNameRelation o) {
        return this.toString().compareTo(o.toString());
    }
}
