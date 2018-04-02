package exe;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.TimeUnit;

import Object.CandidatTerm;
import cc.mallet.types.FeatureSequence;
import cc.mallet.types.InstanceList;
import cc.mallet.types.LabelSequence;
import core.CallBiotex;
import core.Constantes;
import core.ForIndexing;
import core.MonVocabulaire;
import core.MyDate;
import core.MyDocument;
import core.TFSort;
import core.TermValuePair;
import evaluation.TopicCoherence;
import io.Export;
import io.ExportDistribDocs;
import io.ExportDocs;
import io.ExportLabels;
import io.ExportPZ;
import io.ExportTopMaxForDocs;
import io.ExportTopdocs;
import io.ExportTopdocsBetween;
import io.ExportTopdocsTemp;
import io.ExportTopdocs_pzd;
import io.ExportTopicAnalytics;
import io.ExportTopicQuality;
import io.ExportTopicWord;
import io.ExportTopics;
import io.ExportVocab;
import io.config.LoadConfigFile;
import io.LoadDataset;
import jxl.read.biff.BiffException;
import jxl.write.WriteException;
import labeling.COrderLabeler;
import labeling.ClusteringLabeler;
import labeling.ClusteringLabeler2;
import labeling.DocBasedLabeler;
import labeling.NormDocBasedLabeler;
import labeling.OneOrderLabeler;
import labeling.SentenceBasedLabeler;
import labeling.Sentence_Based_3;
import labeling.TopicLabelerSkeleton;
import labeling.ZeroOrderLabeler;
import topicmodeling.NoModel;
import topicmodeling.LDATopicModel;
import jline.*;
import utils.CommonsUtils;

/**
 * Cette classe permet de lire le résultat fourni par LDA (package MALLET)
 * 
 * @author julien
 *
 */

public class BrowseTopics
{
				
	/* configuration file */
	private static String config;
		
	/* Instances of MALLET */
	private static InstanceList instances;
	
	/* topic model */
	private static LDATopicModel topic_model;

	/* labeling */
	private static ZeroOrderLabeler zero_labeler_n;
	private static ZeroOrderLabeler zero_labeler_u;
	private static OneOrderLabeler one_labeler;
	private static DocBasedLabeler docbased_labeler_n;
	private static DocBasedLabeler docbased_labeler_u;
	private static NormDocBasedLabeler normed_docbased_labeler;
	private static ClusteringLabeler clustering_labeler;
	private static ClusteringLabeler2 clustering_labeler_hdp;

	//Antoine
	private static COrderLabeler c_labeler_z;
	private static COrderLabeler c_labeler_t;
	private static SentenceBasedLabeler sentence_based_labeler;
	private static Sentence_Based_3 labeler11;
	private static Sentence_Based_3 labeler12;
	private static Sentence_Based_3 labeler13;
	private static Sentence_Based_3 labeler14;
	private static Sentence_Based_3 labeler15;
	private static Sentence_Based_3 labeler16;
	private static Sentence_Based_3 labeler17;
	private static Sentence_Based_3 labeler18;
	private static Sentence_Based_3 labeler19;
	
	
	public static ArrayList<TopicLabelerSkeleton> all_labelers;
	
	/* maximum number of printed topics, words, documents and labels */
	private static int MAX_PRINT_TOPICS = 10;
	private static int MAX_PRINT_WORDS = 10;
	private static int MAX_PRINT_DOCS = 10; 
	private static int MAX_PRINT_LABELS = 10;
	
	/* special characters */
	public static final String ESC = "\u001B";
	public static final String TOGGLE = "nop";
	public static final String NORMALIZED = "norm";
	public static final String RANK = "rank";
	public static final String SORTED = "sort";
	
	/* colors for ANSI writing */
	public static final String RESET = ESC + "[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";
	public static final String LIGHT_BLUE = "\u001B[94m";
	public static final String LIGHT_GREEN = "\u001B[92m";
	public static final String DIM_GREY = ESC + "[38;5;241m";
	
	/* mapping between a variable and its value */
	private static TreeMap<String,String> ans_map = new TreeMap<>();
	
	public static void main(String[] args) throws IOException, WriteException, BiffException, ClassNotFoundException,
			SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoModel {
		
		if (args.length == 1) load("data " + args[0]);

		loop();
	}
	
	// return the current topic model and throw an exception if it's not loaded yet
	public static LDATopicModel getModel() throws NoModel
	{
		if (topic_model == null)
			throw new NoModel();
		return topic_model;
	}
	
	// return the current dataset indexed by MALLET
	public static InstanceList getInstances()
	{
		return instances;
	}

	public static String load(String s) throws NoModel {
		String[] split = s.split(" ");
		if (s.equals("")) {
			help("load");
			return "";
		}
		//int index = -1;
		String fun = split[0];
		switch (fun) {
			case "data":
				try {
					return load_data(s.substring(s.indexOf(fun) + fun.length()));
				} catch (IOException e) {
					System.out.println(RED + "Error reading file :" + RESET);
					e.printStackTrace();
					return "";
				}
			case "topics":
				return load_topics(s.substring(s.indexOf(fun) + fun.length()));
			case "labels":
				return load_labeling(s.substring(s.indexOf(fun) + fun.length()));
			case "all":
				String r = load_topics(s.substring(s.indexOf(fun) + fun.length()));
				load_labeling(s.substring(s.indexOf(fun) + fun.length()));
				return r;
			default:
				help("load");
		}
		return "";
	}
	
	/* Load the dataset via config file s */
	
	private static String load_data(String s) throws IOException
	{
		if (s.equals(""))
		{
			System.out.println(RED + "Expecting a configuration file" + RESET);
			help("load data");
			System.out.println("Available configuration files:");
	    	String foldpath = LoadDataset.getPath() + Constantes.separateur + Constantes.CONFIG_DIR;
	    	String return_s = "";
	    	File file = new File(foldpath);
	    	for (File f : file.listFiles())
	    	{ // for each configuration file
	    		if (f.isFile() && !f.isHidden())
	    		{
	    			return_s = f.getName();
	    			System.out.println("  " + return_s);
	    		}
	    	}
	    	return return_s;
		}
		
		InputStream inputStream = new FileInputStream(Constantes.CONFIG_DIR + Constantes.separateur+s.trim());

		LoadConfigFile.loadConfig(inputStream);
				
		if (LoadDataset.isRawData())
			LoadDataset.extractFullCorpus(LoadDataset.getPath() + Constantes.separateur + Constantes.DATA_DIR + Constantes.separateur + LoadDataset.getRawData());
		
		//System.out.println("Weighting scheme: " + LoadConfigFile.getVocTypes()[0]);
		System.out.print("Load dataset: " + LoadDataset.getDataName());
		
		long startTime = System.nanoTime();
		MyDocument.reinitAllDocs();
		LoadDataset.extractDocs();
		long estimatedTime = System.nanoTime() - startTime;
		System.out.println(" (" + TimeUnit.NANOSECONDS.toMillis(estimatedTime) + " ms)");
		System.out.println(MyDate.toprint());
		
		String biotex_input_dir = LoadDataset.getPath() + Constantes.separateur + Constantes.RESULT_DIR + Constantes.separateur + LoadDataset.getDataName() + Constantes.separateur + "biotex";
		String path = biotex_input_dir + Constantes.separateur + LoadConfigFile.getVocTypes()[0] + Constantes.separateur;
		MonVocabulaire.indexing(path);
		
		/* set the ID used by LDA to the documents */
		MyDocument.setInternalIDforLDA();
		
		/* for browsing only */
		
		System.out.print("Compute number of tokens temporal distributions: ");
		/* compute the temporal distribution of documents */
		MyDocument.compute_distrib_temp_doc();
		/* compute p(d) for each period and for the whole period */
		MyDocument.compute_proba_docs();
		System.out.println(MyDocument.getTotalTokens() + " tokens for the whole period");		
		System.out.println("Compute word distribution over time");
		MonVocabulaire.computeDistribTemporelle();
				
		// set which vocabulary is used for LDA
		//int s = LoadConfigFile.getVocSizes()[0];
		//MonVocabulaire.setVocabAllWords(biotex_input_dir, LoadConfigFile.getVocTypes()[0], LoadConfigFile.getMinDocs(), LoadConfigFile.getMinTF());
		MonVocabulaire.setVocabAllWords(LoadConfigFile.getMinDocs(), LoadConfigFile.getMinTF());
		
		for (String sl : LoadConfigFile.getStopLists())
			if (!sl.isEmpty())
				MonVocabulaire.removeStopwords(LoadDataset.getPath() + Constantes.separateur + sl);
		
		instances = RunLDA.preprocessForTopicModeling();
		//dataAlphabet = instances.getDataAlphabet();
		
		return "";
	}
	
	/* Load the topic model named "s" (usually, a folder) related to the current dataset */ 
	
	private static String load_topics(String s) throws NoModel
	{
		if (LoadDataset.getDataName().isEmpty())
		{
			System.out.println(RED + "Load a dataset first" + RESET);
			return help("load data");
		}
		if (s.equals(""))
		{
			System.out.println(RED + "Expecting a model folder" + RESET);
			help("load topics");
			System.out.println("Available model folders:");
	    	String foldpath = LoadDataset.getPath() + Constantes.separateur + Constantes.RESULT_DIR + Constantes.separateur + LoadDataset.getDataName() + Constantes.separateur + "models";
	    	String return_s = "";
			CommonsUtils.getContentOfFolder(foldpath,true,false).forEach(System.out::println);
	    	/*File file = new File(foldpath);
	    	File[] files = file.listFiles();
	    	if(files != null){
				for (File f : files)
				{ // for each configuration file
					if (f.isDirectory() && !f.isHidden())
					{
						return_s = f.getName();
						System.out.println("  " + return_s);
					}
				}
			}*/

	    	return return_s;
		}

		String filename = s.trim();
		
		/*if (models.size() > 1)
		{
			System.out.println(": dealing with several models is not supported currently, try again");
			System.exit(0);
			System.out.print(LIGHT_GREEN + models.size() + RESET + " models loaded from " + YELLOW + filename + DIM_GREY + " (" + topic_model.numTopics() + " topics)" + RESET);
		}*/
		
		LDATopicModel.loadTopicModels(filename);
		topic_model = LDATopicModel.getFirstTopicModel();
		
		/*if (topic_model == null)
		{
			System.out.println(RED + ": no model to be loaded" + RESET);
			return "";
		}*/
		
		System.out.println(LIGHT_GREEN + topic_model.getAlphabet().size() + RESET + " words loaded for the vocabulary");		
		System.out.print("Model loaded from " + YELLOW + filename + DIM_GREY + " (" + topic_model.numTopics() + " topics)" + RESET);

		// load quality measures
		String input = LoadDataset.getPath() + Constantes.separateur + Constantes.RESULT_DIR + Constantes.separateur + LoadDataset.getDataName() + Constantes.separateur + "eval" + Constantes.separateur + filename;
		TopicCoherence.loadMeasures(topic_model, input + Constantes.separateur + "model.eval");
				
		double[][] correlations;
		try {
			correlations = LDATopicModel.getIOFile().import_correlation(input, "docbased");
			System.out.println("Pairwise doc-based correlation loaded");
			topic_model.setCorrelation_docbased(correlations);
		} catch (Exception ignored) { }
		try {
			correlations = LDATopicModel.getIOFile().import_correlation(input, "wordbased");
			System.out.println("Pairwise doc-based correlation loaded");
			topic_model.setCorrelation_wordbased(correlations);
		} catch (Exception ignored) { }
		
		// load the (sorted) distributions p(w/z) an p(d/z)
		//topicSortedWords = topic_word.getSortedWords();
		//topicSortedDocs = topic_word.getSortedDocuments(0, 50); // 0 means no smoothing, top 50 docs
		
		/*startTime = System.nanoTime();
		cor_topics = compute_correlation_topics();
		estimatedTime = System.nanoTime() - startTime;
		System.out.println(" in " + TimeUnit.NANOSECONDS.toSeconds(estimatedTime) + " s");*/
		
		System.out.println("Pay attention that the topics are numbered from 0");
		/*if (models.size() > 1) 
			System.out.println("For now, the first model only is open for browsing");*/
		
		return "0:" + (topic_model.numTopics()-1);
	}

