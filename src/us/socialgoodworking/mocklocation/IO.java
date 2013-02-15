/*
 * Copyright (C) 2012 Paul Corriveau
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

package us.socialgoodworking.mocklocation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import android.content.Context;

import us.socialgoodworking.utility.Logging;

/**
 * Helper class. 
 *
 */
public class IO {
	static final String TAG = "IO";
	
    /**
     * 
     * @param context		Application context
     * @param directory		The name of the directory
     * @param fName			The name of the file
     * @param what			The string to save
     * @param append		True to append string to end of file, false to overwrite
     */
	public static void writeFile(Context context, String directory, String fName, String what, boolean append) {
    	
    	// This creates the directory if it doesn't exist...
    	File dir = context.getDir(directory, Context.MODE_PRIVATE);
    	if ( dir == null) {
    		Logging.debug(TAG, "writeFile:", "Error with " + directory );
    		return;
    	}
    	
    	try { 
	    	File rf = new File(dir, fName);
	    	Logging.debug(TAG, "writeFile - routeFile = ", rf.getName() + ", " + rf.getAbsolutePath());
	        FileOutputStream fOut = new FileOutputStream(rf, append);
	        OutputStreamWriter writer = new OutputStreamWriter(fOut, "UTF-8");
	        BufferedWriter fbw = new BufferedWriter(writer);
	        fbw.write(what);
	        fbw.newLine();
	        fbw.flush();
	        fbw.close();
	        writer.close();
	        fOut.close();
	    }
	    
	    catch (IOException e) {
	    	Logging.debug(TAG, "writeFile:", e.getMessage());
	    	e.printStackTrace();
	    	throw new RuntimeException(e);
	    }
    }

    /**
     * 
     * @param context		Application context
     * @param directory		The name of the directory
     * @param fName			The name of the file
     * @return				The saved routes if they exist, or an empty string otherwise. 
     */
    public static String readFile(Context context, String directory, String fName) {
    	
    	BufferedReader in = null;
    	File dir = context.getDir(directory, Context.MODE_PRIVATE);
    	if ( dir == null) {
    		Logging.debug(TAG, "readFile:", "Context.getDir() failed");
    		return new String();
    	}
    	
    	File rf = new File(dir, fName);
    	if (!rf.exists()) {
    		Logging.debug(TAG, "readFile", rf.getAbsolutePath() + "/" + rf.getName() + " does NOT exist");
    		return new String();
    	}
  
    	StringBuilder sb;
    	
    	try {
    		InputStream inStream = new FileInputStream(rf);
      	
    		InputStreamReader inputreader = new InputStreamReader(inStream, "UTF-8");
    		in = new BufferedReader(inputreader);        	
      	
    		String line;
    		sb = new StringBuilder();

    		line = in.readLine();

    		while (line != null) {
    			sb.append(line);
    			line = in.readLine();
    		}
          
    		Logging.debug(TAG, "readFile - routeFile contents = ", sb.toString());

    		return sb.toString();
    	} 
  
    	catch (IOException e) {
    		Logging.debug(TAG, "readFile:", e.getMessage());
    	}

    	finally {
    		if (in != null) {
    			try {
    				in.close();
    			} 
    			catch (IOException e) {
    				Logging.debug(TAG, "readFile:", e.getMessage());
    			}
    		}
    	}
    	
        return new String();
    }
}
