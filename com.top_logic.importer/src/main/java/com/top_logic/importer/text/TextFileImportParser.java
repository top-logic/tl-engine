/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.text;

import java.io.BufferedReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.filter.TrueFilter;
import com.top_logic.basic.col.filter.configurable.AbstractConfigurableFilter;
import com.top_logic.basic.col.filter.typed.FilterResult;
import com.top_logic.basic.col.filter.typed.TypedFilter;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.tools.NameBuilder;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.importer.base.AbstractImportParser;
import com.top_logic.importer.base.AbstractImportPerformer;
import com.top_logic.importer.base.ListDataImportPerformer;
import com.top_logic.importer.logger.ImportLogger;
import com.top_logic.importer.text.TextImportParser.Text;
import com.top_logic.tool.boundsec.assistent.AssistentComponent;

/**
 * Upload handler for import of text based files which have a fixed value length.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
@SuppressWarnings("serial")
public class TextFileImportParser extends AbstractImportParser {

    /**
     * Configuration of this upload handler. 
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public interface Config extends AbstractImportParser.Config {

		/** Unique ID for identifying lines in the text file. */
        public static final String UID = "uid";

		@Override
        @FormattedDefault(".txt")
        List<String> getExtensions();

        /** Comma separated list of columns to build up an unique ID for a text line. */
		@Name(UID)
        @Mandatory
        @Format(CommaSeparatedStrings.class)
		List<String> getUID();

        /** Optional filter for skipping lines in the text file. */
        @InstanceFormat
        @InstanceDefault(TrueFilter.class)
        TypedFilter getFilter();

        /** Offset for start parsing the values of a column. */
        @IntDefault(-1)
        int getColumnOffset();

        /** Columns to be imported. */
        @Mandatory
        @Key(MappingConfig.NAME_ATTRIBUTE)
        Map<String, MappingConfig> getMappings();
    }

    /**
     * Configuration of one fixed value column.
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public interface MappingConfig extends ListDataImportPerformer.MappingConfig {

        /** Start character to read the value (starting from {@link TextFileImportParser.Config#getColumnOffset()}). */
        @Mandatory
        int getStart();

        /** End character to read the value (starting from {@link TextFileImportParser.Config#getColumnOffset()}). */
        @Mandatory
        int getEnd();

        /** Parser the string read for usage in setter of {@link Wrapper} later on. */
        @InstanceFormat
        @InstanceDefault(Text.class)
        TextImportParser<?> getParser();

        /** Parser the string read for usage in setter of {@link Wrapper} later on. */
        @InstanceFormat
        TextImportExtractor<?> getExtractor();
    }

    /** Configuration provided by this import upload handler. */
    protected final Config config;

    private TypedFilter filter;
