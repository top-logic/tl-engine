/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.authoring;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationDescriptorConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.PropertyDescriptorConfig;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.format.FuzzyClassFormat;
import com.top_logic.basic.func.IGenericFunction;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;
import com.top_logic.model.format.ObjectType;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.tl.ai.core.tools.authoring.ScriptToolStore.ParamSpec;
import com.top_logic.tl.ai.core.tools.model.AgentObjectFormat;
import com.top_logic.tl.ai.core.tools.model.AgentObjectListFormat;
import com.top_logic.tl.ai.core.tools.registry.ScriptToolFactory.ScriptToolConfig;
import com.top_logic.util.Resources;

/**
 * Translation between the {@link ParamSpec}s an agent writes and the
 * {@link ScriptToolConfig#getParameters() parameter declaration} of a script tool.
 */
final class ScriptToolParameters {

	/** The texts of a declaration written by an agent are English, as everything told to a model. */
	private static final Locale LOCALE = Locale.ENGLISH;

	/** Property name of {@link Format#value()}, {@link ObjectType#value()}. */
	private static final String VALUE = "value";

	/** Property name of {@link Options#fun()}. */
	private static final String FUN = "fun";

	private static final FuzzyClassFormat CLASS_FORMAT = new FuzzyClassFormat();

	private ScriptToolParameters() {
		// Static utility.
	}

	/**
	 * The parameter declaration for the given specifications.
	 *
	 * @return The declaration, {@code null} for no parameters.
	 * @throws IllegalArgumentException
	 *         If a specification is invalid, with a message for the model.
	 */
	static ConfigurationDescriptorConfig declaration(List<ParamSpec> specs) {
		if (specs == null || specs.isEmpty()) {
			return null;
		}
		ConfigurationDescriptorConfig declaration = TypedConfiguration.newConfigItem(ConfigurationDescriptorConfig.class);
		Map<String, PropertyDescriptorConfig> properties = declaration.getProperties();
		for (ParamSpec spec : specs) {
			PropertyDescriptorConfig property = property(spec);
			if (properties.containsKey(property.getName())) {
				throw new IllegalArgumentException("The parameter '" + property.getName() + "' is declared twice.");
			}
			properties.put(property.getName(), property);
		}
		BufferingProtocol log = new BufferingProtocol();
		try {
			TypedConfiguration.getConfigurationDescriptor(log, declaration);
		} catch (RuntimeException ex) {
			throw new IllegalArgumentException("The parameters are invalid: " + ex.getMessage(), ex);
		}
		if (log.hasErrors()) {
			throw new IllegalArgumentException("The parameters are invalid: " + String.join(" ", log.getErrors()));
		}
		return declaration;
	}

	private static PropertyDescriptorConfig property(ParamSpec spec) {
		String name = spec.name();
		if (isBlank(name)) {
			throw new IllegalArgumentException("Each parameter needs a non-empty " + ParamSpec.NAME + ".");
		}
		PropertyDescriptorConfig property = TypedConfiguration.newConfigItem(PropertyDescriptorConfig.class);
		property.setName(name);

		if (!isBlank(spec.objectType())) {
			checkModelType(name, spec.objectType());
			if (spec.multiple()) {
				set(property, PropertyDescriptorConfig.TYPE, List.class);
				set(property, PropertyDescriptorConfig.ELEMENT_TYPE, TLObject.class);
				add(property, format(AgentObjectListFormat.class));
			} else {
				set(property, PropertyDescriptorConfig.TYPE, TLObject.class);
				add(property, format(AgentObjectFormat.class));
			}
			add(property, annotation(ObjectType.class, VALUE, spec.objectType().trim()));
		} else {
			Class<?> type = valueType(name, spec.type());
			if (spec.multiple()) {
				if (type != String.class) {
					throw new IllegalArgumentException("The parameter '" + name + "' is a list of '"
						+ spec.type() + "'. A list holds texts (" + ParamSpec.TYPE + " String) or objects ("
						+ ParamSpec.OBJECT_TYPE + ").");
				}
				set(property, PropertyDescriptorConfig.TYPE, List.class);
				set(property, PropertyDescriptorConfig.ELEMENT_TYPE, String.class);
				add(property, format(CommaSeparatedStrings.class));
			} else {
				set(property, PropertyDescriptorConfig.TYPE, type);
			}
		}

		ResKey.Builder label = ResKey.builder().add(LOCALE, name);
		if (!isBlank(spec.description())) {
			label.suffix(ResKey.TOOLTIP).add(LOCALE, spec.description());
		}
		set(property, PropertyDescriptorConfig.LABEL, label.build());

		if (spec.mandatory()) {
			add(property, TypedConfiguration.newAnnotationItem(Mandatory.class));
		}
		if (!isBlank(spec.defaultValue())) {
			set(property, PropertyDescriptorConfig.DEFAULT, spec.defaultValue());
		}
		if (!isBlank(spec.options())) {
			add(property, annotation(Options.class, FUN, optionsFunction(name, spec.options())));
		}
		return property;
	}

