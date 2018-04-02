package io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import core.Constantes;

public abstract class Export {

	// do we print the associated probability?
	protected boolean print_prob;
		
	private static String path;
	
	public Export()
	{
		if (path.equals(" "))
			path = LoadDataset.getPath() + Constantes.separateur + "export";
	}
	
	public void setPrintProba(boolean b)
	{
		this.print_prob = b;
	}
	
	public static void setPath(String p)
	{
		path = p;
	}
	
	protected void export_to_textfile(String filename, String text)
	{
	   new File(path).mkdirs();
       BufferedWriter writer = null;
        try {
            File myfile = new File(path + Constantes.separateur + filename);
            writer = new BufferedWriter(new FileWriter(myfile));
            writer.write(text);
        } catch (Exception e) {
            //e.printStackTrace();
        	System.out.println("Impossible to write to the file " + filename);
        } finally {
            try {
                writer.close();
            } catch (Exception e) {
            }
        }
	}
	
	public void export(String filename, String filetype)
	{
		String txt_export = "";
		switch(filetype)
		{
			case "csv":
				txt_export = export_csv(filename);
				break;
			case "json":
				txt_export = export_json(filename);
				break;
		}	
		if (!txt_export.isEmpty())
			export_to_textfile(filename, txt_export);
	}
	
	protected String export_csv(String filename)
	{
		return "not available";
	}
	
	protected String export_json(String filename)
	{
		return "not available";
	}
	
	
}
