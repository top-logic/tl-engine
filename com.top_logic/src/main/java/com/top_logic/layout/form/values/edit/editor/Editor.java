/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.editor;

import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.ValueModel;

/**
 * Factory for {@link FormMember}s based on {@link ValueModel}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Editor {

	/**
	 * {@link Property} to annotate a display template for the created member.
	 * 
	 * <p>
	 * When no {@link HTMLTemplateFragment} is annotated, some default heuristic is used to
	 * determine the display template.
	 * </p>
	 * 
	 * @see #createUI(EditorFactory, FormContainer, ValueModel)
	 */
	Property<HTMLTemplateFragment> MEMBER_TEMPLATE = TypedAnnotatable.property(HTMLTemplateFragment.class, "template");

	/**
	 * {@link Property} to annotate a field which is used to add
	 * {@link com.top_logic.layout.form.Constraint form constraints} derived from
	 * {@link com.top_logic.basic.config.constraint.annotation.Constraint config constraints}.
	 * 
	 * <p>
	 * When no field is annotated, the used {@link FormField} is the one created by
	 * {@link #createUI(EditorFactory, FormContainer, ValueModel)} or none, if the member is not a
	 * field.
	 * </p>
	 * 
	 * @see #createUI(EditorFactory, FormContainer, ValueModel)
	 */
	Property<FormField> CONSTRAINT_FIELD = TypedAnnotatable.property(FormField.class, "constraint field");

	/**
	 * Creates the edit UI for the given {@link ValueModel}.
	 * 
	 * @param editorFactory
	 *        The {@link EditorFactory} responsible for binding the whole form.
	 * @param container
	 *        The container to add the edit UI to.
	 * @param model
	 *        The value model the editor should edit.
	 * 
	 * @return The generated form member.
	 */
	public FormMember createUI(EditorFactory editorFactory, FormContainer container, ValueModel model);

}
