package com.cervantesvirtual.corpus;

import java.util.List;

import org.apache.lucene.search.similarities.BasicStats;

public class SearchModel {

    private String queryString;
    private BasicStats stats;
    private int hitsPerPage;
    private List<String> hits;
    
    public SearchModel(){
    	hitsPerPage = 10;
    }

	public String getQueryString() {
		return queryString;
	}

	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	public BasicStats getStats() {
		return stats;
	}

	public void setStats(BasicStats stats) {
		this.stats = stats;
	}

	public int getHitsPerPage() {
		return hitsPerPage;
	}

	public void setHitsPerPage(int hitsPerPage) {
		this.hitsPerPage = hitsPerPage;
	}

	public List<String> getHits() {
		return hits;
	}

	public void setHits(List<String> hits) {
		this.hits = hits;
	}

}