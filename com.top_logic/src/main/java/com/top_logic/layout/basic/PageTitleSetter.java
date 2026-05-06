/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.channel.ComponentChannel.ChannelListener;
import com.top_logic.layout.channel.linking.impl.ChannelLinking;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.mig.html.layout.ComponentResolver;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.mig.html.layout.VisibilityListener;

/**
 * {@link ComponentResolver} that updates the browser page title whenever its carrying component
 * becomes visible.
 *
 * <p>
 * The resolver claims the page title while its component is visible: on
 * {@link LayoutComponent#isVisible() visible} it pushes the value derived from the configured
 * {@link Config#getValue() source}; on invisible it asks the {@link MainLayout} to
 * {@link MainLayout#applyDefaultPageTitle() restore the default}.
 * </p>
 *
 * <p>
 * The page title only follows the source while the component is visible, so navigating away from a
 * section frees the title; the next visible section's resolver (or the {@link MainLayout} default)
 * takes over.
 * </p>
 */
public class PageTitleSetter extends ComponentResolver
		implements ConfiguredInstance<PageTitleSetter.Config> {

	/**
	 * Configuration of a {@link PageTitleSetter}.
	 */
	public interface Config extends PolymorphicConfiguration<ComponentResolver> {

		/** @see #getValue() */
		String VALUE = "value";

		/**
		 * Source of the page title value.
		 *
		 * <p>
		 * Resolved relative to the carrying component, e.g. <code>selection(self())</code> for the
		 * component's own selection, or <code>model(someName)</code> for the model of another
		 * component. The resolved value is pushed to the browser as the new page title.
		 * </p>
		 */
		@Name(VALUE)
		@Mandatory
		ModelSpec getValue();

	}

	private final Config _config;

	/**
	 * Creates a {@link PageTitleSetter} from configuration.
	 */
	@CalledByReflection
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

		if (linking == null) {
			// Resolver unconfigured (e.g. emitted by a template with an unset page title
			// parameter); do not touch the title.
			return;
		}

		Claim claim = new Claim(component, linking);
		component.addListener(LayoutComponent.VISIBILITY_EVENT, claim);
		ComponentChannel source = linking.resolveChannel(context, component);
		if (source != null) {
			source.addListener(claim);
		}
		if (component.isVisible()) {
			claim.applyTitle();
		}
	}

	private static class Claim implements VisibilityListener, ChannelListener {

		private final LayoutComponent _component;

		private final ChannelLinking _linking;

		Claim(LayoutComponent component, ChannelLinking linking) {
			_component = component;
			_linking = linking;
		}

		@Override
		public Bubble handleVisibilityChange(Object sender, Boolean oldValue, Boolean newValue) {
			if (_component != sender) {
				return Bubble.BUBBLE;
			}
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

		void applyTitle() {
			MainLayout mainLayout = _component.getMainLayout();
			if (mainLayout == null) {
				return;
			}
			Object titleValue = _linking == null ? null : ChannelLinking.eval(_component, _linking);
			String title = titleValue == null ? "" : MetaLabelProvider.INSTANCE.getLabel(titleValue);
			mainLayout.getWindowScope().setPageTitle(title);
		}

	}

}
