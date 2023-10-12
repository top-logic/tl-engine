/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.formeditor.export.pdf;

import java.io.IOException;
import java.util.Map;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.config.order.DisplayInherited;
import com.top_logic.basic.config.order.DisplayInherited.DisplayStrategy;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.element.layout.formeditor.builder.ConfiguredDynamicFormBuilder;
import com.top_logic.element.layout.formeditor.builder.FormDefinitionUtil;
import com.top_logic.element.layout.formeditor.builder.TypedForm;
import com.top_logic.element.layout.formeditor.builder.TypedFormDefinition;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.values.edit.annotation.ControlProvider;
import com.top_logic.layout.wysiwyg.ui.MacroControlProvider;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLType;
import com.top_logic.model.form.definition.FormDefinition;
import com.top_logic.model.form.implementation.FormEditorContext;
import com.top_logic.model.form.implementation.FormElementTemplateProvider;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.MacroFormat;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.config.TextWithEmbeddedExpressionsFormat;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.NullModelHidden;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link AbstractCommandHandler} exporting the model of a component as PDF document.
 * 
 * <p>
 * When an export definition for the type of the model is given, that definition is used. If no such
 * definition is found and a form definition for the model type is locally defined at the component,
 * this definition is also used for the export.
 * </p>
 * 
 * <p>
 * When no such locally defined form, can be found the most specific export definition for the type,
 * defined in the model, and at least the most specific layout form (defined in the model) is used.
 * </p>
 * 
 * <p>
 * When no form can be found at all, a generic export form is used.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@InApp
@Label("Export model as PDF")
public class DefaultPDFExportCommand extends AbstractCommandHandler {

	/**
	 * Configuration of a {@link DefaultPDFExportCommand}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@DisplayOrder({
		Config.RESOURCE_KEY_PROPERTY_NAME,
		Config.PDF_NAME,
		Config.TARGET,
		Config.HEADER,
		Config.EXPORT_DESCRIPTIONS,
	})
	@DisplayInherited(DisplayStrategy.APPEND)
	@TagName("default-pdf-export")
	/**
	 * Configuration of an {@link DefaultPDFExportCommand}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AbstractCommandHandler.Config {

		/** Configuration name of {@link #getPDFName()}. */
		String PDF_NAME = "pdf-name";

		/** Configuration name of {@link #getHeader()}. */
		String HEADER = "header";

		/** Configuration name of {@link #getExportDescriptions()}. */
		String EXPORT_DESCRIPTIONS = "export-descriptions";

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

		/**
		 * Name of the downloaded PDF.
		 * 
		 * <p>
		 * The expression is evaluated with the exported model as the only argument. The model can
		 * be accessed with the variable {@link TextWithEmbeddedExpressionsFormat#MODEL}.
		 * </p>
		 * 
		 * <p>
		 * The name of the PDF file must end with ".pdf".
		 * </p>
		 */
		@NonNullable
		@FormattedDefault("{dateFormat('yyyy-MM-dd').format(now())}-{$model}.pdf")
		@Label("PDF name")
		@Name(PDF_NAME)
		@Format(TextWithEmbeddedExpressionsFormat.class)
		Expr getPDFName();

		@Override
		@FormattedDefault(TARGET_MODEL_SELF)
		ModelSpec getTarget();

		@Override
		@FormattedDefault("tl.command.defaultPDFExport")
		ResKey getResourceKey();

		/**
		 * The {@link Expr} computing the heading line of the PDF export. When nothing is
		 * configured, no header line is exported.
		 * 
		 * <p>
		 * The expression is evaluated with the exported model as the only argument. The model can
		 * be accessed with the variable {@link MacroFormat#MODEL}.
		 * </p>
		 */
		@ControlProvider(MacroControlProvider.class)
		@Format(MacroFormat.class)
		@Name(HEADER)
		Expr getHeader();

		/**
		 * Configuration of the export description depending on the type of the exported model. When
		 * no export description can be found for the type of the model, a default export
		 * description is used.
		 */
		@Name(EXPORT_DESCRIPTIONS)
		@Key(TypedFormDefinition.TYPE)
		Map<TLModelPartRef, TypedFormDefinition> getExportDescriptions();

