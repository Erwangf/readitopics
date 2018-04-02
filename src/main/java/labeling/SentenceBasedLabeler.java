package labeling;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.InstanceList;
import core.CleanWord;
import core.Constantes;
import core.MonVocabulaire;
import core.MyDocument;
import core.StopWords;
import core.TFSort;
import core.TermValuePair;
import exe.RunLDA;
import io.config.LoadConfigFile;
import io.LoadDataset;
import topicmodeling.LDATopicModel;
import topicmodeling.NoModel;
import topicmodeling.TopicAlphabet;
import utils.DataUtils;

/**
 * @author Antoine
 * Labeling based on simple cosin similarity beetween topic distribution over words and sentences of the topdocs
 */	
public class SentenceBasedLabeler extends TopicLabelerSkeleton
{
	/* Instances of MALLET */
	public double[][] top_dis;
	private static InstanceList instances;

	private static LDATopicModel topic_model;

	private int nbtopdoc;



	public SentenceBasedLabeler(LDATopicModel topic_word,InstanceList instances, String dir,int numtopdoc)
	{
		super(topic_word, dir); //, topicSortedWords, topicSortedDocs);
		topic_model = topic_word;
		nbtopdoc = numtopdoc;
		this.instances = instances;
		///init:just to make sure StopWords are loaded		
		try {
			StopWords.loadTextFile(LoadDataset.getPath() + Constantes.separateur + LoadConfigFile.getStopLists()[0]);
			//			StopWords.loadTextFile(LoadConfigFile.getStopLists()[0]);
		} catch (IOException e) {
			e.printStackTrace();
		}
		//Initialisaiton of the vector of topics distibution overs words
		top_dis = getTopicWords(topic_model.getCurrentTopicModel(),true,true);
	}	

	/**
	 * Method to add the best labels based on the Sentence labeling
	 * @param topic int index of the considered topic 
	 */	
	void computeLabel(int topic) 
	{		
		//	try {
		//		StopWords.loadTextFile(LoadConfigFile.getStopLists()[0]);
		//	} catch (IOException e) {
		//		e.printStackTrace();
		//	}
		////////ici init du bow du topic

		double[] topic_bow  =top_dis[topic];

		//
		TFSort res = new TFSort();

		//On recupere les top_docs (differents) du Topic topic
		TFSort list_docs = topic_model.getSortedDocuments(0).get(topic);
		//on phrase les textes brut -> on obtient nos phrases candidates
		ArrayList<String> top_docs_phrased = retTDocs(list_docs, nbtopdoc);
		////////////////Pour chaque phrase :
		//		int ind =0;
		for (String TT : top_docs_phrased){
			//			System.out.println(TT);
			//on clean
			ArrayList<String> vect =  tokenize_phr(TT);
			//On cree le BOW
			double[] bow_p = initBow(topic_model.getAlphabet(),vect);
			//On calcule la sim au topic
			double cosinesim =utils.Vector.sim(bow_p,topic_bow);
			//			System.out.println("p"+ind+"/topic"+topic+": "+cosinesim);
			//			ind++;
			if (!Double.isNaN(cosinesim)){
				res.add(TT, cosinesim);
			}

		}


		////////////////

		//ON ressort les candidats triés
		int done = 0;
		TreeSet<TermValuePair> sorted_list = res.getList();
		Iterator<TermValuePair> iter = sorted_list.iterator(); 
		while ((iter.hasNext()) && (done < max_top_terms_calculated))
		{
			//					System.out.println(iter.next().getTerm());;
			TermValuePair tvp = iter.next();
			TermValuePair tvpout = new TermValuePair(CleanWord.TCleaner(tvp.getTerm()),tvp.getValue());
			topicLabels[topic].add(tvpout);
			done++;
		}	
		for(int i=0;i<4;i++){
			System.out.println(topicLabels[topic].get(i).getTerm());
		}

	}
	/**
	 * Method to compute labels generation for every topics of the current model
	 * StopWord or loaded to be sure ^^. Is it useless? 
	 */	
	@Override
	public void computeLabels()
	{

		for (int i=0; i<k; i++)
		{
			System.out.print(i+"-");
			computeLabel(i);
		}
	}

