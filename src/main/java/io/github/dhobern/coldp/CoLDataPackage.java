/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.dhobern.coldp;

import io.github.dhobern.coldp.COLDPReference.BibliographicSort;
import io.github.dhobern.coldp.COLDPTaxon.AlphabeticalSortByScientificName;
import io.github.dhobern.utils.CSVReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author stang
 */
public class COLDataPackage {
    
    private static final Logger LOG = LoggerFactory.getLogger(COLDataPackage.class);
    
    private Map<Integer,COLDPName> names;
    private Map<Integer,COLDPReference> references;
    private List<COLDPNameReference> nameReferences;
    private List<COLDPSynonym> synonyms;
    private List<COLDPNameRelation> nameRelations;
    private Map<Integer,COLDPTaxon> taxa;
    private List<COLDPDistribution> distributions;
    private Map<String,COLDPRegion> regions;
    
    public Map<Integer, COLDPName> getNames() {
        return names;
    }

    public Map<Integer, COLDPReference> getReferences() {
        return references;
    }

    public List<COLDPNameReference> getNameReferences() {
        return nameReferences;
    }

    public List<COLDPSynonym> getSynonyms() {
        return synonyms;
    }

    public List<COLDPNameRelation> getNameRelations() {
        return nameRelations;
    }

    public Map<Integer, COLDPTaxon> getTaxa() {
        return taxa;
    }

    public List<COLDPTaxon> getRootTaxa() {
        List<COLDPTaxon> rootTaxa = new ArrayList<>();
        for (COLDPTaxon taxon : taxa.values()) {
            if (taxon.getParent() == null) {
                rootTaxa.add(taxon);
            }
        }
        return rootTaxa;
    }

    public COLDPTaxon getTaxonByName(String name) {
        for (COLDPTaxon taxon : taxa.values()) {
            if (taxon.getName().getScientificName().equals(name)) {
                return taxon;
            }
        }
        return null;
    }

    public List<COLDPDistribution> getDistributions() {
        return distributions;
    }

    public Map<String, COLDPRegion> getRegions() {
        return regions;
    }

    private COLDataPackage() {
    }
    
