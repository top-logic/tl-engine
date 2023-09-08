/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.themeedit.browser.providers.theme;

import java.util.List;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.gui.config.ThemeConfig;
import com.top_logic.layout.form.declarative.DeclarativeFormBuilder;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.themeedit.browser.providers.theme.ThemeFormBuilder.EditModel;

/**
 * {@link DeclarativeFormBuilder} for a theme configuration.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class ThemeFormBuilder extends DeclarativeFormBuilder<ThemeConfig, EditModel> {

	/**
	 * Model of the displayed form.
	 *
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	@DisplayOrder({
		ThemeConfig.ID_PROPERTY,
		ThemeConfig.EXTENDS_PROPERTY,
		ThemeConfig.STATE_PROPERTY
	})
	public interface EditModel extends ThemeConfig {

		@Override
		@Constraint(value = AvailableThemeNameContraint.class)
		String getId();

		@Override
		@Hidden
		String getPath();

		@Override
		@Hidden
		String getPathEffective();

		@Override
		@Hidden
		List<StyleSheetRef> getStyles();

		@Override
		@Options(fun = AllThemeIds.class)
		List<String> getExtends();
		
		@Override
		@Hidden
		boolean isProtected();

	}

	/**
	 * Creates a {@link ThemeFormBuilder} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public ThemeFormBuilder(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected Class<? extends ThemeConfig> getModelType() {
		return ThemeConfig.class;
	}

	@Override
	protected Class<? extends EditModel> getFormType(Object contextModel) {
		return EditModel.class;
	}

	@Override
	protected void fillFormModel(EditModel formModel, ThemeConfig businessModel) {
		formModel.setId(businessModel.getId());
		formModel.setExtends(businessModel.getExtends());
		formModel.setState(businessModel.getState());
	}

}
