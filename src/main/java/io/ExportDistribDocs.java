package io;

import java.util.TreeSet;
import java.io.IOException;
import java.util.Iterator;

import cc.mallet.types.InstanceList;
import core.*;
import io.config.LoadConfigFile;
import topicmodeling.LDATopicModel;
import utils.DataUtils;

public class ExportDistribDocs  extends Export {
	
	private LDATopicModel topic_model;
	
	private InstanceList instances;
	
	// num_topics = number of top topics exported (-1 if all topics)
	private int num_topics;
	
	// if true, also export the (lightly preprocessed) raw text
	private boolean with_text = true;
	private DataUtils du;	
	
	public ExportDistribDocs(LDATopicModel topic_word, InstanceList instances, int num_topics)
	{
		super();
		this.topic_model = topic_word;
		this.instances = instances;
		this.num_topics = num_topics;
		if (with_text) {
			try {
				StopWords.loadTextFile(LoadDataset.getPath() + Constantes.separateur + LoadConfigFile.getStopLists()[0]);
			} catch (IOException e) {
				e.printStackTrace();
			}
			du = new DataUtils("tolower,punct,stopwords");
		}
	}
	
	protected String export_csv(String filename)
	{		
		StringBuffer out = new StringBuffer();
		for (int i=0; i<instances.size(); i++)
		{
			String name = (String)instances.get(i).getName();
			out.append("doc\t" + name + "\t");
			double[] proba = topic_model.getTopicProbabilities(i);
			for (int j=0; j<proba.length; j++)
				out.append(proba[j] + "\t");
			out.append("\n");
		}
		return out.toString();
	}	

	private void addDoc(StringBuffer sb, boolean sep, int i)
	{
		sb.append("\t\t{\n");
		String name = (String)instances.get(i).getName();
		sb.append("\t\t\"docname\" : \"" + name +"\",\n");
		if (with_text) {
			MyDocument d =  MyDocument.get(name);
			String t = CleanWord.clean(d.getText());
			sb.append("\t\t\"text\" : \"" + t +"\",\n");
		}
		sb.append("\t\t\"topics_prop\" : [\n");		
		double[] proba = topic_model.getTopicProbabilities(i);
		TFSort mylist = null;
		if (num_topics != -1)
			mylist = new TFSort(num_topics); // limit to the top num_topics
		else
			mylist = new TFSort(); // we keep all the topics
		for (int j=0; j<proba.length; j++)
			mylist.add(""+j, proba[j]);	
		TreeSet<TermValuePair> ts = mylist.getList();
		Iterator<TermValuePair> iter = ts.iterator();
		while (iter.hasNext())
		{
			TermValuePair t = iter.next();
			sb.append("\t\t\t{\"z\" : \"" +  t.getTerm() + "\", \"proba\" : \"" + t.getValue() + "\"}");
			if (iter.hasNext())
				sb.append(",");
			sb.append("\n");
		}
		/*for (int j=0; j<proba.length; j++)
		{
			sb.append("\t\t\t{\"z\" : \"" +  j + "\", \"proba\" : \"" + proba[j] + "\"}");
			if (j < (proba.length - 1))
				sb.append(",");
			sb.append("\n");
			//nb++;
		}*/
		sb.append("\t\t\t]\n\t\t}");
		if (sep) {
			sb.append(",");
		}
		sb.append("\n");
	}	
	
	protected String export_json(String filename)
	{
		StringBuffer sb = new StringBuffer();
		sb.append("{\n\t");
		sb.append("\"numdocs\" : \"" + instances.size() + "\",\n");
		sb.append("\t\"docs\" : [\n");
		for (int i=0; i<instances.size(); i++)
		{
			addDoc(sb, (i < instances.size() - 1), i);
		}
		sb.append("\t]\n}\n");
		return sb.toString();
	}		

}
