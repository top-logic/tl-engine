/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.excel;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.importer.base.ListDataImportPerformer;
import com.top_logic.knowledge.wrap.Wrapper;

/**
 * Post processing for a map of values after the {@link ListDataImportPerformer performer} has
 * handled an attributed.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public interface ImportPerformerPostProcessor {

	/**
	 * Method called when an object has been created or updated for doing some additional operations
	 * on it.
	 * 
	 * @param anAttributed
	 *        object which has been created or updated.
	 * @param someValues
	 *        the value map. Must not be <code>null</code>.
	 * @param aModel
	 *        Model which is selected by the importer.
	 * @param created
	 *        <code>true</code> when given attributed has been created.
	 */
    void postProcessObject(Wrapper anAttributed, Map<String, Object> someValues, Wrapper aModel, boolean created);

    /**
	 * Simple implementation of interface which actually do nothing.
	 * 
	 * <p>
	 * This class is a place holder for configuration items with
	 * {@link ImportPerformerPostProcessor} to avoid null check.
	 * </p>
	 */
	public static class NoImportPerformerPostProcessor implements ImportPerformerPostProcessor {

		/**
		 * Singleton {@link NoImportPerformerPostProcessor} instance.
		 */
		public static final NoImportPerformerPostProcessor INSTANCE = new NoImportPerformerPostProcessor();

		/**
		 * Creates a new {@link NoImportPerformerPostProcessor}.
		 */
		protected NoImportPerformerPostProcessor() {
			// singleton instance
		}

		@Override
		public void postProcessObject(Wrapper anAttributed, Map<String, Object> someValues, Wrapper aModel,
				boolean isCreated) {
			// Do nothing
		}
	}

	/**
	 * Collection of different post performers called one after the other.
	 * 
	 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
    public static class CollectingImportPerformerPostProcessor<C extends CollectingImportPerformerPostProcessor.Config> implements ImportPerformerPostProcessor {
        
        @SuppressWarnings("javadoc")
        public interface Config extends PolymorphicConfiguration<CollectingImportPerformerPostProcessor<?>> {

            @InstanceFormat
            List<ImportPerformerPostProcessor> getPostProcessors();
        }

        // Attributes

		/** Configuration of this instance. */
        protected final C config;

        // Constructors

        /** 
         * Creates a {@link CollectingImportPerformerPostProcessor}.
         */
		public CollectingImportPerformerPostProcessor(@SuppressWarnings("unused") InstantiationContext aContext, C aConfig) {
            this.config = aConfig;
        }
        
        // Implementation of interface ImportPostProcessor
        
        @Override
        public void postProcessObject(Wrapper anAttributed, Map<String, Object> someValues, Wrapper aModel, boolean created) {
            for (ImportPerformerPostProcessor theProcessor : this.config.getPostProcessors()) {
				theProcessor.postProcessObject(anAttributed, someValues, aModel, created);
            }
        }
    }
}

