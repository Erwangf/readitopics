package io.config;

import io.LoadDataset;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

public class LoadConfigFile {

	private static final Logger log = Logger.getLogger(LoadConfigFile.class.getName());

	/** number of batch for word embedding learning. Default is 10 */
	private static int WE_BATCH = 10;

	/** number of iteration for word embedding learning. Default is 10 */
	private static int WE_ITERATIONS = 10;

	/** number of features for word embedding learning. Default is 100 */
	private static int WE_DIMENSIONS = 100;

	/* number of parallel threads for LDA */
	private static int NB_THREADS = 2;

	/* number of iterations for the gibb's sampler */
	private static int NB_ITER = 2000;

	/* number of topics used by LDA */
	private static int NB_TOPICS = 5;

	/* minimum number of documents for a selected term */
	private static int minnbdocs = 2;

	/* minimum TF for a selected term */
	private static int mintf = 2;

	// set the used vocabulary
	private static String[] voc_type = { "TF" };

	// int[] size_vocab_table = { 100, 200, 500, 1000, 2000, 5000, 10000, 20000
	// };
	private static int[] size_vocab_table = { 10000 };
	private static double[][] proportions = { { -1, 0, 0, 0 }
			// {0.5, 0.5, 0, 0}
			// {0.2, 0.8, 0, 0},
			// {0, 0.5, 0.5, 0}
	};
	private static int[][] volumes = { { -1, 0, 0, 0 } };
	private static int[][] skipTerms = { { 0, 0, 0, 0 } };

	private static double[] alpha_table = { -1 };
	private static double[] beta_table = { -1 };
	private static int nbruns = 1;

	/* List of stop lists */
	private static String[] stoplists = { "" };

	/* List of preprocessing operations (e.g., tolower) */
	private static String preprocess = "";

	private static String pathForIndexing = "";
	
	private static String BIOTEX_TMP;

	private static String[] cleanProperty(String prop){
		String[] t = prop.trim().split(",");
		String[] result = new String[t.length];
		for (int i = 0; i < t.length; i++) result[i] = t[i].trim();
		return(result);
	}

