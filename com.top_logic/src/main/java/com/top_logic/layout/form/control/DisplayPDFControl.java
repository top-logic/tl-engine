/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;

import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.dsa.util.MimeTypes;
import com.top_logic.layout.AbstractDispatchingContentHandler;
import com.top_logic.layout.ContentHandler;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.FrameScope;
import com.top_logic.layout.URLBuilder;
import com.top_logic.layout.URLParser;
import com.top_logic.layout.URLPathBuilder;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.AttachedPropertyListener;
import com.top_logic.layout.basic.ConstantControl;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.mig.html.HTMLConstants;

/**
 * Control rendering an PDF within an {@link HTMLConstants#IFRAME}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DisplayPDFControl extends ConstantControl<BinaryDataSource> {

	/** Content type of PDF documents. */
	public static final String PDF_CONTENT_TYPE = "application/pdf";

	/**
	 * ControlProvider for creation of an {@link Control} to display a PDF value of a
	 * {@link FormField}.
	 */
	public static final ControlProvider CONTROL_PROVIDER = new ControlProvider() {

		@Override
		public Control createControl(Object model, String style) {
			FormField f = (FormField) model;
			BinaryDataSource content;
			if (f.hasValue()) {
				content = (BinaryDataSource) (f.getValue());
			} else {
				content = null;
			}
			DisplayPDFControl pdfView = new DisplayPDFControl(content);

			AbstractFormFieldControl displayPDFFormWrapper = new AbstractFormFieldControl(f) {

				@Override
				protected void internalHandleValueChanged(FormField field, Object oldValue, Object newValue) {
					pdfView.setModel((BinaryDataSource) (newValue));
				}

				@Override
				protected void internalHandleDisabledEvent(FormMember sender, Boolean oldValue, Boolean newValue) {
					// Ignore. Control is only display view.
				}

				@Override
				protected void writeImmutable(DisplayContext context, TagWriter out) throws IOException {
					pdfView.write(context, out);
				}

				@Override
				protected void writeEditable(DisplayContext context, TagWriter out) throws IOException {
					pdfView.write(context, out);
				}
			};
			ValueListener vl = new ValueListener() {

				@Override
				public void valueChanged(FormField field, Object oldValue, Object newValue) {
					pdfView.setModel((BinaryDataSource) newValue);
				}
			};
			displayPDFFormWrapper.addListener(AbstractControlBase.ATTACHED_PROPERTY, new AttachedPropertyListener() {
				
				@Override
				public void handleAttachEvent(AbstractControlBase sender, Boolean oldValue, Boolean newValue) {
					if (newValue.booleanValue()) {
						f.addValueListener(vl);
					} else {
						f.removeValueListener(vl);
					}
				}
			});
			return displayPDFFormWrapper;
		}
	};

	private class PDFContentHandler extends AbstractDispatchingContentHandler {

		private final DeliverContentHandler _pdfDisplay = new DeliverContentHandler();

		public PDFContentHandler() {
		}

		@Override
		protected void internalHandleContent(DisplayContext context, URLParser url) throws IOException {
			handleNotFound(context, null);
		}

		@Override
		protected ContentHandler getContentHandler(String id) {
			if (pdfName().equals(id)) {
				_pdfDisplay.setData(getModel());
				return _pdfDisplay;
			}
			return null;
		}

		public String getPdfURL(DisplayContext context) {
			URLBuilder url = frameScope().getURL(context, this);
			// Use the name of the pdf as last resource to ensure correct download name.
			url.addResource(pdfName());
			return url.getURL();
		}

		private String pdfName() {
			String name;
			if (getModel() != null) {
				name = getModel().getName();
			} else {
				// Is actually not used.
				name = "noPDF.pdf";
			}
			return name;
		}

	}


	private PDFContentHandler _display = new PDFContentHandler();

	/**
	 * Creates a new {@link DisplayPDFControl}.
	 * 
	 * @param model
	 *        The {@link BinaryDataSource} containing a PDF to display.
	 */
	public DisplayPDFControl(BinaryDataSource model) {
		super(model);
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {

		BinaryDataSource document = getModel();
		if (document == null) {
			out.beginBeginTag(H4);
			writeControlAttributes(context, out);
			out.endBeginTag();
			out.beginTag(DIV);
			Icons.PDF_MISSING.write(context, out);
			out.endTag(DIV);
			out.beginTag(DIV);
			out.writeText(context.getResources().getString(I18NConstants.PDF_DISPLAY_NO_DOCUMENT_AVAILABLE));
			out.endTag(DIV);
			out.endTag(H4);
			return;
		}
		String contentType = MimeTypes.getInstance().getMimeType(document.getName());
		if (contentType != null && contentType.toLowerCase().equals(PDF_CONTENT_TYPE)) {
			out.beginBeginTag(IFRAME);
			writeControlAttributes(context, out);
			String pdfURL = _display.getPdfURL(context);
			out.writeAttribute(SRC_ATTR, displayPdfURL(context, pdfURL).getURL());
			out.writeAttribute(WIDTH_ATTR, "100%");
			out.writeAttribute(HEIGHT_ATTR, "100%");
			out.endBeginTag();
			out.endTag(IFRAME);
			return;
		}

		out.beginBeginTag(H4);
		writeControlAttributes(context, out);
		out.endBeginTag();
		ResKey msg = I18NConstants.PDF_DISPLAY_INVALID_DOCUMENT__CONTENT_TYPE__DOCUMENT_NAME
			.fill(contentType, document.getName());
		out.writeText(context.getResources().getString(msg));
		out.endTag(H4);

	}

	/**
	 * Creates an {@link URLPathBuilder} to display the PDF represented by the given
	 * <code>pdfURL</code>.
	 * 
	 * See ext.org.mozilla.pdfjs.
	 * 
	 * @param pdfURL
	 *        URL identifying the PDF document on the server.
	 */
	public static URLPathBuilder displayPdfURL(DisplayContext context, String pdfURL) {
		URLPathBuilder builder = URLPathBuilder.newBuilder(context);
		// Path to viewer.HTML in ext.org.mozilla.pdfjs
		builder.addResource("html");
		builder.addResource("pdfjs");
		builder.addResource("web");
		builder.addResource("viewer.html");
		builder.appendParameter("file", pdfURL);
		return builder;
	}

	@Override
	protected void internalAttach() {
		super.internalAttach();
		frameScope().registerContentHandler(frameScope().createNewID(), _display);
	}

	@Override
	protected void internalDetach() {
		frameScope().deregisterContentHandler(_display);
		super.internalDetach();
	}

	FrameScope frameScope() {
		return getScope().getFrameScope();
	}

	@Override
	protected String getTypeCssClass() {
		return "cPDFDisplay";
	}

}
