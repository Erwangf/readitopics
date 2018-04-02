package core;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;

public class CleanWord {

	private static boolean tolower = false;
	private static boolean punctuation = false;
	
	private static boolean stopwords = false;
	private static boolean noaccent = false;

	private static int minChar = 1;
	
	public static void setStrategy(String strategy) {
		String[] split = strategy.split(",");
		for (String t : split)
		{
			String ch = t.toLowerCase().trim();
			switch(ch)
			{
			case "tolower":
				tolower = true;
				break;
			case "punct":
				punctuation = true;
				break;
			case "stopwords":
				stopwords = true;
				break;
			case "noaccent":
				noaccent = true;
				break;
			}
			if (ch.startsWith("min=")) {
				String[] min = ch.split("min=");
				if (min.length>1) {
					int val = 1;
					try {
						val = Integer.parseInt(min[1]);
					}
					catch (NumberFormatException e) {
						System.out.println("Wrong number for the min. length of words");
					}
					finally {
						setMinChar(val);
					}
				}
			}
		}
	}
	
	public static ArrayList<String> tokenize(String s){
		ArrayList<String> list = new ArrayList<>();
		if (!punctuation) {
			list.addAll(Arrays.asList(s.split(" ")));
			return list;
		}
		String s_aug = s + " ";
		int i = 0;
		int len = 0;
        while (i < s_aug.length()) {
        	char ch = s_aug.charAt(i);
        	if (Character.isDigit(ch) || Character.isLetter(ch)) {
				len++;
			}
        	else {
        		if (len > 0) {
        			list.add(s_aug.substring(i-len, i));
        			len = 0;
        		}
        	}
            i++;
        }

		return list;
	}
	
	public static void setMinChar(int m)
	{
		minChar = m;
	}

	// TODO
	public static String clean(String token)
	{
		if (tolower)
			token = token.toLowerCase();

		if (token.equals(""))
			return "";
		if (stopwords && StopWords.isStopWords(token))
			return "";
		if (token.length() < minChar)
			return "";
		if (noaccent)
			token = stripAccents(token);
		return token; 
	}
    	
    // TODO Documentation
	public static String TCleaner(String str){
		str = str.replace("\"","''");
		str = str.replace("}",")");
		str = str.replace("{","(");
		str = str.replace("[","(");
		str = str.replace("]",")");
		str = str.replace(";",". ");
		str = str.replace("\\"," ");
		return str;
	}

	// TODO Documentation
	/* replace all the accents by the corresponded uninflected char */
	public static String stripAccents(String s) {
	    s = Normalizer.normalize(s, Normalizer.Form.NFD);
	    s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
	    return s;
	}
}
