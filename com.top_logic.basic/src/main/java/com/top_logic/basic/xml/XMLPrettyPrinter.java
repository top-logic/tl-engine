/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.xml;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.filter.TrueFilter;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationSchemaConstants;
import com.top_logic.basic.config.NamedConfiguration;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.MapBinding;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Subtypes;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.CharDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.module.TypedRuntimeModule.ModuleConfiguration;

/**
 * Utility for pretty-printing XML in a normalized form.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class XMLPrettyPrinter implements AutoCloseable {

	/**
	 * Configuration of the {@link XMLPrettyPrinter}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends ConfigurationItem {

		/** @see #isXMLHeader() */
		String XML_HEADER = "xml-header";

		/** @see #getEncoding() */
		String ENCODING = "encoding";

		/** @see #getAdditionalNamespaces() */
		String ADDITIONAL_NAMESPACES = "additional-namespaces";

		/** @see #isNoIndent() */
		String NO_INDENT = "no-indent";

		/** @see #isNoAttributeIndent() */
		String NO_ATTRIBUTE_INDENT = "no-attribute-indent";

		/** @see #getIndentChar() */
		String INDENT_CHAR = "indent-char";

		/** @see #getIndentStep() */
		String INDENT_STEP = "indent-step";

		/** @see #getMaxIndent() */
		String MAX_INDENT = "max-indent";

		/** @see #getNewLineStyle() */
		String NEW_LINE_STYLE = "new-line-style";

		/** Default value of {@link #getEncoding()}. */
		String DEFAULT_ENCODING = StringServices.UTF8;

		/**
		 * @see #getPreserveWhitespace()
		 */
		String PRESERVE_WHITESPACE = "preserve-whitespace";

		/**
		 * The encoding used to produce the output.
		 */
		@Name(ENCODING)
		@StringDefault(Config.DEFAULT_ENCODING)
		String getEncoding();

		/**
		 * Setter of {@link #getEncoding()}.
		 */
		void setEncoding(String encoding);

		/**
		 * Whether the XML header must be written for a {@link Document}.
		 */
		@Name(XML_HEADER)
		@BooleanDefault(true)
		boolean isXMLHeader();

		/**
		 * Setter of {@link #isXMLHeader()}.
		 */
		void setXMLHeader(boolean b);

		/**
		 * Whether in general no indentation must be written.
		 */
		@Name(NO_INDENT)
		@BooleanDefault(false)
		boolean isNoIndent();

		/**
		 * Setter of {@link #isNoIndent()}.
		 */
		void setNoIndent(boolean b);

		/**
		 * Whether no indentation of attribute values must be written, i.e. there is an indentation
		 * between different tags, but no indentation between different attribute values.
		 */
		@Name(NO_ATTRIBUTE_INDENT)
		@BooleanDefault(false)
		boolean isNoAttributeIndent();

		/**
		 * Setter of {@link #isNoAttributeIndent()}.
		 */
		void setNoAttributeIndent(boolean b);
		
		/**
		 * Returns the additional namespaces to be written.
		 */
		@Name(ADDITIONAL_NAMESPACES)
		@MapBinding(key = "prefix", attribute = "uri")
		Map<String, String> getAdditionalNamespaces();

		/**
		 * Setter for {@link #getAdditionalNamespaces()}.
		 * 
		 * @see #getAdditionalNamespaces()
		 */
		void setAdditionalNamespaces(Map<String, String> namespaces);

		/**
		 * How new-lines are encoded.
		 */
		@Name(NEW_LINE_STYLE)
		NewLineStyle getNewLineStyle();

		/**
		 * @see #getNewLineStyle()
		 */
		void setNewLineStyle(NewLineStyle value);

		/**
		 * The character to use for indentation.
		 */
		@Name(INDENT_CHAR)
		@CharDefault('\t')
		char getIndentChar();

		/**
		 * @see #getIndentChar()
		 */
		void setIndentChar(char value);

		/**
		 * The number of {@link #getIndentChar() indent chars} to use per level.
		 */
		@Name(INDENT_STEP)
		@IntDefault(1)
		int getIndentStep();

		/**
		 * @see #getIndentStep()
		 */
		void setIndentStep(int value);

		/**
		 * The maximum indentation depth.
		 */
		@Name(MAX_INDENT)
		@IntDefault(20)
		int getMaxIndent();

		/**
		 * @see #getMaxIndent()
		 */
		void setMaxIndent(int value);


		/**
		 * Whether elements with single-line text content should be rendered in one line.
		 */
		@BooleanDefault(true)
		boolean getCompactSingleLineText();

		/**
		 * @see #getCompactSingleLineText()
		 */
		void setCompactSingleLineText(boolean value);


		/**
		 * {@link Filter} that must accept a tag name to allow creating an empty tag.
		 */
		@InstanceFormat
		@InstanceDefault(TrueFilter.class)
		@Subtypes({}) // Do not require the type indexer module.
		Filter<? super String> getEmptyTags();

		/**
		 * @see #getEmptyTags()
		 */
		void setEmptyTags(Filter<? super String> value);

		/**
		 * Whether structure in text content (expressed with '{' and '}' characters) should
		 * influence indentation.
		 */
		boolean getStructuredText();

		/**
		 * @see #getStructuredText()
		 */
		void setStructuredText(boolean value);

		/**
		 * Whether white space in non-white-space-only text should be preserved.
		 */
		@Name(PRESERVE_WHITESPACE)
		boolean getPreserveWhitespace();

		/**
		 * @see #getPreserveWhitespace()
		 */
		void setPreserveWhitespace(boolean value);
	}

	/**
	 * Utility for dumping the given {@link Node} to the given stream.
	 * 
	 * @param stream
	 *        The stream to write to.
	 * @param node
	 *        The {@link Node} to dump.
	 * @throws IOException
	 *         If writing fails.
	 */
	public static final void dump(OutputStream stream, Node node) throws IOException {
		dump(newConfiguration(), stream, node);
	}

	/**
	 * Utility for dumping the given {@link Node} to the given stream.
	 * 
	 * @param config
	 *        The pretty-printer configuration.
	 * @param stream
	 *        The stream to write to.
	 * @param node
	 *        The {@link Node} to dump.
	 * @throws IOException
	 *         If writing fails.
	 */
	public static void dump(Config config, OutputStream stream, Node node)
			throws IOException, UnsupportedEncodingException {
		try (XMLPrettyPrinter printer = new XMLPrettyPrinter(stream, config)) {
			printer.write(node);
		}
	}

	/**
	 * Produces a pretty-printed version of the given XML string.
	 * 
	 * @param xml
	 *        The source XML.
	 * @return The pretty-printed version.
	 */
	public static String prettyPrint(String xml) {
		return prettyPrint(newConfiguration(), xml);
	}

	/**
	 * Produces a pretty-printed version of the given XML string.
	 * 
	 * @param config
	 *        The pretty-printer configuration.
	 * @param xml
	 *        The source XML.
	 * 
	 * @return The pretty-printed version.
	 */
	public static String prettyPrint(Config config, String xml) throws UnreachableAssertion {
		return prettyPrint(config, DOMUtil.parse(xml));
	}

	/**
	 * Produces a pretty-printed version of the given XML node.
	 * 
	 * @param node
	 *        The source XML.
	 * @return The pretty-printed version.
	 */
	public static String prettyPrint(Node node) throws UnreachableAssertion {
		return prettyPrint(newConfiguration(), node);
	}

	/**
	 * Produces a pretty-printed version of the given XML node.
	 * 
	 * @param config
	 *        The pretty-printer configuration.
	 * @param node
	 *        The source XML.
	 * @return The pretty-printed version.
	 */
	public static String prettyPrint(Config config, Node node) throws UnreachableAssertion {
		String xml;
		try {
			ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
			dump(config, byteBuffer, node);
			xml = new String(byteBuffer.toByteArray(), config.getEncoding());
			return xml;
		} catch (IOException ex) {
			throw new UnreachableAssertion("Writing to buffer must not fail.", ex);
		}
	}

	private static final Map<QName, Integer> ID_ATTRIBUTE_PRIORITY = new HashMap<>();
	{
		ID_ATTRIBUTE_PRIORITY.put(new QName(null, "key"), 1);
		ID_ATTRIBUTE_PRIORITY.put(new QName(null, "id"), 2);
		ID_ATTRIBUTE_PRIORITY.put(new QName(ConfigurationSchemaConstants.CONFIG_NS,
			ConfigurationSchemaConstants.CONFIG_INTERFACE_ATTR), 3);
		ID_ATTRIBUTE_PRIORITY.put(new QName(null, PolymorphicConfiguration.IMPLEMENTATION_CLASS_NAME), 4);
		ID_ATTRIBUTE_PRIORITY.put(new QName(null, ModuleConfiguration.SERVICE_CLASS), 5);
		ID_ATTRIBUTE_PRIORITY.put(new QName(null, NamedConfiguration.NAME_ATTRIBUTE), 6);
	}

	private static final Comparator<Attribute> NAME_ORDER = new Comparator<>() {
		@Override
		public int compare(Attribute n1, Attribute n2) {
			String ns1 = n1.getPrefix();
			String ns2 = n2.getPrefix();
			if (ns1 == null) {
				if (ns2 == null) {
					return compareLocalName(n1, n2);
				} else {
					return 1;
				}
			} else {
				if (ns2 == null) {
					return -1;
				} else {
					int nsCompare = ns1.compareTo(ns2);
					if (nsCompare != 0) {
						return nsCompare;
					}
					return compareLocalName(n1, n2);
				}
			}
		}

		private int compareLocalName(Attribute n1, Attribute n2) {
			String name1 = n1.getName();
			String name2 = n2.getName();
			return name1.compareTo(name2);
		}
	};

	/**
	 * Creates a new configuration for the {@link XMLPrettyPrinter}.
	 * 
	 * @return A configuration valid for
	 *         {@link XMLPrettyPrinter#XMLPrettyPrinter(OutputStream, Config)}.
	 */
	public static Config newConfiguration() {
		return TypedConfiguration.newConfigItem(Config.class);
	}

	private final Map<String, String> _prefixByNamespace = new HashMap<>();

	private final TagWriter _out;

	private int _nextId = 1;

	private List<Attribute> _nodeBuffer = new ArrayList<>();

	private boolean _writeNameSpaces;

	private final String _encoding;

	private final boolean _xmlHeader;

	private final Map<String, String> _additionalNamespaces;

	private boolean _compactSingleLineText;

	private final Filter<? super String> _emptyTags;

	private final boolean _structuredText;

	private boolean _preserveWhiteSpace;

	/**
	 * Creates a {@link XMLPrettyPrinter}.
	 * 
	 * @param stream
	 *        The stream to write to.
	 * @param encoding
	 *        The character encoding to use.
	 * @param xmlHeader
	 *        Whether to write an XML header.
	 * @throws UnsupportedEncodingException
	 *         If given encoding is not supported.
	 */
	public XMLPrettyPrinter(OutputStream stream, String encoding, boolean xmlHeader)
			throws UnsupportedEncodingException {
		this(stream, toConfig(encoding, xmlHeader));
	}

	private static Config toConfig(String encoding, boolean xmlHeader) {
		Config config = newConfiguration();
		config.setEncoding(encoding);
		config.setXMLHeader(xmlHeader);
		return config;
	}

	/**
	 * Creates a {@link XMLPrettyPrinter}.
	 * 
	 * @param stream
	 *        The stream to write to.
	 * @param config
	 *        The configuration for this {@link XMLPrettyPrinter}.
	 * @throws UnsupportedEncodingException
	 *         If the configured {@link Config#getEncoding() encoding} is not supported.
	 */
	public XMLPrettyPrinter(OutputStream stream, Config config) throws UnsupportedEncodingException {
		this(new OutputStreamWriter(stream, config.getEncoding()), config);
	}

	/**
	 * Creates a {@link XMLPrettyPrinter}.
	 * 
	 * @param writer
	 *        The writer to write to.
	 * @param config
	 *        The configuration for this {@link XMLPrettyPrinter}. The configured
	 *        {@link Config#getEncoding()} must match the encoding of the writer.
	 * @throws UnsupportedEncodingException
	 *         If the configured {@link Config#getEncoding() encoding} is not supported.
	 */
	public XMLPrettyPrinter(Writer writer, Config config) throws UnsupportedEncodingException {
		_additionalNamespaces = config.getAdditionalNamespaces();
		_encoding = config.getEncoding();
		_xmlHeader = config.isXMLHeader();
		_compactSingleLineText = config.getCompactSingleLineText();
		_emptyTags = config.getEmptyTags();
		_structuredText = config.getStructuredText();
		_preserveWhiteSpace = config.getPreserveWhitespace();

		boolean suppressFirstIndent = !_xmlHeader;
		TagWriter out;
		if (config.isNoAttributeIndent()) {
			out = new PrettyTagWriter(writer, suppressFirstIndent);
		} else {
			out = new AttributeIndentingTagWriter(writer, suppressFirstIndent);
		}
		boolean indent = !config.isNoIndent();
		out.setIndent(indent);

		if (indent) {
			char ch = config.getIndentChar();
			StringBuilder buffer = new StringBuilder();
			for (int n = 0, cnt = config.getMaxIndent(); n < cnt; n++) {
				buffer.append(ch);
			}
			String whiteSpaceBuffer = buffer.toString();
			out.setIndentWhitespace(whiteSpaceBuffer);
			out.setIndentStep(config.getIndentStep());
		}
		out.setNewLine(config.getNewLineStyle().getChars());

		_out = out;
	}

	/**
	 * The {@link TagWriter} in use.
	 */
	public TagWriter getOut() {
		return _out;
	}

	/**
	 * Writes the given {@link Node}.
	 * 
	 * @param node
	 *        The {@link Node} to pretty-pring.
	 * @return This instance for call chaining.
	 * @throws IOException
	 *         If writing fails.
	 */
	public XMLPrettyPrinter write(Node node) throws IOException {
		for (Entry<String, String> additionalNamespaces : _additionalNamespaces.entrySet()) {
			_prefixByNamespace.put(additionalNamespaces.getValue(), additionalNamespaces.getKey());
		}
		collectNamespaces(node);
		_writeNameSpaces = true;
		if (_xmlHeader) {
			_out.writeXMLHeader(_encoding);
		}
		return write(node, containsCData(node));
	}

	/**
	 * Pretty-prints a single {@link Node}.
	 * 
	 * @param node
	 *        The {@link Node} to print.
	 * @param hasCDataSiblings
	 *        Whether indentation on the first level should be prevented.
	 * @return This instance for call chaining.
	 * @throws IOException
	 *         If writing fails.
	 */
	private XMLPrettyPrinter write(Node node, boolean hasCDataSiblings) throws IOException {
		switch (node.getNodeType()) {
			case Node.DOCUMENT_NODE:
				writeChildren(node, containsCData(node));
				break;
			case Node.DOCUMENT_FRAGMENT_NODE:
				writeChildren(node, hasCDataSiblings);
				break;
			case Node.ELEMENT_NODE:
				writeElement((Element) node, hasCDataSiblings);
				break;
			case Node.ATTRIBUTE_NODE:
			case Node.TEXT_NODE:
				Text textNode = (Text) node;
				if (handledAsCData(textNode)) {
					writeCData(textNode);
				} else {
					writeText(textNode, hasCDataSiblings);
				}
				break;
			case Node.CDATA_SECTION_NODE:
				writeCData((Text) node);
				break;
			case Node.COMMENT_NODE:
				writeComment((Comment) node);
				break;
			case Node.DOCUMENT_TYPE_NODE:
				writeDocumentType((DocumentType) node);
				break;
			case Node.ENTITY_NODE:
			case Node.ENTITY_REFERENCE_NODE:
			case Node.NOTATION_NODE:
			case Node.PROCESSING_INSTRUCTION_NODE:
			default:
				throw new UnsupportedOperationException("Node type not supported: " + node.getNodeType());
		}
		return this;
	}

	private boolean handledAsCData(Text textNode) {
		if (!_preserveWhiteSpace) {
			return false;
		}
		String textContent = textNode.getTextContent();
		String trimmed = textContent.trim();
		boolean whiteSpaceOnly = trimmed.isEmpty();
		if (whiteSpaceOnly) {
			return false;
		}
		return (textContent.indexOf('\n') >= 0 || textContent.indexOf('<') >= 0 || textContent.indexOf('&') >= 0);
	}

	private void writeDocumentType(DocumentType node) throws IOException {
		String name = node.getName();
		String publicID = node.getPublicId();
		boolean hasPublicId = !StringServices.isEmpty(publicID);
		String systemID = node.getSystemId();
		boolean hasSystemId = !StringServices.isEmpty(systemID);
		StringBuilder docTypeStart = new StringBuilder();
		docTypeStart.append("<!DOCTYPE ");
		docTypeStart.append(name);
		if (hasPublicId) {
			docTypeStart.append(" PUBLIC \"").append(publicID).append("\"");
			if (hasSystemId) {
				docTypeStart.append(" \"").append(systemID).append("\"");
			}
		} else if (hasSystemId) {
			docTypeStart.append(" SYSTEM \"").append(systemID).append("\"");
		}
		String internalSubset = node.getInternalSubset();
		if (internalSubset != null) {
			String[] split = internalSubset.split("\n");
			docTypeStart.append(" [");
			if (_out.isIndenting()) {
				docTypeStart.append(_out.getNewLine());
			}
			for (int i = 0 ; i < split.length;i++) {
				docTypeStart.append(split[i]);
				if (_out.isIndenting()) {
					docTypeStart.append(_out.getNewLine());
				}
			}
			docTypeStart.append("]");
		}
		docTypeStart.append(">");
		_out.writeContent(docTypeStart);
	}

	private void collectNamespaces(Node node) {
		recordNamespace(node, false);

		NamedNodeMap attributes = node.getAttributes();
		if (attributes != null) {
			for (int n = 0, cnt = attributes.getLength(); n < cnt; n++) {
				recordNamespace(attributes.item(n), true);
			}
		}

		for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			collectNamespaces(child);
		}
	}

	private void recordNamespace(Node node, boolean requireNonEmptyPrefix) {
		String ns = node.getNamespaceURI();
		if (ns != null && !XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(ns)) {
			String prefix = _prefixByNamespace.get(ns);
			if (prefix == null || ("".equals(prefix) && requireNonEmptyPrefix)) {
				prefix = node.getPrefix();
				if (prefix == null) {
					prefix = requireNonEmptyPrefix ? newPrefix() : "";
				}
				prefix = makeUnique(prefix);
				_prefixByNamespace.put(ns, prefix);
			}
		}
	}

	private String makeUnique(String prefix) {
		if (_prefixByNamespace.values().contains(prefix)) {
			do {
				prefix = newPrefix();
			} while (_prefixByNamespace.values().contains(prefix));
		}
		return prefix;
	}

	private String newPrefix() {
		String prefix;
		prefix = "ns" + (_nextId++);
		return prefix;
	}

	@Override
	public void close() throws IOException {
		_out.close();
	}

	private void writeElement(Element node, boolean hasCDataSiblings) throws IOException {
		String tagName = name(node);
		List<Attribute> attributes = list(node.getAttributes());

		if (hasCDataSiblings) {
			boolean before = _out.setIndent(false);
			_out.beginBeginTag(tagName);
			_out.setIndent(before);
		} else {
			_out.beginBeginTag(tagName);
		}

		if (_writeNameSpaces) {
			if (!_prefixByNamespace.isEmpty()) {
				for (Entry<String, String> entry : _prefixByNamespace.entrySet()) {
					String prefix = entry.getValue();
					String ns = entry.getKey();
					Attribute definition;
					if (prefix.isEmpty()) {
						definition = new Attribute("", XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
							XMLConstants.XMLNS_ATTRIBUTE, ns);
					} else {
						definition = new Attribute(XMLConstants.XMLNS_ATTRIBUTE, XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
							prefix, ns);
					}
					attributes.add(definition);
				}
			}

			_writeNameSpaces = false;
		}

		if (hasCDataSiblings) {
			boolean before = _out.setIndent(false);
			writeAttributesPretty(attributes);
			_out.setIndent(before);
		} else {
			boolean multiLine = writeAttributesPretty(attributes);
			if (multiLine && _out.isIndenting() && isAttributeIntent()) {
				_out.decreaseIndent();
				_out.writeIndent();
				_out.increaseIndent();
			}
		}

		if (isEmptyElement(node)) {
			if (_emptyTags.accept(tagName)) {
				_out.endEmptyTag();
			} else {
				_out.endBeginTag();
				_out.endTag(tagName);
			}
		} else {
			_out.endBeginTag();
			boolean containsCData = containsCData(node);
			writeChildren(node, containsCData);
			if (singleLineTextContent(node) || containsCData) {
				boolean before = _out.setIndent(false);
				_out.endTag(tagName);
				_out.setIndent(before);
			} else {
				_out.endTag(tagName);
			}
		}
	}

	boolean isAttributeIntent() {
		return _out instanceof AttributeIndentingTagWriter;
	}

	private boolean singleLineTextContent(Element node) {
		if (!_compactSingleLineText) {
			return false;
		}
		Node lastChild = node.getLastChild();
		return lastChild.getPreviousSibling() == null && isSingleLineText(lastChild);
	}

	private boolean containsCData(Node node) {
		for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getNodeType() == Node.CDATA_SECTION_NODE) {
				return true;
			}
			if (child.getNodeType() == Node.TEXT_NODE && handledAsCData((Text) child)) {
				return true;
			}
		}
		return false;
	}

	private boolean isSingleLineText(Node node) {
		if (node.getNodeType() != Node.TEXT_NODE) {
			return false;
		}

		String content = node.getTextContent().trim();
		if (content.isEmpty()) {
			return false;
		}

		return content.indexOf('\n') < 0;
	}

	private List<Attribute> list(NamedNodeMap nodes) {
		ArrayList<Attribute> result = new ArrayList<>();
		for (int n = 0, cnt = nodes.getLength(); n < cnt; n++) {
			Node node = nodes.item(n);
			String prefix = node.getPrefix();
			if (XMLConstants.XMLNS_ATTRIBUTE.equals(prefix)) {
				continue;
			}
			String ns = node.getNamespaceURI();
			if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(ns)) {
				continue;
			}
			result.add(new Attribute(_prefixByNamespace.get(ns), ns, localName(node), node.getNodeValue()));
		}
		return result;
	}

	private boolean writeAttributesPretty(List<Attribute> attributes) {
		boolean multiLine = attributes.size() > 1;
		if (multiLine) {
			Attribute idAttribute = null;
			for (Attribute attribute : attributes) {
				Integer priority = ID_ATTRIBUTE_PRIORITY.get(attribute.toQName());
				if (priority != null) {
					if (idAttribute == null || priority < ID_ATTRIBUTE_PRIORITY.get(idAttribute.toQName())) {
						if (idAttribute != null) {
							_nodeBuffer.add(idAttribute);
						}
						idAttribute = attribute;
						continue;
					}
				}

				_nodeBuffer.add(attribute);
			}
			Collections.sort(_nodeBuffer, NAME_ORDER);

			if (idAttribute != null) {
				boolean before = _out.setIndent(false);
				_out.writeAttribute(idAttribute.qname(), idAttribute.getNodeValue());
				_out.setIndent(before);
			}
			for (Attribute attribute : _nodeBuffer) {
				_out.writeAttribute(attribute.qname(), attribute.getNodeValue());
			}
			_nodeBuffer.clear();
		} else {
			boolean before = _out.setIndent(false);
			writeAttributesDirect(attributes);
			_out.setIndent(before);
		}
		return multiLine;
	}

	private void writeAttributesDirect(List<Attribute> attributes) {
		for (Attribute attribute : attributes) {
			_out.writeAttribute(attribute.qname(), attribute.getNodeValue());
		}
	}

	private String name(Node node) {
		String namespaceURI = node.getNamespaceURI();
		if (namespaceURI != null) {
			String prefix = _prefixByNamespace.get(namespaceURI);
			assert prefix != null : "No prefix was assigned to namespace: " + namespaceURI;
			if (prefix.isEmpty()) {
				return node.getLocalName();
			}
			return prefix + ":" + node.getLocalName();
		} else {
			return localName(node);
		}
	}

	private String localName(Node node) {
		if (node.getNamespaceURI() == null) {
			// Be compatible with non-namespace-aware documents.
			return node.getNodeName();
		} else {
			return node.getLocalName();
		}
	}

	private void writeText(Text node, boolean hasCDataSiblings) throws IOException {
		String rawContent = node.getTextContent();
		if (hasCDataSiblings) {
			_out.writeText(normalizeNewlines(rawContent));
			return;
		}
		String content = rawContent.trim();
		if (content.isEmpty()) {
			if (multipleLineBreaks(rawContent) && node.getPreviousSibling() != null && node.getNextSibling() != null) {
				_out.nl();
			}
			return;
		}

		String[] lines = content.split("\\r?\\n");
		boolean singleLine = _compactSingleLineText && lines.length == 1;
		if (singleLine && node.getPreviousSibling() == null && node.getNextSibling() == null) {
			String text = lines[0].trim();
			_out.writeText(text);
			if (_structuredText) {
				adjustTextIndent(textIndentDelta(text));
			}
		} else {
			for (String line : lines) {
				String trimmedLine = line.trim();
				int delta = textIndentDelta(trimmedLine);
				boolean unindentSingleLine = isEndAndStartOfIndentBlocks(delta, trimmedLine);
				if (_structuredText) {
					if (unindentSingleLine) {
						adjustTextIndent(-1);
					} else {
						adjustTextIndentBefore(delta);
					}
				}
				_out.writeIndent();
				_out.writeText(trimmedLine);
				if (_structuredText) {
					if (unindentSingleLine) {
						adjustTextIndent(+1);
					} else {
						adjustTextIndentAfter(delta);
					}
				}
			}
		}
	}

	private boolean isEndAndStartOfIndentBlocks(int delta, String line) {
		if (delta != 0) {
			return false;
		}
		/* It is not sufficient to check whether the line starts and ends with '}' and '{',
		 * respectively, as the line might end with a comment. But it is sufficient to check for a
		 * "}" at the beginning, as a line should not start with a comment followed by a '}'. That
		 * style is not supported. */
		return (line.startsWith("}") && (line.lastIndexOf("{") > line.lastIndexOf("}")))
			|| (line.startsWith(")") && (line.lastIndexOf("(") > line.lastIndexOf(")")));
	}

	private void adjustTextIndentBefore(int delta) {
		if (delta < 0) {
			adjustTextIndent(delta);
		}
	}

	private void adjustTextIndentAfter(int delta) {
		if (delta > 0) {
			adjustTextIndent(delta);
		}
	}

	private void adjustTextIndent(int delta) {
		_out.indent += delta;
	}

	private int textIndentDelta(String line) {
		return charCount(line, '{')
			+ charCount(line, '(')
			- charCount(line, ')')
			- charCount(line, '}');
	}

	private int charCount(String line, char ch) {
		int result = 0;
		int index = 0;
		while ((index = line.indexOf(ch, index)) >= 0) {
			result++;
			index++;
		}
		return result;
	}

	private boolean multipleLineBreaks(String content) {
		int firstNL = content.indexOf('\n');
		if (firstNL < 0) {
			return false;
		}
		int secondNL = content.indexOf('\n', firstNL + 1);
		if (secondNL < 0) {
			return false;
		}
		return true;
	}

	/**
	 * While parsing, all newlines are replaced to <code>LF</code>.
	 * 
	 * <p>
	 * Applies the reverse transformation adjusting new line characters to the current output.
	 * </p>
	 * 
	 * @see "https://www.w3.org/TR/REC-xml/#sec-line-ends"
	 */
	private CharSequence normalizeNewlines(String textContent) {
		return textContent.replace("\n", _out.getNewLine());
	}

	private void writeCData(Text node) throws IOException {
		_out.beginCData();
		_out.writeCDATAContent(normalizeNewlines(node.getTextContent()));
		_out.endCData();
	}

	private void writeComment(Comment node) {
		String content = node.getTextContent().trim();
		if (content.isEmpty()) {
			return;
		}
		String[] lines = content.split("\\r?\\n");
		if (lines.length == 1) {
			_out.writeComment(lines[0].trim());
		} else {
			_out.writeIndent();
			_out.beginComment();
			_out.increaseIndent();

			for (String line : lines) {
				_out.writeIndent();
				_out.writeCommentContent(line.trim());
			}
			_out.decreaseIndent();
			_out.writeIndent();

			_out.endComment();
		}
	}

	private void writeChildren(Node node, boolean hasCDataContent) throws IOException {
		for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			write(child, hasCDataContent);
		}
	}

	private boolean isEmptyElement(Element node) {
		for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (!isEmptyText(child)) {
				return false;
			}
		}
		return true;
	}

	private boolean isEmptyText(Node child) {
		return child.getNodeType() == Node.TEXT_NODE && child.getTextContent().trim().isEmpty();
	}

	/**
	 * Tool to normalize single files or whole directories.
	 */
	public static void main(String[] args) throws XMLStreamException, IOException, SAXException {
		Config config = newConfiguration();

		int argCnt = args.length;
		int n = 0;
		while (n < argCnt) {
			String arg = args[n];
			if (!arg.startsWith("-")) {
				break;
			}
			n++;
			if (arg.equals("--")) {
				// All following arguments are files.
				break;
			}
			
			PropertyDescriptor property = config.descriptor().getProperty(arg.substring(1));
			if (property == null) {
				throw new IllegalArgumentException("There is no option '" + arg + "'.");
			}
			String rawValue = args[n++];
			Object value;
			try {
				value = property.getValueProvider().getValue(property.getPropertyName(), rawValue);
			} catch (ConfigurationException ex) {
				throw new IllegalArgumentException(
					"Invalid value '" + rawValue + "' for option '" + arg + "': " + ex.getMessage(), ex);
			}
			config.update(property, value);
		}

		while (n < argCnt) {
			String path = args[n++];
			for (String name : path.split(File.pathSeparator)) {
				File file = new File(name);
				normalize(config, file);
			}
		}
	}

	private static void normalize(Config config, File file)
			throws FileNotFoundException, IOException, XMLStreamException,
			SAXException {
		if (file.isDirectory()) {
			for (File content : FileUtilities.listFiles(file)) {
				if (content.getName().startsWith(".")) {
					continue;
				}
				if (content.isFile() && !content.getName().endsWith(".xml")) {
					continue;
				}

				normalize(config, content);
			}
		} else {
			updateIfChanged(config, file, load(file));
		}
	}

	/**
	 * Normalises the content of the given file.
	 * 
	 * @param file
	 *        The file to normalise. The content must be of type XML.
	 * @throws FileNotFoundException
	 *         When the given file does not exists.
	 * @throws SAXException
	 *         When the given file does not contain valid XML content.
	 */
	public static void normalizeFile(File file) throws FileNotFoundException, IOException, SAXException {
		updateIfChanged(file, load(file));
	}

	/**
	 * Normalizes the given document and writes the content to the given file, when content changed.
	 * 
	 * @param file
	 *        The file to write document to.
	 * @param document
	 *        The {@link Document} to normalize.
	 */
	public static void updateIfChanged(File file, Document document) throws IOException {
		updateIfChanged(newConfiguration(), file, document);
	}

	/**
	 * Normalizes the given document and writes the content to the given file, when content changed.
	 * 
	 * @param config
	 *        The {@link XMLPrettyPrinter} config to use for normalization.
	 * @param file
	 *        The file to write document to.
	 * @param document
	 *        The {@link Document} to normalize.
	 */
	public static void updateIfChanged(Config config, File file, Document document) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		dump(config, buffer, document);

		byte[] normalizedContent = buffer.toByteArray();
		if (!Arrays.equals(normalizedContent, loadBinary(file))) {
			System.out.println("Updating: " + file);
			write(file, normalizedContent);
		}
	}

	private static byte[] loadBinary(File file) throws IOException {
		return FileUtilities.getBytesFromFile(file);
	}

	private static void write(File file, byte[] content) throws FileNotFoundException, IOException {
		try (OutputStream out = new FileOutputStream(file)) {
			out.write(content);
		}
	}

	private static Document load(File file) throws FileNotFoundException, IOException, SAXException {
		return DOMUtil.getDocumentBuilder().parse(file);
	}

	private static class PrettyTagWriter extends TagWriter {

		private boolean _suppressFirstIndent;

		public PrettyTagWriter(Writer out, boolean suppressFirstIndent) {
			super(out);
			_suppressFirstIndent = suppressFirstIndent;
		}

		@Override
		public void writeIndent() {
			if (_suppressFirstIndent) {
				_suppressFirstIndent = false;
			} else {
				super.writeIndent();
			}
		}
	}

	/**
	 * {@link TagWriter} that also indents each attribute of an element.
	 */
	private static final class AttributeIndentingTagWriter extends PrettyTagWriter {

		public AttributeIndentingTagWriter(Writer out, boolean suppressFirstIndent) {
			super(out, suppressFirstIndent);
		}

		@Override
		public void writeAttribute(String name, CharSequence value) {
			// Don't even think about checking for the empty string here! Otherwise,
			// it would be impossible to render empty attributes.
			if (value != null) {
				try {
					if (isIndenting()) {
						writeIndent();
					} else {
						out.append(' ');
					}
					out.append(name);
					out.append("=\"");
				} catch (IOException ex) {
					throw new IOError(ex);
				}
				TagUtil.writeAttributeTextDQuot(out, value);
				TagUtil.endAttribute(out);
			}
		}

		@Override
		public void beginComment() {
			assert state.commentAllowed() : "No comment in state " + state;

			try {
				out.append("<!--");
			} catch (IOException ex) {
				throw new IOError(ex);
			}
			state = State.COMMENT_CONTENT;
		}

		@Override
		public void endComment() {
			assert state.commentContentAllowed() : "No comment end in state " + state;

			try {
				out.append("-->");
			} catch (IOException ex) {
				throw new IOError(ex);
			}

			state = state.endComment(stack.isEmpty());
		}
	}

	static class Attribute {

		private final String _prefix;

		private final String _ns;

		private final String _name;

		private final String _value;

		public Attribute(String prefix, String ns, String name, String value) {
			_prefix = prefix;
			_ns = ns;
			_name = name;
			_value = value;
		}

		public String getPrefix() {
			return _prefix;
		}

		public String getName() {
			return _name;
		}

		public String getNodeValue() {
			return _value;
		}

		public String getNamespaceURI() {
			return _ns;
		}

		public QName toQName() {
			return new QName(getNamespaceURI(), getName());
		}

		public String qname() {
			if (getNamespaceURI() != null && !getPrefix().isEmpty()) {
				return getPrefix() + ":" + getName();
			} else {
				return getName();
			}
		}

	}

}
