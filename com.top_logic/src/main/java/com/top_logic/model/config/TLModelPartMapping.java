/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.config;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.layout.form.values.edit.OptionMapping;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link OptionMapping} for {@link TLModelPart} options providing the qualified name of the
 * {@link TLModelPart} as selection.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TLModelPartMapping extends AbstractModelPartMapping<String> {

	/** Singleton {@link TLModelPartMapping} instance. */
	public static final TLModelPartMapping INSTANCE = new TLModelPartMapping();

	private TLModelPartMapping() {
		// singleton instance
	}

	@Override
	protected TLModelPart resolveName(String name) throws ConfigurationException {
		return (TLModelPart) TLModelUtil.resolveQualifiedName(name);
	}

	@Override
	protected String buildName(TLModelPart option) {
		return TLModelUtil.qualifiedName(option);
	}

}