	private static String nbtopics(String s) throws NoModel
	{
		System.out.println("NB topics: " + getModel().numTopics());
		return "";
	}
	
	private static ArrayList<Integer> getListNumbers(String s, int max)
	{
		ArrayList<Integer> list_docs = new ArrayList<>();
		String[] param = s.split(" ");
		try {
			for (String p : param)
			{
				if (p.contains(":"))
				{
					String[] del = p.split(":");
					if (del.length < 2)
						throw new NumberFormatException();
					for (int i=Integer.parseInt(del[0]); i<=Integer.parseInt(del[1]); i++)
					{
						Integer j = i;
						if ((j < 0) || (j >= max))
						{
							System.out.println(RED + "Error: index out of bound" + RESET);
							return new ArrayList<>();
						}
						list_docs.add(j);
					}
				}
				else
				if (p.length() > 0)
				{
					Integer j = Integer.parseInt(p);
					if ((j < 0) || (j >= max))
					{
						System.out.println(RED + "Error: index out of bound" + RESET);
						return new ArrayList<>();
					}
					list_docs.add(j);					
				}	
			}
		}
		catch (NumberFormatException e)
		{
			System.out.println(RED + "Invalid format: expecting integer numbers" + RESET);
			return new ArrayList<>();
		}
		if (list_docs.size() == 0)
		{
			String s_sub = ans_map.get("ans");
			if (s_sub != null)
			{
				list_docs = getListNumbers(s_sub, max);
			}
		}
		return list_docs;
	}
	
	private static String doc(String s) throws NoModel
	{
		boolean toggle = lookfortag(TOGGLE, s);
		if (toggle) s = s.replace(TOGGLE, "");
		System.out.print("Printing the list of tokens in " + LIGHT_BLUE + "documents " + s + RESET);
		if (!toggle)
			System.out.println(DIM_GREY + " (with attributed topics)" + RESET);
		else
			System.out.println();
		ArrayList<Integer> list_docs = getListNumbers(s, MyDocument.size());
		if (list_docs.size() == 0)
		{
			help("doc");
			return "";
		}				
		String return_s = "";
		for (Integer list_doc : list_docs) return_s = print_doc(list_doc, toggle);
		return return_s;
	}
	
	private static String remove_blanks(String s)
	{
		String out = s;
		while (!Objects.equals(out, out.replaceAll("  ", " ")))
			out = out.replaceAll("  ", " ");
		//System.out.println("CLEAN = *" + out + "*");
		return out.trim();		
	}
	
	private static String substitute_var(String s)
	{
		String new_s = s;
		for (String key : ans_map.keySet())
		{
			String replacement = ans_map.get(key);
			/*if (replacement == null)
				return s;*/			
			// var in the middle of the param
			new_s = new_s.replace(" " + key + " ", " " + replacement + " ");
			// var at the end
			int len = key.length();
			if (new_s.endsWith(" " + key))
				new_s = new_s.substring(0, new_s.length() - (len+1)) + " "  + replacement;
			if (new_s.startsWith(key + " "))
				new_s = replacement + " " + new_s.substring(len+1, new_s.length());
			if (new_s.equals(key))
				new_s = replacement;
		}
		return new_s;
	}
	
	private static void loop() throws ClassNotFoundException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException
	{
		BrowseTopics l = new BrowseTopics();
		History history = new History();

		ConsoleReader command = new ConsoleReader();
		command.setHistory(history);

		// IMPORTANT :
		// For windows users, use the following Java VM option :
		// -Djline.WindowsTerminal.directConsole=false

		System.out.println(YELLOW + "help" + RESET + " for a list of available commands");
		while(true) {


			String s = command.readLine("> ");

			s = s.trim();

			if (s.length() == 0) continue;

			String var_name = "ans";
			int egal = s.indexOf("=");
			if ((egal != -1) && (egal > 1) && (s.length() > 2))  // introducing a variable
			{
				var_name = s.substring(0, egal-1).trim();
				s = s.substring(egal+1, s.length()).trim();
			}

			int space = s.indexOf(" ");
			String fun;
			String par = "";
			if (space != -1)
			{
				fun = s.substring(0, space);
				if (space < s.length())
				{
					par = remove_blanks(s.substring(space+1, s.length()));
					par = substitute_var(par);
				}
			}
			else
			{
				fun = s;
			}
			//System.out.println("lancer " + fun + " : " + par);
			Class c = l.getClass();
			Class[] parameterTypes = { String.class };
			try
			{
				Method  method = c.getDeclaredMethod (fun, parameterTypes);
				String ans = (String)method.invoke(l, par);
				if (!ans.equals(""))
				{
					System.out.println(DIM_GREY + var_name + " = " + ans + RESET);
					if (!var_name.isEmpty())
					{
						ans_map.remove(var_name);
						ans_map.put(var_name, ans);
					}
				}
			}
			catch (NoSuchMethodException e)
			{
				String n = ans_map.get(fun);
				if (n != null)
					System.out.println(DIM_GREY + fun + " = " + RESET + n);
				else
					System.out.println("No such method.");
			}
			catch (InvocationTargetException ite)
			{ 
				  try { throw ite.getCause(); }
				  catch (NoModel e)
				  {
					  System.out.println(RED + "Error: you must load a model first" + RESET);
				  }
				  catch (Throwable e)
				  {
					  e.printStackTrace();
				  }
			}
		}
	}
		
	private static String print_doc(int instanceID, boolean toggle) throws NoModel
	{
		StringBuilder return_s = new StringBuilder();
		// The data alphabet maps word IDs to strings		
		FeatureSequence tokens = (FeatureSequence)getModel().getData().get(instanceID).instance.getData();
		LabelSequence topics = getModel().getData().get(instanceID).topicSequence;
		
		Formatter out = new Formatter(new StringBuilder(), Locale.US);
		out.format(LIGHT_BLUE + "doc %5d" + RESET + " :", instanceID);
		for (int position = 0; position < tokens.getLength(); position++)
		{
			return_s.append(topic_model.getAlphabet().lookupObject(tokens.getIndexAtPosition(position))).append(" ");
			out.format(" %s", topic_model.getAlphabet().lookupObject(tokens.getIndexAtPosition(position)));
			if (!toggle)
				out.format(DIM_GREY + " (%d)" + RESET, topics.getIndexAtPosition(position));
		}
		System.out.println(out);
		return return_s.toString();
	}
	
	/* get the list of documents related to two topics */
	private static String pdz(String s) throws NoModel
	{
		ArrayList<Integer> list_docs = getListNumbers(s, getModel().numTopics());
		TFSort list = getModel().get_pdz_ij(list_docs.get(0), list_docs.get(1));
		Formatter out = new Formatter(new StringBuilder(), Locale.US);
		for (TermValuePair t : list.getList()) {
			out.format("%3d (%.5f) ", Integer.parseInt(t.getTerm()), t.getValue());
		}
		System.out.println(out.toString());
		return out.toString();
	}
	
	private static String topic(String s) throws NoModel
	{
		boolean toggle = lookfortag(TOGGLE, s);
		if (toggle) s = s.replace(TOGGLE, "");		
		boolean rank = lookfortag(SORTED, s);
		if (rank) s = s.replace(SORTED, "");
		ArrayList<Integer> list_docs = getListNumbers(s, getModel().numTopics());
		if (list_docs.size() == 0)
		{	
			help("topic");
			return "";
		}
		System.out.print("Printing " + PURPLE + "top " + MAX_PRINT_WORDS + RESET + " words for " + LIGHT_GREEN + "topics " + s.trim() + RESET);
		if (!toggle)
			System.out.println(DIM_GREY + " (with the associated probabilities p(w/z))" + RESET);
		else
			System.out.println();		
		StringBuilder return_s = new StringBuilder();
		//try {
			String[] list_s = new String[getModel().numTopics()];
			for (int i=0; i<list_docs.size(); i++)
			{
				return_s = new StringBuilder();
				int topic = list_docs.get(i);
				Formatter out = new Formatter(new StringBuilder(), Locale.US);
				Iterator<Integer> iterator = topic_model.getTopWords(topic).iterator();
				out.format(LIGHT_GREEN + "topic %3d" + RESET, topic);
				if (!toggle)
				{
					double p = topic_model.get_pz("all",  topic);
					out.format(DIM_GREY + " (%.2f)" + RESET, p);
				}
				out.format(" :");
				int nb = 0;
				while (iterator.hasNext() && nb < MAX_PRINT_WORDS)
				{
					Integer ind = iterator.next();
					String word = topic_model.getAlphabet().lookupObject(ind);
					out.format(" %s", word);
					return_s.append(word).append(" ");
					if (!toggle) {
						out.format(DIM_GREY + " (%.3f)" + RESET, topic_model.getProbaWordGivenTopic(ind, topic));
					}
					nb++;
				}
				if (rank)
					list_s[topic_model.get_rank("all", topic)-1] = out.toString();
				else
					list_s[i] = out.toString();
				out.close();
			}
			for (int i=0; i<getModel().numTopics(); i++)
			if (list_s[i] != null)
				System.out.println(list_s[i]);
		/*}
		catch (IndexOutOfBoundsException e)
		{
			System.out.println(RED + "Error: index out of bounds" + RESET);
			return_s = "";
		}*/
		return return_s.toString();
	}
	
	private static String distrib(String s) throws NoModel
	{
		ArrayList<Integer> list_docs = getListNumbers(s, MyDocument.size());
		if (list_docs.size() == 0)
		{
			help("distrib");
			return "";
		}		
		boolean toggle = lookfortag(TOGGLE, s);
		if (toggle) s = s.replace(TOGGLE, "");
		System.out.print("Printing " + LIGHT_GREEN + "top " + MAX_PRINT_TOPICS + RESET + " topics for " + LIGHT_BLUE + "documents " + s.trim() + RESET);
		if (!toggle)
			System.out.println(DIM_GREY + " (with the associated probabilities p(z/d))" + RESET);
		else
			System.out.println();		
		StringBuilder return_s = new StringBuilder();
		for (Integer list_doc : list_docs) {
			int doc = list_doc;
			return_s = new StringBuilder();
			Formatter out = new Formatter(new StringBuilder(), Locale.US);

			// Estimate the topic distribution of the first instance, given the current Gibbs state.
			double[] topicDistribution = getModel().getTopicProbabilities(doc);

			TFSort sort = new TFSort();
			for (int j = 0; j < topicDistribution.length; j++)
				if (topicDistribution[j] > 0)
					sort.add("" + j, topicDistribution[j]);

			out.format(LIGHT_BLUE + "doc %5d" + RESET + " :", doc);

			TreeSet<TermValuePair> list = sort.getList();
			Iterator<TermValuePair> iter = list.iterator();
			int nb = 0;
			while (iter.hasNext() && (nb < MAX_PRINT_TOPICS)) {
				TermValuePair t = iter.next();
				out.format(" %2d", Integer.parseInt(t.getTerm()));
				return_s.append(Integer.parseInt(t.getTerm())).append(" ");
				if (!toggle)
					out.format(DIM_GREY + " (%.3f)" + RESET, t.getValue());
				nb++;
			}
			System.out.println(out);
		}
		return return_s.toString();
	}
	
	private static String quit(String s)
	{
		System.out.println("Bye");
		System.exit(0);
		return "";
	}
	
	private static String proba(String s) throws NoModel
	{
		String[] split = s.split(" ");
		if (split.length < 1)
		{
			help("proba");
			return "";
		}
		String word = split[0];
		if (split.length < 2)
		{
			double p = getModel().getProbaWord(word);
			Formatter out = new Formatter(new StringBuilder(), Locale.US);
			out.format("Probability of word " + YELLOW + "%s" + RESET + " based on the model: %.5f", word, p);
			System.out.println(out.toString());
			out.close();			
		}
		else
		{
			int topic = Integer.parseInt(split[1]);
			double p = getModel().getProbaWordGivenTopic(word, topic);
			Formatter out = new Formatter(new StringBuilder(), Locale.US);
			out.format("Probability of word " + YELLOW + "%s" + RESET + " in topic " + LIGHT_GREEN + "%2d" + RESET + ": %.5f", word, topic, p);
			System.out.println(out.toString());
			out.close();
		}
		return "";
	}
		
