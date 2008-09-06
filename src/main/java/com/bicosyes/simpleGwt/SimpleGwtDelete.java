/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bicosyes.simpleGwt;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.util.List;
import static com.bicosyes.simpleGwt.SimpleGwtUtils.deleteResource;;
/**
 * 
 *
 * @goal gwtClean
 * @phase process-classes
 */
public class SimpleGwtDelete extends AbstractMojo
{
	/**
	 * The directory for compiled classes. For maven this is <tt>target/classes</tt>.
	 *
	 * @parameter expression="${project.build.outputDirectory}"
	 * @required
	 * @readonly
	 */
	private File outputDirectory = null;

   /**
    * The list of GWT modules to compile.
    *
    * @parameter
    * @required
    */
   private List modules = null;
   
	/**
	 * The list of files to be removed
	 *
	 * @parameter
	 * @optional
	 */
	private List deletes = null;
	
	/**
	 * Delete or not the gwt stuff
	 *
	 * @parameter default-value="true"
	 * @required
	 */
	private boolean delete = true;
	
	public void execute() throws MojoExecutionException
	{
		String activeModule = "";
	   
		try{
			for (Object module: modules)
			{				
				activeModule = (String)module;
				if(delete || deletes != null)
				{
					getLog().info("Trying to delete GWT stuff like you suggest, master");
					deleteGWTstuff(outputDirectory,activeModule, deletes);
				}
			}
		}catch (Exception e){
			throw new MojoExecutionException("Error compiling module " + activeModule, e);
		}
	}
	
	@SuppressWarnings("unused")
	private void print(File file)
	{
		if(file.isDirectory())
			for(File f : file.listFiles())
				print(f);
		else
			System.out.println(file.getAbsolutePath());
	}

	private void deleteGWTstuff(File classesDirectory, String module, List deletes) throws MojoExecutionException
	{        
		// module is something like com.bicosyes.someApp     
		String [] stuffToDelete;     
		if(deletes == null)
		{
			String GWTXML = module.substring(module.lastIndexOf(".") + 1) + ".gwt.xml"; // someApp.gwt.xml
			stuffToDelete = new String [] {"public", "client", GWTXML};       
		}
		else
		{
			stuffToDelete = new String [deletes.size()];
			for(int i = 0; i < deletes.size(); i++)
				stuffToDelete[i] = (String)deletes.get(i);
		}
		String stuffToBeDeleted = "";
		for(String s : stuffToDelete)
			stuffToBeDeleted += s + " ";
		getLog().debug("The following file will be deleted: " + stuffToBeDeleted);     
		module = module.substring(0, module.lastIndexOf("."));
		String path = "";
		int levels = 0;
		while(module.indexOf(".") != -1)
		{
			path += module.substring(0, module.indexOf(".")) + File.separator;
			module = module.substring(module.indexOf(".") + 1, module.length());
			levels++;
		}
		path += module;  // path would be com/bicosyes
		levels++;
		File gwtModuleDir = new File(classesDirectory.getAbsolutePath() + File.separator + path);
		if(!gwtModuleDir.exists() || !gwtModuleDir.isDirectory())
			throw new MojoExecutionException("Error trying to remove the gwt stuff."+
					"The following directory should exists!: " +
					gwtModuleDir.getAbsolutePath()+ ".  WTF just happened?"
			);
		else
			getLog().debug("Ready for starting since " + gwtModuleDir.getAbsolutePath());
		// go to delete!     
		for(String toDelete : stuffToDelete)
		{
			File crap = new File(gwtModuleDir.getAbsolutePath() + File.separator + toDelete);
			getLog().debug("Deleting "+ crap.getAbsolutePath());
			if(crap.exists())
				deleteResource(crap, true);					         
		}
		
		while(gwtModuleDir.list().length == 0 && levels > 0)
		{ // deleteing dir's :-/ so dangerous...
			File aux = gwtModuleDir.getParentFile();
			getLog().debug("Deleting "+ gwtModuleDir.getAbsolutePath());
			if(!deleteResource(gwtModuleDir))
				throw new MojoExecutionException("Error while trying to remove " + gwtModuleDir.getAbsolutePath());
			gwtModuleDir = aux;
			levels--;
		}
	}	
}
