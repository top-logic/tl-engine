/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.contextmenu.component.config;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.layout.basic.contextmenu.component.ContextMenuFactory;
import com.top_logic.layout.basic.contextmenu.component.factory.PlainComponentContextMenuFactory;

/**
 * {@link WithContextMenuFactory} that uses {@link PlainComponentContextMenuFactory} by default.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface WithPlainContextMenuFactory extends WithContextMenuFactory {

	@Override
	@ItemDefault(PlainComponentContextMenuFactory.class)
	PolymorphicConfiguration<? extends ContextMenuFactory> getContextMenuFactory();

}
