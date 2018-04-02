package evaluation;

/** This class contains all the possible measures for a given term. */

public class Measures4Term implements Comparable<Measures4Term>
{
	
	private String feature_name;
	private int feature_length;
	private int feature_tf;
	private int feature_nbdocs;
	private double feature_entropyTF;
	private double feature_entropyNBDocs;
	private double feature_tfxentropy;
	private double feature_nbdocsxentropy;

	/* various ways for comparing measures */
	public static final int TF = 1;
	public static final int TFxEnt = 2;	
	public static final int LogTFxEnt = 3;
	
	private static int comparator = TF;	
	
	public Measures4Term(String s)
	{
		setFeature_name(s);
	}


	public int getFeature_length() {
		return feature_length;
	}


	public void setFeature_length(int feature_length) {
		this.feature_length = feature_length;
	}


	public int getFeature_tf() {
		return feature_tf;
	}


	public void setFeature_tf(int feature_tf) {
		this.feature_tf = feature_tf;
	}


	public int getFeature_nbdocs() {
		return feature_nbdocs;
	}


	public void setFeature_nbdocs(int feature_nbdocs) {
		this.feature_nbdocs = feature_nbdocs;
	}


	public double getFeature_entropyTF() {
		return feature_entropyTF;
	}


	public void setFeature_entropyTF(double feature_entropyTF) {
		this.feature_entropyTF = feature_entropyTF;
	}


	public double getFeature_entropyNBDocs() {
		return feature_entropyNBDocs;
	}


	public void setFeature_entropyNBDocs(double feature_entropyNBDocs) {
		this.feature_entropyNBDocs = feature_entropyNBDocs;
	}


	public double getFeature_tfxentropy() {
		return feature_tfxentropy;
	}


	public void setFeature_tfxentropy(double feature_tfxentropy) {
		this.feature_tfxentropy = feature_tfxentropy;
	}


	public double getFeature_nbdocsxentropy() {
		return feature_nbdocsxentropy;
	}


	public void setFeature_nbdocsxentropy(double feature_nbdocsxentropy) {
		this.feature_nbdocsxentropy = feature_nbdocsxentropy;
	}


	public String getFeature_name() {
		return feature_name;
	}


	public void setFeature_name(String feature_name) {
		this.feature_name = feature_name;
	}
	
	public double getFeature_logtfxentropy() {
		return log2(feature_tf) * feature_entropyTF;
	}

	
	/* Compares using the chosen measure defined with the comparator. */
	
	public int compareTo(Measures4Term m)
	{
		switch (comparator)
		{
		case TF :
			if (this.feature_tf > m.feature_tf)
				return -1;
			else
				if (this.feature_tf < m.feature_tf)
					return 1;
				else return 0;
		case TFxEnt:
			if (this.feature_tfxentropy > m.feature_tfxentropy)
				return -1;
			else
				if (this.feature_tfxentropy < m.feature_tfxentropy)
					return 1;
				else return 0;			
		case LogTFxEnt:
		if (this.getFeature_logtfxentropy() > m.getFeature_logtfxentropy())
			return -1;
		else
			if (this.getFeature_logtfxentropy() < m.getFeature_logtfxentropy())
				return 1;
			else return 0;			
		}	
		return 0;
	} 
	
	private double log2(double x)
	{
		return (Math.log(x) / Math.log(2));
	}
	
	public static void setComparator(int c)
	{
		comparator = c;
	}

}
