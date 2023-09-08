/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.excel.extractor;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.base.office.excel.ExcelContext;
import com.top_logic.basic.Named;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.tools.NameBuilder;
import com.top_logic.importer.excel.AbstractExcelFileImportParser;
import com.top_logic.importer.excel.transformer.Transformer;
import com.top_logic.importer.logger.ImportLogger;

/**
 * Extract values out of the definition of known objects.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public abstract class AbstractDataExtractor<O extends Named, C extends AbstractDataExtractor.Config> implements Extractor {

	/**
	 * Configuration for the AbstractDataExtractor. 
	 * 
	 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
	public interface Config extends Extractor.Config {

        /** Meta attributes to be excluded. */
        @Format(CommaSeparatedStrings.class)
        List<String> getExcludeAttributes();
    }

    /** Configuration of this instance. */
	protected final C config;

    /**
	 * Creates a {@link AbstractDataExtractor}.
	 */
	public AbstractDataExtractor(@SuppressWarnings("unused") InstantiationContext aContext, C aConfig) {
        this.config = aConfig;
    }

	/**
	 * Return all objects to create transformers for.
	 * 
	 * @return The requested collection of objects.
	 */
	protected abstract Collection<O> getObjects();

	/** 
	 * Provide a name as used in the excel sheet for identifying the correct {@link #getObjects() object}.
	 * 
	 * @param    anObject     Object to get the transformer for.
	 * @return   The I18N name used in the excel sheet.
     * @see      #getObjects()
	 */
	protected abstract String getI18NName(O anObject);

    /** 
     * Create a transformer for the given object.
     * 
     * <p>This method will try to create a transformer for the given object.
     * When the type is too complex or didn't provide enough information for the 
     * transformer, this method will return <code>null</code>.</p>
     * 
     * @param    anObject     Object to get the transformer for.
     * @return   The new created transformer or <code>null</code> when type of object is not supported.
     * @see      #getObjects()
     */
    protected abstract Transformer<?> createInnerTransformer(O anObject);

    @Override
    public Map<String, Object> extract(ExcelContext aContext, AbstractExcelFileImportParser<?> aParser, ImportLogger aLogger) {
        Map<String, Object> theMap = new HashMap<>();

        for (Entry<String, NamedTransformer<?>> theEntry : this.getTransformers().entrySet()) {
            this.addValue(theMap, aContext, theEntry.getValue(), aParser, aLogger);
        }

        return theMap;
    }

    /** 
     * Read the current row in the excel context and create entries in the given map for valid values.
     * 
     * @param    aMap             Map to store the read value.
     * @param    aContext         Source to get the value from.
     * @param    aTransformer     Transformer for extracting value from context.
     * @param    aParser          Parser calling this extractor.
     * @param    aLogger          For logging messages to the user.
     */
    protected void addValue(Map<String, Object> aMap, ExcelContext aContext, NamedTransformer<?> aTransformer, AbstractExcelFileImportParser<?> aParser, ImportLogger aLogger) {
        Object theValue = aTransformer.transformer.transform(aContext, aTransformer.i18n, aParser, aLogger);

        if (theValue != null) {
            aMap.put(aTransformer.name, theValue);
        }
    }

    /**
	 * Build the map of transformers out of the {@link #getObjects()}.
	 * 
	 * @return The requested map of transformers, never <code>null</code>.
	 */
    protected Map<String, NamedTransformer<?>> getTransformers() {
        Map<String, NamedTransformer<?>> theMap     = new HashMap<>();
        List<String>                     theExclude = this.config.getExcludeAttributes();

        for (O theObject : this.getObjects()) {
            if (!theExclude.contains(theObject.getName())) {
                String              theKey         = this.getI18NName(theObject);
                NamedTransformer<?> theTransformer = this.createNamedTransformer(theKey, theObject);

                if ((theKey != null) && (theTransformer != null)) {
                    theMap.put(theKey, theTransformer);
                }
            }
        }

        return theMap;
    }

    /** 
     * Create a transformer for the given object.
     * 
     * <p>This method will try to create a transformer for the given object.
     * When the type is too complex or didn't provide enough information for the 
     * transformer, this method will return <code>null</code>.</p>
     * 
     * @param    anI18N       I18N translation of the object name.
     * @param    anObject     Object to get the transformer for.
     * @return   The new created transformer or <code>null</code> when type of object is not supported.
     */
    protected NamedTransformer<?> createNamedTransformer(String anI18N, O anObject) {
        Transformer<?> theTransformer = this.createInnerTransformer(anObject);

        return (theTransformer != null) ? this.createNamedTransformer(anI18N, anObject, theTransformer) : null;
    }

	/** 
	 * Create the named transformer wrapping the given transformer.
	 * 
     * @param    anI18N       I18N translation of the object name.
     * @param    anObject     Object to get the transformer for.
	 * @param    aTransformer    The transformer to be wrapped, must not be <code>null</code>.
     * @return   The new created transformer, never <code>null</code>.
	 */
    @SuppressWarnings("unchecked")
	protected NamedTransformer<Object> createNamedTransformer(String anI18N, O anObject, Transformer<?> aTransformer) {
		return new NamedTransformer<>(anI18N, anObject.getName(), (Transformer<Object>) aTransformer);
	}

	/**
	 * Enriched transformer with information about the translated and also the objects name.
	 * 
	 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
	public static class NamedTransformer<T> {

		// Attributes

		/** I18N name to be used. */
		public final String i18n;

		/** Technical name to be used. */
		public final String name;

		/** Transformer to do the work. */
		public final Transformer<T> transformer;

		// Constructors

		/**
		 * Creates a {@link NamedTransformer}.
		 */
		public NamedTransformer(String anI18N, String aName, Transformer<T> aTransformer) {
			this.i18n        = anI18N;
			this.name        = aName;
			this.transformer = aTransformer;
		}

		// Overridden methods from Object

		@Override
		public String toString() {
			return new NameBuilder(this)
				.add("i18n", this.i18n)
				.add("name", this.name)
				.add("transformer", this.transformer)
				.build();
		}
	}
}

