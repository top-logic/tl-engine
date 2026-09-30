/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.config;

import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.func.Function1;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.model.annotate.AnnotationInheritance;
import com.top_logic.model.annotate.AnnotationInheritance.Policy;

/**
 * {@link TLTypeAnnotation} defining the implementation and interface class of the annotated type.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@TagName("implementation-binding")
@AnnotationInheritance(Policy.REDEFINE)
public interface JavaClass extends TLTypeAnnotation {

	/** @see #getClassName() */
	String CLASS_NAME = "class-name";

	/** @see #getInterfaceName() */
	String INTERFACE_NAME = "interface-name";

	/**
	 * The name of the java class that implements the annotated type.
	 * 
	 * <p>
	 * Implementation detail: The type is {@link String} instead of {@link Class}, because this
	 * annotation is used in generators that generates the java classes. Therefore the class does
	 * not exist at load time.
	 * </p>
	 */
	@Name(CLASS_NAME)
	String getClassName();

	/**
	 * @see #getClassName()
	 */
	void setClassName(String value);

	/**
	 * Resolved {@link #getClassName()}, or <code>null</code> if the class was not found.
	 */
	@Derived(fun = ImplementationClassParser.class, args = @Ref(CLASS_NAME))
	@Hidden
	Class<? extends Wrapper> getImplementationClass();

	/**
	 * Parser function for {@link JavaClass#getImplementationClass()}.
	 */
	class ImplementationClassParser extends Function1<Class<? extends Wrapper>, String> {
		@Override
		public Class<? extends Wrapper> apply(String className) {
			try {
				@SuppressWarnings("unchecked")
				Class<? extends Wrapper> applicationType = (Class<? extends Wrapper>) Class.forName(className);
				return applicationType;
			} catch (ClassNotFoundException ex) {
				return null;
			}
		}
	}

	/**
	 * The name of the java interface that represents the annotated type.
	 * 
	 * <p>
	 * Implementation detail: The type is {@link String} instead of {@link Class}, because this
	 * annotation is used in generators that generates the java classes. Therefore the class does
	 * not exist at load time.
	 * </p>
	 */
	@Name(INTERFACE_NAME)
	String getInterfaceName();

	/**
	 * @see #getInterfaceName()
	 */
	void setInterfaceName(String value);

}
