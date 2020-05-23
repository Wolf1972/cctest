package ru.bis.cc.misc.test;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.HashMap;

/**
 * UFEBS parsing
 *
 */
public class App 
{
    public static void main( String[] args )

    {

        HashMap<Long, FDocument> fDocs = new HashMap<>(); // Documents array

        System.out.println( "UFEBS test helper (c) BIS 2020." );
        try {
            String fileName = "packet.xml"; // TODO

            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = documentBuilder.parse(fileName);

            // Try to obtain root element
            Node root = document.getDocumentElement();
            String rootNodeName = root.getNodeName();
            if (rootNodeName.equals("PacketEPD")) {
                // root: Packet ED
                NodeList eds = root.getChildNodes();
                for (int i = 0; i < eds.getLength(); i++) {
                    // Each node: ED, empty text etc
                    Node ed = eds.item(i);
                    if (ed.getNodeType() != Node.TEXT_NODE) {
                        String nodeName = ed.getNodeName();
                        if (nodeName.matches("ED10[134]")) {
                            FDocument fDoc = new FDocument();
                            fDoc.getFromED(ed);

                            System.out.println("AccDocNo " + fDoc.docNum);
                            System.out.println("PayerName " + fDoc.payerName);
                            System.out.println("PayeeName " + fDoc.payeeName);

                            fDocs.put(Long.getLong(fDoc.docNum), fDoc);
                        }
                        else {
                            System.out.println("TH002: File " + fileName + ", element " + i + " contains unknown element: " + nodeName);
                        }
                    }
                }
            }
            else if (rootNodeName.matches("ED1[134]")) {
                // root: Single ED
            }
            else {
                System.out.println("TH001: File " + fileName + " contains unknown root element: " + rootNodeName);
            }
/*
            System.out.println("List of packet:");
            System.out.println();
            // Просматриваем все подэлементы корневого - т.е. ED
            NodeList eds = root.getChildNodes();
            for (int i = 0; i < eds.getLength(); i++) {
                Node ed = eds.item(i);
                // Если нода не текст, то это ED - заходим внутрь
                if (ed.getNodeType() != Node.TEXT_NODE) {
                    NodeList edOne = ed.getChildNodes();
                    for (int j = 0; j < edOne.getLength(); j++) {
                        Node edProp = edOne.item(j);
                        // Если нода не текст, то это один из параметров - печатаем
                        if (edProp.getNodeType() != Node.TEXT_NODE) {
                            System.out.println(edProp.getNodeName()); // + ":" + edProp.getChildNodes().item(0).getTextContent());
                        }
                    }
                    System.out.println("===========>>>>");
                }
            }
*/
        } catch (ParserConfigurationException ex) {
            ex.printStackTrace(System.out);
        } catch (SAXException ex) {
            ex.printStackTrace(System.out);
        } catch (IOException ex) {
            ex.printStackTrace(System.out);
        }
    }
}
