package labeling;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import core.Constantes;
import io.LoadDataset;
import opennlp.maxent.Main;
import topicmodeling.NoModel;

public class GetBiot {
	public String mot;
	public double score;
	public String type;

	public GetBiot(String mot, double score, String type) {
		this.mot = mot;
		this.score = score;
		this.type = type;

	}
	public static ArrayList<GetBiot> Lid_read(String type,String dir) {
		// LIDF ou TF ou C-value??? a mettre en parametre
		// public static void main(String[] args) throws IOException{
		ArrayList<GetBiot> mots = new ArrayList<GetBiot>();
		if(type!="LIDF_value" && type!="TF" && type!="L_value" && type!="C_value"){
			System.out.println("unknown score for termhood");
		}
		else{
			ArrayList<String> phrases_b = new ArrayList<String>();
			String biotex_input_dir = dir + Constantes.separateur + type
					+ Constantes.separateur + "ALL_gram.txt";
			BufferedReader buff;

			try {
				buff = new BufferedReader(new FileReader(biotex_input_dir));
				String line;
				while ((line = buff.readLine()) != null) {
					phrases_b.add(line);
				}
				buff.close();
				for (String s : phrases_b) {
					String[] s2 = s.split(";");
					// selon TF ou LIDF pas les même indices!!!!!!!!!
					// A pathiser
					//ici on a seulement le cas LIDF
					GetBiot temp = new GetBiot(s2[0], Double.parseDouble(s2[2]),type);
//					System.out.println(temp.mot);
					mots.add(temp);
				}
			} catch (IOException e2) {
				System.out.println("can't reach the Biotex file, impossible to build c-order labels");
			}
		}
		return mots;
	}
	public static ArrayList<GetBiot> Lid_read(String type) {
		// LIDF ou TF ou C-value??? a mettre en parametre
		// public static void main(String[] args) throws IOException{
		ArrayList<GetBiot> mots = new ArrayList<GetBiot>();
		if(type!="LIDF_value" && type!="TF"){
			System.out.println("unknown score for termhood");
		}
		else{
			ArrayList<String> phrases_b = new ArrayList<String>();
			String biotex_input_dir = LoadDataset.getPath() + Constantes.separateur + Constantes.RESULT_DIR + Constantes.separateur
					+ LoadDataset.getDataName() + Constantes.separateur + "biotex" + Constantes.separateur + type
					+ Constantes.separateur + "ALL_gram.txt";
			BufferedReader buff;

			try {
				buff = new BufferedReader(new FileReader(biotex_input_dir));
				String line;
				while ((line = buff.readLine()) != null) {
					phrases_b.add(line);
				}
				buff.close();
				for (String s : phrases_b) {
					String[] s2 = s.split(";");
					// selon TF ou LIDF pas les même indices!!!!!!!!!
					// A pathiser
					//ici on a seulement le cas LIDF
					GetBiot temp = new GetBiot(s2[0], Double.parseDouble(s2[2]),type);
//					System.out.println(temp.mot);
					mots.add(temp);
				}
			} catch (IOException e2) {
				System.out.println("can't reach the Biotex file, impossible to build c-order labels");
			}
		}
		return mots;
	}

}
