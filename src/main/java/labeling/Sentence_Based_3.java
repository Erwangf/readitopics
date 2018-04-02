package labeling;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import utils.Vector;

import cc.mallet.types.InstanceList;
import core.CleanWord;
import core.Constantes;
import core.ForIndexing;
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
import utils.DataUtils;

/**
 * @author Antoine Gourru
 * Labeling 3
 */	
public class Sentence_Based_3 extends TopicLabelerSkeleton
{
	private static InstanceList instances;

	private static LDATopicModel topic_model;

	private int nbtopdoc;
	private String context_gen;
	private double mu;
	private TreeSet<ForIndexing> vocabulaire;
	private String context_strat;


	public Sentence_Based_3(LDATopicModel topic_word,InstanceList instances, String dir,int numtopdoc,String context,double mu)
	{
		super(topic_word, dir); //, topicSortedWords, topicSortedDocs);
		topic_model = topic_word;
		nbtopdoc = numtopdoc;
		this.instances = instances;
		this.context_gen = context.split("_")[0];
		this.context_strat = context.split("_")[1];
		this.mu = mu;
		vocabulaire = new TreeSet<ForIndexing>();
		for (ForIndexing w : MonVocabulaire.getIndex().getActivatedFeatures())
		{
			if (w.getLength()<2){
				//				System.out.println(w.getTerm());
				vocabulaire.add(w);
			}
		}
	}	

