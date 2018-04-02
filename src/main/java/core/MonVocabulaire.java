package core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import io.config.LoadConfigFile;

/**
 *
 * Cette classe offre plusieurs fonctionnalités permettant d'extraire la terminologie avec Biotex ou réaliser l'indexation avec l'index inversé.
 *
 * @author julien
 */

public class MonVocabulaire {

	// whole inverted index
	private static InvertedIndex index;
	
	// temporal distribution of terms
	private static HashMap<String,TreeMap<String,Integer>> distrib_terms;
	
	public static InvertedIndex getIndex()
	{
		return index;
	}
	
	// index all the words present in documents
	private static void indexAllWords()
	{
		ArrayList<ForIndexing> list_words = index.getAllWords();
		for (ForIndexing f : list_words)
		{
        	TreeSet<MyDocument> list = f.getDocs();
        	Iterator<MyDocument> iter = list.iterator();
        	while (iter.hasNext())
        	{
        		MyDocument d = iter.next();
        		f.addNBDocs("all", 1);
    			f.addNBDocs(d.getGround_truth(), 1);
    			int tf = d.getNBwords(f);
        		d.add(f,tf);
    			f.addTF("all", tf);
    			f.addTF(d.getGround_truth(), tf);
        	}
    	}
	}
	
	// index the terms listed in a given file (for n-grams, the base words must be already indexed) 
	private static void indexVocab(String filename) throws IOException
	{
		CleanWord.setStrategy("tolower,punct,stopwords,min=2");
		BufferedReader reader = new BufferedReader(new FileReader(filename));
	    String line;
	    int psize = index.size();
	    System.out.print("Indexing " + filename + "...");
	    long startTime = System.nanoTime();
        while ((line = reader.readLine()) != null)
        {
        	String feature = line.split(";")[0];

        	//feature = CleanWord.clean(feature);
        	ForIndexing newf = index.look_up(feature);
        			
        	ArrayList<String> tokens = CleanWord.tokenize(feature);
    		ArrayList<ForIndexing> features = index.getFeatures(tokens);
    		
    		//System.out.println("FEAT : " + feature);
    		/*if (feature.contains("high street"))
    		{
    			System.out.println("### " + feature);
    			System.out.print("tokens : ");
    			for (String w : tokens)
    				System.out.print(w + " - ");
    			System.out.println();
    			System.out.print("indexes : ");
    			for (ForIndexing f : features)
    				System.out.print(f.getTerm() + " (" + f.getTF("all") + ") ");
    			System.out.println();
    			//System.exit(0);
    		}*/

    		if (features.size() < 2)
    			continue;
    		
    		ForIndexing f = features.get(0);
        	for (int i=1; i<features.size(); i++)
        	{
        		//ForIndexing f2 = index.get(words[i]);
        		ForIndexing f2 = features.get(i);
        		if ((f != null) && (f2 != null))  
        			f = f.intersect(f2);	
        	}
        	if (f != null)
        	{	
	        	TreeSet<MyDocument> list = f.getDocs();
	        	Iterator<MyDocument> iter = list.iterator();
	        	while (iter.hasNext())
	        	{
	        		MyDocument d = iter.next();
	        		int tf = d.indexFeatures(features);
	        		if (tf > 0)
	        		{
	        			newf.add(d);
        				newf.addNBDocs("all", 1);
        				newf.addNBDocs(d.getGround_truth(), 1);
	        			d.add(newf,tf);
	    				newf.addTF("all", tf);
	    				newf.addTF(d.getGround_truth(), tf);
	        		}
	        	}
        	}
        }        
        reader.close();
        long estimatedTime = System.nanoTime() - startTime;
        psize = index.size() - psize;
        System.out.println(" " + psize + " new terms (" + TimeUnit.NANOSECONDS.toMillis(estimatedTime) + " ms)");
	}	
	
	public static void extractTerminology(String dir, String filename, String value)
	{
		String input = dir + Constantes.separateur + filename;
		String output = dir + Constantes.separateur + value;
		File ff = new File(output);
		if (!ff.exists())
		{
			System.out.print("Exécution de Biotex avec " + value + " : ");
			ff.mkdirs();
			long startTime = System.nanoTime();
			// appel de la fonction BioTex qui encapsule l'appel à BioTex (oui, c'est simple, je vous dis)
			CallBiotex.BioTex(input, output, value);
			long estimatedTime = System.nanoTime() - startTime;
	        System.out.println(TimeUnit.NANOSECONDS.toMillis(estimatedTime) + " ms");
		}
	}
	   
	// used by the indexing method below
	//private static boolean found;
	
