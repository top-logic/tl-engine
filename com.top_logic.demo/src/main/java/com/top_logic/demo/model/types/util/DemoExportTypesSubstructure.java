/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.model.types.util;

import java.io.PrintWriter;
import java.util.List;

import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.tool.export.AbstractExportHandler;

/**
 * Demo export that exports all properties of objects in a structure.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DemoExportTypesSubstructure extends DemoExportTypes {

	/**
	 * Creates a {@link DemoExportTypesSubstructure}.
	 * 
	 * @param anExportID
	 *        See {@link AbstractExportHandler#AbstractExportHandler(String)}.
	 */
	public DemoExportTypesSubstructure(String anExportID) {
		super(anExportID);
	}

	@Override
	protected void writeObject(PrintWriter writer, Wrapper exportedObject, String indent) {
		super.writeObject(writer, exportedObject, indent);
		if (exportedObject instanceof StructuredElement) {
			List<? extends StructuredElement> children = ((StructuredElement) exportedObject).getChildren();

			for (StructuredElement child : children) {
				writeObject(writer, (Wrapper) child, indent + "\t");
			}
		}
	}
}
