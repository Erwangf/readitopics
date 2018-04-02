package core;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import io.LoadDataset;

/**
 *
 * @author jimmy La classe Document défini un document comme comportant : - Un
 * identifiant : le numéro de ligne d'où le document est extrait - Toutes les
 * colones du fichier CSV (attention spéficique au fichier :
 * "data_conf_all_sorted.csv" utilisé) - Une classe : attribuée lors du
 * clustering (initialisé à -1 signifiant l'absence d'un passage dans un
 * algorithme de clustering) - Un vecteur de Term Frequency - Un vecteur de
 * TFIDF - Une vérité de terrain : donnée à la récupération des documents
 *
 */
public class MyDocument implements Comparable<MyDocument>
{

	private static HashMap<String,MyDocument> documents = new HashMap<String,MyDocument>();	
	
    private String title;
    private String date;
    private String _abstract;
    private String author;
    // period of time, timeline must be discretized first, see method compute_distrib_temp_doc()
    private String period;
    private String groundTruth;
    private String id; // id of the doc
    private ArrayList<ForIndexing> words; // ordered list of words
    private int id_for_TM; // id for mapping this doc to the array list id of TM
    private Integer[] raw_lines; // number of lines related to this document in the initial file
    
    /* index the terms present in the document */
    private TreeMap<String,ForIndexing> terms;
    /* index the frequency associated to the terms */
    private TreeMap<String,Integer> tf;
    
	/* total number of tokens for each period and the whole period */
	private static TreeMap<String,Integer> total_tokens;
	/* p(d) if you consider the length of the docs */
	private static TreeMap<String,ArrayList<Double>> pd;
	/* distribution of docs over time */
	private static TreeMap<String,TreeSet<MyDocument>> list_doc_date;
	/* number of docs per period of time */
	private static TreeMap<String,Integer> num_doc_per_period;
	
	private static TreeSet<MyDate> sorted_dates;

	/* constructor of a document */
    public MyDocument(String id, String title, String date, String _abstract, String groundTruth, String author)
    {
        this.id = id;
        this.date = date;
        this.title = title;
        this._abstract = _abstract;
        this.author = author;
        String[] sp = groundTruth.split("\"");
        if (sp.length > 1) // trim the possible double quotes
        	this.groundTruth = sp[1];
        else
        	this.groundTruth = sp[0];
        this.words = new ArrayList<>();
        this.tf = new TreeMap<>();
        this.terms = new TreeMap<>();
        this.period = "no discretization of time yet";
    }
    
    /* add a new document to the database and return it */
    public static MyDocument add(String id, String title, String date, String _abstract, String groundTruth, String author)
    {
    	MyDocument ndoc = new MyDocument(id, title, date, _abstract, groundTruth, author);
    	documents.put(id, ndoc);
    	return ndoc;
    }
    
    public static MyDocument get(String id)
    {
    	return documents.get(id);
    }
    
    /*public static Iterator<Map.Entry<String,Document>> getIteratorOverDocuments()
    {
    	return documents.entrySet().iterator();
    }*/
    
    public static Set<String> getAllKeys()
    {
    	return documents.keySet();
    }
    
    public static int size()
    {
    	return documents.size();
    }

    public String getGround_truth() {
        return this.groundTruth;
    }
    
    public TreeMap<String,ForIndexing> getTermList()
    {
    	return terms;
    }

    public TreeMap<String,Integer> getTFList()
    {
    	return tf;
    }
    
    // return the TF value of term "name"
    public int getTF(String name)
    {
    	Integer i = tf.get(name);
    	if (i == null)
    		return 0;
    	else
    		return i.intValue();
    }
    
    public String getId() {
        return id;
    }

