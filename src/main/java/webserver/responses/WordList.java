package webserver.responses;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

import webserver.pojo.DatasetResources;

public class WordList {

	private List<Word> wordsList = null;

	public WordList(DatasetResources datasetResources, int topicid, int count) throws IOException {
		wordsList = new ArrayList<>();
		Reader reader = new InputStreamReader(new FileInputStream(datasetResources.getTopicsFile()));
		try {
			JsonObject jsonObject = Json.parse(reader).asObject();
			JsonArray topics = jsonObject.get("topics").asArray();
			JsonObject topic = topics.get(topicid).asObject();
			JsonArray words = topic.get("tokens").asArray();
			int nb = Math.min(words.size(), count);
			for (int i = 0; i < nb; i++) {
				JsonObject word = words.get(i).asObject();
				String w = word.get("mot").asString();
				String p = word.get("proba").asString();
				wordsList.add(new Word(w, Double.parseDouble(p)));
			}
			
		} finally {
			reader.close();
		}
	}

	class Word {
		String mot;
		double proba;

		public Word(String mot, double proba) {
			super();
			this.mot = mot;
			this.proba = proba;
		}

		public String getMot() {
			return mot;
		}

		public void setMot(String mot) {
			this.mot = mot;
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
			result = prime * result + ((mot == null) ? 0 : mot.hashCode());
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
			Word other = (Word) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (mot == null) {
				if (other.mot != null)
					return false;
			} else if (!mot.equals(other.mot))
				return false;
			if (Double.doubleToLongBits(proba) != Double.doubleToLongBits(other.proba))
				return false;
			return true;
		}

		private WordList getOuterType() {
			return WordList.this;
		}

	}

}
