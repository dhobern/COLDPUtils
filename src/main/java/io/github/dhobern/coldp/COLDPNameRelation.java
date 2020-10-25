/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.dhobern.coldp;

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
public class COLDPNameRelation implements Comparable<COLDPNameRelation>, TreeRenderable {
    
    private static final Logger LOG = LoggerFactory.getLogger(COLDPNameRelation.class);
      
    private String nameID;
    private String relatedNameID;
    private String type;
    private String referenceID;
    private String remarks;
    
    private COLDPName name;
    private COLDPName relatedName;
    private COLDPReference reference;

    public COLDPNameRelation() {
    }

    public String getNameID() {
        return name == null ? nameID : name.getID();
    }

    public void setNameID(String nameID) {
        if (name == null) {
            this.nameID = nameID;
        } else {
            LOG.error("Attempted to set nameID to " + nameID + " when nameRelation associated with name " + name);
        }
    }

    public COLDPName getName() {
        return name;
    }

    public void setName(COLDPName name) {
        if (!Objects.equals(this.name, name)) {
            if (this.name != null) {
                this.name.deregisterNameRelation(this);
            }
            this.name = name;
            nameID = null;
            if (name != null) {
                name.registerNameRelation(this);
            }
        }
    }

    public String getRelatedNameID() {
        return relatedName == null ? relatedNameID :  relatedName.getID();
    }

    public void setRelatedNameID(String relatedNameID) {
        if (relatedName == null) {
            this.relatedNameID = relatedNameID;
        } else {
            LOG.error("Attempted to set relatedNameID to " + relatedNameID + " when nameRelation associated with relatedName " + relatedName);
        }
    }

    public COLDPName getRelatedName() {
        return relatedName;
    }

    public void setRelatedName(COLDPName relatedName) {
        if (!Objects.equals(this.relatedName, relatedName)) {
            if (this.relatedName != null) {
                this.relatedName.deregisterRelatedNameRelation(this);
            }
            this.relatedName = relatedName;
            relatedNameID = null;
            if (relatedName != null) {
                relatedName.registerRelatedNameRelation(this);
            }
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getReferenceID() {
        return reference == null ? referenceID : reference.getID();
    }

    public void setReferenceID(String referenceID) {
        if (reference == null) {
            this.referenceID = referenceID;
        } else {
            LOG.error("Attempted to set referenceID to " + referenceID + " when nameRelation associated with reference " + reference);
        }
    }

    public COLDPReference getReference() {
        return reference;
    }

    public void setReference(COLDPReference reference) {
        if (!Objects.equals(this.reference, reference)) {
            if (this.reference != null) {
                this.reference.deregisterNameRelation(this);
            }
            this.reference = reference;
            referenceID = null;
            if (reference != null) {
                reference.registerNameRelation(this);
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
        final COLDPNameRelation other = (COLDPNameRelation) obj;
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
        return "[" + name.toString() + "] " 
                + NameRelationTypeEnum.getLabel(type, false, false) 
                + " [" + relatedName.toString() + "]";
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
        return s + relatedName.getSpecificEpithet() + name.getSpecificEpithet();
    }

    @Override
    public int compareTo(COLDPNameRelation o) {
        return this.getSortString().compareTo(o.getSortString());
    }

    public static String getCsvHeader() {
        return "nameID,relatedNameID,type,referenceID,remarks"; 
    }
    
    public String toCsv() {
        return buildCSV(getNameID(), getRelatedNameID(), type,
                        getReferenceID(), remarks);
    }

    @Override
    public void render(PrintWriter writer, TreeRenderProperties context) {
        TreeRenderType renderType = context.getTreeRenderType();
        COLDPName contextName = context.getCurrentName();
        COLDPTaxon contextTaxon = context.getCurrentTaxon();
        COLDPName taxonName = (contextTaxon == null) ? null : contextTaxon.getName();
        
        COLDPName nameToRender;
        String formatted;
        boolean continueRendering = true;
        
        if (name.equals(contextName)) {
            if (Objects.equals(relatedName, taxonName)) {
                // This name relation will already have been shown in enclosing taxon
                return;
            }
            nameToRender = relatedName;
            formatted = NameRelationTypeEnum.getLabel(type, false, true);
        } else {
            if (Objects.equals(name, taxonName)) {
                // This name relation will already have been shown in enclosing taxon
                return;
            }
            nameToRender = name;
            formatted = NameRelationTypeEnum.getLabel(type, true, true);
        }

        if (remarks != null && !remarks.equalsIgnoreCase(formatted)) {
            formatted += " (" + upperFirst(renderType.linkURLs(remarks)) + ")";
        }

        formatted += ": " + COLDPName.formatName(nameToRender, renderType);

        writer.println(context.getIndent() + renderType.openNode("NameRelationship") + formatted + renderType.closeNode());

        if (reference != null) {
            context.addReference(reference);
        }
    }
}
