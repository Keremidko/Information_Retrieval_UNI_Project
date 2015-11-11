package com.project.informationRetrieval.indexer;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory; //org.apache.lucene.analysis.standard
import org.apache.lucene.util.Version;
import org.apache.lucene.analysis.standard.*;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.Field.TermVector;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Indexer {

	// Directory where documents come from
	public static final String DATA_DIR = "D:\\Programming\\Java_64_Workspace\\informationRetrieval\\Corpus\\Docs\\";
	// Directory where index is stored
	public static final String INDEX_DIR = "D:\\Programming\\Java_64_Workspace\\informationRetrieval\\Corpus\\Index\\";
	private IndexWriter indexWriter;

	public static StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_46);
	
	public Indexer() throws IOException {
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_46,
				analyzer);

		indexWriter = new IndexWriter(FSDirectory.open(new File(INDEX_DIR)),
				config);
	}

	public void startIndexing() throws Exception {
		File[] files = new File(DATA_DIR).listFiles();
		
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		
		double segmentSize = files.length/100;
		double startPrec = segmentSize;
		
		for(int i = 0 ; i < files.length ; i++)  {
			File file = files[i];
			
			org.w3c.dom.Document doc = dBuilder.parse(file);
			addFileToIndex(doc);
			
			if(i > startPrec) { 
				System.out.println((startPrec / segmentSize) + " % done.");
				startPrec += segmentSize;
			}
		}
		
		indexWriter.close();
	}
	
	@SuppressWarnings("deprecation")
	private void addFileToIndex(org.w3c.dom.Document doc) throws IOException { 
		//Extracting info from XML document
		String title = doc.getElementsByTagName("Title").item(0).getTextContent();
		String content = doc.getElementsByTagName("Content").item(0).getTextContent();
		String url = doc.getElementsByTagName("Url").item(0).getTextContent();
		
		//Field types and properties
		Field fileNameField = new Field("fileName", title, Field.Store.YES, Field.Index.NO, TermVector.NO);
		Field titleField = new Field("title", title, Field.Store.NO, Field.Index.ANALYZED, TermVector.WITH_POSITIONS_OFFSETS);
		Field contentField = new Field("content", content, Field.Store.NO, Field.Index.ANALYZED, TermVector.WITH_POSITIONS_OFFSETS);
		Field urlField = new Field("url", url, Field.Store.YES, Field.Index.NOT_ANALYZED, TermVector.NO);
		
		//Boosting
		titleField.setBoost(0.6f);
		contentField.setBoost(0.4f);
		
		//Adding the fields to the lucene Document
		Document luceneDoc = new Document();
		luceneDoc.add(fileNameField);
		luceneDoc.add(titleField);
		luceneDoc.add(contentField);
		luceneDoc.add(urlField);
		
		indexWriter.addDocument(luceneDoc);
	}
}
