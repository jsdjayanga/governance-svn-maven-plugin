package governance.plugin;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.google.inject.internal.util.$SourceProvider;
import governance.plugin.rxt.ArtifactCreatorUtil;
import governance.plugin.rxt.DependencyCreator;
import governance.plugin.rxt.ModuleCreator;
import governance.plugin.services.ServiceCreator;
import governance.plugin.services.ServiceJavaFileParser;
import governance.plugin.services.ServicesXMLParser;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Generates dependency tree by reading a pom.xml
 */
@Mojo( name = "service", defaultPhase = LifecyclePhase.DEPLOY,  aggregator = true)
public class ServiceGovernanceMojo extends AbstractMojo
{
	//TODO : Get from a Configuration

	public static final String GREG_URL = "https://localhost:9443/services/";
	public static final String GREG_PASSWORD = "admin";
	public static final String GREG_USERNAME = "admin";
	public static final String GREG_HOME = "/home/jayanga/WSO2/WSO2Software/wso2greg-4.6.0";

	//GReg Media types
	public static final String GREG_MODULE_MEDIA_TYPE = "application/vnd.wso2-module+xml";
	public static final String GREG_DEPENDENCY_MEDIA_TYPE = "application/vnd.wso2-dependency+xml";

	//GReg resource paths
	public static final String GREG_TRUNK_LOCATION = "/_system/governance";
	public static final String GREG_MODULE_RESOURCE_PATH = "/trunk/modules/";
    public static final String GREG_SERVICE_RESOURCE_PATH = "/trunk/services/";
	public static final String GREG_DEPENDENCY_RESOURCE_PATH = "/trunk/dependencies/";

	//End point references
	private String moduleEndPointRef = ServiceGovernanceMojo.GREG_URL + "Module.ModuleHttpsSoap12Endpoint";
    private String serviceEndPointRef = ServiceGovernanceMojo.GREG_URL + "Service.ServiceHttpsSoap12Endpoint";
	private String dependecnyEndPointRef =  ServiceGovernanceMojo.GREG_URL + "Dependency.DependencyHttpsSoap12Endpoint";
	private String genericArtifactManagerEndPointRef = ServiceGovernanceMojo.GREG_URL +
			"ManageGenericArtifactService.ManageGenericArtifactServiceHttpsSoap12Endpoint";
	private String relationServiceEndPointRef = ServiceGovernanceMojo.GREG_URL
			+ "RelationAdminService.RelationAdminServiceHttpsSoap12Endpoint";

	private int pomFileCount = 0;
	private int directoryCount = 0;
    private int servicesXMLCount = 0;
    private int javaFileCount = 0;

	private ModuleCreator moduleCreator;
    private ServiceCreator serviceCreator;
	private DependencyCreator dependencyCreator;
    private ArtifactCreatorUtil artifactCreatorUtil;

    //private File currentPOM;
    private Map<String, File> pomMap;

	/**
     * The Maven project.
 	*/
    @Parameter ( defaultValue = "${project}" )
    private MavenProject project;
    /**
     * Location of the SVN repository.
     */
    @Parameter( property = "location" )
    private String repositoryLocation;

    public ServiceGovernanceMojo() throws MojoExecutionException{
    	 moduleCreator = new ModuleCreator(getLog(), moduleEndPointRef, genericArtifactManagerEndPointRef);
         serviceCreator = new ServiceCreator(getLog(), serviceEndPointRef, genericArtifactManagerEndPointRef);
    	 dependencyCreator = new DependencyCreator(getLog(), dependecnyEndPointRef, genericArtifactManagerEndPointRef);
         artifactCreatorUtil = new ArtifactCreatorUtil(genericArtifactManagerEndPointRef);

         pomMap = new HashMap<String, File>();
    }
    
    public void execute() throws MojoExecutionException
    {	
    	configure();
    	
    	if (repositoryLocation == null){
    		repositoryLocation = project.getBasedir().getPath();
    	}
    
    	getLog().info("Starting to scan with root:" + repositoryLocation);
        scanDirectory(repositoryLocation);
        
        
        getLog().info("SUMMARY:" 
        			  + "\nDirectories Scanned..............." + directoryCount
                      + "\npom.xml Files Processed..........." + pomFileCount
                      + "\nservices.xml Files Processed......" + servicesXMLCount
                      + "\njava Files Processed.............." + javaFileCount
                      + "\nModules ...........[Created:" + moduleCreator.getCreatedArtifactCount() 
                      + ", Existing:" + moduleCreator.getExistingArtifactCount() + "]"
                      + "\nDependencies ......[Created:" + dependencyCreator.getCreatedArtifactCount() 
                      + ", Existing:" + dependencyCreator.getExistingArtifactCount() + "]"
                      + "\nServices...........[Created:" + serviceCreator.getCreatedArtifactCount()
                      + ", Existing:" + serviceCreator.getExistingArtifactCount() + "]");
                      
    }
    
