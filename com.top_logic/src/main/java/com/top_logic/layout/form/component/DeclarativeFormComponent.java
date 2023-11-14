/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.component;

import static com.top_logic.basic.config.TypedConfiguration.*;
import static com.top_logic.layout.basic.fragments.Fragments.*;
import static com.top_logic.layout.form.values.Fields.*;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.layout.Control;
import com.top_logic.layout.basic.FragmentControl;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.declarative.DeclarativeFormBuilder;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.initializer.Initializer;
import com.top_logic.layout.form.values.edit.initializer.InitializerIndex;
import com.top_logic.layout.form.values.edit.initializer.InitializerProvider;
import com.top_logic.layout.structure.ControlRepresentable;
import com.top_logic.layout.structure.ControlRepresentableCP;
import com.top_logic.layout.structure.LayoutControlProvider;

/**
 * A {@link FormComponent} that uses declarative form definitions.
 * 
 * @deprecated Use {@link DeclarativeFormBuilder} for creating the {@link FormContext}.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
@Deprecated
public class DeclarativeFormComponent extends FormComponent implements ControlRepresentable {

	/**
	 * Configuration interface for {@link DeclarativeFormComponent}.
	 */
	public interface Config extends FormComponent.Config {

		/**
		 * @see #getFormType()
		 */
		String FORM_TYPE = "formType";

		/**
		 * @see #getFormBodyClass()
		 */
		String FORM_BODY_CLASS = "formBodyClass";

		/**
		 * @see #getControlProvider()
		 */
		String CONTROL_PROVIDER = "controlProvider";

		/** Property name of {@link #getCompactLayout()}. */
		String COMPACT_LAYOUT = "compactLayout";

		/**
		 * The CSS class to use in the outermost <code>div</code> element.
		 */
		@Name(FORM_BODY_CLASS)
		@StringDefault(FormConstants.FORM_BODY_CSS_CLASS)
		String getFormBodyClass();

		@Override
		@ItemDefault(ControlRepresentableCP.Config.class)
		PolymorphicConfiguration<LayoutControlProvider> getComponentControlProvider();

		/**
		 * {@link ControlProvider} to use to render primitive fields.
		 */
		@Name(CONTROL_PROVIDER)
		@InstanceFormat
		@InstanceDefault(DefaultFormFieldControlProvider.class)
		@NonNullable
		ControlProvider getControlProvider();

		/**
		 * {@link ConfigurationItem} class declaring the properties of the form.
		 */
		@Name(FORM_TYPE)
		@Mandatory
		Class<? extends ConfigurationItem> getFormType();

		/**
		 * @see EditorFactory#isCompact()
		 */
		@Name(COMPACT_LAYOUT)
		boolean getCompactLayout();

	}

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link DeclarativeFormComponent}.
	 * <p>
	 * <b>Don't call directly.</b> Use
	 * {@link InstantiationContext#getInstance(PolymorphicConfiguration)} instead.
	 * </p>
	 * 
	 * @param context
	 *        For error reporting and instantiation of dependent configured objects.
	 * @param config
	 *        The configuration for the new instance.
	 */
	@CalledByReflection
	public DeclarativeFormComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

	@Override
	public FormContext createFormContext() {
		FormContext formContext = formContext(this);
		ConfigurationItem formModel = createFormModel();
		InitializerProvider initializerProvider = createInitializerProvider();
		boolean compactLayout = getConfig().getCompactLayout();
		EditorFactory.initEditorGroup(formContext, formModel, initializerProvider, compactLayout);
		return formContext;
	}

	/**
	 * Create the {@link ConfigurationItem} that describes this form.
	 * <p>
	 * Must not try to access the {@link #getFormContext()}, as this method is called during
	 * {@link #createFormContext()}.
	 * </p>
	 * 
	 * @return Never null.
	 */
	protected ConfigurationItem createFormModel() {
		return newConfigItem(getConfig().getFormType());
	}

	/**
	 * The {@link ConfigurationItem} describing the {@link FormContext}.
	 * <p>
	 * Is not allowed to be called when the {@link FormContext} is not yet created and accessible
	 * via {@link #getFormContext()}.
	 * </p>
	 * 
	 * @return Never null.
	 */
	public ConfigurationItem getFormModel() {
		return (ConfigurationItem) EditorFactory.getModel(getFormContext());
	}

	/**
	 * Create the collection of {@link Initializer}s that react on property updates.
	 * 
	 * <p>
	 * Implementations must not access the {@link #getFormContext()}, as this method is called from
	 * within {@link #createFormContext()}.
	 * </p>
	 * 
	 * @return The initializers to apply.
	 * 
	 * @see #buildInitializerIndex(InitializerIndex)
	 */
	protected InitializerProvider createInitializerProvider() {
		InitializerIndex result = new InitializerIndex();
		buildInitializerIndex(result);
		return result;
	}

	/**
	 * Allows to add own dynamic initializers.
	 * 
	 * @param index
	 *        The index of all initializers to apply.
	 * 
	 * @see #createInitializerProvider()
	 */
	protected void buildInitializerIndex(InitializerIndex index) {
		// Hook for sub-classes.
	}

	@Override
	public Control getRenderingControl() {
		return new FragmentControl(
			div(getConfig().getFormBodyClass(),
				getControlProvider().createFragment(getFormMemberToDisplay())));
	}

	/**
	 * Hook for subclasses that don't want to render the whole {@link #getFormContext()}.
	 * 
	 * @return Is not allowed to be null.
	 */
	protected FormMember getFormMemberToDisplay() {
		return getFormContext();
	}

	/**
	 * {@link ControlProvider} to display {@link #getFormMemberToDisplay()}.
	 */
	protected ControlProvider getControlProvider() {
		return getConfig().getControlProvider();
	}

}
