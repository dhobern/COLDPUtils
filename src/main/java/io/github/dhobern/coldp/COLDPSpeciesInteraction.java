/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.dhobern.coldp;

import io.github.dhobern.col.COLClient;
import io.github.dhobern.col.Classification;
import io.github.dhobern.col.NameUsageSearchResponse;
import io.github.dhobern.col.NameUsageSearchResult;
import io.github.dhobern.coldp.TreeRenderProperties.ContextType;
import io.github.dhobern.coldp.TreeRenderProperties.TreeRenderType;
import static io.github.dhobern.utils.StringUtils.*;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author stang
 */
public class COLDPSpeciesInteraction implements Comparable<COLDPSpeciesInteraction>, TreeRenderable {
    
    private static final Logger LOG = LoggerFactory.getLogger(COLDPSpeciesInteraction.class);
     
    private String taxonID;
    private String relatedTaxonID;
    private String relatedTaxonScientificName;
    private String type;
    private String referenceID;
    private String remarks;
    private String relatedTaxonLink;
    private String relatedTaxonOrder;
    private String relatedTaxonFamily;
    private String relatedTaxonFullName;
    private String relatedTaxonHTMLName;
    
    private COLDPTaxon taxon;
    private COLDPTaxon relatedTaxon;
    private COLDPReference reference;

    public COLDPSpeciesInteraction() {
    }

    public String getTaxonID() {
        return taxon == null ? taxonID : taxon.getID();
    }

    public void setTaxonID(String taxonID) {
        if (taxon == null) {
            this.taxonID = taxonID;
        } else {
            LOG.error("Attempted to set taxonID to " + taxonID + " when speciesInteraction associated with taxon " + taxon);
        }
    }

    public COLDPTaxon getTaxon() {
        return taxon;
    }

    public void setTaxon(COLDPTaxon taxon) {
        if (!Objects.equals(this.taxon, taxon)) {
            if (this.taxon != null) {
                taxon.deregisterSpeciesInteraction(this);
            }
            this.taxon = taxon;
            taxonID = null;
            if (taxon != null) {
                taxon.registerSpeciesInteraction(this);
            }
        }
    }

    public String getRelatedTaxonID() {
        return relatedTaxon == null ? relatedTaxonID : relatedTaxon.getID();
    }

    public void setRelatedTaxonID(String relatedTaxonID) {
        if (relatedTaxon == null) {
            this.relatedTaxonID = relatedTaxonID;
        } else {
            LOG.error("Attempted to set relatedTaxonID to " + relatedTaxonID + " when speciesInteraction associated with relatedTaxon " + relatedTaxon);
        }
    }

    public COLDPTaxon getRelatedTaxon() {
        return relatedTaxon;
    }

    public void setRelatedTaxon(COLDPTaxon relatedTaxon) {
        if (!Objects.equals(this.relatedTaxon, relatedTaxon)) {
            if (this.relatedTaxon != null) {
                relatedTaxon.deregisterRelatedSpeciesInteraction(this);
            }
            this.relatedTaxon = relatedTaxon;
            relatedTaxonID = null;
            if (relatedTaxon != null) {
                relatedTaxon.registerRelatedSpeciesInteraction(this);
            }
        }
    }

    public String getRelatedTaxonScientificName() {
        return relatedTaxonScientificName;
    }

