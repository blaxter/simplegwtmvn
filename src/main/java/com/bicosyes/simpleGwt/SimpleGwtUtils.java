package com.bicosyes.simpleGwt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SimpleGwtUtils
{
	protected static boolean deleteResource(File file)
	{
		return deleteResource(file, false);
	}
		
	protected static boolean deleteResource(File file, boolean exclude)
	{          
		if(file.isDirectory())
		{
			if(exclude)
				//TODO: crear una lista de excludes
				if(file.getName().equals("common")) return true;			
			for(File f : file.listFiles())
				if(!deleteResource(f, exclude))
					return false;
		}
		return file.delete();        
	}
   
	protected static void copyDirectory(File sourceDir, File destDir) throws IOException
	{
		if(!destDir.exists())
			destDir.mkdir();
	  
		File[] children = sourceDir.listFiles();
		for(File sourceChild : children)
		{
			String name = sourceChild.getName();
			File destChild = new File(destDir, name);
			if(sourceChild.isDirectory())
				copyDirectory(sourceChild, destChild);	    
			else
				copyFile(sourceChild, destChild);	     
		}
	}
	  
	protected static void copyFile(File source, File dest) throws IOException
	{
		if(!dest.exists())
			dest.createNewFile();
	    
		InputStream in = null;
		OutputStream out = null;
		try{
			in = new FileInputStream(source);
			out = new FileOutputStream(dest);
			byte[] buf = new byte[1024];
			int len;
			while((len = in.read(buf)) > 0)
				out.write(buf, 0, len);	    
		}finally{
			in.close();
			out.close();
		}
	}
   
	protected static String getGWTjar(String homeGWT, String OS)
	{
		final String jarName = "gwt-dev-";
		if(homeGWT.charAt(homeGWT.length() - 1) != File.separatorChar)
			homeGWT += File.separator;
		return homeGWT + jarName + OS.toLowerCase() + ".jar";
	}
}
