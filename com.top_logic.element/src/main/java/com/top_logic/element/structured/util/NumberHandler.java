/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.structured.util;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;

/**
 * Generator for unique IDs for objects.
 *
 * @author <a href=mailto:tdi@top-logic.com>tdi</a>
 */
@Label("ID generator")
public interface NumberHandler {

	/**
	 * {@link ConfigurationItem} which a number handler must be used to configur
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface NumberHandlerConfig<I extends NumberHandler>
			extends PolymorphicConfiguration<I>, NamedConfigMandatory {
		// Pure sum interface
	}

    /**
	 * Generates a unique ID in the context of the given object.
	 * 
	 * @param context
	 *        The object describing the context of the generated ID.
	 * @return An identifier that is unique in the given context. It depends on the implementation
	 *         of the {@link NumberHandler} what type the generated ID has. It may be a
	 *         {@link String} with a certain format, or a plain {@link Number}.
	 */
	Object generateId(Object context) throws GenerateNumberException;

}
