/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.excel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import com.top_logic.base.office.OfficeException;
import com.top_logic.base.office.excel.ExcelContext;
import com.top_logic.base.office.excel.POIExcelContext;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotationContainer;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.util.ResKey;
import com.top_logic.importer.ImportUtils;
import com.top_logic.importer.base.AbstractImportParser;
import com.top_logic.importer.base.ListDataImportPerformer;
import com.top_logic.importer.excel.ExcelContextProvider.SheetExcelContextProvider;
import com.top_logic.importer.excel.ExcelContextProvider.WorkbookExcelContextProvider;
import com.top_logic.importer.excel.ImportParserPostProcessor.SimpleImportParserPostProcessor;
import com.top_logic.importer.excel.extractor.Extractor;
import com.top_logic.importer.excel.transformer.StringTransformer;
import com.top_logic.importer.excel.transformer.TransformException;
import com.top_logic.importer.excel.transformer.Transformer;
import com.top_logic.importer.logger.ImportLogger;
import com.top_logic.importer.logger.ImportMessageLogger;
import com.top_logic.importer.text.TextFileImportParser;
import com.top_logic.tool.boundsec.assistent.AssistentComponent;
import com.top_logic.util.error.TopLogicException;

/**
 * Base class for excel and CSV based import upload handler with a {@link Config configuration}.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public abstract class AbstractExcelFileImportParser<C extends AbstractExcelFileImportParser.Config> extends AbstractImportParser implements TypedAnnotatable {
    
    /**
     * Configuration for parsing the excel file. 
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public interface Config extends AbstractImportParser.Config {

        /** The mapping from excel columns to attribute values. */
        List<MappingConfig> getMappings();

        /** Extractors for an excel row to attribute values. */
        @InstanceFormat
        List<Extractor> getExtractors();

        /** Post processor called when a row has been read from the file. */
		@ItemDefault(SimpleImportParserPostProcessor.Config.class)
		PolymorphicConfiguration<ImportParserPostProcessor> getPostProcessor();

        /** will the names of the excel columns (A, B, C..) be used as names. */
        @BooleanDefault(false)
        boolean isUseExcelHeader();

        /**
         * overrides {@link #isUseExcelHeader()}
         * 
         * @return use header if available but it is always possible to use excel column addressing. To explicitly denote header use <code>%[header]</code>, to explicitely denote excel column adress use
         *         <code>#[colAdress]</code>. By default header is assumed.
         */
        @BooleanDefault(false)
        boolean isMixedMode();

        /** Comma separated list of columns to build up an unique ID for an excel row. */
		@Name(TextFileImportParser.Config.UID)
        @Mandatory
        @Format(CommaSeparatedStrings.class)
		List<String> getUID();

        /** Return the name of the sheet the data is located in. */
        String getSheet();

        /** Number of row to look for the header names (-1 will use {@link #getHeaderKeyWord()} for look up). */
        int getHeaderRow();

        /** Number of first row to be imported. */
        int getFirstRow();

        /** Unique string value in an excel cell identifying the header row. */
        String getHeaderKeyWord();
    }

    /**
     * Configuration of a mapping from excel cell to a java representation stored in the result map. 
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public interface MappingConfig extends ListDataImportPerformer.MappingConfig {

        /** Transformer to be used for reading the cell value. */
        @InstanceFormat
        @InstanceDefault(StringTransformer.class)
        Transformer<?> getTransformer();

        /** <code>true</code> for adding <code>null</code> values to the map too. */
        @BooleanDefault(true)
        boolean isAddNullValue();

        /** <code>true</code> when exceptions in reading data is fine for this column. */
        @BooleanDefault(true)
        boolean isExceptionTolerated();

		String getChannel();
    }

	/** Source for the importer. */
	public static final Property<Object> IMPORT_SOURCE = TypedAnnotatable.property(Object.class, "IMPORT_SOURCE");

    /** Configuration provided by this import upload handler. */
	private final C _config;

	/** Map for handling additional data during parse process. */
	private TypedAnnotatable _properties;

	private ImportParserPostProcessor _postProcessor;

    /** 
     * Creates a {@link AbstractExcelFileImportParser}.
     */
    public AbstractExcelFileImportParser(InstantiationContext aContext, C aConfig) {
        super(aContext, aConfig);
        
		this._config = aConfig;
		_postProcessor = aContext.getInstance(aConfig.getPostProcessor());
    }

    /** 
     * Hook before the {@link #readData(ExcelContextProvider, Object, ImportLogger)} method starts.
     * 
     * @param aComponent
     *        Component calling this importer.
     */
    protected abstract void preRead(AssistentComponent aComponent);

    /**
     * Read the data for the import out of the given excel context.
     * 
     * @param aContext
     *        The reader to be parsed.
     * @param aResult
     *        The list of results to be filled.
     * @param aLogger
     *        Messages for information to the user.
     * @return The requested data.
     */
    protected abstract List<Map<String, Object>> doRead(ExcelContext aContext, List<Map<String, Object>> aResult, Object aModel, ImportLogger aLogger) throws InvalidFormatException;

    /** 
     * Hook when {@link #readData(ExcelContextProvider, Object, ImportLogger)} method is finished.
     * 
     * @param aComponent
     *        Component calling this importer.
     */
    protected abstract void postRead(AssistentComponent aComponent, ExcelContextProvider<?> aProvider);

	@Override
	public <T> T get(final Property<T> property) {
		return _properties.get(property);
	}

	@Override
	public <T> T set(final Property<T> property, final T value) {
		return _properties.set(property, value);
	}

	@Override
	public boolean isSet(final Property<?> property) {
		return _properties.isSet(property);
	}

	@Override
	public <T> T reset(final Property<T> property) {
		return _properties.reset(property);
	}

    @Override
    public C getConfig() {
		return _config;
    }

	public ImportParserPostProcessor getPostProcessor() {
		return _postProcessor;
	}

    @Override
    protected List<Map<String, Object>> readData(AssistentComponent aComponent, Object aSource, ImportLogger aLogger) {
        if (aComponent != null) {
            this.preRead(aComponent);
        }

        try {
            ExcelContextProvider<?> aProvider = this.createExcelContextProvider(aComponent);
            Object                  theModel  = this.getModel(aComponent);

            this.initProvider(aProvider, aSource, aLogger);

			this._properties = new TypedAnnotationContainer();

			this.set(IMPORT_SOURCE, aSource);

            List<Map<String, Object>> theResult = this.readData(aProvider, theModel, aLogger);

            if (aComponent != null) {
                this.postRead(aComponent, aProvider);
            }

			getPostProcessor().postProcessGlobal(theResult, aLogger);

            return theResult;
        }
        catch (TransformException ex) {
            ImportMessageLogger theLogger = ImportUtils.INSTANCE.getImportMessageLogger(aLogger);

            if (theLogger != null) {
                theLogger.logTransformException(this, ex);
            }
        } catch (I18NRuntimeException ex) {
			ImportMessageLogger theLogger = ImportUtils.INSTANCE.getImportMessageLogger(aLogger);

			if (theLogger != null) {
				theLogger.error(this, ex.getErrorKey());
			}
		}
        catch (Exception ex) {
            aLogger.error(AbstractExcelFileImportParser.class, I18NConstants.ERROR_PARAM, ex);
        }
		finally {
			this._properties = null;
		}

        return Collections.emptyList();
    }

    protected Map<String, Object> extractValues(ExcelContext aContext, ImportLogger aLogger) {
		return ExcelImportUtils.INSTANCE.doExtractValues(aContext, aLogger, getConfig().getMappings(),
			getConfig().getExtractors(), this);
    }

    /** 
     * Hook for doing some initialization code when extracting values starts.
     * 
     * @param    aComponent    Component calling this handler.
     */
    protected void initFromComponent(AssistentComponent aComponent) {
        // Nothing to do in general
    }

    /**
     * Check the extracted value map for problems.
     * 
     * @param someValues
     *        the value map. Must not be <code>null</code>.
     * @param aContext
     *        the current {@link ExcelContext}. Must not be <code>null</code>
     * @param aLogger
     *        the {@link ImportLogger}. Must not be <code>null</code>.
     */
    protected void postProcessMap(Map<String, Object> someValues, ExcelContext aContext, ImportLogger aLogger) {
		getPostProcessor().postProcessMap(someValues, aContext, aLogger);
    }

    protected void constraintsAware(ExcelContext aContext, ImportLogger aLogger) {
        // hook to be overwritten.
    }

    protected void checkConstraints(ImportLogger aLogger) {
        // hook to be overwritten.
    }

    protected int getHeaderRow() {
		return getConfig().getHeaderRow();
    }

    protected int getFirstRow() {
		return getConfig().getFirstRow();
    }

    protected String getHeaderKeyWord() {
		return getConfig().getHeaderKeyWord();
    }

    /**
     * Name of the sheet imported.
     *
     * @return the tab name, must not be <code>null</code> or empty.
     */
    protected String getTabName() {
		return getConfig().getSheet();
    }

    /** 
     * Identify if the current row in the given context is an empty row.
     * 
     * @param    aContext    The context to get the information from.
     * @param    logger     Logger for messages.
     * @return   <code>true</code> when row is empty (will result in ignoring this row in import).
     */
    protected boolean isEmptyRow(ExcelContext aContext, ImportLogger logger) {
        String theKey = this.getHeaderKeyWord();

        return !StringServices.isEmpty(theKey) ? StringServices.isEmpty(aContext.getString(theKey)) : false;
    }

    /**
	 * Create the excel context provider for an import run.
	 * 
	 * @param aComponent
	 *        Component calling this method.
	 * @return The requested provider, never <code>null</code>.
	 * @see #validateFile(AssistentComponent, BinaryData)
	 */
    protected ExcelContextProvider<?> createExcelContextProvider(AssistentComponent aComponent) {
        String theTab = this.getTabName();

        if (StringServices.isEmpty(theTab)) {
            return new WorkbookExcelContextProvider(aComponent);
        }
        else { 
            return new SheetExcelContextProvider(aComponent, theTab);
        }
    }

    /**
     * Validate values found in the uploaded file.
     *
     * @return   A list of values representing the desired result of the import.
     */
    protected List<Map<String, Object>> readData(ExcelContextProvider<?> aProvider, Object aModel, ImportLogger aLogger) throws Exception {
        ExcelContext theContext = null;

        try {
            theContext = this.initExcelContext(aProvider);

			return this.doRead(theContext, new ArrayList<>(), aModel, aLogger);
        }
        finally {
            try {
                if (theContext != null) {
                    theContext.close();
                }
            }
            catch (IOException ex) {
                aLogger.error(AbstractExcelFileImportParser.class, I18NConstants.CONTEXT_CLOSE_FAILED, ex);
            }
        }
    }

	protected void initProvider(ExcelContextProvider<?> aProvider, Object aSource, ImportLogger aLogger)
			throws OfficeException, IOException {
		if (aSource instanceof BinaryContent) {
			aProvider.init((BinaryContent) aSource);
		} else if (aSource instanceof File) {
            aProvider.init((File) aSource);
        }
        else if (aSource instanceof InputStream) {
            aProvider.init((InputStream) aSource);
        }
        else {
			if (aSource == null) {
				throw new IllegalArgumentException("No source given!");
			} else {
				throw new IllegalArgumentException("Given source must be " + File.class.getName() + ", "
					+ InputStream.class.getName() + ", or " + BinaryContent.class.getName() + ", but was " + aSource
					+ " (" + aSource.getClass().getName() + ")");
			}
        }

        this.initFromComponent(aProvider.getComponent());
    }

    /**
	 * Initialize the excel context with the first content row.
	 * 
	 * @param aProvider
	 *        The source to create the context from, must not be <code>null</code>.
	 * @return The requested context, never <code>null</code>.
	 * @throws TopLogicException
	 *         When parsing the file fails.
	 */
    protected ExcelContext initExcelContext(ExcelContextProvider<?> aProvider) throws TopLogicException {
		boolean isMixedMode = getConfig().isMixedMode();
        ExcelContext theContext   = aProvider.createContext(isMixedMode);
        int          theHeaderRow = this.getHeaderRow();
        int          thePos;

        if (theHeaderRow >= 0) {
            thePos = this.prepareHeaderByNumber(theContext, theHeaderRow, isMixedMode);
        }
        else {
       		thePos = this.prepareHeaderBySearch(theContext, aProvider.getMaxHeaderRowSize());
        }

        return theContext.row(thePos);
    }

    private int prepareHeaderByNumber(ExcelContext aContext, int aHeaderRow, boolean isMixedMode) {
        aContext.row(aHeaderRow);

		if (!isMixedMode && getConfig().isUseExcelHeader()) {
            aContext.prepareColNamesAsHeaders();
        }
        else {
            aContext.prepareHeaderRows();
        }

        return this.getFirstRow();
    }

    /**
     * Find the header row in the given sheet and prepare the header mapping.
     * 
     * @param aContext
     *        The context to be used while searching, must not be <code>null</code>.
     * @param aSize
     *        The maximum number of rows to be inspected.
     * @return The first content row (which is the next to the header row).
	 * @throws TopLogicException
	 *         When header column couldn't be found.
     */
    protected int prepareHeaderBySearch(ExcelContext aContext, int aSize) throws TopLogicException {
        String  theKey = this.getHeaderKeyWord();
        int     thePos = 0;
        boolean found  = false;

        while (!found && thePos < aSize) {
            aContext.row(thePos++);
            found = this.findMarker(aContext, theKey);
        }

        if (found) {
            aContext.prepareHeaderRows();
        }
        else {
			throw new TopLogicException(this.getMissingColumnMessage(aContext, theKey));
        }

        return thePos;
    }

	/**
	 * Return the message about a missing column in the given excel context.
	 * 
	 * @param aContext
	 *        The context to get additional information from.
	 * @param aColumn
	 *        The name of the requested column.
	 * @return The requested message key.
	 */
	protected ResKey getMissingColumnMessage(ExcelContext aContext, String aColumn) {
		String theSheet;

		if (aContext instanceof WorkbookExcelContext) {
			theSheet = ((WorkbookExcelContext) aContext).getCurrentSheetName();
		}
		else if (aContext instanceof POIExcelContext) {
			theSheet = ((POIExcelContext) aContext).sheet().getSheetName();
		}
		else {
			theSheet = null;
		}

		if (StringServices.isEmpty(theSheet)) {
			return I18NConstants.KEY_WORD_NOT_FOUND.fill(aColumn);
		}
		else {
			return I18NConstants.KEY_WORD_NOT_FOUND_IN_SHEET.fill(aColumn, theSheet);
		}
	}

    /**
     * Parse the given row and try to find an entry with the given name.
     * 
     * @param aContext
     *        The row to be parsed.
     * @param aMarker
     *        The text to be found.
     * @return <code>true</code> when the row contains a cell with the marker text.
     */
    private boolean findMarker(ExcelContext aContext, String aMarker) {
        int theMax = aContext.getLastCellNum();

        for (int thePos = 0; thePos < theMax; thePos++) {
            String theValue = aContext.getString(thePos);

			if ((theValue != null) && aMarker.equals(theValue.trim())) {
				return true;
			}
        }

        return false;
    }
}
