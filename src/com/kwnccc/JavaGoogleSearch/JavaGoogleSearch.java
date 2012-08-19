package com.kwnccc.JavaGoogleSearch;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Console;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.PrettyXmlSerializer;
import org.htmlcleaner.TagNode;

public class JavaGoogleSearch {

	private final static String SEARCH_ENGINE_URL = "http://www.google.com/";
	private final static String SEARCH_CHARSET = "UTF-8";
	
	private static String _searchTerm, _query;
	private static int _numOfSnippets;
	private static ArrayList<GoogleResultSnippet> _snippetList;

	
	public JavaGoogleSearch(String searchTerm, int numOfSnippets){
		_searchTerm = searchTerm;
		_numOfSnippets = numOfSnippets;
	
		searchTerm();
	}
	
	public void searchTerm(){
		
		_snippetList = new ArrayList<GoogleResultSnippet>();
		_query = formatQuery();
		
		//1.make the connection and read all the 1000 results from Google
		//2.for each response from the Google Search get rid of the unused info, saving only what is needed 
		try {				
			// We have to 'clean' the html file to a fixed structure!
            CleanerProperties props = new CleanerProperties();
 
            // set some properties to non-default values
            props.setTranslateSpecialEntities(true);
            props.setTransResCharsToNCR(true);
            props.setOmitComments(true);
                
            int numOfSnippetPages = _numOfSnippets/10;
                
			for(int j = 0; j < numOfSnippetPages; j++){
					
				URLConnection connection = getConnection(j);
				InputStream response = connection.getInputStream();
					
	            // clean the response from unneeded information
	            TagNode tagNode = new HtmlCleaner(props).clean(response);
	            List<TagNode> cleanedSnippetSet = tagNode.getElementListByAttValue("class", "g", true, false); 
	    	    extractToHTML(cleanedSnippetSet, props, j);    
	           	            
	    	    for(int g = 0; g < cleanedSnippetSet.size(); g++){
	    	   
	    	      	GoogleResultSnippet snippet = extractSnippet(cleanedSnippetSet, g); 
	    	      	
	    	       	if(snippet == null)
	    	       		continue;
	    	       	
	    	        _snippetList.add(snippet);    
	    	    } 
			}
				
	        System.out.println(_searchTerm + ": " + _snippetList.size() + " Results");
	        
		} catch (MalformedURLException e) {
			System.out.println("MalformedURLException:: " + e.toString());
			System.out.println("An error has been occured while reading results... ");
		} catch (FileNotFoundException e) {
			System.out.println("MalformedURLException:: " + e.toString());
			System.out.println("An error has been occured while reading results... ");
		} catch (IOException e) {
			System.out.println("IOException:: " + e.toString());
			System.out.println("An error has been occured while reading results... ");
		}
	}
	
	private String formatQuery(){
		
		String query = null;
		
		try {
			query = String.format("q=%s", URLEncoder.encode(_searchTerm, SEARCH_CHARSET));
		} catch (UnsupportedEncodingException e) {
			System.out.println("UnsupportedEncodingException:: " + e.toString());
		}
		
		return query;
	}
	
	private URLConnection getConnection(int start){
		URLConnection connection = null;
		
		// Create the url returned 
		try {
			connection = new URL(SEARCH_ENGINE_URL + "search?" + _query + "+-video&hl=en&prmd=imvns&filter=0&start=" + (start*10)).openConnection();
			connection.setRequestProperty("Accept-Charset", SEARCH_CHARSET);
			connection.setRequestProperty("User-Agent", "Mozilla/8.0");
			System.out.println("Connection established...");
		} catch (MalformedURLException e) {
			e.printStackTrace();
			System.out.println("Connection failed!");
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Connection failed!");
		}
			
		return connection;
	}
	
	private GoogleResultSnippet extractSnippet(List<TagNode> cleanedSnippetSet, int index){
		
		GoogleResultSnippet snippet;
		
		TagNode currentDoc = cleanedSnippetSet.get(index);
       	TagNode cleanedTitle = currentDoc.findElementByAttValue("class", "r", true, false);
       	TagNode cleanedDescr = currentDoc.findElementByAttValue("class", "s", true, false);
            	
       	if(cleanedTitle == null)
       		return null;
        if(cleanedDescr == null)
        	return null;
            	
        TagNode cleanedURL = cleanedTitle.findElementHavingAttribute("href", true);
           	
        String title = cleanedTitle.getText().toString();
       	String body = cleanedDescr.getText().toString();
        String address = cleanedURL.getAttributeByName("href"); 
        
        // because google uses "/url?q=" before each link
        if(address.startsWith("/url?q="))
        	address = address.substring(7); 
           	
        snippet = new GoogleResultSnippet(title, body, address);
        return snippet;
	}

public void extractToXML(){
		
		System.out.println("Extracting to XML files...");
		
		File file = new File(_searchTerm);
		if(file.mkdir())
			System.out.println("Directory created.");
		else
			System.out.println("Directory is not created.");
		
		for(int i = 0; i < _snippetList.size(); i++){
			
			try {
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(_searchTerm+"/"+_searchTerm+"-docID="+(i+1)+".xml"), SEARCH_CHARSET));
				bw.write(_snippetList.get(i).toString());
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Error while extracting to XML files.");
			}
		}
	}
	
	public void extractToHTML(List<TagNode> cleanedSnippetSet, CleanerProperties props, int index){
		
		System.out.println("Extracting to HTML files...");
		
		for(int i = 0; i < cleanedSnippetSet.size(); i++){
			
			TagNode cleanedNode = new TagNode("html");
			cleanedNode.addChildren(cleanedSnippetSet);
			try {
				new PrettyXmlSerializer(props).writeToFile(cleanedNode, _searchTerm+(index+1)+".html", SEARCH_CHARSET);
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Error while extracting to HTML files.");
			}
		}
	}
	
	public static void main(String args[]){
		
		JavaGoogleSearch jgs = new JavaGoogleSearch("Russel", 50);
		jgs.extractToXML();
		System.out.println("//end");
	}
}