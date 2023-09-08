/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.timer;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import com.top_logic.base.services.simpleajax.JSSnipplet;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link Control} that schedules a timer on the client.
 * 
 * <p>
 * A {@link TimerControl} provides an {@link #getExecutor()} that allows to schedule commands to be
 * executed in the display thread with some delay (not in a separate thread, which would not be
 * allowed to update the UI).
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TimerControl extends AbstractControlBase {

	private static final Map<String, ControlCommand> COMMANDS = createCommandMap(new Update());

	private final UIExecutorService _executor = new UIExecutorService() {
		@Override
		protected void onScheduleChanged(long nextDelayMillis) {
			scheduleUpdate(nextDelayMillis);
		}
	};

	private long _nextDelayMillis;

	/**
	 * Creates a {@link TimerControl}.
	 */
	public TimerControl() {
		super(COMMANDS);
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	public Object getModel() {
		return null;
	}

	/**
	 * The {@link ScheduledExecutorService} that allows scheduling commands.
	 */
	public ScheduledExecutorService getExecutor() {
		return _executor;
	}

	@Override
	protected void internalAttach() {
		super.internalAttach();
		_nextDelayMillis = 0;
	}

	void update() {
		long nextDelayMillis = _executor.process();
		if (nextDelayMillis > 0) {
			scheduleUpdate(nextDelayMillis);
		}
	}

	@Override
	protected boolean hasUpdates() {
		return _nextDelayMillis > 0;
	}

	@Override
	protected void internalRevalidate(DisplayContext context, UpdateQueue actions) {
		if (_nextDelayMillis > 0) {
			actions.add(new JSSnipplet(scheduleScript(_nextDelayMillis)));
			_nextDelayMillis = 0;
		}
	}

	void scheduleUpdate(long nextDelayMillis) {
		if (isAttached()) {
			_nextDelayMillis = nextDelayMillis;
		}
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		long nextDelay = _executor.nextDelay();
		out.beginBeginTag(SPAN);
		writeControlAttributes(context, out);
		out.endBeginTag();
		if (nextDelay > 0) {
			out.beginScript();
			out.writeScript(scheduleScript(nextDelay));
			out.endScript();
		}
		out.endTag(SPAN);
	}

	private String scheduleScript(long nextDelayMillis) {
		return "services.form.TimerControl.schedule('" + getID() + "', " + nextDelayMillis + ");";
	}

	private static final class Update extends ControlCommand {

		public Update() {
			super("update");
		}

		@Override
		protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
			((TimerControl) control).update();
			return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.TIMER_UPDATE;
		}

	}

}
