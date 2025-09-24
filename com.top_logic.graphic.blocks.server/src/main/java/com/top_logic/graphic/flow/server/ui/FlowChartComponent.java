/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.server.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.graphic.flow.data.Diagram;
import com.top_logic.graphic.flow.data.SelectableBox;
import com.top_logic.graphic.flow.data.Widget;
import com.top_logic.graphic.flow.server.control.DiagramControl;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.DirtyHandling;
import com.top_logic.layout.basic.check.ChangeHandler;
import com.top_logic.layout.basic.check.MasterSlaveCheckProvider;
import com.top_logic.layout.basic.contextmenu.component.ContextMenuFactory;
import com.top_logic.layout.basic.contextmenu.component.factory.SelectableContextMenuFactory;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.channel.ComponentChannel.ChannelListener;
import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.component.SelectableWithSelectionModel;
import com.top_logic.layout.component.model.SelectionEvent;
import com.top_logic.layout.component.model.SelectionListener;
import com.top_logic.layout.scripting.action.SelectAction.SelectionChangeKind;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.structure.ControlRepresentable;
import com.top_logic.layout.structure.DialogWindowControl;
import com.top_logic.layout.table.component.BuilderComponent;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.SelectionModelConfig;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.tool.boundsec.HandlerResult;

import de.haumacher.msgbuf.observer.Listener;
import de.haumacher.msgbuf.observer.Observable;

/**
 * {@link LayoutComponent} displaying a flow chart.
 */
