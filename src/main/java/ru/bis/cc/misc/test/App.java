package ru.bis.cc.misc.test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static java.nio.file.Files.isRegularFile;
import static java.nio.file.Files.newDirectoryStream;

/**
 * UFEBS files parsing, converting to FT14 packet
 *
 */
public class App {

    private static Logger logger = null; // We will define configuration later

    public static HashMap<Long, FDocument> fDocs = new HashMap<>(); // Documents array

    public static void main(String[] args) {

        String configPath = ".\\target\\";

        String inPath = ".\\target\\in\\";
        String outPath = ".\\target\\out\\";
        String xsdPath = ".\\target\\XMLSchemas\\";

        int docCount = 0;
        int filesCount = 0;
        int filesError = 0;

        System.out.println("UFEBS CC test helper (c) BIS 2020.");

        // Looking for log4j
        String log4JPropertyFile = configPath + "log4j2.xml"; // Is Log4j configuration file in custom place?
        if (Files.isRegularFile(Paths.get(log4JPropertyFile))) {
            System.setProperty("log4j.configurationFile", log4JPropertyFile);
        }
        logger = LogManager.getLogger(App.class);

        FileInputStream fis;
        Properties property = new Properties();
        String configFile = configPath + "config.properties";
        try {
            fis = new FileInputStream(configFile);
            property.load(fis);
            inPath = property.getProperty("inPath");
            outPath = property.getProperty("outPath");
            xsdPath = property.getProperty("xsdPath");
        }
        catch (IOException e) {
            logger.error("THE0007: Error opening properties file: " + configFile);
        }

        if (!Files.isDirectory(Paths.get(outPath))) {
            logger.error("THE0004: Error access output directory " + outPath);
        }
        else {

            try (DirectoryStream<Path> directoryStream = newDirectoryStream(Paths.get(inPath))) {
                for (Path path : directoryStream) {
                    filesCount++;
                    if (isRegularFile(path)) {
                        String fileName = path.getFileName().toString();
                        logger.info("THI0001: Processing file: " + inPath + fileName);
                        if (isXMLFile(inPath + fileName)) {
                            if (!processOneFile(inPath + fileName, xsdPath)) filesError++;
                        } else {
                            logger.error("THE0002: File " + fileName + " is not contains XML prolog.");
                            filesError++;
                        }
                    }
                }
            }
            catch (IOException e) {
                logger.error("THE0001: Error while file system access: " + inPath);
                filesError++;
            }

            String outFile = outPath + "ft14test.txt";
            try {
                Charset chs = Charset.forName("ISO-8859-5");
                OutputStream os = new FileOutputStream(outFile);
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, chs));
                if (fDocs.size() > 0) {
                    for (Map.Entry<Long, FDocument> item : fDocs.entrySet()) {
                        Long key = item.getKey();
                        FDocument value = item.getValue();
                        writer.write(value.toFT14String(key));
                        writer.write("\r\n");
                    }
                }
                writer.close();
            }
            catch (IOException e) {
                logger.error("TH0005: Error write output file " + outFile);
            }

        }
        logger.info("THI0002: Files processed: " + filesCount + ", errors: " + filesError);
        logger.info("THI0003: Documents added: " + fDocs.size());
        logger.info("THI0001: End of work.");

    }

    public static boolean processOneFile(String fileName, String path2XSD) {
/**
 * Process one UFEBS file, fills fDocs array
 * Returns errors count
 */
        try {

            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = documentBuilder.parse(fileName);

            // Try to obtain root element
            Node root = document.getDocumentElement();
            String rootNodeName = root.getNodeName();
            if (rootNodeName.equals("PacketEPD")) { // For packets EPD
                if (isXMLValid(fileName, path2XSD + "ed\\cbr_packetepd_v2020.2.0.xsd")) {
                    NodeList eds = root.getChildNodes();
                    for (int i = 0; i < eds.getLength(); i++) {
                        // Each node: ED, empty text etc
                        Node ed = eds.item(i);
                        if (ed.getNodeType() != Node.TEXT_NODE) {
                            String nodeName = ed.getNodeName();
                            if (nodeName.matches("ED10[134]")) {
                                FDocument fDoc = new FDocument();
                                fDoc.getFromED(ed);
                                logger.trace("THI0101: Packet item: " + fDoc.toString());
                                if (!fDocs.containsKey(fDoc.docNum)) {
                                    fDocs.put(Long.parseLong(fDoc.docNum), fDoc);
                                }
                                else {
                                    logger.error("THE1008: Document ID " + fDoc.docNum + "has already added.");
                                }
                            } else {
                                logger.error("THE1001: File " + fileName + ", element " + i + " contains unknown element: " + nodeName);
                            }
                        }
                    }
                }
            }
            else if (rootNodeName.matches("ED10[134]")) { // For single EPD
                if (isXMLValid(fileName, path2XSD + "ed\\" + "cbr_" + rootNodeName + "_v2020.2.0.xsd")) {
                    FDocument fDoc = new FDocument();
                    fDoc.getFromED(root);
                    logger.trace("THEI0102: Single ED: " + fDoc.toString());
                    fDocs.put(Long.parseLong(fDoc.docNum), fDoc);
                }
            } else {
                logger.error("THE1002: File " + fileName + " contains unknown root element: " + rootNodeName);
            }

        } catch (ParserConfigurationException | SAXException e) {
            logger.error("THE1003: Error parsing file " + fileName, e);
            return false;
        } catch (IOException e) {
            logger.error("THE1004. Error while file access: " + fileName, e);
            return false;
        }
        return true;
    }

    public static boolean isXMLFile(String fileName) {
        try {
            RandomAccessFile raf = new RandomAccessFile(fileName, "r");
            String firstStr = raf.readLine();
            if (firstStr != null) {
                if (firstStr.matches("^<\\?xml?.+"))
                    return true;
            }
            raf.close();
        }
        catch (IOException e) {
            logger.error("THE0201: Error access file: " + fileName, e);
        }
        return false;
    }

    public static boolean isXMLValid(String fileName, String xsdFile) {
        if (!Files.isRegularFile(Paths.get(xsdFile))) {
            logger.error("THE0202: Error access XSD file " + xsdFile);
            return false;
        }
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new StreamSource(xsdFile));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(fileName));
            logger.trace("THI0201: XSD check completed for file " + fileName);
            return true;
        }
        catch (IOException e) {
            logger.error("THE0203: Error access file " + fileName + " while XML scheme check.", e);
            return false;
        }
        catch (SAXException e) {
            logger.error("THE0204: XML file " + fileName + " doesn't accord with XML scheme.", e);
            return false;
        }
    }
}
