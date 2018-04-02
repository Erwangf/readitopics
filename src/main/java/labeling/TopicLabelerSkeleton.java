package labeling;

import core.*;
import exe.BrowseTopics;
import io.LoadDataset;
import topicmodeling.LDATopicModel;

import java.io.*;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Locale;

/**
 * Abstract class defining common attributes of a TopicLabeller implementation
*/
public abstract class TopicLabelerSkeleton implements TopicLabeler
{

	// number of topics
	protected int k;
	
	// alphabet mapping LDA IDs to (word) strings
	//protected Alphabet dataAlphabet;
	 
	//protected ParallelTopicModel model;
	//protected int[] tokensPerTopic;
	//protected ArrayList<TFSort> topicSortedDocs;
	//protected ArrayList<TreeSet<IDSorter>> topicSortedDocs;
	//protected ArrayList<TreeSet<IDSorter>> topicSortedWords;
	protected LDATopicModel topic_word;

	// fields dedicated to labeling
	protected int num_top_terms;
	protected int max_top_terms_calculated;
	protected ArrayList<ForIndexing>[] candidates;
	protected ArrayList<TermValuePair>[] topicLabels;
	protected int num_words_for_filtering;
	protected boolean different_candidates;
	
	// "label" directory 
	protected String dir_labeling;
	
	protected static String col_sep = ": ";
	protected static String sep_gauche = " (";
	protected static String sep_droite = ")";
	protected static String DIM_GREY = BrowseTopics.DIM_GREY;
	protected static String RESET = BrowseTopics.RESET;
	protected static String PURPLE = BrowseTopics.PURPLE; //"\u001B[35m";	
	protected static String YELLOW = BrowseTopics.YELLOW; //"\u001B[33m";
	protected static String LIGHT_BLUE = BrowseTopics.LIGHT_BLUE; 
	
	public TopicLabelerSkeleton(LDATopicModel topic_word, String dir) 
	{
		super();		
		
		this.topic_word = topic_word;
		k = topic_word.numTopics();
		
		num_top_terms = 1;
		max_top_terms_calculated = 100;
		
		num_words_for_filtering = 10;
		//this.dataAlphabet = dataAlphabet;
		topicLabels = new ArrayList[k];
		for (int i=0; i<k; i++)
			topicLabels[i] = new ArrayList<>();
		// default choice = no candidate
		//candidates = new ArrayList<ForIndexing>();
		dir_labeling = LoadDataset.getPath() + Constantes.separateur + Constantes.RESULT_DIR + Constantes.separateur + LoadDataset.getDataName() + Constantes.separateur + "labels" + Constantes.separateur + dir + Constantes.separateur;
	}

	public void setNBtopterms(int i)
	{
		if (i > 0)
			this.num_top_terms = i;
	}
	
	public void setNBwords_for_filtering(int i)
	{
		if (i > 0)
			this.num_words_for_filtering = i;
	}

	public int getnumtopics()
	{
		return this.k;
	}
	
	/* return the ordered list of labels for topic i */
	public ArrayList<TermValuePair> getAllTopicLabels(int i)
	{
		return topicLabels[i];
	}


	public void addCandidates(ArrayList<String>[] list_c)
	{
		if (list_c == null)
		{
			addCandidates(MonVocabulaire.getIndex().getAllTerms());
			return;
		}
		this.candidates = new ArrayList[topic_word.numTopics()];
		for (int i=0; i<topic_word.numTopics(); i++)
		{
			this.candidates[i] = new ArrayList<ForIndexing>();
			for (String s : list_c[i])
			{
				this.candidates[i].add(new ForIndexing(s));
			}
		}
		different_candidates = true; 
	}
	
	public void addCandidates(ArrayList<ForIndexing> activatedTerms)
	{
		this.candidates = new ArrayList[1];
		this.candidates[0] = activatedTerms;
		different_candidates = false;
	}				
	
	public int getNBcandidates()
	{
		return this.candidates[0].size(); 
	}

	public abstract void computeLabels();
	
	private String getDetailsOnLabel(ArrayList<TermValuePair> list_labs, int j)
	{
		TermValuePair lab = list_labs.get(j);
		Formatter out = new Formatter(new StringBuilder(), Locale.US);
		String t = lab.getTerm();
		double val = lab.getValue();
		if (val < 0.1)
			out.format("%.3e", val);
		else
			out.format("%.3f", val);

		return out.toString();
	}
	
