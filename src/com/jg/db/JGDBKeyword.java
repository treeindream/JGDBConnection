package com.jg.db;

public class JGDBKeyword {
	static public final String STR_ELEMENT_QUERYSET = "queryset";
	static public final String STR_ELEMENT_QUERY = "query";
	static public final String STR_ELEMENT_ISNOTNULL = "isnotnull";
	static public final String STR_ELEMENT_ISEQUALS = "isequals";
	
	static public final String STR_ATTR_KEYNAME = "keyName";
	
	static public final String STR_ATTR_COLUMNNAME = "columnName";
	static public final String STR_ATTR_COLUMNVALUE = "columnValue";
	static public final String STR_ATTR_ISREVERSE = "isReverse";
	
	static public final String STR_FORMAT_COND_STATEMENT = "####%d####";
	static public final String STR_FORMAT_COND_COLUMN = "##%@##";
	
	static public final String STR_REGEXP_COLUMN = "\\#\\{[\\w\\-\\.]+[\\,]*(true|false)*\\}";
	static public final String STR_REGEXP_IMPORT = "\\#imp\\{[\\w\\-\\.]+[\\,]*[\\w\\-\\.]+\\}";
	
	static public final String makeRegexpColumn(String columnName_){
		return "\\#\\{("+columnName_+")\\}";
	}
	static public final String makeRegexpImport(String keyName_){
		return "\\#imp\\{("+keyName_+")\\}";
	}
}
