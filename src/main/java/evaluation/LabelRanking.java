package evaluation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import core.ForIndexing;
import core.InvertedIndex;
import core.MyDocument;
import jxl.Workbook;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.NumberFormat;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;

/**
 * 
 * Cette classe calcule différente mesure sur les termes qui composent le vocabulaire, globalement ou en fonction d'une catégorie.
 * 
 * @author julien
 *
 */

public class LabelRanking
{
	// min. TF value
	private static int MIN_TF = 4;
	
	// number of documents for each category (including "all")
	private HashMap<String,Integer> nbdocs;
	
	// ordered lists of terms for the gt
	private OrderedLabels gt_labels;
	
	// total number of categories
	private int nb_category;
	
	public LabelRanking()
	{
		nbdocs = new HashMap<>();
		gt_labels = new OrderedLabels();
		calc_nbdocs();
	}
	
	/* calculate the number of docs for each category + the whole set ("all"). */ 
	private void calc_nbdocs()
	{
		for (String key : MyDocument.getAllKeys())
		{
			MyDocument d = MyDocument.get(key);
			String gt = d.getGround_truth();
			Integer i1 = nbdocs.get(gt);
			if (i1 == null)
				nbdocs.put(gt, new Integer(1));
			else
				nbdocs.put(gt, new Integer(i1.intValue() + 1));
			Integer i2 = nbdocs.get("all");
			if (i2 == null)
				nbdocs.put("all", new Integer(1));
			else
				nbdocs.put("all", new Integer(i2.intValue() + 1));
		}
	}
	
	public String toString()
	{
		String s = "";
		for (String key : nbdocs.keySet())
		{
			s += key + " : " + nbdocs.get(key) + " ; ";
		}
		return s;
	}

	public void exportMesures(InvertedIndex index, String filename) throws IOException, RowsExceededException, WriteException
	{
		WritableWorkbook workbook = Workbook.createWorkbook(new File(filename));
		NumberFormat fivedps = new NumberFormat("#.#####"); 
		WritableCellFormat fivedpsFormat = new WritableCellFormat(fivedps);
		// Create a cell format for Arial 10 point font 
		WritableFont arial12font = new WritableFont(WritableFont.ARIAL, 12, WritableFont.BOLD, true);  
		WritableCellFormat arial12format = new WritableCellFormat (arial12font); 

		nb_category = nbdocs.size() - 1;
		for (String category : nbdocs.keySet())
		{
			WritableSheet sheet = workbook.createSheet(category, workbook.getNumberOfSheets());
			Label label;
			label = new Label(0, 0, "Term", arial12format);			
			sheet.addCell(label); 
			label = new Label(1, 0, "Length", arial12format);
			sheet.addCell(label);
			label = new Label(2, 0, "TF", arial12format);
			sheet.addCell(label);
			label = new Label(3, 0, "NBDocs", arial12format);
			sheet.addCell(label);
			label = new Label(4, 0, "EntTF", arial12format);
			sheet.addCell(label);
			label = new Label(5, 0, "TFxEntTF", arial12format);
			sheet.addCell(label);
			label = new Label(6, 0, "EntNBDocs", arial12format);
			sheet.addCell(label);
			label = new Label(7, 0, "NBDocsxEntNBDocs", arial12format);
			sheet.addCell(label);
			label = new Label(8, 0, "log2(NBDocs)xEntNBDocs", arial12format);
			sheet.addCell(label);
			int current_row = 1;
			ArrayList<Measures4Term> measures = calc_measures(index, category, false);
			for (int i=0; i<measures.size(); i++)
			{
				Measures4Term m = measures.get(i);
				if (m.getFeature_tf() > MIN_TF)
				{
					label = new Label(0, current_row, m.getFeature_name());
					sheet.addCell(label);
					Number number;
					number = new Number(1, current_row, m.getFeature_length()); 
					sheet.addCell(number);
					number = new Number(2, current_row, m.getFeature_tf()); 
					sheet.addCell(number);
					number = new Number(3, current_row, m.getFeature_nbdocs()); 
					sheet.addCell(number);
					if (m.getFeature_entropyTF() != 0)
						number = new Number(4, current_row, m.getFeature_entropyTF(), fivedpsFormat);
					else
						number = new Number(4, current_row, m.getFeature_entropyTF());
					sheet.addCell(number);
					number = new Number(5, current_row, m.getFeature_tfxentropy(), fivedpsFormat); 
					sheet.addCell(number);
					if (m.getFeature_entropyNBDocs() != 0)
						number = new Number(6, current_row, m.getFeature_entropyNBDocs(), fivedpsFormat);
					else
						number = new Number(6, current_row, m.getFeature_entropyNBDocs());
					sheet.addCell(number);
					number = new Number(7, current_row, m.getFeature_nbdocsxentropy(), fivedpsFormat);
					sheet.addCell(number);
					double aveclog = log2(m.getFeature_entropyNBDocs()) * m.getFeature_entropyNBDocs();
					if (aveclog != 0)
						number = new Number(8, current_row, aveclog , fivedpsFormat);
					else
						number = new Number(8, current_row, aveclog);
					sheet.addCell(number);
					current_row++;
				}
			}
			System.out.println((current_row-1) + " lignes ont été ajoutées à l'onglet " + category + " du fichier " + filename);
		}
		workbook.write(); 
		workbook.close();
	}
	