	/* index either the terms present in the path or all the words otherwise */
    public static void indexing(String path)// throws IOException
    {	  
    	System.out.println("Populating the inverted index: ");
    	index = new InvertedIndex();
    	CleanWord.setStrategy(LoadConfigFile.getPreprocess());
    	for (String k : MyDocument.getAllKeys())
    	{
    		index.add(MyDocument.get(k));
    	}
    	System.out.println(index);
    	System.out.println("First index all the single words");
    	indexAllWords();
    	//found = false;
    	System.out.println("Next index all the terms if files found");
    	System.out.println(Paths.get(path));
		try {    	
			if (Files.isDirectory(Paths.get(path)))
				Files.walk(Paths.get(path)).forEach(filePath -> {
					if (Files.isRegularFile(filePath) && !filePath.toFile().isHidden())
						try {
							//found = true;
							indexVocab(filePath.toString());
						} catch (Exception e) {
							System.out.println("ERROR: file " + filePath.toString() + " is not readable!");
						}
				});
		} catch (IOException e) {
			System.out.println("ERROR: impossible to access files");
		} catch (InvalidPathException e) {
			System.out.println("ERROR: trouble with the path");
		}

		// if there is no biotex folder, so indexing is done on words only
		/*if (!found)
		{
			System.out.println("No folder for indexing: only words are indexed (so no n-grams for labeling)");
			indexAllWords();
		}*/
		
    	System.out.println(index);
    }
        
    /*public static void setVocab(String dir, String value, int n, boolean prop, double[] p, int[] v, int minnbdocs, int mintf, int[] skipTerms) throws IOException
    {
    	index.desactivateAll();
    	int total = 0;
    	if (prop)
    	{   // compute the exact value for the proportion of the vocabulary
	    	double[] res = new double[4];
	    	for (int i=0; i<4; i++)
	    	{
	    		double m = n*p[i];
	    		v[i] = (int)Math.floor(m);
	    		res[i] = m - v[i];
	    		total += v[i]; 
	    	}
	    	while (total < n)
	    	{
	    		// find the max residual
	    		int j = -1;
	    		double max = 0;
	    		for (int i=0; i<4; i++)
	    			if (res[i] > max)
	    			{
	    				j = i;
	    				max = res[i];
	    			}
	    		// assign a new n for j
	    		if (j != -1)
	    		{
		    		v[j]++;
		    		res[j] = 0;
		    		total++;
	    		}
	    	}
    	}
	    else
	    {
	    	for (int i=0; i<4; i++)
	    	{
	    		total += v[i];
	    	}
	    	for (int i=0; i<4; i++)
	    	{
	    		p[i] = (double)v[i] / (double)total;
	    		System.out.println(i + " => " + v[i]);
	    	}	    	
	    }
    	System.out.println("Size : " + n);
    	System.out.println("Volumes : " + v[0] + ","+v[1] + "," + v[2] + "," + v[3]);
    	String input = dir + Constantes.separateur + value + Constantes.separateur;
	    index.setActivated(input + "t1gram.txt", v[0], minnbdocs, mintf, skipTerms[0]);
	    index.setActivated(input + "t2gram.txt", v[1], minnbdocs, mintf, skipTerms[1]);
	    index.setActivated(input + "t3gram.txt", v[2], minnbdocs, mintf, skipTerms[2]);
	    index.setActivated(input + "t4gram.txt", v[3], minnbdocs, mintf, skipTerms[3]);
    }*/

    /** setActivated all words only */
    public static void setVocabAllWords(int minnbdocs, int mintf) throws IOException
    {
    	index.desactivateAll();
    	System.out.println("Activate all words");
    	index.activateTerms(f -> f.getLength() == 1);
    }

    /** setActivated all words + all terms found in the biotex output files (n>=2) */
    public static void setVocabAllTerms(String dir, String value, int minnbdocs, int mintf) throws IOException
    {
    	index.desactivateAll();
    	System.out.println("Activate all terms");
    	String input = dir + Constantes.separateur + value + Constantes.separateur;
	    //index.setActivated(input + "t1gram.txt", -1, minnbdocs, mintf, 0);
    	index.activateTerms(f -> f.getLength() == 1);    	
	    index.activate(input + "t2gram.txt", -1, minnbdocs, mintf, 0);
	    index.activate(input + "t3gram.txt", -1, minnbdocs, mintf, 0);
	    index.activate(input + "t4gram.txt", -1, minnbdocs, mintf, 0);
    }
    
    /*private static void setNbWords(int n)
    {
		nb_words = n;
	}

    public static int getNbWords()
    {
		return nb_words;
	}*/

