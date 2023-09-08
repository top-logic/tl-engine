/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values;

import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.customization.AnnotationCustomizations;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.form.values.edit.initializer.InitializerProvider;

/**
 * Access interface to various settings for creating an algorithm instance in a declarative form.
 * 
 * <p>
 * Most algorithms annotated to a declarative form property (such as {@link Options#fun()}) can
 * declare a constructor taking a {@link DeclarativeFormOptions} parameter to get access to the
 * context.
 * </p>
 * 
 * <p>
 * Through the {@link TypedAnnotatable} super interface, one can get access to all custom settings
 * implicitly passed to the form creation in
 * {@link EditorFactory#EditorFactory(com.top_logic.layout.form.values.edit.initializer.InitializerProvider)}
 * (the settings done to {@link InitializerProvider} that itself implements
 * {@link TypedAnnotatable}).
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface DeclarativeFormOptions extends TypedAnnotatable {

	/**
	 * The property being operated on.
	 */
	PropertyDescriptor getProperty();

	/**
	 * The global {@link AnnotationCustomizations} declared for the form creation.
	 * 
	 * @see EditorFactory#setCustomizations(AnnotationCustomizations)
	 */
	AnnotationCustomizations getCustomizations();

}
