/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.dhobern.coldp;

import static io.github.dhobern.utils.StringUtils.*;
import io.github.dhobern.coldp.COLDPReference.BibliographicSort;
import io.github.dhobern.coldp.COLDPTaxon.AlphabeticalSort;
import io.github.dhobern.utils.CSVReader;
import java.io.File;
import java.io.FileNotFoundException;
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
public class COLDPFormatterCSS {
    
    private static final Logger LOG = LoggerFactory.getLogger(COLDPFormatterCSS.class);
    
    private static Map<String,COLDPName> names;
    private static Map<Integer,COLDPName> namesByID;
    private static Map<Integer,COLDPReference> references;
    private static Map<Integer,Set<COLDPNameReference>> nameReferencesByNameID;
    private static Map<Integer,Set<COLDPSynonym>> synonymsByNameID;
    private static Map<Integer,Set<COLDPSynonym>> synonymsByTaxonID;
    private static Map<Integer,Set<COLDPNameRelation>> relationsByNameID;
    private static Map<Integer,Set<COLDPNameRelation>> relationsByRelatedNameID;
    private static Map<Integer,COLDPTaxon> taxa;
    private static Map<Integer,COLDPTaxon> taxaByNameID;
    private static Map<Integer,Set<COLDPTaxon>> childrenByParentID;
    private static Map<Integer,Set<COLDPDistribution>> distributionsByTaxonID;
    private static Map<String,COLDPRegion> regions;

    private static final String INDENT = "    ";
    private static final String EQUALS = "=&nbsp;";
    private static boolean generateFullHtmlPage = false;

