/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.person;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.mig.html.layout.QualifiedComponentNameConstraint;

/**
 * Definition of the homepage of the user.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface Homepage extends ConfigurationItem {

	/** Configuration name for {@link #getMainLayout()} */
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
	 * The definition of the components and the necessary models on the path to the homepage.
	 */
	@EntryTag("path")
	List<Path> getComponentPaths();

	/**
	 * A step on the path to the homepage.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Path extends ConfigurationItem {

		/** Configuration name for {@link #getComponent()} */
		String COMPONENT_NAME = "component";

		/**
		 * The name of the component that was set as homepage.
		 */
		@Name(COMPONENT_NAME)
		@Constraint(QualifiedComponentNameConstraint.class)
		@Mandatory
		ComponentName getComponent();

		/**
		 * Setter for {@link #getComponent()}.
		 * 
		 * @param component
		 *        New value of {@link #getComponent()}.
		 */
		void setComponent(ComponentName component);

		/**
		 * Mapping of channel name to channel value to set to {@link #getComponent()}.
		 */
		@Key(ChannelValue.NAME_ATTRIBUTE)
		Map<String, ChannelValue> getChannelValues();
	}

	/**
	 * Definition of a channel value of a {@link LayoutComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface ChannelValue extends NamedConfigMandatory {

		/**
		 * The value of the channel.
		 */
		@Mandatory
		ModelName getValue();

		/**
		 * Setter for {@link #getValue()}.
		 */
		void setValue(ModelName value);

	}

}
