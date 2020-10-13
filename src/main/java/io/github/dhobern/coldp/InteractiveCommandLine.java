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
        while ((line = icl.readLine("Enter a command", "")) != null) {
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
                    case "a": icl.setRegion(line == null ? null : coldp.getRegions().get(line)); break;
                    case "r": icl.setReference(line == null ? null : coldp.getReferences().get(line)); break;
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
                    case "d": 
                        index = Integer.parseInt(line);
                        icl.setDistribution(
                                (line == null || icl.getTaxon() == null 
                                        || icl.getTaxon().getDistributions() == null 
                                        || icl.getTaxon().getDistributions().size() < index) 
                                    ? null : icl.getTaxon().getDistributions().iterator().next());
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
                    case "r+":
                        String author = icl.readLine("Author", "");
                        String year = icl.readLine("Year", "", "^([0-9]{4})?$");
                        String title = icl.readLine("Title", "");
                        String source = icl.readLine("Source", "");
                        String details = icl.readLine("Details", "");
                        String link = icl.readLine("Link", "");
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
                            reference.setAuthor(icl.readLine("Author", reference.getAuthor()));
                            reference.setYear(icl.readLine("Year", reference.getYear(), "^([0-9]{4})?$"));
                            reference.setTitle(icl.readLine("Title", reference.getTitle()));
                            reference.setSource(icl.readLine("Source", reference.getSource()));
                            reference.setDetails(icl.readLine("Details", reference.getDetails()));
                            reference.setLink(icl.readLine("Link", reference.getLink()));
                        }
                        break;
                    case "w":
                        coldp.write(coldpFolderName, "-JUNK");
                        break;
                }
            }
        }
    }
    
    public InteractiveCommandLine() {
        try {
            Terminal terminal = TerminalBuilder.builder().system(true)
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
    
    public String readLine(String guidance, String defaultText) {
        String line = null;
        
        if (lineReader != null) {
            try {
                line = lineReader.readLine(preparePrompt(guidance), null, defaultText);
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
        public String readLine(String guidance, String defaultText, String pattern) {
        String line = null;
        
        if (lineReader != null) {
            try {
                do {
                    line = lineReader.readLine(preparePrompt(guidance), null, defaultText);
                    line = line.trim();
                } while (!line.matches(pattern));
            } catch (UserInterruptException | EndOfFileException e) {
                return null;
            }
        }
        
        return line;
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
               .append("Rel: " + nameRelation.toReferenceString() + "\n");
        }
        if(reference != null) {
            asb.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN))
               .append("Ref: " + reference.toReferenceString() + "\n");
        }
        if(nameReference != null) {
            asb.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW))
               .append("NRef: " + nameReference.toReferenceString() + "\n");
        }
        if(region != null) {
            asb.style(AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN))
               .append("Reg: " + region.toReferenceString() + "\n");
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
