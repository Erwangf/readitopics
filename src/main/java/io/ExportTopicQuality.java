package io;

import java.util.ArrayList;
import java.util.Iterator;

import evaluation.TopicCoherence;

public class ExportTopicQuality extends Export {
		
	private int nbtopics;
	
	public ExportTopicQuality(int nbtopics)
	{
		super();
		this.nbtopics = nbtopics;
	}
				
	protected String export_csv(String filename)
	{
		StringBuffer out = new StringBuffer();
		out.append("id");
		for (String measure : TopicCoherence.measures)
			out.append("\t" + measure);
		out.append("\n");
		for (int topic=0; topic<nbtopics; topic++)
		{			
			out.append("topic " + topic);
			for (String measure : TopicCoherence.measures)
				out.append("\t" + TopicCoherence.getCoherence().getMeasure(measure, topic));
			out.append("\n");
		}
		return out.toString();
	}
	
	private void addTopic(StringBuffer sb, boolean sep, int i)
	{
		sb.append("\t\t{\n");
		sb.append("\t\t\"topicid\" : " + i +",\n");
		sb.append("\t\t\"qual\" : {\n");
		sb.append("\t\t\t");
		boolean flag = false;
		for (String measure : TopicCoherence.measures)
		{
			if (flag)
				sb.append(",");
			else
				flag = true;
			sb.append("\"" + measure + "\":");

			// If the coherence value is -Inf or +Inf, it will be printed as 0.0
			// Coherence value is -Inf when lucene index is not defined
			double val = TopicCoherence.getCoherence().getMeasure(measure, i);
			if(val == Double.NEGATIVE_INFINITY || val == Double.POSITIVE_INFINITY){
				val = 0;
			}
			sb.append(val);
		}
		sb.append("}\n");
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
		sb.append("\"numtopics\" : \"" + nbtopics + "\",\n");
		sb.append("\t\"topics\" : [\n");
		for (int i = 0; i < nbtopics; i++)
		{
			addTopic(sb, (i < nbtopics - 1), i);
		}
		sb.append("\t]\n}\n");
		return sb.toString();
	}		
	
}
