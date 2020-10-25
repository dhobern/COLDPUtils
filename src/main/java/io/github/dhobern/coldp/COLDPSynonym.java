/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.dhobern.coldp;

import io.github.dhobern.coldp.TreeRenderProperties.ContextType;
import io.github.dhobern.coldp.TreeRenderProperties.TreeRenderType;
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
  
    private String taxonID;
    private String nameID; 
    private String status;
    private String referenceID; 
    private String remarks;
    
    private COLDPTaxon taxon;
    private COLDPName name;
    private COLDPReference reference;

    public COLDPSynonym() {
    }

    public String getTaxonID() {
        return taxon == null ? taxonID : taxon.getID();
    }

    public void setTaxonID(String taxonID) {
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
        if (!Objects.equals(this.taxon, taxon)) {
            if (this.taxon != null) {
                taxon.deregisterSynonym(this);
            }
            taxonID = null;
            this.taxon = taxon;
            if (taxon != null) {
                taxon.registerSynonym(this);
            }
        }
    }

    public String getNameID() {
        return name == null ? nameID : name.getID();
    }

    public void setNameID(String nameID) {
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
        if (!Objects.equals(this.name, name)) {
            if (this.name != null) {
                name.deregisterSynonym(this);
            }
            this.name = name;
            nameID = null;
            if (name != null) {
                name.registerSynonym(this);
            }
        }
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReferenceID() {
        return reference == null ? referenceID : reference.getID();
    }

    public void setReferenceID(String referenceID) {
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
        if (!Objects.equals(this.reference, reference)) {
            if (this.reference != null) {
                reference.deregisterSynonym(this);
            }
            this.reference = reference;
            referenceID = null;
            if (reference != null) {
                reference.registerSynonym(this);
            }
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
        return "[" + taxon.toString() + "] has synonym [" + name.toString() + "]";
    }

    public String getSortString() {
        String s;
        if (reference != null) {
            s = reference.getYear();
        } else if (name.getPublishedInYear() != null) {
            s = name.getPublishedInYear();
        } else {
            s = "9999";
        }
        return s + name.getSpecificEpithet();
    }

    @Override
    public int compareTo(COLDPSynonym o) {
        return this.toString().compareTo(o.toString());
    }

    public static String getCsvHeader() {
        return "taxonID,nameID,status,referenceID,remarks"; 
    }
    
    public String toCsv() {
        return buildCSV(getTaxonID(), getNameID(), status,
                        getReferenceID(), remarks);
    }

    @Override
    public void render(PrintWriter writer, TreeRenderProperties context) {
        TreeRenderType renderType = context.getTreeRenderType();
        writer.println(context.getIndent() + renderType.openNode("Synonym"));

        if (reference != null) {
            context.addReference(reference);
        }
        name.render(writer, new TreeRenderProperties(context, this, ContextType.Synonym));

        String closeNode = renderType.closeNode();
        if (closeNode.length() > 0) {
            writer.println(context.getIndent() + closeNode);
        }
    }
}