//
//    private String name;
//
//    private String desc;

    /**
     * Creates a {@link TextFileImportParser}.
     */
    public TextFileImportParser(InstantiationContext aContext, Config aConfig) {
        super(aContext, aConfig);

		this.config = aConfig;
        this.filter = this.config.getFilter();

        Map<String, MappingConfig> theColumns = this.config.getMappings();

        if (this.filter instanceof AbstractConfigurableFilter) {
            TextImportFilter.initFilter(this.filter, theColumns);
        }

        for (MappingConfig theColumn : theColumns.values()) {
            TextImportExtractor<?> theExtractor = theColumn.getExtractor();

            if (theExtractor != null) {
                theExtractor.init(theColumns);
            }
        }
    }

    @Override
    public Config getConfig() {
        return this.config;
    }

    @Override
    public String toString() {
        return new NameBuilder(this)
                .add("name", this.getName())
                .add("filter", !this.filter.equals(TrueFilter.INSTANCE))
                .build();
    }

    @Override
    protected List<Map<String, Object>> readData(AssistentComponent aComponent, Object aSource, ImportLogger aLogger) {
        if (aComponent != null) {
            aComponent.setData(ListDataImportPerformer.PROP_KEY_MAPPING, null);
        }

        List<Map<String, Object>> theResult = new ArrayList<>();
        BufferedReader            theReader = null;

        try {
            Object theModel  = this.getModel(aComponent);
            int    theOffset = this.config.getColumnOffset();
            int    theRows   = 0;
            int    theItems  = 0;

            theReader = new BufferedReader(this.createReader(aSource));

            TextLine theLine = new TextLine(this.config.getMappings(), theOffset);

            while (theReader.ready()) {
                theLine.init(theRows + 1, theReader.readLine());

                if (this.isValid(theLine)) {
                    try {
                        Map<String, Object> theValues = theLine.getValues();

                        if (theValues != null) {
                            if (!theValues.containsKey(AbstractImportPerformer.MODEL)) {
                                theValues.put(AbstractImportPerformer.MODEL, theModel);
                            }

                            theResult.add(theValues);
                            theItems++;
                        }
                    }
                    catch (Exception ex) {
                        aLogger.error(TextFileImportParser.class, I18NConstants.PARSE_EXCEPTION_PARAM, ex, new Object[] {theRows, theLine.line});
                    }
                }

                theRows++;
            }

            aLogger.info(TextFileImportParser.class, I18NConstants.UPLOAD_HANDLER_READ, new Object[] {theRows, theItems, this.getName()});

            if (aComponent != null) {
                Collection<MappingConfig> theMappings = this.getConfig().getMappings().values();
                List<String>              theIDs      = this.getConfig().getUID();

                aComponent.setData(ListDataImportPerformer.PROP_KEY_MAPPING, ListDataImportPerformer.getIDsFromMapping(theMappings, theIDs));
            }
        }
        catch (Exception ex) {
            aLogger.error(TextFileImportParser.class, I18NConstants.EXCEPTION_PARAM, ex);
        }
        finally {
            FileUtilities.close(theReader);
        }

        return theResult;
    }

    /** 
     * Check, if the given line is relevant for the importer according to the configured {@link Config#getFilter()}.
     * 
     * @param    aLine    The line to be checked.
     * @return   <code>true</code> when line is relevant.
     */
    protected boolean isValid(TextLine aLine) {
        return this.config.getFilter().matches(aLine) == FilterResult.TRUE;
    }

    /**
     * Representation of one line in the text file. 
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public static class TextLine {

        private static final Object NULL_VALUE = new Object();

        // Attributes

        private final int offset;
        private final Map<String, MappingConfig> columns;
        private final Map<MappingConfig, Object> valueMap;

        private String line;
        private int row;

        // Constructors

        /** 
         * Creates a {@link TextLine}.
         */
        public TextLine(Map<String, MappingConfig> aColumnMap, int anOffset) {
            this.columns  = aColumnMap;
            this.offset   = anOffset;
            this.valueMap = new HashMap<>();
        }

        // Overridden methods from Object

        @Override
        public String toString() {
            return new NameBuilder(this)
                    .add("row", this.row)
                    .add("line", this.line)
                    .build();
        }

        // Public methods

        /** 
         * Set a new line to this instance.
         * 
         * @param aRow     The row the line represents.
         * @param aLine    The line to be held.
         */
        public void init(int aRow, String aLine) {
            this.row  = aRow;
            this.line = aLine;

            this.valueMap.clear();
        }

        /** 
         * Return the parsed values from this line.
         * 
         * @return    The requested map, never <code>null</code>.
         * @throws    ParseException    When parsing one value fails.
         */
        public Map<String, Object> getValues() throws ParseException {
            Map<String, Object> theMap = new HashMap<>();

            for (MappingConfig theConfig : this.columns.values()) {
                String theKey = theConfig.getAttribute();

                // Forget about parts which doesn't match to an attribute.
                if (!StringServices.isEmpty(theKey)) {
                    Object theValue = this.getValue(theConfig);

                    // Forget about parts which doesn't have a value.
                    if (theValue != null) {
                        theMap.put(theKey, theValue);
                    }
                }
            }

            return theMap;
        }

        // Protected methods

        /** 
         * Return the converted value for the given column.
         * 
         * @param    aConfig    The requested column.
         * @return   The converted value.
         * @throws   ParseException   When converting the raw string value failed.
         * @see      #getString(MappingConfig)
         * @see      #toObject(MappingConfig, String)
         */
        protected Object getValue(MappingConfig aConfig) throws ParseException {
            Object theValue = this.valueMap.get(aConfig);

            if (theValue == null) {
                theValue = this.toObject(aConfig, this.getString(aConfig));

                if (theValue == null) {
                    theValue = NULL_VALUE;
                }

                this.valueMap.put(aConfig, theValue);
            }

            return (theValue == NULL_VALUE) ? null : theValue;
        }

        /** 
         * Return the raw string value for the given column.
         * 
         * @param    aConfig    The requested column.
         * @return   The raw string value.
         */
        protected String getString(MappingConfig aConfig) {
            int theStart = aConfig.getStart();
            int theEnd   = aConfig.getEnd() + 1;

            return this.line.substring(theStart + this.offset, theEnd + this.offset);
        }

        /** 
         * Convert the given string into the requested object type.
         * 
         * @param    aConfig   Column description defining the {@link MappingConfig#getParser()}.
         * @param    aString   Raw string value from this line.
         * @return   The converted value.
         * @throws   ParseException   When converting the raw string value failed.
         */
        protected Object toObject(MappingConfig aConfig, String aString) throws ParseException {
            TextImportExtractor<?> theExtractor = aConfig.getExtractor();

            if (theExtractor != null) {
                return theExtractor.map(this);
            }
            else {
                return aConfig.getParser().map(aString);
            }
        }
    }
}