	/////Setter/getters
	public String getName()
	{
		return "sentence_based" ;
	}

	public String getShortName()
	{
		return "sb";
	}
	public int getNbtopdoc() {
		return nbtopdoc;
	}

	public void setNbtopdoc(int nbtopdoc) {
		this.nbtopdoc = nbtopdoc;
	}

	/**
	 * Method to return the top docs parsed in sentence of the current model
	 * @param nbtodoc the number of top_doc wanted
	 * @param list_docs TFSort of the docs
	 * @return ArrayList<String> of top_docs raw text
	 */	
	private ArrayList<String> retTDocs(TFSort list_docs,int nbtodoc) {
		// TODO Auto-generated method stub
		ArrayList<String> top_docs_out = new ArrayList<String>();
		int nbdoc = list_docs.size();
		int nb = nbtodoc;
		for (TermValuePair t : list_docs.getList())
		{
			String name =(String)instances.get(Integer.parseInt(t.getTerm())).getName();
			MyDocument doc = MyDocument.get(name);
			String text_brut = doc.getText();
			//			System.out.println(te);
			String te = text_brut;
			
			//This is adirty patch for the dirty data.
			//data need a clean
			if (LoadDataset.getDataName().equals("dataconf")){
				te ="";
				String[] blobed = text_brut.split("\" \"");
				if(blobed.length > 1){
					te = blobed[0].substring(1)+" " + blobed[1].substring(0,blobed[1].length()-2);
				}
				else{
					te = blobed[0];
				}
			}
			if (LoadDataset.getDataName().equals("us-newsClean")){
				te ="";
				String[] blobed = text_brut.split("\" \"");
				if(blobed.length > 1){
					te = blobed[0].substring(1) + ". " + blobed[1].substring(0,blobed[1].length()-1);
				}
				else{
					te = blobed[0];
				}
			}

			if(!top_docs_out.contains(te) || nbdoc<=nb){
				top_docs_out.add(te);
				nb--;
			}	
			nbdoc--;
			if (nb==0){
				break;
			}
		}

		ArrayList<String> top_docs_out_f = new ArrayList<String>();
		List<String> splitted = new ArrayList<String>(); 
		for (String s : top_docs_out ){
			splitted  =  DataUtils.extractSentences(s);
			for (String sp : splitted){
				if (sp.length()>1){
					top_docs_out_f.add(sp);
				}
			}
		}
		return top_docs_out_f;
	}

	/**
	 * Method to tokenize and clean a String
	 * @param TT String to tokenize
	 * @return ArrayList<String> of tokens
	 */	
	private ArrayList<String> tokenize_phr(String TT ) {
		// TODO Auto-generated method stub
		//On clean la phrase
		CleanWord.setStrategy(LoadConfigFile.getPreprocess());
		ArrayList<String> tokens = CleanWord.tokenize(TT);
		ArrayList<String> phrased_tokenized = new ArrayList<String>();
		for (String token : tokens)
		{
			String t = CleanWord.clean(token);
			if ((!t.equals("")) && (!t.equals(" "))) 
			{
				phrased_tokenized.add(t);
			}
		}
		return phrased_tokenized;
	}

	/**
	 * Method to generate a BOW from a voc and a tokenized text
	 */	
	public double[] initBow(TopicAlphabet voc,ArrayList<String> text) {
		int taille =voc.size();
		double[] bowe = new double[taille];

		for (int j=0;j<taille;j++){
			bowe[j] = 0;
		}
		for(String t : text){
			try{
				bowe[voc.getIndex(t)]=1;
			}catch(ArrayIndexOutOfBoundsException e){
				System.out.println(t);
			}
		}
		return bowe;
	}

