/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.excel.extractor;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.base.office.excel.ExcelContext;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.filter.PatternFilter;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;

/**
 * Extract the level of the element from only one cell by pattern matching.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class ExcelStructureOneColumnExtractor<C extends ExcelStructureOneColumnExtractor.ColumnConfig> extends AbstractExcelStructureExtractor<C> {

    public interface Config<C extends ExcelStructureOneColumnExtractor.ColumnConfig> extends AbstractExcelStructureExtractor.Config<C> {

		/**
		 * Configuration of the columns, indexed by the level of the column.
		 */
		@Key(C.LEVEL_ATTRIBUTE)
        Map<Object, C> getColumns();
    }

    public interface ColumnConfig extends AbstractExcelStructureExtractor.ColumnConfig {

        @Mandatory
        String getPattern();
    }

    private Map<C, PatternFilter> patternMap;

	private final Map<Object, C> _columns;

    /** 
     * Creates a {@link ExcelStructureOneColumnExtractor}.
     */
    public ExcelStructureOneColumnExtractor(InstantiationContext aContext, Config<C> aConfig) {
        super(aContext, aConfig);
		_columns = aConfig.getColumns();
        this.patternMap = new HashMap<>();

        for (C theColumn : aConfig.getColumns().values()) {
            this.patternMap.put(theColumn, new PatternFilter(theColumn.getPattern()));
        }
    }

	@Override
	public Map<Object, C> getColumns() {
		return _columns;
	}

	@Override
    protected C getColumnConfig(ExcelContext aContext) {
        for (C theColumn : this.getColumns().values()) {
            String theValue = aContext.getString(theColumn.getName());

            if (this.matches(theColumn, theValue)) {
                return theColumn;
            }
        }

        return null;
    }

    private boolean matches(C aLevel, String aValue) {
        if (!StringServices.isEmpty(aValue)) {
            return this.patternMap.get(aLevel).accept(aValue);
        }
        else { 
            return false;
        }
    }
}

