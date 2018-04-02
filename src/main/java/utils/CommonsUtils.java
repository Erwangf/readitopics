package utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import core.CleanWord;
import core.TermValuePair;
import org.apache.commons.io.FileUtils;

import core.Constantes;

public class CommonsUtils {

	// TODO Documentation
	public static String loadFromFolder(String folder) throws IOException {
		StringBuilder learningData = new StringBuilder();
		File[] files = new File(folder).listFiles();
		if(files==null){
			System.out.println("Folder : "+folder+"is empty");
			return learningData.toString();
		}
		for (File file : files) {
			if (file.isFile()) {
				learningData
						.append(readFile(folder + Constantes.separateur + file.getName(), Charset.defaultCharset()));
			}
		}
		return learningData.toString();
	}

	// TODO Documentation
	public static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}

	/**
	 * Merge files in a folder into a subfolder named "merged". Clean up if
	 * necessary. Returns the content of the file containing the merged dataset.
	 * @param folder folder's path
	 * @return a file containing the merged dataset
	 */
	public static File mergeDataset(String folder) throws IOException {

		// clean up merged subfolder
		String mergedOutput = folder + Constantes.separateur + "merged" + Constantes.separateur;
		File directory = new File(mergedOutput);
		if (!directory.exists()) {
			directory.mkdir();
		} else {
			deleteFolderContent(directory);
		}

		// Load the merged content into memory
		String content = loadFromFolder(folder);

		File merged = new File(mergedOutput + Constantes.separateur + "merged.txt");
		FileUtils.writeStringToFile(new File(mergedOutput + Constantes.separateur + "merged.txt"), content);

		return merged;
	}

	// TODO Documentation
	public static void deleteFolderContent(File folder) {
		File[] files = folder.listFiles();
		if (files != null) { // some JVMs return null for empty dirs
			for (File f : files) {
				if (f.isDirectory()) {
					deleteFolderContent(f);
					f.delete();
				} else {
					f.delete();
				}
			}
		}
	}

	// TODO Documentation
	public static List<File> getContentOfFolder(String folderPath, boolean printDirectories, boolean printHiddenFiles){
		List<File> result = new ArrayList<>();
		File file = new File(folderPath);
		File[] files = file.listFiles();
		if(files != null){
			for (File f : files)
			{ // for each configuration file
				if ((!f.isDirectory() || printDirectories) && (!f.isHidden() || printHiddenFiles)) {
					result.add(f);
				}
			}
		}
		return result;
	}

}
