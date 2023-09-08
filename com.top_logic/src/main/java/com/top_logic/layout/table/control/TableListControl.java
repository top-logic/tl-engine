/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.control;

import java.util.Map;
import java.util.Set;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.control.ImageButtonRenderer;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.table.I18NConstants;
import com.top_logic.layout.table.ITableRenderer;
import com.top_logic.layout.table.RowObjectCreator;
import com.top_logic.layout.table.RowObjectRemover;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.model.EditableRowTableModel;
import com.top_logic.layout.table.model.TableUtil;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Displays an editable list as table specified by a TableField with an ObjectTableModel.
 *
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 */
public class TableListControl extends TableControl {

    // Commands:
    static final TableCommand ADD_ROW_COMMAND            = new AddRowCommand();
    static final TableCommand MOVE_ROW_UP_COMMAND        = new MoveRowUpCommand();
    static final TableCommand MOVE_ROW_DOWN_COMMAND      = new MoveRowDownCommand();
    static final TableCommand MOVE_ROW_TO_TOP_COMMAND    = new MoveRowToTopCommand();
    static final TableCommand MOVE_ROW_TO_BOTTOM_COMMAND = new MoveRowToBottomCommand();
    static final TableCommand REMOVE_ROW_COMMAND         = new RemoveRowCommand();

    static final Map<String, ControlCommand> COMMANDS =
    	createCommandMap(TABLE_COMMANDS, 
	    	new TableCommand[] {
		    	ADD_ROW_COMMAND, 
		    	MOVE_ROW_UP_COMMAND, 
		    	MOVE_ROW_DOWN_COMMAND, 
		    	MOVE_ROW_TO_TOP_COMMAND, 
		    	MOVE_ROW_TO_BOTTOM_COMMAND, 
		    	REMOVE_ROW_COMMAND
		    });



	private final RowObjectCreator rowObjectCreator;
    private final RowObjectRemover rowObjectRemover;

	private ButtonControl theMoveRowUpButton, theMoveRowDownButton, theMoveRowToTopButton, theMoveRowToBottomButton,
			theRemoveRowButton, theAddRowButton;

    /** Flag indicating that new rows should always be appended at the end of the list. */
    private boolean appendAlways = false;

    /**
     * If <code>true</code>, the table will be automatically sortable and the move
     * buttons will be disabled.
     */
    private boolean sortable = false;

    /**
     * Creates a {@link TableListControl}.
     */
	protected TableListControl(TableData tableData, ITableRenderer tableRenderer, RowObjectCreator rowObjectCreator,
			RowObjectRemover rowObjectRemover, boolean isSortable) {
		super(tableData, COMMANDS, tableRenderer);
		this.rowObjectCreator = rowObjectCreator;
		this.rowObjectRemover = rowObjectRemover;
		this.sortable = isSortable;
    }

	/**
	 * Factory method to create a new {@link TableListControl} from a {@link TableField}.
	 */
	public static TableListControl createTableListControl(TableField tableField, ITableRenderer tableRenderer,
			RowObjectCreator rowObjectCreator, RowObjectRemover rowObjectRemover, boolean isSortable) {
		final boolean createButtons = !tableField.isImmutable();
		final boolean selectable = tableField.isSelectable();
		return createTableListControl(tableField, tableRenderer, rowObjectCreator, rowObjectRemover, selectable,
			createButtons, isSortable);
	}

	/**
	 * Factory method to create a new {@link TableListControl}
	 * 
	 * @param isSelectable
	 *        Indicates whether this table shall be selectable or not
	 */
	public static TableListControl createTableListControl(TableData tableData, ITableRenderer tableRenderer,
			RowObjectCreator rowObjectCreator, RowObjectRemover rowObjectRemover, boolean isSelectable,
			boolean createButtons, boolean isSortable) {
		final TableListControl tableListControl =
			createTableListControl(tableData, tableRenderer, rowObjectCreator, rowObjectRemover, createButtons,
				isSortable);
		tableListControl.setSelectable(isSelectable);
		return tableListControl;
	}

