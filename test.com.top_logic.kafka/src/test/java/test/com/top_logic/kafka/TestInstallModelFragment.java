/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.kafka;

import static com.top_logic.basic.io.FileUtilities.*;

import junit.framework.Test;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.element.config.DefinitionReader;
import com.top_logic.element.config.ModelConfig;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.kafka.sync.knowledge.service.KafkaExportImportConfiguration;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.util.model.ModelService;

/**
 * {@link BasicTestCase} for model fragment import in a running application.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
public class TestInstallModelFragment extends BasicTestCase {

	public void testInstallFragment() {
		try (Transaction transaction = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			DynamicModelService.getInstance().installFragment(readFragmentConfig());
			transaction.commit();
		}
	}

	private ModelConfig readFragmentConfig() {
		return DefinitionReader.readElementConfig(getFragmentBinaryContent());
	}

	private BinaryContent getFragmentBinaryContent() {
		Class<?> type = TestInstallModelFragment.class;
		String fileName = type.getSimpleName() + XML_FILE_ENDING;
		return new ClassRelativeBinaryContent(type, fileName);
	}

	/** Setups the test within the TopLogic framework. */
	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(
			KBSetup.getSingleKBTest(ServiceTestSetup.createSetup(
				TestInstallModelFragment.class,
				ModelService.Module.INSTANCE,
				/* When the KafkaExportImportConfiguration is not started, the test does not find
				 * the problem. */
				KafkaExportImportConfiguration.Module.INSTANCE))
		);
	}
}
