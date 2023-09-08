/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form;

import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.element.meta.AttributeSettings;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.form.tag.DisplayProvider;
import com.top_logic.element.meta.form.tag.TagProviderAnnotation;
import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.form.template.FormTemplateConstants;

/**
 * {@link ControlProvider} based on tag configuration in {@link AttributeSettings}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class MetaControlProvider implements ControlProvider {

	/**
	 * Singleton {@link MetaControlProvider} instance.
	 */
	public static final MetaControlProvider INSTANCE = new MetaControlProvider();

	private MetaControlProvider() {
		// Singleton constructor.
	}

	@Override
	public Control createControl(Object model, String style) {
		if (style == null || style.equals(FormTemplateConstants.STYLE_DIRECT_VALUE)) {
			FormMember member = (FormMember) model;
			AttributeUpdate update = DefaultAttributeFormFactory.getAttributeUpdate(member);
			if (update != null) {
				TagProviderAnnotation annotation =
					update.getAttribute().getAnnotation(TagProviderAnnotation.class);
				if (annotation != null) {
					DisplayProvider tagProvider = TypedConfigUtil.createInstance(annotation.getImpl());
					Control control = tagProvider.createDisplay(update, member);
					return control;
				}
			}
		}
		return DefaultFormFieldControlProvider.INSTANCE.createControl(model, style);
	}
}
