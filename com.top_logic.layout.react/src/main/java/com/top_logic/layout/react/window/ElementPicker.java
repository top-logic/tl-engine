/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.window;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import com.top_logic.basic.Logger;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommandTarget;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.protocol.PickEvent;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;

/**
 * Lets the user point at an element of a running application window and reports what was hit.
 *
 * <p>
 * A pick puts the target window's browser page into pick mode: the element under the pointer is
 * highlighted and the next click reports it to the server, where the registered
 * {@link PendingPick} turns it into a {@link PickResult} and hands it to the caller's callback. The
 * {@link PickKind} decides what a click resolves to - the view source that rendered the element, or
 * the control that owns it. The user aborts with the escape key, in which case the callback never
 * runs and the pending pick is dropped with the session.
 * </p>
 *
 * <p>
 * Typically the picking window is not the picked one: a tool window (the view designer, the UI
 * inspector) starts a pick in the main application window and shows what came back. The callback
 * therefore runs on the thread of the request that reports the click - the picked window's request,
 * not the tool window's - inside the session's
 * {@link ReactWindowRegistry#beginInteraction() interaction} and with the sub-session of the
 * {@link PendingPick#requesterWindowId() requesting window} installed. Channels and controls of the
 * requesting window may therefore be updated directly from the callback: the updates flush to that
 * window's SSE queue like those of any of its own commands.
 * </p>
 */
public class ElementPicker {

	/** Servlet path of the endpoint the client posts a pick result to. */
	public static final String PICK_PATH = "/pick";

	/** Name of the JSON field holding the correlation token of the pick. */
	public static final String TOKEN_FIELD = "token";

	/** Name of the JSON field holding the {@link PickKind}'s external name. */
	public static final String KIND_FIELD = "kind";

	/** Name of the JSON field holding the picked view's source path, see {@link PickKind#VIEW}. */
	public static final String PATH_FIELD = "path";

	/** Name of the JSON field holding the picked control's ID, see {@link PickKind#CONTROL}. */
	public static final String CONTROL_ID_FIELD = "controlId";

	/**
	 * Puts the target window into pick mode.
	 *
	 * @param requester
	 *        The window starting the pick, in whose sub-session the callback runs.
	 * @param target
	 *        The window the user picks in.
	 * @param kind
	 *        What a click resolves to.
	 * @param onPicked
	 *        Receives what the user hit; never called if the user aborts the pick.
	 * @return The correlation token of the started pick, which the client quotes when it reports the
	 *         click. <code>null</code> if pick mode could not be started, because the session has no
	 *         registry or the target window has no update queue to reach its browser page through.
	 */
	public static String start(ReactContext requester, ReactContext target, PickKind kind,
			Consumer<PickResult> onPicked) {
		return start(requester, target.getWindowName(), kind, onPicked);
	}

	/**
	 * Puts the window with the given id into pick mode.
	 *
	 * <p>
	 * The form for a caller that knows its target by id rather than by context - a side-window
	 * picking in the window that {@link ReactContext#getOpenerWindowName() opened} it.
	 * </p>
	 *
	 * @param requester
	 *        The window starting the pick, in whose sub-session the callback runs.
	 * @param targetWindowId
	 *        The id of the window the user picks in.
	 * @param kind
	 *        What a click resolves to.
	 * @param onPicked
	 *        Receives what the user hit; never called if the user aborts the pick.
	 * @return The correlation token of the started pick, which the client quotes when it reports the
	 *         click. <code>null</code> if pick mode could not be started, because the session has no
	 *         registry or the target window has no update queue to reach its browser page through.
	 */
	public static String start(ReactContext requester, String targetWindowId, PickKind kind,
			Consumer<PickResult> onPicked) {
		ReactWindowRegistry registry = requester.getWindowRegistry();
		if (registry == null || targetWindowId == null) {
			return null;
		}
		SSEUpdateQueue targetQueue = registry.getQueue(targetWindowId);
		if (targetQueue == null) {
			return null;
		}

		String token = UUID.randomUUID().toString();
		registry.registerPick(token,
			new PendingPick(requester.getWindowName(), targetWindowId, kind, onPicked));

		targetQueue.enqueue(PickEvent.create()
			.setToken(token)
			.setTargetWindowId(targetWindowId)
			.setKind(kind.getExternalName()));
		return token;
	}

	/**
	 * A pick report that was resolved: the pick it answers, and what the user hit.
	 *
	 * @param pick
	 *        The pick the report answers, already removed from the registry.
	 * @param result
	 *        What the user hit.
	 */
	public record Delivery(PendingPick pick, PickResult result) {

		/**
		 * Hands the result to the pick's callback.
		 *
		 * <p>
		 * The caller decides what the callback sees: {@link ElementPicker} documents the sub-session
		 * and the lock it is run under.
		 * </p>
		 */
		public void deliver() {
			pick().onPicked().accept(result());
		}
	}

	/**
	 * Resolves what the client reported about the user's click.
	 *
	 * <p>
	 * Consumes the pending pick, so a report can be delivered only once. A report that identifies
	 * nothing the pick can deliver is logged and answered with <code>null</code>: the click is over
	 * and there is nothing the client could do about it.
	 * </p>
	 *
	 * @param registry
	 *        The registry the pick was started in.
	 * @param report
	 *        The fields the client posted, see {@link #TOKEN_FIELD}, {@link #KIND_FIELD},
	 *        {@link #PATH_FIELD} and {@link #CONTROL_ID_FIELD}.
	 * @return The pick and its result, or <code>null</code> if the report cannot be delivered.
	 */
	public static Delivery resolve(ReactWindowRegistry registry, Map<String, Object> report) {
		String token = (String) report.get(TOKEN_FIELD);
		PendingPick pick = token == null ? null : registry.consumePick(token);
		if (pick == null) {
			Logger.info("Pick report for an unknown or already consumed token.", ElementPicker.class);
			return null;
		}
		PickKind reportedKind = PickKind.byExternalName((String) report.get(KIND_FIELD));
		if (reportedKind != pick.kind()) {
			Logger.info("Pick report of kind '" + report.get(KIND_FIELD) + "' for a pick of kind '"
				+ pick.kind().getExternalName() + "', ignoring.", ElementPicker.class);
			return null;
		}
		PickResult result = resolveResult(registry, pick, report);
		return result == null ? null : new Delivery(pick, result);
	}

	private static PickResult resolveResult(ReactWindowRegistry registry, PendingPick pick,
			Map<String, Object> report) {
		String targetWindowId = pick.targetWindowId();
		switch (pick.kind()) {
			case VIEW: {
				String path = (String) report.get(PATH_FIELD);
				if (path == null) {
					Logger.info("Pick report without a view source path.", ElementPicker.class);
					return null;
				}
				return new PickResult.ViewPicked(targetWindowId, path);
			}
			case CONTROL: {
				String controlId = (String) report.get(CONTROL_ID_FIELD);
				if (controlId == null) {
					Logger.info("Pick report without a control ID.", ElementPicker.class);
					return null;
				}
				SSEUpdateQueue targetQueue = registry.getQueue(targetWindowId);
				ReactCommandTarget target = targetQueue == null ? null : targetQueue.getControl(controlId);
				if (!(target instanceof ReactControl control)) {
					Logger.info("Picked control '" + controlId + "' is not held by window '"
						+ targetWindowId + "'.", ElementPicker.class);
					return null;
				}
				return new PickResult.ControlPicked(targetWindowId, targetQueue, control);
			}
		}
		return null;
	}

	/**
	 * Only static access.
	 */
	private ElementPicker() {
		// Singleton utility.
	}
}
