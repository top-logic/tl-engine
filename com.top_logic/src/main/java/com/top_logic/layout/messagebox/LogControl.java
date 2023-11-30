/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.messagebox;

import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.base.services.simpleajax.AJAXConstants;
import com.top_logic.base.services.simpleajax.ClientAction;
import com.top_logic.base.services.simpleajax.FragmentInsertion;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.i18n.log.I18NLog;
import com.top_logic.basic.logging.Level;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;
import com.top_logic.util.Resources;

/**
 * {@link Control} displaying a {@link #addMessage(Level, ResKey, Throwable) log messages} from
 * background threads.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LogControl extends AbstractControlBase {

	private final LogBuffer<LogMessage> _buffer = new LogBuffer<>();

	@Override
	public Object getModel() {
		return null;
	}

	@Override
	protected String getTypeCssClass() {
		return "progessLog";
	}

	/**
	 * An {@link I18NLog} that can be used to display messages in this {@link LogControl}.
	 * 
	 * <p>
	 * The log can be used from a background thread to display log messages. In this use case, the
	 * displaying container is responsible for periodically producing updates. Otherwise, the
	 * produced messages are not displayed.
	 * </p>
	 */
	public I18NLog getLog() {
		return this::addMessage;
	}

	/**
	 * Adds a new message to display.
	 * 
	 * <p>
	 * This method may be called from a background thread to display log messages. In this use case,
	 * the displaying container is responsible for periodically producing updates. Otherwise, the
	 * produced messages are not displayed.
	 * </p>
	 */
	public void addMessage(Level level, ResKey message, Throwable ex) {
		_buffer.add(new LogMessage(level, message, ex));
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(DIV);
		writeControlAttributes(context, out);
		out.endBeginTag();

		_buffer.reset();
		for (LogMessage message : _buffer.fetchMessages()) {
			renderMessage(context, out, message);
		}

		out.endTag(DIV);
	}

	static void renderMessage(DisplayContext context, TagWriter out, LogMessage message) throws IOException {
		Resources resources = context.getResources();

		out.beginBeginTag(DIV);
		out.beginCssClasses();
		out.append("logMessage");
		out.append(message.getLevel().toString());
		out.endCssClasses();

		String tooltipHtml = resources.getString(message.getMessage().tooltipOptional());
		OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributes(context, out, tooltipHtml);

		out.endBeginTag();
		out.writeText(append(resources.getString(message.getMessage()), message.getProblem()));
		out.endTag(DIV);
	}

	private static String append(String message, Throwable ex) {
		if (ex == null) {
			return message;
		}
		return append(strip(message) + ": " + message(ex), ex.getCause());
	}

	private static String message(Throwable ex) {
		String result = ex.getMessage();
		if (result == null) {
			return ex.getClass().getName();
		}
		return result;
	}

	private static String strip(String message) {
		if (message.endsWith(".")) {
			return message.substring(0, message.length() - 1);
		}
		return message;
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	protected boolean hasUpdates() {
		return _buffer.hasNewMessages();
	}

	@Override
	protected void internalRevalidate(DisplayContext context, UpdateQueue actions) {
		ClientAction update = new FragmentInsertion(getID(), AJAXConstants.AJAX_POSITION_END_VALUE,
			new MessagesFragment(_buffer.fetchMessages()));
		actions.add(update);
	}

	/**
	 * Data structure that holds a list of messages.
	 * 
	 * <p>
	 * It synchronizes {@link #add(Object) message producers} and {@link #fetchMessages() message
	 * consumers}.
	 * </p>
	 */
	public static class LogBuffer<T> {

		private final List<T> _messages = new ArrayList<>();

		private int _index = 0;

		/**
		 * Resets the fetch pointer of this buffer to zero.
		 * 
		 * <p>
		 * The next call to {@link #fetchMessages()} will return all messages currently in the
		 * buffer.
		 * </p>
		 */
		public synchronized void reset() {
			_index = 0;
		}

		/**
		 * Whether new messages have arrived in this buffer after the last call to
		 * {@link #fetchMessages()}.
		 */
		public synchronized boolean hasNewMessages() {
			return _index < _messages.size();
		}

		/**
		 * Adds a new message to the buffer.
		 */
		public synchronized void add(T message) {
			_messages.add(message);
		}

		/**
		 * Fetches the chunk of {@link #add(Object) newly added} messages after the last call to
		 * {@link #fetchMessages()}.
		 */
		public synchronized List<T> fetchMessages() {
			int size = _messages.size();
			List<T> result = new ListView<>(_messages, _index, size);
			_index = size;
			return result;
		}


		/**
		 * Sub-list of an original list solely based on start and stop indices.
		 * 
		 * <p>
		 * In contrast to {@link List#subList(int, int)}, this view keeps valid after elements are
		 * appended to the backing list.
		 * </p>
		 */
		private static class ListView<T> extends AbstractList<T> {

			private final List<? extends T> _orig;

			private final int _start;

			private final int _stop;

			public ListView(List<? extends T> orig, int start, int stop) {
				_orig = orig;
				_start = start;
				_stop = stop;
			}

			@Override
			public T get(int index) {
				return _orig.get(_start + index);
			}

			@Override
			public int size() {
				return _stop - _start;
			}
		}
	}

	private static final class MessagesFragment implements HTMLFragment {
		private final List<LogMessage> _newMessages;
	
		/**
		 * Creates a {@link MessagesFragment}.
		 */
		public MessagesFragment(List<LogMessage> newMessages) {
			_newMessages = newMessages;
		}
	
		@Override
		public void write(DisplayContext context, TagWriter out) throws IOException {
			for (LogMessage message : _newMessages) {
				renderMessage(context, out, message);
			}
		}
	}

	/**
	 * Messing to be displayed in a {@link LogControl}.
	 */
	private class LogMessage {

		private Level _level;

		private ResKey _message;

		private Throwable _ex;

		/**
		 * Creates a {@link LogMessage}.
		 */
		public LogMessage(Level level, ResKey message, Throwable ex) {
			_level = level;
			_message = message;
			_ex = ex;
		}

		/**
		 * The logging level of the message.
		 */
		public Level getLevel() {
			return _level;
		}

		/**
		 * The message text.
		 * 
		 * <p>
		 * A {@link ResKey#tooltip()} suffix is used as tooltip.
		 * </p>
		 */
		public ResKey getMessage() {
			return _message;
		}

		/**
		 * An optional exception.
		 */
		public Throwable getProblem() {
			return _ex;
		}

	}
}
