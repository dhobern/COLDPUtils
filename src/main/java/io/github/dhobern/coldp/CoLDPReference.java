/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.dhobern.coldp;

import java.util.Comparator;
import java.util.Objects;

/**
 *
 * @author stang
 */
public class CoLDPReference {
    
    private Integer ID;
    private String author;
    private String title;
    private String year;
    private String source;
    private String details;
    private String link;

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
}
