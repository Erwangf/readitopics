package exe;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import  webserver.responses.*;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonObject.Member;

import core.Constantes;
import webserver.pojo.DatasetResources;
import webserver.pojo.DatasetResourcesUtils;

public class ScriptGenerator {
	public static ArrayList<String> arrayFromTxt(String path) throws IOException {
		ArrayList<String> phrases_b = new ArrayList<String>();
		BufferedReader buff = new BufferedReader(new FileReader(path));	
		String line;
		while ((line = buff.readLine()) != null)
		{
			phrases_b.add(line);
		}
		buff.close();
		return phrases_b;
	}
	static int[][] LS(int k,int l,int m){
		//k le nombre d'annotateurs
		//l le nombre de classes
		//m le nombre de n de suites à generer
		int[][] out = new int[k][m];
		for (int i =0;i<k;i++){
			int n =0;
			for (int j =0;j<m;j++){
				out[i][j] = ((i+n)%l);
				n++;
				if (n == l){
					n=0;
				}
			}
		}
		return out;
	}
	static ArrayList<ArrayList<TopListwcoherence>> split_set(TreeSet<TopListwcoherence> sit,int k){
		ArrayList<ArrayList<TopListwcoherence>> out = new ArrayList<ArrayList<TopListwcoherence>>();
		int ec = Math.floorDiv(sit.size(), k);
		for (int i=0;i<k;i++){
			int limb =ec*i;
			int limh =ec*(i+1);
			if (i==k-1){
				limh = sit.size();
			}
			ArrayList<TopListwcoherence> out_temp = new ArrayList<TopListwcoherence>();
			Iterator it = sit.iterator();
			for (int j=0;j<limb;j++){
				it.next();
			}
			for (int j=limb;j<limh;j++){
				TopListwcoherence temp = (TopListwcoherence) it.next();
				out_temp.add(temp);
			}
			out.add(out_temp);
		}
		return out;
	}

	public static void main(String[] args) throws IOException {
		//		"C:\\Users\\antoi\\git\\topiclabeling\\WebServer\\datasets"
		//Initialisaton
		if (args.length == 0)
			throw new IOException("Require at least 1 argument: configuration file");
		//Were are the users
		String users_dir = args[0];
		//Were are the datasets (datasets)
		String datasetsDirectory = args[1];		
		HashMap<String, DatasetResources> webResources = new HashMap<>();
		String[] datasetsFolders = DatasetResourcesUtils.getSubFolders(datasetsDirectory);
		// 2 - build 4 string for each file as expected. If any doesn't exist,
		// rollback launch
		String sep = "/"; // reminder : java NIO will cope with any sep w.r.t.
		// hosting machine runnning the server.

		for (String folder : datasetsFolders) {
			String datasetName = new File(folder).getName();
			//			System.out.println(datasetName);
			//String embFile = datasetsDirectory + sep + folder + sep + "embeddings" + sep + "vectors.txt";
			String labelFile = datasetsDirectory + sep + folder + sep + "labels" + sep + "labels.json";
			String topdocFile = datasetsDirectory + sep + folder + sep + "labels" + sep + "topdocs.json";
			String topdocTopicPairFile = datasetsDirectory + sep + folder + sep + "models" + sep + "topdocs_pairs.json";
			String topicsFile = datasetsDirectory + sep + folder + sep + "models" + sep + "model.json";
			String toptopicsFile = datasetsDirectory + sep + folder + sep + "models" + sep + "theta_toptopics.json";
			//String tsneFile = datasetsDirectory + sep + folder + sep + "t-sne" + sep + "t-sne.json";
			String descFile = datasetsDirectory + sep + folder + sep + "description" + sep + "des.txt";
			String corFile = datasetsDirectory + sep + folder + sep + "analytics" + sep + "cor_doc_based.json";
			String cohFile = datasetsDirectory + sep + folder + sep + "analytics" + sep + "coherence.json";
			// 3 - build a DatasetConfiguration and add it to the Map.
			DatasetResources dr = new DatasetResources(datasetName, labelFile, topdocFile, topicsFile, toptopicsFile,
					descFile, corFile, topdocTopicPairFile,cohFile);
			webResources.put(datasetName, dr);
			System.out.println(datasetName);
		}

		//Debut de la constuction
		//		On construit la listede topic avec la coherence
		TreeSet<TopListwcoherence> top = (TreeSet<TopListwcoherence>) new TopicCoherenceList(webResources).topics;
		//		On parse les users
		ArrayList<String> users = arrayFromTxt(users_dir);
		//Chemin "grades"
		String Grades_dir = DatasetResourcesUtils.getParentFolders(users_dir);
		//Manip heritée où l'on veut seulement les datasets New-US et Sc-art
		ArrayList<String> datasets_auth = new ArrayList<>();
		datasets_auth.add("News-US");
		datasets_auth.add("Sc-art100Topics");	
		TreeSet<TopListwcoherence> top_u= new TreeSet<TopListwcoherence>( top.stream()                // convert list to stream
				.filter(line -> datasets_auth.contains(line.getDataset()))
				.collect(Collectors.toSet()));
		//top_u contient nos topics avec la coherence
		//On le split en trois
		ArrayList<ArrayList<TopListwcoherence>> test = split_set(top_u,3);
		//Voila voila
		int k =0;
		for (String temp_us : users){
			BufferedWriter bw ;
			String[] user_infos = temp_us.split(";");
			k++;
			bw = new BufferedWriter(new FileWriter(Grades_dir+Constantes.separateur+user_infos[0]+"-evaluation_SB.txt"));
			bw.close();
			bw = new BufferedWriter(new FileWriter(Grades_dir+Constantes.separateur+user_infos[0]+"-evaluation_CW.txt"));
			bw.close();
			bw = new BufferedWriter(new FileWriter(Grades_dir+Constantes.separateur+user_infos[0]+"-evaluation_NG.txt"));
			bw.close();
		}
	}
}
