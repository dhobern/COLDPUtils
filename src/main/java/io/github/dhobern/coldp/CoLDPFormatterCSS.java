/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.dhobern.coldp;

import io.github.dhobern.coldp.CoLDPReference.BibliographicSort;
import io.github.dhobern.coldp.CoLDPTaxon.AlphabeticalSort;
import io.github.dhobern.utils.CSVReader;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Platyptilia
 */
public class CoLDPFormatterCSS {
    
    private static final Logger LOG = LoggerFactory.getLogger(CoLDPFormatterCSS.class);
    
    private static Map<String,CoLDPName> names;
    private static Map<Integer,CoLDPName> namesByID;
    private static Map<Integer,CoLDPReference> references;
    private static Map<Integer,Set<CoLDPNameReference>> nameReferencesByNameID;
    private static Map<Integer,Set<CoLDPSynonym>> synonymsByNameID;
    private static Map<Integer,Set<CoLDPSynonym>> synonymsByTaxonID;
    private static Map<Integer,Set<CoLDPNameRelation>> relationsByNameID;
    private static Map<Integer,Set<CoLDPNameRelation>> relationsByRelatedNameID;
    private static Map<Integer,CoLDPTaxon> taxa;
    private static Map<Integer,CoLDPTaxon> taxaByNameID;
    private static Map<Integer,Set<CoLDPTaxon>> childrenByParentID;
    private static Map<Integer,Set<CoLDPDistribution>> distributionsByTaxonID;
    private static Map<String,CoLDPRegion> regions;

    private static final String INDENT = "    ";
    private static final String EQUALS = "=&nbsp;";
    private static boolean generateFullHtmlPage = false;

