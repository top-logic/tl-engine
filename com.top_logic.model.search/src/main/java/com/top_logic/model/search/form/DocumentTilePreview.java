/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.form;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.channel.linking.impl.ChannelLinking;
import com.top_logic.layout.form.control.DisplayPDFControl;
import com.top_logic.mig.html.layout.tiles.component.ComponentTile;
import com.top_logic.mig.html.layout.tiles.component.StaticPreview;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * Tile preview to display a PDF document as preview in a tile.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DocumentTilePreview extends StaticPreview<DocumentTilePreview.Config> {

	/**
	 * Configuration of a {@link DocumentTilePreview}.
	 */
	public interface Config extends StaticPreview.Config {

		/**
		 * Search expression used to calculate the document to be displayed from the business model.
		 */
		@Mandatory
		Expr getDocument();

		/**
		 * Search expression used to calculate the model that is used to determine document from.
		 */
		ModelSpec getModel();

	}

	private final QueryExecutor _documentExpr;

	private ChannelLinking _model;

	/**
	 * Creates a new {@link DocumentTilePreview}.
	 */
	public DocumentTilePreview(InstantiationContext context, Config config) {
		super(context, config);
		_documentExpr = QueryExecutor.compile(getConfig().getDocument());
		_model = context.getInstance(config.getModel());
	}

	@Override
	protected String getAdditionalPreviewClass(ComponentTile tile) {
		return "documentPreview";
	}

	@Override
	protected HTMLFragment image(ComponentTile tile) {
		if (getConfig().getIcon() != null) {
			// A preview icon is configured. Use this.
			return super.image(tile);
		}
		BinaryDataSource document = searchDocument(ChannelLinking.eval(tile.getTileComponent(), _model));
		return new DisplayPDFControl(document);
	}

	private BinaryDataSource searchDocument(Object businessModel) {
		if (!(businessModel instanceof TLObject)) {
			return null;
		}
		Object searchResult = CollectionUtil.getFirst(_documentExpr.execute(businessModel));
		if (searchResult instanceof BinaryDataSource) {
			return (BinaryDataSource) searchResult;
		}
		return null;
	}

}
