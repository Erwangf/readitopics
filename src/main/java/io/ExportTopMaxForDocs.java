package io;

import java.util.TreeSet;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Iterator;
import java.util.Locale;

import cc.mallet.types.InstanceList;
import core.MyDocument;
import core.TFSort;
import core.TermValuePair;
import topicmodeling.LDATopicModel;
import topicmodeling.NoModel;

/* export pour le projet d'évaluation Ratineau-Cabanac-Thonet */

public class ExportTopMaxForDocs extends Export {
	
	private LDATopicModel topic_model;
	
	private InstanceList instances;
		
	public ExportTopMaxForDocs(LDATopicModel topic_word, InstanceList instances)
	{
		super();
		this.topic_model = topic_word;
		this.instances = instances;
	}
	
	protected String export_csv(String filename)
	{		
		StringBuffer out = new StringBuffer();
		int[] toptopics = new int[instances.size()];
		for (int i=0; i<instances.size(); i++) {
			int top_topic = toptopic(i);
			String name = (String)instances.get(i).getName();
			/* particular case: we have to strip out the beginning of the name until "-"
			 * (see the generation of "id/name" in the folder.book setting
			 */
			int id_doc = Integer.parseInt(name.split(".txt-")[1]);
			toptopics[id_doc-1] = top_topic;
			//System.out.print((id_doc-1) + ":" + top_topic + ";");
		}
		for (int i=0; i<instances.size(); i++) {
			out.append(toptopics[i] + "\n");
		}
		return out.toString();
	}	
	
	/* return the (only one) top topic for document number int (pay attention: id from LDA instances */ 
	private int toptopic(int doc)
	{
		double[] topicDistribution = topic_model.getTopicProbabilities(doc);
		TFSort sort = new TFSort();
		for (int j=0; j<topicDistribution.length; j++)
			if (topicDistribution[j] > 0)
				sort.add(""+j, topicDistribution[j]);			
		TreeSet<TermValuePair> list = sort.getList();
		Iterator<TermValuePair> iter = list.iterator();
		TermValuePair t = iter.next();
		return Integer.parseInt(t.getTerm());
	}	
}
