/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.dhobern.coldp;

import io.github.dhobern.coldp.COLDPReference.BibliographicSort;
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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
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
    private List<COLDPSpeciesInteraction> speciesInteractions;
    
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
    
    public COLDPSpeciesInteraction newSpeciesInteraction() {
        COLDPSpeciesInteraction speciesInteraction = new COLDPSpeciesInteraction();
        speciesInteractions.add(speciesInteraction);
        return speciesInteraction;
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
            name.setRank(rankEnum.toString());
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
                            ? reference.getIssued()
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
                taxon.fixHierarchy(false, false, false);
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

    public List<COLDPSpeciesInteraction> getSpeciesInteractions() {
        return speciesInteractions;
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
            if (    name.getScientificName() != null &&
                    name.getScientificName().equals(scientificName)) {
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

    public List<COLDPSynonym> findSynonyms(Optional<COLDPTaxon> taxon,
            Optional<COLDPName> name) {
        return synonyms.stream().filter(s -> {
            if (taxon != null && !taxon.equals(Optional.ofNullable(s.getTaxon()))) return false;
            if (name != null && !name.equals(Optional.ofNullable(s.getName()))) return false;
            return true;
        }).collect(Collectors.toList());
    }

    public List<COLDPDistribution> getDistributions() {
        return distributions;
    }
    
    public List<COLDPDistribution> findDistributions(Optional<COLDPTaxon> taxon,
            Optional<COLDPRegion> region, Optional<COLDPReference> reference) {
        return distributions.stream().filter(d -> {
            if (taxon != null && !taxon.equals(Optional.ofNullable(d.getTaxon()))) return false;
            if (region != null && !region.equals(Optional.ofNullable(d.getRegion()))) return false;
            if (reference != null && !reference.equals(Optional.ofNullable(d.getReference()))) return false;
            return true;
        }).collect(Collectors.toList());
    }

    public Map<String, COLDPRegion> getRegions() {
        return regions;
    }
    
    public boolean deleteReference(COLDPReference r) {
        if (r != null) {
            for (COLDPNameReference nr : r.getNameReferences()) {
                deleteNameReference(nr);
            }
            for (COLDPName name : r.getNames()) {
                name.setReference(null);
            }
            for (COLDPTaxon t : r.getTaxa()) {
                t.setReference(null);
            }
            for (COLDPSynonym s : r.getSynonyms()) {
                s.setReference(null);
            }
            for (COLDPNameRelation nr : r.getNameRelations()) {
                nr.setReference(null);
            }
            for (COLDPDistribution d : r.getDistributions()) {
                d.setReference(null);
            }
            references.remove(r.getID());
            return true;
        }
        
        return false;
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

    public boolean deleteNameRelation(COLDPNameRelation nr) {
        if (nr != null) {
            nr.setName(null);
            nr.setRelatedName(null);
            nr.setReference(null);
            nameRelations.remove(nr);
            return true;
        }
        
        return false;
    }

    public boolean deleteDistribution(COLDPDistribution d) {
        if (d != null) {
            d.setTaxon(null);
            d.setRegion(null);
            d.setReference(null);
            distributions.remove(d);
            return true;
        }
        
        return false;
    }

    public boolean deleteSynonym(COLDPSynonym syn) {
        if (syn != null) {
            if (syn.getTaxon() != null) {
                syn.getTaxon().deregisterSynonym(syn);
            }
            if (syn.getName() != null) {
                syn.getName().deregisterSynonym(syn);
            }
            syn.setTaxon(null);
            syn.setName(null);
            syn.setReference(null);
            synonyms.remove(syn);
            return true;
        }
        
        return false;
    }
    
    public boolean deleteSpeciesInteraction(COLDPSpeciesInteraction si) {
        if (si != null) {
            si.setTaxon(null);
            si.setRelatedTaxon(null);
            si.setReference(null);
            speciesInteractions.remove(si);
            return true;
        }
        
        return false;
    }
    
    public boolean deleteTaxon(COLDPTaxon t) {
        return deleteTaxon(t, false);
    }
    
    public boolean deleteTaxon(COLDPTaxon t, boolean recurse) {
        if (t != null) {
            COLDPTaxon taxon = taxa.get(t.getID());
            if (taxon != null && t == taxon) {
                COLDPTaxon parent = taxon.getParent();
                Set<COLDPTaxon> children = taxon.getChildren();
                while (children != null && children.size() > 0) {
                    if (recurse) {
                        deleteName(children.iterator().next().getName());
                    } else {
                        children.iterator().next().setParent(parent);
                    }
                }
                taxon.setReference(null);
                Set<COLDPDistribution> dists = taxon.getDistributions();
                while (dists != null && dists.size() > 0) {
                    deleteDistribution(dists.iterator().next());
                }
                List<COLDPSpeciesInteraction> sis = taxon.getSpeciesInteractions();
                while (sis != null && sis.size() > 0) {
                    deleteSpeciesInteraction(sis.get(0));
                }
                sis = taxon.getRelatedSpeciesInteractions();
                while (sis != null && sis.size() > 0) {
                    deleteSpeciesInteraction(sis.get(0));
                }
                if (parent != null) {
                    taxon.setParent(null);
                }
                taxon.setName(null);
                taxa.remove(taxon.getID());
                return true;
            }
        }
        
        return false;
    }
    
    public boolean deleteName(COLDPName n) {
        return deleteName(n, false);
    }
    
    public boolean deleteName(COLDPName n, boolean recurse) {
        if (n != null) {
            COLDPName name = names.get(n.getID());
            if (name != null && n == name) {
                List<COLDPSynonym> syns = name.getSynonyms();
                while (syns != null && syns.size() > 0) {
                    deleteSynonym(syns.get(0));
                }
                if (name.getTaxon() != null) {
                    deleteTaxon(name.getTaxon(), recurse);
                }
                name.setReference(null);
                List<COLDPNameRelation> nrels = name.getNameRelations();
                while (nrels != null && nrels.size() > 0) {
                    deleteNameRelation(nrels.get(0));
                }
                List<COLDPNameRelation> rnrels = name.getRelatedNameRelations();
                while (rnrels != null && rnrels.size() > 0) {
                    deleteNameRelation(rnrels.get(0));
                }
                List<COLDPNameReference> nrefs = name.getNameReferences();
                while (nrefs != null && nrefs.size() > 0) {
                    deleteNameReference(nrefs.get(0));
                }
                Set<COLDPName> combs = name.getCombinations();
                while (combs != null && combs.size() > 0) {
                    combs.iterator().next().setBasionym(null);
                }
                name.setBasionym(null);
                names.remove(name.getID());
                return true;
            }
        }
        
        return false;
    }

    private COLDataPackage() {
    }
    
    public COLDataPackage(String folderName) {
        this(folderName, null, ",");
    }

    public COLDataPackage(String folderName, String separator) {
        this(folderName, null, separator);
    }

    public COLDataPackage(String folderName, IdentifierType identifierType) {
        this(folderName, identifierType, ",");
    }

    public COLDataPackage(String folderName, IdentifierType identifierType, String separator) {
        if (!folderName.endsWith("/")) {
            folderName += "/";
        }
        
        taxonIdentifierPolicy = new IdentifierPolicy(identifierType);
        nameIdentifierPolicy = new IdentifierPolicy(identifierType);
        referenceIdentifierPolicy = new IdentifierPolicy(identifierType);

        try {
            CSVReader<COLDPReference> referenceReader 
                    = new CSVReader<>(folderName + "reference.csv", COLDPReference.class, separator);
            references = referenceReader.getMap(COLDPReference::getID);

            CSVReader<COLDPName> nameReader
                    = new CSVReader<>(folderName + "name.csv", COLDPName.class, separator);
            names = nameReader.getMap(COLDPName::getID);
            
            if (new File(folderName + "namereference.csv").exists()) {
                CSVReader<COLDPNameReference> nameReferenceReader 
                        = new CSVReader<>(folderName + "namereference.csv", COLDPNameReference.class, separator);
                nameReferences = nameReferenceReader.getList();
            } else {
                nameReferences = new ArrayList<>();
            }

            if (new File(folderName + "namerelation.csv").exists()) {
                CSVReader<COLDPNameRelation> nameRelationReader 
                        = new CSVReader<>(folderName + "namerelation.csv", COLDPNameRelation.class, separator);
                nameRelations = nameRelationReader.getList();
            } else {
                nameRelations = new ArrayList<>();
            }
 
            CSVReader<COLDPTaxon> taxonReader 
                    = new CSVReader<>(folderName + "taxon.csv", COLDPTaxon.class, separator);
            taxa = taxonReader.getMap(COLDPTaxon::getID);

            CSVReader<COLDPSynonym> synonymReader = new CSVReader<>(folderName + "synonym.csv", COLDPSynonym.class, separator);
            synonyms = synonymReader.getList();
 
            if (new File(folderName + "region.csv").exists()) {
                CSVReader<COLDPRegion> regionReader 
                        = new CSVReader<>(folderName + "region.csv", COLDPRegion.class, separator);
                regions = regionReader.getMap(COLDPRegion::getID);
            } else {
                regions = new HashMap<>();
            }

            if (new File(folderName + "distribution.csv").exists()) {
                CSVReader<COLDPDistribution> distributionReader 
                        = new CSVReader<>(folderName + "distribution.csv", COLDPDistribution.class, separator);
                distributions = distributionReader.getList();
            } else {
                distributions = new ArrayList<>();
            }
            
            if (new File(folderName + "speciesinteraction.csv").exists()) {
                CSVReader<COLDPSpeciesInteraction> speciesInteractionReader 
                        = new CSVReader<>(folderName + "speciesinteraction.csv", COLDPSpeciesInteraction.class, separator);
                speciesInteractions = speciesInteractionReader.getList();
            } else {
                speciesInteractions = new ArrayList<>();
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
                        LOG.error("Taxon " + synonym.getTaxonID() + " not found for synonym");
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

            for (COLDPSpeciesInteraction speciesInteraction : speciesInteractions) {
                speciesInteraction.setTaxon(taxa.get(speciesInteraction.getTaxonID()));
                if (speciesInteraction.getRelatedTaxonID() != null) {
                    COLDPTaxon relatedTaxon = taxa.get(speciesInteraction.getRelatedTaxonID());
                    if (relatedTaxon == null) {
                        LOG.error("SpeciesInteraction " + speciesInteraction.getRelatedTaxonID() + " not found for speciesInteraction " + speciesInteraction.toString());
                    } else {
                        speciesInteraction.setRelatedTaxon(relatedTaxon);
                    }
                }
                if (speciesInteraction.getReferenceID() != null) {
                    COLDPReference reference = references.get(speciesInteraction.getReferenceID());
                    if (reference == null) {
                        LOG.error("SpeciesInteraction " + speciesInteraction.getReferenceID() + " not found for speciesInteraction " + speciesInteraction.toString());
                    } else {
                        speciesInteraction.setReference(reference);
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
        List<COLDPName> sortedNames = new ArrayList<>();
        
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

        writer = safeFileOpen(folderName, "speciesinteraction", suffix, overwrite);
        if (writer != null) {
            writer.println(COLDPSpeciesInteraction.getCsvHeader());
            for(COLDPSpeciesInteraction speciesInteraction : speciesInteractions) {
                writer.println(speciesInteraction.toCsv());
            }
            writer.close();
        }
    }

    COLDPName findNameByGenderAgnosticScientific(String scientificName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    void pruneTaxon(COLDPTaxon taxon) {
        if (taxon != null && taxa.get(taxon.getID()).equals(taxon)) {
            COLDPTaxon parent = taxon.getParent();
            if (parent != null) {
                while (parent.getChildren().size() > 1) {
                    for (COLDPTaxon child : parent.getChildren()) {
                        if (!Objects.equals(taxon, child)) {
                            deleteName(child.getName());
                            break;
                        }
                    }
                }
                pruneTaxon(parent);
            }
        }
    }
}
