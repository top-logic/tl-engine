/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin.monitor;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.version.Version;
import com.top_logic.layout.view.UIElement;
import com.top_logic.util.AbstractStartStopListener;
import com.top_logic.util.Resources;
import com.top_logic.util.monitor.ApplicationMonitor;
import com.top_logic.util.monitor.MonitorMessage;

/**
 * Read-only table of the application's runtime status: the registered application monitors and
 * their messages, JVM heap usage, Java VM identity and basic system facts, grouped into sections.
 *
 * <p>
 * App-specific admin widget (referenced by {@code class=}, not a reusable {@code @TagName} element).
 * The heap figures are a live snapshot read from the {@link Runtime} at render time; the historical
 * memory chart is a separate, deferred concern.
 * </p>
 */
public class ApplicationMonitorTable extends SectionedTable {

	private static final long MEGA_BYTE = 1024L * 1024L;

	/**
	 * Configuration for {@link ApplicationMonitorTable}.
	 */
	public interface Config extends SectionedTable.Config {

		@Override
		@ClassDefault(ApplicationMonitorTable.class)
		Class<? extends UIElement> getImplementationClass();
	}

	/**
	 * Creates a new {@link ApplicationMonitorTable} from configuration.
	 */
	@CalledByReflection
	public ApplicationMonitorTable(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected List<Row> rows() {
		Resources resources = Resources.getInstance();
		String status = resources.getString(I18NConstants.APPLICATION_MONITOR_SECTION_STATUS);
		String memory = resources.getString(I18NConstants.APPLICATION_MONITOR_SECTION_MEMORY);
		String java = resources.getString(I18NConstants.APPLICATION_MONITOR_SECTION_JAVA);
		String system = resources.getString(I18NConstants.APPLICATION_MONITOR_SECTION_SYSTEM);

		List<Row> rows = new ArrayList<>();

		for (MonitorMessage message : ApplicationMonitor.getInstance().checkApplication().getMessages()) {
			rows.add(new Row(status, message.getType().toString(), message.getMessage()));
		}

		Runtime runtime = Runtime.getRuntime();
		long max = runtime.maxMemory();
		long total = runtime.totalMemory();
		long free = runtime.freeMemory();
		rows.add(new Row(memory, "Used", megaBytes(total - free)));
		rows.add(new Row(memory, "Free", megaBytes(free)));
		rows.add(new Row(memory, "Allocated", megaBytes(total)));
		rows.add(new Row(memory, "Available", megaBytes(max - total)));
		rows.add(new Row(memory, "Maximum", megaBytes(max)));

		rows.add(new Row(java, "Name", System.getProperty("java.vm.name")));
		rows.add(new Row(java, "Version", System.getProperty("java.vm.version")));
		rows.add(new Row(java, "Vendor", System.getProperty("java.vm.vendor")));
		rows.add(new Row(java, "Info", System.getProperty("java.vm.info")));

		Version version = Version.getApplicationVersion();
		if (version != null) {
			rows.add(new Row(system, "Application", version.getName() + " " + version.getVersionString()));
		}
		rows.add(new Row(system, "Host", hostName()));
		rows.add(new Row(system, "Time zone", TimeZone.getDefault().getID()));
		Date startup = AbstractStartStopListener.startUpDate();
		if (startup != null) {
			rows.add(new Row(system, "Started", startup.toString()));
		}
		rows.add(new Row(system, "Current time", new Date().toString()));

		return rows;
	}

	/**
	 * The local host name, or its message when the host name cannot be resolved.
	 */
	private static String hostName() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException ex) {
			return ex.getMessage();
		}
	}

	/**
	 * Formats a byte count as whole mega bytes.
	 */
	private static String megaBytes(long bytes) {
		return (bytes / MEGA_BYTE) + " MB";
	}
}
