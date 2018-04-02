package io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import core.Constantes;
import core.MyDate;
import core.MyDocument;
import utils.DataUtils;

public class LoadDataset {

	private static int SKIPLINES = 2;

	private static String dataName = "";
	private static String language = "english";
	private static String sourceType;
	private static String path = ".";
	private static String GTfield = "";
	private static String lucene_index_path = "";
	private static int GTfieldNumber = -1;
	private static String timeField;
	private static int timeFieldNumber = -1;
	private static String IDField;
	private static int IDFieldNumber = -1;
	private static String titleField;
	private static int titleFieldNumber = -1;
	private static String authorField;
	private static int authorFieldNumber = -1;
	private static String rawLinesField;
	private static int rawLinesFieldNumber = -1;
	private static String path_rawData = "";

	private static String[] textField;
	private static String[] textFieldConditionField;
	private static String[] textFieldConditionValue;
	private static ArrayList<Integer> textFieldNumbers;
	private static HashMap<Integer, Integer> textFieldConditionFieldNumbers;

	// save the initial texts per line (for the tweets)
	private static ArrayList<String> raw_text;

	// use as doc id when the field is not specified
	private static int id_count = 1;

	public static String getDataName() {
		return dataName;
	}

	public static void setDataName(String dataName) {
		LoadDataset.dataName = dataName;
	}

	public static String getGTfield() {
		return GTfield;
	}

	public static void setGTfield(String gTfield) {
		GTfield = gTfield;
	}

	/*
	 * public static String getTextField() { return textField; }
	 */

	public static void setTitleField(String s) {
		titleField = s;
	}

	public static void setAuthorField(String s) {
		authorField = s;
	}

	public static void setTextField(String textField) {
		String[] tt = textField.split(",");
		LoadDataset.textField = new String[tt.length];
		LoadDataset.textFieldConditionField = new String[tt.length];
		LoadDataset.textFieldConditionValue = new String[tt.length];
		for (int i = 0; i < tt.length; i++) {
			String[] cond = tt[i].trim().split("-");
			LoadDataset.textField[i] = cond[0];
			if (cond.length > 1) // there is a condition on the field
			{
				LoadDataset.textFieldConditionField[i] = cond[1];
				LoadDataset.textFieldConditionValue[i] = cond[2];
			}
		}
	}

	public static String getLanguage() {
		return language;
	}

	public static void setLanguage(String l) {
		language = l;
	}

	public static String getPath() {
		return path;
	}

	public static void setPath(String path) {
		LoadDataset.path = path;
	}

	public static String getSourceType() {
		return sourceType;
	}

	public static void setSourceType(String sourceType) {
		LoadDataset.sourceType = sourceType;
	}

	public static String getTimeField() {
		return timeField;
	}

	public static void setTimeField(String timeField) {
		LoadDataset.timeField = timeField;
	}

	public static String getIDField() {
		return IDField;
	}

	public static void setIDField(String iDField) {
		IDField = iDField;
	}

	public static String getRawLinesField() {
		return rawLinesField;
	}

	public static void setRawLinesField(String iDField) {
		rawLinesField = iDField;
	}

	public static void setRawData(String path) {
		path_rawData = path;
	}

	public static String getRawData() {
		return path_rawData;
	}

	public static boolean isRawData() {
		return !path_rawData.isEmpty();
	}

	public static void extractDocs() throws IOException {
		switch (sourceType) {
		case "csv":
			extractCSV2Docs();
			break;
		case "folder":
			extractFullFolder();
			break;
		case "folder.books":
			extractFolderBooks();
			break;
		/*
		 * case "xml": extractXML(); break;
		 */
		}
	}

	/**
	 * Récupère l'ensemble des documents d'une année dans un fichier csv bien formé
	 */

