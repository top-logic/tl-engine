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

import jakarta.servlet.ServletContext;

import com.top_logic.basic.InteractionContext;
import com.top_logic.basic.Logger;
import com.top_logic.basic.SubSessionContext;
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
	 * Provides output from the given source as {@link InputStream} by using a separate
	 * {@link Thread} from the {@link SchedulerService}.
	 * 
	 * <p>
	 * If the calling thread has an {@link InteractionContext}, the producer runs in an interaction
	 * of its own with the same {@link SubSessionContext} and {@link ServletContext}, but without
	 * request and response: The calling request may be answered before the producer thread
	 * starts, which makes the request, the response and the calling interaction unusable.
	 * </p>
	 */
	public static InputStream convert(BinaryDataSource source) throws IOException {
		InteractionContext interaction = ThreadContextManager.getInteraction();
		SubSessionContext subSession;
		ServletContext servletContext;
		if (interaction != null) {
			subSession = interaction.getSubSessionContext();
			servletContext = interaction.asServletContext();
		} else {
			subSession = null;
			servletContext = null;
		}
		boolean withContext = interaction != null;

		PipedInputStream in = new PipedInputStream();
		PipedOutputStream out = new PipedOutputStream(in);
		SchedulerService.getInstance().execute(() -> produce(withContext, subSession, servletContext, source, out));
		return in;
	}

	/**
	 * Produces data that can be read on the {@link InputStream} end.
	 * 
	 * @param withContext
	 *        Whether to run the production in an {@link InteractionContext} with the given
	 *        sub-session and servlet context.
	 */
	private static void produce(boolean withContext, SubSessionContext subSession, ServletContext servletContext,
			BinaryDataSource source, OutputStream out) {
		boolean contextSetup = false;

		try {
			if (withContext) {
				ThreadContextManager.setupInteractionContext(subSession, servletContext, null, null);
				contextSetup = true;
			}

			source.deliverTo(out);

		} catch (Throwable ex) {
			Logger.error("Failed to create output: " + source, ex, StreamIOConverter.class);
		} finally {
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