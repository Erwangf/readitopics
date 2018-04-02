package topicmodeling;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicAssignment;
import cc.mallet.types.Alphabet;
import cc.mallet.types.IDSorter;
import core.Constantes;
import core.ForIndexing;
import core.MyDocument;
import core.TFSort;
import core.TermValuePair;
import io.IOTopicModel;
import io.LoadDataset;

/* This class is intended to rebuild the matrix p(w/z) from the internal LDA representation of MALLET */

public class LDATopicModel
{
	private final int MAX_TOPIC_FOR_PAIR_COMPUTATION = 5;
	
	/* for accessing the files */
	private static IOTopicModel io_topicmodel;
	/* collection of models */
	private static ArrayList<ParallelTopicModel> models = null;	
	/* object for the current topic model */
	private static ParallelTopicModel current_model;	
	/* objects that stores the alphabet (our own and the MALLET one) */
	private static TopicAlphabet alphabet;
	private static Alphabet alphabetMALLET;
	
	/* p(w/z) for each topic in the model */
	private ArrayList<TermValuePair>[] pw_z;
	/* p(w) for each word */
	private ArrayList<TermValuePair> pw;
	
	/* p(z) for each period of time + whole period */
	private TreeMap<Integer,TreeMap<String,Double>> pz;
	
	/* p(z/d) */
	private TreeMap<Integer,double[]> pz_d;
	
	/* sorted topics = rank for each period of time + whole period */	
	//private static ArrayList<int[]> sortedTopics;
	private TreeMap<String,int[]> sortedTopics;
	
	/* sorted words per topic */
	private ArrayList<ArrayList<Integer>> sortedWords;
	
	/* sorted documents per topics */
	private ArrayList<TFSort>[] sortedDocuments;
	
	/* sorted documents per topics */
	private TFSort[][] sorted_pairwise_topics_documents;

	/* correlation between topics */
	private double[][] cor_docbased;
	private double[][] cor_wordbased;
	
	public LDATopicModel(ParallelTopicModel model, Alphabet dataAlphabet)
	{
		this.current_model = model;
		this.alphabet = new MALLETTopicAlphabet(dataAlphabet);
		
		System.out.print("Recompute top words for each topic");
		long startTime = System.nanoTime();
		recompute_top_words(); // may be pre-computed
		long estimatedTime = System.nanoTime() - startTime;		
		System.out.println(" (" + TimeUnit.NANOSECONDS.toMillis(estimatedTime) + " ms)");

		System.out.print("compute p(w/z) for each topic");
		startTime = System.nanoTime();
		compute_proba_pw_z();
		estimatedTime = System.nanoTime() - startTime;		
		System.out.println(" (" + TimeUnit.NANOSECONDS.toMillis(estimatedTime) + " ms)");

		System.out.print("compute p(z) for every period of time");
		startTime = System.nanoTime();		
		compute_distrib_temp_pz();
		estimatedTime = System.nanoTime() - startTime;		
		System.out.println(" (" + TimeUnit.NANOSECONDS.toMillis(estimatedTime) + " ms)");		
			
		System.out.print("compute p(w)");
		startTime = System.nanoTime();		
		compute_proba_pw();
		estimatedTime = System.nanoTime() - startTime;		
		System.out.println(" (" + TimeUnit.NANOSECONDS.toMillis(estimatedTime) + " ms)");		

		System.out.print("compute p(d/z)");
		startTime = System.nanoTime();
		sortedDocuments = new ArrayList[2];
		compute_pdz(true, Constantes.MAX_COMPUTED_TOPDOCS); // with the internal ids of LDA & for our inverted index
		estimatedTime = System.nanoTime() - startTime;		
		System.out.println(" (" + TimeUnit.NANOSECONDS.toMillis(estimatedTime) + " ms)");		
	}
	
	public ParallelTopicModel getCurrentTopicModel()
	{
		return current_model;
	}
	
	/* return the assignement of topics given by the MCMC for every word */
	public ArrayList<TopicAssignment> getData()
	{
		return current_model.getData();
	}
	
	/* get the alphabet object */
	public TopicAlphabet getAlphabet()
	{
		return alphabet;
	}
		
