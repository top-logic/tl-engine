/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.attr.storage.CacheValueFactory;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;
import com.top_logic.model.config.TypeRef;
import com.top_logic.model.impl.generated.TlModelFactory;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * {@link CacheValueFactory} resolving a configured {@link TLType} by its qualified name.
 * 
 * @see PolymorphicConfiguration
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TLTypeCacheFactory extends AbstractConfiguredInstance<TLTypeCacheFactory.Config>
		implements CacheValueFactory {

	/**
	 * Typed configuration interface definition for {@link TLTypeCacheFactory}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends PolymorphicConfiguration<TLTypeCacheFactory>, TypeRef {
		// configuration interface definition
	}

	/**
	 * Create a {@link TLTypeCacheFactory}.
	 * 
	 * @param context
	 *            the {@link InstantiationContext} to create the new object in
	 * @param config
	 *            the configuration object to be used for instantiation
	 */
	public TLTypeCacheFactory(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public Object getCacheValue(MOAttribute attribute, DataObject item, Object[] storage) {
		if (!ModelService.Module.INSTANCE.isActive() || ModelService.getApplicationModel() == null) {
			// May happen during startup :-(
			return null;
		}
		TLObject part;
		try {
			part = TLModelUtil.resolveQualifiedName(getConfig().getTypeSpec());
		} catch (TopLogicException ex) {
			if (getConfig().getTypeSpec().startsWith(TlModelFactory.TL_MODEL_STRUCTURE)) {
				// Bootstrap problem: During creation of model elements the corresponding type is
				// not yet created.
				return null;
			}
			throw ex;
		}
		if (!(part instanceof TLType)) {
			throw new ConfigurationError(I18NConstants.ERROR_NO_TL_TYPE__TYPE_SPEC.fill(getConfig().getTypeSpec()));
		}
		return part.tHandle();
	}

	@Override
	public boolean preserveCacheValue(MOAttribute cacheAttribute, DataObject changedObject, Object[] storage,
			MOAttribute changedAttribute) {
		return cacheAttribute.getStorage().getCacheValue(cacheAttribute, changedObject, storage) != null;
	}

}

