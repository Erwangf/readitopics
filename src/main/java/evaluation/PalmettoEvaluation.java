package evaluation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.Iterator;
import java.util.Locale;

import org.aksw.palmetto.Coherence;
import org.aksw.palmetto.DirectConfirmationBasedCoherence;
import org.aksw.palmetto.Palmetto;
import org.aksw.palmetto.VectorBasedCoherence;
import org.aksw.palmetto.aggregation.ArithmeticMean;
import org.aksw.palmetto.calculations.direct.FitelsonConfirmationMeasure;
import org.aksw.palmetto.calculations.direct.LogCondProbConfirmationMeasure;
import org.aksw.palmetto.calculations.direct.LogRatioConfirmationMeasure;
import org.aksw.palmetto.calculations.direct.NormalizedLogRatioConfirmationMeasure;
import org.aksw.palmetto.calculations.indirect.CosinusConfirmationMeasure;
import org.aksw.palmetto.corpus.CorpusAdapter;
import org.aksw.palmetto.corpus.WindowSupportingAdapter;
import org.aksw.palmetto.corpus.lucene.LuceneCorpusAdapter;
import org.aksw.palmetto.corpus.lucene.WindowSupportingLuceneCorpusAdapter;
import org.aksw.palmetto.io.SimpleWordSetReader;
import org.aksw.palmetto.prob.bd.BooleanDocumentProbabilitySupplier;
import org.aksw.palmetto.prob.window.BooleanSlidingWindowFrequencyDeterminer;
import org.aksw.palmetto.prob.window.ContextWindowFrequencyDeterminer;
import org.aksw.palmetto.prob.window.WindowBasedProbabilityEstimator;
import org.aksw.palmetto.subsets.OneOne;
import org.aksw.palmetto.subsets.OnePreceding;
import org.aksw.palmetto.subsets.OneSet;
import org.aksw.palmetto.vector.DirectConfirmationBasedVectorCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.Constantes;
import io.LoadDataset;
import topicmodeling.LDATopicModel;

