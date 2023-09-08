/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.excel;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import com.top_logic.base.office.excel.ExcelContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.importer.base.AbstractImportPerformer;
import com.top_logic.importer.base.ListDataImportPerformer;
import com.top_logic.importer.logger.ImportLogger;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.tool.boundsec.assistent.AssistentComponent;

/**
 * Parse excel files in XLSX, XLS or CSV format and provide a list of read data.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class ExcelListImportParser<C extends ExcelListImportParser.Config> extends AbstractExcelFileImportParser<C> {

    /**
     * Configuration for parsing the excel file. 
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public interface Config extends AbstractExcelFileImportParser.Config {
		// Nothing in here
    }

    /** 
     * Creates a {@link ExcelListImportParser}.
     */
    public ExcelListImportParser(InstantiationContext aContext, C aConfig) {
        super(aContext, aConfig);
    }

    @Override
    protected void preRead(AssistentComponent aComponent) {
        aComponent.setData(ListDataImportPerformer.PROP_KEY_MAPPING, null);
    }

    @Override
    protected List<Map<String, Object>> doRead(ExcelContext aContext, List<Map<String, Object>> aResult, Object aModel, ImportLogger aLogger) throws InvalidFormatException {
        while (aContext.hasMoreRows()) {
            Map<String, Object> theValues = this.parseRow(aContext, aLogger);

            if (theValues != null) {
                if (!theValues.containsKey(AbstractImportPerformer.MODEL)) {
                    theValues.put(AbstractImportPerformer.MODEL, aModel);
                }

                aResult.add(theValues);

                this.postProcessMap(theValues, aContext, aLogger);
            }

            aContext.down();
        }

        this.checkConstraints(aLogger);

        return aResult;
    }

    @Override
    protected void postRead(AssistentComponent aComponent, ExcelContextProvider<?> aProvider) {
        Collection<MappingConfig> theMappings = this.getConfig().getMappings();
		List<String> theIDs = this.getConfig().getUID();
        
        aComponent.setData(ListDataImportPerformer.PROP_KEY_MAPPING, ListDataImportPerformer.getIDsFromMapping(theMappings, theIDs));
    }

    /**
     * Parse the row contents at the given context's position.
     * 
     * @param aContext
     *        The {@link ExcelContext} to be used for excel data access
     * @param aLogger
     *        Messages for information to the user.
     */
    private Map<String, Object> parseRow(ExcelContext aContext, ImportLogger aLogger) {
        if (!this.isEmptyRow(aContext, aLogger)) {
            Map<String, Object> theMap = this.extractValues(aContext, aLogger);

            this.constraintsAware(aContext, aLogger);

            if ((theMap != null) && !theMap.isEmpty()) {
                int    theRow  = aContext.row();
                Object theName = theMap.get(AbstractWrapper.NAME_ATTRIBUTE);

                aLogger.info(this, I18NConstants.ROWS_PROCESSED, theRow + 1, theName);

                return theMap;
            }
        }

        return null;
    }
}
