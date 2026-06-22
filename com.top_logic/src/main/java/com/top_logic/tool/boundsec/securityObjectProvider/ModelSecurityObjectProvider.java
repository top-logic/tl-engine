/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.securityObjectProvider;

import java.util.Collections;
import java.util.Set;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.model.TLClass;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.SecurityObjectProvider;

/**
 * {@link SecurityObjectProvider} that uses the current model as security object.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@InApp
@Label("Current model")
public class ModelSecurityObjectProvider extends AbstractConfiguredInstance<ModelSecurityObjectProvider.Config>
		implements SecurityObjectProvider {

	/**
	 * Typed configuration interface definition for {@link ModelSecurityObjectProvider}.
	 */
	public static interface Config extends PolymorphicConfiguration<ModelSecurityObjectProvider> {

		/** Configuration name for {@link #getModelType()}. */
		String MODEL_TYPE = "model-type";

		/**
		 * The type that the model may have.
		 *
		 * <p>
		 * If the value is <code>null</code> then the model can have each type.
		 * </p>
		 */
		@Name(MODEL_TYPE)
		TLModelPartRef getModelType();

		/**
		 * Setter for {@link #getModelType()}.
		 */
		void setModelType(TLModelPartRef value);

	}

	/**
	 * Create a {@link ModelSecurityObjectProvider}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public ModelSecurityObjectProvider(InstantiationContext context, Config config) {
		super(context, config);
		TLModelPartRef modelType = getConfig().getModelType();
		if (modelType != null) {
			// Try resolving model type. fails if impossible.
			try {
				modelType.resolveClass();
			} catch (ConfigurationException ex) {
				context.error("Unable to resolve '" + modelType + "' to TLClass.", ex);
			}
		}
	}

	@Override
	public BoundObject getSecurityObject(BoundChecker aChecker, Object model, BoundCommandGroup aCommandGroup) {
		if (model instanceof BoundObject) {
			return (BoundObject) model;
		}
		return null;
	}

	@Override
	public Set<TLClass> getPossibleSecurityObjectTypes() {
		TLModelPartRef modelType = getConfig().getModelType();
		if (modelType == null) {
			return SecurityObjectProvider.super.getPossibleSecurityObjectTypes();
		}
		try {
			return Collections.singleton(modelType.resolveClass());
		} catch (ConfigurationException ex) {
			throw new ConfigurationError(ex);
		}
	}

}