	public void recompute_top_words()
	{
		sortedWords = new ArrayList<>();
		ArrayList<TreeSet<IDSorter>> list = current_model.getSortedWords();
		for (int i=0; i<current_model.numTopics; i++)
		{
			ArrayList<Integer> newl_id = new ArrayList<>();
			TreeSet<IDSorter> l_words = list.get(i);
			Iterator<IDSorter> iter = l_words.iterator();
			while (iter.hasNext())
			{
				IDSorter is = iter.next();
				newl_id.add(new Integer(is.getID()));
			}
			sortedWords.add(newl_id);
		}
	}
	
	public ArrayList<Integer> getTopWords(int i)
	{
		return sortedWords.get(i);
	}
	
	/* get p(z/d) and use look-up table for avoiding recomputation */
	public double[] getTopicProbabilities(int doc)
	{
		if (pz_d == null)
			pz_d = new TreeMap<>();
		double[] val = pz_d.get(doc);
		if (val == null) {
			val = current_model.getTopicProbabilities(doc);
			pz_d.put(doc, val);
		}		
		return val; 
	}
	
	public void compute_proba_pw_z()
	{
		int topicMask;
		int topicBits;
		if (Integer.bitCount(current_model.numTopics) == 1) {
			// exact power of 2
			topicMask = current_model.numTopics - 1;
			topicBits = Integer.bitCount(topicMask);
		}
		else {
			// otherwise add an extra bit
			topicMask = Integer.highestOneBit(current_model.numTopics) * 2 - 1;
			topicBits = Integer.bitCount(topicMask);
		}	
		pw_z = new ArrayList[current_model.numTopics];
		for (int i=0; i<current_model.numTopics; i++)
		{
			//ArrayList<TermValuePair> list = new ArrayList<>();
			pw_z[i] = new ArrayList<>();
			double total_tokens = current_model.getTokensPerTopic()[i];
			for (int type = 0; type < alphabet.size(); type++)
			{
				String word = alphabet.lookupObject(type);
				int[] topicCounts = current_model.getTypeTopicCounts()[type];
				double weight = 0.01; // beta initial value for smoothing
				int index = 0;
				while (index < topicCounts.length && topicCounts[index] > 0)
				{
					int currentTopic = topicCounts[index] & topicMask;
					if (currentTopic == i)
					{
						weight += topicCounts[index] >> topicBits;
						break;
					}
					index++;
				}
				TermValuePair t = new TermValuePair(word, (double)(weight / total_tokens));
				pw_z[i].add(t);
			}
		}
	}	
	
	public void compute_proba_pw()
	{
		/*if (pz == null)
			System.out.println("pz not available");*/
		pw = new ArrayList<>();
		//double total_tokens = model.getTokensPerTopic()[i];
		for (int type = 0; type < alphabet.size(); type++)
		{			
			double sum = 0;
			for (int i=0; i<current_model.numTopics; i++)
			{
				TermValuePair t = pw_z[i].get(type);
				sum += t.getValue() * pz.get(i).get("all").doubleValue();
			}
			sum /= MyDocument.size();
			pw.add(new TermValuePair(pw_z[0].get(type).getTerm(), sum));
		}
	}
	
	/* return p(w/z) of word w and the selected topic */
	public double getProbaWordGivenTopic(String word, int topic)
	{
		int id = alphabet.getIndex(word);
		if (id != -1)
			return getProbaWordGivenTopic(id, topic);
		return -1;
	}
	
	/* return p(w) of word indexed by its name (for the model) */
	public double getProbaWord(String word)
	{
		int id = alphabet.getIndex(word);
		if (id != -1)
			return getProbaWord(id);
		return -1;
	}	

	/* return p(w/z) of word indexed by word_id and the selected topic */
	public double getProbaWordGivenTopic(int word_id, int topic)
	{
		if (word_id >= pw_z[topic].size())
			return -1;
		return pw_z[topic].get(word_id).getValue();
	}

	/* return p(w) of word indexed by word_id (for the model) */
	public double getProbaWord(int word_id)
	{
		if (word_id >= pw.size())
			return -1;
		return pw.get(word_id).getValue();
	}

	/* return p(z) for topic i and selected period (possibly "all") */
	public double get_pz(String period, int i)
	{
		return pz.get(i).get(period);
	}
	
