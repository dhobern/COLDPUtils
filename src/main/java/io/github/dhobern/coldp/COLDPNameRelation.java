/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.dhobern.coldp;

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
      
    private Integer nameID;
    private Integer relatedNameID;
    private String type;
    private Integer referenceID;
    private String remarks;
    
    private COLDPName name;
    private COLDPName relatedName;
    private COLDPReference reference;

    public COLDPNameRelation() {
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

    public COLDPName getName() {
        return name;
    }

    public void setName(COLDPName name) {
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

    public COLDPName getRelatedName() {
        return relatedName;
    }

    public void setRelatedName(COLDPName relatedName) {
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

    public COLDPReference getReference() {
        return reference;
    }

    public void setReference(COLDPReference reference) {
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
        return "CoLDPNameRelation{" + "nameID=" + getNameID() + ", relatedNameID=" + getRelatedNameID() + ", type=" + type + ", referenceID=" + getReferenceID() + ", remarks=" + remarks + '}';
    }

    @Override
    public int compareTo(COLDPNameRelation o) {
        return this.toString().compareTo(o.toString());
    }

    public static String getCsvHeader() {
        return "nameID,relatedNameID,type,referenceID,remarks"; 
    }
    
    public String toCsv() {
        return buildCSV(safeString(getNameID()),
                        safeString(getRelatedNameID()),
                        type,
                        safeString(getReferenceID()),
                        remarks);
    }

    @Override
    public void render(PrintWriter writer, TreeRenderProperties context) {
        if (context.getTreeRenderType() == TreeRenderProperties.TreeRenderType.HTML) {
            COLDPName contextName = context.getCurrentName();
            String formatted = upperFirst(type);
            COLDPName nameToRender;

            if (name.equals(contextName)) {
                nameToRender = relatedName;
                switch(formatted) {
                    case "Type":
                        if (contextName.getRank().equals("genus")) {
                            formatted = "Is type for family";
                        } else if (contextName.getRank().equals("species")) {
                            formatted = "Is type for genus";
                        } else {
                            formatted = "Is type for";
                        }
                        break;
                    case "Basionym":
                        formatted = "Has basionym";
                        break;
                    case "Later homonym":
                        formatted = "Is later homonym of";
                        break;
                }
            } else {
                nameToRender = name;
                switch(formatted) {
                    case "Type":
                        if (contextName.getRank().equals("family")) {
                            formatted = "Has type genus";
                        } else if (contextName.getRank().equals("genus")) {
                            formatted = "Has type species";
                        } else {
                            formatted = "Has type";
                        }
                        break;
                    case "Basionym":
                        formatted = "Is basionym for";
                        break;
                    case "Later homonym":
                        formatted = "Has later homonym";
                        break;
                }
            }

            if (remarks != null && !remarks.equalsIgnoreCase(formatted)) {
                formatted += " (" + upperFirst(linkURLs(remarks)) + ")";
            }

            formatted += ": " + COLDPName.formatName(nameToRender);

            writer.println(context.getIndent() + wrapDiv("NameRelationship", formatted));

            if (reference != null) {
                context.addReference(reference);
            }
        }
    }
}
