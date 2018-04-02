package core;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import evaluation.ClusterEvaluator;
import evaluation.ContingencyTable;
import evaluation.LabelRanking;
import evaluation.OrderedLabels;
import io.ExportResults;
import jxl.read.biff.BiffException;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

/** 
 * Cette classe permet de traiter la sortie donnée par un algo de topic modeling (pour le moment, LDA).
 * 
 * @author julien
 *
 */

public class OutputClustering
{
	// list of LDA outputs
	private static ArrayList<OutputClustering> list_outputs = new ArrayList<>();
	
	// number of indexed objects
	private int index;
	
	// array giving the topic assignment for each object
	private int[] topic_assignment;	
	// array giving the ground truth assignment for each object
	private String[] gt_assignment;
	// array giving the name (id) of each object
	private String[] id_assignment;
	
	// save the count for each topic
	private HashMap<Integer,Integer> list_topics;
	// save the count for each ground truth label
	private HashMap<String,Integer> list_labels;
	// save the integer id of each label
	private HashMap<String,Integer> intValue;
	// save the name of the label for each id (symetric of intValue)
	private HashMap<Integer,String> labelName;
	
	// total number of topics
	private int nb_topics;
	
	// total number of ground truth labels
	private int nb_labels;
	
	// confusion matrix
	private int[][] confusion;
	private Double[][] confusion_forClusterEvaluator;
	private ContingencyTable table;
	private HashMap<Integer,String> map_topics_labels;
	
	// object that evaluate the clustering based on the confusion matrix
	private ClusterEvaluator cluster_evaluator;
	
	// value reached by the optimized function (e.g., likelihood for LDA)
	private double objective_function;	
	
	// quality measures
	private double purity; //purity score
	private double ari; // ari score
	private double interTF10; // top term quality @10 (TF)
	private double interTF30; // top term quality @30 (TF)
	private double interLogTFxEnt10; // top term quality @10 (LogTFxEnt)
	private double interLogTFxEnt30; // top term quality @30 (LogTFxEnt)
	
	// topic labeling measures
	private OrderedLabels top_terms;
	
	public OutputClustering(int n)
	{
		topic_assignment = new int[n];
		gt_assignment = new String[n];
		id_assignment = new String[n];
		index = 0;
		nb_topics = 0;
		nb_labels = 0;
		list_topics = new HashMap<>();
		list_labels = new HashMap<>();
		intValue = new HashMap<>();
		labelName = new HashMap<>();
		top_terms = new OrderedLabels();
		map_topics_labels = new HashMap<>();
	}

	public void setTopic(int i)
	{
		topic_assignment[index] = i;
		Integer val = list_topics.get(i);
		if (val == null)
		{
			list_topics.put(i, new Integer(1));
			nb_topics++;
		}
		else
		{
			list_topics.remove(i);
			list_topics.put(i, new Integer(val.intValue() + 1));
		}
	}
	
	public void setOptimizedValue(double v)
	{
		objective_function = v;		
	}

	public int getTopic(int i)
	{
		return topic_assignment[i];
	}

	public String getGT(int i)
	{
		return gt_assignment[i];
	}

	public void setGroundTruth(String ground_truth)
	{
		gt_assignment[index] = ground_truth;
		Integer val = list_labels.get(ground_truth);
		if (val == null)
		{
			list_labels.put(ground_truth, new Integer(1));
			nb_labels++;
		}
		else
		{
			list_labels.remove(ground_truth);
			list_labels.put(ground_truth, new Integer(val.intValue() + 1));
		}
	}
	
	public void next()
	{
		index++;
	}

	public void setLabel(String name)
	{
		id_assignment[index] = name;
		
	}

	public void setIntegers2Labels()
	{
		int id = 0;
		for (String k : list_labels.keySet())
		{
			Integer i = new Integer(id);
			intValue.put(k, i);
			labelName.put(i, k);
			id++;
		}
	}
	
