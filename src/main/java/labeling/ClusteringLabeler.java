package labeling;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
//import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;
import org.apache.commons.math3.linear.ArrayRealVector;

import com.datumbox.opensource.clustering.DPMM;
import com.datumbox.opensource.clustering.MultinomialDPMM;
import com.datumbox.opensource.dataobjects.Point;

import core.Constantes;
/*import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.Alphabet;
import cc.mallet.types.IDSorter;*/
import core.ForIndexing;
import core.MonVocabulaire;
import core.MyDocument;
import core.TFSort;
import core.TermValuePair;
import io.LoadDataset;
import topicmodeling.LDATopicModel;

public class ClusteringLabeler extends TopicLabelerSkeleton
{
	// dpmm objects
	private DPMM[] dpmm;
	// lists of points
	private List<Point>[] pointList;
    // number of clusters
	private int[] cluster_number;
    // number of top words
	private int[] topwords_number;	
	// topic assignment
	private Map<Integer, Integer>[] cluster_assignment;
	// log posterior of words (unnormalized given by DPMM)
	private ArrayList<Double>[] cluster_posterior; 
	// normalized posterior probabilities 
	//private ArrayList<Double>[] p_cw;
	private ArrayList<Double[]>[] p_cw;
	// distribution of words over clusters
	private ArrayList<Integer>[] clu_size;	
	
	//double alpha = 1.0;
    private double alpha;    
    //Hyper parameters of Base Function
    private double alphaWords;    
    private int maxIterations;
    
    private int NB_CLUSTERED_WORDS = 20;
		
	public ClusteringLabeler(LDATopicModel topic_word, String dir)
	{
		super(topic_word, dir);
		this.alpha = 1.0;
		this.alphaWords = 1.0;
		this.maxIterations = 20;
	}
	
	public void setAlpha(double alpha)
	{
		this.alpha = alpha;
	}
	
	public void setAlphaWords(double alphaWords)
	{
		this.alphaWords = alphaWords;
	}
	
	public void setMaxIterations(int i)
	{
		this.maxIterations = i;
	}
	
	private List<Point> getListForDPMM(int i)
	{
		ArrayList<Point> pointList = new ArrayList<>();
		Iterator<Integer> iterator = topic_word.getTopWords(i).iterator();
		int nb = 0;
		/*if (topic_word.getTopWords(i).size() < NB_CLUSTERED_WORDS)
			System.out.println("PB avec topic " + i + " de liste taille " + topic_word.getTopWords(i).size());*/
		while (iterator.hasNext() && (nb < NB_CLUSTERED_WORDS))
		{
			Integer id = iterator.next();
			String word = topic_word.getAlphabet().lookupObject(id);
			ForIndexing f = MonVocabulaire.getIndex().get(word);
			if (f == null)
			{
				System.out.println("No doc for " + word);
			}
			ArrayRealVector ar = new ArrayRealVector(MyDocument.size());
			TreeSet<MyDocument> list_docs = f.getDocs();
			Iterator<MyDocument> iter = list_docs.iterator();
			while (iter.hasNext())
			{
				MyDocument doc = iter.next();
				int id_doc = doc.getInternalIDForTM();
				int tf = doc.getTF(word);
				ar.addToEntry(id_doc, (double)tf);
			}
			pointList.add(new Point(nb, ar));			
			nb++;
		}
		topwords_number[i] = nb;		
		return pointList;
	}
	
	/*private static double sum_iterations = 0;
	private static double sum_clusters = 0;*/
	
	/*private void computeLabels_foreachcluster(int i, DPMM dpmm, TFSort res)
	{
		ArrayList<DPMM.Cluster> clusters = (ArrayList<DPMM.Cluster>)dpmm.getClusterList();
	    StringBuffer[] sb = new StringBuffer[clusters.size()];
	    for (int j=0; j<clusters.size(); j++)
	    	sb[j] = new StringBuffer();
		Map<Integer, Integer> zi = dpmm.getPointAssignments();
		Iterator<IDSorter> iterator = topicSortedWords.get(i).iterator();
		StringBuffer s = new StringBuffer();
		int nb = 0;
		while (iterator.hasNext() && (nb < NB_CLUSTERED_WORDS))
		{
			IDSorter e = iterator.next();
			String word = (String)dataAlphabet.lookupObject(e.getID());
			Integer z = zi.get(nb);
			if (sb[z].length() > 0)
				sb[z].append(",");
			sb[z].append(word);
			nb++;
		}
		for (int j=0; j<clusters.size(); j++)
		{
			DPMM.Cluster clu = clusters.get(j);
			res.add("c" + j + "(" + clu.size() + ") " + sb[j], clu.size());
		}
	}	*/
	
