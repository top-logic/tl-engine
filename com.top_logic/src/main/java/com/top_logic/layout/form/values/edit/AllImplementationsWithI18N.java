/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.func.Function0;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.values.DeclarativeFormOptions;
import com.top_logic.layout.form.values.ImplOptionMapping;
import com.top_logic.layout.form.values.ItemOptionMapping;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.util.Resources;

/**
 * Option provider for a {@link PropertyDescriptor} of any sub-type of {@link ConfigurationItem} or
 * {@link PolymorphicConfiguration}.
 * 
 * <p>
 * Computes all options for compatible implementation classes that have I18N.
 * </p>
 * 
 * @see Options#fun()
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class AllImplementationsWithI18N extends Function0<List<? extends Class<?>>>
		implements OptionMappingProvider {

	private final boolean _instance;

	private final Class<?> _type;

	private final OptionMapping _mapping;

	/**
	 * Creates {@link AllImplementationsWithI18N} for the given type class object.
	 */
	@CalledByReflection
	public AllImplementationsWithI18N(DeclarativeFormOptions options) {
		PropertyDescriptor property = options.getProperty();
		Class<?> instanceType = property.getInstanceType();
		_instance = instanceType != null;
		if (_instance) {
			_type = instanceType;
			_mapping = ImplOptionMapping.INSTANCE;
		} else {
			_type = property.getElementType();
			_mapping = ItemOptionMapping.INSTANCE;
		}
		assert _type != null : "Property '" + property + "' has neither instance type nor element type.";
	}

	@Override
	public List<? extends Class<?>> apply() {
		return allSubtypesWithI18N().collect(Collectors.toList());
	}

	private Stream<? extends Class<?>> allSubtypesWithI18N() {
		return allSubTypes(_instance, _type).filter(type -> isTranslated(type));
	}

	static Stream<? extends Class<?>> allSubTypes(boolean instance, Class<?> type) {
		return instance ? allImplementationTypes(type) : allConfigurationTypes(type);
	}

	private static Stream<Class<?>> allConfigurationTypes(Class<?> type) {
		return TypeIndex.getInstance().getSpecializations(type, true, true, false).stream();
	}

	static List<? extends ConfigurationItem> toConfigurations(boolean instance, Stream<? extends Class<?>> types) {
		return instance ? toInstanceConfigurations(types) : toPlainConfigurations(types);
	}

	static List<? extends ConfigurationItem> toInstanceConfigurations(Stream<? extends Class<?>> stream) {
		return toConfigurations(stream, AllImplementationsWithI18N::createInstanceConfig);
	}

	static List<? extends ConfigurationItem> toPlainConfigurations(Stream<? extends Class<?>> types) {
		return toConfigurations(types, AllImplementationsWithI18N::createPlainConfig);
	}

	static List<? extends ConfigurationItem> toConfigurations(Stream<? extends Class<?>> stream,
			Function<Class<?>, ConfigurationItem> allocator) {
		return stream.map(allocator).filter(Objects::nonNull).collect(Collectors.toList());
	}

	static PolymorphicConfiguration<?> createInstanceConfig(Class<?> implementationType) {
		try {
			return TypedConfiguration.createConfigItemForImplementationClass(implementationType);
		} catch (ConfigurationException exception) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	static ConfigurationItem createPlainConfig(Class<?> configType) {
		return TypedConfiguration.newConfigItem((Class<? extends ConfigurationItem>) configType);
	}

	private static boolean isTranslated(Class<?> type) {
		return !StringServices.isEmpty(getTranslation(type));
	}

	private static String getTranslation(Class<?> type) {
		return Resources.getInstance().getString(ResKey.forClass(type), null);
	}

	static Stream<? extends Class<?>> allImplementationTypes(Class<?> type) {
		return TypeIndex.getInstance().getSpecializations(type, true, false, false).stream();
	}

	@Override
	public OptionMapping getOptionMapping() {
		return _mapping;
	}

}
