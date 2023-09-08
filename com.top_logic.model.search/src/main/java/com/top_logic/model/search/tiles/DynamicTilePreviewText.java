/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.tiles;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link TilePreviewPartProvider.Text} computing the text using an {@link Expr} from the model.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@InApp
public class DynamicTilePreviewText extends AbstractConfiguredInstance<DynamicTilePreviewText.Config>
		implements TilePreviewPartProvider.Text {

	/**
	 * Typed configuration interface definition for {@link DynamicTilePreviewText}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends PolymorphicConfiguration<DynamicTilePreviewText> {

		/**
		 * Expression computing the textual content from the model of the preview.
		 * 
		 * <p>
		 * The expression is expected to accept one input element, the model of the preview. It is
		 * expected that the {@link Expr} returns an Object, that can be displayed as textual value,
		 * e.g. a {@link ResKey} or plain string.
		 * </p>
		 */
		@Mandatory
		Expr getText();
		
	}

	private final QueryExecutor _text;

	/**
	 * Create a {@link DynamicTilePreviewText}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public DynamicTilePreviewText(InstantiationContext context, Config config) {
		super(context, config);
		_text = QueryExecutor.compile(config.getText());
	}

	@Override
	public HTMLFragment createPreviewPart(Object model) {
		Object text = _text.execute(model);
		if (text != null) {
			return rendered(text);
		}
		return Fragments.empty();
	}

	private static <T> HTMLFragment rendered(T obj) {
		return Fragments.rendered(renderer(obj), obj);
	}

	private static <T> Renderer<? super T> renderer(T obj) {
		return LabelProviderService.getInstance().getRenderer(obj);
	}
}

