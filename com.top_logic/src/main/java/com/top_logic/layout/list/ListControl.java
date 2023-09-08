/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.list;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import com.top_logic.base.services.simpleajax.AJAXConstants;
import com.top_logic.base.services.simpleajax.ElementReplacement;
import com.top_logic.base.services.simpleajax.FragmentInsertion;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.base.services.simpleajax.JSSnipplet;
import com.top_logic.base.services.simpleajax.RangeReplacement;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.NullList;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Attachable;
import com.top_logic.layout.Control;
import com.top_logic.layout.DNDUtil;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Drag;
import com.top_logic.layout.DragAndDropCommand;
import com.top_logic.layout.Drop;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.ResourceRenderer;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.form.FormModeModelAdapter;
import com.top_logic.layout.form.model.DefaultModeModel;
import com.top_logic.layout.form.model.ListField;
import com.top_logic.layout.form.model.ModeModel;
import com.top_logic.layout.form.model.ModeModelListener;
import com.top_logic.layout.list.model.ListUpdateAccumulator;
import com.top_logic.layout.list.model.NoSelectionModel;
import com.top_logic.layout.list.model.RestrictedListSelectionModel;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Displays a {@link ListModel}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ListControl extends AbstractControlBase implements ModeModelListener, Drag<List<?>>, Drop<List<?>> {

	private static final Map<String, ControlCommand> COMMANDS = createCommandMap(new ControlCommand[] { 

		//		ListSelectAction.INSTANCE, 
		MouseDownSelect.INSTANCE,
		ListDblClickAction.INSTANCE, 
		DragAndDropCommand.INSTANCE });
	
	/**
	 * This {@link com.top_logic.layout.Control}'s model.
	 */
	private ListModel listModel;

	/**
	 * This {@link com.top_logic.layout.Control}'s selection model.
	 */
	private ListSelectionModel selectionModel;
	
	/**
	 * Alias for {@link #selectionModel}, if the selection model in use
	 * implements the {@link RestrictedListSelectionModel} interface.
	 */
	private RestrictedListSelectionModel selectionFilter;
	
	/**
	 * @see #getListRenderer()
	 */
	private ListRenderer listRenderer = DefaultListRenderer.INSTANCE;
	
	/**
	 * The {@link Renderer} for list items.
	 * 
	 * @see #getItemContentRenderer()
	 */
	private Renderer<Object> itemContentRenderer;
	
	/**
	 * Mapping of item indexes to client-side element identifiers.
	 */
	/* package protected */ArrayList<String> itemIDs;
	
	/* package protected */final ListUpdateAccumulator updateAccumulator;

	private DoubleClickAction<Control, Object> dblClick;
	
	/** The ID of the container holding the items */
	private String contentID;

	private final ModeModel _modeModel;

	private String _fieldCssClasses;

	private ListField _listField;

	/**
	 * Creates a new list control using the {@link ListField}s
	 * {@link ListField#getListModel() list model},
	 * {@link ListField#getSelectionModel() selection model}, and
	 * {@link ListField#getItemRenderer() item renderer} if not
	 * <code>null</code>.
	 */
	public ListControl(ListField listField) {
		this(listField.getListModel(), listField.getSelectionModel(), new FormModeModelAdapter(listField));
		_listField = listField;
		Renderer<Object> itemRenderer = listField.getItemRenderer();
		if (itemRenderer != null) {
			this.itemContentRenderer = itemRenderer;
		}
		_fieldCssClasses = listField.getCssClasses();
	}
	
	/**
	 * Creates a new list control with the given list and list selection models.
	 * @param listModel
	 *        The underlying list model.
	 * @param selectionModel
	 *        See {@link #setSelectionModel(ListSelectionModel)}
	 */
	public ListControl(ListModel listModel, ListSelectionModel selectionModel) {
		this(listModel, selectionModel, new DefaultModeModel());
	}
	
	/**
	 * Creates a new list control with the given list and list selection models.
	 * 
	 * @param listModel
	 *        The underlying list model.
	 * @param selectionModel
	 *        See {@link #setSelectionModel(ListSelectionModel)}
	 * @param modeModel
	 *        Model that handles visibility of this control.
	 */
	public ListControl(ListModel listModel, ListSelectionModel selectionModel, ModeModel modeModel) {
		super(COMMANDS);
		
		this.itemContentRenderer = ResourceRenderer.PLAIN_INSTANCE;
		this.listModel = listModel;
		this.updateAccumulator = new ListUpdateAccumulator(listModel, selectionModel);
		_modeModel = modeModel;

		initSelectionModel(selectionModel);
	}
	
	/**
	 * The {@link ListField} for which this control was built, or <code>null</code>, if the control
	 * was build on-the-fly.
	 */
	public ListField getListField() {
		return _listField;
	}

	@Override
	public Object getModel() {
		return listModel;
	}

	@Override
	protected String getTypeCssClass() {
		return "cListControl";
	}

	@Override
	protected void internalAttach() {
		super.internalAttach();
		_modeModel.addModeModelListener(this);
	}

	@Override
	protected void internalDetach() {
		_modeModel.removeModeModelListener(this);
		super.internalDetach();
	}

	@Override
	protected void attachRevalidated() {
		super.attachRevalidated();
		
		updateAccumulator.attach();
		
		// must be done as Drag'n'Drop needs it
		initIdMapping();
	}

	private void initIdMapping() {
		itemIDs = new ArrayList<>(listModel.getSize());
		
		// Allocate item identifier space.
		itemIDs.addAll(0, new NullList<>(listModel.getSize()));
	}

	@Override
	protected void detachInvalidated() {
		super.detachInvalidated();

		itemIDs = null;

		updateAccumulator.detach();
	}
	
	public void setListModel(ListModel newListModel) {
		assert newListModel != null;
		
		// Make sure to deregister as listener from the old model.
		requestRepaint();
		
		this.listModel = newListModel;
		updateAccumulator.setListModel(newListModel);

		this.selectionModel.clearSelection();
	}
	
	public ListModel getListModel() {
		return listModel;
	}
	
	/**
	 * @see #setSelectionModel(ListSelectionModel)
	 */
	public ListSelectionModel getSelectionModel() {
		return selectionModel;
	}
	
	/**
	 * Updates this control's {@link SelectionModel}.
	 * 
	 * @param newSelectionModel
	 *        The new selection model. If the argument implements
	 *        {@link RestrictedListSelectionModel}, selection on some list
	 *        items can be prevented. If the argument is <code>null</code>,
	 *        no list items can be selected.
	 */
	public void setSelectionModel(ListSelectionModel newSelectionModel) {
		// Make sure to deregister as listener from the old model.
		requestRepaint();

		initSelectionModel(newSelectionModel);
		updateAccumulator.setSelectionModel(this.selectionModel);
	}		

	private void initSelectionModel(ListSelectionModel newSelectionModel) {
		if (newSelectionModel == null) {
			newSelectionModel = NoSelectionModel.INSTANCE;
		}
		
		this.selectionModel = newSelectionModel;
		
		if (newSelectionModel instanceof RestrictedListSelectionModel) {
			this.selectionFilter = (RestrictedListSelectionModel) newSelectionModel;
		} else {
			this.selectionFilter = null;
		}
	}
	
	/**
	 * @see #getItemContentRenderer()
	 */
	public void setItemContentRenderer(Renderer<Object> newItemContentRenderer) {
		this.itemContentRenderer = newItemContentRenderer;
		requestRepaint();
	}
	
	/**
	 * The renderer that is responsible for rendering a single element of this
	 * control's {@link #getListModel() list model}.
	 */
	public Renderer<Object> getItemContentRenderer() {
		return itemContentRenderer;
	}
	
	/**
	 * @see #getListRenderer()
	 */
	public void setListRenderer(ListRenderer newListRenderer) {
		this.listRenderer = newListRenderer;
		requestRepaint();
	}
	
	/**
	 * The list renderer that produces the client-side view of this control.
	 */
	public final ListRenderer getListRenderer() {
		return listRenderer;
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		if (updateAccumulator.hasUpdates()) {
			dropUpdates();
		}
		
		if (isVisible()) {
			listRenderer.write(context, out, this);
		} else {
			// Write an empty view as anchor for inserting updates.
			out.beginBeginTag(HTMLConstants.SPAN);
			writeControlAttributes(context, out);
			out.endBeginTag();
			out.endTag(HTMLConstants.SPAN);
		}
	}

	/**
	 * Whether the list element at the given index can be selected.
	 * 
	 * @see #getSelectionModel() for checking, which elements are currently
	 *      selected.
	 */
	public boolean canSelect(int itemIndex) {
		return selectionFilter == null || selectionFilter.isSelectableIndex(itemIndex);
	}
	
	/**
	 * The client-side identifier that must be used by the {@link ListRenderer}
	 * for the list item at the given index.
	 * 
	 * @see #getID() for the identifier that must be used for the top-level
	 *      element of the client-side view.
	 */
	public String getItemID(int index) {
		String result = itemIDs.get(index);

		if (result == null) {
			result = this.getScope().getFrameScope().createNewID();
			assert notExisting(Collections.singletonList(result));
			itemIDs.set(index, result);
		}
		
		return result;
	}

	@Override
	public boolean isVisible() {
		return _modeModel.getMode() != ModeModel.INVISIBLE_MODE;
	}
	
	@Override
	protected void writeControlClassesContent(Appendable out) throws IOException {
		super.writeControlClassesContent(out);
		HTMLUtil.appendCSSClass(out, _fieldCssClasses);
		listRenderer.appendControlCSSClasses(out, this);
	}

	@Override
	protected boolean hasUpdates() {
		return updateAccumulator.hasUpdates();
	}
	
	@Override
	protected void internalRevalidate(DisplayContext context, UpdateQueue actions) {
		flushUpdates(actions);
	}
	
	@Override
	protected void handleRepaintRequested(UpdateQueue actions) {
		super.handleRepaintRequested(actions);
		dropUpdates();
	}

	/**
	 * Drops pending updates.
	 */
	private void dropUpdates() {
		updateAccumulator.reset();
		initIdMapping();
	}

	/**
	 * Flushes pending updates by converting them to actions that are added to
	 * the given {@link UpdateQueue}.
	 * 
	 * <p>
	 * As side-effect, the internal state of this control is brought up to date
	 * with the current state of the model.
	 * </p>
	 */
	private void flushUpdates(final UpdateQueue actions) {
		updateAccumulator.forwardConsolidatedEvents(new ListClientUpdateProducer(actions));
		updateAccumulator.reset();
		resetClientCache(actions);
	}

	/**
	 * adds actions to reset the client side cache for the dusplayed list.
	 */
	private void resetClientCache(UpdateQueue actions) {
		actions.add(new JSSnipplet("services.form.ListControl.resetCache('" + getID() + "');"));
	}

	public final class ListClientUpdateProducer implements ListDataListener {
		private final UpdateQueue actions;

		public ListClientUpdateProducer(UpdateQueue actions) {
			this.actions = actions;
		}

		@Override
		public void contentsChanged(ListDataEvent event) {
			final int firstIndex = event.getIndex0();
			final int lastIndex = event.getIndex1();
			
			if (firstIndex == lastIndex) {
				int index = firstIndex;
				actions.add(
					new ElementReplacement(
							getItemID(index),
							new ItemFragment(index, index)));
			} else {
				actions.add(
					new RangeReplacement(
						getItemID(firstIndex),
						getItemID(lastIndex),
						new ItemFragment(firstIndex, lastIndex)));
			}
		}

		@Override
		public void intervalAdded(ListDataEvent event) {
			final int firstIndex = event.getIndex0();
			final int lastIndex = event.getIndex1();
			final int insertionCount = lastIndex - firstIndex + 1;
			
			// Note: Accessing the list model outside the range of the current
			// event is illegal. If events are buffered or consolidated, the
			// state of the list model outside the range of the current event
			// may not be consistent with the current event.
			// 
			// int sizeBefore = listModel.getSize() - insertionCount;
			
			// Shift the existing item identifiers. The identifiers at the newly
			// created indexes are filled during rendering the inserted
			// elements.
			itemIDs.addAll(firstIndex, new NullList<>(insertionCount));
			
			/*
			 * Note: Fetching the client-side identifiers for indexes larger or equal to firstIndex
			 * must be done, before inserting new indexes into the ID cache.
			 */
			if (firstIndex == 0) {
				// Insertion into a potentially empty list.
				actions.add(
					new FragmentInsertion(
						getContentID(),
						AJAXConstants.AJAX_POSITION_START_VALUE,
						new ItemFragment(firstIndex, lastIndex)));
			} else {
				// The insertion took place after some pre-existing element.
				actions.add(
					new FragmentInsertion(
						getItemID(firstIndex - 1), 
						AJAXConstants.AJAX_POSITION_AFTER_VALUE, 
						new ItemFragment(firstIndex, lastIndex)));
			}
		}

		@Override
		public void intervalRemoved(ListDataEvent event) {
			final int firstIndex = event.getIndex0();
			final int lastIndex = event.getIndex1();
			
			if (firstIndex == lastIndex) {
				actions.add(
					new ElementReplacement(
						getItemID(firstIndex),
						Fragments.empty()));
			} else {
	
				// Note: Fetching the client-side identifiers must be done, before
				// removing the corresponding indexes from the internal cache!
				actions.add(
					new RangeReplacement(
						getItemID(firstIndex),
						getItemID(lastIndex), 
						Fragments.empty()));
			}
			// Remove corresponding item identifiers.
			for (int index = lastIndex; index >= firstIndex; index--) {
				itemIDs.remove(index);
			}
		
		}
	}
	
	/**
	 * Returns the action to execute when a double click on some item of the
	 * list occurs.
	 * 
	 * @return may be <code>null</code> if no special action shall be executed.
	 */
	public DoubleClickAction<Control, Object> getDblClickAction() {
		return this.dblClick;
	}
	
	/** 
	 * This method sets the double click action.
	 * 
	 * @see #getDblClickAction()
	 */
	@SuppressWarnings("unchecked")
	public void setDblClickAction(DoubleClickAction<? extends Control, ? extends Object> dblClick) {
		this.dblClick = (DoubleClickAction<Control, Object>) dblClick;
		requestRepaint();
	}
	
	/**
	 * Returns the id of the GUI element which is the direct parent of the GUI
	 * elements for the sole items. the ID is as unique as the {@link #getID()
	 * ID} of this control is.
	 * 
	 * <p>
	 * <b>Note:</b> this method must not be called before the
	 * {@link #getScope() control scope} of this control was set, i.e. before
	 * the control flow has reached
	 * {@link #internalWrite(DisplayContext, TagWriter)}.
	 * </p>
	 */
	public String getContentID() {
		if (contentID == null) {
			assert getScope() != null : "No control scope was set. Method was called before attach(ControlScope) was called.";
			contentID = getScope().getFrameScope().createNewID();
		}
		return contentID;
	}
	
	/**
	 * Dispatches the {@link #execute(DisplayContext, Control, Map)} to the
	 * {@link ListControl#getDblClickAction() double click action} of the
	 * {@link ListControl} the instance of this {@link ControlCommand} belongs
	 * to.
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	private static class ListDblClickAction extends ControlCommand {

		/**
		 * Command parameter holding the {@link ListControl#getItemID(int) item
		 * identifier} of the double clicked list item.
		 */
		private static final Object ITEM_ID_PARAM = "item";

		public static final ListControl.ListDblClickAction INSTANCE = new ListControl.ListDblClickAction();

		private ListDblClickAction() {
			super("listDblClick");
		}

		@Override
		protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
			String itemID = (String) arguments.get(ITEM_ID_PARAM);
			ListControl listControl = (ListControl) control;
			return listControl.getDblClickAction().handleDoubleClick(commandContext, listControl,
				listControl.itemIDs.indexOf(itemID));
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.LIST_DOUBLE_CLICK;
		}

	}

	/**
	 * Action that changes the current list selection via on click.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class ListSelectAction extends ControlCommand {

		private static final String COMMAND_NAME = "listSelect";
		
		/**
		 * Command parameter holding the
		 * {@link ListControl#getItemID(int) item identifier} of the clicked
		 * list item.
		 */
		private static final Object ITEM_ID_PARAM = "item";
		
		/**
		 * Command parameter that decides, whether the shift key was hold during
		 * the click of the list item.
		 */
		private static final Object SHIFT_MOD_PARAM = "shift";
		
		/**
		 * Command parameter that decides, whether the ctrl key was hold during
		 * the click on the list item.
		 */
		private static final Object CTRL_MOD_PARAM = "ctrl";

		static final ListSelectAction INSTANCE = new ListSelectAction(COMMAND_NAME);
		
		protected ListSelectAction(String id) {
			super(id);
		}

		@Override
		protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
			ListControl listControl = (ListControl) control;
			ListField listField = listControl.getListField();

			HandlerResult result;
			if (ScriptingRecorder.isRecordingActive() && listField != null
				&& !ScriptingRecorder.mustNotRecord(listField)) {
				ListSelectionModel selectionModel = listControl.getSelectionModel();

				Set<Object> selectedBefore = new HashSet<>();
				ListModel<?> listModel = listControl.getListModel();
				for (int n = selectionModel.getMinSelectionIndex(), max = selectionModel.getMaxSelectionIndex(); n <= max; n++) {
					if (selectionModel.isSelectedIndex(n)) {
						selectedBefore.add(listModel.getElementAt(n));
					}
				}

				result = doExecute(listControl, arguments);

				Set<Object> selectedAfter = new HashSet<>();
				for (int n = selectionModel.getMinSelectionIndex(), max = selectionModel.getMaxSelectionIndex(); n <= max; n++) {
					if (selectionModel.isSelectedIndex(n)) {
						selectedAfter.add(listModel.getElementAt(n));
					}
				}

				ListSelectionChange change = TypedConfiguration.newConfigItem(ListSelectionChange.class);
				change.setModel(ModelResolver.buildModelName(listField));
				List<ModelName> added = change.getAdded();
				for (Object element : selectedAfter) {
					if (!selectedBefore.contains(element)) {
						added.add(ModelResolver.buildModelName(listField, element));
					}
				}
				List<ModelName> removed = change.getRemoved();
				if (added.isEmpty() || selectionModel.getSelectionMode() != ListSelectionModel.SINGLE_SELECTION) {
					for (Object element : selectedBefore) {
						if (!selectedAfter.contains(element)) {
							removed.add(ModelResolver.buildModelName(listField, element));
						}
					}
				}
				if (!(added.isEmpty() && removed.isEmpty())) {
					ScriptingRecorder.recordAction(change);
				}
			} else {
				result = doExecute(listControl, arguments);
			}
			return result;
		}

		private HandlerResult doExecute(ListControl theListControl, Map<String, Object> arguments) {
			ListSelectionModel theSelectionModel = theListControl.getSelectionModel();

			String itemID = (String) arguments.get(ITEM_ID_PARAM);
			int clickedItemIndex = theListControl.itemIDs.indexOf(itemID);
			if (clickedItemIndex < 0) {
				// Got a click of an element that is not part of this control.
				Logger.warn("Received list selection for non-existing list item: itemID='" + itemID + "'", this);
				return HandlerResult.DEFAULT_RESULT;
			}
			
			if (! theListControl.canSelect(clickedItemIndex)) {
				Logger.warn("Received list selection of non-selectable list item: itemID='" + itemID + "', clickedItemIndex=" + clickedItemIndex, this);
				return HandlerResult.DEFAULT_RESULT;
			}
			
			boolean shiftModifier = ((Boolean) arguments.get(SHIFT_MOD_PARAM)).booleanValue();
			boolean ctrlModifier = ((Boolean) arguments.get(CTRL_MOD_PARAM)).booleanValue();
			/* Logic is copied to ajax-form.js servies.form.ListControl */
			if (shiftModifier && (!theListControl.isSingleSelection())) {
				// The shift modifier enables range selections.
				int leadSelectionIndex = theSelectionModel.getLeadSelectionIndex();
				if (leadSelectionIndex == -1) {
					/* nothing is currently selected so handle it like single selection of first
					 * element. */
					leadSelectionIndex = 0;
				}
				if (ctrlModifier) {
					if (theSelectionModel.isSelectedIndex(clickedItemIndex)) {
						theSelectionModel.removeSelectionInterval(leadSelectionIndex, clickedItemIndex);
					} else {
						theSelectionModel.addSelectionInterval(leadSelectionIndex, clickedItemIndex);
					}
				} else {
					theSelectionModel.setSelectionInterval(leadSelectionIndex, clickedItemIndex);
				}
			} else {
				if (ctrlModifier) {
					// The ctrl modifier toggles the selection of the clicked
					// item.
					if (theSelectionModel.isSelectedIndex(clickedItemIndex)) {
						theSelectionModel.removeSelectionInterval(clickedItemIndex, clickedItemIndex);
					} else {
						theSelectionModel.addSelectionInterval(clickedItemIndex, clickedItemIndex);
					}
				} else {
					if (theSelectionModel.isSelectedIndex(clickedItemIndex)) {
						int minSelectionIndex = theSelectionModel.getMinSelectionIndex();
						int maxSelectionIndex = theSelectionModel.getMaxSelectionIndex();
						if (clickedItemIndex > Math.max(0, minSelectionIndex) && maxSelectionIndex >= clickedItemIndex) {
							theSelectionModel.removeSelectionInterval(0, Math.min(maxSelectionIndex, clickedItemIndex - 1));
						}
						ListModel<?> listModel = theListControl.getListModel();
						int size = listModel.getSize();
						if (clickedItemIndex < Math.min(size - 1, maxSelectionIndex) && minSelectionIndex <= clickedItemIndex) {
							theSelectionModel.removeSelectionInterval(clickedItemIndex + 1, Math.min(maxSelectionIndex, size - 1));

						}
					}
					// A click on a not selected item removes the current
					// selection
					// and selects exactly the clicked item.
					/*
					 * A click on some selected index must also clear the
					 * selection: If more than one index is selected and a
					 * selected item is clicked it is expected that the newly
					 * clicked item is the single selection.
					 */
					else {
						theSelectionModel.clearSelection();
						theSelectionModel.addSelectionInterval(clickedItemIndex, clickedItemIndex);
					}
				}
			}
			
			return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.LIST_SELECT;
		}
		
	}

	/**
	 * Action that changes the current list selection via on mouse down.
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	public static class MouseDownSelect extends ListSelectAction {

		static final MouseDownSelect INSTANCE = new MouseDownSelect("mouseDownSelect");

		protected MouseDownSelect(String id) {
			super(id);
		}

		@Override
		protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
			ListControl listControl = (ListControl) control;
			Attachable updates = listControl.updateAccumulator;
			assert updates.isAttached() : "Try to change incremental without attached "
				+ ListUpdateAccumulator.class.getSimpleName();

			updates.detach();
			try {
				return super.execute(commandContext, control, arguments);
			} finally {
				updates.attach();

			}
		}

	}
	
	/**
	 * Incremental update re-painting a contiguous sequence of list elements.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	private final class ItemFragment implements HTMLFragment {
		
		/**
		 * First rendered index.
		 */
		private final int firstIndex;

		/**
		 * Last rendered index.
		 */
		private final int lastIndex;

		/*package protected*/ ItemFragment(int firstIndex, int lastIndex) {
			this.firstIndex = firstIndex;
			this.lastIndex = lastIndex;
		}

		@Override
		public void write(DisplayContext context, TagWriter out) throws IOException {
			getListRenderer().renderItems(context, out, ListControl.this, firstIndex, lastIndex + 1);
		}
	}

	/*
	 * The collection holding the Drag&amp;Drop targets. The first approach to
	 * store the targets in a {@link Map} is currently not possible since the
	 * {@link Drop} could be a {@link Control} which does not have an ID at time
	 * it is added to this {@link ListControl}.
	 */
	private Collection<Drop<? super List<?>>> dndTargets = new ArrayList<>();

	/*
	 * The {@link DropAcceptor} to delegate the actual drop operation.
	 */
	private DropAcceptor<Object> dropAcceptor = DefaultDropAcceptor.OBJECT_INSTANCE;

	/**
	 * Adds the given object as potential {@link Drop} target.
	 */
	public void addDropTarget(Drop<? super List<?>> target) {
		if (target == null) {
			throw new NullPointerException("target must not be null");
		}
		dndTargets.add(target);
	}

	/**
	 * Removes the given object from the potential {@link Drop} targets.
	 * 
	 * @see #addDropTarget(Drop)
	 */
	public void removeDropTarget(Drop<? super List<?>> target) {
		dndTargets.remove(target);
	}
	
	@Override
	public void notifyDrag(String dropId, Object dragInfo, Object dropInfo) {
		for (Drop<? super List<?>> drop : dndTargets) {
			if (dropId.equals(drop.getID())) {
				@SuppressWarnings("unchecked")
				List<String> elementIds = (List<String>) dragInfo;

				final List<Object> elementsToMove = getElements(elementIds);

				int[] indexOfDraggedElements = getIndexes(elementIds);

				int numberMovedElements = indexOfDraggedElements.length;
				final Object[] draggedElements = new Object[numberMovedElements];
				final boolean[] selection = new boolean[numberMovedElements];

				storeValues(indexOfDraggedElements, selection, draggedElements);
				try {
					drop.notifyDrop(elementsToMove, dropInfo);
				} catch (DropException ex) {
					restoreOldValues(indexOfDraggedElements, selection, draggedElements);
				}
				break;
			}
		}
	}

	/**
	 * Returns a list containing the business object for the client side IDS in the same order
	 */
	private List<Object> getElements(List<String> clientSideIDs) {
		ArrayList<Object> elements = new ArrayList<>();
		for (int index = 0, size = clientSideIDs.size(); index < size; index++) {
			final String clientSideID = clientSideIDs.get(index);
			final int indexOfID = itemIDs.indexOf(clientSideID);
			final Object businedssObject = listModel.getElementAt(indexOfID);
			elements.add(businedssObject);
		}
		return elements;
	}

	/**
	 * @param indexOfDraggedElements
	 *        the indexes of the elements which are moved
	 * @param selection
	 *        former filled by {@link #storeValues(int[], boolean[], Object[])}
	 * @param draggedElements
	 *        former filled by {@link #storeValues(int[], boolean[], Object[])}
	 * 
	 * @see #storeValues(int[], boolean[], Object[])
	 */
	private void restoreOldValues(int[] indexOfDraggedElements, boolean[] selection, Object[] draggedElements) {
		boolean wasAttached = updateAccumulator.detach();
		for (int i = 0; i < indexOfDraggedElements.length; i++) {
			final int index = indexOfDraggedElements[i];
			((DefaultListModel) listModel).add(index, draggedElements[i]);
			if (selection[i]) {
				selectionModel.addSelectionInterval(index, index);
			}
		}
		if (wasAttached) {
			updateAccumulator.attach();
		}

		/* itemIDs will be deleted in requestRepaint so no need to reset it. */
		requestRepaint();
	}

	/**
	 * @param elementIDs
	 *        the list of client side ids.
	 * 
	 * @return array containing the indexes in ascending order
	 * */
	private int[] getIndexes(List<String> elementIDs) {
		int[] indexOfDraggedElements = new int[elementIDs.size()];
		for (int index = 0, size = elementIDs.size(); index < size; index++) {
			indexOfDraggedElements[index] = itemIDs.indexOf(elementIDs.get(index));
		}
		Arrays.sort(indexOfDraggedElements);
		return indexOfDraggedElements;
	}

	/**
	 * @param indexOfDraggedElements
	 *        the indexes of the elements which are moved
	 * @param elements
	 *        array filled with the moved business objects
	 * @param selection
	 *        array filled with the selection of the moved elements.
	 * 
	 * @see #restoreOldValues(int[], boolean[], Object[])
	 */
	private void storeValues(int[] indexOfDraggedElements, boolean[] selection,
			Object[] elements) {
		boolean wasAttached = updateAccumulator.detach();
		for (int i = indexOfDraggedElements.length - 1; i >= 0; i--) {
			int index = indexOfDraggedElements[i];
			itemIDs.remove(index);

			// Remove selection first, before removing element from list model, to ensure that any
			// list field listener can operate on a valid list field state.
			selectionModel.removeIndexInterval(index, index);

			final Object draggedElem = ((DefaultListModel) listModel).remove(index);
			selection[i] = selectionModel.isSelectedIndex(index);
			elements[i] = draggedElem;
		}
		if (wasAttached) {
			updateAccumulator.attach();
		}
	}

	/**
	 * Creates the {@link HTMLConstants#TL_DRAG_N_DROP} attribute.
	 * 
	 * @see DNDUtil#writeDNDInfo(TagWriter, com.top_logic.layout.FrameScope, Collection)
	 */
	public void writeDNDInfo(DisplayContext context, TagWriter out) throws IOException {
		if (!(listModel instanceof DefaultListModel)) {
			return;
		}
		DNDUtil.writeDNDInfo(out, getScope().getFrameScope(), dndTargets);
	}

	@Override
	public void notifyDrop(List<? extends Object> businessObjects, Object dropInfo) throws DropException {
		@SuppressWarnings("unchecked")
		Map<String, Object> dropArgs = (Map<String, Object>) dropInfo;

		String pos = (String) dropArgs.get("position");
		if (StringServices.isEmpty(pos)) {
			requestRepaint();
			throw new DropException("no position given.");
		}

		int indexToInsert;
		if ("end".equals(pos)) {
			indexToInsert = listModel.getSize();
		} else {
			indexToInsert = itemIDs.indexOf(pos);
			if (indexToInsert < 0) {
				requestRepaint();
				throw new DropException("Unknown position '" + pos + "'");
			}
		}

		@SuppressWarnings("unchecked")
		List<String> newIDs = (List<String>) dropArgs.get("newElementIDs");
		boolean wasAttached = updateAccumulator.detach();
		try {
			boolean insertedCorrect = dropAcceptor.accept(businessObjects, listModel, selectionModel, indexToInsert);
			if (!insertedCorrect || newIDs == null) {
				/* itemIDs are rebuild during rendering */
				requestRepaint();
			} else {
				assert notExisting(newIDs);
				itemIDs.addAll(indexToInsert, newIDs);
			}
		} catch (DropException ex) {
			requestRepaint();
			throw ex;
		} finally {
			if (wasAttached) {
				updateAccumulator.attach();
			}
		}

	}
	
	private boolean notExisting(List<String> newIDs) {
		for (String id : newIDs) {
			if (itemIDs.contains(id)) {
				Logger.warn("ID " + id + " already known", ListControl.class);
			}
		}
		return true;
	}

	/**
	 * This method is a hook to give the {@link ListControl} logic for the drop
	 * operation.
	 * 
	 * @param acceptor
	 *        must not be <code>null</code>
	 */
	public void setDropAcceptor(DropAcceptor<Object> acceptor) {
		if (acceptor == null) {
			throw new NullPointerException("acceptor must not be null");
		}
		this.dropAcceptor = acceptor;
	}

	/**
	 * Checks whether the {@link #getSelectionModel() selection model} of this control is in single
	 * selection mode.
	 */
	public final boolean isSingleSelection() {
		return getSelectionModel().getSelectionMode() == ListSelectionModel.SINGLE_SELECTION;
	}

	@Override
	public void handleModeChange(Object sender, int oldMode, int newMode) {
		if (newMode == ModeModel.INVISIBLE_MODE || oldMode == ModeModel.INVISIBLE_MODE) {
			requestRepaint();
		}
	}

}
