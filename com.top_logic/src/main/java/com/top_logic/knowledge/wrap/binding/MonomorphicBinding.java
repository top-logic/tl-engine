/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.binding;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.model.TLObject;

/**
 * {@link ImplementationBinding} that uses a single type for all objects in the same table.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MonomorphicBinding extends AbstractImplementationBinding
		implements ConfiguredInstance<MonomorphicBinding.Config> {

	/**
	 * Configuration options for {@link MonomorphicBinding}.
	 */
	public interface Config extends PolymorphicConfiguration<MonomorphicBinding> {

		/**
		 * The common application type of all objects in the table.
		 */
		@Name("application-type")
		Class<? extends Wrapper> getApplicationType();

		/**
		 * @see #getApplicationType()
		 */
		void setApplicationType(Class<? extends Wrapper> value);

	}

	private final Config _config;
	private final Class<? extends Wrapper> _wrapperClass;

	/**
	 * Creates a {@link MonomorphicBinding} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public MonomorphicBinding(InstantiationContext context, Config config) {
		_config = config;
		_wrapperClass = config.getApplicationType();
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public TLObject createBinding(KnowledgeItem item) {
		String staticType = item.tTable().getName();
		assert staticTypeName().equals(staticType) : "Mismatch of static type, expected '"
			+ staticTypeName() + "', got '" + staticType + "'.";

		return wrapWith(_wrapperClass, item);
	}

	@Override
	public Class<? extends TLObject> getDefaultImplementationClassForTable() {
		return _wrapperClass;
	}

	@Override
	public String toString() {
		return staticTypeName() + " => " + _wrapperClass.getName();
	}
}