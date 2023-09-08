/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.util.Set;

import com.top_logic.basic.config.annotation.ImplClasses;

/**
 * Provider that declares classes that can be instantiated using a {@link ConfigurationItem}
 * represented by a {@link ConfigurationDescriptor}.
 * 
 * <p>
 * A {@link ImplementationClassesProvider} is annotated to a configuration property through the
 * {@link ImplClasses} annotation.
 * </p>
 * 
 * @author <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public interface ImplementationClassesProvider {
    
    /**
     * Set of {@link Class}s that can be instantiated with a {@link ConfigurationItem} 
     * with a static type represented by its {@link ConfigurationDescriptor}.
     * 
     * <p>
     * All classes must declare a constructor that can be used from a {@link InstantiationContext}
     * </p>
     */
    Set getImplementationClasses(ConfigurationDescriptor descriptor);
}

