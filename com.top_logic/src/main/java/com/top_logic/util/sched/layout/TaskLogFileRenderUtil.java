/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.layout;

import java.io.File;
import java.io.IOException;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.common.folder.impl.FileDocument;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.basic.SimpleConstantControl;
import com.top_logic.layout.form.control.DownloadControl;
import com.top_logic.layout.form.model.DataField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.util.sched.layout.table.results.TaskResultAccessor;
import com.top_logic.util.sched.task.Task;
import com.top_logic.util.sched.task.result.TaskResult;

/**
 * Methods useful for rendering log files of {@link Task}s.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class TaskLogFileRenderUtil {

	private static final ResKey I18N_NO_LOG_FILE = I18NConstants.TASK_NO_LOG_FILE;

	private static final ResKey I18N_LOG_FILE_NOT_EXISTING = I18NConstants.LOG_FILE_NOT_AVAILABLE__FILE;

	/** Writes the message for: "This task has no log file." */
	public static void writeNoLogFile(DisplayContext context, TagWriter out) {
		out.writeText(context.getResources().getString(I18N_NO_LOG_FILE));
	}

	/** Writes the message for: "The log file for this task is no longer available." */
	public static void writeLogFileNotAvailable(DisplayContext context, TagWriter out, File file) {
		out.writeText(context.getResources().getMessage(I18N_LOG_FILE_NOT_EXISTING, file.getName()));
	}

	/**
	 * The renderer to be used, when the {@link Task} has no log file.
	 * <p>
	 * Does not use the given value.
	 * </p>
	 */
	public static final Renderer<Object> RENDERER_NO_LOG_FILE = new Renderer<>() {

		@Override
		public void write(DisplayContext context, TagWriter out, Object value) throws IOException {
			writeNoLogFile(context, out);
		}
	};

	/**
	 * The renderer to be used, when the log file is no longer {@link File#exists() available}.
	 * <p>
	 * Expects the given value to be a {@link DataField} containing a {@link FileDocument} as
	 * {@link DataField#getDataItem()}.
	 * </p>
	 */
	public static final Renderer<DataField> RENDERER_LOG_FILE_NOT_AVAILABLE = new Renderer<>() {

		@Override
		public void write(DisplayContext context, TagWriter out, DataField value) throws IOException {
			DataField field = value;
			FileDocument fileDocument = (FileDocument) field.getDataItem();
			writeLogFileNotAvailable(context, out, fileDocument.getFile());
		}
	};

	/**
	 * The {@link ControlProvider} for log file {@link DataField}s.
	 * <p>
	 * Expects the {@link DataField#getDataItem()} to be a {@link FileDocument} or <code>null</code>
	 * , if the {@link Task} has no log file.
	 * </p>
	 */
	public static final ControlProvider LOG_FILE_LINK_CONTROL_PROVIDER = new ControlProvider() {

		@Override
		public Control createControl(Object model, String style) {
			DataField field = (DataField) model;
			FileDocument fileDocument = (FileDocument) field.getDataItem();
			if (fileDocument == null) {
				return new SimpleConstantControl<>(model, RENDERER_NO_LOG_FILE);
			}
			if (!fileDocument.getFile().exists()) {
				return new SimpleConstantControl<>((DataField) model, RENDERER_LOG_FILE_NOT_AVAILABLE);
			}
			return new DownloadControl(fileDocument);
		}

	};

	/** Creates the {@link DataField} for the log file of the {@link TaskResult}. */
	public static DataField createLogFileField(TaskResult taskResult) {
		DataField dataField = FormFactory.newDataField(TaskResultAccessor.LOG_FILE);
		File logFile = (File) TaskResultAccessor.INSTANCE.getValue(taskResult, TaskResultAccessor.LOG_FILE);
		if (logFile != null) {
			dataField.initializeField(new FileDocument(logFile));
		}
		dataField.setControlProvider(LOG_FILE_LINK_CONTROL_PROVIDER);
		return dataField;
	}

}
