/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge;

import junit.framework.Test;

import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.RearrangableThreadContextSetup;
import test.com.top_logic.knowledge.KBSetup.KBType;

import com.top_logic.basic.col.TupleFactory;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.knowledge.service.CreateTablesContext;
import com.top_logic.knowledge.service.DBSetupActions;
import com.top_logic.knowledge.service.DropTablesContext;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;

/**
 * Test setup creating tables for an application schema.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class KBTableSetup extends RearrangableThreadContextSetup {

	private static final MultipleSetupCounter SETUP_CNT = newMultipleCounter();

	private final KBType _type;

	/**
	 * Creates a {@link KBTableSetup}.
	 * 
	 * @param test
	 *        The test to wrap.
	 * @param type
	 *        The {@link KBType} to create tables for.
	 */
	public KBTableSetup(Test test, KBType type) {
		super(test, SETUP_CNT.getCounterFor(type));
		_type = type;
	}

	@Override
	public Object configKey() {
		return TupleFactory.newTuple(super.configKey(), _type);
	}

	@Override
	protected void doSetUp() throws Exception {
		if (KBSetup.shouldCreateTables()) {
			DBSetupActions setupAction = DBSetupActions.newInstance();
			setupAction.dropTables(new DropTablesContext(new AssertProtocol()), null);
			setupAction.createTables(new CreateTablesContext(new AssertProtocol()));
			/* Restart the KnowledgeBaseFactory to get rid of all Wrappers. */
			if (KnowledgeBaseFactory.Module.INSTANCE.isActive()) {
				ModuleUtil.INSTANCE.restart(KnowledgeBaseFactory.Module.INSTANCE, null);
			}
		}
	}

	@Override
	protected void doTearDown() throws Exception {
		// Ignore.
	}

}
