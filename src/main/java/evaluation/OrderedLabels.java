package evaluation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import core.TermValuePair;

/**
 * 
 * This class maintains an ordered list of weighted terms (descending order).
 * The size is limited to the top MAX_TOP_TERMS terms.
 * 
 * @author julien
 *
 */

public class OrderedLabels
{
	
	public static final int MAX_TOP_TERMS = 30;
	
	private HashMap<String,TreeSet<TermValuePair>> labels;
	
	public OrderedLabels()
	{
		labels = new HashMap<>();
		//lda = false;
	}
		
	/* add a (weighted) term to the list of top-k terms. */
	 
	public void add(String label, String term, double weight)
	{
		TermValuePair tp = new TermValuePair(term, weight);
		TreeSet<TermValuePair> list = labels.get(label);
		if (list == null)
		{
			list = new TreeSet<>();
			list.add(tp);
			labels.put(label, list);
		}
		else
		{
			list.add(tp);
			if (list.size() > MAX_TOP_TERMS)
			{
				list.remove(list.descendingIterator().next());
			}
		}
	}
	
	/*public TreeSet<TermValuePair> getTopTerms(String key)
	{
		
		return labels.get(key);
	}*/

	public ArrayList<String> getTopTerms(String key, int topk)
	{
		ArrayList<String> nlist = new ArrayList<String>();
		TreeSet<TermValuePair> list = labels.get(key);
		if (list != null)
		{
			Iterator<TermValuePair> iter = list.iterator();
			while (iter.hasNext())
			{
				nlist.add(iter.next().getTerm());
			}
		}
		return nlist;
	}

	public String toString()
	{
		String s = "";
		for (String key : labels.keySet())
		{
			s += toString(key);
			s += "\n";
		}
		return s;
	}

	public String toString(String type)
	{
		String s = type + " : ";
		TreeSet<TermValuePair> list = labels.get(type);
		if (list != null)
		{
			Iterator<TermValuePair> iter = list.iterator();
			while (iter.hasNext())
			{
				TermValuePair tp = iter.next();
				String t = tp.getTerm();
				double w = tp.getValue(); 
				s += t + " (" + w + ") - ";
			}
			return s;
			
		}
		else
			return "nowhere to be found";
	}

}
