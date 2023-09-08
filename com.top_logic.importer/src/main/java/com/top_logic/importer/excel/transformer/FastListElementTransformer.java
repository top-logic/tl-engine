/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.excel.transformer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.base.office.excel.ExcelContext;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.MapBinding;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.CharDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourceTransaction;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.importer.excel.AbstractExcelFileImportParser;
import com.top_logic.importer.logger.ImportLogger;
import com.top_logic.knowledge.gui.layout.list.FastListElementLabelProvider;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.list.FastList;
import com.top_logic.knowledge.wrap.list.FastListElement;
import com.top_logic.model.util.TLModelNamingConvention;
import com.top_logic.util.Resources;

/**
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class FastListElementTransformer implements Transformer<Collection<FastListElement>> {
	protected boolean _mandatory;
	protected boolean _checkColumn;
	protected boolean _create;
	protected String  _fallBackName;
	protected String  _listName;
	protected char    _separator;
	protected Map<String, String> _mappings;
	
    /** Cache Options {@link FastListElement}s by their I18N name.  */
    protected Map<String, FastListElement> optionMap;

	public interface Config extends Transformer.Config {
		@MapBinding(key="value",attribute="element")
		Map<String, String> getMappings();
		
		@CharDefault(',')
		char getSeparator();
		
		String getListName();

        String getFallbackName();

		@BooleanDefault(false)
		boolean isCreate();
		
		@BooleanDefault(true)
		boolean isCheckColumnName();
	}
	
	public FastListElementTransformer(InstantiationContext context, Config config){
		_mandatory    = config.isMandatory();
		_checkColumn  = config.isCheckColumnName();
		_create       = config.isCreate();
		_mappings     = config.getMappings();
		_fallBackName = config.getFallbackName();
		_listName     = config.getListName();
		_separator    = config.getSeparator();
	}
    
    public FastListElementTransformer(boolean mandatory, boolean checkColumnName, boolean create, Map<String, String> mappings, String fallBackName, String listName, char separator) {
        _mandatory    = mandatory;
        _checkColumn  = checkColumnName;
        _create       = create;
        _mappings     = mappings;
        _fallBackName = fallBackName;
        _listName     = listName;
        _separator    = separator;
    }

	@Override
	public Collection<FastListElement> transform(ExcelContext aContext, String columnName, AbstractExcelFileImportParser<?> handler, ImportLogger logger) {
		if (_checkColumn && !aContext.hasColumn(columnName)) {
			return null;
		}

		String key = this.getStringValue(aContext, columnName, logger);
		Collection<FastListElement> res = null;
		
		if (key!= null) {
		    try {
		        res = getFLE(key, logger, aContext, columnName);
		    }
		    catch (TransformException ex) {
		        String theDefault = _fallBackName;

		        if (!StringServices.isEmpty(theDefault)) {
		            res = getFLE(theDefault, logger, aContext, columnName);
		        }
		        else {
		            throw ex;
		        }
		    }
		}

		if (this._mandatory && (res == null || (res.isEmpty()))) {
			throw new TransformException(I18NConstants.VALUE_EMPTY, aContext.row() + 1, columnName, key);
		}

		return (res != null) ? res : Collections.<FastListElement>emptyList();
	}

    protected String getStringValue(ExcelContext aContext, String columnName, ImportLogger logger) {
        return StringTransformer.getString(aContext, columnName, false, logger);
    }
	
	protected Collection<FastListElement> getFLE(String key, ImportLogger logger, ExcelContext aContext, String aColumn) {
		List<FastListElement> theResult = new ArrayList<>();
		for (String theKey : StringServices.toList(key, _separator)) {
			String fastListElementName = _mappings.get(theKey);
			FastListElement fastListElement = null;
			if (fastListElementName != null) {
				fastListElement = FastListElement.getElementByName(fastListElementName);
			}
			if (fastListElement == null) {
				fastListElement = this.getFastListElement(theKey, this._listName, this._create);
			}
			if (fastListElement != null) {
				if (!theResult.contains(fastListElement)) {
					theResult.add(fastListElement);
				}
				else {
					throw new TransformException(I18NConstants.FLE_NOT_FOUND, aContext.row() + 1, aColumn, fastListElementName);
				}
			}
			else {
				throw new TransformException(I18NConstants.FLE_NO_MAPPING, aContext.row() + 1, aColumn, theKey);
			}
		}
		
		return theResult;
	}
	
	public FastListElement getFastListElement(String aValue) {
	    return this.getFastListElement(aValue, this._listName, this._create);
	}

    protected FastListElement getFastListElement(String aValue, String aListName, boolean create) {
    	if (StringServices.isEmpty(aValue)) {
    		return null;
    	}
    	
        Map<String, FastListElement> theOptions = this.getOptions(aListName);
		FastListElement              theResult  = theOptions.get(aValue.toLowerCase());

        if (create && theResult == null) {
            FastList              theList    = FastList.getFastList(aListName);
            if (theList == null) {
            	return null;
            }
			try (Transaction transaction = theList.tKnowledgeBase().beginTransaction()) {
				String theID = theList.getName() + '.' + Integer.toString(theList.size());
				FastListElement theElement = theList.addElement(null, theID, "", 0);
				try (ResourceTransaction tx = ResourcesModule.getInstance().startResourceTransaction()) {
					for (String theLang : ResourcesModule.getInstance().getSupportedLocaleNames()) {
						tx.saveI18N(theLang, TLModelNamingConvention.resourceKey(theElement), aValue);
					}

					tx.commit();
				}

				theResult = theElement;
				try {
					transaction.commit();
				} catch (KnowledgeBaseException e) {
					e.printStackTrace();
				}
            }
            theOptions.put(aValue.toLowerCase(), theResult);
        }

        return theResult;
    }
    
    protected Map<String, FastListElement> getOptions(String aListName) {
        if (this.optionMap == null) {
            Map<String, FastListElement> theResult = new HashMap<>();

			{
            	FastList theList = FastList.getFastList(aListName);

            	if (theList != null) {
            	    Resources theRes = Resources.getInstance();

            	    for (FastListElement theElement : theList.elements()) {
	                    ResKey theName = FastListElementLabelProvider.labelKey(theElement);

	                    theResult.put(theRes.getString(theName).toLowerCase(), theElement);
	                }
            	}
            }

            this.optionMap = theResult;
        }

        return this.optionMap;
    }

    public static class SingleFastListElementTransformer implements Transformer<FastListElement> {

        public interface Config extends FastListElementTransformer.Config {
            
        }

        private FastListElementTransformer transformer;

        // Constructors
        
        /** 
         * Creates a {@link Integer2FastListElementTransformer}.
         */
        public SingleFastListElementTransformer(InstantiationContext aContext, Config aConfig) {
            this.transformer = new FastListElementTransformer(aContext, aConfig);
        }

        @Override
        public FastListElement transform(ExcelContext aContext, String aColumnName, AbstractExcelFileImportParser<?> aParser, ImportLogger aLogger) {
            Collection<FastListElement> theValue = this.transformer.transform(aContext, aColumnName, aParser, aLogger);

            return CollectionUtil.getSingleValueFromCollection(theValue);
        }
    }

    public static class Integer2FastListElementTransformer extends FastListElementTransformer {

	    // Constructors
        
        /** 
         * Creates a {@link Integer2FastListElementTransformer}.
         */
        public Integer2FastListElementTransformer(InstantiationContext aContext, Config aConfig) {
            super(aContext, aConfig);
        }

        @Override
	    protected String getStringValue(ExcelContext aContext, String aColumnName, ImportLogger logger) {
	        return Integer2StringTransformer.getString(aContext, aColumnName, true,_mandatory, logger);
	    }
	}
}
