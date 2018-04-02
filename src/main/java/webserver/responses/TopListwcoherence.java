package webserver.responses;

public class TopListwcoherence implements Comparable<TopListwcoherence> {
	String dataset;
	int index;
	double NPMI;
	double UMASS;
	double UCI;
	public double getUMASS() {
		return UMASS;
	}


	public void setUMASS(double uMASS) {
		UMASS = uMASS;
	}


	public double getUCI() {
		return UCI;
	}


	public void setUCI(double uCI) {
		UCI = uCI;
	}

	double DBT;

	public TopListwcoherence(String dataset, int inde, double NPMI,double DBT,double UMASS, double UCI) {
		this.dataset = dataset;
		this.index = inde;
		this.NPMI = NPMI;
		this.DBT = DBT;
		this.UMASS = UMASS;
		this.UCI = UCI;
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

		if (NPMI < o.getNPMI()) {
			return 1;
		} else if (NPMI > o.getNPMI()) {
			return -1;
		}else{
			if (DBT < o.getDBT()) {
				return 1;
			} else if(DBT > o.getDBT()) {
				return -1;
			}	
		}
		double r = Math.random();
		if (r<0.5){
			return 1;
		}
		return -1;
	}
}