	public void computeConfusionMatrix()
	{
		//System.out.println("Matrice " + nb_labels + " / " + nb_topics);
		confusion = new int[nb_labels][nb_topics];
		confusion_forClusterEvaluator = new Double[nb_topics][nb_labels];
		for (int i=0; i<nb_topics; i++)
			for (int j=0; j<nb_labels; j++)
			{
				confusion_forClusterEvaluator[i][j] = new Double(0); 
			}		
		for (int i=0; i<index; i++)
		{
			int label_id = intValue.get(gt_assignment[i]).intValue();
			confusion[label_id][topic_assignment[i]]++;
			confusion_forClusterEvaluator[topic_assignment[i]][label_id]
					= new Double(confusion_forClusterEvaluator[topic_assignment[i]][label_id].doubleValue() + 1);
		}
	}
	
	public String printConfusionMatrix()
	{
		String s = "";
		for (int i=0; i<nb_labels; i++)
		{
			s += labelName.get(new Integer(i)) + "\t";
			for (int j=0; j<nb_topics; j++)
			{
				s += confusion[i][j] + "\t";
			}
			s += "\n";
		}
		s += "\n";
		return s;
	}
	
	public void setClusterEvaluator()
	{
		table = new ContingencyTable(confusion_forClusterEvaluator);
		cluster_evaluator = new ClusterEvaluator();
		cluster_evaluator.setData(table);
	}
	
	public double computeARI()
	{
		ari = cluster_evaluator.getAdjustedRandIndex();
		return ari;
	}
	
	public double getARI() 
	{
		return ari;
	}
	
	public double computePurity()
	{
		purity = cluster_evaluator.getPurity();
		return purity;
	}

	public double getPurity() 
	{
		return purity;
	}
	
	public void addTopTerm(String l, String t, double w)
	{
		top_terms.add(l, t, w);
	}
	
	public void printTopics()
	{
		System.out.println(top_terms);
	}
	
	public String getAssociations()
	{
		String s = "";
		for (Integer i : map_topics_labels.keySet())
		{
			String lab = map_topics_labels.get(i);
			s += i + " => " + lab + "\n";
		}
		return s;
	}
	
	/* Associate each topic with a ground truth label.
	 * The process is the following:
	 * - look for the maximum value in the confusion matrix
	 * - attribute the label to the topic
	 * - both selected label and topic are available
	 * - iterate until each label has been associated
	 * Pay attention: works when the number of topics is less or equal to the number of labels only 
	 */
	public void attributeTopics()
	{
		boolean[] label_taken = new boolean[nb_labels];
		for (int i=0; i<label_taken.length; i++) label_taken[i] = false;
		boolean[] topic_taken = new boolean[nb_topics];
		for (int i=0; i<topic_taken.length; i++) topic_taken[i] = false;
		for (int k=0; k<nb_topics; k++)
		{
			int max = -1;
			int indlab = -1;
			int indtop = -1;
			for (int j=0; j<nb_topics; j++)
			if (!topic_taken[j])
			{
				for (int i=0; i<nb_labels; i++)
				if (!label_taken[i])
				{
					if (confusion[i][j] > max)
					{
						max = confusion[i][j];
						indlab = i;
						indtop = j;
					}
				}
			}
			map_topics_labels.put(indtop, labelName.get(indlab));
			//label_taken[indlab] = true;
			topic_taken[indtop] = true;
		}
	}	
	
	/* compute the quality scores between term ranking given by the algorithm and the ground truth ranking of <b>gtranking</b>. */ 
	public void computeIntersectionMeasures(LabelRanking gtranking)
	{
		interTF10 = computeIntersection(gtranking, 10, "TF");
		interTF30 = computeIntersection(gtranking, 30, "TF");
		interLogTFxEnt10 = computeIntersection(gtranking, 10, "LogTFxEnt");
		interLogTFxEnt30 = computeIntersection(gtranking, 30, "LogTFxEnt");
	}
	