	/**
	 * Factory method to create a new {@link TableListControl}
	 * @param createButtons
	 *        Indicates whether buttons should be created or not. Buttons should not be created if
	 *        the table is not in EditMode (is immutable).
	 */
	public static TableListControl createTableListControl(TableData tableData, ITableRenderer tableRenderer,
			RowObjectCreator rowObjectCreator, RowObjectRemover rowObjectRemover, boolean createButtons,
			boolean isSortable) {
		final TableListControl tableListControl =
			createTableListControl(tableData, tableRenderer, rowObjectCreator, rowObjectRemover, isSortable);
		if (createButtons) {
			tableListControl.createButtons();
		}
		return tableListControl;
	}

	/**
	 * Factory method to create a new {@link TableListControl}
	 * @param rowObjectCreator
	 *        the RowObjectCreator to create new rows. If it is <code>null</code>, the add command
	 *        of this control will be disabled.
	 * @param rowObjectRemover
	 *        the RowObjectRemover to make cleanup work after removing a row. If it is
	 *        <code>null</code>, the remove command of this control will be disabled.
	 * @param isSortable
	 *        If <code>true</code>, the table will be automatically sortable and the move buttons
	 *        will be disabled
	 */
	public static TableListControl createTableListControl(TableData tableData, ITableRenderer tableRenderer,
			RowObjectCreator rowObjectCreator, RowObjectRemover rowObjectRemover, boolean isSortable) {
		return new TableListControl(tableData, tableRenderer, rowObjectCreator, rowObjectRemover, isSortable);
	}

    private void createButtons() {
        if (!sortable && isSelectable()) {
            // If table is automatically sortable, the move buttons will be disabled
			CommandModel theMoveRowToTopCommandModel = newTableCommandModel(MOVE_ROW_TO_TOP_COMMAND);
        	theMoveRowToTopCommandModel.setImage(Icons.MOVE_ROW_TO_TOP);
        	theMoveRowToTopCommandModel.setNotExecutableImage(Icons.MOVE_ROW_TO_TOP_DISABLED);
            theMoveRowToTopButton = new ButtonControl(theMoveRowToTopCommandModel, ImageButtonRenderer.NO_REASON_INSTANCE);
            
			CommandModel theMoveRowUpCommandModel = newTableCommandModel(MOVE_ROW_UP_COMMAND);
            theMoveRowUpCommandModel.setImage(Icons.MOVE_ROW_UP);
            theMoveRowUpCommandModel.setNotExecutableImage(Icons.MOVE_ROW_UP_DISABLED);
            theMoveRowUpButton = new ButtonControl(theMoveRowUpCommandModel, ImageButtonRenderer.NO_REASON_INSTANCE);

			CommandModel theMoveRowDownCommandModel = newTableCommandModel(MOVE_ROW_DOWN_COMMAND);
            theMoveRowDownCommandModel.setImage(Icons.MOVE_ROW_DOWN);
            theMoveRowDownCommandModel.setNotExecutableImage(Icons.MOVE_ROW_DOWN_DISABLED);
            theMoveRowDownButton = new ButtonControl(theMoveRowDownCommandModel, ImageButtonRenderer.NO_REASON_INSTANCE);
            
			CommandModel theMoveRowToBottomCommandModel = newTableCommandModel(MOVE_ROW_TO_BOTTOM_COMMAND);
            theMoveRowToBottomCommandModel.setImage(Icons.MOVE_ROW_TO_BOTTOM);
            theMoveRowToBottomCommandModel.setNotExecutableImage(Icons.MOVE_ROW_TO_BOTTOM_DISABLED);
            theMoveRowToBottomButton = new ButtonControl(theMoveRowToBottomCommandModel, ImageButtonRenderer.NO_REASON_INSTANCE);
            
            addTitleBarControl(theMoveRowToTopButton);
            addTitleBarControl(theMoveRowUpButton);
            addTitleBarControl(theMoveRowDownButton);
            addTitleBarControl(theMoveRowToBottomButton);
        }
		if (getRowObjectRemover() != null && isSelectable()) {
			CommandModel theRemoveRowCommandModel = newTableCommandModel(REMOVE_ROW_COMMAND);
            theRemoveRowCommandModel.setImage(Icons.DELETE_TOOLBAR);
            theRemoveRowCommandModel.setNotExecutableImage(Icons.DELETE_TOOLBAR_DISABLED);
			theRemoveRowButton = new ButtonControl(theRemoveRowCommandModel);
            addTitleBarControl(theRemoveRowButton);
        }
		if (getRowObjectCreator() != null) {
			CommandModel theAddRowCommandModel = newTableCommandModel(ADD_ROW_COMMAND);
            theAddRowCommandModel.setImage(Icons.ADD_ROW);
            theAddRowCommandModel.setNotExecutableImage(Icons.ADD_ROW_DISABLED);
			theAddRowButton = new ButtonControl(theAddRowCommandModel);
            addTitleBarControl(0, theAddRowButton);
        }
    }

