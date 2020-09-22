/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.dhobern.coldp;

import java.util.Objects;

/**
 *
 * @author stang
 */
public class CoLDPNameReference implements Comparable<CoLDPNameReference> {
    
    private Integer nameID;
    private Integer referenceID;
    private String page;
    private String link;
    private String remarks;

    public CoLDPNameReference() {
    }

    public Integer getNameID() {
        return nameID;
    }

    public void setNameID(Integer nameID) {
        this.nameID = nameID;
    }

    public Integer getReferenceID() {
        return referenceID;
    }

    public void setReferenceID(Integer referenceID) {
        this.referenceID = referenceID;
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
        hash = 41 * hash + Objects.hashCode(this.nameID);
        hash = 41 * hash + Objects.hashCode(this.referenceID);
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
        final CoLDPNameReference other = (CoLDPNameReference) obj;
        if (!Objects.equals(this.page, other.page)) {
            return false;
        }
        if (!Objects.equals(this.link, other.link)) {
            return false;
        }
        if (!Objects.equals(this.remarks, other.remarks)) {
            return false;
        }
        if (!Objects.equals(this.nameID, other.nameID)) {
            return false;
        }
        if (!Objects.equals(this.referenceID, other.referenceID)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "CoLDPNameReference{" + "nameID=" + nameID + ", referenceID=" + referenceID + ", page=" + page + ", link=" + link + ", remarks=" + remarks + '}';
    }

    @Override
    public int compareTo(CoLDPNameReference o) {
        return this.toString().compareTo(o.toString());
    }

}
