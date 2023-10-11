/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.log.entry;

import static java.util.stream.Collectors.*;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

import com.top_logic.base.administration.LoggerAdminBean;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * A {@link ListModelBuilder} that collects the log files, parses them and returns the
 * {@link ParsedLogEntry} objects as {@link List} elements.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class ParsedLogEntriesModelBuilder implements ListModelBuilder {

	/** The {@link ParsedLogEntriesModelBuilder} instance. */
	public static final ParsedLogEntriesModelBuilder INSTANCE = new ParsedLogEntriesModelBuilder();

	@Override
	public boolean supportsModel(Object model, LayoutComponent component) {
		return model == null;
	}

	@Override
	public Collection<?> getModel(Object businessModel, LayoutComponent component) {
		List<LogFile> logFiles = LogFileCollector.INSTANCE.collect(getLogDirectory());
		LogParser logParser = new LogParser();
		return logFiles.parallelStream()
			.map(logFile -> logParser.parseLog(logFile))
			.flatMap(List::stream)
			.collect(toList());
	}

	/** The {@link Path} where the log files are searched. */
	protected Path getLogDirectory() {
		return LoggerAdminBean.getInstance().getLogDir().toPath();
	}

	@Override
	public boolean supportsListElement(LayoutComponent component, Object candidate) {
		return candidate instanceof ParsedLogEntry;
	}

	@Override
	public Object retrieveModelFromListElement(LayoutComponent component, Object candidate) {
		return null;
	}

}
