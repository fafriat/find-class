/*
 * #%L
 * find-class
 * %%
 * Copyright (C) 2013 Frank Afriat
 * %%
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
 * #L%
 */
package afriat.frank.tools;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FindClass {

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Needs 2 arguments <path> <classname> \n<path> : root folder for the search, path can be absolute or relative to current dir\n<classname> : package and/or classname to search for ex: com/bar/somepackage/SomeClass.class or SomeClass or SomeClass.class");
			System.exit(1);
		}
		String path = args[0];
		String fullClassName = args[1];
		
		System.out.println("path = [" + path + "]");
		System.out.println("fullClassName = [" + fullClassName + "]");
		
		File pathFile = new File(path);
		if (! pathFile.exists()) {
			System.out.println("the provided pathFile = [" + pathFile.getAbsolutePath() + "] must exist!");
			System.exit(2);
		}
		String packageName, className;
		int indexSlash = fullClassName.lastIndexOf('/');
		if (indexSlash < 0) {
			packageName = "";
			className = fullClassName; 
		}
		else {
			packageName = fullClassName.substring(0, indexSlash+1);
			className = fullClassName.substring(indexSlash+1);
		}
		System.out.println("packageName = [" + packageName + "]");
		System.out.println("className = [" + className + "]");
		search(pathFile, "", packageName, className, fullClassName);
	}
	

    private static void search(File start, final String currentPath, final String packageName, final String className, final String fullClassName) {
    	try {
    		final FileFilter filter = new FileFilter() {
    			public boolean accept(File file) {
    				//System.out.println("currentPath = [" + currentPath + "]");
    				return file.getName().endsWith(".jar") || 
    					   file.getName().endsWith(".zip") || 
    					   file.isDirectory() ||  
    					   ((packageName.length() == 0) && (file.getName().startsWith(fullClassName)) || 
    					   ((packageName.length() > 0) && (currentPath.endsWith(packageName)) && (file.getName().startsWith(className)) ));
    			}

    		};
    		for (File f : start.listFiles(filter)) {
    			if (f.isDirectory()) {
    				search(f, currentPath + f.getName() + "/", packageName, className, fullClassName);
    			} 
    			else if (f.getName().endsWith(".jar") || f.getName().endsWith(".zip")) {
    				searchJar(f, packageName, className, fullClassName);
    			}
    			else {
    				// f.getName().startsWith(classToFind)
    				if (packageName.length() == 0) {
    					System.out.println("Found class [" + fullClassName + "] inside folder [" + f.getPath() + "]" );
    				}
    				else {
    					if (currentPath.endsWith(packageName)) {
        					System.out.println("Found class [" + fullClassName + "] inside folder [" + f.getPath() + "]" );
    					}
    					else {
    						System.out.println("WARN : Found class [" + fullClassName + "] inside folder [" + f.getPath() + "] but in another package [" + currentPath + "]");
    					}
    				}
    			}
    		}
    	} catch (Exception e) {
    		System.err.println("Error at: " + start.getPath() + " " + e.getMessage());
    	}
    }

    @SuppressWarnings("unused")
	private static void searchJar(File f, String packageName, String className, String fullClassName) {
    	try {
    		//System.out.println("Searching: " + f.getPath());
    		ZipFile zip = new ZipFile(f);
    		//ZipEntry e = zip.getEntry(classToFind);
    		Enumeration<? extends ZipEntry> enumeration = zip.entries();
    		while(enumeration.hasMoreElements()) {
    			ZipEntry entry = enumeration.nextElement();
    			
    			if (packageName.length() > 0) {
    				
    			}
    			if ((packageName.length() > 0) && (entry.getName().startsWith(fullClassName))) {
    				System.out.println("Found class [" + entry.getName() + "] inside zip/jar [" + f.getPath() + "] : crc=" +entry.getCrc());    				
    			}
    			else if (packageName.length() == 0) {
    				String fullClassName2 = entry.getName();
    				String packageName2, className2;
    				int indexSlash = fullClassName2.lastIndexOf('/');
    				if (indexSlash < 0) {
    					packageName2 = "";
    					className2 = fullClassName2; 
    				}
    				else {
    					packageName2 = fullClassName2.substring(0, indexSlash+1);
    					className2 = fullClassName2.substring(indexSlash+1);
    				}
    				if (className2.startsWith(className)) {
						System.out.println("Found class [" + entry.getName() + "] inside zip/jar [" + f.getPath() + "] : crc=" +entry.getCrc());
    				}
    			}
    		}
    		zip.close();
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    }	

}
