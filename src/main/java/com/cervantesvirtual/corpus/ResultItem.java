package com.cervantesvirtual.corpus;

import java.util.List;

public class ResultItem {

	private List<String> paragraphs;
	private String title;
	private String author;
	private String slug;
	private String workdata;
	private String wikidata;

	public String getSlug() {
		return slug;
	}
	public void setSlug(String slug) {
		this.slug = slug;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public List<String> getParagraphs() {
		return paragraphs;
	}
	public void setParagraphs(List<String> paragraphs) {
		this.paragraphs = paragraphs;
	}
	public String getWikidata() {
		return wikidata;
	}
	public void setWikidata(String wikidata) {
		this.wikidata = wikidata;
	}
	public String getWorkdata() {
		return workdata;
	}
	public void setWorkdata(String workdata) {
		this.workdata = workdata;
	}
}
