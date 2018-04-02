package io;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import core.ForIndexing;

public class ExportVocab
{
	
	private String filename;
		
	public ExportVocab(String filename) throws IOException
	{
		this.filename = filename;
	}
		
	public void export(ArrayList<ForIndexing> list) throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
		for (ForIndexing f : list)
		{
			writer.write(f.getTerm()+"\n");				
		}
        writer.close();
	}
	

}
