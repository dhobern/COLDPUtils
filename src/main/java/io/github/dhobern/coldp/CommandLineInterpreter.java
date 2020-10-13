/*
 * Copyright 2020 Platyptilia.
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

import java.util.Map;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 *
 * @author Platyptilia
 */
public class CommandLineInterpreter {
    
    private static String templateEyecatcherText = "COLDPTool.Output";
    
    private static final Map<String, Option> availableOptions = Map.of(
/*
        "x", new Option("x", "overwrite", false, "Overwrite existing output file"),
        "t", new Option("t", "taxon", true, "Name of focus taxon"),
        "f", new Option("f", "format", true, "Output format, defaults to HTML"),
        "o", new Option("o", "output-file", true, "Output file name (defaults to stdout)"),
        "l", new Option("l", "log-file", true, "Log file name"),
        "L", new Option("L", "log-threshold", true, "Log reporting level, one of ERROR, WARNING, INFO"),
        "w", new Option("w", "whitespace-level", true, "Initial indent level (two spaces per level)"),
        "i", new Option("i", "identifier-type", true, "Current identifier type for Taxon, Name and Reference records - one of Int, UUID and String - defaults to match existing records"),
        "h", new Option("h", "help", false, "Show help"),
        "T", new Option("T", "template", true, "Template in which to embed output (replacing first row containing text specified using -e option or (by default) " + templateEyecatcherText + "\") - if this text is not found, content will be inserted at the end of the output file"),
        "e", new Option("e", "eyecatcher", true, "Text contained in line in template file at point where output content is to be inserted"),
        "Z", new Option("Z", "zip-backup-folder", true, "Name of folder to store ZIP file backups, default current folder"),
*/
        "O", new Option("O", "output-folder", true, "Output folder name"),
        "M", new Option("M", "modification-strategy", true, "Aggressiveness of efforts to modify data, one of CAUTIOUS (default), AGGRESSIVE"),
        "S", new Option("S", "suffix", true, "Suffix to file names when printing to CSV, defaults to \"-NEW\" - replace with care to avoid overwriting source CSV files"),
        "I", new Option("I", "new-identifer-type", true, "Update type of identifiers for references, names and taxa, one of Int, UUID and String"),
        "T", new Option("T", "treatment-file", true, "File containing name reference and distribution data from one or more publications"),
        "R", new Option("R", "fix-combination-reference", false, "Remove reference details from combination name records where these are identical with the basionym reference details"),
        "v", new Option("v", "verbose", false, "Verbose"));

    private static void stuffAndNonsense() {
        Options options = new Options();
        options.addOption("x", "overwrite", false, "Overwrite existing output file")
                .addOption("t", "taxon", true, "Name of focus taxon")
                .addOption("f", "format", true, "Output format, defaults to HTML")
                .addOption("o", "output-file", true, "Output file name (defaults to stdout)")
                .addOption("l", "log-file", true, "Log file name")
                .addOption("L", "log-threshold", true, "Log reporting level, one of ERROR, WARNING, INFO")
                .addOption("w", "whitespace-level", true, "Initial indent level (two spaces per level)")
                .addOption("i", "identifier-type", true, "Current identifier type for Taxon, Name and Reference records - one of Int, UUID and String - defaults to match existing records")
                .addOption("h", "help", false, "Show help")
                .addOption("T", "template", true, "Template in which to embed output (replacing first row containing text specified using -e option or (by default) " + templateEyecatcherText + "\") - if this text is not found, content will be inserted at the end of the output file")
                .addOption("e", "eyecatcher", true, "Text contained in line in template file at point where output content is to be inserted")
                .addOption("Z", "zip-backup-folder", true, "Name of folder to store ZIP file backups, default current folder")
                .addOption("O", "output-folder", true, "Output folder name")
                .addOption("Z", "zip-backup-folder", true, "Name of folder to store ZIP file backups, default current folder")
                .addOption("M", "modification-strategy", true, "Aggressiveness of efforts to modify data, one of CAUTIOUS (default), AGGRESSIVE")
                .addOption("S", "suffix", true, "Suffix to file names when printing to CSV, defaults to \"-NEW\" - replace with care to avoid overwriting source CSV files")
                .addOption("I", "new-identifer-type", true, "Update type of identifiers for references, names and taxa, one of Int, UUID and String")
                .addOption("T", "treatment-file", true, "File containing name reference and distribution data from one or more publications")
                .addOption("R", "fix-combination-reference", false, "Remove reference details from combination name records where these are identical with the basionym reference details")
                .addOption("v", "verbose", false, "Verbose");
        

    }
    
}
