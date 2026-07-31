/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

/**
 * Interface for classes configured through {@link PolymorphicConfiguration}s.
 *
 * <p>
 * A class instantiated from a {@link PolymorphicConfiguration} implements this interface, so that
 * the configuration an instance was built from can be read back off that instance — by the
 * configuration editor showing it, by code deriving a variant of it, and by an inspector displaying
 * it. {@link AbstractConfiguredInstance} supplies the implementation, which is why a configured
 * class usually extends it rather than implementing this interface itself.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ConfiguredInstance<C extends PolymorphicConfiguration<?>> {
	
	/**
	 * The configuration of this instance.
	 */
	C getConfig();

}
