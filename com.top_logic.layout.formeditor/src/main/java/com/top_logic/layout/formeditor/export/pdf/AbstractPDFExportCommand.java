/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.formeditor.export.pdf;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import com.lowagie.text.DocumentException;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.dsa.util.MimeTypes;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DummyControlScope;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.DummyDisplayContext;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLObject;
import com.top_logic.model.form.implementation.FormEditorContext;
import com.top_logic.model.form.implementation.FormElementTemplateProvider;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.util.TLContext;

/**
 * Abstract super class for {@link CommandHandler} exporting a PDF defined by a
 * {@link FormElementTemplateProvider}.
 * 
 * @see PDFExport
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractPDFExportCommand extends AbstractCommandHandler {

	/**
	 * Configuration of an {@link AbstractPDFExportCommand}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AbstractCommandHandler.Config {

		@Override
		@FormattedDefault(SimpleBoundCommandGroup.EXPORT_NAME)
		CommandGroupReference getGroup();

		@Override
		@FormattedDefault("theme:ICONS_EXPORT_PDF")
		ThemeImage getImage();

		@Override
		@FormattedDefault("theme:ICONS_EXPORT_PDFDISABLED")
		ThemeImage getDisabledImage();

		@Override
		@StringDefault(CommandHandlerFactory.EXPORT_BUTTONS_GROUP)
		String getClique();

	}

	/**
	 * Creates a new {@link AbstractPDFExportCommand}.
	 */
	public AbstractPDFExportCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model,
			Map<String, Object> someArguments) {
		
		aContext.getWindowScope().deliverContent(new BinaryDataSource() {
			
			@Override
			public String getName() {
				return getExportName(aContext, aComponent, (TLObject) model, someArguments);
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
				// Note: Must set up a separate display context, to allow one-time rendering of
				// controls during export. The "current" display context is not available for
				// control rendering, since the current session is not in rendering mode.
				DisplayContext exportContext = new DummyDisplayContext()
					.initServletContext(DefaultDisplayContext.getDisplayContext().asServletContext());
				exportContext.initScope(new DummyControlScope());
				exportContext.installSubSessionContext(TLContext.getContext());

				try {
					TLObject exportObject = (TLObject) model;
					exporter(exportContext, aComponent, exportObject, someArguments).createPDFExport(exportContext, out,
						getExportDescription(exportContext, aComponent, exportObject, someArguments),
						getExportContext(exportContext, aComponent, exportObject, someArguments));
				} catch (DocumentException ex) {
					throw new IOException("Invalid PDF document.", ex);
				}
			}
		});
		
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Creates the export context object.
	 * 
	 * @param context
	 *        Export interaction.
	 * @param component
	 *        {@link LayoutComponent} creating the PDF export.
	 * @param model
	 *        The exported model.
	 * @param someArguments
	 *        Arguments given in
	 *        {@link #handleCommand(DisplayContext, LayoutComponent, Object, Map)}
	 * @return {@link FormEditorContext} retrieving all context informations for the export from.
	 */
	protected abstract FormEditorContext getExportContext(DisplayContext context, LayoutComponent component,
			TLObject model, Map<String, Object> someArguments);

	/**
	 * Creates the description of the exported PDF.
	 * 
	 * @param context
	 *        Export interaction.
	 * @param component
	 *        {@link LayoutComponent} creating the PDF export.
	 * @param model
	 *        The exported model.
	 * @param someArguments
	 *        Arguments given in
	 *        {@link #handleCommand(DisplayContext, LayoutComponent, Object, Map)}
	 * @return {@link FormElementTemplateProvider} defining the exported PDF layout as HTML.
	 */
	protected abstract FormElementTemplateProvider getExportDescription(DisplayContext context,
			LayoutComponent component, TLObject model, Map<String, Object> someArguments);

	/**
	 * Determines the name of the exported file.
	 * 
	 * @param context
	 *        Export interaction.
	 * @param component
	 *        {@link LayoutComponent} creating the PDF export.
	 * @param model
	 *        The exported model.
	 * @param someArguments
	 *        Arguments given in
	 *        {@link #handleCommand(DisplayContext, LayoutComponent, Object, Map)}
	 */
	protected abstract String getExportName(DisplayContext context, LayoutComponent component, TLObject model,
			Map<String, Object> someArguments);

	/**
	 * Creates the actual {@link PDFExport exporter}.
	 * 
	 * @param context
	 *        Export interaction.
	 * @param component
	 *        {@link LayoutComponent} creating the PDF export.
	 * @param model
	 *        The exported model.
	 * @param someArguments
	 *        Arguments given in
	 *        {@link #handleCommand(DisplayContext, LayoutComponent, Object, Map)}
	 */
	protected PDFExport exporter(DisplayContext context, LayoutComponent component, TLObject model,
			Map<String, Object> someArguments) {
		return new PDFExport();
	}

}

