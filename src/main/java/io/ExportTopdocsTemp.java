package io;

import java.util.ArrayList;
import java.util.Iterator;
import cc.mallet.types.InstanceList;
import core.ForIndexing;
import core.MonVocabulaire;
import core.MyDocument;
import core.TFSort;
import core.TermValuePair;
import topicmodeling.LDATopicModel;
import utils.DataUtils;

public class ExportTopdocsTemp extends Export {
		
	private LDATopicModel topic_model;
	private InstanceList instances;
	
	private int nb_printed_docs;
	
	public ExportTopdocsTemp(LDATopicModel topic_word, InstanceList instances)
	{
		super();
		this.topic_model = topic_word;
		this.instances = instances;
		nb_printed_docs = 10; // default value
		print_prob = true;
	}
	
	public void setPrintDocs(int n)
	{
		this.nb_printed_docs = n;
	}	
	
	protected String export_csv(String filename)
	{
		StringBuffer out = new StringBuffer();
		out.append("topic" + "\t" + "doc_id" + "\t" + "pdz" + "\t" + "title" + "\t" + "date" + "\t" + "period\n");
		for (int topic=0; topic<topic_model.numTopics(); topic++)
		{							
			/*out.append("topic " + topic + "\t" + topic_model.get_pz("all", topic) + "\n");*/
			int nb = 0;
			TFSort list_docs = topic_model.getSortedDocuments(0).get(topic);
			Iterator<TermValuePair> iter = list_docs.getList().iterator();
			while ((iter.hasNext()) && (nb < this.nb_printed_docs))
			{
				TermValuePair t = iter.next();				
				int id_doc = Integer.parseInt(t.getTerm());
				String name = (String)instances.get(id_doc).getName();
				MyDocument doc = MyDocument.get(name);
				//out.append(doc.getTitle());
				out.append(topic + "\t" + id_doc + "\t" + t.getValue() + "\t" + doc.getTitle() + "\t" + doc.getDate() + "\t" + doc.getPeriod());
				out.append("\n");
				nb++;
			}
			//System.out.println(out);
		}
		return out.toString();
	}
	
	/*private void addTopic(StringBuffer sb, boolean sep, int i)
	{
		sb.append("\t\t{\n");
		sb.append("\t\t\"topicid\" : " + i +",\n");
		sb.append("\t\t\"docs\" : [\n");		
		TFSort list_docs = topic_model.getSortedDocuments(0).get(i);
		Iterator<TermValuePair> iter = list_docs.getList().iterator();
		DataUtils cleaner = new DataUtils("");
		int nb = 0;
		while ((iter.hasNext()) && (nb < this.nb_printed_docs))
		{
			TermValuePair t = iter.next();				
			int id_doc = Integer.parseInt(t.getTerm());
			String name = (String)instances.get(id_doc).getName();
			MyDocument doc = MyDocument.get(name);
			sb.append("\t\t\t{");
			sb.append("\"name\" : \"" + name + "\"");
			sb.append(",\n");
			sb.append("\t\t\t\t");
			String text = cleaner.protectQuotes(doc.getText());
			sb.append("\"text\" : \"" + text + "\"");
			sb.append(",\n");
			sb.append("\t\t\t\t");
			String author = cleaner.protectQuotes(doc.getAuthor());
			sb.append("\"author\" : \"" + author + "\"");
			sb.append(",\n");
			sb.append("\t\t\t\t");
			String time = cleaner.protectQuotes(doc.getDate());			
			sb.append("\"time\" : \"" + time + "\"");
			sb.append(",\n");
			sb.append("\t\t\t\t");
			sb.append("\"proba\" : \"" + t.getValue() + "\"");
			sb.append(",\n");
			sb.append("\t\t\t\t");
			sb.append("\"index\" : {");
			ArrayList<ForIndexing> activatedWords = MonVocabulaire.getActivatedTerms();
			boolean flag = false;
			for (ForIndexing token : activatedWords) {
				String term = token.getTerm();
				//int tf = doc.getNBwords(token);
				int tf = doc.getTF(term);
				if (tf>0)
				{
					if (flag)
						sb.append(",");
					else
						flag = true;
					sb.append("\"" + term + "\":\"");
					sb.append(tf);
					sb.append("\"");
				}
			}
			sb.append("}}");			
			if (iter.hasNext() && nb < (this.nb_printed_docs-1))
			{
				sb.append(",");
			}
			sb.append("\n");
			nb++;			
		}
		sb.append("\t\t\t]\n\t\t}");
		if (sep) {
			sb.append(",");
		}
		sb.append("\n");
	}	
	
	protected String export_json(String filename)
	{
		StringBuffer sb = new StringBuffer();
		sb.append("jsonstr = {\n\t");
		sb.append("\"numtopics\" : \"" + topic_model.numTopics() + "\",\n");
		sb.append("\t\"topics\" : [\n");
		for (int i = 0; i < topic_model.numTopics(); i++)
		{
			System.out.print(".");
			addTopic(sb, (i < topic_model.numTopics() - 1), i);
		}
		sb.append("\t]\n}\n");
		return sb.toString();
	}		*/
	
}
