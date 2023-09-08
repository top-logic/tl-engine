/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.util.Computation;

/**
 * Service class to create {@link TestSetupDecorator} that starts {@link BasicRuntimeModule} for the
 * setup process.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ModuleContextDecorator {

	private static final class SingleModule implements TestSetupDecorator {

		private final BasicRuntimeModule<?> desiredService;

		SingleModule(BasicRuntimeModule<?> desiredService) {
			this.desiredService = desiredService;
		}

		@Override
		public void setup(final SetupAction innerSetup) throws Exception {
			Exception thrownException = ModuleUtil.INSTANCE.inModuleContext(desiredService, new Computation<Exception>() {

				@Override
				public Exception run() {
					try {
						innerSetup.setUpDecorated();
					} catch (Exception ex) {
						return ex;
					}
					return null;
				}
			});

			if (thrownException != null) {
				throw thrownException;
			}
		}

		@Override
		public void tearDown(final SetupAction innerSetup) throws Exception {
			Exception thrownException = ModuleUtil.INSTANCE.inModuleContext(desiredService, new Computation<Exception>() {

				@Override
				public Exception run() {
					try {
						innerSetup.tearDownDecorated();
					} catch (Exception ex) {
						return ex;
					}
					return null;
				}
			});

			if (thrownException != null) {
				throw thrownException;
			}
		}
	}

	private static final class MultiModule implements TestSetupDecorator {
		
		private final BasicRuntimeModule<?>[] desiredServices;
		
		MultiModule(BasicRuntimeModule<?>[] desiredServices) {
			this.desiredServices = desiredServices;
		}
		
		@Override
		public void setup(final SetupAction innerSetup) throws Exception {
			Exception thrownException = ModuleUtil.INSTANCE.inModuleContext(new Computation<Exception>() {
				
				@Override
				public Exception run() {
					try {
						innerSetup.setUpDecorated();
					} catch (Exception ex) {
						return ex;
					}
					return null;
				}
			}, desiredServices);
			
			if (thrownException != null) {
				throw thrownException;
			}
		}
		
		@Override
		public void tearDown(final SetupAction innerSetup) throws Exception {
			Exception thrownException = ModuleUtil.INSTANCE.inModuleContext(new Computation<Exception>() {
				
				@Override
				public Exception run() {
					try {
						innerSetup.tearDownDecorated();
					} catch (Exception ex) {
						return ex;
					}
					return null;
				}
			}, desiredServices);
			
			if (thrownException != null) {
				throw thrownException;
			}
		}
	}
	
	private static final class SenselessDecorator implements TestSetupDecorator {

		public static final SenselessDecorator INSTANCE = new SenselessDecorator();

		private SenselessDecorator() {
			// singleton instance
		}

		@Override
		public void setup(SetupAction innerSetup) throws Exception {
			innerSetup.setUpDecorated();
		}

		@Override
		public void tearDown(SetupAction innerSetup) throws Exception {
			innerSetup.tearDownDecorated();
		}

	}

	/**
	 * Creates a {@link TestSetupDecorator} that starts the given service for the setup and teardown
	 * process.
	 * 
	 * @param desiredService
	 *        The service which is needed during setup.
	 */
	public static TestSetupDecorator createSingleModuleDecorator(BasicRuntimeModule<?> desiredService) {
		return new SingleModule(desiredService);
	}

	/**
	 * Creates a {@link TestSetupDecorator} that starts the given services for the setup and
	 * teardown process.
	 * 
	 * @param desiredServices
	 *        The services which are needed during setup.
	 */
	public static TestSetupDecorator createMultiModulesDecorator(BasicRuntimeModule<?>... desiredServices) {
		if (desiredServices == null) {
			return SenselessDecorator.INSTANCE;
		}
		if (desiredServices.length == 1) {
			return createSingleModuleDecorator(desiredServices[0]);
		}

		return new MultiModule(desiredServices);
	}
	
}
