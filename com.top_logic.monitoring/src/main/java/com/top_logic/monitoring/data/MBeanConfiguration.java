/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.data;

import com.top_logic.basic.config.NamedPolymorphicConfiguration;
import com.top_logic.basic.config.PolymorphicConfiguration;

/**
 * A {@link PolymorphicConfiguration} with a name property to identify MBeans.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public interface MBeanConfiguration<T extends NamedMonitor> extends NamedPolymorphicConfiguration<T> {
	// Pure marker configuration.
}
