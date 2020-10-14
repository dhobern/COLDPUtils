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

import static io.github.dhobern.utils.StringUtils.buildCSV;
import static io.github.dhobern.utils.CollectionUtils.getKeyedSets;
import io.github.dhobern.coldp.IdentifierPolicy.IdentifierType;
import io.github.dhobern.coldp.TreeRenderProperties.ContextType;
import io.github.dhobern.coldp.TreeRenderProperties.TreeRenderType;
import io.github.dhobern.utils.CSVReader;
import static io.github.dhobern.utils.ZipUtils.zipFolder;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CoLDPFormatter is a processor for handling taxonomic checklists structured
 * as CoLDP (https://github.com/CatalogueOfLife/coldp) archive contents. It 
 * allows for a range of manipulations to simplify reuse of the contents of 
 * such checklists.
 * 
 * @author dhobern@gmail.com
 */
public class COLDPTool {

    /**
     * Enumeration of validation severities
     */
    private static enum ValidationSeverity {
        ERROR(0), WARNING(1), INFO(2);
        
        private int threshold;
        
        private ValidationSeverity(int threshold) {
            this.threshold = threshold;
        }
        
        public int getThreshold() {
            return threshold;
        }
    }

    /**
     * Enumeration of modification strategies
     */
    private static enum ModificationStrategy {
        CAUTIOUS, AGGRESSIVE;
    }
    
    /**
     * Enumeration of validation codes
     */
    private static enum ValidationCode {
        NAME_NO_BASIONYM(ValidationSeverity.ERROR, "No basionym specified for combination"),
        NAME_NO_REFERENCE(ValidationSeverity.ERROR, "No reference specified for original name"),
        NAME_SAME_REFERENCE_BASIONYM(ValidationSeverity.ERROR, "Same reference specified for basionym and combination"),
        COMBINATION_NO_REFERENCE(ValidationSeverity.WARNING, "No reference specified for combination"),
        NAMEREFERENCE_REDUNDANT(ValidationSeverity.INFO, "Name reference repeats information from name")
        ;
        
        private ValidationSeverity severity;
        private String message;
        
        private ValidationCode() {}
        
        private ValidationCode(ValidationSeverity severity, String message) {
            this.severity = severity;
            this.message = message;
        }

        public ValidationSeverity getSeverity() {
            return severity;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * Enumeration of command names
     */
    private static enum CommandName {
        NONE(false), TOHTML(false), VALIDATE(false), MODIFY(true), EDIT(true); 
        
        private boolean backupFirst;
        
        CommandName(boolean backupFirst) {
            this.backupFirst = backupFirst;
        }
        
        public boolean performBackupFirst() {
            return backupFirst;
        }
    }

    /**
     * Enumeration of output formats and their current support level
     */
    private static enum OutputFormat {
        HTML(true), TSV(false), JSON(false);
        
        private boolean supported = false;
        
        OutputFormat(boolean supported) {
            this.supported = supported;
        }
        
        public boolean isSupported() {
            return supported;
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(COLDPTool.class);
    
    private static String templateEyecatcherText = "COLDPTool.Output";
    
    private static String selectedTaxonName;
    private static String coldpFolderName;
    private static String outputFolderName;
    private static String outputFileName;
    private static String logFileName;
    private static ValidationSeverity logThreshold = ValidationSeverity.WARNING;
    private static PrintWriter logWriter;
    private static BufferedReader templateReader;
    private static int indentCount = 0;
    private static IdentifierType currentIdentifierType = null;
    private static IdentifierType newIdentifierType = null;
    private static int[] issueCounts = { 0, 0, 0 };
    private static boolean verbose = false;
    private static boolean fixNameSameReferenceBasionym = false;
    private static ModificationStrategy modificationStrategy = ModificationStrategy.CAUTIOUS;
    private static String treatmentFileName;
    private static String backupFolderName = ".";
    
    private static OutputFormat format = OutputFormat.HTML;
    private static String newCsvSuffix = "-NEW";
    
    private static boolean overwrite = false;
    
    
    /**
     *
     * @param argv
     */
    public static void main(String argv[]) {
        
        CommandName commandName = CommandName.NONE;
        String[] arguments = null;
        
        if (argv.length > 0) {
            try {
                commandName = CommandName.valueOf(argv[0].toUpperCase());
                arguments = new String[argv.length - 1];
                System.arraycopy(argv, 1, arguments, 0, argv.length - 1);
            } catch (Exception e) {
                // Swallow exception here and show help below
            }
        }

        if (commandName == CommandName.NONE) {
            showMainHelp();
            return;
        }
        
        boolean continueExecution = true;
        
        switch (commandName) {
            case VALIDATE -> continueExecution = parseValidateCommandLine(arguments);
            case TOHTML -> continueExecution = parseToHTMLCommandLine(arguments);
            case MODIFY -> continueExecution = parseModifyCommandLine(arguments);
            case EDIT -> continueExecution = parseEditCommandLine(arguments);
        }
        
        if (continueExecution && commandName.performBackupFirst()) {
            continueExecution = backupCOLDP();
        }
        
        if (continueExecution) {
            switch (commandName) {
                case TOHTML -> executeToHTML();
                case MODIFY -> executeModify();
                case VALIDATE -> executeValidate();
                case EDIT -> executeEdit();
            }
        }
        
        if (logWriter != null) {
            logWriter.close();
        }
        
        if (verbose && (issueCounts[0] + issueCounts[1] > 0)) {
            reportInfo("Issues detected in data: " + issueCounts[0] + " Errors, " + issueCounts[1] + " Warnings");
            reportInfo("Run COLDPTool VALIDATE to review");
        }
    }
    
    private static void executeToHTML() {
        COLDataPackage coldp = new COLDataPackage(coldpFolderName);
        
        boolean continueExecution = true;

        if (format == OutputFormat.HTML) {
            PrintWriter writer = null;

            if (outputFileName == null) {
                writer = new PrintWriter(System.out, true, StandardCharsets.UTF_8);
            } else if (!overwrite && new File(outputFileName).exists()) {
                reportError("File exists: " + outputFileName + " - specify -x to overwrite");
                continueExecution = false;
            } else {
                try {
                    writer = new PrintWriter(outputFileName, "UTF-8");
                } catch (FileNotFoundException | UnsupportedEncodingException ex) {
                    reportError("Could not open output file [" + outputFileName + "]:\n" + ex);
                }
            }

            if (continueExecution && writer != null) {

                if (templateReader != null) {
                    writeTemplate(writer, templateReader, templateEyecatcherText);
                }

                if(selectedTaxonName != null) {
                    COLDPTaxon taxon = coldp.getTaxonByScientificName(selectedTaxonName);

                    if (taxon == null) {
                        reportError("Selected taxon name " + selectedTaxonName + " not found in " + coldpFolderName);
                    } else {
                        renderHigherTaxa(writer, taxon);
                        renderTaxon(writer, taxon);
                    }
                } else {
                    for(COLDPTaxon taxon : coldp.getRootTaxa()) {
                        renderTaxon(writer, taxon);
                    }
                }

                if (templateReader != null) {
                    writeTemplate(writer, templateReader, null);
                }

                writer.close();
            }
        }
    }
    
    private static void executeEdit() {
        boolean continueProcessing  = true;

        COLDataPackage coldp = new COLDataPackage(coldpFolderName);

    }
    
    private static void executeModify() {
        boolean continueProcessing  = true;

        COLDataPackage coldp = new COLDataPackage(coldpFolderName);

        if (treatmentFileName != null) {
            continueProcessing = processTreatmentFile(coldp, treatmentFileName);
        }

        if (continueProcessing && fixNameSameReferenceBasionym) {
            for (COLDPName name : coldp.getNames().values()) {
                if (!name.getID().equals(name.getBasionymID())) {
                    COLDPName basionym = name.getBasionym();
                    if (    name.getReferenceID() != null 
                         && basionym.getReferenceID() != null
                         && name.getReferenceID().equals(basionym.getReferenceID())
                         && Objects.equals(name.getPublishedInPage(), basionym.getPublishedInPage())) {
                        COLDPNameReference nr = name.getRedundantNameReference(modificationStrategy == ModificationStrategy.CAUTIOUS);
                        if (nr != null) {
                            if (basionym.getRedundantNameReference(false) == null) {
                                nr.setName(basionym);
                            } else {
                                coldp.deleteNameReference(nr);
                            }
                        }
                        name.setReference(null);
                        name.setPublishedInPage(null);
                        name.setPublishedInYear(null);
                    }
                }
            }
        }

        // Changing identifiers should be the last stage, since it
        // affects various collections
        if (continueProcessing && newIdentifierType == IdentifierType.Int) {
            coldp.tidyIdentifiers();
        }

        coldp.write(outputFolderName, newCsvSuffix, overwrite);
    }
    
    private static boolean processTreatmentFile(COLDataPackage coldp, String fileName) {
        boolean continueProcessing = true;
        
        List<String> issues = new ArrayList<>();
        CSVReader<ReferenceDetails> detailReader;
        try {
            detailReader = new CSVReader<>(fileName, ReferenceDetails.class, ",");
            List<ReferenceDetails> items = detailReader.getList();
            Map<String,Set<COLDPName>> namesByScientificName = getKeyedSets(coldp.getNames().values(), COLDPName::getScientificName);
            Map<String,Set<COLDPName>> namesByStems = getKeyedSets(coldp.getNames().values(), COLDPName::getNameStem);

            for (ReferenceDetails item : items) {
                String scientificName = item.getScientificName();

                // This allows for the name key to be supplied directly
                COLDPName name = coldp.getNames().get(scientificName);

                if (name == null) {
                    Set<COLDPName> names = namesByScientificName.get(scientificName);
                    if (names == null || names.size() == 0) {
                        Set<COLDPName> namesByStem = namesByStems.get(COLDPName.trimScientificNameToStem(scientificName));
                        if (namesByStem == null || namesByStem.size() == 0) {
                            issues.add("Name " + scientificName + " not recognised");
                            continueProcessing = false;
                        } else if (namesByStem.size() == 1) {
                            name = namesByStem.iterator().next();
                            issues.add("Name " + scientificName + " will be interpreted as " 
                                    + name.getScientificName() + " " + name.getAuthorship());
                        } else {
                            String message = "Name " + scientificName + " may match several - use ID for correct name:";
                            for (COLDPName n : namesByStem) {
                                message += " ID [" + n.getID() + "] -> " 
                                        + n.getScientificName() + " " + n.getAuthorship() 
                                        + (n.getTaxon() == null ? " (not accepted)" : " (accepted)");
                                issues.add(message);
                                continueProcessing = false;
                            }
                        }
                    } else if (names.size() == 1) {
                        name = names.iterator().next();
                    } else if (names.size() > 1) {
                        String message = "Name " + scientificName + " ambiguous - use ID for correct name:";
                        for (COLDPName n : names) {
                            message += " ID [" + n.getID() + "] -> " 
                                    + n.getScientificName() + " " + n.getAuthorship() 
                                    + (n.getTaxon() == null ? " (not accepted)" : " (accepted)");
                            issues.add(message);
                            continueProcessing = false;
                        }
                    }

                    COLDPTaxon taxon = null;

                    if (name != null) {
                        taxon = name.getTaxon();

                        if (taxon == null) {
                            if (name.getSynonyms() != null && name.getSynonyms().size() != 0) {
                                if (name.getSynonyms().size() == 1) {
                                    taxon = name.getSynonyms().iterator().next().getTaxon();
                                    issues.add("Name " + scientificName + " will be treated as a synonym for " 
                                            + taxon.getName().getScientificName() + " " + taxon.getName().getAuthorship());
                                } else {
                                    issues.add("Name " + scientificName + " is a synonym for multiple taxa");
                                    continueProcessing = false;
                                }
                            } else {
                                issues.add("Name " + scientificName + " not associated with a taxon");
                                continueProcessing = false;
                            }
                        }
                    }
                }
            }

            if (issues != null) {
                for (String issue : issues) {
                    System.err.println(issue);
                }
            }
        } catch (UnsupportedEncodingException | FileNotFoundException ex) {
            LOG.error("Could not process treatment file " + fileName, ex);
        }
        
        return continueProcessing;
    }

    private static void executeValidate() {
        COLDataPackage coldp = new COLDataPackage(coldpFolderName);

        boolean continueExecution = true;

        PrintWriter writer = null;

        if (outputFileName == null) {
            writer = new PrintWriter(System.out, true, StandardCharsets.UTF_8);
        } else if (!overwrite && new File(outputFileName).exists()) {
            reportError("File exists: " + outputFileName + " - specify -x to overwrite");
            continueExecution = false;
        } else {
            try {
                writer = new PrintWriter(outputFileName, "UTF-8");
            } catch (FileNotFoundException | UnsupportedEncodingException ex) {
                reportError("Could not open output file [" + outputFileName + "]:\n" + ex);
            }
        }

        if (continueExecution && logWriter != null) {

            for (COLDPName name : coldp.getNames().values()) {

                // Is the basionym supplied (default is self-reference for
                // original combination)? 
                //      --> NAME_NO_BASIONYM

                // If the basionym is different from the current name, does
                // the name have the same reference as the basionym? 
                //      --> NAME_SAME_REFERENCE_BASIONYM

                if (name.getBasionymID() == null) {
                    logIssue(ValidationCode.NAME_NO_BASIONYM, "name", 
                            name.getID().toString(), 
                            name.getScientificName() + " " + name.getAuthorship());
                } else if (!name.getID().equals(name.getBasionymID())) {
                    COLDPName basionym = name.getBasionym();
                    if (    basionym.getReferenceID() != null 
                         && name.getReferenceID() != null
                         && basionym.getReferenceID().equals(name.getReferenceID())) {
                        logIssue(ValidationCode.NAME_SAME_REFERENCE_BASIONYM, "name", name.getID().toString(), 
                            name.getScientificName() + " " + name.getAuthorship());
                    }
                }

                // Is the reference supplied for an original combination? 
                //      --> NAME_NO_REFERENCE                
                // Is the reference supplied for an subsequent combination? 
                //      --> COMBINATION_NO_REFERENCE

                if (name.getReferenceID() == null) {
                    logIssue(name.getID().equals(name.getBasionymID())
                                ? ValidationCode.NAME_NO_REFERENCE
                                : ValidationCode.COMBINATION_NO_REFERENCE, 
                            "name", name.getID().toString(), 
                            name.getScientificName() + " " + name.getAuthorship());
                } else {
                    COLDPNameReference nr = name.getRedundantNameReference(true);
                    if (nr != null) {
                        logIssue(ValidationCode.NAMEREFERENCE_REDUNDANT, "name",
                                 name.getID().toString(), 
                                 name.getScientificName() + " " + name.getAuthorship() + " --> " + nr.toCsv());
                    }
                }
            }

            writer.close();
        }
    }
    
    private static void renderTaxon(PrintWriter writer, COLDPTaxon taxon) {
        taxon.render(writer, 
            new TreeRenderProperties(TreeRenderType.HTML, 
                                     ContextType.None, 
                                     TreeRenderType.HTML.getIndentUnit(), 
                                     indentCount));
    }

    private static void renderHigherTaxa(PrintWriter writer, COLDPTaxon taxon) {
        List<COLDPTaxon> higherTaxa = new ArrayList<>();

        while (taxon.getParent() != null) {
            taxon = taxon.getParent();
            higherTaxa.add(0, taxon);
        }
        
        for (COLDPTaxon ancestor : higherTaxa) {
            ancestor.render(writer, 
                new TreeRenderProperties(TreeRenderType.HTML, 
                                         ContextType.HigherTaxa, 
                                         TreeRenderType.HTML.getIndentUnit(), 
                                         indentCount));
        }
    }

    private static boolean parseToHTMLCommandLine(String[] argv) {
        CommandLine command = null;
        Options options = new Options();
        options.addOption("x", "overwrite", false, "Overwrite existing output file")
                .addOption("t", "taxon", true, "Name of focus taxon")
                .addOption("f", "format", true, "Output format, defaults to HTML")
                .addOption("o", "output-file", true, "Output file name (defaults to stdout)")
                .addOption("l", "log-file", true, "Log file name")
                .addOption("L", "log-threshold", true, "Log reporting level, one of ERROR, WARNING, INFO")
                .addOption("w", "whitespace-level", true, "Initial indent level (two spaces per level)")
                .addOption("i", "identifier-type", true, 
                           "Current identifier type for Taxon, Name and "
                                   + "Reference records - one of Int, UUID and "
                                   + "String - defaults to match existing "
                                   + "records")
                .addOption("h", "help", false, "Show help")
                .addOption("v", "verbose", false, "Verbose")
                .addOption("T", "template", true, 
                           "Template in which to embed output (replacing first "
                                   + "row containing text specified using"
                                   + "-e option or (by default) "
                                   + "\"" + templateEyecatcherText + "\") - if "
                                   + "this text is not found, content will be "
                                   + "inserted at the end of the output file")
                .addOption("e", "eyecatcher", true, 
                           "Text contained in line in template file at point "
                                   + "where output content is to be inserted");

        CommandLineParser parser = new DefaultParser();
        
        try {
            command = parser.parse(options, argv);
        } catch (ParseException ex) {
            reportError("Failed to parse command line: " + ex.toString());
        }

        if (command != null) {
            verbose = command.hasOption("v");
            
            for (Option o : command.getOptions()) {
                switch (o.getOpt()) {
                    case "t":
                        selectedTaxonName = o.getValue();
                        if (verbose) {
                            reportInfo("Selected taxon name: " + selectedTaxonName);
                        }
                        break;
                    case "i":
                        try {
                            currentIdentifierType = IdentifierType.valueOf(o.getValue());
                        } catch(Exception e) {
                            // Swallow exception - error reporting will occur automatically
                            currentIdentifierType = null;
                        }
                        if (format == null) {
                            reportError("Unrecognised current identifier type for option -I: " + o.getValue());
                            command = null;
                        } else if (verbose) {
                            reportInfo("Selected current identifier type: " + currentIdentifierType.name());
                        }
                        break;
                    case "f":
                        try {
                            format = OutputFormat.valueOf(o.getValue());
                        } catch(Exception e) {
                            // Swallow exception - error reporting will occur automatically
                            format = null;
                        }
                        if (format == null) {
                            reportError("Unrecognised output format for option -f: " + o.getValue());
                            command = null;
                        } else if (!format.isSupported()) {
                            reportError("Currently unsupported output format: " + o.getValue());
                            command = null;
                        } else if (verbose) {
                            reportInfo("Output format: " + format.name());
                        }
                        break;
                    case "o":
                        outputFileName = o.getValue();
                        if (verbose) {
                            reportInfo("Output file name: " + outputFileName);
                        }
                        break;
                    case "l":
                        logFileName = o.getValue();
                        if (verbose) {
                            reportInfo("Log file name: " + logFileName);
                        }
                        break;
                    case "L":
                        try {
                            logThreshold = ValidationSeverity.valueOf(o.getValue().toUpperCase());
                            if (verbose) {
                                reportInfo("Log threshold: " + logThreshold.name());
                            }
                        } catch(Exception e) {
                            reportError("Invalid log threshold : " + o.getValue());
                        }
                        break;
                    case "w":
                        indentCount = Integer.parseInt(o.getValue());
                        if (verbose) {
                            reportInfo("Initial indent level: " + indentCount);
                        }
                        break;
                    case "x":
                        overwrite = true;
                        if (verbose) {
                            reportInfo("Overwrite existing output file: Yes");
                        }
                        break;
                    case "e":
                        templateEyecatcherText = o.getValue();
                        if (verbose) {
                            reportInfo("Text included in line for replacement in output template: " + templateEyecatcherText);
                        }
                        break;
                    case "T":
                        String templateFileName = o.getValue();
                        try {
                            templateReader = new BufferedReader(new InputStreamReader(new FileInputStream(templateFileName), "UTF-8"));
                            if (verbose) {
                                reportInfo("Output template file name: " + templateFileName);
                            }
                        } catch (FileNotFoundException | UnsupportedEncodingException e) {
                            reportError("Could not open template file " + templateFileName + ": " + e.toString());
                            command = null;
                        }
                        break;
                }
            }
            if (command != null) {
                String[] args = command.getArgs();
                if (args != null) {
                    switch (command.getArgs().length) {
                        case 0:
                            reportError("CoLDP input folder name not specified");
                            command = null;
                            break;
                        case 1:
                            coldpFolderName = command.getArgs()[0];
                            if (verbose) {
                                reportInfo("CoLDP input folder name: " + coldpFolderName);
                            }
                            break;
                        default:
                            reportError("Too many command line arguments");
                            command = null;
                            break;
                    }
                }
            }
        }
        
        if (command == null || command.hasOption("h")) {
            showHelp(options);
            command = null;
        }
        
        if (command != null && !openLogWriter()) {
            command = null;
        }

        return (command != null);
    }

    private static boolean parseEditCommandLine(String[] argv) {
        CommandLine command = null;
        Options options = new Options();
        options.addOption("l", "log-file", true, "Log file name")
                .addOption("L", "log-threshold", true, "Log reporting level, one of ERROR, WARNING, INFO")
                .addOption("Z", "zip-backup-folder", true, "Name of folder to store ZIP file backups, default current folder")
                .addOption("i", "identifier-type", true, 
                           "Current identifier type for Taxon, Name and "
                                   + "Reference records - one of Int, UUID and "
                                   + "String - defaults to match existing "
                                   + "records")
                .addOption("h", "help", false, "Show help")
                .addOption("v", "verbose", false, "Verbose");
        
        CommandLineParser parser = new DefaultParser();
        
        try {
            command = parser.parse(options, argv);
        } catch (ParseException ex) {
            reportError("Failed to parse command line: " + ex.toString());
        }

        if (command != null) {
            verbose = command.hasOption("v");
            
            for (Option o : command.getOptions()) {
                switch (o.getOpt()) {
                    case "i":
                        try {
                            currentIdentifierType = IdentifierType.valueOf(o.getValue());
                        } catch(Exception e) {
                            // Swallow exception - error reporting will occur automatically
                            currentIdentifierType = null;
                        }
                        if (format == null) {
                            reportError("Unrecognised current identifier type for option -I: " + o.getValue());
                            command = null;
                        } else if (verbose) {
                            reportInfo("Selected current identifier type: " + currentIdentifierType.name());
                        }
                        break;
                    case "l":
                        logFileName = o.getValue();
                        if (verbose) {
                            reportInfo("Log file name: " + logFileName);
                        }
                        break;
                    case "L":
                        try {
                            logThreshold = ValidationSeverity.valueOf(o.getValue().toUpperCase());
                            if (verbose) {
                                reportInfo("Log threshold: " + logThreshold.name());
                            }
                        } catch(Exception e) {
                            reportError("Invalid log threshold : " + o.getValue());
                        }
                        break;
                    case "Z":
                       backupFolderName = o.getValue();
                        if (verbose) {
                            reportInfo("Backup folder name: " + backupFolderName);
                        }
                        break;
                }
            }
            if (command != null) {
                String[] args = command.getArgs();
                if (args != null) {
                    switch (command.getArgs().length) {
                        case 0:
                            reportError("CoLDP input folder name not specified");
                            command = null;
                            break;
                        case 1:
                            coldpFolderName = command.getArgs()[0];
                            if (verbose) {
                                reportInfo("CoLDP input folder name: " + coldpFolderName);
                            }
                            break;
                        default:
                            reportError("Too many command line arguments");
                            command = null;
                            break;
                    }
                }
            }
        }
        
        if (command == null || command.hasOption("h")) {
            showHelp(options);
            command = null;
        }
        
        if (command != null && !openLogWriter()) {
            command = null;
        }

        return (command != null);
    }

    private static boolean parseModifyCommandLine(String[] argv) {
        CommandLine command = null;
        Options options = new Options();
        options.addOption("x", "overwrite", false, "Overwrite existing output file")
                .addOption("o", "output-folder", true, "Output folder name")
                .addOption("l", "log-file", true, "Log file name")
                .addOption("L", "log-threshold", true, 
                           "Log reporting level, one of ERROR, WARNING, INFO")
                .addOption("Z", "zip-backup-folder", true, "Name of folder to store ZIP file backups, default current folder")
                .addOption("M", "modification-strategy", true, 
                           "Aggressiveness of efforts to modify data, one of "
                                   + "CAUTIOUS (default), AGGRESSIVE")
                .addOption("h", "help", false, "Show help")
                .addOption("v", "verbose", false, "Verbose")
                .addOption("S", "suffix", true, 
                           "Suffix to file names when printing to CSV,"
                                   + "defaults to \"-NEW\" - a value of \"-\" "
                                   + "disables the suffix and causes files to "
                                   + "share the same names as originals - "
                                   + "replace with care to avoid overwriting "
                                   + "source CSV files")
                .addOption("i", "identifier-type", true, 
                           "Current identifier type for Taxon, Name and "
                                   + "Reference records - one of Int, UUID and "
                                   + "String - defaults to match existing "
                                   + "records")
                .addOption("I", "new-identifer-type", true, "Update type of "
                                   + "identifiers for references, names and "
                                   + "taxa, one of Int, UUID and String")
                .addOption("T", "treatment-file", true, 
                            "File containing name reference and distribution "
                                    + "data from one or more publications")
                .addOption("R", "fix-combination-reference", false, 
                           "Remove reference details from combination name "
                                   + "records where these are identical with the "
                                   + "basionym reference details");

        CommandLineParser parser = new DefaultParser();
        
        try {
            command = parser.parse(options, argv);
        } catch (ParseException ex) {
            reportError("Failed to parse command line: " + ex.toString());
        }

        if (command != null) {
            verbose = command.hasOption("v");
            
            for (Option o : command.getOptions()) {
                switch (o.getOpt()) {
                    case "o":
                        outputFolderName = o.getValue();
                        if (verbose) {
                            reportInfo("Output folder name: " + outputFolderName);
                        }
                        break;
                    case "l":
                        logFileName = o.getValue();
                        if (verbose) {
                            reportInfo("Log file name: " + logFileName);
                        }
                        break;
                    case "L":
                        try {
                            logThreshold = ValidationSeverity.valueOf(o.getValue().toUpperCase());
                            if (verbose) {
                                reportInfo("Log threshold: " + logThreshold.name());
                            }
                        } catch(Exception e) {
                            reportError("Invalid log threshold : " + o.getValue());
                        }
                        break;
                    case "Z":
                       backupFolderName = o.getValue();
                        if (verbose) {
                            reportInfo("Backup folder name: " + backupFolderName);
                        }
                        break;
                    case "M":
                        try {
                            modificationStrategy = ModificationStrategy.valueOf(o.getValue().toUpperCase());
                            if (verbose) {
                                reportInfo("Modification strategy: " + modificationStrategy.name());
                            }
                        } catch(Exception e) {
                            reportError("Invalid modification strategy : " + o.getValue());
                        }
                        break;
                    case "R":
                        fixNameSameReferenceBasionym = true;
                        if (verbose) {
                            reportInfo("Fix names reusing basionym reference: Yes");
                        }
                        break;
                    case "x":
                        overwrite = true;
                        if (verbose) {
                            reportInfo("Overwrite existing output files: Yes");
                        }
                        break;
                    case "I":
                        try {
                            newIdentifierType = IdentifierType.valueOf(o.getValue());
                        } catch(Exception e) {
                            // Swallow exception - error reporting will occur automatically
                            newIdentifierType = null;
                        }
                        if (format == null) {
                            reportError("Unrecognised new identifier type for option -I: " + o.getValue());
                            command = null;
                        } else if (verbose) {
                            reportInfo("Selected new identifier type: " + newIdentifierType.name());
                        }
                        break;
                    case "i":
                        try {
                            currentIdentifierType = IdentifierType.valueOf(o.getValue());
                        } catch(Exception e) {
                            // Swallow exception - error reporting will occur automatically
                            currentIdentifierType = null;
                        }
                        if (format == null) {
                            reportError("Unrecognised current identifier type for option -I: " + o.getValue());
                            command = null;
                        } else if (verbose) {
                            reportInfo("Selected current identifier type: " + currentIdentifierType.name());
                        }
                        break;
                    case "S":
                        newCsvSuffix = o.getValue();
                        if (newCsvSuffix.equals("-")) {
                            newCsvSuffix = "";
                        } 
                        if (verbose) {
                            reportInfo("Suffix for new CSV file names: " + newCsvSuffix);
                        }
                        break;
                    case "T":
                        treatmentFileName = o.getValue();
                        if (verbose) {
                            reportInfo("Treatment file name: " + treatmentFileName);
                        }
                        break;
                }
            }
            if (command != null) {
                String[] args = command.getArgs();
                if (args != null) {
                    switch (command.getArgs().length) {
                        case 0:
                            reportError("CoLDP input folder name not specified");
                            command = null;
                            break;
                        case 1:
                            coldpFolderName = command.getArgs()[0];
                            if (verbose) {
                                reportInfo("CoLDP input folder name: " + coldpFolderName);
                            }
                            break;
                        default:
                            reportError("Too many command line arguments");
                            command = null;
                            break;
                    }
                }
            }
        }
        
        if (command == null || command.hasOption("h")) {
            showHelp(options);
            command = null;
        }

        if (command != null && !openLogWriter()) {
            command = null;
        }

        return (command != null);
    }

    private static boolean parseValidateCommandLine(String[] argv) {
        CommandLine command = null;
        Options options = new Options();
        options.addOption("x", "overwrite", false, "Overwrite existing output file")
                .addOption("i", "identifier-type", true, 
                           "Current identifier type for Taxon, Name and "
                                   + "Reference records - one of Int, UUID and "
                                   + "String - defaults to match existing "
                                   + "records")
                .addOption("l", "log-file", true, "Log file name (defaults to stdout)")
                .addOption("L", "log-threshold", true, "Log reporting level, one of ERROR, WARNING, INFO")
                .addOption("h", "help", false, "Show help")
                .addOption("v", "verbose", false, "Verbose");

        CommandLineParser parser = new DefaultParser();
        
        try {
            command = parser.parse(options, argv);
        } catch (ParseException ex) {
            reportError("Failed to parse command line: " + ex.toString());
        }

        if (command != null) {
            verbose = command.hasOption("v");
            
            for (Option o : command.getOptions()) {
                switch (o.getOpt()) {
                    case "i":
                        try {
                            currentIdentifierType = IdentifierType.valueOf(o.getValue());
                        } catch(Exception e) {
                            // Swallow exception - error reporting will occur automatically
                            currentIdentifierType = null;
                        }
                        if (format == null) {
                            reportError("Unrecognised current identifier type for option -I: " + o.getValue());
                            command = null;
                        } else if (verbose) {
                            reportInfo("Selected current identifier type: " + currentIdentifierType.name());
                        }
                        break;
                    case "l":
                        logFileName = o.getValue();
                        if (verbose) {
                            reportInfo("Log file name: " + logFileName);
                        }
                        break;
                    case "L":
                        try {
                            logThreshold = ValidationSeverity.valueOf(o.getValue().toUpperCase());
                            if (verbose) {
                                reportInfo("Log threshold: " + logThreshold.name());
                            }
                        } catch(Exception e) {
                            reportError("Invalid log threshold : " + o.getValue());
                        }
                        break;
                    case "x":
                        overwrite = true;
                        if (verbose) {
                            reportInfo("Overwrite existing output file: Yes");
                        }
                        break;
                }
            }
            
            if (command != null) {
                String[] args = command.getArgs();
                if (args != null) {
                    switch (command.getArgs().length) {
                        case 0:
                            reportError("CoLDP input folder name not specified");
                            command = null;
                            break;
                        case 1:
                            coldpFolderName = command.getArgs()[0];
                            if (verbose) {
                                reportInfo("CoLDP input folder name: " + coldpFolderName);
                            }
                            break;
                        default:
                            reportError("Too many command line arguments");
                            command = null;
                            break;
                    }
                }
            }
        }
        
        if (command == null || command.hasOption("h")) {
            showHelp(options);
            command = null;
        }

        if (command != null && !openLogWriter()) {
            command = null;
        }

        return (command != null);
    }
    
    private static boolean backupCOLDP() {
        boolean success = true;
        
        String coldpName = coldpFolderName;
        int i = coldpName.lastIndexOf("/");
        if (i > 0) {
            coldpName = coldpName.substring(i + 1);
        }
 
        String backupFileName = backupFolderName
                    + (backupFolderName.endsWith("/") ? "" : "/")
                    + coldpName + "-"
                    + DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now()).replace(":", ".")
                    + ".zip";
        
        try {
            success = zipFolder(coldpFolderName, backupFileName);
        } catch (IOException e) {
            LOG.error("Could not open backup file " + backupFileName, e);
            success = false;
        }
       
        return success;
    }

    private static boolean openLogWriter() {
        boolean success = true;
        
        if (logFileName == null) {
            logWriter = new PrintWriter(System.out, true, StandardCharsets.UTF_8);
        } else if (!overwrite && new File(logFileName).exists()) {
            reportError("Log file exists: " + logFileName + " - specify -x to overwrite");
            success = false;
        } else {
            try {
                logWriter = new PrintWriter(logFileName, "UTF-8");
                logWriter.println("category,severity,issue,ID,text");

            } catch (FileNotFoundException | UnsupportedEncodingException ex) {
                reportError("Could not open log file [" + logFileName + "]:\n" + ex);
            }
        }
        return success;
    }

    private static void writeTemplate(PrintWriter writer, BufferedReader templateReader, String stopLine) {
        try {
            String line;
            boolean continueReading = true;
            
            while (continueReading && (line = templateReader.readLine()) != null) {
                if (stopLine == null || !line.contains(stopLine)) {
                    writer.println(line);
                } else {
                    continueReading = false;
                }
            }
        } catch (IOException e) {
            reportError("Failed to copy template to output file: " + e.toString());    
        }
    }
    
    private static void logIssue(ValidationCode code, String category, String associatedID, String text) {
        issueCounts[code.getSeverity().getThreshold()]++;
        
        if (code.getSeverity().getThreshold() <= logThreshold.getThreshold()) {
            String formatted;
            if (logFileName != null) {
                formatted = buildCSV(category, code.getSeverity().name(), code.getMessage(), associatedID, text);
            } else {
                formatted = code.getSeverity().name() + ": " + code.getMessage() + ": [" + associatedID + "] " + text;
            }
            logWriter.println(formatted);
        }
    }

    private static void reportError(String s) {
        outputText("ERROR: " + s);
    }
    
    private static void reportInfo(String s) {
        outputText(" INFO: " + s);
    }

    private static void showMainHelp() {
        for (String s : MAIN_HELP) { 
            outputText(s);
        }
    }
 
    private static void showHelp(Options options) {
        showMainHelp();
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("CoLDPTool <options> coldpFolderName", options);
    }
    
    private static void outputText(String s) {
        System.err.println(s);
    }
    
    private static final String[] MAIN_HELP = {
        "",
        "COLDPTool Help",
        "--------------",
        "",
        "Tools for manipulating the contents of Catalogue of Life Data Package",
        "(COLDP) files. Current tools operate on unzipped folders of CSV files.",
        "",
        "Available commands - type \"COLDPTool <COMMANDNAME>\" for more help:",
        "",
        "  COLDP VALIDATE <options> folderName",
        "    - Report on issues detected in COLDP data",
        "",
        "  COLDP TOHTML <options> folderName",
        "    - Format contents as a set of nested HTML <div> elements",
        "",
        "  COLDP MODIFY <options> folderName",
        "    - Transform and rewrite COLDP data",
        ""
    };
}