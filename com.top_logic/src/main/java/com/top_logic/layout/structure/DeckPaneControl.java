/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.layout.SingleSelectionModel;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.ControlRenderer;
import com.top_logic.layout.component.model.DefaultDeckPaneModel;
import com.top_logic.layout.component.model.ModelChangeListener;
import com.top_logic.layout.component.model.SingleSelectionListener;
import com.top_logic.layout.form.control.DeckPaneModel;
import com.top_logic.layout.layoutRenderer.DeckPaneRenderer;


/**
 * The class {@link DeckPaneControl} is a container for a list of {@link LayoutControl}s. It
 * defines which {@link LayoutControl} is currently shown.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DeckPaneControl extends ContainerControl<DeckPaneControl> {

	/** the model which determines the currently shown pane */
	private final DeckPaneModel deckPaneModel;
	private ModelChangeListener modelChangedListener;
	private SingleSelectionListener selectionChangedListener;

	/**
	 * Creates a new {@link CollapsibleControl}.
	 * 
	 * @param aDeck
	 *        the model of this {@link DeckPaneControl}
	 */
	public DeckPaneControl(DeckPaneModel aDeck) {
		this(aDeck, Collections.<String, ControlCommand> emptyMap());
	}

	/**
	 * @param aDeck
	 *        the model of this {@link DeckPaneControl}
	 * 
	 * @see AbstractLayoutControl#AbstractLayoutControl(Map)
	 */
	protected DeckPaneControl(DeckPaneModel aDeck, Map<String, ControlCommand> commandsByName) {
		super(commandsByName);
		this.deckPaneModel = aDeck;
		initDeckPaneModel();
	}

	private final void initDeckPaneModel() {
		this.modelChangedListener = new ModelChangeListener() {

			@Override
			public Bubble modelChanged(Object sender, Object oldModel, Object newModel) {
				requestRepaint();
				return Bubble.BUBBLE;
			}
		};
		this.selectionChangedListener = new SingleSelectionListener() {

			@Override
			public void notifySelectionChanged(SingleSelectionModel model, Object formerlySelectedObject, Object selectedObject) {
				requestRepaint();
				List<? extends LayoutControl> theChildren = getChildren();
				for (int index = 0, size = theChildren.size(); index < size; index++) {
					LayoutControl currentChild = theChildren.get(index);
					if (index != getActiveChildIndex()) {
						currentChild.detach();
					}
				}
			}
			
		};
	}

	/**
	 * This method returns the index of the currently shown child or -1 if this
	 * {@link DeckPaneControl} contains no children.
	 */
	public final int getActiveChildIndex() {
		return this.deckPaneModel.getSelectedIndex();
	}

	/**
	 * This method sets which child shall be shown and sets this {@link LayoutControl} invalid if
	 * the active child index changed.
	 * 
	 * @param anIndex
	 *            the index of the child which shall be shown. must be geq 0 and leq
	 *            {@link #getChildren()}.getSize().
	 */
	public final void setActiveChildIndex(int anIndex) {
		if (0 <= anIndex && anIndex < getChildren().size()) {
			if (this.deckPaneModel.getSelectedIndex() == anIndex) {
				return;
			} else {
				beforeSettingNewActiveChildIndex(anIndex);
				this.deckPaneModel.setSelectedIndex(anIndex);
			}
		} else {
			throw new IndexOutOfBoundsException("'anIndex' must be geq 0 and leq getChildren().size() but is " + anIndex + ".");
		}
	}

	/**
	 * This method is a hook for subclasses. It is called by {@link #setActiveChildIndex(int)} if a
	 * new active child index was set, before the new active child index was set.
	 * 
	 * @param anIndex
	 *            the index of the new active child.
	 */
	protected void beforeSettingNewActiveChildIndex(int anIndex) {
		// just a hook for subclasses. nothing to do here
	}

	/**
	 * This method returns the current active child or <code>null</code> if this
	 * {@link DeckPaneControl} does not contain any children.
	 */
	public final LayoutControl getActiveChild() {
		if (getActiveChildIndex() == -1) {
			return null;
		}
		return getChildren().get(getActiveChildIndex());
	}

	/**
	 * the {@link DefaultDeckPaneModel} displayed by this control.
	 */
	@Override
	public DeckPaneModel getModel() {
		return this.deckPaneModel;
	}

	@Override
	protected void internalDetach() {
		super.internalDetach();
		getModel().removeModelChangedListener(this.modelChangedListener);
		getModel().removeSingleSelectionListener(this.selectionChangedListener);
	}

	@Override
	protected void internalAttach() {
		super.internalAttach();
		getModel().addModelChangedListener(this.modelChangedListener);
		getModel().addSingleSelectionListener(this.selectionChangedListener);
		List<? extends LayoutControl> theChildren = getChildren();
		int activeChildIndex = getActiveChildIndex();
		LayoutControl currentChild;
		for (int index = 0, size = theChildren.size(); index < size; index++) {
			currentChild = theChildren.get(index);
			if (index != activeChildIndex) {
				currentChild.detach();
			}
		}
	}

	@Override
	protected ControlRenderer<? super DeckPaneControl> createDefaultRenderer() {
		return DeckPaneRenderer.INSTANCE;
	}

	@Override
	public DeckPaneControl self() {
		return this;
	}

}
