/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.StreamEvent;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.tl.ai.core.tools.ToolStubs;
import com.top_logic.tl.ai.tool.api.CancellationToken;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolResult;

/**
 * Cancelling a run must never interrupt a thread that runs one of its tools.
 *
 * <p>
 * H2 reads its database file through a {@link FileChannel}. A {@link FileChannel} is interruptible:
 * when a thread is interrupted while - or before - it reads one, the JDK closes the channel, for
 * every thread of the process and for good. A cancel that interrupts a tool reading the knowledge
 * base therefore leaves the whole application without its database (Gitea #901). The tools here
 * read a file channel the way H2 does, and the channel has to survive the cancel.
 * </p>
 */
public class TestCancelInterruptsNoTool extends TestCase {

	private static final Instant T0 = Instant.parse("2026-10-14T12:00:00Z");

	private static final String MODEL = "test-model";

	private Path _file;

	private FileChannel _channel;

	/** Failures the tools ran into, reported to the test thread. */
	private final Queue<Throwable> _failures = new ConcurrentLinkedQueue<>();

	/**
	 * Boots the thread context the loop runs tools in.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestCancelInterruptsNoTool.class)));
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_file = Files.createTempFile("TestCancelInterruptsNoTool", ".db");
		_channel = FileChannel.open(_file, StandardOpenOption.READ, StandardOpenOption.WRITE);
		_channel.write(ByteBuffer.wrap(new byte[4096]), 0);
	}

	@Override
	protected void tearDown() throws Exception {
		_channel.close();
		Files.deleteIfExists(_file);
		super.tearDown();
	}

	/**
	 * A run cancelled while a parallel batch of reading tools works gives the tools up, but does
	 * not interrupt them.
	 *
	 * <p>
	 * The batch path of the Pietro crash: {@code collectBatchResults} cancelled the batch's futures
	 * with {@code cancel(true)}, which interrupted the tool workers in the middle of their reads.
	 * </p>
	 */
	public void testCancelDuringParallelBatch() throws Exception {
		CountDownLatch started = new CountDownLatch(2);
		CountDownLatch ended = new CountDownLatch(2);
		ToolCatalog registry = new ToolCatalog();
		registry.register(fileReader("read1", started, ended));
		registry.register(fileReader("read2", started, ended));
		registry.freeze();

		CancellationToken token = new CancellationToken();
		cancelWhenStarted(started, token);

		AgentResult result = run(registry, token,
			new ToolCall("tc1", "read1", "{}"), new ToolCall("tc2", "read2", "{}"));

		assertEquals(StopReason.ABORTED, result.stopReason());
		assertTrue("The tools must end after the cancel.", ended.await(10, TimeUnit.SECONDS));
		assertTrue("The tools must not be interrupted: " + _failures, _failures.isEmpty());
		assertTrue("The file channel - the database file - must still be open.", _channel.isOpen());
	}

	/**
	 * A tool that waits for a slow server ends its wait when the run is cancelled, and reads on
	 * without the interrupt that ended the wait.
	 *
	 * <p>
	 * The run stops promptly although nothing interrupts its thread from outside: the wait is the
	 * one place a cancellation may interrupt, and the interrupt does not outlive it.
	 * </p>
	 */
	public void testCancelEndsABlockingWaitOnly() throws Exception {
		CountDownLatch started = new CountDownLatch(1);
		AtomicInteger readsAfterCancel = new AtomicInteger();
		Tool slowServer = ToolStubs.builder("slowServer").execute((args, context) -> {
			try {
				RunCancellationBinding.whileBlocked(() -> {
					started.countDown();
					Thread.sleep(TimeUnit.MINUTES.toMillis(5));
					return null;
				});
				return ToolResult.of("The server answered.");
			} catch (CancellationException ex) {
				// What follows a wait in a tool often reads the knowledge base.
				readSome(readsAfterCancel);
				return ToolResult.failure("Cancelled.");
			} catch (InterruptedException ex) {
				_failures.add(ex);
				return ToolResult.failure("Interrupted.");
			}
		}).build();
		ToolCatalog registry = new ToolCatalog();
		registry.register(slowServer);
		registry.freeze();

		CancellationToken token = new CancellationToken();
		cancelWhenStarted(started, token);

		long before = System.nanoTime();
		AgentResult result = run(registry, token, new ToolCall("tc1", "slowServer", "{}"));
		long millis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - before);

