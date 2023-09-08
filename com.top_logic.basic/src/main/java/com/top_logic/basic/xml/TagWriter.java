/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.xml;

import java.io.BufferedWriter;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Pattern;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.BufferingWriter;
import com.top_logic.basic.io.NullWriter;
import com.top_logic.basic.util.Utils;

/**
 * {@link Writer} that can create properly nested XML structures.
 * 
 * <p>
 * Using the inherited API from {@link Writer}, properly escaped text content is produced in the
 * current context.
 * </p>
 *
 * <p>
 * Usage example:
 * </p>
 * 
 * <pre>
 * try (TagWriter xml = new TagWriter(new OutputStreamWriter(System.out))) {
 * 	xml.beginBeginTag("div");
 * 	xml.writeAttribute("class", "my-paragraph");
 * 	xml.endBeginTag();
 * 	{
 * 		xml.beginTag("b");
 * 		{
 * 			xml.write("Some text");
 * 		}
 * 		xml.endTag("b");
 * 	}
 * 	xml.endTag("div");
 * }
 * </pre>
 * 
 * <p>
 * Special support for writing HTML {@value #SCRIPT_TAG} tags is provided, see
 * {@link #beginScript()}.
 * </p>
 *
 * @author <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class TagWriter extends Writer {

	private static final String QUOTED_CDATA_END = "]]" + TagUtil.CDATA_END + TagUtil.CDATA_BEGIN + ">";

	private static final String SCRIPT_TAG = "script";

	private static final String SCRIPT_TYPE_ATTR = "type";
	
	private static final String SCRIPT_TYPE_JAVASCRIPT = "text/javascript";
	
	private static final String SCRIPT_LINE_COMMENT = "// ";

	private static final String QUOTED_XML_PSEUDO_TAG = "<![CDATA[";

	private static final String CLASS_ATTR = "class";

	private static final class ProxyWriter extends Writer {

		private Writer _impl;

		public ProxyWriter() {
			super();
		}

		public Writer getImpl() {
			return _impl;
		}

		public void setImpl(Writer impl) {
			_impl = impl;
		}

		@Override
		public void write(char[] cbuf, int off, int len) throws IOException {
			_impl.write(cbuf, off, len);
		}

		@Override
		public void flush() throws IOException {
			_impl.flush();
		}

		@Override
		public void close() throws IOException {
			_impl.close();
		}
	}

	/**
	 * Abstract position within an XML document.
	 * 
	 * @see TagWriter#setState(State)
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public enum State {
		/**
		 * Within a start tag.
		 */
		START_TAG {
			@Override
			public boolean spaceAllowed() {
				return true;
			}

			@Override
			public boolean attributeAllowed() {
				return true;
			}
		},
		
		/**
		 * Within an common attribute value.
		 */
		ATTRIBUTE {
			@Override
			public boolean attributeTextAllowed() {
				return true;
			}

			@Override
			public boolean jsAllowed() {
				return true;
			}

			@Override
			public State beginJsString() {
				return JS_ATTRIBUTE_STRING;
			}
		},
		
		/**
		 * Start of an class attribute value.
		 */
		CLASS_ATTRIBUTE_START,

		/**
		 * Start of an attribute value.
		 */
		ATTRIBUTE_START {
			@Override
			public boolean attributeTextAllowed() {
				return true;
			}

			@Override
			public boolean jsAllowed() {
				return true;
			}

			@Override
			public State beginJsString() {
				return JS_ATTRIBUTE_STRING;
			}
		},

		/**
		 * Within an class attribute value.
		 */
		CLASS_ATTRIBUTE,

		/**
		 * Within an element.
		 */
		ELEMENT_CONTENT {
			@Override
			public boolean spaceAllowed() {
				return true;
			}

			@Override
			public boolean textAllowed() {
				return true;
			}
			
			@Override
			public boolean jsAllowed() {
				return true;
			}

			@Override
			public boolean beginTagAllowed() {
				return true;
			}
			
			@Override
			public boolean endTagAllowed() {
				return true;
			}
			
			@Override
			public boolean commentAllowed() {
				return true;
			}

			@Override
			public State beginJsString() {
				return JS_ELEMENT_STRING;
			}
		},
		
		/**
		 * Within a CDATA section.
		 */
		CDATA_CONTENT {
			@Override
			public boolean spaceAllowed() {
				return true;
			}

			@Override
			public boolean textAllowed() {
				return true;
			}

			@Override
			public boolean jsAllowed() {
				return true;
			}

			@Override
			public State beginJsString() {
				return JS_CDATA_STRING;
			}
		},
		
		/**
		 * Within a script section.
		 * 
		 * <p>
		 * In a script section, absolutely no quoting must occur, see
		 * {@link TagWriter#beginScript()}.
		 * </p>
		 */
		SCRIPT {
			@Override
			public boolean spaceAllowed() {
				return true;
			}

			@Override
			public boolean textAllowed() {
				return true;
			}

			@Override
			public boolean jsAllowed() {
				return true;
			}

			@Override
			public State beginJsString() {
				return JS_SCRIPT_STRING;
			}
		},

		/**
		 * Within a JavaScript string literal in an XML attribute.
		 */
		JS_ATTRIBUTE_STRING {
			@Override
			public boolean inJsString() {
				return true;
			}

			@Override
			public State endJsString() {
				return ATTRIBUTE;
			}
		},

		/**
		 * Within a JavaScript string literal textual element content.
		 */
		JS_ELEMENT_STRING {
			@Override
			public boolean inJsString() {
				return true;
			}

			@Override
			public State endJsString() {
				return ELEMENT_CONTENT;
			}
		},

		/**
		 * Within a JavaScript string literal textual element content.
		 */
		JS_CDATA_STRING {
			@Override
			public boolean inJsString() {
				return true;
			}

			@Override
			public State endJsString() {
				return CDATA_CONTENT;
			}
		},

		/**
		 * Within a JavaScript string literal in {@link #SCRIPT} content.
		 */
		JS_SCRIPT_STRING {
			@Override
			public boolean inJsString() {
				return true;
			}

			@Override
			public State endJsString() {
				return SCRIPT;
			}
		},

		/**
		 * Within a comment.
		 */
		COMMENT_CONTENT {
			@Override
			public boolean spaceAllowed() {
				return true;
			}

			@Override
			public boolean commentContentAllowed() {
				return true;
			}
		};

		/**
		 * Whether any white space content is allowed (including new line characters).
		 */
		public boolean spaceAllowed() {
			return false;
		}

		/**
		 * Whether (quoted) plain text content is allowed.
		 */
		public boolean textAllowed() {
			return false;
		}

		/**
		 * Whether (quoted) attribute text is allowed.
		 */
		public boolean attributeTextAllowed() {
			return false;
		}

		/**
		 * Whether JavaScript content can be rendered.
		 * 
		 * @see TagWriter#writeScript(CharSequence)
		 * @see TagWriter#writeJsString(CharSequence)
		 */
		public boolean jsAllowed() {
			return false;
		}

		/**
		 * Whether opening a tag is allowed.
		 */
		public boolean beginTagAllowed() {
			return false;
		}

		/**
		 * Whether writing an attribute is allowed.
		 */
		public boolean attributeAllowed() {
			return false;
		}

		/**
		 * Whether ending an attribute is allowed.
		 */
		public final boolean endAttributeAllowed() {
			return attributeTextAllowed();
		}

		/**
		 * Whether closing an open start tag is allowed.
		 */
		public final boolean endBeginTagAllowed() {
			return attributeAllowed();
		}

		/**
		 * Whether writing an end tag is allowed.
		 */
		public boolean endTagAllowed() {
			return false;
		}

		/**
		 * Whether inserting a comment is allowed.
		 */
		public boolean commentAllowed() {
			return false;
		}

		/**
		 * Whether comment content is allowed.
		 */
		public boolean commentContentAllowed() {
			return false;
		}

		/**
		 * The next {@link State} after ending a tag.
		 * 
		 * @param documentRoot
		 *        Whether no elements are open.
		 */
		public State endTag(boolean documentRoot) {
			return State.ELEMENT_CONTENT;
		}

		/**
		 * The next {@link State} after closing a comment.
		 * 
		 * @param documentRoot
		 *        Whether no elements are open.
		 */
		public State endComment(boolean documentRoot) {
			return State.ELEMENT_CONTENT;
		}

		/**
		 * Switches the state to JavaScript string literal rendering.
		 */
		public State beginJsString() {
			throw new AssertionError("No JavaScript content in state '" + this + "'.");
		}

		/**
		 * Switches the state back after rendering a JavaScript string literal.
		 */
		public State endJsString() {
			throw new AssertionError("Not rendering a JavaScript string literal, but '" + this + "'.");
		}

		/**
		 * Whether a JavaScript string literal is currently rendered.
		 */
		public boolean inJsString() {
			return false;
		}

	}
	
    /** newline + 80 spaces for indentation */
    private static final String SPACES = 
        "                                                                                ";
    
    /** Constant to be used with endTag() */
    public static final boolean  DOINDENT = true;

	/**
	 * Constant to be used with #setendTag()
	 */
    public static final boolean  NOINDENT = ! DOINDENT;

	/**
	 * RegExp {@link Pattern} for the content of XML comments.
	 * <p>
	 * "Content" means the characters inside of: "&lt;!--" and "--&gt;"
	 * </p>
	 * <ul>
	 * <li><a href="http://www.w3.org/TR/2008/REC-xml-20081126/#sec-comments">XML 1.0
	 * Specification</a></li>
	 * <li><a href="http://www.w3.org/TR/2006/REC-xml11-20060816/#sec-comments">XML 1.1
	 * Specification</a></li>
	 * </ul>
	 */
	public static final Pattern XML_COMMENT_CONTENT_PATTERN = Pattern.compile("([^-]|(-[^-]))*");

	private final ProxyWriter _outProxy = new ProxyWriter();

	/**
	 * The {@link Writer} that is written to.
	 */
	protected Writer out;
    
	/**
	 * The {@link State} at the current output position.
	 */
	protected State state;
    
	/**
	 * Whether {@link #beginQuotedXML()} was called that is not yet ended with a call to
	 * {@link #endQuotedXML()}.
	 */
	private boolean _quoting;

	/** {@link PrintWriter} facade to the {@link Writer} interface of this. */
	private PrintWriter _outPrinter;

	/**
	 * An on-demand allocated buffering wrapper for {@link #_outProxy}.
	 * 
	 * @see #allocateBuffer()
	 */
	private BufferingWriter _outBuffered;

    /** Stack of currently open tags. */
    protected final Stack<String> stack = new Stack<>();
    
    /** The current depth on indentation */
    protected   int     indent;
    
	private String _newLine = "\n";

	/**
	 * @see #getIndentStep()
	 */
	private int _indentStep = 2;

	/**
	 * @see #getIndentWhitespace()
	 */
	private String _indentWhitespace = SPACES;
    
    /** Set of used attribute names used for assertions only */
    protected Set<String> attributeNames;
    {
    	assert initChecks();
    }

    /**
     * @see #setIndent(boolean)
     */
	private boolean _doIndent = NOINDENT;

	private String _attributeName;

	/**
	 * When this {@link TagWriter} currently writes <code>CDATA</code> content, this variable holds
	 * the last written character.
	 */
	private char _lastCDATAChar;

	/**
	 * When this {@link TagWriter} currently writes <code>CDATA</code> content, this variable holds
	 * the next to last written character.
	 */
	private char _nextToLastCDATAChar;

    /** Helper to fetch potential encoding from aWriter */
    public static String fetchEncoding(Writer aWriter) {
        if (aWriter instanceof OutputStreamWriter) {
            return ((OutputStreamWriter) aWriter).getEncoding();
        }
        return null;
    }

    /** 
     * Create new TagWriter for the given Writer.
     *
     * @param out Writer where the output will be sent to
     */
    public TagWriter(Writer out) {
		initOut(out);
		this.state = State.ELEMENT_CONTENT;
    }

	private void initOut(Writer newOut) {
		boolean bufferingRequired = bufferingRequired(newOut);
		_outProxy.setImpl(newOut);
		if (bufferingRequired) {
			this.out = allocateBuffer();
		} else {
			this.out = newOut;
		}
	}

	private boolean bufferingEnabled() {
		return out == _outBuffered;
	}

	private BufferingWriter allocateBuffer() {
		if (_outBuffered == null) {
			_outBuffered = new BufferingWriter(_outProxy);
		}
		return _outBuffered;
	}

	private static boolean bufferingRequired(Writer origOut) {
		if (origOut instanceof StringWriter) {
			return false;
		}
		if (origOut instanceof CharArrayWriter) {
			return false;
		}
		if (origOut instanceof BufferedWriter) {
			return false;
		}
		if (origOut instanceof BufferingWriter) {
			return false;
		}
		if (origOut instanceof NullWriter) {
			return false;
		}
		return true;
	}
    
    /** 
     * Create new TagWriter using a StringWriter.
     *
     * You may later use {@link #toString()} to retrive the data
     */
    public TagWriter() {
        this (new StringWriter(4096));
    }

	/**
	 * The line ending in use.
	 */
	public String getNewLine() {
		return _newLine;
	}

	/**
	 * Sets the String used to render new lines.
	 * 
	 * @param newLine
	 *        value to write for new lines.
	 * 
	 * @see #getNewLine()
	 */
	public void setNewLine(String newLine) {
		this._newLine = newLine;
	}
	
	/**
	 * The number of indentation characters added at each indentation level.
	 */
	public int getIndentStep() {
		return _indentStep;
	}

	/**
	 * @see #getIndentStep()
	 */
	public void setIndentStep(int indentStep) {
		_indentStep = indentStep;
	}
	
	/**
	 * The buffer to take indentation characters from.
	 */
	public String getIndentWhitespace() {
		return _indentWhitespace;
	}

	/**
	 * @see #setIndentWhitespace(String)
	 */
	public void setIndentWhitespace(String indentWhitespace) {
		_indentWhitespace = indentWhitespace;
	}
	
	/**
	 * Sets whether to indent the output.
	 * 
	 * @param doIndent
	 *        Whether to indent.
	 * @return Whether indentation was enabled before.
	 */
    public boolean setIndent(boolean doIndent) {
		boolean before = this._doIndent;
		this._doIndent = doIndent;
		return before;
	}
    
    /**
     * Whether {@link #setIndent(boolean)} is enabled.
     */
    public final boolean isIndenting() {
		return _doIndent;
	}
    
    /**
     * Return accumulated HTML when used with default CTor.
     *
     * @return    default Object implemenation of toString() 
     *            when not created with default CTor.
     */
    @Override
	public String toString () {
		Writer target = internalTargetWriter();
		if (target instanceof StringWriter) {
			// Note: With an underlying StringWriter, buffering is not enabled, therefore, no flush
			// is required.
			return target.toString();
        }

        return super.toString();    
    }

	/**
	 * Increases the indentation for the next writing operation.
	 * Use only when writing Javascript.
	 */
	public void increaseIndent() {
        indent += _indentStep;
	}

    /**
     * Decreases the indentation for the next writing operation.
     * Use only when writing Javascript.
     */	
	public void decreaseIndent() {
        indent -= _indentStep; 
	}

	/**
	 * Write xml Header with the given encoding.
	 * 
	 * @see TagUtil#writeXMLHeader(Appendable, String)
	 */
	public void writeXMLHeader(String encoding) throws IOException {
		TagUtil.writeXMLHeader(out, encoding);
		nl();
	}

	/**
	 * Write the given XML fragment without encoding.
	 * 
	 * <p>
	 * The given fragment must be a well-formed XML fragment. A well-formed XML
	 * fragment results in a well formed XML document, if it is directly
	 * embedded into a single top-level element. With other words: Start and end
	 * tags in the given fragment must match. The fragment may consist of more
	 * than one top-level element and text content is allowed without embedding
	 * it into a top-level element.
	 * </p>
	 * 
	 * @param xmlFragment
	 *        The XML fragment to be directly embedded into the output document.
	 * 
	 * @deprecated Dangerous API. Better use methods that ensure that the output
	 *             is well-formed. This method has the potential to introduce
	 *             XSS weaknesses.
	 */
    @Deprecated
    public void writeContent(CharSequence xmlFragment) throws IOException {
		switch (state) {
			case START_TAG:
				// Separate first attribute from tag name.
				out.write(' ');
				break;
			case ATTRIBUTE_START:
				startAttribute();
				break;
			case CLASS_ATTRIBUTE_START:
				if (!shouldWriteClass(xmlFragment)) {
					return;
				}
				startClassAttribute();
				break;
			case CLASS_ATTRIBUTE:
				if (!shouldWriteClass(xmlFragment)) {
					return;
				}
				writeClassSeparator();
				break;
			default:
				break;
		}
		if (_quoting) {
			TagUtil.writeCDataContent(out, xmlFragment);
		} else {
			out.write(xmlFragment.toString());
		}
    }
    
    /**
	 * Write the given text in element content (by quoting XML control
	 * characters).
	 * 
	 * <p>
	 * <b>Note:</b> A parent element must be open e.g. after calling
	 * {@link #beginTag(String)}. This method <b>must not</b> be used to encode
	 * an attribute value after calling {@link #beginAttribute(String)}.
	 * </p>
	 * 
	 * @param aText
	 *        The text content to be written.
	 *        
	 * @see #writeAttributeText(CharSequence)
	 */
	public void writeText(CharSequence aText) {
		try {
			append(aText);
		} catch (IOException ex) {
			throw new IOError(ex);
		}
	}

	/**
	 * Write the given text in element content (by quoting XML control characters).
	 * 
	 * <p>
	 * <b>Note:</b> A parent element must be open e.g. after calling {@link #beginTag(String)}. This
	 * method <b>must not</b> be used to encode an attribute value after calling
	 * {@link #beginAttribute(String)}.
	 * </p>
	 * 
	 * @param buffer
	 *        The character buffer to take the text from.
	 * @param offset
	 *        The index of the first character to write.
	 * @param length
	 *        the number of characters to encode.
	 * 
	 * @see #writeAttributeText(CharSequence)
	 */
	public final void writeText(char[] buffer, int offset, int length) throws IOException {
		write(buffer, offset, length);
	}

	/**
	 * Opens an inline script block.
	 * 
	 * <p>
	 * Within an inline script block, unquoted contents is written. Applications must make sure that
	 * they never write any of the character sequences <code>"&lt;/script>"</code> or "]]&gt;"
	 * within those script blocks.
	 * </p>
	 * 
	 * <p>
	 * An inline script block must be closed with {@link #endScript()}.
	 * </p>
	 * 
	 * @throws IOException
	 *         If writing to the underlying writer fails.
	 */
	public void beginScript() throws IOException {
		beginBeginTag(SCRIPT_TAG);
		writeAttribute(SCRIPT_TYPE_ATTR, SCRIPT_TYPE_JAVASCRIPT);
		endBeginTag();

		// Script element in XHTML is declared PCDATA (vs. CDATA in HTML 4). Avoid quoting
		// XML specials in script by using a CDATA section. Use JavaScript comment to be backwards
		// compatible to HTML 4.
		append(_newLine);
		append(SCRIPT_LINE_COMMENT);

		// Note: Must not start a true CDATA section, because not quoting at all (not even CDATA
		// section breaking) must be happen in script tags.
		// Otherwise, browsers interpreting the script in HTML4 or HTML5 mode (not XHTML) will fail
		// since they have overlooked the CDATA section. Even if no "]]>" character sequence is
		// literally rendered in the following script, in CDATA mode, quoting may occur even for
		// single ']' characters, if they are at the end of script chunks passed to this writer.
		// This happens for simplicity avoiding managing an additional state in the wirter that
		// remembers, whether ']' symbols have been seen, wich may compose to the CDATA end sequence
		// "]]>" with following output.
		out.append(TagUtil.CDATA_BEGIN);
		state = State.SCRIPT;

		append(_newLine);
	}

	/**
	 * Closes an inline script block started with {@link #beginScript()}.
	 * 
	 * @throws IOException
	 *         If writing to the underlying writer fails.
	 */
	public void endScript() throws IOException {
		assert state == State.SCRIPT : "Cannot end script in state " + state;

		// XHTML compatibility.
		append(_newLine);
		append(SCRIPT_LINE_COMMENT);
		writeEndCDATA();
		state = State.ELEMENT_CONTENT;

		append(_newLine);

		endTag(SCRIPT_TAG);
	}

	/**
	 * Writes script content.
	 * 
	 * @param text
	 *        Script source.
	 * @throws IOException
	 *         If writing to the underlying writer fails.
	 */
	public final void writeScript(CharSequence text) throws IOException {
		assert state.jsAllowed() : "Not within script content, but " + state;
		
		append(text);
	}

	/**
	 * Writes script content.
	 * 
	 * @param text
	 *        Script source character.
	 * @throws IOException
	 *         If writing to the underlying writer fails.
	 */
	public final void writeScript(char text) throws IOException {
		assert state.jsAllowed() : "Not within script content, but " + state;
		
		append(text);
	}

	/**
	 * Writes script content.
	 * 
	 * @param text
	 *        Script source buffer.
	 * @param offset
	 *        The index of the first script character to write.
	 * @param length
	 *        The lenght of the script to write.
	 * @throws IOException
	 *         If writing to the underlying writer fails.
	 */
	public final void writeScript(char[] text, int offset, int length) throws IOException {
		assert state.jsAllowed() : "Not within script content, but " + state;

		write(text, offset, length);
	}

	/**
	 * Writes the given value as JavaScript literal (either <code>null</code>, string or number).
	 * 
	 * @param value
	 *        The value to quote.
	 */
	public void writeJsLiteral(Object value) throws IOException {
		if (value == null || value instanceof CharSequence) {
			writeJsString((CharSequence) value);
		} else if (value instanceof Number) {
			append(value.toString());
		} else if (value instanceof Boolean) {
			append(value.toString());
		} else {
			throw new IllegalArgumentException("Not a JavaScript literal: " + value);
		}
	}

	/**
	 * Writes the given value as JavaScript string literal.
	 * 
	 * @param value
	 *        The value to quote.
	 * @throws IOException
	 *         If writing to the underlying writer fails.
	 */
	public void writeJsString(CharSequence value) throws IOException {
		switch (state) {
			case ATTRIBUTE_START: {
				startAttribute();
			}
			//$FALL-THROUGH$
			case ELEMENT_CONTENT:
			case ATTRIBUTE: {
				TagUtil.writeAttributeJsString(out, value);
				break;
			}
			case SCRIPT:
			case CDATA_CONTENT: {
				TagUtil.writeDirectJsString(out, value);
				break;
			}
			default: {
				assert false : "No JavaScript in state " + state;
			}
		}
	}

	/**
	 * Starts writing a JavaScript string literal to the given output.
	 * 
	 * <p>
	 * Note: The content is expected to be written in potentially multiple calls to
	 * {@link #writeJsStringContent(CharSequence)}.
	 * </p>
	 * 
	 * <p>
	 * The literal is expected to be closed with a call to {@link #endJsString()}.
	 * </p>
	 */
	public void beginJsString() {
		switch (state) {
			case ATTRIBUTE_START: {
				startAttribute();
				break;
			}
			default: {
				// Nothing.
			}
		}
		state = state.beginJsString();
		TagUtil.beginAttributeJsString(out);
	}

	/**
	 * Writes the given value as part of JavaScript string literal to the given output.
	 * 
	 * <p>
	 * Note: It is expected that {@link #beginJsString()} is called before.
	 * </p>
	 * 
	 * @param value
	 *        The value to encode.
	 */
	public final void writeJsStringContent(CharSequence value) throws IOException {
		assert state.inJsString() : "Not within a JavaScript string literal.";
		append(value);
	}
	
	/**
	 * Implementation of {@link #writeJsStringContent(CharSequence)} in state
	 * {@link State#ATTRIBUTE}.
	 * 
	 * @throws IOException
	 *         If writing to the underlying writer fails.
	 */
	protected void internalWriteJsStringContentInAttribute(CharSequence value) throws IOException {
		TagUtil.writeAttributeJsStringContent(out, value);
	}
	
	/**
	 * Implementation of {@link #writeJsStringContent(CharSequence)} in state
	 * {@link State#CDATA_CONTENT}.
	 * 
	 * @throws IOException
	 *         If writing to the underlying writer fails.
	 */
	protected void internalWriteJsStringContentInCDATA(CharSequence value) throws IOException {
		TagUtil.writeDirectJsStringContent(out, value);
	}
	
	/**
	 * Writes the given value as part of JavaScript string literal to the given output.
	 * 
	 * <p>
	 * Note: It is expected that {@link #beginJsString()} is called before.
	 * </p>
	 * 
	 * @param source
	 *        The buffer to take the script source from.
	 */
	public final void writeJsStringContent(char[] source, int offset, int length) throws IOException {
		assert state.inJsString() : "Not within a JavaScript string literal.";
		write(source, offset, length);
	}

	/**
	 * Implementation of {@link #writeJsStringContent(char[], int, int)} in state
	 * {@link State#ATTRIBUTE}.
	 */
	protected void internalWriteJsStringContentInAttribute(char[] source, int offset, int length) throws IOException {
		for (int n = offset, end = offset + length; n < end; n++) {
			internalWriteJsStringContentInAttribute(source[n]);
		}
	}

	/**
	 * Implementation of {@link #writeJsStringContent(char[], int, int)} in state
	 * {@link State#CDATA_CONTENT}.
	 */
	protected void internalWriteJsStringContentInCDATA(char[] source, int offset, int length) throws IOException {
		for (int n = offset, end = offset + length; n < end; n++) {
			internalWriteJsStringContentInCDATA(source[n]);
		}
	}

	/**
	 * Writes the given value as part of JavaScript string literal to the given output.
	 * 
	 * <p>
	 * Note: It is expected that {@link #beginJsString()} is called before.
	 * </p>
	 * 
	 * @param value
	 *        The value to encode.
	 */
	public final void writeJsStringContent(char value) throws IOException {
		assert state.inJsString() : "Not within a JavaScript string literal.";
		append(value);
	}
	
	/**
	 * Implementation of {@link #writeJsStringContent(char)} in state {@link State#ATTRIBUTE}.
	 * 
	 * @throws IOException
	 *         If writing to the underlying writer fails.
	 */
	protected void internalWriteJsStringContentInAttribute(char value) throws IOException {
		TagUtil.writeAttributeJsStringContent(out, value);
	}

	/**
	 * Implementation of {@link #writeJsStringContent(char)} in state {@link State#CDATA_CONTENT}.
	 * 
	 * @throws IOException
	 *         If writing to the underlying writer fails.
	 */
	protected void internalWriteJsStringContentInCDATA(char value) throws IOException {
		TagUtil.writeDirectJsStringContent(out, value);
	}
	
	/**
	 * Ends a JavaScript string literal started with {@link #beginJsString()}.
	 */
	public void endJsString() {
		state = state.endJsString();
		TagUtil.endAttributeJsString(out);
	}

	/**
	 * Write the given string as XML-encoded attribute value.
	 * 
	 * @param value
	 *        The attribute value to encode.
	 * @throws IOException
	 *         If writing to the underlying writer fails.
	 *
	 * @see #writeText(CharSequence) Writing text within element content.
	 * @see #writeAttribute(String, CharSequence) Writing the complete attribute key value pair.
	 */
	public final void writeAttributeText(CharSequence value) throws IOException {
		append(value);
	}

	private void writeClassSeparator() {
		TagUtil.writeAttributeTextDQuot(out, 0, ' ');
	}

	private boolean shouldWriteClass(CharSequence value) {
		if (value == null) {
			return false;
		}
		String text = value.toString().trim();
		if (text.isEmpty()) {
			return false;
		}
		return true;
	}

	private void startClassAttribute() {
		internalBeginAttribute(_attributeName);
		state = State.CLASS_ATTRIBUTE;
	}

	/**
	 * Write the given string as XML-encoded attribute value.
	 * 
	 * @param buffer
	 *        The character buffer to take the text from.
	 * @param offset
	 *        The index of the first character to write.
	 * @param length
	 *        the number of characters to encode.
	 * @throws IOException
	 *         If writing to the underlying writer fails.
	 */
	public final void writeAttributeText(char[] buffer, int offset, int length) throws IOException {
		write(buffer, offset, length);
	}

	private void writeAttributeStart(char[] buffer, int offset, int length) {
		startAttribute();
		writeAttribute(buffer, offset, length);
	}

	private void startAttribute() {
		internalBeginAttribute(_attributeName);
		state = State.ATTRIBUTE;
	}

	private void writeAttribute(char[] buffer, int offset, int length) {
		TagUtil.writeAttributeTextDQuot(out, buffer, offset, length);
	}

	private void writeClass(char[] buffer, int offset, int length) {
		while (length > 0 && Character.isWhitespace(buffer[offset])) {
			offset++;
			length--;
		}
		while (length > 0 && Character.isWhitespace(buffer[offset + length - 1])) {
			length--;
		}
		if (length > 0) {
			writeClassSeparator();
			writeAttribute(buffer, offset, length);
		}
	}

	private void writeClassStart(char[] buffer, int offset, int length) {
		while (length > 0 && Character.isWhitespace(buffer[offset])) {
			offset++;
			length--;
		}
		while (length > 0 && Character.isWhitespace(buffer[offset + length - 1])) {
			length--;
		}
		if (length > 0) {
			startClassAttribute();
			writeAttribute(buffer, offset, length);
		}
	}

	/**
	 * Verbatim write the given text content (by quoting XML control characters).
	 * 
	 * <p>
	 * Either a parent element must be open e.g. after calling {@link #beginTag(String)}, or an
	 * attribute must be open after a call to {@link #beginAttribute(String)}. Quoting is performed
	 * depending on the current context (element content or attribute content).
	 * </p>
	 * 
	 * @param aText
	 *        The text content to be written.
	 */
    public void writeText(char aText) {
		try {
			append(aText);
		} catch (IOException ex) {
			throw new IOError(ex);
		}
    }
    
	/**
	 * Write the given character as XML-encoded attribute value.
	 * 
	 * @param value
	 *        The character to encode.
	 * @throws IOException
	 *         If writing to the underlying writer fails.
	 * 
	 * @see #writeText(CharSequence)
	 */
	public final void writeAttributeText(char value) throws IOException {
		write(value);
	}

    /**
	 * Write a newline and then a number of spaces as of current indentation level.
	 * 
	 * @see #indented() Only writing the indentation spaces.
	 * @see #nl() separately writing a single line break.
	 */
    public void writeIndent() {
    	// Error checking on a PrintWriter is a major performance problem:
		// checkError() flushes the stream and creates a new TCP/IP packet. 
		// If checking for errors at each indentation, a new packet is created
		// for every tag that is written with this writer. 
    	// 
    	// // Error checking to abort page generation, if the underlying stream has
		// // detected a connection breakdown.
        // if (out.checkError()) {
        //     throw new IOException("Output closed");
    	// }

    	try {
			nl();
			indented();
		} catch (IOException ex) {
			throw new IOError(ex);
		}
    }

	/**
	 * Writes the number of spaces corresponding to the current indentation level.
	 * 
	 * <p>
	 * In contrast to {@link #writeIndent()}, no new line characters are written.
	 * </p>
	 * 
	 * @return This {@link TagWriter} for call chaining.
	 */
	public TagWriter indented() throws IOException {
		out.write(_indentWhitespace, 0, Math.min(_indentWhitespace.length(), indent));
		return this;
	}

	/**
	 * Writes a (textual) single line break.
	 */
	public void nl() throws IOException {
		assert state.spaceAllowed();
		out.write(_newLine);
	}

	/**
	 * {@link PrintWriter} facade to the {@link Writer} interface of this {@link TagWriter}.
	 * 
	 * <p>
	 * Through the returned {@link PrintWriter}, only textual content may be output.
	 * </p>
	 */
	public PrintWriter getPrinter() {
		if (_outPrinter == null) {
			_outPrinter = new PrintWriter(this);
		}
		return _outPrinter;
    }

	/**
	 * Hands out the underlying writer circumventing all structural checks of {@link TagWriter}.
	 * 
	 * @deprecated Avoid using this unsafe API whenever possible, see
	 *             {@link #writeContent(CharSequence)}. Instead use the tag generation API of
	 *             {@link TagWriter}, e.g. {@link #beginTag(String)} and {@link #endTag(String)}.
	 */
	@Deprecated
	public Writer contentWriter() {
		return out;
	}

    /** 
     * Begin a tag by writing &lt;tag.
     *
     * You must call endBeginTag after writing the inner part 
     *  (usually the attributes) of the tag.
     */
    public void beginBeginTag(String tag) {
    	assert state.beginTagAllowed() : "No tag in state " + state;
        stack.push(tag);
        if (_doIndent) {
             writeIndent();
        }
        TagUtil.beginBeginTag(out, tag);
        state = State.START_TAG;
        increaseIndent();
    }

    /**
     * Writes an XML attribute: name="value". The value's special characters are
     * quoted according to XML.
     * 
     * @param name
     *     The name of the attribute to write. May not be <code>null</code>.
     * @param value
     *     Value of the attribute to write. A value of <code>null</code>, 
     *     prevents the attribute from being written at all.
     */
    public void writeAttribute(String name, CharSequence value) {
    	assert state.attributeAllowed() : "No attribute in state " + state;
        assert name != null;
        assert checkAttributeName(name);
        
        TagUtil.writeAttribute(out, name, value);
    }
    
	/**
	 * Opens an attribute with the given name.
	 * 
	 * <p>
	 * The attribute value is expected to be written through
	 * {@link #writeAttributeText(CharSequence)} or corresponding methods.
	 * </p>
	 * 
	 * <p>
	 * If no value (or just <code>null</code>) is written, then the attribute itself is also not
	 * written.
	 * </p>
	 * 
	 * @see #endAttribute() Closing the attribute after its complete value has been written.
	 * 
	 * @param name
	 *        The name of the attribute to open.
	 */
    public void beginAttribute(String name) {
		assert state.attributeAllowed() : "No attribute in state " + state;

		_attributeName = name;
		state = State.ATTRIBUTE_START;
    }

	private void internalBeginAttribute(String name) {
		assert name != null;
    	assert checkAttributeName(name);
    	
    	TagUtil.beginAttribute(out, name);
	}
    
	/**
	 * Closes an attribute opened with {@link #beginAttribute(String)}.
	 */
    public void endAttribute() {
		if (state == State.ATTRIBUTE) {
			assert state.endAttributeAllowed() : "No attribute end in state " + state;
			internalEndAttribute();
		} else {
			assert state == State.ATTRIBUTE_START : "No attribute end in state " + state;
			state = State.START_TAG;
		}
    	
		_attributeName = null;
    }

	private void internalEndAttribute() {
		TagUtil.endAttribute(out);
    	state = State.START_TAG;
	}
    
	/**
	 * Closes an CSS class attribute opened with {@link #beginCssClasses()}.
	 */
	public void endCssClasses() {
		if (state == State.CLASS_ATTRIBUTE) {
			internalEndAttribute();
		} else {
			assert state == State.CLASS_ATTRIBUTE_START : "No class attribute end in state " + state;
			state = State.START_TAG;
		}
		_attributeName = null;
	}

	/**
	 * Begins a CSS class attribute.
	 * 
	 * <p>
	 * The attribute is only created, if some contents is written before {@link #endCssClasses()}
	 * with {@link #append(CharSequence)} or {@link #writeAttributeText(CharSequence)}. Between
	 * successive calls to {@link TagWriter#append(CharSequence)} and
	 * {@link TagWriter#writeAttributeText(CharSequence)} correct separation character between CSS
	 * classes are entered automatically.
	 * </p>
	 */
	public void beginCssClasses() {
		beginCssClasses(CLASS_ATTR);
	}

	/**
	 * Begins a CSS class attribute with the given name.
	 * 
	 * <p>
	 * The attribute is only created, if some contents is written before {@link #endCssClasses()}
	 * with {@link #append(CharSequence)} or {@link #writeAttributeText(CharSequence)}. Between
	 * successive calls to {@link TagWriter#append(CharSequence)} and
	 * {@link TagWriter#writeAttributeText(CharSequence)} correct separation character between CSS
	 * classes are entered automatically.
	 * </p>
	 * 
	 * @param classAttr
	 *        The name of the attribute that receives CSS classes content.
	 */
	public void beginCssClasses(String classAttr) {
		assert state.attributeAllowed() : "No class attribute in state " + state;
		_attributeName = classAttr;
		state = State.CLASS_ATTRIBUTE_START;
	}

    /**
     * Writes an XML attribute: name="value". The value's special characters are
     * quoted for xml.
     * @param name the attributes name
     * @param value the attributes value
     */
    public void writeAttribute(String name, char value) {
        assert state.attributeAllowed() : "No attribute in state " + state;
        assert checkAttributeName(name);
        
    	TagUtil.writeAttribute(out, name, value);
    }
    
    
    /**
     * Writes an XML attribute: name="value". The value's special characters are
     * quoted for xml.
     * @param name the attributes name
     * @param value the attributes value
     */
    public void writeAttribute(String name, boolean value) {
        assert state.attributeAllowed() : "No attribute in state " + state;
        assert checkAttributeName(name);
        
    	TagUtil.writeAttribute(out, name, value);
    }
    
    
    /**
     * Writes an XML attribute: name="value". The value's special characters are
     * quoted for xml.
     * @param name the attributes name
     * @param value the attributes value
     */
	public void writeAttribute(String name, int value) {
		assert state.attributeAllowed() : "No attribute in state " + state;
		assert checkAttributeName(name);

		TagUtil.writeAttribute(out, name, value);
    }

	/**
	 * Writes an XML attribute: name="value". The value's special characters are quoted for xml.
	 * 
	 * @param name
	 *        the attributes name
	 * @param value
	 *        the attributes value
	 */
	public void writeAttribute(String name, long value) {
		assert state.attributeAllowed() : "No attribute in state " + state;
		assert checkAttributeName(name);

		TagUtil.writeAttribute(out, name, value);
	}

    /** 
     * Close the tag opened by beginBeginTag by writing &gt;
     */
    public void endBeginTag()  {
    	assert clearAttributes();
    	assert state.endBeginTagAllowed() : "No closing of start tag in state " + state;
    	
        TagUtil.endBeginTag(out);
        
        state = State.ELEMENT_CONTENT;
    }

	/** 
     * Close an empty tag opened by beginBeginTag by writing /&gt;
     */
    public void endEmptyTag() {
    	assert clearAttributes();
        assert state.endBeginTagAllowed() : "No end tag in state " + state;
    	
        TagUtil.endEmptyTag(out);
        stack.pop();
        decreaseIndent();
        state = state.endTag(stack.isEmpty());
    }

    /** 
     * Begin a tag by writing &lt;tag&gt;
     */
    public void beginTag(String tag) {
    	beginBeginTag(tag);
    	endBeginTag();
    }

    /**
	 * Short hand for a begin tag with a single attribute.
	 * 
	 * @param tag
	 *        The tag name.
	 * @param attributeName
	 *        The name of the single attribute,
	 * @param attributeValue
	 *        The value of the single attribute.
	 * 
	 * @since TL_5_6_1
     */
    public final void beginTag(String tag, String attributeName, CharSequence attributeValue) {
    	beginBeginTag(tag);
    	writeAttribute(attributeName, attributeValue);
    	endBeginTag();
    }
    
    /**
     * Short hand for a begin tag with a given attribute/value pair list.
     * 
     * @param tag
     *        The tag name.
     * @param attributeValuePairs
     *        List of attribute name, attribute value pairs.
	 * 
	 * @since TL_5_6_1
     */
    public final void beginTag(String tag, String... attributeValuePairs) {
    	beginBeginTag(tag);
    	for (int n = 0, cnt = attributeValuePairs.length; n < cnt; n+=2) {
    		writeAttribute(attributeValuePairs[n], attributeValuePairs[n + 1]);
    	}
    	endBeginTag();
    }

    /** 
     * End a tag by writing &lt;/tag inner&gt;
     */
    public void endTag(String tag) {
        assert state.endTagAllowed() : "No end tag in state " + state;
	    assert !stack.isEmpty() : "Root tag already closed.";
	    Object current = stack.pop();
	    assert tag.equals(current) : "Expected '" + current + "'";
	    
         decreaseIndent();
         if (_doIndent) {
             writeIndent();
         }
         TagUtil.endTag(out, tag);

         state = state.endTag(stack.isEmpty());
    }

	/**
	 * Short hand for an empty tag with a single attribute.
	 * 
	 * @param tag
	 *        The tag name.
	 * @param attributeName
	 *        The name of the single attribute,
	 * @param attributeValue
	 *        The value of the single attribute.
	 */
    public final void emptyTag(String tag, String attributeName, CharSequence attributeValue) {
    	beginBeginTag(tag);
    	writeAttribute(attributeName, attributeValue);
    	endEmptyTag();
    }
    
    /** 
     * Write an empty tag by writing &lt;tag /&gt;
     */
    public void emptyTag(String tag) {
    	beginBeginTag(tag);
    	endEmptyTag();
    }

	/**
	 * You may need to flush() the underlying writer using this function.
	 */
	@Override
	public void flush() throws IOException {
		out.flush();
	}

	/**
	 * In buffering mode, flushes the buffer to the underlying writer without flushing the
	 * underlying {@link Writer} itself.
	 */
	public void flushBuffer() throws IOException {
		if (bufferingEnabled()) {
			_outBuffered.flushBuffer();
		}
	}

	/**
	 * You may need to close() the underlying writer using this function.
	 */
	@Override
	public void close() throws IOException {
		out.close();
	}

	/**
	 * Creates a {@link TagWriter} that writes into the given file in the given encoding.
	 * 
	 * @param out
	 *        The {@link File} to write to.
	 * @param encoding
	 *        The encoding of the file
	 * @throws IOException
	 *         In case initialisation of TagWriter fails.
	 */
	public static TagWriter newTagWriter(File out, String encoding) throws IOException {
		boolean success = false;
		FileOutputStream stream = new FileOutputStream(out);
		try {
			TagWriter tagWriter = new TagWriter(new OutputStreamWriter(stream, encoding));
			success = true;
			return tagWriter;
		} finally {
			if (!success) {
				stream.close();
			}
		}
	}

	/**
	 * Starts a CDATA section.
	 * 
	 * <p>
	 * Within a CDATA section, arbitrary textual content may be written using
	 * {@link #writeCDATAContent(CharSequence)}.
	 * </p>
	 * 
	 * 
	 * 
	 * <p>
	 * The section must be ended with a call to {@link #endCData()}.
	 * </p>
	 * 
	 * <p>
	 * Note: CDATA sections must not be nested. Calling {@link #beginCData()} twice (without an
	 * interleaving call to {@link #endCData()}) yields an error.
	 * </p>
	 */
	public void beginCData() throws IOException {
		assert state.beginTagAllowed() : "No CDATA in state " + state;
		state = State.CDATA_CONTENT;

		TagUtil.beginCData(out);
		// Reset CDATA state
		updateCDATAChars('[', 'A');
	}

	/**
	 * Ends a CDATA section started with {@link #beginCData()}.
	 */
	public void endCData() throws IOException {
		assert state == State.CDATA_CONTENT : "No end of CDATA in state " + state;
		state = State.ELEMENT_CONTENT;

		writeEndCDATA();
	}

	private void writeEndCDATA() throws IOException {
		if (_quoting) {
			out.write(QUOTED_CDATA_END);
		} else {
			TagUtil.endCData(out);
		}
	}

	/**
	 * Puts this writer in a mode for rendering quoted XML as text.
	 * 
	 * <p>
	 * Quoting may not be nested. In a quoted XML fragment, no other quoted fragment may be
	 * embedded.
	 * </p>
	 * 
	 * @see #endQuotedXML()
	 */
	public void beginQuotedXML() throws IOException {
		assert !_quoting : "No nested quoting supported.";
		assert state == State.ELEMENT_CONTENT : "Cannot start quoting in state " + state;
		stack.push(QUOTED_XML_PSEUDO_TAG);

		_quoting = true;
		out.write(TagUtil.CDATA_BEGIN);
	}

	/**
	 * Stops quoting mode.
	 * 
	 * @see #beginQuotedXML()
	 */
	public void endQuotedXML() throws IOException {
		assert _quoting : "Not in quoting mode.";
		assert state == State.ELEMENT_CONTENT : "Cannot stop quoting in state " + state;
		assert QUOTED_XML_PSEUDO_TAG.equals(stack.peek()) : "Expected closing tag '" + stack.peek() + "'.";

		stack.pop();
		_quoting = false;
		out.write(TagUtil.CDATA_END);
	}

	/**
	 * Writes arbitrary text within a CDATA section.
	 * 
	 * @param content
	 *        The content to write.
	 */
	public void writeCDATAContent(CharSequence content) throws IOException {
		assert state == State.CDATA_CONTENT : "Not within a CDATA section.";
		switch (content.length()) {
			case 0:
				break;
			case 1:
				internalWriteCDATAContent(content.charAt(0));
				break;
			default:
				breakCDATAIfNeeded(content.charAt(0), content.charAt(1));
				TagUtil.writeCDataContent(out, content);
				updateCDATAChars(content.charAt(content.length() - 2), content.charAt(content.length() - 1));
		}
	}

	/**
	 * Writes arbitrary text within a CDATA section.
	 * 
	 * @param content
	 *        The buffer to take characters from.
	 * @param offset
	 *        The index of the first character to write.
	 * @param length
	 *        the number of characters to write.
	 */
	public void writeCDATAContent(char[] content, int offset, int length) throws IOException {
		assert state == State.CDATA_CONTENT : "Not within a CDATA section.";
		switch (length) {
			case 0:
				break;
			case 1:
				internalWriteCDATAContent(content[offset]);
				break;
			default:
				breakCDATAIfNeeded(content[offset], content[offset + 1]);
				TagUtil.writeCDataContent(out, content, offset, length);
				updateCDATAChars(content[offset + length - 2], content[offset + length - 1]);
		}
	}

	/**
	 * Writes arbitrary character within a CDATA section.
	 * 
	 * @param content
	 *        The content to write.
	 */
	public void writeCDATAContent(char content) throws IOException {
		assert state == State.CDATA_CONTENT : "Not within a CDATA section.";
		internalWriteCDATAContent(content);
	}

	private boolean isCDATAEnd(char char1,char char2,char char3) {
		return char1 == ']' && char2 == ']' && char3 == '>';
	}

	private void internalWriteCDATAContent(char content) throws IOException {
		if (isCDATAEnd(_nextToLastCDATAChar, _lastCDATAChar, content)) {
			TagUtil.breakCDataSection(out);
			updateCDATAChars('A', '[');
		}
		out.append(content);
		updateCDATAChars(_lastCDATAChar, content);
	}

	/**
	 * Updates the cached written CDATA characters.
	 * 
	 * @param nextToLastChar
	 *        The next to last character in CDATA content.
	 * @param lastChar
	 *        The last character in CDATA content.
	 */
	private void updateCDATAChars(char nextToLastChar, char lastChar) {
		_lastCDATAChar = lastChar;
		_nextToLastCDATAChar = nextToLastChar;
	}

	private void breakCDATAIfNeeded(char firstChar, char secondChar) throws IOException {
		if (isCDATAEnd(_nextToLastCDATAChar, _lastCDATAChar, firstChar) ||
			isCDATAEnd(_lastCDATAChar, firstChar, secondChar)) {
			TagUtil.breakCDataSection(out);
		}
	}

	/**
	 * Writes arbitrary text within a {@link State#SCRIPT} section.
	 * 
	 * @param content
	 *        The buffer to take characters from.
	 * @param offset
	 *        The index of the first character to write.
	 * @param length
	 *        the number of characters to write.
	 */
	private void writeScriptContent(char[] content, int offset, int length) throws IOException {
		assert state == State.SCRIPT : "Not within a script section.";
		out.write(content, offset, length);
	}

    /**
	 * Writes the given string as a comment. (with indentation)
	 * <p>
	 * If you don't want indentation, use {@link #beginComment()},
	 * {@link #writeCommentContent(CharSequence)} and {@link #endComment()} instead. (It still
	 * writes an (unwanted) space as first and last comment character, though.)
	 * </p>
	 * 
	 * @param comment
	 *        comment's content.
	 */
    public void writeComment(String comment) {
    	assert state.commentAllowed() : "No comment in state " + state;

        if (comment != null){
            writeIndent();
            TagUtil.writeComment(out, comment);
        }
    }

	/**
	 * Like {@link #writeComment(String)} but without indentation and spaces at the comment start
	 * and end.
	 * <p>
	 * An assert fails, if the comment does not match {@link #XML_COMMENT_CONTENT_PATTERN}.
	 * </p>
	 */
	public void writeCommentPlain(String comment) {
		assert state.commentAllowed() : "No comment in state " + state;

		if (comment != null) {
			assert XML_COMMENT_CONTENT_PATTERN.matcher(comment).matches() : "Not a valid XML comment: '" + comment
				+ "'";
			try {
				out.append("<!--");
				TagUtil.writeCommentContent(out, comment);
				out.append("-->");
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		}
	}

	/**
	 * Begin a HTML comment on a new line, not indented.
	 * 
	 * @see #writeCommentContent(CharSequence)
	 */
    public void beginComment() {
    	assert state.commentAllowed() : "No comment in state " + state;
    
        TagUtil.beginComment(out);
        state = State.COMMENT_CONTENT;
    }

    /**
     * Writes text within a comment.
     * 
     * @param comment The comment text.
     * 
     * @see #beginComment()
     * @see #endComment()
     */
    public void writeCommentContent(CharSequence comment) {
    	assert state.commentContentAllowed() : "Comment content in state " + state;

        TagUtil.writeCommentContent(out, comment.toString());
    }

    /**
	 * Writes text within a comment.
	 * 
	 * @param buffer
	 *        The character buffer to take the text from.
	 * @param offset
	 *        The index of the first character to write.
	 * @param length
	 *        the number of characters to encode.
	 */
	public void writeCommentContent(char[] buffer, int offset, int length) {
		assert state.commentContentAllowed() : "Comment content in state " + state;
		TagUtil.writeCommentContent(out, buffer, offset, length);
	}

	/**
	 * End a HTML comment on a new line, not indented.
	 */
    public void endComment() {
    	assert state.commentContentAllowed() : "No comment end in state " + state;

    	TagUtil.endComment(out);

    	state = state.endComment(stack.isEmpty());
    }
    
    /**
     * Reports the number of currently open elements.
     * 
     * @see #endAll(int)
     */
    public int getDepth() {
    	return stack.size();
    }

    /**
     * Closes all open elements up to and including the document element.
     */
	public void endAll() throws IOException {
    	endAll(0);
    }

    /**
	 * Closes all open elements up to the given depth.
	 * 
	 * <p>
	 * This method can be used in combination with {@link #getDepth()} to
	 * recover from failures during writing a certain block.
	 * </p>
	 * 
	 * <pre>
	 *  int depthBefore = out.{@link #getDepth()};
	 *  try {
	 *      // Code that may terminate exceptionally.
	 *  } catch (Exception ex) {
	 *      out.writeText(ex.getMessage());
	 *      out.{@link #endAll(int) endall(depthBefore)};
	 *  }
	 * </pre>
	 * 
	 * @param depth
	 *     The number of elements that should stay open, when this method
	 *     returns.
	 */
	public void endAll(int depth) throws IOException {
		if (state.inJsString()) {
			endJsString();
		}
		if (state == State.CLASS_ATTRIBUTE || state == State.CLASS_ATTRIBUTE_START) {
			endCssClasses();
		}
		if (state == State.ATTRIBUTE || state == State.ATTRIBUTE_START) {
    		endAttribute();
    	}
    	if (state == State.START_TAG) {
    		endBeginTag();
    	}
		if (state == State.COMMENT_CONTENT) {
			endComment();
		}
		if (state == State.CDATA_CONTENT) {
			endCData();
		}
		if (state == State.SCRIPT) {
			endScript();
		}
    	while (stack.size() > depth) {
    		String current = stack.peek();
			if (Utils.equals(current, QUOTED_XML_PSEUDO_TAG)) {
				endQuotedXML();
			} else {
				endTag(current);
			}
    	}
    }

    /**
	 * Called from an assert statement to conditionally enable the attribute
	 * name check in debug mode.
	 * 
	 * @return <code>true</code>
	 */
    private boolean initChecks() {
    	 this.attributeNames = new HashSet<>();
    	 return true;
    }

    /**
	 * Called from an assert statement to conditionally clear attribute names of
	 * an element in debug mode.
	 * 
	 * @return <code>true</code>
	 */
    private boolean clearAttributes() {
    	this.attributeNames.clear();
    	return true;
	}

    /**
	 * Called from an assert statement to conditionally check, whether the given
	 * attribute was not already included in the current element.
	 * 
	 * @param name
	 *        The name of the currently rendered attribute.
	 * @return <code>true</code>
	 */
	private boolean checkAttributeName(String name) {
		if (!this.attributeNames.add(name)) {
            throw new AssertionError("Attribute '" + name + "', already given in current element: " + stack);
        }
		
		return true;
	}
	
	/**
	 * This method returns a clone of the stack which stores the currently opened tags.
	 */
	@SuppressWarnings("unchecked")
	public final Stack<String> getStack() {
		return (Stack<String>) stack.clone();
	}

	/**
	 * This method changes the {@link Writer} this TagWriter is based onto.
	 * 
	 * @param writer
	 *            must not be <code>null</code>;
	 * @return The writer formerly installed. 
	 * @throws IllegalArgumentException
	 *             if the writer does not fulfills the conditions.
	 */
	public Writer setOut(Writer writer) {
		if (writer == null) {
			throw new IllegalArgumentException("Writer must not be 'null'!");
		}

		Writer oldTarget = internalTargetWriter();
		if (writer == oldTarget) {
			// Nothing changed.
			return writer;
		}

		if (bufferingEnabled()) {
			// Buffering is enabled. The buffer must be flushed before the underlying writer can be
			// changed.
			try {
				_outBuffered.flushBuffer();
			} catch (IOException ex) {
				throw new IOError(ex);
			}
		}

		initOut(writer);

		return oldTarget;
	}

	/**
	 * Hands out internal writer. 
	 */
	public final Writer internalTargetWriter() {
		return _outProxy.getImpl();
	}

	@Override
	public void write(char[] content, int offset, int length) throws IOException {
		switch (state) {
			case CLASS_ATTRIBUTE_START: {
				writeClassStart(content, offset, length);
				break;
			}
			case CLASS_ATTRIBUTE: {
				writeClass(content, offset, length);
				break;
			}
			case ATTRIBUTE: {
				writeAttribute(content, offset, length);
				break;
			}
			case ATTRIBUTE_START: {
				writeAttributeStart(content, offset, length);
				break;
			}
			case ELEMENT_CONTENT: {
				assert state.textAllowed() : "No text in state " + state;
				TagUtil.writeText(out, content, offset, length);
				break;
			}
			case CDATA_CONTENT: {
				writeCDATAContent(content, offset, length);
				break;
			}
			case SCRIPT: {
				writeScriptContent(content, offset, length);
				break;
			}
			case JS_ELEMENT_STRING:
			case JS_ATTRIBUTE_STRING: {
				internalWriteJsStringContentInAttribute(content, offset, length);
				break;
			}
			case JS_SCRIPT_STRING:
			case JS_CDATA_STRING: {
				internalWriteJsStringContentInCDATA(content, offset, length);
				break;
			}
			case COMMENT_CONTENT: {
				writeCommentContent(content, offset, length);
				break;
			}
			default: {
				failWrongState();
			}
		}
	}

	@Override
	public Writer append(CharSequence csq) throws IOException {
		if (csq == null) {
			return this;
		}
		return super.append(csq);
	}

	private void failWrongState() {
		assert false : "No text content allowed in state " + state;
	}

	/**
	 * Appends the given decimal value.
	 * 
	 * @throws IOException
	 *         If writing fails.
	 * @see #append(CharSequence)
	 */
	public void writeInt(int value) throws IOException {
		StringServices.append(this, value);
	}

	/**
	 * Appends the given decimal value.
	 * 
	 * @throws IOException
	 *         If writing fails.
	 * @see #append(CharSequence)
	 */
	public void writeLong(long value) throws IOException {
		StringServices.append(this, value);
	}

	/**
	 * Appends the given floating point value.
	 * 
	 * @throws IOException
	 *         If writing fails.
	 * @see #append(CharSequence)
	 */
	public void writeFloat(float value) throws IOException {
		int intValue = (int) value;
		if (value == intValue) {
			writeInt(intValue);
		} else {
			append(Float.toString(value));
		}
	}

	/**
	 * The current State of this writer.
	 */
	public State getState() {
		return state;
	}

	/**
	 * Adjusts the current state of this writer, e.g. after calling
	 * {@link #writeContent(CharSequence)}.
	 * 
	 * @param state
	 *        The new state.
	 */
	public void setState(State state) {
		this.state = state;
	}

}
