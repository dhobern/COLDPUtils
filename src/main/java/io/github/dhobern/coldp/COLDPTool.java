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
import io.github.dhobern.coldp.TreeRenderProperties.ContextType;
import io.github.dhobern.coldp.TreeRenderProperties.TreeRenderType;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
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
     * Enumeration of validation codes
     */
    private static enum ValidationCode {
        NAME_NO_BASIONYM(ValidationSeverity.ERROR, "No basionym specified for combination"),
        NAME_NO_REFERENCE(ValidationSeverity.ERROR, "No reference specified for original name"),
        NAME_SAME_REFERENCE_BASIONYM(ValidationSeverity.ERROR, "Same reference specified for basionym and combination"),
        COMBINATION_NO_REFERENCE(ValidationSeverity.WARNING, "No reference specified for combination")
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
        NONE, TOHTML, MODIFY, VALIDATE; 
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

    /**
     * Enumeration of identifier styles
     */
    private static enum IdentifierStyle {
        None, Int;
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
    private static IdentifierStyle identifierStyle = IdentifierStyle.None;
    private static int[] issueCounts = { 0, 0, 0 };
    private static boolean verbose = false;
    
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
        
        switch (commandName) {
            case TOHTML -> executeToHTML(arguments);
            case MODIFY -> executeModify(arguments);
            case VALIDATE -> executeValidate(arguments);
        }
        
        if (logWriter != null) {
            logWriter.close();
        }
        
        if (verbose && (issueCounts[0] + issueCounts[1] > 0)) {
            reportInfo("Issues detected in data: " + issueCounts[0] + " Errors, " + issueCounts[1] + " Warnings");
            reportInfo("Run COLDPTool VALIDATE to review");
        }
    }
    
    private static void executeToHTML(String[] arguments) {
        boolean continueExecution = parseToHTMLComandLine(arguments);
        
        if (continueExecution) {
           
            COLDataPackage coldp = new COLDataPackage(coldpFolderName);

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
                        COLDPTaxon taxon = coldp.getTaxonByName(selectedTaxonName);

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
    }
    
    private static void executeModify(String[] arguments) {
        if (parseModifyComandLine(arguments)) {

            COLDataPackage coldp = new COLDataPackage(coldpFolderName);

            if (identifierStyle == IdentifierStyle.Int) {
                coldp.tidyIdentifiers();
            }

            coldp.write(outputFolderName, newCsvSuffix, overwrite);
        }
    }
    
    private static void executeValidate(String[] arguments) {
        boolean continueExecution = parseValidateComandLine(arguments);
        
        if (continueExecution) {
            COLDataPackage coldp = new COLDataPackage(coldpFolderName);

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
                    }
                }

                writer.close();
            }
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

    private static boolean parseToHTMLComandLine(String[] argv) {
        CommandLine command = null;
        Options options = new Options();
        options.addOption("x", "overwrite", false, "Overwrite existing output file")
                .addOption("t", "taxon", true, "Name of focus taxon")
                .addOption("f", "format", true, "Output format, defaults to HTML")
                .addOption("o", "output-file", true, "Output file name (defaults to stdout)")
                .addOption("l", "log-file", true, "Log file name")
                .addOption("L", "log-threshold", true, "Log reporting level, one of ERROR, WARNING, INFO")
                .addOption("i", "initial-indent", true, "Initial indent level (two spaces per level)")
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
                    case "i":
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

    private static boolean parseModifyComandLine(String[] argv) {
        CommandLine command = null;
        Options options = new Options();
        options.addOption("x", "overwrite", false, "Overwrite existing output file")
                .addOption("o", "output-folder", true, "Output folder name")
                .addOption("l", "log-file", true, "Log file name")
                .addOption("L", "log-threshold", true, "Log reporting level, one of ERROR, WARNING, INFO")
                .addOption("h", "help", false, "Show help")
                .addOption("v", "verbose", false, "Verbose")
                .addOption("S", "suffix", true, 
                           "Suffix to file names when printing to CSV,"
                                   + "defaults to \"-NEW\" - replace with "
                                   + "care to avoid overwriting source CSV "
                                   + "files")
                .addOption("I", "identifer-style", true, "Update style of "
                                   + "identifiers for references, names and "
                                   + "taxa, one of Int");

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
                    case "x":
                        overwrite = true;
                        if (verbose) {
                            reportInfo("Overwrite existing output files: Yes");
                        }
                        break;
                    case "I":
                        try {
                            identifierStyle = IdentifierStyle.valueOf(o.getValue());
                        } catch(Exception e) {
                            // Swallow exception - error reporting will occur automatically
                            identifierStyle = null;
                        }
                        if (format == null) {
                            reportError("Unrecognised identifierStyle for option -I: " + o.getValue());
                            command = null;
                        } else if (verbose) {
                            reportInfo("Selected identifier style: " + identifierStyle.name());
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

    private static boolean parseValidateComandLine(String[] argv) {
        CommandLine command = null;
        Options options = new Options();
        options.addOption("x", "overwrite", false, "Overwrite existing output file")
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