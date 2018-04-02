package labeling;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import topicmodeling.MyHLDA;
import cc.mallet.pipe.Noop;
import cc.mallet.types.Alphabet;
import cc.mallet.types.FeatureSequence;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.util.Randoms;
import core.BuildFeatures4Mallet;
import core.Constantes;
import core.ForIndexing;
import core.MonVocabulaire;
import core.MyDocument;
import core.TFSort;
import core.TermValuePair;
import io.LoadDataset;
import topicmodeling.LDATopicModel;

public class ClusteringLabeler2 extends TopicLabelerSkeleton
{
	// hdp objects
	private MyHLDA[] hlda;
	// lists of points as instances
	private InstanceList[] instances_wordbased;
    // number of clusters
	private int[] cluster_number;
    // number of top words
	private int[] topwords_number;	
	// topic assignment
	private ArrayList<String>[] hlda_out;
	//private Map<Integer, Integer>[] cluster_assignment;
	// log posterior of words (unnormalized given by DPMM)
	//private ArrayList<Double>[] cluster_posterior; 
	// normalized posterior probabilities 
	//private ArrayList<Double>[] p_cw;
	//private ArrayList<Double[]>[] p_cw;
	// distribution of words over clusters
	//private ArrayList<Integer>[] clu_size;
	// alphabet needed for HDP
	private Alphabet dataAlphabet;
	
	//double alpha = 1.0;
    private double alpha;    
    private double eta;    
    private double gamma;
    //Hyper parameters of Base Function
    private double alphaWords;    
    private int maxIterations;
    
    private int NB_CLUSTERED_WORDS = 20;
    private int NB_DOCS_FOR_CLUSTERING = 20;
    
    private InstanceList full_instances;
		
	public ClusteringLabeler2(LDATopicModel topic_word, String dir, InstanceList full_instances)
	{
		super(topic_word, dir);
		this.alpha = 10.0;
		this.eta = 0.01;
		this.gamma = 0.01;
		this.alphaWords = 1.0;
		this.maxIterations = 500;
		this.dataAlphabet = new Alphabet(1000);
		this.full_instances = full_instances;
	}
	
	public void setAlpha(double alpha)
	{
		this.alpha = alpha;
	}
	
	public void setEta(double eta)
	{
		this.eta = eta;
	}

	public void setGamma(double gamma)
	{
		this.gamma = gamma;
	}

	public void setNbTopDoc(int topd)
	{
		this.NB_DOCS_FOR_CLUSTERING = topd;
	}
	
	public void setAlphaWords(double alphaWords)
	{
		this.alphaWords = alphaWords;
	}
	
	public void setMaxIterations(int i)
	{
		this.maxIterations = i;
	}
	
	/*private InstanceList getListForHDP(int i)
	{
		InstanceList instances = new InstanceList(new Noop()); 
		Iterator<Integer> iterator = topic_word.getTopWords(i).iterator();
		int nb = 0;
		while (iterator.hasNext() && (nb < NB_CLUSTERED_WORDS))
		{
			Integer id = iterator.next();
			String word = topic_word.getAlphabet().lookupObject(id);
			ForIndexing f = MonVocabulaire.getIndex().get(word);
			if (f == null)
			{
				System.out.println("No doc for " + word);
			}
			FeatureSequence fs = new FeatureSequence(this.dataAlphabet);
			TreeSet<MyDocument> list_docs = f.getDocs();
			Iterator<MyDocument> iter = list_docs.iterator();
			while (iter.hasNext())
			{
				MyDocument doc = iter.next();
				int id_doc_interne = doc.getInternalIDForTM();
				int tf = doc.getTF(word);
				//int indice_new_alphabet = this.dataAlphabet.lookupIndex(doc.getId(), true);
				int indice_new_alphabet = this.dataAlphabet.lookupIndex(id_doc_interne, true);
				for (int j=0; j<tf; j++)
				{
					fs.add(indice_new_alphabet);
				}
			}
			instances.addThruPipe(new Instance(fs, "1", word, word));
			nb++;
		}
		topwords_number[i] = nb;
		return instances;
	}*/
	
