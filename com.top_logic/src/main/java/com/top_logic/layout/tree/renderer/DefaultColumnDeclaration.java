/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.renderer;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.util.css.CssUtil;

/**
 * Default {@link ColumnDeclaration} implementation for programatic
 * construction.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultColumnDeclaration implements ColumnDeclaration {

    private final int type;

	private ControlProvider controlProvider;

	private Renderer<Object> renderer;

	private ResKey headerKey;

	private Renderer<Object> headerRenderer;

	private String columnWidth;
	private String headerStyle;
	private String style;
	private boolean selectable = true;

	/** One of the {@link ColumnDeclaration} _HEADER constants */
	private int headerType = DEFAULT_HEADER;

	public DefaultColumnDeclaration(int type) {
		assert type == DEFAULT_COLUMN || type == RENDERED_COLUMN
		    || type == CONTROL_COLUMN || type == TEMPLATE_COLUMN;
		this.type = type;
	}

    public DefaultColumnDeclaration(ControlProvider cp) {
        this.type            = CONTROL_COLUMN;
        this.controlProvider = cp;
    }

	public DefaultColumnDeclaration(Renderer<Object> aRenderer) {
        this.type     = RENDERED_COLUMN;
        this.renderer = aRenderer;
    }

    @Override
	public int getColumnType() {
		return type;
	}

	@Override
	public ControlProvider getControlProvider() {
		return controlProvider;
	}

	public void setControlProvider(ControlProvider controlProvider) {
	    assert type == CONTROL_COLUMN;
		this.controlProvider = controlProvider;
	}

	@Override
	public Renderer<Object> getRenderer() {
		return renderer;
	}

	public void setRenderer(Renderer<Object> renderer) {
		assert type == RENDERED_COLUMN || type == DEFAULT_COLUMN;
		this.renderer = renderer;
	}

	@Override
	public ResKey getHeaderKey() {
		return headerKey;
	}

	public void setHeaderKey(ResKey headerKey) {
		this.headerKey = headerKey;
	}

	@Override
	public Renderer<Object> getHeaderRenderer() {
		return headerRenderer;
	}

	public void setHeaderRenderer(Renderer<?> headerRenderer) {
		this.headerRenderer = headerRenderer.generic();
	}

	@Override
	public int getHeaderType() {
		return headerType;
	}

	/**
	 * @param headerType One of the {@link ColumnDeclaration} _HEADER constants
	 */
	public void setHeaderType(int headerType) {
		this.headerType = headerType;
	}

	@Override
	public String getHeaderStyle() {
        return (this.headerStyle);
    }

	/**
	 * Sets the new header style. Must not contain the 'width' part of the style
	 * 
	 * @param headerStyle
	 *        the new style for the header in HTML-style attribute compatible form or
	 *        <code>null</code>.
	 */
	public void setHeaderStyle(String headerStyle) {
		this.headerStyle = CssUtil.terminateStyleDefinition(headerStyle);
    }

	@Override
	public String getStyle() {
        return (this.style);
    }

	/**
	 * Sets the new style for the column. Must not contain the 'width' part of the style
	 * 
	 * @param style
	 *        the new style for the header in HTML-style attribute compatible form or
	 *        <code>null</code>.
	 */
	public void setStyle(String style) {
		this.style = CssUtil.terminateStyleDefinition(style);
    }
	
	@Override
	public String getWidthStyle() {
		return (this.columnWidth);
	}
	
	/**
	 * Sets the width of the column. It must be of the form "xx<i>Unit</i>" where xx is the size and
	 * <i>Unit</i> the unit of the size, e.g. 'width:60px' or 'width:50%'.
	 * 
	 * @see #getWidthStyle()
	 */
	public void setWidth(String width) {
		this.columnWidth = CssUtil.ensureWidthStyle(width);
	}

	@Override
	public boolean isSelectable() {
		return selectable;
	}

	/**
	 * Sets the selectable flag.
	 *
	 * @see #isSelectable()
	 */
	public void setSelectable(boolean selectable) {
		this.selectable = selectable;
	}

}