    /**
     *
     * @param argv
     */
    public static void main(String argv[]) {
        
        String taxonName = (argv.length > 0 ? argv[0] : "Alucitoidea");
        String dataFolderName = (argv.length > 1 ? argv[1] : "." + "");
        if (argv.length > 2 && argv[2].equalsIgnoreCase("TRUE")) {
            generateFullHtmlPage = true;
        }
        String fileNamePrefix = dataFolderName + "/";
       
        try (PrintWriter writer = new PrintWriter(taxonName + (generateFullHtmlPage ? "" : "-catalogue") + ".html", "UTF-8")) {
            CSVReader<COLDPName> nameReader
                    = new CSVReader<>(fileNamePrefix + "name.csv", COLDPName.class, ",");   
            names = nameReader.getMap(COLDPName::getScientificName);
            
            nameReader = new CSVReader<>(fileNamePrefix + "name.csv", COLDPName.class, ",");
            namesByID = nameReader.getIntegerMap(COLDPName::getID);
            
            CSVReader<COLDPTaxon> taxonReader 
                    = new CSVReader<>(fileNamePrefix + "taxon.csv",
                            COLDPTaxon.class, ",");
            taxa = taxonReader.getIntegerMap(COLDPTaxon::getID);

            taxonReader 
                    = new CSVReader<>(fileNamePrefix + "taxon.csv", COLDPTaxon.class, ",");
            taxaByNameID = taxonReader.getIntegerMap(COLDPTaxon::getNameID);
            
            Comparator<COLDPTaxon> alphabeticalSort = new AlphabeticalSort(namesByID);
            childrenByParentID = new HashMap<>();
            for (COLDPTaxon taxon : taxa.values()) {
                if (taxon.getParentID() != null) {
                    Set<COLDPTaxon> children = childrenByParentID.get(taxon.getParentID());
                    if (children == null) {
                        children = new TreeSet<>(alphabeticalSort);
                        childrenByParentID.put(taxon.getParentID(), children);
                    }
                    children.add(taxon);
                }
            }

            CSVReader<COLDPReference> referenceReader 
                    = new CSVReader<>(fileNamePrefix + "reference.csv", COLDPReference.class, ",");
            references = referenceReader.getIntegerMap(COLDPReference::getID);

            CSVReader<COLDPNameReference> nameReferenceReader 
                    = new CSVReader<>(fileNamePrefix + "namereference.csv", COLDPNameReference.class, ",");
            nameReferencesByNameID = nameReferenceReader.getIntegerKeyedSets(COLDPNameReference::getNameID);

            CSVReader<COLDPSynonym> synonymReader 
                    = new CSVReader<>(fileNamePrefix + "synonym.csv", COLDPSynonym.class, ",");
            synonymsByNameID = synonymReader.getIntegerKeyedSets(COLDPSynonym::getNameID);
 
            synonymReader = new CSVReader<>(fileNamePrefix + "synonym.csv", COLDPSynonym.class, ",");
            synonymsByTaxonID = synonymReader.getIntegerKeyedSets(COLDPSynonym::getTaxonID);
 
            CSVReader<COLDPNameRelation> nameRelationReader 
                    = new CSVReader<>(fileNamePrefix + "namerelation.csv", COLDPNameRelation.class, ",");
            relationsByNameID = nameRelationReader.getIntegerKeyedSets(COLDPNameRelation::getNameID);
 
            nameRelationReader 
                    = new CSVReader<>(fileNamePrefix + "nameRelation.csv", COLDPNameRelation.class, ",");
            relationsByRelatedNameID = nameRelationReader.getIntegerKeyedSets(COLDPNameRelation::getRelatedNameID);
 
            if (new File(fileNamePrefix + "region.csv").exists()) {
                CSVReader<COLDPRegion> regionReader 
                    = new CSVReader<>(fileNamePrefix + "region.csv", COLDPRegion.class, ",");
                regions = regionReader.getMap(COLDPRegion::getID);
            } else {
                regions = new HashMap<>();
            }

            if (new File(fileNamePrefix + "distribution.csv").exists()) {
                CSVReader<COLDPDistribution> distributionReader 
                    = new CSVReader<>(fileNamePrefix + "distribution.csv", COLDPDistribution.class, ",");
                distributionsByTaxonID = distributionReader.getIntegerKeyedSets(COLDPDistribution::getTaxonID);
            } else {
                distributionsByTaxonID = new HashMap<>();
            }
 
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
            
            COLDPName parentName = names.get(taxonName);
            COLDPTaxon parent = taxaByNameID.get(parentName.getID());
            Stack<COLDPTaxon> higherTaxa = getHigherTaxa(parent);
            
            writeTaxon(writer, null, higherTaxa, generateFullHtmlPage ? 2 : 0);

            if (generateFullHtmlPage) {
                writer.println("    </body>");
                writer.println("</html>");
            }
        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            java.util.logging.Logger.getLogger(COLDPFormatterCSS.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static Stack<COLDPTaxon> getHigherTaxa(COLDPTaxon taxon) {
        Stack<COLDPTaxon> higherTaxa = new Stack<>();
        
        higherTaxa.push(taxon);
        while (taxon != null && taxon.getParentID() != null) {
            taxon = taxa.get(taxon.getParentID());
            if (taxon != null) {
                higherTaxa.push(taxon);
            }
        }
        
        return higherTaxa;
    }
    
    private static void writeTaxon(PrintWriter writer, COLDPTaxon taxon, Stack<COLDPTaxon> higherTaxa, int depth) {
        if (higherTaxa != null) {
            taxon = higherTaxa.pop();
            if (higherTaxa.empty()) {
                higherTaxa = null;
            }
        }
        
        String prefix = "";
        for (int i = 0; i < depth; i++) prefix += INDENT;
	String childPrefix = prefix + INDENT;
		
        Set<COLDPReference> referenceList = new TreeSet<>(new BibliographicSort());
        if (taxon.getReferenceID() != null) {
            referenceList.add(references.get(taxon.getReferenceID()));
        }
        
        COLDPName name = namesByID.get(taxon.getNameID());
        String divClass = (higherTaxa == null) ? upperFirst(name.getRank()) : "HigherTaxon";
	writer.println(prefix + "<div class=\"" + divClass + "\">");
        writeName(writer, name, childPrefix, referenceList, false, null);
        
        Set<COLDPSynonym> synonyms = synonymsByTaxonID.get(taxon.getID());
        if (synonyms != null && synonyms.size() > 0) {
            writer.println(childPrefix + "<div class=\"Synonyms\">");
            
            for (COLDPSynonym synonym : synonyms) {
                writeSynonym(writer, synonym, childPrefix + INDENT, referenceList);
            }

            writer.println(childPrefix + "</div>");
        }
                
        if (referenceList.size() > 0) {
            writer.println(childPrefix + "<div class=\"References\">");
            
            for (COLDPReference reference : referenceList) {
                writeReference(writer, reference, childPrefix + INDENT);
            }

            writer.println(childPrefix + "</div>");
        }

        if (taxon.getRemarks() != null) {
            writer.println(prefix + wrapDiv("Note", "Note: " + wrapEmphasis(linkURLs(taxon.getRemarks()))));
        }

        if (higherTaxa == null) {
            Set<COLDPTaxon> children = childrenByParentID.get(taxon.getID());
            if (children != null) {
                for (COLDPTaxon child : children) {
                    writeTaxon(writer, child, null, depth + 1);
                }
            }
        } else {
            writeTaxon(writer, null, higherTaxa, depth + 1);
        }
        
        writer.println(prefix + "</div>");
    }

    private static void writeName(PrintWriter writer, COLDPName name, 
            String prefix, Set<COLDPReference> referenceList, boolean isSynonym,
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
    
    private static void writeNameInformation(PrintWriter writer, COLDPName name, String prefix, 
                Set<COLDPReference> referenceList, boolean isSynonym) {
        if (name.getReferenceID() != null) {
            COLDPReference reference = references.get(name.getReferenceID());
            
            referenceList.add(reference);
        }
        
        Set<COLDPNameReference> nameReferences = nameReferencesByNameID.get(name.getID());
        if (nameReferences != null) {
            for (COLDPNameReference nameReference : nameReferences) {
                COLDPReference reference = references.get(nameReference.getReferenceID());
                if (reference == null) {
                    LOG.error("Reference missing for NameReference: " + nameReference.toString());
                }
                referenceList.add(reference);
                writeNameReference(writer, nameReference, reference, prefix);
            }
        }

        writeNameRelations(writer, name, prefix, referenceList);
        
        if (!isSynonym && name.getRemarks() != null) {
            writer.println(prefix + wrapDiv("Note", "Note: " + wrapEmphasis(linkURLs(name.getRemarks()))));
        }
    }
    
    private static void writeNameRelations(PrintWriter writer, COLDPName name, String prefix, Set<COLDPReference> referenceList) {
        Set<COLDPNameRelation> relations = relationsByNameID.get(name.getID());
        if (relations != null) {
            for (COLDPNameRelation relation : relations) {
                writeNameRelation(writer, relation, prefix, true, name.getRank(), referenceList);
            }
        }

        relations = relationsByRelatedNameID.get(name.getID());
        if (relations != null) {
            for (COLDPNameRelation relation : relations) {
                writeNameRelation(writer, relation, prefix, false, name.getRank(), referenceList);
            }
        }
    }

    private static void writeNameRelation(PrintWriter writer, COLDPNameRelation relation, 
            String prefix, boolean isSubject, String rankName, Set<COLDPReference> referenceList) {
        COLDPName name = namesByID.get(isSubject ? relation.getRelatedNameID() : relation.getNameID());
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
                case "Basionym":
                    formatted = "Basionym for";
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
                COLDPReference reference = references.get(relation.getReferenceID());
                referenceList.add(reference);
            }
        }
    }

    private static void writeSynonym(PrintWriter writer, COLDPSynonym synonym, String prefix, Set<COLDPReference> referenceList) {
        COLDPName name = namesByID.get(synonym.getNameID());
        String synonymRemarks = safeTrim(linkURLs(synonym.getRemarks()));
        
        if(synonymRemarks == null && !synonym.getStatus().equalsIgnoreCase("synonym")) {
            synonymRemarks = upperFirst(synonym.getStatus());
        }

        writer.println(prefix + "<div class=\"Synonym\">");
        
        writeName(writer, name, prefix + INDENT, referenceList, true, synonymRemarks);

        if (synonym.getReferenceID() != null) {
            COLDPReference reference = references.get(synonym.getReferenceID());
            referenceList.add(reference);
        }
        
        writer.println(prefix + "</div>");
    }

    private static void writeReference(PrintWriter writer, COLDPReference reference, String prefix) {
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

    private static void writeNameReference(PrintWriter writer, COLDPNameReference nameReference, COLDPReference reference, String prefix) {
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

    private static String formatName(COLDPName name) {
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
        writer.println(buildCSV(values));
    }
}