	/**
	 * Method to add the best labels based on the Sentence labeling
	 * @param topic int index of the considered topic 
	 */	
	private void computeLabel(int topic) 
	{	
		TFSort res = new TFSort();	
		if(context_gen.equals("BS")){
			TFSort list_docs = topic_model.getSortedDocuments(0).get(topic);
			HashMap<String,MyDocument> documents = new HashMap<String,MyDocument>();
			for (TermValuePair t : list_docs.getList())
			{
				String name =(String)instances.get(Integer.parseInt(t.getTerm())).getName();
				MyDocument doc = MyDocument.get(name);
				documents.put(doc.getId(),doc);
			}
			HashMap<String,MyDocument> topdocuments =Topdocs(list_docs, nbtopdoc);
			documents.remove(topdocuments);

			ArrayList<String> top_docs_phrased = cleanedDoc(documents);
			for (String doc : top_docs_phrased){
				res.add(doc,Math.random());
			}
		}else{
			////////ici init du bow du topic
			double[] topic_bow  =initLMTopic(topic);
			//		for (double temp : topic_bow){
			//			if(temp<0){
			//			System.out.print(temp+" ");}
			//		}

			//On recupere les top_docs (differents) du Topic topic et on crée le LM du contexte
			TFSort list_docs = topic_model.getSortedDocuments(0).get(topic);
			HashMap<String,MyDocument> topdocuments =Topdocs(list_docs, nbtopdoc);
			//		for (Map.Entry<String,MyDocument> mapentry : topdocuments.entrySet()) {
			//			System.out.println(mapentry.getValue().getText());
			//		}
			//on phrase les textes brut -> on obtient nos phrases candidates
			ArrayList<String> top_docs_phrased = cleanedDoc(topdocuments);

			////Cas COS -> cosine
			if (context_gen.equals("COS")){
				////////////////Pour chaque phrase :
				for (String TT : top_docs_phrased){
					//on clean
					ArrayList<String> tok_phrase =  tokenize_phr(TT);
					//On cree le BOW
					double[] bow_pn = initLMCOS(tok_phrase);
					//On calcule le membre a du score au topic
					double sum_a =utils.Vector.somme_produit(bow_pn,topic_bow);
					if (!Double.isNaN(sum_a)){
						res.add(TT, sum_a);
						//										System.out.println(TT);
						//										System.out.println(sum_a);
					}		

				}
			}
			if(context_gen.equals("TD") || context_gen.equals("FULL")){
				double[] lm_context = initLMContext(topic);
				//			System.out.println("contexte : " +Vector.norme(lm_context));
				////////////////Pour chaque phrase :
				for (String TT : top_docs_phrased){
					//on clean
					ArrayList<String> tok_phrase =  tokenize_phr(TT);
					//On cree le BOW
					double[] bow_p = initLM(tok_phrase,lm_context);
					//On calcule le membre a du score au topic
					double sum_a =utils.Vector.somme_produit(bow_p,topic_bow);
					if (!Double.isNaN(sum_a)){
						double score = sum_a - Math.log(tok_phrase.size()+mu);
						if (!Double.isInfinite(score)){
							res.add(TT, (score));
						}else{
							System.out.println(tok_phrase.size());
						}
						//					System.out.println(TT);
						//					System.out.println(sum_a);
					}			

				}
			}
			if (context_gen.equals("LOC")){
				////////////////Pour chaque phrase :
				for (String TT : top_docs_phrased){
					//on clean
					ArrayList<String> tok_phrase =  tokenize_phr(TT);
					//On cree le BOW
					double[] lm_context1 = initLMContext(topic,which_con(topdocuments,TT));
					double[] bow_p = initLM(tok_phrase,lm_context1);
					//On calcule le membre a du score au topic
					double sum_a =utils.Vector.somme_produit(bow_p,topic_bow);
					//				System.out.println(sum_a);
					if (!Double.isNaN(sum_a)){
						double score = sum_a - Math.log(tok_phrase.size()+mu);
						//					if (!Double.isInfinite(score)){
						res.add(TT, score);
						//						System.out.println(TT);
						//						System.out.println(sum_a);
						//					}
					}
				}
			}
		}
		////////////////

		//ON ressort les candidats triés
		int done = 0;
		TreeSet<TermValuePair> sorted_list = res.getList();
		Iterator<TermValuePair> iter = sorted_list.iterator(); 
		while ((iter.hasNext()) && (done < max_top_terms_calculated))
		{
			TermValuePair tvp = iter.next();
			TermValuePair tvpout = new TermValuePair(CleanWord.TCleaner(tvp.getTerm()),tvp.getValue());
			topicLabels[topic].add(tvpout);
			//						System.out.println(tvpout.getTerm());
			//						System.out.println(tvpout.getValue());
			done++;
		}	
		int t = topicLabels[topic].size();
		//		for(int i=t-4;i<t;i++){
		for(int i=0;i<4;i++){
			System.out.println(topicLabels[topic].get(i).getTerm());
			//			System.out.println(topicLabels[topic].get(i).getValue());
		}

	}

	private MyDocument which_con(HashMap<String, MyDocument> topdocuments, String tT) {
		for (Map.Entry<String,MyDocument> mapentry : topdocuments.entrySet()) {
			MyDocument doc = mapentry.getValue();
			if (doc.getText().contains(tT)){
				return doc;
			}
		}
		return null;
	}

