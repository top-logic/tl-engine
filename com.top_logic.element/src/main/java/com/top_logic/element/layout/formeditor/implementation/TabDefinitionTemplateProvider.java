/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.formeditor.implementation;

import static com.top_logic.layout.form.template.model.Templates.*;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.layout.formeditor.definition.TabbarDefinition.TabDefinition;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.ImageProvider;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.form.FormEditorCSS;
import com.top_logic.model.form.ReactiveFormCSS;
import com.top_logic.model.form.implementation.AbstractFormContainerProvider;
import com.top_logic.model.form.implementation.ColumnsDefinitionTemplateProvider;
import com.top_logic.model.form.implementation.FormEditorContext;
import com.top_logic.model.form.implementation.FormElementTemplateProvider;

/**
 * {@link FormElementTemplateProvider} for a {@link TabDefinition}.
 * 
 * <p>
 * Note this template provider is exclusively used as part of the template generation of a
 * {@link TabbarDefinitionTemplateProvider}.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TabDefinitionTemplateProvider extends AbstractFormContainerProvider<TabDefinition> {

	/**
	 * Creates a {@link TabDefinitionTemplateProvider} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TabDefinitionTemplateProvider(InstantiationContext context, TabDefinition config) {
		super(context, config);
	}

	@Override
	public boolean getWholeLine(TLStructuredType modelType) {
		return true;
	}

	@Override
	public boolean getIsTool() {
		return false;
	}

	@Override
	public ImageProvider getImageProvider() {
		return (any, flavor) -> null;
	}

	@Override
	public ResKey getLabel(FormEditorContext context) {
		return I18NConstants.TAB_LABEL;
	}

	@Override
	public HTMLTemplateFragment decorateContainer(HTMLTemplateFragment content, FormEditorContext context) {
		HTMLTemplateFragment container = div(getIdAttribute(),
			css(ReactiveFormCSS.RF_CONTAINER + " " + FormEditorCSS.FE_DROP_TARGET), content);
		return contentBox(container);
	}

	@Override
	public void addCssClassForContent(List<HTMLTemplateFragment> buffer) {
		buffer.add(css(getCssClass()));
	}

	String getCssClass() {
		return ColumnsDefinitionTemplateProvider.cssClassForColumnsLayout(getConfig());
	}

}
