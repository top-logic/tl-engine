/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.kafka.server.module;

import java.io.File;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.basic.RearrangableTestSetup;

import com.top_logic.basic.col.MutableInteger;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.kafka.server.module.KafkaModule;

import kafka.Kafka;
import scala.collection.JavaConverters;

/**
 * {@link RearrangableTestSetup} removing {@link Kafka} data.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class RemoveKafkaFiles extends RearrangableTestSetup {

	private static final MutableInteger SETUP_CNT = new MutableInteger();

	/**
	 * Creates a new {@link RemoveKafkaFiles}.
	 */
	public RemoveKafkaFiles(Test test) {
		super(test, SETUP_CNT);
	}

	@Override
	protected void doSetUp() throws Exception {
		KafkaModule kafkaModule = KafkaModule.Module.INSTANCE.getImplementationInstance();
		List<String> logDirs = JavaConverters.asJava(kafkaModule.getKafkaConfig().logDirs());
		for (String logDir : logDirs) {
			FileUtilities.deleteR(new File(logDir));
		}
	}

	@Override
	protected void doTearDown() throws Exception {
		// Kafka files are removed on startup.
	}

}