    private void configure(){
    	System.setProperty("javax.net.ssl.trustStore", GREG_HOME +  File.separator + "repository" + File.separator + 
    	                   "resources" + File.separator + "security"+ File.separator + "client-truststore.jks");
    	
    	System.setProperty("javax.net.ssl.trustStorePassword","wso2carbon");
    	System.setProperty("javax.net.ssl.trustStoreType","JKS");
    }
    
    private void scanDirectory(String path) throws MojoExecutionException{
      	File root = new File(path);
    	if (root.isDirectory()){
    		directoryCount++;
    		File[] children = root.listFiles();
    		if (children == null){
       		 	getLog().debug("Empty directory skipping.. :" + path);
    		}else{
                File pomFile = findPOMFileInCurrentDirectory(children);
                if (pomFile != null){
                    pomMap.put(pomFile.getParent(), pomFile);
                }

    			for (File child : children){
    				scanDirectory(child.getAbsolutePath());
    			}
    		}
    	}
    	else{
    		process(new File(path));
    	}
    	getLog().debug("Finished scanning directory :" + path);
    }
    
    private void addAssociation(String sourcePath, String destinationPath, String type) throws MojoExecutionException{
    	String addAssocationRequest = GovernanceSOAPMessageCreator.
    			createAddAssociationRequst(sourcePath, destinationPath, type, "add");

        RegistrySOAPClient.sendMessage(relationServiceEndPointRef, addAssocationRequest);

        getLog().debug("Association added. [Source=" + sourcePath + ", Destination=" + destinationPath + "]");
    }
    
    public void process(File file) throws MojoExecutionException{
        getLog().debug("Processing " + file.getAbsoluteFile());

    	if (file.getName().equals("services.xml")){
            servicesXMLCount++;

            List<Object> serviceInfoList = ServicesXMLParser.parse(file);
            //Object[] serviceInfoArray = serviceInfoList.toArray(new Object[serviceInfoList.size()]);

            for (int i = 0; i < serviceInfoList.size(); i++){
                serviceCreator.create((Map<String, String>)serviceInfoList.get(i));
                linkServiceWithModule((Map<String, String>)serviceInfoList.get(i), file);
            }

        }else if (file.getName().endsWith(".java")){
            javaFileCount++;

            List<Object> serviceInfoList = ServiceJavaFileParser.parse(file);
            //Object[] serviceInfoArray = serviceInfoList.toArray(new Object[serviceInfoList.size()]);

            for (int i = 0; i < serviceInfoList.size(); i++){
                serviceCreator.create((Map<String, String>)serviceInfoList.get(i));
                linkServiceWithModule((Map<String, String>)serviceInfoList.get(i), file);
            }
        }


    }

    public void linkServiceWithModule(Map<String, String> parameters, File file) throws MojoExecutionException {

        File currentPOM = findNearestPOMFile(file);
        if (currentPOM == null){
            throw new MojoExecutionException("Cannot find a POM related to this module. [file=" + file.getAbsolutePath() + "]");
        }

        Model model = PomParser.parse(currentPOM);
        if (model == null){
            throw new MojoExecutionException("Error while processing  " + currentPOM.getAbsoluteFile());
        }

        // Creating a module representing the artifact generated by pom file
        MavenProject project = new MavenProject(model);

        String moduleAbsolutPath = moduleCreator.
                getAbsoluteArtifactResourcePath(new String[]{project.getArtifactId(), project.getVersion()});

        String dependencyAbsolutePath = serviceCreator.
                getAbsoluteArtifactResourcePath(new String[]{parameters.get("name"),parameters.get("namespace")});

        if (!artifactCreatorUtil.isArtifactExisting(moduleAbsolutPath)){
            moduleCreator.create(new String[]{project.getArtifactId(), project.getVersion(), currentPOM.getAbsolutePath()});
        }

        //Adding association for each service with the module
        addAssociation(moduleAbsolutPath, dependencyAbsolutePath, "ownedBy");
    }

    private File findPOMFileInCurrentDirectory(File[] files){
        File file = null;

        for (int index = 0; index < files.length; index++){
            file = files[index];
            if (file != null && file.isFile()){
                if (file.getName().equals("pom.xml")){
                    return file;
                }
            }
        }
        return null;
    }
    
    private File findNearestPOMFile(File file){
        while (true){

            File pomFile = pomMap.get(file.getParent());
            if (pomFile != null){
                return pomFile;
            }
            file = file.getParentFile();
        }
    }
}
