/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.base.services.simpleajax.HTMLFragmentProvider;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.SingleSelectionModel;
import com.top_logic.layout.basic.AbstractVisibleControl;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.component.model.DefaultDeckPaneModel;
import com.top_logic.layout.component.model.ModelChangeListener;
import com.top_logic.layout.component.model.SingleSelectionListener;
import com.top_logic.layout.form.template.ControlProvider;

/**
 * The class {@link DeckPaneControl} has an {@link DefaultDeckPaneModel} as model and an
 * {@link ControlProvider} which creates {@link Control} for the possible selectable {@link Object}
 * in the {@link DefaultDeckPaneModel}. the controls renders exactly the control for the currently
 * selected {@link Object} in the {@link DefaultDeckPaneModel}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DeckPaneControl extends AbstractVisibleControl implements SingleSelectionListener, ModelChangeListener {

	private final DeckPaneModel deckPane;
	private final ControlProvider controlProvider;

	private final HashMap<Object, HTMLFragment> controlsByCard = new HashMap<>();

	public DeckPaneControl(Map<String, ControlCommand> commandsByName, DeckPaneModel aDeckPane,
			ControlProvider controlProvider) {
		super(commandsByName);
		this.deckPane = aDeckPane;
		this.controlProvider = controlProvider;
		
	}
	
	public DeckPaneControl(DeckPaneModel aDeckPane, ControlProvider controlProvider) {
		this(Collections.<String, ControlCommand>emptyMap(), aDeckPane, controlProvider);
	}
	
	/**
	 * This method returns the {@link DefaultDeckPaneModel} of this {@link DeckPaneControl}.
	 */
	@Override
	public DeckPaneModel getModel() {
		return deckPane;
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(DIV);
		writeControlAttributes(context, out);
		out.endBeginTag();
		HTMLFragment theDisplayedSelection = getControlFor(deckPane.getSingleSelection());
		if (theDisplayedSelection != null) {
			theDisplayedSelection.write(context, out);
		}
		out.endTag(DIV);
	}

	@Override
	protected void internalAttach() {
		super.internalAttach();
		deckPane.addSingleSelectionListener(this);
		deckPane.addModelChangedListener(this);
	}

	@Override
	protected void internalDetach() {
		super.internalDetach();
		deckPane.removeSingleSelectionListener(this);
		deckPane.removeModelChangedListener(this);
		HTMLFragment associatedControl = controlsByCard.get(deckPane.getSingleSelection());
		if (associatedControl instanceof Control) {
			((Control) associatedControl).detach();
		}
		controlsByCard.clear();
	}

	@Override
	public void notifySelectionChanged(SingleSelectionModel model, Object formerlySelectedObject, Object selectedObject) {
		if (formerlySelectedObject != null) {
			HTMLFragment theAssociatedControl = controlsByCard.get(formerlySelectedObject);
			if (theAssociatedControl instanceof Control) {
				((Control) theAssociatedControl).detach();
			}
			requestRepaint();
		}
	}

	@Override
	public Bubble modelChanged(Object sender, Object oldModel, Object newModel) {
		requestRepaint();
		return Bubble.BUBBLE;
	}

	/**
	 * This method returns an {@link HTMLFragment} for the given Object
	 * <code>anSelectedObject</code>, given by the {@link HTMLFragmentProvider} of this
	 * {@link DeckPaneControl}.
	 * 
	 * @return a {@link HTMLFragment} for the given Object or <code>null</code> if
	 *         <code>anSelectedObject</code> is <code>null</code>.
	 */
	public HTMLFragment getControlFor(Object anSelectedObject) {
		if (anSelectedObject == null) {
			return null;
		}
		HTMLFragment associatedControl = controlsByCard.get(anSelectedObject);
		if (associatedControl == null) {
			associatedControl = controlProvider.createFragment(anSelectedObject);
			if (associatedControl == null) {
				return null;
			}
			controlsByCard.put(anSelectedObject, associatedControl);
		}
		return associatedControl;
	}

}
