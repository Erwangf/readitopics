package core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import topicmodeling.LDATopicModel;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 *  Cette classe permet de construire puis gérer l'index inversé.
 * 
 * @author julien
 *
 */

public class InvertedIndex
{

	private TreeMap<String,ForIndexing> list;
	private int docs;
	
	public InvertedIndex()
	{
		list = new TreeMap<>();
		docs = 0;
	}
	
	public int size()
	{
		return list.size();
	}
	
	public TreeMap<String,ForIndexing> getList()
	{
		return list;
	}
	
	private ForIndexing add(String term)
	{
		ForIndexing in = new ForIndexing(term);
		list.put(term, in);
		return in;
	}
	
	public boolean print = false;
	
	private int nn = 0;
	
	/*
	 *  Look up for the term in the treemap and return the corresponding ForIndexing object.
	 * If the term doesn't exit, it is automatically added to the treemap and the new index is returned.
	 */
	public ForIndexing look_up(String term)
	{
		ForIndexing f = list.get(term);
		if (f==null)
		{
			//System.out.print(term+"-");
			nn++;
			if (print)
				System.out.print(term+"//");
			f = add(term);
		}
		return f;	
	}
	
	public void add(MyDocument d)
	{
		ArrayList<String> tokens = CleanWord.tokenize(d.getText());
		/*for (int i = 0; i < tokens.length; i++)*/
		for (String token : tokens)
		{
			String t = CleanWord.clean(token);
			if ((!t.equals("")) && (!t.equals(" "))) 
			{
				ForIndexing f = look_up(t);
				f.add(d);
				d.add(f);
			}
		}
		docs++;
	}
	
	public ForIndexing get(String term)
	{
		ForIndexing f = list.get(term);
		return f;
	}
	
	public ArrayList<ForIndexing> getFeatures(ArrayList<String> words)
	{
		ArrayList<ForIndexing> ff = new ArrayList<>();
		for (String word : words)
		{
			ForIndexing f = get(CleanWord.clean(word));
			if (f != null)
				ff.add(f);
		}
		return ff;
	}

	public String toString()
	{
		return docs + " documents indexed with " + list.size() + " terms";
	}
	
	/* des-setActivated all the features */
	public void desactivateAll()
	{
		for (String k : list.keySet())
		{
			ForIndexing fi = list.get(k);
			fi.setActivated(false);
			//fi.setID(-1);
		}
	}
	
	public void desactivateTop(int n)
	{
		TFSort list_ranks = sortByTF("1", "all");
		Iterator<TermValuePair> iter = list_ranks.getList().iterator();
		int i=0;
		while (iter.hasNext() && (i<n))
		{
			TermValuePair t = iter.next();
			ForIndexing f = list.get(t.getTerm());
			f.setActivated(false);
			i++;
		}
		System.out.println(n + " top words desactivated");
	}
	