	private static String word(String s) throws NoModel
	{
		boolean toggle = lookfortag(TOGGLE, s);
		if (toggle) s = s.replace(TOGGLE, "");
		String[] split = s.split(" ");
		if ((split.length == 0) || ((split.length == 1) && (split[0].equals(""))))
		{
			help("word");
			return "";
		}
		System.out.print("Printing " + LIGHT_GREEN + "top " + MAX_PRINT_TOPICS + RESET + " topics for " + YELLOW + "words " + s.trim() + RESET);
		if (!toggle)
			System.out.println(DIM_GREY + " (with the associated probabilities p(w/z))" + RESET);
		else
			System.out.println();		
		Formatter out = new Formatter(new StringBuilder(), Locale.US);
		StringBuilder return_s = new StringBuilder();
		for (String w : split)
		{
			return_s = new StringBuilder();
			int id_w = getModel().getAlphabet().getIndex(w);//dataAlphabet.lookupIndex(w);
			TFSort sort = new TFSort();
			for (int topic = 0; topic < getModel().numTopics(); topic++)				
				sort.add(""+topic, getModel().getProbaWordGivenTopic(id_w, topic));
			out.format(YELLOW + "word " + w + RESET + "\t:");
			TreeSet<TermValuePair> list = sort.getList();
			Iterator<TermValuePair> iter = list.iterator();
			int nb = 0;
			while (iter.hasNext() && (nb < MAX_PRINT_TOPICS))
			{
				TermValuePair t = iter.next();
				out.format(" %2d", Integer.parseInt(t.getTerm()));
				return_s.append(t.getTerm()).append(" ");
				if (!toggle)
					out.format(DIM_GREY + " (%.3f)" + RESET, t.getValue());
				nb++;
			}
			out.format("\n");
		}
		System.out.print(out);
		return return_s.toString();
	}
	
	private static String namedoc(String s)
	{
		ArrayList<Integer> list_docs = getListNumbers(s, MyDocument.size());
		for (Integer list_doc : list_docs) {
			Formatter out = new Formatter(new StringBuilder(), Locale.US);
			String name = (String) instances.get(list_doc).getName();
			out.format(LIGHT_BLUE + "doc %5d" + RESET + " : %s", list_doc, name);
			System.out.println(out);
		}
		return "";
	}
	
	private static String iddoc(String s)
	{
		System.out.println(s.trim());
		MyDocument doc = MyDocument.get(s.trim());
		int id = doc.getInternalIDForTM();
		Formatter out = new Formatter(new StringBuilder(), Locale.US);
		out.format(LIGHT_BLUE + "doc %s" + RESET + " : %5d", s, id);
		System.out.println(out);
		return "" + id;
	}

	private static String textdoc(String s)
	{		
		return get_meta_data(s, "text", "textdoc");
	}

	private static String tweets(String s)
	{		
		if (LoadDataset.isRawData())
		{
			System.out.println("Printing the raw initial text of " + LIGHT_BLUE + "documents " + s + RESET);
			return get_meta_data(s, "tweets", "tweets");
		}
		else
		{
			System.out.println(RED + "Error: no initial data available" + RESET);
			return "";
		}
	}

	private static String active(String s) throws NoModel
	{
		MonVocabulaire.getIndex().activateTermsUsedInTopics(getModel(), MAX_PRINT_WORDS);
		return "size = " + MonVocabulaire.getActivatedTerms().size();
	}	
	
	private static String titledoc(String s)
	{
		System.out.println("Printing the title(s) of " + LIGHT_BLUE + "documents " + s + RESET);
		return get_meta_data(s, "title", "titledoc");
	}

	private static String datedoc(String s)
	{
		System.out.println("Printing the date(s) of " + LIGHT_BLUE + "documents " + s + RESET);
		return get_meta_data(s, "date", "datedoc");
	}
	
	private static String perdoc(String s)
	{
		System.out.println("Printing the period(s) of " + LIGHT_BLUE + "documents " + s + RESET);
		return get_meta_data(s, "period", "perdoc");
	}
		
	private static String autdoc(String s)
	{
		System.out.println("Printing the author(s) of " + LIGHT_BLUE + "documents " + s + RESET);
		return get_meta_data(s, "author", "autdoc");
	}

	private static String get_meta_data(String s, String meta, String help)
	{		
		ArrayList<Integer> list_docs = getListNumbers(s, MyDocument.size());
		if (list_docs.size() == 0)
		{
			help(help);
			return "";
		}
		String return_s = "";
		for (int i=0; i<list_docs.size(); i++)
		{
			Formatter out = new Formatter(new StringBuilder(), Locale.US);
			String name = (String)instances.get(list_docs.get(i)).getName();
			MyDocument doc = MyDocument.get(name);
			String toprint = "";
			switch(meta)
			{
			case "title":
				toprint = doc.getTitle();
				break;
			case "author":
				toprint = doc.getAuthor();
				break;
			case "date":
				toprint = doc.getDate();
				break;				
			case "period":
				toprint = doc.getPeriod();
				break;				
			case "text":
				toprint = doc.getText();
				break;
			case "tweets":
				toprint = doc.getAuthor() + DIM_GREY + "(" + doc.getNumInitialTexts() + ")" + RESET + "\n" + doc.getInitialTexts();				
				break;
			}
			out.format(LIGHT_BLUE + "doc %5d" + RESET + " : %s", list_docs.get(i), toprint);
			System.out.println(out);
			return_s += list_docs.get(i) + " ";
		}
		return return_s;
	}

	private static ArrayList<String> get_list_terms(String s)
	{
		ArrayList<String> res = new ArrayList<>();
		String[] split = s.trim().toLowerCase().split("\"");
		for (String t : split)
		if (t.trim().length()>0)
		{
			res.add(t.trim());
		}
		return res;
	}
	
	private static String print_list_terms(ArrayList<String> l)
	{
		String res = "";
		for (int i=0; i<l.size(); i++)
		{
			String t = l.get(i);
			ForIndexing f = MonVocabulaire.getIndexTerm(t);
			if (i>0)
				res += " + ";			
			res += YELLOW + l.get(i) + RESET;
			res += DIM_GREY + "(" + f.getNBDocs("all") + ")";
		}
		return res;
	}
	
	private static String grep(String s)
	{
		ArrayList<String> list_t = get_list_terms(s);
		if ((list_t == null) || (list_t.size() ==0))
		{
			System.out.println("You must specify at least one term");
			return "";
		}		
		ForIndexing f = MonVocabulaire.getIndexTerm(list_t.get(0));
		int i = 1;
		while ((f != null) && (i < list_t.size()))
		{
			String t = list_t.get(i);
			ForIndexing f2 = MonVocabulaire.getIndexTerm(list_t.get(i));
			if (f2 != null)
				f = f.intersect(f2);
			else
				f = null;
			i++;
		}
		if (f == null)
		{
			System.out.println("Term not found");
			return "";			
		}
		Formatter out = new Formatter(new StringBuilder(), Locale.US);
		System.out.println("Printing all the doc ID that match the following pattern: " + YELLOW + print_list_terms(list_t) + RESET);
		String return_s = "";
		TreeSet<MyDocument> list = f.getDocs();
		Iterator<MyDocument> iter = list.iterator();
		ArrayList<Integer> id_sorted = new ArrayList<>();
		while (iter.hasNext())
		{
			id_sorted.add(iter.next().getInternalIDForTM());			
		}
		out.format(LIGHT_BLUE + "%d documents\n" + RESET, id_sorted.size());
		Collections.sort(id_sorted);
		for (Integer j : id_sorted)
		{
			out.format("%d ", j);
			return_s += j + " ";
		}		
		System.out.println(out);
		return return_s;
	}

	private static String replace_each_occ(String s, ArrayList<String> l)
	{
		String new_s = s;
		for (int i=0; i<l.size(); i++)
		{
			String t = l.get(i);
			String new_new_s = "";
			int index = new_s.toLowerCase().indexOf(t);
			while (index != -1)
			{
				new_new_s += new_s.substring(0, index) + YELLOW + new_s.substring(index, index + t.length()) + RESET;
				new_s = new_s.substring(index + t.length());
				index = new_s.toLowerCase().indexOf(t);
			}
			new_new_s += new_s;
			new_s = new_new_s;
		}
		return new_s;
	}
	
	private static String greptext(String s)
	{
		ArrayList<String> list_t = get_list_terms(s);
		if ((list_t == null) || (list_t.size() ==0))
		{
			System.out.println("You must specify at least one term");
			return "";
		}		
		ForIndexing f = MonVocabulaire.getIndexTerm(list_t.get(0));
		int i = 1;
		while ((f != null) && (i < list_t.size()))
		{
			String t = list_t.get(i);
			ForIndexing f2 = MonVocabulaire.getIndexTerm(list_t.get(i));
			if (f2 != null)
				f = f.intersect(f2);
			else
				f = null;
			i++;
		}
		if (f == null)
		{
			System.out.println("Term not found");
			return "";			
		}
		Formatter out = new Formatter(new StringBuilder(), Locale.US);
		System.out.println("Printing" + PURPLE + " max " + MAX_PRINT_DOCS + " docs" + RESET + " that match the followin pattern: " + YELLOW + print_list_terms(list_t) + RESET);		
		TreeSet<MyDocument> list = f.getDocs();
		Iterator<MyDocument> iter = list.iterator();
		ArrayList<Integer> id_sorted = new ArrayList<>();
		while (iter.hasNext())
		{
			id_sorted.add(iter.next().getInternalIDForTM());			
		}
		out.format(LIGHT_BLUE + "%d documents\n" + RESET, id_sorted.size());
		Collections.sort(id_sorted);
		int nb = 0;
		String return_s = "";
		for (Integer j : id_sorted)
		{
			return_s += j + " ";
			String name = (String)instances.get(j).getName();
			MyDocument doc = MyDocument.get(name);
			String txt = doc.getText();
			String new_txt = replace_each_occ(txt, list_t);
			out.format(LIGHT_BLUE + "doc %5d " + RESET + " : %s\n", j, new_txt);
			nb++;
			if (nb >= MAX_PRINT_DOCS)
				break;
		}		
		System.out.println(out);
		return return_s;
	}	
	
	private static boolean lookfortag(String tag, String s)
	{
		
		int index = s.indexOf(tag);
		if (index != -1)
		{
			System.out.println("Tag " + tag + " detected");
			return true;
		}
		else
			return false;
	}
	
	private static String topdoc(String s) throws NoModel
	{
		//getModel();
		boolean toggle = lookfortag(TOGGLE, s);
		if (toggle) s = s.replace(TOGGLE, "");
		boolean norm = lookfortag(NORMALIZED, s);
		if (norm) s = s.replace(NORMALIZED, "");		
		ArrayList<Integer> list_topics = getListNumbers(s, getModel().numTopics());
		if (list_topics.size() == 0)
		{
			help("topdoc");
			return "";
		}
		String return_s = "";
		for (int i=0; i<list_topics.size(); i++)
		{
			int topic = list_topics.get(i);
			return_s = "";
			Formatter out = new Formatter(new StringBuilder(), Locale.US);				
			out = new Formatter(new StringBuilder(), Locale.US);
			out.format(LIGHT_GREEN + "topic %3d" + RESET + " : ", topic);
			int nb = 0;
			/*if (norm)
			{*/
				//TFSort list_docs = topic_word.get_pdz(topic); //, false, 50, 0); // 0: get the LDA id
				TFSort list_docs = getModel().getSortedDocuments(0).get(topic);
				Iterator<TermValuePair> iter = list_docs.getList().iterator();
				while ((iter.hasNext()) && (nb < MAX_PRINT_DOCS))
				{
					TermValuePair t = iter.next();
					return_s += t.getTerm() + " ";
					out.format(" %5d", Integer.parseInt(t.getTerm()));
					if (!toggle)
						out.format(DIM_GREY + " (%.3e)" + RESET, t.getValue());
					nb++;
				}
			//}
			System.out.println(out);
		}
		return return_s;
	}
	
