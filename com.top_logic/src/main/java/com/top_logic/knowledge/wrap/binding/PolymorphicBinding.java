/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.binding;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.model.TLObject;

/**
 * {@link ImplementationBinding} that determines the application type based on the contents of a key
 * attribute.
 * 
 * @see Config#getKeyAttribute()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class PolymorphicBinding extends AbstractImplementationBinding
		implements ConfiguredInstance<PolymorphicBinding.Config> {

	/**
	 * Configuration options for {@link PolymorphicBinding}
	 */
	public interface Config extends PolymorphicConfiguration<PolymorphicBinding> {

		/**
		 * @see #getKeyAttribute()
		 */
		String KEY_ATTRIBUTE = "key-attribute";

		/**
		 * @see #getResourceTypePrefix()
		 */
		@Deprecated
		String RESOURCE_TYPE_PREFIX = "resource-type-prefix";

		/**
		 * The attribute containing discriminating values on which the decision of the concrete
		 * application type is based on.
		 * 
		 * @see Binding#getKey()
		 */
		@Name(KEY_ATTRIBUTE)
		String getKeyAttribute();

		/**
		 * @see #getKeyAttribute()
		 */
		void setKeyAttribute(String value);

		/**
		 * No longer in use, property kept for compatibility with legacy configuration.
		 */
		@Name(RESOURCE_TYPE_PREFIX)
		@Deprecated
		String getResourceTypePrefix();

		/**
		 * @see #getResourceTypePrefix()
		 */
		void setResourceTypePrefix(String value);

		/**
		 * Mapping of {@link #getKeyAttribute()} values to {@link Binding#getApplicationType()}s.
		 */
		@Key(Binding.KEY)
		Map<String, Binding> getBindings();

		/**
		 * Entry of {@link Config#getBindings()}.
		 */
		interface Binding extends ConfigurationItem {

			/**
			 * @see #getApplicationType()
			 */
			String APPLICATION_TYPE = "application-type";

			/**
			 * @see #getKey()
			 */
			String KEY = "key";

			/**
			 * Value of {@link Config#getKeyAttribute()} that triggers creating an application type
			 * given as {@link #getApplicationType()}.
			 */
			@Name(KEY)
			String getKey();

			/**
			 * @see #getKey()
			 */
			void setKey(String value);

			/**
			 * The application type to allocate, if {@link Config#getKeyAttribute()} contains the
			 * value {@link #getKey()}.
			 */
			@Name(APPLICATION_TYPE)
			Class<? extends Wrapper> getApplicationType();

			/**
			 * @see #getApplicationType()
			 */
			void setApplicationType(Class<? extends Wrapper> value);
		}
	}

	private final Config _config;

	private final Map<String, Class<? extends Wrapper>> _typeByKeyValue;

	private MOAttribute _keyAttribute;

	/**
	 * Creates a {@link PolymorphicBinding} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public PolymorphicBinding(InstantiationContext context, Config config) {
		_config = config;

		_typeByKeyValue = new HashMap<>();
		for (Config.Binding binding : config.getBindings().values()) {
			_typeByKeyValue.put(binding.getKey(), binding.getApplicationType());
		}
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public void initTable(MOClass staticType) {
		super.initTable(staticType);

		try {
			_keyAttribute = staticType.getAttribute(_config.getKeyAttribute());
		} catch (NoSuchAttributeException ex) {
			throw new KnowledgeBaseRuntimeException("No type key attribute '" + _config.getKeyAttribute()
				+ "' defined in table '" + staticTypeName() + "'.", ex);
		}
	}

	@Override
	public TLObject createBinding(KnowledgeItem item) {
		String dynamicType = keyValue(item);

		Class<? extends Wrapper> result = _typeByKeyValue.get(dynamicType);
		if (result == null) {
			throw new AssertionError("No implementation class binding in static type '" + staticTypeName()
				+ "' for dynamic type '" + dynamicType + "'.");
		}
		return wrapWith(result, item);
	}

	@Override
	public Class<? extends TLObject> getDefaultImplementationClassForTable() {
		return null;
	}

	private String keyValue(KnowledgeItem item) {
		return (String) item.getValue(_keyAttribute);
	}

	@Override
	public String toString() {
		return _typeByKeyValue.toString();
	}
}