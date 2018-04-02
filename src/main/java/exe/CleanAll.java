package exe;

import core.Constantes;
import opennlp.tools.parser.Cons;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Scanner;

/**
 * Can be used by any user to clean resultats and WebServer/datasets folders
 */
public class CleanAll {

    public static void main(String[] args) throws IOException {

        String resultatsFolderPath = "." + Constantes.separateur + Constantes.RESULT_DIR;
        String webServerDatasetsFolderPath = "." + Constantes.separateur + "WebServer" + Constantes.separateur + "datasets";

        File resultFolder = new File(resultatsFolderPath);
        File wsdFolder = new File(webServerDatasetsFolderPath);

        System.out.println("This program will clean the following folders : \n"
                + resultFolder.getAbsolutePath() + "\n" + wsdFolder.getAbsolutePath());

        Scanner sc = new Scanner(System.in);
        System.out.println("\nAre you sure ? (Enter 'yes')");
        String res = sc.nextLine();
        if (res.equalsIgnoreCase("yes")) {
            FileUtils.cleanDirectory(resultFolder);
            FileUtils.cleanDirectory(new File(webServerDatasetsFolderPath));
            System.out.println("Done !");
        }
        System.out.println("Program will exit");
        System.exit(0);

    }
}
