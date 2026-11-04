/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.TypeIcon;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.view.ContainerElement;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.form.FormModel;
import com.top_logic.layout.view.form.ObjectFieldsControl;

/**
 * {@link UIElement} displaying, within a {@link FormElement}, the fields of the object a
 * single-valued reference of the form's object points to.
 *
 * <p>
 * The content is what a form contains: {@link FieldElement}s, {@link GroupElement}s and
 * {@link FieldsElement}s, whose fields display attributes of the referenced object instead of the
 * form's object. The referenced object is edited in the edit mode of the form, and stored by the
 * form's save together with the form's object. The fields take part in the grid of the form like
 * the fields of a {@link GroupElement} without heading.
 * </p>
 *
 * <p>
 * Example: the form on a person contact edits the login data of its account (the backward
 * reference {@code account}) in the same edit mode:
 * </p>
 *
 * <pre>
 * &lt;form input="selectedContact"&gt;
 *     &lt;field attribute="firstname"/&gt;
 *     &lt;field attribute="name"/&gt;
 *     &lt;object-fields attribute="account"&gt;
 *         &lt;field attribute="name"/&gt;
 *         &lt;field attribute="locale"/&gt;
 *     &lt;/object-fields&gt;
 * &lt;/form&gt;
 * </pre>
 *
 * <p>
 * Without a referenced object, nothing is displayed. The element follows the display mode of the
 * reference, see {@link ObjectFieldsControl}.
 * </p>
 */
@InApp
public class ObjectFieldsElement extends ContainerElement {

	/**
	 * Configuration for {@link ObjectFieldsElement}.
	 */
	@TagName("object-fields")
	@TypeIcon("css:bi bi-box-arrow-in-down-right")
	public interface Config extends ContainerElement.Config {

		/** Configuration name for {@link #getAttribute()}. */
		String ATTRIBUTE = "attribute";

		/** Configuration name for {@link #getReadonly()}. */
		String READONLY = "readonly";

		@Override
		@ClassDefault(ObjectFieldsElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * The name of the single-valued reference of the form's object pointing to the object whose
		 * fields are displayed.
		 *
		 * <p>
		 * Forward and backward references are supported.
		 * </p>
		 */
		@Name(ATTRIBUTE)
		@Mandatory
		String getAttribute();

		/**
		 * Whether the referenced object is only displayed, also while the form is edited.
		 */
		@Name(READONLY)
		boolean getReadonly();
	}

	private final Config _config;

	/**
	 * Creates a new {@link ObjectFieldsElement} from configuration.
	 */
	@CalledByReflection
	public ObjectFieldsElement(InstantiationContext context, Config config) {
		super(context, config);
		_config = config;
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		FormModel formModel = context.getFormModel();
		if (formModel == null) {
			throw new IllegalStateException(
				"ObjectFieldsElement must be nested inside a FormElement. No FormModel found in ViewContext.");
		}

		ObjectFieldsControl control = new ObjectFieldsControl(context, formModel, _config.getAttribute(),
			_config.getReadonly(), context.getModelScope());
		control.setCssClass(_config.getCssClass());

		ViewContext nestedContext = context.childContext("object-fields");
		nestedContext.setFormModel(control);
		List<ReactControl> children = createChildControls(nestedContext).stream()
			.map(child -> (ReactControl) child)
			.collect(Collectors.toList());
		control.setContent(children);
		return control;
	}
}
