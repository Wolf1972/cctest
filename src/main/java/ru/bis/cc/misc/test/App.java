package ru.bis.cc.misc.test;

import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Files processing: parse, compares and assembles files in FT14, SWIFT and UFEBS formats
 *
 */

public class App {

  // TODO: corr. account, BIC and other business constants move into separated class
  // TODO: MT940 import and compare
  // TODO: MT940 export
  // TODO: process split operations in FT14 import
  // TODO: import static information - clients, accounts
  // TODO: FT14 import

  private static FDocumentArray sampleDocs = new FDocumentArray(); // Checked documents array
  private static FDocumentArray patternDocs = new FDocumentArray(); // Pattern documents array (for compare)
  private static FDocumentArray reverseDocs = new FDocumentArray(); // Incoming documents array

  static ClientArray clients = new ClientArray(); // Static clients information
  static AccountArray accounts = new AccountArray(); // Static account information

  public static void main(String[] args) {

    System.out.println("CC test suite (c) BIS 2020.");

    Options options = new Options();
    options.addRequiredOption("a", "action", true, "Type of action: \"transform\", \"compare\", \"check\".");
    options.addOption("i", "input", true, "Input files directory.");
    options.addOption("o", "output", true, "Output files directory (for \"transform\" action only).");
    options.addOption("t", "type", true, "Output files type: UFEBS, FT14, MT103, BQ (for \"transform\" action only).");
    options.addOption("p", "pattern", true, "Pattern files directory (for \"compare\" action only).");
    options.addOption("x", "xsd", true, "XSD files directory (when specified and input or output files are UFEBS it performs XSD check).");
    options.addOption("d", "date", true, "Change date in transformed documents [YYYY-MM-DD] (for \"transform\" action only).");
    options.addOption("c", "codepage", true, "Code page for output XML files [\"windows-1251\" or \"utf-8\"] (for \"transform\" action only).");
    options.addOption("r", "reverse", false, "Output incoming (reverse) documents, confirmations and statement from payment system (with -t UFEBS option only).");
    options.addOption("s", "static", true, "Load static data (clients, accounts) from specified directory.");

    // Command line options
    String cmdAction = null;
    String cmdOutputType = null;
    String inPath = null;
    String outPath = null;
    String patternPath = null;
    String xsdPath = null;
    String cmdDate = null;
    String cmdCodePage = null;
    String staticPath = null;
    boolean cmdReverse; // Prepare incoming documents, confirmations, statement by outgoing documents

    CommandLineParser parser = new DefaultParser();
    try {
      CommandLine command = parser.parse(options, args);

      if (command.hasOption('a')) cmdAction = command.getOptionValue('a');
      if (command.hasOption('i')) inPath = command.getOptionValue('i');
      if (command.hasOption('o')) outPath = command.getOptionValue('o');
      if (command.hasOption('p')) patternPath = command.getOptionValue('p');
      if (command.hasOption('x')) xsdPath = command.getOptionValue('x');
      if (command.hasOption('t')) cmdOutputType = command.getOptionValue('t');
      if (command.hasOption('d')) cmdDate = command.getOptionValue('d');
      if (command.hasOption('c')) cmdCodePage = command.getOptionValue('c');
      if (command.hasOption('s')) staticPath = command.getOptionValue('s');
      cmdReverse = command.hasOption('r');

    }
    catch (ParseException e) {
      System.out.println("Command line parse exception.");
      HelpFormatter help = new HelpFormatter();
      help.printHelp(App.class.getSimpleName(), options);
      return;
    }

    // Looking for log4j
    String log4JPropertyFile = "log4j2.xml"; // Is Log4j configuration file in custom place?
    if (Files.isRegularFile(Paths.get(log4JPropertyFile))) {
      System.setProperty("log4j.configurationFile", log4JPropertyFile);
    }
    // We will define configuration later
    Logger logger = LogManager.getLogger(App.class);
    logger.trace("0001: Current directory: " + System.getProperty("user.dir"));

    ProcessorFabric fab = new ProcessorFabric();

    if (cmdAction != null) {

      if (staticPath != null) {
        BQProcessor bqProc = (BQProcessor) fab.getProcessor(FileType.BQ);
        if (bqProc != null) {
          bqProc.readAllStatic(staticPath);
        }
      }

      if (cmdAction.equalsIgnoreCase("compare")) {
        if (inPath != null) {
          AProcessor procSample = fab.getProcessor(inPath);
          if (procSample != null) {
            procSample.readAll(inPath, sampleDocs);
            if (xsdPath != null && procSample.getClass() == UFEBSProcessor.class) {
              procSample.checkAll(inPath, xsdPath);
            }
            if (patternPath != null) {
              AProcessor procPattern = fab.getProcessor(patternPath);
              if (procPattern != null) {
                procPattern.readAll(patternPath, patternDocs);
                Comparator comparator = new Comparator();
                comparator.compare(patternDocs, sampleDocs);
              }
              else {
                logger.error("0012: Unknown pattern files format.");
              }
            }
            else {
              logger.error("0013: Pattern path doesn't specified. Use -p option.");
            }
          }
          else {
            logger.error("0014: Unknown input files format.");
          }
        }
        else {
          logger.error("0015: Input path doesn't specified. Use -i option.");
        }
      }

      else if (cmdAction.equalsIgnoreCase("transform")) {
        if (inPath != null) {
          AProcessor procIn = fab.getProcessor(inPath);
          if (procIn != null) {
            if (xsdPath != null && procIn.getClass() == UFEBSProcessor.class) {
              procIn.checkAll(inPath, xsdPath);
            }
            procIn.readAll(inPath, sampleDocs);
          }
          try {
            if (cmdOutputType != null) {
              FileType fileType = FileType.valueOf(cmdOutputType);
              AProcessor procOut = fab.getProcessor(fileType);
              if (procOut != null) {
                if (outPath != null) {
                  if (cmdDate != null) {
                    logger.info("0016: New date assigned to output documents: " + cmdDate);
                    sampleDocs.setDate(cmdDate); // Change date
                  }
                  procOut.createAll(outPath, sampleDocs); // Create target documents
                  if (procOut.getClass() == UFEBSProcessor.class) {
                    if (cmdReverse) { // Create reverse documents if UFEBS and -r option
                      reverseDocs = sampleDocs.createReverse();
                      procOut.createAll(outPath, reverseDocs);
                      UFEBSProcessor procUFEBS = (UFEBSProcessor) procOut; // Downcast with no doubts
                      if (cmdCodePage != null) procUFEBS.setCodePage(cmdCodePage);
                      procUFEBS.createConfirmations(outPath, sampleDocs);
                      procUFEBS.createStatement(outPath, sampleDocs, reverseDocs);
                    }
                    if (xsdPath != null)
                      procOut.checkAll(outPath, xsdPath); // Check output documents if UFEBS
                  }
                }
                else {
                  logger.error("0017: Output path doesn't specified. Use -o option.");
                }
              }
            }
            else {
              logger.error("0018: Output file type doesn't specified. Use -t option.");
            }
          }
          catch(IllegalArgumentException e){
            logger.error("0019: Unknown format type specified: " + cmdOutputType);
          }
        }
        else {
          logger.error("0020: Input path doesn't specified. Use -i option.");
        }
      }

      else if (cmdAction.equalsIgnoreCase("check")) {
        AProcessor procIn = fab.getProcessor(inPath);
        if (procIn != null) {
          if (procIn.getClass() == UFEBSProcessor.class) {
            procIn.checkAll(inPath, xsdPath);
          }
          else {
            logger.error("0021: XSD check available for UFEBS input/output files.");
          }
        }
        else {
          logger.error("0022: Input path doesn't specified. Use -i option.");
        }
      }
    }

    logger.info("0000: End of work.");

  }

}