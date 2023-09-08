/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.gui.templates;

import java.io.IOError;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.basic.xml.XMLPrettyPrinter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.FormHandler;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.scripting.template.gui.templates.node.TemplateResource;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.template.xml.TemplateSchema;
import com.top_logic.template.xml.source.ResourceTransaction;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link CommandHandler} saving an edited template.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class TemplateApplyCommand extends AbstractCommandHandler {

	/**
	 * Creates a {@link TemplateApplyCommand} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TemplateApplyCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent,
			Object model, Map<String, Object> someArguments) {

		FormHandler form = form(aComponent);
		boolean ok = form.getFormContext().checkAll();
		if (!ok) {
			return AbstractApplyCommandHandler.createErrorResult(form.getFormContext());
		}

		TemplateEditModel editModel = getTemplateEditModel(aComponent);
		TemplateResource template = getTemplateResource(aComponent);

		// Create template skeleton.
		String content =
			"<t:template xmlns:t='http://www.top-logic.com/ns/template/1.0'>" +
			"<t:head>" +
			"<t:settings output-format='xml' output-xml-header='false' />" +
			"<t:parameters>" +
			"</t:parameters>" +
			"</t:head>" +
				"<t:body>" + editModel.getContent() + "</t:body>" +
			"</t:template>";
		
		Document document;
		try {
			document = DOMUtil.parse(content);
		} catch (IllegalArgumentException ex) {
			throw new TopLogicException(
				I18NConstants.ERROR_TEMPLATE_INVALID__ERROR.fill(ex.getMessage()));
		}

		Set<String> names = new HashSet<>();

		// Fill in parameters.
		updateParameters:
		for (Element head : DOMUtil.elementsNS(document.getDocumentElement(), TemplateSchema.TEMPLATE_NS,
			TemplateSchema.HEAD_ELEMENT)) {
			for (Element parameters : DOMUtil.elementsNS(head, TemplateSchema.TEMPLATE_NS,
				TemplateSchema.PARAMETERS_ELEMENT)) {
				for (TemplateEditModel.Parameter param : editModel.getParameters()) {
					String name = param.getName();
					if (!names.add(name)) {
						throw new TopLogicException(
							I18NConstants.ERROR_DUPLICATE_PARAMETER__NAME.fill(name));
					}

					Element paramElement =
						document.createElementNS(TemplateSchema.TEMPLATE_NS, TemplateSchema.PARAMETER_ELEMENT);
					paramElement.setAttributeNS(null, TemplateSchema.NAME_ATTRIBUTE, name);
					String defaultValue = param.getDefault();
					if (!StringServices.isEmpty(defaultValue)) {
						paramElement.setAttributeNS(null, TemplateSchema.DEFAULT_ATTRIBUTE, defaultValue);
					}
					paramElement.setAttributeNS(null, TemplateSchema.TYPE_ATTRIBUTE, "string");

					parameters.appendChild(paramElement);
				}
				break updateParameters;
			}
		}

		// Pretty-print the result.
		try {
			try (ResourceTransaction tx = template.update()) {
				try (OutputStream out = tx.open()) {
					XMLPrettyPrinter printer =
						new XMLPrettyPrinter(out,
							((AbstractTemplateFormBuilder.Config) ((FormComponent.Config) aComponent.getConfig())
								.getModelBuilder()).getXMLStorage());
					try {
						printer.write(document);
					} finally {
						printer.close();
					}
				}
				tx.commit();
			}
		} catch (IOException ex) {
			throw new IOError(ex);
		}

		aComponent.fireModelModifiedEvent(template, aComponent);

		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * The context component.
	 */
	protected final FormHandler form(LayoutComponent component) {
		return (FormHandler) component;
	}

	/**
	 * The template being edited.
	 */
	protected final TemplateEditModel getTemplateEditModel(LayoutComponent component) {
		return (TemplateEditModel) EditorFactory.getModel(form(component));
	}

	/**
	 * The {@link TemplateResource} being edited / saved.
	 */
	protected abstract TemplateResource getTemplateResource(LayoutComponent component);
}
