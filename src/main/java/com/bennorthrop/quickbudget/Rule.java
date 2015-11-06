package com.bennorthrop.quickbudget;

public class Rule {

	private String categoryCode;

	private String searchString;

	public Rule(String categoryCode, String searchString) {
		this.categoryCode = categoryCode;
		this.searchString = searchString;
	}

	public String getCategoryCode() {
		return categoryCode;
	}

	public String getSearchString() {
		return searchString;
	}
}
