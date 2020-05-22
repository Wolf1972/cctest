package ru.bis.cc.misc.test;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        try {
            // Создается построитель документа
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            // Создается дерево DOM документа из файла
            Document document = documentBuilder.parse("packet.xml");

            // Получаем корневой элемент
            Node root = document.getDocumentElement();

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

        } catch (ParserConfigurationException ex) {
            ex.printStackTrace(System.out);
        } catch (SAXException ex) {
            ex.printStackTrace(System.out);
        } catch (IOException ex) {
            ex.printStackTrace(System.out);
        }
    }
}
