/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref;

import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;


/**
 * {@link ModelNamingScheme} that identifies a singleton by its class
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SingletonNaming extends AbstractModelNamingScheme<Object, SingletonNaming.Name> {

	/**
	 * {@link ModelName} of the {@link SingletonNaming}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Name extends ModelName {

		/** Property name of {@link #getSingletonClass()}. */
		String SINGLETON_CLASS_PROPERTY_NAME = "singleton-class";

		/**
		 * The class of the represented singleton.
		 */
		@Mandatory
		@com.top_logic.basic.config.annotation.Name(SINGLETON_CLASS_PROPERTY_NAME)
		Class<?> getSingletonClass();

		/**
		 * Setter of {@link #getSingletonClass()}.
		 */
		void setSingletonClass(Class<?> singletonClass);
	}

	@Override
	public Class<Name> getNameClass() {
		return Name.class;
	}

	@Override
	public Class<Object> getModelClass() {
		return Object.class;
	}

	@Override
	public Object locateModel(ActionContext context, Name name) {
		Class<?> singletonClass = name.getSingletonClass();
		try {
			return ConfigUtil.getSingletonMandatory(Name.SINGLETON_CLASS_PROPERTY_NAME, singletonClass);
		} catch (ConfigurationException ex) {
			throw ApplicationAssertions.fail(name, "Unable to get singleton from class " + singletonClass.getName());
		}
	}

	@Override
	protected void initName(Name name, Object model) {
		name.setSingletonClass(model.getClass());
	}

	@Override
	protected boolean isCompatibleModel(Object model) {
		Class<? extends Object> modelClass = model.getClass();
		try {
			Object singleton = ConfigUtil.getSingleton(modelClass);
			return singleton == model;
		} catch (ConfigurationException ex) {
			return false;
		}
	}
}

