package exe;

import cc.mallet.pipe.Noop;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.*;
import core.*;
import evaluation.LabelRanking;
import evaluation.OrderedLabels;
import io.ExportResults;
import io.IOTopicModel;
import io.config.LoadConfigFile;
import io.LoadDataset;
import jxl.read.biff.BiffException;
import jxl.write.WriteException;
import org.apache.commons.io.FileUtils;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.LineSentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import utils.CommonsUtils;
import utils.TopicModelingToJson;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Cette classe permet d'exécuter la version parallèle de LDA (package MALLET)
 * sur les données.
 *
 * @author julien
 */

public class RunLDA {

    private static final Logger log = Logger.getLogger(RunLDA.class.getName());

    /* configuration file */
    private static String config;

    /* store the ground truth labels */
    private static LabelRanking ranking;

    public static void main(String[] args) throws IOException, WriteException, BiffException {

        if (args.length == 0)
            throw new IOException("Require at least 1 argument: configuration file");

        config = args[0];

		/*if (args.length > 1) // second argument can be the min. number of docs
                                // for a term to be included into the vocabulary
			LoadConfigFile.setMinDocs(Integer.parseInt(args[1]));*/

        // check configuration file

        InputStream inputStream;
        inputStream = new FileInputStream(config);

        LoadConfigFile.loadConfig(inputStream);

		/*
         * else { System.out.println("Use default configuration values");
		 * //throw new FileNotFoundException("property file '" + propFileName +
		 * "' not found in the classpath"); }
		 */

        System.out.println("Dataset : " + LoadDataset.getDataName());
        System.out.println("Number of threads: " + LoadConfigFile.getNBthreads());

        experiments(
                LoadConfigFile.getVocTypes(),
                LoadConfigFile.getVocSizes(),
                LoadConfigFile.getProportions(),
                LoadConfigFile.getVolumes(),
                LoadConfigFile.getAlphaTable(),
                LoadConfigFile.getBetaTable(),
                LoadConfigFile.getNBruns(),
                LoadConfigFile.getSkipTerms()
        );

    }

    private static void experiments(String[] voc_type_table, int[] size_vocab_table, double[][] proportions,
                                    int[][] volumes, double[] alpha_table, double[] beta_table, int nbruns, int[][] skipTerms)
            throws IOException, WriteException, BiffException {

        String biotex_input_dir = LoadDataset.getPath() + Constantes.separateur + Constantes.RESULT_DIR + Constantes.separateur
                + LoadDataset.getDataName() + Constantes.separateur + "biotex";
        new File(biotex_input_dir).mkdirs();

		/* loop on all the possible vocabularies */
        for (String voc_type : voc_type_table) {

			/* reinit all */
            MyDocument.reinitAllDocs();

			/* extraction des documents en mémoire et indexation */
            long startTime;
            long estimatedTime;
            extractCorpus();

			/* index the vocabulary */
            String path = biotex_input_dir + Constantes.separateur + voc_type + Constantes.separateur;
            MonVocabulaire.indexing(path);

			/* set the ID used by LDA to the documents */
            MyDocument.setInternalIDforLDA();

			/* init the excel file for topic evaluation */
            MyExcelGroungTruth myExcelGroungTruth = initGroundTruthIfAny(voc_type);

			/* init the folder and object for exporting topic models */
            IOTopicModel exportTM = initStuffForTopicExport();

			/*
			 * int s = size_vocab;
			 * 
			 * boolean prop = false; if (LoadConfigFile.getProportions()[0][0]
			 * != -1) prop = true;
			 */
            // MonVocabulaire.setVocab(biotex_input_dir, voc_type, s, prop, p,
            // v, LoadConfigFile.getMinDocs(), LoadConfigFile.getMinTF(), sk);
            // MonVocabulaire.setVocabAllWords(biotex_input_dir,
            // LoadConfigFile.getVocTypes()[0], LoadConfigFile.getMinDocs(),
            // LoadConfigFile.getMinTF());

            // setActivated all words but the stopwords
            // => dont work anymore :-(
            MonVocabulaire.setVocabAllWords(LoadConfigFile.getMinDocs(), LoadConfigFile.getMinTF());
            for (String sl : LoadConfigFile.getStopLists())
                if (!sl.isEmpty())
                    MonVocabulaire.removeStopwords(LoadDataset.getPath() + Constantes.separateur + sl);

			/* main training loop */
            trainModels(size_vocab_table, proportions, volumes, alpha_table, beta_table, nbruns, skipTerms, voc_type,
                    myExcelGroungTruth, exportTM);

//            trainEmbeddings();
        }
    }

