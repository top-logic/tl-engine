/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.gui.inspector.plugin.debuginfo;

import org.w3c.dom.Document;

import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.template.FormPatternConstants;
import com.top_logic.layout.form.template.FormTemplateConstants;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.GuiInspectorPlugin;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion.AssertionPlugin;
import com.top_logic.mig.html.HTMLConstants;

/**
 * A {@link GuiInspectorPlugin} that shows debug information.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class DebugInfoPlugin<M> extends GuiInspectorPlugin<M> {

	private static final String FIELD_DISPLAY = "display";

	private static final Document TEMPLATE = DOMUtil.parseThreadSafe(
			"<tbody"
		+ "		xmlns='" + HTMLConstants.XHTML_NS + "'"
		+ "		xmlns:t='" + FormTemplateConstants.TEMPLATE_NS + "'"
		+ "		xmlns:p='" + FormPatternConstants.PATTERN_NS + "'"
		+ "	>"
		+ "		<tr>"
		+ "			<td class='content'>"
		+ "			</td>"
		+ "			<td class='label'>"
		+ "				<p:field name='" + FIELD_DISPLAY + "' style='label' />"
		+ "			</td>"
		+ "			<td class='content'>"
		+ "				<p:field name='" + FIELD_DISPLAY + "' style='input' />"
		+ "			</td>"
		+ "		</tr>"
		+ "	</tbody>");

	private ResPrefix _resPrefix;

	/**
	 * @param resPrefix
	 *        The resource prefix to use for information provided by this {@link DebugInfoPlugin}.
	 * @param internalName
	 *        The internal name of the FormGroup, in which all UI elements of this
	 *        {@link AssertionPlugin} are contained.
	 */
	public DebugInfoPlugin(M model, ResPrefix resPrefix, String internalName) {
		super(model, internalName);
		_resPrefix = resPrefix;
	}

	@Override
	protected Document getFormTemplateDocument() {
		return TEMPLATE;
	}

	@Override
	protected final void initGuiElements(FormContainer group) {
		initInformationField(group);
	}

	/**
	 * Creates and fills the {@link FormField} displaying the information.
	 */
	private void initInformationField(FormContainer group) {
		group.addMember(createInformationField(FIELD_DISPLAY));
	}

	/**
	 * Fills the {@link FormField} displaying the information.
	 */
	protected abstract FormMember createInformationField(String name);

	@Override
	protected ResPrefix getI18nPrefix() {
		return _resPrefix;
	}

}
