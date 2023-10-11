/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.formeditor.export.pdf;

import java.io.IOException;
import java.io.OutputStream;

import com.lowagie.text.DocumentException;

import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.dsa.util.MimeTypes;
import com.top_logic.element.layout.formeditor.builder.TypedForm;
import com.top_logic.element.layout.formeditor.definition.PDFExportAnnotation;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.model.TLObject;
import com.top_logic.model.form.implementation.FormEditorContext;
import com.top_logic.model.form.implementation.FormElementTemplateProvider;

/**
 * {@link BinaryDataSource} that renders an object as PDF document.
 * 
 * <p>
 * To create the PDF, the export definition annotated to the object's type is used.
 * </p>
 * 
 * @see PDFExportAnnotation
 */
public class PDFData implements BinaryDataSource {

	private String _name;
	private TLObject _exportObject;

	/**
	 * Creates a {@link PDFData}.
	 * 
	 * @param name
	 *        The file name of the generated PDF.
	 */
	public PDFData(String name, TLObject exportObject) {
		_name = name;
		_exportObject = exportObject;
	}

	/**
	 * The model object being exported.
	 */
	public TLObject getExportObject() {
		return _exportObject;
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public long getSize() {
		return 0;
	}

	@Override
	public String getContentType() {
		return MimeTypes.getInstance().getMimeType(getName());
	}

	@Override
	public void deliverTo(OutputStream out) throws IOException {
		try {
			TypedForm exportForm = lookupForm();

			FormElementTemplateProvider template =
				TypedConfigUtil.createInstance(exportForm.getFormDefinition());
			FormEditorContext exportContext = new FormEditorContext.Builder()
				.formType(exportForm.getFormType())
				.concreteType(exportForm.getDisplayedType())
				.model(getExportObject())
				.build();
			createExporter().createPDFExport(DefaultDisplayContext.getDisplayContext(), out, template, exportContext);
		} catch (DocumentException ex) {
			throw new IOException("Invalid PDF document.", ex);
		}
	}

	/**
	 * Looks up the form definition for export.
	 */
	protected TypedForm lookupForm() {
		return TypedForm.lookupExport(getExportObject().tType());
	}

	/**
	 * Creates the exporter.
	 */
	protected PDFExport createExporter() {
		return new PDFExport();
	}

}
