/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.xml;

import java.io.IOError;
import java.io.IOException;
import java.io.Writer;

import com.top_logic.basic.CalledFromJSP;
import com.top_logic.basic.StringServices;

/**
 * Static utilities for writing XML.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TagUtil {

	/**
	 * The XML/HTML character entity for the <code>'<'</code> character.
	 */
	public static final String LT_CHAR = "&lt;";
	
	/**
	 * The XML/HTML character entity for the <code>'>'</code> character.
	 */
	public static final String GT_CHAR = "&gt;";
	
	/**
	 * The XML/HTML character entity for the <code>'&amp;'</code> character.
	 */
	public static final String AMP_CHAR = "&amp;";
	
	/**
	 * The XML/HTML character entity for the <code>'"'</code> character.
	 */
	public static final String QUOT_CHAR = "&quot;";
	
	/**
	 * The XML/HTML character entity for the <code>"'"</code> character.
	 * 
	 * <p>
	 * <b>Note:</b> The "'" character cannot be escaped with <code>&amp;apos;</code>
	 * in both XML and HTML, because the corresponding entity is not defined in
	 * HTML. Even if rendering XHTML, its not safe to use <code>&amp;apos;</code>
	 * for backwards compatibility with old and insane browsers.
	 * </p>
	 */
	public static final String APOS_CHAR = "&#39;";

	/**
	 * Quoted backslash char in a JavaScript string literal.
	 */
	public static final String JS_BACKSLASH_QUOTE = "\\\\";

	/**
	 * Quoted tab char in a JavaScript string literal.
	 */
	public static final String JS_TAB_QUOTE = "\\t";

	/**
	 * Quoted newline char in a JavaScript string literal.
	 */
	public static final String JS_NL_QUOTE = "\\n";

	/**
	 * Quoted carriage return char in a JavaScript string literal.
	 */
	public static final String JS_CR_QUOTE = "\\r";

	/**
	 * JavaScript string literal quote in attribute JavaScript content.
	 */
	public static final char JS_STRING_QUOTE_ATTR = '\'';

	/**
	 * JavaScript string literal quote in direct JavaScript content.
	 */
	public static final char JS_STRING_QUOTE_DIRECT = '\'';

	/**
	 * Quoting of {@link #JS_STRING_QUOTE_DIRECT} within a JavaScript string literal.
	 */
	public static final String JS_APOS_QUOTE_DIRECT = "\\" + JS_STRING_QUOTE_DIRECT;

	/**
	 * Single quote within a JavaScript string literal in attribute content.
	 * 
	 * <p>
	 * Note: {@link #APOS_CHAR} cannot be used, since this is also used in the content of a
	 * <code>script</code> tag within a <code>CDATA</code> section. Therefore, XML entity references
	 * may not be used.
	 * </p>
	 */
	public static final String JS_APOS_QUOTE_ATTR = "\\" + JS_STRING_QUOTE_DIRECT;

	/** Begin of a <code>CDATA</code> block. */
	public static final String CDATA_BEGIN = "<![CDATA[";

	/** End of a <code>CDATA</code> block. */
	public static final String CDATA_END = "]]>";

	private static final char[] CDATA_END_CHARS = "]]>".toCharArray();

	/**
	 * Writes an XML header.
	 * 
	 * @param out
	 *        The output to write to.
	 * @param encodingName
	 *        The enconding of the output.
	 */
	public static void writeXMLHeader(Appendable out, String encodingName) {
		try {
			out.append("<?xml version=\"1.0\" encoding=\"");
			out.append(encodingName);
			out.append("\" ?>");
		} catch (IOException ex) {
			throw new IOError(ex);
		}
	}

	/**
	 * Writes a start tag with the given name without any attributes.
	 * 
	 * @param out
	 *        The output to write to.
	 * @param tagName
	 *        The name of the tag to open.
	 */
	public static void beginTag(Appendable out, String tagName) {
		beginBeginTag(out, tagName);
		endBeginTag(out);
	}

	/**
	 * Opens a start tag for writing attributes.
	 * 
	 * @see #writeAttribute(Appendable, String, CharSequence)
	 * @see #endBeginTag(Appendable)
	 * 
	 * @param out
	 *        The output to write to.
	 * @param tagName
	 *        the name of the tag to open.
	 */
	public static void beginBeginTag(Appendable out, String tagName) {
		try {
			out.append('<');
			out.append(tagName);
		} catch (IOException ex) {
			throw new IOError(ex);
		}
	}

	/**
	 * Writes an attribute with the given name and value.
	 * 
	 * @param out
	 *        The output to write to.
	 * @param attributeName
	 *        The name of the attribute to write.
	 * @param value
	 *        The attribute value.
	 */
	public static void writeAttribute(Appendable out, String attributeName, CharSequence value) {
		// Don't even think about checking for the empty string here! Otherwise,
		// it would be impossible to render empty attributes.
		if (value != null) {
			beginAttribute(out, attributeName);
			writeAttributeTextNonNullDQuot(out, value);
			endAttribute(out);
		}
	}

	/**
	 * Writes an attribute with the given name and value.
	 * 
	 * @param out
	 *        The output to write to.
	 * @param attributeName
	 *        The name of the attribute to write.
	 * @param value
	 *        The attribute value.
	 */
	public static void writeAttribute(Appendable out, String attributeName, char value) {
		beginAttribute(out, attributeName);
		writeAttributeTextDQuot(out, 0, value);
		endAttribute(out);
	}

	/**
	 * Writes an attribute with the given name and value.
	 * 
	 * @param out
	 *        The output to write to.
	 * @param attributeName
	 *        The name of the attribute to write.
	 * @param value
	 *        The attribute value.
	 */
	public static void writeAttribute(Appendable out, String attributeName, boolean value) {
		try {
			beginAttribute(out, attributeName);
			out.append(String.valueOf(value));
			endAttribute(out);
		} catch (IOException ex) {
			throw new IOError(ex);
		}
	}

	/**
	 * Writes an attribute with the given name and value.
	 * 
	 * @param out
	 *        The output to write to.
	 * @param attributeName
	 *        The name of the attribute to write.
	 * @param value
	 *        The attribute value.
	 */
	public static void writeAttribute(Appendable out, String attributeName, int value) {
		try {
			beginAttribute(out, attributeName);
			StringServices.append(out, value);
			endAttribute(out);
		} catch (IOException ex) {
			throw new IOError(ex);
		}
	}

	/**
	 * Writes an attribute with the given name and value.
	 * 
	 * @param out
	 *        The output to write to.
	 * @param attributeName
	 *        The name of the attribute to write.
	 * @param value
	 *        The attribute value.
	 */
	public static void writeAttribute(Appendable out, String attributeName, long value) {
		try {
			beginAttribute(out, attributeName);
			StringServices.append(out, value);
			endAttribute(out);
		} catch (IOException ex) {
			throw new IOError(ex);
		}
	}

	/**
	 * Opens an attribute with the given name.
	 * 
	 * <p>
	 * The attribute value is expected to be written through
	 * {@link #writeAttributeText(Appendable, CharSequence)} or corresponding methods.
	 * </p>
	 * 
	 * @see #endAttribute(Appendable) Closing the attribute after its complete value has been
	 *      written.
	 * 
	 * @param out
	 *        The {@link Appendable} to write to.
	 * @param name
	 *        The name of the attribute to open.
	 */
	public static void beginAttribute(Appendable out, String name) {
		try {
			out.append(' ');
			out.append(name);
			out.append("=\"");
		} catch (IOException ex) {
			throw new IOError(ex);
		}
	}

	/**
	 * Closes an attribute opened with {@link #beginAttribute(Appendable, String)}.
	 * 
	 * @param out
	 *        The {@link Appendable} to write to.
	 */
	public static void endAttribute(Appendable out) {
		try {
			out.append("\"");
		} catch (IOException ex) {
			throw new IOError(ex);
		}
	}

	/**
	 * Encode the given string as XML attribute value and append the encoded
	 * result to the given {@link Appendable}.
	 * 
	 * @param out
	 *        The output to append the encoded string to.
	 * @param value
	 *        The attribute value to encode.
	 *
	 * @see #writeText(Appendable, CharSequence) Writing text within element content.
	 * @see #writeAttribute(Appendable, String, CharSequence) Writing the complete attribute key value pair.
	 * @see #encodeXMLAttribute(String) Less efficient implementation, if the encoded
	 *      result is required as string.
	 */
	@CalledFromJSP
	public static void writeAttributeText(Appendable out, CharSequence value) {
		if (value == null) {
			return;
		}
		
	    writeAttributeTextNonNull(out, value);
	}

	static void writeAttributeTextDQuot(Appendable out, CharSequence value) {
		if (value == null) {
			return;
		}

		writeAttributeTextNonNullDQuot(out, value);
	}

	private static void writeAttributeTextNonNull(Appendable out, CharSequence value) {
		assert value != null;
		
		int closing = 0;
		for (int i = 0, cnt = value.length(); i < cnt; i++ ) {
	        char ch = value.charAt(i);
			closing = writeAttributeText(out, closing, ch);
	    }
	}

	private static void writeAttributeTextNonNullDQuot(Appendable out, CharSequence value) {
		assert value != null;

		int closing = 0;
		for (int i = 0, cnt = value.length(); i < cnt; i++) {
			char ch = value.charAt(i);
			closing = writeAttributeTextDQuot(out, closing, ch);
		}
	}

	/**
	 * Encode the given character as XML attribute value and append the encoded result to the given
	 * {@link Appendable}.
	 * 
	 * @param out
	 *        The output to append the encoded string to.
	 * @param buffer
	 *        The character buffer to take the text from.
	 * @param offset
	 *        The index of the first character to write.
	 * @param length
	 *        the number of characters to encode.
	 */
	public static void writeAttributeText(Writer out, char[] buffer, int offset, int length) {
		int closing = 0;
		for (int i = offset, cnt = offset + length; i < cnt; i++) {
			char ch = buffer[i];
			closing = writeAttributeText(out, closing, ch);
		}
	}

	/**
	 * Encode the given character as XML attribute value assuming that the attribute uses the double
	 * quote character for surrounding its value.
	 * 
	 * <p>
	 * It is therefore not required to quote the single quote character.
	 * </p>
	 * 
	 * @param out
	 *        The output to append the encoded string to.
	 * @param buffer
	 *        The character buffer to take the text from.
	 * @param offset
	 *        The index of the first character to write.
	 * @param length
	 *        the number of characters to encode.
	 */
	static void writeAttributeTextDQuot(Writer out, char[] buffer, int offset, int length) {
		int closing = 0;
		for (int i = offset, cnt = offset + length; i < cnt; i++) {
			char ch = buffer[i];
			closing = writeAttributeTextDQuot(out, closing, ch);
		}
	}

	/**
	 * Encode the given character as XML attribute value and append the encoded result to the given
	 * {@link Appendable}.
	 * 
	 * @param out
	 *        The output to append the encoded string to.
	 * @param closing
	 *        The number of closing brackets (<code>']'</code>) seen so far.
	 * @param value
	 *        The character to encode.
	 * @return The new value for <code>closing</code>.
	 * 
	 * @see #writeText(Appendable, CharSequence)
	 */
	public static int writeAttributeText(Appendable out, int closing, char value) {
		return writeAttributeText(out, closing, value, true);
	}
	
	/**
	 * Encode the given character as XML attribute assuming that the attribute uses the double quote
	 * character for its value.
	 * 
	 * <p>
	 * Therefore it is not required to quote the single quote character (<code>'</code>).
	 * </p>
	 * 
	 * @param out
	 *        The output to append the encoded string to.
	 * @param closing
	 *        The number of closing brackets (<code>']'</code>) seen so far.
	 * @param value
	 *        The character to encode.
	 * @return The new value for <code>closing</code>.
	 * 
	 * @see #writeText(Appendable, CharSequence)
	 */
	public static int writeAttributeTextDQuot(Appendable out, int closing, char value) {
		return writeAttributeText(out, closing, value, false);
	}

	/**
	 * Encode the given character as XML attribute.
	 * 
	 * @param out
	 *        The output to append the encoded string to.
	 * @param closing
	 *        The number of closing brackets (<code>']'</code>) seen so far.
	 * @param value
	 *        The character to encode.
	 * @param quoteSQuote
	 *        Whether to quote the single quote character.
	 * @return The new value for <code>closing</code>.
	 * 
	 * @see #writeText(Appendable, CharSequence)
	 */
	static int writeAttributeText(Appendable out, int closing, char value, boolean quoteSQuote) {
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
					// Skip, legal XML characters are: #x9 | #xA | #xD | [#x20-#xD7FF] |
					// [#xE000-#xFFFD] | [#x10000-#x10FFFF]
					break;
				case 9:
					// character: \t
					out.append("&#9;");
					break;
				case 10:
					// character: \n
					out.append("&#10;");
					break;
				case 13:
					// character: \r
					out.append("&#13;");
					break;
				case '<':
					out.append(LT_CHAR);
					break;
				case ']':
					out.append(value);
					closing++;
					break;
				case '>':
					if (closing >= 2) {
						out.append("&gt;");
					} else {
						out.append(value);
					}
					closing = 0;
					break;
				case '&':
					out.append(AMP_CHAR);
					break;
				case '"':
					out.append(QUOT_CHAR);
					break;
				case '\'':
					if (quoteSQuote) {
						out.append(APOS_CHAR);
					} else {
						out.append(value);
					}
					break;
				default:
					out.append(value);
					closing = 0;
			}
		} catch (IOException ex) {
			throw new IOError(ex);
		}
		return closing;
	}

	private static boolean isSafeXMLAttribute(CharSequence s) {
		for (int n = 0, cnt = s.length(); n < cnt; n++) {
			switch (s.charAt(n)) {
			case '<':
			case '&':
			case '"':
			case '\'':
				return false;
			}
		}
		return true;
	}

	/**
	 * Closes a begin tag opened with {@link #beginBeginTag(Appendable, String)} for writing contents.
	 * 
	 * @param out
	 *        The output to write to.
	 */
	public static void endBeginTag(Appendable out) {
		try {
			out.append('>');
		} catch (IOException ex) {
			throw new IOError(ex);
		}
	}

	/**
	 * Closes a tag opened with {@link #beginBeginTag(Appendable, String)} if no contents follows.
	 * 
	 * @param out
	 *        The output to write to.
	 */
	public static void endEmptyTag(Appendable out) {
		try {
			out.append("/>");
		} catch (IOException ex) {
			throw new IOError(ex);
		}
	}

	/**
	 * Writes an end tag
	 * 
	 * @param out
	 *        The output to write to.
	 * @param tagName
	 *        The name of the tag to end.
	 */
	public static void endTag(Appendable out, String tagName) {
		try {
			out.append("</");
			out.append(tagName);
			out.append('>');
		} catch (IOException ex) {
			throw new IOError(ex);
		}
	}

	/**
	 * Encode the given string as XML element text content and append the
	 * encoded string to the given {@link Appendable}.
	 * 
	 * <p>
	 * <b>Note:</b> This method <b>must not</b> be used for writing text content as
	 * attribute value.
	 * </p>
	 * 
	 * @param out
	 *        The output to append the encoded string to.
	 * @param text
	 *        The string to encode.
	 * 
	 * @see #writeAttributeText(Appendable, CharSequence) Writing text content
	 *      as attribute value.
	 * @see #encodeXML(String) Less efficient implementation, if the
	 *      encoded result is required as string.
	 */
	@CalledFromJSP
	public static void writeText(Appendable out, CharSequence text) {
		if (text == null) {
			return;
		}
		
	    writeTextNonNull(out, text);
	}

	private static void writeTextNonNull(Appendable out, CharSequence text) {
		for (int i = 0, cnt = text.length(); i < cnt; i++ ) {
	        char ch = text.charAt(i);
	        writeText(out, ch);
	    }
	}

	/**
	 * Encode the given string as XML element text content and append the encoded string to the
	 * given {@link Appendable}.
	 * 
	 * <p>
	 * <b>Note:</b> This method <b>must not</b> be used for writing text content as attribute value.
	 * </p>
	 * 
	 * @param out
	 *        The output to append the encoded string to.
	 * @param buffer
	 *        The character buffer to take the text from.
	 * @param offset
	 *        The index of the first character to write.
	 * @param length
	 *        the number of characters to encode.
	 * 
	 * @see #writeAttributeText(Appendable, CharSequence) Writing text content as attribute value.
	 * @see #encodeXML(String) Less efficient implementation, if the encoded result is required as
	 *      string.
	 */
	public static void writeText(Appendable out, char[] buffer, int offset, int length) {
		for (int i = offset, cnt = offset + length; i < cnt; i++) {
			char ch = buffer[i];
			writeText(out, ch);
		}
	}

	/**
	 * Encode the given char as XML text content and append the encoded
	 * result to the given {@link Appendable}.
	 * 
	 * @param out
	 *        The output to append the encoded string to.
	 * @param text
	 *        The char to encode.
	 *        
	 * @see #writeText(Appendable, CharSequence)
	 */
	public static void writeText(Appendable out, char text) {
		try {
			switch (text) {
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
					// Skip, legal XML characters are: #x9 | #xA | #xD | [#x20-#xD7FF] |
					// [#xE000-#xFFFD] | [#x10000-#x10FFFF]
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
			default:
				out.append(text);
			}
		} catch (IOException ex) {
			throw new IOError(ex);
		}
	}

	private static boolean isSafeXML(CharSequence s) {
		for (int n = 0, cnt = s.length(); n < cnt; n++) {
			switch (s.charAt(n)) {
			case '<':
			case '>':
			case '&':
				return false;
			}
		}
		return true;
	}

	/**
	 * Writes the start sequence of a CDATA section.
	 * 
	 * @param out
	 *        The output to write to.
	 */
	public static void beginCData(Writer out) throws IOException {
		out.write(CDATA_BEGIN);
	}

	/**
	 * Writes the stop sequence of a CDATA section.
	 * 
	 * @param out
	 *        The output to write to.
	 */
	public static void endCData(Writer out) throws IOException {
		out.write(CDATA_END);
	}

	/**
	 * Writes arbitrary content within a CDATA section.
	 * 
	 * @param out
	 *        The output to write to.
	 * @param content
	 *        The buffer to take characters from.
	 * @param offset
	 *        The index of the first character to write.
	 * @param length
	 *        the number of characters to write.
	 */
	public static void writeCDataContent(Writer out, char[] content, int offset, int length)
			throws IOException {
		if (length == 0) {
			return;
		}

		int pos = indexOfCDATAEnd(content, offset, length, 0);
		if (pos < 0) {
			// Save to write directly.
			out.write(content, offset, length);
		} else {
			int start = 0;
			do {
				// Write chunk with safe contents splitting the CDATA end sequence.
				int nextChunk = pos + 2;
				out.write(content, offset + start, nextChunk - start);
				breakCDataSection(out);
				start = nextChunk;
				pos = indexOfCDATAEnd(content, offset, length, start + 1);
			} while (pos >= 0);
			out.write(content, offset + start, length - start);
		}
	}

	private static int indexOfCDATAEnd(char[] content, int offset, int length, int fromIndex) {
		return StringServices.indexOf(content, offset, length, CDATA_END_CHARS, 0, CDATA_END_CHARS.length, fromIndex);
	}

	/**
	 * Writes arbitrary content within a CDATA section.
	 * 
	 * @param out
	 *        The output to write to.
	 * @param content
	 *        Arbitrary textual content.
	 */
	public static void writeCDataContent(Writer out, CharSequence content) throws IOException {
		int length = content.length();
		if (length == 0) {
			return;
		}
		
		int pos = StringServices.indexOf(content, CDATA_END);
		if (pos < 0) {
			// Save to write directly.
			out.append(content);
		} else {
			int start = 0;
			do {
				// Write chunk with safe contents splitting the CDATA end sequence.
				int nextChunk = pos + 2;
				out.append(content, start, nextChunk);
				breakCDataSection(out);
				start = nextChunk;
				pos = StringServices.indexOf(content, CDATA_END, start + 1);
			} while (pos >= 0);
			out.append(content, start, length);
		}
	}

	static void breakCDataSection(Writer out) throws IOException {
		// To allow safely adding another chunk of CDATA content, the section must be restarted.
		// Otherwise, the end of this chunk and the beginning of the following chunk may combine
		// to the CDATA section end marker in the output.
		out.append(CDATA_END);
		out.append(CDATA_BEGIN);
	}

	/**
	 * Writes an XML comment.
	 * 
	 * @param out
	 *        The output to write to.
	 * @param comment
	 *        The comment text.
	 */
	public static void writeComment(Appendable out, String comment) {
		if (comment != null) {
			beginComment(out);
			writeCommentContentNonNull(out, comment);
			endComment(out);
		}
	}

	/**
	 * Opens an XML comment.
	 * 
	 * @param out
	 *        The output to write to.
	 * 
	 * @see #writeCommentContent(Appendable, String)
	 * @see #endComment(Appendable)
	 */
	public static void beginComment(Appendable out) {
		try {
			out.append("<!-- ");
		} catch (IOException ex) {
			throw new IOError(ex);
		}
	}

	/**
	 * Writes text within a comment opened with {@link #beginComment(Appendable)}
	 * 
	 * @param out
	 *        The output to append the encoded string to.
	 * @param buffer
	 *        The character buffer to take the text from.
	 * @param offset
	 *        The index of the first character to write.
	 * @param length
	 *        the number of characters to encode.
	 */
	public static void writeCommentContent(Writer out, char[] buffer, int offset, int length) {
		writeCommentContent(out, new String(buffer, offset, length));
	}

	/**
	 * Writes text within a comment opened with {@link #beginComment(Appendable)}
	 * 
	 * @param out
	 *        The output to write to.
	 * @param comment
	 *        The comment text.
	 */
	public static void writeCommentContent(Appendable out, String comment) {
		if (comment != null) {
			writeCommentContentNonNull(out, comment);
		}
	}

	private static void writeCommentContentNonNull(Appendable out, String comment) {
		assert comment != null;
		
		try {
			out.append(comment.replaceAll("--+", "-"));
		} catch (IOException ex) {
			throw new IOError(ex);
		}
	}

	/**
	 * Ends a comment opened with {@link #beginComment(Appendable)}.
	 * 
	 * @param out
	 *        The output to write to.
	 */
	public static void endComment(Appendable out) {
		try {
			out.append(" -->");
		} catch (IOException ex) {
			throw new IOError(ex);
		}
	}

	/**
	 * Same as {@link #writeText(Appendable, CharSequence)}, but performing an extra copy.
	 */
	public static String encodeXML(String s) {
		if (s == null) {
			return StringServices.EMPTY_STRING;
		}
		if (isSafeXML(s)) {
			return s;
		}
		StringBuilder out = new StringBuilder();
		writeTextNonNull(out, s);
		return out.toString();
	}

	/**
	 * Same as {@link #writeAttribute(Appendable, String, CharSequence)}, but performing an extra copy.
	 */
	public static String encodeXMLAttribute(String s) {
		if (s == null) {
			return StringServices.EMPTY_STRING;
		}
		if (isSafeXMLAttribute(s)) {
			return s;
		}
		StringBuilder out = new StringBuilder();
		writeAttributeTextNonNull(out, s);
		return out.toString();
	}

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
	 * Writes the JavaScript <code>null</code> literal to the given output.
	 * 
	 * @param out
	 *        The writer.
	 */
	public static void writeJsNullLiteral(Appendable out) {
		try {
			out.append("null");
		} catch (IOException ex) {
			throw new IOError(ex);
		}
	}

	static void writeDirectJsString(Appendable out, CharSequence value) {
		if (value == null) {
			writeJsNullLiteral(out);
		} else {
			beginDirectJsString(out);
			writeDirectJsStringContentNonNull(out, value);
			endDirectJsString(out);
		}
	}

	private static void beginDirectJsString(Appendable out) {
		writeDirectJsStringQuote(out);
	}

	static void writeDirectJsStringContent(Appendable out, CharSequence value) {
		if (value == null) {
			return;
		}

		writeDirectJsStringContentNonNull(out, value);
	}

	private static void endDirectJsString(Appendable out) {
		writeDirectJsStringQuote(out);
	}

	private static void writeDirectJsStringContentNonNull(Appendable out, CharSequence value) {
		assert value != null;

		for (int i = 0, cnt = value.length(); i < cnt; i++) {
			char ch = value.charAt(i);
			writeDirectJsStringContent(out, ch);
		}
	}

	static void writeDirectJsStringContent(Appendable out, char value) {
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
					// The tag start character must be quoted to workaround quoting issues within
					// script tags, see
					// https://html.spec.whatwg.org/multipage/scripting.html#restrictions-for-contents-of-script-elements
					encodeUnicodeJS(out, value);
					break;
				case '>':
					/* The script may be contained in a CDATA section. If in the content another
					 * CDATA section is opened, ']]>' closes the outer section. This is omitted if
					 * '>' is written by its unicode representation. */
					encodeUnicodeJS(out, value);
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
					out.append(JS_APOS_QUOTE_DIRECT);
					break;
				default:
					out.append(value);
			}
		} catch (IOException ex) {
			throw new IOError(ex);
		}
	}

	private static void writeDirectJsStringQuote(Appendable out) {
		try {
			out.append(JS_STRING_QUOTE_DIRECT);
		} catch (IOException ex) {
			throw new IOError(ex);
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

	private static void encodeUnicodeJS(Appendable out, char value) throws IOException {
		out.append("\\u");
		String hex = Integer.toHexString(value);
		for (int n = hex.length(); n < 4; n++) {
			out.append('0');
		}
		out.append(hex.toUpperCase());
	}

	private static void writeAttributeJsStringQuote(Appendable out) {
		try {
			out.append(JS_STRING_QUOTE_ATTR);
		} catch (IOException ex) {
			throw new IOError(ex);
		}
	}

}
