/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

/**
 * A plain {@link PolymorphicConfiguration} with a {@link #getName() name}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface NamedPolymorphicConfiguration<T> extends PolymorphicConfiguration<T>, NamedConfigMandatory {

	// Pure sum interface.

}