	/**
	 * Callback called to actually create an row object
	 */
    public RowObjectCreator getRowObjectCreator() {
        return rowObjectCreator;
    }

	/**
	 * Callback called to actually remove an row object
	 */
    public RowObjectRemover getRowObjectRemover() {
        return rowObjectRemover;
    }

	/**
	 * If <code>true</code> then new rows are always append to the end of the list
	 */
    public boolean getAppendAlways() {
        return appendAlways;
    }

	/**
	 * @param appendAlways
	 *        the new value of {@link #getAppendAlways()}
	 * 
	 * @see #getAppendAlways()
	 */
    public void setAppendAlways(boolean appendAlways) {
        this.appendAlways = appendAlways;
    }

	@Override
	protected void internalAttach() {
		super.internalAttach();
		if (!sortable)
			disableSorting();
		updateButtons();
	}

    /**
     * Disables the sorting function.
     */
    private void disableSorting() {
		TableViewModel viewModel = getViewModel();
		int cnt = getApplicationModel().getColumnCount();
        for (int i = 0; i < cnt; i++) {
			viewModel.setColumnComparator(i, null);
        }
    }

	@Override
	public void notifySelectionChanged(SelectionModel model, Set<?> formerlySelectedObjects, Set<?> selectedObjects) {
		super.notifySelectionChanged(model, formerlySelectedObjects, selectedObjects);
		updateButtons();
	}

	/**
	 * Updates the buttons due to the corresponding selection
	 */
	private void updateButtons() {
		int selected = TableUtil.getSingleSelectedRow(getTableData());
		int size = getViewModel().getRowCount();
		updateUpButton(theMoveRowUpButton, selected, I18NConstants.MOVE_ROW_UP_DISABLED);
		updateUpButton(theMoveRowToTopButton, selected, I18NConstants.MOVE_ROW_TO_TOP_DISABLED);
		updateDownButton(theMoveRowDownButton, selected, size, I18NConstants.MOVE_ROW_DOWN_DISABLED);
		updateDownButton(theMoveRowToBottomButton, selected, size, I18NConstants.MOVE_ROW_TO_BOTTOM_DISABLED);
		updateRemoveButton(selected);
		updateAddButton(selected);
	}

	private void updateAddButton(int selected) {
		if (theAddRowButton == null) {
			return;
		}
		RowObjectCreator theCreator = getRowObjectCreator();
		
		ResKey disabledReason = theCreator.allowCreateNewRow(selected, getTableData(), this);
		if (disabledReason == null) {
			theAddRowButton.enable();
		} else {
			theAddRowButton.disable(disabledReason);
		}
	}

	private void updateRemoveButton(int selected) {
		if (theRemoveRowButton == null) {
			return;
		}
		boolean nothingSelected = selected < 0;
		if (nothingSelected) {
			theRemoveRowButton.disable(I18NConstants.NO_ROW_SELECTED);
			return;
		}
		RowObjectRemover theRemover = getRowObjectRemover();
		ResKey disabledReason = theRemover.allowRemoveRow(selected, getTableData(), this);
		if (disabledReason == null) {
			theRemoveRowButton.enable();
		} else {
			theRemoveRowButton.disable(disabledReason);
		}
	}

	private void updateDownButton(ButtonControl downButton, int selected, int size, ResKey lastRowReason) {
		if (downButton == null) {
			return;
		}
		boolean nothingSelected = selected < 0;
		if (nothingSelected) {
			downButton.disable(I18NConstants.NO_ROW_SELECTED);
			return;
		}
		boolean lastRowSelected = selected > size - 2;
		if (lastRowSelected) {
			downButton.disable(lastRowReason);
			return;
		}
		downButton.enable();
	}