	private int get_id_word(int i, String w)
	{
		int nb = 0;
		Iterator<Integer> iterator = topic_word.getTopWords(i).iterator();
		while (iterator.hasNext())
		{
			Integer id = iterator.next();
			String word = topic_word.getAlphabet().lookupObject(id);
			if (word.equals(w))
				return nb;
			nb++;
		}
		return -1;
	}
	
	private void computeLabels_ngrams_docbased_2(int i, TFSort res)
	{
		ArrayList<ForIndexing> c_for_topic;
		if (different_candidates)
			c_for_topic = candidates[i];
		else
			c_for_topic = filter_candidates(i, num_words_for_filtering);
		Formatter[] sb = new Formatter[cluster_number[i]];
	    TFSort[] list_c = new TFSort[cluster_number[i]];
	    for (int j=0; j<cluster_number[i]; j++)
	    {
	    	sb[j] = new Formatter(new StringBuilder(), Locale.US);
	    	list_c[j] = new TFSort();
	    }
	    Map<Integer, Integer> zi = cluster_assignment[i];
		for (ForIndexing c : c_for_topic)
		{
			String[] split = c.getTerm().split(" ");
			double[] score = new double[cluster_number[i]];
			for (int j=0; j<cluster_number[i]; j++)
			{
				// first: compute the average prob of covered words
				double prod = 1;
				int nb_prod = 0;
				for (String w : split)
				{
					int id_w = get_id_word(i, w);
					//if ((id_w == -1) || (id_w >= NB_CLUSTERED_WORDS))
					if ((id_w == -1) || (id_w >= topwords_number[i]))
					{
						continue;
					}
					Integer z = zi.get(id_w);
					if (z == j)
					{
						double p = topic_word.getProbaWordGivenTopic(w, i);
						if (p != 0)
						{
							prod *= p;
							nb_prod++;
						}
					}
				}
				if (nb_prod>0)
					prod = Math.pow(prod, 1.0/nb_prod);
				else
					prod = 0;
				// second: compute the set of covered docs by the clustered words
				int nb = 0;
				Iterator<Integer> iterator = topic_word.getTopWords(i).iterator();
				TreeSet<MyDocument> list_doc_with_topwords = new TreeSet<>();
				while (iterator.hasNext() && (nb < topwords_number[i]))
				{
					Integer id = iterator.next();
					String word = topic_word.getAlphabet().lookupObject(id);
					Integer z = zi.get(nb);
					if (z == j)
					{
						ForIndexing f = MonVocabulaire.getIndexTerm(word);			
						Iterator<MyDocument> iter2 = f.getDocs().iterator();
						while (iter2.hasNext())
							list_doc_with_topwords.add(iter2.next());
					}
					nb++;
				}
				// third: compute the average likelihood for those documents
				double doclike = topic_word.getAvDocLogLikelihood(c, i, false, list_doc_with_topwords);
				/*if ((c.getTerm().equals("queen daenerys")) && (i == 0))
					System.out.println("\n==> " + doclike + " ; " + prod);*/
				score[j] = doclike * prod;
				if (score[j] != 0)
				{
					list_c[j].add(c.getTerm(), score[j]);
				}
			}			
		}
		for (int j=0; j<cluster_number[i]; j++)
		{				
			/*sb[j].format("c%d (%d) ", j, clu_size[j]);
			Iterator<TermValuePair> iter = list_c[j].getList().iterator();
			while (iter.hasNext())
			{
				TermValuePair e = iter.next();				
				sb[j].format("%s (%.3f) ", e.getTerm(), e.getValue());
			}
			res.add(sb[j].toString(), clu_size[j]);*/
			Iterator<TermValuePair> iter = list_c[j].getList().iterator();
			if (iter.hasNext())
			{
				TermValuePair e = iter.next();
				res.add(e.getTerm(),  clu_size[i].get(j));
			}
			else
				res.add("(empty)",  clu_size[i].get(j));
		}
	}	
	
