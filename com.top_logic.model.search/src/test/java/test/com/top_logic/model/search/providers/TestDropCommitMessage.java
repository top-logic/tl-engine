/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.providers;

import java.util.Arrays;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.model.search.expr.AbstractSearchExpressionTest;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.providers.DropCommitMessage;
import com.top_logic.model.search.providers.DropTargetByExpressionConfig;
import com.top_logic.model.search.providers.I18NConstants;
import com.top_logic.model.search.providers.TableDropTargetByExpression;

/**
 * Test for {@link DropCommitMessage}.
 */
@SuppressWarnings("javadoc")
public class TestDropCommitMessage extends AbstractSearchExpressionTest {

	private static final ResKey TITLE = ResKey.forTest("test.drop.component.title");

	public void testDefaultMessage() throws Exception {
		ResKey message = noScript().create(List.of("A"), Args.some("T"), "M", TITLE);

		assertEquals(I18NConstants.DROPPED__OBJECTS_COMPONENT.fill(null, null).plain(), message.plain());
		assertEquals(Arrays.asList("A", TITLE), Arrays.asList(message.arguments()));
	}

	public void testDefaultMessageJoinsDroppedObjects() throws Exception {
		ResKey message = noScript().create(Arrays.asList("A", "B", "C"), Args.some((Object) null), "M", TITLE);

		assertEquals(I18NConstants.DROPPED__OBJECTS_COMPONENT.fill(null, null).plain(), message.plain());
		assertEquals(Arrays.asList("A, B, C", TITLE), Arrays.asList(message.arguments()));
	}

	public void testScriptReceivesDropArgumentsAndModel() throws Exception {
		DropCommitMessage commitMessage = script("d -> r -> m -> toString($d.size(), '/', $r, '/', $m)");

		ResKey message = commitMessage.create(Arrays.asList("A", "B"), Args.some("T"), "M", TITLE);

		assertEquals(ResKey.text("2/T/M"), message);
	}

	public void testScriptReceivesAllTreeDropArguments() throws Exception {
		DropCommitMessage commitMessage = script("d -> p -> r -> m -> toString($d.size(), '/', $p, '/', $r, '/', $m)");

		ResKey message = commitMessage.create(List.of("A"), Args.some("P", "R"), "M", TITLE);

		assertEquals(ResKey.text("1/P/R/M"), message);
	}

	public void testScriptResultResKeyUsedAsIs() throws Exception {
		DropCommitMessage commitMessage = script("d -> r -> m -> $m");

		ResKey message = commitMessage.create(List.of("A"), Args.some("T"), TITLE, null);

		assertSame(TITLE, message);
	}

	public void testScriptWithoutResultUsesDefault() throws Exception {
		DropCommitMessage commitMessage = script("d -> r -> m -> null");

		ResKey message = commitMessage.create(List.of("A"), Args.some("T"), "M", TITLE);

		assertEquals(I18NConstants.DROPPED__OBJECTS_COMPONENT.fill(null, null).plain(), message.plain());
		assertEquals(Arrays.asList("A", TITLE), Arrays.asList(message.arguments()));
	}

	private static DropCommitMessage noScript() {
		return new DropCommitMessage(config());
	}

	private static DropCommitMessage script(String expr) throws Exception {
		DropTargetByExpressionConfig config = config();
		TypedConfigUtil.setProperty(config, DropTargetByExpressionConfig.COMMIT_MESSAGE, parse(expr));
		return new DropCommitMessage(config);
	}

	private static DropTargetByExpressionConfig config() {
		return TypedConfiguration.newConfigItem(TableDropTargetByExpression.Config.class);
	}

	public static Test suite() {
		return KBSetup.getSingleKBTest(ServiceTestSetup.createSetup(TestDropCommitMessage.class, getModules()));
	}

}
