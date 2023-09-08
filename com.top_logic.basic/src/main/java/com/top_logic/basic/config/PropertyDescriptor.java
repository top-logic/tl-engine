/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.Set;

import com.top_logic.basic.Log;
import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.annotation.Container;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.DerivedRef;
import com.top_logic.basic.util.ResKey;

/**
 * Runtime representation of a property of a {@link ConfigurationDescriptor}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface PropertyDescriptor {

	/**
	 * {@link Comparator} of {@link PropertyDescriptor} by
	 * {@link PropertyDescriptor#getPropertyName()}.
	 */
	Comparator<PropertyDescriptor> BY_NAME_COMPARATOR = new Comparator<>() {

		@Override
		public int compare(PropertyDescriptor o1, PropertyDescriptor o2) {
			return o1.getPropertyName().compareTo(o2.getPropertyName());
		}
	};

	/**
	 * Whether this {@link PropertyDescriptor} is inherited, i.e. whether it has super properties.
	 */
	default boolean isInherited() {
		return getSuperProperties().length > 0;
	}

	/**
	 * Whether this property is not inherited.
	 */
	default boolean isLocalProperty() {
		return !isInherited();
	}

	PropertyDescriptor[] getSuperProperties();

	/**
	 * Is used to identify this {@link PropertyDescriptor} in sub- and superclasses, even if it is
	 * "different" there.
	 * <p>
	 * "Different" means: It might have different annotations, for example a different default
	 * value.
	 * </p>
	 */
	NamedConstant identifier();

	String getPropertyName();

	Object getDefaultValue();

	/**
	 * The potentially generic {@link #getType()} of this property.
	 */
	Type getGenericType();

	/**
	 * The {@link Annotation} of the given class that was declared at some getter of this this
	 * property.
	 * 
	 * <p>
	 * If this property is an inherited one, this annotation is reported that was declared on the
	 * first getter found in depth-first-search order over all super interfaces of the
	 * {@link #getDescriptor()}.
	 * </p>
	 * 
	 * @param annotationClass
	 *        The annotation to locate.
	 * @return The {@link Annotation} instance, or <code>null</code>, if no such annotation was
	 *         declared on any getter of this property.
	 */
	<T extends Annotation> T getAnnotation(Class<T> annotationClass);

	/**
	 * The locally defined (not inherited) annotations of this property.
	 * 
	 * @see #getDescriptor()
	 * @see ConfigurationDescriptor#getSuperDescriptors()
	 */
	Annotation[] getLocalAnnotations();

	/**
	 * Returns the requested {@link Annotation}, if this {@link PropertyDescriptor} (not just a
	 * super {@link PropertyDescriptor}) has that {@link Annotation}.
	 */
	<T extends Annotation> T getLocalAnnotation(Class<T> annotationClass);

	/**
	 * The type which a value for this property has.
	 * 
	 * <p>
	 * Note: It is not necessarily the return type of the getter, e.g. when a sub property has a
	 * more special getter, the {@link #getType()} of the super property is returned.
	 * </p>
	 */
	Class<?> getType();

	Class<?> getElementType();

	/**
	 * Is there an explicit default for this property or any super property, i.e. is there an
	 * default annotation on this or any super property?
	 */
	boolean hasExplicitDefault();

	ConfigurationValueProvider getValueProvider();

	ConfigurationValueBinding getValueBinding();

	ConfigurationDescriptor getDescriptor();

	/**
	 * Whether the property directly contains an implementation instance instead of a configuration
	 * thereof.
	 * 
	 * @see #getInstanceType()
	 */
	boolean isInstanceValued();

	/**
	 * The implementation type this property either directly contains (if
	 * {@link #isInstanceValued()}) or provides a configuration for (if being declared as
	 * {@link PolymorphicConfiguration}).
	 */
	Class<?> getInstanceType();

	/**
	 * Is this property mandatory?
	 */
	boolean isMandatory();

	boolean isMultiple();

	boolean isOrdered();

	/**
	 * Is this property abstract?
	 */
	boolean isAbstract();

	/**
	 * Is this property derived?
	 * 
	 * @see Derived
	 * @see DerivedRef
	 * 
	 * @see PropertyKind#DERIVED
	 */
	boolean isDerived();

	/**
	 * Has this property a {@link Container} annotation or is it inheriting one?
	 */
	boolean hasContainerAnnotation();

	/**
	 * Whether this property is the {@link DefaultContainer} of its declaring type.
	 */
	boolean isDefaultContainer();

	/**
	 * Whether <code>null</code> is a legal value of this property.
	 */
	boolean isNullable();

	/**
	 * Returns the kind of this property.
	 *
	 * @see PropertyKind
	 * 
	 * @since 5.7.3
	 */
	PropertyKind kind();

	/**
	 * The property of the {@link #getValueDescriptor()} that delivers the values to index this
	 * collection with. Returns <code>null</code> when the property is not indexed.
	 */
	PropertyDescriptor getKeyProperty();

	/**
	 * Can this property have a default value?
	 */
	boolean canHaveDefault();

	/**
	 * Can this property have a setter?
	 */
	boolean canHaveSetter();

	/**
	 * The minimum descriptor of the contents of an {@link PropertyKind#ITEM},
	 * {@link PropertyKind#LIST}, {@link PropertyKind#MAP}, or {@link PropertyKind#ARRAY} property.
	 * 
	 * <p>
	 * A potential configuration must be compatible with this descriptor.
	 * </p>
	 * 
	 * @see #getDefaultDescriptor()
	 */
	ConfigurationDescriptor getValueDescriptor();

	/**
	 * The descriptor of the contents of an {@link PropertyKind#ITEM}, {@link PropertyKind#LIST},
	 * {@link PropertyKind#MAP}, or {@link PropertyKind#ARRAY} property, if no explicit
	 * implementation class or configuration interface is configured.
	 * 
	 * @see #getValueDescriptor()
	 */
	ConfigurationDescriptor getDefaultDescriptor();

	/**
	 * The concrete type descriptor for the {@link #getElementNames() given sub tag}.
	 */
	ConfigurationDescriptor getElementDescriptor(String someElementName);

	/**
	 * The {@link #getElementNames() sub tag} to use for the given concrete type descriptor.
	 */
	String getElementName(ConfigurationDescriptor elementDescriptor);

	/**
	 * Set of potential sub tags of this property.
	 */
	Set<String> getElementNames();

	/**
	 * Access algorithm to the key value of an indexed property.
	 */
	Mapping<Object, Object> getKeyMapping();

	/**
	 * Access algorithm to the configuration of a property that allows sub-configuration.
	 */
	ConfigurationAccess getConfigurationAccess();

	/** Storage key for the result of {@link ConfigurationItem#valueSet(PropertyDescriptor)}. */
	@FrameworkInternal
	NamedConstant getValueSetIdentifier();

	/** Storage key for {@link ConfigurationListener}s interested on changes of this property. */
	@FrameworkInternal
	NamedConstant getListenerIdentifier();

	void checkMandatory(Log protocol, ConfigurationItem configBuilder);

	/**
	 * The algorithm computing the value in case this is a {@link PropertyKind#DERIVED} property.
	 */
	DerivedPropertyAlgorithm getAlgorithm();

	/**
	 * Alternative label resource for the given property.
	 * 
	 * @param keySuffix
	 *        The suffix to the key that allows to customize the label for a certain special
	 *        situation. <code>null</code> to retrieve the standard label.
	 * @return The {@link ResKey} for the label.
	 */
	default ResKey labelKey(String keySuffix) {
		ResKey specificKey = getDescriptor().getPropertyLabel(getPropertyName(), keySuffix);
		if (isInherited()) {
			PropertyDescriptor superProperty = getSuperProperties()[0];
			return ResKey.fallback(specificKey, superProperty.labelKey(keySuffix));
		} else {
			return specificKey;
		}
	}

	/**
	 * The "qualified" name of this {@link PropertyDescriptor}.
	 */
	default String qualifiedPropertyName() {
		return getDescriptor().getConfigurationInterface().getName() + "#" + getPropertyName();
	}

}
