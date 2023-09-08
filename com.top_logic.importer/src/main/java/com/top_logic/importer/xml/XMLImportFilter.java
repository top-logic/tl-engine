/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.xml;

import java.util.Map;

import org.xml.sax.Attributes;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.filter.configurable.AbstractConfigurableFilter;
import com.top_logic.basic.col.filter.configurable.ConfigurableCompositeFilter;
import com.top_logic.basic.col.filter.configurable.ConfigurableFilter;
import com.top_logic.basic.col.filter.configurable.ConfigurableNotFilter;
import com.top_logic.basic.col.filter.typed.FilterResult;
import com.top_logic.basic.col.filter.typed.TypedFilter;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.tools.NameBuilder;
import com.top_logic.importer.text.TextImportParser;

/**
 * Base filter class providing the configuration and the reading of a value from an {@link Attributes}.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public abstract class XMLImportFilter extends AbstractConfigurableFilter<Attributes, XMLImportFilter.Config> {

    /**
     * Configuration of a filter working on {@link Attributes}s.
     * 
     * @author    <a href="mailto:mga@top-logic.com">mga</a>
     */
    public interface Config extends AbstractConfigurableFilter.Config<XMLImportFilter> {

        /** Name of the XML attribute to be requested (named "column" for better matching with text based filters). */
        @Mandatory
        String getColumn();

		/**
		 * Value to be used for compare.
		 * 
		 * <p>
		 * The value is the raw value parsed with the corresponding {@link TextImportParser}.
		 * </p>
		 * 
		 * @see XMLImportFilter#getCompareValue()
		 */
        String getValue();
    }

    /** Value to be used for compare ({@link XMLImportFilter.Config#getValue()} parsed by {@link #parser}). */
    private Object compareValue;

    private TextImportParser<?> parser;

    /** Name of the attribute to be read (in configuration named "column" for better matching with text based filters). */
    private final String attributeName;

    /** 
     * Creates a {@link XMLImportFilter}.
     */
    public XMLImportFilter(InstantiationContext aContext, Config aConfig) {
		super(aContext, aConfig);

        this.attributeName = this.getConfig().getColumn();
    }

	@Override
	public Class<?> getType() {
		return Attributes.class;
	}

    @Override
    public String toString() {
        return new NameBuilder(this)
                .add("attributeName", this.attributeName)
                .add("compareValue", this.compareValue)
                .add("parser", this.parser)
                .build();
    }

    /**
	 * Callback to initialize the {@link #parser} and {@link #compareValue} of this filter.
	 * 
	 * @param someParsers
	 *        Map of columns defined by the importer.
	 */
    public void init(Map<String, TextImportParser<?>> someParsers) {
        TextImportParser<?> theParser = someParsers.get(this.attributeName);

        if (theParser == null) {
            theParser = TextImportParser.Text.DEFAULT_INSTANCE;
        }

        this.parser       = theParser;
        this.compareValue = theParser.map(this.getConfig().getValue());
    }

    /**
	 * Return the trimmed string value from the given {@link Attributes} for the
	 * {@link #attributeName} defined in the configuration.
	 * 
	 * @param anAttr
	 *        The text line to get the trimmed value from.
	 * @return The requested string, never <code>null</code>.
	 */
    protected Object getValue(Attributes anAttr) {
        String theValue = anAttr.getValue(this.attributeName);

        return this.parser.map(theValue);
    }

    /** 
     * Return the value to be used for compare.
     * 
     * @return    The requested value.
     */
    protected Object getCompareValue() {
        return this.compareValue;
    }

    /** 
     * Initialize the XML filters by the given map of {@link TextImportParser}s.
     * 
     * @param    aFilter        The containing the filters to be updated.
     * @param    someParsers    The parsers to be used in the filters later on.
     */
    public static void initFilter(TypedFilter aFilter, Map<String, TextImportParser<?>> someParsers) {
        if (aFilter instanceof XMLImportFilter) {
            ((XMLImportFilter) aFilter).init(someParsers);
        }

        ConfigurableFilter.Config<?> theConfig = ((AbstractConfigurableFilter<?, ?>) aFilter).getConfig();

        if (theConfig instanceof ConfigurableCompositeFilter.Config) {
			for (PolymorphicConfiguration<? extends TypedFilter> theFilter : ((ConfigurableCompositeFilter.Config<?>) theConfig)
				.getFilters()) {
				XMLImportFilter.initFilter(TypedConfigUtil.createInstance(theFilter), someParsers);
            }
        }
        else if (theConfig instanceof ConfigurableNotFilter.Config) {
			XMLImportFilter.initFilter(
				TypedConfigUtil.createInstance(((ConfigurableNotFilter.Config<?>) theConfig).getFilter()), someParsers);
        }
    }

    /**
     * Check if the found string value is equal to the expected one. 
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public static class Equals extends XMLImportFilter {

        /**
		 * Creates a {@link Equals} filter.
		 */
        public Equals(InstantiationContext context, Config config) {
            super(context, config);
        }

        @Override
        protected FilterResult matchesTypesafe(Attributes aLine) {
            return this.getValue(aLine).equals(this.getCompareValue()) ? FilterResult.TRUE : FilterResult.FALSE;
        }
    }

    /**
     * Check if the found string value is larger than the expected one. 
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public static class Larger extends XMLImportFilter {

        /**
		 * Creates a {@link Larger} filter.
		 */
        public Larger(InstantiationContext context, Config config) {
            super(context, config);
        }

        @Override
        protected FilterResult matchesTypesafe(Attributes aLine) {
            @SuppressWarnings("unchecked")
            Comparable<Object> theValue = (Comparable<Object>) this.getValue(aLine);

            return (theValue.compareTo(this.getCompareValue()) > 0) ? FilterResult.TRUE : FilterResult.FALSE;
        }
    }

    /**
     * Check if the found string value is smaller than the expected one. 
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public static class Smaller extends XMLImportFilter {

        /**
		 * Creates a {@link Smaller} filter.
		 */
        public Smaller(InstantiationContext context, Config config) {
            super(context, config);
        }

        @Override
        protected FilterResult matchesTypesafe(Attributes aLine) {
            @SuppressWarnings("unchecked")
            Comparable<Object> theValue = (Comparable<Object>) this.getValue(aLine);

            return (theValue.compareTo(this.getCompareValue()) < 0) ? FilterResult.TRUE : FilterResult.FALSE;
        }
    }

    /**
     * Check if the found string value is empty. 
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public static class Empty extends XMLImportFilter {

        /**
		 * Creates a {@link Empty} filter.
		 */
        public Empty(InstantiationContext context, Config config) {
            super(context, config);
        }

        @Override
        protected FilterResult matchesTypesafe(Attributes aLine) {
            Object theValue = this.getValue(aLine);

            return StringServices.isEmpty(theValue) ? FilterResult.TRUE : FilterResult.FALSE;
        }
    }

    /**
     * Check if the found string has an expected length. 
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public static class Length extends XMLImportFilter {

        private int length;

        /**
		 * Creates a {@link Length} filter.
		 */
        public Length(InstantiationContext context, Config config) {
            super(context, config);

            this.length = Integer.parseInt(config.getValue());
        }

        @Override
        protected FilterResult matchesTypesafe(Attributes aLine) {
            return this.length == this.getValue(aLine).toString().trim().length() ? FilterResult.TRUE : FilterResult.FALSE;
        }
    }
}
