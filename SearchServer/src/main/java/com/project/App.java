package com.project;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ComponentScan;

import com.project.informationRetrieval.crawler.*;
import com.project.informationRetrieval.indexer.*;
import com.project.informationRetrieval.searcher.*;

@ComponentScan
@EnableAutoConfiguration
public class App 
{
    public static void  main( String[] args ) throws Exception
    {
    	SpringApplication.run(App.class, args);
    }
    
    private static void startSearch() throws ParseException, IOException { 
    	Searcher srch = new Searcher();
    	TopDocs result = srch.executeQuery("Zuckerberg", 1, 20);
    	for(ScoreDoc scoreDoc : result.scoreDocs) { 
    		Document document = srch.getDoc(scoreDoc.doc);
    		System.out.println(document.get("fileName"));
    	}
    }
    
    private static void startCrawling() throws Exception { 
    	CrawlerController cController = new CrawlerController();
        cController.start();
        
        System.out.println("Crawling done");
    }
    
    private static void startIndexing() throws Exception { 
    	Indexer i = new Indexer();
    	i.startIndexing();
    	
    	System.out.println("Indexing done");
    }
}
