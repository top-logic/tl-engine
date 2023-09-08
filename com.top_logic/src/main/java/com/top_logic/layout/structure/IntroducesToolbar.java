/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Annotation for {@link LayoutControlProvider}s that explicitly introduce toolbars.
 * 
 * <p>
 * A toolbar of a component {@link LayoutComponent#getComponentControlProvider() using} a
 * {@link LayoutControlProvider} annotated with {@link IntroducesToolbar} is not automatically moved
 * to its surrounding layout, even if it is embedded only in a technical layout.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface IntroducesToolbar {

	// Marker annotation.

}
