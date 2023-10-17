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

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.bpe.bpml.model.Participant;
import com.top_logic.bpe.execution.model.ProcessExecution;
import com.top_logic.element.layout.formeditor.builder.TypedForm;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.formeditor.export.pdf.PDFData;
import com.top_logic.layout.formeditor.export.pdf.PDFExport;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLObject;
import com.top_logic.model.form.definition.FormDefinition;
import com.top_logic.model.form.implementation.FormEditorContext;
import com.top_logic.model.form.implementation.FormElementTemplateProvider;
import com.top_logic.tool.boundsec.conditional.CommandStep;
import com.top_logic.tool.boundsec.conditional.Hide;
import com.top_logic.tool.boundsec.conditional.PreconditionCommandHandler;
import com.top_logic.tool.boundsec.conditional.Success;

/**
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class ExportAsPdfHandler extends PreconditionCommandHandler {

	public ExportAsPdfHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected CommandStep prepare(LayoutComponent component, Object model, Map<String, Object> arguments) {
		if (model instanceof ProcessExecution) {
			ProcessExecution execution = (ProcessExecution) model;

			TypedForm exportDescription = getExportDescription(execution);
			if (exportDescription != null) {
				return new Success() {
					@Override
					protected void doExecute(DisplayContext context) {
						exportAsPdf(exportDescription, execution, context);
					}
				};
			}
		}
		return new Hide();
	}

	void exportAsPdf(TypedForm exportDescription, TLObject model, DisplayContext context) {
		String name =
			context.getResources().getString(getConfig().getResourceKey().suffix("pdfExport")) + ".pdf";

		context.getWindowScope().deliverContent(new PDFData(name, model) {
			@Override
			protected TypedForm lookupForm() {
				return exportDescription;
			}

			@Override
			protected PDFExport createExporter() {
				return exporter();
			}
		});
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

	private TypedForm getExportDescription(ProcessExecution model) {
		Participant participant = model.getProcess().getParticipant();
		FormDefinition exportDescription = participant.getExportDescription();
		if (exportDescription != null) {
			exportDescription = participant.getDisplayDescription();
		}
		return new TypedForm(model.tType(), participant.getModelType(), exportDescription, true);
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
