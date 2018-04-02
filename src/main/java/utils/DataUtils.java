package utils;

import core.CleanWord;
import core.Constantes;
import io.LoadDataset;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;

public class DataUtils {


	/**
	 * Stratregy to split tokens. E.g. "tolower,punct,stopwords,min=2"
	 */
	private String strategy;

	public DataUtils(String strategy) {
		this.strategy = strategy;
		CleanWord.setStrategy(strategy);
	}

	/**
	 * Fonction permettant d'extraire les phrases d'un texte
	 * @author Julien Velcin
	 * @param data le texte dont on veut extraire les phrases
	 * @return List de phrases
	 */	
	public static List<String> extractSentences(String data) {
		String sentenceModelPath = LoadDataset.getPath() + Constantes.separateur + "en-sent.bin";
		return extractSentences(sentenceModelPath,data);
	}

	/**
	 * charge un SentenceModel depuis le chemin <b>path</b> puis extrait les phrases du <b>text</b>
	 * @param sentenceModelPath le chemin vers le fichier à charger
	 * @param text le texte dont on veut extraire les phrases
	 * @return une liste de phrases
	 */
	public static List<String> extractSentences(String sentenceModelPath, String text) {
		 // sentence detector model from openNLP
		List<String> sentences = null;
		try {
			InputStream inputStream = new FileInputStream(sentenceModelPath);
			SentenceModel model = new SentenceModel(inputStream);
			SentenceDetectorME sentenceDetector = new SentenceDetectorME(model);
			String[] result = sentenceDetector.sentDetect(text);
			sentences = Arrays.asList(result);
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sentences;
	}

	// TODO Documentation
	public static String tokenizeAndClean(String s) {

		ArrayList<String> tokens = CleanWord.tokenize(s);
		boolean passe = false;
		StringBuilder stringBuilder = new StringBuilder();
		for (String word : tokens) {
			String w = CleanWord.clean(word);
			if (!w.isEmpty()) {
				if (passe)
					stringBuilder.append(" ");
				else
					passe = true;
				stringBuilder.append(w);
			}
		}
		return stringBuilder.toString();
	}

	// TODO peut-être déplacer cette fonction dans une classe moins généraliste ?
	/**
	 * Vérifie qu'une chaine de caractère correspond à un nombre entier.
	 * @param s une chaîne de caractères
	 * @return true si la chaine de caractères est un nombre entier
	 */
	public static boolean isInteger(String s) {
		if(s == null || s.isEmpty()) return false;
		for(int i = 0; i < s.length(); i++) {
			if(i == 0 && s.charAt(i) == '-') {
				if(s.length() == 1) return false;
				else continue;
			}
			if(Character.digit(s.charAt(i),10) < 0) return false;
		}
		return true;
	}

	/**
	 * Remplace les quotes ( " ) d'un texte par une version protégée ( \" )
	 * @param s un texte
	 * @return un texte dont les quptes ont été protégées
	 */
	public String protectQuotes(String s) {
		return s.replaceAll("\"", Matcher.quoteReplacement("\\\""));
	}

	/**
	 * Enlève les quotes ( " ) d'un texte
	 * @param s un texte
	 * @return un texte sans quotes
	 */
	public static String stripQuotes(String s)
	{
		return s.replaceAll("^(\")+|(\"$)+", "");
	}

	/**
	 * Rempalce les backslash d'un texte par des doubles backslash
	 * @param s un texte
	 * @return un texte dont les backslash ont été doublés
	 */
	public static String doubleBackslash(String s) {
		return s.replace("\\", "\\\\");
	}



	// general methods

	public String getStrategy() {
		return strategy;
	}

	public void setStrategy(String strategy) {
		this.strategy = strategy;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((strategy == null) ? 0 : strategy.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DataUtils other = (DataUtils) obj;
		if (strategy == null) {
			if (other.strategy != null)
				return false;
		} else if (!strategy.equals(other.strategy))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DataUtils [strategy=" + strategy + "]";
	}

}
