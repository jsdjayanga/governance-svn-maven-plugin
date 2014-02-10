package governance.plugin.rxt;

import governance.plugin.GovernanceMojo;
import governance.plugin.RegistrySOAPClient;

import org.apache.maven.plugin.MojoExecutionException;


public abstract class  AbstractArtifactCreator{

	private int createdArtifacteCount = 0;
	private int existingArtifactCount = 0;
	
	private String artifactEndPointRef;
	
	private ArtifactCreatorUtil artifactCreatorUtil;
	
	public AbstractArtifactCreator(String endPointRef, String genericArtifactManagerEndPointRef) throws MojoExecutionException{
		this.artifactEndPointRef = endPointRef;
		artifactCreatorUtil = new ArtifactCreatorUtil(genericArtifactManagerEndPointRef);	
	}
	
	public abstract boolean create(Object[] parameters) throws MojoExecutionException;
	
	public abstract String getArtifactResourcePath(String[] parameters)throws MojoExecutionException;
	
	
	public final int getCreatedArtifactCount() {
	    return createdArtifacteCount;
    }

	public final int getExistingArtifactCount() {
	    return existingArtifactCount;
    }
	
	public final void increaseCreatedArtifactCount(){
		createdArtifacteCount++;
	}
	
	public final void increasExistingArtifactCount(){
		existingArtifactCount++;
	}
	
	public final String getArtifactEndpointRef(){
		return artifactEndPointRef;
	}
		
	public boolean createArtifact(String resourcePath, String soapRequest) throws MojoExecutionException{
		boolean isArtifactCreated = false;

		if (artifactCreatorUtil.isArtifactExisting(resourcePath) == false){
			RegistrySOAPClient.sendMessage(artifactEndPointRef, soapRequest);
			createdArtifacteCount++;
			isArtifactCreated = true;
		}else{
			existingArtifactCount++;
		}
		return isArtifactCreated;
	}

	public String getAbsoluteArtifactResourcePath(String[] parameters) throws MojoExecutionException{
		return GovernanceMojo.GREG_TRUNK_LOCATION + getArtifactResourcePath(parameters);
	}
}
