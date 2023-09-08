/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.text;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfiguration;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.basic.util.ResourcesModule;

/**
 * Convert a string to the defined type of object.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public abstract class TextImportParser<T> implements Mapping<String, T>, ConfiguredInstance<TextImportParser.Config> {

    /**
     * Base configuration for a {@link TextImportParser}. 
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public interface Config extends PolymorphicConfiguration<TextImportParser<?>>, NamedConfiguration {
    }

    private final Config config;

    /** 
     * Creates a {@link TextImportParser}.
     */
    public TextImportParser(InstantiationContext aContext, Config aConfig) {
        this.config = aConfig;
    }

    @Override
    public Config getConfig() {
        return config;
    }

    /**
     * Convert the string to a none null, trimmed string. 
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public static class Text extends TextImportParser<String> {

        public static final Text DEFAULT_INSTANCE = new Text(null, null);

        // Constructors

        /** 
         * Singleton constructor.
         */
        public Text(InstantiationContext aContext, Config aConfig) {
            super(aContext, aConfig);
        }

        // Overridden methods from TextImportParser

        @Override
        public String map(String input) {
            return (input != null) ? input.trim() : "";
        }
    }

    /**
     * Convert the string to a date or null (when string is <code>null</code> or empty). 
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public static class Date extends TextImportParser<java.util.Date> {

        public interface Config extends TextImportParser.Config {

            @Mandatory
            String getFormat();
        }

        // Attributes

        private SimpleDateFormat format;

        // Constructors
        
        /** 
         * Creates a {@link Date}.
         */
        public Date(InstantiationContext aContext, Config aConfig) {
            super(aContext, aConfig);

			this.format = CalendarUtil.newSimpleDateFormat(aConfig.getFormat());
        }

        // Overridden methods from TextImportParser

        @Override
        public java.util.Date map(String input) {
            try {
                String theString = (input != null) ? input.trim() : "";

                return theString.isEmpty() ? null : this.format.parse(theString);
            }
            catch (ParseException ex) {
                throw new IllegalArgumentException("Invalid date format in '" + input + "' (defined format is '" + this.format + "')!", ex);
            }
        }
    }
    
    /**
     * Convert the string to a boolean or null (when string is <code>null</code> or empty). 
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public static class Boolean extends TextImportParser<java.lang.Boolean> {

        public interface Config extends TextImportParser.Config {
            
            @Mandatory
            String getTrue();
        }

        // Attributes

        private String trueValue;

        // Constructors

        /** 
         * Creates a {@link Boolean}.
         */
        public Boolean(InstantiationContext aContext, Config aConfig) {
            super(aContext, aConfig);

            this.trueValue = aConfig.getTrue();
        }

        // Overridden methods from TextImportParser

        @Override
        public java.lang.Boolean map(String input) {
            return (input == null) ? null : this.trueValue.equals(input.trim());
        }
    }

    /**
     * Convert the string to a number or null (when string is <code>null</code> or empty). 
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public static abstract class AbstractNumber<T extends Number> extends TextImportParser<T> {

        public interface Config extends TextImportParser.Config {

            @Mandatory
            String getFormat();

			/**
			 * The {@link Locale} for the {@link #getFormat()}.
			 * 
			 * @see ResourcesModule#localeFromString(String) The format of the locale.
			 */
			@StringDefault("de")
            String getLocale();
        }

        // Attributes

        private NumberFormat format;

        // Constructors

        /** 
         * Creates a {@link AbstractNumber}.
         */
        public AbstractNumber(InstantiationContext aContext, Config aConfig) {
            super(aContext, aConfig);

			Locale theLocale = ResourcesModule.localeFromString(aConfig.getLocale());

            this.format = new DecimalFormat(aConfig.getFormat(), DecimalFormatSymbols.getInstance(theLocale));
        }

        // Protected methods

        /** 
         * Convert the given string to a number or <code>null</code>.
         * 
         * @param    input   The string to be converted, may be <code>null</code>.
         * @return   The requested {@link Number} or <code>null</code>.
         */
        protected Number doParse(String input) {
            try {
                String theString = (input != null) ? input.trim() : "";

                return theString.isEmpty() ? null : this.format.parse(theString);
            }
            catch (ParseException ex) {
                throw new IllegalArgumentException("Invalid number in '" + input + "' (defined format is '" + this.format + "')!", ex);
            }
        }
    }

    /**
     * Convert the string to a float or null (when string is <code>null</code> or empty). 
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public static class Float extends AbstractNumber<java.lang.Float> {

        // Constructors
        
        /** 
         * Creates a {@link Float}.
         */
        public Float(InstantiationContext aContext, AbstractNumber.Config aConfig) {
            super(aContext, aConfig);
        }

        // Overridden methods from AbstractNumber

        @Override
        public java.lang.Float map(String input) {
            Number theNumber = this.doParse(input);

            return (theNumber == null) ? null : theNumber.floatValue();
        }
    }

    /**
     * Convert the string to an integer or null (when string is <code>null</code> or empty). 
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public static class Integer extends AbstractNumber<java.lang.Integer> {

        // Constructors
        
        /** 
         * Creates a {@link Integer}.
         */
        public Integer(InstantiationContext aContext, AbstractNumber.Config aConfig) {
            super(aContext, aConfig);
        }

        // Overridden methods from AbstractNumber

        @Override
        public java.lang.Integer map(String input) {
            Number theNumber = this.doParse(input);

            return (theNumber == null) ? null : theNumber.intValue();
        }
    }
    
    /**
     * Convert the string to a long or null (when string is <code>null</code> or empty). 
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public static class Long extends AbstractNumber<java.lang.Long> {

        // Constructors
        
        /** 
         * Creates a {@link Long}.
         */
        public Long(InstantiationContext aContext, AbstractNumber.Config aConfig) {
            super(aContext, aConfig);
        }

        // Overridden methods from AbstractNumber

        @Override
        public java.lang.Long map(String input) {
            Number theNumber = this.doParse(input);

            return (theNumber == null) ? null : theNumber.longValue();
        }
    }
}

