/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.dhobern.coldp;

import io.github.dhobern.utils.StringUtils;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author stang
 */
public class COLDPRegion {
    
    private static final Logger LOG = LoggerFactory.getLogger(COLDPRegion.class);
      
    private String ID;
    private String name;
    
    private Set<COLDPDistribution> distributions;

    public COLDPRegion() {
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<COLDPDistribution> getDistributions() {
        return distributions;
    }

    void registerDistribution(COLDPDistribution distribution) {
        if (distribution != null) {
            if (distributions == null) {
                distributions = new HashSet<>();
            }
            distributions.add(distribution);
        }
    }
 
    void deregisterDistribution(COLDPDistribution distribution) {
        if (distribution != null && distributions != null) {
            distributions.remove(distribution);
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.ID);
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
        final COLDPRegion other = (COLDPRegion) obj;
        if (!Objects.equals(this.ID, other.ID)) {
            return false;
        }
        return true;
    }
    
    public static String getCsvHeader() {
        return "ID,name"; 
    }
    
    public String toCsv() {
        return StringUtils.buildCSV(ID, name);
    }
    
    public String toReferenceString() {
        return  ID + " " + name;
    }
}
