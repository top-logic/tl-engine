/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Subtypes;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.func.misc.EmptyCollection;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.declarative.DeclarativeFormBuilder;
import com.top_logic.layout.form.values.edit.FormBuilder;
import com.top_logic.layout.form.values.edit.annotation.ControlProvider;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.form.values.edit.editor.GroupInlineControlProvider;
import com.top_logic.layout.form.values.edit.initializer.InitializerProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.TLLayout;
import com.top_logic.util.error.TopLogicException;

/**
 * General {@link FormBuilder} for {@link TLLayout}s.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public abstract class TLLayoutFormBuilder extends DeclarativeFormBuilder<TLLayout, TLLayoutFormBuilder.EditModel> {

	/**
	 * Configuration of {@link TLLayoutFormBuilder}.
	 *
	 * @author <a href=mailto:sfo@top-logic.com>sfo</a>
	 */
	public interface Config extends DeclarativeFormBuilder.Config {

		@Override
		@BooleanDefault(false)
		boolean getCompactLayout();

	}

	/**
	 * Model of the displayed form.
	 *
	 * @author <a href=mailto:sfo@top-logic.com>sfo</a>
	 */
	public interface EditModel extends ConfigurationItem {

		/**
		 * Displayed configuration.
		 */
		@Subtypes(adjust = false, value = {})
		@ControlProvider(GroupInlineControlProvider.class)
		@Options(fun = EmptyCollection.class)
		ConfigurationItem getConfiguration();

		/**
		 * @see #getConfiguration()
		 */
		void setConfiguration(ConfigurationItem configuration);

		/**
		 * Layout identifier.
		 */
		@Hidden
		String getLayoutKey();

		/**
		 * @see #getLayoutKey()
		 */
		void setLayoutKey(String layoutKey);

	}

	/**
	 * Creates a {@link TLLayoutFormBuilder} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public TLLayoutFormBuilder(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected void buildInitializerIndex(InitializerProvider index, LayoutComponent component, EditModel formModel) {
		super.buildInitializerIndex(index, component, formModel);

		index.set(AbstractComponentConfigurationDialogBuilder.COMPONENT, getContextComponent(component, formModel));
	}

	private LayoutComponent getContextComponent(LayoutComponent component, EditModel formModel) {
		return component.getMainLayout().getComponentForLayoutKey(formModel.getLayoutKey());
	}

	@Override
	protected Class<? extends EditModel> getFormType(Object contextModel) {
		return EditModel.class;
	}

	@Override
	protected void fillFormModel(EditModel formModel, TLLayout layout) {
		try {
			formModel.setConfiguration(TypedConfiguration.copy(getConfiguration(layout)));
			formModel.setLayoutKey(layout.get().getName().scope());
		} catch (ConfigurationException exception) {
			ResKey key;
			if (layout.hasTemplate()) {
				key = I18NConstants.LAYOUT_RESOLVE_ERROR.fill(layout.getTemplateName());
			} else {
				key = I18NConstants.LAYOUT_RESOLVE_ERROR.fill("<no template>");
			}
			throw new TopLogicException(key, exception);
		}
	}

	abstract ConfigurationItem getConfiguration(TLLayout layout) throws ConfigurationException;

}