	public static void extractTermBasedOnTF(String dir, String out) throws IOException
    {
    	String output = dir + Constantes.separateur + out;
    	new File(output).mkdirs();
    	TFSort tfs = index.sortByTF("all", "all");
    	tfs.toFile(output + Constantes.separateur + "ALL_gram.txt");
    	tfs = index.sortByTF("1", "all");
    	tfs.toFile(output + Constantes.separateur + "t1gram.txt");
    	tfs = index.sortByTF("2", "all");
    	tfs.toFile(output + Constantes.separateur + "t2gram.txt");
    	tfs = index.sortByTF("3", "all");
    	tfs.toFile(output + Constantes.separateur + "t3gram.txt");
    	tfs = index.sortByTF("4", "all");
    	tfs.toFile(output + Constantes.separateur + "t4gram.txt");    	
    }
    
    public static void extractTermBasedOnRandom(String dir, String out) throws IOException
    {
    	String output = dir + Constantes.separateur + out;
    	new File(output).mkdirs();
    	RandomSort tfs = index.sortByRandom("all");
    	tfs.toFile(output + Constantes.separateur + "ALL_gram.txt");
    	tfs = index.sortByRandom("1");
    	tfs.toFile(output + Constantes.separateur + "t1gram.txt");
    	tfs = index.sortByRandom("2");
    	tfs.toFile(output + Constantes.separateur + "t2gram.txt");
    	tfs = index.sortByRandom("3");
    	tfs.toFile(output + Constantes.separateur + "t3gram.txt");
    	tfs = index.sortByRandom("4");
    	tfs.toFile(output + Constantes.separateur + "t4gram.txt");    	
    }

	public static HashMap<String, Integer> getGTdistribTF(String k)
	{
		ForIndexing f = index.get(k);
		return f.getAllTF();
	}
	
	public static HashMap<String, Integer> getGTdistribNBDocs(String k)
	{
		ForIndexing f = index.get(k);
		return f.getAllNBDocs();
	}

	public static int computeAveragedTF()
	{
		int sum = 0;
		ArrayList<ForIndexing> list = index.getActivatedFeatures();
		for (ForIndexing fi : list)
		{
			sum += fi.getTF("all");
		}
		return sum;
	}

	public static int computeAveragedEntropy()
	{
		int sum = 0;
		ArrayList<ForIndexing> list = index.getActivatedFeatures();
		for (ForIndexing fi : list)
		{
			sum += calc_entropy(fi.getAllTF());
		}
		return sum;
	}
	
	// duplicate functions (see LabelRanking.java), trying to avoid that...
	private static double calc_entropy(HashMap<String,Integer> list)
	{
		Double result = 0.0;
		Integer i = list.get("all");
		if (i != null)
		{
			Double total = (double) i.intValue();
			for (String key : list.keySet())
			{
				if (!key.equals("all"))
				{
					Double frequency = (double) list.get(key) / total;
					result -= frequency * (Math.log(frequency) / Math.log(2));
				}
			}
		}
		else
			result = -999.0;
		return result;
	}
	
	private static double log2(double x)
	{
		return (Math.log(x) / Math.log(2));
	}

	/* Desactivate the terms given in filename. */
	public static void removeStopwords(String filename)
	{
		if (filename.toLowerCase().startsWith("top="))
		{
			String[] s = filename.split("=");
			if (s.length > 1)
			try {
				index.desactivateTop(Integer.parseInt(s[1].trim()));
			}
			catch (NumberFormatException e)
			{
				System.out.println("Error: invalid format for top command (ignored)");
			}
		}
		else
		try {
			index.desactivateFromFile(filename);
		}
		catch (IOException e)
		{
			System.out.println("Error: impossible to read the stoplist file " + filename + " (ignored)");
		}
	}
	
	public static ForIndexing getIndexTerm(String s)
	{
		return index.get(s);
	}
	
	public static ArrayList<ForIndexing> getActivatedTerms()
	{
		return index.getActivatedFeatures();
	}

	public static ArrayList<ForIndexing> getAllTerms()
	{
		return index.getAllWords();
	}	
	
	public static void computeDistribTemporelle()
	{
		distrib_terms = new HashMap<>();
		TreeMap<String,ForIndexing> list = index.getList();
		for (String s : list.keySet())
		{
			ForIndexing f = list.get(s);
			if (f.getTF("all") < 20)
				continue;
			TreeMap<String,Integer> d = new TreeMap<>();
			for (MyDate date : MyDocument.getSortedDates())
			{
				d.put(date.toString(), new Integer(0));
			}
			TreeSet<MyDocument> list_doc = f.getDocs();
			Iterator<MyDocument> iter = list_doc.iterator();
			while (iter.hasNext())
			{
				MyDocument doc = iter.next();
				String date_doc = MyDate.getTimePoint(doc.getDate());
				Integer i = d.get(date_doc);
				d.put(date_doc, new Integer(i + 1));
			}
			distrib_terms.put(s, d);
		}
		System.out.println(distrib_terms.size() + " words added");
	}
	
	public static TreeMap<String,Integer> getDistribTerm(String t)
	{
		return distrib_terms.get(t);
	}

}
