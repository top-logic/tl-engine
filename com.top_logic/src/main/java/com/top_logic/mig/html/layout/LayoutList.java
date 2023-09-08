/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.DerivedRef;
import com.top_logic.basic.config.annotation.Hidden;

/**
 * {@link LayoutContainer} organizing its children in a copy-on-write list.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class LayoutList extends LayoutContainer {

	/**
	 * Configuration of a {@link LayoutList}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends LayoutContainer.Config, SubComponentConfig {

		@Override
		@DerivedRef({ SubComponentConfig.COMPONENTS })
		@Hidden
		List<? extends LayoutComponent.Config> getChildConfigurations();

	}

	/**
	 * The internal list of {@link LayoutComponent}s.
	 */
	private ArrayList<LayoutComponent> _children = new ArrayList<>();

	/**
	 * Unmodifiable variant of {@link #_children}.
	 */
	private List<LayoutComponent> _childrenView = Collections.unmodifiableList(_children);

	/**
	 * Whether a reference to {@link #_childrenView} has been handed out.
	 * 
	 * <p>
	 * In that case, the underlying buffer must be copied before modification to prevent concurrent
	 * modification exceptions being thrown in customer code.
	 * </p>
	 */
	private boolean _shared = false;

	/**
	 * Creates a {@link LayoutList} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public LayoutList(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	private Config config() {
		return (Config) _config;
	}

	@Override
	public void createSubComponents(InstantiationContext context) {
		super.createSubComponents(context);

		initChildren(context, config());
	}

	private void initChildren(InstantiationContext context, Config config) {
		List<LayoutComponent> children = TypedConfiguration.getInstanceList(context, config.getComponents());
		setChildren(children);

		for (LayoutComponent child : children) {
			ComponentInstantiationContext.createSubComponents(context, child);
		}
	}

	@Override
	public final List<LayoutComponent> getChildList() {
		_shared = true;
		return children();
	}

	private List<LayoutComponent> children() {
		// Due to a quirks-design, the super-class calls getChildList() from its constructor.
		return _childrenView == null ? Collections.<LayoutComponent> emptyList() : _childrenView;
	}

	@Override
	public int getChildCount() {
		return children() == null ? 0 : children().size();
	}

	/**
	 * The child {@link LayoutComponent} with the given index.
	 */
	public LayoutComponent getChild(int index) {
		return children().get(index);
	}

	/**
	 * The index of the given {@link LayoutComponent} in {@link #getChildList()}.
	 */
	public int getIndexOfChild(Object child) {
		return children().indexOf(child);
	}

	@Override
	public final void setChildren(List<LayoutComponent> newChildren) {
		if (children().equals(newChildren)) {
			// No change
			return;
		}
		ArrayList<LayoutComponent> children = childrenForModify();
		ArrayList<LayoutComponent> oldChildren = new ArrayList<>(children);

		for (int n = 0, cnt = children.size(); n < cnt; n++) {
			LayoutComponent child = children.get(n);

			child.setParent(null);
		}

		children.clear();
		children.addAll(newChildren);

		for (int n = 0, cnt = newChildren.size(); n < cnt; n++) {
			LayoutComponent child = newChildren.get(n);

			child.setParent(this);
		}
		onSet(oldChildren);
		setChildVisibility(_isVisible());
		fireChildrenChanged(oldChildren);
	}

	/**
	 * Informs the component about a change in {@link #setChildren(List)}.
	 * 
	 * <p>
	 * The new children are available using {@link #getChildList()}.
	 * </p>
	 * 
	 * @param oldChildren
	 *        The children before the setting. Must not be modified.
	 */
	protected void onSet(List<LayoutComponent> oldChildren) {
		// Hook for subclasses
	}

	protected final void addChild(int index, LayoutComponent newChild) {
		newChild.setParent(this);

		ArrayList<LayoutComponent> newChildren = childrenForModify();
		ArrayList<LayoutComponent> oldChildren = new ArrayList<>(newChildren);
		newChildren.add(index, newChild);

		onAdd(index, newChild);
		fireChildrenChanged(oldChildren);
	}

	protected void onAdd(int index, LayoutComponent newChild) {
		// Hook for subclasses.
	}

	protected final LayoutComponent removeChild(int index) {
		ArrayList<LayoutComponent> newChildren = childrenForModify();
		ArrayList<LayoutComponent> oldChildren = new ArrayList<>(newChildren);
		LayoutComponent result = newChildren.remove(index);
		result.setParent(null);

		onRemove(index, result);
		fireChildrenChanged(oldChildren);
		return result;
	}

	protected void onRemove(int index, LayoutComponent removed) {
		// Hook for subclasses.
	}

	private ArrayList<LayoutComponent> childrenForModify() {
		ArrayList<LayoutComponent> result;
		if (_shared) {
			result = new ArrayList<>(_children);

			_children = result;
			_childrenView = Collections.unmodifiableList(result);
			_shared = false;
		} else {
			result = _children;
		}
		return result;
	}

}
