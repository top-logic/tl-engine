/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.transform;

import java.util.List;

import com.top_logic.model.TLModel;

/**
 * Generic main class that applies an arbitrary number of
 * {@link ModelTransformation}s to a {@link TLModel}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class GenericModelTransformerMain extends ModelTransformerMain {

	private static final String TRANSFORM_PARAM = "-transform";
	
	private String transformerNames;

	private List<Class<? extends ModelTransformation>> transformationClasses;

	@Override
	protected void showHelpOptions() {
        info("\t-transform <classes>   comma separated list of transformer classes");
		super.showHelpOptions();
	}
	
	@Override
	protected int shortOption(char c, String[] args, int i) {
		int index = i - 1;
		if (TRANSFORM_PARAM.equals(args[index])) {
			transformerNames = args[i++];
		}
		else {
			return super.shortOption(c, args, i);
		}

		return i;
	}
	
	@Override
	protected void doActualPerformance() throws Exception {
		if (transformerNames == null) {
			error("Missing '" + TRANSFORM_PARAM + "' argument.");
		} else {
			try {
				transformationClasses = GenericModelTransformer.parseTransformerClasses(transformerNames);
			} catch (IllegalArgumentException ex) {
				error("Invalid transformation specification.", ex);
			}
		}
		
		if (!hasErrors()) {
			super.doActualPerformance();
		}
	}

	@Override
	protected void transform(TLModel model) {
		GenericModelTransformer.createGenericModelTransformer(this, model, transformationClasses)
			.transform();
	}
	
	public static void main(String[] args) throws Exception {
		new GenericModelTransformerMain().runMainCommandLine(args);
	}

}
