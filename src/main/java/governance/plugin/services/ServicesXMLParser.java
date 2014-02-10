package governance.plugin.services;

import com.google.inject.internal.util.$SourceProvider;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by jayanga on 2/9/14.
 */
public class ServicesXMLParser {
    public static List<Object> parse(File file){

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        DocumentBuilder documentBuilder = null;

        List<Object> serviceInfoList = new LinkedList<Object>();

        try {
            documentBuilder = factory.newDocumentBuilder();
            try {
                Document document = documentBuilder.parse(file);
                NodeList nodeList = document.getElementsByTagName("service");

                for (int index = 0; index < nodeList.getLength(); index++){
                    Node node = nodeList.item(index);
                    if (node != null){
                        Node nameNode = node.getAttributes().getNamedItem("name");
                        Node versionNode = node.getAttributes().getNamedItem("version");
                        Node descriptionNode = node.getAttributes().getNamedItem("description");

                        String name = (nameNode != null && nameNode.getTextContent() != "")? nameNode.getTextContent(): "un-named";
                        String version = (versionNode != null && versionNode.getTextContent() != "")? versionNode.getTextContent(): "1.0.0";
                        String description = (descriptionNode != null && descriptionNode.getTextContent() != "")? descriptionNode.getTextContent(): "";
                        String namespace = getNamespace(node);

                        String[] serviceInfo = {name, version, "Axis2", description};
                        serviceInfoList.add(serviceInfo);

                        //System.out.println("============================:" + index + "|" + file.toString());
                    }
                }
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        return serviceInfoList;
    }

    private static String getNamespace(Node node){

        Node namespaceNode = node.getAttributes().getNamedItem("targetNamespace");
        if (namespaceNode != null && namespaceNode.getTextContent() != ""){
            return namespaceNode.getTextContent();
        }

        NodeList nodeList = node.getChildNodes();
        if (nodeList != null){
            for (int index = 0; index < nodeList.getLength(); index++){
                Node cnode = nodeList.item(index);
                if (cnode != null){
                    if (cnode.getNodeName().equals("parameter")){
                        NamedNodeMap namedNodeMap = cnode.getAttributes();
                        if (namedNodeMap != null){
                            Node nameNode = namedNodeMap.getNamedItem("name");
                            if (nameNode != null && nameNode.getTextContent().equals("ServiceClass")){
                                String serviceClassName = cnode.getTextContent();
                                String[] split = serviceClassName.split("[.]");
                                StringBuilder sb = new StringBuilder();
                                sb.append("http://");
                                for (int i = split.length - ((split.length > 2)?2:1); i >= 0; i--) {
                                    sb.append(split[i]);
                                    if (i > 0){
                                        sb.append(".");
                                    }
                                }
                                return sb.toString();
                            }
                        }
                    }
                }
            }
        }

        return "ns";
    }
}
