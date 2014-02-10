package governance.plugin.services;

import governance.plugin.ServiceGovernanceMojo;
import governance.plugin.rxt.AbstractArtifactCreator;
import governance.plugin.GovernanceSOAPMessageCreator;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import java.util.Map;

/**
 * Created by jayanga on 2/9/14.
 */
public class ServiceCreator extends AbstractArtifactCreator {
    Log logger;
    public ServiceCreator(Log logger, String endPointRef, String genericArtifactManagerEndPointRef) throws MojoExecutionException {
        super(endPointRef, genericArtifactManagerEndPointRef);
        this.logger = logger;
    }

    public boolean create(Map<String, String> parameters) throws MojoExecutionException {

        String name = parameters.get("name");
        String namespace = parameters.get("namespace");
        String version = parameters.get("version");
        String type = parameters.get("type");
        String description = parameters.get("description");

        description = (description != null && description != "")? description: "Generated by governance maven plugin.";

        String servicePath = getArtifactResourcePath(new String[]{name, version});

        String createServiceRequst =
                ServiceGovernanceSOAPMessageCreator.createAddServiceRequest(name
                        , version
                        , type
                        , description);

        logger.debug("Service creation request. [" + createServiceRequst + "]");

        boolean isServiceCreated = super.createArtifact(servicePath, createServiceRequst);

        if (logger.isInfoEnabled()){
            if (isServiceCreated){
                logger.info("Request sent to create Service: "+ name);
            }else{
                logger.info("Service already available: " + name);
            }
        }

        return isServiceCreated;
    }

    @Override
    public boolean create(Object[] parameters) throws MojoExecutionException {
        return false;
    }

    @Override
    public String getArtifactResourcePath(String[] parameters) throws MojoExecutionException {
        String name = parameters[0];
        String version = parameters[1];

        return ServiceGovernanceMojo.GREG_SERVICE_RESOURCE_PATH  + name + "/" + version;
    }
}
