/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security.layout;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.ResPrefix;
import com.top_logic.util.Resources;

/**
 * What a tool does, in one sentence written for an administrator.
 *
 * <p>
 * A different text from the one the tool carries for the language model: that one is written in English
 * for a caller who has to get the arguments right, and it says how to page through a result rather than
 * what granting the tool means. This one says what an administrator is deciding about, in their
 * language.
 * </p>
 *
 * <p>
 * Every tool the framework ships has one. A tool of a connected server, or one authored in the running
 * application, has nobody to write it, so those fall back to the description the model is given - marked
 * as such, per row, because it is the exception.
 * </p>
 */
/*
 * Proposal G. The key is derived from the qualified tool name, so a tool needs no annotation and the tool
 * contract stays untouched: describing a tool is a resource, exactly as translating a group label is.
 *
 * The texts live in the hand-written tl-ai-core.app-messages bundle rather than in the doclet-generated
 * one, because there is no Java declaration they could hang off - the doclet exports JavaDoc of types and
 * of I18NConstants fields, and eighty-odd pseudo-constants would be a declaration written for the
 * generator rather than for a reader. SecDemoToolDescriptionTest is what keeps the bundle complete: a
 * newly registered framework tool fails the build until somebody describes it in both languages.
 */
public class AdminToolDescriptions {

	/**
	 * The resource namespace of the administrator-facing tool descriptions.
	 *
	 * <p>
	 * A key is this prefix plus the qualified tool name, e.g.
	 * {@code admin.ai.tool.model-browser.listModules}.
	 * </p>
	 */
	public static final String PREFIX = "admin.ai.tool";

	/*
	 * A string namespace rather than one derived from a class, because these keys are administration
	 * resources of the same "admin.ai." family the views' own keys are in, and they are maintained in the
	 * same file. The class-derived form would name this class in every one of eighty-odd keys, which says
	 * nothing about the tool the key describes.
	 */
	@SuppressWarnings("deprecation")
	private static final ResPrefix NAMESPACE = ResPrefix.legacyString(PREFIX);

	private AdminToolDescriptions() {
		// Static utility.
	}

	/**
	 * The resource carrying the description of the named tool.
	 *
	 * @param qualifiedName
	 *        The name the policy knows the tool by, {@code {factory}.{tool}}.
	 * @return The resource, whether or not anybody wrote it.
	 */
	public static ResKey key(String qualifiedName) {
		return NAMESPACE.key(qualifiedName);
	}

	/** The plain resource key string, for a test that has to look it up per language. */
	public static String keyString(String qualifiedName) {
		return PREFIX + "." + qualifiedName;
	}

	/**
	 * Whether the named tool has an administrator-facing description.
	 *
	 * @param qualifiedName
	 *        The name the policy knows the tool by.
	 */
	public static boolean isDescribed(String qualifiedName) {
		return qualifiedName != null && NAMESPACE.hasStringResource(qualifiedName);
	}

	/**
	 * The administrator-facing description of the named tool.
	 *
	 * @param qualifiedName
	 *        The name the policy knows the tool by.
	 * @return The sentence, or <code>null</code> if nobody wrote one - which the caller has to render as
	 *         the model's own text, marked as such, rather than as an empty cell.
	 */
	public static String description(String qualifiedName) {
		return isDescribed(qualifiedName) ? Resources.getInstance().getString(key(qualifiedName)) : null;
	}

}
