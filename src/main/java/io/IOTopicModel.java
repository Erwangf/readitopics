package io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.Alphabet;
import core.Constantes;

public class IOTopicModel {

	private String path;
	// private boolean modelname = false;
	private static int nb = 0;

	public IOTopicModel(String path) {
		/*
		 * if (path.endsWith("model")) modelname = true;
		 */
		this.path = path;
	}

	/* export both the MALLET alphabet and the LDA model(s) into a folder */

	public void export(Alphabet dataAlphabet, ParallelTopicModel model) {
		nb++;
		// first, export the common alphabet (meaning, the mapping ID <-> word
		// string)
		try {
			FileOutputStream w = new FileOutputStream(
					path + Constantes.separateur + "vocab_" + model.numTopics + ".alpha");
			ObjectOutputStream o = new ObjectOutputStream(w);
			o.writeObject(dataAlphabet);
			o.close();
			w.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// next, export the models
		try {
			FileOutputStream w = new FileOutputStream(
					path + Constantes.separateur + "lda_" + model.numTopics + "_" + nb + ".model");
			ObjectOutputStream o = new ObjectOutputStream(w);
			o.writeObject(model);
			o.close();
			w.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* import the MALLET alphabet, common to all the estimated models */

	public Alphabet importAlphabet(ParallelTopicModel model) throws IOException, ClassNotFoundException {
		String filename = path + Constantes.separateur + "vocab_" + model.numTopics + ".alpha";
		FileInputStream r = new FileInputStream(filename);
		ObjectInputStream o = new ObjectInputStream(r);
		Object lu = o.readObject();
		o.close();
		r.close();
		return (Alphabet) lu;
	}

	/* import all the LDA models contained in the folder */

	public ArrayList<ParallelTopicModel> importTM() throws IOException, FileNotFoundException, ClassNotFoundException {
		ArrayList<String> names = new ArrayList<>();
		ArrayList<ParallelTopicModel> col = new ArrayList<>();
		if (Files.isRegularFile(Paths.get(path)) && path.endsWith("model"))
			names.add(path);
		else
			Files.walk(Paths.get(path)).forEach(filePath -> {
				if (Files.isRegularFile(filePath) && filePath.toString().endsWith("model"))
					names.add(filePath.toString());
			});
		for (String f : names) {
			FileInputStream r = new FileInputStream(f);
			ObjectInputStream o = new ObjectInputStream(r);
			Object lu = o.readObject();
			col.add((ParallelTopicModel) lu);
			o.close();
			r.close();
		}
		return col;
	}

	/* export the pairwise correlation between topics */

	public void export_correlation(String path, String name, double[][] correlation) {
		new File(path).mkdirs();
		try {
			FileOutputStream w = new FileOutputStream(path + Constantes.separateur + name + ".cor");
			ObjectOutputStream o = new ObjectOutputStream(w);
			o.writeObject(correlation);			
			o.close();
			w.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* import the pairwise correlation between topics */

	public double[][] import_correlation(String path, String name) throws IOException, ClassNotFoundException {
		ArrayList<String> names = new ArrayList<>();
		double[][] col = null;
		if (Files.isRegularFile(Paths.get(path)) && path.endsWith(name + ".cor"))
			names.add(path);
		else
			Files.walk(Paths.get(path)).forEach(filePath -> {
				if (Files.isRegularFile(filePath) && filePath.toString().endsWith(name + ".cor"))
					names.add(filePath.toString());
			});
		for (String f : names) {
			FileInputStream r = new FileInputStream(f);
			ObjectInputStream o = new ObjectInputStream(r);
			Object lu = o.readObject();
			col = (double[][]) lu;
			o.close();
			r.close();
		}
		if (col == null)
			throw new ClassNotFoundException();
		return col;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public static int getNb() {
		return nb;
	}

	public static void setNb(int nb) {
		IOTopicModel.nb = nb;
	}

}
