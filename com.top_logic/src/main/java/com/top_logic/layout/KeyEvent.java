/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;


/**
 * Event that represents a client-side key press event.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class KeyEvent extends KeySelector {

	private Object _source;

	private String _contentControlId;

	/**
	 * Create a {@link KeyEvent}.
	 * 
	 * @param source
	 *        See {@link #getSource()}.
	 * @param keyCode
	 *        See {@link #getKeyCode()}.
	 * @param shift
	 *        See {@link #hasShiftModifier()}.
	 * @param ctrl
	 *        See {@link #hasCtrlModifier()}.
	 * @param alt
	 *        See {@link #hasAltModifier()}.
	 */
	public KeyEvent(Object source, KeyCode keyCode, boolean shift, boolean ctrl, boolean alt) {
		this(source, keyCode, constructModifiers(shift, ctrl, alt));
	}

	/**
	 * Create a {@link KeyEvent}.
	 * 
	 * @param source
	 *        See {@link #getSource()}.
	 * @param keyCode
	 *        See {@link #getKeyCode()}.
	 * @param modifiers
	 *        See {@link #getModifiers()}.
	 */
	public KeyEvent(Object source, KeyCode keyCode, int modifiers) {
		this(source, keyCode, modifiers, null);
	}

	/**
	 * Create a {@link KeyEvent}.
	 * 
	 * @param source
	 *        See {@link #getSource()}.
	 * @param keyCode
	 *        See {@link #getKeyCode()}.
	 * @param modifiers
	 *        See {@link #getModifiers()}.
	 * @param contentControlId
	 *        See {@link #getContentControlId()}.
	 */
	public KeyEvent(Object source, KeyCode keyCode, int modifiers, String contentControlId) {
		super(keyCode, modifiers);
		_source = source;
		_contentControlId = contentControlId;
	}

	/**
	 * The server-side representation of the client-side widget in which the event was triggered.
	 */
	public Object getSource() {
		return _source;
	}

	/**
	 * Updates the {@link #getSource()} property.
	 * 
	 * @param source
	 *        The new event source.
	 * @return The previous value of the {@link #getSource()} property.
	 */
	public Object setSource(Object source) {
		Object oldSource = _source;
		_source = source;
		return oldSource;
	}

	/**
	 * The ID of the {@link com.top_logic.layout.structure.ContentControl} that encloses the
	 * client-side element in which the key was pressed.
	 * 
	 * @return The ID of the nearest client-side ancestor with the attribute
	 *         {@link com.top_logic.mig.html.HTMLConstants#TL_CONTENT_CONTROL_ATTR}, or
	 *         <code>null</code>, if the key was pressed outside of a component's content.
	 */
	public String getContentControlId() {
		return _contentControlId;
	}

}
