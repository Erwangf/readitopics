package labeling;

import java.util.ArrayList;
import java.util.Iterator;
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

public class ZeroOrderLabeler extends TopicLabelerSkeleton
{
	
	public static final int EVEN_NORM = 1;
	public static final int FREQ_NORM = 2;
	
	private int norm;
	
	public ZeroOrderLabeler(LDATopicModel topic_word, String dir)
			//ArrayList<TreeSet<IDSorter>> topicSortedWords, ArrayList<TFSort> topicSortedDocs)
	{
		super(topic_word, dir); //, topicSortedWords, topicSortedDocs);
		this.norm = EVEN_NORM;
	}
	
	public void setNorm(int i)
	{
		norm = i;
	}
	
	private void computeLabel(int i)
	{
		//System.out.println("topic " + i);
		// get the label candidates for topic i
		ArrayList<ForIndexing> c_for_topic;
		if (different_candidates)
			c_for_topic = candidates[i];
		else
			c_for_topic = filter_candidates(i, num_words_for_filtering);
		TFSort res = new TFSort();
		for (ForIndexing c : c_for_topic)
		{
			//System.out.print("add:"+c.getTerm()+"=");
			String[] split = c.getTerm().split(" ");
			double sum = 0;
			for (String w : split)
			{
				//int ind = (int)dataAlphabet.lookupIndex(w, false);
				//double num = getLikelihood_word_byID(ind, i, num_words_for_filtering);
				//double num = getProbaWord(w, i);
				double num = topic_word.getProbaWordGivenTopic(w, i);
				if (num == -1)
					continue;
				double den;
				if (norm == EVEN_NORM)
					den = 1.0 / topic_word.getAlphabet().size();
				else
				{
					den = (double)MonVocabulaire.getIndexTerm(w).getNBDocs("all") / (double)MyDocument.size();
					//System.out.print(den+";;");
				}
				if ((num != 0) && (den != 0))
				{
					sum += Math.log(num/den);
					//System.out.print(num+"/"+den+";;");
				}
			}
			/*if (sum > 0)
			{*/
				res.add(c.getTerm(), sum);
				
			//}
		}
		//System.out.println();
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
	public void computeLabels() {
		for (int i=0; i<k; i++)
		{
			computeLabel(i);
		}
	}
	
	public String getName()
	{
		return "zero_order_" + norm;
	}
	
	public String getShortName()
	{
		//return "0" + norm;
		String s = "u";
		if (norm == FREQ_NORM)
			s = "n";
		return "0"+s;
	}


}
