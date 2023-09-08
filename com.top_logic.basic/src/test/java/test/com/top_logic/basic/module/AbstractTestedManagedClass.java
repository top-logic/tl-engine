/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.module;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.module.ManagedClass;

/**
 * Abstract {@link ManagedClass} to test that modules also works with abstract service classes.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractTestedManagedClass extends ManagedClass {

	/**
	 * Configuration for an {@link AbstractTestedManagedClass}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends ServiceConfiguration<AbstractTestedManagedClass> {
		// No content here
	}

	/**
	 * Creates a new {@link AbstractTestedManagedClass}.
	 * 
	 * @see ManagedClass#ManagedClass()
	 */
	public AbstractTestedManagedClass() {
		super();
	}

	/**
	 * Creates a new {@link AbstractTestedManagedClass}.
	 * 
	 * @see ManagedClass#ManagedClass(InstantiationContext, Config)
	 */
	public AbstractTestedManagedClass(InstantiationContext context, Config config) {
		super(context, config);
	}

}

