package io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

public class EvaluationMeasures {

	private static HashMap<String,double[]> evaluations;
	
	public static HashMap<String,double[]> load(String filename) throws Exception
	{
		FileInputStream r = new FileInputStream(filename);
		ObjectInputStream o = new ObjectInputStream(r);
		Object lu = o.readObject();
		evaluations = (HashMap<String,double[]>)lu;
		o.close();
		r.close();
		return evaluations;
	}

	public static HashMap<String,double[]> save(String filename, HashMap<String,double[]> evaluations)
	{
		new File(filename).getParentFile().mkdirs();
		try {
			FileOutputStream w = new FileOutputStream(filename);
			ObjectOutputStream o = new ObjectOutputStream(w);
			o.writeObject(evaluations);
			o.close();
			w.close();
		}
		catch (Exception e)
		{
			System.out.println("ERROR input-output");
		}
		return evaluations;
	}

}
