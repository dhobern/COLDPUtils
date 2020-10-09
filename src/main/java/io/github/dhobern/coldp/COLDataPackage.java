/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.dhobern.coldp;

import io.github.dhobern.coldp.COLDPReference.BibliographicSort;
import io.github.dhobern.coldp.COLDPTaxon.AlphabeticalSortByScientificName;
import io.github.dhobern.coldp.IdentifierPolicy.IdentifierType;
import io.github.dhobern.utils.CSVReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author stang
 */
public class COLDataPackage {
    
    private static final Logger LOG = LoggerFactory.getLogger(COLDataPackage.class);
    
    private Map<String,COLDPName> names;
    private Map<String,COLDPReference> references;
    private List<COLDPNameReference> nameReferences;
    private List<COLDPSynonym> synonyms;
    private List<COLDPNameRelation> nameRelations;
    private Map<String,COLDPTaxon> taxa;
    private List<COLDPDistribution> distributions;
    private Map<String,COLDPRegion> regions;
    
    private IdentifierPolicy taxonIdentifierPolicy;
    private IdentifierPolicy nameIdentifierPolicy;
    private IdentifierPolicy referenceIdentifierPolicy;
    
    public COLDPTaxon newTaxon() {
        COLDPTaxon taxon = new COLDPTaxon();
        taxon.setID(taxonIdentifierPolicy.nextIdentifier());
        taxa.put(taxon.getID(), taxon);
        return taxon;
    }
    
    public COLDPName newName() {
        COLDPName name = new COLDPName();
        name.setID(nameIdentifierPolicy.nextIdentifier());
        names.put(name.getID(), name);
        return name;
    }

    public COLDPReference newReference() {
        COLDPReference reference = new COLDPReference();
        reference.setID(referenceIdentifierPolicy.nextIdentifier());
        references.put(reference.getID(), reference);
        return reference;
    }

    public COLDPNameReference newNameReference() {
        COLDPNameReference nameReference = new COLDPNameReference();
        nameReferences.add(nameReference);
        return nameReference;
    }
    
    public COLDPNameRelation newNameRelation() {
        COLDPNameRelation nameRelation = new COLDPNameRelation();
        nameRelations.add(nameRelation);
        return nameRelation;
    }
    
    public COLDPSynonym newSynonym() {
        COLDPSynonym synonym = new COLDPSynonym();
        synonyms.add(synonym);
        return synonym;
    }
    
    public COLDPDistribution newDistribution() {
        COLDPDistribution distribution = new COLDPDistribution();
        distributions.add(distribution);
        return distribution;
    }
    
    public COLDPRegion newRegion(String ID) {
        COLDPRegion region = new COLDPRegion();
        region.setID(ID);
        regions.put(ID, region);
        return region;
    }
    
    public COLDPName addName(RankEnum rankEnum, String uninomialOrGenus, 
                             String specificEpithet, String infraspecificEpithet, 
                             String authorship, COLDPName basionym, COLDPTaxon taxon, COLDPTaxon parent,
                             COLDPReference reference, String page, String url, 
                             String nameRemarks, String nameStatus, String taxonStatus,
                             String taxonRemarks, String scrutinizer) {
        String scientificName = COLDPName.getScientificNameFromParts(rankEnum, uninomialOrGenus, specificEpithet, infraspecificEpithet);
        COLDPName name = getNameByScientificName(scientificName);
        if (name != null) {
            LOG.error("Name " + scientificName + " already exists [" + name.getID() + "]");
        } else {
            name = newName();
            name.setRank(rankEnum.getRankName());
            name.setScientificName(scientificName);
            if (rankEnum.isUninomial()) {
                name.setUninomial(uninomialOrGenus);
            } else {
                name.setGenus(uninomialOrGenus);
                name.setSpecificEpithet(specificEpithet);
                name.setInfraspecificEpithet(infraspecificEpithet);
            }
            name.setAuthorship(authorship);
            name.setReference(reference);
            name.setPublishedInPage(page);
            name.setPublishedInYear(reference != null 
                            ? reference.getYear()
                            : (authorship != null ? getYearFromAuthorship(authorship) : null));
            if (taxon != null) {
                COLDPSynonym synonym = newSynonym();
                synonym.setTaxon(taxon);
                synonym.setName(name);
                synonym.setReference(reference);
                synonym.setStatus(taxonStatus);
                synonym.setRemarks(taxonRemarks);
            } else if (parent != null) {
                taxon = newTaxon();
                taxon.setParent(parent);
                taxon.setName(name);
                taxon.fixHierarchy(false);
                taxon.setReference(reference);
                taxon.setRemarks(taxonRemarks);
                taxon.setScrutinizer(scrutinizer);
                taxon.setScrutinizerDate(new SimpleDateFormat("yyyy-MM-dd")
                            .format(Calendar.getInstance().getTime()));

                // The following three lines are a hack for now
                taxon.setExtinct(parent.isExtinct());
                taxon.setLifezone(parent.getLifezone());
                taxon.setTemporalRangeEnd(parent.getTemporalRangeEnd());
            }
        }
        
        return name;
    }
    
    private static String getYearFromAuthorship(String authorship) {
        Pattern pattern = Pattern.compile("[0-9]{4}");
        Matcher matcher = pattern.matcher(authorship);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }
    
    public Map<String, COLDPName> getNames() {
        return names;
    }

