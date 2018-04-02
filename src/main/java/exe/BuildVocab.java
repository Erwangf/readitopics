package exe;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import core.Constantes;
import core.MonVocabulaire;
import core.MyDocument;
import io.config.LoadConfigFile;
import io.LoadDataset;

/**
 * Cette classe est dédiée à la génération de vocabulaire avec Biotex.
 * En plus, elle ajoute deux vocabulaires spécifiques : un basé sur le nombre d'occurrences (TF) et un aléatoire (Random).
 *
 * @author julien
 */

public class BuildVocab {
    public static void main(String[] args) throws IOException {
        if (args.length == 0) throw new IOException("Require at least 1 argument: configuration file");

        String config = args[0];

        buildVocab(config,true);
    }

    public static void buildVocab(String config, boolean buildBiotex) throws IOException {
        System.out.println("Build the vocabulary with Biotex");

        InputStream inputStream;
        inputStream = new FileInputStream(config);

        // load config
        LoadConfigFile.loadConfig(inputStream);


        String biotex_input_dir = LoadDataset.getPath() + Constantes.separateur + Constantes.RESULT_DIR
                + Constantes.separateur + LoadDataset.getDataName() + Constantes.separateur + "biotex";

        // create directory
        new File(biotex_input_dir).mkdirs();

		/* extraction des documents en mémoire + construction du fichier .txt source pour biotex */
        System.out.print("Extraction des documents : ");
        long startTime = System.nanoTime();
        LoadDataset.extractDocs();

        if (buildBiotex){
            // output the text file needed by biotex
            MyDocument.print2File(biotex_input_dir + Constantes.separateur + "biotex_source.txt");
        }

        long estimatedTime = System.nanoTime() - startTime;
        System.out.println(TimeUnit.NANOSECONDS.toMillis(estimatedTime) + " ms");

		/* extraction des termes avec biotex */
        if (buildBiotex) {
            MonVocabulaire.extractTerminology(biotex_input_dir, "biotex_source.txt", "LIDF_value");
            //MonVocabulaire.extractTerminology(biotex_input_dir,  "biotex_source.txt", "F-TFIDF-C_A");
            //MonVocabulaire.extractTerminology(biotex_input_dir,  "biotex_source.txt", "TFIDF_A");
            //MonVocabulaire.extractTerminology(biotex_input_dir,  "biotex_source.txt", "F-OCapi_A");
            //MonVocabulaire.extractTerminology(biotex_input_dir,  "biotex_source.txt", "C_value");
            //MonVocabulaire.extractTerminology(biotex_input_dir,  "biotex_source.txt", "TFIDF_S");
        }

		/* indexation des documents avec l'un des vocabulaires */
        String path = biotex_input_dir + Constantes.separateur + "LIDF_value" + Constantes.separateur;
//		String path = biotex_input_dir + Constantes.separateur + "TFIDF_S" + Constantes.separateur;
        //String path = biotex_input_dir + Constantes.separateur + "TFIDF_S " + Constantes.separateur;

        MonVocabulaire.indexing(path);
        //MonVocabulaire.indexing(biotex_input_dir);

		/* extraction de la liste des termes basés sur TF ou	 Random */
        //startTime = System.nanoTime();
        MonVocabulaire.extractTermBasedOnTF(biotex_input_dir, "TF");

        //MonVocabulaire.extractTermBasedOnRandom(biotex_input_dir, "Rand");
        estimatedTime = System.nanoTime() - startTime;
        //System.out.println("Calcul du vocabulaire basé sur Random : " + TimeUnit.NANOSECONDS.toMillis(estimatedTime) + " ms");
    }

}