	private static String author(String s) throws NoModel
	{
		if (s.length() == 0)
		{
			help("author");
			return "";
		}
		String return_s = "";
		System.out.println("docs for " + LIGHT_GREEN + s + RESET + " : ");
		for (String key : MyDocument.getAllKeys())
		{
			MyDocument doc = MyDocument.get(key);
			if (doc.getAuthor().equalsIgnoreCase(s))
				return_s += doc.getInternalIDForTM() + " ";
		}
		System.out.println(return_s);
		return return_s;
	}	
	
	public static String set(String s)
	{
		String[] split = s.trim().split(" ");
		if (split.length < 2)
		{
			help("set");
			return "";
		}
		String cst = split[0];
		int val = 10;
		try {
			val = Integer.parseInt(split[1]);
		}
		catch (NumberFormatException e)
		{
			System.out.println(RED + "Invalid format: expecting an integer number" + RESET);
			return "";
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			System.out.println("Bad usage of set command, please consult the dedicate help");			
		}
		switch(cst)
		{
		case "maxtopics":
			MAX_PRINT_TOPICS = val;
			System.out.println("MAX number of printed topics set to " + PURPLE + val + RESET);
			break;
		case "maxwords":
			MAX_PRINT_WORDS = val;
			System.out.println("MAX number of printed words set to " + PURPLE + val + RESET);
			break;
		case "maxdocs":
			MAX_PRINT_DOCS = val;
			System.out.println("MAX number of printed docs set to " + PURPLE + val + RESET);
			break;
		case "maxlabels":
			MAX_PRINT_LABELS = val;
			// lazy... I should setup a list of labelers!
			zero_labeler_u.setNBtopterms(val);
			zero_labeler_n.setNBtopterms(val);
			one_labeler.setNBtopterms(val);
			docbased_labeler_u.setNBtopterms(val);
			docbased_labeler_n.setNBtopterms(val);
			//Antoine
			c_labeler_z.setNBtopterms(val);
			c_labeler_t.setNBtopterms(val);
			System.out.println("MAX number of printed labels set to " + PURPLE + val + RESET);
			break;			
		default:
			System.out.println("Unknow constant");
			return "";
		}		
		return "";
	}
		
	private static String export_topics(String s) throws NoModel
	{
		System.out.print("Exporting " + LIGHT_GREEN + getModel().numTopics() + " topics" + RESET);
		String[] split = s.trim().split(" ");
		boolean nop = false;
		String filetype = "csv"; 
		String filename = LoadDataset.getDataName() + "_default_topics.csv";
		for (String w : split)
		if (!w.isEmpty())
		{
			if (w.equals("nop"))
				nop = true;				
			else
			if (w.toLowerCase().endsWith("csv"))
				filename = w;
			else
			if (w.toLowerCase().endsWith("json"))
			{
				filename = w;
				filetype = "json";
			}
		}
		System.out.print(" into " + YELLOW + filename + RESET);
		if (nop)
			System.out.println(" (only " + PURPLE + "top " + MAX_PRINT_WORDS + RESET + " words)");
		else
			System.out.println(" (" + PURPLE + "top " + MAX_PRINT_WORDS + RESET + " words along with probabilities p(w/z))");
		ExportTopics export = new ExportTopics(getModel());
		export.setPrintProba(nop);
		export.setPrintWords(MAX_PRINT_WORDS);
		export.export(filename, filetype);
		return filename;
	}
	
	private static String export_topicwords(String s) throws NoModel
	{
		System.out.print("Exporting " + LIGHT_GREEN + getModel().numTopics() + " topics" + RESET
				+ " (full for Ian)");
		String[] split = s.trim().split(" ");
		boolean nop = false;
		String filetype = "csv"; 
		String filename = LoadDataset.getDataName() + "_default_topicwords.csv";
		for (String w : split)
		if (!w.isEmpty())
		{
			if (w.toLowerCase().endsWith("csv"))
				filename = w;
		}
		System.out.print(" into " + YELLOW + filename + RESET);
		ExportTopicWord export = new ExportTopicWord(getModel());
		export.setPrintProba(nop);
		export.setPrintWords(MAX_PRINT_WORDS);
		export.export(filename, filetype);
		return filename;
	}	

	private static String export_cor(String s) throws NoModel
	{
		getModel();
		System.out.print("Exporting the pairwise correlation");

		String type = "docbased";
		String[] split = cut(s);
		if (split[0].equals("d"))
		{
			type = "docbased";
			s = split[1];
		}
		else
		if (split[0].equals("w"))
		{
			type = "wordbased";
			s = split[1];
		}
		split = s.trim().split(" ");
		String filetype = "csv"; 
		String filename = LoadDataset.getDataName() + "_default_cor_" + type + ".csv";
		for (String w : split)
		if (!w.isEmpty())
		{
			if (w.toLowerCase().endsWith("csv"))
				filename = w;
			else
				if (w.toLowerCase().endsWith("json"))
				{
					filename = w;
					filetype = "json";
				}
		}
		System.out.println(" into " + YELLOW + filename + RESET);
		ExportTopicAnalytics export = new ExportTopicAnalytics(getModel(), type);
		export.export(filename, filetype);
		return filename;
	}

	private static String export_pz(String s) throws NoModel
	{
		getModel();
		System.out.print("Exporting p(z) over time");
		//MonVocabulaire.getIndex().activateTermsUsedInTopics(getModel(), MAX_PRINT_WORDS);
		String[] split = s.trim().split(" ");
		String filetype = "csv"; 
		String filename = LoadDataset.getDataName() + "_default_pz.csv";
		for (String w : split)
		if (!w.isEmpty())
		{
			if (w.toLowerCase().endsWith("csv"))
				filename = w;
			else
			if (w.toLowerCase().endsWith("json"))
			{
				filename = w;
				filetype = "json";
			}
		}
		System.out.print(" into " + YELLOW + filename + RESET);
		ExportPZ export = new ExportPZ(getModel());
		export.export(filename, filetype);
		return filename;
	}

	private static String export_topdocs_temp(String s) throws NoModel
	{
		System.out.print("Exporting " + LIGHT_GREEN + MAX_PRINT_DOCS + " top documents with temporal information" + RESET);
		MonVocabulaire.getIndex().activateTermsUsedInTopics(getModel(), MAX_PRINT_WORDS);
		String[] split = s.trim().split(" ");
		boolean nop = false;		
		String filetype = "csv"; 
		String filename = LoadDataset.getDataName() + "_default_topdocs_temp.csv";
		for (String w : split)
		if (!w.isEmpty())
		{
			if (w.equals("nop"))
				nop = true;				
			else
			if (w.toLowerCase().endsWith("csv"))
				filename = w;
			else
			if (w.toLowerCase().endsWith("json"))
			{
				filename = w;
				filetype = "json";
			}
		}
		System.out.print(" into " + YELLOW + filename + RESET);
		if (nop)
			System.out.println(" (only " + PURPLE + "top " + MAX_PRINT_DOCS + RESET + " documents)");
		else
			System.out.println(" (" + PURPLE + "top " + MAX_PRINT_DOCS + RESET + " docs along with probabilities p(d/z))");
		ExportTopdocsTemp export = new ExportTopdocsTemp(getModel(), instances);
		export.setPrintProba(nop);
		export.setPrintDocs(MAX_PRINT_DOCS);		
		export.export(filename, filetype);
		return filename;
	}	
	
	private static String export_topdocs(String s) throws NoModel
	{		
		System.out.print("Exporting " + LIGHT_GREEN + MAX_PRINT_DOCS + " top documents" + RESET);
		MonVocabulaire.getIndex().activateTermsUsedInTopics(getModel(), MAX_PRINT_WORDS);
		String[] split = s.trim().split(" ");
		boolean nop = false;
		ArrayList<TopicLabelerSkeleton> lab = null;		
		String filetype = "csv"; 
		String filename = LoadDataset.getDataName() + "_default_topdocs.csv";
		boolean pzd = false;
		for (String w : split)
		if (!w.isEmpty())
		{
			if (w.equals("nop"))
				nop = true;				
			else
			if (w.toLowerCase().endsWith("csv"))
				filename = w;
			else
			if (w.toLowerCase().endsWith("json"))
			{
				filename = w;
				filetype = "json";
			}
			else
			if (w.equals("ngrams"))
				lab = all_labelers;
			else
			if (w.equals("pzd"))
				pzd = true;
		}
		System.out.print(" into " + YELLOW + filename + RESET);
		if (nop)
			System.out.println(" (only " + PURPLE + "top " + MAX_PRINT_DOCS + RESET + " documents)");
		else
			System.out.println(" (" + PURPLE + "top " + MAX_PRINT_DOCS + RESET + " docs along with probabilities p(d/z))");
		ExportDocs export = null;
		if (pzd)
			export = new ExportTopdocs_pzd(getModel(), instances);
		else {
			export = new ExportTopdocs(getModel(), instances);
			export.setNgrams(lab);
		}
		export.setPrintProba(nop);
		export.setPrintDocs(MAX_PRINT_DOCS);		
		export.export(filename, filetype);
		return filename;
	}
	
	private static String export_topdocs_between(String s) throws NoModel
	{		
		System.out.print("Exporting " + LIGHT_GREEN + MAX_PRINT_DOCS + " top documents" + RESET + " for topic pairs");
		MonVocabulaire.getIndex().activateTermsUsedInTopics(getModel(), MAX_PRINT_WORDS);
		String[] split = s.trim().split(" ");
		boolean nop = false;
		String filetype = "csv"; 
		String filename = LoadDataset.getDataName() + "_default_topdocs_between.csv";
		for (String w : split)
		if (!w.isEmpty())
		{
			if (w.equals("nop"))
				nop = true;				
			else
			if (w.toLowerCase().endsWith("csv"))
				filename = w;
			else
			if (w.toLowerCase().endsWith("json"))
			{
				filename = w;
				filetype = "json";
			}
		}
		System.out.print(" into " + YELLOW + filename + RESET);
		if (nop)
			System.out.println(" (only " + PURPLE + "top " + MAX_PRINT_DOCS + RESET + " documents)");
		else
			System.out.println(" (" + PURPLE + "top " + MAX_PRINT_DOCS + RESET + " docs along with probabilities p(d/z))");
		ExportTopdocsBetween export = new ExportTopdocsBetween(getModel(), instances);
		export.setPrintProba(nop);
		export.setPrintDocs(MAX_PRINT_DOCS);
		export.setPrintTopics(MAX_PRINT_TOPICS);	
		export.export(filename, filetype);
		return filename;
	}
	
	private static String export_toptopics(String s) throws NoModel
	{		
		System.out.print("Exporting the top topics for every docs ");
		String[] split = s.trim().split(" ");
		boolean nop = false;
		String filetype = "csv"; 
		String filename = LoadDataset.getDataName() + "_default_toptopics.csv";
		for (String w : split)
		if (!w.isEmpty())
		{
			if (w.equals("nop"))
				nop = true;				
			else
			if (w.toLowerCase().endsWith("csv"))
				filename = w;
			else
			if (w.toLowerCase().endsWith("json"))
			{
				filename = w;
				filetype = "json";
			}
		}
		System.out.print(" into " + YELLOW + filename + RESET);
		ExportTopMaxForDocs export = new ExportTopMaxForDocs(getModel(), instances);	
		export.export(filename, filetype);
		return filename;
	}	
		
	private static String export_distrib(String s) throws NoModel
	{
		String[] split = s.trim().split(" ");
		int top_topics = -1;
		String filetype = "csv"; 
		String filename = LoadDataset.getDataName() + "_default_distrib_docs.csv";
		for (String w : split)
		if (!w.isEmpty())
		{
			if (w.equals("top"))
				top_topics = MAX_PRINT_TOPICS;
			else
				if (w.toLowerCase().endsWith("csv"))
					filename = w;
				else
				if (w.toLowerCase().endsWith("json"))
				{
					filename = w;
					filetype = "json";
				}
		}
		if (top_topics != -1)
			System.out.print("Exporting top " + LIGHT_GREEN + MAX_PRINT_TOPICS + RESET + " topics for each document");
		else
			System.out.print("Exporting the whole document x topic distribution");
		System.out.println(" into " + YELLOW + filename + RESET);
		ExportDistribDocs export = new ExportDistribDocs(getModel(), instances, top_topics);
		export.export(filename, filetype);
		return filename;
	}	
	
