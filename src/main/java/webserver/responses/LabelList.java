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

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonObject.Member;

import webserver.pojo.DatasetResources;

public class LabelList {

	Map<String, List<LabelTopic>> labels = null;

	public LabelList(DatasetResources datasetResources, int topicid, int count) throws IOException {
		Reader reader = new InputStreamReader(new FileInputStream(datasetResources.getLabelsFile()));
		labels = new HashMap<>();

		try {
			JsonObject jsonObject = Json.parse(reader).asObject();
			JsonArray topics = jsonObject.get("topics").asArray();

			JsonObject topic = topics.get(topicid).asObject();

			Iterator<Member> i = topic.iterator();
			while (i.hasNext()) {

				Member obj = (Member) i.next();
				String labelingStrategyName = obj.getName();
				List<LabelTopic> theseLabels = new ArrayList<>();

				if (!labelingStrategyName.equals("topicid")) {

					JsonArray labelingStrategyLabels = topic.get(labelingStrategyName).asArray();
					int nb = Math.min(labelingStrategyLabels.size(), count);
					for (int k = 0; k < nb; k++) {
						JsonObject labelObj = labelingStrategyLabels.get(k).asObject();
						String labelValue = labelObj.get("label").asString();
						Double labelScore = Double.parseDouble(labelObj.get("score").asString());
						LabelTopic l = new LabelTopic(labelValue, labelScore);
						theseLabels.add(l);
					}
					labels.put(labelingStrategyName, theseLabels);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			reader.close();
		}
	}

	class LabelTopic {
		String label;
		Double score;

		public LabelTopic(String label, Double score) {
			super();
			this.label = label;
			this.score = score;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public Double getScore() {
			return score;
		}

		public void setScore(Double score) {
			this.score = score;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((label == null) ? 0 : label.hashCode());
			result = prime * result + ((score == null) ? 0 : score.hashCode());
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
			LabelTopic other = (LabelTopic) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (label == null) {
				if (other.label != null)
					return false;
			} else if (!label.equals(other.label))
				return false;
			if (score == null) {
				if (other.score != null)
					return false;
			} else if (!score.equals(other.score))
				return false;
			return true;
		}

		private LabelList getOuterType() {
			return LabelList.this;
		}

	}

}
