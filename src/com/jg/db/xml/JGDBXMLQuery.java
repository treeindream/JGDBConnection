package com.jg.db.xml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom.Element;
import org.jdom.Text;

import com.jg.db.JGDBKeyword;
import com.jg.db.vo.JGDBQuery;
import com.jg.db.xml.cond.JGDBXMLQueryConditionDef;
import com.jg.vo.JGDataset;


public class JGDBXMLQuery{
	protected Element _queryElement = null;
	public Element getQueryElement(){
		return _queryElement;
	}
	
	protected JGDBXMLQuerySet _parent = null;
	protected void setParent(JGDBXMLQuerySet querySet_){
		_parent = querySet_;
	}
	public JGDBXMLQuerySet getParent(){
		return _parent;
	}
	
	protected String _keyName = null;
	public String getKeyName(){
		return _keyName;
	}
	
	protected ArrayList<Element> _conditionList = new ArrayList<Element>();
	public ArrayList<Element> getConditionList(){
		return _conditionList;
	}
	
	protected JGDBXMLQuery(){}
	protected JGDBXMLQuery(Element queryElement_){
		_keyName = queryElement_.getAttributeValue(JGDBKeyword.STR_ATTR_KEYNAME);
		_queryElement = queryElement_;
	}
	
	protected String createPrototypeQueryString(JGDataset dataset_, int rowIndex_) throws Exception{
		JGDBXMLQueryManager sharedDBXMLManager_ = JGDBXMLQueryManager.sharedManager();
		
		//get original xml string
		Element copiedQueryElement_ = (Element)_queryElement.clone();
		
		//convert condition node to statement
		Iterator<String> conditionNames_ = sharedDBXMLManager_._conditionDefs.keySet().iterator();
		while(conditionNames_.hasNext()){
			String conditionKeyName_ = conditionNames_.next();
			JGDBXMLQueryConditionDef conditionDef_ = sharedDBXMLManager_.getConditionDef(conditionKeyName_);
			
			List<?> condElementList_ = copiedQueryElement_.getChildren(conditionKeyName_);
			
			while(condElementList_.size() > 0){
				Element condElement_ = (Element)condElementList_.get(0);
				int targetIndex_ = copiedQueryElement_.indexOf(condElement_);
				String conditionStatement_ = null;
				
				if(conditionDef_.acceptConditionStatement(condElement_, dataset_, rowIndex_)){
					conditionStatement_ = conditionDef_.getStatement(condElement_).trim();
				}
				
				copiedQueryElement_.removeContent(targetIndex_);
				if(conditionStatement_ != null){
					copiedQueryElement_.addContent(targetIndex_, new Text(conditionStatement_));
				}
			}
		}
		
		String queryStr_ = copiedQueryElement_.getValue();
		
		//replace other imported query data
		Pattern regexpImportPattern_ = Pattern.compile(JGDBKeyword.STR_REGEXP_IMPORT);
		Matcher regexpImportMatcher_ = regexpImportPattern_.matcher(queryStr_);

		while(regexpImportMatcher_.find()){
			String matchedStr_ = regexpImportMatcher_.group();
			String importKeyName_ = matchedStr_.substring(5, matchedStr_.length()-1);
			String[] convertedKeyNames_ = importKeyName_.split(",");
			JGDBXMLQuery importedXMLQuery_ = null;
			if(convertedKeyNames_.length >= 2){
				importedXMLQuery_ = JGDBXMLQueryManager.sharedManager().getQuery(convertedKeyNames_[0], convertedKeyNames_[1]);
			}else{
				importedXMLQuery_ = _parent.getQuery(importKeyName_);
			}
			
			if(importedXMLQuery_ == null){
				throw new Exception("imported query not found");
			}
			
			queryStr_ = queryStr_.replaceAll(JGDBKeyword.makeRegexpImport(importKeyName_), importedXMLQuery_.createPrototypeQueryString(dataset_, rowIndex_));
		}
		
		return queryStr_;
	}
	
	public JGDBQuery createQuery(JGDataset dataset_, int rowIndex_) throws Exception{
		JGDBQuery query_ = new JGDBQuery();
		
		String queryStr_ = createPrototypeQueryString(dataset_, rowIndex_);
		
		//convert regexp to column data
		Pattern regexpColumnPattern_ = Pattern.compile(JGDBKeyword.STR_REGEXP_COLUMN);
		Matcher regexpColumnMatcher_ = regexpColumnPattern_.matcher(queryStr_);
		
		while(regexpColumnMatcher_.find()){
			String matchedStr_ = regexpColumnMatcher_.group(); 
			String keyNames_ = matchedStr_.substring(2, matchedStr_.length()-1);
			String columnName_ = null;
			String[] convertedKeyNames_ = keyNames_.split(",");
			
			boolean doParameterize_ = true;
			if(convertedKeyNames_.length >= 2){
				doParameterize_ = Boolean.valueOf(convertedKeyNames_[1]).booleanValue();
				columnName_ = convertedKeyNames_[0];
			}else{
				columnName_ = keyNames_;
			}
			
			if(doParameterize_){
				queryStr_ = queryStr_.replaceFirst(JGDBKeyword.makeRegexpColumn(columnName_), "?");
				query_.addParameter(dataset_.getColumnValue(columnName_, rowIndex_));
			}else{
				queryStr_ = queryStr_.replaceFirst(JGDBKeyword.makeRegexpColumn(keyNames_), String.valueOf(dataset_.getColumnValue(columnName_, rowIndex_)));
			}
		}
		
		query_.setQuery(queryStr_.trim());
		
		return query_;
	}
	
	public JGDBQuery createQuery(JGDataset dataSet_) throws Exception{
		return createQuery(dataSet_, 0);
	}
	
	public JGDBQuery createQuery() throws Exception{
		return createQuery(new Object[]{});
	}
	
	/**
	 * 
	 * @param columnNamesAndValues_ ({"key1", value1, "key2", value2,...})
	 * @param keyColumns_ ({"key1","key2",...})
	 * @return
	 */
	public JGDBQuery createQuery(Object[] columnNamesAndValues_, String[] keyColumns_) throws Exception{
		JGDataset condDataset_ = new JGDataset();
		condDataset_.addRow();
		
		int columnCount_ = columnNamesAndValues_.length;
		for(int columnIndex_=0;columnIndex_<columnCount_;columnIndex_ += 2){
			String columnName_ = (String)columnNamesAndValues_[columnIndex_];
			Object columnValue_ = columnNamesAndValues_[columnIndex_+1];
			
			condDataset_.addColumn(columnName_);
			condDataset_.setColumnValue(columnName_, 0, columnValue_);
		}
		
		if(keyColumns_ != null){
			int keyCount_ = keyColumns_.length;
			for(int keyIndex_=0;keyIndex_<keyCount_;++keyIndex_){
				condDataset_.setKeyColumn(keyColumns_[keyIndex_], true);
			}
		}
		
		return createQuery(condDataset_,0);
	}
	
	/**
	 * 
	 * @param columnNamesAndValues_ ({"key1", value1, "key2", value2,...})
	 * @return {@link JGDBXMLQuery}
	 */
	public JGDBQuery createQuery(Object[] columnNamesAndValues_) throws Exception{
		return createQuery(columnNamesAndValues_, null);
	}
}
