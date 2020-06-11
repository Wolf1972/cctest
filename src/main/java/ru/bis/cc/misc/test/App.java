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

  private static FDocumentArray sampleDocs = new FDocumentArray(); // Checked documents array
  private static FDocumentArray patternDocs = new FDocumentArray(); // Pattern documents array (for compare)
  private static FDocumentArray reverseDocs = new FDocumentArray(); // Incoming documents array

  public static void main(String[] args) {

    System.out.println("UFEBS CC test helper (c) BIS 2020.");

    Options options = new Options();
    options.addRequiredOption("a", "action", true, "Type of action: \"transform\", \"compare\", \"check\".");
    options.addOption("i", "input", true, "Input files directory.");
    options.addOption("o", "output", true, "Output files directory (for \"transform\" action only).");
    options.addOption("t", "type", true, "Output files type: UFEBS, FT14, MT103, BQ (for \"transform\" action only).");
    options.addOption("p", "pattern", true, "Pattern files directory (for \"compare\" action only).");
    options.addOption("x", "xsd", true, "XSD files directory (when specified and input or output files are UFEBS it performs XSD check).");
    options.addOption("d", "date", true, "Change date in transformed documents [YYYY-MM-DD] (for \"transform\" action only).");
    options.addOption("c", "codepage", true, "Code page for output XML files [\"windows-1251\" or \"utf-8\"] (for \"transform\" action only).");
    options.addOption("r", "reverse", false, "Prepare incoming (reverse) documents, confirmations and statement from payment system (with -t UFEBS option only).");

    // Command line options
    String cmdAction = null;
    String cmdOutputType = null;
    String inPath = null;
    String outPath = null;
    String patternPath = null;
    String xsdPath = null;
    String cmdDate = null;
    String cmdCodePage = null;
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
      cmdReverse = command.hasOption('r');

    }
    catch (ParseException e) {
      System.out.println("Command line parse exception. ");
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

    if (cmdAction != null) {

      if (cmdAction.equalsIgnoreCase("compare")) {
        if (inPath != null) {
          AProcessor procSample = ProcessorFabric.getProcessor(inPath, logger);
          if (procSample != null) {
            procSample.readAll(inPath, sampleDocs);
            if (xsdPath != null && procSample.getClass() == UFEBSProcessor.class) {
              procSample.checkAll(inPath, xsdPath);
            }
            if (patternPath != null) {
              AProcessor procPattern = ProcessorFabric.getProcessor(patternPath, logger);
              if (procPattern != null) {
                procPattern.readAll(patternPath, patternDocs);
                Comparator comparator = new Comparator(logger);
                comparator.compare(patternDocs, sampleDocs);
              }
              else {
                logger.error("0012: Unknown pattern files format.");
              }
            }
            else {
              logger.error("0012: Pattern path doesn't specified. Use -p option.");
            }
          }
          else {
            logger.error("0012: Unknown input files format.");
          }
        }
        else {
          logger.error("0012: Input path doesn't specified. Use -i option.");
        }
      }

      else if (cmdAction.equalsIgnoreCase("transform")) {
        if (inPath != null) {
          AProcessor procIn = ProcessorFabric.getProcessor(inPath, logger);
          if (procIn != null) {
            if (xsdPath != null && procIn.getClass() == UFEBSProcessor.class) {
              procIn.checkAll(inPath, xsdPath);
            }
            procIn.readAll(inPath, sampleDocs);
          }
          try {
            FileType fileType = FileType.valueOf(cmdOutputType);
            AProcessor procOut = ProcessorFabric.getProcessor(fileType, logger);
            if (procOut != null) {
              if (outPath != null) {
                if (cmdDate != null) {
                  logger.info("0012: New date assigned to output documents: " + cmdDate);
                  sampleDocs.setDate(cmdDate); // Change date
                }
                procOut.createAll(outPath, sampleDocs); // Create target documents
                if (procOut.getClass() == UFEBSProcessor.class) {
                  if (cmdReverse) { // Create reverse documents if UFEBS and -r option
                    reverseDocs = sampleDocs.createReverse(logger);
                    procOut.createAll(outPath, reverseDocs);
                    UFEBSProcessor procUFEBS = (UFEBSProcessor) procOut; // Downcast with no doubts
                    if (cmdCodePage != null) procUFEBS.setCodePage(cmdCodePage);
                    procUFEBS.createConfirmations(outPath, sampleDocs);
                    procUFEBS.createStatement(outPath, sampleDocs, reverseDocs);
                  }
                  if (xsdPath != null) procOut.checkAll(outPath, xsdPath); // Check output documents if UFEBS
                }
              }
              else {
                logger.error("0012: Output path doesn't specified. Use -o option.");
              }
            }
          }
          catch (IllegalArgumentException e) {
            logger.error("0010: Unknown format type specified: " + cmdOutputType);
          }
        }
        else {
          logger.error("0012: Input path doesn't specified. Use -i option.");
        }
      }

      else if (cmdAction.equalsIgnoreCase("check")) {
        AProcessor procIn = ProcessorFabric.getProcessor(inPath, logger);
        if (procIn != null) {
          if (procIn.getClass() == UFEBSProcessor.class) {
            procIn.checkAll(inPath, xsdPath);
          }
          else {
            logger.error("0012: XSD check available for UFEBS input/output files.");
          }
        }
        else {
          logger.error("0012: Input path doesn't specified. Use -i option.");
        }
      }
    }

    logger.info("0000: End of work.");

  }

}