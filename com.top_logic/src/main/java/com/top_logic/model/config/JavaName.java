/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.config;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.AnnotationInheritance;
import com.top_logic.model.annotate.AnnotationInheritance.Policy;
import com.top_logic.model.annotate.DefaultStrategy;
import com.top_logic.model.annotate.DefaultStrategy.Strategy;
import com.top_logic.model.annotate.TLAttributeAnnotation;

/**
 * {@link TLAttributeAnnotation} defining the name from which the generated Java access methods of
 * the annotated {@link TLStructuredTypePart} are built.
 *
 * <p>
 * The wrapper generator builds getter, setter, the <code>add</code> and <code>remove</code>
 * methods of a multiple reference (with the singular of the name), and the modification getter
 * from this name instead of the attribute name. The name of the attribute in the model, its name
 * constant, and its storage are not affected.
 * </p>
 *
 * <p>
 * Use this annotation, if the access methods built from the attribute name clash with a method
 * the generated interface or implementation class inherits, e.g. an attribute
 * <code>lastModified</code> with {@link com.top_logic.knowledge.wrap.Wrapper#getLastModified()}.
 * </p>
 *
 * <p>
 * An attribute overriding the annotated attribute inherits the annotation, since its generated
 * getter overrides the getter of the overridden attribute.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName(JavaName.TAG_NAME)
@AnnotationInheritance(Policy.FINAL)
@DefaultStrategy(Strategy.NONE)
public interface JavaName extends TLAttributeAnnotation {

	/** Tag name of the {@link JavaName} annotation. */
	String TAG_NAME = "java-name";

	/** @see #getValue() */
	String VALUE = "value";

	/**
	 * The name from which the Java access methods of the annotated attribute are built.
	 *
	 * <p>
	 * The value is a Java identifier in lower camel case, e.g. <code>configurationChanged</code>
	 * results in <code>getConfigurationChanged()</code> and
	 * <code>setConfigurationChanged(...)</code>.
	 * </p>
	 */
	@Name(VALUE)
	@Mandatory
	String getValue();

	/**
	 * @see #getValue()
	 */
	void setValue(String value);

}
