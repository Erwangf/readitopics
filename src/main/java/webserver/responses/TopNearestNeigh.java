package webserver.responses;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Set;
import java.util.TreeSet;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

import opennlp.maxent.Main;
import webserver.pojo.DatasetResources;

public class TopNearestNeigh {

	Set<Pair>topics_cor;

	public TopNearestNeigh(DatasetResources datasetResources, int idtop) throws IOException {
//	public TopNearestNeigh(String datasetResources, int idtop) throws IOException {
		topics_cor = new TreeSet<Pair>();
		Reader reader = new InputStreamReader(new FileInputStream(datasetResources.getCorFile()));
//		Reader reader = new InputStreamReader(new FileInputStream(datasetResources));
		try {
			JsonObject jsonObject = Json.parse(reader).asObject();
			JsonArray topic= jsonObject.get("topics").asArray();
			JsonObject topcor = topic.get(idtop).asObject();
			JsonArray val= topcor.get("doc_cor").asArray();
			for (int j = 0; j < val.size(); j++) {
				//System.out.println(val.get(j).asDouble());
				topics_cor.add(new Pair(j,val.get(j).asDouble()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			reader.close();
		}
	}
	private void show() {
		for (Pair i :topics_cor ){
			System.out.println(i.index + " : "+ i.value);
		}
	}
	public class Pair implements Comparable<Pair> {
		public final int index;
		public final double value;

		public Pair(int index, double value) {
			this.index = index;
			this.value = value;
		}

		@Override
		public int compareTo(Pair other) {
			return Double.compare(other.value,this.value);
		}
	}
//public static void main(String[] args) throws IOException {
//	TopNearestNeigh t = new TopNearestNeigh("C:/Users/antoi/git/topiclabeling/WebServer/datasets/dataconf/analytics/cor_doc_based.json",0);
//	t.show();
//}
}