	/**
	 * Method to compute labels generation for every topics of the current model
	 * StopWord or loaded to be sure ^^. Is it useless? 
	 */	
	@Override
	public void computeLabels()
	{
		///init:just to make sure StopWords are loaded		
		try {
			StopWords.loadTextFile(LoadDataset.getPath() + Constantes.separateur + LoadConfigFile.getStopLists()[0]);
			//			StopWords.loadTextFile(LoadConfigFile.getStopLists()[0]);
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (int i=0; i<k; i++)
		{
			System.out.print(i+"-");
			computeLabel(i);
		}
	}

	/////Setter/getters
	public String getName()
	{
		if (context_gen.equals("COS")){
			return "sentence_based_"+context_gen+context_strat;
		}else{
			return "sentence_based_"+context_gen+context_strat+"_"+Double.toString(mu).replace(".","@");
		}

	}

	public String getShortName()
	{
		if (context_gen.equals("COS")){
			return "s"+context_gen+context_strat;
		}else{
			return "s"+context_gen+context_strat+Double.toString(mu).replace(".","@");
		}

	}
	public int getNbtopdoc() {
		return nbtopdoc;
	}

	public void setNbtopdoc(int nbtopdoc) {
		this.nbtopdoc = nbtopdoc;
	}

	/**
	 * Method to return the top docs parsed in sentence of the current model

	 * @return ArrayList<String> of top_docs raw text
	 */	
	private ArrayList<String> cleanedDoc(HashMap<String,MyDocument> top_documents) {

		// TODO Auto-generated method stub
		ArrayList<String> top_docs_out = new ArrayList<String>();
		for (Map.Entry<String,MyDocument> mapentry : top_documents.entrySet()) {
			MyDocument doc = mapentry.getValue();
			String text_brut = doc.getText();
			//			System.out.println(te);
			String te = text_brut;
			//This is a dirty patch for the dirty data.
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
			top_docs_out.add(te);		
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
	 * Method to return the top docs of the current model
	 * @param nbtodoc the number of top_doc wanted
	 * @param list_docs TFSort of the docs
	 * @return HashMap<String,MyDocument> documents of top_docs raw text
	 */			
	private HashMap<String,MyDocument> Topdocs(TFSort list_docs,int nbtodoc) {		
		// TODO Auto-generated method stub
		HashMap<String,MyDocument> top_docs_out = new HashMap<String,MyDocument>();
		int nbdoc = list_docs.size();
		int nb = nbtodoc;
		for (TermValuePair t : list_docs.getList())
		{
			String name =(String)instances.get(Integer.parseInt(t.getTerm())).getName();
			MyDocument doc = MyDocument.get(name);

			if(!top_docs_out.containsValue(doc) || nbdoc<=nb){
				top_docs_out.put(doc.getId(),doc);
				nb--;
			}	
			nbdoc--;
			if (nb==0){
				break;
			}
		}
		return top_docs_out;
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
	 * Method to generate a language model from a tokenized textual entity and a context language model
	 */	
	public double[] initLM(ArrayList<String> text,double[] bowe_Context) {
		int taille = vocabulaire.size();
		double[] bowe = new double[taille];
		int i=0;
		for (ForIndexing w : vocabulaire)
		{
			for (String t : text){
				if (t.equals(w.getTerm())){
					bowe[i]+=1;
				}
			}
			double temp = bowe[i] + (mu *(bowe_Context[i]));
			if (temp == 0 ){
				bowe[i]=0;
			}else{
				bowe[i] = Math.log(temp);
			}
			i++;
		}
		return bowe;
	}
	public double[] initLMCOS(ArrayList<String> text) {
		int taille = vocabulaire.size();
		double[] bowe = new double[taille];
		for (int j=0;j<taille;j++){
			bowe[j] = 0;
		}
		int i=0;
		if(context_strat.equals("0")){
			for (ForIndexing w : vocabulaire)
			{
				for (String t : text){
					if (t.equals(w.getTerm())){
						bowe[i]+=1;
					}
				}
				i++;
			}
			bowe = Vector.normalise(bowe);
		}
		if(context_strat.equals("1")){
			for (ForIndexing w : vocabulaire)
			{
				for (String t : text){
					if (t.equals(w.getTerm())){
						bowe[i]+=1;
					}
				}
				i++;
			}
			bowe = Vector.normalise(bowe);
			i = 0;
			int numdoc = topic_model.getSortedDocuments(0).size();
			for (ForIndexing w : vocabulaire){
				int nbdoc_w = w.getNBDocs("all");
				if (nbdoc_w!=0){
					bowe[i] = bowe[i] * (numdoc/nbdoc_w);
				}else{
					bowe[i]=0;
				}
				i++;
			}
			bowe = Vector.normalise(bowe);
		}
		return bowe;
	}
	/**
	 * Method to generate a language Model from a context
	 */	
	public double[] initLMContext(int topic) {
		int taille = vocabulaire.size();
		double[] bowe = new double[taille];
		for (int j=0;j<taille;j++){
			bowe[j] = 0;
		}
		int i=0;
		int nbmot = 0;
		if (context_gen.equals("TD")){
			HashMap<String,MyDocument> topdocuments =Topdocs(topic_model.getSortedDocuments(0).get(topic), Integer.parseInt(context_strat));
			for (Map.Entry<String,MyDocument> mapentry : topdocuments.entrySet()) {
				MyDocument doc = mapentry.getValue();
				nbmot+=doc.getWordNumber();
			}
			for (ForIndexing w : vocabulaire)
			{
				double TF = 0;
				for (Map.Entry<String,MyDocument> mapentry : topdocuments.entrySet()) {
					MyDocument doc = mapentry.getValue();
					TF += doc.getTF(w.getTerm());
					//				System.out.println(TF);
				}
				bowe[i]=(TF/nbmot);
				i++;
			}
			bowe = Vector.normalise(bowe);
		}
		if(context_gen.equals("FULL")){
			for (ForIndexing w : vocabulaire)
			{
				double temp = topic_model.getProbaWord(w.getTerm());
				if (temp!=-1){
					bowe[i]=(temp);
					//					System.out.println(bowe[i]);
				}else{
					bowe[i]= 0;
				}

				i++;
			}	
			bowe = Vector.normalise(bowe);
		}
		return bowe;
	}
	/**
	 * Method to generate a language Model from a context
	 */	
	public double[] initLMContext(int topic,MyDocument doc) {
		int taille = vocabulaire.size();
		double[] bowe = new double[taille];
		int i=0;
		int nbmot = doc.getWordNumber();;
		for (ForIndexing w : vocabulaire)
		{
			double TF = doc.getTF(w.getTerm());
			bowe[i]=(TF/nbmot);
			i++;
		}
		return bowe;
	}
	/**
	 * Method to generate the distribution over words of the topics(more precisely a model)
	 * @param topic the index of the topic
	 * @return double[number of topics][length of vocabulary] ditribution over words of every topics
	 */	
	public double[] initLMTopic(int topic) {
		double[] result = new double[vocabulaire.size()];
		int i =0;
		for (ForIndexing w : vocabulaire){
			result[i] = topic_model.getProbaWordGivenTopic(w.getTerm(), topic);
			if (result[i]<0){
				result[i]=0;
			}
			i++;
		}
		return Vector.normalise(result);
	}










	////////////////////////////////Main for testing///////////////////////////////////////////


















	public static void main(String[] args) throws NoModel, IOException {
		//		loadData("C:/Users/antoi/git/topiclabeling/LDA/config.huffpostc.us");
		//		String model_file = "2017_05_19";
		//		loadData("C:/Users/antoi/git/topiclabeling/LDA/config.huffpostc.us");
		loadData("C:/Users/antoi/git/topiclabeling/LDA/config.HP.full");
		//		String model_file = "2017_05_19";
		String model_file = "2017_05_15";
		System.out.println("Initiate topic labeling");
		System.out.println("Load topic model: " + model_file);

		// load the first topic model in the directory
		LDATopicModel.loadTopicModels(model_file);
		topic_model = LDATopicModel.getFirstTopicModel();

		//		for (ForIndexing w : MonVocabulaire.getIndex().getActivatedFeatures())
		//		{
		//			if (w.getLength()<2){
		//				System.out.println(w.getTerm());
		//			}
		//		}
		long startTime;
		long estimatedTime;
		startTime = System.nanoTime();

		estimatedTime = System.nanoTime() - startTime;



		////Numero du topic
		int topic = 1;

		//		TD -> contexte = top doc, _n -> nombre de TD pris en compte
		//		FULL -> contexte = tt le corpus
		//			Dans ces deux cas : mu -> pondération de la distribution contextuelle
		//		COS -> mesure habituelle:
		//				_0 -> TF
		//				_1 -> TF*IDF


		System.out.println(" (" + TimeUnit.NANOSECONDS.toMillis(estimatedTime) + " ms)");
		startTime = System.nanoTime();		
		//		System.out.println();
		//		Sentence_Based_3 labeler001 = new Sentence_Based_3(topic_model, instances, model_file, 15,"TD_15",0.2);
		//		System.out.println("TD_15 0.2 : ");
		//		labeler001.computeLabel(topic);
		//		System.out.println();
		//		Sentence_Based_3 labeler002 = new Sentence_Based_3(topic_model, instances, model_file, 15,"TD_15",0.8);
		//		System.out.println("TD_15 0.8 : ");
		//		labeler002.computeLabel(topic);
		//		System.out.println();
		Sentence_Based_3 labeler1 = new Sentence_Based_3(topic_model, instances, model_file, 15,"TD_15",20);
		System.out.println("TD_15 20: ");
		labeler1.computeLabel(topic);
		System.out.println();
		//		Sentence_Based_3 labeler1856 = new Sentence_Based_3(topic_model, instances, model_file, 15,"TD_15",100);
		//		System.out.println("TD_15 100: ");
		//		labeler1856.computeLabel(topic);
		//		System.out.println();
		//		Sentence_Based_3 labeler101 = new Sentence_Based_3(topic_model, instances, model_file, 15,"TD_15",5000);
		//		System.out.println("TD_15 5000 : ");
		//		labeler101.computeLabel(topic);
		//		System.out.println();
		//		Sentence_Based_3 labeler2 = new Sentence_Based_3(topic_model, instances, model_file, 10,"TD_10",500);
		//		System.out.println("TD_10 500 : ");
		//		labeler2.computeLabel(topic);
		//		System.out.println();
		//		Sentence_Based_3 labeler21 = new Sentence_Based_3(topic_model, instances, model_file, 20,"TD_20",500);
		//		System.out.println("TD_20 500 : ");
		//		labeler21.computeLabel(topic);
		//		System.out.println();
		//		Sentence_Based_3 labeler212 = new Sentence_Based_3(topic_model, instances, model_file, 30,"TD_30",500);
		//		System.out.println("TD_30 500 : ");
		//		labeler212.computeLabel(topic);
		//		System.out.println();
		//		Sentence_Based_3 labeler3 = new Sentence_Based_3(topic_model, instances, model_file, 20,"FULL_0",500);
		//		System.out.println("FULL 500 : ");
		//		labeler3.computeLabel(topic);
		//		System.out.println();
		//		Sentence_Based_3 labeler66 = new Sentence_Based_3(topic_model, instances, model_file, 20,"LOC_20",500);
		//		System.out.println("LOC_20 500 : ");
		//		labeler66.computeLabel(topic);
		//		System.out.println();
		Sentence_Based_3 labeler4 = new Sentence_Based_3(topic_model, instances, model_file, 10,"COS_0",0);
		System.out.println("COS TF: ");
		labeler4.computeLabel(topic);
		//		System.out.println();
		//		Sentence_Based_3 labeler5 = new Sentence_Based_3(topic_model, instances, model_file, 10,"COS_1",0);
		//		System.out.println("COS TF-IDF : ");
		//		labeler5.computeLabel(topic);
		Sentence_Based_3 labelerl = new Sentence_Based_3(topic_model, instances, model_file, 20,"BS_0",0);
		System.out.println("BS labels : ");
		labelerl.computeLabel(topic);
		System.out.println();
		//		System.out.println();
		//		Sentence_Based_3 labeler6 = new Sentence_Based_3(topic_model, instances, model_file, 20,"LOC_0",1000000);
		//		System.out.println("LOC 1000000: ");
		//		labeler6.computeLabel(topic);
		//		Sentence_Based_3 labeler7 = new Sentence_Based_3(topic_model, instances, model_file, 20,"COS_0",0);
		//		System.out.println("COS TF on mix : ");
		//		labeler7.computeLabelTransv(5,1);
		//		System.out.println();
		//Labl Computataton
		//		try {
		//			labeler9.import_labels();
		//		} catch (ClassNotFoundException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}


		//labeler9.computeLabels();


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
		estimatedTime = System.nanoTime() - startTime;		
		System.out.println(" (" + TimeUnit.NANOSECONDS.toMillis(estimatedTime) + " ms)");


	}

	private void computeLabelTransv(int topic, int topic2) {
		////////ici init du bow du topic
		double[] topic_bow1  =initLMTopic(topic);
		double[] topic_bow2  =initLMTopic(topic2);
		double[] topic_bow ;



		//		if (context_strat.equals("0")){
		topic_bow = Vector.normalise(Vector.addi(topic_bow1 ,topic_bow2));
		//		}else{
		//			topic_bow = nana;
		//		}
		//		for (double temp : topic_bow){
		//			if(temp<0){
		//			System.out.print(temp+" ");}
		//		}

		TFSort res = new TFSort();

		//On recupere les top_docs (differents) du Topic topic et on crée le LM du contexte
		TFSort list_docs = topic_model.getSortedDocuments(0).get(topic);
		HashMap<String,MyDocument> topdocuments =Topdocs(list_docs, nbtopdoc);
		topdocuments.putAll(Topdocs(topic_model.getSortedDocuments(0).get(topic2), nbtopdoc));
		//		for (Map.Entry<String,MyDocument> mapentry : topdocuments.entrySet()) {
		//			System.out.println(mapentry.getValue().getText());
		//		}
		//on phrase les textes brut -> on obtient nos phrases candidates
		ArrayList<String> top_docs_phrased = cleanedDoc(topdocuments);

		////Cas COS -> cosine
		if (context_gen.equals("COS")){
			////////////////Pour chaque phrase :
			for (String TT : top_docs_phrased){
				//on clean
				ArrayList<String> tok_phrase =  tokenize_phr(TT);
				//On cree le BOW
				double[] bow_pn = initLMCOS(tok_phrase);
				//On calcule le membre a du score au topic
				double sum_a =utils.Vector.somme_produit(bow_pn,topic_bow);
				if (!Double.isNaN(sum_a)){
					res.add(TT, sum_a);
					//											System.out.println(TT);
					//											System.out.println(sum_a);
				}		

			}
		}
		if(context_gen.equals("TD") || context_gen.equals("FULL")){
			double[] lm_context = initLMContext(topic);
			//			System.out.println("contexte : " +Vector.norme(lm_context));
			////////////////Pour chaque phrase :
			for (String TT : top_docs_phrased){
				//on clean
				ArrayList<String> tok_phrase =  tokenize_phr(TT);
				//On cree le BOW
				double[] bow_p = initLM(tok_phrase,lm_context);
				//On calcule le membre a du score au topic
				double sum_a =utils.Vector.somme_produit(bow_p,topic_bow);
				if (!Double.isNaN(sum_a)){
					double score = sum_a - Math.log(tok_phrase.size()+mu);
					res.add(TT, score);
					//					System.out.println(TT);
					//					System.out.println(sum_a);
				}			

			}
		}

		////////////////

		//ON ressort les candidats triés
		int done = 0;
		TreeSet<TermValuePair> sorted_list = res.getList();
		Iterator<TermValuePair> iter = sorted_list.iterator(); 
		while ((iter.hasNext()) && (done < max_top_terms_calculated))
		{
			TermValuePair tvp = iter.next();
			TermValuePair tvpout = new TermValuePair(CleanWord.TCleaner(tvp.getTerm()),tvp.getValue());
			topicLabels[topic].add(tvpout);
			done++;
		}	
		int t = topicLabels[topic].size();
		//		for(int i=t-4;i<t;i++){
		for(int i=0;i<4;i++){
			System.out.println(topicLabels[topic].get(i).getTerm());
			System.out.println(topicLabels[topic].get(i).getValue());
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

}