	private double computeIntersection(LabelRanking gtranking, int topk, String type)
	{
		double sum = 0;
		for (int k=0; k<nb_topics; k++)
		{
			String label = map_topics_labels.get(k);			
			String key = type + " " + label;
			ArrayList<String> inter = new ArrayList<String>();
			ArrayList<String> topGT = gtranking.getTopTerms(key);
			if (topGT.size() < topk)
				return -1;
			for (int i=0; i<topk; i++)
				inter.add(topGT.get(i));
			ArrayList<String> topLDA = top_terms.getTopTerms("topic " + k, topk);
			if (topLDA.size() < topk)
				return -1;
			ArrayList<String> substract = new ArrayList<String>();
			for (int i=0; i<topk; i++)
				substract.add(topLDA.get(i));
			inter.retainAll(substract);
			sum += ((double)inter.size() / (double)topk);			
		}
		return sum / (double)nb_topics;
	}
		
	
	// METHODS FOR OBJECT SET

	public static void add(OutputClustering out)
	{
		list_outputs.add(out);
	}

	public static double getMeanARI()
	{
		double sum = 0;
		for (OutputClustering out : list_outputs)
		{
			sum += out.getARI();
		}
		return (sum / list_outputs.size());
	}

	public static double getVarianceARI(double mean)
	{
		double sum = 0;
		for (OutputClustering out : list_outputs)
		{
			sum += Math.pow(mean - out.getARI(), 2);
		}
		return (sum / list_outputs.size());
	}
	
	public static OutputClustering getBestARI()
	{
		double max = -1;
		OutputClustering best = null;
		for (OutputClustering out : list_outputs)
		{
			if (out.getARI() > max)
			{
				max = out.getPurity();
				best = out;
			}
		}
		return best;
	}

	public static double getMeanPurity()
	{
		double sum = 0;
		for (OutputClustering out : list_outputs)
		{
			sum += out.getPurity();
		}
		return (sum / list_outputs.size());
	}

	public static double getVariancePurity(double mean)
	{
		double sum = 0;
		for (OutputClustering out : list_outputs)
		{
			sum += Math.pow(mean - out.getPurity(), 2);
		}
		return (sum / list_outputs.size());
	}
	
	private static double getMeanOptimizedFunction()
	{
		double sum = 0;
		for (OutputClustering out : list_outputs)
		{
			sum += out.objective_function;
		}
		return (sum / list_outputs.size());

	}
	
	public static double getVarianceOptimizedFunction(double mean)
	{
		double sum = 0;
		for (OutputClustering out : list_outputs)
		{
			sum += Math.pow(mean - out.objective_function, 2);
		}
		return (sum / list_outputs.size());
	}
	
	public static OutputClustering getBestOptimizedFunction()
	{
		double max = -Double.MAX_VALUE;
		OutputClustering best = null;
		for (OutputClustering out : list_outputs)
		{
			if (out.objective_function > max)
			{
				max = out.objective_function;
				best = out;
			}
		}
		return best;
	}

	public static OutputClustering getBestPurity()
	{
		double max = -1;
		OutputClustering best = null;
		for (OutputClustering out : list_outputs)
		{
			if (out.getPurity() > max)
			{
				max = out.getPurity();
				best = out;
			}
		}
		return best;
	}
		
	public static double getMeanIntersect(int type)
	{
		double sum = 0;
		for (OutputClustering out : list_outputs)
		{
			switch(type)
			{
			case 1: sum += out.interTF10;
			break;
			case 2: sum += out.interTF30;
			break;
			case 3: sum += out.interLogTFxEnt10;
			break;
			case 4: sum += out.interLogTFxEnt30;
			break;			
			}
		}
		return (sum / list_outputs.size());
	}