	public static void extractCSV2Docs() throws IOException {
		textFieldNumbers = new ArrayList<>();
		textFieldConditionFieldNumbers = new HashMap<>();
		if ((textField == null) || (textField.length == 0))
			throw new IOException("Unspecified text field");
		String line;
		BufferedReader buff = new BufferedReader(
				new FileReader(path + Constantes.separateur + Constantes.DATA_DIR + Constantes.separateur + dataName + ".csv"));
		String[] header = buff.readLine().split("\t");
		for (int i = 0; i < header.length; i++) {
			String title = header[i];
			if ((GTfield != null) && (title.equalsIgnoreCase(GTfield)))
				GTfieldNumber = i;
			if ((IDField != null) && (title.equalsIgnoreCase(IDField)))
				IDFieldNumber = i;
			if ((timeField != null) && (title.equalsIgnoreCase(timeField)))
				timeFieldNumber = i;
			if ((titleField != null) && (title.equalsIgnoreCase(titleField)))
				titleFieldNumber = i;
			if ((authorField != null) && (title.equalsIgnoreCase(authorField)))
				authorFieldNumber = i;
			if ((rawLinesField != null) && (title.equalsIgnoreCase(rawLinesField)))
				rawLinesFieldNumber = i;
			for (int j = 0; j < textField.length; j++) {
				if (title.equalsIgnoreCase(textField[j])) {
					textFieldNumbers.add(i);
				}
			}
			for (int j = 0; j < textFieldConditionField.length; j++) {
				if ((textFieldConditionField[j] != null) && (title.equalsIgnoreCase(textFieldConditionField[j]))) { // map
																													// the
																													// conditioned
																													// field
																													// number
																													// to
																													// the
																													// corresponding
																													// conditioning
																													// field
																													// number
					textFieldConditionFieldNumbers.put(textFieldNumbers.get(j), i);
				}
			}
		}
		/*
		 * int j=0; int max = 3; while ((j<max) && ((line = buff.readLine()) !=
		 * null))
		 */
		while ((line = buff.readLine()) != null) {
			/*
			 * j++; System.out.println(j);
			 */
			String[] tokens = line.split("\t");
			if (tokens.length > 1) {
				String id = "" + (id_count++);
				if (IDFieldNumber != -1)
					id = DataUtils.stripQuotes(tokens[IDFieldNumber]);
				String time = "not_specified";
				if (timeFieldNumber != -1)
					time = MyDate.addDate(DataUtils.stripQuotes(tokens[timeFieldNumber]));
				String gt = "";
				if (GTfieldNumber != -1)
					gt = DataUtils.stripQuotes(tokens[GTfieldNumber]);
				String title = "";
				if (titleFieldNumber != -1)
					title = DataUtils.stripQuotes(tokens[titleFieldNumber]);
				String author = "";
				if (authorFieldNumber != -1)
					author = DataUtils.stripQuotes(tokens[authorFieldNumber].trim());
				String text = "";
				for (int i = 0; i < textFieldNumbers.size(); i++) {
					Integer index = textFieldNumbers.get(i);
					String t = "";
					if (index >= tokens.length)
						t = "";
					else
						t = DataUtils.stripQuotes(tokens[index]);
					if (textFieldConditionFieldNumbers.get(index) != null) { // a
																				// test
																				// must
																				// be
																				// done
						String val = tokens[textFieldConditionFieldNumbers.get(index)];
						String val_2compare = textFieldConditionValue[i];
						if (val.equals(val_2compare))
							text += t + " ";
					} else // no test
						text += t + " ";
				}
				MyDocument ndoc = MyDocument.add(id, title, time, text, gt, author);
				// add the line numbers in the initial text file if they are
				// available
				if (rawLinesFieldNumber != -1) {
					try {
						String t_lines = DataUtils.stripQuotes(tokens[rawLinesFieldNumber]);
						ArrayList<String> lines = new ArrayList<>();
						String[] split = t_lines.split(",");
						for (String s : split) {
							String strim = s.trim();
							if (!strim.isEmpty())
								lines.add(strim);
						}
						ndoc.addRawLines(lines);
					} catch (NumberFormatException e) {
						System.out.println("Error: bad format for the line numbers in the initial file");
					}
				}
			}
		}
		buff.close();
	}

	public static void extractFullFolder() throws IOException {
		String foldpath = path + Constantes.separateur + Constantes.DATA_DIR + Constantes.separateur + dataName;
		File[] files = new File(foldpath).listFiles();
		if(files == null) throw new IOException();
		for (File d : files) { // for each class d
			if (d.isDirectory()) {
				// System.out.print("Read folder : " + d.getAbsolutePath() + " :
				// ");
				// int nb=0;
				File[] subfiles = d.listFiles();
				if(subfiles==null) throw new IOException();
				for (File f : subfiles) { // for each file f in this folder
					String text = "";
					String line;
					// System.out.println("Read " + f.getAbsolutePath());
					BufferedReader buff = new BufferedReader(new FileReader(f.getAbsolutePath()));
					for (int i = 0; i < SKIPLINES; i++)
						buff.readLine();
					while ((line = buff.readLine()) != null)
						text += line + " ";
					MyDocument.add(f.getName(), f.getName(), "notime", text, d.getName(), "");
					buff.close();
					// nb++;
				}
				// System.out.println(nb + " files");
			}
		}
	}

	public static void extractFolderBooks() throws IOException {
		String foldpath = path + Constantes.separateur + Constantes.DATA_DIR + Constantes.separateur + dataName;

		if(!Files.exists(Paths.get(foldpath))){
			throw new IOException("No files found at " + foldpath);
		}

		File file = new File(foldpath);

		for (File b : file.listFiles()) { // for each book b

			if (!b.isDirectory()) {
				BufferedReader buff = new BufferedReader(new FileReader(b.getAbsolutePath()));
				String line;
				int i = 1;
				while ((line = buff.readLine()) != null) {
					String id = b.getName() + "-" + i;
					MyDocument.add(id, id, MyDate.addDate(b.getName()), line, b.getName(), "");
					i++;
				}
				buff.close();
			}
		}
	}

	/*
	 * extract the whole corpus without any preprocessing (especially for the
	 * Twitter corpus)
	 */
	public static void extractFullCorpus(String path) {
		raw_text = new ArrayList<String>();
		long startTime = 0;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(path));
			String line;
			System.out.print("Load raw text file " + path + "... ");
			startTime = System.nanoTime();
			while ((line = reader.readLine()) != null)
				raw_text.add(line);
			reader.close();
			long estimatedTime = System.nanoTime() - startTime;
			System.out.println(TimeUnit.NANOSECONDS.toMillis(estimatedTime) + " ms");
		} catch (IOException e) {
			System.out.print("No raw text found");
		}
	}

	public static String getRawText(int i) {
		return raw_text.get(i);
	}

	public static void setListDates(String[] l) {
		for (String s : l)
			MyDate.addDate(s);
		MyDate.lock();
	}

	public static void setPathIndex(String p) {
		lucene_index_path = p + "/";
	}

	public static String getPathIndex() {
		return lucene_index_path;
	}

}