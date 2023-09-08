/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.form;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.layout.form.FormMember;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.tiles.document.AbstractDisplayDocumentBuilder;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link AbstractDisplayDocumentBuilder} computing the document to display using an {@link Expr}
 * from the given business model.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DisplayDocumentBuilder extends AbstractDisplayDocumentBuilder<DisplayDocumentBuilder.Config> {

	/**
	 * Typed configuration interface definition for {@link DisplayDocumentBuilder}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends PolymorphicConfiguration<DisplayDocumentBuilder> {

		/**
		 * Search expression used to calculate the document to be displayed from the business model.
		 */
		@Mandatory
		Expr getDocument();

	}

	private final QueryExecutor _documentExpr;

	/**
	 * Create a {@link DisplayDocumentBuilder}.
	 * 
	 * @param context
	 *        The {@link InstantiationContext} to create the new object in.
	 * @param config
	 *        The configuration object to be used for instantiation.
	 */
	public DisplayDocumentBuilder(InstantiationContext context, Config config) {
		super(context, config);
		_documentExpr = QueryExecutor.compile(getConfig().getDocument());
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return searchDocument(aModel) != null;
	}


	@Override
	protected FormMember createField(String fieldName, Object businessModel, LayoutComponent component) {
		BinaryDataSource document = searchDocument(businessModel);
		return createDisplayPDFField(fieldName, document);
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

