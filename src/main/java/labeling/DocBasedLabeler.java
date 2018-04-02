	package labeling;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;

import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.Alphabet;
import cc.mallet.types.IDSorter;
import core.ForIndexing;
import core.MonVocabulaire;
import core.MyDocument;
import core.TFSort;
import core.TermValuePair;
import topicmodeling.LDATopicModel;

public class DocBasedLabeler extends TopicLabelerSkeleton
{
		
	public static final int EVEN_NORM = 1;
	public static final int FREQ_NORM = 2;
	
	public static final TreeMap<Integer,String> map_norm = new TreeMap<>();
	
	private int norm;
	
	public DocBasedLabeler(LDATopicModel topic_word, String dir)
	{
		super(topic_word, dir); //, topicSortedWords, topicSortedDocs);
		this.norm = EVEN_NORM;
		map_norm.put(EVEN_NORM, "u");
		map_norm.put(FREQ_NORM, "n");
	}	
	
	public void setNorm(int i)
	{
		norm = i;
	}	
	
	private void computeLabel(int i)
	{
		// get the label candidates for topic i
		ArrayList<ForIndexing> c_for_topic;
		if (different_candidates)
			c_for_topic = candidates[i];
		else
			c_for_topic = filter_candidates(i, num_words_for_filtering);
		TFSort res = new TFSort();
		for (ForIndexing c : c_for_topic)
		{
			double score = 0;
			if (norm == FREQ_NORM)
				score = topic_word.getAvDocLogLikelihood(c, i, true);
			else
				score = topic_word.getAvDocLogLikelihood(c, i, false);
			if (score > 0)
				res.add(c.getTerm(), score);
		}
		int done = 0;
		TreeSet<TermValuePair> sorted_list = res.getList();
		Iterator<TermValuePair> iter = sorted_list.iterator(); 
		while ((iter.hasNext()) && (done < max_top_terms_calculated))
		{
			topicLabels[i].add(iter.next());
			done++;
		}		
	}
	
	@Override
	public void computeLabels()
	{
		for (int i=0; i<k; i++)
		{
			System.out.print(i+"-");
			computeLabel(i);
		}
	}
	
	public String getName()
	{
		return "doc_based_" + map_norm.get(norm);
	}
	
	public String getShortName()
	{
		return "d" + map_norm.get(norm);
	}

}
