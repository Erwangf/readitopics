package core;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

public class StopWords
{

	private static HashSet<String> stop_list = new HashSet<>();
	
	private static void addStopWord(String s)
	{
		stop_list.add(s);
	}
	
	public static void loadTextFile(String filename) throws IOException
	{
        BufferedReader buff = new BufferedReader(new FileReader(filename));
        String line;
        while ((line = buff.readLine()) != null)
        {
            String w = line.trim();
            if (!w.isEmpty())
            	addStopWord(w);
        }
        buff.close();
	}
	
	public static boolean isStopWords(String s)
	{
		return stop_list.contains(s);
	}
	
}