    public COLDataPackage(String folderName) {
        if (!folderName.endsWith("/")) {
            folderName += "/";
        }
            
        try {
            CSVReader<COLDPReference> referenceReader 
                    = new CSVReader<>(folderName + "reference.csv", COLDPReference.class, ",");
            references = referenceReader.getIntegerMap(COLDPReference::getID);

            CSVReader<COLDPName> nameReader
                    = new CSVReader<>(folderName + "name.csv", COLDPName.class, ",");
            names = nameReader.getIntegerMap(COLDPName::getID);
            
            CSVReader<COLDPNameReference> nameReferenceReader 
                    = new CSVReader<>(folderName + "namereference.csv", COLDPNameReference.class, ",");
            nameReferences = nameReferenceReader.getList();

            CSVReader<COLDPNameRelation> nameRelationReader 
                    = new CSVReader<>(folderName + "namerelation.csv", COLDPNameRelation.class, ",");
            nameRelations = nameRelationReader.getList();
 
            CSVReader<COLDPTaxon> taxonReader 
                    = new CSVReader<>(folderName + "taxon.csv", COLDPTaxon.class, ",");
            taxa = taxonReader.getIntegerMap(COLDPTaxon::getID);

            CSVReader<COLDPSynonym> synonymReader = new CSVReader<>(folderName + "synonym.csv", COLDPSynonym.class, ",");
            synonyms = synonymReader.getList();
 
            if (new File(folderName + "region.csv").exists()) {
                CSVReader<COLDPRegion> regionReader 
                        = new CSVReader<>(folderName + "region.csv", COLDPRegion.class, ",");
                regions = regionReader.getMap(COLDPRegion::getID);
            } else {
                regions = new HashMap<>();
            }

            if (new File(folderName + "distribution.csv").exists()) {
                CSVReader<COLDPDistribution> distributionReader 
                        = new CSVReader<>(folderName + "distribution.csv", COLDPDistribution.class, ",");
                distributions = distributionReader.getList();
            } else {
                distributions = new ArrayList<>();
            }
 
            for(COLDPName name : names.values()) {
                if (name.getBasionymID() != null) {
                    COLDPName basionym = names.get(name.getBasionymID());
                    if (basionym == null) {
                        LOG.error("Basionym " + name.getBasionymID() + " not found for name " + name.getID());
                    } else {
                        name.setBasionym(basionym);
                    }
                }
                if (name.getReferenceID() != null) {
                    COLDPReference reference = references.get(name.getReferenceID());
                    if (reference == null) {
                        LOG.error("Reference " + name.getReferenceID() + " not found for name " + name.getID());
                    } else {
                        name.setReference(reference);
                    }
                }
            }
            
            for(COLDPNameReference nr : nameReferences) {
                nr.setName(names.get(nr.getNameID()));
                nr.setReference(references.get(nr.getReferenceID()));
            }
            
            for (COLDPNameRelation r: nameRelations) {
                r.setName(names.get(r.getNameID()));
                r.setRelatedName(names.get(r.getRelatedNameID()));
                if (r.getReferenceID() != null) {
                    COLDPReference reference = references.get(r.getReferenceID());
                    if (reference == null) {
                        LOG.error("Reference " + r.getReferenceID() + " not found for nameRelation " + r.toString());
                    } else {
                        r.setReference(reference);
                    }
                }
            }
            
            for(COLDPTaxon taxon : taxa.values()) {
                // Scientific name is used in sorting taxa so set this first
                taxon.setName(names.get(taxon.getNameID()));
                if (taxon.getParentID() != null) {
                    COLDPTaxon parent = taxa.get(taxon.getParentID());
                    if (parent == null) {
                        LOG.error("Parent " + taxon.getParentID() + " not found for taxon " + taxon.getID());
                    } else {
                        taxon.setParent(parent);
                    }
                }
                if (taxon.getReferenceID() != null) {
                    COLDPReference reference = references.get(taxon.getReferenceID());
                    if (reference == null) {
                        LOG.error("Reference " + taxon.getReferenceID() + " not found for taxon " + taxon.getID());
                    } else {
                        taxon.setReference(reference);
                    }
                }
            }
            
            for(COLDPSynonym synonym : synonyms) {
                if (synonym.getTaxonID() != null) {
                     COLDPTaxon taxon = taxa.get(synonym.getTaxonID());
                    if (taxon == null) {
                        LOG.error("Taxon " + synonym.getTaxonID() + " not found for synonym " + synonym.toString());
                    } else {
                        synonym.setTaxon(taxon);
                    }
                }
                if (synonym.getNameID() != null) {
                     COLDPName name = names.get(synonym.getNameID());
                    if (name == null) {
                        LOG.error("Name " + synonym.getNameID() + " not found for synonym " + synonym.toString());
                    } else {
                        synonym.setName(name);
                    }
                }
                if (synonym.getReferenceID() != null) {
                    COLDPReference reference = references.get(synonym.getReferenceID());
                    if (reference == null) {
                        LOG.error("Reference " + synonym.getReferenceID() + " not found for synonym " + synonym.toString());
                    } else {
                        synonym.setReference(reference);
                    }
                }
            }
            
            for (COLDPDistribution distribution : distributions) {
                // Region is used in sorting distributions so set this first
                distribution.setRegion(regions.get(distribution.getArea()));
                distribution.setTaxon(taxa.get(distribution.getTaxonID()));
                if (distribution.getReferenceID() != null) {
                    COLDPReference reference = references.get(distribution.getReferenceID());
                    if (reference == null) {
                        LOG.error("Distribution " + distribution.getReferenceID() + " not found for distribution " + distribution.toString());
                    } else {
                        distribution.setReference(reference);
                    }
                }
            }

        } catch (UnsupportedEncodingException | FileNotFoundException ex) {
            java.util.logging.Logger.getLogger(COLDataPackage.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void tidyIdentifiers() {
        Set<COLDPReference> sortedReferences = new TreeSet<>(new BibliographicSort());
        for (COLDPReference reference : references.values()) {
            sortedReferences.add(reference);
        }
        references = new HashMap<>();
        int id = 1;
        for (COLDPReference reference : sortedReferences) {
            reference.setID(id++);
            references.put(reference.getID(), reference);
        }

        List<COLDPTaxon> sortedTaxa = new ArrayList<>();
        List<COLDPName> sortedNames = new ArrayList();
        
        List<COLDPTaxon> rootTaxa = getRootTaxa();
        
        id = 1;
        for (COLDPTaxon root : rootTaxa) {
            id = tidyNameAndTaxonIdentifiers(root, id, sortedTaxa, sortedNames);
        }
        for (COLDPTaxon taxon : taxa.values()) {
            taxon.setID(++id);
            sortedTaxa.add(taxon);
        }
        for (COLDPName name : names.values()) {
            name.setID(++id);
            sortedNames.add(name);
        }
        
        taxa = new HashMap<>();
        for (COLDPTaxon taxon : sortedTaxa) {
            taxa.put(taxon.getID(), taxon);
        }

        names = new HashMap<>();
        for (COLDPName name : sortedNames) {
            names.put(name.getID(), name);
        }
    }
    
    private int tidyNameAndTaxonIdentifiers(COLDPTaxon taxon, int id, List<COLDPTaxon> sortedTaxa, List<COLDPName> sortedNames) {
        COLDPName name = taxon.getName();
        
        taxa.remove(taxon.getID());
        names.remove(name.getID());
        
        taxon.setID(id);
        name.setID(id++);

        sortedTaxa.add(taxon);
        sortedNames.add(name);
        
        if (taxon.getSynonyms() != null) {
            for (COLDPSynonym synonym : taxon.getSynonyms()) {
                COLDPName sname = synonym.getName();
                names.remove(sname.getID());
                sname.setID(id++);
                sortedNames.add(sname);
            }
        }

        if (taxon.getChildren() != null) {
            for (COLDPTaxon child : taxon.getChildren()) {
                id = tidyNameAndTaxonIdentifiers(child, id, sortedTaxa, sortedNames);
            }
        }
        
        return id;
    }
    
    public void write(String folderName, String suffix) {
        if (!folderName.endsWith("/")) {
            folderName += "/";
        }
        
        try {
           
            PrintWriter writer = new PrintWriter(folderName + "name" + suffix + ".csv", "UTF-8");
            writer.println(COLDPName.getCsvHeader());
            for(COLDPName name : names.values()) {
                writer.println(name.toCSV());
            }
            writer.close();
            
            writer = new PrintWriter(folderName + "reference" + suffix + ".csv", "UTF-8");
            writer.println(COLDPReference.getCsvHeader());
            for(COLDPReference reference : references.values()) {
                writer.println(reference.toCsv());
            }
            writer.close();
            
            writer = new PrintWriter(folderName + "namereference" + suffix + ".csv", "UTF-8");
            writer.println(COLDPNameReference.getCsvHeader());
            for(COLDPNameReference nameReference : nameReferences) {
                writer.println(nameReference.toCsv());
            }
            writer.close();
            
            writer = new PrintWriter(folderName + "taxon" + suffix + ".csv", "UTF-8");
            writer.println(COLDPTaxon.getCsvHeader());
            for(COLDPTaxon taxon : taxa.values()) {
                writer.println(taxon.toCsv());
            }
            writer.close();
            
            writer = new PrintWriter(folderName + "synonym" + suffix + ".csv", "UTF-8");
            writer.println(COLDPSynonym.getCsvHeader());
            for(COLDPSynonym synonym : synonyms) {
                writer.println(synonym.toCsv());
            }
            writer.close();
            
            writer = new PrintWriter(folderName + "namerelation" + suffix + ".csv", "UTF-8");
            writer.println(COLDPNameRelation.getCsvHeader());
            for(COLDPNameRelation nameRelation : nameRelations) {
                writer.println(nameRelation.toCsv());
            }
            writer.close();
            
            writer = new PrintWriter(folderName + "region" + suffix + ".csv", "UTF-8");
            writer.println(COLDPRegion.getCsvHeader());
            for(COLDPRegion region : regions.values()) {
                writer.println(region.toCsv());
            }
            writer.close();
            
            writer = new PrintWriter(folderName + "distribution" + suffix + ".csv", "UTF-8");
            writer.println(COLDPDistribution.getCsvHeader());
            for(COLDPDistribution distribution : distributions) {
                writer.println(distribution.toCsv());
            }
            writer.close();
        } catch (IOException ex) {
            LOG.error(ex.toString());
        }
    }
}
