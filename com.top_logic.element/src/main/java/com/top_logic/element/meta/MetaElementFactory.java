/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta;

import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.element.config.ClassConfig;
import com.top_logic.element.config.ModuleConfig;
import com.top_logic.element.config.annotation.ScopeRef;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLScope;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;
import com.top_logic.util.model.TL5Types;

/**
 * Create {@link TLClass}s in their {@link MetaElementHolder defining scopes}. 
 *
 * @author    <a href="mailto:kbu@top-logic.com></a>
 */
@ServiceDependencies(PersistencyLayer.Module.class)
public abstract class MetaElementFactory extends ManagedClass {

	/**
	 * Configuration options for {@link MetaElementFactory}.
	 */
	public interface Config extends ServiceConfiguration<MetaElementFactory> {
		// No additional options.
	}

	/**
	 * Creates a {@link MetaElementFactory} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public MetaElementFactory(InstantiationContext context, Config config) {
		super(context, config);
	}

    /**
	 * Create a {@link TLClass} in the given scope
     * @param module
     *        The defining module.
	 * @param scope
	 *        The defining scope, <code>null</code> to create global types.
	 * @param className
	 *        The {@link TLClass} name (must be unique in the given scope). Must not be
	 *        <code>null</code>.
	 * 
	 * @return The created {@link TLClass}.
	 * @throws MetaElementException
	 *         if creation fails or the name is not unique for the given scope.
	 */
	public final TLClass createMetaElement(TLModule module, TLScope scope, String className)
			throws MetaElementException {
		KnowledgeBase theKB = PersistencyLayer.getKnowledgeBase();
		return this.createMetaElement(module, scope, className, theKB);
	}

    /**
	 * Create a {@link TLClass} in the given scope
	 * @param module
	 *        The defining module.
	 * @param scope
	 *        The defining scope, <code>null</code> to create global types.
	 * @param className
	 *        The {@link TLClass} name (must be unique in the given scope). Must not be
	 *        <code>null</code>.
	 * @param kb
	 *        The {@link KnowledgeBase} in which to create the {@link TLClass}.
	 * 
	 * @return The created {@link TLClass}.
	 * @throws MetaElementException
	 *         if creation fails or the name is not unique for the given scope.
	 */
	public abstract TLClass createMetaElement(TLModule module, TLScope scope,
			String className, KnowledgeBase kb) throws MetaElementException;

	/**
	 * Create a {@link TLClass} in the given scope that represents a {@link TLAssociation}.
	 * 
	 * @param module
	 *        The defining module.
	 * @param scope
	 *        The defining scope, <code>null</code> to create global types.
	 * @param className
	 *        The {@link TLClass} name (must be unique in the given scope). Must not be
	 *        <code>null</code>.
	 * @param kb
	 *        The {@link KnowledgeBase} in which to create the {@link TLClass}.
	 * 
	 * @return The created {@link TLClass}.
	 * 
	 * @throws MetaElementException
	 *         if creation fails or the name is not unique for the given scope.
	 */
	public abstract TLClass createAssociationMetaElement(TLModule module, TLScope scope,
			String className, KnowledgeBase kb) throws MetaElementException;

    /**
	 * Resolve the concrete type for a new instance.
	 * 
	 * @param scopeBase
	 *        The context in which the creation happens.
	 * @param moduleName
	 *        The {@link ModuleConfig} name in which the new object resides.
	 * @param className
	 *        The (local) name of the {@link ClassConfig} to instantiate.
	 * 
	 * @return The concrete type to use for an object in the given context.
	 */
	public final TLClass findType(Object scopeBase, String moduleName, String className) {
		return findType(scopeBase, AttributeOperations.globalTypeRef(className), moduleName);
	}

    /**
	 * Resolve the concrete type for a new instance.
	 * 
	 * @param scopeBase
	 *        The context in which the creation happens.
	 * @param scopeRef
	 *        Reference to the target type.
	 * @param moduleName
	 *        The {@link ModuleConfig} name in which the new object resides.
	 * @return The concrete type to use for an object in the given context.
	 */
	public abstract TLClass findType(Object scopeBase, ScopeRef scopeRef, String moduleName);

    /**
	 * Setup the {@link MetaElementHolder} aspect of the given instance by creating local interfaces
	 * as configured.
	 * 
	 * @param module
	 *        The defining module.
	 * @param scopeInstance
	 *        The instance to initialize.
	 * @param className
	 *        The instance {@link ClassConfig#getName()} type.
	 */
	public final void setupLocalScope(TLModule module, TLScope scopeInstance, String className) {
		DynamicModelService.getInstance().setupLocalScope(module, scopeInstance, className);
	}

	/**
	 * Get all {@link TLClass}s that in the global scope.
	 * 
	 * @return The {@link TLClass}s in global scope.
	 */
	public abstract Set<TLClass> getGlobalMetaElements();

    /**
	 * Get all {@link TLClass}s.
	 * 
	 * @return All {@link TLClass}s.
	 */
	public abstract Set<TLClass> getAllMetaElements();

