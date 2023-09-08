/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate;

import java.util.Collection;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Indexed;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;

/**
 * Base interface for configuration that can be annotated with {@link TLAnnotation}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface AnnotatedConfig<A extends TLAnnotation> extends ConfigurationItem, AnnotationLookup {

	/** Property name of {@link #getAnnotations()}. */
	String ANNOTATIONS = "annotations";

	/**
	 * Mapping from the configuration interface of a {@link TLAnnotation} to the annotation itself.
	 */
	@Name(ANNOTATIONS)
	@Key(ConfigurationItem.CONFIGURATION_INTERFACE_NAME)
	@Options(fun = AllInAppImplementations.class)
	Collection<A> getAnnotations();

	/**
	 * Implementation as indexed getter of the {@link #getAnnotations()} property.
	 * 
	 * @see AnnotationLookup#getAnnotation(Class)
	 */
	@Override
	@Indexed(collection = ANNOTATIONS)
	<T extends TLAnnotation> T getAnnotation(Class<T> annotationType);

}
