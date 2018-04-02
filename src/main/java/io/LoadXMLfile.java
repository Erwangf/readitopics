package io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import core.CleanWord;
import core.StopWords;
import utils.DataUtils;


public class LoadXMLfile {

	private static boolean testTitle(String s) {
		String[] split1 = s.split("\"");
		if (split1.length < 6) {
			System.out.println("ERROR : " + s);
		}

		String title = split1[5];

		if(DataUtils.isInteger(title)){
			System.out.println(title + " deleted");
			return false;
		}
		else{
			return true;
		}
	}

	// TODO Documentation
    public static void extractXML(String filename) throws IOException {

    	StopWords.loadTextFile("Biotex/stopWords/Stop-words-english.txt");

    	CleanWord.setStrategy("tolower,punct,stopwords,min=2");

		BufferedReader buff = new BufferedReader(new FileReader(filename));
		BufferedWriter bw = new BufferedWriter(new FileWriter(filename + "_clean.txt"));
		String line;
		StringBuilder out = new StringBuilder();
		boolean first = false;
		boolean valid = true;

		while ((line = buff.readLine()) != null) {
			if (line.startsWith("<doc id")) { // new doc
				valid = testTitle(line);
				out.setLength(0);
				first = true;
			}
			if (line.startsWith("</doc>")) {
				if (valid) {
					bw.write(out.toString());
					bw.newLine();
				}
			} else {
				String cleanedLine = DataUtils.tokenizeAndClean(line.replaceAll("<.+>", " "));
				if (!cleanedLine.isEmpty()) {
					if (!first) {
						out.append(" ");
					} else {
						first = false;
					}

					out.append(cleanedLine);
				}
			}
		}
		buff.close();
		bw.close();
	}

}
