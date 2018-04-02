package io;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

import cc.mallet.types.InstanceList;
import core.CleanWord;
import core.ForIndexing;
import core.MonVocabulaire;
import core.MyDocument;
import core.TFSort;
import core.TermValuePair;
import exe.BrowseTopics;
import labeling.TopicLabelerSkeleton;
import topicmodeling.LDATopicModel;
import topicmodeling.NoModel;
import utils.DataUtils;

public class ExportTopdocsBetween extends Export {
		
	private LDATopicModel topic_model;
	private InstanceList instances;
	
	private int nb_printed_docs;
	private int max_top_topics;
	
	private ArrayList<TopicLabelerSkeleton> labelers;
	
	public ExportTopdocsBetween(LDATopicModel topic_word, InstanceList instances)
	{
		super();
		this.topic_model = topic_word;
		this.instances = instances;
		nb_printed_docs = 10; // default value
		max_top_topics = 10;
		print_prob = true;
	}
	
	public void setPrintDocs(int n)
	{
		this.nb_printed_docs = n;
	}	
	
	public void setPrintTopics(int n)
	{
		this.max_top_topics = n;
	}
	
	public void setNgrams(ArrayList<TopicLabelerSkeleton> lab)
	{
		this.labelers = lab;
	}
	
	/*protected String export_csv(String filename)
	{
		StringBuffer out = new StringBuffer();
		for (int topic=0; topic<topic_model.numTopics(); topic++)
		{							
			out.append("topic " + topic + "\t" + topic_model.get_pz("all", topic) + "\n");
			int nb = 0;
			TFSort list_docs = topic_model.getSortedDocuments(0).get(topic);
			Iterator<TermValuePair> iter = list_docs.getList().iterator();
			while ((iter.hasNext()) && (nb < this.nb_printed_docs))
			{
				TermValuePair t = iter.next();				
				int id_doc = Integer.parseInt(t.getTerm());
				String name = (String)instances.get(id_doc).getName();
				MyDocument doc = MyDocument.get(name);
				out.append(doc.getTitle());				
				if (!print_prob)
					out.append(" (" + t.getValue() + ")");
				out.append("\n");
				nb++;
			}
			//System.out.println(out);
		}
		return out.toString();
	}*/
	
	private void addDoc(StringBuffer sb, MyDocument doc, double proba, DataUtils cleaner)
	{	
		sb.append("\t\t\t{");
		sb.append("\"name\" : \"" + CleanWord.TCleaner(doc.getId()) + "\"");
		sb.append(",\n");
		sb.append("\t\t\t\t");
		String text = cleaner.protectQuotes(doc.getText());
		sb.append("\"text\" : \"" + CleanWord.TCleaner(text) + "\"");
		sb.append(",\n");
		sb.append("\t\t\t\t");
		String author = cleaner.protectQuotes(doc.getAuthor());
		sb.append("\"author\" : \"" + CleanWord.TCleaner(author) + "\"");
		sb.append(",\n");
		sb.append("\t\t\t\t");
		String time = cleaner.protectQuotes(doc.getDate());			
		sb.append("\"time\" : \"" + time + "\"");
		sb.append(",\n");
		sb.append("\t\t\t\t");
		sb.append("\"proba\" : \"" + proba + "\"");
		sb.append(",\n");
		sb.append("\t\t\t\t");
		sb.append("\"index\" : {");
		ArrayList<ForIndexing> activatedWords = MonVocabulaire.getActivatedTerms();
		boolean flag = false;
		for (ForIndexing token : activatedWords) {
			String term = token.getTerm();
			int tf = doc.getTF(term);
			if (tf>0)
			{
				if (flag)
					sb.append(",");
				else
					flag = true;
				sb.append("\"" + CleanWord.TCleaner(term) + "\":\"");
				sb.append(tf);
				sb.append("\"");
			}
		}
		sb.append("}}");
	}
	
	private void addTopicPair(StringBuffer sb, int i, int j)
	{
		sb.append("\t\t{\n");
		sb.append("\t\t\"topicid\" : \"" + i + "-" + j + "\",\n");
		sb.append("\t\t\"docs\" : [\n");
		TFSort list_docs = topic_model.get_pdz_ij(i, j);
		if (list_docs != null)
		{
			Iterator<TermValuePair> iter = list_docs.getList().iterator();
			DataUtils cleaner = new DataUtils("");
			int nb = 0;		
			while ((iter.hasNext()) && (nb < this.nb_printed_docs))
			{
				TermValuePair t = iter.next();				
				int id_doc = Integer.parseInt(t.getTerm());
				String name = (String)instances.get(id_doc).getName();
				MyDocument doc = MyDocument.get(name);
				addDoc(sb, doc, t.getValue(), cleaner);
				if ((iter.hasNext()) && (nb < (this.nb_printed_docs-1)))
					sb.append(",");
				sb.append("\n");
				nb++;			
			}
		}
		sb.append("\t\t\t]\n\t\t}");
	}
	
	private void addBestTopicPairs(StringBuffer sb, boolean sep, int i)
	{
		TFSort list_sorted = null;
		try {
			list_sorted = BrowseTopics.get_top_correlated_topics(i, "docbased");
		} catch (NoModel e) {	}
		Iterator<TermValuePair> iter = list_sorted.getList().iterator();
		int nb = 0;
		while (iter.hasNext() && (nb < max_top_topics))
		{
			TermValuePair t = iter.next();
			int j = Integer.parseInt(t.getTerm());
			if (i != j)
			{
				addTopicPair(sb, i, j);
				if (!sep || ((iter.hasNext() && (nb < (max_top_topics-1)))))
					sb.append(",");
				sb.append("\n");
				nb++;
			}
		}
	}
	
	protected String export_json(String filename)
	{
		StringBuffer sb = new StringBuffer();
		sb.append("{\n\t");
		sb.append("\t\"topics\" : [\n");
		for (int i = 0; i < topic_model.numTopics(); i++)
		{
			System.out.print(".");
			addBestTopicPairs(sb, (i == (topic_model.numTopics() - 1)), i);
		}
		sb.append("\t]\n}\n");
		return sb.toString();
	}		
	
}