	/* return p(z) for topic i all periods */
	public TreeMap<String,Double> get_pz(int i)
	{
		return pz.get(i);
	}	
	
	/* return the rank of topic i for the selected period (possibly "all") */
	public int get_rank(String period, int i)
	{
		return sortedTopics.get(period)[i];
	}
	
	/* return the rank array for all topics for the selected period (possibly "all") */
	public int[] get_rank(String period)
	{
		return sortedTopics.get(period);
	}
	
	public void compute_distrib_temp_pz()
	{
		pz = new TreeMap<>();
		sortedTopics = new TreeMap<>();
		// calculate p(z) for each period of time + the sum
		for (int i=0; i<current_model.numTopics; i++)
			pz.put(i, new TreeMap<>());
		for (String key : MyDocument.getDocPerDate().keySet())
		{
			double[] topicDistribution = new double[current_model.numTopics];
			for (int i=0; i<current_model.numTopics; i++)
				topicDistribution[i] = 0;
			TreeSet<MyDocument> l = MyDocument.getDocPerDate().get(key);
			Iterator<MyDocument> iter = l.iterator();
			while (iter.hasNext())
			{
				MyDocument doc = iter.next();
				double[] td = current_model.getTopicProbabilities(doc.getInternalIDForTM());
				for (int i=0; i<current_model.numTopics; i++)
					topicDistribution[i] += td[i];
			}
			for (int i=0; i<current_model.numTopics; i++)
			{
				TreeMap<String,Double> list_p = pz.get(i);
				list_p.put(key, topicDistribution[i]);
				pz.put(i, list_p);
			}
			// compute the ranking
			TFSort list = new TFSort();
			for (int i=0; i<current_model.numTopics; i++)
				list.add("" + i, topicDistribution[i]);
			int[] list_ranks = new int[current_model.numTopics];
			TreeSet<TermValuePair> list_sorted = list.getList();
			Iterator<TermValuePair> iter2 = list_sorted.iterator();
			int r = 1;
			while (iter2.hasNext())
			{
				TermValuePair t = iter2.next();
				list_ranks[Integer.parseInt(t.getTerm())] = r;
				r++;
			}
			sortedTopics.put(key, list_ranks);
		}
		// add the total sum over time
		TFSort list = new TFSort();
		int[] list_ranks = new int[current_model.numTopics];
		for (int i=0; i<current_model.numTopics; i++)
		{
			double sum = 0;
			for (String key : pz.get(i).keySet())
				sum += pz.get(i).get(key);
			list.add(""+i, sum);
			pz.get(i).put("all", sum);
		}
		// compute the total ranking
		TreeSet<TermValuePair> list_sorted = list.getList();
		Iterator<TermValuePair> iter2 = list_sorted.iterator();
		int r = 1;
		while (iter2.hasNext())
		{
			TermValuePair t = iter2.next();
			list_ranks[Integer.parseInt(t.getTerm())] = r;
			r++;
		}
		sortedTopics.put("all", list_ranks);		
	}

	public void setCorrelation_docbased(double[][] c)
	{
		cor_docbased = c;		
	}

	public void setCorrelation_wordbased(double[][] c)
	{
		cor_wordbased = c;		
	}

	public double getCorrelation(String type, int i, int j)
	{
		switch(type)
		{
		case "docbased":
			if (cor_docbased == null)
				return -99;
			else
				return cor_docbased[i][j];
		case "wordbased":
			if (cor_wordbased == null)
				return -99;
			else
				return cor_wordbased[i][j];
		}
		return -99;
	}

	public ArrayList<TermValuePair> get_pw_z(int i)
	{
		return pw_z[i];
	}	

	public ArrayList<TermValuePair> get_pw()
	{
		return pw;
	}	
	
