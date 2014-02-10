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

import governance.plugin.rxt.DependencyCreator;
import governance.plugin.rxt.ModuleCreator;

import java.io.File;
import java.util.List;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;


/**
 * Generates dependency tree by reading a pom.xml
 */
@Mojo( name = "gen", defaultPhase = LifecyclePhase.DEPLOY,  aggregator = true)
public class GovernanceMojo extends AbstractMojo
{
	//TODO : Get from a Configuration
	
	public static final String GREG_URL = "https://localhost:9443/services/";
	public static final String GREG_PASSWORD = "admin";
	public static final String GREG_USERNAME = "admin";
	//public static final String GREG_HOME = "/home/sajith/WSO2Products/wso2greg-4.6.0/";
    public static final String GREG_HOME = "/home/jayanga/WSO2/WSO2Software/wso2greg-4.6.0";
	
	//GReg Media types
	public static final String GREG_MODULE_MEDIA_TYPE = "application/vnd.wso2-module+xml";
	public static final String GREG_DEPENDENCY_MEDIA_TYPE = "application/vnd.wso2-dependency+xml";
	
	//GReg resource paths
	public static final String GREG_TRUNK_LOCATION = "/_system/governance";
	public static final String GREG_MODULE_RESOURCE_PATH = "/trunk/modules/";
	public static final String GREG_DEPENDENCY_RESOURCE_PATH = "/trunk/dependencies/";
	
	//End point references 
	private String moduleEndPointRef = GovernanceMojo.GREG_URL + "Module.ModuleHttpsSoap12Endpoint";
	private String dependecnyEndPointRef =  GovernanceMojo.GREG_URL + "Dependency.DependencyHttpsSoap12Endpoint";
	private String genericArtifactManagerEndPointRef = GovernanceMojo.GREG_URL + 
			"ManageGenericArtifactService.ManageGenericArtifactServiceHttpsSoap12Endpoint";
	private String relationServiceEndPointRef = GovernanceMojo.GREG_URL 
			+ "RelationAdminService.RelationAdminServiceHttpsSoap12Endpoint";
	
	private int pomFileCount = 0;
	private int directoryCount = 0;

	private ModuleCreator moduleCreator; 
	private DependencyCreator dependencyCreator;
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
    
    public GovernanceMojo() throws MojoExecutionException{
    	 moduleCreator = new ModuleCreator(getLog(), moduleEndPointRef, genericArtifactManagerEndPointRef);
    	 dependencyCreator = new DependencyCreator(getLog(), dependecnyEndPointRef, genericArtifactManagerEndPointRef);
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
                      + "\nModules ...........[Created:" + moduleCreator.getCreatedArtifactCount() 
                      + ", Existing:" + moduleCreator.getExistingArtifactCount() + "]"
                      + "\nDependencies ......[Created:" + dependencyCreator.getCreatedArtifactCount() 
                      + ", Existing:" + dependencyCreator.getExistingArtifactCount() + "]");
                      
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
       		 	getLog().debug("Empty directoy skipping.. :" + path);
    		}else{
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
    }
    
    public void process(File file) throws MojoExecutionException{
    	if (file.isFile() &&  file.getName().equals("pom.xml")){
    		pomFileCount++;
    		getLog().debug("Processing " + file.getAbsoluteFile());
    		
    		Model model = PomParser.parse(file);
    		if (model == null){
    			throw new MojoExecutionException("Error while processing  " + file.getAbsoluteFile());
    		}

    		// Creating a module representing the artifact generated by pom file
        	MavenProject project = new MavenProject(model);
        	moduleCreator.create(new String[]{project.getArtifactId(), project.getVersion(), file.getAbsolutePath()});
        	
        	String moduleAbsolutPath = moduleCreator.
        			getAbsoluteArtifactResourcePath(new String[]{project.getArtifactId(), project.getVersion()});
        	
        	// Creating dependency artifacts to represent dependencies of the module
        	List<Dependency> dependencies = project.getDependencies();
        	for (Dependency dependency : dependencies){
        		dependencyCreator.create(new String[]{dependency.getGroupId(), dependency.getArtifactId(), 
        		                                      dependency.getVersion()});
        		
        		String dependencyAbsolutePath = dependencyCreator.
            			getAbsoluteArtifactResourcePath(new String[]{dependency.getGroupId(), dependency.getArtifactId()});
        		
        		// Adding association for each dependency with the module
        		addAssociation(moduleAbsolutPath, dependencyAbsolutePath, "depends");
        	}
    	}
    }  
}
