/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.kafka.server.module;

import junit.framework.Test;

import test.com.top_logic.basic.NamedTestSetup;
import test.com.top_logic.basic.RearrangableTestSetup;

import com.top_logic.basic.col.MutableInteger;
import com.top_logic.basic.col.TupleFactory;
import com.top_logic.kafka.server.module.KafkaModule;

/**
 * {@link NamedTestSetup} creating a topic in Kafka.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CreateTopicSetup extends RearrangableTestSetup {

	private final String _topic;

	private static final MutableInteger SETUP_CNT = new MutableInteger();

	/**
	 * Creates a new {@link CreateTopicSetup}.
	 */
	public CreateTopicSetup(Test test, String topic) {
		super(test, SETUP_CNT);
		_topic = topic;
	}

	@Override
	public Object configKey() {
		return TupleFactory.newTuple(super.configKey(), _topic);
	}

	@Override
	protected void doSetUp() throws Exception {
		KafkaModule.Module.INSTANCE.getImplementationInstance().createTopic(_topic);
	}

	@Override
	protected void doTearDown() throws Exception {
		// Topic is not deleted
	}

}

