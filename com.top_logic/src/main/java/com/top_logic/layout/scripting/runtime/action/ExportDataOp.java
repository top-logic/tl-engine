/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.layout.scripting.action.ExportData;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * {@link ApplicationActionOp} that saves a {@link BinaryData} value from the tested application to a
 * file in the test workspace.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ExportDataOp extends AbstractApplicationActionOp<ExportData> {

	/**
	 * Creates a {@link ExportDataOp} from configuration.
	 */
	public ExportDataOp(InstantiationContext context, ExportData config) {
		super(context, config);
	}

	@Override
	public Object processInternal(ActionContext context, Object argument) {
		BinaryData data = BinaryData.cast(context.resolve(config.getData()));
		ApplicationAssertions.assertNotNull(config, "No data available to export.", data);
		try {
			File file = new File(config.getExportFileName());
			File dir = file.getParentFile();
			if (! dir.exists()) {
				dir.mkdirs();
			}
			try (InputStream input = data.getStream()) {
				try (FileOutputStream out = new FileOutputStream(file)) {
					StreamUtilities.copyStreamContents(input, out);
				}
			}
		} catch (IOException ex) {
			ApplicationAssertions.fail(config, "Exporting test data failed.", ex);
		}
		return argument;
	}

}
