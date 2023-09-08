/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.importer.text.TextFileImportParser.MappingConfig;
import com.top_logic.importer.text.TextFileImportParser.TextLine;

/**
 * Convert a text line to the defined type of object.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public abstract class TextImportExtractor<T> implements Mapping<TextLine, T> {

    /** 
     * Hook for sub classes to initialize themselves.
     * 
     * @param someColumns    The whole mapping configurations.
     */
    public void init(Map<String, MappingConfig> someColumns) {
        // Nothing in here
    }

    /**
     * Merge the named column values into one string. 
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public static class Merge extends TextImportExtractor<String> {

        /**
         * Configuration of the merge extractor. 
         * 
         * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
         */
        public interface Config extends PolymorphicConfiguration<Merge> {

            /** Name of columns to be combined. */
            @Format(CommaSeparatedStrings.class)
            @Mandatory
            List<String> getColumns();

            /** Delimiter between the different values. */
            String getDelimiter();
        }

        // Attributes

        private final List<String> columnNames;

        private final String delimiter;

        private List<MappingConfig> columns;

        // Constructors

        /** 
         * Creates a {@link Merge}.
         */
        public Merge(InstantiationContext aContext, Config aConfig) {
            this.columnNames = aConfig.getColumns();
            this.delimiter   = aConfig.getDelimiter();
        }

        // Overridden methods from TextImportExtractor

        @Override
        public void init(Map<String, MappingConfig> someColumns) {
            this.columns = new ArrayList<>();

            for (String theName : this.columnNames) {
                MappingConfig theColumn = someColumns.get(theName);

                if (theColumn != null) {
                    this.columns.add(theColumn);
                }
            }
        }

        @Override
        public String map(TextLine aLine) {
            StringBuffer theBuffer = null;

            for (MappingConfig theColumn : this.columns) {
                if (theBuffer == null) {
                    theBuffer = new StringBuffer();
                }
                else if (this.delimiter != null) {
                    theBuffer.append(this.delimiter);
                }

                theBuffer.append(aLine.getString(theColumn));
            }

            return (theBuffer != null) ? theBuffer.toString() : "";
        }
    }
}

