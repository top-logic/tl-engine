/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.top_logic.basic.XMain;

/**
 * Empty tool for benchmarking the startup of the configuration service.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BenchmarkStartAppConfig extends XMain {

	/**
	 * Creates a {@link BenchmarkStartAppConfig}.
	 */
	public BenchmarkStartAppConfig() {
		super(false);
	}

	@Override
	protected void doActualPerformance() throws Exception {
		System.out.println("Done.");
	}

	public static void main(String[] args) throws Exception {
//		readln();
		new BenchmarkStartAppConfig().runMainCommandLine(args);
//		readln();
	}

	private static void readln() throws IOException {
		new BufferedReader(new InputStreamReader(System.in)).readLine();
	}
}
