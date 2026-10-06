/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.providers;

import java.util.Arrays;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.model.search.providers.DropTargetByExpressionConfig;
import com.top_logic.model.search.providers.I18NConstants;
import com.top_logic.model.search.providers.TableDropTargetByExpression;

/**
 * Test for the commit message of a drop built by
 * {@link DropTargetByExpressionConfig#buildCommitMessage(java.util.Collection, Object)}.
 */
@SuppressWarnings("javadoc")
public class TestDropCommitMessage extends BasicTestCase {

	public void testDefaultMessageWithTarget() {
		ResKey message = config().buildCommitMessage(List.of("A"), "T");

		assertMessage(I18NConstants.DROPPED__OBJECTS_TARGET.fill(null, null), message, "A", "T");
	}

	public void testDefaultMessageWithoutTarget() {
		ResKey message = config().buildCommitMessage(List.of("A"), null);

		assertMessage(I18NConstants.DROPPED__OBJECTS.fill(null), message, "A");
	}

	public void testMultipleDroppedObjectsJoined() {
		ResKey message = config().buildCommitMessage(Arrays.asList("A", "B", "C"), "T");

		assertMessage(I18NConstants.DROPPED__OBJECTS_TARGET.fill(null, null), message, "A, B, C", "T");
	}

	public void testCustomMessage() {
		ResKey custom = ResKey.forTest("test.drop.custom");
		DropTargetByExpressionConfig config = config();
		TypedConfigUtil.setProperty(config, DropTargetByExpressionConfig.COMMIT_MESSAGE, (ResKey2) custom);

		ResKey message = config.buildCommitMessage(Arrays.asList("A", "B"), "T");

		assertMessage(custom, message, "A, B", "T");
	}

	public void testCustomMessageWithoutTarget() {
		ResKey custom = ResKey.forTest("test.drop.custom");
		DropTargetByExpressionConfig config = config();
		TypedConfigUtil.setProperty(config, DropTargetByExpressionConfig.COMMIT_MESSAGE, (ResKey2) custom);

		ResKey message = config.buildCommitMessage(List.of("A"), null);

		assertEquals(custom.plain(), message.plain());
		assertEquals("A", message.arguments()[0]);
	}

	private static void assertMessage(ResKey expectedKey, ResKey message, Object... expectedArguments) {
		assertEquals(expectedKey.plain(), message.plain());
		assertEquals(Arrays.asList(expectedArguments), Arrays.asList(message.arguments()));
	}

	private static DropTargetByExpressionConfig config() {
		return TypedConfiguration.newConfigItem(TableDropTargetByExpression.Config.class);
	}

	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(TestDropCommitMessage.class, LabelProviderService.Module.INSTANCE));
	}

}
