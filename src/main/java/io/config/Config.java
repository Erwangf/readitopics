package io.config;

import io.LoadDataset;

import java.io.*;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Dirty Config all-public object
 * <br>
 * <p>
 * <p>
 * Deadline approaching, we need a way to manage configs (create, save, find, etc).
 * <br>
 * <p>
 * However, in order to do so, we should refactor the way the application use LoadConfigFile.
 * It's a static class, initialized at the beginning of the program, and use by a lot of
 * components. There is also LoadDataset, a similar, static, class, containing some information
 * from the configuration file. So, config is split in 2 static (bad bad pattern !), widely used
 * in this application. The cost of refactoring is high, and the deadline approaching.
 * <br>
 * So this class is a dirty, temporary (or not...) class for handling config files.
 *
 * @author Erwan Giry-Fouquet
 */
public class Config {

    public String name;
    public String path;

    public Config() {

    }


    public Config(String configPath) throws IOException {
        this(new File(configPath));
    }



    public Config(File file) throws IOException {


        readFromFile(file);

    }


    public void readFromFile(File file) throws IOException {
        FileInputStream is = new FileInputStream(file);
        Properties prop = new Properties();
        prop.load(is);

        name = file.getName();
        path = file.getPath();

        // load all properties
        WE_BATCH = prop.getProperty("WE_BATCH");
        WE_DIMENSION = prop.getProperty("WE_DIMENSION");
        WE_ITERATIONS = prop.getProperty("WE_ITERATIONS");
        BIOTEX_TMP = prop.getProperty("BIOTEX_TMP");
        NB_THREADS = prop.getProperty("NB_THREADS");
        NB_ITER = prop.getProperty("NB_ITER");
        NB_TOPICS = prop.getProperty("NB_TOPICS");
        VOCABULARY = prop.getProperty("VOCABULARY");
        SIZE_VOCAB = prop.getProperty("SIZE_VOCAB");
        PROPORTIONS = prop.getProperty("PROPORTIONS");
        VOLUMES = prop.getProperty("VOLUMES");
        ALPHA = prop.getProperty("ALPHA");
        BETA = prop.getProperty("BETA");
        NB_RUNS = prop.getProperty("NB_RUNS");
        SKIPTERMS = prop.getProperty("SKIPTERMS");
        DATA = prop.getProperty("DATA");
        SOURCE = prop.getProperty("SOURCE");
        PATH = prop.getProperty("PATH");
        TEXT = prop.getProperty("TEXT");
        GTRUTH = prop.getProperty("GTRUTH");
        TIME = prop.getProperty("TIME");
        ID = prop.getProperty("ID");
        INDEXING = prop.getProperty("INDEXING");
        STOPLIST = prop.getProperty("STOPLIST");
        PREPROCESS = prop.getProperty("PREPROCESS");
        DATES = prop.getProperty("DATES");
        TITLE = prop.getProperty("TITLE");
        AUTHOR = prop.getProperty("AUTHOR");
        RAWDATA = prop.getProperty("RAWDATA");
        RAWLINES = prop.getProperty("RAWLINES");
        LANGUAGE = prop.getProperty("LANGUAGE");
        INDEX_LUCENE = prop.getProperty("INDEX_LUCENE");

        is.close();
    }



    public Properties getProperties(){
        Properties prop = new Properties();

        if(WE_BATCH!= null) prop.setProperty("WE_BATCH",WE_BATCH);
        if(WE_DIMENSION!= null) prop.setProperty("WE_DIMENSION",WE_DIMENSION);
        if(WE_ITERATIONS!= null) prop.setProperty("WE_ITERATIONS",WE_ITERATIONS);
        if(BIOTEX_TMP!= null) prop.setProperty("BIOTEX_TMP",BIOTEX_TMP);
        if(NB_THREADS!= null) prop.setProperty("NB_THREADS",NB_THREADS);
        if(NB_ITER!= null) prop.setProperty("NB_ITER",NB_ITER);
        if(NB_TOPICS!= null) prop.setProperty("NB_TOPICS",NB_TOPICS);
        if(VOCABULARY!= null) prop.setProperty("VOCABULARY",VOCABULARY);
        if(SIZE_VOCAB!= null) prop.setProperty("SIZE_VOCAB",SIZE_VOCAB);
        if(PROPORTIONS!= null) prop.setProperty("PROPORTIONS",PROPORTIONS);
        if(VOLUMES!= null) prop.setProperty("VOLUMES",VOLUMES);
        if(ALPHA!= null) prop.setProperty("ALPHA",ALPHA);
        if(BETA!= null) prop.setProperty("BETA",BETA);
        if(NB_RUNS!= null) prop.setProperty("NB_RUNS",NB_RUNS);
        if(SKIPTERMS!= null) prop.setProperty("SKIPTERMS",SKIPTERMS);
        if(DATA!= null) prop.setProperty("DATA",DATA);
        if(SOURCE!= null) prop.setProperty("SOURCE",SOURCE);
        if(PATH!= null) prop.setProperty("PATH",PATH);
        if(TEXT!= null) prop.setProperty("TEXT",TEXT);
        if(GTRUTH!= null) prop.setProperty("GTRUTH",GTRUTH);
        if(TIME!= null) prop.setProperty("TIME",TIME);
        if(ID!= null) prop.setProperty("ID",ID);
        if(INDEXING!= null) prop.setProperty("INDEXING",INDEXING);
        if(STOPLIST!= null) prop.setProperty("STOPLIST",STOPLIST);
        if(PREPROCESS!= null) prop.setProperty("PREPROCESS",PREPROCESS);
        if(DATES!= null) prop.setProperty("DATES",DATES);
        if(TITLE!= null) prop.setProperty("TITLE",TITLE);
        if(AUTHOR!= null) prop.setProperty("AUTHOR",AUTHOR);
        if(RAWDATA!= null) prop.setProperty("RAWDATA",RAWDATA);
        if(RAWLINES!= null) prop.setProperty("RAWLINES",RAWLINES);
        if(LANGUAGE!= null) prop.setProperty("LANGUAGE",LANGUAGE);
        if(INDEX_LUCENE!= null) prop.setProperty("INDEX_LUCENE",INDEX_LUCENE);

        return(prop);
    }

