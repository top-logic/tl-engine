/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.page;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.mig.html.layout.ComponentResolver;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.mig.html.layout.VisibilityListener;

/**
 * {@link ComponentResolver} base class for resolvers setting the browser page title.
 *
 * <p>
 * Concrete resolvers attach a {@link Claim} to a {@link LayoutComponent} that pushes a freshly
 * computed title to the browser while the component is visible and relinquishes the title when
 * the component becomes invisible.
 * </p>
 */
public abstract class PageTitleResolver<C extends PolymorphicConfiguration<?>> extends ComponentResolver
		implements ConfiguredInstance<C> {

	private C _config;

	/**
	 * Creates a {@link PageTitleResolver} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public PageTitleResolver(InstantiationContext context, C config) {
		_config = config;
	}

	@Override
	public C getConfig() {
		return _config;
	}

	/**
	 * Visibility-aware page title claim attached to a {@link LayoutComponent}.
	 *
	 * <p>
	 * While the carrying {@link #_component component} is {@link LayoutComponent#isVisible()
	 * visible}, the claim pushes the result of {@link #computeTitle()} via
	 * {@link MainLayout#getWindowScope()}. When the component becomes invisible, the claim asks the
	 * {@link MainLayout} to {@link MainLayout#applyDefaultPageTitle() restore the default page
	 * title}, freeing the title for a different resolver to take over.
	 * </p>
	 *
	 * <p>
	 * Subclasses implement {@link #computeTitle()} and may listen to additional change sources
	 * (channel updates, tile-path changes, ...). On each such update they should call
	 * {@link #applyTitle()}, typically guarded by {@link LayoutComponent#isVisible()}.
	 * </p>
	 */
	protected static abstract class Claim implements VisibilityListener {

		/**
		 * The {@link LayoutComponent} carrying this claim.
		 */
		protected final LayoutComponent _component;

		/**
		 * Creates a {@link Claim} for the given component.
		 *
		 * @param component
		 *        The {@link LayoutComponent} carrying this claim.
		 */
		protected Claim(LayoutComponent component) {
			_component = component;
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

		/**
		 * Pushes the result of {@link #computeTitle()} as the new browser page title.
		 *
		 * <p>
		 * If the component is no longer attached to a {@link MainLayout}, or if the computed title
		 * is <code>null</code>, the title is left unchanged.
		 * </p>
		 */
		protected final void applyTitle() {
			MainLayout mainLayout = _component.getMainLayout();
			if (mainLayout == null) {
				return;
			}
			String title = computeTitle();
			if (title == null) {
				return;
			}
			mainLayout.getWindowScope().setPageTitle(title);
		}

		/**
		 * Computes the title to display while the component is visible.
		 *
		 * @return The title to display, or <code>null</code> to leave the current title untouched.
		 */
		protected abstract String computeTitle();

	}

}
