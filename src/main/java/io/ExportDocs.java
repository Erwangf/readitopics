package io;

import java.util.ArrayList;

import cc.mallet.types.InstanceList;
import labeling.TopicLabelerSkeleton;
import topicmodeling.LDATopicModel;

public class ExportDocs extends Export {
	
	protected LDATopicModel topic_model;
	protected InstanceList instances;
	protected int nb_printed_docs;
	protected ArrayList<TopicLabelerSkeleton> labelers;
	
	public ExportDocs(LDATopicModel topic_word, InstanceList instances)
	{
		super();
		this.topic_model = topic_word;
		this.instances = instances;
		nb_printed_docs = 10; // default value
	}

	public void setPrintDocs(int n)
	{
		this.nb_printed_docs = n;
	}	
	
	public void setNgrams(ArrayList<TopicLabelerSkeleton> lab)
	{
		this.labelers = lab;
	}

}
