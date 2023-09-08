/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.text;

import java.text.ParseException;
import java.util.Map;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.filter.configurable.AbstractConfigurableFilter;
import com.top_logic.basic.col.filter.configurable.ConfigurableCompositeFilter;
import com.top_logic.basic.col.filter.configurable.ConfigurableNotFilter;
import com.top_logic.basic.col.filter.typed.FilterResult;
import com.top_logic.basic.col.filter.typed.TypedFilter;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.tools.NameBuilder;
import com.top_logic.importer.text.TextFileImportParser.MappingConfig;
import com.top_logic.importer.text.TextFileImportParser.TextLine;

/**
 * Base filter class providing the configuration and the reading of a value from a {@link TextLine}.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public abstract class TextImportFilter extends AbstractConfigurableFilter<TextLine, TextImportFilter.Config> {

    /**
     * Configuration of a filter working on {@link TextLine}s.
     * 
     * @author    <a href="mailto:mga@top-logic.com">mga</a>
     */
    public interface Config extends AbstractConfigurableFilter.Config<TextImportFilter> {

        /** Name of the column to be requested in the {@link TextLine}. */
        @Mandatory
        String getColumn();

		/**
		 * Value to be used for compare.
		 * 
		 * <p>
		 * The value is the raw value parsed by
		 * {@link TextFileImportParser.MappingConfig#getParser()}.
		 * </p>
		 * 
		 * @see TextImportFilter#getCompareValue()
		 */
        String getValue();
    }

    /** Configured column to be used. */
    private MappingConfig columnConfig;

    /**
	 * @see #getCompareValue()
	 */
    private Object compareValue;

    /** 
     * Creates a {@link TextImportFilter}.
     */
    public TextImportFilter(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public Class<?> getType() {
		return TextLine.class;
    }

    @Override
    public String toString() {
        Config theConfig = this.getConfig();

        return new NameBuilder(this)
                .add("column", theConfig.getColumn())
                .add("compareValue", this.compareValue)
                .build();
    }

    /** 
     * Callback to initialize the {@link #columnConfig} and {@link #compareValue} of this filter.
     * 
     * @param someColumns    Map of columns defined by the importer.
     */
    public void init(Map<String, MappingConfig> someColumns) {
        MappingConfig theColumn = someColumns.get(this.getConfig().getColumn());

        if (theColumn != null) {
            this.columnConfig = theColumn;
            this.compareValue = theColumn.getParser().map(this.getConfig().getValue());
        }
    }

    /**
	 * Return the trimmed string value from the given {@link TextLine} for the {@link #columnConfig}
	 * defined in the configuration.
	 * 
	 * @param aLine
	 *        The text line to get the trimmed value from.
	 * @return The requested string, never <code>null</code>.
	 */
    protected Object getValue(TextLine aLine) {
        try {
            return aLine.getValue(this.columnConfig);
        }
        catch (ParseException ex) {
            throw new IllegalArgumentException("Failed to parse value for '" + this.getConfig().getColumn() + "'!", ex);
        }
    }

    /**
	 * The value to compare with.
	 * 
	 * @see Config#getValue()
	 */
    protected Object getCompareValue() {
        return this.compareValue;
    }

    /** 
     * Initialize the text filters by the given map of {@link MappingConfig}s.
     * 
     * @param    aFilter        The containing the filters to be updated.
     * @param    someColumns    The mappings to be used in the filters later on.
     */
    public static void initFilter(TypedFilter aFilter, Map<String, MappingConfig> someColumns) {
        if (aFilter instanceof TextImportFilter) {
            ((TextImportFilter) aFilter).init(someColumns);
        }

		if (aFilter instanceof ConfigurableCompositeFilter) {
			for (TypedFilter theFilter : ((ConfigurableCompositeFilter<?>) aFilter).getFilters()) {
				TextImportFilter.initFilter(theFilter, someColumns);
            }
        }
        else if (aFilter instanceof ConfigurableNotFilter) {
			TextImportFilter.initFilter(((ConfigurableNotFilter<?>) aFilter).getInnerFilter(), someColumns);
        }
    }

    /**
     * Check if the found string value is equal to the expected one. 
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public static class Equals extends TextImportFilter {

        /**
		 * Creates a {@link Equals} filter.
		 */
        public Equals(InstantiationContext context, Config config) {
            super(context, config);
        }

        @Override
        protected FilterResult matchesTypesafe(TextLine aLine) {
            return this.getCompareValue().equals(this.getValue(aLine)) ? FilterResult.TRUE : FilterResult.FALSE;
        }
    }

    /**
     * Check if the found string value is larger than the expected one. 
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public static class Larger extends TextImportFilter {

        /**
		 * Creates a {@link Larger} filter.
		 */
        public Larger(InstantiationContext context, Config config) {
            super(context, config);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected FilterResult matchesTypesafe(TextLine aLine) {
            Comparable<Object> theCompare = (Comparable<Object>) this.getCompareValue();
            Object             theValue   = this.getValue(aLine);

            if (theValue == null) {
                return FilterResult.INAPPLICABLE;
            }
            else { 
                return (theCompare.compareTo(theValue) < 0) ? FilterResult.TRUE : FilterResult.FALSE;
            }
        }
    }

    /**
     * Check if the found string value is smaller than the expected one. 
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public static class Smaller extends TextImportFilter {

        /**
		 * Creates a {@link Smaller} filter.
		 */
        public Smaller(InstantiationContext context, Config config) {
            super(context, config);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected FilterResult matchesTypesafe(TextLine aLine) {
            Comparable<Object> theCompare = (Comparable<Object>) this.getCompareValue();
            Object             theValue   = this.getValue(aLine);

            if (theValue == null) {
                return FilterResult.INAPPLICABLE;
            }
            else { 
                return (theCompare.compareTo(theValue) > 0) ? FilterResult.TRUE : FilterResult.FALSE;
            }
        }
    }

    /**
     * Check if the found string value is empty. 
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public static class Empty extends TextImportFilter {

        /**
		 * Creates a {@link Empty} filter.
		 */
        public Empty(InstantiationContext context, Config config) {
            super(context, config);
        }

        @Override
        protected FilterResult matchesTypesafe(TextLine aLine) {
            Object theValue = this.getValue(aLine);

            return StringServices.isEmpty(theValue) ? FilterResult.TRUE : FilterResult.FALSE;
        }
    }

    /**
     * Check if the found string has an expected length. 
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public static class Length extends TextImportFilter {

        private int length;

        /**
		 * Creates a {@link Length} filter.
		 */
        public Length(InstantiationContext context, Config config) {
            super(context, config);

            this.length = Integer.parseInt(config.getValue());
        }

        @Override
        protected FilterResult matchesTypesafe(TextLine aLine) {
            Object theValue = this.getValue(aLine);

            if (theValue == null) {
                theValue = "";
            }

            return this.length == theValue.toString().trim().length() ? FilterResult.TRUE : FilterResult.FALSE;
        }
    }
}
