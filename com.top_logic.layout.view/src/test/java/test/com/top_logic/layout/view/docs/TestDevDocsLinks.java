/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.docs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import com.top_logic.layout.react.control.html.ReactHtmlControl;
import com.top_logic.layout.view.docs.DevDoc;
import com.top_logic.layout.view.docs.DevDocs;

/**
 * Checks the links of the developer documentation on the class path: every link to another
 * article names an existing article, every section a link names is a heading of its target, and no
 * link points to a file outside the documentation, which the application does not have.
 */
public class TestDevDocsLinks extends TestCase {

	private static final Pattern DOC_LINK = Pattern.compile("\\]\\(" + Pattern.quote(DevDocs.LINK_SCHEME) + "([^)\\s]*)\\)");

	private static final Pattern RENDERED_LINK =
		Pattern.compile("<a href=\"#([^\"]*)\"(?: " + Pattern.quote(ReactHtmlControl.LINK_ATTRIBUTE) + "=\"([^\"#]*)[^\"]*\")?");

	private static final Pattern ID = Pattern.compile(" id=\"([^\"]*)\"");

	/** The target of a Markdown link (outside code). */
	private static final Pattern LINK_TARGET = Pattern.compile("\\]\\(([^)\\s]*)\\)");

	/** The link targets that are not files: an article, a section, a page outside the application. */
	private static final Pattern ABSOLUTE_TARGET = Pattern.compile("(doc:|#|https?://|mailto:).*");

	/**
	 * Resolves all links of all articles.
	 */
	public void testLinks() {
		DevDoc root = DevDocs.load();
		Map<String, String> html = new HashMap<>();
		List<DevDoc> docs = new ArrayList<>();
		collect(root, docs);
		assertFalse("No documentation found on the class path.", docs.isEmpty());
		for (DevDoc doc : docs) {
			html.put(doc.getName(), DevDocs.toHtml(doc.getText(), root));
		}

		List<String> problems = new ArrayList<>();
		for (DevDoc doc : docs) {
			Matcher fileLink = LINK_TARGET.matcher(withoutCode(doc.getText()));
			while (fileLink.find()) {
				if (!ABSOLUTE_TARGET.matcher(fileLink.group(1)).matches()) {
					problems.add(doc.getName() + ": link to a file outside the documentation " + fileLink.group(1));
				}
			}

			Matcher source = DOC_LINK.matcher(doc.getText());
			while (source.find()) {
				if (DevDocs.find(root, source.group(1)) == null) {
					problems.add(doc.getName() + ": no article " + source.group(1));
				}
			}

			Matcher link = RENDERED_LINK.matcher(html.get(doc.getName()));
			while (link.find()) {
				String section = link.group(1);
				if (section.isEmpty()) {
					continue;
				}
				String target = link.group(2) == null ? doc.getName() : link.group(2);
				String targetHtml = html.get(target);
				if (targetHtml == null) {
					problems.add(doc.getName() + ": no text in " + target + " for section " + section);
				} else if (!ids(targetHtml).contains(section)) {
					problems.add(doc.getName() + ": no section " + section + " in " + target);
				}
			}
		}
		assertEquals("Broken links of the developer documentation.", List.of(), problems);
	}

	private static void collect(DevDoc node, List<DevDoc> result) {
		if (node.getText() != null) {
			result.add(node);
		}
		for (DevDoc child : node.getChildren()) {
			collect(child, result);
		}
	}

	/** The given Markdown source without code blocks and code spans, which show links as text. */
	private static String withoutCode(String markdown) {
		return markdown.replaceAll("(?s)```.*?```", "").replaceAll("`[^`\\n]*`", "");
	}

	private static List<String> ids(String html) {
		List<String> result = new ArrayList<>();
		Matcher matcher = ID.matcher(html);
		while (matcher.find()) {
			result.add(matcher.group(1));
		}
		return result;
	}

}
