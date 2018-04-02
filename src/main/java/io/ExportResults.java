package io;

import java.io.File;
import java.io.IOException;

import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.NumberFormat;
import jxl.write.NumberFormats;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class ExportResults
{
	
	private String filename;
	
	private WritableWorkbook copy;
	private WritableSheet sheet; // the current sheet to write on
	
	private NumberFormat fivedps; 
	private WritableCellFormat fivedpsFormat; 
	private WritableCellFormat percentFormat;
	private WritableFont arial12font;
	private WritableCellFormat arial12format;
	
	private int current_row;
	private int current_col;
	
	public ExportResults(String filename, String algo) throws IOException, RowsExceededException, WriteException
	{
		this.filename = filename;
		fivedps = new NumberFormat("#.#####");
		fivedpsFormat = new WritableCellFormat(fivedps);
		percentFormat = new WritableCellFormat (NumberFormats.PERCENT_FLOAT);
		WritableWorkbook workbook = Workbook.createWorkbook(new File(this.filename));
		//WritableSheet sheet = workbook.createSheet(algo, 0);
		sheet = workbook.createSheet(algo, 0);
		arial12font = new WritableFont(WritableFont.ARIAL, 12, WritableFont.BOLD, true);  
		arial12format = new WritableCellFormat (arial12font);
		current_row = 0;
		current_col = 0;
		newTitleLabel("voc_type");			
		newTitleLabel("p1-gram");			
		newTitleLabel("p2-gram");
		newTitleLabel("p3-gram");
		newTitleLabel("p4-gram");
		newTitleLabel("v1-gram");			
		newTitleLabel("v2-gram");
		newTitleLabel("v3-gram");
		newTitleLabel("v4-gram");		
		newTitleLabel("voc_size");
		newTitleLabel("skip1-gram");
		newTitleLabel("skip2-gram");
		newTitleLabel("skip3-gram");
		newTitleLabel("skip4-gram");
		newTitleLabel("alpha");
		newTitleLabel("beta");
		newTitleLabel("nb_runs");
		newTitleLabel("function(m)");
		newTitleLabel("function(s)");
		newTitleLabel("purity(m)");
		newTitleLabel("purity(s)");
		newTitleLabel("ari(m)");
		newTitleLabel("ari(s)");
		newTitleLabel("interTF10(m)");
		newTitleLabel("interTF10(s)");
		newTitleLabel("interTF30(m)");
		newTitleLabel("interTF30(s)");
		newTitleLabel("interLogTFxEnt10(m)");
		newTitleLabel("interLogTFxEnt10(s)");
		newTitleLabel("interLogTFxEnt30(m)");
		newTitleLabel("interLogTFxEnt30(s)");
		newTitleLabel("function(MAX)");
		newTitleLabel("purity(MAX)");
		newTitleLabel("ari(MAX)");
		newTitleLabel("interTF10(MAX)");
		newTitleLabel("interTF30(MAX)");
		newTitleLabel("interLogTFxEnt10(MAX)");
		newTitleLabel("interLogTFxEnt30(MAX)");
		newTitleLabel("time");
		workbook.write(); 
		workbook.close();
		nextRow();
	}
		
	public void openStream() throws IOException, RowsExceededException, BiffException
	{
		Workbook workbook = Workbook.getWorkbook(new File(filename));
		copy = Workbook.createWorkbook(new File(filename+"_tmp.xls"), workbook);
		sheet = copy.getSheet(0);
	}
	
	public void closeStream() throws IOException, BiffException, WriteException
	{
		copy.write(); 
		copy.close();
		new File(filename).delete();
		new File(filename+"_tmp.xls").renameTo(new File(filename));
	}
		
	private void newTitleLabel(String s) throws RowsExceededException, WriteException
	{
		Label label = new Label(current_col, current_row, s, arial12format);		
		sheet.addCell(label);
		current_col++;
	}
	
	private void newStringLabel(String s) throws RowsExceededException, WriteException
	{
		Label label = new Label(current_col, current_row, s);		
		sheet.addCell(label);
		current_col++;
	}

	public void newNumberLabel(double v, String type) throws RowsExceededException, WriteException
	{
		Number number = new Number(current_col, current_row, v);
		switch (type)
		{
		case "float":
			if (v != 0)
				number = new Number(current_col, current_row, v, fivedpsFormat);
			else
				number = new Number(current_col, current_row, v);			
			break;
		case "percent":
			number = new Number(current_col, current_row, v, percentFormat);
			break;
		}
		sheet.addCell(number);
		current_col++;	
	}
	
	public void nextRow() throws IOException
	{
		current_row++;
		current_col=0;
	}

	public void addCommonStats(String voc_type, double[] p, int[] v, int size_vocab, double alpha, double beta, int nbruns, int[] skipTerms) throws BiffException, IOException, WriteException
	{
		openStream();
		newStringLabel(voc_type);
		newNumberLabel(p[0], "float");
		newNumberLabel(p[1], "float");
		newNumberLabel(p[2], "float");
		newNumberLabel(p[3], "float");
		newNumberLabel(v[0], "int");
		newNumberLabel(v[1], "int");
		newNumberLabel(v[2], "int");
		newNumberLabel(v[3], "int");		
		newNumberLabel(size_vocab, "int");
		newNumberLabel(skipTerms[0], "int");
		newNumberLabel(skipTerms[1], "int");
		newNumberLabel(skipTerms[2], "int");
		newNumberLabel(skipTerms[3], "int");
		newNumberLabel(alpha, "float");
		newNumberLabel(beta, "float");
		newNumberLabel(nbruns, "int");
		closeStream();
	}

}
