package com.hbase.cli.schemaobjects;

import java.util.List;

public class Column {

	String familyName;
	long versions;
	List<Qualifier> qualifiers;

	public String getFamilyName() {
		return familyName;
	}

	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}

	public long getVersions() {
		return versions;
	}

	public void setVersions(long versions) {
		this.versions = versions;
	}

	public List<Qualifier> getQualifiers() {
		return qualifiers;
	}

	public void setQualifiers(List<Qualifier> qualifiers) {
		this.qualifiers = qualifiers;
	}

}
