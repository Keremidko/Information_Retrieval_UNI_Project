package com.project.informationRetrieval.searcher;

import com.project.informationRetrieval.indexer.Indexer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.codec.language.Metaphone;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.spell.LevensteinDistance;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;
import org.apache.lucene.index.DirectoryReader;

public class Searcher {

	private IndexSearcher searcher;

	private HashSet<String> uniqueTerms;

	public Searcher() throws IOException {
		// Init the searcher
		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(
				Indexer.INDEX_DIR)));
		searcher = new IndexSearcher(reader);

		// Init uniqueTerms
		uniqueTerms = new HashSet<String>();
		Fields fields = MultiFields.getFields(reader);
		for (String field : fields) {
			Terms terms = fields.terms(field);
			TermsEnum termsEnum = terms.iterator(null);
			while (termsEnum.next() != null) {
				BytesRef bytesTerm = termsEnum.term();
				String utfString = bytesTerm.utf8ToString();
				uniqueTerms.add(utfString);
			}
		}

	}

	public TopDocs executeQuery(String stringQuery, int pageNumber,
			int resultsPerPage) throws ParseException, IOException {
		QueryParser qParser = new QueryParser(Version.LUCENE_46, "content",
				Indexer.analyzer);
		Query q = qParser.parse(stringQuery);

		TopDocs totalDocs = searcher.search(q, pageNumber * resultsPerPage);
		ScoreDoc[] scoreDocsResult = new ScoreDoc[resultsPerPage];

		// Pagination
		for (int i = resultsPerPage * (pageNumber - 1), j = 0; i < resultsPerPage
				* pageNumber
				&& i < totalDocs.totalHits; j++, i++) {
			if (totalDocs.scoreDocs[i] != null)
				scoreDocsResult[j] = totalDocs.scoreDocs[i];
		}

		TopDocs result = new TopDocs(totalDocs.totalHits, scoreDocsResult, 1.0f);

		return result;
	}

	public String correctQuery(String q) {
		String[] terms = q.split(" ");

		String newQuery = "";

		for (String term : terms) {
			if(term.equals(""))
				continue;
			newQuery += findNearestTerm(term) + " ";
		}
		
		return newQuery.substring(0, newQuery.length()-1);
	}

	private String findNearestTerm(String term) {
		if (uniqueTerms.contains(term)) {
			return term;
		}
		else {

			LevensteinDistance ltDistance = new LevensteinDistance();

			Iterator<String> it = uniqueTerms.iterator();
			String next = it.next();

			String bestMatch = next;
			float bestMatchScore = ltDistance.getDistance(term, next);

			try {
				while ((next = it.next()) != null) {
					float current = ltDistance.getDistance(term, next);
					if (current > bestMatchScore ) {
						bestMatchScore = current;
						bestMatch = next;
					}
				}
			} catch (Exception e) {
				return bestMatch;
			}

			return bestMatch;
		}
	}

	public Document getDoc(int docID) throws IOException {
		return searcher.doc(docID);
	}

	public Object[] suggestComplete(String s) {
		Iterator<String> it = uniqueTerms.iterator();

		List<String> suggestions = new ArrayList<String>();
		String next = it.next();

		try {
			while ((next = it.next()) != null) {
				if (next.length() < s.length())
					continue;

				String crop = next.substring(0, s.length());

				if (s.equalsIgnoreCase(crop))
					suggestions.add(next);
			}
		} catch (Exception e) {
			return suggestions.toArray();
		}

		return suggestions.toArray();
	}
}
