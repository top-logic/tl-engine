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

import com.top_logic.basic.InteractionContext;
import com.top_logic.basic.Logger;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.thread.ThreadContextManager;

/**
 * Utility to provide data written by an algorithm encapsulated in a {@link BinaryDataSource} to an
 * {@link OutputStream} for reading it back through an {@link InputStream}.
 * 
 * @see BinaryDataSource#deliverTo(OutputStream)
 * @see #convert(BinaryDataSource)
 */
public class StreamIOConverter {

	/**
	 * Provides output from the given source as {@link InputStream} buy using a separate
	 * {@link Thread} from the {@link SchedulerService}.
	 */
	public static InputStream convert(BinaryDataSource source) throws IOException {
		InteractionContext context = ThreadContextManager.getInteraction();

		PipedInputStream in = new PipedInputStream();
		PipedOutputStream out = new PipedOutputStream(in);
		SchedulerService.getInstance().execute(() -> produce(context, source, out));
		return in;
	}

	/**
	 * Produces data that can be read on the {@link InputStream} end. If context is provided, sets
	 * up interaction context during production.
	 */
	private static void produce(InteractionContext context, BinaryDataSource source, OutputStream out) {
		boolean contextSetup = false;

		try {
			// Set up interaction context if provided
			if (context != null) {
				ThreadContextManager.setupInteractionContext(
					context.getSubSessionContext(),
					context.asServletContext(),
					context.asRequest(),
					context.asResponse());
				contextSetup = true;
			}

			// Deliver the data
			source.deliverTo(out);

		} catch (Throwable ex) {
			Logger.error("Failed to create output: " + source, ex, StreamIOConverter.class);
		} finally {
			// Clean up thread context if it was set up
			if (contextSetup) {
				try {
					ThreadContextManager.getManager().removeInteraction();
				} catch (Exception ex) {
					Logger.error("Failed to remove thread context interaction", ex, StreamIOConverter.class);
				}
			}

			// Always close the output stream, regardless of any previous exceptions
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