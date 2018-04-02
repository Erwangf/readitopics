package exe;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;

import org.aksw.palmetto.Palmetto;
import org.aksw.palmetto.corpus.lucene.creation.SimpleLuceneIndexCreator;

public class IndexForPalmetto {

	public static void main(String[] args)
	{
		File indexDir = new File("index/");
		Iterator<String> docIterator = loadFile("wiki_00_clean.txt");
		SimpleLuceneIndexCreator creator = new SimpleLuceneIndexCreator(Palmetto.DEFAULT_TEXT_INDEX_FIELD_NAME);
		creator.createIndex(indexDir, docIterator);
	}
	
	public static Iterator<String> loadFile(String filename)
	{
		BufferedReader buff = null;
		HashSet<String> list = new HashSet<>();
		try {
			buff = new BufferedReader(new FileReader(filename));
			String line;
			while ((line = buff.readLine()) != null)
			{
				list.add(line);
			}
			buff.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return list.iterator();
	}
	
}
