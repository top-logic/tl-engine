/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.button;

import java.util.HashMap;
import java.util.Map;

/**
 * An immutable keyboard gesture: a key plus optional modifiers (Ctrl/Alt/Shift/Meta).
 *
 * <p>
 * Build gestures concisely in code via the constants and factories ({@link #ENTER},
 * {@link #of(Key)}, {@link #of(char)}) combined with the fluent modifier methods ({@link #ctrl()},
 * {@link #shift()}, ...), e.g.:
 * </p>
 *
 * <pre>
 * KeyStroke.ENTER                 // Enter
 * KeyStroke.of(Key.S).ctrl()      // Ctrl+S
 * KeyStroke.of('k').ctrl().shift()// Ctrl+Shift+K
 * </pre>
 *
 * <p>
 * In configuration the gesture is written as a string attribute (e.g. {@code key="Ctrl+S"}) and
 * parsed/validated by {@link KeyStrokeFormat}. {@link #toString()} produces the canonical form
 * consumed by the client-side keyboard dispatcher.
 * </p>
 */
public final class KeyStroke {

	/** The {@link Key#ENTER Enter} key without modifiers. */
	public static final KeyStroke ENTER = of(Key.ENTER);

	/** The {@link Key#ESCAPE Escape} key without modifiers. */
	public static final KeyStroke ESCAPE = of(Key.ESCAPE);

	/** The {@link Key#DELETE Delete} key without modifiers. */
	public static final KeyStroke DELETE = of(Key.DELETE);

	/** The {@link Key#SPACE Space} bar without modifiers. */
	public static final KeyStroke SPACE = of(Key.SPACE);

	/** Aliases (lower-cased) accepted by {@link #parse(String)} for the key token. */
	private static final Map<String, Key> NAMED = new HashMap<>();
	static {
		put(Key.ENTER, "enter", "return");
		put(Key.ESCAPE, "escape", "esc");
		put(Key.SPACE, "space", "spacebar");
		put(Key.ARROW_UP, "arrowup", "up");
		put(Key.ARROW_DOWN, "arrowdown", "down");
		put(Key.ARROW_LEFT, "arrowleft", "left");
		put(Key.ARROW_RIGHT, "arrowright", "right");
		put(Key.HOME, "home");
		put(Key.END, "end");
		put(Key.PAGE_UP, "pageup");
		put(Key.PAGE_DOWN, "pagedown");
		put(Key.DELETE, "delete", "del");
		put(Key.TAB, "tab");
		put(Key.BACKSPACE, "backspace");
		put(Key.INSERT, "insert", "ins");
	}

	private static void put(Key key, String... aliases) {
		for (String alias : aliases) {
			NAMED.put(alias, key);
		}
	}

	private final String _key;

	private final boolean _ctrl;

	private final boolean _alt;

	private final boolean _shift;

	private final boolean _meta;

	private KeyStroke(String key, boolean ctrl, boolean alt, boolean shift, boolean meta) {
		_key = key;
		_ctrl = ctrl;
		_alt = alt;
		_shift = shift;
		_meta = meta;
	}

	/**
	 * A gesture for the given well-known key without modifiers.
	 */
	public static KeyStroke of(Key key) {
		return new KeyStroke(key.canonicalName(), false, false, false, false);
	}

	/**
	 * A gesture for the given character key (letter, digit, punctuation) without modifiers.
	 */
	public static KeyStroke of(char character) {
		return new KeyStroke(String.valueOf(Character.toUpperCase(character)), false, false, false, false);
	}

	/**
	 * This gesture with the Ctrl modifier added.
	 */
	public KeyStroke ctrl() {
		return new KeyStroke(_key, true, _alt, _shift, _meta);
	}

	/**
	 * This gesture with the Alt modifier added.
	 */
	public KeyStroke alt() {
		return new KeyStroke(_key, _ctrl, true, _shift, _meta);
	}