    public Map<String, COLDPReference> getReferences() {
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

    public Map<String, COLDPTaxon> getTaxa() {
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

    public COLDPName getNameByScientificName(String scientificName) {
        for (COLDPName name : names.values()) {
            if (name.getScientificName().equals(scientificName)) {
                return name;
            }
        }
        return null;
    }

    public COLDPTaxon getTaxonByScientificName(String name) {
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
    
    public boolean deleteNameReference(COLDPNameReference nr) {
        if (nr != null) {
            nr.setName(null);
            nr.setReference(null);
            nameReferences.remove(nr);
            return true;
        }
        
        return false;
    }

    private COLDataPackage() {
    }
    
    public COLDataPackage(String folderName) {
        this(folderName, null);
    }

    public COLDataPackage(String folderName, IdentifierType identifierType) {
        if (!folderName.endsWith("/")) {
            folderName += "/";
        }
            
        taxonIdentifierPolicy = new IdentifierPolicy(identifierType);
        nameIdentifierPolicy = new IdentifierPolicy(identifierType);
        referenceIdentifierPolicy = new IdentifierPolicy(identifierType);

        try {
            CSVReader<COLDPReference> referenceReader 
                    = new CSVReader<>(folderName + "reference.csv", COLDPReference.class, ",");
            references = referenceReader.getMap(COLDPReference::getID);

            CSVReader<COLDPName> nameReader
                    = new CSVReader<>(folderName + "name.csv", COLDPName.class, ",");
            names = nameReader.getMap(COLDPName::getID);
            
            CSVReader<COLDPNameReference> nameReferenceReader 
                    = new CSVReader<>(folderName + "namereference.csv", COLDPNameReference.class, ",");
            nameReferences = nameReferenceReader.getList();

            CSVReader<COLDPNameRelation> nameRelationReader 
                    = new CSVReader<>(folderName + "namerelation.csv", COLDPNameRelation.class, ",");
            nameRelations = nameRelationReader.getList();
 
            CSVReader<COLDPTaxon> taxonReader 
                    = new CSVReader<>(folderName + "taxon.csv", COLDPTaxon.class, ",");
            taxa = taxonReader.getMap(COLDPTaxon::getID);

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
            
            for (COLDPReference reference : references.values()) {
                referenceIdentifierPolicy.processInstance(reference.getID());
            }
 
            for(COLDPName name : names.values()) {
                nameIdentifierPolicy.processInstance(name.getID());
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
                taxonIdentifierPolicy.processInstance(taxon.getID());
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
            reference.setID(String.valueOf(id++));
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
            taxon.setID(String.valueOf(++id));
            sortedTaxa.add(taxon);
        }
        for (COLDPName name : names.values()) {
            name.setID(String.valueOf(++id));
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
        
        taxon.setID(String.valueOf(id));
        name.setID(String.valueOf(id++));

        sortedTaxa.add(taxon);
        sortedNames.add(name);
        
        if (taxon.getSynonyms() != null) {
            for (COLDPSynonym synonym : taxon.getSynonyms()) {
                COLDPName sname = synonym.getName();
                names.remove(sname.getID());
                sname.setID(String.valueOf(id++));
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
    
    private static PrintWriter safeFileOpen(String folderName, String baseName, 
                        String suffix, boolean overwrite) {
        String fileName = folderName + baseName + suffix + ".csv";
        
        if (!overwrite && (new File(fileName)).exists()) {
            LOG.error("File " + fileName + " exists");
            return null;
        }
        
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(fileName, "UTF-8");
        } catch (IOException e) {
            LOG.error("Failed to open " + fileName +": " + e.toString());
        }
        
        return writer;
    }
    

    public void write(String folderName, String suffix) {
        write(folderName, suffix, true);
    }
    
    public void write(String folderName, String suffix, boolean overwrite) {
        if (!folderName.endsWith("/")) {
            folderName += "/";
        }

        PrintWriter writer = safeFileOpen(folderName, "name", suffix, overwrite);
        if (writer != null) {
            writer.println(COLDPName.getCsvHeader());
            for(COLDPName name : names.values()) {
                writer.println(name.toCSV());
            }
            writer.close();
        }

        writer = safeFileOpen(folderName, "reference", suffix, overwrite);
        if (writer != null) {
            writer.println(COLDPReference.getCsvHeader());
            for(COLDPReference reference : references.values()) {
                writer.println(reference.toCsv());
            }
            writer.close();
        }

        writer = safeFileOpen(folderName, "namereference", suffix, overwrite);
        if (writer != null) {
            writer.println(COLDPNameReference.getCsvHeader());
            for(COLDPNameReference nameReference : nameReferences) {
                writer.println(nameReference.toCsv());
            }
            writer.close();
        }

        writer = safeFileOpen(folderName, "taxon", suffix, overwrite);
        if (writer != null) {
            writer.println(COLDPTaxon.getCsvHeader());
            for(COLDPTaxon taxon : taxa.values()) {
                writer.println(taxon.toCsv());
            }
            writer.close();
        }

        writer = safeFileOpen(folderName, "synonym", suffix, overwrite);
        if (writer != null) {
            writer.println(COLDPSynonym.getCsvHeader());
            for(COLDPSynonym synonym : synonyms) {
                writer.println(synonym.toCsv());
            }
            writer.close();
        }

        writer = safeFileOpen(folderName, "namerelation", suffix, overwrite);
        if (writer != null) {
            writer.println(COLDPNameRelation.getCsvHeader());
            for(COLDPNameRelation nameRelation : nameRelations) {
                writer.println(nameRelation.toCsv());
            }
            writer.close();
        }

        writer = safeFileOpen(folderName, "region", suffix, overwrite);
        if (writer != null) {
            writer.println(COLDPRegion.getCsvHeader());
            for(COLDPRegion region : regions.values()) {
                writer.println(region.toCsv());
            }
            writer.close();
        }

        writer = safeFileOpen(folderName, "distribution", suffix, overwrite);
        if (writer != null) {
            writer.println(COLDPDistribution.getCsvHeader());
            for(COLDPDistribution distribution : distributions) {
                writer.println(distribution.toCsv());
            }
            writer.close();
        }
    }
}
