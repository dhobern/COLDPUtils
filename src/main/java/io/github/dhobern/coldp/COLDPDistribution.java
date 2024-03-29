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
import java.util.Comparator;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author stang
 */
public class COLDPDistribution implements Comparable<COLDPDistribution>, TreeRenderable {
    
    private static final Logger LOG = LoggerFactory.getLogger(COLDPDistribution.class);
     
    private String taxonID;
    private String area;
    private String gazetteer;
    private String status;
    private String referenceID;
    private String remarks;
    
    private COLDPTaxon taxon;
    private COLDPRegion region;
    private COLDPReference reference;

    public COLDPDistribution() {
    }

    public String getTaxonID() {
        return taxon == null ? taxonID : taxon.getID();
    }

    public void setTaxonID(String taxonID) {
        if (taxon == null) {
            this.taxonID = taxonID;
        } else {
            LOG.error("Attempted to set taxonID to " + taxonID + " when distribution associated with taxon " + taxon);
        }
    }

    public COLDPTaxon getTaxon() {
        return taxon;
    }

    public void setTaxon(COLDPTaxon taxon) {
        if (!Objects.equals(this.taxon, taxon)) {
            if (this.taxon != null) {
                this.taxon.deregisterDistribution(this);
            }
            this.taxon = taxon;
            taxonID = null;
            
            // Defer registration until we have both the region and the taxon
            // Both are needed for the insertion into the TreeSet.
            handleRegistrations();
        }
    }
    
    private void handleRegistrations() {
        if (taxon != null && region != null) {
            taxon.registerDistribution(this);
            region.registerDistribution(this);
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

    public COLDPRegion getRegion() {
        return region;
    }

    public void setRegion(COLDPRegion region) {
        if (!Objects.equals(this.region, region)) {
            if (this.region != null) {
                this.region.deregisterDistribution(this);
            }
            this.region = region;
            area = null;
            
            // Defer registration until we have both the region and the taxon
            // Both are needed for the insertion into the TreeSet.
            handleRegistrations();
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

    public String getReferenceID() {
        return reference == null ? referenceID : reference.getID();
    }

    public void setReferenceID(String referenceID) {
        if (reference == null) {
            this.referenceID = referenceID;
        } else {
            LOG.error("Attempted to set referenceID to " + referenceID + " when distribution associated with reference " + reference);
        }
    }

    public COLDPReference getReference() {
        return reference;
    }

    public void setReference(COLDPReference reference) {
        if (!Objects.equals(this.reference, reference)) {
            if (this.reference != null) {
                this.reference.deregisterDistribution(this);
            }
            this.reference = reference;
            referenceID = null;
            if (reference != null) {
                reference.registerDistribution(this);
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
        final COLDPDistribution other = (COLDPDistribution) obj;
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
        return "[" + (taxon == null ? "" : taxon.toString()) + "] " + status + " in [" + (region == null ? "" : region.toString()) + "]"
                + (reference == null ? "" : " from [" + reference.toString(15, 0) + "]");
    }

    @Override
    public int compareTo(COLDPDistribution o) {
        return this.toString().compareTo(o.toString());
    }

    public static String getCsvHeader() {
        return "taxonId,area,gazetteer,status,referenceID,remarks"; 
    }
    
    public String toCsv() {
        return buildCSV(getTaxonID(), getArea(), gazetteer, status,
                        getReferenceID(), remarks);
    }

    @Override
    public void render(PrintWriter writer, TreeRenderProperties context) {
        TreeRenderType renderType = context.getTreeRenderType();
        String note = null;
        if (reference != null) {
            context.addReference(reference);
            note =  reference.getAuthor();
            if (reference.getIssued() != null) {
                note += " " + reference.getIssued();
            }
        }

        if (remarks != null && remarks.length() > 0) {
            if (note == null) {
                note = remarks;
            } else {
                note = note + ": " + remarks;
            }
        }

        String formatted;
        String nodeClass;
        String nodeID;
        if (context.getCurrentRegion() != null) {
            formatted = COLDPName.formatName(taxon.getName(), renderType);
            nodeClass = upperFirst(taxon.getName().getRank());
            nodeID = taxon.getID();
        } else {
            formatted = (region == null) ? area : region.getName();
            nodeClass = "Region";
            nodeID = null;
        }

        if (status != null && !status.equals("native")) {
            if (note != null && !note.equalsIgnoreCase(status)) {
                formatted += " (" + status + ", " + note + ")";
            } else {
                formatted += " (" + status + ")";
            }
        } else if (note != null) {
            formatted += " (" +  note + ")";
        }

        writer.println(context.getIndent() + renderType.openNodeWithID(nodeClass, nodeID) + formatted + renderType.closeNode());
    }
    public static class RegionSort implements Comparator<COLDPDistribution> { 
        @Override
        public int compare(COLDPDistribution o1, COLDPDistribution o2) {
            int comparison;
            if (o1.region != null && o1.region.getName() != null 
                    && o2.region != null && o2.region.getName() != null) {
                comparison = o1.region.getName().compareTo(o2.region.getName());
            } else {
                comparison = o1.compareTo(o2);
            }
            return comparison;
        }
    }  

    public static class TaxonSort implements Comparator<COLDPDistribution> { 
        @Override
        public int compare(COLDPDistribution o1, COLDPDistribution o2) {
            int comparison;
            if (o1.taxon != null && o1.taxon.getName() != null 
                    && o2.taxon != null && o2.taxon.getName() != null) {
                comparison = o1.taxon.getName().getScientificName().compareTo(o2.taxon.getName().getScientificName());
            } else {
                comparison = o1.compareTo(o2);
            }
            return comparison;
        }
    }  
}
