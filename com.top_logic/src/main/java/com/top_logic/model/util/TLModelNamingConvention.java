/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.util;

import static com.top_logic.basic.generate.CodeUtil.*;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.core.workspace.ModuleLayoutConstants;
import com.top_logic.basic.generate.CodeUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.knowledge.gui.I18NConstants;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.annotate.TLI18NKey;
import com.top_logic.model.config.FactoryClass;
import com.top_logic.model.config.JavaClass;
import com.top_logic.model.config.JavaPackage;

/**
 * Naming convention for java file and constant names for parts in the {@link TLModel} hierarchy.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TLModelNamingConvention {

	/** Common prefix for I18N holding the name of a {@link TLType}. */
	private static final ResKey TYPE_NAME_PREFIX = ResKey.internalModel("model");

	/**
	 * Fully qualified java name for the factory class defining constants and factory methods for
	 * the given module.
	 * 
	 * @param module
	 *        The module to get factory class name for.
	 * 
	 * @return The fully qualified java class name or <code>null</code>, if class can be determined.
	 */
	public static String factoryClassName(TLModule module) {
		FactoryClass factoryAnnotation = module.getAnnotation(FactoryClass.class);
		if (factoryAnnotation != null) {
			String factoryClassName = factoryAnnotation.getValue();
			if (!factoryClassName.isEmpty()) {
				return factoryClassName;
			}
		}
		String intfPackage = javaInterfacePackage(module);
		if (intfPackage == null) {
			return null;
		}
		return intfPackage + '.' + toCamelCase(module.getName()) + "Factory";
	}

	/**
	 * Fully qualified name of the java class holding the constants for the given type.
	 * 
	 * @param type
	 *        The type to get constant class name for.
	 * 
	 * @return The fully qualified java class name ore <code>null</code>, if no class can be
	 *         determined.
	 */
	public static String constantName(TLType type) {
		String intfPackage = javaInterfacePackage(type.getModule());
		if (intfPackage == null) {
			return null;
		}
		return intfPackage + '.' + toCamelCase(type.getName()) + "Constants";
	}

	/**
	 * Name of the accessor method for the given type.
	 * 
	 * @param type
	 *        The type to get accessor method name for.
	 */
	public static String typeAccessorName(TLType type) {
		return "get" + toCamelCase(type.getName()) + "Type";
	}

	/**
	 * Name of the accessor method for the given {@link TLEnumeration}.
	 * 
	 * @param type
	 *        The {@link TLEnumeration} to get accessor method name for.
	 */
	public static String enumAccessorName(TLEnumeration type) {
		return "get" + toCamelCase(type.getName()) + "Enum";
	}

	/**
	 * Name of the factory method of the given type.
	 * 
	 * @param type
	 *        The type to get accessor method name for.
	 */
	public static String typeFactoryName(TLType type) {
		return "create" + toCamelCase(type.getName());
	}

	/**
	 * Name of the accessor method to return the given part.
	 * 
	 * @param part
	 *        The part to get accessor method name for.
	 */
	public static String typePartAccessorName(TLStructuredTypePart part) {
		return "get" + toCamelCase(part.getName()) + toCamelCase(part.getOwner().getName()) + "Attr";
	}

	/**
	 * Name of the accessor method to return the given {@link TLClassifier}.
	 * 
	 * @param part
	 *        The {@link TLClassifier} to get accessor method name for.
	 */
	public static String classifierAccessorName(TLClassifier part) {
		return "get" + toCamelCase(part.getName()) + toCamelCase(part.getOwner().getName()) + "Classifier";
	}

	/**
	 * Name of the constant holding the name of the table which is used to store instances of the
	 * given type.
	 */
	public static String tableNameConstant(TLType type) {
		return "KO_NAME_" + toAllUpperCase(toCamelCase(type.getName()));
	}

	/**
	 * Name of the java constant in type {@link #constantName(TLType)} that hold the name of the
	 * given type.
	 */
	public static String typeNameConstant(TLType type) {
		return toAllUpperCase(toCamelCase(type.getName())) + "_TYPE";
	}

	/**
	 * Name of the java constant for the given {@link TLEnumeration}.
	 */
	public static String enumNameConstant(TLEnumeration type) {
		return toAllUpperCase(toCamelCase(type.getName())) + "_ENUM";
	}

	/**
	 * Name of the java constant for the given {@link TLClassifier}.
	 */
	public static String classifierNameConstant(TLClassifier classifier) {
		return toAllUpperCase(classifier.getName()) + "_" + toAllUpperCase(classifier.getOwner().getName())
				+ "_CLASSIFIER";
	}

	/**
	 * Name of the java constant in type {@link #constantName(TLType)} of the owner type of the
	 * given part, that hold the name of the given type part.
	 */
	public static String partNameConstant(TLTypePart part) {
		return toAllUpperCase(part.getName()) + "_ATTR";
	}

	/**
	 * Fully qualified java name of the base implementation class for the given type.
	 * 
	 * <p>
	 * This class implements {@link #interfaceBaseName(TLType)}.
	 * </p>
	 * 
	 * @param type
	 *        Type to get base implementation class name for.
	 * 
	 * @return Base implementation class name or <code>null</code>, when no class can be determined.
	 */
	public static String legacyImplementationBaseName(TLType type) {
		String implPackage = javaImplementationPackage(type.getModule());
		if (implPackage == null) {
			return null;
		}
		return implPackage + '.' + toCamelCase(type.getName()) + "BaseImpl";
	}

	/**
	 * Fully qualified java name of the base interface class for the given type.
	 * 
	 * <p>
	 * This interface holds getter and setter for attributes of the given type.
	 * </p>
	 * 
	 * @param type
	 *        Type to get base interface class name for.
	 * 
	 * @return Base interface class name or <code>null</code>, when no interface can be determined.
	 * 
	 * @see #legacyImplementationBaseName(TLType)
	 */
	public static String interfaceBaseName(TLType type) {
		String intfPackage = javaImplementationPackage(type.getModule());
		if (intfPackage == null) {
			return null;
		}
		return intfPackage + '.' + toCamelCase(type.getName()) + "Base";
	}

	/**
	 * Fully qualified java name of the interface class for the given type.
	 * 
	 * <p>
	 * This interface extends the {@link #interfaceBaseName(TLType)} and holds additional methods
	 * that are not reflected by the {@link TLModel}.
	 * </p>
	 * 
	 * @param type
	 *        Type to get interface class name for.
	 * 
	 * @return Business interface class name or <code>null</code>, when no interface can be
	 *         determined.
	 * 
	 * @see #interfaceBaseName(TLType)
	 */
	public static String interfaceName(TLType type) {
		JavaClass javaClassAnnotation = type.getAnnotation(JavaClass.class);
		if (javaClassAnnotation != null) {
			String javaInterfaceName = javaClassAnnotation.getInterfaceName();
			if (!javaInterfaceName.isEmpty() ) {
				return javaInterfaceName;
			}
		}
		String intfPackage = javaInterfacePackage(type.getModule());
		if (intfPackage == null) {
			return null;
		}
		return intfPackage + '.' + toCamelCase(type.getName());
	}

	/**
	 * Fully qualified java name of the implementation class for the given type.
	 * 
	 * <p>
	 * This class extends {@link #legacyImplementationBaseName(TLType)} and implements
	 * {@link #interfaceName(TLType)}.
	 * </p>
	 * 
	 * @param type
	 *        Type to get main implementation class name for.
	 * 
	 * @return Business class name or <code>null</code>, when no class can be determined.
	 * 
	 * @see #legacyImplementationBaseName(TLType)
	 * @see #interfaceName(TLType)
	 */
	public static String implementationName(TLType type) {
		JavaClass javaClassAnnotation = type.getAnnotation(JavaClass.class);
		if (javaClassAnnotation != null) {
			String className = javaClassAnnotation.getClassName();
			if (!className.isEmpty()) {
				return className;
			}
		}
		String implPackage = javaImplementationPackage(type.getModule());
		if (implPackage == null) {
			return null;
		}
		return implPackage + '.' + toCamelCase(type.getName()) + "Impl";
	}

	/**
	 * Returns the annotated java interface package of the given {@link TLModule}.
	 * 
	 * <p>
	 * The returned package holds the interfaces and constants for classes in the given module (if a
	 * class does not have a special annotation).
	 * </p>
	 * 
	 * @param module
	 *        The {@link TLModule} to get interface package for.
	 * @return The annotated Java interface package, or <code>null</code>, if no such package is
	 *         defined.
	 */
	public static String javaInterfacePackage(TLModule module) {
		JavaPackage annotation = module.getAnnotation(JavaPackage.class);
		if (annotation != null) {
			return annotation.getInterfacePackage();
		}
		return null;
	}

	/**
	 * Returns the annotated java implementation package of the given {@link TLModule}.
	 * 
	 * <p>
	 * The returned package holds the java classes for {@link TLClass} in the given module (if a
	 * class does not have a special annotation).
	 * </p>
	 * 
	 * @param module
	 *        The {@link TLModule} to get implementation package for.
	 * @return The name of the {@link TLModule}'s implementation package, or <code>null</code> if no
	 *         such package is annotated to the given {@link TLModule}.
	 */
	public static String javaImplementationPackage(TLModule module) {
		JavaPackage annotation = module.getAnnotation(JavaPackage.class);
		if (annotation != null) {
			return annotation.getImplementationPackage();
		}
		return null;
	}

	/**
	 * Name the resources file holding the messages for the given {@link TLModule} in the given
	 * language.
	 * 
	 * @apiNote This method is used by the wrapper generator and In-App-exporter to determine the
	 *          file name to store resources to.
	 * 
	 * @param module
	 *        {@link TLModule} to get name of resource file for.
	 * @param language
	 *        The language as defined in <i>TopLogic</i>, e.g. de, en.
	 */
	public static String resourcesFileName(TLModule module, String language) {
		return getBundleName(module) + "_" + language + ResourcesModule.EXT;
	}

	/**
	 * Bundle name of {@link #resourcesFileName(TLModule, String) resource file names} as entered in
	 * the application configuration.
	 *
	 * @param module
	 *        The {@link TLModel} the resources are for.
	 * @return The base name of the resources files, see
	 *         {@link ModuleLayoutConstants#RESOURCES_RESOURCE_PREFIX}.
	 */
	public static String getBundleName(TLModule module) {
		return "model." + module.getName() + ".messages";
	}

	/**
	 * Name of the resource key for the name of the given {@link TLClass}.
	 */
	public static ResKey resourceKey(TLType type) {
		return TLModelNamingConvention.getTypeLabelKey(type);
	}

	/**
	 * Name of the resource key for the given {@link TLStructuredTypePart type part}.
	 */
	public static ResKey resourceKey(TLTypePart part) {
		return TLModelI18N.getI18nKeyWithoutFallback(part.getOwner(), part);
	}

	/**
	 * Construct a resource key for the internationalized display of the given type name.
	 */
	public static ResKey getTypeNameResourceKey(String moduleName, String typeName) {
		return modelPartNameKey(moduleName + "." + typeName);
	}

	/**
	 * Label key for a {@link TLModule}.
	 */
	public static ResKey getModuleLabelKey(TLModule module) {
		String moduleName = module.getName();
		return ResKey.fallback(modelPartNameKey(moduleName), nameKey(TLModelUtil.getLocalName(moduleName)));
	}
	
	/**
	 * {@link ResKey} for the model part with the given fully qualified dotted name.
	 */
	public static ResKey modelPartNameKey(String qualifiedNameDotted) {
		return TYPE_NAME_PREFIX.suffix(qualifiedNameDotted);
	}

	/**
	 * The {@link ResKey} for displaying the given {@link TLType}.
	 */
	public static ResKey getTypeLabelKey(TLType type) {
		return ResKey.fallback(getCanonicalTypeLabelKey(type), getTypeFallbackKey(type));
	}

	private static ResKey getTypeFallbackKey(TLType type) {
		TLModule module = type.getModule();
		if (module != null) {
			switch (module.getName()) {
				case ApplicationObjectUtil.TL_TABLES_MODULE:
					return getTableTypeInterfaceKey(type);
			}
		}
		return nameKey(type.getName());
	}

	@Deprecated
	private static ResKey getTableTypeInterfaceKey(TLType model) {
		return I18NConstants.TABLE_TYPE_INTERFACE_CLASS_NAME.fill(getTableInterfaceKey(model));
	}

	@Deprecated
	private static ResKey getTableInterfaceKey(TLType model) {
		return ResKey.fallback(
			modelPartNameKey(ApplicationObjectUtil.TL_TABLES_MODULE + "." + ApplicationObjectUtil.iTableName(model)),
			ResKey.text(model.getName()));
	}

	/**
	 * The {@link ResKey} to use for displaying a given {@link TLType}.
	 */
	public static ResKey getCanonicalTypeLabelKey(TLType type) {
		TLI18NKey annotation = type.getAnnotation(TLI18NKey.class);
		if (annotation != null) {
			return annotation.getValue();
		}

		return modelPartNameKey(TLModelUtil.qualifiedNameDotted(type));
	}

	public static ResKey classifierKey(TLClassifier classifier) {
		TLEnumeration owner = classifier.getOwner();
		ResKey canonicalKey = owner != null ? resourceKey(classifier) : null;
		return ResKey.fallback(canonicalKey, nameKey(classifier.getName()));
	}

	@FrameworkInternal
	public static ResKey enumKey(TLEnumeration classification) {
		return ResKey.fallback(getCanonicalTypeLabelKey(classification), nameKey(classification.getName()));
	}

	private static ResKey nameKey(String name) {
		// Prevent missing key errors for technical types by using the type names as last fallback.
		return ResKey.fallback(legacyNameKey(name), ResKey.text(CodeUtil.englishLabel(name)));
	}

	private static ResKey legacyNameKey(String typeName) {
		return ResKey.deprecated(ResKey.legacy(typeName));
	}

}

