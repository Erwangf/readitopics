package webserver.responses;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonObject.Member;

import webserver.pojo.DatasetResources;

public class TopTopics {

	private Set<TopTopic> topics = null;
	private HashMap<String, TreeMap<String,Set<TopTopic>>> lookup_table = null; 

	public TopTopics() {
		lookup_table = new HashMap<>(); 
	}
	
	public TopTopics(DatasetResources datasetResources, String iddoc) throws IOException {

		Reader reader = new InputStreamReader(new FileInputStream(datasetResources.getTopTopicsFile()));
		topics = new TreeSet<>();

		try {
			JsonObject jsonObject = Json.parse(reader).asObject();
			JsonArray docs= jsonObject.get("docs").asArray();
			
			for (int i=0; i<docs.size(); i++)
			{
				JsonObject doc = docs.get(i).asObject();
				String name = doc.get("docname").asString();
				
				
				if (name.equals(iddoc))
				{
					JsonArray topics_prop = doc.get("topics_prop").asArray();
					for (int j = 0; j < topics_prop.size(); j++) {
						JsonObject list_topics = topics_prop.get(j).asObject();
						int z = Integer.parseInt(list_topics.get("z").asString());
						Double pro = Double.valueOf(list_topics.get("proba").asString());
						TopTopic newtop = new TopTopic(z, pro);
						topics.add(newtop);
					}	
				}
			}	
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			reader.close();
		}
	}
	
	/* initialize all top topics for one dataset */ 
	public void add_lookup(String dataset, DatasetResources datasetResources) throws IOException {

		System.out.println("add look up for " + dataset + " from " + datasetResources.getTopTopicsFile());
		Reader reader = new InputStreamReader(new FileInputStream(datasetResources.getTopTopicsFile()));
		TreeMap<String,Set<TopTopic>> lookup = new TreeMap<String,Set<TopTopic>>();

		try {
			JsonObject jsonObject = Json.parse(reader).asObject();
			JsonArray docs= jsonObject.get("docs").asArray();			
			for (int i=0; i<docs.size(); i++)
			{
				JsonObject doc = docs.get(i).asObject();
				String name = doc.get("docname").asString();
				//Set<TopTopic> topics = new TreeSet<TopTopic>();
				Set<TopTopic> t = new TreeSet<TopTopic>();
				JsonArray topics_prop = doc.get("topics_prop").asArray();
				for (int j = 0; j < topics_prop.size(); j++) {
					JsonObject list_topics = topics_prop.get(j).asObject();
					int z = Integer.parseInt(list_topics.get("z").asString());
					Double pro = Double.valueOf(list_topics.get("proba").asString());
					TopTopic newtop = new TopTopic(z, pro);					
					t.add(newtop);
				}	
				lookup.put(name, t);
			}
			lookup_table.put(dataset, lookup);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			reader.close();
		}
	}
	
	/* get one set of top topics using the lookup table */
	public Set<TopTopic> get_toptopics(String dataset, String iddoc) {
		return lookup_table.get(dataset).get(iddoc);
	}
	
	/* good old-fashioned without the lookup table */
	public Set<TopTopic> get_toptopics() {		
		return topics;
	}

	class TopTopic implements Comparable<TopTopic> {

		int num;
		double proba;

		public TopTopic(int num, double proba) {
			super();
			this.num = num;
			this.proba = proba;
		}

		public int getNum() {
			return num;
		}

		public void setNum(int num) {
			this.num = num;
		}

		public double getProba() {
			return proba;
		}

		public void setProba(double proba) {
			this.proba = proba;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + Integer.hashCode(num);
			long temp;
			temp = Double.doubleToLongBits(proba);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TopTopic other = (TopTopic) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (num != other.num)
					return false;
			if (Double.doubleToLongBits(proba) != Double.doubleToLongBits(other.proba))
				return false;
			return true;
		}

		private TopTopics getOuterType() {
			return TopTopics.this;
		}

		@Override
		public int compareTo(TopTopic o) {
			if (this.equals(o)) {
				return 0;
			}

			if (proba < o.getProba()) {
				return 1;
			} else if (proba > o.getProba()) {
				return -1;
			}
			return new Integer(num).compareTo(o.num);
		}
		
		public String toString() {
			return "z" + num + " ";
		}

	}

}