	/* re-implementation of the MALLET method but keeping only the first max docs */
	public ArrayList<TFSort> getSortedDocumentsMALLET(double smoothing, int max)
	{
		ArrayList<TFSort> topicSortedDocuments = new ArrayList<TFSort>(current_model.numTopics);
		// Initialize the tree sets
		for (int topic = 0; topic < current_model.numTopics; topic++)
		{
			topicSortedDocuments.add(new TFSort(max));
		}

		int[] topicCounts = new int[current_model.numTopics];

		ArrayList<TopicAssignment> data = current_model.getData();
		
		for (int doc = 0; doc < data.size(); doc++)
		{
			int[] topics = data.get(doc).topicSequence.getFeatures();
			for (int position = 0; position < topics.length; position++)
			{
				topicCounts[ topics[position] ]++;
			}

			for (int topic = 0; topic < current_model.numTopics; topic++)
			{
				topicSortedDocuments.get(topic).add(""+doc, (topicCounts[topic] + smoothing) / (topics.length + current_model.numTopics * smoothing) );
				topicCounts[topic] = 0;
			}
		}
		return topicSortedDocuments;
	}
	
	public ArrayList<TFSort> getSortedDocuments(int type)
	{
		return sortedDocuments[type];
	}
	
	/* get the list of top words in the MALLET format */
	public ArrayList<TreeSet<IDSorter>> getSortedWords() 
	{
		return current_model.getSortedWords();
	}
	
	/* compute the topic max documents depending on:
	 *  p(z/d) * p(d) (not normalized) 
	 *	p(z/d) * p(d) / p(z) (normalized, ie usuel when sorting labels for one given topic)
	 * type = 0 => get the LDA doc id
	 * type = 1 => get the official doc id
	 */ 
	public void compute_pdz(boolean norm, int max)
	{
		sortedDocuments[0] = new ArrayList<>();
		sortedDocuments[1] = new ArrayList<>();
		for (int i=0; i<numTopics(); i++)
		{
			TFSort list_docs_0 = new TFSort(max);
			TFSort list_docs_1 = new TFSort(max);
			for (String key : MyDocument.getAllKeys())
			{
				MyDocument doc = MyDocument.get(key);
				int id = doc.getInternalIDForTM();
				double pzd = current_model.getTopicProbabilities(id)[i];
				double pd = doc.pd(1);
				if ((pzd>0) && (pd>0))
				{
					double pdz = 0;
					if (norm)
					{
						double pz = get_pz("all", i);
						pdz = (pzd * pd) / pz;
					}
					else
						pdz = pzd * pd;
					list_docs_0.add(""+id, pdz);
					list_docs_1.add(doc.getId(), pdz);
				}				
			}
			sortedDocuments[0].add(list_docs_0);
			sortedDocuments[1].add(list_docs_1);
		}
	}
	
	/* compute top doc following p(z/d), just for topic z (export oriented) */
	public TFSort compute_pzd(int id_z, int max)
	{
		TFSort list = new TFSort(max);
		for (String key : MyDocument.getAllKeys())
		{
			MyDocument doc = MyDocument.get(key);
			int id = doc.getInternalIDForTM();
			double[] topicDistribution = current_model.getTopicProbabilities(id);
			list.add(key, topicDistribution[id_z]);
		}
		return list;
	}
	
	public void compute_pdzi_zj(int max)
	{
		sorted_pairwise_topics_documents = new TFSort[numTopics()][numTopics()];
		for (String key : MyDocument.getAllKeys())
		{
			MyDocument doc = MyDocument.get(key);
			double pd = doc.pd(1);
			int id = doc.getInternalIDForTM();
			double[] topicDistribution = current_model.getTopicProbabilities(id);
			TFSort sort = new TFSort(max);
			for (int j=0; j<topicDistribution.length; j++)
			if (topicDistribution[j] > 0)
				sort.add(""+j, topicDistribution[j]);
			ArrayList<TermValuePair> list = new ArrayList<>();
			list.addAll(sort.getList());
			for (int i=0; i<list.size(); i++)
			for (int j=i+1; j<list.size(); j++)
			{
				int z_i = Integer.parseInt(list.get(i).getTerm());
				int z_j = Integer.parseInt(list.get(j).getTerm());
				double p_i = list.get(i).getValue();
				double p_j = list.get(j).getValue();
				if (z_i > z_j) {
					int z_t = z_i;
					z_i = z_j;
					z_j = z_t;
					double p_t = p_i;
					p_i = p_j;
					p_j = p_t;		
				}
				double prod = p_i * p_j * pd;
				if (sorted_pairwise_topics_documents[z_i][z_j] == null)
					sorted_pairwise_topics_documents[z_i][z_j] = new TFSort();
				sorted_pairwise_topics_documents[z_i][z_j].add(id+"", prod);
			}
		}
	}
	
