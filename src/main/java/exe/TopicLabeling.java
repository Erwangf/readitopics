package exe;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import Object.CandidatTerm;
import cc.mallet.types.InstanceList;
import core.CallBiotex;
import core.Constantes;
import core.MonVocabulaire;
import core.MyDocument;
import core.TFSort;
import io.config.LoadConfigFile;
import io.LoadDataset;
import labeling.COrderLabeler;
import labeling.DocBasedLabeler;
import labeling.OneOrderLabeler;
import labeling.SentenceBasedLabeler;
import labeling.Sentence_Based_3;
import labeling.ZeroOrderLabeler;
import topicmodeling.LDATopicModel;
import topicmodeling.NoModel;

public class TopicLabeling
{

	/* Instances of MALLET */
	private static InstanceList instances;

	private static LDATopicModel topic_model;

	private static ArrayList<String>[] candidates;

	public static void main(String[] args) throws IOException, NoModel
	{
		if (args.length < 2)
			throw new IOException("Require at least 2 arguments: configuration file + model directory");

		loadData(args[0]);

		String model_file = args[1];

		System.out.println("Initiate topic labeling");
		System.out.println("Load topic model: " + model_file);

		// load the first topic model in the directory
		LDATopicModel.loadTopicModels(model_file);
		topic_model = LDATopicModel.getFirstTopicModel();
		//topic_word = LDATopicModel.loadFirstTopicModel(model_file);

		compute_labeling(topic_model, instances, model_file);

	}

	// compute the labeling candidates for every topics independently
	private static void compute_candidates_for_topics()
	{
		candidates = new ArrayList[topic_model.numTopics()];
		for (int i=0; i<topic_model.numTopics(); i++)
		{
			compute_candidates_for_topics(i);
		}
	}
	// compute the labeling candidates for a specific topic

	private static void compute_candidates_for_topics(int topic)
	{
		candidates[topic] = new ArrayList<String>();
		/*if (topic>1)
			return;*/
		String foldpath = LoadDataset.getPath() + Constantes.separateur + Constantes.RESULT_DIR + Constantes.separateur + LoadDataset.getDataName()
			+ Constantes.separateur + "models" + Constantes.separateur + "biotex" + Constantes.separateur;
		new File(foldpath).mkdirs();
		String filetopdoc = foldpath + "topdoc-" + topic + ".txt";
		//TFSort list_docs = topic_word.get_pdz(topic, false, 50, 1); // 1: get the official doc id
		TFSort list_docs = topic_model.getSortedDocuments(1).get(topic); //.get_pdz(topic, false, 50, 1); // 1: get the official doc id
		try {
			MyDocument.save2FileForBiotex(filetopdoc, list_docs);
		} catch (IOException e) {
			System.out.println("Error: impossible to read/write files for biotex");
		}
		ArrayList<CandidatTerm> list_candidat_terms_validated = CallBiotex.BioTex(filetopdoc, "", "TF");
		boolean pass =false;
		for (CandidatTerm ct : list_candidat_terms_validated)
		{
			candidates[topic].add(ct.getTerm());
		}
	}

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
		MonVocabulaire.setVocabAllTerms(biotex_input_dir, LoadConfigFile.getVocTypes()[0], LoadConfigFile.getMinDocs(), LoadConfigFile.getMinTF());

