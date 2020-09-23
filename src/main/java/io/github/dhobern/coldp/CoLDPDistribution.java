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
public class CoLDPDistribution implements Comparable<CoLDPDistribution> {
    
    private static final Logger LOG = LoggerFactory.getLogger(CoLDPDistribution.class);
     
    private Integer taxonID;
    private String area;
    private String gazetteer;
    private String status;
    private Integer referenceID;
    
    private CoLDPTaxon taxon;
    private CoLDPRegion region;

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
        return referenceID;
    }

    public void setReferenceID(Integer referenceID) {
        this.referenceID = referenceID;
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
        return "CoLDPDistribution{" + "taxonID=" + getTaxonID() + ", area=" + getArea() + ", gazetteer=" + gazetteer + ", status=" + status + ", referenceID=" + getReferenceID() + '}';
    }

    @Override
    public int compareTo(CoLDPDistribution o) {
        return this.toString().compareTo(o.toString());
    }

    public static String getCsvHeader() {
        return "taxonId,area,gazetteer,status,referenceID"; 
    }
    
    public String toCsv() {
        return StringUtils.toCsv(StringUtils.safeString(getTaxonID()),
                                 getArea(), gazetteer, status,
                                 StringUtils.safeString(referenceID));
    }
}
