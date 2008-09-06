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

import static com.bicosyes.simpleGwt.SimpleGwtUtils.copyDirectory;
import static com.bicosyes.simpleGwt.SimpleGwtUtils.copyFile;
import static com.bicosyes.simpleGwt.SimpleGwtUtils.deleteResource;
import static com.bicosyes.simpleGwt.SimpleGwtUtils.getGWTjar;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
/**
 * Goal which touches a timestamp file.
 *
 * @goal gwt
 * @phase compile
 */
public class SimpleGwtCompile extends AbstractMojo
{

	/**
	 * Allows running the compiler in a separate process.
	 * If "false" it uses the built in compiler, while if "true" it will use an executable.
	 *
	 * @parameter default-value="false"
	 */
	private boolean fork = false;
	/**
	 * Indicates whether the build will continue even if there are compilation errors; defaults to true.
	 *
	 * @parameter expression="${maven.compiler.failOnError}" default-value="true"
	 */
	private boolean failOnError = true;

	/**
	 * Set to true to include debugging information in the compiled class files.
	 *
	 * @parameter expression="${maven.compiler.debug}" default-value="true"
	 */
	private boolean debug = true;

	/**
	 * Set to true to show messages about what the compiler is doing.
	 *
	 * @parameter expression="${maven.compiler.verbose}" default-value="false"
	 */
	private boolean verbose = false;

	/**
	 * Sets whether to show source locations where deprecated APIs are used.
	 *
	 * @parameter expression="${maven.compiler.showDeprecation}" default-value="false"
	 */
	private boolean showDeprecation = false;

	/**
	 * Set to true to optimize the compiled code using the compiler's optimization methods.
	 *
	 * @parameter expression="${maven.compiler.optimize}" default-value="false"
	 */
	private boolean optimize = false;

	/**
	 * Set to true to show compilation warnings.
	 *
	 * @parameter expression="${maven.compiler.showWarnings}" default-value="false"
	 */
	private boolean showWarnings = false;

	/**
	 * The source directories containing the sources to be compiled. For maven this is <tt>src/main/java</tt>
	 *
	 * @parameter expression="${project.compileSourceRoots}"
	 * @required
	 * @readonly
	 */
	private List compileSourceRoots = null;

	/**
	 * Project classpath.
	 *
	 * @parameter expression="${project.compileClasspathElements}"
	 * @required
	 * @readonly
	 */
	private List classpathElements = null;

	/**
	 * The directory where the webapp is built. For maven this is <tt>target/artifactId-version</tt>.
	 *
	 * @parameter expression="${project.build.directory}/${project.build.finalName}"
	 * @required
	 */
	private File webappDirectory = null;

	/**
	 * The directory for compiled classes. For maven this is <tt>target/classes</tt>.
	 *
	 * @parameter expression="${project.build.outputDirectory}"
	 * @required
	 * @readonly
	 */
	private File outputDirectory = null;

	/**
	 * Location of the build. For maven this is <tt>target</tt>.
	 *
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File buildDirectory = null;

	/**
	 * The list of GWT modules to compile.
	 *
	 * @parameter
	 * @required
	 */
	private List modules = null;

	/**
	 * move or not the content of the compiled modules
	 *
	 * @parameter default-value="true"
	 * @optional
	 */
	private boolean move = true;

	/**
	 * Where GWT is installed
	 *
	 * @parameter default-value="/opt/gwt"
	 * @required
	 */
	private String gwtHome = null;

	/**
	 * What OS is been used
	 *
	 * @parameter default-value="linux"
	 * @required
	 */
	private String os = null;

	public void execute() throws MojoExecutionException
	{
		// debug stuff
		if (getLog().isDebugEnabled())
		{
			getLog().debug("fork: " + fork);
			getLog().debug("failOnError: " + failOnError);
			getLog().debug("debug: " + debug);
			getLog().debug("verbose: " + verbose);
			getLog().debug("showDep: " + showDeprecation);
			getLog().debug("opt: " + optimize);
			getLog().debug("showWarn: " + showWarnings);
			getLog().debug("out: " + outputDirectory);
			getLog().debug("build: " + buildDirectory);
			getLog().debug("web: " + webappDirectory);
			getLog().debug("homeGWT: " + gwtHome);
			StringBuffer srcRoots = new StringBuffer("src: ");

			for (Object root: compileSourceRoots)
				srcRoots.append(" ").append(root);
			getLog().debug(srcRoots.toString());

			StringBuffer classpath = new StringBuffer("cp: ");
			for (Object path: classpathElements)
				classpath.append(" ").append(path);
			getLog().debug(classpath.toString());

			StringBuffer moduleInfo = new StringBuffer("mods: ");
			for (Object module: modules)
				moduleInfo.append(" ").append(module);
			getLog().debug(moduleInfo.toString());
		}

		if (!webappDirectory.exists())
		{
			webappDirectory.mkdirs();
			getLog().info("Created directory: " + webappDirectory.getAbsolutePath());
		}

		String activeModule = null;
		byte [] buffer = new byte [1024];
		String classpath;
		StringBuffer cp = new StringBuffer();

		for (Object path: compileSourceRoots)
			cp.append(File.pathSeparator).append(path);

		for (Object path: classpathElements)
			cp.append(File.pathSeparator).append(path);

		cp.append(File.pathSeparator).append(getGWTjar(gwtHome, os));

		classpath = cp.toString();

		getLog().debug("CP: "+ classpath);
		ProcessBuilder processBuilder = new ProcessBuilder(
				"java","-classpath",classpath,"com.google.gwt.dev.GWTCompiler","-out",webappDirectory.getAbsolutePath()
		);
		processBuilder.directory(webappDirectory);
		try {
			for (Object module: modules)
			{
				activeModule = module.toString();
				List<String> commands = processBuilder.command();
				while (commands.size() > 6)
					commands.remove(6);

				commands.add(activeModule);
				Process process = processBuilder.start();
				InputStream std = process.getInputStream();
				for (int read = std.read(buffer); read != -1; read = std.read(buffer))
					getLog().info(new String(buffer,0,read).trim());

				std = process.getErrorStream();
				for (int read = std.read(buffer); read != -1; read = std.read(buffer))
					getLog().error(new String(buffer,0,read).trim());

				if (process.waitFor() != 0)
					throw new MojoExecutionException("Error compiling module " + activeModule);

				if(move)
				{
					File moduleDir = new File(webappDirectory.getAbsolutePath()+File.separatorChar+activeModule);
					for(File f : moduleDir.listFiles())
					{
						getLog().debug("Copy to " + webappDirectory.getAbsolutePath()+File.separator+f.getName());
						if(f.isDirectory())
							copyDirectory(f, new File(webappDirectory.getAbsolutePath()+File.separator+f.getName()));
						else
							copyFile(f, new File(webappDirectory.getAbsolutePath()+File.separator+f.getName()));
					}
					deleteResource(moduleDir);
				}
			}
		}catch (Exception e){
			throw new MojoExecutionException("Error compiling module " + activeModule, e);
		}
	}

	interface Compiler
	{
		void compile();
	}
}
