/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.log;

import static java.util.stream.Collectors.*;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

import com.top_logic.base.administration.LoggerAdminBean;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * A {@link ListModelBuilder} that collects the log files, parses them and returns the
 * {@link LogLine} objects as {@link List} elements.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class LogLineModelBuilder extends AbstractConfiguredInstance<LogLineModelBuilder.Config>
		implements ListModelBuilder {

	/** {@link ConfigurationItem} for the {@link LogLineModelBuilder}. */
	public interface Config extends PolymorphicConfiguration<LogLineModelBuilder> {

		/** The {@link LogFileCollector} to use. */
		@ItemDefault
		LogFileCollector.Config getFileCollector();

		/** The {@link LogParser} to use. */
		@ItemDefault
		LogParser.Config getLogParser();
	}

	private final LogFileCollector _fileCollector;

	private final LogParser _logParser;

	/** {@link TypedConfiguration} constructor for {@link LogLineModelBuilder}. */
	public LogLineModelBuilder(InstantiationContext context, Config config) {
		super(context, config);
		_fileCollector = context.getInstance(config.getFileCollector());
		_logParser = context.getInstance(config.getLogParser());
	}

	@Override
	public boolean supportsModel(Object model, LayoutComponent component) {
		return model == null;
	}

	@Override
	public Collection<?> getModel(Object businessModel, LayoutComponent component) {
		List<LogFile> logFiles = getFileCollector().collect(getLogDirectory());
		return logFiles.parallelStream()
			.map(logFile -> getLogParser().parseLog(logFile))
			.flatMap(List::stream)
			.collect(toList());
	}

	/** @see Config#getFileCollector() */
	protected LogFileCollector getFileCollector() {
		return _fileCollector;
	}

	/** @see Config#getLogParser() */
	protected LogParser getLogParser() {
		return _logParser;
	}

	/** The {@link Path} where the log files are searched. */
	protected Path getLogDirectory() {
		return LoggerAdminBean.getInstance().getLogDir().toPath();
	}

	@Override
	public boolean supportsListElement(LayoutComponent component, Object candidate) {
		return candidate instanceof LogLine;
	}

	@Override
	public Object retrieveModelFromListElement(LayoutComponent component, Object candidate) {
		return null;
	}

}
