/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.form.implementation;

import static com.top_logic.layout.form.template.model.Templates.*;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.model.form.definition.ContainerDefinition;
import com.top_logic.model.form.definition.LabelPlacement;

/**
 * {@link FormElementTemplateProvider} which contains other {@link FormElementTemplateProvider}s within.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public abstract class AbstractFormContainerProvider<T extends ContainerDefinition<?>>
		extends AbstractFormElementProvider<T> {

	private List<FormElementTemplateProvider> _content;

	/**
	 * Create a new {@link AbstractFormContainerProvider} for a given {@link ContainerDefinition} in
	 * a given {@link InstantiationContext}. Holds the content of the container.
	 * 
	 * @see ContainerDefinition#getContent()
	 */
	public AbstractFormContainerProvider(InstantiationContext context, T config) {
		super(context, config);
		_content = TypedConfiguration.getInstanceList(context, config.getContent());
	}

	@Override
	protected HTMLTemplateFragment createDisplayTemplate(FormEditorContext context) {
		return decorateContainer(createContentTemplate(context), context);
	}

	/**
	 * Decorates the given {@link HTMLTemplateFragment} with visible properties for the concrete
	 * container type.
	 * 
	 * @param content
	 *        Elements inside the container.
	 * @param context
	 *        The {@link FormEditorContext} to create the template.
	 * 
	 * @return The created {@link HTMLTemplateFragment}.
	 */
	public abstract HTMLTemplateFragment decorateContainer(HTMLTemplateFragment content, FormEditorContext context);

	/**
	 * Create a {@link HTMLTemplateFragment} for the content of a
	 * {@link FormElementTemplateProvider}.
	 * 
	 * @param context
	 *        The {@link FormEditorContext} to create the template.
	 * 
	 * @see #addCssClassForContent(List)
	 * 
	 * @return The created {@link HTMLTemplateFragment}.
	 */
	public HTMLTemplateFragment createContentTemplate(FormEditorContext context) {
		List<HTMLTemplateFragment> formFieldTemplates = new ArrayList<>();

		addCssClassForContent(formFieldTemplates);

		LabelPlacement labelPlacement = getConfig().getLabelPlacement();
		for (FormElementTemplateProvider content : getContent()) {
			if (content.isVisible(context)) {
				HTMLTemplateFragment innerTemplate =
					context.withLabelPlacement(labelPlacement, content::createTemplate);
				formFieldTemplates.add(innerTemplate);
			}
		}

		return div(formFieldTemplates);
	}
	
	/**
	 * Adds the attribute tempates of the container element to the given list.
	 * 
	 * <p>
	 * The container element is rendered directly around the {@link FormElementTemplateProvider}s of
	 * the content.
	 * </p>
	 * 
	 * @param buffer
	 *        Buffer of {@link HTMLTemplateFragment}s to add attribute {@link HTMLTemplateFragment}s
	 *        to.
	 */
	public void addCssClassForContent(List<HTMLTemplateFragment> buffer) {
		// do nothing here, but needed for subclasses
	}

	/**
	 * Returns the content of the configuration which is a {@link ContainerDefinition}.
	 * 
	 * @return The content as list.
	 */
	public List<FormElementTemplateProvider> getContent() {
		return _content;
	}

}