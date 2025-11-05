/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.ReferenceResolver;

/**
 * Annotation to mark a configuration as possible reference target.
 * 
 * <p>
 * The annotation can either be set on a property or a configuration interface extending
 * {@link PolymorphicConfiguration}.
 * </p>
 * 
 * @implNote
 *           <p>
 *           When set on a property, the value of an identifier attribute uniquely identifies an
 *           instance instantiated from a {@link PolymorphicConfiguration} carrying this attribute.
 *           Each configuration item may have at most one identifier attribute. The instance created
 *           from a configuration with an identifier attribute can be resolved from constructors of
 *           other instances using the
 *           {@link InstantiationContext#resolveReference(Object, Class, ReferenceResolver)} method
 *           by passing the value of the identifier attribute (or the special identifier
 *           {@link InstantiationContext#OUTER} when called from the constructor of an inner
 *           instance).
 *           </p>
 * 
 *           <p>
 *           When set on a configuration interface, the {@link #value()} parameter must be
 *           specified. Instances instantiated from those configuration can then be resolved from
 *           inner instances by calling the
 *           {@link InstantiationContext#resolveReference(Object, Class, ReferenceResolver)} from
 *           the constructor only passing the special identifier {@link InstantiationContext#OUTER}.
 *           </p>
 * 
 * @see InstantiationContext#resolveReference(Object, Class,
 *      com.top_logic.basic.config.ReferenceResolver)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@TagName("id")
public @interface Id {

	/**
	 * The type scope of the property annotated with {@link Id}.
	 * 
	 * <p>
	 * Identifiers of scope <code>T</code> can only be resolved through {@link ReferenceResolver}s
	 * with the same type.
	 * </p>
	 * 
	 * @see InstantiationContext#resolveReference(Object, Class, ReferenceResolver)
	 */
	Class<?> value() default Void.class;

}
