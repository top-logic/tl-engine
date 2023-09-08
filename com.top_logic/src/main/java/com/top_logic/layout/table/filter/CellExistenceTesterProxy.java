/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.util.Objects;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;

/**
 * Proxy for a configured {@link CellExistenceTester}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CellExistenceTesterProxy implements CellExistenceTester {

	/**
	 * Typed configuration interface definition for {@link CellExistenceTesterProxy}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends PolymorphicConfiguration<CellExistenceTesterProxy> {

		/** Configuration name for {@link #getTester()}. */
		String TESTER = "tester";

		/**
		 * Configuration of the {@link CellExistenceTester} to dispatch to.
		 */
		@Name(TESTER)
		@Mandatory
		PolymorphicConfiguration<CellExistenceTester> getTester();

		/**
		 * Setter for {@link #getTester()}.
		 */
		void setTester(PolymorphicConfiguration<CellExistenceTester> tester);

	}

	private final CellExistenceTester _tester;

	/**
	 * Create a {@link CellExistenceTesterProxy}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public CellExistenceTesterProxy(InstantiationContext context, Config config) {
		_tester = context.getInstance(config.getTester());
	}

	/**
	 * Creates a {@link CellExistenceTesterProxy}.
	 */
	public CellExistenceTesterProxy(CellExistenceTester tester) {
		_tester = Objects.requireNonNull(tester);
	}

	@Override
	public boolean isCellExistent(Object rowObject, String columnName) {
		return _tester.isCellExistent(rowObject, columnName);
	}

}

