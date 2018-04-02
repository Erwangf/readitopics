package core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import cc.mallet.types.Alphabet;
import cc.mallet.types.FeatureSequence;
import cc.mallet.types.Instance;

public class BuildFeatures4Mallet 
{
	
    private Alphabet dataAlphabet;
    
    public BuildFeatures4Mallet()
    {
    	dataAlphabet = new Alphabet(1000);
    }
    
    /* build the instance object for MALLET */
    public Instance addInstanceAsSequence(MyDocument d)
    {
    	/*TreeMap<String,ForIndexing> terms = d.getTermList();
    	TreeMap<String,Integer> tf = d.getTFList();
    	FeatureSequence fs = new FeatureSequence(dataAlphabet);
    	for (String mapKey : terms.keySet())
		{
			ForIndexing f = terms.get(mapKey);
			if (f.isActivated())
			{
				int ind = dataAlphabet.lookupIndex(f.getTerm(), true);
				for (int i=0; i<tf.get(mapKey).intValue(); i++)
				{
					fs.add(ind);
				}
			}
		}
        return new Instance(fs, d.getGround_truth(), d.getId(), d.getTitle());*/
    	return addInstanceAsSequence(d, null);
    }
    
    /* build the instance object for MALLET given a list of filtering words */
    public Instance addInstanceAsSequence(MyDocument d, ArrayList<ForIndexing> words)
    {
    	TreeMap<String,ForIndexing> terms = d.getTermList();
    	TreeMap<String,Integer> tf = d.getTFList();
    	FeatureSequence fs = new FeatureSequence(dataAlphabet);
    	for (String mapKey : terms.keySet())
		{
			ForIndexing f = terms.get(mapKey);
			if (f.isActivated() && (words ==null || words.contains(f)))
			{
				int ind = dataAlphabet.lookupIndex(f.getTerm(), true);
				for (int i=0; i<tf.get(mapKey).intValue(); i++)
				{
					fs.add(ind);
				}
			}
		}
        return new Instance(fs, d.getGround_truth(), d.getId(), d.getTitle());
    }
    
}
