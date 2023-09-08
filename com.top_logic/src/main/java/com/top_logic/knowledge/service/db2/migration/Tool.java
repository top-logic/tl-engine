/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration;

import java.util.HashSet;
import java.util.Set;

import com.top_logic.basic.XMain;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.util.Computation;
import com.top_logic.util.TLContext;

/**
 * Base class for KB migration tools.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class Tool extends XMain {

	private static final String MODULE_OPTION = "module";

	private Set<BasicRuntimeModule<?>> _customModules = new HashSet<>();

	@Override
	protected int longOption(String option, String[] args, int i) {
		if (MODULE_OPTION.equals(option)) {
			try {
				Class<? extends Object> moduleClazz = Class.forName(args[i] + "$Module");
				BasicRuntimeModule<?> module = (BasicRuntimeModule<?>) ConfigUtil.getInstance(moduleClazz);
				_customModules.add(module);
			} catch (ClassNotFoundException ex) {
				throw new RuntimeException(ex);
			} catch (ConfigurationException ex) {
				throw new RuntimeException(ex);
			}
			return i + 1;
		}
		return super.longOption(option, args, i);
	}

	@Override
	protected void showHelpOptions() {
		super.showHelpOptions();
		info("\t   | --module             <module> Use given module");
	}

	/**
	 * Registers the given module as needed for {@link #runTool()}.
	 * 
	 * <p>
	 * This method should be called in sub classes from {@link #setUp(String[])}.
	 * </p>
	 * 
	 * @param module
	 *        The module to need for {@link #runTool()}
	 */
	protected final void addCustomModule(BasicRuntimeModule<?> module) {
		_customModules.add(module);
	}

	@Override
	protected void doActualPerformance() throws Exception {
		super.doActualPerformance();

		initFileManager();
		initXMLProperties();
		setupModuleContext(_customModules.toArray(new BasicRuntimeModule<?>[0]));

		TLContext.inSystemContext(Tool.class, new Computation<Void>() {
			@Override
			public Void run() {
				try {
					runTool();
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
				return null;
			}
		});
	}

	/**
	 * Actually executes the tool.
	 */
	protected abstract void runTool() throws Exception;

}
