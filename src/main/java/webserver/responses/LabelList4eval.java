package webserver.responses;

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

import webserver.pojo.DatasetResources;
//Désolé tu peux me joindre pour plus dinformation au 01 86 400 400
public class LabelList4eval{

	ArrayList<LabelTopic> labels = null;

	public LabelList4eval(DatasetResources datasetResources, int topicid, int count) throws IOException {
		Reader reader = new InputStreamReader(new FileInputStream(datasetResources.getLabelsFile()));
		labels = new ArrayList<LabelTopic>();

		try {
			JsonObject jsonObject = Json.parse(reader).asObject();
			JsonArray topics = jsonObject.get("topics").asArray();

			JsonObject topic = topics.get(topicid).asObject();

			Iterator<Member> i = topic.iterator();
			while (i.hasNext()) {

				Member obj = (Member) i.next();
				String labelingStrategyName = obj.getName();
				if (!labelingStrategyName.equals("topicid")) {

					JsonArray labelingStrategyLabels = topic.get(labelingStrategyName).asArray();
					int nb = Math.min(labelingStrategyLabels.size(), count);
					for (int k = 0; k < nb; k++) {
						JsonObject labelObj = labelingStrategyLabels.get(k).asObject();
						String labelValue = labelObj.get("label").asString();
						labels.add(new LabelTopic(labelingStrategyName+"_"+k,labelValue));
					}				
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			reader.close();
		}

		clean4d();
	}
	private void clean4d() {
		ArrayList<LabelTopic> without_doublon = new ArrayList<LabelTopic>();
		for (LabelTopic t : this.labels){
			String labelstoWrite =t.labeler;
			boolean alreadydoubled = false;
			for (LabelTopic tocomp : without_doublon){
				if (tocomp.label.equals(t.label)){
					alreadydoubled = true;
					break;
				}
			}
			if(!alreadydoubled){
				for (int j=this.labels.indexOf(t)+1;j<this.labels.size();j++){
					LabelTopic tprime =  this.labels.get(j);
					if(tprime.getLabel().equals(t.label)){
						labelstoWrite +="--"+tprime.getLabeler();
					}	
				}
				without_doublon.add(new LabelTopic(labelstoWrite,t.label));
			}
		}
		labels.clear();
		labels.addAll(without_doublon);
	}
	class LabelTopic {
		String labeler;
		String label;

		public LabelTopic(String labeler,String label) {
			this.labeler = labeler;
			this.label = label;
		}
		public String getLabeler() {
			return labeler;
		}

		public void setLabeler(String labeler) {
			this.labeler = labeler;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((labeler == null) ? 0 : labeler.hashCode());
			result = prime * result + ((label == null) ? 0 : label.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			LabelTopic other = (LabelTopic) obj;
			if (label == null) {
				if (other.label != null)
					return false;
			} else if (!label.equals(other.label))
				return false;
			return true;
		}

		private LabelList4eval getOuterType() {
			return LabelList4eval.this;
		}

	}

}
