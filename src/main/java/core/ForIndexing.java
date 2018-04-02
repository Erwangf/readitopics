package core;

import java.util.HashMap;
import java.util.TreeSet;

/**
 * Cette classe implémente un terme du vocabulaire, bloc de base de l'indexation.
 * 
 * @author julien
 *
 */

public class ForIndexing implements Comparable<ForIndexing> {
	
	private String term;
	private TreeSet<MyDocument> list;
	private boolean activated;
	private int length;
	
	private HashMap<String,Integer> tf_byCategory;
	private HashMap<String,Integer> docs_byCategory;
	
	public ForIndexing(String term)
	{
		this.term = term;
		String[] cut = term.split(" ");
		length = cut.length;
		activated = false;
		list = new TreeSet<>();
		tf_byCategory = new HashMap<>();
		docs_byCategory = new HashMap<>();
	}
	
	public String getTerm()
	{
		return term;
	}
		
	public TreeSet<MyDocument> getDocs()
	{
		return list;
	}
	
	public int getLength()
	{
		return length;
	}

	/**
	 * Return the intersection of the current index with another index
	 * @param f another index
	 * @return the intersection of the current index with f
	 */
	public ForIndexing intersect(ForIndexing f)
	{
		ForIndexing newf = new ForIndexing(this.getTerm() + "+" + f.getTerm());
		for (MyDocument d : this.list) {
			if (f.list.contains(d)) {
				newf.add(d);
				newf.addNBDocs("all", 1);
			}
		}
		return newf;
	}

	public int compareTo(ForIndexing o) {
		return term.compareTo(o.getTerm());
		//return 0;
	}

	/**
	 * Ajoute un document à l'index
	 * @param d un document
	 */
	public void add(MyDocument d)
	{
		list.add(d);
	}

	/**
	 * Active ou désactive l'index
	 * @param b booléen : true si index activé, false sinon
	 */
	public void setActivated(boolean b)
	{
		activated = b;
	}

	public boolean isActivated()
	{
		return activated;
	}
	
	public int getTF(String k) {
		Integer i = tf_byCategory.get(k);
		if (i != null)
			return i;
		else
			return 0;
	}
	
	public void addTF(String k, int val) {
		tf_byCategory.merge(k, val, (a, b) -> a + b);
	}
	
	public HashMap<String,Integer> getAllTF()
	{
		return tf_byCategory;
	}

	public int getNBDocs(String k)
	{
		Integer i = docs_byCategory.get(k);
		if (i != null)
			return i;
		else
			return 0;
	}
	
	public void addNBDocs(String k, int val)
	{
		docs_byCategory.merge(k, val, (a, b) -> a + b);

	}
	
	public HashMap<String,Integer> getAllNBDocs()
	{
		return docs_byCategory;
	}

}
