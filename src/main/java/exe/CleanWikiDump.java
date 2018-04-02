package exe;

import java.io.IOException;

import io.LoadXMLfile;

public class CleanWikiDump {

	public static void main(String[] args) {
		try {
			for (String arg : args) {
				LoadXMLfile.extractXML(arg);
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
