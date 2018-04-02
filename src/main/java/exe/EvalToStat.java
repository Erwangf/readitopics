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
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonObject.Member;
import com.eclipsesource.json.JsonValue;

import core.Constantes;
import webserver.pojo.DatasetResources;
import webserver.pojo.DatasetResourcesUtils;
import webserver.responses.TopListwcoherence;
import webserver.responses.TopicCoherenceList;

import static java.nio.file.StandardCopyOption.*;
//TODO A virer
public class EvalToStat {
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
	public static void main(String[] args) throws IOException {
		String sep = java.io.File.separator;
		String path_grades;
		String path_stats;

		if (args.length>0){
			path_grades = args[0];
			path_stats = args[1];
		}else{
			path_grades = System.getProperty("user.dir")+sep+"Webserver"+sep+"Grades";
			path_stats = System.getProperty("user.dir")+sep+Constantes.RESULT_DIR+sep+"StatEval";
		}

		//On crée le folder pour accueillir les stats
		DateFormat dateFormat = new SimpleDateFormat("dd_MM_yy_HHmm");
		Date date = new Date();
		String path_stats_cur = path_stats+sep+dateFormat.format(date);
		File dir = new File(path_stats_cur);
		//		System.out.println(dir.getAbsolutePath());
		dir.mkdirs();

		//On copie les fichier "patron"
		Files.copy(new File(path_stats+sep+"CW.csv").toPath(), new File(path_stats_cur+sep+"CW.csv").toPath(),REPLACE_EXISTING);
		Files.copy(new File(path_stats+sep+"SB.csv").toPath(), new File(path_stats_cur+sep+"SB.csv").toPath(),REPLACE_EXISTING);
		Files.copy(new File(path_stats+sep+"NG.csv").toPath(), new File(path_stats_cur+sep+"NG.csv").toPath(),REPLACE_EXISTING);
		Files.copy(new File(path_stats+sep+"NG.csv").toPath(), new File(path_stats_cur+sep+"NG-qual.csv").toPath(),REPLACE_EXISTING);


		//Maintenant pour chaque fichier on va mettre dans le fichier commun
		//utilitaires IO
		BufferedWriter bw ;
		BufferedReader br ;
		String line;
		br = new BufferedReader(new FileReader(path_stats_cur+sep+"NG.csv"));
		String[] col_NG = br.readLine().split(";");
		br.close();
		br = new BufferedReader(new FileReader(path_stats_cur+sep+"SB.csv"));
		String[] col_SB = br.readLine().split(";");
		br.close();
		//
		ArrayList<String> users = arrayFromTxt(path_grades+sep+"users.txt");
		for (String temp_us : users){
			String[] user_infos = temp_us.split(";");
			//			System.out.println(user_infos[0]);
			//CW
			br = new BufferedReader(new FileReader(path_grades+sep+user_infos[0]+"-evaluation_cw.txt"));
			bw = new BufferedWriter(new FileWriter(path_stats_cur+sep+"cw.csv",true));
			while ((line = br.readLine()) != null)
			{
				bw.newLine();
				bw.write(user_infos[0]+";"+line);
			}
			bw.close();
			br.close();
			//SB
			br = new BufferedReader(new FileReader(path_grades+sep+user_infos[0]+"-evaluation_sb.txt"));
			bw = new BufferedWriter(new FileWriter(path_stats_cur+sep+"sb.csv",true));
			while ((line = br.readLine()) != null)
			{
				String[] res = line.split(";");			
				String[] out = new String[col_SB.length];
				out[0] = user_infos[0];
				out[1] =res[0];
				out[2] =res[1];
				for(int i = 2;i<res.length;i++){
					String[] label_gr = res[i].split(",");
					String[] labeler = label_gr[0].split("--");	
					for(String lbl : labeler){
						//						System.out.println(lbl);
						boolean ptrouve = true;
						int j = 0;
						while(ptrouve && j<col_SB.length){
							if (lbl.equals(col_SB[j])){
								ptrouve=false;
								out[j]=label_gr[1];
							}
							j++;
						}
					}
				}

				bw.newLine();
				String out_stringed = String.join(";", out);
				bw.write(out_stringed);
			}
			bw.close();
			br.close();
			//NG
			br = new BufferedReader(new FileReader(path_grades+sep+user_infos[0]+"-evaluation_ng.txt"));
			bw = new BufferedWriter(new FileWriter(path_stats_cur+sep+"NG-qual.csv",true));
			BufferedWriter bw2 = new BufferedWriter(new FileWriter(path_stats_cur+sep+"NG.csv",true));
			while ((line = br.readLine()) != null)
			{
				String[] res = line.split(";");			
				String[] out = new String[col_NG.length];
				out[0] = user_infos[0];
				out[1] =res[0];
				out[2] =res[1];
				for(int i = 2;i<res.length;i++){
					String[] label_gr = res[i].split(",");
					String[] labeler = label_gr[0].split("--");	
					for(String lbl : labeler){
						//						System.out.println(lbl);
						boolean ptrouve = true;
						int j = 0;
						while(ptrouve && j<col_NG.length){
							if (lbl.equals(col_NG[j])){
								ptrouve=false;
								out[j]=label_gr[1];

							}
							j++;
						}
					}
				}
				bw.newLine();
				bw2.newLine();
				String out_stringed = String.join(";", out);
				bw.write(out_stringed);
				String out2too = out_stringed.replaceAll("2.a", "2");
				out2too = out2too.replaceAll("2.b", "2");
				bw2.write(out2too);
			}
			bw.close();
			bw2.close();
			br.close();	
		}
		bw = new BufferedWriter(new FileWriter(path_stats_cur+sep+"Quality_Measures.csv",true));
		String datapath = DatasetResourcesUtils.getParentFolders(path_grades);
		ArrayList<String> top =Dirty_topic_coherence(datapath+sep+"datasets"+sep+"News-US"+sep+"analytics"+sep+"coherence.json","News-US",true);
		ArrayList<String> top2 =Dirty_topic_coherence(datapath+sep+"datasets"+sep+"Sc-art100Topics"+sep+"analytics"+sep+"coherence.json","Sc-art100Topics",false);
		top.addAll(top2);
		for (String tl : top){
			bw.write(tl.toString());
			bw.newLine();
		}
		bw.close();
	}
	private static ArrayList<String> Dirty_topic_coherence(String Datasetpath,String datasetname,boolean first) {
		//		TreeSet<TopListwcoherence> topics = new TreeSet<>();
		ArrayList<String> topics = new ArrayList<String>();
		try {
			Reader reader = new InputStreamReader(new FileInputStream(Datasetpath));	
			JsonObject jsonObject = Json.parse(reader).asObject();
			JsonArray topic = jsonObject.get("topics").asArray();
			JsonObject document = topic.get(0).asObject();			
			JsonObject Coh = document.get("qual").asObject();	
			String val;	
			if (first){
				val ="Dataset;Topic";
				for (Member member : Coh) {
					String name = member.getName();
					val = val + ";" + name ;
				}
				topics.add(val);
			}
			for (int i = 0; i < topic.size(); i++) {
				document = topic.get(i).asObject();			
				Coh = document.get("qual").asObject();	
				val = datasetname+";"+i;
				for (Member member : Coh) {
					String name = member.getName();
					JsonValue value = member.getValue();
					val = val + ";" + value ;
				}

				topics.add(val);
				System.out.println(val);
			}
			reader.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
		return topics;
	}

}
