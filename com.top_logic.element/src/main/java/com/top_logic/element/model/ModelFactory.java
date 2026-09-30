/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.LogProtocol;
import com.top_logic.element.config.ClassConfig;
import com.top_logic.element.config.ModuleConfig;
import com.top_logic.element.config.annotation.ScopeRef;
import com.top_logic.element.meta.MetaElementFactory;
import com.top_logic.element.meta.schema.HolderType;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.element.structured.TLStructureFactory;
import com.top_logic.element.structured.wrap.StructuredElementWrapperFactory;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLScope;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.config.ScopeConfig;
import com.top_logic.model.config.TypeConfig;
import com.top_logic.model.factory.TLFactory;

/**
 * Base class for all factories creating objects in a dynamic model.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ModelFactory implements TLStructureFactory {

	private TLModule _module;

	/**
	 * {@link ClassConfig} scope by class name.
	 */
	private Map<String, ScopeConfig> _scopeConfigs = new HashMap<>();

	@Override
	public TLModule getModule() {
		return _module;
	}

	/**
	 * Starts this factory.
	 * 
	 * @param moduleConfig
	 *        The model definition to read.
	 * @param module
	 *        The module this factory is responsible for.
	 */
	public final void startUp(ModuleConfig moduleConfig, TLModule module) {
		_module = module;

		registerScopes(moduleConfig);

		startUp();
	}

	private void registerScopes(ScopeConfig scopeConf) {
		if (scopeConf == null) {
			return;
		}
		for (TypeConfig typeConfig : scopeConf.getTypes()) {
			if (typeConfig instanceof ScopeConfig) {
				ScopeConfig innerConfig = (ScopeConfig) typeConfig;
				_scopeConfigs.put(typeConfig.getName(), innerConfig);
				registerScopes(innerConfig);
			}
		}
	}

	@Override
	public final String getModuleName() {
		return _module.getName();
	}

	/**
	 * Hook that notifies an implementation about the startup.
	 */
	protected void startUp() {
		// Nothing to do by default.
	}

	/**
	 * Informs this factory that it is no longer active.
	 */
	public void shutDown() {
		_module = null;
	}

	/**
	 * Setup the {@link TLScope} aspect of the given instance by creating local types as configured.
	 * 
	 * @param module
	 *        The defining module.
	 * @param scopeInstance
	 *        The instance to initialize.
	 * @param className
	 *        The instance {@link ClassConfig#getName()} type.
	 */
	public void setupLocalScope(TLModule module, TLScope scopeInstance, String className, TLFactory factory) {
		ScopeConfig scopeConfig = _scopeConfigs.get(className);
		if (scopeConfig != null) {
			LogProtocol log = new LogProtocol(ModelResolver.class);
			ModelResolver modelResolver = new ModelResolver(log, module.getModel(), factory);
			modelResolver.setupScope(module, scopeInstance, scopeConfig);
			modelResolver.complete();
		}
	}

	/**
	 * Looks up the {@link TLClass concrete type} for an object with the given element name in the
	 * context of the given object.
	 * 
	 * @param aContext
	 *        The context in which the type lookup should be performed.
	 * @param aName
	 *        The element name to resolve.
	 * @return The concrete type of an object with the given element name.
	 */
	@Override
	public final TLClass getNodeType(StructuredElement aContext, String aName) {
		ScopeRef scopeRef = StructuredElementWrapperFactory.getScopeRef(aContext, aName);

		MetaElementFactory factory = MetaElementFactory.getInstance();
		return factory.getMetaElement(HolderType.parentScopeBase(aContext), scopeRef.getScopeRef(), getModuleName(),
			scopeRef.getCreateType());
	}

	/**
	 * Sets the default values for the given {@link TLObject} without create context.
	 * 
	 * @see TLFactory#setupDefaultValues(Object, TLObject, TLStructuredType)
	 * 
	 * @deprecated Use {@link TLFactory#setupDefaultValues(Object, TLObject, TLStructuredType)}
	 */
	@Deprecated
	protected final void setupDefaultValues(TLObject newWrapper, TLClass type) {
		TLFactory.setupDefaultValues(null, newWrapper, type);
	}

}
