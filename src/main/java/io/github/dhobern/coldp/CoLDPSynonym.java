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
public class CoLDPSynonym implements Comparable<CoLDPSynonym> {
    
    private static final Logger LOG = LoggerFactory.getLogger(CoLDPSynonym.class);
  
    private Integer taxonID;
    private Integer nameID; 
    private String status;
    private Integer referenceID; 
    private String remarks;
    
    private CoLDPTaxon taxon;
    private CoLDPName name;
    private CoLDPReference reference;

    public CoLDPSynonym() {
    }

    public Integer getTaxonID() {
        return taxon == null ? taxonID : taxon.getID();
    }

    public void setTaxonID(Integer taxonID) {
        if (taxon == null) {
            this.taxonID = taxonID;
        } else {
            LOG.error("Attempted to set taxonID to " + taxonID + " when synonym associated with taxon " + taxon);
        }
    }

    public CoLDPTaxon getTaxon() {
        return taxon;
    }

    public void setTaxon(CoLDPTaxon taxon) {
        if (this.taxon != null) {
            taxon.deregisterSynonym(this);
        }
        taxonID = null;
        this.taxon = taxon;
        if (taxon != null) {
            taxon.registerSynonym(this);
        }
    }

    public Integer getNameID() {
        return name == null ? nameID : name.getID();
    }

    public void setNameID(Integer nameID) {
        if (name == null) {
            this.nameID = nameID;
        } else {
            LOG.error("Attempted to set nameID to " + nameID + " when synonym associated with name " + name);
        }
    }

    public CoLDPName getName() {
        return name;
    }

    public void setName(CoLDPName name) {
        if (this.name != null) {
            name.deregisterSynonym(this);
        }
        this.name = name;
        nameID = null;
        if (name != null) {
            name.registerSynonym(this);
        }
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getReferenceID() {
        return reference == null ? referenceID : reference.getID();
    }

    public void setReferenceID(Integer referenceID) {
        if (reference == null) {
            this.referenceID = referenceID;
        } else {
            LOG.error("Attempted to set referenceID to " + referenceID + " when synonym associated with reference " + reference);
        }
    }

    public CoLDPReference getReference() {
        return reference;
    }

    public void setReference(CoLDPReference reference) {
        if (this.reference != null) {
            reference.deregisterSynonym(this);
        }
        this.reference = reference;
        referenceID = null;
        if (reference != null) {
            reference.registerSynonym(this);
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
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.getTaxonID());
        hash = 59 * hash + Objects.hashCode(this.getNameID());
        hash = 59 * hash + Objects.hashCode(this.status);
        hash = 59 * hash + Objects.hashCode(this.getReferenceID());
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
        if (!Objects.equals(this.getTaxonID(), other.getTaxonID())) {
            return false;
        }
        if (!Objects.equals(this.getNameID(), other.getNameID())) {
            return false;
        }
        if (!Objects.equals(this.getReferenceID(), other.getReferenceID())) {
            return false;
        }
        if (!Objects.equals(this.remarks, other.remarks)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "CoLDPSynonym{" + "taxonID=" + getTaxonID() + ", nameID=" + getNameID()
                + ", status=" + status + ", referenceID=" + getReferenceID ()
                + ", remarks=" + remarks + '}';
    }

    @Override
    public int compareTo(CoLDPSynonym o) {
        return this.toString().compareTo(o.toString());
    }

    public static String getCsvHeader() {
        return "taxonID,nameID,status,referenceID,remarks"; 
    }
    
    public String toCsv() {
        return StringUtils.toCsv(StringUtils.safeString(getTaxonID()),
                                 StringUtils.safeString(getNameID()),
                                 status,
                                 StringUtils.safeString(getReferenceID()),
                                 remarks);
    }
}
