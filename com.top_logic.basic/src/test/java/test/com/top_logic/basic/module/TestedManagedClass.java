/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.module;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.module.ManagedClass;

/**
 * The class {@link TestedManagedClass} is a dummy {@link ManagedClass} for tests
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestedManagedClass extends AbstractTestedManagedClass {

	/** Whether this instance is created using a factory method. */
	public boolean _createdViaFactory;

	/**
	 * The class {@link TestServiceConfiguration} is the
	 * {@link com.top_logic.basic.module.ManagedClass.ServiceConfiguration} of a
	 * {@link TestedManagedClass}
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static interface TestServiceConfiguration extends AbstractTestedManagedClass.Config {
		// no special content
	}

	/**
	 * Creates a {@link TestedManagedClass} from the given configuration.
	 */
	public TestedManagedClass(InstantiationContext context, TestServiceConfiguration config) {
		super(context, config);
	}

	/**
	 * Creates a {@link TestedManagedClass}
	 */
	public static TestedManagedClass newInstance(InstantiationContext context, TestServiceConfiguration config) {
		TestedManagedClass testedManagedClass = new TestedManagedClass(context, config);
		testedManagedClass._createdViaFactory = true;
		return testedManagedClass;
	}

	/**
	 * Creates a {@link TestedManagedClass}.
	 */
	public TestedManagedClass() {
	}

}