		assertEquals(StopReason.ABORTED, result.stopReason());
		assertTrue("The cancel must end the wait promptly, took " + millis + " ms.", millis < 10_000);
		assertEquals("The tool must read on after the cancel.", 100, readsAfterCancel.get());
		assertTrue("Nothing may be interrupted outside the wait: " + _failures, _failures.isEmpty());
		assertTrue("The file channel - the database file - must still be open.", _channel.isOpen());
		assertFalse("The run's thread must not carry an interrupt.", Thread.currentThread().isInterrupted());
	}

	/**
	 * A reading tool that reads the file channel until the run is cancelled, and for a second more,
	 * as a query does that is in the middle of its work.
	 */
	private Tool fileReader(String name, CountDownLatch started, CountDownLatch ended) {
		return ToolStubs.builder(name).execute((args, context) -> {
			try {
				AtomicInteger reads = new AtomicInteger();
				readSome(reads);
				started.countDown();
				while (!context.cancellationToken().isCancelled()) {
					readSome(reads);
				}
				// A query does not look at the token; it reads on until it is done.
				long until = System.nanoTime() + TimeUnit.SECONDS.toNanos(1);
				while (System.nanoTime() < until && _failures.isEmpty()) {
					readSome(reads);
				}
				return ToolResult.of("Read " + reads.get() + " pages.");
			} finally {
				ended.countDown();
			}
		}).build();
	}

	/** Reads 100 pages of the file; records a failure instead of throwing it. */
	private void readSome(AtomicInteger reads) {
		ByteBuffer page = ByteBuffer.allocate(4096);
		try {
			for (int n = 0; n < 100; n++) {
				page.clear();
				_channel.read(page, 0);
				reads.incrementAndGet();
			}
		} catch (IOException ex) {
			_failures.add(ex);
		}
	}

	/** Cancels the token from another thread - as the stop button does - once the tools work. */
	private static void cancelWhenStarted(CountDownLatch started, CancellationToken token) {
		Thread stop = new Thread(() -> {
			try {
				if (started.await(10, TimeUnit.SECONDS)) {
					Thread.sleep(50);
				}
			} catch (InterruptedException ex) {
				// Cancel anyway.
			}
			token.cancel();
		}, "TestCancelInterruptsNoTool-stop");
		stop.setDaemon(true);
		stop.start();
	}

	/** Runs a loop whose model calls the given tools in its first turn. */
	private static AgentResult run(ToolCatalog registry, CancellationToken token, ToolCall... calls) {
		Conversation conversation = new Conversation();
		conversation.add(new UserMessage("u1", T0, List.of(new TextContent("Go"))));
		AgentLoop loop = new AgentLoop(new AgentParams(MODEL, "You are a test assistant.",
			AgentParams.DEFAULT_MAX_TURNS, AgentParams.DEFAULT_MAX_OUTPUT_TOKENS), callingOnce(calls), registry);
		return loop.run(conversation, new ToolContext(allowAll(), ApprovalCallback.AUTO_APPROVE,
			TransactionScope.NOOP, token), event -> {
				// Not looked at.
			});
	}

	/** A model that calls the given tools, and answers with text after that. */
	private static LLMProvider callingOnce(ToolCall... calls) {
		AtomicInteger turn = new AtomicInteger();
		return (request, handler) -> {
			if (turn.getAndIncrement() > 0) {
				handler.onEvent(new StreamEvent.TextDelta(0, "Done."));
				handler.onEvent(new StreamEvent.MessageComplete("m2", MODEL, "end_turn", TokenUsage.EMPTY,
					List.of(new TextContent("Done."))));
				return;
			}
			List<ContentPart> content = List.of(calls);
			for (int i = 0; i < calls.length; i++) {
				handler.onEvent(new StreamEvent.ToolCallStart(i, calls[i].id(), calls[i].name()));
				handler.onEvent(new StreamEvent.ToolCallDelta(i, calls[i].arguments()));
			}
			handler.onEvent(new StreamEvent.MessageComplete("m1", MODEL, "tool_use", TokenUsage.EMPTY, content));
		};
	}

	private static SecurityContext allowAll() {
		return new SecurityContext() {
			@Override
			public boolean hasToolPermission(Tool tool) {
				return true;
			}

			@Override
			public ExecutionMode executionMode() {
				return ExecutionMode.DEVELOPMENT;
			}
		};
	}
}
