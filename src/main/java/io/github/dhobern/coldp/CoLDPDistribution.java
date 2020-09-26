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
public class CoLDPDistribution implements Comparable<CoLDPDistribution>, TreeRenderable {
    
    private static final Logger LOG = LoggerFactory.getLogger(CoLDPDistribution.class);
     
    private Integer taxonID;
    private String area;
    private String gazetteer;
    private String status;
    private Integer referenceID;
    private String remarks;
    
    private CoLDPTaxon taxon;
    private CoLDPRegion region;
    private CoLDPReference reference;

    public CoLDPDistribution() {
    }

    public Integer getTaxonID() {
        return taxon == null ? taxonID : taxon.getID();
    }

    public void setTaxonID(Integer taxonID) {
        if (taxon == null) {
            this.taxonID = taxonID;
        } else {
            LOG.error("Attempted to set taxonID to " + taxonID + " when distribution associated with taxon " + taxon);
        }
    }

    public CoLDPTaxon getTaxon() {
        return taxon;
    }

    public void setTaxon(CoLDPTaxon taxon) {
        if (this.taxon != null) {
            taxon.deregisterDistribution(this);
        }
        this.taxon = taxon;
        taxonID = null;
        if (taxon != null) {
            taxon.registerDistribution(this);
        }
    }

    public String getArea() {
        return region == null ? area : region.getID();
    }

    public void setArea(String area) {
        if (region == null) {
            this.area = area;
        } else {
            LOG.error("Attempted to set area to " + area + " when distribution associated with region " + region);
        }
    }

    public CoLDPRegion getRegion() {
        return region;
    }

    public void setRegion(CoLDPRegion region) {
        if (this.region != null) {
            taxon.deregisterDistribution(this);
        }
        this.region = region;
        area = null;
        if (region != null) {
            region.registerDistribution(this);
        }
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

    public Integer getReferenceID() {
        return reference == null ? referenceID : reference.getID();
    }

    public void setReferenceID(Integer referenceID) {
        if (reference == null) {
            this.referenceID = referenceID;
        } else {
            LOG.error("Attempted to set referenceID to " + referenceID + " when distribution associated with reference " + reference);
        }
    }

    public CoLDPReference getReference() {
        return reference;
    }

    public void setReference(CoLDPReference reference) {
        if (this.reference != null) {
            this.reference.deregisterDistribution(this);
        }
        this.reference = reference;
        referenceID = null;
        if (reference != null) {
            reference.registerDistribution(this);
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
        if (!Objects.equals(this.getArea(), other.getArea())) {
            return false;
        }
        if (!Objects.equals(this.gazetteer, other.gazetteer)) {
            return false;
        }
        if (!Objects.equals(this.status, other.status)) {
            return false;
        }
        if (!Objects.equals(this.getReferenceID(), other.getReferenceID())) {
            return false;
        }
        if (!Objects.equals(this.getTaxonID(), other.getTaxonID())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "CoLDPDistribution{" + "taxonID=" + getTaxonID() + ", area=" + getArea() + ", gazetteer=" + gazetteer + ", status=" + status + ", referenceID=" + getReferenceID() + ", remarks=" + remarks + "}";
    }

    @Override
    public int compareTo(CoLDPDistribution o) {
        return this.toString().compareTo(o.toString());
    }

    public static String getCsvHeader() {
        return "taxonId,area,gazetteer,status,referenceID,remarks"; 
    }
    
    public String toCsv() {
        return buildCSV(safeString(getTaxonID()), getArea(), gazetteer, status,
                        safeString(referenceID), remarks);
    }

    @Override
    public void render(PrintWriter writer, TreeRenderProperties context) {
        if (context.getTreeRenderType() == TreeRenderProperties.TreeRenderType.HTML) {
            String note = null;
            if (reference != null) {
                context.addReference(reference);
                remarks =  reference.getAuthor();
                if (reference.getYear() != null) {
                    remarks += " " + reference.getYear();
                }
            }
            
            if (remarks != null) {
                if (note == null) {
                    note = linkURLs(remarks);
                } else {
                    note += ": " + linkURLs(remarks);
                }
            }
            
            String formatted = (region == null) ? area : region.getName();
            if (status != null) {
                formatted += " (" + status + ")";
            }

            if (note == null) {
                writer.println(context.getIndent() + wrapDiv("Region", formatted));
            } else {
                writer.println(context.getIndent() + "<div class=\"Region\">");
                renderNote(writer, new TreeRenderProperties(context, this, ContextType.Distribution), note);
                writer.println(context.getIndent() + "</div>");
        }
        }
    }
    
    private void renderNote(PrintWriter writer, TreeRenderProperties context, String note) {
        if (context.getTreeRenderType() == TreeRenderProperties.TreeRenderType.HTML) {
            writer.println(context.getIndent() + wrapDiv("Note", "Note: " + note));
        }
    }    
}
