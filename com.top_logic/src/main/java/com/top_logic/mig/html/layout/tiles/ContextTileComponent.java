/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.func.Function2;
import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.listener.PropertyListener;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.channel.ComponentChannel.ChannelListener;
import com.top_logic.layout.component.LayoutContainerBoundChecker;
import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.structure.LayoutControlProvider;
import com.top_logic.mig.html.layout.ComponentInstantiationContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutContainer;
import com.top_logic.mig.html.layout.tiles.control.ContextTileControlProvider;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCheckerDelegate;

/**
 * {@link LayoutContainer} representing a {@link Selectable selector} and {@link LayoutComponent
 * content} in tile environment.
 * 
 * <p>
 * A {@link ContextTileComponent} has exactly 2 children. Either the first or the second component
 * is visible.
 * </p>
 * 
 * <p>
 * The first child is the {@link #getSelector() selector}. Its selection represents the context
 * object for the {@link #getContent() content}.
 * </p>
 * 
 * <p>
 * The second child is the {@link #getContent() content}. It is displayed when something is selected
 * in the {@link #getContextSelection() selector}.
 * </p>
 * 
 * @see RootTileComponent
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ContextTileComponent extends LayoutContainer implements BoundCheckerDelegate {

	/**
	 * Configuration of a {@link ContextTileComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends LayoutContainer.Config {

		/** Name of the configuration option {@link #getContent()}. */
		String CONTENT_NAME = "content";

		/** Name of the configuration option {@link #getSelector()}. */
		String SELECTOR_NAME = "selector";

		/**
		 * Configuration of the component that selects the context for {@link #getContent()}.
		 */
		@Name(SELECTOR_NAME)
		@Mandatory
		LayoutComponent.Config getSelector();

		/**
		 * Configuration of the actual content component. This component uses the selection of
		 * {@link #getSelector()} as context.
		 */
		@Name(CONTENT_NAME)
		@Mandatory
		LayoutComponent.Config getContent();

		@Override
		@ItemDefault(ContextTileControlProvider.class)
		PolymorphicConfiguration<LayoutControlProvider> getComponentControlProvider();

		@Override
		@Derived(fun = ToList.class, args = { @Ref(SELECTOR_NAME), @Ref(CONTENT_NAME) })
		List<? extends LayoutComponent.Config> getChildConfigurations();

		/**
		 * Creates a list from the given arguments.
		 * 
		 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
		 */
		class ToList extends
				Function2<List<? extends LayoutComponent.Config>, LayoutComponent.Config, LayoutComponent.Config> {

			@Override
			public List<? extends LayoutComponent.Config> apply(LayoutComponent.Config arg1,
					LayoutComponent.Config arg2) {
				return Arrays.asList(arg1, arg2);
			}
		}

	}

	/**
	 * {@link PropertyListener} informing about the change if the content component is
	 * {@link ContextTileComponent#isContentDisplayed() displayed}.
	 * 
	 * @see ContentComponentChangedListener
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface ContentDisplayedListener extends PropertyListener {

		/**
		 * Handles the change of {@link ContextTileComponent#isContentDisplayed()}.
		 * 
		 * @param sender
		 *        Changed {@link ContextTileComponent}.
		 * @param oldValue
		 *        Whether {@link ContextTileComponent#getContent() content} was displayed before.
		 * @param newValue
		 *        Whether {@link ContextTileComponent#getContent() content} is displayed now.
		 */
		void handleDisplayedChanged(ContextTileComponent sender, Boolean oldValue, Boolean newValue);
	}

	/**
	 * {@link EventType} handling changes of {@link ContextTileComponent#isContentDisplayed()}.
	 * 
	 * @see #addListener(EventType, com.top_logic.basic.listener.PropertyListener)
	 * @see #removeListener(EventType, com.top_logic.basic.listener.PropertyListener)
	 * @see ContentDisplayedListener
	 */
	public static final EventType<ContentDisplayedListener, ContextTileComponent, Boolean> DETAIL_DISPLAYED_PROPERTY =
		new EventType<>("Content displayed") {

			@Override
			public Bubble dispatch(ContentDisplayedListener listener, ContextTileComponent sender, Boolean oldValue,
					Boolean newValue) {
				listener.handleDisplayedChanged(sender, oldValue, newValue);
				return Bubble.BUBBLE;
			}

		};

	/**
	 * {@link PropertyListener} informing about the change of
	 * {@link ContextTileComponent#getContent()}.
	 * 
	 * @see ContentDisplayedListener
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface ContentComponentChangedListener extends PropertyListener {

		/**
		 * Handles the change of {@link ContextTileComponent#getContent()}.
		 * 
		 * @param sender
		 *        Changed {@link ContextTileComponent}.
		 * @param oldValue
		 *        Former value of {@link ContextTileComponent#getContent()}. May be
		 *        <code>null</code>.
		 * @param newValue
		 *        Current value of {@link ContextTileComponent#getContent()}.
		 */
		void handleContentChanged(ContextTileComponent sender, LayoutComponent oldValue, LayoutComponent newValue);
	}

	/** {@link EventType} handling changes of {@link ContextTileComponent#isContentDisplayed()}. */
	public static final EventType<ContentComponentChangedListener, ContextTileComponent, LayoutComponent> CONTENT_COMPONENT_PROPERTY =
		new EventType<>("Content component") {

			@Override
			public Bubble dispatch(ContentComponentChangedListener listener, ContextTileComponent sender,
					LayoutComponent oldValue, LayoutComponent newValue) {
				listener.handleContentChanged(sender, oldValue, newValue);
				/* Do not bubble event. Otherwise, a listener on an ancestor ContextTileComponent is
				 * informed about the content change of a descendant. This difference can only be
				 * detected if the listener checks that the component it is attached to is not the
				 * same as the sender. Therefore the listener cannot not be static. */
				return Bubble.CANCEL_BUBBLE;
			}

		};

	private boolean _contentDisplayed = false;

	private LayoutComponent _content;

	private LayoutComponent _selector;

	private List<LayoutComponent> _children = Collections.emptyList();

	private final BoundChecker _boundCheckerDelegate =
		new LayoutContainerBoundChecker<>(this, ContextTileComponent::selectorAsList);

	/**
	 * Creates a new {@link ContextTileComponent}.
	 */
	public ContextTileComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	/**
	 * Whether the content is displayed.
	 * 
	 * <p>
	 * If return value is <code>true</code>, then {@link #getContent()} is visible and
	 * {@link #getSelector()} invisible, otherwise vice versa.
	 * </p>
	 * 
	 * @see #getContent()
	 * @see #getSelector()
	 */
	public boolean isContentDisplayed() {
		return _contentDisplayed;
	}

	/**
	 * Setter for {@link #isContentDisplayed()}.
	 */
	public void setContentDisplayed(boolean displayed) {
		if (displayed == _contentDisplayed) {
			return;
		}
		_contentDisplayed = displayed;
		setChildVisibility(_isVisible());

		firePropertyChanged(DETAIL_DISPLAYED_PROPERTY, this, !_contentDisplayed, _contentDisplayed);
	}

	@Override
	public void createSubComponents(InstantiationContext context) {
		super.createSubComponents(context);
		updateChildren(context.getInstance(getConfig().getSelector()), context.getInstance(getConfig().getContent()));

		for (LayoutComponent child : getChildList()) {
			ComponentInstantiationContext.createSubComponents(context, child);
		}
	}

	private void updateChildren(LayoutComponent selector, LayoutComponent content) {
		boolean changed = setSelector(selector);
		changed |= setContent(content);
		if (!changed) {
			return;
		}
		List<LayoutComponent> oldChildren = _children;
		_children = Arrays.asList(getSelector(), getContent());
		fireChildrenChanged(oldChildren);
	}

	private boolean setContent(LayoutComponent newContent) {
		assert newContent != null;
		if (newContent.equals(_content)) {
			// Not changed.
			return false;
		}
		if (_content != null) {
			_content.setParent(null);
		}
		newContent.setParent(this);
		newContent.setVisible(isContentDisplayed() && _isVisible());
		LayoutComponent oldContent = _content;
		_content = newContent;
		fireContentComponentChanged(oldContent);
		return true;
	}

	private void fireContentComponentChanged(LayoutComponent oldContent) {
		firePropertyChanged(CONTENT_COMPONENT_PROPERTY, this, oldContent, _content);
	}

	private boolean setSelector(LayoutComponent newSelector) {
		assert newSelector != null;
		if (newSelector.equals(_selector)) {
			// Not changed.
			return false;
		}
		assertSelectable(newSelector);
		if (_selector != null) {
			_selector.setParent(null);
		}
		newSelector.setParent(this);
		newSelector.setVisible(!isContentDisplayed() && _isVisible());
		_selector = newSelector;
		return true;
	}

	private void assertSelectable(LayoutComponent selector) {
		if (!(selector instanceof Selectable)) {
			throw new IllegalArgumentException(
				"Selector of " + this + " is expected to be a " + Selectable.class.getName() + ": " + selector);
		}
	}

	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

	@Override
	public Iterable<? extends LayoutComponent> getVisibleChildren() {
		return Collections.singletonList(isContentDisplayed() ? getContent() : getSelector());
	}

	@Override
	public boolean makeVisible(LayoutComponent child) {
		makeVisible();
		if (child.equals(getSelector())) {
			setContentDisplayed(false);
		} else if (child.equals(getContent())) {
			setContentDisplayed(true);
		} else {
			throw new IllegalArgumentException(child + " is not a child of " + this);
		}
		return child.isVisible();
	}

	@Override
	public boolean isOuterFrameset() {
		return false;
	}

	@Override
	protected void setChildVisibility(boolean visible) {
		List<LayoutComponent> children = getChildList();
		if (children.isEmpty()) {
			// Children may not be initialized yet.
			return;
		}
		if (!visible) {
			children.forEach(lc -> lc.setVisible(false));
		} else {
			getContent().setVisible(isContentDisplayed());
			getSelector().setVisible(!isContentDisplayed());
		}
	}

	/**
	 * Type-safe access to {@link #getSelector()}.
	 */
	public final Selectable getContextSelection() {
		return (Selectable) getSelector();
	}

	/**
	 * Selector defining the context for {@link #getContent() the content}.
	 * 
	 * @return May be <code>null</code> in case this component is not completely initialized.
	 * 
	 * @see #getContextSelection()
	 */
	public LayoutComponent getSelector() {
		return _selector;
	}

	private List<LayoutComponent> selectorAsList() {
		return CollectionUtil.singletonOrEmptyList(getSelector());
	}

	/**
	 * Content of the context tile.
	 * 
	 * @return May be <code>null</code> in case this component is not completely initialized.
	 */
	public LayoutComponent getContent() {
		return _content;
	}

	@Override
	public void linkChannels(Log log) {
		super.linkChannels(log);
		modelChannel().addListener(this::handleModelChanged);
		modelChannel().link(getContextSelection().selectionChannel());
	}

	/**
	 * Handles change of {@link #getModel()}.
	 * 
	 * @param sender
	 *        See {@link ChannelListener#handleNewValue(ComponentChannel, Object, Object)}.
	 * @param oldValue
	 *        See {@link ChannelListener#handleNewValue(ComponentChannel, Object, Object)}.
	 * @param newValue
	 *        See {@link ChannelListener#handleNewValue(ComponentChannel, Object, Object)}.
	 */
	private void handleModelChanged(ComponentChannel sender, Object oldValue, Object newValue) {
		setContentDisplayed(newValue != null);
	}

	@Override
	public void setChildren(List<LayoutComponent> newChildren) {
		switch (newChildren.size()) {
			case 2:
				updateChildren(newChildren.get(0), newChildren.get(1));
				break;
			default:
				throw new IllegalArgumentException(this + " expects to have exactly 2 children: " + newChildren);
		}
	}

	@Override
	public List<LayoutComponent> getChildList() {
		return _children;
	}

	@Override
	public int getChildCount() {
		return getChildList().size();
	}

	@Override
	public BoundChecker getDelegate() {
		return _boundCheckerDelegate;
	}

	@Override
	public ResKey hideReason() {
		return hideReason(internalModel());
	}

}