	/**
	 * Method to generate the distribution over words of the topics(more precisely a model) 
	 * This is a redeclaration of the MALLET method
	 * @param mod  a MALLET model
	 * @return double[number of topics][length of vocabulary] ditribution over words of every topics
	 */	
	public static double[][] getTopicWords(ParallelTopicModel mod,boolean normalized, boolean smoothed) {
		double[][] result = new double[mod.numTopics][mod.getAlphabet().size()];

		for (int type = 0; type < mod.getAlphabet().size(); type++) {
			int[] topicCounts = mod.typeTopicCounts[type];

			int index = 0;
			while (index < topicCounts.length &&
					topicCounts[index] > 0) {

				int topic = topicCounts[index] & mod.topicMask;
				int count = topicCounts[index] >> mod.topicBits;

		result[topic][type] += count;

		index++;
			}
		}

		if (smoothed) {
			for (int topic = 0; topic < mod.numTopics; topic++) {
				for (int type = 0; type < mod.getAlphabet().size(); type++) {
					result[topic][type] += mod.beta;
				}
			}
		}

		if (normalized) {
			double[] topicNormalizers = new double[mod.numTopics];
			if (smoothed) {
				for (int topic = 0; topic < mod.numTopics; topic++) {
					topicNormalizers[topic] = 1.0 / (mod.tokensPerTopic[topic] + mod.getAlphabet().size() * mod.beta);
				}
			}
			else {
				for (int topic = 0; topic < mod.numTopics; topic++) {
					topicNormalizers[topic] = 1.0 / mod.tokensPerTopic[topic];
				}
			}

			for (int topic = 0; topic < mod.numTopics; topic++) {
				for (int type = 0; type < mod.getAlphabet().size(); type++) {
					result[topic][type] *= topicNormalizers[topic];
				}
			}
		}
		return result;
	}



	//	//////////////////MAIn for testing
	public static void main(String[] args) throws NoModel, IOException {
		loadData("C:/Users/antoi/git/topiclabeling/LDA/config.HP.full");
//		config.articles
//		2017_05_16
		
//		config.huffpostc.us
//		2017_05_19

		String model_file = "2017_05_15";

		System.out.println("Initiate topic labeling");
		System.out.println("Load topic model: " + model_file);

		// load the first topic model in the directory
		LDATopicModel.loadTopicModels(model_file);
		topic_model = LDATopicModel.getFirstTopicModel();


		long startTime;
		long estimatedTime;
		startTime = System.nanoTime();

		estimatedTime = System.nanoTime() - startTime;		
		System.out.println(" (" + TimeUnit.NANOSECONDS.toMillis(estimatedTime) + " ms)");
		startTime = System.nanoTime();		
		SentenceBasedLabeler labeler9 = new SentenceBasedLabeler(topic_model, instances, model_file, 10);


		//Labl Computataton
//		try {
//			labeler9.import_labels();
//		} catch (ClassNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		labeler9.computeLabel(1);
//		labeler9.export_labels();	
//		labeler9.setNBtopterms(3);
//		ArrayList<TopicLabelerSkeleton> all_labelers = new ArrayList<TopicLabelerSkeleton>();
//		File myfile = new File("D:\\Bureau\\labels.json");
//		String sb = "";
//		BufferedWriter writer = new BufferedWriter(new FileWriter(myfile));
//		for (int i=0;i<topic_model.numTopics();i++){
//			sb+=("\t\t{\n");
//			sb+=("\t\t\"topicid\" : " + i +",\n");
//			sb+=(",\n");
//			sb+=("\t\t\"");
//			sb+=(labeler9.getName() + "\" : [\n");
//			sb+=("\t\t\t");
//			sb+=(labeler9.getLabelJSON(i, true));
//			sb+=("\n\t\t]");
//
//			sb+=("\n\t\t}");
//			sb+=("\n");
//		}
//		writer.write(sb);
//		writer.flush();
//		writer.close();
//		estimatedTime = System.nanoTime() - startTime;		
//		System.out.println(" (" + TimeUnit.NANOSECONDS.toMillis(estimatedTime) + " ms)");


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

}


