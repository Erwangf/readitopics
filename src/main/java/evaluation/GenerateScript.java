package evaluation;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import core.Constantes;


public class GenerateScript {
	public static void main(String[] args) throws IOException {
		System.out.println("Generate Script");	
		String ch = "C:\\Users\\antoi\\git\\topiclabeling\\WebServer\\Grades";
		//		if (args.length == 0)
		//			throw new IOException("Require at least 1 argument: configuration file");	
		//		String ch = args[0];
		ArrayList<String> candidats = genererVect();
		Scanner ent = new Scanner(System.in);
		System.out.println("User name>");
		String user = ent.nextLine();
		System.out.println("number of tests>");
		int nb_tt = Integer.parseInt(ent.nextLine());
		BufferedWriter bw = new BufferedWriter(new FileWriter (ch+Constantes.separateur+user+"-evaluation.txt"));
		bw.write("0");
		bw.newLine();
		for (int k = 0;k<nb_tt;k++){
			int taille_e_c = candidats.size();
			Random rand = new Random();
			int index = rand.nextInt(taille_e_c);
			bw.write(candidats.get(index));
			bw.newLine();
			candidats.remove(index);
		}
		bw.close();
		System.out.print("done");
	}
	private static ArrayList<String> genererVect() {
		//		You can custom here the candidates
		ArrayList<String> out = new ArrayList<String>();
		ArrayList<String> dstn = new ArrayList<String>();
		dstn.add("ASOIAF");
		dstn.add("HP");
		dstn.add("News-US");
		dstn.add("Sc-art100Topics");
		for (String s : dstn){
			for (int i=0;i<100;i++){
				out.add(s+";"+i);
			}
		}
		for (int i=0;i<5;i++){
			out.add("Sc-art5Topics"+";"+i);
		}
		return out;
	}
}
