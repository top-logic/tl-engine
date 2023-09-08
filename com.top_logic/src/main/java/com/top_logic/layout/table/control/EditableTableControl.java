/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.control;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.WrappedModel;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.control.ImageButtonRenderer;
import com.top_logic.layout.form.control.SelectionControl;
import com.top_logic.layout.form.model.DefaultModeModel;
import com.top_logic.layout.form.model.ModeModel;
import com.top_logic.layout.form.model.ModeModelListener;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.table.I18NConstants;
import com.top_logic.layout.table.ITableRenderer;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.model.EditableTableModel;
import com.top_logic.layout.table.model.SelectionTableModel;
import com.top_logic.layout.table.model.TableUtil;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A {@link TableControl} that displays an {@link EditableTableModel} and allows
 * to interactively add and delete rows as well as moving them up or down.
 * 
 * <p>
 * To allow interactive modifications of rows, an {@link EditableTableControl} adds
 * an a set of buttons to the table header. 
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class EditableTableControl extends TableControl implements ModeModelListener {

	/**
	 * Moving rows up and down is enabled.
	 * 
	 * @see #EditableTableControl(TableData, Map, ITableRenderer, ModeModel, boolean)
	 */
	public static final boolean ROW_MOVE = true;

	/**
	 * Moving rows up and down is disabled.
	 * 
	 * @see #EditableTableControl(TableData, Map, ITableRenderer, ModeModel, boolean)
	 */
	public static final boolean NO_ROW_MOVE = false;

	/**
	 * Name of the choose command.
	 */
	public static final String CHOOSE_COMMAND_NAME = "openRowSelector";
	
	/**
	 * Name of the delete row command.
	 */
	public static final String DEL_COMMAND_NAME = "removeRow";

	/**
	 * Name of the move row to top command.
	 */
	public static final String TOP_COMMAND_NAME = "moveRowTop";
	
	/**
	 * Name of the move row up command.
	 */
	public static final String UP_COMMAND_NAME = "moveRowUp";
	
	/**
	 * Name of the move row to bottom command.
	 */
	public static final String BOTTOM_COMMAND_NAME = "moveRowBottom";
	
	/**
	 * Name of the move row down command.
	 */
	public static final String DOWN_COMMAND_NAME = "moveRowDown";
	
	/**
	 * Default {@link #CHOOSE_COMMAND_NAME choose command}.
	 */
	public static final ChooseAction DEFAULT_CHOOSE_ACTION = new ChooseAction(CHOOSE_COMMAND_NAME);
	
	/**
	 * Default {@link #DEL_COMMAND_NAME delete row command}.
	 */
	public static final RemoveRowAction DEFAULT_DEL_ACTION = new RemoveRowAction(DEL_COMMAND_NAME);
	
	/**
	 * Default {@link #UP_COMMAND_NAME move row up command}.
	 */
	public static final MoveRowUpAction DEFAULT_UP_ACTION = new MoveRowUpAction(UP_COMMAND_NAME);

	/**
	 * Default {@link #DOWN_COMMAND_NAME move row down command}.
	 */
	public static final MoveRowDownAction DEFAULT_DOWN_ACTION = new MoveRowDownAction(DOWN_COMMAND_NAME);
	
	/**
	 * Default {@link #TOP_COMMAND_NAME move row to top command}.
	 */
	public static final MoveRowToTopAction DEFAULT_TOP_ACTION = new MoveRowToTopAction(TOP_COMMAND_NAME);
	
	/**
	 * Default {@link #BOTTOM_COMMAND_NAME move row to bottom command}.
	 */
	public static final MoveRowToBottomAction DEFAULT_BOTTOM_ACTION = new MoveRowToBottomAction(BOTTOM_COMMAND_NAME);
	

	private static final Map<String, ControlCommand> EDITABLE_TABLE_COMMANDS =
		createCommandMap(
			TABLE_COMMANDS, 
			new TableCommand[] {
				DEFAULT_DEL_ACTION,
				DEFAULT_UP_ACTION,
				DEFAULT_DOWN_ACTION,
				DEFAULT_CHOOSE_ACTION,
				DEFAULT_TOP_ACTION,
				DEFAULT_BOTTOM_ACTION
			});
	
	
	/** Buttons for moving table rows */
    
	/*package protected*/ ButtonControl upButton, downButton, topButton, bottomButton, delButton, chooseButton;
	
	private ModeModel modeModel;
	
	SelectionListener _selectionListener = new SelectionListener();

    /** Holds the own (not external added) buttons of this table. */
	private ArrayList<Control> editButtons;

	public EditableTableControl(TableData model, ITableRenderer renderer, boolean rowMove) {
		this(model, renderer, new DefaultModeModel(), rowMove);
	}

	public EditableTableControl(TableData model, ITableRenderer renderer, ModeModel aModeModel, boolean rowMove) {
		
		this(model, EDITABLE_TABLE_COMMANDS, renderer, aModeModel, rowMove);
	}

	/**
	 * Creates a {@link EditableTableControl}.
	 * 
	 * @param model
	 *        The table to display.
	 * @param tableCommands
	 *        See {@link TableControl#TableControl(TableData, Map, ITableRenderer)}
	 * @param renderer
	 *        See {@link TableControl#TableControl(TableData, Map, ITableRenderer)}
	 * @param aModeModel
	 *        The model that decides about mode of the {@link #getTableData()}.
	 * @param rowMove
	 *        Whether rows can be moved up and down.
	 */
	public EditableTableControl(TableData model, Map<String, ControlCommand> tableCommands, ITableRenderer renderer,
			ModeModel aModeModel, boolean rowMove) {
		
		super(model, tableCommands, renderer);
		
		this.modeModel = aModeModel;
		
		ControlCommand chooseAction = getCommand(CHOOSE_COMMAND_NAME);
		if (chooseAction != null) {
			CommandModel chooseCommandModel = newTableCommandModel(chooseAction);
			ScriptingRecorder.annotateAsDontRecord(chooseCommandModel);
			chooseCommandModel.setImage(Icons.OPEN_SELECTOR);
			chooseCommandModel.setNotExecutableImage(Icons.OPEN_SELECTOR_DISABLED);
			chooseButton = new ButtonControl(chooseCommandModel, ImageButtonRenderer.INSTANCE);
		}

		ControlCommand delAction = getCommand(DEL_COMMAND_NAME);
		if (delAction != null) {
			CommandModel delCommandModel = newTableCommandModel(delAction);
			delCommandModel.setImage(Icons.DELETE_TOOLBAR);
			delCommandModel.setNotExecutableImage(Icons.DELETE_TOOLBAR_DISABLED);
			delButton = new ButtonControl(delCommandModel, ImageButtonRenderer.INSTANCE);
		}
       
		if (rowMove) {
			
			ControlCommand topAction = getCommand(TOP_COMMAND_NAME);
			if (topAction != null) {
				CommandModel topCommandModel = newTableCommandModel(topAction);
				topCommandModel.setImage(Icons.MOVE_ROW_TO_TOP);
				topCommandModel.setNotExecutableImage(Icons.MOVE_ROW_TO_TOP_DISABLED);
				topButton = new ButtonControl(topCommandModel, ImageButtonRenderer.INSTANCE);
			}
	
			ControlCommand upAction = getCommand(UP_COMMAND_NAME);
			if (upAction != null) {
				CommandModel upCommandModel = newTableCommandModel(upAction);
				upCommandModel.setImage(Icons.MOVE_ROW_UP);
				upCommandModel.setNotExecutableImage(Icons.MOVE_ROW_UP_DISABLED);
				upButton = new ButtonControl(upCommandModel, ImageButtonRenderer.INSTANCE);
			}
	       
	        ControlCommand downAction = getCommand(DOWN_COMMAND_NAME);
	        if (downAction != null) {
				CommandModel downCommandModel = newTableCommandModel(downAction);
	        	downCommandModel.setImage(Icons.MOVE_ROW_DOWN);
	        	downCommandModel.setNotExecutableImage(Icons.MOVE_ROW_DOWN_DISABLED);
	        	downButton = new ButtonControl(downCommandModel, ImageButtonRenderer.INSTANCE);
	        }
	
	        ControlCommand bottomAction = getCommand(BOTTOM_COMMAND_NAME);
	        if (bottomAction != null) {
				CommandModel bottomCommandModel = newTableCommandModel(bottomAction);
	        	bottomCommandModel.setImage(Icons.MOVE_ROW_TO_BOTTOM);
	        	bottomCommandModel.setNotExecutableImage(Icons.MOVE_ROW_TO_BOTTOM_DISABLED);
	        	bottomButton = new ButtonControl(bottomCommandModel,ImageButtonRenderer.INSTANCE);
	        }
	    }
        
		this.editButtons = new ArrayList<>();
        if (delButton != null) {
            editButtons.add(delButton);
        }
        if (topButton != null) {
            editButtons.add(topButton);
        }
        if (upButton != null) {
            editButtons.add(upButton);
        }
        if (downButton != null) {
            editButtons.add(downButton);
        }
        if (bottomButton != null) {
            editButtons.add(bottomButton);
        }
        if (chooseButton != null) {
            editButtons.add(chooseButton);
        }
		addTitleBarControls(editButtons);
	}
	
	/*package protected*/ int getMode() {
		return this.modeModel.getMode();
	}
	
	public void setModeModel(ModeModel newModeModel) {
		int oldMode = modeModel.getMode();
		
		boolean wasAttached = isAttached();
		if (wasAttached) {
			modeModel.removeModeModelListener(this);
		}
		
		this.modeModel = newModeModel;
		
		if (wasAttached) {
			modeModel.addModeModelListener(this);
		}
		
		int newMode = modeModel.getMode();
		
		if (newMode != oldMode) {
			// Send synthesized event to reflect the mode model change.
			handleModeChange(modeModel, oldMode, newMode);
		}
	}

	/**
	 * If this TableControl is currently not selectable due to
	 * {@link ModeModel#DISABLED_MODE}, {@link ModeModel#IMMUTABLE_MODE}, or
	 * {@link ModeModel#INVISIBLE_MODE} this method returns <code>false</code>.
	 * 
	 * @see TableControl#isSelectable()
	 */
	@Override
	public boolean isSelectable() {
		if (isSelectableByMode()) {
			return true;
		}
		return super.isSelectable();
	}
	
	private boolean isSelectableByMode() {
		return modeModel.getMode() == ModeModel.EDIT_MODE;
	}

	@Override
	public void handleModeChange(Object sender, int oldMode, int newMode) {
        switch (newMode) {
		case ModeModel.EDIT_MODE: {
			setVisible(true);
				updateButtons(editButtons, true, false);
			break;
		}
		case ModeModel.DISABLED_MODE: {
			setVisible(true);
				updateButtons(editButtons, true, true);
			break;
		}
		case ModeModel.IMMUTABLE_MODE: {
			setVisible(true);
				updateButtons(editButtons, false, true);
			break;
		}
		case ModeModel.INVISIBLE_MODE: {
			setVisible(false);
				updateButtons(editButtons, false, false);
			break;
		}
		default: {
			throw new IllegalArgumentException("Mode not supported: " + newMode);
		}
		}

		if (chooseButton != null) {
			if (!providesChooser()) {
				chooseButton.setVisible(false);
			}
		}

		// Ensure initial  consistency.
		SelectionModel selectionModel = getSelectionModel();
		_selectionListener.notifySelectionChanged(selectionModel, selectionModel.getSelection(),
			Collections.emptySet());

		requestRepaint();
	}

	private void updateButtons(List controls, boolean visible, boolean disabled) {
		for(int n = 0, cnt = controls.size(); n < cnt; n++) {
			Control control = (Control) controls.get(n);
			if (control instanceof ButtonControl) {
				ButtonControl button = (ButtonControl) control;
				
				button.setVisible(visible);
				if (visible) {
					if (disabled) {
						button.disable(I18NConstants.TABLE_DISABLED);
					} else {
						button.enable();
					}
				}
			}
		}
	}

	private SelectField getSelectField(TableModel aTableModel) {
		if (aTableModel instanceof WrappedModel) {
			aTableModel = (TableModel) ((WrappedModel) aTableModel).getWrappedModel();
		}
		if (aTableModel instanceof SelectionTableModel) {
			return ((SelectionTableModel) aTableModel).getField();
		} else
			throw new IllegalArgumentException("First ensure via providesChooser(TableModel) that this table model has a SelectField!");
	}
	
	private boolean providesChooser() {
		TableModel model = getApplicationModel();
	    if (model instanceof WrappedModel) {
	    	model = (TableModel) ((WrappedModel) model).getWrappedModel();
	    }
	    
	    return model instanceof SelectionTableModel;
	}

	@Override
	protected void internalAttach() {
		super.internalAttach();
		modeModel.addModeModelListener(this);
		handleModeChange(modeModel, -1, modeModel.getMode());
	}
	
	@Override
	protected void internalDetach() {
		super.internalDetach();
		modeModel.removeModeModelListener(this);
	}
	
	@Override
	protected void attachRevalidated() {
	    super.attachRevalidated();
	    
		SelectionModel selectionModel = getSelectionModel();
		selectionModel.addSelectionListener(_selectionListener);
        
        // Ensure initial consistency.
		_selectionListener.notifySelectionChanged(selectionModel, selectionModel.getSelection(), Collections.emptySet());
	}

	@Override
	protected void detachInvalidated() {
		super.detachInvalidated();
		
		getSelectionModel().removeSelectionListener(_selectionListener);
	}
	
	/**
	 * Removes the selected row. 
	 */
	public static class RemoveRowAction extends TableCommand {

		/** 
		 * Singleton constructor. 
		 */
		protected RemoveRowAction(String id) {
			super(id);
		}
		
		@Override
		protected HandlerResult execute(DisplayContext context, TableControl table, Map<String, Object> arguments) {
			TableViewModel viewModel = table.getViewModel();
			EditableTableModel applicationModel = (EditableTableModel) table.getApplicationModel();
			
			if (((EditableTableControl) table).getMode() != ModeModel.EDIT_MODE) {
				throw new IllegalStateException("Control not in edit mode.");
			}
			
			// Find the row to remove.
			int theRowID = TableUtil.getSingleSelectedRow(table.getTableData());

			// Execute the remove.
			applicationModel.removeRow(viewModel.getApplicationModelRow(theRowID));
			if (applicationModel instanceof SelectionTableModel) {
				((SelectionTableModel) applicationModel).getField().check();
			}
            return HandlerResult.DEFAULT_RESULT;
		}
		
        @Override
		public ResKey getI18NKey() {
            return I18NConstants.REMOVE_ROW;
        }

	}
	
	/**
	 * Moves the selected row to the beginning of the table. 
	 */
	public static class MoveRowToTopAction extends TableCommand {
		
		/**
		 * Singleton constructor. 
		 */
		protected MoveRowToTopAction(String id) {
			super(id);
		}
		
		@Override
		protected HandlerResult execute(DisplayContext context, TableControl table, Map<String, Object> arguments) {
			TableViewModel viewModel = table.getViewModel();
			EditableTableModel applicationModel = (EditableTableModel) table.getApplicationModel();
			
			if (((EditableTableControl) table).getMode() != ModeModel.EDIT_MODE) {
				throw new IllegalStateException("Control not in edit mode.");
			}
			
			// Find the row to move.
			int theRowID = TableUtil.getSingleSelectedRow(table.getTableData());
			
			// Execute the move.
			applicationModel.moveRowToTop(viewModel.getApplicationModelRow(theRowID));
			TableUtil.selectRow(table.getTableData(), 0);
			return HandlerResult.DEFAULT_RESULT;
		}
		
        @Override
		public ResKey getI18NKey() {
            return I18NConstants.MOVE_ROW_TO_TOP;
        }
		
	}
	
	/**
	 * Moves the selected row one row up. 
	 */
	public static class MoveRowUpAction extends TableCommand {

		/** 
		 * Singleton constructor. 
		 */ 
		protected MoveRowUpAction(String id) {
			super(id);
		}
		
		@Override
		protected HandlerResult execute(DisplayContext context, TableControl table, Map<String, Object> arguments) {
			TableViewModel viewModel = table.getViewModel();
			EditableTableModel applicationModel = (EditableTableModel) table.getApplicationModel();
			
			if (((EditableTableControl) table).getMode() != ModeModel.EDIT_MODE) {
				throw new IllegalStateException("Control not in edit mode.");
			}
			
			// Find the row to move.
			int theRowID = TableUtil.getSingleSelectedRow(table.getTableData());

			// Execute the move.
			if(theRowID >= 0) {
				applicationModel.moveRowUp(viewModel.getApplicationModelRow(theRowID));
				theRowID = (theRowID - 1 >= 0) ? (theRowID - 1) : 0;
				TableUtil.selectRow(table.getTableData(), theRowID);
			}
            return HandlerResult.DEFAULT_RESULT;
		}
		
        @Override
		public ResKey getI18NKey() {
            return I18NConstants.MOVE_ROW_UP;
        }
	}
	
	/**
	 * Moves the selected row one row down. 
	 */
	public static class MoveRowDownAction extends TableCommand {

		/** 
		 * Singleton constructor. 
		 */
		protected MoveRowDownAction(String id) {
			super(id);
		}
		
		@Override
		protected HandlerResult execute(DisplayContext context, TableControl table, Map<String, Object> arguments) {
			TableViewModel viewModel = table.getViewModel();
			EditableTableModel applicationModel = (EditableTableModel) table.getApplicationModel();
			
			if (((EditableTableControl) table).getMode() != ModeModel.EDIT_MODE) {
				throw new IllegalStateException("Control not in edit mode.");
			}
			
			// Find the row to move.
			int theRowID = TableUtil.getSingleSelectedRow(table.getTableData());
			int theMax = viewModel.getRowCount();

			// Execute the move.
			if(theRowID < theMax) {
				applicationModel.moveRowDown(viewModel.getApplicationModelRow(theRowID));
				theRowID = ((theRowID + 1) < theMax) ? (theRowID + 1) : theRowID;
				TableUtil.selectRow(table.getTableData(), theRowID);
			}
            return HandlerResult.DEFAULT_RESULT;
		}
		
        @Override
		public ResKey getI18NKey() {
            return I18NConstants.MOVE_ROW_DOWN;
        }

	}
	
	/**
	 * Moves the selected row to the end of the table. 
	 */
	public static class MoveRowToBottomAction extends TableCommand {
		
		/** 
		 * Singleton constructor. 
		 */
		protected MoveRowToBottomAction(String id) {
			super(id);
		}
		
		@Override
		protected HandlerResult execute(DisplayContext context, TableControl table, Map<String, Object> arguments) {
			TableViewModel viewModel = table.getViewModel();
			EditableTableModel applicationModel = (EditableTableModel) table.getApplicationModel();
			
			if (((EditableTableControl) table).getMode() != ModeModel.EDIT_MODE) {
				throw new IllegalStateException("Control not in edit mode.");
			}
			
			// Find the row to move.
			int theRowID = TableUtil.getSingleSelectedRow(table.getTableData());
			
			// Execute the move.
			applicationModel.moveRowToBottom(viewModel.getApplicationModelRow(theRowID));
			TableUtil.selectRow(table.getTableData(), viewModel.getRowCount() - 1);
			return HandlerResult.DEFAULT_RESULT;
		}
		
		@Override
		public ResKey getI18NKey() {
			return I18NConstants.MOVE_ROW_TO_BOTTOM;
		}
		
	}

	public static class ChooseAction extends TableCommand {

		public ChooseAction(String id) {
			super(id);
		}

		@Override
		protected HandlerResult execute(DisplayContext context, TableControl table, Map<String, Object> arguments) {
			TableModel theApplicationModel = table.getViewModel().getApplicationModel();
		    if (theApplicationModel instanceof WrappedModel) {
		    	theApplicationModel = (TableModel) ((WrappedModel) theApplicationModel).getWrappedModel();
		    }
			 
		    SelectField field = ((SelectionTableModel) theApplicationModel).getField();
            
            LayoutComponent formComponent = FormComponent.componentForMember(field);

			return SelectionControl.openSelectorPopup(context, context.getWindowScope(), field);
		}
		
		@Override
		public ResKey getI18NKey() {
			return I18NConstants.OPEN_CHOOSER;
		}
		
	}
	
	class SelectionListener implements com.top_logic.layout.component.model.SelectionListener {

		@Override
		public void notifySelectionChanged(SelectionModel model, Set<?> oldSelection, Set<?> newSelection) {
			TableViewModel theViewModel = getViewModel();
			int rowCount = theViewModel.getRowCount();
			int selected = TableUtil.getSingleSelectedRow(getModel());

			boolean noneSelected = selected < 0;

			boolean firstSelected = selected == 0;
			boolean lastSelected = selected == rowCount - 1;
			boolean tableDisabled = getMode() == ModeModel.DISABLED_MODE;

			if (delButton != null) {
				if (tableDisabled) {
					disableTableDisabled(delButton);
				} else if (noneSelected) {
					disableNoSelection(delButton);
				} else {
					boolean canRemove = true;
					TableModel theApplicationModel = getApplicationModel();
					if (theApplicationModel instanceof EditableTableModel) {
						canRemove = ((EditableTableModel) theApplicationModel)
							.canRemove(theViewModel.getApplicationModelRow(selected));
					}
					if (!canRemove) {
						delButton.disable(I18NConstants.REMOVE_ROW_DISABLED);
					} else {
						delButton.enable();
					}
				}
			}

			updateUpButton(upButton, noneSelected, firstSelected, I18NConstants.MOVE_ROW_UP_DISABLED, tableDisabled);
			updateUpButton(topButton, noneSelected, firstSelected, I18NConstants.MOVE_ROW_TO_TOP_DISABLED,
				tableDisabled);
			updateDownButton(downButton, noneSelected, lastSelected, I18NConstants.MOVE_ROW_DOWN_DISABLED,
				tableDisabled);
			updateDownButton(bottomButton, noneSelected, lastSelected, I18NConstants.MOVE_ROW_TO_BOTTOM_DISABLED,
				tableDisabled);
		}

		private void updateDownButton(ButtonControl aDownButton, boolean noneSelected, boolean lastSelected,
				ResKey lastSelectedReason, boolean tableDisabled) {
			if (aDownButton == null) {
				return;
			}
			if (tableDisabled) {
				disableTableDisabled(aDownButton);
			} else if (noneSelected) {
				disableNoSelection(aDownButton);
			} else if (lastSelected) {
				disableFirstSelected(aDownButton, lastSelectedReason);
			} else {
				aDownButton.enable();
			}
		}

		private void updateUpButton(ButtonControl anUpButton, boolean noneSelected, boolean firstSelected,
				ResKey firstSelectedReason, boolean tableDisabled) {
			if (anUpButton == null) {
				return;
			}
			if (tableDisabled) {
				disableTableDisabled(anUpButton);
			} else if (noneSelected) {
				disableNoSelection(anUpButton);
			} else if (firstSelected) {
				disableFirstSelected(anUpButton, firstSelectedReason);
			} else {
				anUpButton.enable();
			}
		}

		private void disableTableDisabled(ButtonControl button) {
			button.disable(I18NConstants.TABLE_DISABLED);
		}

		private void disableFirstSelected(ButtonControl button, ResKey firstSelectedReason) {
			button.disable(firstSelectedReason);
		}

		private void disableNoSelection(ButtonControl button) {
			button.disable(I18NConstants.NO_ROW_SELECTED);
		}
	}
	
}
