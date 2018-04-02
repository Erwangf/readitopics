package topicmodeling;

public abstract class TopicAlphabet
{
	
	/*  get the size of the alphabet */
	public abstract int size();
	
	/* get the ID corresponding to the word w */
	public abstract int getIndex(String w);
	
	/* get the word corresponding to the id */
	public abstract String lookupObject(int id);
	
}
