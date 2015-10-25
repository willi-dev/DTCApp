package node.server.main;

import java.io.IOException;
//import java.text.DateFormat;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

//import node.server.main.Oauth;
import node.server.main.DirectoryHandler;
import node.server.main.JSONHandler;

public class Downloader extends Oauth{

	//private Oauth oauth = new Oauth();
	private static DirectoryHandler dirHandler = new DirectoryHandler();
	private static JSONHandler jHandler = new JSONHandler();
	private static String getSearch = null; 
	private static JSONArray getStatuses = null;
	private static String pathFileResult = null; 
	public static String pathFileResultTemp = null;
	//private StringBuilder tempResult = new StringBuilder();
	
	public Downloader(){
		//Set Consumer Key & Consumer Secret Key
		//setConsumerKey(" nYIIxPgqsT10rV2112zM2Q");
		//setConsumerSecret("IoWDuwpYmbwIS4qvUCHRZbl0XikzPvHtBHVg8vYAC4");
		setConsumerKey("TsvM8moGzffjMOZC0LDibtXAQ");
		setConsumerSecret("3PxnfeSwtEDRWzRMWrh5Owz17IzR6iOp4KSfAO1aDxc560sl2d");
	}
	
	/*public void appendTempResult(String statuses){
		tempResult.append(statuses);
	}
	
	public StringBuilder getTempResult(){
		return tempResult;
	}*/
	
	/*
	 * Method crawlingProcess
	 * get parameters of Twitter search API 1.1 client application
	 */
	public void crawlingProcess(String paramsFromClient){
//		Map<String, String> pfq = paramsFromQueue;
//		String q = pfq.get("q");
//		String geocode = pfq.get("geocode");
//		String lang = pfq.get("lang");
//		String locale = pfq.get("locale");
//		String result_type = pfq.get("result_type");
//		String count = pfq.get("count");
//		String until = pfq.get("geocode");
//		String since_id = pfq.get("geocode");
//		String max_id = pfq.get("geocode");
//		String include_entities = pfq.get("geocode");
//		String callback = pfq.get("geocode");
		JSONObject objparam = (JSONObject)JSONValue.parse(paramsFromClient);
		String q = (String)objparam.get("q");
		String geocode = (String)objparam.get("geocode");
		String lang = (String)objparam.get("lang");
		String locale = (String)objparam.get("locale");
		String result_type = (String)objparam.get("result_type");
		String count = (String)objparam.get("count");
		String until = (String)objparam.get("until");
		String since_id = (String)objparam.get("since_id");
		String max_id = (String)objparam.get("max_id");
		String include_entities = (String)objparam.get("include_entities");
		String callback = (String)objparam.get("callback");
		System.out.println("CRAWLING INFO : keyword -> "+ q);
		System.out.println("CRAWLING INFO : geocode -> "+ ((geocode==null) ? "none" : geocode));
		System.out.println("CRAWLING INFO : lang -> " + ((lang==null) ? "none" : lang));
		System.out.println("CRAWLING INFO : locale -> " + ((locale==null) ? "none" :locale));
		System.out.println("CRAWLING INFO : result_type -> " + ((result_type==null) ? "none" :result_type));
		System.out.println("CRAWLING INFO : count -> " + ((count==null) ? "none, default=15" : count));
		System.out.println("CRAWLING INFO : until -> " + ((until==null) ? "none" : until));
		System.out.println("CRAWLING INFO : since_id -> " + ((since_id==null) ? "none" : since_id));
		System.out.println("CRAWLING INFO : max_id -> " + ((max_id==null) ? "none" :max_id));
		System.out.println("CRAWLING INFO : include_entities -> " + ((include_entities==null) ? "none" : include_entities));
		System.out.println("CRAWLING INFO : callback -> " + ((callback==null) ? "none" : callback));
		
		/*
		 * Call collectingTweets method
		 * collectingTweets(q, geocode, lang, locale, resultType, count, until, sinceId, maxId, includeEntities, callback);
		 */
		collectingTweets(q, geocode, lang, locale, result_type, count, until, since_id, max_id, include_entities, callback);
	}
	
	
	/*
	 * Method collectingTweets
	 * implement of oauth.Search();
	 */
	private void collectingTweets(String q, String geocode, String lang, String locale, String resultType, String count, String until, String sinceId, String maxId, String includeEntities, String callback){
		try {
			getSearch = null;
			getSearch = Search(q, geocode, lang, locale, resultType, count, until, sinceId, maxId, includeEntities, callback);
			System.out.println("");
			System.out.println("==============================================================================");
			System.out.println("");
			System.out.println("CRAWLING INFO : DOWNLOADER -> Proses pengumpulan tweet selesai dilakukan...");
			System.out.println("");
			System.out.println("==============================================================================");
			//int ukuranArray = getStatuses(getSearch).size();
			//System.out.println("ukuran array statuses : " + ukuranArray);
			
			/*
			 * saveResult(getSearch);
			System.out.println("");
			System.out.println("CRAWLING INFO : DOWNLOADER -> Hasil pengumpulan tweets telah disimpan...");
			System.out.println("");
			System.out.println("==============================================================================");
			*/
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getSearch(){
		return getSearch;
	}
	
	public JSONArray getStatuses(String getsearch){
		// try to print json result object
		JSONObject objResult = (JSONObject)JSONValue.parse(getSearch);
		
		//JSONObject objStatuses = (JSONObject)objResult.get("statuses");
		JSONArray arrayStatuses = (JSONArray)objResult.get("statuses");
		
		getStatuses = arrayStatuses;
		return getStatuses;
	}
	
	
	/*
	 * Method getRateLimit
	 * get rate limit status of application
	 */
	public void getRateLimit(){
		try {
			String getratelimit = rateLimitStatus();
			System.out.println("==============================================================================");
			System.out.println("");
			System.out.println("CRAWLING INFO : DOWNLOADER -> Rate limit status of application : ");
			System.out.println("CRAWLING INFO : DOWNLOADER -> "+ getratelimit);
			System.out.println("");
			System.out.println("==============================================================================");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			
			e.printStackTrace();
		}
	}
	
	/*
	 * Method saveResult
	 * save result of Search
	 */
	public static void saveResult(String namefile, String getSearch){
		/*DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		Date date = new Date();
		String dateSimpan = dateFormat.format(date);*/
		//String directoryresult = "results"; 
		pathFileResult = "dtcapp/results/result_"+ namefile;
		String pathNamefile = dirHandler.getDirResult()+"/"+"result_"+ namefile;
		//saveToJSON(getSearch, directoryresult, namefile);
		jHandler.saveToJSON(getSearch, pathNamefile);
	}
	
	/*
	 * Method saveResult
	 * save result of Search
	 */
	public static void saveResultTemp(String namefile, String result){
		/*DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		Date date = new Date();
		String dateSimpan = dateFormat.format(date);*/
		//String directoryresult = "results"; 
		pathFileResultTemp = "dtcapp/results/"+ namefile;
		String pathNamefile = dirHandler.getDirResult()+"/"+ namefile;
		//saveToJSON(getSearch, directoryresult, namefile);
		jHandler.saveToJSON(result, pathNamefile);
	}
	
	/*
	 * Method getPathResult
	 * get path of results
	 */
	public String getPathResult(){
		return "/" + pathFileResult +".json";
	}
}