public class FlowChartComponent extends BuilderComponent
		implements SelectableWithSelectionModel, ControlRepresentable {

	private DiagramControl _control = new DiagramControl();

	/**
	 * User object of {@link SelectableBox} nodes mapped the box for updating the UI selection, if
	 * the selection channel changes.
	 */
	private Map<Object, List<SelectableBox>> _selectableIndex = Collections.emptyMap();

	private Map<Object, List<Widget>> _observedIndex = Collections.emptyMap();

	private final SelectionModel _selectionModel;

	boolean _uiSelectionProcessed = false;

	private final SelectionListener _updateUISelection = new SelectionListener() {
		@Override
		public void notifySelectionChanged(SelectionModel model, SelectionEvent event) {
			// Forward selection to UI.
			updateUISelection(event.getNewSelection());
		}
	};

	private final SelectionListener<Object> _updateChannelSelection = new SelectionListener<>() {
		@Override
		public void notifySelectionChanged(SelectionModel<Object> model, SelectionEvent<Object> event) {
			// Forward selection to component channel.
			Set<?> selectedUserObjects = event.getNewSelection().stream()
				.map(s -> s instanceof Widget w ? w.getUserObject() : null)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());

			setSelected(selectedUserObjects);
		}
	};

	private final Listener _processUISelection = new Listener() {

		private final List<Consumer<SelectionModel>> _deferredUpdates = new ArrayList<>();

		@Override
		public void beforeSet(Observable obj, String property, Object value) {
			if (Diagram.SELECTION__PROP.equals(property)) {
				update(selectionModel -> selectionModel.setSelection(new HashSet<>((Collection<?>) value)));
			}
		}

		@Override
		public void afterRemove(Observable obj, String property, int index, Object element) {
			if (Diagram.SELECTION__PROP.equals(property)) {
				update(selectionModel -> selectionModel.setSelected(element, false));
			}
		}

		@Override
		public void beforeAdd(Observable obj, String property, int index, Object element) {
			if (Diagram.SELECTION__PROP.equals(property)) {
				update(selectionModel -> selectionModel.setSelected(element, true));
			}
		}

		private void update(Consumer<SelectionModel> update) {
			Collection<? extends ChangeHandler> handlers =
				MasterSlaveCheckProvider.INSTANCE.getCheckScope(FlowChartComponent.this).getAffectedFormHandlers();

			DirtyHandling dirtyHandling = DirtyHandling.getInstance();
			if (!dirtyHandling.checkDirty(handlers)) {
				updateDirectly(update);
			} else {
				boolean firstUpdate = _deferredUpdates.isEmpty();
				_deferredUpdates.add(update);
				if (!firstUpdate) {
					// Dialog has been opened before.
					return;
				}
				DialogWindowControl dialog =
					dirtyHandling.createConfirmDialog(this::applyDeferredUpdates, this::cancelUpdates, handlers);

				DefaultDisplayContext.getDisplayContext()
					.getWindowScope()
					.openDialog(dialog);
			}
		}

		/**
		 * @param continuationContext
		 *        {@link DisplayContext} when applying updates.
		 */
		private HandlerResult applyDeferredUpdates(DisplayContext continuationContext) {
			boolean removed = pause();
			try {
				try {
					_deferredUpdates.forEach(deferred -> deferred.accept(_selectionModel));
					if (ScriptingRecorder.isRecordingActive()) {
						_uiSelectionProcessed = true;
					}
				} finally {
					_deferredUpdates.clear();
				}
			} finally {
				resume(removed);
			}
			return HandlerResult.DEFAULT_RESULT;
		}

		/**
		 * @param cancelContext
		 *        {@link DisplayContext} when the user cancels selection change.
		 */
		private HandlerResult cancelUpdates(DisplayContext cancelContext) {
			// Re-install old selection.
			updateUISelection(_selectionModel.getSelection());
			_deferredUpdates.clear();
			return HandlerResult.DEFAULT_RESULT;
		}

		private void updateDirectly(Consumer<SelectionModel> update) {
			boolean removed = pause();
			try {
				update.accept(_selectionModel);
				if (ScriptingRecorder.isRecordingActive()) {
					_uiSelectionProcessed = true;
				}
			} finally {
				resume(removed);
			}
		}

		private boolean pause() {
			return _selectionModel.removeSelectionListener(_updateUISelection);
		}

		private void resume(boolean removed) {
			if (removed) {
				_selectionModel.addSelectionListener(_updateUISelection);
			}
		}
	};

	private ChannelListener _processChannelSelection = new ChannelListener() {
		@Override
		public void handleNewValue(ComponentChannel sender, Object oldValue, Object newValue) {
			boolean removed = _selectionModel.removeSelectionListener(_updateChannelSelection);
			try {
				Set<SelectableBox> selectedBoxes = SearchExpression.asCollection(newValue)
					.stream()
					.flatMap(s -> _selectableIndex.getOrDefault(s, Collections.emptyList()).stream())
					.collect(Collectors.toSet());

				_selectionModel.setSelection(selectedBoxes);
			} finally {
				if (removed) {
					_selectionModel.addSelectionListener(_updateChannelSelection);
				}
			}
		}
	};

	/**
	 * Configuration options for {@link FlowChartComponent}.
	 */
	@TagName("flowChart")
	public interface Config extends BuilderComponent.Config, Selectable.SelectableConfig, SelectionModelConfig {

		@Override
		PolymorphicConfiguration<? extends FlowChartBuilder> getModelBuilder();

		/**
		 * The provider for the diagram elemnt's context menu.
		 */
		@ItemDefault(SelectableContextMenuFactory.class)
		@ImplementationClassDefault(SelectableContextMenuFactory.class)
		PolymorphicConfiguration<? extends ContextMenuFactory> getContextMenuFactory();

		@Override
		@ClassDefault(FlowChartComponent.class)
		Class<? extends LayoutComponent> getImplementationClass();
	}

	/**
	 * Creates a {@link FlowChartComponent}.
	 */
	public FlowChartComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);

		_selectionModel = createSelectionModel(config);

		ContextMenuFactory contextMenuFactory = context.getInstance(config.getContextMenuFactory());
		_control.setContextMenuProvider(contextMenuFactory.createContextMenuProvider(this));
	}

	private SelectionModel createSelectionModel(Config config) {
		SelectionModel selectionModel = config.getSelectionModelFactory().newSelectionModel(this);
		selectionModel.addSelectionListener(_updateChannelSelection);
		selectionModel.addSelectionListener(_updateUISelection);
		return selectionModel;
	}

	@Override
	public SelectionModel getSelectionModel() {
		return _selectionModel;
	}

	@Override
	protected boolean doValidateModel(DisplayContext context) {
		if (_uiSelectionProcessed) {
			/* ScriptingRecorder is paused during model validation. */
			boolean wasPaused = ScriptingRecorder.resume();
			try {
				/* Scripting is active, because the value is only set in this case. */
				ScriptingRecorder.recordSelection(this, getSelected(), true, SelectionChangeKind.ABSOLUTE);
			} finally {
				if (wasPaused) {
					ScriptingRecorder.pause();
				}
			}
			_uiSelectionProcessed = false;
		}
		return super.doValidateModel(context);
	}

	@Override
	protected void handleNewModel(Object newModel) {
		super.handleNewModel(newModel);

		Diagram before = _control.getModel();
		if (before != null) {
			before.unregisterListener(_processUISelection);
		}

		Diagram diagram = (Diagram) getBuilder().getModel(getModel(), this);
		if (diagram != null) {
			diagram.setMultiSelect(_selectionModel.isMultiSelectionSupported());
		}

		if (diagram != null) {
			_selectableIndex = diagram.getRoot().visit(new SelectableIndexCreator(), null).getIndex();
			_observedIndex = diagram.getRoot()
				.visit(new ObservedIndexCreator(node -> builder().getObserved(node, this)), null).getIndex();

			Collection<?> oldSelection = SearchExpression.asCollection(getSelected());
			Collection<?> newSelection = oldSelection.stream()
				.filter(x -> _selectableIndex.containsKey(x)).toList();
			if (newSelection.size() != oldSelection.size()) {
				setSelected(newSelection);
			}

			boolean removed = _selectionModel.removeSelectionListener(_updateChannelSelection);
			try {
				_selectionModel.clear();

				// Update the visible selection.
				Set<SelectableBox> selectedBoxes = newSelection.stream()
					.<SelectableBox> flatMap(x -> CollectionUtil.toCollection(_selectableIndex.get(x)).stream())
					.collect(Collectors.toSet());
				diagram.setSelection(new ArrayList<>(selectedBoxes));
				for (SelectableBox box : selectedBoxes) {
					box.setSelected(true);
				}
				_selectionModel.setSelection(selectedBoxes);
			} finally {
				if (removed) {
					_selectionModel.addSelectionListener(_updateChannelSelection);
				}
			}

			diagram.registerListener(_processUISelection);
		} else {
			_selectableIndex = Collections.emptyMap();
			_observedIndex = Collections.emptyMap();

			_selectionModel.clear();
		}

		_control.setModel(diagram);
	}

	@Override
	public Control getRenderingControl() {
		return _control;
	}

	@Override
	public void linkChannels(Log log) {
		super.linkChannels(log);

		linkSelectionChannel(log);

		selectionChannel().addListener(_processChannelSelection);
	}

	@Override
	protected boolean observeAllTypes() {
		return true;
	}

	@Override
	protected boolean receiveModelChangedEvent(Object changed, Object changedBy) {
		boolean result = super.receiveModelChangedEvent(changed, changedBy);

		if (!_observedIndex.getOrDefault(changed, Collections.emptyList()).isEmpty()) {
			// A part has been deleted, redraw.
			// TODO: Optimize update.
			invalidate();
			return true;
		}

		return result;
	}

	@Override
	protected boolean receiveModelDeletedEvent(Set<TLObject> deletedObjects, Object changedBy) {
		boolean result = super.receiveModelDeletedEvent(deletedObjects, changedBy);

		// Remove deleted nodes from selection.
		Collection<?> oldSelection = null;
		Set<?> newSelection = null;

		boolean touched = false;
		for (Object deleted : deletedObjects) {
			List<Widget> touchedElements = _observedIndex.getOrDefault(deleted, Collections.emptyList());
			if (!touchedElements.isEmpty()) {
				// A part has been deleted, redraw.
				if (!touched) {
					touched = true;

					// Lazy initialize only if it might be relevant.
					oldSelection = _selectionModel.getSelection();
					newSelection = new HashSet<>(oldSelection);
				}

				assert newSelection != null;
				newSelection.removeAll(touchedElements);
			}
		}

		if (touched) {
			assert newSelection != null;
			assert oldSelection != null;
			if (newSelection.size() != oldSelection.size()) {
				_selectionModel.setSelection(newSelection);
			}

			// TODO: Optimize update.
			invalidate();
		}

		return result;
	}

	private FlowChartBuilder builder() {
		return (FlowChartBuilder) getBuilder();
	}

	void updateUISelection(Set<?> newSelection) {
		HashSet<Object> newlySelected = new HashSet<>(newSelection);
		List<SelectableBox> uiSelection = _control.getModel().getSelection();
		for (int n = uiSelection.size() - 1; n >= 0; n--) {
			SelectableBox uiSelected = uiSelection.get(n);
			if (!newSelection.contains(uiSelected)) {
				uiSelection.remove(n);
				uiSelected.setSelected(false);
			}
			newlySelected.remove(uiSelected);
		}
		for (Object x : newlySelected) {
			uiSelection.add((SelectableBox) x);
			((SelectableBox) x).setSelected(true);
		}
	}
}
