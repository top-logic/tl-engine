/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import static java.awt.event.KeyEvent.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration of client-side key codes that can be handled on the server.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public enum KeyCode {
	/** The escape key. */
	ESCAPE(VK_ESCAPE),
	/** The return (enter) key. */
	RETURN(13),
	/** The cursor up key. */
	UP(VK_UP),
	/** The cursor down key. */
	DOWN(VK_DOWN),
	/** The cursor left key. */
	LEFT(VK_LEFT),
	/** The cursor right key. */
	RIGHT(VK_RIGHT),
	/** The tabulator key. */
	TAB(VK_TAB),
	/** The space key. */
	SPACE(VK_SPACE),
	/** The delete key. */
	DELETE(VK_DELETE),
	/** The backspace key. */
	BACK_SPACE(VK_BACK_SPACE),
	/** The home key. */
	HOME(VK_HOME),
	/** The end key. */
	END(VK_END),
	/** The page up key. */
	PAGE_UP(VK_PAGE_UP),
	/** The page down key. */
	PAGE_DOWN(VK_PAGE_DOWN),
	/** The insert key. */
	INSERT(VK_INSERT),
	/** The function key F1. */
	F1(VK_F1),
	/** The function key F2. */
	F2(VK_F2),
	/** The function key F3. */
	F3(VK_F3),
	/** The function key F4. */
	F4(VK_F4),
	/** The function key F5. */
	F5(VK_F5),
	/** The function key F6. */
	F6(VK_F6),
	/** The function key F7. */
	F7(VK_F7),
	/** The function key F8. */
	F8(VK_F8),
	/** The function key F9. */
	F9(VK_F9),
	/** The function key F10. */
	F10(VK_F10),
	/** The function key F11. */
	F11(VK_F11),
	/** The function key F12. */
	F12(VK_F12),
	/** The digit key 0. */
	NUM_0(VK_0),
	/** The digit key 1. */
	NUM_1(VK_1),
	/** The digit key 2. */
	NUM_2(VK_2),
	/** The digit key 3. */
	NUM_3(VK_3),
	/** The digit key 4. */
	NUM_4(VK_4),
	/** The digit key 5. */
	NUM_5(VK_5),
	/** The digit key 6. */
	NUM_6(VK_6),
	/** The digit key 7. */
	NUM_7(VK_7),
	/** The digit key 8. */
	NUM_8(VK_8),
	/** The digit key 9. */
	NUM_9(VK_9),
	/** The letter key A. */
	LETTER_A(VK_A),
	/** The letter key B. */
	LETTER_B(VK_B),
	/** The letter key C. */
	LETTER_C(VK_C),
	/** The letter key D. */
	LETTER_D(VK_D),
	/** The letter key E. */
	LETTER_E(VK_E),
	/** The letter key F. */
	LETTER_F(VK_F),
	/** The letter key G. */
	LETTER_G(VK_G),
	/** The letter key H. */
	LETTER_H(VK_H),
	/** The letter key I. */
	LETTER_I(VK_I),
	/** The letter key J. */
	LETTER_J(VK_J),
	/** The letter key K. */
	LETTER_K(VK_K),
	/** The letter key L. */
	LETTER_L(VK_L),
	/** The letter key M. */
	LETTER_M(VK_M),
	/** The letter key N. */
	LETTER_N(VK_N),
	/** The letter key O. */
	LETTER_O(VK_O),
	/** The letter key P. */
	LETTER_P(VK_P),
	/** The letter key Q. */
	LETTER_Q(VK_Q),
	/** The letter key R. */
	LETTER_R(VK_R),
	/** The letter key S. */
	LETTER_S(VK_S),
	/** The letter key T. */
	LETTER_T(VK_T),
	/** The letter key U. */
	LETTER_U(VK_U),
	/** The letter key V. */
	LETTER_V(VK_V),
	/** The letter key W. */
	LETTER_W(VK_W),
	/** The letter key X. */
	LETTER_X(VK_X),
	/** The letter key Y. */
	LETTER_Y(VK_Y),
	/** The letter key Z. */
	LETTER_Z(VK_Z);

	private final int _scancode;

	private KeyCode(int scancode) {
		_scancode = scancode;
	}

	/**
	 * Numeric identifier of the key.
	 */
	public int getScanCode() {
		return _scancode;
	}

	private static final Map<Integer, KeyCode> byScanCode = new HashMap<>();
	static {
		for (KeyCode keyCode : values()) {
			byScanCode.put(keyCode.getScanCode(), keyCode);
		}
	}

	/**
	 * Looks up the {@link KeyCode} matching the given {@link #getScanCode() scan code}.
	 */
	public static KeyCode fromScanCode(int scancode) {
		return byScanCode.get(scancode);
	}

}