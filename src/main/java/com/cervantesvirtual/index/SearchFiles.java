package com.cervantesvirtual.index;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermAutomatonQuery;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.search.TokenStreamToTermAutomatonQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.store.FSDirectory;

import com.cervantesvirtual.analyzer.SynonymSearchAnalyzer;
import com.cervantesvirtual.corpus.SearchModel;

/** Simple command-line based search demo. */
public class SearchFiles {

	private String index = "/var/lucene/hibernate/corpusbvmc";
	private String field = "contents";

	public SearchFiles() {
	}

	public SearchModel search(SearchModel searchModel) throws Exception {
		
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
		IndexSearcher searcher = new IndexSearcher(reader);
		
		String queryString = searchModel.getQueryString();
		queryString = queryString.trim();
		
		Term term = new Term(field, queryString);
		searchModel.setStats(getBasicStats(reader, term, 0));
		
		if (queryString.contains("#")){
			
			SynonymSearchAnalyzer analyzer = new SynonymSearchAnalyzer();

			TokenStreamToTermAutomatonQuery q = new TokenStreamToTermAutomatonQuery();
			TermAutomatonQuery autQuery = q.toQuery(field,
					analyzer.tokenStream(field, queryString));
			
			TopDocs hits = searcher.search(autQuery, 10);
			
			searchModel.setHits(highlightResults(reader, searcher, autQuery, hits, new SynonymSearchAnalyzer()));
			
			analyzer.close();
			
		}else{
			Analyzer analyzer = new WhitespaceAnalyzer();
			QueryParser parser = new QueryParser(field, analyzer);
            Query query = parser.parse(queryString);
			System.out.println("Searching for: " + query.toString(field));

			// doPagingSearch(in, searcher, query, hitsPerPage, raw, queries ==
			// null && queryString == null);
			TopDocs hits = searcher.search(query, 5 * searchModel.getHitsPerPage());
			
			searchModel.setHits(highlightResults(reader, searcher, query, hits, new WhitespaceAnalyzer()));

			analyzer.close();
		}
		
		reader.close();
		
		return searchModel;
	}

	/**
	 * This demonstrates a typical paging search scenario, where the search
	 * engine presents pages of size n to the user. The user can then go to the
	 * next page if interested in the next hits.
	 * 
	 * When the query is executed for the first time, then only enough results
	 * are collected to fill result pages. If the user wants to page beyond this
	 * limit, then the query is executed another time and all hits are
	 * collected.
	 * 
	 */
	public static void doPagingSearch(BufferedReader in, IndexSearcher searcher, Query query, int hitsPerPage,
			boolean raw, boolean interactive) throws IOException {

		// Collect enough docs to show pages
		TopDocs results = searcher.search(query, 5 * hitsPerPage);

		ScoreDoc[] hits = results.scoreDocs;

		int numTotalHits = Math.toIntExact(results.totalHits);
		System.out.println(numTotalHits + " total matching documents");

		int start = 0;
		int end = Math.min(numTotalHits, hitsPerPage);

		while (true) {
			if (end > hits.length) {
				System.out.println("Only results 1 - " + hits.length + " of " + numTotalHits
						+ " total matching documents collected.");
				System.out.println("Collect more (y/n) ?");
				String line = in.readLine();
				if (line.length() == 0 || line.charAt(0) == 'n') {
					break;
				}

				hits = searcher.search(query, numTotalHits).scoreDocs;
			}

			end = Math.min(hits.length, start + hitsPerPage);

			for (int i = start; i < end; i++) {
				if (raw) { // output raw format
					System.out.println("doc=" + hits[i].doc + " score=" + hits[i].score);
					continue;
				}

				Document doc = searcher.doc(hits[i].doc);
				String path = doc.get("path");
				if (path != null) {
					System.out.println((i + 1) + ". " + path);
					String title = doc.get("title");
					if (title != null) {
						System.out.println("   Title: " + doc.get("title"));
					}
				} else {
					System.out.println((i + 1) + ". " + "No path for this document");
				}

			}

			if (!interactive || end == 0) {
				break;
			}

			if (numTotalHits >= end) {
				boolean quit = false;
				while (true) {
					System.out.print("Press ");
					if (start - hitsPerPage >= 0) {
						System.out.print("(p)revious page, ");
					}
					if (start + hitsPerPage < numTotalHits) {
						System.out.print("(n)ext page, ");
					}
					System.out.println("(q)uit or enter number to jump to a page.");

					String line = in.readLine();
					if (line.length() == 0 || line.charAt(0) == 'q') {
						quit = true;
						break;
					}
					if (line.charAt(0) == 'p') {
						start = Math.max(0, start - hitsPerPage);
						break;
					} else if (line.charAt(0) == 'n') {
						if (start + hitsPerPage < numTotalHits) {
							start += hitsPerPage;
						}
						break;
					} else {
						int page = Integer.parseInt(line);
						if ((page - 1) * hitsPerPage < numTotalHits) {
							start = (page - 1) * hitsPerPage;
							break;
						} else {
							System.out.println("No such page");
						}
					}
				}
				if (quit)
					break;
				end = Math.min(numTotalHits, start + hitsPerPage);
			}
		}
	}

