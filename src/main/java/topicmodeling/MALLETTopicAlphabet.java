package topicmodeling;

import cc.mallet.types.Alphabet;

public class MALLETTopicAlphabet extends TopicAlphabet {

	private Alphabet dataAlphabet;
	
	public MALLETTopicAlphabet(Alphabet dataAlphabet)
	{
		this.dataAlphabet = dataAlphabet;
	}
	
	public int getIndex(String w)
	{
		return dataAlphabet.lookupIndex(w);
	}

	public int size()
	{
		return dataAlphabet.size();
	}
	
	public String lookupObject(int id)
	{
		return (String)dataAlphabet.lookupObject(id);		
	}
	
	
}
