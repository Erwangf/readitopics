package core;

import java.io.Serializable;

public class TermValuePair implements Comparable<TermValuePair>, Serializable
{
	
	private String term;
	private double value;
	
	public TermValuePair(String s, double v)
	{
		term = s;
		value = v;
	}

	public int compareTo(TermValuePair o)
	{
		if (o.value < this.value) return -1;
		else
			if (o.value > this.value) return 1;
		return term.compareTo(o.term);
	}

	public String getTerm()
	{
		return term;
	}
	
	public double getValue()
	{
		return value;
	}

}
