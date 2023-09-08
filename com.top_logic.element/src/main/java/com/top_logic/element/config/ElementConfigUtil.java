/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.config;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.FilterUtil;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.element.config.annotation.TLSingletons;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.InstancePresentation;
import com.top_logic.model.annotate.util.TLAnnotations;
import com.top_logic.model.config.JavaClass;
import com.top_logic.model.config.ScopeConfig;
import com.top_logic.model.config.TypeConfig;

/**
 * Operations on {@link ModelConfig} and its contents.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ElementConfigUtil {

	/**
	 * Check that there are definitions for all used reference names in the given configuration.
	 */
	public static void resolveNames(ModelConfig self) {
		for (ModuleConfig module : self.getModules()) {
			ElementConfigUtil.resolveElements(module);
		}
	}

	private static void resolveElements(ModuleConfig self) {
		Set<String> definitions = definedClassNames(self);

		String theRootType = ElementConfigUtil.getDefaultSingletonType(self);
		if ((!StringServices.isEmpty(theRootType)) && (!definitions.contains(theRootType))) {
			throw new IllegalArgumentException("Missing root element '" + theRootType + "' in structure.");
		}
	}

	private static Set<String> definedClassNames(ModuleConfig self) {
		return fillClassNames(new HashSet<>(), self);
	}

	private static Set<String> fillClassNames(Set<String> buffer, ScopeConfig self) {
		for (ClassConfig classConfig : getClassTypes(self)) {
			buffer.add(classConfig.getName());

			fillClassNames(buffer, classConfig);
		}
		return buffer;
	}

	/**
	 * {@link ModuleConfig} from given {@link ModelConfig#getModules()} with the given name.
	 * 
	 * @throws IllegalArgumentException
	 *         If there is not structure with the given name.
	 */
	public static ModuleConfig getStructureOrFail(ModelConfig self, String structureName) {
		ModuleConfig result = self.getModule(structureName);
		
		if (result == null) {
			throw new IllegalArgumentException("Request for non-existent structure: " + structureName);
		}
		
		return result;
	}

	/**
	 * Merges definitions in other into target.
	 * 
	 * @param target
	 *        The modified configuration.
	 * @param source
	 *        The source of the merged definitions.
	 */
	public static void merge(ModelConfig target, ModelConfig source) {
		// nothing to do if other config contains nothing
		if (source != null && !ElementConfigUtil.isEmpty(source)) {
			if (ElementConfigUtil.isMergable(target, source)) {
				// handle structures
				for (ModuleConfig sourceModule : source.getModules()) {
					String moduleName = sourceModule.getName();
		
					ModuleConfig targetModule = target.getModule(moduleName);
					if (targetModule == null) {
						// Modules that are not present yet can just be created.
						ModuleConfig newModule = TypedConfiguration.copy(sourceModule);
						target.getModules().add(newModule);
					} else {
						// For modules that are already present the content must be merged.
						mergeTypes(targetModule, sourceModule);
					}
				}

				// Ensure integrity.
				ElementConfigUtil.resolveNames(target);
			} else {
				throw new IllegalArgumentException("Given configuration can not be merged.");
			}
		}
	}

	private static boolean isEmpty(ModelConfig self) {
		return self.getModules().isEmpty();
	}

	private static boolean isMergable(ModelConfig targetModel, ModelConfig otherModel) {
		if (otherModel.getModules() != null && targetModel.getModules() != null) {
			for (ModuleConfig otherModule : otherModel.getModules()) {
				String moduleName = otherModule.getName();
				ModuleConfig targetModule = targetModel.getModule(moduleName);
				if (targetModule != null) {
					// The types of common modules must be disjunct
					for (TypeConfig otherType : otherModule.getTypes()) {
						String typeName = otherType.getName();
						if (ElementConfigUtil.definesType(targetModule, typeName)) {
							Logger.debug("Multiple definitions of type '" + typeName + "', modules '" + moduleName
								+ "' not mergeable.", ElementConfigUtil.class);
							return false;
						}
					}
				}
			} 
		}
		return true;
	}

	private static boolean definesType(ModuleConfig moduleConfig, String name) {
		return moduleConfig.getType(name) != null;
	}

	private static void mergeTypes(ModuleConfig baseModule, ModuleConfig addonModule) {
		assert baseModule != null : "No destination for merge given.";
		if (addonModule == null) {
			// No merge required.
			return;
		}

		for (TypeConfig sourceType : addonModule.getTypes()) {
			String typeName = sourceType.getName();
			if (baseModule.getType(typeName) != null) {
				throw new IllegalArgumentException("Type configurations are not dijunct, encountered " + typeName
					+ " in both versions of module '" + baseModule.getName() + "'.");
			}
			TypeConfig newType = TypedConfiguration.copy(sourceType);
			baseModule.getTypes().add(newType);
		}
	}

	/**
	 * Resolved {@link #getJavaClassName(ClassConfig)}.
	 */
	public static Class<? extends Wrapper> getWrapperClass(ClassConfig self) {
		String className = ElementConfigUtil.getJavaClassName(self);
		if (className.isEmpty()) {
			return null;
		}
		try {
			return ConfigUtil.lookupClassForName(Wrapper.class, className);
		} catch (ConfigurationException e) {
			Logger.error("can't find configured java class '" + className + "' in '" + self.location()
				+ "'.", e, ElementConfigUtil.class);
			return null;
		}
	}

	/**
	 * The Java class name of the wrapper class to be used for elements described by the given
	 * configuration.
	 */
	public static String getJavaClassName(ClassConfig classConfig) {
		JavaClass annotation = TLAnnotations.getAnnotation(classConfig, JavaClass.class);
		if (annotation == null) {
			return StringServices.EMPTY_STRING;
		}
		return annotation.getClassName();
	}

	/**
	 * {@link InstancePresentation#getIcon()}, or <code>null</code>, if it's not defined.
	 */
	public static ThemeImage getDefaultIconOrNull(TLType type) {
		return getIcon(type);
	}

	private static ThemeImage getIcon(TLType type) {
		InstancePresentation instancePresentation = instancePresentation(type);
		if (instancePresentation == null) {
			return null;
		}
		return instancePresentation.getIcon();
	}

	private static InstancePresentation instancePresentation(TLType type) {
		return TLAnnotations.getAnnotation(type, InstancePresentation.class);
	}

	/**
	 * {@link InstancePresentation#getLargeIcon()}, or <code>null</code>, if it's not defined.
	 */
	public static ThemeImage getLargeIconOrNull(TLType self) {
		return getLargeIcon(self);
	}

	private static ThemeImage getLargeIcon(TLType self) {
		InstancePresentation instancePresentation = instancePresentation(self);
		if (instancePresentation == null) {
			return null;
		}
		return instancePresentation.getLargeIcon();
	}

	/**
	 * {@link InstancePresentation#getExpandedIconOrEmpty()}, or
	 * {@link InstancePresentation#getIcon()}, if the former is not given.
	 */
	public static ThemeImage getOpenedIconWithDefaultAsFallback(TLType type) {
		ThemeImage openedIcon = getExpandedIconOrEmpty(type);

		return openedIcon != null ? openedIcon : ElementConfigUtil.getDefaultIconOrNull(type);
	}

	private static ThemeImage getExpandedIconOrEmpty(TLType type) {
		InstancePresentation instancePresentation = instancePresentation(type);
		if (instancePresentation == null) {
			return null;
		}
		return instancePresentation.getExpandedIconOrEmpty();
	}

	public static String getDefaultSingletonType(ModuleConfig moduleConfig) {
		SingletonConfig singleSingleton = getDefaultSingleton(moduleConfig);
		if (singleSingleton == null) {
			return null;
		}
		return singleSingleton.getTypeSpec();
	}

	private static SingletonConfig getDefaultSingleton(ModuleConfig moduleConfig) {
		return getSingleton(moduleConfig, TLModule.DEFAULT_SINGLETON_NAME);
	}

	public static SingletonConfig getSingleton(ModuleConfig moduleConfig, String name) {
		TLSingletons singletonsAnnotation = moduleConfig.getAnnotation(TLSingletons.class);
		if (singletonsAnnotation == null) {
			return null;
		}
		return singletonsAnnotation.getSingleton(name);
	}

	public static Collection<SingletonConfig> getSingletons(ModuleConfig moduleConfig) {
		TLSingletons singletonsAnnotation = moduleConfig.getAnnotation(TLSingletons.class);
		if (singletonsAnnotation == null) {
			return Collections.emptyList();
		}
		return singletonsAnnotation.getSingletons();
	}

	public static ClassConfig getClassType(ModuleConfig moduleConfig, String name) {
		return (ClassConfig) moduleConfig.getType(name);
	}

	public static Iterable<ClassConfig> getClassTypes(ScopeConfig module) {
		return FilterUtil.filterIterable(ClassConfig.class, module.getTypes());
	}

	public static Iterable<ObjectTypeConfig> getObjectTypes(ScopeConfig scopeConfig) {
		return FilterUtil.filterIterable(ObjectTypeConfig.class, scopeConfig.getTypes());
	}

	public static ObjectTypeConfig getObjectType(ScopeConfig scopeConfig, String name) {
		return (ObjectTypeConfig) scopeConfig.getType(name);
	}

}
