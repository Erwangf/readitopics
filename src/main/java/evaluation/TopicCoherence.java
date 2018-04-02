package evaluation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;

import core.MyDate;
import core.MyDocument;
import core.TFSort;
import core.TermValuePair;
import io.EvaluationMeasures;
import topicmodeling.LDATopicModel;
import topicmodeling.TopicAlphabet;

public class TopicCoherence
{

	/* number of top words considered for calculating the palmetto measures */
	public static final int NB_TOP_WORDS_FOR_EVAL = 10;

	/* palmetto measures */
	public static final String[] palmetto_measures = { "umass", "uci", "c_v", "c_p", "c_a", "npmi" };

	/* all measures */
	public static final String[] measures = { "umass", "uci", "c_v", "c_p", "c_a", "npmi", "dist_background", "temp_entropy","tw_entropy","inv_lambda","avg_docrank","nb_firstrank","pz","tw_avglength","mean_KL" };

	private static TopicCoherence topic_coherence;

	private LDATopicModel model;	

	private HashMap<String,double[]> evaluations;

	public TopicCoherence(LDATopicModel model)
	{
		this.model = model;
		this.evaluations = new HashMap<>();
	}

	/*
	//return the entropy of topic <b>i</b> calculated on its temporal distribution
	public double getTemporalEntropy(int i)
	{
		return evaluations.get("temp_entropy")[i];
	}

	// return the KL distance (symmetrized KL divergence) between topic <b>i</b> and the whole distribution 
	public double getDistJunkA(int i)
	{
		return evaluations.get("dist_background")[i];
	} */

	/** return the value of palmetto <b>measure</b> for topic <b>i</b> */
	public double getMeasure(String measure, int i)
	{
		double[] r = evaluations.get(measure);
		if (r != null)
			return r[i];
		else
			return Double.NEGATIVE_INFINITY;
	}

	private void compute_temporal_entropy()
	{
		System.out.println("Compute the temporal entropy");
		double[] temporal_entropy = new double[model.numTopics()];
		for (int i=0; i<model.numTopics(); i++)
		{
			double total = model.get_pz("all", i);
			double sum = 0;
			for (MyDate key : MyDocument.getSortedDates())

			{
				double proba = model.get_pz(key.toString(), i) / total; 
				sum -= proba * Math.log(proba) / Math.log(2);
			}
			temporal_entropy[i] = sum;
		}
		evaluations.put("temp_entropy", temporal_entropy);
	}
	//Compute the entropy of the nb_topWords top-words distribution
	private void compute_topwords_entropy()
	{
		System.out.println("Computing the top word ditribution entropy");
		int nb_topWords = NB_TOP_WORDS_FOR_EVAL ;

		double[] tw_entropy = new double[model.numTopics()];
		for (int i=0; i<model.numTopics(); i++)
		{
			double entropy = 0;
			for (int j=0; j<nb_topWords; j++){
				double p = model.getProbaWordGivenTopic(i, j);
				entropy -= p * Math.log(p) / Math.log(2);
			}
			tw_entropy[i] = entropy;
		}
		evaluations.put("tw_entropy", tw_entropy);
	}
	//Compute the avg size of the nb_topWords top-words distribution
	private void compute_topwords_avglength()
	{
		System.out.println("Computing the top word ditribution entropy");
		int nb_topWords = NB_TOP_WORDS_FOR_EVAL ;

		double[] tw_avglength = new double[model.numTopics()];
		for (int i=0; i<model.numTopics(); i++)
		{
			int length = 0;
			ArrayList<Integer> ar = model.getTopWords(i);
			for (int j=0;j<nb_topWords;j++){ 
				length += model.getAlphabet().lookupObject(ar.get(j)).length();
			}
			tw_avglength[i] = length/nb_topWords;
		}
		evaluations.put("tw_avglength", tw_avglength);
	}
	private void compute_lambda_topwords()
	{
		System.out.println("Computing the mean of top-word probabilities");
		int nb_topWords = NB_TOP_WORDS_FOR_EVAL ;

		double[] inv_lambda = new double[model.numTopics()];
		for (int i=0; i<model.numTopics(); i++)
		{
			double mean = 0;
			for (int j=0; j<nb_topWords; j++){
				double p = model.getProbaWordGivenTopic(i, j);
				mean += p;
			}
			inv_lambda[i] = mean/nb_topWords;
		}
		evaluations.put("inv_lambda", inv_lambda);
	}
	private void compute_avg_docrank()
	{
		System.out.println("Computing the avg doc ranking of the topic");
		int num_docs=0;
		boolean end = true;
		//On cherche le nombre de doc (pas trouvé où,désolé. Si vous savez, virez cette boucle degeu)
		while(end){
			try{
				model.getTopicProbabilities(num_docs);
				num_docs++;
			}catch(IndexOutOfBoundsException e){
				end=false;
			}
		}
		int[][] doc_dis = new int[model.numTopics()][num_docs] ;
		//		System.out.println(num_docs);
		for (int i=0;i < num_docs;i++){
			double[] proba = model.getTopicProbabilities(i);
			TFSort mylist = null;
			mylist = new TFSort(); // we keep all the topics
			for (int j=0; j<proba.length; j++){
				mylist.add(""+j, proba[j]);	
			}
			int k=1;
			for(TermValuePair tvp : mylist.getList()){
				doc_dis[Integer.parseInt(tvp.getTerm())][i] = k;
				k++;
			}
		}

		double[] avg_docrank = new double[model.numTopics()];
		for (int i=0; i<model.numTopics(); i++)
		{
			double mean = 0;
			for(int rank : doc_dis[i]){
				mean+=rank;
			}
			avg_docrank[i] = mean/num_docs;
		}
		evaluations.put("avg_docrank", avg_docrank);
	}

