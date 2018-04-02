package exe;

import java.io.IOException;

import cc.mallet.types.InstanceList;
import topicmodeling.LDATopicModel;
import topicmodeling.NoModel;

public class TestTopCoh
{

	/* Instances of MALLET */
	private static InstanceList instances;

	private static LDATopicModel topic_model;
	
	
	public static void main(String[] args) throws IOException, NoModel
	{
//		if (args.length < 2)
//			throw new IOException("Require at least 2 arguments: configuration file + model directory");
//		
		String conf ="C:/Users/antoi/git/topiclabeling/LDA/config.articles";
		BrowseTopics.load("data " + conf);
		
//		String path_server = "WebServer" + Constantes.separateur + "datasets" + Constantes.separateur + LoadDataset.getDataName();
//		String conf2 = "C:/Users/antoi/git/topiclabeling/resultats/dataconf/models/2017_09_04/model.jsonp";
		
		String model_file = "2017_09_04";
		
		//System.out.println("Initiate topic labeling");
		System.out.println("Load topic model: " + model_file);

		// load topic model
		BrowseTopics.load("topics " + model_file);
		
		// we need to provide both the topic model + the instance lists for the labeling and analytics
		topic_model = BrowseTopics.getModel(); 
		instances = BrowseTopics.getInstances();
		
//		// run labeling
//		TopicLabeling.compute_labeling(topic_model, instances, model_file);
//		
//		// load labeling
//		BrowseTopics.load("labels " + model_file);

		
		// create empty "embeddings" folder
//		new File(path_server + Constantes.separateur + "embeddings").mkdirs();
		
//		// run analytics
		TopicAnalytics.analyze(topic_model, model_file);
//			
//		BrowseTopics.set("maxdocs 30");
//		BrowseTopics.set("maxwords 30");
//		BrowseTopics.set("maxtopics 5");
//		BrowseTopics.export("labels labels.json");
//		BrowseTopics.export("topdocs ngrams topdocs.json");
//		BrowseTopics.export("topdocs pzd topdocs_pzd.json");
//		BrowseTopics.set("maxdocs 5");
//		BrowseTopics.set("maxtopics 20");
//		BrowseTopics.export("docbetween topdocs_pairs.json");
//		BrowseTopics.set("maxdocs 30");
//		BrowseTopics.set("maxtopics 5");
//		BrowseTopics.export("topics model.json");		
//		BrowseTopics.export("distrib top theta_toptopics.json");
//		BrowseTopics.export("cor d cor_doc_based.csv");
//		BrowseTopics.export("cor w cor_word_based.csv");
//		BrowseTopics.export("cor d cor_doc_based.json");
//		BrowseTopics.export("cor w cor_word_based.json");
//		BrowseTopics.export("topics topics.csv");
//		BrowseTopics.export("distrib theta.csv");		
		BrowseTopics.export("qual coherence.json");
//		BrowseTopics.export("pz pz.csv");
		
	}

}
