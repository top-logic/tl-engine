/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.page;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.channel.ComponentChannel.ChannelListener;
import com.top_logic.layout.channel.linking.impl.ChannelLinking;
import com.top_logic.layout.channel.linking.impl.DirectLinking;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.mig.html.layout.ComponentResolver;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link PageTitleResolver} that computes the browser page title from a component channle value.
 */
public class DynamicPageTitle extends PageTitleResolver<DynamicPageTitle.Config> {

	/**
	 * Configuration of a {@link DynamicPageTitle}.
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
		@ImplementationClassDefault(value = DirectLinking.class)
		ModelSpec getValue();

	}

	/**
	 * Creates a {@link DynamicPageTitle} from configuration.
	 */
	@CalledByReflection
	public DynamicPageTitle(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void resolveComponent(InstantiationContext context, LayoutComponent component) {
		ModelSpec valueRef = getConfig().getValue();
		ChannelLinking linking = valueRef == null ? null : ChannelLinking.linking(getConfig().getValue());

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

	private static class Claim extends PageTitleResolver.Claim implements ChannelListener {

		private final ChannelLinking _linking;

		Claim(LayoutComponent component, ChannelLinking linking) {
			super(component);
			_linking = linking;
		}

		@Override
		public void handleNewValue(ComponentChannel sender, Object oldValue, Object newValue) {
			if (_component.isVisible()) {
				applyTitle();
			}
		}

		@Override
		protected String computeTitle() {
			Object titleValue = _linking == null ? null : ChannelLinking.eval(_component, _linking);
			return titleValue == null ? "" : MetaLabelProvider.INSTANCE.getLabel(titleValue);
		}

	}

}
