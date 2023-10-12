/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.execution.export;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;

import org.xhtmlrenderer.pdf.ITextRenderer;

import com.lowagie.text.DocumentException;

import com.top_logic.basic.Settings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.bpe.bpml.model.Participant;
import com.top_logic.bpe.execution.model.ProcessExecution;
import com.top_logic.dsa.util.MimeTypes;
import com.top_logic.knowledge.gui.layout.upload.DefaultDataItem;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.formeditor.export.pdf.PDFExport;
import com.top_logic.layout.table.model.I18NConstants;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.form.definition.FormDefinition;
import com.top_logic.model.form.implementation.FormEditorContext;
import com.top_logic.model.form.implementation.FormElementTemplateProvider;
import com.top_logic.tool.boundsec.conditional.CommandStep;
import com.top_logic.tool.boundsec.conditional.Hide;
import com.top_logic.tool.boundsec.conditional.PreconditionCommandHandler;
import com.top_logic.tool.boundsec.conditional.Success;
import com.top_logic.util.error.TopLogicException;

/**
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class ExportAsPdfHandler extends PreconditionCommandHandler {

	public ExportAsPdfHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected CommandStep prepare(LayoutComponent component, Object model, Map<String, Object> arguments) {
		if (model instanceof Wrapper) {
			Wrapper wrap = (Wrapper) model;
			FormDefinition exportDescription = getExportDescription(wrap);
			if (exportDescription != null) {
				return new Success() {

					@Override
					protected void doExecute(DisplayContext context) {
						exportAsPdf(exportDescription, wrap, context);
					}
				};
			}
		}
		return new Hide();
	}

	void exportAsPdf(FormDefinition exportDescription, Wrapper wrapper, DisplayContext aContext) {
		String ending = "pdf";
		File tmpFile;
		try {
			tmpFile =
				File.createTempFile("SimpleDataExport", ending, Settings.getInstance().getTempDir());
			try (FileOutputStream pdfOut = new FileOutputStream(tmpFile)) {
				exporter().createPDFExport(pdfOut, TypedConfigUtil.createInstance(exportDescription), wrapper);
			}
		} catch (Exception ex) {
			throw new TopLogicException(I18NConstants.ERROR_TABLE_EXPORT, ex);
		}
		
		deliverPdfContent(aContext, tmpFile);
	}

	private PDFExport exporter() {
		return new PDFExport() {

			@Override
			protected void writeBodyContent(DisplayContext context, TagWriter out,
					FormElementTemplateProvider exportDescription, FormEditorContext exportContext) throws IOException {
				out.beginTag(HTMLConstants.H1);
				out.writeText("<i>TopLogic</i> BPE");
				out.endTag(HTMLConstants.H1);
				out.writeText("Export vom " + HTMLFormatter.getInstance().formatDateTime(new Date()));

				super.writeBodyContent(context, out, exportDescription, exportContext);
			}
		};
	}

	private FormDefinition getExportDescription(Wrapper wrap) {
		if(wrap instanceof ProcessExecution) {
			ProcessExecution pe = (ProcessExecution)wrap;
			Participant participant = pe.getProcess().getParticipant();
			FormDefinition exportDescription = participant.getExportDescription();
			if (exportDescription != null) {
				return exportDescription;
			}
			return participant.getDisplayDescription();
		}
		return null;
	}

	private void deliverPdfContent(DisplayContext context, File tempExcelFile) {
		String downloadFileName =
			context.getResources().getString(getConfig().getResourceKey().suffix("pdfExport")) + ".pdf";
		BinaryData dataItem = BinaryDataFactory.createBinaryData(tempExcelFile);
		String contentType = MimeTypes.getInstance().getMimeType(tempExcelFile.getName());
		// Needs to wrap BinaryData created from temporary file to get correct name.
		DefaultDataItem downloadItem = new DefaultDataItem(downloadFileName, dataItem, contentType);
		context.getWindowScope().deliverContent(downloadItem);
	}


	/**
	 * Creates a PDF from the given XHTML and writes it in the given output file. The scaling is
	 * done with the parameters dotsPerPoint and dotsPerPixel which means that the number of dots of
	 * the html (dotsPerPoint) are mapped to the number of dots of the PDF (dotsPerPixel). (e.g.
	 * "2:1" means that 2 dots of the html are mapped to one dot of the PDF document so the html is
	 * scaled down)
	 * 
	 * See POS for a more elaborate use of this export
	 *
	 * @param theXHTML
	 *        The XHTML that has to be converted into PDF
	 * @param output
	 *        Name of the output file to be created
	 */
	protected void flyingSaucerHtmlToPdf(String theXHTML, File output)
			throws DocumentException, IOException {
		try (OutputStream os = new FileOutputStream(output)) {
			ITextRenderer renderer = new ITextRenderer(1f, 1);
			renderer.setDocumentFromString(theXHTML);
			renderer.layout();
			renderer.createPDF(os);
		}
	}

}