	/* get the list of top docs indexed by top words */
	private InstanceList getListForHDP(int i)
	{
		BuildFeatures4Mallet buildF = new BuildFeatures4Mallet();
		InstanceList instances = new InstanceList(new Noop()); 
		Iterator<Integer> iterator = topic_word.getTopWords(i).iterator();
		int nb = 0;
		ArrayList<ForIndexing> list_to_words = new ArrayList<>();
		while (iterator.hasNext() && (nb < NB_CLUSTERED_WORDS))
		{
			Integer id = iterator.next();
			String word = topic_word.getAlphabet().lookupObject(id);
			ForIndexing f = MonVocabulaire.getIndex().get(word);
			if (f != null)
				list_to_words.add(f);
			nb++;
		}
		topwords_number[i] = nb;
		
		TFSort list_docs = topic_word.getSortedDocuments(0).get(i);
		Iterator<TermValuePair> iter = list_docs.getList().iterator();
		nb = 0;
		while ((iter.hasNext()) && (nb < this.NB_DOCS_FOR_CLUSTERING))
		{
			TermValuePair t = iter.next();				
			int id_doc = Integer.parseInt(t.getTerm());
			String name = (String)full_instances.get(id_doc).getName();			
			MyDocument doc = MyDocument.get(name);
			instances.addThruPipe(buildF.addInstanceAsSequence(doc, list_to_words));
			nb++;
		}
		return instances;
	}


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
	
	// parse the node string s for extracting total nb of token
	private double get_total_token(String s)
	{
		String[] split = s.split("\t");
		return Double.parseDouble(split[0]);
	}

	// parse the node string s for extracting total nb of docs
	private double get_nbdocs(String s)
	{
		String[] split = s.split("\t");
		return Double.parseDouble(split[1]);
	}
	
	// parse the node string s for extracting nb of token for word w
	// pay attention: need the prob associated to words
	private double get_token_for(String s, String w)
	{
		String[] split = s.split("\t");
		String[] split_words = split[2].split(" ");
		for (String tuple : split_words)
		{
			String[] split_val = tuple.split(":");
			if (split_val[0].equals(w))
				return Double.parseDouble(split_val[1]); 
		}
		return 0.0;

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
	    ArrayList<String> node_list = hlda_out[i];
		for (ForIndexing c : c_for_topic)
		{
			String[] split = c.getTerm().split(" ");
			double[] score = new double[cluster_number[i]];
			for (int j=0; j<cluster_number[i]; j++)
			{
				// first: compute the average prob of covered words
				double sum_log = 0;
				//int nb_prod = 0;
				for (String w : split)
				{
					// compute p(w/c_i)					
					double proba_word = 0;					

					double total_root = get_total_token(node_list.get(0));
					double root_word = get_token_for(node_list.get(0), w);
					double total_leaf = get_total_token(node_list.get(j+1));
					double leaf_word = get_token_for(node_list.get(j+1), w);
					
					// tweak: more importance to leaf nodes
					double delta = leaf_word;
					leaf_word *= 3;
					delta = leaf_word - delta;
					total_leaf += delta;
					
					double total_token = total_root + total_leaf;
					double total_word = root_word + leaf_word;
					
					if ((total_token>0) && (total_word>0))
						proba_word = total_word / total_token;
					
					if (proba_word>0)
						sum_log += Math.log(proba_word);	
				}				
				score[j] = sum_log;
				if (score[j] != 0)
				{
					list_c[j].add(c.getTerm(), score[j]);
				}
			}			
		}
		for (int j=0; j<cluster_number[i]; j++)
		{				
			Iterator<TermValuePair> iter = list_c[j].getList().iterator();
			if (iter.hasNext())
			{
				TermValuePair e = iter.next();
				res.add(e.getTerm(), get_nbdocs(node_list.get(j+1)));
			}
			else
				res.add("(empty)", get_nbdocs(node_list.get(j+1)));
		}
	}	
		
	/*private void check()
	{
		System.out.print("TEST = ");
		for (int i=0; i<k; i++)
			System.out.print(topwords_number[i] + "-");
		System.out.println();
	}*/
	
	//private static int total_nbc = 0;
	
