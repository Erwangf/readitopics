package io;

import core.Constantes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class UnzipFile {
    public static void unzip(String fileZip,String destination) throws IOException {
        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZip));
        ZipEntry zipEntry = zis.getNextEntry();
        boolean first = true;
        while(zipEntry != null){
            String fileName = zipEntry.getName();
            String extension = fileName.substring(fileName.lastIndexOf('.'), fileName.length());

            if(first && !zipEntry.isDirectory()
                    && !extension.equals(".csv")
                    && !extension.equals(".xml")
                    && !extension.equals(".dtd")){
                File newDirFile = new File(destination + fileName.substring(0, fileName.lastIndexOf('.')));
                newDirFile.mkdirs();
                fileName = newDirFile.getName() + Constantes.separateur + fileName;
            }
            first = false;

            File newFile = new File(destination + fileName);
            if(zipEntry.isDirectory()){
                newFile.mkdirs();
            }
            else{
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }

            zipEntry = zis.getNextEntry();
        }
        zis.closeEntry();
        zis.close();
    }
}