/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.util;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.element.meta.kbbased.AbstractWrapperResolver;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.element.model.ModelFactory;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.model.TLClass;
import com.top_logic.model.util.TLModelUtil;

/**
 * Description of a class type.
 * 
 * <p>
 * This interface provides notations for "structure"/"element" description (old way) and also for
 * "type" (which is the new way).
 * </p>
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public interface TypeReferenceConfig extends ConfigurationItem {

	/** Property name of {@link #getType()}. */
	String TYPE = "type";

	/** Structure name the {@link Wrapper} lives in. */
	@Deprecated
	String getStructure();

	/** Meta element name of the {@link Wrapper} to be created. */
	@Deprecated
	String getElement();

	/** Qualified type name of the {@link Wrapper} to be created. */
	@Nullable
	@Name(TYPE)
	String getType();

	/**
	 * Resolve a meta element out of a {@link TypeReferenceConfig}. 
	 * 
	 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
	public static class Resolver {

		/**
		 * Resolve a meta element out of the given configuration.
		 * 
		 * @param aConfig
		 *        Configuration to be used for resolving.
		 * @return The requested meta element.
		 * @throws ConfigurationException
		 *         When configuration is invalid.
		 */
		public static TLClass getMetaElement(TypeReferenceConfig aConfig) throws ConfigurationException {
			String theType = aConfig.getType();

			if (theType != null) {
				return (TLClass) TLModelUtil.findType(theType);
			} else {
				String theMEName = aConfig.getElement();

				return (TLClass) ((AbstractWrapperResolver) DynamicModelService
					.getFactoryFor(aConfig.getStructure())).getModule().getType(theMEName);
			}
		}

		/**
		 * Resolve a model factory out of the given configuration.
		 * 
		 * @param aConfig
		 *        Configuration to be used for resolving.
		 * @return The requested model factory.
		 * @throws ConfigurationException
		 *         When configuration is invalid.
		 */
		public static ModelFactory getFactory(TypeReferenceConfig aConfig) throws ConfigurationException {
			return Resolver.getFactory(Resolver.getMetaElement(aConfig));
		}

		/**
		 * Resolve a model factory out of the given meta element.
		 * 
		 * @param aME
		 *        The element to get the factory from.
		 * @return The requested model factory.
		 */
		public static ModelFactory getFactory(TLClass aME) {
			return DynamicModelService.getFactoryFor(aME.getModule().getName());
		}
	}
}

