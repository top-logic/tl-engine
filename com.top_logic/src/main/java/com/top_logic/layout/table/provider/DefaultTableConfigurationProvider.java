/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.provider;


import com.top_logic.knowledge.wrap.WrapperAccessor;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.table.ITableRenderer;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.layout.table.renderer.DefaultTableRenderer;

/**
 * {@link TableConfigurationProvider} that has no additional model information.
 * 
 * @see GenericTableConfigurationProvider
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultTableConfigurationProvider implements TableConfigurationProvider {

	/**
	 * Singleton {@link DefaultTableConfigurationProvider} instance.
	 */
	public static final DefaultTableConfigurationProvider INSTANCE = new DefaultTableConfigurationProvider();

	/**
	 * Creates a {@link DefaultTableConfigurationProvider}.
	 */
	protected DefaultTableConfigurationProvider() {
		// Singleton constructor.
	}

	@Override
	public void adaptConfigurationTo(TableConfiguration table) {
		if (!table.hasTableRenderer()) {
			table.setTableRenderer(getTableRenderer());
		}
		ResourceProvider rowObjectResourceProvider = getRowObjectResourceProvider();
		if (rowObjectResourceProvider != null) {
			table.setRowObjectResourceProvider(rowObjectResourceProvider);
		}
	}

	@Override
	public void adaptDefaultColumn(ColumnConfiguration defaultColumn) {
		if (defaultColumn.getAccessor() == null) {
			defaultColumn.setAccessor(getAccessor());
		}
	}

	/**
	 * The table renderer to be used.
	 */
	protected ITableRenderer getTableRenderer() {
		return DefaultTableRenderer.newInstance();
	}

	/**
	 * The {@link ResourceProvider} used for the internal row objects.
	 */
	protected ResourceProvider getRowObjectResourceProvider() {
		return null;
	}

	/**
	 * Return the value accessor to be used.
	 */
	protected Accessor<?> getAccessor() {
		return WrapperAccessor.INSTANCE;
	}

}