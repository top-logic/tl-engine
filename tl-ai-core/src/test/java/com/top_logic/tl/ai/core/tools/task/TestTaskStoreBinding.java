/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.task;

import java.util.concurrent.atomic.AtomicReference;

import junit.framework.TestCase;

/**
 * Tests for {@link TaskStoreBinding} covering thread-local binding, cleanup, and thread isolation.
 */
public class TestTaskStoreBinding extends TestCase {

	@Override
	protected void tearDown() {
		// Defensive: in case a test leaks state.
		try (TaskStoreBinding.Binding b = TaskStoreBinding.bind(null)) {
			// nothing
		}
	}

	public void testBindAndGetStore() {
		TaskStore store = new TaskStore();
		try (TaskStoreBinding.Binding b = TaskStoreBinding.bind(store)) {
			assertSame(store, TaskStoreBinding.getStore());
		}
	}

	public void testNoStoreReturnsNull() {
		assertNull(TaskStoreBinding.getStore());
	}

	public void testThreadIsolation() throws Exception {
		TaskStore mainStore = new TaskStore();
		try (TaskStoreBinding.Binding b = TaskStoreBinding.bind(mainStore)) {
			AtomicReference<TaskStore> otherThreadStore = new AtomicReference<>();
			Thread other = new Thread(() -> otherThreadStore.set(TaskStoreBinding.getStore()));
			other.start();
			other.join(5000);
			assertNull(otherThreadStore.get());
			assertSame(mainStore, TaskStoreBinding.getStore());
		}
	}

	public void testBindRestoresAbsence() {
		assertNull(TaskStoreBinding.getStore());
		TaskStore store = new TaskStore();
		try (TaskStoreBinding.Binding b = TaskStoreBinding.bind(store)) {
			assertSame(store, TaskStoreBinding.getStore());
		}
		assertNull(TaskStoreBinding.getStore());
	}

	public void testBindRestoresPriorValue() {
		TaskStore outer = new TaskStore();
		try (TaskStoreBinding.Binding b1 = TaskStoreBinding.bind(outer)) {
			TaskStore inner = new TaskStore();
			try (TaskStoreBinding.Binding b2 = TaskStoreBinding.bind(inner)) {
				assertSame(inner, TaskStoreBinding.getStore());
			}
			assertSame(outer, TaskStoreBinding.getStore());
		}
	}

	public void testBindRestoresOnException() {
		TaskStore outer = new TaskStore();
		try (TaskStoreBinding.Binding b1 = TaskStoreBinding.bind(outer)) {
			try (TaskStoreBinding.Binding b2 = TaskStoreBinding.bind(new TaskStore())) {
				throw new RuntimeException("boom");
			} catch (RuntimeException expected) {
				// expected
			}
			assertSame(outer, TaskStoreBinding.getStore());
		}
	}
}
