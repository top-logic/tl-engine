/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.grid;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.table.CellRenderer;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.tree.renderer.RowTypeCellRenderer;
import com.top_logic.layout.tree.renderer.RowTypeCellRenderer.Config;
import com.top_logic.mig.html.HTMLConstants;

/**
 * Renders an image in front of the control that will be created for the given
 * cell if the following preconditions are met:
 * 
 * <ol>
 *   <li>The Application model is an {@link ObjectTableModel} with {@link FormGroup}s as row objects.</li>
 *   <li>The {@link FormGroup} has a property called {@link GridComponent#PROP_ATTRIBUTED} with an {@link Wrapper}.</li>
 * </ol>
 * 
 * @author    <a href=mailto:mga@top-logic.com>Michael Gänsler</a>
 */
public class GridIconCellRenderer implements HTMLConstants {

	/**
	 * Singleton {@link GridIconCellRenderer} instance.
	 */
	public static final CellRenderer INSTANCE;

	static {
		INSTANCE = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config());
	}

	private static Config config() {
		try {
			Config config = TypedConfiguration.newConfigItem(RowTypeCellRenderer.Config.class);
			config.setResourceProvider(
				TypedConfiguration.createConfigItemForImplementationClass(TableGridTypeProvider.class));
			return config;
		} catch (ConfigurationException ex) {
			throw new ConfigurationError(ex);
		}
	}

}
