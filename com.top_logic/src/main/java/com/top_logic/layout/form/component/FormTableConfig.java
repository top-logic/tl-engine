/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.component;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.layout.basic.contextmenu.ContextMenuProvider;
import com.top_logic.layout.basic.contextmenu.component.factory.adapter.TypeBasedContextMenuProvider;
import com.top_logic.layout.table.model.TableConfig;

/**
 * {@link TableConfig} for tables in the context of {@link FormComponent} automatically adds
 * type-based context menu commands.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface FormTableConfig extends TableConfig {

	@Override
	@ItemDefault(TypeBasedContextMenuProvider.class)
	@ImplementationClassDefault(TypeBasedContextMenuProvider.class)
	PolymorphicConfiguration<? extends ContextMenuProvider> getContextMenu();

}