	private void computeLabels_ngrams_docbased(int i, TFSort res)
	{
		ArrayList<ForIndexing> c_for_topic;
		if (different_candidates)
			c_for_topic = candidates[i];
		else
			c_for_topic = filter_candidates(i, num_words_for_filtering);
		Formatter[] sb = new Formatter[cluster_number[i]];
	    TFSort[] list_c = new TFSort[cluster_number[i]];
	    for (int j=0; j<cluster_number[i]; j++)
	    {
	    	sb[j] = new Formatter(new StringBuilder(), Locale.US);
	    	list_c[j] = new TFSort();
	    }
	    Map<Integer, Integer> zi = cluster_assignment[i];
		for (ForIndexing c : c_for_topic)
		{
			double[] score = new double[cluster_number[i]];
			for (int j=0; j<cluster_number[i]; j++)
			{
				// first: compute the set of covered docs by the clustered words
				int nb = 0;
				Iterator<Integer> iterator = topic_word.getTopWords(i).iterator();
				TreeSet<MyDocument> list_doc_with_topwords = new TreeSet<>();
				while (iterator.hasNext() && (nb < NB_CLUSTERED_WORDS))
				{
					Integer z = zi.get(nb);
					if (z == j)
					{
						Integer id = iterator.next();
						String word = topic_word.getAlphabet().lookupObject(id);
						ForIndexing f = MonVocabulaire.getIndexTerm(word);			
						Iterator<MyDocument> iter2 = f.getDocs().iterator();
						while (iter2.hasNext())
							list_doc_with_topwords.add(iter2.next());
					}
					nb++;
				}
				// second: compute the average likelihood for those documents
				score[j] = topic_word.getAvDocLogLikelihood(c, i, false, list_doc_with_topwords);
				if (score[j] != 0)
				{
					list_c[j].add(c.getTerm(), score[j]);	
				}
			}			
		}
		for (int j=0; j<cluster_number[i]; j++)
		{				
			sb[j].format("c%d (%d) ", j, clu_size[i].get(j));
			Iterator<TermValuePair> iter = list_c[j].getList().iterator();
			while (iter.hasNext())
			{
				TermValuePair e = iter.next();
				sb[j].format("%s (%.3f) ", e.getTerm(), e.getValue());
			}
			res.add(sb[j].toString(), clu_size[i].get(j));
		}
	}
	
	private void computeLabels_ngrams(int i, TFSort res)
	{
		ArrayList<ForIndexing> c_for_topic;
		if (different_candidates)
			c_for_topic = candidates[i];
		else
			c_for_topic = filter_candidates(i, num_words_for_filtering);
		Formatter[] sb = new Formatter[cluster_number[i]];
	    TFSort[] list_c = new TFSort[cluster_number[i]];
	    for (int j=0; j<cluster_number[i]; j++)
	    {
	    	sb[j] = new Formatter(new StringBuilder(), Locale.US);
	    	list_c[j] = new TFSort();
	    }
	    Map<Integer, Integer> zi = cluster_assignment[i];
		int[] clu_size = new int[cluster_number[i]];
		for (int j=0; j<cluster_number[i]; j++)
			clu_size[j] = 0;		
		for (ForIndexing c : c_for_topic)
		{
			String[] split = c.getTerm().split(" ");
			double[] score = new double[cluster_number[i]];
			for (String w : split)
			{
				int id_w = get_id_word(i, w);
				if ((id_w == -1) || (id_w >= NB_CLUSTERED_WORDS))
				{
					continue;
				}
				Integer z = zi.get(id_w);
				clu_size[z]++;
				double num = topic_word.getProbaWordGivenTopic(w, i);
				if (num == -1)
				{
					continue;
				}
				/*if (norm == EVEN_NORM)
					den = 1.0 / topic_word.getAlphabet().size();
				else
				{
					den = (double)MonVocabulaire.getIndexTerm(w).getNBDocs("all") / (double)MyDocument.size();
					//System.out.print(den + "//");
				}*/
				double den = 1.0 / topic_word.getAlphabet().size();
				for (int j=0; j<cluster_number[i]; j++)
				if (j == z)
				{
					score[j] += Math.log(num/den);
				}
			}
			for (int j=0; j<cluster_number[i]; j++)
			{
				if (score[j] != 0)
				{
					list_c[j].add(c.getTerm(), score[j]);
				}	
			}			
		}
		for (int j=0; j<cluster_number[i]; j++)
		{				
			sb[j].format("c%d (%d) ", j, clu_size[j]);
			Iterator<TermValuePair> iter = list_c[j].getList().iterator();
			while (iter.hasNext())
			{
				TermValuePair e = iter.next();
				sb[j].format("%s (%.3f) ", e.getTerm(), e.getValue());
			}
			res.add(sb[j].toString(), clu_size[j]);
		}
	}
	
