package io.config;

import core.Constantes;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class ConfigManager {


    public static List<Config> findConfig(String query) {

        // parse query into words
        String[] words = query.split(" ");


        List<Config> result = new LinkedList<>();

        for (File file : getConfigFiles()) {
            for (String word : words) {
                if(file.getName().contains(word)){
                    try {
                        result.add(new Config(file));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        return result;
    }

    public static List<String> getAllConfigNames(){

        List<File> list = getConfigFiles();
        List<String> result = new LinkedList<>();

        for (File file : list) {
            result.add(file.getName());
        }

        return(result);
    }

    public static Config getConfigByName(String name) throws IOException {
        List<File> filesArray = getConfigFiles();
        File targetFile = null;
        for (File file : filesArray) {
            if (file.getName().equals(name)) {
                targetFile = file;
                break;
            }
        }
        return new Config(targetFile);
    }

    public static void createConfig(HashMap<String,String> rawConfig){
        Config c = new Config();
        c.name = rawConfig.get("name");
        c.path = "." + Constantes.separateur + Constantes.CONFIG_DIR + Constantes.separateur + "config." + c.name;

        if(rawConfig.get("WE_BATCH")!=null) c.WE_BATCH = rawConfig.get("WE_BATCH");
        if(rawConfig.get("WE_DIMENSION")!=null) c.WE_DIMENSION = rawConfig.get("WE_DIMENSION");
        if(rawConfig.get("WE_ITERATIONS")!=null) c.WE_ITERATIONS = rawConfig.get("WE_ITERATIONS");
        if(rawConfig.get("BIOTEX_TMP")!=null) c.BIOTEX_TMP = rawConfig.get("BIOTEX_TMP");
        if(rawConfig.get("NB_THREADS")!=null) c.NB_THREADS = rawConfig.get("NB_THREADS");
        if(rawConfig.get("NB_ITER")!=null) c.NB_ITER = rawConfig.get("NB_ITER");
        if(rawConfig.get("NB_TOPICS")!=null) c.NB_TOPICS = rawConfig.get("NB_TOPICS");
        if(rawConfig.get("VOCABULARY")!=null) c.VOCABULARY = rawConfig.get("VOCABULARY");
        if(rawConfig.get("SIZE_VOCAB")!=null) c.SIZE_VOCAB = rawConfig.get("SIZE_VOCAB");
        if(rawConfig.get("PROPORTIONS")!=null) c.PROPORTIONS = rawConfig.get("PROPORTIONS");
        if(rawConfig.get("VOLUMES")!=null) c.VOLUMES = rawConfig.get("VOLUMES");
        if(rawConfig.get("ALPHA")!=null) c.ALPHA = rawConfig.get("ALPHA");
        if(rawConfig.get("BETA")!=null) c.BETA = rawConfig.get("BETA");
        if(rawConfig.get("NB_RUNS")!=null) c.NB_RUNS = rawConfig.get("NB_RUNS");
        if(rawConfig.get("SKIPTERMS")!=null) c.SKIPTERMS = rawConfig.get("SKIPTERMS");
        if(rawConfig.get("DATA")!=null) c.DATA = rawConfig.get("DATA");
        if(rawConfig.get("SOURCE")!=null) c.SOURCE = rawConfig.get("SOURCE");
        if(rawConfig.get("PATH")!=null) c.PATH = rawConfig.get("PATH");
        if(rawConfig.get("TEXT")!=null) c.TEXT = rawConfig.get("TEXT");
        if(rawConfig.get("GTRUTH")!=null) c.GTRUTH = rawConfig.get("GTRUTH");
        if(rawConfig.get("TIME")!=null) c.TIME = rawConfig.get("TIME");
        if(rawConfig.get("ID")!=null) c.ID = rawConfig.get("ID");
        if(rawConfig.get("INDEXING")!=null) c.INDEXING = rawConfig.get("INDEXING");
        if(rawConfig.get("STOPLIST")!=null) c.STOPLIST = rawConfig.get("STOPLIST");
        if(rawConfig.get("DATES")!=null) c.DATES = rawConfig.get("DATES");
        if(rawConfig.get("PREPROCESS")!=null) c.PREPROCESS = rawConfig.get("PREPROCESS");
        if(rawConfig.get("TITLE")!=null) c.TITLE = rawConfig.get("TITLE");
        if(rawConfig.get("AUTHOR")!=null) c.AUTHOR = rawConfig.get("AUTHOR");
        if(rawConfig.get("RAWDATA")!=null) c.RAWDATA = rawConfig.get("RAWDATA");
        if(rawConfig.get("RAWLINES")!=null) c.RAWLINES = rawConfig.get("RAWLINES");
        if(rawConfig.get("LANGUAGE")!=null) c.LANGUAGE = rawConfig.get("LANGUAGE");
        if(rawConfig.get("INDEX_LUCENE")!=null) c.INDEX_LUCENE = rawConfig.get("INDEX_LUCENE");
        try {
            c.writeToDefaultPath();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }




    public static List<Config> getAllConfigs() {
        List<File> list = getConfigFiles();
        List<Config> result = new LinkedList<>();

        for (File file : list) {
            try {
                result.add(new Config(file));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return(result);
    }

    private static List<File> getConfigFiles() {
        String foldpath = "." + Constantes.separateur + Constantes.CONFIG_DIR;
        File file = new File(foldpath);
        List<File> allFiles = Arrays.asList(file.listFiles());
        List<File> filteredList = new LinkedList<>();
        for (File f : allFiles) {
            if(f.getName().startsWith("config.")){
                filteredList.add(f);
            }
        }
        return filteredList;
    }

    // TODO Delete a config by name
    public static void deleteConfig(String name) {

        try {
            Config c = getConfigByName(name);
            Files.delete(Paths.get(c.path));
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static void editConfig(HashMap<String, String> rawConfig) {

        String name = rawConfig.get("name");
        Config c = null;
        try {
            c = getConfigByName(name);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }



        if(rawConfig.get("WE_BATCH")!=null) c.WE_BATCH = rawConfig.get("WE_BATCH");
        if(rawConfig.get("WE_DIMENSION")!=null) c.WE_DIMENSION = rawConfig.get("WE_DIMENSION");
        if(rawConfig.get("WE_ITERATIONS")!=null) c.WE_ITERATIONS = rawConfig.get("WE_ITERATIONS");
        if(rawConfig.get("BIOTEX_TMP")!=null) c.BIOTEX_TMP = rawConfig.get("BIOTEX_TMP");
        if(rawConfig.get("NB_THREADS")!=null) c.NB_THREADS = rawConfig.get("NB_THREADS");
        if(rawConfig.get("NB_ITER")!=null) c.NB_ITER = rawConfig.get("NB_ITER");
        if(rawConfig.get("NB_TOPICS")!=null) c.NB_TOPICS = rawConfig.get("NB_TOPICS");
        if(rawConfig.get("VOCABULARY")!=null) c.VOCABULARY = rawConfig.get("VOCABULARY");
        if(rawConfig.get("SIZE_VOCAB")!=null) c.SIZE_VOCAB = rawConfig.get("SIZE_VOCAB");
        if(rawConfig.get("PROPORTIONS")!=null) c.PROPORTIONS = rawConfig.get("PROPORTIONS");
        if(rawConfig.get("VOLUMES")!=null) c.VOLUMES = rawConfig.get("VOLUMES");
        if(rawConfig.get("ALPHA")!=null) c.ALPHA = rawConfig.get("ALPHA");
        if(rawConfig.get("BETA")!=null) c.BETA = rawConfig.get("BETA");
        if(rawConfig.get("NB_RUNS")!=null) c.NB_RUNS = rawConfig.get("NB_RUNS");
        if(rawConfig.get("SKIPTERMS")!=null) c.SKIPTERMS = rawConfig.get("SKIPTERMS");
        if(rawConfig.get("DATA")!=null) c.DATA = rawConfig.get("DATA");
        if(rawConfig.get("SOURCE")!=null) c.SOURCE = rawConfig.get("SOURCE");
        if(rawConfig.get("PATH")!=null) c.PATH = rawConfig.get("PATH");
        if(rawConfig.get("TEXT")!=null) c.TEXT = rawConfig.get("TEXT");
        if(rawConfig.get("GTRUTH")!=null) c.GTRUTH = rawConfig.get("GTRUTH");
        if(rawConfig.get("TIME")!=null) c.TIME = rawConfig.get("TIME");
        if(rawConfig.get("ID")!=null) c.ID = rawConfig.get("ID");
        if(rawConfig.get("INDEXING")!=null) c.INDEXING = rawConfig.get("INDEXING");
        if(rawConfig.get("STOPLIST")!=null) c.STOPLIST = rawConfig.get("STOPLIST");
        if(rawConfig.get("DATES")!=null) c.DATES = rawConfig.get("DATES");
        if(rawConfig.get("PREPROCESS")!=null) c.PREPROCESS = rawConfig.get("PREPROCESS");
        if(rawConfig.get("TITLE")!=null) c.TITLE = rawConfig.get("TITLE");
        if(rawConfig.get("AUTHOR")!=null) c.AUTHOR = rawConfig.get("AUTHOR");
        if(rawConfig.get("RAWDATA")!=null) c.RAWDATA = rawConfig.get("RAWDATA");
        if(rawConfig.get("RAWLINES")!=null) c.RAWLINES = rawConfig.get("RAWLINES");
        if(rawConfig.get("LANGUAGE")!=null) c.LANGUAGE = rawConfig.get("LANGUAGE");
        if(rawConfig.get("INDEX_LUCENE")!=null) c.INDEX_LUCENE = rawConfig.get("INDEX_LUCENE");
        try {
            c.writeToDefaultPath();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
