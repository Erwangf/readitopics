package labeling;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.Alphabet;
import cc.mallet.types.IDSorter;
import core.TFSort;
import core.TermValuePair;
import topicmodeling.LDATopicModel;

/**
 * A very naive topic labeller that concatenate the first 3 word topics. Dummy, he ?
 * 
 * @author cgravier
 *
 */
public class NaiveTopicLabeler extends TopicLabelerSkeleton
{

	public NaiveTopicLabeler(LDATopicModel topic_word, String dir)
			//ArrayList<TreeSet<IDSorter>> topicSortedWords, ArrayList<TFSort> topicSortedDocs)
	{
		super(topic_word, dir); //, topicSortedWords, topicSortedDocs);
	}

	public void computeLabel(int i)
	{
		int done = 0;
		Iterator<Integer> iter = topic_word.getTopWords(i).iterator();
		while (done < max_top_terms_calculated)
		{
			Integer id = iter.next();
			double p = topic_word.getProbaWordGivenTopic(id,  i);
			String word = topic_word.getAlphabet().lookupObject(id);
			topicLabels[i].add(new TermValuePair(word, p));
			done++;
		}
	}
	
	@Override
	public void computeLabels()
	{
		for (int i=0; i<k; i++)
		{
			computeLabel(i);
		}		
	}

	public String getName()
	{
		return "naive";
	}

	public String getShortName()
	{
		return "na";
	}
	
}