/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.app.layout.tiles;

import static com.top_logic.layout.basic.fragments.Fragments.*;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.mig.html.layout.DefaultDescendingLayoutVisitor;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.tiles.component.ComponentTile;
import com.top_logic.mig.html.layout.tiles.component.StaticPreview;

/**
 * {@link ContextAwarePreview} displaying the number folders and documents in a {@link WebFolder}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ContextAwarePreview extends StaticPreview<StaticPreview.Config> {

	static class FindModelVisitor extends DefaultDescendingLayoutVisitor {

		private Object _model;

		@Override
		public boolean visitLayoutComponent(LayoutComponent aComponent) {
			if (_model != null) {
				return false;
			}
			_model = aComponent.getModel();
			return _model == null;
		}

		public Object getModel() {
			return _model;
		}
	}

	/**
	 * Creates a new {@link ContextAwarePreview}.
	 */
	public ContextAwarePreview(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected HTMLFragment descriptionContent(ComponentTile componentTile) {
		ResKey description = getConfig().getDescription();
		if (description != null) {
			LayoutComponent component = componentTile.getTileComponent();
			FindModelVisitor visitor = new FindModelVisitor();
			component.acceptVisitorRecursively(visitor);
			Object model = visitor.getModel();
			String label = MetaLabelProvider.INSTANCE.getLabel(model);
			ResKey message = ResKey.message(description, label);
			return message(message);
		} else {
			return empty();
		}
	}

}

