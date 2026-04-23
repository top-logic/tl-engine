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
		// Always clean up thread-local state after each test.
		TaskStoreBinding.setStore(null);
	}

	/**
	 * Verifies basic set and get of a {@link TaskStore}.
	 */
	public void testSetAndGetStore() {
		TaskStore store = new TaskStore();
		TaskStoreBinding.setStore(store);

		assertSame("Should return the same store that was set", store, TaskStoreBinding.getStore());
	}

	/**
	 * Verifies that setting {@code null} removes the binding, returning {@code null} on subsequent
	 * get.
	 */
	public void testSetNullRemovesBinding() {
		TaskStoreBinding.setStore(new TaskStore());
		TaskStoreBinding.setStore(null);

		assertNull("Store should be null after setting null", TaskStoreBinding.getStore());
	}

	/**
	 * Verifies that {@link TaskStoreBinding#getStore()} returns {@code null} when no store has been
	 * set on the current thread.
	 */
	public void testNoStoreReturnsNull() {
		assertNull("Should return null when no store is set", TaskStoreBinding.getStore());
	}

	/**
	 * Verifies that each thread has its own independent store binding. A store set on one thread
	 * must not be visible on another thread.
	 */
	public void testThreadIsolation() throws Exception {
		TaskStore mainStore = new TaskStore();
		TaskStoreBinding.setStore(mainStore);

		AtomicReference<TaskStore> otherThreadStore = new AtomicReference<>();
		Thread other = new Thread(() -> {
			otherThreadStore.set(TaskStoreBinding.getStore());
		});
		other.start();
		other.join(5000);

		assertNull("Other thread should not see main thread's store", otherThreadStore.get());
		assertSame("Main thread's store should be unaffected", mainStore, TaskStoreBinding.getStore());
	}

	/**
	 * Verifies that a store set on a child thread is independent from the main thread's store.
	 */
	public void testChildThreadSetsOwnStore() throws Exception {
		TaskStore mainStore = new TaskStore();
		TaskStoreBinding.setStore(mainStore);

		TaskStore childStore = new TaskStore();
		AtomicReference<TaskStore> retrievedOnChild = new AtomicReference<>();

		Thread child = new Thread(() -> {
			TaskStoreBinding.setStore(childStore);
			retrievedOnChild.set(TaskStoreBinding.getStore());
			TaskStoreBinding.setStore(null);
		});
		child.start();
		child.join(5000);

		assertSame("Child thread should see its own store", childStore, retrievedOnChild.get());
		assertSame("Main thread should still see its own store", mainStore, TaskStoreBinding.getStore());
	}
}
