/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor.config;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.model.TLClass;
import com.top_logic.model.util.TLModelPartRef;

/**
 * Base interface for layout template parameter definitions to provide an optional type parameter.
 *
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
@Abstract
public interface OptionalTypeTemplateParameters extends ConfigurationItem {

	/**
	 * Configuration option name for {@link #getType()}.
	 */
	public static final String TYPE = "type";

	/**
	 * Common type for all displayed elements.
	 */
	@Name(TYPE)
	TLModelPartRef getType();

	/**
	 * @see #getType()
	 */
	void setType(TLModelPartRef value);

	/**
	 * Resolves the {@link #getType()} referenced by the given configuration.
	 */
	static TLClass resolve(OptionalTypeTemplateParameters self) {
		try {
			TLModelPartRef typeRef = self.getType();
			if (typeRef == null) {
				return null;
			}
			return typeRef.resolveClass();
		} catch (ConfigurationException ex) {
			return null;
		}
	}

}
