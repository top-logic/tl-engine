/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.kafka.server.module;

import java.io.File;
import java.util.List;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.kafka.server.module.KafkaModule;

import kafka.server.KafkaConfig;
import scala.collection.JavaConversions;

/**
 * Extension of {@link KafkaModule} for tests.
 * 
 * <p>
 * This {@link KafkaModule} removes the currently stored data before startup.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestKafkaModule extends KafkaModule {

	/**
	 * Creates a new {@link TestKafkaModule}.
	 */
	public TestKafkaModule(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected void startUp() {
		removeKafkaData(getKafkaConfig());
		super.startUp();
	}

	private void removeKafkaData(KafkaConfig kafkaConfig) {
		List<String> logDirs = JavaConversions.seqAsJavaList(kafkaConfig.logDirs());
		for (String logDir : logDirs) {
			FileUtilities.deleteR(new File(logDir));
		}
	}

}