	private static String export_labels(String s) throws NoModel
	{
		System.out.print("Exporting the labels");
		String[] split = s.trim().split(" ");
		boolean nop = false;
		String filetype = "csv"; 
		String filename = LoadDataset.getDataName() + "_default_labels.csv";
		for (String w : split)
		if (!w.isEmpty())
		{
			if (w.equals("nop"))
				nop = true;				
			else
			if (w.toLowerCase().endsWith("csv"))
				filename = w;
			else
			if (w.toLowerCase().endsWith("json"))
			{
				filename = w;
				filetype = "json";
			}
		}
		System.out.print(" into " + YELLOW + filename + RESET);
		if (nop)
			System.out.println(" (only " + PURPLE + "top " + MAX_PRINT_LABELS + RESET + " labels)");
		else
			System.out.println(" (" + PURPLE + "top " + MAX_PRINT_LABELS + RESET + " labels along with scores)");
		ExportLabels export = new ExportLabels(getModel().numTopics(), all_labelers);
		export.setPrintProba(nop);
		export.export(filename, filetype);
		return filename;
	}	
	private static String export_topic_quality(String s) throws NoModel
	{
		getModel();
		System.out.print("Exporting the topic quality measures");
		String[] split = s.trim().split(" ");
		String filetype = "csv"; 
		String filename = LoadDataset.getDataName() + "_default_topic_qual.csv";
		for (String w : split)
		if (!w.isEmpty())
		{
			if (w.toLowerCase().endsWith("csv"))
				filename = w;
			else
				if (w.toLowerCase().endsWith("json"))
				{
					filename = w;
					filetype = "json";
				}
		}
		System.out.println(" into " + YELLOW + filename + RESET);
		ExportTopicQuality export = new ExportTopicQuality(getModel().numTopics());
		export.export(filename, filetype);
		return filename;
	}
	
	private static String[] cut(String s)
	{
		String[] split = s.split(" ");
		String[] out = new String[2];
		boolean first = true;
		String concat = "";
		out[0] = "";
		for (int i=0; i<split.length; i++)
		{
			if (!split[i].isEmpty())
			{
				if (first)
				{
					out[0] = split[i];
					first = false;
				}
				else
					concat = concat + split[i] + " ";				
			}
		}
		out[1] = concat.trim();
		return out;
	}
	
	public static String export(String s) throws NoModel
	{		
		String path_server = "WebServer" + Constantes.separateur + "datasets" + Constantes.separateur + LoadDataset.getDataName();
		String[] split = cut(s); 		
		switch (split[0].toLowerCase())
		{
		case "topics":
			Export.setPath(path_server + Constantes.separateur + "models");
			return export_topics(split[1]);
		case "vocab":
			Export.setPath(path_server + Constantes.separateur + "vocab");
			return export_vocab(split[1]);
		case "labels":
			Export.setPath(path_server + Constantes.separateur + "labels");
			return export_labels(split[1]);
		case "topdocs":
			Export.setPath(path_server + Constantes.separateur + "models");
			return export_topdocs(split[1]);
		case "topdocstemp":
			Export.setPath(path_server + Constantes.separateur + "models");
			return export_topdocs_temp(split[1]);
		case "qual":
			Export.setPath(path_server + Constantes.separateur + "analytics");
			return export_topic_quality(split[1]);
		case "cor":
			Export.setPath(path_server + Constantes.separateur + "analytics");
			return export_cor(split[1]);
		case "pz":
			Export.setPath(path_server + Constantes.separateur + "models");
			return export_pz(split[1]);
		case "distrib":
			Export.setPath(path_server + Constantes.separateur + "models");
			return export_distrib(split[1]);
		case "docbetween":			
			Export.setPath(path_server + Constantes.separateur + "models");
			return export_topdocs_between(split[1]);
		case "toptopics":			
			Export.setPath(path_server + Constantes.separateur + "carathove");
			return export_toptopics(split[1]);
		case "topicwords":
			Export.setPath(path_server + Constantes.separateur + "models");
			return export_topicwords(split[1]);			
		default:
			help("export");
		}
		return "";
	}
	
	/* export the full list of vocabulary, activated or not */
	private static String export_vocab(String s)
	{
		ArrayList<ForIndexing> list = MonVocabulaire.getActivatedTerms();
		try {
			new ExportVocab("vocab.txt").export(list);
		} catch (IOException e) {
			System.out.println(RED + "Error: impossible to write into file vocab.txt");
		}
		return "";
	}
	
	private static String help(String s) {
		//String[] split = s.trim().split(" ");
		//String rest = s.substring(s.indexOf("help") + "help".length());
		boolean general = false;
		if (!s.equals(""))
		{
			switch(s)
			{
			case "topic":
				System.out.println("-------------");
				System.out.println(YELLOW + "topic" + RESET + " : give the " + PURPLE + "top " + MAX_PRINT_WORDS + " words" + RESET + " of a given list of " + LIGHT_GREEN + "topics" + RESET);
				System.out.println("-------------");
				System.out.println("For instance:");
				System.out.println(DIM_GREY + "> topic 1");
				System.out.println("> topic 1 2 5 10");
				System.out.println("> topic 1:5" + RESET);
				System.out.println("The" + LIGHT_BLUE + " option nop" + RESET + " can be used to hide the probabilities");
				System.out.println(DIM_GREY + "> topic 1:5 10 nop" + RESET);
				System.out.println("The" + LIGHT_BLUE + " option sort" + RESET + " can be used to sort the topics by decreasing probability p(z)");
				System.out.println(DIM_GREY + "> topic 1:5 10 sort" + RESET);
				System.out.println(DIM_GREY + "> topic 0:199 sort nop" + RESET);
				System.out.println("-------------");
				break;
			case "set":
				System.out.println("-------------");
				System.out.println(YELLOW + "set\t" + RESET + " : set values for the max number of printed topics, documents, words or labels");
				System.out.println("-------------");
				System.out.println("For instance:");
				System.out.println(DIM_GREY + "> set maxtopics 5");
				System.out.println("> set maxdocs 12");
				System.out.println("> set maxwords 30");
				System.out.println("> set maxlabels 5" + RESET);
				//System.out.println("At the beginning the default value is 10");
				System.out.println("-------------");
				break;
			case "export":
				System.out.println("-------------");
				System.out.println(YELLOW + "export\t" + RESET + " : export the model to CSV files");
				System.out.println("-------------");
				System.out.println("For instance:");
				System.out.println(DIM_GREY + "> export topics" + RESET);
				System.out.println("The default export filename is default.csv, clever isn't it?");
				System.out.println(DIM_GREY + "> export topics myname.csv");
				System.out.println("> export topics anothername.csv nop" + RESET);
				System.out.println("The " + LIGHT_BLUE + "option nop" + RESET + " is for hiding the probabilities if you don't like them");
				System.out.println("Other export will be available... soon...");
				System.out.println("-------------");
				break;
			case "load":
				System.out.println("-------------");
				System.out.println(YELLOW + "load\t" + RESET + " : load the dataset, the topic model and/or the labeling");
				System.out.println("-------------");
				System.out.println("For instance:");
				System.out.println(DIM_GREY + "> load data config.HP.full" + RESET);
				System.out.println(DIM_GREY + "> load data config.ASOIAF" + RESET);
				System.out.println("The second argument is a configuration file that is in the " + LIGHT_BLUE + "LDA/" + RESET + " folder");				
				System.out.println(DIM_GREY + "> load topics 2016_04_30" + RESET);
				System.out.println("Here you load the topic models that are in " + LIGHT_BLUE + "resultats/yourdata/models/2016_04_30" + RESET);
				System.out.println(DIM_GREY + "> load labels 2016_04_30" + RESET);
				System.out.println("Here you load the topic labeling that are in " + LIGHT_BLUE + "resultats/yourdata/labeling/2016_04_30" + RESET);
				System.out.println(DIM_GREY + "> load all 2016_04_30" + RESET);
				System.out.println("Here you load both the topic models and labeling that are in " + LIGHT_BLUE + "resultats/yourdata/.../2016_04_30" + RESET);				
				System.out.println("-------------");
				break;								
			case "load topics":
				System.out.println("-------------");
				System.out.println(YELLOW + "load topics\t" + RESET + " : load the output of a topic model from either a single file or a directory (multiple models, soon taken into account)");
				System.out.println("-------------");
				System.out.println("For instance:");
				System.out.println(DIM_GREY + "> load topics whatever.model" + RESET);
				System.out.println("For a single file, take care that the suffix is " + PURPLE + ".model" + RESET);				
				System.out.println(DIM_GREY + "> load topics 2016_04_30" + RESET);
				System.out.println("If you specify a directory, it must contain at least one " + PURPLE + ".model" + RESET + " file");
				System.out.println("-------------");
				break;
			case "load labels":
				System.out.println("-------------");
				System.out.println(YELLOW + "load labels\t" + RESET + " : load the topic labeling from a directory");
				System.out.println("-------------");
				System.out.println("For instance:");
				System.out.println(DIM_GREY + "> load labels 2016_04_30" + RESET);
				System.out.println("-------------");
				break;				
			case "load data":
				System.out.println("-------------");
				System.out.println(YELLOW + "load data\t" + RESET + " : load dataset");
				System.out.println("-------------");
				System.out.println("For instance:");
				System.out.println(DIM_GREY + "> load data config.HP.full" + RESET);
				System.out.println(DIM_GREY + "> load data config.ASOIAF" + RESET);				
				System.out.println("-------------");
				break;								
			case "topdoc":
				System.out.println("-------------");
				System.out.println(YELLOW + "topdoc\t" + RESET + " : print the " + PURPLE + "top " + MAX_PRINT_DOCS + " documents" + RESET + " for a given set of topics, here based on p(z/d)");
				System.out.println("-------------");
				System.out.println("For instance:");
				System.out.println(DIM_GREY + "> topdoc 0:199" + RESET);
				System.out.println(DIM_GREY + "> topdoc 0 1 5 10:12" + RESET);
				System.out.println("The" + LIGHT_BLUE + " option nop" + RESET + " can be used to hide the probabilities");
				System.out.println(DIM_GREY + "> topdoc 10 12 14 nop" + RESET);
				break;
			case "distrib":
				System.out.println("-------------");
				System.out.println(YELLOW + "distrib\t" + RESET + " : print the " + PURPLE + "top " + MAX_PRINT_TOPICS + " topics" + RESET + "for a given set of topics, here based on p(z/d)");
				System.out.println("-------------");
				System.out.println("For instance:");
				System.out.println(DIM_GREY + "> distrib 12876" + RESET);
				System.out.println(DIM_GREY + "> distrib 12876 12878 15171" + RESET);
				System.out.println(DIM_GREY + "> distrib 1000:1005" + RESET);
				System.out.println("The" + LIGHT_BLUE + " option nop" + RESET + " can be used to hide the probabilities");
				System.out.println(DIM_GREY + "> distrib 12876 nop" + RESET);
				break;			
			case "labels":
				System.out.println("-------------");
				System.out.println(YELLOW + "labels\t" + RESET + " : print the " + PURPLE + "top " + MAX_PRINT_LABELS + " labels" + RESET + " for a given set of topics");
				System.out.println("-------------");
				System.out.println("For instance:");
				System.out.println(DIM_GREY + "> labels 0u 0" + RESET);
				System.out.println(DIM_GREY + "> labels 0n 0:19" + RESET);
				System.out.println(DIM_GREY + "> labels dn 0 2 8 10" + RESET);
				System.out.println(DIM_GREY + "> labels all 0:199" + RESET);
				System.out.println("You have to specify what kind of labels you want to print, among: "
						+ LIGHT_BLUE + "0u" + RESET + " (0-order unnormalized), "
						+ LIGHT_BLUE + "0n" + RESET + " (0-order normalized), "
						+ LIGHT_BLUE +  "1" + RESET + " (1-order), "
						+ LIGHT_BLUE + "du" + RESET + " (doc-based unnormalized), "
						+ LIGHT_BLUE + "dn" + RESET + " (doc-based normalized), "
						+ LIGHT_BLUE + "all" + RESET + " all of the above");
				System.out.println("The" + LIGHT_BLUE + " option nop" + RESET + " can be used to hide the weights");
				System.out.println(DIM_GREY + "> labels 1 1:99 nop" + RESET);
				break;
			case "pz":
				System.out.println("-------------");
				System.out.println(YELLOW + "pz\t" + RESET + " : print the temporal distribution of topics and the sum over all periods");
				System.out.println("-------------");
				System.out.println("For instance:");
				System.out.println(DIM_GREY + "> pz 17" + RESET);
				System.out.println(DIM_GREY + "> pz 0:99" + RESET);
				System.out.println("I strongly suggest to use the " + LIGHT_BLUE + "option norm" + RESET + " in order to erase the size effect");
				System.out.println(DIM_GREY + "> pz 0:199 norm" + RESET);
				System.out.println("You can use the " + LIGHT_BLUE + "option rank" + RESET + " for getting the topic ranking (incompatible with norm)");
				System.out.println(DIM_GREY + "> pz 0:199 rank" + RESET);				
				break;
			case "stats":
				System.out.println("-------------");
				System.out.println(YELLOW + "stats\t" + RESET + " : give basic statistics on the corpus");
				break;
			case "doc":
				System.out.println("No help available yet");
				break;
			case "grep":
				System.out.println("-------------");
				System.out.println(YELLOW + "grep\t" + RESET + " : print the " + PURPLE + "top " + MAX_PRINT_LABELS + "documents" + RESET + " ID that fit a term-based query");
				System.out.println("-------------");
				System.out.println("For instance:");
				System.out.println(DIM_GREY + "> grep \"harry potter\" \"wands\"" + RESET);
				System.out.println(DIM_GREY + "> grep quill" + RESET);
				break;
			case "greptext":
				System.out.println("-------------");
				System.out.println(YELLOW + "greptext\t" + RESET + " : print the " + PURPLE + "top " + MAX_PRINT_LABELS + "documents" + RESET + " textual content that fit a term-based query");
				System.out.println("-------------");
				System.out.println("For instance:");
				System.out.println(DIM_GREY + "> greptext \"harry potter\" \"wands\"" + RESET);
				System.out.println(DIM_GREY + "> greptext quill" + RESET);
				break;
			case "top":
				System.out.println("-------------");
				System.out.println(YELLOW + "top\t" + RESET + " : give the " + PURPLE + "top " + MAX_PRINT_TOPICS + " topics" + RESET + " or the " + PURPLE + "top " + MAX_PRINT_WORDS + " words" + RESET );
				System.out.println("-------------");
				System.out.println("For instance:");
				System.out.println(DIM_GREY + "> top topics" + RESET);
				System.out.println(DIM_GREY + "> top words" + RESET);
				break;
			case "quit":
				System.out.println("Really?");
				break;
			case "word":
				System.out.println("No help available yet");
				break;				
			default:
				general = true;
			}
		}
		else
			general = true;
		if (general)
		{
			System.out.println(YELLOW + "load\t" + RESET + " : load the dataset, the topic model and/or the labeling");
			System.out.println(YELLOW + "topic\t" + RESET + " : give the " + YELLOW + "top " + MAX_PRINT_WORDS + " words" + RESET + " of a given list of " + LIGHT_GREEN + "topics" + RESET);
			System.out.println(YELLOW + "topdoc\t" + RESET + " : give the " + LIGHT_BLUE + "top " + MAX_PRINT_DOCS + " documents" + RESET + " of a given list of " + LIGHT_GREEN + "topics" + RESET);
			System.out.println(YELLOW + "distrib\t" + RESET + " : give the " + LIGHT_GREEN + "top " + MAX_PRINT_TOPICS + " topics" + RESET + " of a given list of " + LIGHT_BLUE + "documents" + RESET);
			System.out.println(YELLOW + "doc\t" + RESET + " : give the list of tokens describing a given list of " + LIGHT_BLUE + "documents" + RESET + " (possibly with the attributed topics)");
			System.out.println(YELLOW + "textdoc\t" + RESET + " : give the raw text associated to a given list of " + LIGHT_BLUE + "documents" + RESET);
			System.out.println(YELLOW + "word\t" + RESET + " : give the " + LIGHT_GREEN + "top " + MAX_PRINT_TOPICS + " topics" + RESET + " of a given list of " + YELLOW + "words" + RESET); 
			System.out.println(YELLOW + "grep\t" + RESET + " : give the entire list of documents ID that contain the given (already indexed) " + YELLOW + "term" + RESET);
			System.out.println(YELLOW + "greptext" + RESET + " : give the raw text associated to max " + MAX_PRINT_DOCS + " documents indexed by the given " + YELLOW + "term" + RESET);
			System.out.println(YELLOW + "set\t" + RESET + " : set values for the max number of printed topics, documents or words");
			System.out.println(YELLOW + "export\t" + RESET + " : export the model to CSV files");
			System.out.println(YELLOW + "labels\t" + RESET + " : print the " + PURPLE + "top " + MAX_PRINT_LABELS + " labels" + RESET + " for a given set of topics");
			System.out.println(YELLOW + "pz\t" + RESET + " : print the temporal distribution of topics and the sum over all periods");
			System.out.println(YELLOW + "stats\t" + RESET + " : give basic statistics on the corpus");
			System.out.println(YELLOW + "top\t" + RESET + " : give the " + PURPLE + "top " + MAX_PRINT_TOPICS + " topics" + RESET + " or the " + PURPLE + "top " + MAX_PRINT_WORDS + " words" + RESET );
			System.out.println(YELLOW + "cor\t" + RESET + " : give the " + PURPLE + "top " + MAX_PRINT_TOPICS + " topics" + RESET + " most correlated to a given list of " + LIGHT_GREEN + "topics" + RESET);
			System.out.println(YELLOW + "quit\t" + RESET + " : please, make a guess ;)"); 
		}
		return "";
	}
	
