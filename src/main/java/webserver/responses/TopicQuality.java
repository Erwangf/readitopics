package webserver.responses;

public class TopicQuality implements Comparable<TopListwcoherence> {
	String dataset;
	int index;
	double NPMI;
	double DBT;

	public TopicQuality(String dataset, int inde, double NPMI,double DBT) {
		this.dataset = dataset;
		this.index = inde;
		this.NPMI = NPMI;
		this.DBT = DBT;
	}


	/*@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((author == null) ? 0 : author.hashCode());
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			long temp;
			temp = Double.doubleToLongBits(proba);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			result = prime * result + ((text == null) ? 0 : text.hashCode());
			result = prime * result + ((time == null) ? 0 : time.hashCode());
			return result;
		}*/

	@Override
	public boolean equals(Object obj) {
		TopListwcoherence obcasted = (TopListwcoherence)obj;
		if (this.dataset ==  obcasted.dataset && this.index ==  obcasted.index)
			return true;
		else
			return false;
	}

	public String getDataset() {
		return dataset;
	}


	public void setDataset(String dataset) {
		this.dataset = dataset;
	}


	public int getIndex() {
		return index;
	}


	public void setIndex(int index) {
		this.index = index;
	}


	public double getNPMI() {
		return NPMI;
	}


	public void setNPMI(double nPMI) {
		NPMI = nPMI;
	}


	public double getDBT() {
		return DBT;
	}


	public void setDBT(double dBT) {
		DBT = dBT;
	}
	@Override
	public String toString() {
		return 	getDataset()+";"+getIndex()+";"+getNPMI()+";"+getDBT();
	}

	@Override
	public int compareTo(TopListwcoherence o) {
		if (this.equals(o)) {
			return 0;
		}
		int c =dataset.compareTo(o.getDataset());
		if(c==0){
			return index - o.getIndex();
		}else{
			return c;
		}
	}
}