    /**
	 * Get the {@link TLClass} with the given name from global scope.
	 * 
	 * @param moduleName
	 *        The module that defines the given type.
	 * @param className
	 *        The {@link TLClass#getName()} to look up.
	 * 
	 * @return The {@link TLClass}, or <code>null</code>, if there is not global {@link TLClass}
	 *         with the given name.
	 */
	public abstract TLClass lookupGlobalMetaElement(String moduleName, String className);

    /**
	 * Exactly the same as {@link #lookupGlobalMetaElement(String)}, but may never return
	 * <code>null</code>.
	 * 
	 * @throws IllegalArgumentException
	 *         in case there is no global meta element for the given name.
	 */
	@Deprecated
	public final TLClass getGlobalMetaElement(String meName) throws IllegalArgumentException {
		TLClass result = lookupGlobalMetaElement(meName);
		if (result == null) {
			throw new IllegalArgumentException(
				"There is no global interface definition with the given name: " + meName);
		}
		return result;
	}

	/**
	 * TODO #6121: Delete TL 5.8.0 deprecation
	 * 
	 * @deprecated Use {@link TLModelUtil#findType(String)}.
	 */
	@Deprecated
	public final TLClass lookupGlobalMetaElement(String meName) {
		try {
			String interfaceName;
			if (meName.indexOf(TLModelUtil.QUALIFIED_NAME_SEPARATOR) == -1) {
				interfaceName = TL5Types.meTypeSpec(meName);
			} else {
				interfaceName = meName;
			}
			return (TLClass) TLModelUtil.findType(interfaceName);
		} catch (TopLogicException ex) {
			return null;
		}
	}

	public final TLClass getGlobalMetaElement(String moduleName, String interfaceName)
			throws IllegalArgumentException {
		TLModule module = ModelService.getApplicationModel().getModule(moduleName);
		if (module == null) {
			throw new IllegalArgumentException("There is no module '" + moduleName + "' with the given name.");

		}
		TLClass result = (TLClass) module.getType(interfaceName);
        if (result == null) {
            throw new IllegalArgumentException(
				"There is no interface definition in module '" + moduleName + "' with the given name: " + interfaceName);
        }
        return result;
    }

	/**
	 * The singleton service instance
	 */
    public static MetaElementFactory getInstance () {
    	return Module.INSTANCE.getImplementationInstance();
    }
    
	/**
	 * {@link TLClass} to use for class instances with the given class name in the given
	 * context.
	 * 
	 * @param scopeBase
	 *        See {@link #getMetaElement(Object, String, String, String)}.
	 * @param scopeRef
	 *        Reference to the scope to search the concrete type in.
	 * @param moduleName
	 *        The module that defines the class type.
	 * @param className
	 *        The local {@link ClassConfig#getName()} to be resolved.
	 * @return The {@link TLClass} to use.
	 * @throws IllegalArgumentException
	 *         If there is not such {@link TLClass}.
	 */
	public final TLClass getMetaElementForClass(Object scopeBase, String scopeRef, String moduleName,
			String className) {
		return getMetaElement(scopeBase, scopeRef, moduleName, className);
	}

	/**
	 * @deprecated Use {@link DynamicModelService#lookupType(Object, String, String, String)}
	 */
	@Deprecated
	public final TLClass getMetaElement(Object scopeBase, String scopeRef, String moduleName,
			String className) {
		return (TLClass) DynamicModelService.getInstance().lookupType(scopeBase, scopeRef, moduleName,
			className);
	}
    
	/**
	 * TODO #6121: Delete TL 5.8.0 deprecation
	 * 
	 * @deprecated Use {@link #createMetaElement(TLModule, TLScope, String)} and optionally set a
	 *             generalization.
	 */
	@Deprecated
	public final TLClass createMetaElement(TLScope scope, String className,
			TLClass superType) throws MetaElementException {
		TLClass result = this.createMetaElement((TLModule) scope, scope, className);
		MetaElementUtil.setSuperMetaElement(result, superType);
		return result;
	}

	/**
	 * TODO #6121: Delete TL 5.8.0 deprecation
	 * 
	 * @deprecated Use {@link #createMetaElement(TLModule, TLScope, String, KnowledgeBase)} and
	 *             optionally set a super type.
	 */
	@Deprecated
	public final TLClass createMetaElement(TLScope scope, String className,
			TLClass superType, KnowledgeBase kb) throws MetaElementException {
		TLClass result = createMetaElement((TLModule) scope, scope, className, kb);
		MetaElementUtil.setSuperMetaElement(result, superType);
		return result;
	}

	/**
	 * Singleton reference for the {@link MetaElementFactory} service.
	 */
	public static final class Module extends TypedRuntimeModule<MetaElementFactory> {

		/**
		 * Singleton {@link Module} instance.
		 */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<MetaElementFactory> getImplementation() {
			return MetaElementFactory.class;
		}

	}

}