    public void setRelatedTaxonScientificName(String relatedTaxonScientificName) {
        this.relatedTaxonScientificName = relatedTaxonScientificName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRelatedTaxonLink() {
        return relatedTaxonLink;
    }

    public void setRelatedTaxonLink(String relatedTaxonLink) {
        this.relatedTaxonLink = relatedTaxonLink;
    }

    public String getRelatedTaxonOrder() {
        return relatedTaxonOrder;
    }

    public void setRelatedTaxonOrder(String relatedTaxonOrder) {
        this.relatedTaxonOrder = relatedTaxonOrder;
    }

    public String getRelatedTaxonFamily() {
        return relatedTaxonFamily;
    }

    public void setRelatedTaxonFamily(String relatedTaxonFamily) {
        this.relatedTaxonFamily = relatedTaxonFamily;
    }

    public String getRelatedTaxonFullName() {
        return relatedTaxonFullName;
    }

    public void setRelatedTaxonFullName(String relatedTaxonFullName) {
        this.relatedTaxonFullName = relatedTaxonFullName;
    }

    public String getRelatedTaxonHTMLName() {
        return relatedTaxonHTMLName;
    }

    public void setRelatedTaxonHTMLName(String relatedTaxonHTMLName) {
        this.relatedTaxonHTMLName = relatedTaxonHTMLName;
    }

    public String getReferenceID() {
        return reference == null ? referenceID : reference.getID();
    }

    public void setReferenceID(String referenceID) {
        if (reference == null) {
            this.referenceID = referenceID;
        } else {
            LOG.error("Attempted to set referenceID to " + referenceID + " when speciesInteraction associated with reference " + reference);
        }
    }

    public COLDPReference getReference() {
        return reference;
    }

    public void setReference(COLDPReference reference) {
        if (!Objects.equals(this.reference, reference)) {
            if (this.reference != null) {
                this.reference.deregisterSpeciesInteraction(this);
            }
            this.reference = reference;
            referenceID = null;
            if (reference != null) {
                reference.registerSpeciesInteraction(this);
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
        hash = 37 * hash + Objects.hashCode(this.relatedTaxonID);
        hash = 37 * hash + Objects.hashCode(this.relatedTaxonScientificName);
        hash = 37 * hash + Objects.hashCode(this.type);
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
        final COLDPSpeciesInteraction other = (COLDPSpeciesInteraction) obj;
        if (!Objects.equals(this.getReferenceID(), other.getReferenceID())) {
            return false;
        }
        if (!Objects.equals(this.getRelatedTaxonID(), other.getRelatedTaxonID())) {
            return false;
        }
        if (!Objects.equals(this.getType(), other.getType())) {
            return false;
        }
        if (!Objects.equals(this.getRelatedTaxonScientificName(), other.getRelatedTaxonScientificName())) {
            return false;
        }
        if (!Objects.equals(this.getTaxonID(), other.getTaxonID())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "[" + taxon.toString() + "] " + type + " [" + (relatedTaxon == null ? relatedTaxonScientificName :  relatedTaxon.toString()) + "]"
                + (reference == null ? "" : " from [" + reference.toString(15, 0) + "]");
    }

    @Override
    public int compareTo(COLDPSpeciesInteraction o) {
        return this.toString().compareTo(o.toString());
    }

    public static String getCsvHeader() {
        return "taxonID,relatedTaxonID,relatedTaxonScientificName,type,referenceID,remarks,relatedTaxonLink,relatedTaxonOrder,relatedTaxonFamily,relatedTaxonFullName,relatedTaxonHTMLName"; 
    }
    
    public String toCsv() {
        return buildCSV(getTaxonID(), getRelatedTaxonID(), relatedTaxonScientificName, type,
                        getReferenceID(), remarks, relatedTaxonLink,relatedTaxonOrder,
                        relatedTaxonFamily, relatedTaxonFullName, relatedTaxonHTMLName);
    }

    @Override
    public void render(PrintWriter writer, TreeRenderProperties context) {
        TreeRenderType renderType = context.getTreeRenderType();
        String note = null;
        if (reference != null) {
            context.addReference(reference);
            note =  reference.getAuthor();
            if (reference.getYear() != null) {
                note += " " + reference.getYear();
            }
        }

        if (remarks != null) {
            if (note == null) {
                note = remarks;
            } else {
                note = note + ": " + remarks;
            }
        }

        String formatted;
        if (relatedTaxon == null) {
            if (renderType.equals(TreeRenderType.HTML)) {
                if (relatedTaxonHTMLName != null) {
                    formatted = relatedTaxonHTMLName;
                } else if (relatedTaxonScientificName != null) {
                    formatted = renderType.wrapEmphasis(relatedTaxonScientificName);
                } else {
                    formatted = "<Unknown>";
                }
            } else {
                if (relatedTaxonFullName != null) {
                    formatted = relatedTaxonFullName;
                } else {
                    formatted = relatedTaxonScientificName;
                }
            }
        } else {
            formatted = COLDPName.formatName(relatedTaxon.getName(), renderType);
        }

        if (relatedTaxonOrder != null) {
            formatted += " (" + relatedTaxonOrder + (relatedTaxonFamily == null ? "" : ": " + relatedTaxonFamily) + ")";
        } else if (relatedTaxonFamily != null) {
            formatted += " (" + relatedTaxonFamily + ")";
        }
        
        if (relatedTaxonLink != null && relatedTaxonLink.startsWith("http")) {
            if (renderType.equals(TreeRenderType.HTML)) {
                formatted += " <a href=\"" + relatedTaxonLink + "\" target=\"_blank\"><i class=\"fas fa-external-link-alt fa-sm\"></i></a>";
            } else {
                formatted += " " + relatedTaxonLink;
            }
        }
        
        if (type != null) {
            if (note != null) {
                formatted = upperFirst(type) + " (" +  note + "): " + formatted;
            } else {
                formatted = upperFirst(type) + ": " + formatted;
            }
        }
        
        writer.println(context.getIndent() + renderType.openNode("SpeciesInteraction") + formatted + renderType.closeNode());
    }
    
    public boolean linkToCOL() {
        boolean success = false;

        if (relatedTaxonScientificName != null) {
            COLClient client = new COLClient();
            NameUsageSearchResponse response = client.searchForNameUsage(relatedTaxonScientificName);
            if (response != null && response.getResult() != null) {
                List<NameUsageSearchResult> usages 
                        = response.getResult().stream().filter(u 
                                -> u.getUsage().getName().getScientificName()
                                        .equals(relatedTaxonScientificName))
                            .collect(Collectors.toList());
                if (usages.size() == 1) {
                    NameUsageSearchResult result = response.getResult().get(0);
                    if (result.getUsage().getAccepted() != null) {
                        relatedTaxonLink = client.getWebUrlForTaxonKey(result.getUsage().getAccepted().getId());
                        relatedTaxonFullName = result.getUsage().getAccepted().getLabel();
                        relatedTaxonHTMLName = result.getUsage().getAccepted().getLabelHtml();
                    } else {
                        relatedTaxonLink = client.getWebUrlForTaxonKey(result.getId());
                        relatedTaxonFullName = result.getUsage().getLabel();
                        relatedTaxonHTMLName = result.getUsage().getLabelHtml();
                    }
                    if (result.getClassification() != null) {
                        for (Classification classification : result.getClassification()) {
                            switch (classification.getRank()) {
                                case "order": relatedTaxonOrder = classification.getName(); break;
                                case "family": relatedTaxonFamily = classification.getName(); break;
                            }
                        }
                    }
                } else {
                    LOG.error("COL search for " + relatedTaxonScientificName + " returned " + response.getResult().size() + " matches");
                }
            } else {
                LOG.error("COL search for " + relatedTaxonScientificName + " failed");
            }
        }
        return success;
    }
}
