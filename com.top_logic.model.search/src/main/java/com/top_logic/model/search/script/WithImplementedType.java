/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.script;

import java.lang.reflect.Type;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.script.ScriptImplementationProvider;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.format.JavaTypeFormat;

/**
 * Configuration of a script implementation that knows the generic Java type it implements.
 *
 * <p>
 * The implemented type is not part of the configuration text of the script: when a script is
 * read for a configuration property, the {@link ScriptImplementationProvider} derives it from the
 * generic type of that property, e.g. <code>Mapping&lt;Object, String&gt;</code> for a property of
 * type <code>PolymorphicConfiguration&lt;Mapping&lt;Object, String&gt;&gt;</code>. The type
 * arguments determine the Java types the script result is converted to.
 * </p>
 *
 * @see TLScriptImplementationProvider
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface WithImplementedType extends ConfigurationItem {

	/** Configuration name of {@link #getImplementedType()}. */
	String IMPLEMENTED_TYPE = "implemented-type";

	/**
	 * The interface implemented by the script, parameterized with the type arguments of the
	 * configuration property the script is configured for.
	 *
	 * <p>
	 * The value contains no type variables. If not set, the type arguments of the implemented
	 * interface are unknown and the script result is converted to the bounds of its type
	 * parameters.
	 * </p>
	 *
	 * @see PolymorphicConfiguration
	 */
	@Name(IMPLEMENTED_TYPE)
	@Format(JavaTypeFormat.class)
	@Hidden
	Type getImplementedType();

	/**
	 * @see #getImplementedType()
	 */
	void setImplementedType(Type value);

}
