/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.selector;

import java.util.List;

import com.top_logic.layout.SingleSelectionModel;
import com.top_logic.layout.component.TabComponent.TabbedLayoutComponent;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.tabbar.TabBarModel;
import com.top_logic.layout.tabbar.TabBarModel.TabBarListener;
import com.top_logic.mig.html.layout.Card;
import com.top_logic.model.search.persistency.expressions.SearchExpression;
import com.top_logic.model.search.persistency.expressions.SearchExpressionImpl;
import com.top_logic.model.search.ui.SearchComponent;
import com.top_logic.model.search.ui.SearchExpressionEditor;

/**
 * {@link SearchExpressionEditorChangeListener} actualize the selected search expression if the
 * SearchExpressionEditor to create a model based search expression is changed.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
final class SearchExpressionEditorChangeListener implements TabBarListener {

	private SearchComponent _searchComponent;

	/**
	 * Creates a {@link SearchExpressionEditorChangeListener} for the given {@link SearchComponent}.
	 */
	public SearchExpressionEditorChangeListener(SearchComponent searchComponent) {
		_searchComponent = searchComponent;
	}

	@Override
	public void notifySelectionChanged(SingleSelectionModel model, Object formerlySelectedObject,
			Object selectedObject) {
		if (selectedObject != null && formerlySelectedObject != null) {
			SearchExpression searchExpressionModel = getSearchExpressionModel(selectedObject);

			SelectField selectorField = getSearchSelectorField();
			Object singleSelection = selectorField.getSingleSelection();

			if (singleSelection == getOldSearchExpression(formerlySelectedObject)) {
				selectorField.setAsSingleSelection(searchExpressionModel);
			}
		}
	}

	private SelectField getSearchSelectorField() {
		SearchSelectorComponent searchSelector = _searchComponent.getSearchExpressionSelector();

		return searchSelector.getSelectorField();
	}

	private SearchExpressionImpl getOldSearchExpression(Object formerlySelectedObject) {
		TabbedLayoutComponent formerlyTab = (TabbedLayoutComponent) formerlySelectedObject;
		SearchExpressionEditor formerlyContent = (SearchExpressionEditor) formerlyTab.getContent();

		return formerlyContent.getLoadedPersistentSearchExpression();
	}

	private SearchExpression getSearchExpressionModel(Object selectedObject) {
		TabbedLayoutComponent tab = (TabbedLayoutComponent) selectedObject;
		SearchExpressionEditor content = (SearchExpressionEditor) tab.getContent();

		return content.getLoadedPersistentSearchExpression();
	}

	@Override
	public void notifyCardsChanged(TabBarModel sender, List<Card> oldAllCards) {
		// Nothing to do.
	}

	@Override
	public void inactiveCardChanged(TabBarModel sender, Card aCard) {
		// Nothing to do.
	}
}