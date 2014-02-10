package governance.plugin.rxt;

import governance.plugin.GovernanceMojo;
import governance.plugin.GovernanceSOAPMessageCreator;


import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

public class DependencyCreator extends AbstractArtifactCreator{
	Log logger;
	
	public DependencyCreator(Log logger, String dependencyEnpointRef, String genericArtifactManagerEndPointRef) 
			throws MojoExecutionException{
		super(dependencyEnpointRef, genericArtifactManagerEndPointRef);
		this.logger = logger;	
	}
	
	@Override
	public String getArtifactResourcePath(String[] parameters) throws MojoExecutionException{
		if (parameters.length != 2){
			throw new MojoExecutionException("Dependecncy Resource Path expects 2 parameters:" +
					"'GroupID' and 'ArtifactID'");
		}
		
		String groupId = parameters[0];
		String artifactId = parameters[1];
		
		return GovernanceMojo.GREG_DEPENDENCY_RESOURCE_PATH  + groupId + "/" + artifactId;
	}
	
	@Override
	public boolean create(Object[] parameters) throws MojoExecutionException{
		if (parameters.length != 3){
			throw new MojoExecutionException("Module Creater expects 3 parameters:" +
					"'GroupID', 'ArtifactID' and 'version'");
		}
		
		String groupId = (String)parameters[0];
		String artifactId = (String)parameters[1];
		String version = (String)parameters[2];
		
		String modulePath = getArtifactResourcePath(new String[]{groupId, artifactId});
		
		String createDependencyRequst = 
    			GovernanceSOAPMessageCreator.createAddDependencyRequest(groupId, artifactId, version);

        logger.debug("Dependency creation request. [" + createDependencyRequst + "]");

		boolean isDependencyCreated = super.createArtifact(modulePath, createDependencyRequst);
		
		if (logger.isInfoEnabled()){
    		if (isDependencyCreated){
    			logger.info("Request sent to create dependency: "+ groupId + "/" +  artifactId);
    		}else{
    			logger.info("Dependency already available: " + groupId + "/" +  artifactId);
    		}
		}
		
		return isDependencyCreated;
	}
}
