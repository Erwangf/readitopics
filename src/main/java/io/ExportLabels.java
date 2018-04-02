package io;

import java.util.ArrayList;
import labeling.TopicLabelerSkeleton;

public class ExportLabels extends Export {
		
	private int nb_printed_labels;
	private TopicLabelerSkeleton[] labelers;
	private int nbtopics;
	
	public ExportLabels(int nbtopics, TopicLabelerSkeleton... labelers)
	{
		super();
		this.labelers = labelers;
		nb_printed_labels = 1; // default value
		print_prob = true;
		this.nbtopics = nbtopics;
	}
	
	public ExportLabels(int nbtopics, ArrayList<TopicLabelerSkeleton> labelers)
	{
		this.labelers = new TopicLabelerSkeleton[labelers.size()];
		for (int i=0; i<labelers.size(); i++)
			this.labelers[i] = labelers.get(i);
		nb_printed_labels = 1; // default value
		print_prob = true;
		this.nbtopics = nbtopics;
	}
	
	public void setPrintLabels(int n)
	{
		this.nb_printed_labels = n;
	}
		
	protected String export_csv(String filename)
	{
		TopicLabelerSkeleton.set_export("csv");
		StringBuffer out = new StringBuffer();
		for (int i=0; i<nbtopics; i++)
		{
			out.append("z" + i + "\n");	
			for (TopicLabelerSkeleton labeler : labelers)
				out.append(labeler.getName() + "\t" + labeler.getLabel(i, !this.print_prob) + "\n");
		}
		TopicLabelerSkeleton.set_export("browse");
		return out.toString();
	}
	
	private void addTopic(StringBuffer sb, boolean sep, int i)
	{
		sb.append("\t\t{\n");
		sb.append("\t\t\"topicid\" : " + i +",\n");
		boolean flag = false;
		for (TopicLabelerSkeleton labeler : labelers)
		{
			if (flag)
				sb.append(",\n");
			else
				flag = true;
			sb.append("\t\t\"");
			sb.append(labeler.getName() + "\" : [\n");
			sb.append("\t\t\t");
			sb.append(labeler.getLabelJSON(i, !this.print_prob));
			sb.append("\n\t\t]");
		}
		sb.append("\n\t\t}");
		if (sep)
			sb.append(",");
		sb.append("\n");
	}	
	
	protected String export_json(String filename)
	{
		//TopicLabelerSkeleton.set_export("json");
		StringBuffer sb = new StringBuffer();
		sb.append("{\n\t");
		sb.append("\"numtopics\" : \"" + nbtopics + "\",\n");
		sb.append("\t\"topics\" : [\n");
		for (int i = 0; i < nbtopics; i++)
		{
			//System.out.print(".");
			addTopic(sb, (i < nbtopics - 1), i);
		}
		sb.append("\t]\n}\n");
		//TopicLabelerSkeleton.set_export("browse");
		return sb.toString();
	}	
	
}
