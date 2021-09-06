/*
 * Copyright 2020 dhobern@gmail.com.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.dhobern.coldp;

import io.github.dhobern.coldp.TreeRenderProperties.ContextType;
import io.github.dhobern.coldp.TreeRenderProperties.TreeRenderType;
import static io.github.dhobern.utils.ZipUtils.zipFolder;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.DefaultHighlighter;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dhobern@gmail.com
 */
public class InteractiveCommandLine {
    private static final Logger LOG = LoggerFactory.getLogger(InteractiveCommandLine.class);

    private static void subtractPackage(COLDataPackage coldp, String packageName, String separator, boolean ignoreGender) {
        COLDataPackage coldpSubtract = new COLDataPackage(packageName, separator);
        for (COLDPName n : coldpSubtract.getNames().values()){
            if (n.getRankEnum().inSpeciesGroup()) {
                COLDPName name;
                if (ignoreGender) {
                    name = coldp.findNameByGenderAgnosticScientific(n.getScientificName());
                } else {
                    name = coldp.getNameByScientificName(n.getScientificName());
                }
                if (name != null) {
                    COLDPTaxon parent = null;
                    if (name.getTaxon() != null) {
                        parent = name.getTaxon().getParent();
                    }
                    coldp.deleteName(name);
                    while (parent != null) {
                        if (parent.getChildren().size() == 0) {
                            name = parent.getName();
                            parent = parent.getParent();
                            coldp.deleteName(name);
                        } else {
                            parent = null;
                        }
                    }
                }
            }
        }
    }
    
    private COLDPTaxon taxon = null;
    private COLDPName name = null;
    private COLDPReference reference = null;
    private COLDPSynonym synonym = null;
    private COLDPNameReference nameReference = null;
    private COLDPNameRelation nameRelation = null;
    private COLDPDistribution distribution = null;
    private COLDPRegion region = null;
    private COLDPSpeciesInteraction speciesInteraction = null;
    
    private String prompt = "> ";
    
    private LineReader lineReader = null; 
    private Terminal terminal = null;

    public static void main(String[] args) {
        InteractiveCommandLine icl = new InteractiveCommandLine();
        String coldpFolderName = (args.length > 0) ? args[0] : "mockdata";
        String separator = ",";
        
        if (args.length > 1 && Boolean.valueOf(args[1])) {
            separator = "\t";
        }
        String coldpName = coldpFolderName;
        COLDataPackage coldp = new COLDataPackage(coldpName, separator);
        int i = coldpName.lastIndexOf("/");
        if (i > 0) {
            coldpName = coldpName.substring(i + 1);
        }
 
        String backupFileName = coldpName + "-"
                    + DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now()).replace(":", ".")
                    + ".zip";
        
