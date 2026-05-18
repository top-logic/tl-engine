/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.lang.reflect.Method;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.tools.interaction.TaskTool;
import com.top_logic.tl.ai.core.tools.model.ModelEditorTool;
import com.top_logic.tl.ai.tool.api.AnnotatedTools;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolMode;

/**
 * Tests for object-level locking rework.
 *
 * <p>Verifies {@link LockConflictException} formatting, {@link TransactionScope} NOOP behavior,
 * lock tracking, tool mode reclassification, and locking parameter presence on model editor
 * methods.</p>
 */
public class TestObjectLevelLocking extends TestCase {

	/**
	 * {@link LockConflictException} message contains resource, holder, retry count, and
	 * do-not-retry instruction.
	 */
	public void testLockConflictMessage() {
		LockConflictException exception =
			new LockConflictException("test.locking:Customer", "root", 3, 20, new RuntimeException("locked"));

		assertEquals("test.locking:Customer", exception.resource());
		assertEquals("root", exception.holder());
		assertTrue(exception.getMessage().contains("LOCK_CONFLICT"));
		assertTrue(exception.getMessage().contains("test.locking:Customer"));
		assertTrue(exception.getMessage().contains("root"));
		assertTrue(exception.getMessage().contains("Do NOT retry"));
		assertTrue(exception.getMessage().contains("3 times over 20 seconds"));
	}

	/**
	 * NOOP scope accepts {@code acquireLock} without error and reports no lock held.
	 */
	public void testNoOpAcquireLock() {
		TransactionScope.NOOP.acquireLock("editValues");
		assertFalse(TransactionScope.NOOP.holdsLock());
	}

	/**
	 * Multiple lock acquisitions accumulate; release clears all.
	 */
	public void testMultipleLockRelease() {
		LockTrackingScope scope = new LockTrackingScope();

		scope.acquireLock("editValues");
		scope.acquireLock("editValues");

		assertEquals(2, scope.lockCount);
		assertTrue(scope.holdsLock());

		scope.releaseLock();

		assertEquals(0, scope.lockCount);
		assertFalse(scope.holdsLock());
	}

	/**
	 * {@link TaskTool} methods are classified as {@link ToolMode#USER_SCOPED} or
<<<<<<< CWS/CWS_ArchitectureRefinements
	 * {@link ToolMode#READ}, never {@link ToolMode#DATA_WRITE} or {@link ToolMode#STRUCTURAL}.
=======
	 * {@link ToolMode#READ}, never {@link ToolMode#STRUCTURAL}.
>>>>>>> 6941e23 Refine message layer: tighten PdfContent validation, add missing tests.
	 */
	public void testReclassifiedModes() {
		TaskTool taskTool = new TaskTool();
		List<Tool> tools = AnnotatedTools.from(taskTool);

		assertFalse("TaskTool should expose at least one tool", tools.isEmpty());

		for (Tool tool : tools) {
			assertNotNull("Tool mode must not be null for " + tool.name(), tool.mode());
			assertTrue(
				"TaskTool." + tool.name() + " should be USER_SCOPED or READ but was " + tool.mode(),
				tool.mode() == ToolMode.USER_SCOPED || tool.mode() == ToolMode.READ);
		}
	}

	/**
	 * Create methods on {@link ModelEditorTool} do not accept {@link ToolContext} (no locking).
	 * Update methods do accept {@link ToolContext} (participate in locking).
	 */
	public void testCreateToolsNoContext() throws Exception {
		// createModule has no ToolContext parameter
		Method createModule = ModelEditorTool.class.getDeclaredMethod("createModule",
			String.class, String.class, String.class, String.class, String.class);
		assertNotNull(createModule);
		assertFalse("createModule should not have ToolContext parameter",
			hasToolContextParameter(createModule));

		// createClass has no ToolContext parameter
		Method createClass = ModelEditorTool.class.getDeclaredMethod("createClass",
			String.class, String.class, String.class, String.class,
			String.class, String.class, Boolean.class, Boolean.class, String.class);
		assertNotNull(createClass);
		assertFalse("createClass should not have ToolContext parameter",
			hasToolContextParameter(createClass));

		// createProperty has no ToolContext parameter
		Method createProperty = ModelEditorTool.class.getDeclaredMethod("createProperty",
			String.class, String.class, String.class, String.class,
			String.class, String.class, String.class, String.class,
			Boolean.class, Boolean.class, Boolean.class, Boolean.class, Boolean.class);
		assertNotNull(createProperty);
		assertFalse("createProperty should not have ToolContext parameter",
			hasToolContextParameter(createProperty));

		// updateClass DOES have ToolContext as last parameter
		boolean hasContext = false;
		for (Method m : ModelEditorTool.class.getDeclaredMethods()) {
			if (m.getName().equals("updateClass")) {
				if (hasToolContextParameter(m)) {
					hasContext = true;
				}
			}
		}
		assertTrue("updateClass should have ToolContext parameter", hasContext);
	}

	/**
	 * {@link LockConflictException} extends {@link TransactionException} which extends
	 * {@link RuntimeException}.
	 */
	public void testLockConflictExceptionInheritance() {
		LockConflictException exception =
			new LockConflictException("res", "holder", 1, 10, new RuntimeException("cause"));

		assertTrue("LockConflictException should be a TransactionException",
			exception instanceof TransactionException);
		assertTrue("LockConflictException should be a RuntimeException",
			exception instanceof RuntimeException);
	}

	private static boolean hasToolContextParameter(Method method) {
		for (Class<?> paramType : method.getParameterTypes()) {
			if (ToolContext.class.isAssignableFrom(paramType)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Test subclass that tracks lock state without {@code LockService}.
	 */
	private static class LockTrackingScope extends TransactionScope {

		int lockCount = 0;

		/** */
		@Override
		public void acquireLock(String operation, TLObject... targets) {
			lockCount++;
		}

		/** */
		@Override
		public void releaseLock() {
			lockCount = 0;
		}

		/** */
		@Override
		public boolean holdsLock() {
			return lockCount > 0;
		}

		/** */
		@Override
		public void begin() {
			// no-op
		}

		/** */
		@Override
		public void begin(ResKey msg) {
			// no-op
		}

		/** */
		@Override
		public void commit() {
			releaseLock();
		}

		/** */
		@Override
		public void rollback() {
			releaseLock();
		}

		/** */
		@Override
		public boolean isActive() {
			return false;
		}

		/** */
		@Override
		public boolean isNoOp() {
			return false;
		}

		/** */
		@Override
		public void close() {
			rollback();
		}
	}
}
