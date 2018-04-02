package io;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;

import core.CleanWord;
import topicmodeling.LDATopicModel;
import utils.DataUtils;

public class ExportTopics extends Export {
	
	private LDATopicModel topic_model;
	
	private int nb_printed_words;
	
	public ExportTopics(LDATopicModel topic_word)
	{
		super();
		this.topic_model = topic_word;
		nb_printed_words = 10; // default value
		print_prob = true;
	}
	
	public void setPrintWords(int n)
	{
		this.nb_printed_words = n;
	}
		
	protected String export_csv(String filename)
	{		
		StringBuffer out = new StringBuffer();
		for (int i=0; i<topic_model.numTopics(); i++)
		{			
			ArrayList<Integer> sortedWords = topic_model.getTopWords(i);
			Iterator<Integer> iterator = sortedWords.iterator();
			out.append("topic " + i + "\t" + topic_model.get_pz("all", i) + "\t");
			int nb = 0;
			while (iterator.hasNext() && (nb < nb_printed_words))
			{
				int id = iterator.next();
				String word = topic_model.getAlphabet().lookupObject(id);
				String weight = "" + topic_model.getProbaWordGivenTopic(id, i);
				out.append(word + "\t");
				if (!this.print_prob)
					out.append(weight + "\t");
				nb++;				
			}
			out.append("\n");
		}
		return out.toString();
	}
	
	private void addTopic(StringBuffer sb, boolean sep, int i)
	{
		sb.append("\t\t{\n");
		sb.append("\t\t\"topicid\" : " + i +",\n");
		TreeMap<String,Double> list_p = topic_model.get_pz(i);
		double total_num = list_p.get("all");
		sb.append("\t\t\"size\" : " + total_num +",\n");
		sb.append("\t\t\"tokens\" : [\n");		
		ArrayList<Integer> sortedWords = topic_model.getTopWords(i);
		Iterator<Integer> iterator = sortedWords.iterator();
		int nb = 0;
		while (iterator.hasNext() && nb < nb_printed_words)
		{
			int id = iterator.next();
			String word = topic_model.getAlphabet().lookupObject(id);
			String weight = ""+topic_model.getProbaWordGivenTopic(id, i);
			sb.append("\t\t\t{\"mot\" : \"" +  CleanWord.TCleaner(word) + "\", \"proba\" : \"" + weight + "\"}");
			if (iterator.hasNext() && nb < (nb_printed_words-1))
				sb.append(",");
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
		sb.append("{\n\t");
		sb.append("\"numtopics\" : \"" + topic_model.numTopics() + "\",\n");
		sb.append("\t\"topics\" : [\n");
		for (int i = 0; i < topic_model.numTopics(); i++)
		{
			addTopic(sb, (i < topic_model.numTopics() - 1), i);
		}
		sb.append("\t]\n}\n");
		return sb.toString();
	}	
	
}
