package exe;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
//import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.Alphabet;
import cc.mallet.types.InstanceList;
import core.Constantes;
import core.MonVocabulaire;
import core.MyDocument;
import core.TermValuePair;
import evaluation.TopicCoherence;
import io.config.LoadConfigFile;
import io.LoadDataset;
import topicmodeling.LDATopicModel;
import topicmodeling.NoModel;

public class TopicAnalytics {
	
	private static LDATopicModel topic_model;
	
	public static void main(String[] args) throws IOException, NoModel
	{
		
		if (args.length < 2)
			throw new IOException("Require at least 2 arguments: configuration file + model directory");
		
		loadData(args[0]);
		
		String model_file = args[1];
		
		System.out.println("Load topic model: " + model_file);

		// load the first topic model in the directory
		LDATopicModel.loadTopicModels(model_file);
		topic_model = LDATopicModel.getFirstTopicModel(); 
		
		if (topic_model == null)
		{
			System.out.println("No model found: abort");
			System.exit(0);
		}
		
		analyze(topic_model, model_file);
		
	}
		
	public static void analyze(LDATopicModel t, String model_file) 
	{
		
		topic_model = t;
		
		// set folder for evaluation
		String input = LoadDataset.getPath() + Constantes.separateur + Constantes.RESULT_DIR
				+ Constantes.separateur + LoadDataset.getDataName() + Constantes.separateur + "eval"
				+ Constantes.separateur + model_file + Constantes.separateur;
		
		if (!LoadDataset.getPathIndex().isEmpty())
		{			
			System.out.println("Evaluation measures (path: " + LoadDataset.getPathIndex() + ")");
			compute_coherence_measures(input);
		}
		else
			System.out.println("Evaluation measures not computed (need of a lucene index)");
		
		
		System.out.println("Pairwise topic correlation");
		compute_correlation_topics(input);
		/*if ((correlations != null) && (correlations.size() > 0)) 
		topic_model.setCorrelation(correlations.get(0));*/
	}		

	// duplicate with TopicLabeling
	// similarities with BrowseTopics
	//  => to be factorized!
	private static void loadData(String config) throws IOException
	{
		   			
        // check configuration file
		 
		InputStream inputStream;
		inputStream = new FileInputStream(config);
		
		if (inputStream != null)
			LoadConfigFile.loadConfig(inputStream);
		
		if (LoadDataset.isRawData())
			LoadDataset.extractFullCorpus(LoadDataset.getPath() + Constantes.separateur + Constantes.DATA_DIR + Constantes.separateur + LoadDataset.getRawData());

		//System.out.println("Weighting scheme: " + LoadConfigFile.getVocTypes()[0]);
		System.out.print("Load dataset: " + LoadDataset.getDataName());
		
		long startTime = System.nanoTime();
		LoadDataset.extractDocs();		
		long estimatedTime = System.nanoTime() - startTime;		
		System.out.println(" (" + TimeUnit.NANOSECONDS.toMillis(estimatedTime) + " ms)");
		
		String biotex_input_dir = LoadDataset.getPath() + Constantes.separateur + Constantes.RESULT_DIR + Constantes.separateur + LoadDataset.getDataName() + Constantes.separateur + "biotex";
		String path = biotex_input_dir + Constantes.separateur + LoadConfigFile.getVocTypes()[0] + Constantes.separateur;
		MonVocabulaire.indexing(path);
		
		/* set the ID used by LDA to the documents */
		MyDocument.setInternalIDforLDA();
		
		/* compute the temporal distribution of documents */
		MyDocument.compute_distrib_temp_doc();
		
		/* compute p(d) for each period and for the whole period */
		MyDocument.compute_proba_docs();		

		System.out.println("Compute word distribution over time");
		MonVocabulaire.computeDistribTemporelle();
				
		// set which vocabulary is used for LDA
		//int s = LoadConfigFile.getVocSizes()[0];
		//MonVocabulaire.setVocabAllWords(biotex_input_dir, LoadConfigFile.getVocTypes()[0], LoadConfigFile.getMinDocs(), LoadConfigFile.getMinTF());
		//MonVocabulaire.setVocabAllWords(LoadConfigFile.getMinDocs(), LoadConfigFile.getMinTF());
		//MonVocabulaire.setVocabAllTerms(biotex_input_dir, LoadConfigFile.getVocTypes()[0], LoadConfigFile.getMinDocs(), LoadConfigFile.getMinTF());
		MonVocabulaire.setVocabAllWords(LoadConfigFile.getMinDocs(), LoadConfigFile.getMinTF());
	}
	
	// return the vector that contains the scoring for every document (here, based on probabilities)
	private static double[] get_distrib_docs(int i, ParallelTopicModel model) {
		double[] distrib = new double[MyDocument.size()];
		for (int j = 0; j < MyDocument.size(); j++) {			
			double[] td = topic_model.getTopicProbabilities(j);
			distrib[j] = td[i];
		}
		return distrib;
	}	