    /**
     *
     * @param argv
     */
    public static void main(String argv[]) {
        
        String taxonName = (argv.length > 0 ? argv[0] : "Alucitoidea" + "");
        if (argv.length > 1 && argv[1].equalsIgnoreCase("TRUE")) {
            generateFullHtmlPage = true;
        }
        String fileNamePrefix = taxonName + "/";
       
        try (PrintWriter writer = new PrintWriter(taxonName + (generateFullHtmlPage ? "" : "-catalogue") + ".html", "UTF-8")) {
            CSVReader<CoLDPName> nameReader
                    = new CSVReader<>(fileNamePrefix + "name.csv", CoLDPName.class, ",");   
            names = nameReader.getMap(CoLDPName::getScientificName);
            
            nameReader = new CSVReader<>(fileNamePrefix + "name.csv", CoLDPName.class, ",");
            namesByID = nameReader.getIntegerMap(CoLDPName::getID);
            
            CSVReader<CoLDPTaxon> taxonReader 
                    = new CSVReader<>(fileNamePrefix + "taxon.csv",
                            CoLDPTaxon.class, ",");
            taxa = taxonReader.getIntegerMap(CoLDPTaxon::getID);

            taxonReader 
                    = new CSVReader<>(fileNamePrefix + "taxon.csv", CoLDPTaxon.class, ",");
            taxaByNameID = taxonReader.getIntegerMap(CoLDPTaxon::getNameID);
            
            Comparator<CoLDPTaxon> alphabeticalSort = new AlphabeticalSort(namesByID);
            childrenByParentID = new HashMap<>();
            for (CoLDPTaxon taxon : taxa.values()) {
                if (taxon.getParentID() != null) {
                    Set<CoLDPTaxon> children = childrenByParentID.get(taxon.getParentID());
                    if (children == null) {
                        children = new TreeSet<>(alphabeticalSort);
                        childrenByParentID.put(taxon.getParentID(), children);
                    }
                    children.add(taxon);
                }
            }

            CSVReader<CoLDPReference> referenceReader 
                    = new CSVReader<>(fileNamePrefix + "reference.csv", CoLDPReference.class, ",");
            references = referenceReader.getIntegerMap(CoLDPReference::getID);

            CSVReader<CoLDPNameReference> nameReferenceReader 
                    = new CSVReader<>(fileNamePrefix + "namereference.csv", CoLDPNameReference.class, ",");
            nameReferencesByNameID = nameReferenceReader.getIntegerKeyedSets(CoLDPNameReference::getNameID);

            CSVReader<CoLDPSynonym> synonymReader 
                    = new CSVReader<>(fileNamePrefix + "synonym.csv", CoLDPSynonym.class, ",");
            synonymsByNameID = synonymReader.getIntegerKeyedSets(CoLDPSynonym::getNameID);
 
            synonymReader = new CSVReader<>(fileNamePrefix + "synonym.csv", CoLDPSynonym.class, ",");
            synonymsByTaxonID = synonymReader.getIntegerKeyedSets(CoLDPSynonym::getTaxonID);
 
            CSVReader<CoLDPNameRelation> nameRelationReader 
                    = new CSVReader<>(fileNamePrefix + "namerelation.csv", CoLDPNameRelation.class, ",");
            relationsByNameID = nameRelationReader.getIntegerKeyedSets(CoLDPNameRelation::getNameID);
 
            nameRelationReader 
                    = new CSVReader<>(fileNamePrefix + "nameRelation.csv", CoLDPNameRelation.class, ",");
            relationsByRelatedNameID = nameRelationReader.getIntegerKeyedSets(CoLDPNameRelation::getRelatedNameID);
 
            CSVReader<CoLDPRegion> regionReader 
                    = new CSVReader<>(fileNamePrefix + "region.csv", CoLDPRegion.class, ",");
            regions = regionReader.getMap(CoLDPRegion::getID);

            CSVReader<CoLDPDistribution> distributionReader 
                    = new CSVReader<>(fileNamePrefix + "distribution.csv", CoLDPDistribution.class, ",");
            distributionsByTaxonID = distributionReader.getIntegerKeyedSets(CoLDPDistribution::getTaxonID);
 
            if (generateFullHtmlPage) {
            writer.println("<!DOCTYPE html>");
			writer.println("<html>");
			writer.println("    <head>");
			writer.println("        <title>Catalogue of World " + taxonName + "</title>");
			writer.println("        <meta charset=\"UTF-8\">"); 
			writer.println("        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
			writer.println("        <link rel=\"stylesheet\" href=\"./css/catalogue.css\">");
			writer.println("        <script src=\"https://kit.fontawesome.com/9dcb058c00.js\" crossorigin=\"anonymous\"></script>");
			writer.println("    </head>");
			writer.println("    <body>");
            }
            
            CoLDPName parentName = names.get(taxonName);
            CoLDPTaxon parent = taxaByNameID.get(parentName.getID());
            Stack<CoLDPTaxon> higherTaxa = getHigherTaxa(parent);
            
            writeTaxon(writer, null, higherTaxa, generateFullHtmlPage ? 2 : 0);

            if (generateFullHtmlPage) {
                writer.println("    </body>");
                writer.println("</html>");
            }
        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            java.util.logging.Logger.getLogger(CoLDPFormatterCSS.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static Stack<CoLDPTaxon> getHigherTaxa(CoLDPTaxon taxon) {
        Stack<CoLDPTaxon> higherTaxa = new Stack<>();
        
        higherTaxa.push(taxon);
        while (taxon != null && taxon.getParentID() != null) {
            taxon = taxa.get(taxon.getParentID());
            if (taxon != null) {
                higherTaxa.push(taxon);
            }
        }
        
        return higherTaxa;
    }
    
    private static void writeTaxon(PrintWriter writer, CoLDPTaxon taxon, Stack<CoLDPTaxon> higherTaxa, int depth) {
        if (higherTaxa != null) {
            taxon = higherTaxa.pop();
            if (higherTaxa.empty()) {
                higherTaxa = null;
            }
        }
        
        String prefix = "";
        for (int i = 0; i < depth; i++) prefix += INDENT;
	String childPrefix = prefix + INDENT;
		
        Set<CoLDPReference> referenceList = new TreeSet<>(new BibliographicSort());
        if (taxon.getReferenceID() != null) {
            referenceList.add(references.get(taxon.getReferenceID()));
        }
        
        CoLDPName name = namesByID.get(taxon.getNameID());
        String divClass = (higherTaxa == null) ? upperFirst(name.getRank()) : "HigherTaxon";
	writer.println(prefix + "<div class=\"" + divClass + "\">");
        writeName(writer, name, childPrefix, referenceList, false, null);
        
        Set<CoLDPSynonym> synonyms = synonymsByTaxonID.get(taxon.getID());
        if (synonyms != null && synonyms.size() > 0) {
            writer.println(childPrefix + "<div class=\"Synonyms\">");
            
            for (CoLDPSynonym synonym : synonyms) {
                writeSynonym(writer, synonym, childPrefix + INDENT, referenceList);
            }

            writer.println(childPrefix + "</div>");
        }
                
        if (referenceList.size() > 0) {
            writer.println(childPrefix + "<div class=\"References\">");
            
            for (CoLDPReference reference : referenceList) {
                writeReference(writer, reference, childPrefix + INDENT);
            }

            writer.println(childPrefix + "</div>");
        }

        if (taxon.getRemarks() != null) {
            writer.println(prefix + wrapDiv("Note", "Note: " + wrapEmphasis(linkURLs(taxon.getRemarks()))));
        }

        if (higherTaxa == null) {
            Set<CoLDPTaxon> children = childrenByParentID.get(taxon.getID());
            if (children != null) {
                for (CoLDPTaxon child : children) {
                    writeTaxon(writer, child, null, depth + 1);
                }
            }
        } else {
            writeTaxon(writer, null, higherTaxa, depth + 1);
        }
        
        writer.println(prefix + "</div>");
    }

    private static void writeName(PrintWriter writer, CoLDPName name, 
            String prefix, Set<CoLDPReference> referenceList, boolean isSynonym,
            String remarks) {
        
        String qualifier = "";
        if (isSynonym) {
            qualifier = EQUALS;
            if (name.getRemarks() != null) {
                if (remarks != null) {
                    remarks = remarks + "; " + safeTrim(linkURLs(name.getRemarks()));
                } else {
                    remarks = safeTrim(linkURLs(name.getRemarks()));
                }
            }
        }
        if (remarks != null) {
            remarks = " (" + remarks + ")"; 
        } else {
            remarks = "";
        }

        String formatted = qualifier + upperFirst(name.getRank()) + remarks + ": " + wrapStrong(formatName(name));

        writer.println(prefix + "<div class=\"Name\">" + formatted);
        
        writeNameInformation(writer, name, prefix + INDENT, referenceList, isSynonym);
        
        writer.println(prefix + "</div>");
    }
    
    private static void writeNameInformation(PrintWriter writer, CoLDPName name, String prefix, 
                Set<CoLDPReference> referenceList, boolean isSynonym) {
        if (name.getReferenceID() != null) {
            CoLDPReference reference = references.get(name.getReferenceID());
            
            referenceList.add(reference);
        }
        
        Set<CoLDPNameReference> nameReferences = nameReferencesByNameID.get(name.getID());
        if (nameReferences != null) {
            for (CoLDPNameReference nameReference : nameReferences) {
                CoLDPReference reference = references.get(nameReference.getReferenceID());
                referenceList.add(reference);
                writeNameReference(writer, nameReference, reference, prefix);
            }
        }

        writeNameRelations(writer, name, prefix, referenceList);
        
        if (!isSynonym && name.getRemarks() != null) {
            writer.println(prefix + wrapDiv("Note", "Note: " + wrapEmphasis(linkURLs(name.getRemarks()))));
        }
    }
    
    private static void writeNameRelations(PrintWriter writer, CoLDPName name, String prefix, Set<CoLDPReference> referenceList) {
        Set<CoLDPNameRelation> relations = relationsByNameID.get(name.getID());
        if (relations != null) {
            for (CoLDPNameRelation relation : relations) {
                writeNameRelation(writer, relation, prefix, true, name.getRank(), referenceList);
            }
        }

        relations = relationsByRelatedNameID.get(name.getID());
        if (relations != null) {
            for (CoLDPNameRelation relation : relations) {
                writeNameRelation(writer, relation, prefix, false, name.getRank(), referenceList);
            }
        }
    }

    private static void writeNameRelation(PrintWriter writer, CoLDPNameRelation relation, 
            String prefix, boolean isSubject, String rankName, Set<CoLDPReference> referenceList) {
        CoLDPName name = namesByID.get(isSubject ? relation.getRelatedNameID() : relation.getNameID());
        String formatted = upperFirst(relation.getType());
        
        if (isSubject) {
            switch(formatted) {
                case "Type":
                    if (rankName.equals("genus")) {
                        formatted = "Type genus for family";
                    } else if (rankName.equals("species")) {
                        formatted = "Type for genus";
                    } else {
                        formatted = "Type for";
                    }
                    break;
                case "Basionym":
                    formatted = "Basionym for";
                    break;
            }
        } else {
            switch(formatted) {
                case "Type":
                    if (rankName.equals("family")) {
                        formatted = "Type genus";
                    } else if (rankName.equals("genus")) {
                        formatted = "Type species";
                    } else {
                        formatted = "Type";
                    }
                    break;
            }
        }
        
        if (formatted != null) {
            if (relation.getRemarks() != null) {
                if (!relation.getRemarks().equalsIgnoreCase(formatted)) {
                    formatted += " (" + upperFirst(linkURLs(relation.getRemarks())) + ")";
                }
            }

            formatted += ": " + formatName(name);

            writer.println(prefix + wrapDiv("Relationship", formatted));

            if (relation.getReferenceID() != null) {
                CoLDPReference reference = references.get(relation.getReferenceID());
                referenceList.add(reference);
            }
        }
    }

    private static void writeSynonym(PrintWriter writer, CoLDPSynonym synonym, String prefix, Set<CoLDPReference> referenceList) {
        CoLDPName name = namesByID.get(synonym.getNameID());
        String synonymRemarks = safeTrim(linkURLs(synonym.getRemarks()));

        writer.println(prefix + "<div class=\"Synonym\">");
        
        writeName(writer, name, prefix + INDENT, referenceList, true, synonymRemarks);

        if (synonym.getReferenceID() != null) {
            CoLDPReference reference = references.get(synonym.getReferenceID());
            referenceList.add(reference);
        }
        
        writer.println(prefix + "</div>");
    }

    private static String safeTrim(String s) {
        if (s != null) {
            s = s.trim();
            if (s.length() == 0) {
                s = null;
            } 
        }
        return s;
    }
    
    private static void writeReference(PrintWriter writer, CoLDPReference reference, String prefix) {
        String formatted = reference.getAuthor();
        if (reference.getYear() != null) {
            formatted += " (" + reference.getYear() + ") ";
        }
        formatted = wrapStrong(formatted);
        formatted += reference.getTitle();
        if (!formatted.endsWith(".")) {
            formatted += ".";
        }   
        if (reference.getSource() != null && reference.getSource().length() > 0) {
            formatted += " <em>" + reference.getSource() + "</em>";
        }
        if (reference.getDetails() != null && reference.getDetails().length() > 0) {
            formatted += " " + reference.getDetails();
        }
        if (!formatted.endsWith(".")) {
            formatted += ".";
        }
        if (reference.getLink() != null && reference.getLink().length() > 0) {
            formatted += " <a href=\"" + reference.getLink() + "\" target=\"_blank\"><i class=\"fas fa-external-link-alt fa-sm\"></i></a>";
        }
        writer.println(prefix + wrapDiv("Reference", formatted));
    }

    private static void writeNameReference(PrintWriter writer, CoLDPNameReference nameReference, CoLDPReference reference, String prefix) {
        String formatted = "Page reference";
                
        if (nameReference.getRemarks() != null) {
            formatted += " (" + linkURLs(nameReference.getRemarks()) + ")";
        }
        
        formatted += ": " + reference.getAuthor() + " ";
        if (reference.getYear() != null) {
            formatted += "(" + reference.getYear() + ") ";
        }

        if (nameReference.getLink() != null && nameReference.getLink().startsWith("http")) {
            formatted += "<a href=\"" + nameReference.getLink() + "\" target=\"_blank\">" 
                    + wrapStrong(nameReference.getPage()) + " <i class=\"fas fa-external-link-alt fa-sm\"></i></a>";
        } else {
            formatted += wrapStrong(nameReference.getPage());
        }

        writer.println(prefix + wrapDiv("Reference", formatted));
    }

    private static String wrapDiv(String divClass, String name) {
        name = "<div class=\"" + divClass + "\">" + name + "</div>";
        return name;
    }

    private static String formatName(CoLDPName name) {
        String scientificName = name.getScientificName();
        switch (name.getRank()) {
            case "genus":
            case "species":
            case "subspecies":
                scientificName = wrapEmphasis(scientificName);
                break;
            case "variety":
                scientificName = wrapEmphasis(name.getGenus() + " " + name.getSpecificEpithet()) 
                        + " var. " + wrapEmphasis(name.getInfraspecificEpithet());
                break;
            case "form":
                scientificName = wrapEmphasis(name.getGenus() + " " + name.getSpecificEpithet()) 
                        + " f. " + wrapEmphasis(name.getInfraspecificEpithet());
                break;
            case "aberration":
                scientificName = wrapEmphasis(name.getGenus() + " " + name.getSpecificEpithet()) 
                        + " ab. " + wrapEmphasis(name.getInfraspecificEpithet());
                break;
        }
        
        String authorship = name.getAuthorship();
        if (authorship == null) {
            authorship = "";
        } else {
            authorship = " " + authorship;
        }

        return scientificName + authorship;    
    }

    private static void csvWrite(PrintWriter writer, String ... values) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String v: values) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }
            if (v == null || v.equalsIgnoreCase("NULL")) {
                v = "";
            }
            if (v.contains(",")) {
                sb.append("\""); 
                sb.append(v); 
                sb.append("\""); 
            } else {             
                sb.append(v);
            }
        }
        writer.println(sb.toString());
    }

