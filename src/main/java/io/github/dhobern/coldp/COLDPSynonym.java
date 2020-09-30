/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.dhobern.coldp;

import io.github.dhobern.coldp.TreeRenderProperties.ContextType;
import static io.github.dhobern.utils.StringUtils.*;
import java.io.PrintWriter;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author stang
 */
public class COLDPSynonym implements Comparable<COLDPSynonym>, TreeRenderable {
    
    private static final Logger LOG = LoggerFactory.getLogger(COLDPSynonym.class);
  
    private Integer taxonID;
    private Integer nameID; 
    private String status;
    private Integer referenceID; 
    private String remarks;
    
    private COLDPTaxon taxon;
    private COLDPName name;
    private COLDPReference reference;

    public COLDPSynonym() {
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

    public COLDPTaxon getTaxon() {
        return taxon;
    }

    public void setTaxon(COLDPTaxon taxon) {
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

    public COLDPName getName() {
        return name;
    }

    public void setName(COLDPName name) {
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

    public COLDPReference getReference() {
        return reference;
    }

    public void setReference(COLDPReference reference) {
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
        final COLDPSynonym other = (COLDPSynonym) obj;
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
    public int compareTo(COLDPSynonym o) {
        return this.toString().compareTo(o.toString());
    }

    public static String getCsvHeader() {
        return "taxonID,nameID,status,referenceID,remarks"; 
    }
    
    public String toCsv() {
        return buildCSV(safeString(getTaxonID()),
                        safeString(getNameID()),
                        status,
                        safeString(getReferenceID()),
                        remarks);
    }

    @Override
    public void render(PrintWriter writer, TreeRenderProperties context) {
        if (context.getTreeRenderType() == TreeRenderProperties.TreeRenderType.HTML) {
            writer.println(context.getIndent() + "<div class=\"Synonym\">");

            if (reference != null) {
                context.addReference(reference);
            }
            name.render(writer, new TreeRenderProperties(context, this, ContextType.Synonym));

            writer.println(context.getIndent() + "</div>");
        }
    }    
}