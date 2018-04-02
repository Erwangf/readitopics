package gui;
import java.util.ArrayList;

public class History 
{
	
	private static ArrayList<String> history = new ArrayList<String>(20);
	private static int index = -1;
	
	public static void add(String s)
	{
		history.add(0, s);
		if (history.size() > 20) history.remove(20);
	}
	
	public static void up()
	{
		if (index<(history.size()-1)) index++;
	}
	
	public static void down()
	{
		if (index > -1) index--;
	}

	public static void restart()
	{
		index = -1;
	}
	
	public static String getText()
	{
		if ((index > -1) && (index < history.size()))
			return history.get(index);
		else
			return "";
	}
	
}
