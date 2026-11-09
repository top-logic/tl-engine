/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.scriptable;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;

/**
 * Configuration referencing implementable interfaces.
 */
public interface Holder extends ConfigurationItem {

	/**
	 * Directly bound interface.
	 */
	PolymorphicConfiguration<Provider> getProvider();

	/**
	 * Interface bound through a sub-interface of the polymorphic configuration.
	 */
	CallbackConfig getCallback();

	/**
	 * Interface as list element type.
	 */
	List<PolymorphicConfiguration<? extends Listed>> getListed();

	/**
	 * Interface as map value type.
	 */
	Map<String, PolymorphicConfiguration<Mapped>> getMapped();

	/**
	 * Interface with type parameters.
	 */
	PolymorphicConfiguration<Generic<String>> getGeneric();

	/**
	 * Interface from the class path without source.
	 */
	PolymorphicConfiguration<Runnable> getRunnable();

	/**
	 * Second reference to an exported interface.
	 */
	PolymorphicConfiguration<Provider> getOtherProvider();

}
