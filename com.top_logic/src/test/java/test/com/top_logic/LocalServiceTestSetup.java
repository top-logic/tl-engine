/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic;

import junit.framework.Test;
import test.com.top_logic.basic.TestSetupDecorator;
import test.com.top_logic.basic.module.ServiceStarter;

import com.top_logic.basic.module.BasicRuntimeModule;

/**
 * The class {@link LocalServiceTestSetup} is an adaptor to start some module within a
 * {@link LocalTestSetup}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class LocalServiceTestSetup extends LocalTestSetup {

	private ServiceStarter _serviceStarter;

	/**
	 * Creates a undecorated {@link LocalServiceTestSetup}.
	 * 
	 * @see #LocalServiceTestSetup(TestSetupDecorator, Test, BasicRuntimeModule)
	 */
	public LocalServiceTestSetup(Test test, BasicRuntimeModule<?> service) {
		this(null, test, service);
	}

	public LocalServiceTestSetup(TestSetupDecorator decorator, Test test, BasicRuntimeModule<?> service) {
		this(decorator, test, new ServiceStarter(service));
	}

	/**
	 * Creates a undecorated {@link LocalServiceTestSetup}.
	 * 
	 * @see #LocalServiceTestSetup(TestSetupDecorator, Test, ServiceStarter)
	 */
	public LocalServiceTestSetup(Test test, ServiceStarter starter) {
		this(null, test, starter);
	}

	public LocalServiceTestSetup(TestSetupDecorator decorator, Test test, ServiceStarter starter) {
		super(decorator, test);
		_serviceStarter = starter;
	}

	@Override
	protected void setUpLocal() throws Exception {
		super.setUpLocal();
		_serviceStarter.startService();
	}

	@Override
	protected void tearDownLocal() throws Exception {
		_serviceStarter.stopService();
		super.tearDownLocal();
	}

}

