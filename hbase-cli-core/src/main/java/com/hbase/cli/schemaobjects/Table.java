package com.hbase.cli.schemaobjects;

import java.util.List;

public class Table {

	String tableName, rowkeyDataType;
	Codec codec;
	List<Column> columns;

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getRowkeyDataType() {
		return rowkeyDataType;
	}

	public void setRowkeyDataType(String rowkeyDataType) {
		this.rowkeyDataType = rowkeyDataType;
	}

	public Codec getCodec() {
		return codec;
	}

	public void setCodec(Codec codec) {
		this.codec = codec;
	}

	public List<Column> getColumns() {
		return columns;
	}

	public void setColumns(List<Column> columns) {
		this.columns = columns;
	}

}