	/**
	 * This gesture with the Shift modifier added.
	 */
	public KeyStroke shift() {
		return new KeyStroke(_key, _ctrl, _alt, true, _meta);
	}

	/**
	 * This gesture with the Meta (Command/Windows) modifier added.
	 */
	public KeyStroke meta() {
		return new KeyStroke(_key, _ctrl, _alt, _shift, true);
	}

	/**
	 * Parses a gesture from its string form, e.g. {@code "Enter"}, {@code "Ctrl+S"} or
	 * {@code "Shift+ArrowDown"}.
	 *
	 * <p>
	 * Modifiers ({@code Ctrl}/{@code Control}, {@code Alt}/{@code Option}, {@code Shift},
	 * {@code Meta}/{@code Cmd}/{@code Command}/{@code Win}) are case-insensitive and joined with
	 * {@code +} to a single key, which is either a {@link Key well-known key} (or one of its
	 * aliases), a single character, or a function key ({@code F1}..{@code F12}).
	 * </p>
	 *
	 * @throws IllegalArgumentException
	 *         if the gesture is empty, has no or several keys, or names an unknown key.
	 */
	public static KeyStroke parse(String spec) {
		if (spec == null || spec.trim().isEmpty()) {
			throw new IllegalArgumentException("Empty key gesture.");
		}
		boolean ctrl = false, alt = false, shift = false, meta = false;
		String keyToken = null;
		for (String raw : spec.split("\\+")) {
			String part = raw.trim();
			if (part.isEmpty()) {
				continue;
			}
			switch (part.toLowerCase()) {
				case "ctrl":
				case "control":
					ctrl = true;
					break;
				case "alt":
				case "option":
					alt = true;
					break;
				case "shift":
					shift = true;
					break;
				case "meta":
				case "cmd":
				case "command":
				case "win":
					meta = true;
					break;
				default:
					if (keyToken != null) {
						throw new IllegalArgumentException("More than one key in gesture: '" + spec + "'.");
					}
					keyToken = part;
			}
		}
		if (keyToken == null) {
			throw new IllegalArgumentException("No key in gesture: '" + spec + "'.");
		}
		return new KeyStroke(canonicalKey(keyToken, spec), ctrl, alt, shift, meta);
	}

	private static String canonicalKey(String token, String spec) {
		Key named = NAMED.get(token.toLowerCase());
		if (named != null) {
			return named.canonicalName();
		}
		if (token.length() == 1) {
			return token.toUpperCase();
		}
		if (token.matches("[Ff]\\d{1,2}")) {
			return token.toUpperCase();
		}
		throw new IllegalArgumentException("Unknown key '" + token + "' in gesture '" + spec + "'.");
	}

	/**
	 * The canonical string form, e.g. {@code "Ctrl+S"} or {@code "Shift+ArrowDown"}.
	 *
	 * <p>
	 * Modifiers are emitted in the fixed order Ctrl, Alt, Shift, Meta - the form the client-side
	 * keyboard dispatcher matches against.
	 * </p>
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		if (_ctrl) {
			result.append("Ctrl+");
		}
		if (_alt) {
			result.append("Alt+");
		}
		if (_shift) {
			result.append("Shift+");
		}
		if (_meta) {
			result.append("Meta+");
		}
		result.append(_key);
		return result.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof KeyStroke)) {
			return false;
		}
		KeyStroke other = (KeyStroke) obj;
		return _ctrl == other._ctrl && _alt == other._alt && _shift == other._shift && _meta == other._meta
			&& _key.equals(other._key);
	}

	@Override
	public int hashCode() {
		int hash = _key.hashCode();
		hash = 31 * hash + (_ctrl ? 1 : 0);
		hash = 31 * hash + (_alt ? 2 : 0);
		hash = 31 * hash + (_shift ? 4 : 0);
		hash = 31 * hash + (_meta ? 8 : 0);
		return hash;
	}
}
