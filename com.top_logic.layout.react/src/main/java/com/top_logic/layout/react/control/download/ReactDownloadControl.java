/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.download;

import java.util.Map;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.react.DataProvider;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.ReactControl;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A React control that provides a download button for binary data stored on the server.
 *
 * <p>
 * On the client side, this control renders a {@code TLDownload} React component that displays the
 * file name and a download icon. When clicked, it fetches data from the {@code /react-api/data}
 * endpoint and triggers a browser file download.
 * </p>
 *
 * <p>
 * If {@link #setClearable(boolean) clearable} is enabled, a clear button is rendered next to the
 * file name, allowing the user to remove the current data. The {@link ClearListener} is notified
 * when the user clears the data.
 * </p>
 */
public class ReactDownloadControl extends ReactControl implements DataProvider {

	/** State key indicating whether download data is available. */
	private static final String HAS_DATA = "hasData";

	/** State key whose value increments each time the download data is replaced. */
	private static final String DATA_REVISION = "dataRevision";

	/** State key for the suggested file name. */
	private static final String FILE_NAME = "fileName";

	/** State key indicating whether the clear button is shown. */
	private static final String CLEARABLE = "clearable";

	private static final Map<String, ControlCommand> COMMANDS = createCommandMap(new ClearCommand());

	private BinaryData _data;

	private int _dataRevision;

	private ClearListener _clearListener;

	/**
	 * Listener that is notified when the user clears the download data.
	 */
	public interface ClearListener {

		/**
		 * Called when the user clicks the clear button.
		 */
		void handleClear();
	}

	/**
	 * Creates a new {@link ReactDownloadControl}.
	 *
	 * @param data
	 *        The initial download data, or {@code null} if no data is available yet.
	 * @param fileName
	 *        The suggested file name for the download, or {@code null}.
	 */
	public ReactDownloadControl(BinaryData data, String fileName) {
		super(null, "TLDownload", COMMANDS);
		_data = data;
		putState(HAS_DATA, data != null);
		putState(DATA_REVISION, _dataRevision);
		putState(FILE_NAME, fileName);
		putState(CLEARABLE, false);
	}

	/**
	 * Sets the data to download.
	 *
	 * <p>
	 * If the control is already rendered, a state patch is sent to the client so that the download
	 * button is enabled or disabled accordingly.
	 * </p>
	 *
	 * @param data
	 *        The binary data, or {@code null} to clear.
	 * @param fileName
	 *        The suggested file name for the download, or {@code null}.
	 */
	public void setData(BinaryData data, String fileName) {
		_data = data;
		_dataRevision++;
		putState(HAS_DATA, data != null);
		putState(DATA_REVISION, _dataRevision);
		putState(FILE_NAME, fileName);
	}

	/**
	 * Enables or disables the clear button on the client.
	 *
	 * @param clearable
	 *        Whether the clear button should be shown.
	 */
	public void setClearable(boolean clearable) {
		putState(CLEARABLE, clearable);
	}

	/**
	 * Sets the listener that is notified when the user clears the download data.
	 *
	 * @param listener
	 *        The listener, or {@code null} to remove.
	 */
	public void setClearListener(ClearListener listener) {
		_clearListener = listener;
	}

	@Override
	public BinaryData getDownloadData() {
		return _data;
	}

	/**
	 * Command that handles the clear event from the React client.
	 */
	static class ClearCommand extends ControlCommand {

		ClearCommand() {
			super("clear");
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.REACT_DOWNLOAD_CLEAR;
		}

		@Override
		protected HandlerResult execute(DisplayContext context, Control control, Map<String, Object> arguments) {
			ReactDownloadControl downloadControl = (ReactDownloadControl) control;
			downloadControl.setData(null, null);
			ClearListener listener = downloadControl._clearListener;
			if (listener != null) {
				listener.handleClear();
			}
			return HandlerResult.DEFAULT_RESULT;
		}
	}

}