	private static void checkModelType(String name, String objectType) {
		TLType type;
		try {
			type = TLModelPartRef.ref(objectType.trim()).resolveType();
		} catch (RuntimeException ex) {
			throw new IllegalArgumentException("The parameter '" + name + "' has an unknown " + ParamSpec.OBJECT_TYPE
				+ " '" + objectType + "'. Use a qualified model type such as my.module:MyType.", ex);
		}
		if (type == null) {
			throw new IllegalArgumentException("The parameter '" + name + "' has an unknown " + ParamSpec.OBJECT_TYPE
				+ " '" + objectType + "'. Use a qualified model type such as my.module:MyType.");
		}
	}

	private static Class<?> valueType(String name, String type) {
		if (isBlank(type)) {
			return String.class;
		}
		try {
			return CLASS_FORMAT.getValue(ParamSpec.TYPE, type.trim());
		} catch (ConfigurationException ex) {
			throw new IllegalArgumentException("The parameter '" + name + "' has an unknown " + ParamSpec.TYPE + " '"
				+ type + "'. Use String, int, long, double, boolean or the class name of a Java enum, or "
				+ ParamSpec.OBJECT_TYPE + " for a model object.", ex);
		}
	}

	private static Class<?> optionsFunction(String name, String className) {
		Class<?> function;
		try {
			function = CLASS_FORMAT.getValue(ParamSpec.OPTIONS, className.trim());
		} catch (ConfigurationException ex) {
			throw new IllegalArgumentException("The " + ParamSpec.OPTIONS + " of the parameter '" + name
				+ "' name no existing class: '" + className + "'.", ex);
		}
		if (!IGenericFunction.class.isAssignableFrom(function)) {
			throw new IllegalArgumentException("The " + ParamSpec.OPTIONS + " of the parameter '" + name
				+ "' name the class '" + className + "', which is no options function.");
		}
		return function;
	}

	private static Format format(Class<? extends ConfigurationValueProvider<?>> format) {
		return annotation(Format.class, VALUE, format);
	}

	private static <A extends Annotation> A annotation(Class<A> type, String property, Object value) {
		A annotation = TypedConfiguration.newAnnotationItem(type);
		set((ConfigurationItem) annotation, property, value);
		return annotation;
	}

	private static void add(PropertyDescriptorConfig property, Annotation annotation) {
		property.getAnnotations().put(annotation.annotationType(), annotation);
	}

	private static void set(ConfigurationItem item, String property, Object value) {
		item.update(item.descriptor().getProperty(property), value);
	}

	/**
	 * The specifications of the parameters of the given declaration.
	 */
	static List<ParamSpec> specs(ConfigurationDescriptorConfig declaration) {
		List<ParamSpec> result = new ArrayList<>();
		if (declaration == null) {
			return result;
		}
		for (PropertyDescriptorConfig property : declaration.getProperties().values()) {
			result.add(spec(property));
		}
		return result;
	}

	private static ParamSpec spec(PropertyDescriptorConfig property) {
		Map<Class<? extends Annotation>, Annotation> annotations = property.getAnnotations();
		ObjectType objectType = (ObjectType) annotations.get(ObjectType.class);
		Format format = (Format) annotations.get(Format.class);
		Options options = (Options) annotations.get(Options.class);
		Class<?> type = property.getType();
		boolean multiple = type != null && List.class.isAssignableFrom(type);
		boolean object = objectType != null
			|| (format != null && (format.value() == AgentObjectFormat.class || format.value() == AgentObjectListFormat.class));

		String typeName;
		if (object) {
			typeName = "";
		} else if (multiple || type == null) {
			typeName = String.class.getSimpleName();
		} else {
			typeName = typeName(type);
		}
		ResKey label = property.getLabel();
		String description = label == null ? null : Resources.getInstance(LOCALE).getString(label.tooltip(), null);
		return new ParamSpec(
			property.getName(),
			typeName,
			objectType == null ? "" : objectType.value(),
			description == null ? "" : description,
			annotations.containsKey(Mandatory.class),
			multiple,
			property.getDefault() == null ? "" : property.getDefault(),
			options == null ? "" : options.fun().getName());
	}

	private static String typeName(Class<?> type) {
		String simpleName = type.getSimpleName();
		return FuzzyClassFormat.CLASSES_BY_NAME.get(simpleName) == type ? simpleName : type.getName();
	}

	private static boolean isBlank(String value) {
		return value == null || value.isBlank();
	}

}
