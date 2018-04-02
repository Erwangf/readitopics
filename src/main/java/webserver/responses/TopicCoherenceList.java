package webserver.responses;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonObject.Member;

import core.Constantes;
import webserver.pojo.DatasetResources;
import webserver.pojo.DatasetResourcesUtils;
import webserver.responses.LabelList.LabelTopic;

public class TopicCoherenceList {

	public TreeSet<TopListwcoherence> topics = null;


	public TopicCoherenceList(Map<String, DatasetResources> webResources) throws IOException {
		topics = new TreeSet<>();
		for (Map.Entry mapentry : webResources.entrySet()) {
			DatasetResources dat = (DatasetResources)mapentry.getValue();
			String d = (String) mapentry.getKey() ;
			String dir = DatasetResourcesUtils.getParentFolders(dat.getCorFile())+ Constantes.separateur+"coherence.json";
			Reader reader = new InputStreamReader(new FileInputStream(dir));	
			try {
				JsonObject jsonObject = Json.parse(reader).asObject();
				JsonArray topic = jsonObject.get("topics").asArray();
				for (int i = 0; i < topic.size(); i++) {
					JsonObject document = topic.get(i).asObject();			
					JsonObject Coh = document.get("qual").asObject();
					Double npmii = Coh.get("npmi").asDouble();
					Double umass = Coh.get("umass").asDouble();
					Double uci = Coh.get("npmi").asDouble();
					Double dbto =Coh.get("dist_background").asDouble();
					TopListwcoherence newtop = new TopListwcoherence(d, i, npmii,dbto,umass,uci);
					//					System.out.println(newtop.dataset + newtop.index +newtop.DBT + newtop.NPMI);
					topics.add(newtop);
				}
			}catch (Exception e) {
				e.printStackTrace();
			} finally {
				reader.close();
			}
		}
	}

	public TopListwcoherence coher = null;
	public TopicCoherenceList(DatasetResources datasetResources, String dataset,int topicid) throws IOException {
		Reader reader = new InputStreamReader(new FileInputStream(datasetResources.getCohFile()));
		try {
			JsonObject jsonObject = Json.parse(reader).asObject();
			JsonArray topic = jsonObject.get("topics").asArray();
			//			for (int i = 0; i < topic.size(); i++) {
			//				JsonObject document = topic.get(i).asObject();	
			JsonObject document = topic.get(topicid).asObject();
			JsonObject Coh = document.get("qual").asObject();
			Double npmii =Coh.get("npmi").asDouble();
			Double umass =Coh.get("umass").asDouble();
			Double uci =Coh.get("npmi").asDouble();
			Double dbto =Coh.get("dist_background").asDouble();
			coher = new TopListwcoherence(dataset, topicid, npmii,dbto,umass,uci);
			//			}
		}catch (Exception e) {
			e.printStackTrace();
		} finally {
			reader.close();
		}
	}

}

//	public static void main(String[] args) throws IOException {
//		Map<String, DatasetResources> webResources = new HashMap<>();
//		String datasetsDirectory = "C:\\Users\\antoi\\git\\topiclabeling\\WebServer\\datasets";
//		String[] datasetsFolders = DatasetResourcesUtils.getSubFolders(datasetsDirectory);
//		// 2 - build 4 string for each file as expected. If any doesn't exist,
//		// rollback launch
//		String sep = "/"; // reminder : java NIO will cope with any sep w.r.t.
//		// hosting machine runnning the server.
//
//		for (String folder : datasetsFolders) {
//			String datasetName = new File(folder).getName();
//			System.out.println(datasetName);
//			String embFile = datasetsDirectory + sep + folder + sep + "embeddings" + sep + "vectors.txt";
//			String labelFile = datasetsDirectory + sep + folder + sep + "labels" + sep + "labels.json";
//			String topdocFile = datasetsDirectory + sep + folder + sep + "labels" + sep + "topdocs.json";
//			String topdocTopicPairFile = datasetsDirectory + sep + folder + sep + "models" + sep + "topdocs_pairs.json";
//			String topicsFile = datasetsDirectory + sep + folder + sep + "models" + sep + "model.json";
//			String toptopicsFile = datasetsDirectory + sep + folder + sep + "models" + sep + "theta_toptopics.json";
//			String tsneFile = datasetsDirectory + sep + folder + sep + "t-sne" + sep + "t-sne.json";
//			String descFile = datasetsDirectory + sep + folder + sep + "description" + sep + "des.txt";
//			String corFile = datasetsDirectory + sep + folder + sep + "analytics" + sep + "cor_doc_based.json";
//			// 3 - build a DatasetConfiguration and add it to the Map.
//			DatasetResources dr = new DatasetResources(datasetName, embFile, labelFile, topdocFile, topicsFile, toptopicsFile,
//					tsneFile, descFile, corFile, topdocTopicPairFile);
//			webResources.put(datasetName, dr);
//		}
//		Set<TopListwcoherence> top = null;
//		top = new TopicCoherence(webResources).topics;
//		for (TopListwcoherence t : top){
//			System.out.println(t.dataset +" "+ t.index +" "+t.DBT +" "+ t.NPMI);
//		}
//	}
