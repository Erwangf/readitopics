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

public class OneOrderLabeler extends TopicLabelerSkeleton
{
	
	/*public static final int EVEN_NORM = 1;
	public static final int FREQ_NORM = 2;	
	private int norm;*/
	
	private int smooth;
	
	private static final int DEFAULT_SMOOTHING = 0;
	
	public OneOrderLabeler(LDATopicModel topic_word, String dir)
			//ArrayList<TreeSet<IDSorter>> topicSortedWords, ArrayList<TFSort> topicSortedDocs)
	{
		super(topic_word, dir); //, topicSortedWords, topicSortedDocs);
		this.smooth = DEFAULT_SMOOTHING;
	}
	
	public void setSmoothing(int i)
	{
		smooth = i;
	}
	
	private double computePPMI(ForIndexing word, ForIndexing label)
	{
		int nbdocs_word = word.getNBDocs("all") + smooth;
		int nbdocs_label = label.getNBDocs("all") + smooth;
		int inter = label.intersect(word).getNBDocs("all") + smooth;
		if (inter > 0)
		{
			return Math.log((inter * (MyDocument.size()+smooth)) / (nbdocs_word * nbdocs_label));
		}
		else
			return 0;
	}
	
	private double computeWeightedPPMI(int i, ForIndexing word, ForIndexing label)
	{
		/*int ind = (int)dataAlphabet.lookupIndex(word.getTerm(), false);
		if (ind == -1)
			return -1;*/
		//double num = getLikelihood_word_byID(ind, i, -1);
		//double num = getProbaWord(word.getTerm(), i);
		double num = topic_word.getProbaWordGivenTopic(word.getTerm(), i);
		if (num == -1)
			return -1;
		double wppmi = num * computePPMI(word, label);
		//System.out.print(word.getTerm()+"+"+label.getTerm() + "=" + wppmi + "/" + num + "//");
		return num * wppmi;
	}
	
	private double computePPMI(int i, ForIndexing label)
	{
		double sum = 0;
		ArrayList<ForIndexing> list_words = MonVocabulaire.getIndex().getActivatedFeatures();
		for (ForIndexing w : list_words)
		{
			double ppmi = computeWeightedPPMI(i, w, label);
			if (ppmi > 0)
				sum += ppmi;
		}
		return sum;
	}
	
	private void computeLabel(int i)
	{
		// get the label candidates for topic i
		ArrayList<ForIndexing> c_for_topic = filter_candidates(i, num_words_for_filtering);
		TFSort res = new TFSort();
		for (ForIndexing c : c_for_topic)
		if (c.getLength() > 1) // filter ngrams with size > 1
		{
			double score = computePPMI(i, c);
			if (score > 0)
			{
				res.add(c.getTerm(), score);
			}
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
		return "one_order";
	}

	public String getShortName()
	{
		return "1o";
	}

}
