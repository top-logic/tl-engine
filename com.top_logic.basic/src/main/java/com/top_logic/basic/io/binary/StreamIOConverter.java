/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io.binary;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import com.top_logic.basic.Logger;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.thread.ThreadContextManager;

/**
 * Utility to provide data written by an algorithm encapsulated in a {@link BinaryDataSource} to an
 * {@link OutputStream} for reading it back through an {@link InputStream}.
 * 
 * @see BinaryDataSource#deliverTo(OutputStream)
 * @see #convert(BinaryDataSource)
 */
class StreamIOConverter {

	/**
	 * Provides output from the given source as {@link InputStream} buy using a separate
	 * {@link Thread} from the {@link SchedulerService}.
	 */
	public static InputStream convert(BinaryDataSource source) throws IOException {
		ThreadContext context = ThreadContext.getThreadContext();

		PipedInputStream in = new PipedInputStream();
		PipedOutputStream out = new PipedOutputStream(in);
		SchedulerService.getInstance().execute(() -> produceIn(context, source, out));
		return in;
	}

	private static void produceIn(ThreadContext context, BinaryDataSource source, OutputStream out) {
		ThreadContextManager.inContext(context, () -> produce(source, out));
	}

	/**
	 * Produces data that can be read on the {@link InputStream} end.
	 */
	public static void produce(BinaryDataSource source, OutputStream out) {
		try {
			source.deliverTo(out);
		} catch (Throwable ex) {
			Logger.error("Faild to create output: " + source, ex, StreamIOConverter.class);
		} finally {
			try {
				out.close();
			} catch (IOException ex) {
				// Ignore.
			}
		}
	}

	/**
	 * Upgrades the given {@link BinaryDataSource} to a full {@link BinaryData}.
	 */
	public static BinaryData toData(BinaryDataSource source) {
		if (source instanceof BinaryData) {
			return (BinaryData) source;
		}

		return new BinaryDataSourceUpgrade(source) {
			@Override
			public InputStream getStream() throws IOException {
				return StreamIOConverter.convert(this);
			}
		};
	}
}