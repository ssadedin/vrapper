package net.sourceforge.vrapper.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Basic support for parsing CTags format. Only supports search string as address.
 */
public class CTags {
	
	public Map<String, CTag> index = new HashMap<String, CTag>();
	
	public static class CTag {
		public String name;
		public String file;
		public String address;
		
	    public CTag(String name, String file, String address) {
	        this.name = name;
	        this.file = file;
	        this.address = address;
	    }		
	    
	    @Override
	    public String toString() {
	    	return "CTag(" + name + "," + file + ", address: " + address + ")";
	    }

	    /**
	     * Return a version of the address as a searchable string. Meaning:
	     * 
	     * <li>regex special characters have been escaped
	     * <li>surrounding non-search characters are stripped
	     */
		public String getCleanAddress() {
			return address.substring(1, address.length()-3)
					.replace("{","\\{")
					.replace("(","\\(")
					.replace(")","\\)");
		}
	}
	
	public static CTags parse(File source) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(source));
		try {
			CTags result = new CTags();
			
			String line = null;
			while((line = reader.readLine()) != null) {
			    if (line.startsWith("!")) {
			        continue;
			    }
			    
			    String[] parts = line.split("\t");
			    if (parts.length >= 3) {
			        result.index.put(parts[0], new CTag(parts[0], parts[1], parts[2]));
			    }
			}            
			return result;
		} finally {
			reader.close();
		}
	}
}