	private void updateUpButton(ButtonControl upButton, int selected, ResKey firstRowReason) {
		if (upButton == null) {
			return;
		}
		boolean nothingSelected = selected < 0;
		if (nothingSelected) {
			upButton.disable(I18NConstants.NO_ROW_SELECTED);
			return;
		}
		boolean firstRowSelected = selected < 1;
		if (firstRowSelected) {
			upButton.disable(firstRowReason);
			return;
		}
		upButton.enable();
	}

	/**
	 * Adds a new row to the {@link TableModel} of this control.
	 * 
	 * @param newRow
	 *        The new row object to insert.
	 */
	public void addNewRow(Object newRow) {
		EditableRowTableModel tableModel = (EditableRowTableModel) getApplicationModel();
		if (isSelectable()) {
			int selectedRow = getSelectedRow();
			if (getAppendAlways() || selectedRow < 0 || selectedRow > tableModel.getRowCount() - 2) {
				tableModel.addRowObject(newRow);
				setSelectedRow(tableModel.getRowCount() - 1);
			} else {
				int applicationRow = getViewModel().getApplicationModelRow(selectedRow);
				tableModel.insertRowObject(applicationRow + 1, newRow);
				setSelectedRow(getViewModel().getViewModelRow((applicationRow + 1)));
	        }
		} else {
			tableModel.addRowObject(newRow);
	    }
	}

	/**
	 * Command to add a new row to the list.
	 * 
	 * @author <a href=mailto:CBR@top-logic.com>CBR</a>
	 */
    public static class AddRowCommand extends TableCommand {

		private final static String COMMAND = "AddRowCommand";

        /**
         * Creates a {@link AddRowCommand}.
         */
        public AddRowCommand() {
            super(COMMAND);
        }

        @Override
		protected HandlerResult execute(DisplayContext aContext, TableControl tableControl,
				Map<String, Object> aArguments) {
			TableListControl listControl = (TableListControl) tableControl;
			RowObjectCreator creator = listControl.getRowObjectCreator();
			if (creator == null) {
				return HandlerResult.DEFAULT_RESULT;
			}

			Object newRow = creator.createNewRow(tableControl);
			if (newRow != null) {
				listControl.addNewRow(newRow);
			}
			return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		public ResKey getI18NKey() {
            return I18NConstants.ADD_ROW;
        }

    }


    /**
     * Command to move a row up in the list.
     *
     * @author <a href=mailto:CBR@top-logic.com>CBR</a>
     */
    public static class MoveRowUpCommand extends TableCommand {

		private final static String COMMAND = "MoveRowUpCommand";

        /**
         * Creates a {@link MoveRowUpCommand}.
         */
        public MoveRowUpCommand() {
            super(COMMAND);
        }

        @Override
		protected HandlerResult execute(DisplayContext aContext, TableControl aTable, Map<String, Object> aArguments) {
            EditableRowTableModel theTableModel = (EditableRowTableModel) aTable.getApplicationModel();
            int theSelectedRow = aTable.getSelectedRow();
            if (theSelectedRow < 1) return HandlerResult.DEFAULT_RESULT;
            theTableModel.moveRowUp(theSelectedRow);
            aTable.setSelectedRow(theSelectedRow - 1);
            return HandlerResult.DEFAULT_RESULT;
        }

        @Override
		public ResKey getI18NKey() {
            return I18NConstants.MOVE_ROW_UP;
        }

    }


    /**
     * Command to move a row down in the list.
     *
     * @author <a href=mailto:CBR@top-logic.com>CBR</a>
     */
    public static class MoveRowDownCommand extends TableCommand {

		private final static String COMMAND = "MoveRowDownCommand";

        /**
         * Creates a {@link MoveRowDownCommand}.
         */
        public MoveRowDownCommand() {
            super(COMMAND);
        }

        @Override
		protected HandlerResult execute(DisplayContext aContext, TableControl aTable, Map<String, Object> aArguments) {
            EditableRowTableModel theTableModel = (EditableRowTableModel)aTable.getApplicationModel();
            int theSelectedRow = aTable.getSelectedRow();
            if (theSelectedRow < 0 || theSelectedRow > theTableModel.getRowCount() - 2) return HandlerResult.DEFAULT_RESULT;
            theTableModel.moveRowDown(theSelectedRow);
            aTable.setSelectedRow(theSelectedRow + 1);
            return HandlerResult.DEFAULT_RESULT;
        }

