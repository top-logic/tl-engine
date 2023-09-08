/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.util.Collections;
import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.util.Utils;
import com.top_logic.layout.structure.ContentControl;
import com.top_logic.layout.structure.ContextMenuLayoutControlProvider;
import com.top_logic.layout.structure.LayoutControl;
import com.top_logic.mig.html.layout.tiles.ToList;

/**
 * {@link LayoutContainer} holding at most one child.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class SingleLayoutContainer extends LayoutContainer {

	/**
	 * Configuration of a {@link SingleLayoutContainer}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends LayoutContainer.Config {

		/** Configuration name for the property {@link #getComponent()}. */
		String COMPONENT_NAME = LayoutComponent.Config.COMPONENT;

		@Override
		@Derived(fun = ToList.class, args = @Ref(COMPONENT_NAME))
		List<? extends LayoutComponent.Config> getChildConfigurations();

		/**
		 * Configuration of the single child component.
		 */
		@Name(COMPONENT_NAME)
		LayoutComponent.Config getComponent();

	}

	/**
	 * {@link ContextMenuLayoutControlProvider} for {@link SingleLayoutContainer} which creates a
	 * Control that inlines the child component, i.e. there is no GUI representation for the
	 * container itself.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class ChildControlProvider
			extends ContextMenuLayoutControlProvider<ChildControlProvider> {
	
		/**
		 * Creates a new {@link ChildControlProvider}.
		 */
		public ChildControlProvider(InstantiationContext context, Config<ChildControlProvider> config) {
			super(context, config);
		}
	
		@Override
		public LayoutControl mkLayout(Strategy strategy, LayoutComponent component) {
			SingleLayoutContainer layout = (SingleLayoutContainer) component;
			ContentControl result = createContentWithMenu(component);
			LayoutComponent child = layout.getChild();
			if (child != null) {
				result.setView(strategy.createLayout(child, layout.getToolBar()));
			}
			return result;
		}
	}

	/** @see #getChildList() */
	private List<LayoutComponent> _children = Collections.emptyList();

	/**
	 * Creates a new {@link SingleLayoutContainer}.
	 */
	public SingleLayoutContainer(InstantiationContext context, Config atts) throws ConfigurationException {
		super(context, atts);
	}

	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

	@Override
	public Iterable<? extends LayoutComponent> getVisibleChildren() {
		return getChildList();
	}

	@Override
	public boolean makeVisible(LayoutComponent child) {
		return makeVisible();
	}

	@Override
	public List<LayoutComponent> getChildList() {
		return _children;
	}

	@Override
	public boolean isOuterFrameset() {
		return false;
	}

	@Override
	public int getChildCount() {
		return getChildList().size();
	}

	/**
	 * Access to the child of this component.
	 * 
	 * @return May be <code>null</code>, when this component has no child.
	 */
	public LayoutComponent getChild() {
		switch (getChildCount()) {
			case 0:
				return null;
			case 1:
				return getChildList().get(0);
			default:
				throw new IllegalStateException("Only one child.");
		}
	}

	@Override
	protected void setChildVisibility(boolean aVisible) {
		LayoutComponent child = getChild();
		if (child != null) {
			child.setVisible(aVisible);
		}
	}

	@Override
	public void createSubComponents(InstantiationContext context) {
		super.createSubComponents(context);

		initChildren(context, getConfig());
	}

	private void initChildren(InstantiationContext context, Config config) {
		LayoutComponent child = context.getInstance(config.getComponent());

		setChild(child);

		if (child != null) {
			ComponentInstantiationContext.createSubComponents(context, child);
		}
	}

	/**
	 * Sets the given {@link LayoutComponent} as child of this {@link SingleLayoutContainer}.
	 * 
	 * @param newChild
	 *        New value of {@link #getChild()}. May be <code>null</code>.
	 */
	protected void setChild(LayoutComponent newChild) {
		LayoutComponent oldChild = getChild();
		if (Utils.equals(oldChild, newChild)) {
			return;
		}
		if (oldChild != null) {
			oldChild.setParent(null);
		}
		List<LayoutComponent> newChildren;
		if (newChild != null) {
			newChild.setParent(this);
			newChildren = Collections.singletonList(newChild);
		} else {
			newChildren = Collections.emptyList();
		}

		List<LayoutComponent> oldChildren = getChildList();
		_children = newChildren;
		onSet(oldChild);
		setChildVisibility(_isVisible());
		fireChildrenChanged(oldChildren);
	}

	/**
	 * Informs the component about a change in {@link #setChild(LayoutComponent)}.
	 * 
	 * <p>
	 * The new child are available using {@link #getChild()}.
	 * </p>
	 * 
	 * @param oldChild
	 *        The children before the setting. Must not be modified.
	 */
	protected void onSet(LayoutComponent oldChild) {
		// Hook for subclasses
	}

	@Override
	public final void setChildren(List<LayoutComponent> newChildren) {
		switch (newChildren.size()) {
			case 0:
				setChild(null);
				break;
			case 1:
				setChild(newChildren.get(0));
				break;
			default:
				throw new IllegalArgumentException(this + " can have at most one child: " + newChildren);
		}
	}

}

