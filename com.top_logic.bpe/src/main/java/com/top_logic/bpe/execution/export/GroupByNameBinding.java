/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.execution.export;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.model.io.AttributeValueBinding;
import com.top_logic.model.search.providers.KeyValueAttributeBinding;
import com.top_logic.tool.boundsec.wrap.Group;

/**
 * {@link AttributeValueBinding} that is able to serialize attributes of type {@link Group} to XML.
 */
public class GroupByNameBinding extends KeyValueAttributeBinding<GroupByNameBinding.Config<?>> {

	/**
	 * Configuration options for {@link GroupByNameBinding}.
	 */
	public interface Config<I extends GroupByNameBinding> extends KeyValueAttributeBinding.Config<I> {

		@Override
		@StringDefault("group")
		String getReferenceTag();

		@Override
		@FormattedDefault("name")
		List<String> getKeyAttributes();

	}

	/**
	 * Creates a {@link GroupByNameBinding} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public GroupByNameBinding(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	protected Object resolveKey(Object key) {
		return Group.getGroupByName(key.toString());
	}

	@Override
	protected Object createKey(Object value) {
		return ((Group) value).getName();
	}

}
