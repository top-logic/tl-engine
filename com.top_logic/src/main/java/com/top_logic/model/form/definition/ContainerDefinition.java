/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.form.definition;

import java.util.List;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.model.form.implementation.FormEditorTemplateProvider;
import com.top_logic.model.form.implementation.FormElementTemplateProvider;

/**
 * A container with a list of {@link FormElement}s.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
@Abstract
public interface ContainerDefinition<T extends FormEditorTemplateProvider> extends ContainerProperties<T> {

	/** The name of the property {@link #getContent()}. */
	String CONTENT = "content";

	/**
	 * {@link FormElementTemplateProvider} that create the contents of this this container.
	 */
	@Hidden
	@Name(CONTENT)
	@DefaultContainer
	List<PolymorphicConfiguration<? extends FormElementTemplateProvider>> getContent();

	/**
	 * @see #getContent()
	 */
	void setContent(List<PolymorphicConfiguration<? extends FormElementTemplateProvider>> content);

}
