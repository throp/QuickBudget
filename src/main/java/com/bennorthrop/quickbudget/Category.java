package com.bennorthrop.quickbudget;

import java.util.List;
import java.util.stream.Collectors;

public class Category {

	int order;

	String name;

	String code;

	List<String> rules;

	CategoryType type;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getRules() {
		return rules;
	}

	public void setRules(List<String> rules) {
		this.rules = rules;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public CategoryType getType() {
		return type;
	}

	public void setType(CategoryType type) {
		this.type = type;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public List<Rule> initRules() {
		return rules.stream().map(r -> new Rule(code, r)).collect(Collectors.toList());
	}
}
