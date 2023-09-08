/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tools.tempates;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import com.top_logic.layout.processor.LayoutModelConstants;

/**
 * Rewriter for variable references.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class Rewriter {

	public static Rewriter createVariableRewriter(Collection<String> names, String prefix, String postfix) {
		return new Rewriter(names, prefix, postfix, false, null);
	}

	public static Rewriter createParamRewriter(Collection<String> names, String prefix, String postfix, String includeId) {
		return new Rewriter(names, prefix, postfix, true, includeId);
	}

	private final Pattern _pattern;

	private final boolean _forParam;

	private final String _includeId;

	private Rewriter(Collection<String> names, String prefix, String postfix, boolean forParam, String includeId) {
		_forParam = forParam;
		_includeId = includeId;
		_pattern = createParameterReferencePattern(names, prefix, postfix);
	}

	private Pattern createParameterReferencePattern(Collection<String> names, String prefix, String postfix) {
		StringBuilder patternSrc = new StringBuilder();
		patternSrc.append(Pattern.quote(prefix));
		patternSrc.append("(");
		boolean first = true;
		for (String name : names) {
			if (first) {
				first = false;
			} else {
				patternSrc.append("|");
			}
			patternSrc.append("(?:");
			patternSrc.append(Pattern.quote(name));
			patternSrc.append(")");
		}
		patternSrc.append(")");
		patternSrc.append(Pattern.quote(postfix));

		Pattern pattern = Pattern.compile(patternSrc.toString());
		return pattern;
	}

	public void rewrite(Node child) {
		if (child instanceof Element) {
			rewriteElement((Element) child);
		}
		else if (child instanceof Text) {
			rewriteText((Text) child);
		}
	}

	public String rewriteString(String value) {
		Matcher matcher = _pattern.matcher(value);
	
		StringBuilder result = null;
		int pos = 0;
		while (matcher.find(pos)) {
			if (result == null) {
				result = new StringBuilder();
			}
			result.append(value.substring(pos, matcher.start()));
			result.append(_forParam ? LayoutModelConstants.PARAMETER_PREFIX : LayoutModelConstants.VARIABLE_PREFIX);
			if (_includeId != null) {
				result.append(_includeId);
				result.append(":");
			}
			result.append(UpgradeTemplateSyntax.getTemplateParamName(matcher.group(1)));
			result.append(_forParam ? LayoutModelConstants.PARAMETER_SUFFIX : LayoutModelConstants.VARIABLE_SUFFIX);
			pos = matcher.end();
		}
		if (result == null) {
			return value;
		}
		result.append(value.substring(pos));
		return result.toString();
	}

	public void rewriteElement(Element content) {
		NamedNodeMap attributes = content.getAttributes();
		for (int n = 0, cnt = attributes.getLength(); n < cnt; n++) {
			rewriteAttribute((Attr) attributes.item(n));
		}
		rewriteContent(content);
	}

	public void rewriteContent(Node content) {
		Node next;
		for (Node child = content.getFirstChild(); child != null; child = next) {
			next = child.getNextSibling();

			rewrite(child);
		}
	}

	public void rewriteText(Text text) {
		text.setTextContent(rewriteString(text.getTextContent()));
	}

	public void rewriteAttribute(Attr attr) {
		attr.setNodeValue(rewriteString(attr.getNodeValue()));
	}

}