	private static String ans(String s)
	{
		System.out.println(DIM_GREY + "ans = " + RESET + ans_map.get("ans"));
		return "";
	}
	
	// shortcuts
		
	private static String load_labeling(String s)
	{
		if (LoadDataset.getDataName().isEmpty())
		{
			System.out.println(RED + "Load a dataset first" + RESET);
			return help("load data");
		}
		if (s.equals(""))
		{
			System.out.println(RED + "Expecting a labeling folder" + RESET);
			help("load labels");
			System.out.println("Available labeling folders:");
	    	String foldpath = LoadDataset.getPath() + Constantes.separateur + Constantes.RESULT_DIR + Constantes.separateur + LoadDataset.getDataName() + Constantes.separateur + "labels";
	    	String return_s = "";
			CommonsUtils.getContentOfFolder(foldpath,true,false).forEach(System.out::println);
	    	return return_s;
		}


		String dir = s.trim();
		all_labelers = new ArrayList<>();

		try {			
			sentence_based_labeler = new SentenceBasedLabeler(getModel(),instances, dir,10);
			System.out.println("Labeling loaded from " + YELLOW + sentence_based_labeler.getName() + ".label" + RESET);
			sentence_based_labeler.import_labels();
			sentence_based_labeler.setNBtopterms(MAX_PRINT_LABELS);
			all_labelers.add(sentence_based_labeler);
		}
		catch (Exception e)
		{
			//e.printStackTrace();
			System.out.println(RED + "Impossible to load the labels" + RESET);
			sentence_based_labeler = null;
		}
		try {			
			labeler11 = new  Sentence_Based_3(topic_model, instances, dir, 15,"TD_10",0.1);
			System.out.println("Labeling loaded from " + YELLOW + sentence_based_labeler.getName() + ".label" + RESET);
			labeler11.import_labels();
			labeler11.setNBtopterms(MAX_PRINT_LABELS);
			all_labelers.add(labeler11);
		}
		catch (Exception e)
		{
			//e.printStackTrace();
			System.out.println(RED + "Impossible to load the labels" + RESET);
			labeler11 = null;
		}
		try {			
			labeler12 = new  Sentence_Based_3(topic_model, instances,dir, 15,"TD_20",0.1);
			System.out.println("Labeling loaded from " + YELLOW + sentence_based_labeler.getName() + ".label" + RESET);
			labeler12.import_labels();
			labeler12.setNBtopterms(MAX_PRINT_LABELS);
			all_labelers.add(labeler12);
		}
		catch (Exception e)
		{
			//e.printStackTrace();
			System.out.println(RED + "Impossible to load the labels" + RESET);
			labeler12 = null;
		}
		try {			
			labeler13 = new  Sentence_Based_3(topic_model, instances,dir, 15,"TD_10",10);
			System.out.println("Labeling loaded from " + YELLOW + sentence_based_labeler.getName() + ".label" + RESET);
			labeler13.import_labels();
			labeler13.setNBtopterms(MAX_PRINT_LABELS);
			all_labelers.add(labeler13);
		}
		catch (Exception e)
		{
			//e.printStackTrace();
			System.out.println(RED + "Impossible to load the labels" + RESET);
			labeler13 = null;
		}
		try {			
			labeler14 = new  Sentence_Based_3(topic_model, instances,dir, 15,"TD_20",10);
			System.out.println("Labeling loaded from " + YELLOW + sentence_based_labeler.getName() + ".label" + RESET);
			labeler14.import_labels();
			labeler14.setNBtopterms(MAX_PRINT_LABELS);
			all_labelers.add(labeler14);
		}
		catch (Exception e)
		{
			//e.printStackTrace();
			System.out.println(RED + "Impossible to load the labels" + RESET);
			labeler14 = null;
		}
		try {			
			labeler15 = new  Sentence_Based_3(topic_model, instances,dir, 15,"TD_10",1000);
			System.out.println("Labeling loaded from " + YELLOW + sentence_based_labeler.getName() + ".label" + RESET);
			labeler15.import_labels();
			labeler15.setNBtopterms(MAX_PRINT_LABELS);
			all_labelers.add(labeler15);
		}
		catch (Exception e)
		{
			//e.printStackTrace();
			System.out.println(RED + "Impossible to load the labels" + RESET);
			labeler15 = null;
		}
		try {			
			labeler16 = new  Sentence_Based_3(topic_model, instances,dir, 15,"TD_20",1000);
			System.out.println("Labeling loaded from " + YELLOW + sentence_based_labeler.getName() + ".label" + RESET);
			labeler16.import_labels();
			labeler16.setNBtopterms(MAX_PRINT_LABELS);
			all_labelers.add(labeler16);
		}
		catch (Exception e)
		{
			//e.printStackTrace();
			System.out.println(RED + "Impossible to load the labels" + RESET);
			labeler16 = null;
		}
		try {			
			labeler17 = new Sentence_Based_3(topic_model, instances,dir, 15,"COS_0",1000);
			System.out.println("Labeling loaded from " + YELLOW + sentence_based_labeler.getName() + ".label" + RESET);
			labeler17.import_labels();
			labeler17.setNBtopterms(MAX_PRINT_LABELS);
			all_labelers.add(labeler17);
		}
		catch (Exception e)
		{
			//e.printStackTrace();
			System.out.println(RED + "Impossible to load the labels" + RESET);
			labeler17 = null;
		}
		try {			
			labeler18 = new Sentence_Based_3(topic_model, instances,dir, 15,"COS_1",1000);
			System.out.println("Labeling loaded from " + YELLOW + sentence_based_labeler.getName() + ".label" + RESET);
			labeler18.import_labels();
			labeler18.setNBtopterms(MAX_PRINT_LABELS);
			all_labelers.add(labeler18);
		}
		catch (Exception e)
		{
			//e.printStackTrace();
			System.out.println(RED + "Impossible to load the labels" + RESET);
			labeler18 = null;
		}
		try {			
			labeler19 = new Sentence_Based_3(topic_model, instances, dir, 20,"BS_0",0);
			System.out.println("Labeling loaded from " + YELLOW + sentence_based_labeler.getName() + ".label" + RESET);
			labeler19.import_labels();
			labeler19.setNBtopterms(MAX_PRINT_LABELS);
			all_labelers.add(labeler19);
		}
		catch (Exception e)
		{
			//e.printStackTrace();
			System.out.println(RED + "Impossible to load the labels" + RESET);
			labeler19 = null;
		}
		
		try {
			zero_labeler_u = new ZeroOrderLabeler(getModel(), dir);
			zero_labeler_u.setNorm(ZeroOrderLabeler.EVEN_NORM);
			System.out.println("Labeling loaded from " + YELLOW + zero_labeler_u.getName() + ".label" + RESET);
			zero_labeler_u.import_labels();
			zero_labeler_u.setNBtopterms(MAX_PRINT_LABELS);
			all_labelers.add(zero_labeler_u);
		}
		catch (Exception e)
		{
			//e.printStackTrace();
			System.out.println(RED + "Impossible to load the labels" + RESET);
			zero_labeler_u = null;
		}

		try {			
			zero_labeler_n = new ZeroOrderLabeler(getModel(), dir);
			zero_labeler_n.setNorm(ZeroOrderLabeler.FREQ_NORM);
			System.out.println("Labeling loaded from " + YELLOW + zero_labeler_n.getName() + ".label" + RESET);
			zero_labeler_n.import_labels();
			zero_labeler_n.setNBtopterms(MAX_PRINT_LABELS);
			all_labelers.add(zero_labeler_n);
		}
		catch (Exception e)
		{
			//e.printStackTrace();
			System.out.println(RED + "Impossible to load the labels" + RESET);
			zero_labeler_n = null;
		}

		try {	
			one_labeler = new OneOrderLabeler(getModel(), dir);
			System.out.println("Labeling loaded from " + YELLOW + one_labeler.getName() + ".label" + RESET);
			one_labeler.import_labels();
			one_labeler.setNBtopterms(MAX_PRINT_LABELS);
			all_labelers.add(one_labeler);
		}
		catch (Exception e)
		{
			System.out.println(RED + "Impossible to load the labels" + RESET);
			one_labeler = null;
		}			
			
		try {			
			docbased_labeler_u = new DocBasedLabeler(getModel(), dir);
			docbased_labeler_u.setNorm(DocBasedLabeler.EVEN_NORM);
			System.out.println("Labeling loaded from " + YELLOW + docbased_labeler_u.getName() + ".label" + RESET);
			docbased_labeler_u.import_labels();
			docbased_labeler_u.setNBtopterms(MAX_PRINT_LABELS);
			all_labelers.add(docbased_labeler_u);
		}
		catch (Exception e)
		{
			System.out.println(RED + "Impossible to load the labels" + RESET);
			docbased_labeler_u = null;
		}

		try {	
			docbased_labeler_n = new DocBasedLabeler(getModel(), dir);
			docbased_labeler_n.setNorm(DocBasedLabeler.FREQ_NORM);
			System.out.println("Labeling loaded from " + YELLOW + docbased_labeler_n.getName() + ".label" + RESET);
			docbased_labeler_n.import_labels();
			docbased_labeler_n.setNBtopterms(MAX_PRINT_LABELS);
			all_labelers.add(docbased_labeler_n);
		}
		catch (Exception e)
		{
			System.out.println(RED + "Impossible to load the labels" + RESET);
			docbased_labeler_n = null;
		}

		try {	
			clustering_labeler = new ClusteringLabeler(getModel(), dir);
			clustering_labeler.setAlpha(10.0);
			System.out.println("Labeling loaded from " + YELLOW + clustering_labeler.getName() + ".label" + RESET);
			clustering_labeler.import_labels();
			clustering_labeler.setNBtopterms(MAX_PRINT_LABELS);
			all_labelers.add(clustering_labeler);
		}
		catch (Exception e)
		{
			System.out.println(RED + "Impossible to load the labels" + RESET);
			clustering_labeler = null;

		}

		try {	
			clustering_labeler_hdp = new ClusteringLabeler2(getModel(), dir, instances);
			clustering_labeler_hdp.setEta(0.00001);
			clustering_labeler_hdp.setGamma(0.00001);
			clustering_labeler_hdp.setNbTopDoc(20);
			clustering_labeler_hdp.setMaxIterations(1000);
			System.out.println("Labeling loaded from " + YELLOW + clustering_labeler_hdp.getName() + ".label" + RESET);
			clustering_labeler_hdp.import_labels();
			clustering_labeler_hdp.setNBtopterms(MAX_PRINT_LABELS);
			all_labelers.add(clustering_labeler_hdp);
		}
		catch (Exception e)
		{
			System.out.println(RED + "Impossible to load the labels" + RESET);
			clustering_labeler = null;
		}

		try {			
			c_labeler_z = new COrderLabeler(getModel(), dir);
			c_labeler_z.setNorm(COrderLabeler.ZERO_NORM);
			System.out.println("Labeling loaded from " + YELLOW + c_labeler_z.getName() + ".label" + RESET);
			c_labeler_z.import_labels();
			c_labeler_z.setNBtopterms(MAX_PRINT_LABELS);
			all_labelers.add(c_labeler_z);
		}
		catch (Exception e)
		{
			//e.printStackTrace();
			System.out.println(RED + "Impossible to load the labels" + RESET);
			c_labeler_z = null;
		}
		try {			
			c_labeler_t = new COrderLabeler(getModel(), dir);
			c_labeler_t.setNorm(COrderLabeler.TERM_NORM);
			System.out.println("Labeling loaded from " + YELLOW + c_labeler_t.getName() + ".label" + RESET);
			c_labeler_t.import_labels();
			c_labeler_t.setNBtopterms(MAX_PRINT_LABELS);
			all_labelers.add(c_labeler_t);
		}
		catch (Exception e)
		{
			//e.printStackTrace();
			System.out.println(RED + "Impossible to load the labels" + RESET);
			c_labeler_t = null;
		}
		return "";
	}
	
