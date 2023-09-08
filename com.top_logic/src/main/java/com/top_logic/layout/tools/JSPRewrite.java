/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tools;

import static com.top_logic.basic.xml.DOMUtil.*;
import static com.top_logic.mig.html.HTMLConstants.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.basic.col.filter.PatternFilter;
import com.top_logic.basic.col.filter.SetFilter;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.basic.xml.NewLineStyle;
import com.top_logic.basic.xml.XMLPrettyPrinter;
import com.top_logic.basic.xml.XMLPrettyPrinter.Config;
import com.top_logic.mig.html.HTMLConstants;

/**
 * Base class for rewriting JSPs.
 * 
 * <p>
 * The base implementation just pretty-prints the page.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class JSPRewrite extends Rewriter {

	private boolean _dump = false;

	@Override
	protected int parameter(String[] args, int i) throws Exception {
		String arg = args[i];
		if (arg.equals("dump")) {
			_dump = true;
			return i + 1;
		} else {
			return super.parameter(args, i);
		}
	}

	@Override
	public void handleFile(String fileName) throws Exception {
		descend(new File(fileName));
	}

	private void descend(File file) throws Exception {
		String name = file.getName();
		if (name.startsWith(".")) {
			return;
		}
		if (file.isDirectory()) {
			File[] contents = file.listFiles();
			if (contents != null) {
				for (File content : contents) {
					descend(content);
				}
			}
		} else {
			if (name.endsWith(".jsp") || name.endsWith(".inc") || name.endsWith(".jspf")) {
				handleJSP(file);
			}
		}
	}

	private void handleJSP(File jsp) {
		try {
			String contents = FileUtilities.readFileToString(jsp);
			String target = processContents(contents);

			if (!target.equals(contents)) {
				System.out.println("Rewriting: " + jsp.getAbsolutePath());
				FileUtilities.writeStringToFile(target, jsp);
			}
		} catch (Exception ex) {
			error("Failed to process '" + jsp.getAbsolutePath() + "'.", ex);
		}
	}

	/**
	 * Applies the transformation to the given JSP contents.
	 * 
	 * @param contents
	 *        The JSP contents to process.
	 * @return The transformed result.
	 */
	public String processContents(String contents) throws Exception {
		String wsWithLineBreak = "\\s*[\\r\\n]\\s*";
		contents =
			contents.replaceAll("\\s*(?<!--)%>" + wsWithLineBreak + "<%@", NewLineStyle.SYSTEM.getChars() + "%><%@");
		contents = contents.replaceAll("<%@\\s+", "<%@");

		String quoted = quote(contents);

		DocumentBuilder builder = DOMUtil.newDocumentBuilder();
		Document document;
		try {
			document = builder.parse(new InputSource(new StringReader(quoted)));
		} catch (Exception ex) {
			if (_dump) {
				System.err.println(quoted);
			}
			throw ex;
		}

		transform(document);

		String printed = prettyPrint(document.getDocumentElement());
		String target = unquote(printed);

		target = target.replaceAll("\\s*(?<!--)%>" + wsWithLineBreak + "<layout:html",
			NewLineStyle.SYSTEM.getChars() + "%><layout:html");

		return encode(target);
	}

	private String encode(String target) {
		Charset charset = Charset.forName(FileUtilities.ENCODING);
		CharsetEncoder encoder = charset.newEncoder();
		if (!encoder.canEncode(target)) {
			StringBuilder encoded = new StringBuilder();
			for (int n = 0, cnt = target.length(); n < cnt; n++) {
				char ch = target.charAt(n);
				if (encoder.canEncode(ch)) {
					encoded.append(ch);
				} else {
					encoded.append(charRef(ch));
				}
			}
			target = encoded.toString();
		}
		return target;
	}

	private static String charRef(int ch) {
		return "&#" + ch + ";";
	}

	private void transform(Node node) {
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			if (node.getParentNode() == null) {
				// Root or already removed.
				return;
			}
			transformElement((Element) node);
		}

		descend(node);

		if (node.getNodeType() == Node.ELEMENT_NODE) {
			if (node.getParentNode() == null) {
				// Root or already removed.
				return;
			}
			transformElementAfter((Element) node);
		}
	}

	/**
	 * Called for each XML {@link Element} found in the JSP.
	 * 
	 * @param node
	 *        The node to process.
	 */
	protected void transformElement(Element node) {
		if (node.getTagName().indexOf(':') >= 0) {
			// Within a JSP tag. Quote nested double quotes.
			NamedNodeMap attributes = node.getAttributes();
			for (int n = 0, cnt = attributes.getLength(); n < cnt; n++) {
				Attr attribute = (Attr) attributes.item(n);
				String value = attribute.getValue();
				attribute.setValue(rewriteAttributesValue(value));
			}
		}
	}

	/**
	 * Called for each XML {@link Element} found in the JSP after {@link #transform(Node)} has
	 * processed the complete subtree.
	 * 
	 * @param node
	 *        The node to process.
	 */
	protected void transformElementAfter(Element node) {
		// Hook for subclasses.

		if (SCRIPT_REF.equals(node.getTagName())) {
			String type = node.getAttribute(TYPE_ATTR);
			if (StringServices.isEmpty(type)) {
				type = JAVASCRIPT_TYPE_VALUE;
				node.setAttribute(TYPE_ATTR, type);
			}
			if (JAVASCRIPT_TYPE_VALUE.equals(type)) {
				if (elements(node).iterator().hasNext()) {
					throw new IllegalArgumentException(
						"Missing CDATA section in script content to prevent XML specials from being parsed.");
				}
			}
		}
	}

	private void descend(Node node) {
		for (Node child : children(node)) {
			transform(child);
		}
	}

	private String quote(String jsp) {
		jsp = jsp.replaceAll("<script[^>]*>", "<basic:script>");
		jsp = jsp.replaceAll("</script[^>]*>", "</basic:script>");

		jsp = jsp.replaceAll(Pattern.quote("<basic:script>"), "<basic:script><![CDATA[");
		jsp = jsp.replaceAll(Pattern.quote("</basic:script>"), "]]></basic:script>");

		Pattern jspPattern = Pattern.compile(q("<%") + "|" + q("%>"), Pattern.DOTALL);
		Matcher jspMatcher = jspPattern.matcher(jsp);

		StringBuilder result = new StringBuilder();

		result.append("<jsp>");
		int lastIndex = 0;
		while (jspMatcher.find(lastIndex)) {
			String separator = jspMatcher.group();
			String contentBefore = jsp.substring(lastIndex, jspMatcher.start());
			if (separator.endsWith(">")) {
				// End of JSP content.
				contentBefore = quoteJSP(contentBefore);
			} else {
				// Start of JSP content.
				contentBefore = quoteXML(contentBefore);
			}
			result.append(contentBefore);
			result.append(quoteSeparator(separator));
			lastIndex = jspMatcher.end();
		}
		result.append(quoteXML(jsp.substring(lastIndex)));
		result.append("</jsp>");

		return result.toString();
	}

	private String quoteXML(String content) {
		content = content.replace("&nbsp;", "&#xA0;");
		content = content.replace("<?", "[[[?");
		content = content.replace("?>", "?]]]");
		content = content.replaceAll(
			"(?s)" + q("<!") + "(" + "(?!" + q("--") + "|" + q("[CDATA[") + ")" + "[^>]*" + ")" + q(">"),
			"[[[!$1]]]");
		return content;
	}

	private String quoteJSP(String content) {
		content = content.replace("&", "&amp;");
		content = content.replace("\"", "&quot;");
		content = content.replace("<", "&lt;");
		content = content.replace(">", "&gt;");
		return content;
	}

	private String unquoteJSP(String content) {
		content = content.replace("&gt;", ">");
		content = content.replace("&lt;", "<");
		content = content.replace("]]]", ">");
		content = content.replace("[[[", "<");
		content = content.replace("&quot;", "\"");
		content = content.replace("&#39;", "'");
		content = content.replace("&amp;", "&");
		return content;
	}

	private String unquoteXML(String content) {
		content = content.replace(HTMLConstants.NBSP, "&#xA0;");
		content = content.replace("]]]", ">");
		content = content.replace("[[[", "<");
		return content;
	}

	private Object quoteSeparator(String group) {
		boolean isDTD = group.charAt(1) == '!';
		String startQuoted = group.replace("<", "[[[");
		String endQuoted = startQuoted.replace(">", isDTD ? "!]]]" : "]]]");
		return endQuoted;
	}

	private Object unquoteSeparator(String group) {
		return group.replace("[[[", "<").replace("!]]]", ">").replace("]]]", ">");
	}

	private String unquote(String xml) {
		Pattern separatorPattern = Pattern
			.compile(q("[[[%") + "|" + q("%]]]"));
		Matcher matcher = separatorPattern.matcher(xml);

		StringBuilder result = new StringBuilder();

		int lastIndex = 0;
		while (matcher.find()) {
			String separator = matcher.group();
			String content = xml.substring(lastIndex, matcher.start());
			if (separator.endsWith("]]]")) {
				content = unquoteJSP(content);
			} else {
				content = unquoteXML(content);
			}
			result.append(content);
			result.append(unquoteSeparator(separator));
			lastIndex = matcher.end();
		}
		result.append(unquoteXML(xml.substring(lastIndex)));

		String jsp = result.toString().replace("<jsp>", "").replace("</jsp>", "");

		jsp = jsp.replaceAll(Pattern.quote("<basic:script><![CDATA["), "<basic:script>");
		jsp = jsp.replaceAll(Pattern.quote("]]></basic:script>"), "</basic:script>");

		return jsp;
	}

	private String rewriteAttributesValue(String value) {
		Pattern separatorPattern = Pattern
			.compile(q("[[[%") + "|" + q("%]]]"));
		Matcher matcher = separatorPattern.matcher(value);

		StringBuilder result = new StringBuilder();

		int lastIndex = 0;
		while (matcher.find()) {
			String separator = matcher.group();
			String content = value.substring(lastIndex, matcher.start());
			if (separator.endsWith("]]]")) {
				if (content.contains("\"")) {
					content = content.replaceAll("(?<!\\\\)\\\"", "\\\\\"");
				}
			}
			result.append(content);
			result.append(separator);
			lastIndex = matcher.end();
		}
		result.append(value.substring(lastIndex));

		return result.toString();
	}

	private String q(String string) {
		return "\\Q" + string + "\\E";
	}

	private static String prettyPrint(Node node) throws UnreachableAssertion {
		String xml;
		try {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			Config config = XMLPrettyPrinter.newConfiguration();
			config.setXMLHeader(false);
			config.setStructuredText(true);
			config.setNewLineStyle(NewLineStyle.SYSTEM);
			config.setCompactSingleLineText(false);
			config.setEmptyTags(
				FilterFactory.or(
					new SetFilter(HTMLConstants.VOID_ELEMENTS),
					new PatternFilter(".*:.*")));
			try (XMLPrettyPrinter out = new XMLPrettyPrinter(stream, config)) {
				for (Node child : DOMUtil.children(node)) {
					out.write(child);
				}
			}
			xml = new String(stream.toByteArray(), "utf-8");
			return xml;
		} catch (IOException ex) {
			throw new UnreachableAssertion("Writing to buffer must not fail.", ex);
		}
	}

	public static void main(String[] args) throws Exception {
		new JSPRewrite().runMain(args);
	}

}
