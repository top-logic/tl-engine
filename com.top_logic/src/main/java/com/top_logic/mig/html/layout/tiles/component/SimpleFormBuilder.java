/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.component;

import java.util.function.Consumer;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.customization.AnnotationCustomizations;
import com.top_logic.basic.config.customization.NoCustomizations;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.initializer.InitializerIndex;
import com.top_logic.layout.form.values.edit.initializer.InitializerProvider;
import com.top_logic.layout.form.values.edit.initializer.InitializerUtil;
import com.top_logic.layout.messagebox.DialogFormBuilder;

/**
 * {@link DialogFormBuilder} that builds the {@link FormContext} from the given
 * {@link ConfigurationItem} by using the {@link EditorFactory}.
 * 
 * @see EditorFactory#initEditorGroup(FormContainer, ConfigurationItem, InitializerProvider)
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SimpleFormBuilder<T extends ConfigurationItem> implements DialogFormBuilder<T> {

	/** Singleton {@link SimpleFormBuilder} instance. */
	@SuppressWarnings("rawtypes")
	private static final SimpleFormBuilder DEFAULT_INSTANCE = new SimpleFormBuilder(NoCustomizations.INSTANCE, null);

	/**
	 * The default instance of {@link SimpleFormBuilder} without customizations.
	 * 
	 * <p>
	 * Note: This factory is used to resolve default values for configuration properties of type
	 * {@link DialogFormBuilder}.
	 * </p>
	 */
	@SuppressWarnings("unchecked")
	public static <T extends ConfigurationItem> SimpleFormBuilder<T> getInstance() {
		return SimpleFormBuilder.DEFAULT_INSTANCE;
	}

	private final AnnotationCustomizations _customizations;

	private final InitializerProvider _initializerProvider;

	/**
	 * The {@link InitializerProvider} for the created form.
	 * 
	 * <p>
	 * If the value is <code>null</code>, a new {@link InitializerIndex} is created while creating
	 * the form.
	 * </p>
	 * 
	 * @see EditorFactory#EditorFactory(InitializerProvider)
	 */
	public InitializerProvider getInitializerProvider() {
		return _initializerProvider;
	}

	/**
	 * Creates a {@link SimpleFormBuilder}.
	 *
	 * @param customizations
	 *        See {@link #getCustomizations()}.
	 * @param initializerProvider
	 *        See {@link #getInitializerProvider()}.
	 */
	public SimpleFormBuilder(AnnotationCustomizations customizations, InitializerProvider initializerProvider) {
		_customizations = customizations;
		_initializerProvider = initializerProvider;
	}

	/**
	 * The {@link AnnotationCustomizations} to use for building the form.
	 * 
	 * @see EditorFactory#setCustomizations(AnnotationCustomizations)
	 */
	public AnnotationCustomizations getCustomizations() {
		return _customizations;
	}

	@Override
	public Consumer<T> initForm(FormContainer form, T model) {
		EditorFactory editorFactory =
			new EditorFactory(_initializerProvider == null ? new InitializerIndex() : _initializerProvider);
		editorFactory.setCustomizations(_customizations);
		editorFactory.setCompact(false);
		InitializerUtil.initAll(editorFactory.getInitializer(), model);
		editorFactory.initEditorGroup(form, model);
		return parmeters -> {
			// Nothing to finish here
		};
	}
}

