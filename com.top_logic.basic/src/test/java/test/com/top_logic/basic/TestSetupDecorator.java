/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;


/**
 * Enhancement to a {@link DecoratedTestSetup}.
 * 
 * @see #setup(SetupAction)
 * @see #tearDown(SetupAction)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TestSetupDecorator {

	/**
	 * Callback interface that exports the inner setup action for a
	 * {@link TestSetupDecorator}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public interface SetupAction {
		
		/**
		 * The core setup action.
		 */
		void setUpDecorated() throws Exception;
		
		/**
		 * The core tear down action.
		 */
		void tearDownDecorated() throws Exception;

		/**
		 * The actual decorated test setup
		 */
		DecoratedTestSetup decoratedTest();
	}

	/**
	 * Action that is wrapped around the inner setup action.
	 * 
	 * <p>
	 * The implementation must call {@link SetupAction#setUpDecorated()} to
	 * executed the wrapped setup.
	 * </p>
	 * 
	 * @param innerSetup
	 *        The wrapped setup that must be invoked from this setup decoration.
	 */
	void setup(SetupAction innerSetup) throws Exception;

	/**
	 * Action that is wrapped around the inner tear down action.
	 * 
	 * <p>
	 * The implementation must call {@link SetupAction#tearDownDecorated()} to
	 * executed the wrapped tear down.
	 * </p>
	 * 
	 * @param innerSetup
	 *        The wrapped setup that must be invoked from this setup decoration.
	 */
	void tearDown(SetupAction innerSetup) throws Exception;

}
