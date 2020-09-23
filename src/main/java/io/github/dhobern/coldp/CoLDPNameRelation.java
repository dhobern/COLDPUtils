/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.dhobern.coldp;

import io.github.dhobern.utils.StringUtils;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author stang
 */
public class CoLDPNameRelation implements Comparable<CoLDPNameRelation> {
    
    private static final Logger LOG = LoggerFactory.getLogger(CoLDPNameRelation.class);
      
    private Integer nameID;
    private Integer relatedNameID;
    private String type;
    private Integer referenceID;
    private String remarks;
    
    private CoLDPName name;
    private CoLDPName relatedName;
    private CoLDPReference reference;

    public CoLDPNameRelation() {
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
        if (this.name != null) {
            this.name.deregisterNameRelation(this);
        }
        this.name = name;
        nameID = null;
        if (name != null) {
            name.registerNameRelation(this);
        }
    }

    public Integer getRelatedNameID() {
        return relatedName == null ? relatedNameID :  relatedName.getID();
    }

    public void setRelatedNameID(Integer relatedNameID) {
        if (relatedName == null) {
            this.relatedNameID = relatedNameID;
        } else {
            LOG.error("Attempted to set relatedNameID to " + relatedNameID + " when nameRelation associated with relatedName " + relatedName);
        }
    }

    public CoLDPName getRelatedName() {
        return relatedName;
    }

    public void setRelatedName(CoLDPName relatedName) {
        if (this.relatedName != null) {
            this.relatedName.deregisterRelatedNameRelation(this);
        }
        this.relatedName = relatedName;
        relatedNameID = null;
        if (relatedName != null) {
            relatedName.registerRelatedNameRelation(this);
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getReferenceID() {
        return reference == null ? referenceID : reference.getID();
    }

    public void setReferenceID(Integer referenceID) {
        if (reference == null) {
            this.referenceID = referenceID;
        } else {
            LOG.error("Attempted to set referenceID to " + referenceID + " when nameRelation associated with reference " + reference);
        }
    }

    public CoLDPReference getReference() {
        return reference;
    }

    public void setReference(CoLDPReference reference) {
        if (this.reference != null) {
            this.reference.deregisterNameRelation(this);
        }
        this.reference = reference;
        referenceID = null;
        if (reference != null) {
            reference.registerNameRelation(this);
        }
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
        hash = 89 * hash + Objects.hashCode(this.getNameID());
        hash = 89 * hash + Objects.hashCode(this.getRelatedNameID());
        hash = 89 * hash + Objects.hashCode(this.type);
        hash = 89 * hash + Objects.hashCode(this.getReferenceID());
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
        if (!Objects.equals(this.getNameID(), other.getNameID())) {
            return false;
        }
        if (!Objects.equals(this.getRelatedNameID(), other.getRelatedNameID())) {
            return false;
        }
        if (!Objects.equals(this.getReferenceID(), other.getReferenceID())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "CoLDPNameRelation{" + "nameID=" + getNameID() + ", relatedNameID=" + getRelatedNameID() + ", type=" + type + ", referenceID=" + getReferenceID() + ", remarks=" + remarks + '}';
    }

    @Override
    public int compareTo(CoLDPNameRelation o) {
        return this.toString().compareTo(o.toString());
    }

    public static String getCsvHeader() {
        return "nameID,relatedNameID,type,referenceID,remarks"; 
    }
    
    public String toCsv() {
        return StringUtils.toCsv(StringUtils.safeString(getNameID()),
                                 StringUtils.safeString(getRelatedNameID()),
                                 type,
                                 StringUtils.safeString(getReferenceID()),
                                 remarks);
    }
}
