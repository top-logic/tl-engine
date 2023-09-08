/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased;

import java.util.Map;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.util.ResKey;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.attr.storage.CacheValueFactory;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;
import com.top_logic.model.config.TypeRef;
import com.top_logic.model.impl.generated.TlModelFactory;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * {@link CacheValueFactory} resolving a configured {@link TLType} by its qualified name. The
 * concrete type is derived from some attribute value.
 * 
 * @see TLTypeCacheFactory
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class PolymorphicTypeCacheFactory extends AbstractConfiguredInstance<PolymorphicTypeCacheFactory.Config>
		implements CacheValueFactory {

	/**
	 * Typed configuration interface definition for {@link PolymorphicTypeCacheFactory}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends PolymorphicConfiguration<PolymorphicTypeCacheFactory> {

		/**
		 * The attribute whose value decides about the actual {@link TLType}.
		 */
		@Mandatory
		String getKeyAttribute();

		/**
		 * Mapping from possible values if the {@link #getKeyAttribute() key attribute} to the
		 * represented {@link TLType}.
		 */
		@Key(TypeMapping.VALUE)
		@EntryTag("entry")
		Map<String, TypeMapping> getValueTypeMapping();
		
	}

	/**
	 * {@link TypeRef} that is used for a special value.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface TypeMapping extends TypeRef {

		/** Configuration name of {@link #getValue()}. */
		String VALUE = "value";

		/**
		 * The expected value which represents {@link #getTypeSpec()}.
		 */
		@Mandatory
		@Name(VALUE)
		String getValue();
	}

	/**
	 * Create a {@link PolymorphicTypeCacheFactory}.
	 * 
	 * @param context
	 *            the {@link InstantiationContext} to create the new object in
	 * @param config
	 *            the configuration object to be used for instantiation
	 */
	public PolymorphicTypeCacheFactory(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public Object getCacheValue(MOAttribute attribute, DataObject item, Object[] storage) {
		if (!ModelService.Module.INSTANCE.isActive() || ModelService.getApplicationModel() == null) {
			// May happen during startup :-(
			return null;
		}

		Config config = getConfig();

		MOClass metaObject = (MOClass) item.tTable();
		MOAttribute typeAttribute = metaObject.getAttributeOrNull(config.getKeyAttribute());
		if (typeAttribute == null) {
			throw new KnowledgeBaseRuntimeException(
				"Item " + item + " does not have the attribute " + config.getKeyAttribute());
		}
		Object typeValue = typeAttribute.getStorage().getCacheValue(typeAttribute, item, storage);
		if (typeValue == null) {
			return null;
		}

		TypeMapping correspondingTypeSpec = config.getValueTypeMapping().get(typeValue);
		if (correspondingTypeSpec == null) {
			throw new ConfigurationError(I18NConstants.ERROR_NO_TL_TYPE_FOUND_FOR__VALUE.fill(typeValue));
		}
		TLObject part;
		try {
			part = TLModelUtil.resolveQualifiedName(correspondingTypeSpec.getTypeSpec());
		} catch (TopLogicException ex) {
			if (correspondingTypeSpec.getTypeSpec().startsWith(TlModelFactory.TL_MODEL_STRUCTURE)) {
				// Bootstrap problem: During creation of model elements the corresponding type is
				// not yet created.
				return null;
			}
			throw ex;
		}
		if (!(part instanceof TLType)) {
			throw new ConfigurationError(unknownTypeMsg(typeValue, correspondingTypeSpec));
		}
		return part.tHandle();
	}

	private static ResKey unknownTypeMsg(Object typeValue, TypeMapping typeSpec) {
		return I18NConstants.ERROR_NO_TL_TYPE_FOR_VALUE__TYPE_SPEC__VALUE.fill(typeSpec.getTypeSpec(),
			typeValue);
	}

	@Override
	public boolean preserveCacheValue(MOAttribute cacheAttribute, DataObject changedObject, Object[] storage,
			MOAttribute changedAttribute) {
		if (changedAttribute.getName().equals(getConfig().getKeyAttribute())) {
			return false;
		}
		return cacheAttribute.getStorage().getCacheValue(cacheAttribute, changedObject, storage) != null;
	}

}