	/* return the top docs associated to both z_i and z_j */
	public TFSort get_pdz_ij(int z_i, int z_j) {
		if (sorted_pairwise_topics_documents == null) {
			System.out.println("compute p(d/z_i,z_j)");
			compute_pdzi_zj(MAX_TOPIC_FOR_PAIR_COMPUTATION);
		}
		if (z_i < z_j)
			return sorted_pairwise_topics_documents[z_i][z_j];
		else
			return sorted_pairwise_topics_documents[z_j][z_i];
	}
	

	/* return the number of topics */
	public int numTopics()
	{
		return current_model.numTopics;
	}

	/* return the probability p(z/d) if norm=F and p(d/z) if norm=T */
	public double getDocLikelihood(MyDocument d, int z, boolean norm)
	{
		double pz_d = getTopicProbabilities(d.getInternalIDForTM())[z];
		if (norm)
		{
			pz_d = (pz_d * d.pd(1)) / get_pz("all", z);
		}
		return pz_d;
	}
	
	/* return the average of the log likelikhood of docs covered by term c (normalized or not) */
	public double getAvDocLogLikelihood(ForIndexing c, int i, boolean norm)
	{
		double score = 0;
		int nb = c.getDocs().size();
		if (nb == 0)
			return score;
		TreeSet<MyDocument> list_docs = c.getDocs(); 
		Iterator<MyDocument> iter = list_docs.iterator();
		while (iter.hasNext())
		{
			MyDocument doc = iter.next();
			score += Math.log(getDocLikelihood(doc, i, norm));
		}
		score /= nb;
		score = Math.exp(score);
		return score;
	}

	public double getAvDocLogLikelihood(ForIndexing c, int i, boolean norm, TreeSet<MyDocument> sublist)
	{
		double score = 0;
		/*int nb = c.getDocs().size();
		if (nb == 0)
			return score;*/
		int nb = 0;
		TreeSet<MyDocument> list_docs = c.getDocs(); 
		Iterator<MyDocument> iter = list_docs.iterator();
		while (iter.hasNext())
		{
			MyDocument doc = iter.next();
			if (sublist.contains(doc))
			{
				score += Math.log(getDocLikelihood(doc, i, norm));
				nb++;
			}
		}
		if (nb > 0)
		{
			score /= nb;
			score = Math.exp(score);
		}
		return score;
	}	
	
	/* check whether one word of the <b>term</b> occurs in the <b>max_top</b> top words of <b>list_words</b>
	 * needs the alphabet object since the word list corresponds to ids */
	public boolean match(String term, ArrayList<Integer> list_words, int max_top)
	{
		String[] split = term.split(" ");
		int count = 0;
		Iterator<Integer> iter = list_words.iterator();
		while (iter.hasNext() && (count < max_top))
		{
			Integer id = iter.next();
			String word = getAlphabet().lookupObject(id);
			for (String s : split)
				if (s.equals(word))
					return true;
			count++;
		}
		return false;
	}	
	
	public static void loadTopicModels(String s)
	{
		String input = LoadDataset.getPath() + Constantes.separateur + Constantes.RESULT_DIR + Constantes.separateur + LoadDataset.getDataName() + Constantes.separateur + "models" + Constantes.separateur + s;
		io_topicmodel = new IOTopicModel(input);		
		/*models = null;
		alphabetMALLET = null;*/
		long startTime = System.nanoTime();
		System.out.print("Loading the topic model");
		try {
			models = io_topicmodel.importTM();
			alphabetMALLET = io_topicmodel.importAlphabet(models.get(0));
		} catch (Exception e) {
			models = null;
		}
		if ((models == null) || (models.size() == 0))
		{
			System.out.println("\nNo model to be loaded");
		}
		long estimatedTime = System.nanoTime() - startTime;		
		System.out.println(" (" + TimeUnit.NANOSECONDS.toMillis(estimatedTime) + " ms)");
	}
	
	public static LDATopicModel getFirstTopicModel() throws NoModel
	{
		if ((models == null) || (models.size() == 0))
			throw new NoModel();
		 return new LDATopicModel(models.get(0), alphabetMALLET);
	}
	
	public static IOTopicModel getIOFile()
	{
		return io_topicmodel;
	}
		
}
