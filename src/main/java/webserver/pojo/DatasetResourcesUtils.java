package webserver.pojo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;



public class DatasetResourcesUtils {

	/**
	 * Get all subfolders from a given folder. Taken from <a href=
	 * "http://stackoverflow.com/questions/5125242/java-list-only-subdirectories-from-a-directory-not-files">SO-5125242</a>
	 * 
	 * @param datasetsDirectory
	 * @return array of subfolder in this directory
	 */
	public static String[] getSubFolders(String datasetsDirectory) {

		File file = new File(datasetsDirectory);
		String[] directories = file.list(new FilenameFilter() {
			@Override
			public boolean accept(File current, String name) {
				return new File(current, name).isDirectory();
			}
		});

		return directories;

	}

	public static String getParentFolders(String datasetsDirectory) {

		File file = new File(datasetsDirectory);
		return file.getParent();
	}

	public static void WriteLabelRanking(String parent_file,String eval_file,String datasetid,int topicid,String tab,String type) throws IOException  {
		if (type.equals("sb")){
			ArrayList<String> temp = new ArrayList<String>();

			ArrayList<String> out = new ArrayList<String>();
			String ligne = null;
			try{
				BufferedReader strel = new BufferedReader(new FileReader(parent_file));
				int j = Integer.parseInt(strel.readLine());
				int num_lgn = 0;
				while ((ligne = strel.readLine()) != null) {
					num_lgn++;
					temp.add(ligne);
				}
				if (num_lgn == j+1){
					out.add("done");
				}else{
					out.add(Integer.toString(j+1));
				}
				for (String t : temp){
					out.add(t);
				}
				strel.close();
			}catch (Exception e) {
				e.printStackTrace();
			}
			BufferedWriter stret = new BufferedWriter(new FileWriter(parent_file));
			for (String t : out){
				stret.write(t);
				stret.newLine();
			}
			stret.flush();
			stret.close();
		}
		//System.out.println(eval_file);
		BufferedWriter stret2 = new BufferedWriter(new FileWriter(eval_file,true));
		stret2.write(datasetid+";"+topicid+";"+tab.replaceAll(":", ";"));
		stret2.newLine();
		stret2.flush();
		stret2.close();
	}

	public static boolean isValidFile(String f) {
		return new File(f).exists();
	}

	public static String getLabelDescription(String parent_file, String lbr) throws IOException {
		//String fic = parent_file+"\\"+"labelerDescription"+"\\"+lbr+".txt";
		String fic = parent_file+"/labelerDescription/"+lbr+".txt";
		//		System.out.println(fic);
		BufferedReader buff = new BufferedReader(new FileReader(fic));
		String desc = buff.readLine();
		buff.close();
		return desc;
	}

	public static String getdataset_topic_foruser(String user,String parent_file ) throws IOException {
		BufferedReader bf = new BufferedReader(new FileReader(parent_file+"/Grades/"+user+"-evaluation.txt"));
		String where = bf.readLine();
		String out="done";
		if (!where.equals("done")){	
			int index = Integer.parseInt(where);
			for (int i=0;i<index+1;i++){
				out=bf.readLine();
			}
			out+=";"+where;
		}
		bf.close();
		return out;
	}
	public static int getMaxEval(String user,String parent_file ) throws IOException {
		BufferedReader bf = new BufferedReader(new FileReader(parent_file+"/Grades/"+user+"-evaluation.txt"));
		String where = bf.readLine();
		int i = 0;
		while((where = bf.readLine()) != null){
			i++;
		}
		return i;
	}

	/* write the hide set (ex. "2;10;18") into the parent_file */
	public static void writeHiddenTopics(String parent_file, String hide_set) throws IOException {
		String[] split = hide_set.split(";");
		BufferedWriter w;
		w = new BufferedWriter(new FileWriter(parent_file));
		for (String z : split)
			if (!z.isEmpty()) {
				w.write(z + "\n");
			}
		w.flush();
		w.close();
	}
}
