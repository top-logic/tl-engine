/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.excel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.base.office.excel.ExcelContext;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.importer.ImportUtils;
import com.top_logic.importer.base.AbstractImportPerformer;
import com.top_logic.importer.base.ListDataImportPerformer;
import com.top_logic.importer.excel.transformer.TransformException;
import com.top_logic.importer.excel.transformer.Transformer;
import com.top_logic.importer.logger.ImportLogger;
import com.top_logic.importer.logger.ImportMessageLogger;
import com.top_logic.tool.boundsec.assistent.AssistentComponent;

/**
 * Parse one excel sheet and provide data read in on {@link Map}.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class ExcelSheetImportParser<C extends ExcelSheetImportParser.Config>
        extends AbstractExcelFileImportParser<C> {

    /**
     * Configuration for parsing the excel file.
     * 
     * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public interface Config extends AbstractExcelFileImportParser.Config {

        /** Column to find the labels. */
        @IntDefault(1)
        int getLabelColumn();

        /** Column to find the labels. */
        @IntDefault(2)
        int getContentColumn();
    }

    private static final String LABEL_COLUMN = "LABEL";

    private static final String CONTENT_COLUMN = "CONTENT";

    /**
     * Creates a {@link ExcelStructureImportParser}.
     */
    public ExcelSheetImportParser(InstantiationContext aContext, C aConfig) {
        super(aContext, aConfig);
    }

    @Override
    protected void preRead(AssistentComponent aComponent) {
        aComponent.setData(ListDataImportPerformer.PROP_KEY_MAPPING, null);
    }

    @Override
    protected List<Map<String, Object>> doRead(ExcelContext aContext,
            List<Map<String, Object>> aResult, Object aModel, ImportLogger aLogger) {
        this.prepareContext(aContext);

        Map<MappingConfig, Integer> theMappings = this.prepareSheet(aContext, aLogger);
        Map<String, Object> theValues = new HashMap<>();

        for (Entry<MappingConfig, Integer> theEntry : theMappings.entrySet()) {
            this.addValue(aContext, aLogger, theEntry.getKey(), theEntry.getValue(),
                    theValues);
        }

        theValues.putAll(ExcelImportUtils.INSTANCE.evaluateExtractors(aContext, aLogger,
                this.getConfig().getExtractors(), this));

        if (!theValues.isEmpty()) {
            if (!theValues.containsKey(AbstractImportPerformer.MODEL)) {
                theValues.put(AbstractImportPerformer.MODEL, aModel);
            }

            this.postProcessMap(theValues, aContext, aLogger);

            aResult.add(theValues);
        }

        return aResult;
    }

    @Override
    protected void postRead(AssistentComponent aComponent,
            ExcelContextProvider<?> aProvider) {
        Collection<MappingConfig> theMappings = this.getConfig().getMappings();
		List<String> theIDs = this.getConfig().getUID();

        aComponent.setData(ListDataImportPerformer.PROP_KEY_MAPPING,
                ListDataImportPerformer.getIDsFromMapping(theMappings, theIDs));
    }

    /**
     * Do some preparations on the given excel context.
     * 
     * <p>
     * We need to define the columns for reading by {@link Transformer}s by ourself.
     * </p>
     * 
     * @param aContext
     *            The context to be prepared.
     */
    protected void prepareContext(ExcelContext aContext) {
        aContext.prepareHeaderRows();

        Map<String, Integer> theMap = aContext.getColumnMap();
        C theConfig = this.getConfig();

        theMap.put(ExcelSheetImportParser.LABEL_COLUMN, theConfig.getLabelColumn());
        theMap.put(ExcelSheetImportParser.CONTENT_COLUMN, theConfig.getContentColumn());
    }

    /**
     * Parse the row contents at the given context's position.
     * 
     * @param aContext
     *            The {@link ExcelContext} to be used for excel data access
     * @param aLogger
     *            Messages for information to the user.
     * @param aConfig
     *            Mapping to be used for getting the value from.
     * @param aRow
     *            Row we will find the value in.
     * @param aResult
     *            Map to put the value to.
     * @see #findValueRow(ExcelContext, Integer)
     * @see #extractValue(ExcelContext, ImportLogger, MappingConfig)
     */
    private void addValue(ExcelContext aContext, ImportLogger aLogger,
            MappingConfig aConfig, Integer aRow, Map<String, Object> aResult) {
        if (this.findValueRow(aContext, aRow)) {
            try {
                Object theValue = this.extractValue(aContext, aLogger, aConfig);

                ExcelImportUtils.INSTANCE.storeValue(aConfig, aResult, theValue);
            }
            catch (TransformException ex) {
                ImportMessageLogger theLogger = ImportUtils.INSTANCE.getImportMessageLogger(aLogger);

                if (theLogger != null) {
                    if (aConfig.isExceptionTolerated()) {
                        theLogger.logTransformExceptionAsWarn(this, ex);
                    }
                    else {
                        theLogger.logTransformException(this, ex);
                    }
                }
            }
        }
    }

    /**
     * Try to select the given row in the given context.
     * 
     * @param aContext
     *            The context to set the row in.
     * @param aRow
     *            The row to be selected.
     * @return <code>true</code> when given row is larger than -1.
     */
    private boolean findValueRow(ExcelContext aContext, Integer aRow) {
        if (aRow >= 0) {
            return aContext.row(aRow) != null;
        }
        else {
            return false;
        }
    }

    /**
     * Return the value from the given context.
     * 
     * @param aContext
     *            The context to get the value from.
     * @param aLogger
     *            Messages for information to the user.
     * @param aConfig
     *            Configuration to get the transformer from.
     * @return The value from the excel context, may be <code>null</code>.
     */
    private Object extractValue(ExcelContext aContext, ImportLogger aLogger,
            MappingConfig aConfig) {
        return aConfig.getTransformer().transform(aContext,
                ExcelSheetImportParser.CONTENT_COLUMN, this, aLogger);
    }

    /**
	 * Return the mapping from a
	 * {@link com.top_logic.importer.excel.AbstractExcelFileImportParser.MappingConfig} to the row
	 * it is located in the context.
	 * 
	 * @param aContext
	 *        The excel context to get the data from.
	 * @param aLogger
	 *        Logger for messages.
	 * @return The requested mapping.
	 */
    private Map<MappingConfig, Integer> prepareSheet(ExcelContext aContext,
            ImportLogger aLogger) {
        C theConfig = this.getConfig();
        int theCol = theConfig.getLabelColumn();
        List<MappingConfig> theMissings = new ArrayList<>(
                theConfig.getMappings());
        Map<String, MappingConfig> theMapping = this.prepareMappingConfig(theMissings);
        Map<MappingConfig, Integer> theMap = new HashMap<>();

        aContext.row(theConfig.getFirstRow());

        do {
            try {
                String theLabel = aContext.getString(theCol);

                if (!StringServices.isEmpty(theLabel)) {
                    if (theLabel.endsWith(":")) {
                        theLabel = theLabel.substring(0, theLabel.length() - 1);
                    }

                    MappingConfig theValue = theMapping.get(theLabel);

                    if (theValue != null) {
                        theMissings.remove(theValue);
                        theMap.put(theValue, aContext.row());
                    }
                }
            }
            catch (RuntimeException ex) {
                if (!"not implemented yet".equals(ex.getMessage())) {
                    throw ex;
                }
                else {
					aLogger.info(this, I18NConstants.CANNOT_READ_LABEL, aContext.row() + 1,
						aContext.getColumnNr(theCol), ex.getLocalizedMessage());
                }
            }

            aContext.down();
        } while (aContext.hasMoreRows());

        for (MappingConfig theMissing : theMissings) {
            theMap.put(theMissing, -1);
        }

        return theMap;
    }

    /**
	 * Map the different
	 * {@link com.top_logic.importer.excel.AbstractExcelFileImportParser.MappingConfig}s by their
	 * {@link com.top_logic.importer.excel.AbstractExcelFileImportParser.MappingConfig#getName()
	 * names}.
	 * 
	 * @param someMappings
	 *        The {@link com.top_logic.importer.excel.AbstractExcelFileImportParser.MappingConfig}s
	 *        to be mapped.
	 * @return The requested map.
	 */
    private Map<String, MappingConfig> prepareMappingConfig(
            List<MappingConfig> someMappings) {
        Map<String, MappingConfig> theMap = new HashMap<>();

        MapUtil.mapValuesInto(theMap, new Mapping<MappingConfig, String>() {

            @Override
            public String map(MappingConfig aConfig) {
                return aConfig.getName();
            }
        }, someMappings);

        return theMap;
    }
}
