# COLDPUtils
Tools for working with [Catalogue of Life Data Package](https://github.com/CatalogueOfLife/coldp) files.

**Under development**

At present, the tools rely on specific characteristics of the COLDP files in the [Alucitoidea](https://github.com/dhobern/alucitoidea) and [Pterophoroidea](https://github.com/dhobern/pterophoroidea) checklists, including:

* Integer IDs for Name, Taxon and Reference records
* Patterns of use for NameReference records
* Separation of Name and Taxon records rather than use of NameUsage records

COLDPTool is the main entry point with three major subcommands:

**COLDP VALIDATE \<options\> folderName**
Report on issues detected in COLDP data

Options:
* -h,--help                  Show help
* -l,--log-file \<arg\>        Log file name (defaults to stdout)
* -L,--log-threshold \<arg\>   Log reporting level, one of ERROR, WARNING, INFO
* -v,--verbose               Verbose
* -x,--overwrite             Overwrite existing output file
  
**COLDP TOHTML \<options\> folderName**
Format contents as a set of nested HTML \<div\> elements

Options:
* -e,--eyecatcher \<arg\>       Text contained in line in template file at point where output content is to be inserted
* -f,--format \<arg\>           Output format, defaults to HTML
* -h,--help                   Show help
* -i,--initial-indent \<arg\>   Initial indent level (two spaces per level)
* -l,--log-file \<arg\>         Log file name
* -L,--log-threshold \<arg\>    Log reporting level, one of ERROR, WARNING, INFO
* -o,--output-file \<arg\>      Output file name (defaults to stdout)
* -t,--taxon \<arg\>            Name of focus taxon
* -T,--template \<arg\>        Template in which to embed output (replacing first row containing text specified using -e option or (by default) "COLDPTool.Output") - if this text is not found, content will be inserted at the end of the output file
* -v,--verbose                Verbose
* -x,--overwrite              Overwrite existing output file

**COLDP MODIFY \<options\> folderName**
Transform and rewrite COLDP data

Options:
* -h,--help                    Show help
* -I,--identifer-style \<arg\>   Update style of identifiers for references, names and taxa, one of Int
* -l,--log-file \<arg\>          Log file name
* -L,--log-threshold \<arg\>     Log reporting level, one of ERROR, WARNING, INFO
* -o,--output-folder \<arg\>     Output folder name
* -S,--suffix \<arg\>            Suffix to file names when printing to CSV,defaults to "-NEW" - replace with care to avoid overwriting source CSV files
* -v,--verbose                 Verbose
* -x,--overwrite               Overwrite existing output file
