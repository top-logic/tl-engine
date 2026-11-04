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
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.view.element.FieldElement;
import com.top_logic.layout.view.element.FieldsElement;
import com.top_logic.model.TLClass;
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

	/** Tag name of the root element of a view. */
	private static final String VIEW = "view";

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

	/**
	 * A view with an input field for each attribute a type declares itself.
	 *
	 * <p>
	 * The view consists of a <code>&lt;fields&gt;</code> grid with a <code>&lt;field&gt;</code> for
	 * each attribute the type declares, inherited attributes left out. It is the content of a form
	 * whose type is known only at runtime: embedded with a <code>&lt;dynamic-view&gt;</code> into a
	 * <code>&lt;form&gt;</code>, its fields edit the object of that form.
	 * </p>
	 *
	 * <pre>
	 * &lt;form input="draft"&gt;
	 *   &lt;dynamic-view inputs="draft" view="$draft == null ? null : viewFields($draft.type())"/&gt;
	 * &lt;/form&gt;
	 * </pre>
	 *
	 * @param type
	 *        The type whose attributes are edited.
	 * @return The view configuration.
	 */
	@Label("Fields of a type")
	@SideEffectFree
	public static ViewElement.Config fields(@Mandatory TLClass type) {
		TagWriter out = new TagWriter();
		out.beginTag(VIEW);
		out.beginTag(FieldsElement.Config.TAG_NAME);
		FieldElement.writeFields(out, type);
		out.endTag(FieldsElement.Config.TAG_NAME);
		out.endTag(VIEW);
		return parse(out.toString());
	}

}