	private void compute_nb_firstrank()
	{
		System.out.println("Computing the nb of doc with topic in first place");
		double sys=0;
		
		int num_docs=0;
		double[] doc_dis = new double[model.numTopics()] ;
		Arrays.fill(doc_dis,0);
		boolean end = true;
		//On cherche le nombre de doc (pas trouvé où,désolé. Si vous savez, virez cette boucle degeu)
		while(end){
			try{
				model.getTopicProbabilities(num_docs);
				num_docs++;
			}catch(IndexOutOfBoundsException e){
				end=false;
			}
		}

		for (int i=0;i < num_docs;i++){
			double[] proba = model.getTopicProbabilities(i);
			int highest = 0;
			double proba_h =proba[0]; 
			for (int j = 1;j<model.numTopics();j++){
				if (proba[j]>proba_h){
					highest = j;
					proba_h =proba[j]; 
				}
			}
			doc_dis[highest]++;
		}
		for (double i : doc_dis){
			sys+=i;
		}
		System.out.println(sys);
		evaluations.put("nb_firstrank", doc_dis);
	}
	private void compute_pz()
	{
		System.out.println("Computing the global document distribution of the topic");

		double[] doc_dis = new double[model.numTopics()] ;

		for (int j = 1;j<model.numTopics();j++){
			doc_dis[j]=model.get_pz("all",j);
		}

		evaluations.put("pz", doc_dis);
	}

	private void compute_distance_junk_topics()
	{	
		System.out.println("Compute the distance to the background word distribution");
		double[] dist_junkA = new double[model.numTopics()];
		ArrayList<TermValuePair> vec_p_w = model.get_pw();
		for (int i=0; i<model.numTopics(); i++)
		{
			ArrayList<TermValuePair> vec_p_wz = model.get_pw_z(i);
			dist_junkA[i] = KL_dist(vec_p_wz, vec_p_w);
		}
		evaluations.put("dist_background", dist_junkA);
	}

	private double KL_dist(ArrayList<TermValuePair> v1, ArrayList<TermValuePair> v2)
	{
		int size = v1.size();
		double sum = 0;	for (int i=0; i<size; i++)
		{
			double value1 = v1.get(i).getValue();
			double value2 = v2.get(i).getValue();
			if ((value1 != 0) && (value2 !=0)) //heuristic there
				sum += 0.5 * (value1 * Math.log(value1/value2) + value2 * Math.log(value2/value1));
		}
		return sum;
	}
	private void compute_mean_KL()
	{	
		System.out.println("Compute the avg KL to other topics");
		double[] mean_KL = new double[model.numTopics()];
		for (int i=0; i<model.numTopics(); i++)
		{
			ArrayList<TermValuePair> vec_p_wz = model.get_pw_z(i);
			double sum = 0;
			for (int j=0; j<model.numTopics(); j++)
			{
				ArrayList<TermValuePair> vec_p_wz_2 = model.get_pw_z(j);
				sum += KL_dist(vec_p_wz,  vec_p_wz_2 );
			}
			mean_KL[i] = sum/model.numTopics();
		}
		evaluations.put("mean_KL", mean_KL);
	}
	private void compute_palmetto_measures()
	{		
		PalmettoEvaluation.populateTopics(model, NB_TOP_WORDS_FOR_EVAL);
		for (String m : palmetto_measures)
			if (!evaluations.containsKey(m))
			{
				System.out.println("Compute the measure " + m);
				double[] res = PalmettoEvaluation.computeMeasure(m);
				evaluations.put(m,  res);
			}
	}

	public static TopicCoherence getCoherence()
	{
		return topic_coherence;
	}

	public static void compute(LDATopicModel model, String filename)
	{
		loadMeasures(model, filename);
		if (!topic_coherence.evaluations.containsKey("dist_background"))
			topic_coherence.compute_distance_junk_topics();
		if (!topic_coherence.evaluations.containsKey("temp_entropy"))
			topic_coherence.compute_temporal_entropy();
		if (!topic_coherence.evaluations.containsKey("tw_entropy"))
			topic_coherence.compute_topwords_entropy();
		if (!topic_coherence.evaluations.containsKey("inv_lambda"))
			topic_coherence.compute_lambda_topwords();
		if (!topic_coherence.evaluations.containsKey("avg_docrank"))
			topic_coherence.compute_avg_docrank();
		if (!topic_coherence.evaluations.containsKey("nb_firstrank"))
			topic_coherence.compute_nb_firstrank();	
		if (!topic_coherence.evaluations.containsKey("pz"))
			topic_coherence.compute_pz();	
		if (!topic_coherence.evaluations.containsKey("mean_KL"))
			topic_coherence.compute_mean_KL();	
		if (!topic_coherence.evaluations.containsKey("tw_avglength"))
			topic_coherence.compute_topwords_avglength();	


		topic_coherence.compute_palmetto_measures();
		EvaluationMeasures.save(filename, topic_coherence.evaluations);
	}

	public static void loadMeasures(LDATopicModel model, String filename)
	{
		topic_coherence = new TopicCoherence(model);
		try {
			topic_coherence.evaluations = EvaluationMeasures.load(filename);
			System.out.println("Evaluation file loaded");
		}
		catch (Exception e)
		{
			System.out.println("No evaluation file to load");
		}		
	}

}
