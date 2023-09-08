/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;


/**
 * Specification of a key press event.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class KeySelector {

	/**
	 * Bit mask for the shift modifier.
	 */
	public static final int SHIFT_MASK = 1 << 0;
	/**
	 * Bit mask for the ctrl modifier.
	 */
	public static final int CTRL_MASK = 1 << 1;
	/**
	 * Bit mask for the alt modifier.
	 */
	public static final int ALT_MASK = 1 << 2;

	private final KeyCode _keyCode;

	private final int _modifiers;

	/**
	 * Create a {@link KeySelector}.
	 * 
	 * @param keyCode
	 *        See {@link #getKeyCode()}.
	 * @param shift
	 *        See {@link #hasShiftModifier()}.
	 * @param ctrl
	 *        See {@link #hasCtrlModifier()}.
	 * @param alt
	 *        See {@link #hasAltModifier()}.
	 */
	public KeySelector(KeyCode keyCode, boolean shift, boolean ctrl, boolean alt) {
		this(keyCode, constructModifiers(shift, ctrl, alt));
	}

	/**
	 * Create a {@link KeySelector}.
	 * 
	 * @param keyCode
	 *        See {@link #getKeyCode()}.
	 * @param modifiers
	 *        See {@link #getModifiers()}.
	 */
	public KeySelector(KeyCode keyCode, int modifiers) {
		_keyCode = keyCode;
		_modifiers = modifiers;
	}

	/**
	 * The {@link KeyCode} that represents the key that was pressed.
	 */
	public KeyCode getKeyCode() {
		return _keyCode;
	}

	/**
	 * A bit set representing all modifiers that were active at the client at the time the key was
	 * pressed.
	 * 
	 * @see #SHIFT_MASK
	 * @see #CTRL_MASK
	 * @see #ALT_MASK
	 */
	public int getModifiers() {
		return _modifiers;
	}

	/**
	 * Whether the shift modifier is set.
	 */
	public boolean hasShiftModifier() {
		return (_modifiers & SHIFT_MASK) > 0;
	}

	/**
	 * Whether the ctrl modifier is set.
	 */
	public boolean hasCtrlModifier() {
		return (_modifiers & CTRL_MASK) > 0;
	}

	/**
	 * Whether the alt modifier is set.
	 */
	public boolean hasAltModifier() {
		return (_modifiers & ALT_MASK) > 0;
	}

	/**
	 * Construct a modifiers bit set
	 * 
	 * @param shift
	 *        Whether the shift modifier is given.
	 * @param ctrl
	 *        Whether the ctrl modifier is given.
	 * @param alt
	 *        Whether the alt modifier is given.
	 * @return The constructed bit set.
	 */
	public static int constructModifiers(boolean shift, boolean ctrl, boolean alt) {
		return (shift ? SHIFT_MASK : 0) |
			(ctrl ? CTRL_MASK : 0) |
			(alt ? ALT_MASK : 0);
	}

}
