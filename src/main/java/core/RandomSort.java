package core;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class RandomSort
{
	
	private ArrayList<TermValuePair> list;
	
	public RandomSort()
	{
		list = new ArrayList<TermValuePair>();
	}
	
	// random adding
	public void add(String n, int v)
	{
		int index = (int)(Math.random()*list.size());
		list.add(index, new TermValuePair(n, v));
	}
	
	public void toFile(String filename) throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
		for (TermValuePair t : list)
			writer.write(t.getTerm() + ";" + t.getValue() + "\n");
		writer.flush();
		writer.close();
	}	
	
	public int size()
	{
		return list.size();
	}
}
