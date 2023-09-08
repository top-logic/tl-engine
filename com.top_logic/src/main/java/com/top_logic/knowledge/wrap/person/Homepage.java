/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.person;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.mig.html.layout.QualifiedComponentNameConstraint;

/**
 * Definition of the homepage of the user.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface Homepage extends ConfigurationItem {

	/** @see Homepage#getComponentName() */
	String COMPONENT_NAME_NAME = "component";

	/** @see #getMainLayout() */
	String MAINLAYOUT_NAME = "main-layout";

	/**
	 * The location of the {@link MainLayout} to which this homepage belongs to.
	 */
	@Name(MAINLAYOUT_NAME)
	String getMainLayout();

	/**
	 * Setter for{@link #getMainLayout()}.
	 */
	void setMainLayout(String mainlayout);

	/**
	 * The name of the component that was set as homepage.
	 */
	@Name(COMPONENT_NAME_NAME)
	@Constraint(QualifiedComponentNameConstraint.class)
	ComponentName getComponentName();

	/**
	 * Setter for {@link #getComponentName()}.
	 * 
	 * @param componentName
	 *        New value of {@link #getComponentName()}.
	 */
	void setComponentName(ComponentName componentName);

	/**
	 * The model to set to the homepage component.
	 */
	ModelName getModel();

	/**
	 * Setter for {@link #getModel()}.
	 * 
	 * @param model
	 *        New value of {@link #getModel()}.
	 */
	void setModel(ModelName model);

}
