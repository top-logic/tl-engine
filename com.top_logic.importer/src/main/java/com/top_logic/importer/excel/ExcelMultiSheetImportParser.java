/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.excel;

import java.util.List;
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import com.top_logic.base.office.excel.ExcelContext;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.importer.excel.ExcelContextProvider.SheetExcelContextProvider;
import com.top_logic.importer.logger.ImportLogger;
import com.top_logic.tool.boundsec.assistent.AssistentComponent;
import com.top_logic.util.error.TopLogicException;

/**
 * Parse several spreadsheets in excel files in XLSX, XLS or CSV format and provide the data read.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class ExcelMultiSheetImportParser<C extends ExcelMultiSheetImportParser.MultiSheetConfig> extends AbstractExcelFileImportParser<C> {

    /**
     * Configuration for parsing multiple sheets within the excel file. 
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public interface MultiSheetConfig extends AbstractExcelFileImportParser.Config {

        /** Default parser to be used for each spreadsheet. */
        @InstanceFormat
        @Mandatory
        @Name("parser")
        AbstractExcelFileImportParser<?> getDefaultParser();

        /** Comma separated list of sheets to be ignored by the parser. */
        @Format(CommaSeparatedStrings.class)
        List<String> getSheetsToExclude();
    }

    private AssistentComponent component;

    /** 
     * Creates a {@link ExcelMultiSheetImportParser}.
     */
    public ExcelMultiSheetImportParser(InstantiationContext aContext, C aConfig) {
        super(aContext, aConfig);
    }

    @Override
    protected void preRead(AssistentComponent aComponent) {
        this.component = aComponent;
    }

    @Override
    protected List<Map<String, Object>> doRead(ExcelContext aContext, List<Map<String, Object>> aResult, Object aModel, ImportLogger aLogger) throws InvalidFormatException {
        if (!(aContext instanceof WorkbookExcelContext)) {
            throw new IllegalArgumentException("Given excel context represents no Workbook, multi sheet is not possible.");
        }

        WorkbookExcelContext theContext = (WorkbookExcelContext) aContext;
        List<String>         theExclude = this.getConfig().getSheetsToExclude();

        for (String theName : theContext.getSheetNames()) {
            if (!theExclude.contains(theName) && !theContext.isHidden(theName)) {
                AbstractExcelFileImportParser<?> theParser = this.getInnerParser(theName);

                if (theParser != null) {
                    theContext.setCurrentSheet(theName);

                    aLogger.info(this, I18NConstants.ACTIVATE_SHEET, theName);

                    this.doInnerRead(theParser, theContext, aResult, aModel, aLogger);
                }
            }
        }

        return aResult;
    }

    @Override
    protected void postRead(AssistentComponent aComponent, ExcelContextProvider<?> aProvider) {
        this.component = null;
    }

    @Override
    protected int prepareHeaderBySearch(ExcelContext aContext, int aSize) throws TopLogicException {
        int                  thePos     = -1;
        WorkbookExcelContext theContext = (WorkbookExcelContext) aContext;
        String               theCurrent = theContext.getCurrentSheetName();
        List<String>         theExclude = this.getConfig().getSheetsToExclude();

        for (String theName : theContext.getSheetNames()) {
            if (!theExclude.contains(theName)) {
                theContext.setCurrentSheet(theName);

                int theInnerPos = super.prepareHeaderBySearch(theContext, aSize);

                if (theName.equals(theCurrent)) {
                    thePos = theInnerPos;
                }
            }
        }

        theContext.setCurrentSheet(theCurrent);

        return thePos;
    }

    /** 
     * Hand over the prepared excel context to the given parser.
     * 
     * <p>This method will do the following steps on the given parser:</p>
     * <ol>
     *   <li>{@link AbstractExcelFileImportParser#preRead(AssistentComponent) setup} the parser.</li>
     *   <li>{@link AbstractExcelFileImportParser#doRead(ExcelContext, List, Object, ImportLogger) read} data using the parser.</li>
     *   <li>{@link AbstractExcelFileImportParser#postRead(AssistentComponent, ExcelContextProvider) tear down} the parser.</li>
     * </ol>
     * 
     * @param aParser
     *        The parser to work on one sheet.
     * @param aContext
     *        The excel context (sheet) to be read.
     * @param aResult
     *        The list of values to append the data read to.
     * @param aModel
     *        The model we are working on.
     * @param aLogger
     *        Messages for information to the user.
     */
    protected void doInnerRead(AbstractExcelFileImportParser<?> aParser, final WorkbookExcelContext aContext, List<Map<String, Object>> aResult, Object aModel, ImportLogger aLogger) throws InvalidFormatException {
        String theName = aContext.getCurrentSheetName();

		if (component == null) {
			aParser.doRead(aContext, aResult, aModel, aLogger);
		} else {
			aParser.preRead(this.component);
			aParser.doRead(aContext, aResult, aModel, aLogger);
			aParser.postRead(this.component, new SheetExcelContextProvider(this.component, theName) {
				@Override
				public ExcelContext getContext() {
					return aContext;
				}
			});
		}
    }

    /** 
     * Provide the parser for reading the next sheet.
     * 
     * @param aSheetName
     *        Name of the sheet to be read next.
     * @return
     *        The requested parser, <code>null</code> will ignore the given sheet.
     */
    protected AbstractExcelFileImportParser<?> getInnerParser(String aSheetName) {
        return this.getConfig().getDefaultParser();
    }
}
