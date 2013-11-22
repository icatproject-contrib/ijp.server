package org.icatproject.ijp.server;

import java.io.File;
import java.io.FilenameFilter;

public class FileExtensionFilter implements FilenameFilter {

	String ext;
	
	public FileExtensionFilter(String ext) {
		this.ext = ext;
	}
	
	@Override
	public boolean accept(File dir, String name) {
		return name.endsWith("." + ext);
	}

}
