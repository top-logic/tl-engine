/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.modeler.component;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.Log;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.bpe.BPEUtil;
import com.top_logic.bpe.bpml.model.Collaboration;
import com.top_logic.bpe.bpml.model.Externalized;
import com.top_logic.bpe.bpml.model.impl.ExternalizedBase;
import com.top_logic.bpe.modeler.display.BPMNDisplay;
import com.top_logic.bpe.modeler.display.BPMNSelectVetoListener;
import com.top_logic.layout.Control;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.basic.DirtyHandling;
import com.top_logic.layout.basic.check.ChangeHandler;
import com.top_logic.layout.basic.check.MasterSlaveCheckProvider;
import com.top_logic.layout.channel.ChannelSPI;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.component.SelectableWithSelectionModel;
import com.top_logic.layout.component.model.SelectionListener;
import com.top_logic.layout.form.component.Editor;
import com.top_logic.layout.form.component.edit.EditMode;
import com.top_logic.layout.scripting.action.SelectAction.SelectionChangeKind;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.structure.ControlRepresentable;
import com.top_logic.mig.html.DefaultSingleSelectionModel;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.BoundComponent;
import com.top_logic.util.Resources;

/**
 * {@link LayoutComponent} allowing to edit a BPMN diagram.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BPMLEditor extends BoundComponent
		implements SelectableWithSelectionModel, ControlRepresentable, SelectionListener, Editor, ChangeHandler,
		BPMNSelectVetoListener {

	private static final Map<String, ChannelSPI> CHANNELS =
		channels(MODEL_AND_SELECTION_CHANNEL, EditMode.EDIT_MODE_SPI);
	
	private static final ComponentChannel.ChannelListener ON_SELECTION_CHANGE = new ComponentChannel.ChannelListener() {
		@Override
		public void handleNewValue(ComponentChannel sender, Object oldValue, Object newValue) {
			SelectionModel selectionModel = ((BPMLEditor) sender.getComponent()).getSelectionModel();
			if (newValue != null) {
				if (!(newValue instanceof Externalized)) {
					throw new IllegalArgumentException(newValue + " is of type: " + newValue.getClass().getName()
						+ ". Expected: " + Externalized.class.getName());
				}
				selectionModel.setSelection(Collections.singleton(newValue));
			} else {
				selectionModel.clear();
			}
		}
	};

	/**
	 * Configuration options for {@link BPMLEditor}.
	 */
	@TagName("bpmlEditor")
	public interface Config extends BoundComponent.Config, Editor.Config, Selectable.SelectableConfig {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		@Override
		@StringDefault("applyBPMNStructuralChanges")
		String getApplyCommand();

		@Override
		@ClassDefault(BPMLEditor.class)
		Class<? extends LayoutComponent> getImplementationClass();

		@Override
		@BooleanDefault(true)
		boolean hasToolbar();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			BoundComponent.Config.super.modifyIntrinsicCommands(registry);
			Editor.Config.super.modifyIntrinsicCommands(registry);
		}

	}

	private BPMNDisplay _control;

	private SelectionModel _selection = new DefaultSingleSelectionModel(this);

	/**
	 * Creates a {@link BPMLEditor}.
	 */
	public BPMLEditor(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
		_selection.addSelectionListener(this);
	}

	@Override
	protected void becomingInvisible() {
		super.becomingInvisible();
		setViewMode();
	}

	@Override
	public Control getRenderingControl() {
		if (_control == null) {
			_control = new BPMNDisplay(getCollaboration(), getSelectionModel());
			_control.addSelectVetoListener(this);
			_control.setEditMode(isInEditMode());
		}
		return _control;
	}

	@Override
	public void invalidate() {
		super.invalidate();
		_control = null;
	}

	private Collaboration getCollaboration() {
		return (Collaboration) getModel();
	}

	@Override
	protected boolean supportsInternalModel(Object object) {
		return object == null || (super.supportsInternalModel(object) && object instanceof Collaboration);
	}

	@Override
	protected boolean observeAllTypes() {
		return true;
	}

	@Override
	protected boolean receiveModelDeletedEvent(Set<TLObject> aModel, Object changedBy) {
		boolean becameInvalid;
		if (aModel.contains(getSelected())) {
			clearSelection();
			becameInvalid = true;
		} else {
			becameInvalid = false;
		}
		boolean superInvalidated = super.receiveModelDeletedEvent(aModel, changedBy);
		return becameInvalid || superInvalidated;
	}

	@Override
	protected boolean receiveMyModelChangeEvent(Object changedBy) {
		Collaboration model = getCollaboration();
		Set<?> selection = getSelectionModel().getSelection();
		switch (selection.size()) {
			case 0:
				return super.receiveMyModelChangeEvent(changedBy);
			case 1: {
				// Ensure that the selected objects belongs to the model. There may be a new object
				// instance, but it has the same external ID.
				Externalized singleSelection = (Externalized) selection.iterator().next();
				Object correspondingPart = BPEUtil.findPart(singleSelection.getExtId(), model);
				if (singleSelection == correspondingPart) {
					return super.receiveMyModelChangeEvent(changedBy);
				}
				setSelected(correspondingPart);
				super.receiveMyModelChangeEvent(changedBy);
				return true;
			}
			default: {
				HashSet<Object> newSelection = new HashSet<>();
				boolean changed = false;
				for (Object singleSelection : selection) {
					Object correspondingPart =
						BPEUtil.findPart(((ExternalizedBase) singleSelection).getExtId(), model);
					if (correspondingPart == null) {
						changed = true;
						continue;
					}
					if (correspondingPart != singleSelection) {
						changed = true;
					}
					newSelection.add(correspondingPart);
				}
				if (!changed) {
					return super.receiveMyModelChangeEvent(changedBy);
				}
				getSelectionModel().setSelection(newSelection);
				fireSelection(newSelection);
				super.receiveMyModelChangeEvent(changedBy);
				return true;

			}
		}
	}

	@Override
	protected void handleNewModel(Object newModel) {
		super.handleNewModel(newModel);
		// new model. No chance to preserve selection.
		setSelected(null);
	}

	@Override
	public void notifySelectionChanged(SelectionModel model, Set<?> formerlySelectedObjects, Set<?> selectedObjects) {
		Object newSelection = extractSelection(selectedObjects);

		if (ScriptingRecorder.isRecordingActive()) {
			if (newSelection == null) {
				ScriptingRecorder.recordSelection(this,
					ScriptingRecorder.NO_SELECTION, false, SelectionChangeKind.ABSOLUTE);
			} else {
				ScriptingRecorder.recordSelection(
					this, newSelection, true, SelectionChangeKind.ABSOLUTE);
			}
		}

		setSelected(extractSelection(getSelectionModel().getSelection()));
	}

	@Override
	protected Map<String, ChannelSPI> channels() {
		return CHANNELS;
	}

	private Object extractSelection(Set<?> selection) {
		if (selection.isEmpty()) {
			return null;
		}
		return selection.iterator().next();
	}

	@Override
	public SelectionModel getSelectionModel() {
		return _selection;
	}

	@Override
	public void linkChannels(Log log) {
		super.linkChannels(log);
		Editor.super.linkChannels(log);

		selectionChannel().addListener(ON_SELECTION_CHANGE);
	}

	@Override
	public void handleComponentModeChange(boolean editMode) {
		BPMNDisplay display = getBPMNDisplay();
		if (display != null) {
			display.setEditMode(editMode);
		}
	}

	/**
	 * The current display, or <code>null</code> when this component is not yet rendered.
	 */
	public final BPMNDisplay getBPMNDisplay() {
		return _control;
	}

	@Override
	public boolean isChanged() {
		if (_control == null) {
			return false;
		}
		return _control.isChanged();
	}

	@Override
	public boolean hasError() {
		return false;
	}

	@Override
	public String getChangeDescription() {
		return Resources.getInstance().getString(I18NConstants.DIAGRAM_CANGE_SCOPE);
	}

	@Override
	public void checkVeto(BPMNDisplay bpmn, Object newSelection) throws VetoException {
		DirtyHandling.checkVeto(MasterSlaveCheckProvider.INSTANCE.getCheckScope(this));
	}

}
