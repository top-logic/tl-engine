/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template.util;

import static com.top_logic.layout.form.template.model.Templates.*;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.html.template.TagTemplate;
import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.form.template.model.Embedd;
import com.top_logic.layout.form.template.model.internal.TemplateControl;
import com.top_logic.layout.form.values.edit.TemplateProvider;

/**
 * {@link TemplateProvider} embedding a {@link FormContainer} without a top-level tag.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class EmbeddedGroup extends TemplateProvider implements ControlProvider {

	private static final Embedd TEMPLATE = embedd(items(self()));

	private static final TagTemplate STANDALONE_TEMPLATE = div(TEMPLATE);

	/**
	 * Singleton {@link EmbeddedGroup} instance.
	 */
	public static final EmbeddedGroup INSTANCE = new EmbeddedGroup();

	private EmbeddedGroup() {
		// Singleton constructor.
	}

	@Override
	public HTMLTemplateFragment get(ConfigurationItem model) {
		return TEMPLATE;
	}

	@Override
	public Control createControl(Object model, String style) {
		return new TemplateControl((FormMember) model, DefaultFormFieldControlProvider.INSTANCE, STANDALONE_TEMPLATE);
	}

}
