package com.project;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

import com.project.SearchResponse.ClientDocument;
import com.project.informationRetrieval.indexer.Indexer;
import com.project.informationRetrieval.searcher.*;

import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SearchApiController {

	private static Searcher tcSearcher;
	
	private static DocumentBuilderFactory dbFactory;
	private static DocumentBuilder dBuilder;

	static {
		try {
			tcSearcher = new Searcher();
			dbFactory = DocumentBuilderFactory.newInstance();
			dBuilder = dbFactory.newDocumentBuilder();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@RequestMapping("/suggest")
	public Object[] suggest(@RequestParam(value = "term", defaultValue = "") String term) {
		term = term.toLowerCase();
		return tcSearcher.suggestComplete(term);
	}

	@RequestMapping("/search")
	public SearchResponse search(
			@RequestParam(value = "query", defaultValue = "") String query,
			@RequestParam(value = "pageNumber", defaultValue = "1") int pageNumber,
			@RequestParam(value = "resultsPerPage", defaultValue = "20") int resultsPerPage) {
		String correctedQuery = tcSearcher.correctQuery(query.toLowerCase());
		SearchResponse result = searchInIndex(correctedQuery, pageNumber, resultsPerPage);
		if(!query.equalsIgnoreCase(correctedQuery)) { 
			result.SearchReplaced = true;
			result.SearchReplacement = correctedQuery;
		}
			
		/*if(result.TotalHits == 0) { 
			String correctedQuery = tcSearcher.correctQuery(query);
			result = searchInIndex(correctedQuery, pageNumber, resultsPerPage);
			result.SearchReplaced = true;
			result.SearchReplacement = correctedQuery;
		}*/
		return result;
	}
	
	private SearchResponse searchInIndex(String query, int pageNumber, int resultsPerPage) { 
		try {
			long start = System.currentTimeMillis();
			
			SearchResponse response = new SearchResponse();
			TopDocs topDocs = tcSearcher.executeQuery(query, pageNumber, resultsPerPage );
			
			response.TotalHits = topDocs.totalHits;
			response.Documents = new SearchResponse.ClientDocument[resultsPerPage];
			
			for(int i = 0, j = (pageNumber-1)*resultsPerPage + 1 ; i < topDocs.scoreDocs.length; j++, i++) { 
				ScoreDoc sdoc = topDocs.scoreDocs[i];
				response.Documents[i] = new SearchResponse.ClientDocument();
				
				if(sdoc != null) { 
					Document resultDoc = tcSearcher.getDoc(sdoc.doc);
		
					String fileName = resultDoc.get("fileName");
					org.w3c.dom.Document doc = dBuilder.parse(new File(Indexer.DATA_DIR + fileName + ".xml"));
					
					response.Documents[i].Score = sdoc.score;
					response.Documents[i].Url = resultDoc.get("url");
					response.Documents[i].Title = doc.getElementsByTagName("Title").item(0).getTextContent();
					response.Documents[i].Content = doc.getElementsByTagName("Content").item(0).getTextContent();
					response.Documents[i].Number = j;
				}
			}
			
			response.TotalTime = System.currentTimeMillis() - start;
			
			return response;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@ModelAttribute
	public void setVaryResponseHeader(HttpServletResponse response) {
		response.setHeader("Access-Control-Allow-Origin", "*");
	}

}
