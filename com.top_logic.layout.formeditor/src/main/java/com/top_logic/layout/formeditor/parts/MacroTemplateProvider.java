/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.formeditor.parts;

import static com.top_logic.layout.form.template.model.Templates.*;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.ImageProvider;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.form.ReactiveFormCSS;
import com.top_logic.model.form.implementation.AbstractFormElementProvider;
import com.top_logic.model.form.implementation.FormEditorContext;
import com.top_logic.model.form.implementation.FormMode;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.MacroFormat;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.util.model.ModelService;

/**
 * {@link AbstractFormElementProvider} for {@link MacroPart}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MacroTemplateProvider extends AbstractFormElementProvider<MacroPart> {

	private static final ImageProvider IMAGE_PROVIDER =
		ImageProvider.constantImageProvider(Icons.TOOLBOX_MACRO);

	/**
	 * Creates a new {@link MacroTemplateProvider}.
	 */
	public MacroTemplateProvider(InstantiationContext context, MacroPart config) {
		super(context, config);
	}

	@Override
	public boolean getWholeLine(TLStructuredType modelType) {
		return getConfig().getWholeLine();
	}

	@Override
	public boolean getIsTool() {
		return true;
	}

	@Override
	public ImageProvider getImageProvider() {
		return IMAGE_PROVIDER;
	}

	@Override
	public ResKey getLabel(FormEditorContext context) {
		return I18NConstants.MACRO_PART_LABEL;
	}

	@Override
	protected DisplayDimension getDialogWidth() {
		return DisplayDimension.dim(680, DisplayUnit.PIXEL);
	}

	@Override
	protected DisplayDimension getDialogHeight() {
		return DisplayDimension.dim(470, DisplayUnit.PIXEL);
	}

	@Override
	protected HTMLTemplateFragment createDisplayTemplate(FormEditorContext context) {
		Expr expression = getConfig().getExpr();
		if (expression != null) {
			if (context.getFormMode() == FormMode.DESIGN) {
				return designTemplate(expression);
			} else {
				return displayTemplate(context, expression);
			}
		} else {
			return contentBox(div(
				css(inputCellCSS(context) + " " + ReactiveFormCSS.RF_EMPTY_CELL),
				resource(I18NConstants.MACRO_PART_LABEL)), getWholeLine(context.getFormType()));
		}
	}

	private HTMLTemplateFragment displayTemplate(FormEditorContext context, Expr expression) {
		return contentBox(htmlTemplate(displayFragment(context.getModel(), expression)),
			getWholeLine(context.getFormType()));
	}

	private HTMLFragment displayFragment(TLObject model, Expr expression) {
		return new HTMLFragment() {

			@Override
			public void write(DisplayContext context, TagWriter out) throws IOException {
				KnowledgeBase knowledgeBase = PersistencyLayer.getKnowledgeBase();
				TLModel tlModel = ModelService.getApplicationModel();
				SearchExpression expr = SearchBuilder.toSearchExpression(tlModel, expression);
				QueryExecutor executor = QueryExecutor.compile(knowledgeBase, tlModel, expr);
				// Executing writes the content to the TagWriter of the evalContext
				executor.executeWith(context, out, Args.some(model));
			}
		};
	}

	private HTMLTemplateFragment designTemplate(Expr expression) {
		return contentBox(htmlSource(MacroFormat.INSTANCE.getSpecification(expression)));
	}

}

