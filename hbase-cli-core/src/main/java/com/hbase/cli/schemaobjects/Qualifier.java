package com.hbase.cli.schemaobjects;

public class Qualifier {

	String name, type, description;
	boolean isFilterable;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isFilterable() {
		return isFilterable;
	}

	public void setFilterable(boolean isFilterable) {
		this.isFilterable = isFilterable;
	}

}
