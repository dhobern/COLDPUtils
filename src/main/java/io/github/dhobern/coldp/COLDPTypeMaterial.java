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
public class COLDPTypeMaterial implements Comparable<COLDPTypeMaterial>, TreeRenderable {
    
    private static final Logger LOG = LoggerFactory.getLogger(COLDPTypeMaterial.class);
     
    private String nameID;
    private String remarks;
    
    private COLDPName name;

    public COLDPTypeMaterial() {
    }

    public String getNameID() {
        return name == null ? nameID : name.getID();
    }

    public void setNameID(String nameID) {
        if (name == null) {
            this.nameID = nameID;
        } else {
            LOG.error("Attempted to set nameID to " + nameID + " when typematerial associated with name " + name);
        }
    }

    public COLDPName getName() {
        return name;
    }

    public void setName(COLDPName name) {
        if (!Objects.equals(this.name, name)) {
            if (this.name != null) {
                this.name.setTypeMaterial(null);
            }
            this.name = name;
            nameID = null;
            name.setTypeMaterial(this);
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
        hash = 37 * hash + Objects.hashCode(this.nameID);
        hash = 37 * hash + Objects.hashCode(this.remarks);
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
        final COLDPTypeMaterial other = (COLDPTypeMaterial) obj;
        if (!Objects.equals(this.getRemarks(), other.getRemarks())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "[" + (name == null ? "" : name.toString()) + "] typeMaterial [" + remarks + "]";
    }

    @Override
    public int compareTo(COLDPTypeMaterial o) {
        return this.toString().compareTo(o.toString());
    }

    public static String getCsvHeader() {
        return "nameId,remarks"; 
    }
    
    public String toCsv() {
        return buildCSV(getNameID(), remarks);
    }

    @Override
    public void render(PrintWriter writer, TreeRenderProperties context) {
        TreeRenderType renderType = context.getTreeRenderType();
        writer.println(context.getIndent() + renderType.openNode("TypeMaterial") + "Type material: " + remarks + renderType.closeNode());
    }
}