	public static double getVarianceIntersect(int type, double mean)
	{
		double sum = 0;
		for (OutputClustering out : list_outputs)
		{
			switch(type)
			{
			case 1: sum += Math.pow(mean - out.interTF10, 2);
			break;
			case 2: sum += Math.pow(mean - out.interTF30, 2);
			break;
			case 3: sum += Math.pow(mean - out.interLogTFxEnt10, 2);
			break;
			case 4: sum += Math.pow(mean - out.interLogTFxEnt30, 2);
			break;			
			}
		}
		return (sum / list_outputs.size());
	}

	public static void removeAll()
	{
		list_outputs.clear();
	}
	
	public static void export_results(ExportResults export, long time) throws RowsExceededException, WriteException, IOException, BiffException
	{
		export.openStream();
		double mean = getMeanOptimizedFunction();
		export.newNumberLabel(mean, "float");
		double var = getVarianceOptimizedFunction(mean);
		export.newNumberLabel(var, "float");
		mean = getMeanPurity();
		export.newNumberLabel(mean, "percent");
		var = getVariancePurity(mean);
		export.newNumberLabel(var, "percent");
		mean = getMeanARI();
		export.newNumberLabel(mean, "float");
		var = getVarianceARI(mean);
		export.newNumberLabel(var, "float");
		for (int i=0; i<4; i++)
		{
			mean = getMeanIntersect(i+1);
			export.newNumberLabel(mean, "percent");
			var = getVarianceIntersect(i+1, mean);
			export.newNumberLabel(var, "percent");
		}
		OutputClustering best = getBestOptimizedFunction();
		export.newNumberLabel(best.objective_function, "float");
		export.newNumberLabel(best.getPurity(), "percent");
		export.newNumberLabel(best.getARI(), "float");
		export.newNumberLabel(best.interTF10, "percent");
		export.newNumberLabel(best.interTF30, "percent");
		export.newNumberLabel(best.interLogTFxEnt10, "percent");
		export.newNumberLabel(best.interLogTFxEnt30, "percent");
		export.newNumberLabel(time, "default");
		export.closeStream();
	}

	public void export(String path, String dataname, String voc_type, int k, int size_vocab, double[] p, double alpha, double beta,
			int nbruns) throws IOException
	{
		String basename = path + dataname + "_" + voc_type + "_" + size_vocab + "_" + "K" + k + "_"
				+ p[0] + "-" + p[1] + "-" + p[2] + "-" + p[3] + "_" + alpha + "_" + beta + "_" + nbruns;

		//export the configuration
		FileWriter fw = new FileWriter(basename + ".config");
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write("LDA/NMF");
		bw.newLine();
		bw.write("number of clusters\t" + nb_topics);
		bw.newLine();
		bw.write("number of true classes\t" + nb_labels);
		bw.newLine();
		bw.write("data name\t" + dataname);
		bw.newLine();
		bw.write("voc type\t" + voc_type);
		bw.newLine();
		bw.write("voc size\t" + size_vocab);
		bw.newLine();
		bw.write("proportions\t" + p[0] + "-" + p[1] + "-" + p[2] + "-" + p[3]);
		bw.newLine();
		bw.write("alpha\t" + alpha);
		bw.newLine();
		bw.write("beta\t" + beta);
		bw.newLine();
		bw.write("nb runs\t" + nbruns);
		bw.newLine();
		bw.flush();
		bw.close();
		
		// export topic assignment for each document
		fw = new FileWriter(basename + ".assign");
		bw = new BufferedWriter(fw);
		for (int i=0; i<index; i++)
		{
			bw.write(id_assignment[i] + "\t" + topic_assignment[i] + "\t" + gt_assignment[i]);
			bw.newLine();
		}
		bw.flush();
		bw.close();
		
		// export the topics (ranked terms)
		fw = new FileWriter(basename + ".topics");
		bw = new BufferedWriter(fw);
		bw.write(top_terms.toString());
		bw.newLine();
		bw.write(getAssociations());
		bw.newLine();
		bw.write(printConfusionMatrix());
		bw.flush();
		bw.close();

	}

}
