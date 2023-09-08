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
	ESCAPE(VK_ESCAPE),
	RETURN(13),
	UP(VK_UP),
	DOWN(VK_DOWN),
	LEFT(VK_LEFT),
	RIGHT(VK_RIGHT),
	TAB(VK_TAB),
	SPACE(VK_SPACE),
	DELETE(VK_DELETE),
	BACK_SPACE(VK_BACK_SPACE),
	HOME(VK_HOME),
	END(VK_END),
	PAGE_UP(VK_PAGE_UP),
	PAGE_DOWN(VK_PAGE_DOWN),
	INSERT(VK_INSERT),
	F1(VK_F1),
	F2(VK_F2),
	F3(VK_F3),
	F4(VK_F4),
	F5(VK_F5),
	F6(VK_F6),
	F7(VK_F7),
	F8(VK_F8),
	F9(VK_F9),
	F10(VK_F10),
	F11(VK_F11),
	F12(VK_F12),
	NUM_0(VK_0),
	NUM_1(VK_1),
	NUM_2(VK_2),
	NUM_3(VK_3),
	NUM_4(VK_4),
	NUM_5(VK_5),
	NUM_6(VK_6),
	NUM_7(VK_7),
	NUM_8(VK_8),
	NUM_9(VK_9),
	LETTER_A(VK_A),
	LETTER_B(VK_B),
	LETTER_C(VK_C),
	LETTER_D(VK_D),
	LETTER_E(VK_E),
	LETTER_F(VK_F),
	LETTER_G(VK_G),
	LETTER_H(VK_H),
	LETTER_I(VK_I),
	LETTER_J(VK_J),
	LETTER_K(VK_K),
	LETTER_L(VK_L),
	LETTER_M(VK_M),
	LETTER_N(VK_N),
	LETTER_O(VK_O),
	LETTER_P(VK_P),
	LETTER_Q(VK_Q),
	LETTER_R(VK_R),
	LETTER_S(VK_S),
	LETTER_T(VK_T),
	LETTER_U(VK_U),
	LETTER_V(VK_V),
	LETTER_W(VK_W),
	LETTER_X(VK_X),
	LETTER_Y(VK_Y),
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