	private void computeLabels_allwords(int i, TFSort res)
	{
		Formatter[] sb = new Formatter[cluster_number[i]];
	    TFSort[] list_c = new TFSort[cluster_number[i]];
	    for (int j=0; j<cluster_number[i]; j++)
	    {
	    	sb[j] = new Formatter(new StringBuilder(), Locale.US);
	    	list_c[j] = new TFSort();
	    }
	    Map<Integer, Integer> zi = cluster_assignment[i];
		Iterator<Integer> iterator = topic_word.getTopWords(i).iterator();
		int nb = 0;
		int[] clu_size = new int[cluster_number[i]];
		for (int j=0; j<cluster_number[i]; j++)
			clu_size[j] = 0;
		while (iterator.hasNext() && (nb < NB_CLUSTERED_WORDS))
		{
			Integer id = iterator.next();
			String word = topic_word.getAlphabet().lookupObject(id);
			//double den = (double)MonVocabulaire.getIndexTerm(word).getNBDocs("all");
			Integer z = zi.get(nb);
			clu_size[z]++;
			double max = Double.NEGATIVE_INFINITY, second_max = Double.NEGATIVE_INFINITY;
			for (int j=0; j<cluster_number[i]; j++)
			{
				int id_array = j * NB_CLUSTERED_WORDS + nb;
				double p = cluster_posterior[i].get(id_array);				
				if (j == 0)
					max = p;
				else
				if (p > max)
				{
					second_max = max;
					max = p;
				}
				else
				if (p > second_max)
					second_max = p;
			}
			for (int j=0; j<cluster_number[i]; j++)
			{
				int id_array = j * NB_CLUSTERED_WORDS + nb;
				double p = cluster_posterior[i].get(id_array);								
				Formatter w = new Formatter(new StringBuilder(), Locale.US);				 
				if (z == j)
					/*w.format(YELLOW + "%s" + RESET, word);
				else*/
					w.format("%s ",word);					
				/*double ratio;
				if (clusters.size() == 1)
					ratio = p;
				else
				if (z == j)
					ratio = p - second_max;
				else
					ratio = p - max;
				list_c[j].add(w.toString(), ratio);*/
				sb[j].format(w.toString());
				w.close();
			}
			nb++;
		}
		for (int j=0; j<cluster_number[i]; j++)
			res.add(sb[j].toString(), clu_size[j]);
	}
		
	/*private void check()
	{
		System.out.print("TEST = ");
		for (int i=0; i<k; i++)
			System.out.print(topwords_number[i] + "-");
		System.out.println();
	}*/
	
