/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.doc.misc;

import java.util.Locale;

import com.top_logic.doc.component.DocumentationTreeComponent;
import com.top_logic.doc.model.Page;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.layout.LabelProvider;
import com.top_logic.util.TLContext;

/**
 * Utility class for TL-Doc.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TLDocUtil {

	/** Separator that is used to separate a path in the {@link DocumentationTreeComponent}. */
	public static final String PAGE_SEPARATOR = "/";

	/**
	 * Creates a path for the given {@link Page} in the {@link Locale} of the user.
	 * 
	 * @see #pagePath(Page, Locale)
	 */
	public static String pagePath(Page page) {
		return pagePath(page, TLContext.getLocale());
	}

	/**
	 * Creates a String representing the path in the {@link DocumentationTreeComponent} from the
	 * root node to the given page, separated by {@link #PAGE_SEPARATOR}.
	 * 
	 * <p>
	 * <b>Note:</b> The root node itself s not contained in the path.
	 * </p>
	 * 
	 * @param page
	 *        The page to create path in the tree for.
	 * @param language
	 *        The language to create a string representation of the elements in the path.
	 */
	public static String pagePath(Page page, Locale language) {
		return pagePath(page, new PageResourceProvider(language));
	}

	/**
	 * Creates a String representing the path in the {@link DocumentationTreeComponent} from the
	 * root node to the given page, separated by {@link #PAGE_SEPARATOR}.
	 * 
	 * <p>
	 * <b>Note:</b> The root node itself s not contained in the path.
	 * </p>
	 * 
	 * @param page
	 *        The page to create path in the tree for.
	 * @param labels
	 *        {@link LabelProvider} to get representations of the elements in the path.
	 */
	public static String pagePath(Page page, LabelProvider labels) {
		StructuredElement rootNode = page.getRoot();
		StringBuilder path = new StringBuilder();
		appendPath(path, rootNode, page, labels);
		return path.toString();
	}

	private static void appendPath(StringBuilder path, Object rootNode, Page page, LabelProvider labels) {
		if (page == rootNode) {
			return;
		}
		appendPath(path, rootNode, page.getParent(), labels);
		if (path.length() > 0) {
			path.append(PAGE_SEPARATOR);
		}
		path.append(labels.getLabel(page));

	}

}
