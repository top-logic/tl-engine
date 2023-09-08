/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.excel.extractor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.base.office.POIUtil;
import com.top_logic.base.office.excel.ExcelContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.importer.excel.AbstractExcelFileImportParser;
import com.top_logic.importer.excel.ExcelImportUtils;
import com.top_logic.importer.excel.transformer.Transformer;
import com.top_logic.importer.logger.ImportLogger;

/**
 * Extract a cell values by using the configured {@link Transformer} in the mapping configurations.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class CellValueExtractor implements Extractor {

	/**
	 * Extractor can handle several cells. 
	 * 
	 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
	public interface Config extends Extractor.Config {

        /** The mapping from excel columns to attribute values. */
	    @Name("mappings")
        List<MappingConfig> getMappings();
	}

    /**
     * Mapping will contain the cell name here. 
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public interface MappingConfig extends AbstractExcelFileImportParser.MappingConfig {

        /** Excel version to define a cell. */
        String getCell();
    }

	private Config config;

	/** 
	 * Creates a {@link CellValueExtractor}.
	 */
	public CellValueExtractor(InstantiationContext aContext, Config aConfig) {
		this.config = aConfig;
	}

	@Override
	public Map<String, Object> extract(ExcelContext aContext, AbstractExcelFileImportParser<?> aParser, ImportLogger aLogger) {
	    int                  theRow    = aContext.row();
	    Map<String, Object>  theResult = new HashMap<>();

	    for (MappingConfig theMapping : this.config.getMappings()) {
	        this.extractValues(aContext, aParser, aLogger, theMapping, theResult);
	    }

	    aContext.row(theRow);

	    return theResult.isEmpty() ? null : theResult;
	}

    private void extractValues(ExcelContext aContext, AbstractExcelFileImportParser<?> aParser, ImportLogger aLogger, MappingConfig aMapping, Map<String, Object> aResult) {
        String theName   = aMapping.getName();
        String theCell   = aMapping.getCell();
        int    theRow    = POIUtil.getRow(theCell);
        int    theColumn = POIUtil.getColumn(theCell);

        aContext.getColumnMap().put(theName, theColumn);
        aContext.row(theRow);

        Object theValue = aMapping.getTransformer().transform(aContext, theName, aParser, aLogger);

        ExcelImportUtils.INSTANCE.storeValue(aMapping, aResult, theValue);
    }
}
