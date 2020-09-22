/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.dhobern.coldp;

import io.github.dhobern.utils.CSVReader;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
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
    
    private static Map<Integer,CoLDPName> names;
    private static Map<Integer,CoLDPReference> references;
    private static Map<Integer,Set<CoLDPNameReference>> nameReferencesByNameID;
    private static Map<Integer,Set<CoLDPSynonym>> synonymsByTaxonID;
    private static Map<Integer,Set<CoLDPNameRelation>> relationsByNameID;
    private static Map<Integer,CoLDPTaxon> taxa;
    private static Map<Integer,Set<CoLDPDistribution>> distributionsByTaxonID;
    private static Map<String,CoLDPRegion> regions;

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
            nameReferencesByNameID = nameReferenceReader.getIntegerKeyedSets(CoLDPNameReference::getNameID);

            CSVReader<CoLDPNameRelation> nameRelationReader 
                    = new CSVReader<>(folderName + "namerelation.csv", CoLDPNameRelation.class, ",");
            relationsByNameID = nameRelationReader.getIntegerKeyedSets(CoLDPNameRelation::getNameID);
 
            CSVReader<CoLDPTaxon> taxonReader 
                    = new CSVReader<>(folderName + "taxon.csv",
                            CoLDPTaxon.class, ",");
            taxa = taxonReader.getIntegerMap(CoLDPTaxon::getID);

            CSVReader<CoLDPSynonym> synonymReader = new CSVReader<>(folderName + "synonym.csv", CoLDPSynonym.class, ",");
            synonymsByTaxonID = synonymReader.getIntegerKeyedSets(CoLDPSynonym::getTaxonID);
 
            CSVReader<CoLDPRegion> regionReader 
                    = new CSVReader<>(folderName + "region.csv", CoLDPRegion.class, ",");
            regions = regionReader.getMap(CoLDPRegion::getID);

            CSVReader<CoLDPDistribution> distributionReader 
                    = new CSVReader<>(folderName + "distribution.csv", CoLDPDistribution.class, ",");
            distributionsByTaxonID = distributionReader.getIntegerKeyedSets(CoLDPDistribution::getTaxonID);
            
        } catch (UnsupportedEncodingException | FileNotFoundException ex) {
            java.util.logging.Logger.getLogger(CoLDataPackage.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
