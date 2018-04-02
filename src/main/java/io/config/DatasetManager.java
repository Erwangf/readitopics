package io.config;

import core.Constantes;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DatasetManager {
    private static String removeExtension(String filename){
        if(filename.lastIndexOf('.')==-1){
            return filename;
        }
        else return filename.substring(0, filename.lastIndexOf('.'));
    }

    public static List<Dataset> getAllDatasets(){

        String foldpath = "." + Constantes.separateur + Constantes.DATA_DIR;
        File file = new File(foldpath);
        List<File> allFiles = Arrays.asList(file.listFiles());

        return allFiles.stream()
                .filter((d)->!d.getName().equals(".gitignore"))
                .map((a)->new Dataset(removeExtension(a.getName()),a.getPath()))
                .collect(Collectors.toList());
    }

    public static Dataset getDatasetByName(String name){
      return getAllDatasets().stream()
              .filter((dataset -> removeExtension(dataset.getName()).equals(name)))
              .findFirst().get();
    }

    public static void createDataset(String name, String type){

    }

    public static void deleteDataset(String name){

        Dataset ds = getDatasetByName(name);
        File file = new File(ds.getPath());
        try {
            //Deleting the directory recursively using FileUtils.
            if(file.isDirectory()){
                FileUtils.deleteDirectory(file);
            } else {
                Files.delete(file.toPath());
            }

            System.out.println("Dataset" + name +" has been deleted recursively !");
        } catch (IOException e) {
            System.out.println("Problem occurs when deleting the directory : " + ds.getPath());
            e.printStackTrace();
        }

    }


    public static void main(String[] args) {
        getAllDatasets().forEach(System.out::println);
        System.out.println(getDatasetByName("ASOIAF"));
    }


}
