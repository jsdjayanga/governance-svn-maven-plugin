package governance.plugin.webapps;

import com.google.inject.internal.util.$SourceProvider;
import governance.plugin.utils.PackageToNamespace;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by jayanga on 2/11/14.
 */
public class WebXMLParser {
    public static List<Object> parse(File file){
        List<Object> serviceInfoList = new LinkedList<Object>();

        Document document = parseFile(file);
        if (document != null){
            System.out.println("====================: " + document.getDocumentElement().getNodeName());

            NodeList servletNodeList = document.getElementsByTagName("servlet");
            if (servletNodeList != null){

                for (int index = 0; index < servletNodeList.getLength(); index++){
                    Node servletNode = servletNodeList.item(index);
                    if (servletNode != null){
                        NodeList servletChildNodeList = servletNode.getChildNodes();
                        if (servletChildNodeList != null){

                            String servletName = getServletName(servletChildNodeList, "servlet-name");
                            String servletDisplyName = getServletName(servletChildNodeList, "display-name");
                            String servletClassName = getServletName(servletChildNodeList, "servlet-class");
                            String version = "1.0.0";
                            String description = "";

                            servletName = servletName.trim();
                            servletDisplyName = servletDisplyName.trim();
                            servletClassName = servletClassName.trim();

                            int dotOffSet = servletClassName.lastIndexOf('.');
                            if (dotOffSet == -1){
                                dotOffSet = servletClassName.length();
                            }
                            String namespace = PackageToNamespace.PackageToNamespace(servletClassName.substring(0, dotOffSet));
                            namespace = namespace.toLowerCase();

                            String serviceClass = servletClassName.substring(dotOffSet);

                            if (servletDisplyName != null && servletDisplyName.equals("")){
                                servletDisplyName = servletName;
                            }

                            Map<String, String> serviceInfo = new HashMap<String, String>();
                            serviceInfo.put("name", servletName.trim());
                            serviceInfo.put("namespace", namespace.trim());
                            serviceInfo.put("serviceclass", namespace);
                            serviceInfo.put("displayname", servletDisplyName);
                            serviceInfo.put("version", version);
                            serviceInfo.put("description", description);

                            serviceInfoList.add(serviceInfo);
                        }
                    }
                }
            }
        }

        return serviceInfoList;
    }

    private static Document parseFile(File file){
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = null;
        Document doc = null;

        try {
            dBuilder = dbFactory.newDocumentBuilder();

            try {
                doc = dBuilder.parse(file);
                doc.getDocumentElement().normalize();

                return doc;

            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static String getServletName(NodeList nodeList, String name){

        for (int index = 0; index < nodeList.getLength(); index++){

            Node node = nodeList.item(index);
            if (node != null){
                if (node.getNodeName().equals(name)){
                    return node.getTextContent();
                }
            }
        }

        return "";
    }
}
