/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.xml;

import java.io.IOError;
import java.io.IOException;

import com.top_logic.basic.shared.html.TagUtilShared;

/**
 * Static utilities for writing XML.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TagUtil extends TagUtilShared {

	/**
	 * Writes the given value as JavaScript string literal to the given output.
	 * 
	 * @param out
	 *        The writer.
	 * @param value
	 *        The value to quote.
	 */
	public static void writeJsString(Appendable out, CharSequence value) {
		if (out instanceof TagWriter) {
			try {
				((TagWriter) out).writeJsString(value);
			} catch (IOException ex) {
				throw new IOError(ex);
			}
		} else {
			writeDirectJsString(out, value);
		}
	}

	/**
	 * Starts writing a JavaScript string literal to the given output.
	 * 
	 * <p>
	 * Note: The content is expected to be written in potentially multiple calls to
	 * {@link #writeJsStringContent(Appendable, CharSequence)}.
	 * </p>
	 * 
	 * <p>
	 * The literal is expected to be closed with a call to {@link #endJsString(Appendable)}.
	 * </p>
	 * 
	 * @param out
	 *        The writer.
	 */
	public static void beginJsString(Appendable out) {
		if (out instanceof TagWriter) {
			((TagWriter) out).beginJsString();
		} else {
			writeDirectJsStringQuote(out);
		}
	}

	/**
	 * Writes the given value as part of JavaScript string literal to the given output.
	 * 
	 * <p>
	 * Note: It is expected that {@link #beginJsString(Appendable)} is called before.
	 * </p>
	 * 
	 * @param out
	 *        The writer.
	 * @param value
	 *        The value to encode.
	 */
	public static void writeJsStringContent(Appendable out, CharSequence value) {
		if (out instanceof TagWriter) {
			try {
				((TagWriter) out).writeJsStringContent(value);
			} catch (IOException ex) {
				throw new IOError(ex);
			}
		} else {
			writeDirectJsStringContent(out, value);
		}
	}

	/**
	 * Writes the given value as part of JavaScript string literal to the given output.
	 * 
	 * <p>
	 * Note: It is expected that {@link #beginJsString(Appendable)} is called before.
	 * </p>
	 * 
	 * @param out
	 *        The writer.
	 * @param value
	 *        The value to encode.
	 */
	public static void writeJsStringContent(Appendable out, char value) {
		if (out instanceof TagWriter) {
			try {
				((TagWriter) out).writeJsStringContent(value);
			} catch (IOException ex) {
				throw new IOError(ex);
			}
		} else {
			writeDirectJsStringContent(out, value);
		}
	}

	/**
	 * Ends a JavaScript string literal started with {@link #beginJsString(Appendable)}.
	 * 
	 * @param out
	 *        The writer.
	 */
	public static void endJsString(Appendable out) {
		if (out instanceof TagWriter) {
			((TagWriter) out).endJsString();
		} else {
			writeDirectJsStringQuote(out);
		}
	}

	/**
	 * Utility exclusively called from {@link TagWriter}.
	 * 
	 * @see #writeJsString(Appendable, CharSequence)
	 */
	static void writeAttributeJsString(Appendable out, CharSequence value) {
		if (value == null) {
			writeJsNullLiteral(out);
		} else {
			beginAttributeJsString(out);
			writeAttributeJsStringContentNonNull(out, value);
			endAttributeJsString(out);
		}
	}

	/**
	 * Utility exclusively called from {@link TagWriter}.
	 * 
	 * @see #beginJsString(Appendable)
	 */
	static void beginAttributeJsString(Appendable out) {
		writeAttributeJsStringQuote(out);
	}

	/**
	 * Utility exclusively called from {@link TagWriter}.
	 * 
	 * @see #writeJsStringContent(Appendable, CharSequence)
	 */
	static void writeAttributeJsStringContent(Appendable out, CharSequence value) {
		if (value == null) {
			return;
		}

		writeAttributeJsStringContentNonNull(out, value);
	}

	/**
	 * Utility exclusively called from {@link TagWriter}.
	 * 
	 * @see #endJsString(Appendable)
	 */
	static void endAttributeJsString(Appendable out) {
		writeAttributeJsStringQuote(out);
	}

	private static void writeAttributeJsStringContentNonNull(Appendable out, CharSequence value) {
		assert value != null;

		for (int i = 0, cnt = value.length(); i < cnt; i++) {
			char ch = value.charAt(i);
			writeAttributeJsStringContent(out, ch);
		}
	}

	/**
	 * @see #writeJsStringContent(Appendable, char)
	 */
	static void writeAttributeJsStringContent(Appendable out, char value) {
		try {
			switch (value) {
				case 0x00:
				case 0x01:
				case 0x02:
				case 0x03:
				case 0x04:
				case 0x05:
				case 0x06:
				case 0x07:
				case 0x08:

				case 0x0B:
				case 0x0C:

				case 0x0E:
				case 0x0F:
				case 0x10:
				case 0x11:
				case 0x12:
				case 0x13:
				case 0x14:
				case 0x15:
				case 0x16:
				case 0x17:
				case 0x18:
				case 0x19:
				case 0x1A:
				case 0x1B:
				case 0x1C:
				case 0x1D:
				case 0x1E:
				case 0x1F:
					// Encode, legal XML characters are: #x9 | #xA | #xD | [#x20-#xD7FF] |
					// [#xE000-#xFFFD] | [#x10000-#x10FFFF]
					encodeUnicodeJS(out, value);
					break;
				case '<':
					out.append(LT_CHAR);
					break;
				case '>':
					out.append(GT_CHAR);
					break;
				case '&':
					out.append(AMP_CHAR);
					break;
				case '"':
					out.append(QUOT_CHAR);
					break;
				case '\\':
					out.append(JS_BACKSLASH_QUOTE);
					break;
				case '\t':
					out.append(JS_TAB_QUOTE);
					break;
				case '\n':
					out.append(JS_NL_QUOTE);
					break;
				case '\r':
					out.append(JS_CR_QUOTE);
					break;
				case '\'':
					out.append(JS_APOS_QUOTE_ATTR);
					break;
				default:
					out.append(value);
			}
		} catch (IOException ex) {
			throw new IOError(ex);
		}
	}

	private static void writeAttributeJsStringQuote(Appendable out) {
		try {
			out.append(JS_STRING_QUOTE_ATTR);
		} catch (IOException ex) {
			throw new IOError(ex);
		}
	}

}
