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

public class TopicSize {

	private double size; 
	
	public TopicSize(DatasetResources datasetResources, int topicid) throws IOException {				
		Reader reader = new InputStreamReader(new FileInputStream(datasetResources.getTopicsFile()));
		try {
			JsonObject jsonObject = Json.parse(reader).asObject();
			JsonArray topics = jsonObject.get("topics").asArray();
			JsonObject topic = topics.get(topicid).asObject();
			size = topic.getDouble("size", -1);
		} finally {
			reader.close();
		}
	}

	public double getsize() {
		return size;
	}
	
}
