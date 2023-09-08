/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.kafka.services;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.model.TLObject;

/**
 * {@link TestCase} for changing an object and deleting it before TLSync can send it.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
public class TestTLSyncChangeAndDeleteBeforeSend extends AbstractTLSyncTest {

	private static final String SOURCE_MODULE_NAME = TestTLSyncChangeAndDeleteBeforeSend.class.getName() + ".source";

	private static final String TARGET_MODULE_NAME = TestTLSyncChangeAndDeleteBeforeSend.class.getName() + ".target";

	private static final String SOURCE_TYPE_NAME = "SourceType";

	private static final String TARGET_TYPE_NAME = "TargetType";

	private static final String TEST_REFERENCE_NAME = "testReference";

	public void testChangeAndDelete() throws Exception {
		TLObject target = sync(() -> createObject());
		TLObject source = sync(() -> createObject());
		try {
			runWithKafka(() -> {
				KBUtils.inTransaction(() -> source.tUpdateByName(TEST_REFERENCE_NAME, target));
				KBUtils.inTransaction(() -> source.tDelete());
			});
		} finally {
			/* If the test fails, TL-Sync is broken at this point and can no longer be used to send
			 * the deletion. Therefore, delete the transmitted object explicitly in the receiving
			 * system. */
			findTargetObject(target).tDelete();
			target.tDelete();
		}
	}

	private TLObject findTargetObject(TLObject source) {
		return findReceivedObjectFor(type(TARGET_MODULE_NAME, TARGET_TYPE_NAME), source);
	}

	private TLObject createObject() {
		return newObject(SOURCE_MODULE_NAME, SOURCE_TYPE_NAME);
	}

	/**
	 * Creates a {@link TestSuite} for all the tests in {@link TestTLSyncChangeAndDeleteBeforeSend}.
	 */
	public static Test suite() {
		return suite(TestTLSyncChangeAndDeleteBeforeSend.class);
	}

}