	public static BasicStats getBasicStats(IndexReader indexReader, Term myTerm, float queryBoost) throws IOException {
		String fieldName = myTerm.field();

		System.out.println("myTerm:" + myTerm.toString());

		CollectionStatistics collectionStats = new CollectionStatistics("field", indexReader.maxDoc(),
				indexReader.getDocCount(fieldName), indexReader.getSumTotalTermFreq(fieldName),
				indexReader.getSumDocFreq(fieldName));

		TermStatistics termStats = new TermStatistics(myTerm.bytes(), indexReader.docFreq(myTerm),
				indexReader.totalTermFreq(myTerm));

		BasicStats myStats = new BasicStats(fieldName, queryBoost);
		assert collectionStats.sumTotalTermFreq() == -1
				|| collectionStats.sumTotalTermFreq() >= termStats.totalTermFreq();
		long numberOfDocuments = collectionStats.maxDoc();

		long docFreq = termStats.docFreq();
		long totalTermFreq = termStats.totalTermFreq();

		if (totalTermFreq == -1) {
			totalTermFreq = docFreq;
		}

		final long numberOfFieldTokens;
		final float avgFieldLength;

		long sumTotalTermFreq = collectionStats.sumTotalTermFreq();

		if (sumTotalTermFreq <= 0) {
			numberOfFieldTokens = docFreq;
			avgFieldLength = 1;
		} else {
			numberOfFieldTokens = sumTotalTermFreq;
			avgFieldLength = (float) numberOfFieldTokens / numberOfDocuments;
		}

		myStats.setNumberOfDocuments(numberOfDocuments);
		myStats.setNumberOfFieldTokens(numberOfFieldTokens);
		myStats.setAvgFieldLength(avgFieldLength);
		myStats.setDocFreq(docFreq);
		myStats.setTotalTermFreq(totalTermFreq);

		/*System.out.println("myStats.getAvgFieldLength():" + myStats.getAvgFieldLength());
		System.out.println("myStats.numberOfFieldTokens():" + myStats.getNumberOfFieldTokens());
		System.out.println("myStats.NumberOfDocuments():" + myStats.getNumberOfDocuments());
		System.out.println("myStats.docFreq():" + myStats.getDocFreq());
		System.out.println("myStats.getTotalTermFreq():" + myStats.getTotalTermFreq());*/

		return myStats;
	}

	public static List<String> highlightResults(IndexReader reader, IndexSearcher searcher, Query query, TopDocs hits,
			Analyzer analyzer) throws IOException, InvalidTokenOffsetsException {

		List<String> results = new ArrayList<String>();
		// Uses HTML &lt;B&gt;&lt;/B&gt; tag to highlight the searched terms
		Formatter formatter = new SimpleHTMLFormatter();

		// It scores text fragments by the number of unique query terms found
		// Basically the matching score in layman terms
		QueryScorer scorer = new QueryScorer(query);

		// used to markup highlighted terms found in the best sections of a text
		Highlighter highlighter = new Highlighter(formatter, scorer);

		// It breaks text up into same-size texts but does not split up spans
		Fragmenter fragmenter = new SimpleSpanFragmenter(scorer, 100);

		// breaks text up into same-size fragments with no concerns over
		// spotting sentence boundaries.
		// Fragmenter fragmenter = new SimpleFragmenter(10);

		// set fragmenter to highlighter
		highlighter.setTextFragmenter(fragmenter);

		// Iterate over found results
		for (int i = 0; i < hits.scoreDocs.length; i++) {
			int docid = hits.scoreDocs[i].doc;
			Document doc = searcher.doc(docid);
			String title = doc.get("path");

			// Printing - to which document result belongs
			System.out.println("Path " + " : " + title);

			// Get stored text from found document
			String text = doc.get("contents");

			// Create token stream
			TokenStream stream = TokenSources.getAnyTokenStream(reader, docid, "contents", analyzer);

			// Get highlighted text fragments
			String[] frags = highlighter.getBestFragments(stream, text, 20);
			for (String f : frags) {
				results.add(f);
			}
		}
		
		return results;
	}
}