	public static void loadConfig(InputStream inputStream) throws IOException {
		Properties prop = new Properties();
		prop.load(inputStream);
		System.out.println("Load configuration file");
		String property;

		/* word ebemdding properties */
		property = prop.getProperty("WE_BATCH");
		if (property != null) {
			WE_BATCH = Integer.parseInt(property.trim());
			log.info("Overriden default number of batches for word embedding to " + WE_BATCH);
		}

		property = prop.getProperty("WE_DIMENSIONS");
		if (property != null) {
			WE_DIMENSIONS = Integer.parseInt(property.trim());
			log.info("Overriden default number of dimensions for word embedding to " + WE_DIMENSIONS);
		}

		property = prop.getProperty("WE_ITERATIONS");
		if (property != null) {
			WE_ITERATIONS = Integer.parseInt(property.trim());
			log.info("Overriden default number of iterations for word embedding to " + WE_ITERATIONS);
		}	
			
		/* End Word embedding properties */

		/* biotex property */
		
		property = prop.getProperty("BIOTEX_TMP");
		if (property != null) {
			BIOTEX_TMP = property.trim();
			log.info("Overriden default tmp folder for biotex to " + BIOTEX_TMP);
		}

		/* end biotex property */

		// NB_THREADS
		property = prop.getProperty("NB_THREADS");
		if (property != null) NB_THREADS = Integer.parseInt(property.trim());

		// NB_ITER
		property = prop.getProperty("NB_ITER");
		if (property != null) NB_ITER = Integer.parseInt(property.trim());

		// NB_TOPICS
		property = prop.getProperty("NB_TOPICS");
		if (property != null) NB_TOPICS = Integer.parseInt(property.trim());

		// VOCABULARY
		property = prop.getProperty("VOCABULARY");
		if (property != null) {
			String[] t = property.trim().split(",");
			voc_type = new String[t.length];
			for (int i = 0; i < t.length; i++)
				voc_type[i] = t[i].trim();
		}

		// SIZE_VOCAB
		property = prop.getProperty("SIZE_VOCAB");
		if (property != null) {
			String[] t = property.trim().split(",");
			size_vocab_table = new int[t.length];
			for (int i = 0; i < t.length; i++)
				size_vocab_table[i] = Integer.parseInt(t[i].trim());
		}

		// PROPORTIONS & VOLUMES CHECK
		if ((prop.getProperty("PROPORTIONS") != null) && (prop.getProperty("VOLUMES") != null)){
			throw new IOException("Error in configuration file: choose between proportions and volumes.");
		}

		// PROPORTIONS
		property = prop.getProperty("PROPORTIONS");
		if (property != null) {
			String[] t = property.trim().split(";");
			proportions = new double[t.length][4];
			for (int i = 0; i < t.length; i++) {
				String[] p = t[i].trim().split(",");
				if (p.length != 4)
					throw new IOException("Configuration file corrupted");
				for (int j = 0; j < p.length; j++)
					proportions[i][j] = Double.parseDouble(p[j].trim());
			}
		}

		// VOLUMES
		property = prop.getProperty("VOLUMES");
		if (property != null) {
			// System.out.println("set volumes to " + property);
			String[] t = property.trim().split(";");
			volumes = new int[t.length][4];
			for (int i = 0; i < t.length; i++) {
				String[] p = t[i].trim().split(",");
				if (p.length != 4)
					throw new IOException("Configuration file corrupted");
				for (int j = 0; j < p.length; j++)
					volumes[i][j] = Integer.parseInt(p[j].trim());
			}
		}

		// ALPHA
		property = prop.getProperty("ALPHA");
		if (property != null) {
			String[] t = property.trim().split(",");
			alpha_table = new double[t.length];
			for (int i = 0; i < t.length; i++)
				alpha_table[i] = Double.parseDouble(t[i].trim());
		}

		// BETA
		property = prop.getProperty("BETA");
		if (property != null) {
			String[] t = property.trim().split(",");
			beta_table = new double[t.length];
			for (int i = 0; i < t.length; i++)
				beta_table[i] = Double.parseDouble(t[i].trim());
		}

		// NB_RUNS
		property = prop.getProperty("NB_RUNS");
		if (property != null) nbruns = Integer.parseInt(property.trim());

		// SKIPTERMS
		property = prop.getProperty("SKIPTERMS");
		if (property != null) {
			String[] t = property.trim().split(";");
			skipTerms = new int[t.length][4];
			for (int i = 0; i < t.length; i++) {
				String[] p = t[i].trim().split(",");
				if (p.length != 4)
					throw new IOException("Configuration file corrupted");
				for (int j = 0; j < p.length; j++)
					skipTerms[i][j] = Integer.parseInt(p[j].trim());
			}
		}

		// DATA
		property = prop.getProperty("DATA");
		if (property != null) LoadDataset.setDataName(property.trim());

		// SOURCE
		property = prop.getProperty("SOURCE");
		if (property != null) LoadDataset.setSourceType(property.trim());

		// PATH
		property = prop.getProperty("PATH");
		if (property != null) LoadDataset.setPath(property.trim());

		// TEXT
		property = prop.getProperty("TEXT");
		if (property != null) LoadDataset.setTextField(property.trim());

		// GTRUTH
		property = prop.getProperty("GTRUTH");
		if (property != null) LoadDataset.setGTfield(property.trim());

		// TIME
		property = prop.getProperty("TIME");
		if (property != null) LoadDataset.setTimeField(property.trim());

		// ID
		property = prop.getProperty("ID");
		if (property != null) LoadDataset.setIDField(property.trim());

		// INDEXING
		property = prop.getProperty("INDEXING");
		if (property != null) pathForIndexing = property.trim();

		// STOPLIST
		property = prop.getProperty("STOPLIST");
		if (property != null) {
			String[] t = property.trim().split(",");
			stoplists = new String[t.length];
			for (int i = 0; i < t.length; i++)
				stoplists[i] = t[i].trim();
		}

		//DATES
		property = prop.getProperty("DATES");
		if (property != null) {
			String[] t = property.trim().split(",");
			String[] list_dates = new String[t.length];
			for (int i = 0; i < t.length; i++)
				list_dates[i] = t[i].trim();
			LoadDataset.setListDates(list_dates);
		}

		// PREPROCESS
		property = prop.getProperty("PREPROCESS");
		if (property != null) preprocess = property.trim();

		// TITLE
		property = prop.getProperty("TITLE");
		if (property != null) LoadDataset.setTitleField(property.trim());

		// AUTHOR
		property = prop.getProperty("AUTHOR");
		if (property != null) LoadDataset.setAuthorField(property.trim());

		// RAWDATA
		property = prop.getProperty("RAWDATA");
		if (property != null) LoadDataset.setRawData(property.trim());

		// RAWLINES
		property = prop.getProperty("RAWLINES");
		if (property != null) LoadDataset.setRawLinesField(property.trim());

		// LANGUAGE
		property = prop.getProperty("LANGUAGE");
		if (property != null) LoadDataset.setLanguage(property.trim());

		// INDEX_LUCENE
		property = prop.getProperty("INDEX_LUCENE");
		if (property != null) LoadDataset.setPathIndex(property.trim());

		inputStream.close();
	}

	public static void setMinDocs(int m) {
		minnbdocs = m;
	}

	public static int getNBthreads() {
		return NB_THREADS;
	}

	public static String[] getVocTypes() {
		return voc_type;
	}

	public static int[] getVocSizes() {
		return size_vocab_table;
	}

	public static double[][] getProportions() {
		return proportions;
	}

	public static int[][] getVolumes() {
		return volumes;
	}

	public static double[] getBetaTable() {
		return beta_table;
	}

	public static double[] getAlphaTable() {
		return alpha_table;
	}

	public static int getNBruns() {
		return nbruns;
	}

	public static int[][] getSkipTerms() {
		return skipTerms;
	}

	public static int getNBtopics() {
		return NB_TOPICS;
	}

	public static int getMinDocs() {
		return minnbdocs;
	}

	public static int getMinTF() {
		return mintf;
	}

	public static int getNBiter() {
		return NB_ITER;
	}

	public static String getPathForIndexing() {
		return pathForIndexing;
	}
	
	public static String getTmpPathForBiotex() {
		return BIOTEX_TMP;
	}

	public static String[] getStopLists() {
		return stoplists;
	}

	public static String getPreprocess() {
		return preprocess;
	}

	public static int getWeBatch() {
		return WE_BATCH;
	}

	public static void setWeBatch(int wE_BATCH) {
		WE_BATCH = wE_BATCH;
	}

	public static int getWeIterations() {
		return WE_ITERATIONS;
	}

	public static void setWeIterations(int wE_ITERATIONS) {
		WE_ITERATIONS = wE_ITERATIONS;
	}

	public static int getWeDimensions() {
		return WE_DIMENSIONS;
	}

	public static void setWeDimensions(int wE_DIMENSIONS) {
		WE_DIMENSIONS = wE_DIMENSIONS;
	}

}