    private static void trainEmbeddings() throws IOException {
        // get infos: origin and dest folders + batch, iterations, layer size.
        String inputFolder = LoadDataset.getPath() + Constantes.separateur + Constantes.DATA_DIR + Constantes.separateur
                + LoadDataset.getDataName();
        String outputFolder = LoadDataset.getPath() + Constantes.separateur + Constantes.RESULT_DIR + Constantes.separateur
                + LoadDataset.getDataName() + Constantes.separateur + "embeddings";

		/* Cleaning vector output */
        File directory = new File(outputFolder);
        if (!directory.exists()) {
            directory.mkdirs();
        } else {
            CommonsUtils.deleteFolderContent(directory);
        }

        int iterations = LoadConfigFile.getWeIterations();
        int batch = LoadConfigFile.getWeBatch();
        int dimensions = LoadConfigFile.getWeDimensions();

        log.info("Merging dataset....");
        File mergeDataset;

        mergeDataset = CommonsUtils.mergeDataset(inputFolder);

        log.info("Load data into memory....");
        SentenceIterator iter = new LineSentenceIterator(mergeDataset);

        log.info("Build model....");
        Word2Vec vec = new Word2Vec.Builder().batchSize(batch).minWordFrequency(10).useAdaGrad(false)
                .layerSize(dimensions) // word feature vector size
                .iterations(iterations) // # iterations to train
                .learningRate(0.01) //
                .minLearningRate(1e-3) // learning rate decays wrt # words.
                // floor learning
                .negativeSample(5) // sample size 10 words
                .iterate(iter) //
                .windowSize(5).tokenizerFactory(new DefaultTokenizerFactory()).build();
        vec.fit();

        log.info("Removed vectors of unactivated words...");
        int before = vec.getVocab().words().size();
        ArrayList<ForIndexing> activatedWords = MonVocabulaire.getActivatedTerms();
        ArrayList<ForIndexing> allTerms = MonVocabulaire.getAllTerms();
        Set<String> toRemove = new HashSet<String>();
        for (ForIndexing word : allTerms) {
            String term = word.getTerm();
            if (vec.getVocab().containsWord(term) && !activatedWords.contains(term)) {
                toRemove.add(term);
            }
        }
        for (String unactivatedTerm : toRemove) {
            vec.getVocab().removeElement(unactivatedTerm);
        }
        int after = vec.getVocab().words().size();
        log.info("Removed " + (before - after + 1) + " vectors of unactivated words. (before : " + before + ", after : "
                + after + ")");

        log.info("Save vectors....");
        try {
            WordVectorSerializer.writeWordVectors(vec, outputFolder + Constantes.separateur + "vectors.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void trainModels(int[] size_vocab_table, double[][] proportions, int[][] volumes,
                                    double[] alpha_table, double[] beta_table, int nbruns, int[][] skipTerms, String voc_type,
                                    MyExcelGroungTruth myExcelGroungTruth, IOTopicModel exportTM)
            throws IOException, BiffException, WriteException {
        long startTime;
        long estimatedTime;
        for (int size_vocab : size_vocab_table)
            for (double[] p : proportions)
                for (int[] v : volumes)
                    for (int[] sk : skipTerms) {

						/*
						 * calculer la vérité terrain pour les topics (on a
						 * besoin que le vocabulaire ait été identifié à l'aide
						 * de la méthode setVocab)
						 */
                        if (!LoadDataset.getGTfield().equals("")) {

                            ranking = new LabelRanking();
                            ranking.ranktopkterms(MonVocabulaire.getIndex());
                            // ranking.exportMesures(MonVocabulaire.getIndex(),
                            // "GT_20NG.xls");
                        }

                        for (double alpha : alpha_table)
                            for (double beta : beta_table) {

                                if (!LoadDataset.getGTfield().equals("")) { // common
                                    // statistics
                                    myExcelGroungTruth.getExport().addCommonStats(voc_type, p, v, size_vocab, alpha, beta,
                                            nbruns, sk);
                                }

                                // Running LDA
                                InstanceList instances = preprocessForTopicModeling();
                                startTime = System.nanoTime();
                                for (int i = 0; i < nbruns; i++) {
                                    ParallelTopicModel model = topicmodeling(instances, LoadConfigFile.getNBtopics(),
                                            LoadConfigFile.getNBiter(), alpha, beta);
                                    // topic evaluation when the ground
                                    // truth is known
                                    if (!LoadDataset.getGTfield().equals(""))
                                        evaluateTopics(instances, model);
                                    // export the topic model
                                    exportTM.export(instances.getAlphabet(), model);

									/*System.out.println("NB DOC exported: " + model.data.size());
									double[] proba = model.getTopicProbabilities(model.data.size()-1);
									System.out.println("PROBA = " + proba);*/

                                    // export the topic model also as jsonp for
                                    // the web app
                                    String json = TopicModelingToJson.toJson(model, 50);
                                    FileUtils.writeStringToFile(
                                            new File(exportTM.getPath() + Constantes.separateur + "model.jsonp"), json);

                                    // exportTM.export_correlation(model,
                                    // compute_correlation_topics(instances,
                                    // model));
                                }
                                estimatedTime = System.nanoTime() - startTime;

                                // export topic evaluation if there is a
                                // ground truth
                                if (!LoadDataset.getGTfield().equals("")) {
                                    OutputClustering.export_results(myExcelGroungTruth.getExport(),
                                            TimeUnit.NANOSECONDS.toSeconds(estimatedTime));
                                    myExcelGroungTruth.getExport().nextRow();
                                    OutputClustering best = OutputClustering.getBestOptimizedFunction();
                                    best.export(myExcelGroungTruth.getFolder_best() + Constantes.separateur,
                                            LoadDataset.getDataName(), voc_type, LoadConfigFile.getNBtopics(), size_vocab, p,
                                            alpha, beta, nbruns);
                                    OutputClustering.removeAll();
                                }

                            }
                    }
    }

    private static IOTopicModel initStuffForTopicExport() throws IOException {
        String result_output_dir = LoadDataset.getPath() + Constantes.separateur + Constantes.RESULT_DIR + Constantes.separateur
                + LoadDataset.getDataName() + Constantes.separateur + "models" + Constantes.separateur
                + new SimpleDateFormat("yyyy_MM_dd").format(new Date());
        String config_output_file = result_output_dir + Constantes.separateur + "configuration.txt";
        new File(result_output_dir).mkdirs();
        Files.copy(new File(config).toPath(), new File(config_output_file).toPath(),
                StandardCopyOption.REPLACE_EXISTING);
        IOTopicModel exportTM = new IOTopicModel(result_output_dir);
        return exportTM;
    }

    private static MyExcelGroungTruth initGroundTruthIfAny(String voc_type)
            throws IOException, WriteException {
        MyExcelGroungTruth myExcelGroungTruth = new MyExcelGroungTruth();

        if (!LoadDataset.getGTfield().equals("")) {
            System.out.println("Ground truth detected so the topics will be evaluated.");
            String result_output_dir = LoadDataset.getPath() + Constantes.separateur + Constantes.RESULT_DIR
                    + Constantes.separateur + LoadDataset.getDataName() + Constantes.separateur + "eval"
                    + Constantes.separateur + config;
            String result_output_file = voc_type + "_" + new SimpleDateFormat("yyyy_MM_dd").format(new Date()) + "_K"
                    + LoadConfigFile.getNBtopics() + ".xls";
            new File(result_output_dir).mkdirs();
            String folder_best = result_output_dir + Constantes.separateur + voc_type + "_"
                    + new SimpleDateFormat("yyyy_MM_dd").format(new Date()) + "_K" + LoadConfigFile.getNBtopics()
                    + Constantes.separateur;
            myExcelGroungTruth.setFolder_best(folder_best);
            new File(folder_best).mkdirs();
            myExcelGroungTruth.setExport(
                    new ExportResults(result_output_dir + Constantes.separateur + result_output_file, "LDA"));
        } else {
            System.out.println("No ground truth so no topic evaluation...");
        }
        return myExcelGroungTruth;
    }

    private static void extractCorpus() throws IOException {
        System.out.print("Document extraction: ");
        long startTime = System.nanoTime();
        LoadDataset.extractDocs();
        long estimatedTime = System.nanoTime() - startTime;
        System.out.println(TimeUnit.NANOSECONDS.toMillis(estimatedTime) + " ms");
    }

    public static InstanceList preprocessForTopicModeling() {
        BuildFeatures4Mallet buildF = new BuildFeatures4Mallet();
        InstanceList instances = new InstanceList(new Noop());
        // int i = 0;
        long startTime = System.nanoTime();
        for (String k : MyDocument.getAllKeys()) {
            // i++;
            MyDocument d = MyDocument.get(k);
            instances.addThruPipe(buildF.addInstanceAsSequence(d));
			/*
			 * Instance ii = instances.get(i); System.out.print(ii+"-"); String
			 * name = (String)ii.getName(); System.out.println(name+"//"); if
			 * (i>20) { System.exit(0); }
			 */
        }
        long estimatedTime = System.nanoTime() - startTime;
        System.out.println(MyDocument.size() + " indexed documents for LDA in "
                + TimeUnit.NANOSECONDS.toMillis(estimatedTime) + " ms");
        return instances;
    }

    private static ParallelTopicModel topicmodeling(InstanceList instances, int k, int iter, double alpha,
                                                    double beta) {
        // preparing for MALLET

        // Create a model with 100 topics, alpha_t = 0.01, beta_w = 0.01
        // Note that the first parameter is passed as the sum over topics, while
        // the second is the parameter for a single dimension of the Dirichlet
        // prior.
        int numTopics = k;
        double alp = alpha;
        double bet = beta;
        if (alpha == -1) // if alpha == beta == -1 then alpha and beta will be
        // estimated automatically
        {
            alp = 0.01;
            bet = 0.01;
        }
        ParallelTopicModel model = new ParallelTopicModel(numTopics, alp, bet);

        model.addInstances(instances);

        // Use two parallel samplers, which each look at one half the corpus and
        // combine
        // statistics after every iteration.
        model.setNumThreads(LoadConfigFile.getNBthreads());

        if (alpha != -1)
            model.setOptimizeInterval(0);

        model.setTopicDisplay(0, 0);

        model.setNumIterations(iter);

        try {
            model.estimate();
            model.maximize(100);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return model;

    }

    public static void evaluateTopics(InstanceList instances, ParallelTopicModel model) {
        OutputClustering out = new OutputClustering(instances.size());
        Alphabet dataAlphabet = instances.getDataAlphabet();

        int k = model.getNumTopics();

        out.setOptimizedValue(model.modelLogLikelihood());

        // part (I) on clustering

        for (int i = 0; i < instances.size(); i++) {
            Instance inst = instances.get(i);
            double[] topicDistribution = model.getTopicProbabilities(i);
            double max = -1;
            int ind = -1;
            for (int j = 0; j < k; j++) {
                if (topicDistribution[j] > max) {
                    max = topicDistribution[j];
                    ind = j;
                }
            }
            String id = (String) inst.getName();
            // set the current document ID
            out.setLabel(id);
            // set the topic number for the current document
            out.setTopic(ind);
            // set the ground truth for the current document
            out.setGroundTruth(MyDocument.get(id).getGround_truth());
            // go to the next document
            out.next();
        }

        out.setIntegers2Labels();
        out.computeConfusionMatrix();
        out.attributeTopics();
        out.setClusterEvaluator();
        out.computePurity();
        out.computeARI();

        // part (II) on topics

        ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();
        for (int topic_id = 0; topic_id < k; topic_id++) {
            Iterator<IDSorter> iterator = topicSortedWords.get(topic_id).iterator();
            int rank = 0;
            while (iterator.hasNext() && rank < OrderedLabels.MAX_TOP_TERMS) {
                IDSorter idCountPair = iterator.next();
                String term = (String) dataAlphabet.lookupObject(idCountPair.getID());
                double w = idCountPair.getWeight();
                out.addTopTerm("topic " + topic_id, term, w);
                rank++;
            }
        }
        out.computeIntersectionMeasures(ranking);

        OutputClustering.add(out);

    }

    public static void simplePrinting(InstanceList instances, ParallelTopicModel model, int instanceID) {

        // Show the words and topics in the first instance

        // The data alphabet maps word IDs to strings
        Alphabet dataAlphabet = instances.getDataAlphabet();
        FeatureSequence tokens = (FeatureSequence) model.getData().get(instanceID).instance.getData();
        LabelSequence topics = model.getData().get(instanceID).topicSequence;

        // System.out.println("suite des topics = " + topics);

        // System.out.println("inst : " + instances);

        Formatter out = new Formatter(new StringBuilder(), Locale.US);
        out.format("%d: ", instanceID);
        for (int position = 0; position < tokens.getLength(); position++) {
            out.format("%s-%d ", dataAlphabet.lookupObject(tokens.getIndexAtPosition(position)),
                    topics.getIndexAtPosition(position));
        }
        System.out.println(out);

        // Estimate the topic distribution of the first instance,
        // given the current Gibbs state.
        double[] topicDistribution = model.getTopicProbabilities(instanceID);

        // Get an array of sorted sets of word ID/count pairs
        ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();

        // Show top 5 words in topics with proportions for the first document
        for (int topic = 0; topic < model.getNumTopics(); topic++) {
            Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();

            out = new Formatter(new StringBuilder(), Locale.US);
            out.format("%d\t%.3f\t", topic, topicDistribution[topic]);
            int rank = 0;
            while (iterator.hasNext() && rank < 5) {
                IDSorter idCountPair = iterator.next();
                out.format("%s (%.0f) ", dataAlphabet.lookupObject(idCountPair.getID()), idCountPair.getWeight());
                rank++;
            }
            // System.out.println(out);
        }

        // System.out.println("Fin : ");

    }

}
