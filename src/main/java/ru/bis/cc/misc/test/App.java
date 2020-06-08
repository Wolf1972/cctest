package ru.bis.cc.misc.test;

import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 * Files processing: parse, compares and assembles files in FT14, SWIFT and UFEBS format
 *
 */
public class App {

  private static HashMap<Long, FDocument> sampleDocs = new HashMap<>(); // Checked documents array
  private static HashMap<Long, FDocument> patternDocs = new HashMap<>(); // Pattern documents array (for compare)

  public static void main(String[] args) {

    System.out.println("UFEBS CC test helper (c) BIS 2020.");

    Options options = new Options();
    Option action = new Option("a", "action", true, "Type of action: transform,compare, check.");
    action.setArgs(1); action.isRequired();
    options.addOption(action);
    Option input = new Option("i", "input", true, "Input files directory.");
    input.setArgs(1);
    options.addOption(input);
    Option output = new Option("o", "output", true, "Output files directory.");
    output.setArgs(1);
    options.addOption(output);
    Option outType = new Option("t", "type", true, "Output files type: UFEBS, FT14, MT103, BQ.");
    outType.setArgs(1);
    options.addOption(outType);
    Option pattern = new Option("p", "pattern", true, "Pattern files directory.");
    pattern.setArgs(1);
    options.addOption(pattern);
    Option xsd = new Option("x", "xsd", true, "XSD files directory.");
    pattern.setArgs(1);
    options.addOption(xsd);
    Option date = new Option("d", "date", true, "Change date in transformed documents [YYYY-MM-DD].");
    date.setArgs(1);
    options.addOption(date);

    // Command line options
    String cmdAction = ""; // Action
    String cmdType = null; // Type of output file
    String inPath = null;
    String outPath = null;
    String patternPath = null;
    String xsdPath = null;
    String cmdDate = null; // Date to change

    CommandLineParser parser = new DefaultParser();
    try {
      CommandLine command = parser.parse(options, args);

      String[] arguments = command.getOptionValues("a");
      if (arguments != null && arguments.length > 0) cmdAction = arguments[0];
      arguments = command.getOptionValues("i");
      if (arguments != null && arguments.length > 0) inPath = arguments[0];
      arguments = command.getOptionValues("o");
      if (arguments != null && arguments.length > 0) outPath = arguments[0];
      arguments = command.getOptionValues("t");
      if (arguments != null && arguments.length > 0) cmdType = arguments[0];
      arguments = command.getOptionValues("p");
      if (arguments != null && arguments.length > 0) patternPath = arguments[0];
      arguments = command.getOptionValues("x");
      if (arguments != null && arguments.length > 0) xsdPath = arguments[0];
      arguments = command.getOptionValues("d");
      if (arguments != null && arguments.length > 0) cmdDate = arguments[0];
    }
    catch (ParseException e) {
      System.out.println("Command line parse exception. ");
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
          AProcessor procIn = ProcessorFabric.getProcessor(inPath, logger);
          if (procIn != null) procIn.readAll(inPath, sampleDocs);
          AProcessor procPattern = ProcessorFabric.getProcessor(patternPath, logger);
          if (procPattern != null) procPattern.readAll(patternPath, patternDocs);
          if (procIn != null && procPattern != null) {
            Comparator comparator = new Comparator(logger);
            comparator.compare(patternDocs, sampleDocs);
          }
        }
        else {
          logger.error("0012: Input path doesn't specified.");
        }
      }
      else if (cmdAction.equalsIgnoreCase("transform")) {
        AProcessor procIn = ProcessorFabric.getProcessor(inPath, logger);
        if (procIn != null) procIn.readAll(inPath, sampleDocs);
        try {
          FileType fileType = FileType.valueOf(cmdType);
          AProcessor procOut = ProcessorFabric.getProcessor(fileType, logger);
          if (cmdDate != null) Helper.setDate(cmdDate, sampleDocs);
          if (procOut != null) procOut.createAll(outPath, sampleDocs);
        }
        catch (IllegalArgumentException e) {
          logger.error("0010: Unknown format type: " + cmdType);
        }
      }
      else if (cmdAction.equalsIgnoreCase("check")) {
        AProcessor procIn = ProcessorFabric.getProcessor(inPath, logger);
        if (procIn != null && procIn.getClass() == UFEBSProcessor.class) {
          procIn.checkAll(inPath, xsdPath);
        }
      }
    }
    else {
      logger.error("0011: Unknown action.");
    }

    logger.info("0000: End of work.");

  }

}