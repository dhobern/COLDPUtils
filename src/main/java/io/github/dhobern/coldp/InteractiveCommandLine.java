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
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.DefaultHighlighter;
import org.jline.reader.impl.LineReaderImpl;
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
                    case "n": icl.setName(line == null ? null : coldp.getNameByScientificName(line)); break;
                    case "t": icl.setTaxon(line == null ? null : coldp.getTaxonByScientificName(line)); break;
                    case "a": icl.setRegion(line == null ? null : icl.findRegion(line, coldp)); break;
                    case "nr": 
                        int index = Integer.parseInt(line);
                        icl.setNameReference(
                                (line == null || icl.getName() == null 
                                        || icl.getName().getNameReferences() == null 
                                        || icl.getName().getNameReferences().size() < index) 
                                    ? null : icl.getName().getNameReferences().get(index - 1));
                        break;
                    case "nn": 
                        index = Integer.parseInt(line);
                        icl.setNameRelation(
                                (line == null || icl.getName() == null 
                                        || icl.getName().getNameRelations() == null 
                                        || icl.getName().getNameRelations().size() < index) 
                                    ? null : icl.getName().getNameRelations().get(index - 1));
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
                        index = Integer.parseInt(line);
                        icl.setSynonym(
                                (line == null || icl.getTaxon() == null 
                                        || icl.getTaxon().getSynonyms() == null 
                                        || icl.getTaxon().getSynonyms().size() < index) 
                                    ? null : icl.getTaxon().getSynonyms().iterator().next());
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
                                        .filter(c -> c.toReferenceString().contains(filter))
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
                                    items[i++] =  c.toReferenceString();
                                }
                                int selection = icl.selectFromList(items);
                                if (selection >= 0) {
                                    icl.setTaxon(children.get(selection));
                                }
                            }
                        } 
                        break;
                    case "r": 
                        icl.setReference(icl.findReference(line, coldp));
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
                        if (reference != null && icl.getConfirmation("Delete reference " + reference.toReferenceString(25, 40) + "?")) {
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
                                        .filter(d -> d.toReferenceString().contains(filter))
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
                                    items[i++] =  c.toReferenceString();
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
                                && icl.getConfirmation("Use currently selected region " + region.toReferenceString() + "?")) {
                                distribution.setRegion(region);
                            }
                            String taxonID = distribution.getTaxonID();
                            COLDPTaxon taxon = icl.getTaxon();
                            if (taxon != null && (taxonID == null || !taxonID.equals(taxon.getID()))
                                && icl.getConfirmation("Use currently selected taxon " + taxon.toReferenceString() + "?")) {
                                distribution.setTaxon(taxon);
                            }
                            String referenceID = distribution.getReferenceID();
                            reference = icl.getReference();
                            if (reference != null) {
                                if (referenceID == null || !referenceID.equals(reference.getID())
                                    && icl.getConfirmation("Use currently selected reference " 
                                            + reference.toReferenceString(25, 40) + "?")) {
                                    distribution.setReference(reference);
                                }
                            } else {
                                if (referenceID != null && reference == null
                                    && icl.getConfirmation("Remove reference from record " 
                                            + distribution.getReference().toReferenceString(25, 40) + "?")) {
                                    distribution.setReference(null);
                                }
                            }
                            distribution.setGazetteer(icl.readLine("Gazetteer", distribution.getGazetteer(), false));
                            distribution.setStatus(icl.readLine("Status", distribution.getStatus(), false));
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
                                COLDPRegion region = icl.findRegion(token.trim(), coldp);
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

            if (line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit"))
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
    
    private COLDPReference findReference(String line, COLDataPackage coldp) {
        COLDPReference reference = null;
        
        if (line != null) {
            reference = coldp.getReferences().get(line); 
            if (reference == null) {
                final String filter = line;
                List<COLDPReference> references = coldp.getReferences().values()
                        .stream()
                        .filter(r -> r.toReferenceString().contains(filter))
                        .collect(Collectors.toList());
                int count = references.size();
                if (count == 1) {
                    reference = references.get(0);
                } else if (count > 1) {
                    String[] items = new String[count];
                    int i = 0;
                    for (COLDPReference r : references) {
                        items[i++] =  r.toReferenceString();
                    }
                    int selection = selectFromList(items);
                    if (selection >= 0) {
                        reference = references.get(selection);
                    }
                }
            }
        } 
        return reference;
    }

    private COLDPRegion findRegion(String filter, COLDataPackage coldp) {
        // Look first for exact match as key
        COLDPRegion region = coldp.getRegions().get(filter);

        // Now hunt for matches
        if (region == null) {
            List<COLDPRegion> regions =
                coldp.getRegions().values()
                    .stream()
                    .filter(r -> r.toReferenceString().contains(filter))
                    .collect(Collectors.toList());
            if (regions.size() == 1) {
                region = regions.get(0);
            } else if (regions.size() > 1) {
                String[] items = new String[regions.size()];
                int i = 0;
                for (COLDPRegion r : regions) {
                    items[i++] = r.toReferenceString();
                }
                int selection = selectFromList(items);
                if (selection >= 0) {
                    region = regions.get(selection);
                }
            }
        }
        
        return region;
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
                        + distributions.get(index).toReferenceString();
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
            distribution.setGazetteer(readLine("Gazetteer", 
                    (distribution.getGazetteer() == null) ? "iso" : distribution.getGazetteer(),
                    "(tdwg)|(iso)|(fao)|(longhurst)|(teow)|(iho)|(text)", false));
        } else {
            distribution.setGazetteer(gazetteer);
        }
        if (status == null) {
            distribution.setStatus(readLine("Status", 
                    (distribution.getStatus() == null) ? "native" : distribution.getStatus(), 
                    "(native)|(domesticated)|(alien)|(uncertain)", false));
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
               .append("T: " + taxon.toReferenceString() + "\n");
        }
        if(synonym != null) {
            asb.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW))
               .append("S: " + synonym.toReferenceString() + "\n");
        }
        if(name != null) {
            asb.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN))
               .append("N: " + name.toReferenceString() + "\n");
        }
        if(nameRelation != null) {
            asb.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW))
               .append("NN: " + nameRelation.toReferenceString() + "\n");
        }
        if(reference != null) {
            asb.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN))
               .append("R: " + reference.toReferenceString(25, 40) + "\n");
        }
        if(nameReference != null) {
            asb.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW))
               .append("NR: " + nameReference.toReferenceString() + "\n");
        }
        if(region != null) {
            asb.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN))
               .append("A: " + region.toReferenceString() + "\n");
        }
        if(distribution != null) {
            asb.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW))
               .append("D: " + distribution.toReferenceString() + "\n");
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