    private static String wrapStrong(String s) {
        return "<strong>" + s + "</strong>";
    }

    private static String wrapEmphasis(String s) {
        return "<em>" + s + "</em>";
    }

    private static String linkURLs(String s) {
        String enabled = "";
        
        if (s != null) {
            int start = 0;
            int end = -1;
            int current = 0;
            start = s.indexOf("http://"); 
            if (start < 0) {
                start = s.indexOf("https://");
            }
            while (start >= 0) {
                end = s.indexOf(" ", start);
                if (end < 0) {
                    end = s.length();
                }
                String url = s.substring(start, end);
                String possibleComma = "";
                if (url.endsWith(",")) {
                    url = url.substring(0, url.length() - 1);
                    possibleComma = ",";
                }
                String prior = "";
                if (start > current) {
                    prior = s.substring(current, start);
                }
                enabled += prior + "<a href=\"" + url +"\" target=\"_blank\">" + url + "</a>" + possibleComma;
                current = end;
                int newStart = s.indexOf("http://", start + 1);
                if (newStart < 0) {
                    newStart = s.indexOf("https://", start + 1);
                }
                start = newStart;
            }
            if (current < s.length()) {
                enabled += s.substring(current);
            }
        }
        
        return enabled;
    }
    
    private static String upperFirst(String s) {
        if (s != null && s.length() > 1) {
            s = s.substring(0, 1).toUpperCase() + s.substring(1);
        }
        return s;
    }
}