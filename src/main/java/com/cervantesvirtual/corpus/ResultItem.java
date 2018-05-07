package com.cervantesvirtual.corpus;

import java.util.List;

public class ResultItem {

	private List<String> paragraphs;
	private String title;
	private String author;
	

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
}