	public String getLabel(int i, boolean print_weight)
	{
		StringBuilder label = new StringBuilder();
		for (int j=0; (j<num_top_terms) && (j< topicLabels[i].size()); j++)
		{
			if (j>0)
				label.append(DIM_GREY).append(col_sep).append(RESET);
			TermValuePair e = topicLabels[i].get(j);
			label.append(e.getTerm());
			if (print_weight)
			{
				label.append(DIM_GREY).append(sep_gauche);
				label.append(getDetailsOnLabel(topicLabels[i], j));
				label.append(sep_droite).append(RESET);
			}
		}
		return label.toString();
	}
	
	/* specific export for JSON format (see ExportLabels.java) */
	public String getLabelJSON(int i, boolean print_weight)
	{
		StringBuffer label = new StringBuffer();
		for (int j=0; (j<num_top_terms) && (j< topicLabels[i].size()); j++)
		{
			if (j>0)
				label.append(",\n\t\t\t");
			TermValuePair e = topicLabels[i].get(j);
			label.append("{\"label\":");
			label.append("\"" + CleanWord.TCleaner(e.getTerm()) + "\"");
			if (print_weight)
			{
				label.append(",\"score\":\"");
				label.append(getDetailsOnLabel(topicLabels[i], j));
				label.append("\""); 
			}
			label.append("}");
		}
		return label.toString();
	}	

	public String getLabels(boolean print_weight)
	{
		StringBuilder fulllabels = new StringBuilder();
		for (int i=0; i<k; i++)
		{
			fulllabels.append("z" + i + col_sep + getLabel(i, print_weight) + "\n");
		}
		return fulllabels.toString();
	}
	
	/* return a list of (indexed) terms that contain at least one word from the top-i words of topic z */
	public ArrayList<ForIndexing> filter_candidates(int z, int i)
	{
		ArrayList<Integer> list_words = topic_word.getTopWords(z);
		ArrayList<ForIndexing> list = new ArrayList<>();
		for (ForIndexing c : candidates[0])
		{
			if (topic_word.match(c.getTerm(), list_words, i))
				list.add(c);
		}
		return list;
	}
	
	/*public double getLikelihood_word_byID(int id, int z, int i)
	{
		int total_tokens = tokensPerTopic[z];
		TreeSet<IDSorter> list_words = topicSortedWords.get(z);
		Iterator<IDSorter> iter = list_words.iterator();
		int count = 0;
		while (iter.hasNext() && (count < i))
		{
			IDSorter e = iter.next();
			if (e.getID() == id)
				return e.getWeight() / (double)total_tokens;
			if (i != -1)
				count++;
		}
		return -1;
	}*/	
	
	public void export_labels()
	{
		String filename = getName();
		new File(dir_labeling).mkdirs();
		try
		{
			 FileOutputStream w = new FileOutputStream(dir_labeling + filename + ".label");
			 ObjectOutputStream o = new ObjectOutputStream(w);
			 o.writeObject(topicLabels);
			 o.close();
			 w.close();			
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void import_labels() throws IOException, ClassNotFoundException
	{
		String filename = getName();
		System.out.println("Load " + dir_labeling + filename + ".label");
		FileInputStream r = new FileInputStream(dir_labeling + filename + ".label");
		ObjectInputStream o = new ObjectInputStream(r);
		topicLabels = (ArrayList<TermValuePair>[])o.readObject();
		o.close();
		r.close();			
	}
	
	/* static methods */
	
	public static void set_export(String type)
	{
		switch(type)
		{
		case "csv":
			col_sep = "\t";
			sep_gauche = "\t";
			sep_droite = "\t";			
			LIGHT_BLUE = YELLOW = PURPLE = RESET = DIM_GREY = "";
			break;
		case "browse":
			col_sep = ": ";
			sep_gauche = " (";
			sep_droite = ")";						
			DIM_GREY = BrowseTopics.DIM_GREY;
			RESET = BrowseTopics.RESET;
			YELLOW = BrowseTopics.YELLOW;
			PURPLE = BrowseTopics.PURPLE;
			LIGHT_BLUE = BrowseTopics.LIGHT_BLUE;
			break;
		}
	}
	
}