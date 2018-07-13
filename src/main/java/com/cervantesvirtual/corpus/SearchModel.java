package com.cervantesvirtual.corpus;

import java.util.List;

import org.apache.lucene.search.similarities.BasicStats;

public class SearchModel {

    private String queryString;
    private BasicStats stats;
    private int hitsPerPage;
    private List<ResultItem> hits;
    private int fragmentNumber;
    private int fragmentSize;
    
    public SearchModel(){
    	hitsPerPage = 10;
    	fragmentNumber = 5;
    	fragmentSize = 75;
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

	public List<ResultItem> getHits() {
		return hits;
	}

	public void setHits(List<ResultItem> hits) {
		this.hits = hits;
	}

	public int getFragmentNumber() {
		return fragmentNumber;
	}

	public void setFragmentNumber(int fragmentNumber) {
		this.fragmentNumber = fragmentNumber;
	}

	public int getFragmentSize() {
		return fragmentSize;
	}

	public void setFragmentSize(int fragmentSize) {
		this.fragmentSize = fragmentSize;
	}

}