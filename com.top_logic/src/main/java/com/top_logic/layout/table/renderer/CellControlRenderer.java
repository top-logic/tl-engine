/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.renderer;

import java.io.IOException;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.basic.ResourceRenderer;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.table.AbstractCellRenderer;
import com.top_logic.layout.table.CellRenderer;
import com.top_logic.layout.table.TableRenderer.Cell;

/**
 * {@link CellRenderer} that renders a table cell value through a {@link ControlProvider}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CellControlRenderer extends AbstractCellRenderer {

	public interface Config extends PolymorphicConfiguration<CellRenderer> {

		@Name(ATTRIBUTE_CONTROL_PROVIDER)
		@InstanceFormat
		@InstanceDefault(DefaultFormFieldControlProvider.class)
		ControlProvider getControlProvider();

		@Name(ATTRIBUTE_FALLBACK_RENDERER)
		@InstanceFormat
		@InstanceDefault(ResourceRenderer.class)
		Renderer<?> getFallbackRenderer();
	}

	private static final String ATTRIBUTE_CONTROL_PROVIDER = "controlProvider";

	private static final String ATTRIBUTE_FALLBACK_RENDERER = "fallbackRenderer";

	private ControlProvider _controlProvider;

	private Renderer<Object> _fallbackRenderer;

	/**
	 * Creates a {@link CellControlRenderer} with {@link ResourceRenderer} as fallback.
	 * 
	 * @param controlProvider
	 *        See {@link #getControlProvider()}.
	 */
	public CellControlRenderer(ControlProvider controlProvider) {
		this(controlProvider, ResourceRenderer.INSTANCE);
	}

	public CellControlRenderer(InstantiationContext context, Config config) throws ConfigurationException {
		setControlProvider(config.getControlProvider());
		setFallbackRenderer(config.getFallbackRenderer().generic());
	}

	/**
	 * Creates a {@link CellControlRenderer}.
	 * 
	 * @param controlProvider
	 *        See {@link #getControlProvider()}.
	 * @param fallbackRenderer
	 *        See {@link #getFallbackRenderer()}.
	 */
	public CellControlRenderer(ControlProvider controlProvider, Renderer<Object> fallbackRenderer) {
		assert controlProvider != null;
		assert fallbackRenderer != null;
		_controlProvider = controlProvider;
		_fallbackRenderer = fallbackRenderer;
	}


	/**
	 * The {@link ControlProvider} that is used to create {@link Control}s for application values in
	 * table cells.
	 */
	public ControlProvider getControlProvider() {
		return _controlProvider;
	}

	/**
	 * The {@link Renderer} to use for values, the {@link #getControlProvider()} does not create
	 * {@link Control}s.
	 */
	public Renderer<Object> getFallbackRenderer() {
		return _fallbackRenderer;
	}

	@Override
	public void writeCell(DisplayContext context, TagWriter out, Cell cell) throws IOException {
		Object value = cell.getValue();
		Control control = _controlProvider.createControl(value);
		if (control != null) {
			control.write(context, out);
		} else {
			_fallbackRenderer.write(context, out, value);
		}
	}

	private void setControlProvider(ControlProvider value) {
		assert value != null;
		_controlProvider = value;
	}

	private void setFallbackRenderer(Renderer<Object> value) {
		assert value != null;
		_fallbackRenderer = value;
	}

}
