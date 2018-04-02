package labeling;

import java.util.Iterator;
import java.util.TreeSet;

import core.Constantes;
import core.ForIndexing;

import core.MonVocabulaire;
import core.MyDocument;
import core.TFSort;
import core.TermValuePair;
import io.config.LoadConfigFile;
import io.LoadDataset;
import topicmodeling.LDATopicModel;

/**
 * A topic labeller based on selecting discriminative words with chi2
 * 
 * @author jvelcin
 *
 */
public class Chi2TopicLabeler extends TopicLabelerSkeleton
{

	public Chi2TopicLabeler(LDATopicModel topic_word, String dir)
	{
		super(topic_word, dir);
		MonVocabulaire.getIndex().desactivateAll();
		MonVocabulaire.getIndex().activateTerms(f -> f.getLength() == 1); // setActivated all words

		// desactivate stop words
		for (String sl : LoadConfigFile.getStopLists())
			if (!sl.isEmpty())
				MonVocabulaire.removeStopwords(LoadDataset.getPath() + Constantes.separateur + sl);		
	}

	// return the binary vector corresponding to the occurrences of topic i (p(z/w) > 0.001)
	private double[] get_binary_vector_docs(int i)
	{
		double[] distrib = new double[MyDocument.size()];
		for (int j = 0; j < MyDocument.size(); j++) {			
			double[] td = topic_word.getTopicProbabilities(j);
			if (td[i] > 0.001)
				distrib[j] = 1;
			else
				distrib[j] = 0;
		}
		return distrib;
	}
	
	// return the binary vector corresponding to the occurrences of term f
	private double[] get_binary_vector_docs(ForIndexing f)
	{
		double[] distrib = new double[MyDocument.size()];
		for (int j = 0; j < MyDocument.size(); j++)
			distrib[j] = 0;
		for (MyDocument doc : f.getDocs())
		{
			int lda_index = doc.getInternalIDForTM();
			distrib[lda_index] = 1;			
		}
		return distrib;
	}		
	
	public void computeLabel(int i)
	{
		TFSort res = new TFSort();
		double[] v_topic = get_binary_vector_docs(i);
		//for (ForIndexing f : MonVocabulaire.getAllTerms())
		for (ForIndexing f : MonVocabulaire.getActivatedTerms())
		if (f.getLength() == 1) // test all the words in my vocabulary
		{
			double[] v_term = get_binary_vector_docs(f);
			double score = compute_chi2(v_term, v_topic);
			if (score > 0)
				res.add(f.getTerm(), score);
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
	
	private double compute_chi2(double[] v_term, double[] v_topic)
	{
		double a = 0.0;
		double b = 0.0;
		double c = 0.0;
		double d = 0.0;
		double n = v_term.length;
		for (int j=0; j<n; j++)
		{
			if (v_term[j] == 1)
			{
				if (v_topic[j] == 1)
					a++;
				else
					b++;
			}
			else
			{
				if (v_topic[j] == 1)
					c++;
				else
					d++;
			}
		}
		double num = n * Math.pow(a*d - c*b, 2.0);
		double den = (a+c) * (b+d) * (a+b) * (c+d);
		if (den == 0)
		{
			/*System.out.println("a = " + a + " b = " + b + " c = " + c + " d = " + d);
			System.out.println("BAD: ZERO DIVISION!");
			System.exit(0);*/
			return 0;
		}
		return num/den;
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
		return "chi2";
	}

	public String getShortName()
	{
		return "chi2";
	}
	
}