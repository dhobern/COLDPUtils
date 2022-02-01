/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.dhobern.coldp;

import io.github.dhobern.coldp.TreeRenderProperties.TreeRenderType;
import static io.github.dhobern.utils.StringUtils.*;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author stang
 */
public class COLDPReference implements TreeRenderable {
    
    private static final Logger LOG = LoggerFactory.getLogger(COLDPReference.class);
    
    private String ID;
    private String author;
    private String title;
    private String issued;
    private String containerTitle;
    private String volume;
    private String issue;
    private String page;
    private String link;
    
    private Set<COLDPName> names;
    private List<COLDPNameReference> nameReferences;
    private List<COLDPNameRelation> nameRelations;
    private Set<COLDPTaxon> taxa;
    private List<COLDPSynonym> synonyms;
    private List<COLDPDistribution> distributions;
    private List<COLDPSpeciesInteraction> speciesInteractions;

    public COLDPReference() {
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
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

    public String getIssued() {
        return issued;
    }

    public void setIssued(String issued) {
        this.issued = issued;
    }

    public String getContainerTitle() {
        return containerTitle;
    }

    public void setContainerTitle(String containerTitle) {
        this.containerTitle = containerTitle;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getIssue() {
        return issue;
    }

    public void setIssue(String issue) {
        this.issue = issue;
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

    public Set<COLDPName> getNames() {
        return names;
    }

    void registerName(COLDPName name) {
        if (name != null) {
            if (names == null) {
                names = new HashSet<>();
            }
            names.add(name);
        }
    }
 
    void deregisterName(COLDPName name) {
        if (name != null && names != null) {
            names.remove(name);
        }
    }
 
    public List<COLDPNameReference> getNameReferences() {
        return nameReferences;
    }

    void registerNameReference(COLDPNameReference nameReference) {
        if (nameReference != null) {
            if (nameReferences == null) {
                nameReferences = new ArrayList<>();
            }
            nameReferences.add(nameReference);
        }
    }
 
    void deregisterNameReference(COLDPNameReference nameReference) {
        if (nameReference != null && nameReferences != null) {
            nameReferences.remove(nameReference);
        }
    }
 
    public List<COLDPNameRelation> getNameRelations() {
        return nameRelations;
    }

    void registerNameRelation(COLDPNameRelation nameRelation) {
        if (nameRelation != null) {
            if (nameRelations == null) {
                nameRelations = new ArrayList<>();
            }
            nameRelations.add(nameRelation);
        }
    }
 
    void deregisterNameRelation(COLDPNameRelation nameRelation) {
        if (nameRelation != null && nameRelations != null) {
            nameRelations.remove(nameRelation);
        }
    }
 
    public Set<COLDPTaxon> getTaxa() {
        return taxa;
    }

    void registerTaxon(COLDPTaxon taxon) {
        if (taxon != null) {
            if (taxa == null) {
                taxa = new HashSet<>();
            }
            taxa.add(taxon);
        }
    }
 
    void deregisterTaxon(COLDPTaxon taxon) {
        if (taxon != null && taxa != null) {
            taxa.remove(taxon);
        }
    }

    public List<COLDPSynonym> getSynonyms() {
        return synonyms;
    }

    void registerSynonym(COLDPSynonym synonym) {
        if (synonym != null) {
            if (synonyms == null) {
                synonyms = new ArrayList<>();
            }
            synonyms.add(synonym);
        }
    }
 
    void deregisterSynonym(COLDPSynonym synonym) {
        if (synonym != null && synonyms != null) {
            synonyms.remove(synonym);
        }
    }

    void registerDistribution(COLDPDistribution distribution) {
        if (distribution != null) {
            if (distributions == null) {
                distributions = new ArrayList<>();
            }
            distributions.add(distribution);
        }
    }
 
    void deregisterDistribution(COLDPDistribution distribution) {
        if (distribution != null && distributions != null) {
            distributions.remove(distribution);
        }
    }

    public List<COLDPDistribution> getDistributions() {
        return distributions;
    }

    void registerSpeciesInteraction(COLDPSpeciesInteraction si) {
        if (si != null) {
            if (speciesInteractions == null) {
                speciesInteractions = new ArrayList<>();
            }
            speciesInteractions.add(si);
        }
    }
 
    void deregisterSpeciesInteraction(COLDPSpeciesInteraction si) {
        if (si != null && speciesInteractions != null) {
            speciesInteractions.remove(si);
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
        final COLDPReference other = (COLDPReference) obj;
        if (!Objects.equals(this.ID, other.ID)) {
            return false;
        }
        return true;
    }

    public static class BibliographicSort implements Comparator<COLDPReference> 
    { 
        public int compare(COLDPReference a, COLDPReference b) 
        { 
            int comparison = a.getAuthor().toLowerCase().compareTo(b.getAuthor().toLowerCase());
            if (comparison == 0) {
                String issuedA = (a.getIssued() == null) ? "" : a.getIssued();
                String issuedB = (b.getIssued() == null) ? "" : b.getIssued();
                comparison = issuedA.compareTo(issuedB);
            } 
            if (comparison == 0) {
                comparison = a.getTitle().compareTo(b.getTitle());
            }
            
            return comparison; 
        } 
    }     

    public static String getCsvHeader() {
        return "ID,author,title,year,containerTitle,volume,link"; 
    }
    
    public String toCsv() {
        return buildCSV(ID, author, title, issued, containerTitle, volume, issue, page, link);
    }
    
    public String toString() {
        return ID + " " + author + ", " + issued + ", " + title;
    }

    public String toString(int authorLength, int titleLength) {
        return ID + " " + abbreviate(author, authorLength) + ", " + issued + (titleLength == 0 ? "" : ", ") + abbreviate(title, titleLength);
    }

    @Override
    public void render(PrintWriter writer, TreeRenderProperties context) {
        TreeRenderType renderType = context.getTreeRenderType();
        String formatted = getAuthor();
        if (getIssued() != null) {
            formatted += " (" + getIssued() + ")";
        }
        formatted = renderType.wrapStrong(formatted) + " ";
        formatted += getTitle();
        if (!formatted.endsWith(".")) {
            formatted += ".";
        }   
        if (getContainerTitle() != null && getContainerTitle().length() > 0) {
            formatted += " " + renderType.wrapEmphasis(getContainerTitle());
        }
        if (getVolume() != null && getVolume().length() > 0) {
            if (getIssue() != null && getIssue().length() > 0) {
                if (getPage() != null && getPage().length() > 0){
                    formatted += " " + getVolume() + " (" + getIssue() + "): " + getPage();
                } else {
                    formatted += " " + getVolume() + " (" + getIssue() + ")";
                }
            } else {
                if (getPage() != null && getPage().length() > 0) {
                    formatted += " " + getVolume() + ": " + getPage();
                } else {
                    formatted += " " + getVolume();
                }
            }
        } else {
            if (getIssue() != null && getIssue().length() > 0) {
                if (getPage() != null && getPage().length() > 0) {
                    formatted += " (" + getIssue() + "): " + getPage();
                } else {
                    formatted += " (" + getIssue() + ")";
                }
            } else {
                if (getPage() != null && getPage().length() > 0) {
                    formatted += " " + getPage();
                }
            }
        }
        if (!formatted.endsWith(".")) {
            formatted += ".";
        }
        if (getLink() != null && getLink().length() > 0) {
            if (renderType.equals(TreeRenderType.HTML)) {
                formatted += " <a href=\"" + getLink() + "\" target=\"_blank\"><i class=\"fas fa-external-link-alt fa-sm\"></i></a>";
            } else {
                formatted += " " + getLink();
            }
        }
        writer.println(context.getIndent() + renderType.openNode("Reference") + formatted + renderType.closeNode());
    }
}
