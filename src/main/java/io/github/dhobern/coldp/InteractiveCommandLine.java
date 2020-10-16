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

import static io.github.dhobern.utils.ZipUtils.zipFolder;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
    
    private COLDPTaxon taxon = null;
    private COLDPName name = null;
    private COLDPReference reference = null;
    private COLDPSynonym synonym = null;
    private COLDPNameReference nameReference = null;
    private COLDPNameRelation nameRelation = null;
    private COLDPDistribution distribution = null;
    private COLDPRegion region = null;
    
    private String prompt = "> ";
    
    private LineReader lineReader = null; 
    private Terminal terminal = null;

    public static void main(String[] args) {
        InteractiveCommandLine icl = new InteractiveCommandLine();
        String coldpFolderName = (args.length > 0) ? args[0] : "mockdata";
        String coldpName = coldpFolderName;
        COLDataPackage coldp = new COLDataPackage(coldpName);
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
                            String basionymString = icl.readLine("Basionym (empty if none)", icl.getName() == null ? "" : icl.getName().getID(), false);
                            if (basionymString != null && basionymString.length() > 0) {
                                basionym = icl.findInstance(basionymString, coldp.getNames(), coldp.getNames().values());
                            }
                            COLDPName name = coldp.newName();
                            icl.editName(name, basionym, reference);
                            icl.setName(name);
                        }
                        break;
                    case "n/": 
                        if (icl.getName() != null) {
                            COLDPName name = icl.getName();
                            COLDPReference reference = icl.getReference();
                            if (reference != null 
                                    && (name.getReference() == null || !name.getReference().equals(reference))
                                    && icl.getConfirmation("Use currently selected reference " + reference.toString(25, 40) + "?")) {
                            }
                            COLDPName basionym = name.getBasionym();
                            if (    (basionym == null && icl.getConfirmation("Select basionym?"))
                                 || (basionym != null && icl.getConfirmation("Replace current basionym " + basionym.toString() + "?"))) {
                                String nameString = icl.readLine("Basionym", "", false);
                                if (nameString != null && nameString.length() > 0) {
                                    basionym = icl.findInstance(nameString, coldp.getNames(), coldp.getNames().values());
                                    if (basionym != null) {
                                    }
                                }
                            }
                            icl.editName(name, basionym, reference);
                        }
                        break;
                        
                    case "t": 
                        icl.setTaxon(icl.findInstance(line, coldp.getTaxa(), coldp.getTaxa().values()));
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
                    case "s": 
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
            distribution.setRegion(region);
            distribution.setTaxon(taxon);
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
    
    private void editName(COLDPName name, COLDPName basionym, COLDPReference reference) {
        COLDPNameReference nameReference = name.getRedundantNameReference(false);
        
        name.setReference(reference);
        name.setBasionym(basionym == null ? name : basionym);

        name.setAuthorship(readLine("Authorship", name.getAuthorship(), false));
        RankEnum rank = readEnum(RankEnum.class, "Rank", (name.getRank() == null ? "species" : name.getRank()), false);
        name.setRank(rank.toString());
        if (rank.isUninomial()) {
            name.setUninomial(readLine("Uninomial", name.getUninomial(), false));
            name.setScientificName(name.getUninomial());
            name.setGenus(null);
            name.setSpecificEpithet(null);
            name.setInfraspecificEpithet(null);
        } else {
            name.setUninomial(null);
            String genus = readLine("Genus", name.getGenus(), false);
            name.setGenus(genus);
            String specificEpithet = readLine("Specific epithet", name.getSpecificEpithet(), false);
            name.setSpecificEpithet(specificEpithet);
            String infraspecificEpithet = null;
            if (rank.isInfraspecific()) {
                infraspecificEpithet = readLine("Infraspecific epithet", name.getInfraspecificEpithet(), false);
                name.setInfraspecificEpithet(infraspecificEpithet);
            }
            name.setScientificName(COLDPName.getScientificNameFromParts(rank, genus, specificEpithet, infraspecificEpithet));
        }
        if (reference != null) {
            name.setPublishedInPage(readLine("Published in page", name.getPublishedInPage(), false));
        }
        name.setPublishedInYear(readLine("Published in year", name.getPublishedInYear(), "^([1-2][0-9]{3})?$", false));
        name.setCode(readEnum(CodeEnum.class, "Code", name.getCode(), false).toString());
        name.setStatus(readEnum(NameStatusEnum.class, "Status", name.getStatus(), false).getStatus());
        name.setRemarks(readLine("Remarks", name.getRemarks(), false));
        name.setLink(readLine("Link", name.getLink(), false));
        if(nameReference != null && getConfirmation("Edit existing associated name reference: " + nameReference.toString())) {
            editNameReference(nameReference);
        } else if (name.getReference() != null && name.getPublishedInPage() != null && getConfirmation("Create associated name reference", false)) {
            nameReference = new COLDPNameReference();
            nameReference.setName(name);
            nameReference.setReference(reference);
            nameReference.setPage(name.getPublishedInPage());
            nameReference.setLink(name.getLink());
            nameReference.setRemarks(name.getRemarks());
            editNameReference(nameReference);
        }
    }
    
    private void editNameReference(COLDPNameReference nameReference) {
        nameReference.setPage(readLine("Page", nameReference.getPage(), false));
        nameReference.setLink(readLine("Link", nameReference.getLink(), false));
        nameReference.setRemarks(readLine("Remarks", nameReference.getRemarks(), false));
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
}