        @Override
		public ResKey getI18NKey() {
            return I18NConstants.MOVE_ROW_DOWN;
        }

    }


    /**
     * Command to move a row to top in the list.
     *
     * @author <a href=mailto:CBR@top-logic.com>CBR</a>
     */
    public static class MoveRowToTopCommand extends TableCommand {

		private final static String COMMAND = "MoveRowToTopCommand";

        /**
         * Creates a {@link MoveRowToTopCommand}.
         */
        public MoveRowToTopCommand() {
            super(COMMAND);
        }

        @Override
		protected HandlerResult execute(DisplayContext aContext, TableControl aTable, Map<String, Object> aArguments) {
            EditableRowTableModel theTableModel = (EditableRowTableModel)aTable.getApplicationModel();
            int theSelectedRow = aTable.getSelectedRow();
            if (theSelectedRow < 1) return HandlerResult.DEFAULT_RESULT;
            theTableModel.moveRowToTop(theSelectedRow);
            aTable.setSelectedRow(0);
            return HandlerResult.DEFAULT_RESULT;
        }

        @Override
		public ResKey getI18NKey() {
            return I18NConstants.MOVE_ROW_TO_TOP;
        }

    }


    /**
     * Command to move a row to bottom in the list.
     *
     * @author <a href=mailto:CBR@top-logic.com>CBR</a>
     */
    public static class MoveRowToBottomCommand extends TableCommand {

		private final static String COMMAND = "MoveRowToBottomCommand";

        /**
         * Creates a {@link MoveRowToBottomCommand}.
         */
        public MoveRowToBottomCommand() {
            super(COMMAND);
        }

        @Override
		protected HandlerResult execute(DisplayContext aContext, TableControl aTable, Map<String, Object> aArguments) {
            EditableRowTableModel theTableModel = (EditableRowTableModel)aTable.getApplicationModel();
            int theSelectedRow = aTable.getSelectedRow();
            if (theSelectedRow < 0 || theSelectedRow > theTableModel.getRowCount() - 2) return HandlerResult.DEFAULT_RESULT;
            theTableModel.moveRowToBottom(theSelectedRow);
            aTable.setSelectedRow(theTableModel.getRowCount() - 1);
            return HandlerResult.DEFAULT_RESULT;
        }

        @Override
		public ResKey getI18NKey() {
            return I18NConstants.MOVE_ROW_TO_BOTTOM;
        }

    }


    /**
     * Command to remove a row from the list.
     *
     * @author <a href=mailto:CBR@top-logic.com>CBR</a>
     */
    public static class RemoveRowCommand extends TableCommand {

		private final static String COMMAND = "RemoveRowCommand";

        /**
         * Creates a {@link RemoveRowCommand}.
         */
        public RemoveRowCommand() {
            super(COMMAND);
        }

        @Override
		protected HandlerResult execute(DisplayContext aContext, TableControl aTable, Map<String, Object> aArguments) {
            EditableRowTableModel theTableModel = (EditableRowTableModel)aTable.getApplicationModel();
            RowObjectRemover theRowObjectRemover = ((TableListControl)aTable).getRowObjectRemover();
            if (theRowObjectRemover == null) return HandlerResult.DEFAULT_RESULT;
            int theSelectedRow = aTable.getSelectedRow();
            if (theSelectedRow < 0) return HandlerResult.DEFAULT_RESULT;
			int theApplicationRow = aTable.getViewModel().getApplicationModelRow(theSelectedRow);
            Object theRowObject = theTableModel.getRowObject(theApplicationRow);
            theTableModel.removeRow(theApplicationRow);
			int theRowCount = aTable.getViewModel().getRowCount();
            if (theRowCount > 0) {
                aTable.setSelectedRow(theSelectedRow >= theRowCount ? theRowCount - 1 : theSelectedRow);
            }
            theRowObjectRemover.removeRow(theRowObject, aTable);
            return HandlerResult.DEFAULT_RESULT;

        }

        @Override
		public ResKey getI18NKey() {
            return I18NConstants.REMOVE_ROW;
        }

    }
}
