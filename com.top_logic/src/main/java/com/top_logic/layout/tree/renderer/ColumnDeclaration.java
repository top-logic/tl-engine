/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.renderer;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.form.template.ControlProvider;

/**
 * Declaration of a single column within a {@link TableDeclaration}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ColumnDeclaration {

    /** Will result in rendering the TreePart and some Label */
	int DEFAULT_COLUMN  = 0;

	/** Will render a Control based on {@link #getControlProvider()} */
	int CONTROL_COLUMN  = 1;

	/** Will render arbitrary content based on some {@link Renderer} */
	int RENDERED_COLUMN = 2;

	/** NYI , do not use */
	int TEMPLATE_COLUMN = 3;

	int NO_HEADER          = -1;
	int DEFAULT_HEADER     = 0;
	int STRING_HEADER      = 1;
	int HTML_HEADER        = 2;
	int CONTROL_HEADER     = 3;
	int RENDERED_HEADER    = 4;
	int TEMPLATE_HEADER    = 5;

	/**
	 * @see #DEFAULT_COLUMN
	 * @see #CONTROL_COLUMN
	 * @see #RENDERED_COLUMN
	 * @see #TEMPLATE_COLUMN
	 */
	int getColumnType();

	/**
	 * The {@link ControlProvider} in case of {@link #CONTROL_COLUMN}
	 */
	ControlProvider getControlProvider();

	/**
	 * The {@link Renderer} in case of {@link #RENDERED_COLUMN}
	 */
	Renderer<Object> getRenderer();

	/**
	 * @see #DEFAULT_HEADER
	 * @see #STRING_HEADER
	 * @see #HTML_HEADER
	 * @see #RENDERED_HEADER
	 * @see #CONTROL_HEADER
	 * @see #TEMPLATE_HEADER
	 */
	int getHeaderType();

	/**
	 * Complete resource key of header in case of {@link #getHeaderType()} being
	 * one of
	 *
	 * <ul>
	 * <li>{@link #STRING_HEADER}</li>
	 * <li>{@link #HTML_HEADER}</li>
	 * </ul>
	 */
	ResKey getHeaderKey();

	/**
	 * The column header {@link Renderer} in case of {@link #getHeaderType()} is {@link #RENDERED_HEADER}.
	 */
	Renderer<Object> getHeaderRenderer();

	/**
	 * Returns the width style of this column in HTML-Style attribute compatible form, i.e. it is of
	 * the form "width:xx<i>Unit</i>;" where xx is the size and <i>Unit</i> the unit of the size,
	 * e.g. 'width:60px' or 'width:50%'.
	 * 
	 * @return may be <code>null</code> in case no width was set.
	 */
	String getWidthStyle();

	/**
	 * Returns the style for the header of the column in HTML-Style attribute compatible form. Does
	 * not contain the 'width' part.
	 * 
	 * @return may be null if no header style was set.
	 * 
	 * @see #getWidthStyle()
	 */
	String getHeaderStyle();

	/**
	 * Returns the style for the column in HTML-Style attribute compatible form. Does not contain
	 * the 'width' part.
	 * 
	 * @return may be null if no style was set.
	 * 
	 * @see #getWidthStyle()
	 */
	String getStyle();

	/**
	 * Whether cells of this column can be clicked to select their rows.
	 * 
	 * <p>
	 * The default value is <code>true</code>.
	 * </p>
	 * 
	 * <p>
	 * The option has no effect, if the whole table cannot be selected.
	 * </p>
	 */
	boolean isSelectable();

}