	private static String print_labels(String s, TopicLabelerSkeleton labeler) throws NoModel
	{
		if (labeler == null)
		{
			System.out.println(RED + "Load the labeling first" + RESET);
			return load("labels");
		}		
		boolean toggle = lookfortag(TOGGLE, s);
		if (toggle) s = s.replace(TOGGLE, "");
		if (labeler instanceof ClusteringLabeler)
			toggle = true;
		System.out.print("Printing the list of " + labeler.getName() + " labels for " + LIGHT_GREEN + "topics" + s + RESET);
		if (!toggle)
			System.out.println(DIM_GREY + " (with attributed weights)" + RESET);
		else
			System.out.println();
		ArrayList<Integer> list_docs = getListNumbers(s, getModel().numTopics());
		if (list_docs.size() == 0) 
		{
			help("labels");
			return "";
		}				
		Formatter out = new Formatter(new StringBuilder(), Locale.US);
		for (int i : list_docs)
		{
			out.format(LIGHT_GREEN + "topic %3d (" + labeler.getShortName() + ")" + RESET + " :\t", i);
			out.format("%s\n", labeler.getLabel(i, !toggle));
		}
		System.out.print(out.toString());
		out.close();
		return "";
	}
	
	private static String getlabels(TopicLabelerSkeleton labeler, int i, boolean toggle)
	{
		if (labeler == null)
		{
			return "not available";
		}
		return labeler.getLabel(i, !toggle);
	}

	private static String print_labels_all(String s) throws NoModel
	{
		boolean toggle = lookfortag(TOGGLE, s);
		if (toggle) s = s.replace(TOGGLE, "");
		System.out.print("Printing the list of (all) zero labels for " + LIGHT_GREEN + "topics " + s + RESET);
		if (!toggle)
			System.out.println(DIM_GREY + " (with attributed weights)" + RESET);
		else
			System.out.println();
		ArrayList<Integer> list_docs = getListNumbers(s, getModel().numTopics());
		if (list_docs.size() == 0)
		{
			help("labels");
			return "";
		}								
		Formatter out = new Formatter(new StringBuilder(), Locale.US);
		for (int i : list_docs)
		{
			for (TopicLabelerSkeleton labeler : all_labelers)
			{
				out.format(LIGHT_GREEN + "topic %3d (" + labeler.getShortName() + ")" + RESET + " :\t", i);
				out.format("%s\n", getlabels(labeler, i, toggle));
			}
		}
		System.out.print(out.toString());
		out.close();
		return "";
	}
	
	private static String labels(String s) throws NoModel
	{
		String[] split = s.split(" ");
		if (s.equals("") || (split.length <= 1))
		{
			help("labels");
			return "";
		}
		String rest = s.substring(s.indexOf(" ") + 1, s.length());
		if (split[0].equals("all"))
			return print_labels_all(rest);
		for (TopicLabelerSkeleton labeler : all_labelers)
		{
			if (split[0].equals(labeler.getShortName()))
				return print_labels(rest, labeler);
		}
		help("labels");
		return "";
	}
	
	private static String top_words(String s)
	{
		boolean toggle = lookfortag(TOGGLE, s);
		if (toggle) s = s.replace(TOGGLE, "");
		String return_s = "";		
		TFSort list_ranks = MonVocabulaire.getIndex().sortByTF("1", "all");
		TreeSet<TermValuePair> l = list_ranks.getList();
		Iterator<TermValuePair> iter = l.iterator();
		int nb = 0;
		Formatter out = new Formatter(new StringBuilder(), Locale.US);
		while (iter.hasNext() && (nb < MAX_PRINT_WORDS))
		{
			TermValuePair t = iter.next();
			String w = t.getTerm();
			if (MonVocabulaire.getIndex().get(w).isActivated())
			{
				out.format(YELLOW + "%s" + RESET, w);
				return_s += w + " ";
				if (!toggle)
				{
					double p = t.getValue();
					out.format(DIM_GREY + " (%.2f)" + RESET, p);
				}
				out.format(" ");
				nb++;
			}
		}
		out.format("\n");
		System.out.print(out.toString());
		out.close();
		return return_s;
	}
	
	private static String top_topics(String s) throws NoModel
	{
		boolean toggle = lookfortag(TOGGLE, s);
		if (toggle) s = s.replace(TOGGLE, "");
		String return_s = "";
		int[] ranks = getModel().get_rank("all");
		TFSort list_ranks = new TFSort();
		for (int i=0; i<getModel().numTopics(); i++)
			list_ranks.add(""+i, getModel().numTopics()-ranks[i]);
		TreeSet<TermValuePair> l = list_ranks.getList();
		Iterator<TermValuePair> iter = l.iterator();
		int nb = 0;
		Formatter out = new Formatter(new StringBuilder(), Locale.US);
		while (iter.hasNext() && (nb < MAX_PRINT_TOPICS))
		{
			TermValuePair t = iter.next();
			int topic = Integer.parseInt(t.getTerm());
			out.format(LIGHT_GREEN + "%2d" + RESET, topic);
			return_s += topic + " ";
			if (!toggle)
			{
				double p = getModel().get_pz("all", topic);
				out.format(DIM_GREY + " (%.2f)" + RESET, p);
			}
			out.format(" ");
			nb++;
		}
		out.format("\n");
		System.out.print(out.toString());
		out.close();
		return return_s;
	}

	private static String top(String s) throws NoModel
	{
		String[] split = s.split(" ");
		if (s.equals(""))
		{
			help("top");
			return "";
		}
		String fun = split[0];
		String args = "";
		if (split.length>1)
			args = split[1];
		switch (fun)
		{
			case "topics":
				return top_topics(args);
			case "words":
				return top_words(args);
			/*case "all":
				String r = load_topics(split[1]);
				load_labeling(split[1]);
				return r; */
			default:
				help("top");
		}
		return "";
	}
	
	private static String stats(String s)
	{
		int sum = 0;
		TreeMap<String,TreeSet<MyDocument>> ld = MyDocument.getDocPerDate();
		for (MyDate key : MyDocument.getSortedDates())
		{
			
			int n = ld.get(key.toString()).size();
			System.out.println(LIGHT_GREEN + n + RESET + " documents in " + key);
			sum += n;
		}
		System.out.println("Total: " + LIGHT_GREEN + sum + RESET + " documents");
		return "";
	}
	
