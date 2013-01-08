package eu.monnetproject.stl;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FileReader {

	private static final String EXCLUDE = ".*\\.svn.*";

	/**
	 * Recursively walk a directory tree and return a List of all
	 * Files found; the List is sorted using File.compareTo().
	 *
	 * @param aStartingDir is a valid directory, which can be read.
	 */
	static public List<File> getFileListing(File aStartingDir, String fileExtensionFilter) throws FileNotFoundException {
		validateDirectory(aStartingDir);
		List<File> result = getFileListingNoSort(aStartingDir, fileExtensionFilter);
		Collections.sort(result);
		return result;
	}

	// PRIVATE //
	static private List<File> getFileListingNoSort(File aStartingDir,String fileExtensionFilter) throws FileNotFoundException {
		List<File> result = new ArrayList<File>();
		File[] filesAndDirs = aStartingDir.listFiles();
		List<File> filesDirs = Arrays.asList(filesAndDirs);
		//System.out.println(aStartingDir.getPath());
		for(File file : filesDirs) {
			if (file.getName().endsWith(fileExtensionFilter)) {
				if (!file.getPath().matches(EXCLUDE)) // do not add exclude pattern (eg svn)
					result.add(file); //always add, even if directory
			} else if ( !file.isFile() ) {
				//must be a directory and file extension must match
				//recursive call!
				List<File> deeperList = getFileListingNoSort(file,fileExtensionFilter);
				result.addAll(deeperList);
			}
		}
		return result;
	}

	/**
	 * Directory is valid if it exists, does not represent a file, and can be read.
	 */
	static private void validateDirectory (
			File aDirectory
	) throws FileNotFoundException {
		if (aDirectory == null) {
			throw new IllegalArgumentException("Directory should not be null.");
		}
		if (!aDirectory.exists()) {
			throw new FileNotFoundException("Directory does not exist: " + aDirectory);
		}
		if (!aDirectory.isDirectory()) {
			throw new IllegalArgumentException("Is not a directory: " + aDirectory);
		}
		if (!aDirectory.canRead()) {
			throw new IllegalArgumentException("Directory cannot be read: " + aDirectory);
		}
	}
} 
