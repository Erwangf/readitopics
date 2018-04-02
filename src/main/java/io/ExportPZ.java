package io;

import java.util.TreeMap;

import core.MyDate;
import core.MyDocument;
import topicmodeling.LDATopicModel;

public class ExportPZ extends Export {
	
	private int nbtopics;
	private LDATopicModel topic_model;
	
	public ExportPZ(LDATopicModel model)
	{
		super();
		this.topic_model = model;
		this.nbtopics = model.numTopics();
	}
				
	protected String export_csv(String filename)
	{
		StringBuffer out = new StringBuffer();
		out.append("dates\t");
		for (MyDate key : MyDocument.getSortedDates())
			out.append(key.toString() + "\t");
		out.append("all\n");
		for (int topic=0; topic<nbtopics; topic++)
		{
			TreeMap<String,Double> list_p = topic_model.get_pz(topic);
			double total_num = list_p.get("all");
			out.append("z" + topic + "\t");
			double sum = 0;				
			for (String key : list_p.keySet())
			if (!key.equals("all"))
			{
				Double d = list_p.get(key);
				sum += (d / MyDocument.getNumDocPerPeriod(key));
			}
			for (MyDate key : MyDocument.getSortedDates())
			{
				Double d = list_p.get(key.toString());
				Double frac = d / MyDocument.getNumDocPerPeriod(key.toString());
				Double d_norm = (frac / sum) * total_num; 
				out.append(d_norm + "\t");
			}
			out.append(total_num + "\n");
		}
		return out.toString();
	}
	
	/*private void addTopic(StringBuffer sb, boolean sep, int i)
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
		sb.append("jsonstr = {\n\t");
		sb.append("\"numtopics\" : \"" + topic_model.numTopics() + "\",\n");
		sb.append("\t\"topics\" : [\n");
		for (int i = 0; i < topic_model.numTopics(); i++)
		{
			addTopic(sb, (i < topic_model.numTopics() - 1), i);
		}
		sb.append("\t]\n}\n");
		return sb.toString();
	}	*/	

}
