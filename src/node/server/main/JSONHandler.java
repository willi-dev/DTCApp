package node.server.main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class JSONHandler {

	/*
	 * Method saveToJSON
	 * process save string to JSON file
	 */
	public void saveToJSON(String stringContent, String nameFile){
		FileWriter fileWriter = null;
        try {
            String content = stringContent;
            File newTextFile = new File( nameFile +".json");
            fileWriter = new FileWriter(newTextFile);
            fileWriter.write(content);
            fileWriter.close();
        } catch (IOException ex) {
        	ex.getStackTrace();
        } finally {
            try {
                fileWriter.close();
            } catch (IOException ex) {
               ex.getStackTrace();
            }
        }
	}
	
}
