/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.basic;

import junit.framework.Test;

import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.CommandModelFactory;
import com.top_logic.layout.basic.ComponentCommandModel;

/**
 * The class {@link TestComponentCommandModel} tests the {@link ComponentCommandModel}
 * 
 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestComponentCommandModel extends CommandHandlerCommandModelTest<CommandModel> {
	
	private CommandModel model;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		model = CommandModelFactory.commandModel(commandHandler, component);
		addListener(model, listener);
	}
	
	@Override
	protected void tearDown() throws Exception {
		removeListener(model, listener);
		model = null;
		super.tearDown();
	}

	@Override
	protected CommandModel getCommandModel() {
		return model;
	}

	/**
	 * Test suite.
	 */
	public static Test suite() {
		return suite(TestComponentCommandModel.class);
	}
}

