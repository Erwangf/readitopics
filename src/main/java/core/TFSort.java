package core;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * 
 * Cette classe permet de trier des paires (terme, valeur) et de les enregistrer dans un fichier.
 * Pour le moment, elle est princpalement utilisée pour générer le vocabulaire pour "TF". 
 * 
 * @author julien
 *
 */

public class TFSort
{
	
	private TreeSet<TermValuePair> list;
	
	// if you want keep the top-max only
	private int max;
	
	public TFSort()
	{
		this.list = new TreeSet<TermValuePair>();
		this.max = -1;
	}
	
	public TFSort(int max)
	{
		this.list = new TreeSet<TermValuePair>();
		this.max = max;
	}	
	
	public void add(String n, int v)
	{
		list.add(new TermValuePair(n, v));
		if ((max != -1) && (list.size() > max))
			list.pollLast();
	}

	public void add(String n, double v)
	{
		list.add(new TermValuePair(n, v));
		if ((max != -1) && (list.size() > max))
			list.pollLast();		
	}
	
	public boolean contains(TermValuePair t)
	{
		return list.contains(t);
	}
	
	public TreeSet<TermValuePair> getList()
	{
		return list;
	}

	public String toString()
	{
		String s = "";
		int i = 0;
		Iterator<TermValuePair> it = list.iterator();
		while ((it.hasNext()) && (i<10))
		{
			TermValuePair t = it.next();
			//sum += d.getTF(term);
			s += t.getTerm() + "(" + t.getValue() + ")-";
			i++;
		}
		return s;
	}

	public void toFile(String filename) throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
		Iterator<TermValuePair> it = list.iterator();
		while (it.hasNext())
		{
			TermValuePair t = it.next();
			//if (t.getValue())
			writer.write(t.getTerm() + ";" + t.getValue() + "\n");
		}
		writer.flush();
		writer.close();
	}	
	
	public int size()
	{
		return list.size();
	}
}
