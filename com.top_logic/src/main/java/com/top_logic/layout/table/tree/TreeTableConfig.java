/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.tree;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.layout.basic.contextmenu.ContextMenuProvider;
import com.top_logic.layout.form.component.FormTableConfig;

/**
 * {@link FormTableConfig} choosing {@link TreeTableContextMenuProvider} by default.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TreeTableConfig extends FormTableConfig {

	@Override
	@ItemDefault(TreeTableContextMenuProvider.class)
	@ImplementationClassDefault(TreeTableContextMenuProvider.class)
	PolymorphicConfiguration<? extends ContextMenuProvider> getContextMenu();

}
