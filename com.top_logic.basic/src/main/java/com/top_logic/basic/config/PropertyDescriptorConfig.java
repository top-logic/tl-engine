/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.lang.annotation.Annotation;
import java.util.Map;

import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.config.format.FuzzyClassFormat;

/**
 * Configuration describing a {@link PropertyDescriptor} of a {@link ConfigurationDescriptorConfig}.
 * 
 * @see ConfigurationDescriptorConfig#getProperties()
 * @see DeclarativePropertyDescriptor
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface PropertyDescriptorConfig extends NamedConfigMandatory {

	/** Configuration name for the value of {@link #getDefault()}. */
	String DEFAULT = "default";

	/** Configuration name for the value of {@link #getType()}. */
	String TYPE = "type";

	/** Configuration name for the value of {@link #getElementType()}. */
	String ELEMENT_TYPE = "element-type";

	/** Configuration name for the value of {@link #getKeyAttribute()}. */
	String KEY_ATTRIBUTE = "key-attribute";

	/** Configuration name for the value of {@link #getInstanceType()}. */
	String INSTANCE_TYPE = "instance-type";

	/**
	 * The {@link PropertyDescriptor#getType() value type} of this property.
	 */
	@Format(FuzzyClassFormat.class)
	@Name(TYPE)
	Class<?> getType();

	/**
	 * The {@link PropertyDescriptor#getElementType() element type} of a property of
	 * {@link PropertyDescriptor#kind() kind} {@link PropertyKind#LIST}, {@link PropertyKind#ARRAY},
	 * or {@link PropertyKind#MAP}.
	 */
	@Format(FuzzyClassFormat.class)
	@Name(ELEMENT_TYPE)
	Class<?> getElementType();

	/**
	 * Name of key attribute of this {@link PropertyDescriptor} in case this property
	 * {@link PropertyDescriptor#kind() kind} is a {@link PropertyKind#MAP}.
	 * 
	 * <p>
	 * This is a short-cut for the {@link Key} annotation.
	 * </p>
	 * 
	 * @see PropertyDescriptor#getKeyProperty()
	 */
	@Nullable
	@Name(KEY_ATTRIBUTE)
	String getKeyAttribute();

	/**
	 * String representation of the {@link PropertyDescriptor#getDefaultValue() default value} of
	 * the property.
	 * 
	 * <p>
	 * The resulting default value is parsed from the given string representation using the
	 * property's format.
	 * </p>
	 * 
	 * <p>
	 * This is a short-cut for various default annotations depending on the properties
	 * {@link #getType()} including {@link StringDefault}, {@link IntDefault}, and so on.
	 * </p>
	 */
	@Nullable
	@Name(DEFAULT)
	String getDefault();

	/**
	 * If {@link #getType()} is assignable to {@link PolymorphicConfiguration}, the
	 * {@link #getInstanceType()} determines the concrete type of {@link PolymorphicConfiguration},
	 * e.g. if the type is actually <code>PolymorphicConfiguration&lt;Mapping&gt;</code> then the
	 * type is PolymorphicConfiguration and the instance type is <code>Mapping</code>.
	 */
	@Name(INSTANCE_TYPE)
	Class<?> getInstanceType();

	/**
	 * Additional {@link Annotation}s (indexed by the type of the annotation) for this property.
	 * 
	 * @see #getDefault()
	 * @see #getKeyAttribute()
	 */
	@Key(ConfigurationItem.CONFIGURATION_INTERFACE_NAME)
	@DefaultContainer
	Map<Class<? extends Annotation>, Annotation> getAnnotations();

}

