/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.fieldprovider.form;

import java.util.List;
import java.util.function.Supplier;

import com.top_logic.element.layout.formeditor.FormDefinitionTemplate;
import com.top_logic.model.TLObject;

/**
 * Algorithm resolving {@link FormDefinitionTemplate}s for defining forms for a given type.
 * 
 * @see TLFormTemplates
 */
public interface TemplateResolver {

	/**
	 * Computes a resolver for {@link FormDefinitionTemplate}s for a given context.
	 * 
	 * <p>
	 * The resulting {@link FormDefinitionTemplate}s are offered in a form editor to derive new form
	 * definitions from.
	 * </p>
	 */
	Supplier<List<FormDefinitionTemplate>> getTemplates(TLObject context);

}