public class PalmettoEvaluation {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Palmetto.class);

	public static final String DEFAULT_TEXT_INDEX_FIELD_NAME = "text";
	public static final String DEFAULT_DOCUMENT_LENGTH_INDEX_FIELD_NAME = "length";

	public static String[][] wordsets = {{}};
	
	/*public static void main(String[] args) {
		String indexPath = LoadDataset.getPathIndex();
		String measure = "c_v";
		CorpusAdapter corpusAdapter = getCorpusAdapter(measure, indexPath);
		if (corpusAdapter == null)
		{
	            return;
		}
        Coherence coherence = getCoherence(measure, corpusAdapter);
        if (coherence == null) {
            return;
        }
        SimpleWordSetReader reader = new SimpleWordSetReader();
        //String wordsets[][] = reader.readWordSets("topics_a_evaluer.txt");
        String wordsets[][] = {{ "nfl", "football", "game", "kaepernick", "anthem", "player", "national", "coach", "players", "protest" }};
        LOGGER.info("Read " + wordsets.length + " from file.");

        double coherences[] = coherence.calculateCoherences(wordsets);
        corpusAdapter.close();

        printCoherences(coherences, wordsets, System.out);
	}*/
	
	public static String[][] readCSVFile(String filename, int num_topics, int num_words) throws IOException
	{
		String[][] topwords = new String[num_topics][num_words];	
		String line;
		int current_row = 0;
		BufferedReader buff = new BufferedReader(new FileReader(filename + ".csv"));
		while ((line = buff.readLine()) != null) {
			String[] tokens = line.split("\t");
			if (tokens.length > 1) {
				for (int i=0; i<num_words; i++) {
					topwords[current_row][i] = tokens[i];				
				}
				current_row++;
			}
		}
		return topwords;
	}
	
	protected static void export(String filename, double[][] eval)
	{		
		
		StringBuffer out = new StringBuffer();
		for (int i=0; i<eval.length; i++)
		{			
			double[] m = eval[i];
			for (int j=0; j<m.length; j++) {
				out.append(m[j] + "\t");
			}
			out.append("\n");
		}
		new File(filename).mkdirs();
		BufferedWriter writer = null;
		try {
			File myfile = new File(filename + "-pal.csv");
			writer = new BufferedWriter(new FileWriter(myfile));
			writer.write(out.toString());
		} catch (Exception e) {
			//e.printStackTrace();
			System.out.println("Impossible to write to the file " + filename);
		} finally {
			try {
				writer.close();
			} catch (Exception e) {
			}
		}
	}
	
	public static double[] computeMeasure(String measure)
	{
		String indexPath = LoadDataset.getPathIndex();
		CorpusAdapter corpusAdapter = getCorpusAdapter(measure, indexPath);
		if (corpusAdapter == null)
			return null;
        Coherence coherence = getCoherence(measure, corpusAdapter);
        if (coherence == null)
        	return null;
        double coherences[] = coherence.calculateCoherences(wordsets);
        corpusAdapter.close();
        return coherences;
	}
	
	public static void populateTopics(LDATopicModel model, int nbwords)
	{
		//String[][] words = new String[model.numTopics()][nbwords];
		wordsets = new String[model.numTopics()][nbwords];
		for (int topic=0; topic<model.numTopics(); topic++)
		{
			Iterator<Integer> iterator = model.getTopWords(topic).iterator();
			int nb = 0;
			while (iterator.hasNext() && (nb < nbwords))
			{
				Integer ind = iterator.next();
				String word = model.getAlphabet().lookupObject(ind);
				wordsets[topic][nb] = word;
				nb++;						
			}
			if (nb < nbwords)
			{
				System.out.println("No enough top words: " + (nbwords - nb) + " words filled with 'platypus' :-)");
				for (int i=nb; i < nbwords; i++)
					wordsets[topic][i] = "platypus";
			}
		}
	}

	public static void populateTopics(String[][] words)
	{
		wordsets = words;
	}

	public static CorpusAdapter getCorpusAdapter(String calcType, String indexPath) {
		try {
			if ("umass".equals(calcType)) {
				return LuceneCorpusAdapter.create(indexPath, DEFAULT_TEXT_INDEX_FIELD_NAME);
			} else {
				return WindowSupportingLuceneCorpusAdapter.create(indexPath, DEFAULT_TEXT_INDEX_FIELD_NAME,
						DEFAULT_DOCUMENT_LENGTH_INDEX_FIELD_NAME);
			}
		} catch (Exception e) {
			System.out.println("Couldn't open lucene index. Aborting.\n" + e);
			return null;
		}
	}

	   public static Coherence getCoherence(String calcType, CorpusAdapter corpusAdapter) {
	        if ("umass".equals(calcType)) {
	            return new DirectConfirmationBasedCoherence(new OnePreceding(),
	                    BooleanDocumentProbabilitySupplier.create(corpusAdapter, "bd", true),
	                    new LogCondProbConfirmationMeasure(), new ArithmeticMean());
	        }

	        if ("uci".equals(calcType)) {
	            return new DirectConfirmationBasedCoherence(
	                    new OneOne(), getWindowBasedProbabilityEstimator(10, (WindowSupportingAdapter) corpusAdapter),
	                    new LogRatioConfirmationMeasure(), new ArithmeticMean());
	        }

	        if ("npmi".equals(calcType)) {
	            return new DirectConfirmationBasedCoherence(
	                    new OneOne(), getWindowBasedProbabilityEstimator(10, (WindowSupportingAdapter) corpusAdapter),
	                    new NormalizedLogRatioConfirmationMeasure(), new ArithmeticMean());
	        }

	        if ("c_a".equals(calcType)) {
	            int windowSize = 5;
	            WindowBasedProbabilityEstimator probEstimator = new WindowBasedProbabilityEstimator(
	                    new ContextWindowFrequencyDeterminer((WindowSupportingAdapter) corpusAdapter, windowSize));
	            probEstimator.setMinFrequency(WindowBasedProbabilityEstimator.DEFAULT_MIN_FREQUENCY * windowSize);
	            return new VectorBasedCoherence(
	                    new OneOne(), new DirectConfirmationBasedVectorCreator(probEstimator,
	                            new NormalizedLogRatioConfirmationMeasure()), new CosinusConfirmationMeasure(),
	                    new ArithmeticMean());
	        }

	        if ("c_p".equals(calcType)) {
	            return new DirectConfirmationBasedCoherence(
	                    new OnePreceding(),
	                    getWindowBasedProbabilityEstimator(70, (WindowSupportingAdapter) corpusAdapter),
	                    new FitelsonConfirmationMeasure(), new ArithmeticMean());
	        }

	        if ("c_v".equals(calcType)) {
	            return new VectorBasedCoherence(new OneSet(),
	                    new DirectConfirmationBasedVectorCreator(
	                            getWindowBasedProbabilityEstimator(110, (WindowSupportingAdapter) corpusAdapter),
	                            new NormalizedLogRatioConfirmationMeasure()),
	                    new CosinusConfirmationMeasure(), new ArithmeticMean());
	        }

	        StringBuilder msg = new StringBuilder();
	        msg.append("Unknown calculation type \"");
	        msg.append(calcType);
	        msg.append("\". Supported types are:\nUMass\nUCI\nNPMI\nC_A\nC_P\nC_V\n\nAborting.");
	        LOGGER.error(msg.toString());
	        return null;
	    }
	   
	   public static WindowBasedProbabilityEstimator getWindowBasedProbabilityEstimator(int windowSize,
	            WindowSupportingAdapter corpusAdapter) {
	        WindowBasedProbabilityEstimator probEstimator = new WindowBasedProbabilityEstimator(
	                new BooleanSlidingWindowFrequencyDeterminer(
	                        corpusAdapter, windowSize));
	        probEstimator.setMinFrequency(WindowBasedProbabilityEstimator.DEFAULT_MIN_FREQUENCY * windowSize);
	        return probEstimator;
	    }
	   
	   public static void main(String[] args)
	   {
		   if (args.length < 3) {
			   System.out.println("Pas assez d'arguments. Pour rappel :");
			   System.out.println("- arg 1 : nom complet du fichier source");
			   System.out.println("- arg 2 : nombre de topics (lignes) considérés");
			   System.out.println("- arg 3 : nombre de top mots considérés");
			   System.out.println("- arg 4 : langage (FR ou EN pour le moment, FR par défaut)");
		   }
		   String[][] test = null;
		   String filename = args[0];
		   int num_topics = Integer.parseInt(args[1]);
		   int num_topwords = Integer.parseInt(args[2]);
		   try {
			   test = readCSVFile(filename, num_topics, num_topwords);
		   } catch (NumberFormatException | IOException e) {
			   e.printStackTrace();
			   System.exit(0);
		   }
			   /*{
					   //{"robb", "winterfell",  "bran",  "arya",  "sansa",  "father", "stark", "septa", "ned", "rickon"},
					   //{"prince", "hotah",  "captain",  "doran",  "areo",  "caleotte", "maester", "sunspear", "dornishmen", "longaxe"},
					   { "game", "sport" },
					   { "game", "game" },
					   { "game", "games" }
					   //{"prince", "prince", "prince", "prince", "prince", "prince", "prince", "prince", "prince", "prince"}
					   //{"prince", "princess", "prince", "princess", "prince", "princess", "prince", "princess", "prince", "princess"}
					   //{"man", "men"},
					   //{"bird", "birds"},
					   //{"bird", "bird"}
					   //{"homme", "hommes"},
					   //{"homme", "femme"},
					   //{"homme", "homme"}
			   };*/
		   if ((args.length > 3) && (args[3].equalsIgnoreCase("en")))
			   LoadDataset.setPathIndex("/Users/julien/Recherche/Projets/TopicLabeling/lucene_index/index");
		   else
			   LoadDataset.setPathIndex("/Users/julien/Recherche/Projets/TopicLabeling/lucene_index/index_fr");
		   if (test != null) {
			   populateTopics(test);
			   String[] palmetto_measures = { "umass", "uci", "c_v", "c_p", "c_a", "npmi" };
			   double[][] full_res = new double[num_topics][palmetto_measures.length];
			   int j = 0;
			   for (String m : palmetto_measures)
				{
					
					double[] res = PalmettoEvaluation.computeMeasure(m);
					for (int i=0; i<num_topics; i++)
						full_res[i][j] = res[i];
					//System.out.println("Compute the measure " + m + ": " + res[0] + " ; " + res[1]+ " ; " + res[2]);
					j++;
				}
			   export(filename, full_res);
		   }
	   }
}