        try {
            zipFolder(coldpFolderName, backupFileName);
        } catch (IOException e) {
            LOG.error("Could not open backup file " + backupFileName, e);
        }

        
        String line;
        while ((line = icl.readLine("", "", true)) != null) {
            String[] words = line.split(" ");
            if (words.length > 0) {
                if (words.length > 1) {
                    line = line.substring(line.indexOf(" ") + 1);
                } else {
                    line = null;
                }
                switch (words[0].toLowerCase()) {
                    case "n": 
                        icl.setName(icl.findInstance(line, coldp.getNames(), coldp.getNames().values()));
                        break;
                    case "b": 
                        if (icl.getName() != null) {
                            icl.setName(icl.getName().getBasionym());
                        }
                        break;
                    case "n+":
                        {
                            COLDPReference reference = icl.getReference();
                            if (reference == null) {
                                String referenceString = icl.readLine("Reference (empty if none)", "", false);
                                reference = icl.findInstance(referenceString, coldp.getReferences(), coldp.getReferences().values());
                            }
                            COLDPName basionym = null;
                            String basionymString = icl.readLine("Basionym (empty if none)", 
                                    (icl.getName() == null || Objects.equals(icl.getName(), icl.getName().getBasionym()))
                                            ? "" : icl.getName().getID(), false);
                            if (basionymString != null && basionymString.length() > 0) {
                                basionym = icl.findInstance(basionymString, coldp.getNames(), coldp.getNames().values());
                            }
                            COLDPName name = coldp.newName();
                            icl.editName(coldp, name, basionym, reference);
                            icl.setName(name);
                        }
                        break;
                    case "n/": 
                        if (icl.getName() != null) {
                            COLDPName name = icl.getName();
                            COLDPReference reference = icl.getReference();
                            COLDPReference selectedReference = null;
                            if (reference != null 
                                    && (name.getReference() == null || !name.getReference().equals(reference))
                                    && icl.getConfirmation("Use currently selected reference " + reference.toString(25, 40) + "?")) {
                                selectedReference = reference;
                            }
                            COLDPName basionym = name.getBasionym();
                            if (    (basionym == null && icl.getConfirmation("Select basionym?"))
                                 || (basionym != null && !basionym.equals(name) 
                                    && icl.getConfirmation("Replace current basionym " + basionym.toString() + "?"))) {
                                String nameString = icl.readLine("Basionym", "", false);
                                if (nameString != null && nameString.length() > 0) {
                                    basionym = icl.findInstance(nameString, coldp.getNames(), coldp.getNames().values());
                                    if (basionym != null) {
                                    }
                                }
                            }
                            icl.editName(coldp, name, basionym, selectedReference);
                        }
                        break;
                    case "t": 
                        icl.setTaxon(icl.findInstance(line, coldp.getTaxa(), coldp.getTaxa().values()));
                        break;
                    case "t/": 
                        if (icl.getTaxon() != null) {
                            icl.editTaxon(coldp, icl.getTaxon(), null);
                        }
                        break;
                    case "t%": 
                        if (icl.getTaxon() != null) {
                            coldp.pruneTaxon(icl.getTaxon());
                        }
                        break;
                    case "a": 
                        icl.setRegion(icl.findInstance(line, coldp.getRegions(), coldp.getRegions().values()));
                        break;
                    case "nr": 
                        {
                            COLDPName name = icl.getName();
                            if (name != null && name.getNameReferences() != null && name.getNameReferences().size() > 0) {
                                if (name.getNameReferences().size() == 1) {
                                    icl.setNameReference(name.getNameReferences().get(0));
                                } else {
                                    String[] nameReferences = new String[name.getNameReferences().size()];
                                    name.getNameReferences().stream().map(nr -> nr.toString()).collect(Collectors.toList()).toArray(nameReferences);
                                    int index = icl.selectFromList(nameReferences);
                                    if (index >= 0 && index < nameReferences.length) {
                                        icl.setNameReference(name.getNameReferences().get(index));
                                    }
                                }
                            }
                        }
                        break;
                    case "nr/":
                        if (icl.getNameReference() != null) {
                            icl.editNameReference(icl.getNameReference());
                        }
                        break;
                    case "nr+":
                        if (icl.getName() != null && icl.getReference() != null) {
                            COLDPNameReference nameReference = coldp.newNameReference();
                            nameReference.setName(icl.getName());
                            nameReference.setReference(icl.getReference());
                            icl.editNameReference(nameReference);
                            icl.setNameReference(nameReference);
                        }
                        break;
                    case "nr-":
                        if (icl.getNameReference() != null && icl.getConfirmation("Delete name reference " + icl.getNameReference().toString() + "?")) {
			    COLDPNameReference nameReference = icl.getNameReference();
                            icl.setNameReference(null);
                            coldp.deleteNameReference(nameReference);
                        }
			break;
                    case "nn":
                        {
                            COLDPName name = icl.getName();
                            if (name != null && name.getNameRelations() != null && name.getNameRelations().size() > 0) {
                                if (name.getNameRelations().size() == 1) {
                                    icl.setNameRelation(name.getNameRelations().get(0));
                                } else {
                                    String[] nameRelations = new String[name.getNameRelations().size()];
                                    name.getNameRelations().stream().map(nr -> nr.toString()).collect(Collectors.toList()).toArray(nameRelations);
                                    int index = icl.selectFromList(nameRelations);
                                    if (index >= 0 && index < nameRelations.length) {
                                        icl.setNameRelation(name.getNameRelations().get(index));
                                    }
                                }
                            }
                        }
                        break;
                    case "rnn":
                        {
                            COLDPName name = icl.getName();
                            if (name != null && name.getRelatedNameRelations() != null 
                                    && name.getRelatedNameRelations().size() > 0) {
                                if (name.getRelatedNameRelations().size() == 1) {
                                    icl.setNameRelation(name.getRelatedNameRelations().get(0));
                                } else {
                                    String[] nameRelations = new String[name.getRelatedNameRelations().size()];
                                    name.getRelatedNameRelations().stream().map(nr -> nr.toString()).collect(Collectors.toList()).toArray(nameRelations);
                                    int index = icl.selectFromList(nameRelations);
                                    if (index >= 0 && index < nameRelations.length) {
                                        icl.setNameRelation(name.getRelatedNameRelations().get(index));
                                    }
                                }
                            }
                        }
                        break;
                    case "p":
                        if (icl.getTaxon() != null 
                            && icl.getTaxon().getParent() != null) {
                            icl.setTaxon(icl.getTaxon().getParent());
                        }
                        break;
                    case "n.t":
                        if (icl.getName() != null) { 
                            icl.setTaxon(icl.getName().getTaxon());
                        }
                        break;
                    case "t.n":
                        if (icl.getTaxon() != null) { 
                            icl.setName(icl.getTaxon().getName());
                        }
                        break;
                    case "n.r":
                        if (icl.getName() != null && icl.getName().getReference() != null) { 
                            icl.setReference(icl.getName().getReference());
                        }
                        break;
                    case "t.r":
                        if (icl.getTaxon() != null && icl.getTaxon().getReference() != null) { 
                            icl.setReference(icl.getTaxon().getReference());
                        }
                        break;
                    case "t.s": 
                        {
                            COLDPTaxon taxon = icl.getTaxon();
                            if (taxon != null && taxon.getSynonyms() != null && taxon.getSynonyms().size() > 0) {
                                if (taxon.getSynonyms().size() == 1) {
                                    icl.setSynonym(taxon.getSynonyms().get(0));
                                } else {
                                    String[] synonyms = new String[taxon.getSynonyms().size()];
                                    taxon.getSynonyms().stream().map(nr -> nr.toString()).collect(Collectors.toList()).toArray(synonyms);
                                    int index = icl.selectFromList(synonyms);
                                    if (index >= 0 && index < synonyms.length) {
                                        icl.setSynonym(taxon.getSynonyms().get(index));
                                    }
                                }
                            }
                        }
                        break;
                    case "t.i":
                        {
                            COLDPTaxon taxon = icl.getTaxon();
                            if (taxon != null && taxon.getSpeciesInteractions() != null && taxon.getSpeciesInteractions().size() > 0) {
                                if (taxon.getSpeciesInteractions().size() == 1) {
                                    icl.setSpeciesInteraction(taxon.getSpeciesInteractions().get(0));
                                } else {
                                    String[] speciesInteractions = new String[taxon.getSpeciesInteractions().size()];
                                    taxon.getSpeciesInteractions().stream().map(nr -> nr.toString()).collect(Collectors.toList()).toArray(speciesInteractions);
                                    int index = icl.selectFromList(speciesInteractions);
                                    if (index >= 0 && index < speciesInteractions.length) {
                                        icl.setSpeciesInteraction(taxon.getSpeciesInteractions().get(index));
                                    }
                                }
                            }
                        }
                        break;
                    case "i!":
                        if (icl.getSpeciesInteraction() != null) {
                            icl.getSpeciesInteraction().render(icl.getWriter(), new TreeRenderProperties(TreeRenderType.TEXT, ContextType.None));
                        }
                        break;
                    case "i/":
                        if (icl.getSpeciesInteraction() != null) {
                            icl.editSpeciesInteraction(icl.getSpeciesInteraction());
                        }
                        break;
                    case "i+":
                        if (icl.getTaxon() != null) {
                            COLDPSpeciesInteraction speciesInteraction = coldp.newSpeciesInteraction();
                            speciesInteraction.setTaxon(icl.getTaxon());
                            speciesInteraction.setReference(icl.getReference());
                            icl.editSpeciesInteraction(speciesInteraction);
                            icl.setSpeciesInteraction(speciesInteraction);
                        }
                        break;
                    case "i<":
                        if (coldp.getSpeciesInteractions() != null) {
                            for (COLDPSpeciesInteraction speciesInteraction : coldp.getSpeciesInteractions()) {
                                if (speciesInteraction.getRelatedTaxonScientificName() != null
                                   && speciesInteraction.getRelatedTaxonHTMLName() == null) {
                                    speciesInteraction.linkToCOL();
                                }
                            }
                        }
                        break;
                    case "n.s": 
                        {
                            COLDPName name = icl.getName();
                            if (name != null && name.getSynonyms() != null && name.getSynonyms().size() > 0) {
                                if (name.getSynonyms().size() == 1) {
                                    icl.setSynonym(name.getSynonyms().get(0));
                                } else {
                                    String[] synonyms = new String[name.getSynonyms().size()];
                                    name.getSynonyms().stream().map(nr -> nr.toString()).collect(Collectors.toList()).toArray(synonyms);
                                    int index = icl.selectFromList(synonyms);
                                    if (index >= 0 && index < synonyms.length) {
                                        icl.setSynonym(name.getSynonyms().get(index));
                                    }
                                }
                            }
                        }
                        break;
                    case "c": 
                        if (icl.getTaxon() != null 
                            && icl.getTaxon().getChildrenSorted() != null) {
                            List<COLDPTaxon> children;
                            if (line != null) {
                                final String filter = line;
                                children = icl.getTaxon()
                                        .getChildrenSorted()
                                        .stream()
                                        .filter(c -> c.toString().contains(filter))
                                        .collect(Collectors.toList());
                            } else {
                                children = new ArrayList<>(icl.getTaxon().getChildrenSorted());
                            }
                            int count = children.size();
                            if (count == 1) {
                                icl.setTaxon(children.get(0));
                            } else if (count > 1) {
                                String[] items = new String[count];
                                i = 0;
                                for (COLDPTaxon c : children) {
                                    items[i++] =  c.toString();
                                }
                                int selection = icl.selectFromList(items);
                                if (selection >= 0) {
                                    icl.setTaxon(children.get(selection));
                                }
                            }
                        } 
                        break;
                    case "r": 
                        icl.setReference(icl.findInstance(line, coldp.getReferences(), coldp.getReferences().values()));
                        break;
                    case "r+":
                        String author = icl.readLine("Author", "", false);
                        String year = icl.readLine("Year", "", "^([0-9]{4})?$", false);
                        String title = icl.readLine("Title", "", false);
                        String source = icl.readLine("Source", "", false);
                        String details = icl.readLine("Details", "", false);
                        String link = icl.readLine("Link", "", false);
                        if (author != null && year != null && title != null) {
                            COLDPReference reference = coldp.newReference();
                            reference.setAuthor(author);
                            reference.setYear(year);
                            reference.setTitle(title);
                            reference.setSource(source);
                            reference.setDetails(details);
                            reference.setLink(link);
                            icl.setReference(reference);
                        }
                        break;
                    case "r/":
                        COLDPReference reference = icl.getReference();
                        if (reference != null) {
                            reference.setAuthor(icl.readLine("Author", reference.getAuthor(), false));
                            reference.setYear(icl.readLine("Year", reference.getYear(), "^([0-9]{4})?$", false));
                            reference.setTitle(icl.readLine("Title", reference.getTitle(), false));
                            reference.setSource(icl.readLine("Source", reference.getSource(), false));
                            reference.setDetails(icl.readLine("Details", reference.getDetails(), false));
                            reference.setLink(icl.readLine("Link", reference.getLink(), false));
                        }
                        break;
                    case "r-":
                        reference = icl.getReference();
                        if (reference != null && icl.getConfirmation("Delete reference " + reference.toString(25, 40) + "?")) {
                            icl.setReference(null);
                            coldp.deleteReference(reference);
                        }
			break;
                    case "w":
                        coldp.write(coldpFolderName, "");
                        break;
                    case "d":
                        if (icl.getTaxon() != null 
                            && icl.getTaxon().getDistributions() != null) {
                            List<COLDPDistribution> distributions;
                            if (line != null) {
                                final String filter = line;
                                distributions = icl.getTaxon()
                                        .getDistributions()
                                        .stream()
                                        .filter(d -> d.toString().contains(filter))
                                        .collect(Collectors.toList());
                            } else {
                                distributions = new ArrayList<>(icl.getTaxon().getDistributions());
                            }
                            int count = distributions.size();
                            if (count == 1) {
                                icl.setDistribution(distributions.get(0));
                            } else if (count > 1) {
                                String[] items = new String[count];
                                i = 0;
                                for (COLDPDistribution c : distributions) {
                                    items[i++] =  c.toString();
                                }
                                int selection = icl.selectFromList(items);
                                if (selection >= 0) {
                                    icl.setDistribution(distributions.get(selection));
                                }
                            }
                        } 
                        break;
                    case "d.t":
                        if (icl.getDistribution() != null) {
                            icl.setTaxon(icl.getDistribution().getTaxon());
                        }
                        break;
                    case "d.r":
                        if (icl.getDistribution() != null) {
                            icl.setReference(icl.getDistribution().getReference());
                        }
                        break;
                    case "d.a":
                        if (icl.getDistribution() != null) {
                            icl.setRegion(icl.getDistribution().getRegion());
                        }
                        break;
                    case "s.t":
                        if (icl.getSynonym() != null) {
                            icl.setTaxon(icl.getSynonym().getTaxon());
                        }
                        break;
                    case "s.r":
                        if (icl.getSynonym() != null) {
                            icl.setReference(icl.getSynonym().getReference());
                        }
                        break;
                    case "s.n":
                        if (icl.getSynonym() != null) {
                            icl.setName(icl.getSynonym().getName());
                        }
                        break;
                    case "d/":
                        COLDPDistribution distribution = icl.getDistribution();
                        if (distribution != null) {
                            String area = distribution.getArea();
                            COLDPRegion region = icl.getRegion();
                            if (region != null && (area == null || !area.equals(region.getID()))
                                && icl.getConfirmation("Use currently selected region " + region.toString() + "?")) {
                                distribution.setRegion(region);
                            }
                            String taxonID = distribution.getTaxonID();
                            COLDPTaxon taxon = icl.getTaxon();
                            if (taxon != null && (taxonID == null || !taxonID.equals(taxon.getID()))
                                && icl.getConfirmation("Use currently selected taxon " + taxon.toString() + "?")) {
                                distribution.setTaxon(taxon);
                            }
                            String referenceID = distribution.getReferenceID();
                            reference = icl.getReference();
                            if (reference != null) {
                                if (referenceID == null || !referenceID.equals(reference.getID())
                                    && icl.getConfirmation("Use currently selected reference " 
                                            + reference.toString(25, 40) + "?")) {
                                    distribution.setReference(reference);
                                }
                            } else {
                                if (referenceID != null && reference == null
                                    && icl.getConfirmation("Remove reference from record " 
                                            + distribution.getReference().toString(25, 40) + "?")) {
                                    distribution.setReference(null);
                                }
                            }
                            distribution.setGazetteer(icl.readEnum(GazetteerEnum.class, "Gazetteer", distribution.getGazetteer(), false).toString());
                            distribution.setStatus(icl.readEnum(DistributionStatusEnum.class, "Status", distribution.getStatus(), false).toString());
                            distribution.setRemarks(icl.readLine("Remarks", distribution.getRemarks(), false));
                        }
                        break;
                    case "d+":
                        if (icl.getTaxon() != null && icl.getRegion() != null) {
                            icl.addDistribution(coldp, null, null, null, null);
                        }
                        break;
                    case "d*a":
                        if (icl.getTaxon() != null && line != null) {
                            String[] tokens = line.split(",");
                            boolean first = true;
                            String gazetteer = null;
                            String status = null;
                            String remarks = null;
                            for (String token : tokens) {
                                COLDPRegion region = icl.findInstance(token.trim(), coldp.getRegions(), coldp.getRegions().values());
                                if (region == null) {
                                    icl.showError("Could not find region for " + token);
                                } else {
                                    if (first) {
                                        gazetteer = icl.readLine("Gazetteer", "iso",
                                            "(tdwg)|(iso)|(fao)|(longhurst)|(teow)|(iho)|(text)", 
                                            false);
                                        status = icl.readLine("Status", "native",
                                            "(native)|(domesticated)|(alien)|(uncertain)", 
                                            false);
                                        remarks = icl.readLine("Remarks", "", 
                                                false);
                                        first = false;
                                    }
                                    icl.addDistribution(coldp, region, gazetteer, status, remarks);
                                }
                            }                                
                        }
                        break;
                    case "nr.n":
                        if (icl.getNameReference() != null) {
                            icl.setName(icl.getNameReference().getName());
                        }
                        break;
                    case "nr.r":
                        if (icl.getNameReference() != null) {
                            icl.setReference(icl.getNameReference().getReference());
                        }
                        break;
                    case "nn.r":
                        if (icl.getNameRelation() != null) {
                            icl.setReference(icl.getNameRelation().getReference());
                        }
                        break;
                    case "nn.n":
                        if (icl.getNameRelation() != null) {
                            icl.setName(icl.getNameRelation().getName());
                        }
                        break;
                    case "nn.rn":
                        if (icl.getNameRelation() != null) {
                            icl.setName(icl.getNameRelation().getRelatedName());
                        }
                        break;
                    case "nn/":
                        if (icl.getNameRelation() != null) {
                            icl.editNameRelation(icl.getNameRelation());
                        }
                        break;
                    case "nn+":
                        if (icl.getName() != null) {
                            icl.addNameRelation(coldp, null, null, null);
                        }
                        break;
                    case "t!":
                        if (icl.getTaxon() != null) {
                            icl.getTaxon().render(icl.getWriter(), new TreeRenderProperties(TreeRenderType.TEXT, ContextType.None));
                        }
                        break;
                    case "n!":
                        if (icl.getName() != null) {
                            icl.getName().render(icl.getWriter(), new TreeRenderProperties(TreeRenderType.TEXT, ContextType.None));
                        }
                        break;
                    case "n-":
                        if (icl.getName() != null && icl.getConfirmation("Delete name")) {
                            coldp.deleteName(icl.getName());
                            icl.setTaxon(null);
                            icl.setName(null);
                        }
                        break;
                    case "t-":
                        if (icl.getTaxon() != null && icl.getConfirmation("Delete taxon")) {
                            coldp.deleteTaxon(icl.getTaxon());
                            icl.setTaxon(null);
                        }
                        break;

                    case "a!":
                        if (icl.getRegion() != null) {
                            icl.getRegion().render(icl.getWriter(), new TreeRenderProperties(TreeRenderType.TEXT, ContextType.None));
                        }
                        break;
                    case "r!":
                        if (icl.getReference() != null) {
                            icl.getReference().render(icl.getWriter(), new TreeRenderProperties(TreeRenderType.TEXT, ContextType.None));
                        }
                        break;
                    case "s!":
                        if (icl.getSynonym() != null) {
                            icl.getSynonym().render(icl.getWriter(), new TreeRenderProperties(TreeRenderType.TEXT, ContextType.None));
                        }
                        break;
                    case "d!":
                        if (icl.getDistribution() != null) {
                            icl.getDistribution().render(icl.getWriter(), new TreeRenderProperties(TreeRenderType.TEXT, ContextType.None));
                        }
                        break;
                    case "nr!":
                        if (icl.getNameReference() != null) {
                            icl.getNameReference().render(icl.getWriter(), new TreeRenderProperties(TreeRenderType.TEXT, ContextType.None));
                        }
                        break;
                    case "nn!":
                        if (icl.getNameRelation() != null) {
                            icl.getNameRelation().render(icl.getWriter(), new TreeRenderProperties(TreeRenderType.TEXT, ContextType.None));
                        }
                        break;
                    case "s/":
                        COLDPSynonym synonym = icl.getSynonym();
                        if (synonym != null) {
                            String taxonID = synonym.getTaxonID();
                            COLDPTaxon taxon = icl.getTaxon();
                            if (taxon != null && (taxonID == null || !taxonID.equals(taxon.getID()))
                                && icl.getConfirmation("Use currently selected taxon " + taxon.toString() + "?")) {
                                synonym.setTaxon(taxon);
                            }
                            String referenceID = synonym.getReferenceID();
                            reference = icl.getReference();
                            if (reference != null) {
                                if (referenceID == null || !referenceID.equals(reference.getID())
                                    && icl.getConfirmation("Use currently selected reference " 
                                            + reference.toString(25, 40) + "?")) {
                                    synonym.setReference(reference);
                                }
                            } else {
                                if (referenceID != null && reference == null
                                    && icl.getConfirmation("Remove reference from record " 
                                            + synonym.getReference().toString(25, 40) + "?")) {
                                    synonym.setReference(null);
                                }
                            }
                            synonym.setStatus(icl.readEnum(SynonymStatusEnum.class, "Status", synonym.getStatus(), false).toString());
                            synonym.setRemarks(icl.readLine("Remarks", synonym.getRemarks(), false));
                        }
                        break;
                    case "s+":
                        if (icl.getTaxon() != null && icl.getName() != null) {
                            icl.addSynonym(coldp, null, null);
                        }
                        break;
                    case "s-":
                        if (icl.getSynonym() != null && icl.getConfirmation("Delete synonym " + icl.getSynonym().toString() + "?")) {
			    COLDPSynonym syn = icl.getSynonym();
                            icl.setSynonym(null);
                            coldp.deleteSynonym(syn);
                        }
			break;

                    case "x":
                        if (icl.getConfirmation("Exclude species and names from package '" + line + "'")) {
                            subtractPackage(coldp, line, ",", false);
                        } 
                        break;
                    case "xx":
                        if (icl.getConfirmation("Exclude species and names (ignoring gender) from package '" + line + "'")) {
                            subtractPackage(coldp, line, ",", false);
                        } 
                        break;
                    case "xt":
                        if (icl.getConfirmation("Exclude species and names from tab-delimited package '" + line + "'")) {
                            subtractPackage(coldp, line, "\t", true);
                        } 
                        break;
                    case "xxt":
                        if (icl.getConfirmation("Exclude species and names (ignoring gender) from tab-delimited package '" + line + "'")) {
                            subtractPackage(coldp, line, "\t", true);
                        } 
                        break;
                }
            }
        }
    }
    
    public InteractiveCommandLine() {
        try {
            terminal = TerminalBuilder.builder().system(true)
                    .nativeSignals(true)
                    .signalHandler(Terminal.SignalHandler.SIG_IGN)
                    .exec(true)
                    .jansi(true)
                    .build();
            lineReader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .highlighter(new DefaultHighlighter())
                    .build();
        } catch (IOException e) {
            LOG.error(e.toString());
        }
    }
    
    public String readLine(String guidance, String defaultText, boolean showContext) {
        String line = null;
        
        if (lineReader != null) {
            try {
                line = lineReader.readLine(showContext ? preparePrompt(guidance) : guidance + prompt, null, defaultText);
            } catch (UserInterruptException | EndOfFileException e) {
                return null;
            }

            line = line.trim();

            if (line.equalsIgnoreCase("q") || line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit"))
            {
                line = null;
            }
        }
        
        return line;
    }
    
    public String readLine(String guidance, String defaultText, String pattern, boolean showContext) {
        String line = null;
        
        if (lineReader != null) {
            try {
                do {
                    line = lineReader.readLine(showContext ? preparePrompt(guidance) : guidance + prompt, null, defaultText);
                    line = line.trim();
                } while (!line.matches(pattern));
            } catch (UserInterruptException | EndOfFileException e) {
                return null;
            }
        }
        
        return line;
    }
    
    public <T extends Enum<T>> T readEnum(Class<T> enumType, 
            String guidance, String defaultText, boolean showContext) {
        T enumValue = null;
        
        try {
            String line = readLine(guidance, defaultText, showContext);
            if (line != null) {
                line = processHardEnumCases(line);
            }
            enumValue = Enum.valueOf(enumType, line);
        } catch(Exception e) {
            // Ignore and try selection
        }
        while (enumValue == null) {
            T[] constants = enumType.getEnumConstants();
            List<String> names = Stream.of(constants)
                            .map(e ->e.toString())
                            .collect(Collectors.toList());
            String[] enumNames = names.toArray(new String[names.size()]);
            int selection = selectFromList(enumNames);
            if (selection >= 0 && selection < constants.length) {
               enumValue = constants[selection];
            }
        }
        
        return enumValue;
    }
    
    private static String processHardEnumCases(String s) {
        switch(s) {
            case "native": return "_native";
            case "class" : return "clazz";
            case "not established" : return "not_established";
            case "provisionally accepted" : return "provisionally_accepted";
            case "ambiguous synonym" : return "ambiguous_synonym";
        }
        return s;
    }
    
    public boolean getConfirmation(String question) {
        return getConfirmation(question, true);
    }

    public boolean getConfirmation(String question, boolean defaultToNo) {
        Boolean response = null;
        while (response == null) {
            String line = readLine(question + " [Y/N]", defaultToNo ? "N" : "Y", false).trim().toUpperCase();
            if (line.equals("Y")) {
                response = Boolean.TRUE;
            } else if (line.equals("N")) {
                response = Boolean.FALSE;
            }
        }
        
        return response.booleanValue();
    }
    
    
    public int selectFromList(String[] list) {
        int index = 0;
        AttributedStringBuilder asb = new AttributedStringBuilder();
        asb.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.MAGENTA))
               .append("Select an item number or return for no selection:\n");

        for (String item : list) {
            asb.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.MAGENTA))
               .append(String.format("%3d: ", ++index))
               .style(AttributedStyle.DEFAULT)
               .append(item + "\n");
        }
        
        String guidance = asb.append(prompt).toAnsi();
        int selection = -1;
        
        while (selection < 0) {
            String line = lineReader.readLine(guidance, null, "");
            if (line == null || line.length() == 0) {
                return -1;
            } else {
                line = line.trim();
                if (line.matches("^[0-9]+$")) {
                    int value = Integer.parseInt(line);
                    if (value > 0 && value <= index) {
                        selection = value - 1;
                    }
                }
            }
        }
        
        return selection;
    }
    
    private <T> T findInstance(String line, Map<String,T> map, Collection<T> instances) {
        if (line == null) {
            return null;
        }
    
        T instance = null;
        
        if (map != null) {
            instance = map.get(line);
        }
        
        if (instance == null) {
            final String filter = line;
            List<T> matches = instances
                    .stream()
                    .filter(r -> r.toString().contains(filter))
                    .collect(Collectors.toList());
            int count = matches.size();
            if (count == 1) {
                instance = matches.get(0);
            } else if (count > 1) {
                String[] items = new String[count];
                int i = 0;
                for (T m : matches) {
                    items[i++] =  m.toString();
                }
                int selection = selectFromList(items);
                if (selection >= 0) {
                    instance = matches.get(selection);
                }
            }
        }
        
        return instance;
    }

    private COLDPDistribution addDistribution(COLDataPackage coldp, COLDPRegion region,
                        String gazetteer, String status, String remarks) {
        // First check for a distribution record with the same value for the reference
        COLDPDistribution distribution = null;
        if (region == null) {
            region = this.region;
        }
        List<COLDPDistribution> distributions 
                = coldp.findDistributions(Optional.ofNullable(taxon), 
                                          Optional.ofNullable(region), 
                                          Optional.ofNullable(reference));

        String promptForExisting = null;
        String promptForNew = null;
        // Then - if a reference is set - look explicitly for a distribution with no reference
        // Or - if no reference is set - look for any existing distribution records
        boolean requireVerification = false;
        if (distributions.size() > 0) {
            promptForExisting = "Use existing distribution record";
            promptForNew = "Create new distribution record for reference";
        } else {
            requireVerification = true;
            if (reference != null) {
                distributions = coldp.findDistributions(Optional.ofNullable(taxon), 
                                              Optional.ofNullable(region),
                                              Optional.empty());
                promptForExisting = "Add reference to existing distribution record";
                promptForNew = "Create new distribution record for reference";
            } else {
                distributions = coldp.findDistributions(Optional.ofNullable(taxon), 
                                              Optional.ofNullable(region),
                                              null);
                promptForExisting = "Edit existing distribution record with reference";
                promptForNew = "Create new distribution record without reference";
            }
        }
        if (!requireVerification && distributions.size() == 1) {
            distribution = distributions.get(0);
        } else if (distributions.size() != 0) {
            String[] choices = new String[distributions.size() + 1];
            int index = 0;
            for (index = 0; index < distributions.size(); index++) {
                choices[index] = promptForExisting + " "
                        + distributions.get(index).toString();
            }
            choices[index] = promptForNew;

            int choice = selectFromList(choices);
            if (choice >= 0 && choice < distributions.size()) {
                distribution = distributions.get(choice);                                            
            } else if (choice != distributions.size()) {
                // User does not want to proceed
                return null;
            }
        }
        if (distribution == null) {
            distribution = coldp.newDistribution();
            distribution.setTaxon(taxon);
            distribution.setRegion(region);
        } 
        if (distribution.getReference() == null) {
            distribution.setReference(reference);
        }
        if (gazetteer == null) {
            distribution.setGazetteer(readEnum(GazetteerEnum.class, "Gazetteer", 
                    (distribution.getGazetteer() == null) ? "iso" : distribution.getGazetteer(),
                    false).toString());
        } else {
            distribution.setGazetteer(gazetteer);
        }
        if (status == null) {
            distribution.setStatus(readEnum(DistributionStatusEnum.class, "Status", 
                    (distribution.getStatus() == null) ? "native" : distribution.getStatus(), 
                    false).toString());
        } else {
            distribution.setStatus(status);
        }
        if (remarks == null) {
            distribution.setRemarks(readLine("Remarks", distribution.getRemarks(), false));
        } else {
            distribution.setRemarks(remarks);
        }

        setDistribution(distribution);
        
        return distribution;
    }
    
    private COLDPSynonym addSynonym(COLDataPackage coldp, String status, String remarks) {
        COLDPSynonym synonym = null;
        List<COLDPSynonym> synonyms 
                = coldp.findSynonyms(Optional.ofNullable(taxon), 
                                     Optional.ofNullable(name));

        if (synonyms.size() == 1) {
            synonym = synonyms.get(0);
        } else if (synonyms.size() != 0) {
            String[] choices = new String[synonyms.size() + 1];
            int index = 0;
            for (index = 0; index < synonyms.size(); index++) {
                choices[index] = "Edit existing synonym record "
                        + synonyms.get(index).toString();
            }
            choices[index] = "Create new synonym record";

            int choice = selectFromList(choices);
            if (choice >= 0 && choice < synonyms.size()) {
                synonym = synonyms.get(choice);                                            
            } else if (choice != synonyms.size()) {
                // User does not want to proceed
                return null;
            }
        }
        if (synonym == null) {
            synonym = coldp.newSynonym();
            synonym.setName(name);
            synonym.setTaxon(taxon);
        } 
        if (synonym.getReference() == null && reference != null 
                && getConfirmation("Use currently selected reference " + reference.toString(20, 40), false)) {
            synonym.setReference(reference);
        }
        if (status == null) {
            synonym.setStatus(readEnum(SynonymStatusEnum.class, "Status", 
                    (synonym.getStatus() == null) ? "synonym" : synonym.getStatus(), 
                    false).toString());
        } else {
            synonym.setStatus(status);
        }
        if (remarks == null) {
            synonym.setRemarks(readLine("Remarks", synonym.getRemarks(), false));
        } else {
            synonym.setRemarks(remarks);
        }

        setSynonym(synonym);
        
        return synonym;
    }

    private COLDPTaxon addTaxon(COLDataPackage coldp, COLDPName name, COLDPTaxon parent) {
        taxon = coldp.newTaxon();
        taxon.setName(name);
        
        editTaxon(coldp, taxon, parent);
        
        setTaxon(taxon);
        return taxon;
    }
    
    private void editTaxon(COLDataPackage coldp, COLDPTaxon taxon, COLDPTaxon parent) {
        if (reference != null 
                && getConfirmation("Use currently selected reference " + reference.toString(20, 40), false)) {
            taxon.setReference(reference);
        }
        if (parent == null) {
            String currentParentName = taxon.getParent().getName().getScientificName();
            String parentName = readLine("Parent", (taxon.getParentID() == null) ? "" : currentParentName, false);
            if (!Objects.equals(parentName, currentParentName)) {
                parent = findInstance(parentName, coldp.getTaxa(), coldp.getTaxa().values());
            }
        }
        if (parent != null) {
            taxon.setParent(parent);
        }
        taxon.fixHierarchy(false, false, false);
        taxon.setLifezone(readEnum(EnvironmentEnum.class, "Lifezone", taxon.getLifezone(), false).toString());
        taxon.setTemporalRangeEnd(readEnum(GeoTimeEnum.class, "Temporal range end", taxon.getTemporalRangeEnd(), false).toString());
        taxon.setExtinct(readLine("Extinct (Y/N)", taxon.isExtinct() ? "Y" : "N", "^[NY]$", false).equals("Y"));
        taxon.setRemarks(readLine("Remarks", taxon.getRemarks(), false));
        taxon.setScrutinizer(readLine("Scrutinizer", taxon.getScrutinizer(), false));
        if (getConfirmation("Set scrutinizer date to today")) {
            taxon.setScrutinizerDate(DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDateTime.now()));
        } else {
            taxon.setScrutinizerDate(readLine("Scrutinizer date", taxon.getScrutinizerDate(), "^[12][0-9]{3}-[01][0-9]-[0-3][0-9]$", false));
        }
    }
    
    private COLDPNameRelation addNameRelation(COLDataPackage coldp, COLDPName relatedName, String type, String remarks) {
        COLDPNameRelation nameRelation = null;
        
        int nrCount = (name.getNameRelations() == null) ? 0 : name.getNameRelations().size();
        int rnrCount = (name.getRelatedNameRelations() == null) ? 0 : name.getRelatedNameRelations().size();
        
        if (nrCount + rnrCount > 0) {
            String[] choices = new String[nrCount + rnrCount + 1];
            int index = 0;
            while (index < nrCount) {
                choices[index] = "Edit existing nameRelation record "
                        + name.getNameRelations().get(index).toString();
                index++;
            }
            while (index < nrCount + rnrCount) {
                choices[index] = "Edit existing nameRelation record "
                        + name.getRelatedNameRelations().get(index - nrCount).toString();
                index++;
            }
            choices[index] = "Create new nameRelation record";

            int choice = selectFromList(choices);
            if (choice >= 0 && choice < nrCount) {
                nameRelation = name.getNameRelations().get(choice);                                            
            } else if (choice >= nrCount && choice < nrCount + rnrCount) {
                nameRelation = name.getRelatedNameRelations().get(choice - nrCount); 
            } else if (choice != nrCount + rnrCount) {
                // User does not want to proceed
                return null;
            }
        }
        if (nameRelation == null) {
            nameRelation = coldp.newNameRelation();
            nameRelation.setName(name);
            nameRelation.setRelatedName(relatedName);
        } 
        if (nameRelation.getReference() == null && reference != null 
                && getConfirmation("Use currently selected reference " + reference.toString(20, 40), false)) {
            nameRelation.setReference(reference);
        }
        while (nameRelation.getRelatedName() == null) {
            String rn = readLine("Related name", "", false);
            relatedName = findInstance(rn, coldp.getNames(), coldp.getNames().values());
            if (relatedName == null) {
                coldp.deleteNameRelation(nameRelation);
                return null;
            }
            nameRelation.setRelatedName(relatedName);
        }
        if (type == null) {
            nameRelation.setType(readEnum(NameRelationTypeEnum.class, "Name relationship type", 
                    (nameRelation.getType() == null) ? "" : nameRelation.getType(), 
                    false).toString());
        } else {
            nameRelation.setType(type);
        }
        if (getConfirmation("Relationship is " + nameRelation.toString() + "\nReverse names")) {
            COLDPName save = nameRelation.getName();
            nameRelation.setName(nameRelation.getRelatedName());
            nameRelation.setRelatedName(save);
        }
        if (remarks == null) {
            nameRelation.setRemarks(readLine("Remarks", nameRelation.getRemarks(), false));
        } else {
            nameRelation.setRemarks(remarks);
        }

        setNameRelation(nameRelation);
        
        return nameRelation;
    }
    
    private void editName(COLDataPackage coldp, COLDPName name, COLDPName basionym, COLDPReference reference) {
        COLDPNameReference nameReference = name.getRedundantNameReference(false);
        
        if ((reference != null && !Objects.equals(reference, name.getReference()))
                || (reference == null && name.getReference() != null && getConfirmation("Remove current reference " + name.getReference()))) {
            name.setReference(reference);
        }
        
        name.setBasionym(basionym == null ? name : basionym);

        boolean taxonAffected = false;
        String authorship = readLine("Authorship", name.getAuthorship(), false);
        if (!Objects.equals(authorship, name.getAuthorship())) {
            name.setAuthorship(authorship);
            taxonAffected = true;
        }
        RankEnum rank = readEnum(RankEnum.class, "Rank", (name.getRank() == null ? "species" : name.getRank()), false);
        if (!(Objects.equals(rank, name.getRankEnum()))) {
            name.setRank(rank.toString());
            taxonAffected = true;
        }
        if (rank.isUninomial()) {
            String uninomial = readLine("Uninomial", name.getUninomial(), false);
            if (!Objects.equals(uninomial, name.getUninomial())) {
                name.setUninomial(uninomial);
                taxonAffected = true;
            }
            name.setScientificName(name.getUninomial());
            name.setGenus(null);
            name.setSpecificEpithet(null);
            name.setInfraspecificEpithet(null);
        } else {
            String genus = readLine("Genus", name.getGenus(), false);
            String specificEpithet = readLine("Specific epithet", name.getSpecificEpithet(), false);
            String infraspecificEpithet = null;
            if (rank.isInfraspecific()) {
                infraspecificEpithet = readLine("Infraspecific epithet", name.getInfraspecificEpithet(), false);
            } else {
                infraspecificEpithet = null;
            }
            if (!Objects.equals(genus, name.getGenus())
                    || !Objects.equals(specificEpithet, name.getSpecificEpithet())
                    || !Objects.equals(infraspecificEpithet, name.getInfraspecificEpithet())) {
                taxonAffected = true;
            }
            name.setUninomial(null);
            name.setGenus(genus);
            name.setSpecificEpithet(specificEpithet);
            name.setInfraspecificEpithet(infraspecificEpithet);
            name.setScientificName(COLDPName.getScientificNameFromParts(rank, genus, specificEpithet, infraspecificEpithet));
        }
        if (name.getReference() != null) {
            name.setPublishedInPage(readLine("Published in page", name.getPublishedInPage(), false));
        }
        String year = name.getPublishedInYear();
        if (year == null && name.getAuthorship() != null) {
            year = name.getAuthorship().replaceAll("^(.*)([0-2][0-9]{3}?)(.*)", "$2");
        }
        name.setPublishedInYear(readLine("Published in year", year, "^([1-2][0-9]{3})?$", false));
        name.setCode(readEnum(CodeEnum.class, "Code", name.getCode(), false).toString());
        name.setStatus(readEnum(NameStatusEnum.class, "Status", name.getStatus(), false).getStatus());
        name.setRemarks(readLine("Remarks", name.getRemarks(), false));
        name.setLink(readLine("Link", name.getLink(), false));
        if(nameReference != null && getConfirmation("Edit existing associated name reference: " + nameReference.toString())) {
            editNameReference(nameReference);
        } else if (name.getReference() != null && name.getPublishedInPage() != null && getConfirmation("Create associated name reference", false)) {
            nameReference = coldp.newNameReference();
            nameReference.setName(name);
            nameReference.setReference(name.getReference());
            nameReference.setPage(name.getPublishedInPage());
            nameReference.setLink(name.getLink());
            nameReference.setRemarks(name.getRemarks());
            editNameReference(nameReference);
            setNameReference(nameReference);
        }
        setName(name);
        if (taxonAffected && name.getTaxon() != null 
                && getConfirmation("Make corresponding changes to taxon: " 
                        + name.getTaxon().toString(), false)) {
            COLDPTaxon taxon = name.getTaxon();
            COLDPTaxon parent = taxon.getParent();
            String parentString = null;
            if (parent == null) {
                parentString = readLine("Parent for taxon", "", false);
            } else if (getConfirmation("Change current parent for taxon: " + parent.toString())) {
                parentString = readLine("Parent", "", false);
            }
            if (parentString != null) {
                parent = findInstance(parentString, null, 
                        coldp.getTaxa().values().stream()
                                   .filter(t -> rank.isLowerThan(t.getName().getRankEnum()))
                                   .collect(Collectors.toList()));
                if (parent != null 
                        || (taxon.getParent() != null && getConfirmation("Set taxon to have no parent"))) {
                    taxon.setParent(parent);
                }
            }
            if (taxon.getChildren() != null) {
                Set<COLDPTaxon> children = new HashSet<COLDPTaxon>();
                children.addAll(taxon.getChildren());
                
                COLDPTaxon newParentOfChild = null;
                COLDPTaxon parentForAll = null;
                for (COLDPTaxon child : children) {
                    if (parentForAll != null) {
                        child.setParent(parentForAll);
                        child.fixHierarchy(true, true, true);
                    } else {
                        if (!rank.isHigherThan(child.getName().getRankEnum())) {
                            String newParentOfChildString 
                                = readLine("Parent taxon for current child " + child.toString(),
                                           (newParentOfChild == null ? "" : newParentOfChild.toString()), 
                                           false);
                            RankEnum childRank = child.getName().getRankEnum();
                            // Avoid getting another parent at too low a rank
                            newParentOfChild = findInstance(newParentOfChildString, null, 
                                            coldp.getTaxa().values().stream()
                                                    .filter(t -> t.getName().getRankEnum().isHigherThan(childRank))
                                                    .collect(Collectors.toList()));
                            if (newParentOfChild != null) {
                                child.setParent(newParentOfChild);
                                child.fixHierarchy(true, true, true);
                                if (children.size() > 1 
                                        && getConfirmation("Use same new parent for any remaining children", false)) {
                                    parentForAll = newParentOfChild;
                                }
                            }
                        }
                    }
                }
            }
            taxon.fixHierarchy(false, false, false);
        } else if (name.getTaxon() == null && (name.getSynonyms() == null || name.getSynonyms().size() == 0)) {
            if (getConfirmation("Add new taxon for name")) {
                COLDPTaxon parent = null;
                if (name.getRankEnum().equals(RankEnum.species)) {
                    parent = coldp.getTaxonByScientificName(name.getGenus());
                } else if (name.getRankEnum().inSpeciesGroup()) {
                    parent = coldp.getTaxonByScientificName(name.getGenus() + " " + name.getSpecificEpithet());
                }
                String parentName = readLine("Name of parent taxon", (parent == null) ? null : parent.getName().getScientificName(), false);
                parent = findInstance(parentName, coldp.getTaxa(), coldp.getTaxa().values());
                setTaxon(addTaxon(coldp, name, parent));
            } else if (getConfirmation("Set name as synonym for taxon")) {
                COLDPTaxon accepted = null;
                if (taxon != null && getConfirmation("Use current taxon " + taxon.toString())) {
                    accepted = taxon;
                } else if (taxon == null) {
                    String acceptedName = readLine("Name of accepted taxon", null, false);
                    accepted = findInstance(acceptedName, coldp.getTaxa(), coldp.getTaxa().values());
                }
                if (accepted != null) {
                    setTaxon(accepted);
                    addSynonym(coldp, null, null);
                }
            }
        }
    }
    
    private void editNameReference(COLDPNameReference nameReference) {
        nameReference.setPage(readLine("Page", nameReference.getPage(), false));
        nameReference.setLink(readLine("Link", nameReference.getLink(), false));
        nameReference.setRemarks(readLine("Remarks", nameReference.getRemarks(), false));
    }

    private void editNameRelation(COLDPNameRelation nameRelation) {
        if (nameRelation.getReference() == null && reference != null 
                && getConfirmation("Use currently selected reference " + reference.toString(20, 40), false)) {
            nameRelation.setReference(reference);
        }
        nameRelation.setType(readEnum(NameRelationTypeEnum.class, "Name relationship type", nameRelation.getType(), false).toString());
        if (getConfirmation("Relationship is " + nameRelation.toString() + "\nReverse names")) {
            COLDPName save = nameRelation.getName();
            nameRelation.setName(nameRelation.getRelatedName());
            nameRelation.setRelatedName(save);
        }
        nameRelation.setRemarks(readLine("Remarks", nameRelation.getRemarks(), false));
    }

    private void editSpeciesInteraction(COLDPSpeciesInteraction speciesInteraction) {
        if (speciesInteraction.getReference() == null && reference != null 
                && getConfirmation("Use currently selected reference " + reference.toString(20, 40), false)) {
            speciesInteraction.setReference(reference);
        }
        String scientificName = speciesInteraction.getRelatedTaxonScientificName();
        speciesInteraction.setRelatedTaxonScientificName(readLine("Related taxon scientific name", 
                speciesInteraction.getRelatedTaxonScientificName(), false));
        if (!Objects.equals(scientificName, speciesInteraction.getRelatedTaxonScientificName())) {
            speciesInteraction.linkToCOL();
        }
        speciesInteraction.setType(readEnum(SpeciesInteractionTypeEnum.class, "Name relationship type", 
                speciesInteraction.getType(), false).toString());
        speciesInteraction.setRemarks(readLine("Remarks", speciesInteraction.getRemarks(), false));
        speciesInteraction.setRelatedTaxonLink(readLine("Related taxon link", speciesInteraction.getRelatedTaxonLink(), false));
    }

    private void showError(String s) {
        AttributedStringBuilder asb = new AttributedStringBuilder();
        asb.style(AttributedStyle.DEFAULT.background(AttributedStyle.RED).foreground(AttributedStyle.WHITE))
           .append(s + "\n")
           .style(AttributedStyle.DEFAULT);
        terminal.writer().write(asb.toAnsi());
    }
    
    private String preparePrompt(String guidance) {
        AttributedStringBuilder asb = new AttributedStringBuilder();
        if(taxon != null) {
            asb.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN))
               .append("T: " + taxon.toString() + "\n");
        }
        if(synonym != null) {
            asb.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW))
               .append("S: " + synonym.toString() + "\n");
        }
        if(name != null) {
            asb.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN))
               .append("N: " + name.toString() + "\n");
        }
        if(nameRelation != null) {
            asb.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW))
               .append("NN: " + nameRelation.toString() + "\n");
        }
        if(reference != null) {
            asb.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN))
               .append("R: " + reference.toString(25, 40) + "\n");
        }
        if(nameReference != null) {
            asb.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW))
               .append("NR: " + nameReference.toString() + "\n");
        }
        if(region != null) {
            asb.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN))
               .append("A: " + region.toString() + "\n");
        }
        if(distribution != null) {
            asb.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW))
               .append("D: " + distribution.toString() + "\n");
        }
        if(speciesInteraction != null) {
            asb.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN))
               .append("I: " + speciesInteraction.toString() + "\n");
        }
        asb.style(AttributedStyle.DEFAULT)
            .append(guidance + prompt).toAnsi();
        return asb.toAnsi();
    }

    public void setTaxon(COLDPTaxon taxon) {
        this.taxon = taxon;
    }

    public void setName(COLDPName name) {
        this.name = name;
    }

    public void setReference(COLDPReference reference) {
        this.reference = reference;
    }

    public void setSynonym(COLDPSynonym synonym) {
        this.synonym = synonym;
    }

    public void setNameReference(COLDPNameReference nameReference) {
        this.nameReference = nameReference;
    }

    public void setNameRelation(COLDPNameRelation nameRelation) {
        this.nameRelation = nameRelation;
    }

    public void setDistribution(COLDPDistribution distribution) {
        this.distribution = distribution;
    }

    public void setSpeciesInteraction(COLDPSpeciesInteraction speciesInteraction) {
        this.speciesInteraction = speciesInteraction;
    }

    public void setRegion(COLDPRegion region) {
        this.region = region;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public COLDPTaxon getTaxon() {
        return taxon;
    }

    public COLDPName getName() {
        return name;
    }

    public COLDPReference getReference() {
        return reference;
    }

    public COLDPSynonym getSynonym() {
        return synonym;
    }

    public COLDPNameReference getNameReference() {
        return nameReference;
    }

    public COLDPNameRelation getNameRelation() {
        return nameRelation;
    }

    public COLDPDistribution getDistribution() {
        return distribution;
    }

    public COLDPRegion getRegion() {
        return region;
    }

    public COLDPSpeciesInteraction getSpeciesInteraction() {
        return speciesInteraction;
    }
    
    public void clearContext() {
        taxon = null;
        name = null;
        reference = null;
        synonym = null;
        nameReference = null;
        nameRelation = null;
        distribution = null;
        region = null;
    }
    
    public PrintWriter getWriter() {
        return (terminal == null) ? null : terminal.writer();
    }
}
