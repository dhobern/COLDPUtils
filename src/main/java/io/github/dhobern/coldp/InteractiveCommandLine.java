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

import java.io.IOException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
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
        COLDataPackage coldp = new COLDataPackage("mockdata");
        String line = icl.readLine("Edit this text", "Something to edit");
        
        icl.setTaxon(coldp.getTaxa().values().iterator().next());
        line = icl.readLine("Is this taxon correct?", "");

        icl.setRegion(coldp.getRegions().values().iterator().next());
        line = icl.readLine("Does this taxon exist here?", "");
    
    }
    
    public InteractiveCommandLine() {
        try {
            Terminal terminal = TerminalBuilder.builder().build();
            lineReader = LineReaderBuilder.builder()
                    .terminal(terminal)
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
    
    private String preparePrompt(String guidance) {
        return (taxon == null ? "" : taxon.toReferenceString() + "\n")
            + (name == null ? "" : name.toReferenceString() + "\n")
            + (synonym == null ? "" : synonym.toReferenceString() + "\n")
            + (nameRelation == null ? "" : nameRelation.toReferenceString() + "\n")
            + (reference == null ? "" : reference.toReferenceString() + "\n")
            + (nameReference == null ? "" : nameReference.toReferenceString() + "\n")
            + (distribution == null ? "" : distribution.toReferenceString() + "\n")
            + (region == null ? "" : region.toReferenceString() + "\n")
            + guidance + prompt;
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
