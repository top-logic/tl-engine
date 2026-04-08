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
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.channel.linking.impl.ChannelLinking;
import com.top_logic.layout.channel.linking.impl.DirectLinking;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.util.AllClasses;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.SecurityObjectProvider;
import com.top_logic.util.model.ModelService;

/**
 * {@link SecurityObjectProvider} that uses a configured model as security object.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@InApp
public class ConfiguredModelSecurityProvider extends AbstractConfiguredInstance<ConfiguredModelSecurityProvider.Config>
		implements SecurityObjectProvider {

	/**
	 * Typed configuration interface definition for {@link ConfiguredModelSecurityProvider}.
	 */
	@DisplayOrder({
		Config.MODEL,
		Config.SECURITY_OBJECT_TYPES,
	})
	public interface Config extends PolymorphicConfiguration<ConfiguredModelSecurityProvider> {

		/** Configuration name for the value {@link #getModel()}. */
		String MODEL = "model";

		/** Configuration name for the value {@link #getSecurityObjectTypes()}. */
		String SECURITY_OBJECT_TYPES = "security-object-types";

		/**
		 * Model which is used as security object.
		 */
		@Mandatory
		@Name(MODEL)
		@ImplementationClassDefault(DirectLinking.class)
		ModelSpec getModel();

		/**
		 * Setter for {@link #getModel()}.
		 */
		void setModel(ModelSpec model);

		/**
		 * The collection of types that a security object may have.
		 */
		@Name(SECURITY_OBJECT_TYPES)
		@Options(fun = AllClasses.class)
		@Format(TLModelPartRef.CommaSeparatedTLModelPartRefs.class)
		List<TLModelPartRef> getSecurityObjectTypes();
	}

	private final Set<TLClass> _securityObjectTypes = new HashSet<>();

	/**
	 * Create a {@link ConfiguredModelSecurityProvider}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public ConfiguredModelSecurityProvider(InstantiationContext context, Config config) {
		super(context, config);
		TLModel applicationModel = ModelService.getApplicationModel();
		for (TLModelPartRef secObjectType : config.getSecurityObjectTypes()) {
			try {
				_securityObjectTypes.add(secObjectType.resolveClass(applicationModel));
			} catch (ConfigurationException ex) {
				context.error("Unable to resolve " + secObjectType.qualifiedName(), ex);
			}
		}
		if (_securityObjectTypes.isEmpty()) {
			// Compatibility
			_securityObjectTypes.add(TLModelUtil.tlObjectType(applicationModel));
		}
	}

	@Override
	public BoundObject getSecurityObject(BoundChecker aChecker, Object model, BoundCommandGroup aCommandGroup) {
		Object securityObject = ChannelLinking.eval((LayoutComponent) aChecker, getConfig().getModel());
		if (securityObject instanceof BoundObject) {
			return (BoundObject) securityObject;
		}
		return null;
	}

	@Override
	public Set<TLClass> getPossibleSecurityObjectTypes() {
		return _securityObjectTypes;
	}

}

