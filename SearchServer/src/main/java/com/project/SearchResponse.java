package com.project;

public class SearchResponse {

	public SearchResponse() { }
	
	public int TotalHits;
	public ClientDocument[] Documents;
	public long TotalTime;
	
	public boolean SearchReplaced = false;
	public String SearchReplacement = "";
	
	public static class ClientDocument { 
		public ClientDocument() { }
		
		public int Number;
		public float Score;
		public String Title;
		public String Content;
		public String Url;
	}
	
}