	private void computeDPMM(int i)
	{		
		// get the label candidates for topic i
		ArrayList<ForIndexing> c_for_topic = filter_candidates(i, num_words_for_filtering);
		//double sum = 0;
		
		pointList[i] = getListForDPMM(i);
		
        //Dirichlet Process parameter
        Integer dimensionality = pointList[i].get(0).data.getDimension();

        //Create a DPMM object
        dpmm[i] = new MultinomialDPMM(dimensionality, alpha, alphaWords);        
        
        // Start capturing
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer));        
        
        int performedIterations = dpmm[i].cluster(pointList[i], maxIterations);
        
        // Stop capturing
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
        
        /*sum_iterations += performedIterations;
        sum_clusters += dpmm.getClusterList().size()*/
        
        //printDPMM(dpmm, i);
        //computeLabels_foreachcluster(i, dpmm, res);
        
        //computeLabels_ngrams(i, dpmm, res, pointList);
        
        /*for (int j=0; j<clusters.size(); j++)
        {
        	DPMM.Cluster clu = clusters.get(j);        	
        	sb.append("c" + j + "(" + clu.size() + ") ");
        	for (int w=0; w<NB_CLUSTERED_WORDS; w++)
        	{
        		sb.append(w+"#"+clu.posteriorLogPdf(pointList.get(w))+",");
        	}
        	sb.append("\n");
        }
        System.out.println(sb.toString());*/
        
        /*if(performedIterations<maxIterations) {
            System.out.println("Converged in "+String.valueOf(performedIterations));
        }
        else {
            System.out.println("Max iterations of "+String.valueOf(performedIterations)+" reached. Possibly did not converge.");
        }*/
        
        //get a list with the point ids and their assignments
        //printDPMM(dpmm, i);
        

		
		/*if (sum > 0)
		{
			res.add(c.getTerm(), sum);
		}*/
			
	}
	
	private void printDPMM(DPMM dpmm, int i)
	{
		Map<Integer, Integer> zi = dpmm.getPointAssignments();
		Iterator<Integer> iterator = topic_word.getTopWords(i).iterator();
		StringBuffer s = new StringBuffer();
		int nb = 0;
		while (iterator.hasNext() && (nb < NB_CLUSTERED_WORDS))
		{
			Integer id = iterator.next();
			String word = topic_word.getAlphabet().lookupObject(id);
			Integer z = zi.get(nb);
			s.append(word+":"+z+";");
			nb++;
		}        
		System.out.println("#clusters for " + i + ": " + dpmm.getClusterList().size());
		System.out.println(s.toString());
	}

	public void computeDPMM()
	{
		pointList = new List[k];
		dpmm = new DPMM[k];
		topwords_number = new int[k]; // in case the number of top words is less than NB_CLUSTERED_WORDS
		System.out.println("Computing DPMM:");
		for (int i=0; i<k; i++)
		{
			System.out.print(i+":");
			computeDPMM(i);
		}
		/*System.out.println();
		System.out.println("Average #clusters: " + (sum_clusters/k));
		System.out.println("Average #iterations: " + (sum_iterations/k));*/
	}
	
	public void computeLabels(int i)
	{
		TFSort res = new TFSort();
        //computeLabels_allwords(i, res);
		//computeLabels_ngrams(i, res);
		//computeLabels_ngrams_docbased(i, res);
		computeLabels_ngrams_docbased_2(i, res);
		int done = 0;
		TreeSet<TermValuePair> sorted_list = res.getList();
		Iterator<TermValuePair> iter = sorted_list.iterator(); 
		while ((iter.hasNext()) && (done < max_top_terms_calculated))
		{
			topicLabels[i].add(iter.next());
			done++;
		}		
	}
	
	private void compute_distrib_clusters()
	{
		clu_size = new ArrayList[k];
		for (int i=0; i<k; i++)
		{
			clu_size[i] = new ArrayList<Integer>();
			Map<Integer, Integer> zi = cluster_assignment[i];
			int[] distrib = new int[cluster_number[i]];
			for (int j=0; j<cluster_number[i]; j++)
				distrib[j] = 0;		
			for (int w=0; w<topwords_number[i]; w++)
			{
				Integer z = zi.get(w);
				distrib[z]++;
			}
			for (int j=0; j<cluster_number[i]; j++)
				clu_size[i].add(new Integer(distrib[j]));
		}
	}
	
	public void computeLabels()
	{
		System.out.println("Computing labels:");
		// (re-)compute distrib over clusters 
		compute_distrib_clusters();
		for (int i=0; i<k; i++)
		{
			System.out.print(i+":");
			computeLabels(i);
		}
	}
	
	public String getName()
	{
		return "clustering_" + alpha;
	}
	
	public String getShortName()
	{
		return "cl" + alpha;
	}
	
	public void export_dpmm()
	{
		//String result_output_dir = Constantes.RESULT_DIR + Constantes.separateur + LoadDataset.getDataName() + Constantes.separateur + "labels" + Constantes.separateur + dir + Constantes.separateur;
		String filename = getName();
		new File(dir_labeling).mkdirs();
		try
		{
			 FileOutputStream w = new FileOutputStream(dir_labeling + filename + ".dpmm");
			 ObjectOutputStream o = new ObjectOutputStream(w);
			 for (int i=0; i<k; i++)
			 {
				 // 1st: save number of top words
				 //System.out.print(topwords_number[i] + "-");
				 o.writeObject(topwords_number[i]);				 
				 // 2nd: save the top-n words for the topic
				 Iterator<Integer> iterator = topic_word.getTopWords(i).iterator();
				 int nb = 0;
				 while (iterator.hasNext() && (nb < NB_CLUSTERED_WORDS))
				 {
						Integer id = iterator.next();
						String word = topic_word.getAlphabet().lookupObject(id);
						o.writeObject(word);
						nb++;
				 }
				 // 3rd: save cluster assignment
				 Map<Integer, Integer> zi = dpmm[i].getPointAssignments();
				 o.writeObject(zi);
				 // 4th: save number of clusters + posteriors p(c/w)
				 ArrayList<DPMM.Cluster> clusters = (ArrayList<DPMM.Cluster>)dpmm[i].getClusterList();
				 int nbclusters = clusters.size();
				 o.writeObject(nbclusters);
				 for (int j=0; j<nbclusters; j++)
				 {
					 DPMM.Cluster clu = clusters.get(j);					 
					 for (int s=0; s< topwords_number[i]; s++)
					 {
						 double p_cw = clu.posteriorLogPdf(pointList[i].get(s));
						 o.writeObject(p_cw); 
					 }
				 }
			 }
			 o.close();
			 w.close();			
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void import_dpmm() throws IOException, ClassNotFoundException
	{
		//String result_output_dir = Constantes.RESULT_DIR + Constantes.separateur + LoadDataset.getDataName() + Constantes.separateur + "labels" + Constantes.separateur + dir + Constantes.separateur;
		String filename = getName();
		cluster_assignment = new Map[k];
		cluster_posterior = new ArrayList[k];
		p_cw = new ArrayList[k];
		cluster_number = new int[k];
		topwords_number = new int[k];
		FileInputStream r = new FileInputStream(dir_labeling + filename + ".dpmm");
		ObjectInputStream o = new ObjectInputStream(r);
		for (int i=0; i<k; i++)
		{
			// 1st: load the number of top words
			topwords_number[i] = (int)o.readObject();
			 // 2nd: load the top-n words for the topic (estimate if there is a mismatch)
			 Iterator<Integer> iterator = topic_word.getTopWords(i).iterator();
			 int nb = 0;
			 while (iterator.hasNext() && (nb < NB_CLUSTERED_WORDS))
			 {
					Integer id = iterator.next();
					String word = topic_word.getAlphabet().lookupObject(id);
					String word_loaded = (String)o.readObject();
					if (!word.equals(word_loaded))
						throw new IOException("Mismatch between top words of topic " + i + ": " + word + " (loaded topic model) <> " + word_loaded + " (loaded from dpmm)");
					if (k==0)
						System.out.print(word+"-");
					nb++;
			 }

			 if (nb != topwords_number[i])
				 throw new IOException("Mismatch between the number of loaded top words and the model for topic " + i + " (" + nb + " vs. " + topwords_number[i] + ")");			 
			 // 3rd: load cluster assignment
			 cluster_assignment[i] = (Map<Integer, Integer>)o.readObject();
			 // 4th: load number of clusters + posteriors p(c/w)
			 cluster_number[i] = (int)o.readObject();
			 cluster_posterior[i] = new ArrayList<Double>();
			 for (int j=0; j<cluster_number[i]; j++)
			 {
				 for (int s=0; s< topwords_number[i]; s++)
				 {
					 Double p_cw = new Double((double)o.readObject());
					 cluster_posterior[i].add(p_cw); 
				 }
			 }
		 }
		 o.close();
		 r.close();					
	}

	private void normalize(int i)
	{
		int nb_clusters = cluster_number[i];
		p_cw[i] = new ArrayList<>();
		double[][] proba_words = new double[nb_clusters][NB_CLUSTERED_WORDS]; 
		for (int id_w=0; id_w<NB_CLUSTERED_WORDS; id_w++)
		{
			double max_logp = Double.NEGATIVE_INFINITY;
			for (int j=0; j<nb_clusters; j++)
			{ // allocate and get the max
				int id_array = j * NB_CLUSTERED_WORDS + id_w;
				double logp = cluster_posterior[i].get(id_array);
				if (logp > max_logp)
					max_logp = logp;			
			}
			// and compute the proba with shifting
			double sum = 0;
			for (int j=0; j<nb_clusters; j++)
			{ 
				int id_array = j * NB_CLUSTERED_WORDS + id_w;
				double logp = cluster_posterior[i].get(id_array);
				double new_logp = logp - max_logp;
				if (new_logp < -20)
					proba_words[j][id_w] = 0;
				else
					proba_words[j][id_w] = Math.exp(new_logp);
				sum += proba_words[j][id_w];		
			}
			// final normalization
			for (int j=0; j<nb_clusters; j++)
				proba_words[j][id_w] = proba_words[j][id_w] / sum;
		}
		for (int j=0; j<nb_clusters; j++)
		{
			Double[] list_proba_words = new Double[NB_CLUSTERED_WORDS];
			for (int id_w=0; id_w<NB_CLUSTERED_WORDS; id_w++)
				list_proba_words[id_w] = new Double(proba_words[j][id_w]);
			p_cw[i].add(list_proba_words);
		}
	}
	
	public void printDPMM_2(int i)
	{
		Map<Integer, Integer> zi = cluster_assignment[i];
		Iterator<Integer> iterator = topic_word.getTopWords(i).iterator();
		StringBuffer s = new StringBuffer();
		s.append(PURPLE + "z" + i + RESET + " + #c=" + cluster_number[i] + ": ");
		int nb = 0;
		while (iterator.hasNext() && (nb < NB_CLUSTERED_WORDS))
		{
			Integer id = iterator.next();
			String word = topic_word.getAlphabet().lookupObject(id);
			Integer z = zi.get(nb);
			s.append(word+"[");
			for (int j=0; j<cluster_number[i]; j++)
			{
				int id_array = j * NB_CLUSTERED_WORDS + nb;
				if (z == j)
					s.append(YELLOW);
				s.append(j);
				if (z == j)
					s.append(RESET);
				s.append(DIM_GREY + "(");
				s.append(cluster_posterior[i].get(id_array) + ")" + RESET);	
			}
			s.append("] ");			
			nb++;
		}        
		System.out.println(s.toString());
	}

	public void printDPMM_3(int i)
	{
		normalize(i);
		Map<Integer, Integer> zi = cluster_assignment[i];
		Iterator<Integer> iterator = topic_word.getTopWords(i).iterator();
		StringBuffer s = new StringBuffer();
		s.append(PURPLE + "z" + i + RESET + " + #c=" + cluster_number[i] + ": "); 
		int nb = 0;
		while (iterator.hasNext() && (nb < NB_CLUSTERED_WORDS))
		{
			Integer id = iterator.next();
			String word = topic_word.getAlphabet().lookupObject(id);
			s.append(word+"[");
			Integer z = zi.get(nb);
			//int id_array = z * NB_CLUSTERED_WORDS + nb;
			for (int j=0; j<cluster_number[i]; j++)
			{
				if (z == j)
					s.append(YELLOW);
				s.append(j);
				if (z == j)
					s.append(RESET);
				s.append(DIM_GREY + "(" + p_cw[i].get(j)[nb] + ")" + RESET);
			}
			s.append("] ");
			nb++;
		}        
		System.out.println(s.toString());
	}
	
}
