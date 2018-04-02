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
import labeling.TopicLabelerSkeleton;
import topicmodeling.LDATopicModel;
import utils.DataUtils;

/* export the top doc following p(z/d) */

public class ExportTopdocs_pzd extends ExportDocs {
			
	
	public ExportTopdocs_pzd(LDATopicModel topic_word, InstanceList instances)
	{
		super(topic_word, instances);
		print_prob = true;
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
	
	private void addTopic(StringBuffer sb, boolean sep, int i)
	{
		sb.append("\t\t{\n");
		sb.append("\t\t\"topicid\" : " + i +",\n");
		sb.append("\t\t\"docs\" : [\n");
		TFSort list_docs = topic_model.compute_pzd(i, this.nb_printed_docs);
		Iterator<TermValuePair> iter = list_docs.getList().iterator();
		DataUtils cleaner = new DataUtils("");
		int nb = 0;		
		while (iter.hasNext())
		{
			TermValuePair t = iter.next();				
			MyDocument doc = MyDocument.get(t.getTerm());
			addDoc(sb, doc, t.getValue(), cleaner);			
			nb++;
			if (iter.hasNext())
				sb.append(",");
			sb.append("\n");
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
		sb.append("{\n\t");
		sb.append("\"numtopics\" : \"" + topic_model.numTopics() + "\",\n");
		sb.append("\t\"topics\" : [\n");
		for (int i = 0; i < topic_model.numTopics(); i++)
		{
			System.out.print(".");
			addTopic(sb, (i < topic_model.numTopics() - 1), i);
		}
		sb.append("\t]\n}\n");
		return sb.toString();
	}		
	
}
