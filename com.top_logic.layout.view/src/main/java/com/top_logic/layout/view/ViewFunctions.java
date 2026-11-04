/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.SideEffectFree;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;
import com.top_logic.util.error.TopLogicException;

/**
 * TL-Script functions about view configurations, the values an attribute of the datatype
 * <code>tl.view:ViewDefinition</code> holds and a <code>&lt;dynamic-view&gt;</code> shows.
 *
 * <p>
 * A script storing a view in such an attribute builds the configuration from its XML text:
 * </p>
 *
 * <pre>
 * $task.set(`my.app:Task#view`, viewParse('&lt;view&gt;&lt;form input="item"/&gt;&lt;/view&gt;'))
 * </pre>
 *
 * @implNote Every public static method here is a TL-Script function named by the
 *           {@link ScriptPrefix} of this class followed by the capitalized method name, so
 *           {@link #parse(String)} is called <code>viewParse</code>.
 */
@ScriptPrefix(ViewFunctions.PREFIX)
public class ViewFunctions extends TLScriptFunctions {

	/** Prefix all function names of this class start with. */
	public static final String PREFIX = "view";

	/** Name of the content a parsed text is reported under in an error message. */
	private static final String SOURCE_NAME = PREFIX + "Parse";

	/**
	 * Reads a view configuration from its XML text.
	 *
	 * <p>
	 * The text reads like a <code>.view.xml</code> file, with the root element
	 * <code>&lt;view&gt;</code>. The result is the value an attribute of the datatype
	 * <code>tl.view:ViewDefinition</code> stores.
	 * </p>
	 *
	 * @param xml
	 *        The XML text of the view.
	 * @return The view configuration.
	 * @throws TopLogicException
	 *         If the text is no valid view configuration.
	 */
	@Label("Parse a view configuration")
	@SideEffectFree
	public static ViewElement.Config parse(@Mandatory String xml) {
		try {
			return ViewLoader.parseConfig(List.of(CharacterContents.newContent(xml, SOURCE_NAME)));
		} catch (ConfigurationException ex) {
			throw new TopLogicException(I18NConstants.ERROR_NO_VIEW_DEFINITION__PROBLEM.fill(ex.getMessage()), ex);
		}
	}

}