	private void computeHDP(int i)
	{		
		// get the label candidates for topic i
		//ArrayList<ForIndexing> c_for_topic = filter_candidates(i, num_words_for_filtering);
		//double sum = 0;
		
		instances_wordbased[i] = getListForHDP(i);
		
		//System.out.println("z" + i+" : " + instances_wordbased[i].size());
		
        //Dirichlet Process parameter
        //Integer dimensionality = instances_wordbased[i].size();

        //Create an HDP object
        hlda[i] = new MyHLDA(); 
        hlda[i].setAlpha(this.alpha);
        hlda[i].setGamma(this.gamma);
        hlda[i].setEta(this.eta);
        
        hlda[i].setProgressDisplay(false);
        //hlda[i].setTopicDisplay(interval, words);
        Randoms random = new Randoms(123);
                
        /*for (int j=0; j<instances_wordbased[i].size(); j++)
        {
        	System.out.print("instance " + j + " : ");
        	Instance in = instances_wordbased[i].get(j);
        	FeatureSequence fs = (FeatureSequence)in.getData();
        	System.out.println(fs.toString());
        }*/
        
        hlda[i].initialize(instances_wordbased[i],null,2,random);
        
        /*System.out.print("["+hlda[i].getNbLeaves()+"]");
        System.exit(0);*/
        
        
        // Start capturing
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer));        
        
        hlda[i].estimate(this.maxIterations);
        
        // Stop capturing
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));        

        /*int nb_c = hlda[i].getNbLeaves();
        total_nbc += nb_c;
        System.out.print("["+nb_c+"]");
        */
        
        //hlda[i].printNodes(true);
			
	}

	public void computeHDP()
	{
		this.instances_wordbased = new InstanceList[k];
		this.hlda = new MyHLDA[k];
		topwords_number = new int[k]; // in case the number of top words is less than NB_CLUSTERED_WORDS
		System.out.println("Computing HDP:");
		for (int i=0; i<k; i++)
		{
			System.out.print(i+":");
			computeHDP(i);
		}
	}
	
	public void computeLabels(int i)
	{
		TFSort res = new TFSort();
		computeLabels_ngrams_docbased_2(i, res);
		//System.exit(0);
		int done = 0;
		TreeSet<TermValuePair> sorted_list = res.getList();
		Iterator<TermValuePair> iter = sorted_list.iterator(); 
		while ((iter.hasNext()) && (done < max_top_terms_calculated))
		{
			topicLabels[i].add(iter.next());
			done++;
		}		
	}
	
	/*private void compute_distrib_clusters()
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
	}*/
	
	public void computeLabels()
	{
		System.out.println("Computing labels:");
		// (re-)compute distrib over clusters 
		//compute_distrib_clusters();
		for (int i=0; i<k; i++)
		{
			System.out.print(i+":");
			computeLabels(i);
		}
	}
	
	public String getName()
	{
		return "clus_hdp_" + eta + "_" + NB_DOCS_FOR_CLUSTERING;
	}
	
	public String getShortName()
	{
		return "c2";
	}
	
	public void export_hdp()
	{
		//String result_output_dir = Constantes.RESULT_DIR + Constantes.separateur + LoadDataset.getDataName() + Constantes.separateur + "labels" + Constantes.separateur + dir + Constantes.separateur;
		String filename = getName();
		new File(dir_labeling).mkdirs();
		try
		{
			 FileOutputStream w = new FileOutputStream(dir_labeling + filename + ".hdp");
			 ObjectOutputStream o = new ObjectOutputStream(w);
			 for (int i=0; i<k; i++)
			 {
				 // 1st: save number of top words
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
				 ArrayList<String> node_list = hlda[i].getNodeForExport(true);
				 o.writeObject(node_list);				 
				 /*hlda[i].
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
				 }*/
			 }
			 o.close();
			 w.close();			
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void import_hdp() throws IOException, ClassNotFoundException
	{
		String filename = getName();
		cluster_number = new int[k];
		topwords_number = new int[k];
		hlda_out = new ArrayList[k];
		FileInputStream r = new FileInputStream(dir_labeling + filename + ".hdp");
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
			 ArrayList<String> node_list = (ArrayList<String>)o.readObject();
			 hlda_out[i] = node_list;
			 // we consider that each branch is meant to be a cluster (fusion of root node and leaf node)
			 cluster_number[i] = node_list.size()-1;
		}
		 o.close();
		 r.close();					
	}


}