    public void writeToFile(String targetPath) throws IOException {

        FileOutputStream fos = new FileOutputStream(targetPath);
        getProperties().store(fos,"");
        fos.close();
    }

    public void writeToDefaultPath()throws IOException {
        FileOutputStream fos = new FileOutputStream(path);
        getProperties().store(fos,"");
        fos.close();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Config{");
        sb.append("name='").append(name).append('\'');
        sb.append(", path='").append(path).append('\'');
        if(WE_BATCH!= null) sb.append(", WE_BATCH='").append(WE_BATCH).append('\'');
        if(WE_DIMENSION!= null)  sb.append(", WE_DIMENSION='").append(WE_DIMENSION).append('\'');
        if(WE_ITERATIONS!= null) sb.append(", WE_ITERATIONS='").append(WE_ITERATIONS).append('\'');
        if(BIOTEX_TMP!= null)  sb.append(", BIOTEX_TMP='").append(BIOTEX_TMP).append('\'');
        if(NB_THREADS!= null)  sb.append(", NB_THREADS='").append(NB_THREADS).append('\'');
        if(NB_ITER!= null)  sb.append(", NB_ITER='").append(NB_ITER).append('\'');
        if(NB_TOPICS!= null)  sb.append(", NB_TOPICS='").append(NB_TOPICS).append('\'');
        if(VOCABULARY!= null)  sb.append(", VOCABULARY='").append(VOCABULARY).append('\'');
        if(SIZE_VOCAB!= null)  sb.append(", SIZE_VOCAB='").append(SIZE_VOCAB).append('\'');
        if(PROPORTIONS!= null)  sb.append(", PROPORTIONS='").append(PROPORTIONS).append('\'');
        if(VOLUMES!= null)  sb.append(", VOLUMES='").append(VOLUMES).append('\'');
        if(ALPHA!= null)  sb.append(", ALPHA='").append(ALPHA).append('\'');
        if(BETA!= null)  sb.append(", BETA='").append(BETA).append('\'');
        if(NB_RUNS!= null)  sb.append(", NB_RUNS='").append(NB_RUNS).append('\'');
        if(SKIPTERMS!= null)  sb.append(", SKIPTERMS='").append(SKIPTERMS).append('\'');
        if(DATA!= null)  sb.append(", DATA='").append(DATA).append('\'');
        if(SOURCE!= null)  sb.append(", SOURCE='").append(SOURCE).append('\'');
        if(PATH!= null)  sb.append(", PATH='").append(PATH).append('\'');
        if(TEXT!= null) sb.append(", TEXT='").append(TEXT).append('\'');
        if(GTRUTH!= null)  sb.append(", GTRUTH='").append(GTRUTH).append('\'');
        if(TIME!= null)  sb.append(", TIME='").append(TIME).append('\'');
        if(ID!= null)  sb.append(", ID='").append(ID).append('\'');
        if(INDEXING!= null)  sb.append(", INDEXING='").append(INDEXING).append('\'');
        if(STOPLIST!= null)  sb.append(", STOPLIST='").append(STOPLIST).append('\'');
        if(DATES!= null)  sb.append(", DATES='").append(DATES).append('\'');
        if(PREPROCESS!= null)  sb.append(", PREPROCESS='").append(PREPROCESS).append('\'');
        if(TITLE!= null)  sb.append(", TITLE='").append(TITLE).append('\'');
        if(AUTHOR!= null)  sb.append(", AUTHOR='").append(AUTHOR).append('\'');
        if(RAWDATA!= null)  sb.append(", RAWDATA='").append(RAWDATA).append('\'');
        if(RAWLINES!= null)  sb.append(", RAWLINES='").append(RAWLINES).append('\'');
        if(LANGUAGE!= null)  sb.append(", LANGUAGE='").append(LANGUAGE).append('\'');
        if(INDEX_LUCENE!= null)  sb.append(", INDEX_LUCENE='").append(INDEX_LUCENE).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public String WE_BATCH;

    public String WE_DIMENSION;

    public String WE_ITERATIONS;

    public String BIOTEX_TMP;

    public String NB_THREADS;

    public String NB_ITER;

    public String NB_TOPICS;

    public String VOCABULARY;

    public String SIZE_VOCAB;

    public String PROPORTIONS;

    public String VOLUMES;

    public String ALPHA;

    public String BETA;

    public String NB_RUNS;

    public String SKIPTERMS;

    public String DATA;

    public String SOURCE;

    public String PATH;

    public String TEXT;

    public String GTRUTH;

    public String TIME;

    public String ID;

    public String INDEXING;

    public String STOPLIST;

    public String DATES;

    public String PREPROCESS;

    public String TITLE;

    public String AUTHOR;

    public String RAWDATA;

    public String RAWLINES;

    public String LANGUAGE;

    public String INDEX_LUCENE;


}
