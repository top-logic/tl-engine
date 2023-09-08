/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.util.Collection;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.annotation.Container;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.DerivedRef;
import com.top_logic.basic.config.container.ConfigPart;

/**
 * The kind of a {@link PropertyDescriptor} provides a coarse classification of properties by their
 * value type.
 * 
 * @see PropertyDescriptor#kind()
 * 
 * @since 5.7.3
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public enum PropertyKind {

	/**
	 * The property value can be represented by a plain string value in a configuration file.
	 * 
	 * @see PropertyDescriptor#getValueProvider()
	 */
	PLAIN,

	/**
	 * The property value is a configuration sub-structure.
	 * 
	 * <p>
	 * The value of an {@link #ITEM} property is another {@link ConfigurationItem}s with
	 * multiplicity <code>0..1</code>.
	 * </p>
	 */
	ITEM,

	/**
	 * The property contains configuration sub-structures (multiplicity 0..*).
	 * 
	 * <p>
	 * The value of an {@link #ARRAY} property are other {@link ConfigurationItem}s technically
	 * organized in a Java array.
	 * </p>
	 */
	ARRAY,

	/**
	 * The property contains configuration sub-structures (multiplicity 0..*).
	 * 
	 * <p>
	 * The value of a {@link #LIST} property are other {@link ConfigurationItem}s technically
	 * organized in a Java {@link Collection}.
	 * </p>
	 */
	LIST,

	/**
	 * The property value is an indexed configuration sub-structure.
	 * 
	 * reference to other {@link ConfigurationItem}s with multiplicity > 1.
	 * 
	 * @see PropertyDescriptor#getKeyProperty()
	 */
	MAP,

	/**
	 * The property value cannot be represented by a plain string but is itself
	 * not an instance of {@link ConfigurationItem}.
	 * 
	 * @see PropertyDescriptor#getValueBinding()
	 */
	COMPLEX,

	/**
	 * The property value contains a reference to another item of its
	 * configuration instance structure, which is not a containment relation.
	 * 
	 * @see #ITEM A single containment reference.
	 * @see #LIST A multiple containment reference.
	 * @see #MAP An indexed containment reference.
	 */
	REF,

	/**
	 * The property value is derived from other property values.
	 * <p>
	 * If a property on the path is null, the derived property is null, unless:
	 * <ul>
	 * <li>it is one of Java's primitive types. In this case, the implicit default value is
	 * used.</li>
	 * <li>it has an explicit default value annotation.</li>
	 * <li>it has a format or value binding annotation. In this case
	 * {@link ConfigurationValueProvider#defaultValue()} is used.</li>
	 * </ul>
	 * <b>This means, even types that normally have implicit defaults in the
	 * {@link TypedConfiguration} can be null, when they are derived.</b>
	 * </p>
	 * <p>
	 * {@link Container} properties store the container of a {@link ConfigPart} and have this
	 * {@link PropertyKind}, too, as they are not set-able, just like normal derived properties.
	 * </p>
	 * 
	 * @see Derived
	 * @see DerivedRef
	 * @see Container
	 * @see PropertyDescriptor#hasContainerAnnotation()
	 */
	DERIVED,
	
	;

	/**
	 * Service method to call in default case of "switch"s when all kinds are covered.
	 */
	public static UnreachableAssertion noSuchPropertyKind(PropertyKind kind) {
		throw new UnreachableAssertion("Unknown " + PropertyKind.class.getName() + ": " + kind);
	}

}
