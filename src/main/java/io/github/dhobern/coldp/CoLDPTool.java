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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.logging.Level;
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
public class CoLDPTool {

    /**
     * Enumeration of output formats and their current support level
     */
    private static enum OutputFormat {
        HTML(true), CSV(true), TSV(false), JSON(false);
        
        private boolean supported = false;
        
        OutputFormat(boolean supported) {
            this.supported = supported;
        }
        
        public boolean isSupported() {
            return supported;
        }
    }
    
    private static final Logger LOG = LoggerFactory.getLogger(CoLDPTool.class);
    
    private static String selectedTaxonName;
    private static String coldpFolderName;
    private static String outputFileName;
    private static int indentCount = 0;
    
    private static OutputFormat format = OutputFormat.HTML;
    
    private static boolean overwrite = false;
    
    
    /**
     *
     * @param argv
     */
    public static void main(String argv[]) {
        
        boolean continueExecution = parseComandLine(argv);
        
        PrintWriter writer = null;

        if (outputFileName == null) {
            writer = new PrintWriter(System.out, true);
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
            CoLDataPackage coldp = new CoLDataPackage(coldpFolderName);
            
            List<CoLDPTaxon> taxa = coldp.getRootTaxa();
            
            for(CoLDPTaxon taxon : taxa) {
                taxon.render(
                    writer, 
                    new TreeRenderProperties(TreeRenderType.HTML, 
                                             ContextType.None, 
                                             TreeRenderType.HTML.getIndentUnit(), 
                                             indentCount));
            }
            
            writer.close();
        }
    }

    private static boolean parseComandLine(String[] argv) {
        CommandLine command = null;
        Options options = new Options();
        options.addOption("x", "overwrite", false, "Overwrite existing output file")
                .addOption("t", "taxon", true, "Name of focus taxon")
                .addOption("f", "format", true, "Output format, one of HTML, JSON, CSV, TSV")
                .addOption("o", "output-file", true, "Output file name (defaults to stdout)")
                .addOption("i", "initial-indent", true, "Initial indent level (two spaces per level)")
                .addOption("h", "help", false, "Show help")
                .addOption("v", "verbose", false, "Verbose");

        CommandLineParser parser = new DefaultParser();
        
        try {
            command = parser.parse(options, argv);
        } catch (ParseException ex) {
            reportError("Failed to parse command line: " + ex.toString());
        }

        if (command != null) {
            boolean verbose = command.hasOption("v");
            
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
                }
            }
            String[] args = command.getArgs();
            if (args != null) {
                switch (command.getArgs().length) {
                    case 0:
                        reportError("CoLDP folder name not specified");
                        command = null;
                        break;
                    case 1:
                        coldpFolderName = command.getArgs()[0];
                        if (verbose) {
                            reportInfo("CoLDP folder name: " + coldpFolderName);
                        }
                        break;
                    default:
                        reportError("Too many command line arguments");
                        command = null;
                        break;
                }
            }
        }
        
        if (command == null || command.hasOption("h")) {
            showHelp(options);
            command = null;
        }

        return (command != null);
    }

    private static void reportError(String s) {
        outputText("ERROR: " + s);
    }
    
    private static void reportInfo(String s) {
        outputText(" INFO: " + s);
    }

    private static void showHelp(Options options) {
        outputText("");
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("CoLDPTool <options> coldpFolderName", options);
    }
    
    private static void outputText(String s) {
        System.out.println(s);
    }
}