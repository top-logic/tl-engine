/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.provider;

import java.util.Map;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ReflectionUtils;

import com.top_logic.basic.SingletonRegistry;

/**
 * The class {@link AbstractSingletonRegistryTest} is an abstract superclass to
 * test {@link SingletonRegistry}s
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractSingletonRegistryTest extends BasicTestCase {

	@Override
	protected final void setUp() throws Exception {
		super.setUp();

		internalSetUp();
		// must reload to clean cache of SingletonRegistry for test
		getSingletonRegistry().reload();
	}
	
	@Override
	protected void tearDown() throws Exception {
		getSingletonRegistry().reload();
		super.tearDown();
	}

	/**
	 * Setup for the TestCase. After executing this method,
	 * {@link #getSingletonRegistry()} must return a non <code>null</code>
	 * {@link SingletonRegistry}.
	 */
	protected void internalSetUp() throws Exception {
	}

	/**
	 * Returns the {@link SingletonRegistry} under test. As the setup reloads
	 * the {@link SingletonRegistry} it must be accessible after call of
	 * {@link #internalSetUp()}
	 * 
	 * @see #internalSetUp()
	 */
	protected abstract SingletonRegistry<?> getSingletonRegistry();
	
	public void testReload() {
		// Test whether reset works
		getSingletonRegistry().reload();
		final Object cache = ReflectionUtils.getValue(getSingletonRegistry(), "entryForKey");
		assertTrue("Cache was not reset.", ((Map<?,?>) cache).isEmpty());
	}
}
