package governance.plugin.rxt;

import governance.plugin.GovernanceSOAPMessageCreator;
import governance.plugin.GovernanceMojo;
import governance.plugin.RegistrySOAPClient;

import java.io.ByteArrayInputStream;
import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.maven.plugin.MojoExecutionException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ArtifactCreatorUtil {
	private DocumentBuilderFactory factory;
	private DocumentBuilder builder;
	
	private String genericArtifactManagerEndPointRef;
	
	public ArtifactCreatorUtil(String genericArtifactManagerEndPointRef) throws MojoExecutionException{
		configure();
		
		try{
			factory = DocumentBuilderFactory.newInstance();
			builder = factory.newDocumentBuilder();
		}
		catch(ParserConfigurationException e){
			throw new MojoExecutionException(e.getMessage());
		}
		
		this.genericArtifactManagerEndPointRef = genericArtifactManagerEndPointRef;
	}
	
	public void configure(){
		System.setProperty("javax.net.ssl.trustStore", GovernanceMojo.GREG_HOME + File.separator + "repository" +
                File.separator + "resources" + File.separator + "security" + File.separator +
                "wso2carbon.jks");
	}
	
	public boolean isArtifactExisting(String resourcePath) throws MojoExecutionException{	
		String getArtifactRequest = GovernanceSOAPMessageCreator.createGetArtifactContentRequest(resourcePath);
		String response = RegistrySOAPClient.sendMessage(genericArtifactManagerEndPointRef, getArtifactRequest);
		return ((response != null) && (isContentAvailable(response)));
	}

	public boolean isContentAvailable(String responseString) throws MojoExecutionException{
	        Document doc = null;
	        String textContent = null;
            try {
	            doc = builder.parse(new ByteArrayInputStream(RegistrySOAPClient.stripMessage(responseString).getBytes()));
	            NodeList nodes = doc.getElementsByTagName("ns:return");
	            if (nodes != null && nodes.getLength() != 0){
	            	Node returnNode = nodes.item(0);
    	            if (returnNode != null){
    	            	textContent = returnNode.getTextContent();
    	            }
	            }
            } catch (Exception e) {
            	throw new MojoExecutionException(e.getMessage());
            }
            return ((textContent != null) && (!textContent.isEmpty()));
	}
}
