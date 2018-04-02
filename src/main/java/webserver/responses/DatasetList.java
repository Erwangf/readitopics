package webserver.responses;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class DatasetList {
	private List<String> datasetsKeys;

	public DatasetList(Set<String> set) {
		super();
		this.datasetsKeys = new ArrayList<String>(set);
		Collections.sort(datasetsKeys);
	}

	public List<String> getDatasetsKeys() {
		return datasetsKeys;
	}

	public void setDatasetsKeys(List<String> datasetsKeys) {
		this.datasetsKeys = datasetsKeys;
	}

}