	private static String pz(String s) throws NoModel
	{
		boolean norm = lookfortag(NORMALIZED, s);
		if (norm) s = s.replace(NORMALIZED, "");
		boolean rank = lookfortag(RANK, s);
		if (rank) s = s.replace(RANK, "");
		ArrayList<Integer> list_docs = getListNumbers(s, getModel().numTopics());
		if (list_docs.size() == 0)
		{	
			help("pz");
			return "";
		}
		System.out.println("Printing the temporal distribution p(z) for " + LIGHT_GREEN + "topics " + s.trim() + RESET);
		Formatter out = new Formatter(new StringBuilder(), Locale.US);
		out.format("\t\t");
		for (MyDate key : MyDocument.getSortedDates())
			out.format("%s\t", key.toString());
		out.format(PURPLE + "all\n");

		for (int i=0; i<list_docs.size(); i++)
		{
			int topic = list_docs.get(i);
			TreeMap<String,Double> list_p = getModel().get_pz(topic);
			double total_num = list_p.get("all");
			out.format(LIGHT_GREEN + "topic %3d" + RESET + " :\t", topic);
			if (norm) // normalized for ignoring the corpus size
			{
				double sum = 0;				
				for (String key : list_p.keySet())
				if (!key.equals("all"))
				{
					Double d = list_p.get(key);
					sum += (d / MyDocument.getNumDocPerPeriod(key));
				}
				for (MyDate key : MyDocument.getSortedDates())
				{
					Double d = list_p.get(key.toString());
					Double frac = d / MyDocument.getNumDocPerPeriod(key.toString());
					Double d_norm = (frac / sum) * total_num; 
					out.format("%.2f\t", d_norm);
				}
				out.format(PURPLE + "%.2f\n" + RESET, total_num);
			}
			else
			{
				if (rank)
				{
					for (MyDate key : MyDocument.getSortedDates())
					//if (!key.equals("all"))
					{
						int[] ranks = getModel().get_rank(key.toString());
						out.format("%d\t", ranks[i]);
					}
					int[] ranks = getModel().get_rank("all");
					out.format(PURPLE + "%d\n" + RESET, ranks[i]);
				}
				else
				{
					for (MyDate key : MyDocument.getSortedDates())
					{
						Double d = list_p.get(key.toString());
						out.format("%.2f\t", d);
					}
					out.format(PURPLE + "%.2f\n" + RESET, total_num);
				}	
			}
		}
		System.out.print(out.toString());
		out.close();
		return "";
	}
	
	public static TFSort get_top_correlated_topics(int i, String type) throws NoModel
	{
		TFSort list = new TFSort();
		for (int j=0; j<getModel().numTopics(); j++)
			list.add(""+j, getModel().getCorrelation(type, i, j));
		return list;
	}
	
	private static String cor(String s) throws NoModel
	{
		System.out.println("1 -> " + s);
		boolean toggle = lookfortag(TOGGLE, s);
		if (toggle) s = s.replace(TOGGLE, "");
		System.out.println("2 -> " + s);
		String type = "docbased";
		String[] spl = cut(s);
		System.out.println("3 -> " + s);
		if (spl != null)
		{
			if (spl[0].equals("d"))
			{
				type = "docbased";
				s = spl[1];
			}
			else
			if (spl[0].equals("w"))
			{
				type = "wordbased";
				s = spl[1];
			}
		}
		else
			System.out.println("NULL");
		ArrayList<Integer> list_docs = getListNumbers(s, getModel().numTopics());
		if (list_docs.size() == 0)
		{	
			help("cor");
			return "";
		}
		System.out.println("Printing the " + PURPLE + "top " + MAX_PRINT_TOPICS + " topics" + RESET + " correlated to " + LIGHT_GREEN + "topics " + s.trim() + RESET);
		System.out.println("Type of correlation: " + PURPLE + type + RESET);
		Formatter out = new Formatter(new StringBuilder(), Locale.US);
		String return_s = "";
		for (int i=0; i<list_docs.size(); i++)
		{
			return_s = "";
			int topic = list_docs.get(i);
			out.format(LIGHT_GREEN + "topic %3d"	 + RESET + " : ", topic);
			TFSort list_sorted = get_top_correlated_topics(topic, type);
			TreeSet<TermValuePair> list = list_sorted.getList();
			Iterator<TermValuePair> iter = list.iterator();
			int nb = 0;
			while (iter.hasNext() && (nb < MAX_PRINT_TOPICS))
			{
				TermValuePair t = iter.next();
				out.format("\t%s", t.getTerm());
				return_s += t.getTerm() + " ";
				if (!toggle)
					out.format(DIM_GREY + " (%.3f)" + RESET, t.getValue());
				nb++;
			}
			out.format("\n");
		}
		System.out.print(out.toString());
		out.close();
		return return_s;
	}
	
	private static String qual(String s) throws NoModel
	{
		/*boolean toggle = lookfortag(TOGGLE, s);
		if (toggle) s = s.replace(TOGGLE, "");		
		boolean rank = lookfortag(SORTED, s);
		if (rank) s = s.replace(SORTED, "");*/
		ArrayList<Integer> list_docs = getListNumbers(s, getModel().numTopics());
		if (list_docs.size() == 0)
		{	
			help("qual");
			return "";
		}
		System.out.println("Printing quality measures for " + LIGHT_GREEN + "topics " + s.trim() + RESET);
		Formatter out = new Formatter(new StringBuilder(), Locale.US);
		for (int i=0; i<list_docs.size(); i++)
		{
			//return_s = "";
			int topic = list_docs.get(i);
			out.format(LIGHT_GREEN + "topic %3d" + RESET + ":\t", topic);
			/*out.format("%.3f", TopicCoherence.getCoherence().getMeasure("temp_entropy", topic));			
			out.format(DIM_GREY + " (temp_coh)" + RESET);
			out.format(" %.3f", TopicCoherence.getCoherence().getMeasure("dist_background", topic));
			out.format(DIM_GREY + " (djunkA)" + RESET);*/
			for (String measure : TopicCoherence.measures)
			{
				double q = TopicCoherence.getCoherence().getMeasure(measure, topic);
				if (q != Double.NEGATIVE_INFINITY)
					out.format("\t%.3f", q);
				else
					out.format("\t-");
				out.format(DIM_GREY + " (" + measure + ")" + RESET);
			}
			out.format("\n");
		}		
		System.out.print(out.toString());
		out.close();
		return "";
	}
	
	private static String wordstat(String s)
	{
		String[] split = s.split(" ");
		if ((split.length == 0) || ((split.length == 1) && (split[0].equals(""))))
		{
			help("wordstat");
			return "";
		}
		System.out.println("Printing temporal distribution for " + YELLOW + "words " + s.trim() + RESET);
		Formatter out = new Formatter(new StringBuilder(), Locale.US);
		for (String w : split)
		{
			out.format(YELLOW + "word %10s" + RESET  + "\t:", w);
			TreeMap<String,Integer> d = MonVocabulaire.getDistribTerm(w);
			if (d == null)
				out.format("not found (or below TF=50)");
			else
			{
				TreeSet<MyDate> dates = MyDocument.getSortedDates();
				//TreeMap<MyDate, TreeSet<MyDocument>> dates = MyDocument.getListDate();
				int numdates = dates.size();
				int num[] = new int[numdates];
				int i = 0;
				int sum = 0;
				for (MyDate date : dates)
				{
					num[i] = d.get(date.toString());
					sum += num[i];
					i++;
				}
				i = 0;
				for (i=0; i<numdates; i++)
				{
					double per = (double)num[i] / (double)sum;
					out.format("%3d%% " + DIM_GREY + "(%d)\t" + RESET, Math.round(per*100), num[i]);
				}
				out.format(YELLOW + "%d" + RESET, sum);
			}
			out.format("\n");
		}
		System.out.print(out.toString());
		out.close();
		return "";
	}
	
	private static String topics(String s) throws NoModel
	{
		return topic(" 0:" + (getModel().numTopics()-1));
	}
	
	private static String ngrams(String s) throws NoModel
	{
		//getModel();
		StringBuilder return_s = new StringBuilder();
		String foldpath = LoadDataset.getPath() + Constantes.separateur + Constantes.RESULT_DIR + Constantes.separateur + LoadDataset.getDataName()
			+ Constantes.separateur + "models" + Constantes.separateur + "biotex" + Constantes.separateur;
		new File(foldpath).mkdirs();
		ArrayList<Integer> list_topics = getListNumbers(s, getModel().numTopics());
		for (int i=0; i<list_topics.size(); i++)
		{
			return_s = new StringBuilder();
			int topic = list_topics.get(i);
			String filetopdoc = foldpath + "topdoc-" + topic + ".txt";
			Formatter out = new Formatter(new StringBuilder(), Locale.US);				
			out.format(LIGHT_GREEN + "topic %3d" + RESET + " : ", topic);
			//TFSort list_docs = topic_word.get_pdz(topic, false, 50, 1); // 1: get the official doc id
			TFSort list_docs = getModel().getSortedDocuments(1).get(topic);
			try {
				MyDocument.save2FileForBiotex(filetopdoc, list_docs);
			} catch (IOException e) {
				System.out.println(RED + "Error: impossible to read/write files for biotex" + RESET);
			}
			ArrayList<CandidatTerm> list_candidat_terms_validated = CallBiotex.BioTex(filetopdoc, "", "LIDF_Value");
			boolean pass =false;
			for (CandidatTerm ct : list_candidat_terms_validated)
			{
				if (pass)
					return_s.append(",");
				return_s.append(ct.getTerm());
				out.format("%s " + DIM_GREY + "(%.3f) " + RESET, ct.getTerm(), ct.getImportance());
				pass = true;
			}
			System.out.println(out.toString());
			out.close();
		}
		return return_s.toString();
	}

	private static String term(String s) throws NoModel
	{
		String s_clear = s.trim();
		String[] split = s_clear.split(" ");
		if (split.length == 0)
			return RED + "Term not found" + RESET;
		String term = "";
		int i = -1;
		try{
			i = Integer.parseInt(split[0]);
			term = s_clear.substring(s_clear.indexOf(" ") + 1);
		}
		catch (NumberFormatException e) // general information
		{
			term = s_clear;
		}
		ForIndexing c = MonVocabulaire.getIndexTerm(term);
		if (c == null)
			return RED + "Term not found" + RESET;
		Formatter out = new Formatter(new StringBuilder(), Locale.US);
		out.format(YELLOW + "%s" + RESET
				+ ":\t %d " + DIM_GREY + "(TF)" + RESET
				+ " %d " + DIM_GREY + "(DF)" + RESET,
				c.getTerm(), c.getTF("all"), c.getNBDocs("all"));
		if (i != -1) // additional information related to topic i
		{
			out.format(" and for topic " + LIGHT_GREEN + "%d" + RESET + ":\t", i);
			out.format(" %.3e " + DIM_GREY + "(docU)" + RESET, getModel().getAvDocLogLikelihood(c, i, false));
			out.format(" %.3e " + DIM_GREY + "(docN)" + RESET, getModel().getAvDocLogLikelihood(c, i, true));
		}
		return out.toString();
	}

	private static String probadoc(String s) throws NoModel
	{
		ArrayList<Integer> list_docs = getListNumbers(s, MyDocument.size());
		if (list_docs.size() < 2)
		{	
			help("probadoc");
			return "";
		}
		int topic = list_docs.get(0);
		System.out.println("Printing the conditional probability p(d/z) for topic " + LIGHT_GREEN + topic + RESET);
		Formatter out = new Formatter(new StringBuilder(), Locale.US);
		boolean flag = false;
		StringBuilder return_s = new StringBuilder();
		for (int i=1; i<list_docs.size(); i++)
		{
			if (flag)
			{
				return_s.append(", ");
				out.format(",");
			}
			else
				flag = true;
			String name = (String)instances.get(list_docs.get(i)).getName();
			MyDocument doc = MyDocument.get(name);
			double p = getModel().getDocLikelihood(doc, topic, true);
			out.format("%.3e", p);
			return_s.append(p);
		}
		System.out.println(out.toString());
		return return_s.toString();
	}
	
}
