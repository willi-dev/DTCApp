/**
 * Oauth.java
 * package node.server.coordinator
 */
package node.server.main;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

//import javax.net.ssl.HttpsURLConnection;
import node.server.main.DirectoryHandler;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * @author Willi
 *
 */
public class Oauth {
	private static DirectoryHandler dirHandler = new DirectoryHandler();
	private static String pathBearer = dirHandler.getDirToken()+"./bearertoken.json";
	
	private static String https = "https://";
	private static String endPointHost = "api.twitter.com";
	private static String searchTweetsPoint = "/1.1/search/tweets.json?";
	
	private String consumerKey = ""; // SET ConsumerKey = nYIIxPgqsT10rV2112zM2Q
	private String consumerSecret = ""; // SET ConsumerSecret = IoWDuwpYmbwIS4qvUCHRZbl0XikzPvHtBHVg8vYAC4
	
	/*
	 * Set Consumer Key Twitter API
	 */
	public void setConsumerKey(String consumerkey){
		this.consumerKey = consumerkey;
	}
	/*
	 * Get Consumer Key Twitter API
	 */
	private String getConsumerKey(){
		return this.consumerKey;
	}
	/*
	 * Set Consumer Secret Twitter API
	 */
	public void setConsumerSecret(String consumersecret){
		this.consumerSecret = consumersecret;
	}
	/*
	 * Get Consumer Secret Twitter API
	 */
	private String getConsumerSecret(){
		return this.consumerSecret;
	}
	/*
	 * Get End Point URL of HOST 
	 */
	private String getEndPointHost(){
		return endPointHost;
	}
	/*
	 * Get End Point URL of HOST with https
	 */
	private String httpsEndPointHost(){
		return https + endPointHost;
	}
	/*
	 * Get Search Tweets Point URL
	 */
	private String getSearchTweetsPoint(){
		return searchTweetsPoint;
	}
	/*
	 * method encodeKeys 
	 * encoding consumerKey & consumerSecret 
	 * using base64 encoding
	 * return String of encode keys
	 */
	private String encodeKeys(String consumerKey, String consumerSecret){
		try{
			String encodedConsumerKey = URLEncoder.encode(consumerKey, "UTF-8");
			String encodedConsumerSecret = URLEncoder.encode(consumerSecret, "UTF-8");
			
			String fullKey = encodedConsumerKey + ":" + encodedConsumerSecret;
			byte[] encodedBytes = Base64.encodeBase64(fullKey.getBytes());
			return new String(encodedBytes);
		}catch(UnsupportedEncodingException e){
			return new String("Encoding error...");
		}
	}
	/*
	 * Method requestBearerToken
	 * request bearer token (application only-auth access) 
	 * save the result to bearertoken.json
	 * return String of bearer token
	 */
	private String requestBearerToken(String endPointUrlToken) throws IOException{
		HttpsURLConnection connection = null;
		String ck = getConsumerKey(); // Get Consumer Key 
		String cs = getConsumerSecret(); // Get Consumer Secret
		String getEPH = getEndPointHost(); // Get End Point Host

		String encodedCredentials = encodeKeys(ck, cs);
		
		FileWriter fw = new FileWriter(pathBearer); // set path to FileWriter
		
		try{
			URL url = new URL(endPointUrlToken);
			connection = (HttpsURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Host", getEPH);
			connection.setRequestProperty("User-Agent", "dtcappServer");
			connection.setRequestProperty("Authorization", "Basic " + encodedCredentials);
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
			connection.setRequestProperty("Content-Length", "29");
			connection.setUseCaches(true);
			
			writeRequest(connection, "grant_type=client_credentials");
			
			JSONObject obj = (JSONObject)JSONValue.parse(readResponse(connection));
			if(obj != null){
				String objToString = obj.toJSONString();
				fw.write(objToString); // write file
				String tokenType = (String)obj.get("token_type");
				String token = (String)obj.get("access_token");
				return ((tokenType.equals("bearer")) && (token != null)) ? token : "";
			}else{
				return new String("no bearer token");
			}
			
		}catch(MalformedURLException e){
			throw new IOException("Invalid endpoint URL specified.", e);
		}
		finally{
			if(connection != null){
				connection.disconnect();
				if(fw != null){
					fw.flush();
					fw.close();
				}
			}
		}
	}
	/*
	 * Method getBearerToken
	 * read bearer token from bearertoken.json
	 * return String of bearer token
	 */
	private String getBearerToken(){
		String httpsEPH = httpsEndPointHost();
		String oauth2token = "/oauth2/token"; // end point oauth2token
		String endPointToken = httpsEPH + oauth2token;
		
		try{
			File file = new File(dirHandler.getDirToken()+"./bearertoken.json");
			Boolean fileExist = file.exists();
			if(fileExist == true){
				JSONObject obj = (JSONObject)JSONValue.parse(new FileReader(dirHandler.getDirToken()+"./bearertoken.json"));
				
				//String tokenType = (String)obj.get("token_type");
				String token = (String)obj.get("access_token");
				//if((tokenType.equals("bearer")) && (token != null)){
				return token;
				//}
			}else{
				return requestBearerToken(endPointToken); // return requestBearerToken if bearer token not available yet.
			}
		}catch(IOException e){
			return new String();
		}
	}
	/*
	 * Method rateLimitStatus
	 * for request rate limit status of application
	 * return String of rate limit status
	 */
	public String rateLimitStatus() throws IOException{
		HttpsURLConnection connection = null;
		String getEPH = getEndPointHost(); // Get End Point Host
		String endPointLimit = "https://api.twitter.com/1.1/application/rate_limit_status.json?resources=search";
		String bearerToken = getBearerToken(); 
		//String bearerToken = requestBearerToken("https://api.twitter.com/oauth2/token"); 
		try{
			URL url = new URL(endPointLimit);
			connection = (HttpsURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Host", getEPH);
			connection.setRequestProperty("User-Agent", "dtcappServer");
			connection.setRequestProperty("Authorization", "Bearer "+ bearerToken);
			connection.setUseCaches(false);
			
			JSONObject obj = (JSONObject) JSONValue.parse(readResponse(connection));
			if(obj != null){
				//String objToString = obj.toJSONString();
				JSONObject objResources = (JSONObject)obj.get("resources");
				JSONObject objSearch = (JSONObject)objResources.get("search");
				JSONObject objSearchTweets = (JSONObject)objSearch.get("/search/tweets");
				String objstToString = objSearchTweets.toJSONString();
				JSONObject newobj = (JSONObject)JSONValue.parse(objstToString);
				Long limit = (Long) newobj.get("limit");
				Long remaining = (Long)newobj.get("remaining");
				return ((newobj != null) ? "limit: " + limit + "; remaining : " + remaining : "unknown rate limit status");
			}
			return new String("failed: rate limit status " + connection.getResponseCode() + " - " + connection.getResponseMessage());
		
		}catch(MalformedURLException e){
			throw new IOException("Invalid endpoint URL specified.", e);
		}
		finally{
			if(connection != null){
				connection.disconnect();
			}
		}
	}
	/*
	 * Method Search
	 * for search tweets with parameters from twitter API 1.1 parameters
	 * return JSON Object
	 */
	public String Search(String q, String geocode, String lang, String locale, String resultType, String count, String until, String sinceId, String maxId, String includeEntities, String callback) throws IOException{
		HttpsURLConnection connection = null;
		String bearerToken = getBearerToken(); // Get Bearer Token
		//String bearerToken = requestBearerToken("https://api.twitter.com/oauth2/token");
		
		String searchTweets = getSearchTweetsPoint(); // Get Search Tweets Point
		String getEPH = getEndPointHost(); // Get End Point Host
		String httpsEPH = httpsEndPointHost(); // Get End Point Host + https
		
		String setSearchParameter = setSearchParameter(q, geocode, lang, locale, resultType, count, until, sinceId, maxId, includeEntities, callback);
		String endPointUrl = httpsEPH + searchTweets + setSearchParameter;
		try{
			URL url = new URL(endPointUrl);
			connection = (HttpsURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Host", getEPH);
			connection.setRequestProperty("User-Agent", "dtcappServer");
			connection.setRequestProperty("Authorization", "Bearer "+ bearerToken);
			connection.setUseCaches(false);
			
			String objResult = readResponse(connection);
			if(objResult != null){ 
				//return new String("success" + connection.getResponseCode() + "-" + connection.getResponseMessage());
				return ((objResult != null) ? objResult : "empty result");
			}
			return new String("bearer token:"+ bearerToken +";\n response value failed \n " + connection.getResponseCode() + "-" + connection.getResponseMessage());
		}catch(MalformedURLException e){
			throw new IOException("Invalid endpoint URL specified.", e);
		}
		finally{
			if(connection != null){
				connection.disconnect();
			}
		}
	}
	/*
	 * Method setSearchParameter
	 * mapping of input parameter
	 * return String of map parameter
	 */
	private String setSearchParameter(String q, String geocode, String lang, String locale, String resultType, String count, String until, String sinceId, String maxId, String includeEntities, String callback) throws IOException{
		Map<String, String> mapParameter = new HashMap<String, String>();
		mapParameter.put("q", q);
		mapParameter.put("lang", lang);
		mapParameter.put("locale", locale);
		mapParameter.put("result_type", resultType);
		mapParameter.put("count", count);
		mapParameter.put("until", until);
		mapParameter.put("since_id", sinceId);
		mapParameter.put("max_id", maxId);
		mapParameter.put("include_entities", includeEntities);
		mapParameter.put("callback", callback);
		String outputEndPoint = mapToStringParameter(mapParameter);
		return outputEndPoint;
	}
	/*
	 * Method mapToString
	 * convert map to string result
	 * return String of parameter with format "param1=value1&param2=value2&param3=value3.."
	 */
	private static String mapToStringParameter(Map<String, String> map){
		StringBuilder sb = new StringBuilder();
		for(String key : map.keySet()){
			if(sb.length() > 0){
				sb.append("&");
			}
			String value = map.get(key);
			try{
				sb.append((key != null ? URLEncoder.encode(key, "UTF-8") : ""));
				sb.append("=");
				sb.append(value != null ? URLEncoder.encode(value, "UTF-8") : "");
			}catch(UnsupportedEncodingException e){
				throw new RuntimeException("This method requires UTF-8 encoding support", e);
			}
		}
		return sb.toString();
	}
	
	/*
	 * Method readResponse 
	 * read response from connection HttpsURLConnection
	 * return String of response
	 */
	private static String readResponse(HttpsURLConnection connection) {
		// TODO Auto-generated method stub
		try {
			StringBuilder str = new StringBuilder();
			//StringBuffer str = new StringBuffer();
			BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line = "";
			while((line = br.readLine()) != null){
				str.append(line);
			}
			br.close();
			return str.toString();
		}catch(IOException e){
			return new String();
		}
	}
	/*
	 * Method writeRequest
	 * write request of HttpsURLConnection
	 * return boolean 
	 */
	private static boolean writeRequest(HttpsURLConnection connection, String textBody) {
		// TODO Auto-generated method stub
		try{
			BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
			wr.write(textBody);
			wr.flush();
			wr.close();
			
			return true;
		}catch(IOException e){
			return false;
		}
	}
	
	/*
	 * Method saveResult
	 * save string result of search method to file
	 
	public void saveResult(String resultOfSearch, String nameFile, String createDate){
		FileWriter fileWriter = null;
        try {
            String content = resultOfSearch;
            File newTextFile = new File("./"+ nameFile +"_"+ createDate +".json");
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
	}*/
	
}

