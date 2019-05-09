package com.hbase.cli.api;

/**
 * Interface for Table's encoding/decoding logic
 *
 */
public interface TableCodec {

	/**
	 * Table description
	 * @return
	 */
	public String getTableDescription();
	
	/**
	 * Rowkey description 
	 * * If multiple components in rowkey, each component should be explained
	 * * Order of the rowkey components should be mentioned as well 
	 * @return
	 */
	public String getRowkeyDescription();

	/**
	 * Rowkey encoding logic
	 * @param arg
	 * @return
	 * @throws Exception
	 */
	public byte[] getEncodedRowkey(String arg) throws Exception;

	/**
	 * Rowkey decoding logic
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public String getDecodedRowkey(byte[] args) throws Exception;

	/**Columns encoding logic
	 * * Column will be in format [columnfamily : columnqualifier]
	 * @param column
	 * @param value
	 * @return
	 * @throws Exception
	 */
	public byte[] getEncodedColumns(String column, String value) throws Exception;
	
	/**
	 * Columns decoding logic
	 * * Column will be in format [columnfamily : columnqualifier]
	 * @param column
	 * @param value
	 * @return
	 * @throws Exception
	 */
	public String getDecodedColumns(String column, byte[] value) throws Exception;

}
