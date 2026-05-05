/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.func.IFunction1;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.ScriptFunction1;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.channel.ComponentChannel.ChannelListener;
import com.top_logic.layout.channel.linking.impl.ChannelLinking;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.mig.html.layout.ComponentResolver;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.mig.html.layout.VisibilityListener;

/**
 * {@link ComponentResolver} that updates the browser page title whenever its carrying
 * component becomes visible.
 *
 * <p>
 * The resolver claims the page title while its component is visible: on
 * {@link LayoutComponent#isVisible() visible} it pushes the value derived from the
 * configured {@link Config#getValue() source} (optionally piped through a
 * {@link Config#getFunction() script function}); on invisible it asks the
 * {@link MainLayout} to {@link MainLayout#applyDefaultPageTitle() restore the default}.
 * </p>
 *
 * <p>
 * The page title only follows the source while the component is visible, so navigating
 * away from a section frees the title; the next visible section's resolver (or the
 * {@link MainLayout} default) takes over.
 * </p>
 */
public class PageTitleSetter extends ComponentResolver
		implements ConfiguredInstance<PageTitleSetter.Config> {

	/**
	 * Configuration of a {@link PageTitleSetter}.
	 */
	public interface Config extends PolymorphicConfiguration<ComponentResolver> {

		/**
		 * Source of the page title value.
		 *
		 * <p>
		 * Resolved relative to the carrying component, e.g. <code>selection(self())</code>
		 * for the component's own selection, or <code>model(someName)</code> for the model
		 * of another component. The resolved value is pushed to the browser as the new
		 * page title (after optional {@link #getFunction() function} application and
		 * label conversion).
		 * </p>
		 */
		@Name("value")
		ModelSpec getValue();

		/**
		 * Optional transformation applied to the {@link #getValue() source} value before it
		 * is converted to a label.
		 *
		 * <p>
		 * The function is a TL-Script taking the raw source value and returning the title
		 * value (typically a string or a {@link com.top_logic.basic.util.ResKey ResKey}).
		 * If unset, the source value is converted directly via {@link MetaLabelProvider}.
		 * </p>
		 */
		@Name("function")
		ScriptFunction1<?, ?> getFunction();

	}

	private final Config _config;

	/**
	 * Creates a {@link PageTitleSetter} from configuration.
	 */
	public PageTitleSetter(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public void resolveComponent(InstantiationContext context, LayoutComponent component) {
		ChannelLinking linking = _config.getValue() == null ? null : ChannelLinking.linking(_config.getValue());
		IFunction1<?, ?> function = _config.getFunction() == null ? null : _config.getFunction().impl();

		Claim claim = new Claim(component, linking, function);
		component.addListener(LayoutComponent.VISIBILITY_EVENT, claim);
		if (linking != null) {
			ComponentChannel source = linking.resolveChannel(context, component);
			if (source != null) {
				source.addListener(claim);
			}
		}
		if (component.isVisible()) {
			claim.applyTitle();
		}
	}

	private static class Claim implements VisibilityListener, ChannelListener {

		private final LayoutComponent _component;

		private final ChannelLinking _linking;

		private final IFunction1<?, ?> _function;

		Claim(LayoutComponent component, ChannelLinking linking, IFunction1<?, ?> function) {
			_component = component;
			_linking = linking;
			_function = function;
		}

		@Override
		public Bubble handleVisibilityChange(Object sender, Boolean oldValue, Boolean newValue) {
			if (Boolean.TRUE.equals(newValue)) {
				applyTitle();
			} else {
				MainLayout mainLayout = _component.getMainLayout();
				if (mainLayout != null) {
					mainLayout.applyDefaultPageTitle();
				}
			}
			return Bubble.BUBBLE;
		}

		@Override
		public void handleNewValue(ComponentChannel sender, Object oldValue, Object newValue) {
			if (_component.isVisible()) {
				applyTitle();
			}
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		void applyTitle() {
			MainLayout mainLayout = _component.getMainLayout();
			if (mainLayout == null) {
				return;
			}
			Object source = _linking == null ? null : ChannelLinking.eval(_component, _linking);
			Object titleValue = _function == null ? source : ((IFunction1) _function).apply(source);
			String title = titleValue == null ? "" : MetaLabelProvider.INSTANCE.getLabel(titleValue);
			mainLayout.getWindowScope().setPageTitle(title);
		}

	}

}