	/* Desactivate the terms given in filename (one word per line). */ 
	public int desactivateFromFile(String filename) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(filename));
	    int nb = 0;
	    String line;
        while ((line = reader.readLine()) != null)
        {
        	//String feature = line.split(";")[0];
        	ForIndexing f = get(line.trim());
        	if ((f != null) && (f.isActivated()))
        	{
        		f.setActivated(false);
        		nb++;
        	}
        }
        reader.close();
	    System.out.println(nb + " stopwords desactivated from " + filename);
	    return nb;
	}
	
	/* setActivated the first nbmax features fetched from filename */
	public int activate(String filename, int nbmax, int minnbdocs, int mintf, int skipTerms) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(filename));
	    String line;
	    System.out.print("Activation from file " + filename + "...");
	    int nb = 0;
	    int nb_skipped = 0;
	    //int numline = 0;
	    long startTime = System.nanoTime();
        while (((line = reader.readLine()) != null) && ((nbmax == -1) || (nb < nbmax)))
        {
        	//numline ++;
        	if (line != null)
        	{
	        	String feature = line.split(";")[0];
	        	ForIndexing f = get(feature);
	        	if ((f != null) && (f.getNBDocs("all") >= minnbdocs) && f.getTF("all") >= mintf)
	        	{
	        		if (nb_skipped < skipTerms)
	        		{
	        			nb_skipped++;
	        		}
	        		else
	        		{
		        		f.setActivated(true);
		        		nb++;
	        		}
	        	}
        	}
        }
        reader.close();
        long estimatedTime = System.nanoTime() - startTime;
        System.out.println(" " + nb + " terms activated (" + TimeUnit.NANOSECONDS.toMillis(estimatedTime) + " ms)");
        return nb;
	}
		
	public TFSort sortByTF(String size, String gt)
	{
		TFSort tsort = new TFSort();
		for (String k : list.keySet())
		{
			//ForIndexing
			ForIndexing fi = list.get(k);
			int tf = fi.getTF(gt);
			boolean pass = true;
			if (!size.equals("all"))
			{ // filter on the term lenth
				int t = Integer.parseInt(size);
				if (fi.getLength() != t)
					pass = false;
			}
			if (pass)
			{
				tsort.add(k,  tf);
			}
		}
		return tsort;
	}
	
	public RandomSort sortByRandom(String t)
	{
		RandomSort tsort = new RandomSort();
		for (String k : list.keySet())
		{
			//ForIndexing
			ForIndexing fi = list.get(k);
			//int tf = fi.getTFAllDoc();
			int tf = fi.getTF("all");
			boolean pass = true;
			if (!t.equals("all"))
			{ // filter on the term lenth
				int size = Integer.parseInt(t);
				if (fi.getLength() != size)
					pass = false;
			}
			if (fi.getTF("all") < 2)
				pass = false;
			if (pass)
			{
				tsort.add(k,  tf);
			}
		}
		return tsort;
	}
	
	public ArrayList<ForIndexing> getActivatedFeatures()
	{
		ArrayList<ForIndexing> nlist = new ArrayList<ForIndexing>();
		for (String k : list.keySet())
		{
			ForIndexing fi = list.get(k);
			if (fi.isActivated())
				nlist.add(fi);
		}
		return nlist;
	}
		
	public int activateTerms(Predicate<ForIndexing> tester) {
		int nb = 0;
		for (String k : list.keySet())
		{
			ForIndexing fi = list.get(k);
			if (tester.test(fi))
			{
				fi.setActivated(true);
				nb++;
			}
		}
		return nb;
	}
		
	/*public int activateAll()
	{
		int nb = 0;
		for (String k : list.keySet())
		{
			ForIndexing fi = list.get(k);
			fi.setActivated(true);
			nb++;
		}
		return nb;
	}*/
	
	/*public int activateAllWords(int minnbdocs, int mintf)
	{
	}*/
	
	/* setActivated the n top words of each topic + terms that contain at least one of those words
	 * (inspired by the filter_candidates method of TopicLabelerSkeleton.java) */
	public void activateTermsUsedInTopics(LDATopicModel topic_word, int num_top_words)
	{
		// first, desactivate all terms (words included)
		desactivateAll();
		// for each topic, setActivated: a) top words, b) terms that contain top words
		for (int z=0; z<topic_word.numTopics(); z++)
		{
			ArrayList<Integer> list_words = topic_word.getTopWords(z);
			Iterator<Integer> iter = list_words.iterator();
			int count = 0;
			while (iter.hasNext() && (count < num_top_words))
			{
				Integer id = iter.next();				
				String word = topic_word.getAlphabet().lookupObject(id);
				look_up(word).setActivated(true);
			/*ArrayList<ForIndexing> list = new ArrayList<>();
			for (ForIndexing c : candidates[0])
			{
				if (topic_word.match(c.getTerm(), list_words, num_words_for_filtering))
					list.add(c);
			}*/
				count++;
			}
			for (ForIndexing f : getAllTerms())
			if (f.getLength() > 1)
			{
				/*if (f.getTerm().equals("attentat à la bombe"))
				{
					System.out.print(f.getTerm() + " found for topic " + z + " : ");
				}*/
				if (topic_word.match(f.getTerm(), list_words, num_top_words))
					f.setActivated(true);
					//if (f.getTerm().equals("attentat à la bombe")) System.out.println("fuck yes");
			}
		}
	}
	
	/* return all features associated to words (1-gram) */ 
	public ArrayList<ForIndexing> getAllWords()
	{
		ArrayList<ForIndexing> list_words = new ArrayList<>(); 
		for (String s : list.keySet())
		{
			ForIndexing f = list.get(s);
			if (f.getLength() == 1)
				list_words.add(f);
		}
		return list_words;
	}
	
	/* return all features in an array */ 
	public ArrayList<ForIndexing> getAllTerms()
	{
		ArrayList<ForIndexing> list_terms = new ArrayList<>(); 
		for (String s : list.keySet())
		{
			ForIndexing f = list.get(s);
			list_terms.add(f);
		}
		return list_terms;
	}	
	
}
