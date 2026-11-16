/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.model;

import com.top_logic.basic.config.CommaSeparatedListValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.model.TLObject;

/**
 * Format of a configuration property holding a list of {@link TLObject}s whose values an agent
 * supplies.
 *
 * <p>
 * Each element is an identifier in the form of {@link AgentObjectFormat}, resolved by it with the
 * same rights. An agent passes the identifiers as JSON array; the type of the elements is restricted
 * by {@link com.top_logic.model.format.ObjectType}, which checks every element.
 * </p>
 */
public class AgentObjectListFormat extends CommaSeparatedListValueProvider<TLObject> {

	/**
	 * The {@link AgentObjectListFormat} instance.
	 */
	public static final AgentObjectListFormat INSTANCE = new AgentObjectListFormat();

	private AgentObjectListFormat() {
		super();
	}

	@Override
	protected TLObject parseSingleValue(String propertyName, CharSequence propertyValue, String singlePropertyValue)
			throws ConfigurationException {
		return AgentObjectFormat.INSTANCE.getValue(propertyName, singlePropertyValue.trim());
	}

	@Override
	protected String formatSingleValue(TLObject singleConfigValue) {
		return AgentObjectFormat.INSTANCE.getSpecification(singleConfigValue);
	}

	@Override
	public ConfigurationValueProvider<TLObject> getElementFormat() {
		return AgentObjectFormat.INSTANCE;
	}

}
