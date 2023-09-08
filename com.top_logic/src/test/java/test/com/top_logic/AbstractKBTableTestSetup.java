/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic;

import junit.framework.Test;
import test.com.top_logic.basic.AbstractTableTestSetup;
import test.com.top_logic.basic.NestableTestSetup;
import test.com.top_logic.basic.TestSetupDecorator;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.col.MutableInteger;

/**
 * {@link AbstractTableTestSetup} that decides upon
 * {@link KBSetup#shouldCreateTables()} whehter thables should be reset.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractKBTableTestSetup extends AbstractTableTestSetup {

	/**
	 * Creates a {@link AbstractKBTableTestSetup}.
	 *
	 * @param test See {@link NestableTestSetup#NestableTestSetup(TestSetupDecorator, Test, MutableInteger)}
	 * @param setupCnt See {@link NestableTestSetup#NestableTestSetup(TestSetupDecorator, Test, MutableInteger)}
	 */
	public AbstractKBTableTestSetup(Test test, MutableInteger setupCnt) {
		super(test, setupCnt);
	}
	
	@Override
	public void doSetUp() throws Exception {
		if (KBSetup.shouldCreateTables()) {
			super.doSetUp();
		}
	}
	
	@Override
	public void doTearDown() throws Exception {
		if (KBSetup.shouldCreateTables()) {
			super.doTearDown();
		}
	}

}
