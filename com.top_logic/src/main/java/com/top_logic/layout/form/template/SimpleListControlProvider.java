/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.layout.Control;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.mig.html.HTMLConstants;

/**
 * The {@link SimpleListControlProvider} can be used to render a {@link FormGroup} in the following style
 * <pre>
 * &lt;table&gt;
 *  &lt;tr&gt;
 *   &lt;td align='right'&gt;label_FormMember_1&lt;/td&gt;&lt;td align='left'&gt;FormMember_1&lt;/td&gt;
 *  &lt;/tr&gt;
 *  &lt;tr&gt;
 *   &lt;td align='right'&gt;label_FormMember_2&lt;/td&gt;&lt;td align='left'&gt;FormMember_2&lt;/td&gt;
 *  &lt;/tr&gt;
 *  ...
 *  &lt;tr&gt;
 *   &lt;td align='right'&gt;label_FormMember_n&lt;/td&gt;&lt;td align='left'&gt;FormMember_n&lt;/td&gt;
 *  &lt;/tr&gt;
 * &lt;/table&gt;
 * </pre>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SimpleListControlProvider extends DefaultFormFieldControlProvider {

	private final ResPrefix resourcePrefix;

	public SimpleListControlProvider(ResPrefix resPrefix) {
		super();
		this.resourcePrefix = resPrefix;
	}

	@Override
	public Control visitFormContainer(FormContainer member, Void arg) {
		return new FormListControl(member, this, createTemplate(), resourcePrefix);
	}

	public static Document createTemplate() {
		Document document = DOMUtil.newDocument();

		Element table = document.createElementNS(HTMLConstants.XHTML_NS, HTMLConstants.TABLE);
		document.appendChild(table);

		Element items =
			document.createElementNS(FormTemplateConstants.TEMPLATE_NS, FormTemplateConstants.ITEMS_LIST_ELEMENT);
		table.appendChild(items);

		Element tr = document.createElementNS(HTMLConstants.XHTML_NS, HTMLConstants.TR);
		items.appendChild(tr);

		Element td = document.createElementNS(HTMLConstants.XHTML_NS, HTMLConstants.TD);
		td.setAttributeNS(null, HTMLConstants.ALIGN_ATTR, "right");
		tr.appendChild(td);

		Element self =
			document.createElementNS(FormPatternConstants.PATTERN_NS, FormPatternConstants.SELF_PATTERN_ELEMENT);
		self.setAttributeNS(null, FormTemplateConstants.STYLE_TEMPLATE_ATTRIBUTE,
			FormTemplateConstants.STYLE_LABEL_VALUE);
		td.appendChild(self);

		td = document.createElementNS(HTMLConstants.XHTML_NS, HTMLConstants.TD);
		td.setAttributeNS(null, HTMLConstants.ALIGN_ATTR, "left");
		tr.appendChild(td);

		self = document.createElementNS(FormPatternConstants.PATTERN_NS, FormPatternConstants.SELF_PATTERN_ELEMENT);
		td.appendChild(self);

		return document;
	}

	protected ResPrefix getResPrefix() {
	    return this.resourcePrefix;
	}
}
