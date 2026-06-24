/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.securityObjectProvider;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.model.TLClass;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.model.util.TLModelPartRef.CommaSeparatedTLModelPartRefs;
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

		/** Configuration name for {@link #getModelTypes()}. */
		String MODEL_TYPES = "model-types";

		/**
		 * The types that the model may have.
		 *
		 * <p>
		 * If the list is empty, the model can have any type.
		 * </p>
		 */
		@Name(MODEL_TYPES)
		@Format(CommaSeparatedTLModelPartRefs.class)
		List<TLModelPartRef> getModelTypes();

		/**
		 * Setter for {@link #getModelTypes()}.
		 */
		void setModelTypes(List<TLModelPartRef> value);

	}

	private final Set<TLClass> _securityObjectTypes = new HashSet<>();

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
		for (TLModelPartRef modelType : getConfig().getModelTypes()) {
			try {
				_securityObjectTypes.add(modelType.resolveClass());
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
		if (_securityObjectTypes.isEmpty()) {
			return SecurityObjectProvider.super.getPossibleSecurityObjectTypes();
		}
		return _securityObjectTypes;
	}

}

