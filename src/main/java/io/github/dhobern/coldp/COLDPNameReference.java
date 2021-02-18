/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.dhobern.coldp;

import io.github.dhobern.coldp.TreeRenderProperties.TreeRenderType;
import io.github.dhobern.utils.StringUtils;
import static io.github.dhobern.utils.StringUtils.*;
import java.io.PrintWriter;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author stang
 */
public class COLDPNameReference implements Comparable<COLDPNameReference>, TreeRenderable {
    
    private static final Logger LOG = LoggerFactory.getLogger(COLDPNameReference.class);
     
    private String nameID;
    private String referenceID;
    private String page;
    private String link;
    private String remarks;
    
    private COLDPName name;
    private COLDPReference reference;

    public COLDPNameReference() {
    }

    public String getNameID() {
        return name == null ? nameID : name.getID();
    }

    public void setNameID(String nameID) {
        if (name == null) {
            this.nameID = nameID;
        } else {
            LOG.error("Attempted to set nameID to " + nameID + " when nameReference associated with name " + name);
        }
    }

    public COLDPName getName() {
        return name;
    }

    public void setName(COLDPName name) {
        if (!Objects.equals(this.name, name)) {
            if (this.name != null) {
                this.name.deregisterNameReference(this);
            }
            this.name = name;
            nameID = null;
            if (name != null) {
                name.registerNameReference(this);
            }
        }
    }

    public String getReferenceID() {
        return reference == null ? referenceID : reference.getID();
    }

    public void setReferenceID(String referenceID) {
        if (reference == null) {
            this.referenceID = referenceID;
        } else {
            LOG.error("Attempted to set referenceID to " + referenceID + " when nameReference associated with reference " + reference);
        }
    }

    public COLDPReference getReference() {
        return reference;
    }

    public void setReference(COLDPReference reference) {
        if (!Objects.equals(this.reference, reference)) {
            if (this.reference != null) {
                this.reference.deregisterNameReference(this);
            }
            this.reference = reference;
            referenceID = null;
            if (reference != null) {
                reference.registerNameReference(this);
            }
        }
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
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
        hash = 41 * hash + Objects.hashCode(this.getNameID());
        hash = 41 * hash + Objects.hashCode(this.getReferenceID());
        hash = 41 * hash + Objects.hashCode(this.page);
        hash = 41 * hash + Objects.hashCode(this.link);
        hash = 41 * hash + Objects.hashCode(this.remarks);
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
        final COLDPNameReference other = (COLDPNameReference) obj;
        if (!Objects.equals(this.page, other.page)) {
            return false;
        }
        if (!Objects.equals(this.link, other.link)) {
            return false;
        }
        if (!Objects.equals(this.remarks, other.remarks)) {
            return false;
        }
        if (!Objects.equals(this.getNameID(), other.getNameID())) {
            return false;
        }
        if (!Objects.equals(this.getReferenceID(), other.getReferenceID())) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(COLDPNameReference o) {
        return this.toString().compareTo(o.toString());
    }

    public static String getCsvHeader() {
        return "nameID,referenceID,page,link,remarks"; 
    }
    
    public String toCsv() {
        return StringUtils.buildCSV(getNameID(), getReferenceID(),
                                    page, link, remarks);
    }

    @Override
    public String toString() {
        return "[" + name.toString() + "] in [" + reference.toString() + "]";
    }

    @Override
    public void render(PrintWriter writer, TreeRenderProperties context) {
        TreeRenderType renderType = context.getTreeRenderType();
        context.addReference(reference);

        String formatted = "Page reference";

        if (remarks!= null) {
            formatted += " (" + renderType.linkURLs(remarks) + ")";
        }

        formatted += ": " + reference.getAuthor();
        if (reference.getYear() != null) {
            formatted += " (" + reference.getYear() + ")";
        }

        if (link != null && link.startsWith("http")) {
            String pageTest = (page == null) ? "Link" : page;
            if (renderType.equals(TreeRenderType.HTML)) {
                formatted += ", <a href=\"" + link + "\" target=\"_blank\">" 
                        + renderType.wrapStrong(page) + " <i class=\"fas fa-external-link-alt fa-sm\"></i></a>";
            } else {
                formatted += ", " + page + ": " + link;
            }            
        } else if (page != null) {
            formatted += ", " + renderType.wrapStrong(page);
        }

        writer.println(context.getIndent() + renderType.openNode("Reference") + formatted + renderType.closeNode());
    }
}