	/* calculate all the possible measures on the indexed terms (activated = true means only the activated terms) */
	private ArrayList<Measures4Term> calc_measures(InvertedIndex index, String category, boolean activated)
	{		
		ArrayList<Measures4Term> list = new ArrayList<Measures4Term>();
		for (String key : index.getList().keySet())
		{
			ForIndexing f = index.getList().get(key);
			if (!activated || f.isActivated())
			{
				Measures4Term newm = new Measures4Term(f.getTerm());
				newm.setFeature_length(f.getLength());
				newm.setFeature_tf(f.getTF(category));
				newm.setFeature_nbdocs(f.getNBDocs(category));
				double entTF = log2(nb_category) - calc_entropy(f.getAllTF());
				newm.setFeature_entropyTF(entTF);
				double tfxentropy = entTF * f.getTF(category);
				newm.setFeature_tfxentropy(tfxentropy);
				double entNBDocs = log2(nb_category) - calc_entropy(f.getAllNBDocs());
				newm.setFeature_entropyNBDocs(entNBDocs);
				double nbdocsxentropy = entNBDocs * f.getNBDocs(category);
				newm.setFeature_nbdocsxentropy(nbdocsxentropy);
				list.add(newm);
			}
		}		
		return list;
	}
	
	private double calc_entropy(HashMap<String,Integer> list)
	{
		Double result = 0.0;
		Integer i = list.get("all");
		if (i != null)
		{
			Double total = (double) i.intValue();
			for (String key : list.keySet())
			{
				if (!key.equals("all"))
				{
					Double frequency = (double) list.get(key) / total;
					result -= frequency * (Math.log(frequency) / Math.log(2));
				}
			}
		}
		else
			result = -999.0;
		return result;
	}
	
	private double log2(double x)
	{
		return (Math.log(x) / Math.log(2));
	}
	
	public void ranktopkterms(InvertedIndex index)
	{
		nb_category = nbdocs.size() - 1;
		for (String category : nbdocs.keySet())
		{
			ArrayList<Measures4Term> measures = calc_measures(index, category, true);
			Measures4Term.setComparator(Measures4Term.TF);
			Collections.sort(measures);
			for (int i=0; i<OrderedLabels.MAX_TOP_TERMS; i++)
			{
				//System.out.print(measures.get(i).getFeature_name() + "(" + measures.get(i).getFeature_tf() + "," + measures.get(i).getFeature_entropyTF()+")-");
				gt_labels.add("TF " + category, measures.get(i).getFeature_name(), measures.get(i).getFeature_tf());
			}
			Measures4Term.setComparator(Measures4Term.LogTFxEnt);
			Collections.sort(measures);
			for (int i=0; i<OrderedLabels.MAX_TOP_TERMS; i++)
			{
				//System.out.print(measures.get(i).getFeature_name() + "(" + measures.get(i).getFeature_tf() + "," + measures.get(i).getFeature_entropyTF()+")-");
				gt_labels.add("LogTFxEnt " + category, measures.get(i).getFeature_name(), measures.get(i).getFeature_logtfxentropy());
			}
		}
	}

	public void printAllGT()
	{
		System.out.println("Pour TF :");
		for (String category : nbdocs.keySet())
		{
			System.out.println(gt_labels.toString("TF " + category));	
		}
		System.out.println("Pour LogTFxEnt :");
		for (String category : nbdocs.keySet())
		{
			System.out.println(gt_labels.toString("LogTFxEnt " + category));	
		}		
	}
	
	public ArrayList<String> getTopTerms(String key)
	{
		return gt_labels.getTopTerms(key, OrderedLabels.MAX_TOP_TERMS);
	}
	
}
