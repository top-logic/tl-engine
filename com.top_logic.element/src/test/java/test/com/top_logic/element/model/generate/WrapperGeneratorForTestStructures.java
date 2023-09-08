/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.model.generate;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import test.com.top_logic.basic.ConfigLoaderTestUtil;

import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.util.Computation;
import com.top_logic.element.model.generate.WrapperGenerator;

/**
 * {@link WrapperGenerator} that can create classes for test structures.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class WrapperGeneratorForTestStructures extends WrapperGenerator {

	@Override
	protected void initXMLProperties() throws ModuleException {
		// XMLProperties are loaded before.
	}

	@Override
	public void run(final String[] args) throws IOException, ModuleException {
		final AtomicReference<Exception> problem = new AtomicReference<>();
		ConfigLoaderTestUtil.INSTANCE.runWithLoadedConfig(new Computation<Void>() {

			@Override
			public Void run() {
				try {
					WrapperGeneratorForTestStructures.this.internalRun(args);
				} catch (IOException ex) {
					problem.set(ex);
				} catch (ModuleException ex) {
					problem.set(ex);
				}
				return null;
			}
		});
		Exception ex = problem.get();
		if (ex != null) {
			rethrowProblem(ex);
		}
	}

	private void rethrowProblem(Exception ex) throws IOException, ModuleException {
		if (ex instanceof IOException) {
			throw (IOException) ex;
		}
		if (ex instanceof ModuleException) {
			throw (ModuleException) ex;
		}
		throw new RuntimeException("Unexpected Error: " + ex.getClass().getName(), ex);
	}

	void internalRun(String[] args) throws IOException, ModuleException {
		super.run(args);
	}

	/**
	 * Main method to execute wrapper generation for test wrappers.
	 */
	public static void main(String[] args) throws Exception {
		new WrapperGeneratorForTestStructures().run(args);
	}

}

