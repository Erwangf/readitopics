package io;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;

import core.CleanWord;
import topicmodeling.LDATopicModel;
import utils.DataUtils;

public class ExportTopicWord extends Export {
	
	private LDATopicModel topic_model;
	
	private int nb_printed_words;
	
	public ExportTopicWord(LDATopicModel topic_word)
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
		TreeSet<Integer> list_id_words = new TreeSet<>();
		StringBuffer out = new StringBuffer();
		for (int i=0; i<topic_model.numTopics(); i++)
		{			
			ArrayList<Integer> sortedWords = topic_model.getTopWords(i);
			Iterator<Integer> iterator = sortedWords.iterator();
			int nb = 0;
			while (iterator.hasNext() && (nb < nb_printed_words))
			{
				int id = iterator.next();
				list_id_words.add(id);
				nb++;				
			}
		}
		// export the number of different words
		int numwords = list_id_words.size();
		out.append("numwords\t" + numwords + "\n");
		// export the correspondance: id - word
		int nb = 0;
		for (Integer i : list_id_words) {
			String word = topic_model.getAlphabet().lookupObject(i);
			out.append(nb + "\t" + word + "\n");
			nb++;
		}
		// export p(w/z)
		for (int i=0; i<topic_model.numTopics(); i++) {
			for (Integer j : list_id_words)
			{
					String weight = "" + topic_model.getProbaWordGivenTopic(j, i);
					out.append(weight + "\t");
			}
			out.append("\n");
		}
		return out.toString();
	}
		
}
