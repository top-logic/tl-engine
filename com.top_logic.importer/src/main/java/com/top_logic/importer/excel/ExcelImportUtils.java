/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.excel;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.top_logic.base.office.excel.ExcelContext;
import com.top_logic.basic.StringServices;
import com.top_logic.importer.ImportUtils;
import com.top_logic.importer.excel.AbstractExcelFileImportParser.MappingConfig;
import com.top_logic.importer.excel.extractor.Extractor;
import com.top_logic.importer.excel.transformer.TransformException;
import com.top_logic.importer.logger.ImportLogger;
import com.top_logic.importer.logger.ImportMessageLogger;

/**
 * Some utility methods for excel based importer.
 * 
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class ExcelImportUtils extends ImportUtils {

	/**
	 * Comment for <code>CHANNEL_KEY_PREFIX</code>
	 */
	private static final String CHANNEL_KEY_PREFIX = "_@";

    public static final ExcelImportUtils INSTANCE = new ExcelImportUtils();

    /**
	 * Evaluates the given {@link MappingConfig}s with the given  {@link ExcelContext} and
	 * creates a map with the values. Any problems are logged into the given {@link ImportLogger}.
	 * After that, the given extractors are performed. 
	 * 
	 * @return map with the values
	 */
	public  Map<String, Object> doExtractValues(ExcelContext aContext, ImportLogger logger, Collection<MappingConfig> mappings, Collection<Extractor> extractors, AbstractExcelFileImportParser<?> handler) {
	    Map<String, Object> theMap = this.evaluateMappings(aContext, logger, mappings, handler);

	    theMap.putAll(this.evaluateExtractors(aContext, logger, extractors, handler));
        
        return theMap;
    }

	/**
	 * Evaluates the given {@link MappingConfig}s with the given  {@link ExcelContext} and
	 * creates a map with the values. Any problems are logged into the given {@link ImportLogger}
	 * 
	 * @return map with the values
	 */
	public Map<String, Object> evaluateMappings(ExcelContext aContext, ImportLogger aLogger, Collection<MappingConfig> someMappings, AbstractExcelFileImportParser<?> aHandler) {
		Map<String, Object> theResult = new HashMap<>();

        for (MappingConfig theMapping : someMappings) {

        	try{
                Object theValue = theMapping.getTransformer().transform(aContext, theMapping.getName(), aHandler, aLogger);

                this.storeValue(theMapping, theResult, theValue);
        	}
        	catch(TransformException ex) {
        		ImportMessageLogger theLogger = this.getImportMessageLogger(aLogger);

        		if (theLogger != null) {
        			if (theMapping.isExceptionTolerated()) {
        				theLogger.logTransformExceptionAsWarn(aHandler, ex);
        			}
        			else {
        				theLogger.logTransformException(aHandler, ex);
        			}
        		}
        	}
        }

        return theResult;
	}

    /** 
     * Store the given value in the map.
     * 
     * @param aMapping
     *        Additional information about <code>null</code> handling.
     * @param aResult
     *        Map to store the value in.
     * @param aValue
     *        Value to be stored.
     * @see   MappingConfig#isAddNullValue()
     */
	@SuppressWarnings("unchecked")
    public void storeValue(MappingConfig aMapping, Map<String, Object> aResult, Object aValue) {
		Map<String, Object> valueMap = aResult;
		final String channel = aMapping.getChannel();
		if (!StringServices.isEmpty(channel)) {
			final String channelKey = buildChannelKey(channel);
			valueMap = (Map<String, Object>) aResult.get(channelKey);
			if (valueMap == null) {
				valueMap = new HashMap<>();
				aResult.put(channelKey, valueMap);
			}
		}
        String theKey = aMapping.getAttribute();

        if (aValue == null) {
        	 if (aMapping.isAddNullValue()) {
				valueMap.put(theKey, aValue);
        	 }
        }
        else {
			valueMap.put(theKey, aValue);
        }
    }

	private final String buildChannelKey(String channelId) {
		return new StringBuilder(CHANNEL_KEY_PREFIX).append(channelId).toString();
	}

	public final Map<String, Object> getValuesOnChannel(Map<String, Object> someValues, String channelId) {
		return (Map<String, Object>) someValues.get(buildChannelKey(channelId));
	}

    public Map<String, Object> evaluateExtractors(ExcelContext aContext, ImportLogger aLogger, Collection<Extractor> someExtractors, AbstractExcelFileImportParser<?> aHandler) {
        Map<String, Object> theMap = new HashMap<>();

        for (Extractor theExtractor : someExtractors) {
            Map<String, Object> theValues = theExtractor.extract(aContext, aHandler, aLogger);

            if (theValues != null){
                theMap.putAll(theValues);
            }
        }

        return theMap;
    }
}
