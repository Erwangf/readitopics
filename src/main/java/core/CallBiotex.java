package core;

import BioTex.Execution;
import BuildListToValidate.BuildFilterManyLists;
import Object.CandidatTerm;
import io.config.LoadConfigFile;
import io.LoadDataset;

import java.io.File;
import java.util.ArrayList;

/**
 *
 * @author jimmy
 */
public class CallBiotex {

    private static ArrayList<CandidatTerm> list_candidat_terms_validated = new ArrayList<>();

    /**
     * Classe principale de BioTex fournie par Juan Lossio permettant
     * l'extraction de termes
     *
     * @param inputFile Fichier correctement formé constitutant l'ensemble des
     * documents
     * @param outputDir Dossier de sortie des fichiers. Si le dossier n'existe
     * pas, il est alors créé
     * @param voc_type Type de vocabulaire
     */
    public static ArrayList<CandidatTerm> BioTex(String inputFile, String outputDir, String voc_type) {

        /*
         * Variables to find: the Pattern List, DataSetReference for Validation, and file where the Tagger Tool is installed
         */
        String source_patterns = LoadDataset.getPath() + Constantes.separateur + "BioTex" + Constantes.separateur + "patterns";
        String source_dataset_reference = LoadDataset.getPath() + Constantes.separateur + "BioTex" + Constantes.separateur + "dataSetReference";
        String source_stop_words = LoadDataset.getPath() + Constantes.separateur + "BioTex" + Constantes.separateur + "stopWords";        
        String source_tagger = LoadDataset.getPath() + Constantes.separateur + "TreeTagger";
        
        /*
         * Variable that saves the extracted terms
         */
        String source_OUTPUT = outputDir; //Mettre le dossier où vous voulez que les fichiers se sauvegardent
        File output_d = new File(source_OUTPUT);
        output_d.mkdir();//Tentative de création du dossier dans l'éventualité où cela n'était pas déjà fait

        /*
         * File to be analized for the term extraction
         */
        String file_to_be_analyzed = inputFile;

        /*
         * Language : english, french, spanish 
         * number_patrons : number of first pattern to take into account 
         * typeTerms : all (single word + multi words terms), 
         * 			   multi (multi words terms) 
         * measure = 15 possible measures 
         * tool_Tagger: TreeTagger by default
         */
        String type_of_terms = "all"; // all    multi
        String language = LoadDataset.getLanguage(); // english french spanish
        //String language = "french"; 
        int frequency_min_of_terms = 4; // frequency minimal to extract the terms
        
        /*if(ParameterVerification.verifySource_is_TextFile(file_to_be_analyzed)==1)
        	System.out.println("OK !");*/
        
        list_candidat_terms_validated = Execution.main_execution(
                language, //english french spanish
                200, // nombre de patrons
                type_of_terms,
                voc_type,
                //"LIDF_value", // For one document       :   L_value     C_value 
                // For a set of documents :   LIDF_value  F-OCapi_A   F-OCapi_M   F-OCapi_S   F-TFIDF-C_A     F-TFIDF-C_M     F-TFIDF-C_S
                //                            TFIDF_A     TFIDF_M     TFIDF_S     Okapi_A     Okapi_M     Okapi_S                                            
                2,/* 1 = sin	gle file (only for L_value  or C_value)
                 2 = set of files (for LIDF-value or any measure)
                 */
                frequency_min_of_terms,
                file_to_be_analyzed,
                "TreeTagger",
                source_patterns,
                source_dataset_reference,
                source_tagger,
                source_OUTPUT,
                LoadConfigFile.getTmpPathForBiotex()
        );

        if (!source_OUTPUT.isEmpty())
        	BuildFilterManyLists.createList(list_candidat_terms_validated, source_stop_words, source_OUTPUT, type_of_terms, language);
        return list_candidat_terms_validated;
    }

}
