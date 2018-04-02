package webserver.pojo;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

/**
 * Information about a given dataset as used by the webserver.
 * 
 * @author cgravier
 *
 */
public class DatasetResources {

	private String datasetName;
	private String labelsFile;
	private String topdocFile;
	private String topdoctopicpairFile;
	private String topicsFile;
	private String toptopicsFile;
	private String descFile;
	private String corFile;
	private String cohFile;

	Map<String, ArrayList<Double>> embeddings = new HashMap<>();

	private final static Logger logger = LoggerFactory.getLogger(DatasetResources.class);

	public DatasetResources(String datasetName, String labelsFile, String topdocFile,
			String topicsFile, String toptopicsFile, String descFile,String corFile, String topdocspairs_File,String cohFile) {
		super();
		this.datasetName = datasetName;
		this.labelsFile = labelsFile;
		this.topdocFile = topdocFile;
		this.topdoctopicpairFile = topdocspairs_File;
		this.topicsFile = topicsFile;
		this.toptopicsFile = toptopicsFile;
		this.embeddings = embeddings;
		this.descFile = descFile;
		this.corFile=corFile;
		this.cohFile=cohFile;

	}

	public String getCohFile() {
		return cohFile;
	}

	public void setCohFile(String cohFile) {
		this.cohFile = cohFile;
	}

	public String getDescFile() {
		return descFile;
	}

	public void setDescFile(String descFile) {
		this.descFile = descFile;
	}

	public String getLabelsFile() {
		return labelsFile;
	}

	public void setLabelsFile(String labelsFile) {
		this.labelsFile = labelsFile;
	}

	public String getTopdocFile() {
		return topdocFile;
	}

	public void setTopdocFile(String topdocFile) {
		this.topdocFile = topdocFile;
	}

	public String getTopicsFile() {
		return topicsFile;
	}

	public void setTopicsFile(String topicsFile) {
		this.topicsFile = topicsFile;
	}

	public String getTopTopicsFile() {
		return toptopicsFile;
	}

	public void setTopTopicsFile(String toptopicsFile) {
		this.toptopicsFile = toptopicsFile;
	}	
	
	public String getTopdocTopicPairFile() {
		return topdoctopicpairFile;
	}

	public void setTopdocTopicPairFile(String t) {
		this.topdoctopicpairFile = t;
	}		

	public String getDatasetName() {
		return datasetName;
	}

	public void setDatasetName(String datasetName) {
		this.datasetName = datasetName;
	}

	public Map<String, ArrayList<Double>> getEmbeddings() {
		return embeddings;
	}

	public void setEmbeddings(Map<String, ArrayList<Double>> embeddings) {
		this.embeddings = embeddings;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((datasetName == null) ? 0 : datasetName.hashCode());
		result = prime * result + ((labelsFile == null) ? 0 : labelsFile.hashCode());
		result = prime * result + ((topdocFile == null) ? 0 : topdocFile.hashCode());
		result = prime * result + ((topicsFile == null) ? 0 : topicsFile.hashCode());
		result = prime * result + ((toptopicsFile == null) ? 0 : toptopicsFile.hashCode());
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
		DatasetResources other = (DatasetResources) obj;
		if (datasetName == null) {
			if (other.datasetName != null)
				return false;
		} else if (!datasetName.equals(other.datasetName))
			return false;
		if (labelsFile == null) {
			if (other.labelsFile != null)
				return false;
		} else if (!labelsFile.equals(other.labelsFile))
			return false;
		if (topdocFile == null) {
			if (other.topdocFile != null)
				return false;
		} else if (!topdocFile.equals(other.topdocFile))
			return false;
		if (topicsFile == null) {
			if (other.topicsFile != null)
				return false;
		} else if (!topicsFile.equals(other.topicsFile))
			return false;
		if (toptopicsFile == null) {
			if (other.toptopicsFile != null)
				return false;
		} else if (!toptopicsFile.equals(other.toptopicsFile))
			return false;		
		return true;
	}

	@Override
	public String toString() {
		return "DatasetResources [labelsFile=" + labelsFile + ", topdocFile="
		+ topdocFile + ", topicsFile=" + topicsFile + ", toptopicsFile=" + toptopicsFile + "]";
	}

	public Integer getTopicCount() throws FileNotFoundException {
		Reader reader = new InputStreamReader(new FileInputStream(this.topicsFile));
		JsonArray topics = null;
		try {
			JsonObject jsonObject = Json.parse(reader).asObject();
			topics = jsonObject.get("topics").asArray();
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
		return new Integer(topics.size());
	}

	public String getCorFile() {
		return corFile;
	}

	public void setCorFile(String corFile) {
		this.corFile = corFile;
	}

}
