package governance.plugin.rxt;

import governance.plugin.GovernanceSOAPMessageCreator;
import governance.plugin.GovernanceMojo;


import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

public class ModuleCreator extends AbstractArtifactCreator{

	Log logger;
	
	public ModuleCreator(Log logger, String moduleEnpointRef, String genericArtifactManagerEndPointRef) 
			throws MojoExecutionException{
		super(moduleEnpointRef, genericArtifactManagerEndPointRef);
		this.logger = logger;	
	}
	
	@Override
	public String getArtifactResourcePath(String[] parameters) throws MojoExecutionException{
		if (parameters.length != 2){
			throw new MojoExecutionException("Module Resource Path expects 2 parameters: " +
					"'artifactID' and 'version'");
		}
		
		String artifactID = parameters[0];
		String version = parameters[1];
		
		return GovernanceMojo.GREG_MODULE_RESOURCE_PATH  + artifactID + "/" + version;
	}
	
	@Override
	public boolean create(Object[] parameters) throws MojoExecutionException{
		if (parameters.length != 3){
			throw new MojoExecutionException("Module Creater expects 3 Parameters: " +
					"'artifactID', 'version' and 'filepath' as parameters");
		}
		
		String artifactID = (String)parameters[0];
		String version = (String)parameters[1];
		String filePath = (String)parameters[2];
		
		String modulePath = getArtifactResourcePath(new String[]{artifactID, version});
		
		String createModuleRequst = 
    			GovernanceSOAPMessageCreator.createAddModuleRequest(artifactID
    			                                                    , version
    			                                                    , filePath);

        logger.debug("Module creation request. [" + createModuleRequst + "]");

		boolean isModuleCreated = super.createArtifact(modulePath, createModuleRequst);
		
		if (logger.isInfoEnabled()){
    		if (isModuleCreated){
    			logger.info("Request sent to create module: "+ artifactID + "/" +  version);
    		}else{
    			logger.info("Module already available: " + artifactID + "/" +  version);
    		}
		}
		
		return isModuleCreated;
	}
	
}
