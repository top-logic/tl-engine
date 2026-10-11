/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.views;

import java.io.File;
import java.util.List;

import static com.top_logic.knowledge.service.KBUtils.inTransaction;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.ai.views.ViewChanges.Change;
import com.top_logic.ai.views.ViewChanges.RefusedException;
import com.top_logic.layout.view.command.CommandCliqueService;
import com.top_logic.model.TLObject;
import com.top_logic.util.model.ModelService;

/**
 * View changes are checked before anything is written, recorded with the text before and after,
 * and put back exactly.
 *
 * <p>
 * The views are real files of this module's test workspace, under {@code test-changes/}, removed
 * after every case.
 * </p>
 */
public class TestViewChanges extends BasicTestCase {

	private static final String VIEW = ViewFiles.resource("test-changes/page.view.xml");

	private static final String OTHER = ViewFiles.resource("test-changes/other.view.xml");

	private static final String TEXT = """
		<view>
			<!-- Kept through every change. -->
			<panel>
				<title>
					<en>Before</en>
				</title>
			</panel>
		</view>
		""";

	@Override
	protected void tearDown() throws Exception {
		for (String resource : List.of(VIEW, OTHER)) {
			File file = ViewFiles.file(resource);
			if (file != null) {
				file.delete();
				file.getParentFile().delete();
			}
		}
		super.tearDown();
	}

	/** A write creates the view and records it as an applied set; reverting deletes it again. */
	public void testWriteIsRecordedAndReverted() {
		TLObject set = inTransaction(() -> ViewChanges.write("Create", "tester", List.of(new Change(VIEW, TEXT))));

		assertEquals(ViewChanges.APPLIED, ViewChanges.status(set));
		assertEquals(TEXT, ViewFiles.read(VIEW));

		List<String> written = inTransaction(() -> ViewChanges.revert(set));

		assertEquals(List.of(VIEW), written);
		assertEquals(ViewChanges.REVERTED, ViewChanges.status(set));
		assertNull(ViewFiles.read(VIEW));
	}

	/** A view that would not work is refused, and nothing is written or recorded. */
	public void testABrokenViewIsNotWritten() {
		int before = ViewChanges.all().size();
		try {
			inTransaction(() -> ViewChanges.write("Broken", "tester", List.of(new Change(VIEW, "<view><panel></view>"))));
			fail("The text is not well-formed.");
		} catch (RefusedException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains("test-changes/page.view.xml:1"));
		}
		assertNull(ViewFiles.read(VIEW));
		assertEquals(before, ViewChanges.all().size());
	}

	/** A view changed after a change is a conflict: reverting the change would lose the newer one. */
	public void testARevertRefusesAViewChangedSince() {
		TLObject first = inTransaction(() -> ViewChanges.write("Create", "tester", List.of(new Change(VIEW, TEXT))));
		String meanwhile = TEXT.replace("Before", "Meanwhile");
		inTransaction(() -> ViewChanges.write("Meanwhile", "someone", List.of(new Change(VIEW, meanwhile))));

		try {
			inTransaction(() -> ViewChanges.revert(first));
			fail("The view changed since.");
		} catch (RefusedException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains("was changed after"));
		}
		assertEquals(meanwhile, ViewFiles.read(VIEW));
		assertEquals(ViewChanges.APPLIED, ViewChanges.status(first));
	}

	/** Two views of one change may refer to each other although neither is written yet. */
	public void testViewsOfOneChangeMayReferToEachOther() {
		String embedding = "<view><view-ref view=\"test-changes/other.view.xml\"/></view>";

		inTransaction(() -> ViewChanges.write("Two", "tester",
			List.of(new Change(VIEW, embedding), new Change(OTHER, TEXT))));

		assertEquals(embedding, ViewFiles.read(VIEW));
		assertEquals(TEXT, ViewFiles.read(OTHER));
	}

	/**
	 * The suite: a knowledge base with the change set model.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(ServiceTestSetup.createSetup(new TestSuite(TestViewChanges.class),
			ModelService.Module.INSTANCE, CommandCliqueService.Module.INSTANCE));
	}
}