	// return the vector that contains the scoring for every word 
	private static double[] get_distrib_words(int i, InstanceList instances, ParallelTopicModel model) {
		Alphabet dataAlphabet = instances.getAlphabet();
		double[] distrib = new double[dataAlphabet.size()];
		ArrayList<TermValuePair> list_pwz = topic_model.get_pw_z(i);		
		for (int j = 0; j < list_pwz.size(); j++) {			
			TermValuePair t = list_pwz.get(j);			
			distrib[j] = t.getValue();
		}
		return distrib;
	}		
	
	private static double pearson_correlation(double[] p_i, double[] p_j)
	{
		int n = p_i.length;
		double sx = 0.0;
		double sy = 0.0;
		double sxx = 0.0;
		double syy = 0.0;
		double sxy = 0.0;				
		for (int k = 0; k < n; k++) {
			double w1 = p_i[k];
			double w2 = p_j[k];
			sx += w1;
			sy += w2;
			sxx += w1 * w1;
			syy += w2 * w2;
			sxy += w1 * w2;
		}
		double cov = (sxy * n) - (sx * sy);
		// standard error of x
		double sigmax = Math.sqrt((sxx * n) - (sx * sx));
		// standard error of y
		double sigmay = Math.sqrt((syy * n) - (sy * sy));
		// correlation is just a normalized covariation
		// cor[i][j] = cov / (sigmax * sigmay);
		return sxy / (Math.sqrt(sxx) * Math.sqrt(syy));
	}
	
	// compute the pairwise (Pearson) correlation between topics (note that we don't compute cor(z_i, z_i) therefore it's set to 0)
	private static double[][] compute_correlation_topics_docbased(InstanceList instances, ParallelTopicModel model) {
		System.out.print("Compute pairwise doc-based correlation: ");
		//Alphabet dataAlphabet = instances.getAlphabet();
		//double n = dataAlphabet.size();
		//double n = MyDocument.size();
		double[][] cor = new double[model.numTopics][model.numTopics];
		for (int i = 0; i < model.numTopics; i++) {
			System.out.print(i + "-");
			double[] p_i = get_distrib_docs(i, model);
			for (int j = i + 1; j < model.numTopics; j++) {
				double[] p_j = get_distrib_docs(j, model);
				cor[i][j] = pearson_correlation(p_i, p_j);
			}
		}
		for (int i = 0; i < model.numTopics; i++)
			for (int j = 0; j < i; j++) {
				cor[i][j] = cor[j][i];
			}
		return cor;
	}
	
	//compute the pairwise (Pearson) correlation between topics based on word distribution
	private static double[][] compute_correlation_topics_wordbased(InstanceList instances, ParallelTopicModel model) {
		System.out.print("Compute pairwise word-based correlation: ");		
		//double n = dataAlphabet.size();
		//double n = MyDocument.size();
		double[][] cor = new double[model.numTopics][model.numTopics];
		for (int i = 0; i < model.numTopics; i++) {
			System.out.print(i + "-");
			double[] p_i = get_distrib_words(i, instances, model);
			for (int j = i + 1; j < model.numTopics; j++) {
				double[] p_j = get_distrib_words(j, instances, model);
				cor[i][j] = pearson_correlation(p_i, p_j);
			}
		}
		for (int i = 0; i < model.numTopics; i++)
			for (int j = 0; j < i; j++) {
				cor[i][j] = cor[j][i];
			}
		return cor;
	}
	
	private static void compute_correlation_topics(String input) {
		InstanceList instances = RunLDA.preprocessForTopicModeling();
		double[][] correlations_db = null;
		try {
			correlations_db = LDATopicModel.getIOFile().import_correlation(input, "docbased");
			System.out.println("Correlation loaded");
		} catch (Exception e) {
			System.out.println("Correlation measure not found");
			long startTime = System.nanoTime();
			double[][] cor_topics_db = compute_correlation_topics_docbased(instances, topic_model.getCurrentTopicModel());
			long estimatedTime = System.nanoTime() - startTime;
			System.out.println(" in " + TimeUnit.NANOSECONDS.toSeconds(estimatedTime) + " s");
			topic_model.setCorrelation_docbased(cor_topics_db);
			LDATopicModel.getIOFile().export_correlation(input, "docbased", cor_topics_db);
		}
		double[][] correlations_wb = null;
		try {
			correlations_wb = LDATopicModel.getIOFile().import_correlation(input, "wordbased");
			System.out.println("Correlation loaded");
		} catch (Exception e) {
			System.out.println("Correlation measure not found");
			long startTime = System.nanoTime();
			double[][] cor_topics_wb = compute_correlation_topics_wordbased(instances, topic_model.getCurrentTopicModel());
			long estimatedTime = System.nanoTime() - startTime;
			System.out.println(" in " + TimeUnit.NANOSECONDS.toSeconds(estimatedTime) + " s");
			topic_model.setCorrelation_docbased(cor_topics_wb);
			LDATopicModel.getIOFile().export_correlation(input, "wordbased", cor_topics_wb);
		}		
		
	}

	private static void compute_coherence_measures(String input) {
		TopicCoherence.compute(topic_model, input + Constantes.separateur + "model.eval");
	}	

}
