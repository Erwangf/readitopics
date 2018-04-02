package labeling;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.Alphabet;
import cc.mallet.types.IDSorter;
import core.ForIndexing;
import core.MyDocument;
import core.TFSort;
import core.TermValuePair;
import topicmodeling.LDATopicModel;

public class NormDocBasedLabeler extends TopicLabelerSkeleton
{
				
	public NormDocBasedLabeler(LDATopicModel topic_word, String dir) 
			//ArrayList<TreeSet<IDSorter>> topicSortedWords, ArrayList<TFSort> topicSortedDocs)
	{
		super(topic_word, dir); //, topicSortedWords, topicSortedDocs);
	}	
	
	/* return the maximum value of array, but ignoring cell index i */
	private double max(double[] array, int i)
	{
		double max = -1;
		for (int j=0; j<array.length; j++)
		if (j != i)
		{
			if (array[j] > max)
			max = array[j];
		}
		return max;
	}
	
	private void computeLabel(int i)
	{
		// get the label candidates for topic i
		ArrayList<ForIndexing> c_for_topic = filter_candidates(i, num_words_for_filtering);
		TFSort res = new TFSort();
		for (ForIndexing c : c_for_topic)
		{
			int nb = c.getDocs().size();
			if (nb == 0)
				continue;
			double score = 0;
			TreeSet<MyDocument> list_docs = c.getDocs(); 
			Iterator<MyDocument> iter = list_docs.iterator();
			while (iter.hasNext())
			{
				MyDocument doc = iter.next();
				double[] pz_all = topic_word.getTopicProbabilities(doc.getInternalIDForTM());
				double pz_d = pz_all[i];
				double pz_d_comp = max(pz_all, i);
				score += Math.log(pz_d) - Math.log(pz_d_comp);
			}
			res.add(c.getTerm(), score);
			/*if (score > 0)
			{
				res.add(c.getTerm(), score);
				System.out.print(c + ":" + score + "//");
			}*/
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
		return "norm_doc_based";
	}
	
	public String getShortName()
	{
		return "nd";
	}

}