    public String getAuthor()
    {
    	return author;
    }
    
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String year) {
        this.date = year;
    }

	public String getPeriod() {
		return this.period;
	}

	private void setPeriod(String period) {
		this.period = period;
	}

    public String getAbstract() {
        return _abstract;
    }
    
    public int getWordNumber() {
    	return words.size();
    }

    public void setAbstract(String _abstract) {
        this._abstract = _abstract;
    }

    /*public int getClasse() {
        return classe;
    }

    public MyDocument setClasse(int c) {
        this.classe = c;
        return this;
    }*/
    
    public void addRawLines(ArrayList<String> list) throws NumberFormatException
    {
    	raw_lines = new Integer[list.size()];
    	int i = 0;
    	for (String s : list)
    		raw_lines[i++] = Integer.parseInt(s);
    }

    public String getText() {
        /*String result = this.getTitle();        
        if (!this.getAbstract().equals(this.getTitle())) {
            result += "\n"+this.getAbstract();
        }*/
    	String result = this.getAbstract();
        return result;
    }
    
	public void setInternalIDForTM(int i)
	{
		id_for_TM = i;
	}
	
	public int getInternalIDForTM()
	{
		return id_for_TM;
	}

    /*public void initTF(ArrayList<String> features, int taille) {
        this.initTf(taille);
        for (int i = 0; i < features.size(); ++i) {
            this.getTf()[i] = termFrequency(this, features.get(i));
        }
    }*/

    /*public static void initTF(ArrayList<String> features, ArrayList<Document> documents,int taille) {
        for (Document document : documents) {
            document.initTF(features, taille);
        }
    }*/

   /* public static Document getById(ArrayList<Document> documents, int id) {
        for (Document d : documents) {
            if (d.getId() == id) {
                return d;
            }
        }
        return null;
    }*/
	
	/* return p(d) either for the local period of time (type = 0) or for the whole period (type = 1) */
	public double pd(int type)
	{
		return pd.get(this.id).get(type);
	}	

	public int compareTo(MyDocument d)
	{
		return this.id.compareTo(d.id);
		/*if (this.id < d.id) return -1;
		if (this.id > d.id) return 1;
		return 0;*/
	}

	/* just add the word f in the ordered list of words */
	public void add(ForIndexing f)
	{
		words.add(f);		
	}

	public void add(ForIndexing f, int n)
	{
		//words.add(f);
		tf.put(f.getTerm(), n);
		terms.put(f.getTerm(), f);
	}

	public void getWords()
	{
		for (int i=0; i<words.size(); i++)
		{
			System.out.print(words.get(i).getTerm()+"-");
		}
		System.out.println();
	}
	
	/* compute how many times the sequence of indexed terms ff = t1-t2-t3... occurs in the whole corpus */
	
	public int indexFeatures(ArrayList<ForIndexing> ff)
	{
		int tf = 0;
		for (int i=0; i<words.size(); i++)
		{
			if ((words.get(i) == ff.get(0)) && ((i+ff.size()) <= words.size())) 
			{
				boolean match = true;
				for (int j=1; j<ff.size(); j++)
				{
					if (words.get(i+j) != ff.get(j))
						match = false;
				}
				if (match)
					tf++;
			}
		}
		return tf;
	}
	
	public String toString()
	{
		String s = id + " : ";
		boolean pass = false;
		for (String mapKey : tf.keySet())
		{
			if (terms.get(mapKey).isActivated())
			{
				if (pass)
					s += ", ";
				else
					pass = true;
				s += mapKey + " (" + tf.get(mapKey).intValue() + ")";
			}
		}
		return s;
	}
	
	/* Compute the length of the document (sum over activated features). */
	public int computeLength()
	{
		int s = 0;
		for (String mapKey : tf.keySet())
		{
			if (terms.get(mapKey).isActivated())
				s += tf.get(mapKey);
		}
		return s;
	}
	
	/* Compute the total number of distinct activated feature in the document. */
	public int computeLengthDistinct()
	{
		int s = 0;
		for (String mapKey : tf.keySet())
		{
			if (terms.get(mapKey).isActivated())
			if (tf.get(mapKey) > 0)
				s += 1;
		}
		return s;
	}
	
	public boolean contains(ForIndexing newf)
	{
		if (terms.get(newf.getTerm()) != null)
			return true;
		else
			return false;
	}
	
	/* Compute the total number of activated term in the corpus. */ 
	public static int computeTotalLength()
	{
		int sum = 0;
		for (String key : documents.keySet())
		{
			MyDocument doc = documents.get(key);
			sum += doc.computeLength();
		}
		return sum;
	}

	/* Compute the total number of (distinct) activated term for each document in the corpus. */
	public static int computeTotalLengthDistinct()
	{
		int sum = 0;
		for (String key : documents.keySet())
		{
			MyDocument doc = documents.get(key);
			sum += doc.computeLengthDistinct();
		}
		return sum;
	}

	public static void print2File(String fileName) throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
		//BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8"));
		for (String k : documents.keySet())
		{
			MyDocument d = documents.get(k);
			writer.write(d._abstract + "\n##########END##########\n");
		}
        writer.close();
	}
	
	public static void reinitAllDocs()
	{
		documents = new HashMap<String,MyDocument>();
	}
	
	public static void compute_distrib_temp_doc()
	{
		list_doc_date = new TreeMap<>();
		// distribution of docs over time
		for (String key : MyDocument.getAllKeys())
		{
			MyDocument doc = MyDocument.get(key);
			String period = MyDate.getTimePoint(doc.getDate());
			doc.setPeriod(period);
			TreeSet<MyDocument> l = list_doc_date.get(period);
			if (l == null)
				l = new TreeSet<MyDocument>();
			l.add(doc);
			list_doc_date.put(period, l);
		}
		// store the total number of docs per period
		num_doc_per_period = new TreeMap<>();
		sorted_dates = new TreeSet<>();
		for (String key : list_doc_date.keySet())
		{
			num_doc_per_period.put(key, new Integer(list_doc_date.get(key).size()));
			sorted_dates.add(new MyDate(key));
		}
	}
	
	public static void compute_proba_docs()
	{
		total_tokens = new TreeMap<>();
		int total = 0;
		// first: compute the totals for each period of time + the whole period
		for (String key : list_doc_date.keySet())
		{
			int period = 0;
			TreeSet<MyDocument> list_docs = list_doc_date.get(key);
			Iterator<MyDocument> iter = list_docs.iterator();
			while (iter.hasNext())
			{
				MyDocument doc = iter.next();
				double doc_len = doc.getWordNumber();
				period += doc_len;
				total += doc_len;
			}
			total_tokens.put(key, period);
		}
		total_tokens.put("all", total);
		// second: compute p(d) for each period + the whole period
		pd = new TreeMap<>();
		for (String key : MyDocument.getAllKeys())
		{			
			MyDocument doc = MyDocument.get(key);
			String doc_date = MyDate.getTimePoint(doc.getDate());
			double doc_len = doc.getWordNumber();
			double pd_period = doc_len / (double)total_tokens.get(doc_date);
			double pd_all = doc_len / (double)total_tokens.get("all");
			ArrayList<Double> list_pd = new ArrayList<>();
			list_pd.add(pd_period);
			list_pd.add(pd_all);
			pd.put(key, list_pd);
		}		
	}

	public static int getTotalTokens()
	{
		return total_tokens.get("all");
	}

	public static TreeSet<MyDate> getSortedDates()
	{
		return sorted_dates;
	}
	
	public static TreeMap<String,TreeSet<MyDocument>> getDocPerDate()
	{
		return list_doc_date;
	}

	public static int getNumDocPerPeriod(String period)
	{
		return num_doc_per_period.get(period);
	}

	public static void setInternalIDforLDA()
	{
		int i=0;
		for (String key : documents.keySet())
		{
			MyDocument doc = documents.get(key);
			doc.setInternalIDForTM(i);
			i++;			
		}
	}

	/* compute the number of occurrences of word w in the doc
	 * (used for indexing when we ignore biotex features) */ 
	
	public int getNBwords(ForIndexing w)
	{
		int n = 0;
		for (ForIndexing f : words)
		if (f == w)
			n++;
		return n;
	}

	/* get the initial messages related to this doc (here, tweets) */
	public String getInitialTexts()
	{
		String res = "";
		for (int i=0; i<raw_lines.length; i++)
		{
			Integer num = raw_lines[i] - 1; // don't forget this offset
			if (i>0)
				res += "\n";
			res += LoadDataset.getRawText(num);
		}
		return res;
	}
	
	/* get the number of initial messages */
	public int getNumInitialTexts()
	{
		return raw_lines.length;
	}

	public static void save2FileForBiotex(String fileName, TFSort list_docs) throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
		Iterator<TermValuePair> iter = list_docs.getList().iterator();
		while (iter.hasNext())
		{
			TermValuePair t = iter.next();
			MyDocument d = documents.get(t.getTerm());
			if (LoadDataset.isRawData())
				writer.write(d.getInitialTexts() + "\n##########END##########\n");
			else
				writer.write(d._abstract + "\n##########END##########\n");
		}
		writer.flush();
        writer.close();
	}

	
}
