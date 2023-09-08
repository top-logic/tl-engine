/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.component;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.layout.Control;
import com.top_logic.layout.structure.ControlRepresentable;
import com.top_logic.layout.structure.ControlRepresentableCP;
import com.top_logic.layout.structure.LayoutControlProvider;
import com.top_logic.layout.table.component.BuilderComponent;
import com.top_logic.layout.tree.breadcrumb.BreadcrumbControl;
import com.top_logic.layout.tree.breadcrumb.BreadcrumbData;
import com.top_logic.layout.tree.breadcrumb.BreadcrumbDataOwner;
import com.top_logic.layout.tree.breadcrumb.BreadcrumbRenderer;
import com.top_logic.layout.tree.breadcrumb.DefaultBreadcrumbRenderer;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link BuilderComponent} displaying a breadcrumb navigation.
 * 
 * <p>
 * The {@link Config#getModelBuilder()} is expected to create a {@link BreadcrumbData} model for
 * display.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BreadcrumbComponent extends BuilderComponent implements ControlRepresentable, BreadcrumbDataOwner {

	/**
	 * Configuration options for {@link BreadcrumbComponent}.
	 */
	@TagName("breadcrumb")
	public interface Config extends BuilderComponent.Config {
		/** @see #getRenderer() */
		String RENDERER = "renderer";

		@Override
		@ClassDefault(BreadcrumbComponent.class)
		Class<? extends LayoutComponent> getImplementationClass();

		@Override
		@ItemDefault
		@ImplementationClassDefault(ControlRepresentableCP.class)
		PolymorphicConfiguration<LayoutControlProvider> getComponentControlProvider();

		/**
		 * The {@link BreadcrumbRenderer} to display the {@link BreadcrumbData} created by
		 * {@link #getModelBuilder()}.
		 */
		@Name(RENDERER)
		@ItemDefault
		@ImplementationClassDefault(DefaultBreadcrumbRenderer.class)
		PolymorphicConfiguration<BreadcrumbRenderer> getRenderer();
	}

	/** The configured renderer used for the {@link BreadcrumbControl}. */
	private final BreadcrumbRenderer _renderer;

	private BreadcrumbControl _control;

	/**
	 * Creates a {@link BreadcrumbComponent}.
	 */
	public BreadcrumbComponent(InstantiationContext context, Config attr) throws ConfigurationException {
		super(context, attr);

		_renderer = context.getInstance(attr.getRenderer());
	}

	@Override
	protected void handleNewModel(Object newModel) {
		super.handleNewModel(newModel);

		_control = createControl();
	}

	/**
	 * Creates the control to render the tab structure.
	 */
	protected BreadcrumbControl createControl() {
		BreadcrumbData data = (BreadcrumbData) getBuilder().getModel(getModel(), this);
		return new BreadcrumbControl(_renderer, data);
	}

	@Override
	public Control getRenderingControl() {
		return _control;
	}

	@Override
	public BreadcrumbData getBreadcrumbData() {
		return _control.getModel();
	}

}
