/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.compare;

import com.top_logic.basic.Log;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.channel.ComponentChannel.ChannelListener;
import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.model.TLObject;

/**
 * Abstract super class for components that transforms models into a different time.
 * 
 * <p>
 * An {@link AbstractRevisionSelectComponent} has a {@link TLObject} as model. It allow selection of
 * a {@link Revision} and has the model in the selected revision as {@link #getSelected()
 * selection}.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractRevisionSelectComponent extends FormComponent implements Selectable {

	/**
	 * Configuration options for {@link AbstractRevisionSelectComponent}.
	 */
	public interface Config extends FormComponent.Config, Selectable.SelectableConfig {
		// Pure sum interface.
	}

	private static final ChannelListener ON_SELECTION_CHANGE = new ChannelListener() {
		@Override
		public void handleNewValue(ComponentChannel sender, Object oldValue, Object newValue) {
			AbstractRevisionSelectComponent self = (AbstractRevisionSelectComponent) sender.getComponent();
			Object newModel = newValue;
			if (self.hasFormContext() && newValue instanceof TLObject) {
				TLObject obj = asObject(newValue);
				Revision wrapperRevision = WrapperHistoryUtils.getRevision(obj);
				self.setSelectedRevision(wrapperRevision);

				TLObject currentVersionOfModel = WrapperHistoryUtils.getCurrent(obj);
				if (currentVersionOfModel != null) {
					newModel = currentVersionOfModel;
				}
			}
			self.setModel(newModel);
		}
	};

	/**
	 * Creates a new {@link AbstractRevisionSelectComponent}.
	 */
	public AbstractRevisionSelectComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	/**
	 * Returns the currently selected {@link Revision}.
	 * 
	 * @return The currently selected {@link Revision} or <code>null</code>.
	 */
	protected abstract Revision getSelectedRevision();

	@Override
	protected boolean supportsInternalModel(Object object) {
		if (object == null || object instanceof TLObject) {
			return super.supportsInternalModel(object);
		}
		return false;
	}

	/**
	 * Installs the given revision due to the {@link #setSelected(Object)} API.
	 * 
	 * @param rev
	 *        The revision of the new {@link #getSelected() selection}. May be <code>null</code> in
	 *        case selection is cleared.
	 */
	protected abstract void setSelectedRevision(Revision rev);

	/**
	 * Notifies this component that the value of {@link #getSelectedRevision()} has changed.
	 * 
	 * @param newRevision
	 *        New value of {@link #getSelectedRevision()}.
	 */
	protected final void handleRevisionChanged(Revision newRevision) {
		setSelected(getModelAtRevision(asObject(getModel()), newRevision));
	}

	static TLObject asObject(Object newModel) {
		return (TLObject) newModel;
	}

	private TLObject toSelectedRevision(TLObject model) {
		return getModelAtRevision(model, getSelectedRevision());
	}

	private TLObject getModelAtRevision(TLObject model, Revision revision) {
		if (model == null) {
			return model;
		}
		if (revision == null) {
			return model;
		}
		return WrapperHistoryUtils.getWrapper(revision, model);
	}

	@Override
	protected void afterModelSet(Object oldModel, Object newModel) {
		super.afterModelSet(oldModel, newModel);
		setSelected(toSelectedRevision(asObject(newModel)));
	}

	@Override
	public void linkChannels(Log log) {
		super.linkChannels(log);
		selectionChannel().addListener(ON_SELECTION_CHANGE);
	}
}

