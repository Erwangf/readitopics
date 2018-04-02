package io;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.Iterator;
import java.util.Locale;
import topicmodeling.LDATopicModel;

public class ExportTopicAnalytics extends Export {
	
	private int nbtopics;
	private LDATopicModel topic_model;
	private String type;
	
	public ExportTopicAnalytics(LDATopicModel model, String type)
	{
		super();
		this.topic_model = model;
		this.nbtopics = model.numTopics();
		this.type = type;
	}
				
	protected String export_csv(String filename)
	{
		Formatter out = new Formatter(new StringBuilder(), Locale.US);
		for (int i=0; i<nbtopics; i++)
			out.format("\tz" + i);
		out.format("\n");
		for (int i=0; i<nbtopics; i++)
		{
			out.format("z" + i);
			for (int j=0; j<nbtopics; j++)
			{
				double c = 1;
				if (i != j)
					c = topic_model.getCorrelation(type, i, j);
				out.format("\t%.5f", c);
			}
			out.format("\n");
		}				
		return out.toString();
	}
	
	private void addTopic(StringBuffer sb, boolean sep, int i)
	{
		sb.append("\t\t{\n");
		sb.append("\t\t\"topicid\" : " + i +",\n");
		sb.append("\t\t\"doc_cor\" : [\n");
		sb.append("\t\t\t");		
		for (int j=0; j<nbtopics; j++)
		{
			double c = 1;
			if (i != j)
				c = topic_model.getCorrelation(type, i, j);
			sb.append(c);
			if (j<(nbtopics-1))
				sb.append(",");
		}
		sb.append("]\n");
		sb.append("\t\t}");
		if (sep) {
			sb.append(",");
		}
		sb.append("\n");
	}	
	
	protected String export_json(String filename)
	{
		StringBuffer sb = new StringBuffer();
		sb.append("{\n\t");
		sb.append("\"numtopics\" : \"" + topic_model.numTopics() + "\",\n");
		sb.append("\t\"topics\" : [\n");
		for (int i = 0; i < topic_model.numTopics(); i++)
		{
			addTopic(sb, (i < topic_model.numTopics() - 1), i);
		}
		sb.append("\t]\n}\n");
		return sb.toString();
	}		

}
