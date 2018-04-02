package webserver.responses;

public class TopicCount {
	private Integer topicsCount;

	public TopicCount(Integer c) {
		super();
		this.topicsCount = c;
	}

	public Integer getDatasetCount() {
		return topicsCount;
	}

	public void setDatasetCount(Integer c) {
		this.topicsCount = c;
	}

}
