/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.kafka;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.kafka.server.module.KafkaModule;

/**
 * Setup starting {@link KafkaModule}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class KafkaTestSetup {

	/**
	 * a cumulative {@link Test} for all Tests in {@link KafkaTestSetup}.
	 */
	public static Test suite(Class<? extends Test> test) {
		return suite(new TestSuite(test));
	}

	/**
	 * Wraps a {@link TestSuite} around the test which starts (and afterwards stops) Kafka and
	 * <i>TopLogic</i>.
	 */
	public static Test suite(Test test) {
		test = startKafka(test);
		test = TLTestSetup.createTLTestSetup(test);
		return test;
	}

	/** Wraps a {@link TestSuite} around the test which starts (and afterwards stops) Kafka. */
	public static Test startKafka(Test test) {
		return ServiceTestSetup.createSetup(test, KafkaModule.Module.INSTANCE);
	}

}
