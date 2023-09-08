/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles;

import java.util.Map;

import com.top_logic.basic.config.LabeledConfiguration;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.MapBinding;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.messagebox.DialogFormBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.tiles.component.ComponentParameters;
import com.top_logic.mig.html.layout.tiles.component.SimpleFormBuilder;

/**
 * Definition of the builder for creating a new {@link LayoutComponent}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ComponentBuilder extends LabeledConfiguration {

	/** Name of the configuration parameter to fill the value of {@link #getTemplate()}. */
	String TEMPLATE_NAME = "template";

	/** Name of the configuration parameter to fill the value of {@link #getDialogWidth()}. */
	String DIALOG_WIDTH_NAME = "dialogWidth";

	/** Name of the configuration parameter to fill the value of {@link #getDialogHeight()}. */
	String DIALOG_HEIGHT_NAME = "dialogHeight";

	/** Name of the configuration parameter to fill the value of {@link #getFormBuilder()}. */
	String FORM_BUILDER_NAME = "formBuilder";

	/** Name of the configuration parameter to fill the value of {@link #getParameters()}. */
	String PARAMETERS_NAME = "parameters";

	/** Name of the configuration parameter to fill the value of {@link #getArguments()}. */
	String ARGUMENTS_NAME = "arguments";

	/**
	 * The referenced layout file containing the template of the {@link LayoutComponent} to create.
	 * 
	 * <p>
	 * The parameters that are used in the referenced template are defined by
	 * {@link #getParameters()}.
	 * </p>
	 */
	@Mandatory
	@Name(TEMPLATE_NAME)
	String getTemplate();

	/**
	 * The type of {@link ComponentParameters} defining the parameter that are used to fill the
	 * referenced {@link #getTemplate() template}.
	 */
	@Mandatory
	@Name(PARAMETERS_NAME)
	Class<? extends ComponentParameters> getParameters();

	/**
	 * Optional arguments the are stored into the created {@link ComponentParameters}.
	 * 
	 * <p>
	 * The arguments typically contains informations that are not given by the user but are derived
	 * from the layout configuration environment, e.g. the "master" of the component to create.
	 * </p>
	 * 
	 * <p>
	 * The configured keys must be the {@link PropertyDescriptor#getPropertyName() names} of the
	 * properties of the concrete {@link #getParameters() parameters type}, and the configured
	 * values valid configuration values for the corresponding keys.
	 * </p>
	 */
	@MapBinding
	@Name(ARGUMENTS_NAME)
	Map<String, String> getArguments();

	/**
	 * The {@link DialogFormBuilder} that creates the form to let the user given the necessary
	 * information that is needed {@link #getParameters() to create the component}.
	 */
	@ItemDefault(SimpleFormBuilder.class)
	@Name(FORM_BUILDER_NAME)
	PolymorphicConfiguration<DialogFormBuilder<? super ComponentParameters>> getFormBuilder();

	/**
	 * The width of the configuration dialog for this {@link ComponentBuilder}.
	 */
	@FormattedDefault("400px")
	@Name(DIALOG_WIDTH_NAME)
	DisplayDimension getDialogWidth();

	/**
	 * The height of the configuration dialog for this {@link ComponentBuilder}.
	 */
	@FormattedDefault("250px")
	@Name(DIALOG_HEIGHT_NAME)
	DisplayDimension getDialogHeight();

}

