/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.dhobern.coldp;

import io.github.dhobern.coldp.CoLDPReference.BibliographicSort;
import io.github.dhobern.coldp.CoLDPTaxon.AlphabeticalSortByScientificName;
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
public class CoLDataPackage {
    
    private static final Logger LOG = LoggerFactory.getLogger(CoLDataPackage.class);
    
    private Map<Integer,CoLDPName> names;
    private Map<Integer,CoLDPReference> references;
    private List<CoLDPNameReference> nameReferences;
    private List<CoLDPSynonym> synonyms;
    private List<CoLDPNameRelation> nameRelations;
    private Map<Integer,CoLDPTaxon> taxa;
    private List<CoLDPDistribution> distributions;
    private Map<String,CoLDPRegion> regions;
    
    public Map<Integer, CoLDPName> getNames() {
        return names;
    }

    public Map<Integer, CoLDPReference> getReferences() {
        return references;
    }

    public List<CoLDPNameReference> getNameReferences() {
        return nameReferences;
    }

    public List<CoLDPSynonym> getSynonyms() {
        return synonyms;
    }

    public List<CoLDPNameRelation> getNameRelations() {
        return nameRelations;
    }

    public Map<Integer, CoLDPTaxon> getTaxa() {
        return taxa;
    }

    public List<CoLDPTaxon> getRootTaxa() {
        List<CoLDPTaxon> rootTaxa = new ArrayList<>();
        for (CoLDPTaxon taxon : taxa.values()) {
            if (taxon.getParent() == null) {
                rootTaxa.add(taxon);
            }
        }
        return rootTaxa;
    }

    public CoLDPTaxon getTaxonByName(String name) {
        for (CoLDPTaxon taxon : taxa.values()) {
            if (taxon.getName().getScientificName().equals(name)) {
                return taxon;
            }
        }
        return null;
    }

    public List<CoLDPDistribution> getDistributions() {
        return distributions;
    }

    public Map<String, CoLDPRegion> getRegions() {
        return regions;
    }

    private CoLDataPackage() {
    }
    
