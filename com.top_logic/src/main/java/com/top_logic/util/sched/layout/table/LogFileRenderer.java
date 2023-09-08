/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.layout.table;

import java.io.File;
import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.common.folder.impl.FileDocument;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.form.control.DownloadControl;
import com.top_logic.util.sched.layout.TaskLogFileRenderUtil;
import com.top_logic.util.sched.task.Task;

/**
 * A {@link Renderer} for the log files of {@link Task}s.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public final class LogFileRenderer implements Renderer<File> {

	/** The singleton instance of the {@link LogFileRenderer}. */
	public static final LogFileRenderer INSTANCE = new LogFileRenderer();

	@Override
	public void write(DisplayContext context, TagWriter out, File value) throws IOException {
		if (value == null) {
			TaskLogFileRenderUtil.writeNoLogFile(context, out);
			return;
		}
		if (!value.exists()) {
			TaskLogFileRenderUtil.writeLogFileNotAvailable(context, out, value);
			return;
		}
		new DownloadControl(new FileDocument(value)).write(context, out);
	}

}
