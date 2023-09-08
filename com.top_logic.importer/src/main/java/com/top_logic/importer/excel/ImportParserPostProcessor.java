/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.excel;

import java.util.List;
import java.util.Map;

import com.top_logic.base.office.excel.ExcelContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.importer.base.AbstractImportPerformer;
import com.top_logic.importer.logger.ImportLogger;

/**
 * Post processing for a map of values read from an excel file in the {@link AbstractExcelFileImportParser}.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public interface ImportParserPostProcessor {

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
    void postProcessMap(Map<String, Object> someValues, ExcelContext aContext, ImportLogger aLogger);

	void postProcessGlobal(List<Map<String, Object>> allRows, ImportLogger aLogger);

    /**
     * Simple implementation of interface which will do nothing. 
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
    public static class SimpleImportParserPostProcessor<C extends SimpleImportParserPostProcessor.Config> implements ImportParserPostProcessor {

        @SuppressWarnings("javadoc")
        public interface Config extends PolymorphicConfiguration<SimpleImportParserPostProcessor<?>> {
            
        }

        // Attributes

        protected final C config;

        // Constructors

        /** 
         * Creates a {@link SimpleImportParserPostProcessor}.
         */
        public SimpleImportParserPostProcessor(InstantiationContext aContext, C aConfig) {
            this.config = aConfig;
        }

        // Implementation of interface ImportPostProcessor

        @Override
        public void postProcessMap(Map<String, Object> someValues, ExcelContext aContext, ImportLogger aLogger) {
            // Nothing to do here...
        }

        // Protected methods

        /** 
         * Return the model from the given map of values.
         * 
         * @param    someValues    The values handed over by {@link #postProcessMap(Map, ExcelContext, ImportLogger)}.
         * @return   The model from the assistant component. 
         */
        protected Object getModel(Map<String, Object> someValues) {
            return someValues.get(AbstractImportPerformer.MODEL);
        }

		/**
		 * @see com.top_logic.importer.excel.ImportParserPostProcessor#postProcessGlobal(java.util.List,
		 *      com.top_logic.importer.logger.ImportLogger)
		 */
		@Override
		public void postProcessGlobal(List<Map<String, Object>> allRows, ImportLogger aLogger) {
			// do nothing here
		}
    }
}

