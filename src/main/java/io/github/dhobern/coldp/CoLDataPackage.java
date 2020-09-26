/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.dhobern.coldp;

import io.github.dhobern.utils.CSVReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
                regions = new HashMap<>();
            }
 
            for(CoLDPName name : names.values()) {
                if (name.getBasionymID() != null) {
                    name.setBasionym(names.get(name.getBasionymID()));
                }
                if (name.getReferenceID() != null) {
                    name.setReference(references.get(name.getReferenceID()));
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
                    r.setReference(references.get(r.getReferenceID()));
                }
            }
            
            for(CoLDPTaxon taxon : taxa.values()) {
                if (taxon.getParentID() != null) {
                    taxon.setParent(taxa.get(taxon.getParentID()));
                }
                taxon.setName(names.get(taxon.getNameID()));
                if (taxon.getReferenceID() != null) {
                    taxon.setReference(references.get(taxon.getReferenceID()));
                }
            }
            
            for(CoLDPSynonym synonym : synonyms) {
                if (synonym.getTaxonID() != null) {
                    synonym.setTaxon(taxa.get(synonym.getTaxonID()));
                }
                if (synonym.getNameID() != null) {
                    synonym.setName(names.get(synonym.getNameID()));
                }
                if (synonym.getReferenceID() != null) {
                    synonym.setReference(references.get(synonym.getReferenceID()));
                }
            }
            
            for (CoLDPDistribution distribution : distributions) {
                distribution.setTaxon(taxa.get(distribution.getTaxonID()));
                distribution.setRegion(regions.get(distribution.getArea()));
                if (distribution.getReferenceID() != null) {
                    distribution.setReference(references.get(distribution.getReferenceID()));
                }
            }

        } catch (UnsupportedEncodingException | FileNotFoundException ex) {
            java.util.logging.Logger.getLogger(CoLDataPackage.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void write(String folderName) {
        if (!folderName.endsWith("/")) {
            folderName += "/";
        }
        
        try {
           
            PrintWriter writer = new PrintWriter(folderName + "name-NEW.csv", "UTF-8");
            writer.println(CoLDPName.getCsvHeader());
            for(CoLDPName name : names.values()) {
                writer.println(name.toCSV());
            }
            writer.close();
            
            writer = new PrintWriter(folderName + "reference-NEW.csv", "UTF-8");
            writer.println(CoLDPReference.getCsvHeader());
            for(CoLDPReference reference : references.values()) {
                writer.println(reference.toCsv());
            }
            writer.close();
            
            writer = new PrintWriter(folderName + "namereference-NEW.csv", "UTF-8");
            writer.println(CoLDPNameReference.getCsvHeader());
            for(CoLDPNameReference nameReference : nameReferences) {
                writer.println(nameReference.toCsv());
            }
            writer.close();
            
            writer = new PrintWriter(folderName + "taxon-NEW.csv", "UTF-8");
            writer.println(CoLDPTaxon.getCsvHeader());
            for(CoLDPTaxon taxon : taxa.values()) {
                writer.println(taxon.toCsv());
            }
            writer.close();
            
            writer = new PrintWriter(folderName + "synonym-NEW.csv", "UTF-8");
            writer.println(CoLDPSynonym.getCsvHeader());
            for(CoLDPSynonym synonym : synonyms) {
                writer.println(synonym.toCsv());
            }
            writer.close();
            
            writer = new PrintWriter(folderName + "namerelation-NEW.csv", "UTF-8");
            writer.println(CoLDPNameRelation.getCsvHeader());
            for(CoLDPNameRelation nameRelation : nameRelations) {
                writer.println(nameRelation.toCsv());
            }
            writer.close();
            
            writer = new PrintWriter(folderName + "region-NEW.csv", "UTF-8");
            writer.println(CoLDPRegion.getCsvHeader());
            for(CoLDPRegion region : regions.values()) {
                writer.println(region.toCsv());
            }
            writer.close();
            
            writer = new PrintWriter(folderName + "distribution-NEW.csv", "UTF-8");
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