		@Override
		@ClassDefault(DefaultPDFExportCommand.class)
		Class<? extends CommandHandler> getImplementationClass();

	}

	private Map<TLType, FormDefinition> _configuredExportForms;

	private QueryExecutor _pdfName;

	/**
	 * Creates a new {@link DefaultPDFExportCommand}.
	 */
	public DefaultPDFExportCommand(InstantiationContext context, Config config) {
		super(context, config);
		_configuredExportForms = FormDefinitionUtil.createTypedFormMapping(config.getExportDescriptions());
		_pdfName = QueryExecutor.compile(config.getPDFName());
	}

	/**
	 * Type-safe access to {@link #getConfig()}.
	 */
	public Config config() {
		return (Config) getConfig();
	}

	@Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> someArguments) {

		TLObject exportObject = (TLObject) model;
		String name = getExportName(component, (TLObject) model);
		context.getWindowScope().deliverContent(new PDFData(name, exportObject) {
			@Override
			protected TypedForm lookupForm() {
				return lookupExport(component, getExportObject());
			}

			@Override
			protected PDFExport createExporter() {
				return exporter(component, getExportObject());
			}
		});

		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Determines the name of the exported file.
	 * @param component
	 *        {@link LayoutComponent} creating the PDF export.
	 * @param model
	 *        The exported model.
	 */
	protected String getExportName(LayoutComponent component, TLObject model) {
		String fileName = _pdfName.execute(model).toString();
		// Replace illegal file name characters
		return fileName.replaceAll("[/;:\\\\]", "_");
	}

	/**
	 * Creates the actual {@link PDFExport exporter}.
	 * @param component
	 *        {@link LayoutComponent} creating the PDF export.
	 * @param model
	 *        The exported model.
	 */
	protected PDFExport exporter(LayoutComponent component, TLObject model) {
		Expr header = config().getHeader();
		if (header == null) {
			return new PDFExport();
		} else {
			return exporter(header, model);
		}
	}

	private TypedForm lookupExport(LayoutComponent aComponent, TLObject model) {
		if (model == null) {
			// Must not occur, because command is hidden for null model.
			throw new TopLogicException(I18NConstants.ERROR_NO_MODEL);
		}
		TLStructuredType modelType = model.tType();

		// Look up export defined locally at the current command.
		TypedForm configuredExport = TypedForm.findForm(_configuredExportForms, modelType);
		if (configuredExport != null) {
			return configuredExport;
		}

		// Look up display form locally defined at the current component.
		if (aComponent instanceof FormComponent) {
			ModelBuilder builder = ((FormComponent) aComponent).getBuilder();
			if (builder instanceof ConfiguredDynamicFormBuilder) {
				Map<TLType, FormDefinition> displayForms = ((ConfiguredDynamicFormBuilder) builder).getConfiguredForms();
				TypedForm componentForm = TypedForm.findForm(displayForms, modelType);
				if (componentForm != null) {
					return componentForm;
				}
			}
		}

		// Use globally defined export from the model.
		return TypedForm.lookupExport(modelType);
	}

	@Override
	protected ExecutabilityRule intrinsicExecutability() {
		return CombinedExecutabilityRule.combine(super.intrinsicExecutability(), NullModelHidden.INSTANCE);
	}

	private PDFExport exporter(Expr header, TLObject model) {
		return new PDFExport() {

			@Override
			protected void writeBodyContent(DisplayContext context, TagWriter out,
					FormElementTemplateProvider exportDescription, FormEditorContext renderContext)
					throws IOException {
				KnowledgeBase knowledgeBase = model.tKnowledgeBase();
				TLModel tlModel = model.tType().getModel();
				SearchExpression expr = SearchBuilder.toSearchExpression(tlModel, header);
				QueryExecutor executor = QueryExecutor.compile(knowledgeBase, tlModel, expr);
				// Executing writes the content to the TagWriter of the evalContext
				executor.executeWith(context, out, Args.some(model));

				out.beginTag(HTMLConstants.DIV);
				super.writeBodyContent(context, out, exportDescription, renderContext);
				out.endTag(HTMLConstants.DIV);
			}

		};
	}

}