		instances = RunLDA.preprocessForTopicModeling();

	}

	public static void compute_labeling(LDATopicModel t, InstanceList inst, String model_file) throws IOException, NoModel
	{
						
		topic_model = t;
		instances = inst;
		
		/* NAIVE LABELING = top K words */
		
		long startTime;
		long estimatedTime;


		//Label Computation
		startTime = System.nanoTime();
		System.out.println("Sentence-based labeling");
		SentenceBasedLabeler labeler0 = new SentenceBasedLabeler(topic_model,instances, model_file,10);
		try {
		labeler0.import_labels();
		} catch (ClassNotFoundException | IOException e1) {
			System.out.println("\nLabeling not found: compute it from scratch");
			labeler0.computeLabels();
			labeler0.export_labels();			
		}	
		estimatedTime = System.nanoTime() - startTime;		
		System.out.println(" (" + TimeUnit.NANOSECONDS.toMillis(estimatedTime) + " ms)");
		
		
//		startTime = System.nanoTime();
//		System.out.println("Word-based chi2 labeling");
//		TopicLabelerSkeleton labeler1 = new Chi2TopicLabeler(topic_model, model_file);
//		
//		labeler1.setNBtopterms(10);		
//		try {
//			
//			labeler1.import_labels();
//		} catch (ClassNotFoundException | IOException e1) {
//			System.out.print("\nLabeling not found: compute it from scratch");
//			labeler1.computeLabels();
//			labeler1.export_labels();			
//		}		
//		estimatedTime = System.nanoTime() - startTime;		
//		System.out.println(" (" + TimeUnit.NANOSECONDS.toMillis(estimatedTime) + " ms)");
		/* ZERO-ORDER LABELING */
		
		// uncomment this part if you want to calculate a specific candidate list for each topic
		/*System.out.print("Computing the labeling candidates");
		startTime = System.nanoTime();
		compute_candidates_for_topics();
		estimatedTime = System.nanoTime() - startTime;		
		System.out.println(" (" + TimeUnit.NANOSECONDS.toSeconds(estimatedTime) + " s)");*/
				
		//BrowseTopics.setTM(topic_word);
		
		MonVocabulaire.getIndex().activateTerms(f -> true); // setActivated all terms
		
		startTime = System.nanoTime();
		System.out.print("Zero-order labeling with even norm");		
		ZeroOrderLabeler labeler2 = new ZeroOrderLabeler(topic_model, model_file);
		labeler2.setNorm(ZeroOrderLabeler.EVEN_NORM);
		labeler2.setNBtopterms(3);
		try {
			
			labeler2.import_labels();
		} catch (ClassNotFoundException | IOException e1) {
			System.out.print("\nLabeling not found: compute it from scratch");
			labeler2.setNBwords_for_filtering(10);
			labeler2.addCandidates(MonVocabulaire.getActivatedTerms());
			labeler2.computeLabels();
			labeler2.export_labels();			
		}		
		estimatedTime = System.nanoTime() - startTime;		
		System.out.println(" (" + TimeUnit.NANOSECONDS.toMillis(estimatedTime) + " ms)");
		
		/*try {
			BrowseTopics.print_labels("0:4", labeler2);
		} catch (NoModel e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/		
		
		startTime = System.nanoTime();
		ZeroOrderLabeler labeler3 = new ZeroOrderLabeler(topic_model, model_file);
		labeler3.setNorm(ZeroOrderLabeler.FREQ_NORM);
		labeler3.setNBtopterms(3);
		System.out.print("Zero-order labeling with freq norm ");
		try {
			labeler3.import_labels();
		} catch (ClassNotFoundException | IOException e1) {
			System.out.print("\nLabeling not found: compute it from scratch");
			labeler3.setNBwords_for_filtering(10);
			labeler3.addCandidates(MonVocabulaire.getActivatedTerms());
			labeler3.computeLabels();
			labeler3.export_labels();
		}
		estimatedTime = System.nanoTime() - startTime;		
		System.out.println(" (" + TimeUnit.NANOSECONDS.toMillis(estimatedTime) + " ms)");
		
		/*try {
			BrowseTopics.print_labels("0:4", labeler3);
		} catch (NoModel e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
					
		/* ONE-ORDER LABELING */
		
		startTime = System.nanoTime();
		System.out.println("One-order labeling");
		OneOrderLabeler labeler4 = new OneOrderLabeler(topic_model, model_file);
		labeler4.setNBtopterms(3);
		try {
			labeler4.import_labels();
		} catch (ClassNotFoundException | IOException e1) {
			System.out.print("\nLabeling not found: compute it from scratch");
			labeler4.setNBwords_for_filtering(10);	
			labeler4.addCandidates(MonVocabulaire.getActivatedTerms());
			//System.out.print(labeler4.getNBcandidates() + " candidate labels competing (all activated terms)");	
			labeler4.setSmoothing(0);
			labeler4.computeLabels();
			labeler4.export_labels();
		}
		estimatedTime = System.nanoTime() - startTime;		
		System.out.println(" (" + TimeUnit.NANOSECONDS.toMillis(estimatedTime) + " ms)");
		
		/* DOC-BASED LABELING */
		
		for (int i=0; i<2; i++)
		{
			startTime = System.nanoTime();
			System.out.print("Doc-based labeling");
			DocBasedLabeler labeler5 = new DocBasedLabeler(topic_model, model_file);
			labeler5.setNBtopterms(3);
			if (i==1)
				labeler5.setNorm(DocBasedLabeler.FREQ_NORM);
			try {
				labeler5.import_labels();
			} catch (ClassNotFoundException | IOException e1) {
				System.out.print("\nLabeling not found: compute it from scratch");
				labeler5.setNBwords_for_filtering(10);
				labeler5.addCandidates(MonVocabulaire.getActivatedTerms());
				//System.out.print(labeler5.getNBcandidates() + " candidate labels competing (all activated terms)");	
				labeler5.computeLabels();
				labeler5.export_labels();
			}
			estimatedTime = System.nanoTime() - startTime;		
			System.out.println(" (" + TimeUnit.NANOSECONDS.toMillis(estimatedTime) + " ms)");
		}
		
		/*startTime = System.nanoTime();
		System.out.println("Normed doc-based labeling");
		NormDocBasedLabeler labeler6 = new NormDocBasedLabeler(dataAlphabet, model, topic_word, topicSortedWords, topicSortedDocs);
		labeler6.setNBtopterms(3);
		labeler6.setNBwords_for_filtering(10);
		// all activated terms are label candidates
		labeler6.addCandidates(MonVocabulaire.getActivatedTerms());
		System.out.print(labeler6.getNBcandidates() + " candidate labels competing (all activated terms)");	
		// compute the best possible labels
		labeler6.computeLabels();
		labeler6.export_labels(model_file);
		estimatedTime = System.nanoTime() - startTime;		
		System.out.println(" (" + TimeUnit.NANOSECONDS.toMillis(estimatedTime) + " ms)");
		*/
		
		/*
		startTime = System.nanoTime();
		System.out.println("Clustering-based labeling");
		ClusteringLabeler labeler7 = new ClusteringLabeler(topic_model, model_file);
		labeler7.setAlpha(10.0);
		try {
			labeler7.import_labels();
		} catch (ClassNotFoundException | IOException e1) {
			System.out.println("Labeling not found: compute it from scratch");
			// all activated terms are label candidates
			labeler7.addCandidates(MonVocabulaire.getActivatedTerms());
			//labeler7.addCandidates(candidates);
			System.out.println(labeler7.getNBcandidates() + " candidate labels competing (all activated terms)");						
			labeler7.setNBtopterms(10);
			labeler7.setNBwords_for_filtering(10);
			// first: DPMM
			try	{
				labeler7.import_dpmm();
			} catch (ClassNotFoundException | IOException e2) {
				System.out.println(e2.getMessage());
				System.out.println("Clustering not found: compute it from scratch");
				labeler7.computeDPMM();
				System.out.println();
				labeler7.export_dpmm();
				try	{ labeler7.import_dpmm(); // new import because of modification of variable names
				} catch (Exception e) { }
			}			
			// next : labeling
			// compute the best possible labels
			labeler7.computeLabels();
			labeler7.export_labels();
		}
		*/
		
		/*ArrayList<TopicLabelerSkeleton> list = new ArrayList<>();
		list.add(labeler7);
		ExportLabels export = new ExportLabels(topic_word.numTopics(), list);
		export.setPrintProba(true);
		export.export("ASOIAF_labels_clustering_ngram_docbased_3.csv", "csv");*/
		
		/*for (int i=0; i<5; i++)
		{
			labeler7.printDPMM_2(i);
			labeler7.printDPMM_3(i);
		}*/
		
		/*estimatedTime = System.nanoTime() - startTime;		
		System.out.println(" (" + TimeUnit.NANOSECONDS.toMillis(estimatedTime) + " ms)");
		
		/* new test of labeling with HDP */
		/*
		startTime = System.nanoTime();
		System.out.println("HDP-based labeling");
		ClusteringLabeler2 labeler8 = new ClusteringLabeler2(topic_model, model_file, instances);
		labeler8.setEta(0.00001);
		labeler8.setGamma(0.00001);
		labeler8.setNbTopDoc(20);
		labeler8.setMaxIterations(1000);
		try {
			labeler7.import_labels();
		} catch (ClassNotFoundException | IOException e1) {
			System.out.println("Labeling not found: compute it from scratch");
			// all activated terms are label candidates
			labeler8.addCandidates(MonVocabulaire.getActivatedTerms());
			System.out.println(labeler8.getNBcandidates() + " candidate labels competing (all activated terms)");
			try {
				labeler8.import_hdp();
			} catch (ClassNotFoundException | IOException e2) {
				System.out.println(e2.getMessage());
				System.out.println("Clustering not found: compute it from scratch");
				labeler8.computeHDP();
				System.out.println();
				labeler8.export_hdp();
				try	{ labeler8.import_hdp(); // new import because of modification of variable names
				}  catch (Exception e) { }			
			}
			// next : labeling
			labeler8.computeLabels();
			labeler8.export_labels();	
		}	
		startTime = System.nanoTime();
		System.out.print("Zero-order labeling with even norm");		
		ZeroOrderLabeler labeler2 = new ZeroOrderLabeler(topic_model, model_file);
		labeler2.setNorm(ZeroOrderLabeler.EVEN_NORM);
		labeler2.setNBtopterms(3);
		try {
			
			labeler2.import_labels();
		} catch (ClassNotFoundException | IOException e1) {
			System.out.print("\nLabeling not found: compute it from scratch");
			labeler2.setNBwords_for_filtering(10);
			labeler2.addCandidates(MonVocabulaire.getActivatedTerms());
			labeler2.computeLabels();
			labeler2.export_labels();			
		}		
		estimatedTime = System.nanoTime() - startTime;		
		System.out.println(" (" + TimeUnit.NANOSECONDS.toMillis(estimatedTime) + " ms)");
		
		/*try {
			BrowseTopics.print_labels("0:4", labeler2);
		} catch (NoModel e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/		
		startTime = System.nanoTime();
		COrderLabeler labeler9 = new COrderLabeler(topic_model, model_file);
		labeler9.setNorm(COrderLabeler.ZERO_NORM);
		labeler9.setNBtopterms(3);
		System.out.print("C-order labeling ");
		try {
			labeler9.import_labels();
		} catch (ClassNotFoundException | IOException e1) {
			System.out.print("\nLabeling not found: compute it from scratch");
			labeler9.setNBwords_for_filtering(10);
			labeler9.addCandidates(MonVocabulaire.getActivatedTerms());
			labeler9.computeLabels();
			labeler9.export_labels();
		}
		estimatedTime = System.nanoTime() - startTime;		
		System.out.println(" (" + TimeUnit.NANOSECONDS.toMillis(estimatedTime) + " ms)");
		
		startTime = System.nanoTime();
		COrderLabeler labeler10 = new COrderLabeler(topic_model, model_file);
		labeler10.setNorm(COrderLabeler.TERM_NORM);
		labeler10.setNBtopterms(3);
		System.out.print("C-order labeling with termhood smoothing ");
		try {
			labeler10.import_labels();
		} catch (ClassNotFoundException | IOException e1) {
			System.out.print("\nLabeling not found: compute it from scratch");
			labeler10.setNBwords_for_filtering(10);
			labeler10.addCandidates(MonVocabulaire.getActivatedTerms());
			labeler10.computeLabels();
			labeler10.export_labels();
		}
		estimatedTime = System.nanoTime() - startTime;		
		System.out.println(" (" + TimeUnit.NANOSECONDS.toMillis(estimatedTime) + " ms)");
		startTime = System.nanoTime();
		Sentence_Based_3 labeler11 = new  Sentence_Based_3(topic_model, instances, model_file, 15,"TD_10",0.1);
		labeler11.setNBtopterms(3);
		System.out.print("S3 with 10 top docs, mu at 0.1");
		try {
			labeler11.import_labels();
		} catch (ClassNotFoundException | IOException e1) {
			System.out.print("\nLabeling not found: compute it from scratch");
			labeler11.setNBwords_for_filtering(10);
			labeler11.addCandidates(MonVocabulaire.getActivatedTerms());
			labeler11.computeLabels();
			labeler11.export_labels();
		}
		estimatedTime = System.nanoTime() - startTime;		
		System.out.println(" (" + TimeUnit.NANOSECONDS.toMillis(estimatedTime) + " ms)");
		startTime = System.nanoTime();
		Sentence_Based_3 labeler12 = new  Sentence_Based_3(topic_model, instances, model_file, 15,"TD_20",0.1);
		labeler12.setNBtopterms(3);
		System.out.print("S3 with 20 top docs, mu at 0.1");
		try {
			labeler12.import_labels();
		} catch (ClassNotFoundException | IOException e1) {
			System.out.print("\nLabeling not found: compute it from scratch");
			labeler12.setNBwords_for_filtering(10);
			labeler12.addCandidates(MonVocabulaire.getActivatedTerms());
			labeler12.computeLabels();
			labeler12.export_labels();
		}
		estimatedTime = System.nanoTime() - startTime;		
		System.out.println(" (" + TimeUnit.NANOSECONDS.toMillis(estimatedTime) + " ms)");
		startTime = System.nanoTime();
		Sentence_Based_3 labeler13 = new  Sentence_Based_3(topic_model, instances, model_file, 15,"TD_10",10);
		labeler13.setNBtopterms(3);
		System.out.print("S3 with 10 top docs, mu at 10");
		try {
			labeler13.import_labels();
		} catch (ClassNotFoundException | IOException e1) {
			System.out.print("\nLabeling not found: compute it from scratch");
			labeler13.setNBwords_for_filtering(10);
			labeler13.addCandidates(MonVocabulaire.getActivatedTerms());
			labeler13.computeLabels();
			labeler13.export_labels();
		}
		estimatedTime = System.nanoTime() - startTime;		
		System.out.println(" (" + TimeUnit.NANOSECONDS.toMillis(estimatedTime) + " ms)");
		startTime = System.nanoTime();
		Sentence_Based_3 labeler14 = new  Sentence_Based_3(topic_model, instances, model_file, 15,"TD_20",10);
		labeler14.setNBtopterms(3);
		System.out.print("S3 with 20 top docs, mu at 10");
		try {
			labeler14.import_labels();
		} catch (ClassNotFoundException | IOException e1) {
			System.out.print("\nLabeling not found: compute it from scratch");
			labeler14.setNBwords_for_filtering(10);
			labeler14.addCandidates(MonVocabulaire.getActivatedTerms());
			labeler14.computeLabels();
			labeler14.export_labels();
		}
		estimatedTime = System.nanoTime() - startTime;		
		System.out.println(" (" + TimeUnit.NANOSECONDS.toMillis(estimatedTime) + " ms)");
		startTime = System.nanoTime();
		Sentence_Based_3 labeler15 = new  Sentence_Based_3(topic_model, instances, model_file, 15,"TD_10",1000);
		labeler15.setNBtopterms(3);
		System.out.print("S3 with 10 top docs, mu at 1000");
		try {
			labeler15.import_labels();
		} catch (ClassNotFoundException | IOException e1) {
			System.out.print("\nLabeling not found: compute it from scratch");
			labeler15.setNBwords_for_filtering(10);
			labeler15.addCandidates(MonVocabulaire.getActivatedTerms());
			labeler15.computeLabels();
			labeler15.export_labels();
		}
		estimatedTime = System.nanoTime() - startTime;		
		System.out.println(" (" + TimeUnit.NANOSECONDS.toMillis(estimatedTime) + " ms)");
		startTime = System.nanoTime();
		Sentence_Based_3 labeler16 = new  Sentence_Based_3(topic_model, instances, model_file, 15,"TD_20",1000);
		labeler16.setNBtopterms(3);
		System.out.print("S3 with 20 top docs, mu at 1000");
		try {
			labeler16.import_labels();
		} catch (ClassNotFoundException | IOException e1) {
			System.out.print("\nLabeling not found: compute it from scratch");
			labeler16.setNBwords_for_filtering(10);
			labeler16.addCandidates(MonVocabulaire.getActivatedTerms());
			labeler16.computeLabels();
			labeler16.export_labels();
		}
		estimatedTime = System.nanoTime() - startTime;		
		System.out.println(" (" + TimeUnit.NANOSECONDS.toMillis(estimatedTime) + " ms)");
		startTime = System.nanoTime();
		Sentence_Based_3 labeler17 = new  Sentence_Based_3(topic_model, instances, model_file, 15,"COS_0",1000);
		labeler17.setNBtopterms(3);
		System.out.print("SB with COS");
		try {
			labeler17.import_labels();
		} catch (ClassNotFoundException | IOException e1) {
			System.out.print("\nLabeling not found: compute it from scratch");
			labeler17.setNBwords_for_filtering(10);
			labeler17.addCandidates(MonVocabulaire.getActivatedTerms());
			labeler17.computeLabels();
			labeler17.export_labels();
		}
		estimatedTime = System.nanoTime() - startTime;		
		System.out.println(" (" + TimeUnit.NANOSECONDS.toMillis(estimatedTime) + " ms)");
		startTime = System.nanoTime();
		Sentence_Based_3 labeler18 = new  Sentence_Based_3(topic_model, instances, model_file, 15,"COS_1",1000);
		labeler18.setNBtopterms(3);
		System.out.print("S3 with COS IDF ");
		try {
			labeler18.import_labels();
		} catch (ClassNotFoundException | IOException e1) {
			System.out.print("\nLabeling not found: compute it from scratch");
			labeler18.setNBwords_for_filtering(10);
			labeler18.addCandidates(MonVocabulaire.getActivatedTerms());
			labeler18.computeLabels();
			labeler18.export_labels();
		}
		estimatedTime = System.nanoTime() - startTime;		
		System.out.println(" (" + TimeUnit.NANOSECONDS.toMillis(estimatedTime) + " ms)");

		startTime = System.nanoTime();
		Sentence_Based_3 labeler19 = new Sentence_Based_3(topic_model, instances, model_file, 20,"BS_0",0);
		labeler19.setNBtopterms(3);
		System.out.print("S3 with Random Score ");
		try {
			labeler19.import_labels();
		} catch (ClassNotFoundException | IOException e1) {
			System.out.print("\nLabeling not found: compute it from scratch");
			labeler19.setNBwords_for_filtering(10);
			labeler19.addCandidates(MonVocabulaire.getActivatedTerms());
			labeler19.computeLabels();
			labeler19.export_labels();
		}
		estimatedTime = System.nanoTime() - startTime;		
		System.out.println(" (" + TimeUnit.NANOSECONDS.toMillis(estimatedTime) + " ms)");
	}

}
