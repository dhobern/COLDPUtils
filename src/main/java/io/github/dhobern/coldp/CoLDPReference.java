/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.dhobern.coldp;

import static io.github.dhobern.utils.StringUtils.*;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author stang
 */
public class CoLDPReference implements TreeRenderable {
    
    private static final Logger LOG = LoggerFactory.getLogger(CoLDPReference.class);
    
    private Integer ID;
    private String author;
    private String title;
    private String year;
    private String source;
    private String details;
    private String link;
    
    private Set<CoLDPName> names;
    private Set<CoLDPNameReference> nameReferences;
    private Set<CoLDPNameRelation> nameRelations;
    private Set<CoLDPTaxon> taxa;
    private Set<CoLDPSynonym> synonyms;
    private Set<CoLDPDistribution> distributions;

    public CoLDPReference() {
    }

    public Integer getID() {
        return ID;
    }

    public void setID(Integer ID) {
        this.ID = ID;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Set<CoLDPName> getNames() {
        return names;
    }

    void registerName(CoLDPName name) {
        if (name != null) {
            if (names == null) {
                names = new HashSet<>();
            }
            names.add(name);
        }
    }
 
    void deregisterName(CoLDPName name) {
        if (name != null && names != null) {
            names.remove(name);
        }
    }
 
    public Set<CoLDPNameReference> getNameReferences() {
        return nameReferences;
    }

    void registerNameReference(CoLDPNameReference nameReference) {
        if (nameReference != null) {
            if (nameReferences == null) {
                nameReferences = new HashSet<>();
            }
            nameReferences.add(nameReference);
        }
    }
 
    void deregisterNameReference(CoLDPNameReference nameReference) {
        if (nameReference != null && nameReferences != null) {
            nameReferences.remove(nameReference);
        }
    }
 
    public Set<CoLDPNameRelation> getNameRelations() {
        return nameRelations;
    }

    void registerNameRelation(CoLDPNameRelation nameRelation) {
        if (nameRelation != null) {
            if (nameRelations == null) {
                nameRelations = new HashSet<>();
            }
            nameRelations.add(nameRelation);
        }
    }
 
    void deregisterNameRelation(CoLDPNameRelation nameRelation) {
        if (nameRelation != null && nameRelations != null) {
            nameRelations.remove(nameRelation);
        }
    }
 
    public Set<CoLDPTaxon> getTaxa() {
        return taxa;
    }

    void registerTaxon(CoLDPTaxon taxon) {
        if (taxon != null) {
            if (taxa == null) {
                taxa = new HashSet<>();
            }
            taxa.add(taxon);
        }
    }
 
    void deregisterTaxon(CoLDPTaxon taxon) {
        if (taxon != null && taxa != null) {
            taxa.remove(taxon);
        }
    }

    public Set<CoLDPSynonym> getSynonyms() {
        return synonyms;
    }

    void registerSynonym(CoLDPSynonym synonym) {
        if (synonym != null) {
            if (synonyms == null) {
                synonyms = new HashSet<>();
            }
            synonyms.add(synonym);
        }
    }
 
    void deregisterSynonym(CoLDPSynonym synonym) {
        if (synonym != null && synonyms != null) {
            synonyms.remove(synonym);
        }
    }

    void registerDistribution(CoLDPDistribution distribution) {
        if (distribution != null) {
            if (distributions == null) {
                distributions = new HashSet<>();
            }
            distributions.add(distribution);
        }
    }
 
    void deregisterDistribution(CoLDPDistribution distribution) {
        if (distribution != null && distributions != null) {
            distributions.remove(distribution);
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
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
        final CoLDPReference other = (CoLDPReference) obj;
        if (!Objects.equals(this.ID, other.ID)) {
            return false;
        }
        return true;
    }

    public static class BibliographicSort implements Comparator<CoLDPReference> 
    { 
        public int compare(CoLDPReference a, CoLDPReference b) 
        { 
            int comparison = a.getAuthor().compareTo(b.getAuthor());
            if (comparison == 0) {
                comparison = a.getYear().compareTo(b.getYear());
            } 
            if (comparison == 0) {
                comparison = a.getTitle().compareTo(b.getTitle());
            }
            
            return comparison; 
        } 
    }     

    public static String getCsvHeader() {
        return "ID,author,title,year,source,details,link"; 
    }
    
    public String toCsv() {
        return buildCSV(safeString(ID),author,title,year,source,details,link);
    }

    @Override
    public void render(PrintWriter writer, TreeRenderProperties context) {
        if (context.getTreeRenderType() == TreeRenderProperties.TreeRenderType.HTML) {
            String formatted = getAuthor();
            if (getYear() != null) {
                formatted += " (" + getYear() + ") ";
            }
            formatted = wrapStrong(formatted);
            formatted += getTitle();
            if (!formatted.endsWith(".")) {
                formatted += ".";
            }   
            if (getSource() != null && getSource().length() > 0) {
                formatted += " <em>" + getSource() + "</em>";
            }
            if (getDetails() != null && getDetails().length() > 0) {
                formatted += " " + getDetails();
            }
            if (!formatted.endsWith(".")) {
                formatted += ".";
            }
            if (getLink() != null && getLink().length() > 0) {
                formatted += " <a href=\"" + getLink() + "\" target=\"_blank\"><i class=\"fas fa-external-link-alt fa-sm\"></i></a>";
            }
            writer.println(context.getIndent() + wrapDiv("Reference", formatted));
        }
    }
}
