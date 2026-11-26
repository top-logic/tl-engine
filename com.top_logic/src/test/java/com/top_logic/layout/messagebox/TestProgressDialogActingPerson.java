/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.messagebox;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.basic.i18n.log.I18NLog;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.thread.ThreadLocalBinding;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.util.ActingPerson;
import com.top_logic.util.TLContext;
import com.top_logic.util.TLContextManager;

/**
 * Tests that the task of a {@link ProgressDialog} acts for the {@link ActingPerson} of the request
 * that opened the dialog.
 */
public class TestProgressDialogActingPerson extends BasicTestCase {

	private static final long TIMEOUT_SECONDS = 60;

	private Person _person;

	private TLContext _window;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_person = TestPerson.createPerson("progressDialogUser");

		ThreadContextManager manager = TLContextManager.getManager();
		_window = (TLContext) manager.newSubSessionContext();
		_window.setSessionContext(manager.newSessionContext());
		_window.setPerson(_person);
	}

	@Override
	protected void tearDown() throws Exception {
		TestPerson.deletePersonAndUser(_person);
		_person = null;
		_window = null;
		super.tearDown();
	}

	/** The task acts for the person who opened the dialog in their window. */
	public void testTaskActsForThePersonWhoOpenedTheDialog() throws Exception {
		assertSame(_person, actingPersonOfTask(_person));
	}

	/** A dialog opened in a request without an acting person starts a task without one. */
	public void testTaskWithoutActingPerson() throws Exception {
		assertNull(actingPersonOfTask(null));
	}

	/**
	 * Starts the task of a dialog in a request of {@link #_window} with the given acting person and
	 * returns the acting person the task sees.
	 */
	private Person actingPersonOfTask(Person acting) throws Exception {
		AtomicReference<Person> seen = new AtomicReference<>();
		CountDownLatch done = new CountDownLatch(1);
		ProgressDialog dialog = new ProgressDialog(ResKey.text("progress"), DisplayDimension.px(100),
			DisplayDimension.px(100)) {
			@Override
			protected void run(I18NLog log) {
				try {
					seen.set(ActingPerson.handOver());
				} finally {
					done.countDown();
				}
			}
		};
		inNewThread(() -> ThreadContextManager.inContext(_window, () -> {
			try (ThreadLocalBinding<Person>.Handle binding = ActingPerson.bind(acting)) {
				dialog.startTask();
			}
			return null;
		}));
		assertTrue("Timeout waiting for the task.", done.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
		return seen.get();
	}

	private static <T> T inNewThread(Callable<T> job) throws Exception {
		FutureTask<T> task = new FutureTask<>(job);
		Thread thread = new Thread(task, TestProgressDialogActingPerson.class.getSimpleName());
		thread.setDaemon(true);
		thread.start();
		try {
			return task.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
		} catch (ExecutionException ex) {
			Throwable cause = ex.getCause();
			if (cause instanceof Error error) {
				throw error;
			}
			throw (Exception) cause;
		}
	}

	/**
	 * Test suite requiring a knowledge base with persons and the {@link SchedulerService} the task
	 * runs on.
	 */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(new TestSuite(TestProgressDialogActingPerson.class),
				SchedulerService.Module.INSTANCE));
	}

}
