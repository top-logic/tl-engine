/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.model.types.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.tool.export.AbstractExcelExportHandler;
import com.top_logic.tool.export.AbstractExportHandler;
import com.top_logic.tool.export.ExportHandler;
import com.top_logic.tool.export.ExportResult;

/**
 * {@link ExportHandler} that generates a textual representation of a model object.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DemoExportTypes extends AbstractExcelExportHandler {

	/**
	 * Creates a {@link DemoExportTypes}.
	 * 
	 * @param anExportID
	 *        See {@link AbstractExportHandler#AbstractExportHandler(String)}.
	 */
	public DemoExportTypes(String anExportID) {
		super(anExportID);
	}

	@Override
	public void exportObject(Object aModel, ExportResult aResult) {
		try {
			OutputStream out = aResult.getOutputStream();
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, "utf-8"));

			Wrapper exportedObject = (Wrapper) aModel;
			writeObject(writer, exportedObject);

			writer.flush();
			aResult.setFileExtension("txt");
			aResult.setFileDisplaynameKey(I18NConstants.DEMO_EXPORT_FILENAME__NAME.fill(exportedObject.getName()));
		} catch (IOException ex) {
			aResult.setFailureKey(I18NConstants.DEMO_EXPORT_FAILURE__MSG.fill(ex.getMessage()));
		}
	}

	protected void writeObject(PrintWriter writer, Wrapper exportedObject) {
		writeObject(writer, exportedObject, "");
	}

	protected void writeObject(PrintWriter writer, Wrapper exportedObject, String indent) {
		writer.println(indent + "Properties of " + exportedObject.getName());
		writeProperties(writer, exportedObject, indent + "\t");
	}

	protected void writeProperties(PrintWriter writer, Wrapper exportedObject, String indent) {
		for (TLStructuredTypePart attribute : exportedObject.tType().getAllParts()) {
			if (DisplayAnnotations.isHidden(attribute)) {
				continue;
			}
			String attributeName = attribute.getName();
			Object attributeValue = exportedObject.getValue(attributeName);

			String valueLabel;
			if (attributeValue != null) {
				valueLabel = MetaLabelProvider.INSTANCE.getLabel(attributeValue);

				if (valueLabel == null) {
					valueLabel = "";
				} else {
					valueLabel = valueLabel.replaceAll("\n", "\n" + indent + "\t");
				}
			} else {
				valueLabel = "";
			}

			writer.println(indent + attributeName + ": " + valueLabel);
		}
	}

	@Override
	public boolean canExport(Object aModel) {
		return true;
	}

}
