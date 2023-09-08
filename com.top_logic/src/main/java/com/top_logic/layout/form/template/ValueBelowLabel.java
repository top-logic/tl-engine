/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template;

import static com.top_logic.layout.form.template.FormTemplateConstants.*;

import org.w3c.dom.Document;

import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.layout.form.FormField;
import com.top_logic.mig.html.HTMLConstants;

/**
 * {@link ControlProvider} that renders a {@link FormField} with label, input and error in two separate lines.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class ValueBelowLabel extends TemplateControlProvider {

	private static final Document TEMPLATE = DOMUtil.parseThreadSafe(
			"<div"
    	+		" xmlns='" + HTMLConstants.XHTML_NS + "'"
    	+  		" xmlns:t='" + FormTemplateConstants.TEMPLATE_NS + "'"
    	+		" xmlns:p='" + FormPatternConstants.PATTERN_NS + "'"
    	+	">"
        +		"<div class='label'>"
        +			"<p:self style='" + STYLE_LABEL_VALUE + "'/>"
        +		"</div>"
        +		"<div class='content'>"
        +			"<p:self style='" + STYLE_DIRECT_VALUE + "'/>"
        +			HTMLConstants.NBSP
        +			"<p:self style='" + STYLE_ERROR_VALUE + "' />"
        +		"</div>"
        +	"</div>")
        ;

	private ValueBelowLabel(ControlProvider inner) {
		super(TEMPLATE, inner);
	}

	/**
	 * Default {@link ValueBelowLabel} instance with {@link DefaultFormFieldControlProvider}.
	 */
	public static final ControlProvider INSTANCE =
		new ValueBelowLabel(DefaultFormFieldControlProvider.INSTANCE);

	/**
	 * {@link ValueBelowLabel} that uses the given {@link ControlProvider} for input, error and
	 * label display.
	 */
	public static ControlProvider getInstance(ControlProvider inner) {
		if (inner == DefaultFormFieldControlProvider.INSTANCE) {
			return INSTANCE;
		} else {
			return new ValueBelowLabel(inner);
		}
	}

}