    public CoLDataPackage(String folderName) {
        if (!folderName.endsWith("/")) {
            folderName += "/";
        }
            
        try {
            CSVReader<CoLDPReference> referenceReader 
                    = new CSVReader<>(folderName + "reference.csv", CoLDPReference.class, ",");
            references = referenceReader.getIntegerMap(CoLDPReference::getID);

            CSVReader<CoLDPName> nameReader
                    = new CSVReader<>(folderName + "name.csv", CoLDPName.class, ",");
            names = nameReader.getIntegerMap(CoLDPName::getID);
            
            CSVReader<CoLDPNameReference> nameReferenceReader 
                    = new CSVReader<>(folderName + "namereference.csv", CoLDPNameReference.class, ",");
            nameReferences = nameReferenceReader.getList();

            CSVReader<CoLDPNameRelation> nameRelationReader 
                    = new CSVReader<>(folderName + "namerelation.csv", CoLDPNameRelation.class, ",");
            nameRelations = nameRelationReader.getList();
 
            CSVReader<CoLDPTaxon> taxonReader 
                    = new CSVReader<>(folderName + "taxon.csv", CoLDPTaxon.class, ",");
            taxa = taxonReader.getIntegerMap(CoLDPTaxon::getID);

            CSVReader<CoLDPSynonym> synonymReader = new CSVReader<>(folderName + "synonym.csv", CoLDPSynonym.class, ",");
            synonyms = synonymReader.getList();
 
            if (new File(folderName + "region.csv").exists()) {
                CSVReader<CoLDPRegion> regionReader 
                        = new CSVReader<>(folderName + "region.csv", CoLDPRegion.class, ",");
                regions = regionReader.getMap(CoLDPRegion::getID);
            } else {
                regions = new HashMap<>();
            }

            if (new File(folderName + "distribution.csv").exists()) {
                CSVReader<CoLDPDistribution> distributionReader 
                        = new CSVReader<>(folderName + "distribution.csv", CoLDPDistribution.class, ",");
                distributions = distributionReader.getList();
            } else {
                distributions = new ArrayList<>();
            }
 
            for(CoLDPName name : names.values()) {
                if (name.getBasionymID() != null) {
                    CoLDPName basionym = names.get(name.getBasionymID());
                    if (basionym == null) {
                        LOG.error("Basionym " + name.getBasionymID() + " not found for name " + name.getID());
                    } else {
                        name.setBasionym(basionym);
                    }
                }
                if (name.getReferenceID() != null) {
                    CoLDPReference reference = references.get(name.getReferenceID());
                    if (reference == null) {
                        LOG.error("Reference " + name.getReferenceID() + " not found for name " + name.getID());
                    } else {
                        name.setReference(reference);
                    }
                }
            }
            
            for(CoLDPNameReference nr : nameReferences) {
                nr.setName(names.get(nr.getNameID()));
                nr.setReference(references.get(nr.getReferenceID()));
            }
            
            for (CoLDPNameRelation r: nameRelations) {
                r.setName(names.get(r.getNameID()));
                r.setRelatedName(names.get(r.getRelatedNameID()));
                if (r.getReferenceID() != null) {
                    CoLDPReference reference = references.get(r.getReferenceID());
                    if (reference == null) {
                        LOG.error("Reference " + r.getReferenceID() + " not found for nameRelation " + r.toString());
                    } else {
                        r.setReference(reference);
                    }
                }
            }
            
            for(CoLDPTaxon taxon : taxa.values()) {
                // Scientific name is used in sorting taxa so set this first
                taxon.setName(names.get(taxon.getNameID()));
                if (taxon.getParentID() != null) {
                    CoLDPTaxon parent = taxa.get(taxon.getParentID());
                    if (parent == null) {
                        LOG.error("Parent " + taxon.getParentID() + " not found for taxon " + taxon.getID());
                    } else {
                        taxon.setParent(parent);
                    }
                }
                if (taxon.getReferenceID() != null) {
                    CoLDPReference reference = references.get(taxon.getReferenceID());
                    if (reference == null) {
                        LOG.error("Reference " + taxon.getReferenceID() + " not found for taxon " + taxon.getID());
                    } else {
                        taxon.setReference(reference);
                    }
                }
            }
            
            for(CoLDPSynonym synonym : synonyms) {
                if (synonym.getTaxonID() != null) {
                     CoLDPTaxon taxon = taxa.get(synonym.getTaxonID());
                    if (taxon == null) {
                        LOG.error("Taxon " + synonym.getTaxonID() + " not found for synonym " + synonym.toString());
                    } else {
                        synonym.setTaxon(taxon);
                    }
                }
                if (synonym.getNameID() != null) {
                     CoLDPName name = names.get(synonym.getNameID());
                    if (name == null) {
                        LOG.error("Name " + synonym.getNameID() + " not found for synonym " + synonym.toString());
                    } else {
                        synonym.setName(name);
                    }
                }
                if (synonym.getReferenceID() != null) {
                    CoLDPReference reference = references.get(synonym.getReferenceID());
                    if (reference == null) {
                        LOG.error("Reference " + synonym.getReferenceID() + " not found for synonym " + synonym.toString());
                    } else {
                        synonym.setReference(reference);
                    }
                }
            }
            
            for (CoLDPDistribution distribution : distributions) {
                // Region is used in sorting distributions so set this first
                distribution.setRegion(regions.get(distribution.getArea()));
                distribution.setTaxon(taxa.get(distribution.getTaxonID()));
                if (distribution.getReferenceID() != null) {
                    CoLDPReference reference = references.get(distribution.getReferenceID());
                    if (reference == null) {
                        LOG.error("Distribution " + distribution.getReferenceID() + " not found for distribution " + distribution.toString());
                    } else {
                        distribution.setReference(reference);
                    }
                }
            }

        } catch (UnsupportedEncodingException | FileNotFoundException ex) {
            java.util.logging.Logger.getLogger(CoLDataPackage.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void tidyIdentifiers() {
        Set<CoLDPReference> sortedReferences = new TreeSet<>(new BibliographicSort());
        for (CoLDPReference reference : references.values()) {
            sortedReferences.add(reference);
        }
        references = new HashMap<>();
        int id = 1;
        for (CoLDPReference reference : sortedReferences) {
            reference.setID(id++);
            references.put(reference.getID(), reference);
        }

        List<CoLDPTaxon> sortedTaxa = new ArrayList<>();
        List<CoLDPName> sortedNames = new ArrayList();
        
        List<CoLDPTaxon> rootTaxa = getRootTaxa();
        
        id = 1;
        for (CoLDPTaxon root : rootTaxa) {
            id = tidyNameAndTaxonIdentifiers(root, id, sortedTaxa, sortedNames);
        }
        for (CoLDPTaxon taxon : taxa.values()) {
            taxon.setID(++id);
            sortedTaxa.add(taxon);
        }
        for (CoLDPName name : names.values()) {
            name.setID(++id);
            sortedNames.add(name);
        }
        
        taxa = new HashMap<>();
        for (CoLDPTaxon taxon : sortedTaxa) {
            taxa.put(taxon.getID(), taxon);
        }

        names = new HashMap<>();
        for (CoLDPName name : sortedNames) {
            names.put(name.getID(), name);
        }
    }
    
    private int tidyNameAndTaxonIdentifiers(CoLDPTaxon taxon, int id, List<CoLDPTaxon> sortedTaxa, List<CoLDPName> sortedNames) {
        CoLDPName name = taxon.getName();
        
        taxa.remove(taxon.getID());
        names.remove(name.getID());
        
        taxon.setID(id);
        name.setID(id++);

        sortedTaxa.add(taxon);
        sortedNames.add(name);
        
        if (taxon.getSynonyms() != null) {
            for (CoLDPSynonym synonym : taxon.getSynonyms()) {
                CoLDPName sname = synonym.getName();
                names.remove(sname.getID());
                sname.setID(id++);
                sortedNames.add(sname);
            }
        }

        if (taxon.getChildren() != null) {
            for (CoLDPTaxon child : taxon.getChildren()) {
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
            writer.println(CoLDPName.getCsvHeader());
            for(CoLDPName name : names.values()) {
                writer.println(name.toCSV());
            }
            writer.close();
            
            writer = new PrintWriter(folderName + "reference" + suffix + ".csv", "UTF-8");
            writer.println(CoLDPReference.getCsvHeader());
            for(CoLDPReference reference : references.values()) {
                writer.println(reference.toCsv());
            }
            writer.close();
            
            writer = new PrintWriter(folderName + "namereference" + suffix + ".csv", "UTF-8");
            writer.println(CoLDPNameReference.getCsvHeader());
            for(CoLDPNameReference nameReference : nameReferences) {
                writer.println(nameReference.toCsv());
            }
            writer.close();
            
            writer = new PrintWriter(folderName + "taxon" + suffix + ".csv", "UTF-8");
            writer.println(CoLDPTaxon.getCsvHeader());
            for(CoLDPTaxon taxon : taxa.values()) {
                writer.println(taxon.toCsv());
            }
            writer.close();
            
            writer = new PrintWriter(folderName + "synonym" + suffix + ".csv", "UTF-8");
            writer.println(CoLDPSynonym.getCsvHeader());
            for(CoLDPSynonym synonym : synonyms) {
                writer.println(synonym.toCsv());
            }
            writer.close();
            
            writer = new PrintWriter(folderName + "namerelation" + suffix + ".csv", "UTF-8");
            writer.println(CoLDPNameRelation.getCsvHeader());
            for(CoLDPNameRelation nameRelation : nameRelations) {
                writer.println(nameRelation.toCsv());
            }
            writer.close();
            
            writer = new PrintWriter(folderName + "region" + suffix + ".csv", "UTF-8");
            writer.println(CoLDPRegion.getCsvHeader());
            for(CoLDPRegion region : regions.values()) {
                writer.println(region.toCsv());
            }
            writer.close();
            
            writer = new PrintWriter(folderName + "distribution" + suffix + ".csv", "UTF-8");
            writer.println(CoLDPDistribution.getCsvHeader());
            for(CoLDPDistribution distribution : distributions) {
                writer.println(distribution.toCsv());
            }
            writer.close();
        } catch (IOException ex) {
            LOG.error(ex.toString());
        }
    }
}
