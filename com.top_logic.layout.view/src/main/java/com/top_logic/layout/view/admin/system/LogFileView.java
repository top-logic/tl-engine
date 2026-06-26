/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin.system;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;
import com.top_logic.util.Resources;

/**
 * Displays the tail of the log file held by the configured {@link Config#getInput() input channel} as
 * scrollable monospace text, re-reading it whenever the input or the {@link Config#getReload() reload
 * channel} changes.
 *
 * <p>
 * App-specific admin widget (referenced by {@code class=}, not a reusable {@code @TagName} element).
 * Only the last {@link #TAIL_BYTES} bytes are read so that arbitrarily large log files stay cheap to
 * view; a leading ellipsis marks a truncated start.
 * </p>
 */
public class LogFileView implements UIElement {

	/** Maximum number of trailing bytes read from a log file. */
	private static final long TAIL_BYTES = 256 * 1024;

	/** CSS class rendering the content as scrollable monospace text (defined in the controls stylesheet). */
	private static final String CSS_CLASS = "tlLogView";

	/**
	 * Configuration for {@link LogFileView}.
	 */
	public interface Config extends UIElement.Config {

		/** Configuration name for {@link #getInput()}. */
		String INPUT = "input";

		/** Configuration name for {@link #getReload()}. */
		String RELOAD = "reload";

		@Override
		@ClassDefault(LogFileView.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Channel holding the selected log {@link File} whose tail is shown.
		 */
		@Name(INPUT)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getInput();

		/**
		 * Channel whose change (a refresh tick) re-reads the current file without changing the selection.
		 */
		@Name(RELOAD)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getReload();
	}

	private final ChannelRef _inputRef;

	private final ChannelRef _reloadRef;

	/**
	 * Creates a new {@link LogFileView} from configuration.
	 */
	@CalledByReflection
	public LogFileView(InstantiationContext context, Config config) {
		_inputRef = config.getInput();
		_reloadRef = config.getReload();
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		ViewChannel input = _inputRef != null ? context.resolveChannel(_inputRef) : null;
		ReactTextControl control =
			new ReactTextControl(context, content(input == null ? null : input.get()), CSS_CLASS);

		if (input != null) {
			ChannelListener listener = (sender, oldValue, newValue) -> control.setText(content(newValue));
			input.addListener(listener);
			control.addCleanupAction(() -> input.removeListener(listener));
		}
		if (_reloadRef != null) {
			ViewChannel reload = context.resolveChannel(_reloadRef);
			ChannelListener listener =
				(sender, oldValue, newValue) -> control.setText(content(input == null ? null : input.get()));
			reload.addListener(listener);
			control.addCleanupAction(() -> reload.removeListener(listener));
		}
		return control;
	}

	/**
	 * The tail of the given file, a placeholder when nothing is selected, or an error message when the
	 * file cannot be read.
	 */
	private static String content(Object value) {
		if (!(value instanceof File file)) {
			return Resources.getInstance().getString(I18NConstants.LOG_NO_SELECTION);
		}
		try {
			return tail(file);
		} catch (IOException ex) {
			return Resources.getInstance().getString(I18NConstants.LOG_READ_FAILED__NAME.fill(file.getName()));
		}
	}

	/**
	 * Reads up to the last {@link #TAIL_BYTES} bytes of the file, dropping a leading partial line and
	 * marking the truncation with an ellipsis.
	 */
	private static String tail(File file) throws IOException {
		long length = file.length();
		long start = Math.max(0, length - TAIL_BYTES);
		try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
			raf.seek(start);
			byte[] buffer = new byte[(int) (length - start)];
			raf.readFully(buffer);
			String text = new String(buffer, StandardCharsets.UTF_8);
			if (start > 0) {
				int newline = text.indexOf('\n');
				text = newline >= 0 ? text.substring(newline + 1) : text;
				text = "…\n" + text;
			}
			return text;
		}
	}
}
