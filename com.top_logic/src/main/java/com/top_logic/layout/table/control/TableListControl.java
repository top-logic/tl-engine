/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.control;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.AttachedPropertyListener;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.component.model.MultiSelectionEvent;
import com.top_logic.layout.component.model.SelectionEvent;
import com.top_logic.layout.component.model.SelectionListener;
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
import com.top_logic.layout.table.model.TableModelEvent;
import com.top_logic.layout.table.model.TableModelListener;
import com.top_logic.layout.table.model.TableUtil;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Displays an editable list as table specified by a {@link TableData} with an
 * {@link EditableRowTableModel}.
 *
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 */
public class TableListControl extends TableControl {

	/**
	 * Provider for views to display in the toolbar of the {@link TableListControl}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@FunctionalInterface
	public static interface ToolbarFragment {

		/**
		 * Creates the views for the given {@link TableListControl}.
		 * 
		 * @return The views to display in the given order in the toolbar of the given table.
		 */
		List<? extends HTMLFragment> createFragment(TableListControl table);

	}

    // Commands:
    static final TableCommand MOVE_ROW_UP_COMMAND        = new MoveRowUpCommand();
    static final TableCommand MOVE_ROW_DOWN_COMMAND      = new MoveRowDownCommand();
    static final TableCommand MOVE_ROW_TO_TOP_COMMAND    = new MoveRowToTopCommand();
    static final TableCommand MOVE_ROW_TO_BOTTOM_COMMAND = new MoveRowToBottomCommand();

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
	public TableListControl(TableData tableData, ITableRenderer tableRenderer, boolean isSortable,
			List<? extends ToolbarFragment> toolbarFragments) {
		super(tableData, TABLE_COMMANDS, tableRenderer);
		this.sortable = isSortable;
		List<? extends HTMLFragment> fragments = toolbarFragments
			.stream()
			.map(builder -> builder.createFragment(this))
			.flatMap(List::stream)
			.toList();

		addTitleBarControls(0, fragments);
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
		List<ToolbarFragment> buttons;
		if (createButtons) {
			buttons = new ArrayList<>();
			if (rowObjectCreator != null) {
				buttons.add(new AddRowFragment(rowObjectCreator));
			}
			if (!isSortable) {
				// If table is automatically sortable, the move buttons will be disabled
				buttons.add(new MoveRowsFragment());
			}
			if (rowObjectCreator != null) {
				buttons.add(new RemoveRowFragment(rowObjectRemover));
			}
		} else {
			buttons = Collections.emptyList();
		}
		return new TableListControl(tableData, tableRenderer, isSortable, buttons);
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
	 * {@link ToolbarFragment} whose button reacts on the change the tables selection.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public abstract static class SelectionObservingFragment<C extends AbstractControlBase> implements ToolbarFragment {

		@Override
		public List<C> createFragment(TableListControl table) {
			C button = createButton(table);
			SelectionListener modelUpdater = new SelectionListener() {

				@Override
				public void notifySelectionChanged(SelectionModel model, SelectionEvent event) {
					updateButton(button, table);
				}
			};
			button.addListener(AbstractControlBase.ATTACHED_PROPERTY, new AttachedPropertyListener() {

				@Override
				public void handleAttachEvent(AbstractControlBase sender, Boolean oldValue, Boolean newValue) {
					SelectionModel selectionModel = table.getSelectionModel();
					if (newValue) {
						selectionModel.addSelectionListener(modelUpdater);
						updateButton(button, table);
					} else {
						selectionModel.removeSelectionListener(modelUpdater);
					}
				}

			});
			return Collections.singletonList(button);
		}

		/**
		 * Creates the button to display in toolbar.
		 * 
		 * @param table
		 *        The table in which the button is displayed.
		 */
		protected abstract C createButton(TableListControl table);

		/**
		 * Updates the button when the selection changes.
		 * 
		 * @param button
		 *        The button to update when selection changes.
		 * @param table
		 *        The table in which the button is displayed.
		 */
		protected abstract void updateButton(C button, TableListControl table);

	}

	/**
	 * {@link ToolbarFragment} creating a button to add a new row.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class AddRowFragment extends SelectionObservingFragment<ButtonControl> {

		private RowObjectCreator _creator;

		/**
		 * Creates a new {@link AddRowFragment}.
		 */
		public AddRowFragment(RowObjectCreator creator) {
			_creator = creator;
		}

		@Override
		protected ButtonControl createButton(TableListControl table) {
			CommandModel addRowCommandModel = newTableCommandModel(table, new AddRowCommand(_creator));
			addRowCommandModel.setImage(Icons.ADD_ROW);
			addRowCommandModel.setNotExecutableImage(Icons.ADD_ROW_DISABLED);
			return new ButtonControl(addRowCommandModel);
		}

		@Override
		protected void updateButton(ButtonControl button, TableListControl table) {
			TableData tableData = table.getTableData();
			int selected = TableUtil.getSingleSelectedRow(tableData);
			ResKey disabledReason = _creator.allowCreateNewRow(selected, tableData, table);
			if (disabledReason == null) {
				button.enable();
			} else {
				button.disable(disabledReason);
			}
		}

	}

	/**
	 * Command to add a new row to the list.
	 * 
	 * @author <a href=mailto:CBR@top-logic.com>CBR</a>
	 */
    public static class AddRowCommand extends TableCommand {

		private final static String COMMAND_ID = "AddRowCommand";

		private final RowObjectCreator _creator;

        /**
         * Creates a {@link AddRowCommand}.
         */
		public AddRowCommand(RowObjectCreator creator) {
			super(COMMAND_ID);
			_creator = creator;
        }

        @Override
		protected HandlerResult execute(DisplayContext aContext, TableControl tableControl,
				Map<String, Object> aArguments) {
			TableListControl listControl = (TableListControl) tableControl;

			Object newRow = _creator.createNewRow(tableControl);
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
	 * {@link ToolbarFragment} to display buttons to move rows.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class MoveRowsFragment implements ToolbarFragment {

		@Override
		public List<? extends HTMLFragment> createFragment(TableListControl table) {
			ButtonControl rowDown = createRowDown(table);
			ButtonControl rowBottom = createRowToBottom(table);
			ButtonControl rowUp = createRowUp(table);
			ButtonControl rowTop = createRowToTop(table);
			
			class ModelUpdater implements TableModelListener, SelectionListener {

				@Override
				public void notifySelectionChanged(SelectionModel model, SelectionEvent event) {
					updateButtons(table, rowTop, rowUp, rowDown, rowBottom);
				}

				@Override
				public void handleTableModelEvent(TableModelEvent event) {
					updateButtons(table, rowTop, rowUp, rowDown, rowBottom);
				}
				
			}
			
			AttachedPropertyListener attachedListener = new AttachedPropertyListener() {
				
				ModelUpdater _updater = new ModelUpdater();

				@Override
				public void handleAttachEvent(AbstractControlBase sender, Boolean oldValue, Boolean newValue) {
					TableModel tableModel = table.getApplicationModel();
					SelectionModel selectionModel = table.getSelectionModel();
					if (newValue) {
						tableModel.addTableModelListener(_updater);
						selectionModel.addSelectionListener(_updater);
						updateButtons(table, rowTop, rowUp, rowDown, rowBottom);
					} else {
						selectionModel.removeSelectionListener(_updater);
						tableModel.removeTableModelListener(_updater);
					}
				}

			};
			
			/* It doesn't matter on which control the listener is attached because always all or no
			 * button is displayed. */
			rowTop.addListener(AbstractControlBase.ATTACHED_PROPERTY, attachedListener);
			
			return Arrays.asList(rowTop, rowUp,rowDown,rowBottom);
		}
		
		/**
		 * Creates the button to move a row down.
		 */
		protected ButtonControl createRowDown(TableListControl table) {
			CommandModel moveRowDownCommandModel = newTableCommandModel(table, MOVE_ROW_DOWN_COMMAND);
			moveRowDownCommandModel.setImage(Icons.MOVE_ROW_DOWN);
			moveRowDownCommandModel.setNotExecutableImage(Icons.MOVE_ROW_DOWN_DISABLED);
			return new ButtonControl(moveRowDownCommandModel, ImageButtonRenderer.NO_REASON_INSTANCE);
		}

		/**
		 * Creates the button to move a row to the bottom.
		 */
		protected ButtonControl createRowToBottom(TableListControl table) {
			CommandModel moveRowToBottomCommandModel = newTableCommandModel(table, MOVE_ROW_TO_BOTTOM_COMMAND);
			moveRowToBottomCommandModel.setImage(Icons.MOVE_ROW_TO_BOTTOM);
			moveRowToBottomCommandModel.setNotExecutableImage(Icons.MOVE_ROW_TO_BOTTOM_DISABLED);
			return new ButtonControl(moveRowToBottomCommandModel, ImageButtonRenderer.NO_REASON_INSTANCE);
		}
	
		/**
		 * Creates the button to move a row to the top.
		 */
		protected ButtonControl createRowToTop(TableListControl table) {
			CommandModel moveRowToTopCommandModel = newTableCommandModel(table, MOVE_ROW_TO_TOP_COMMAND);
			moveRowToTopCommandModel.setImage(Icons.MOVE_ROW_TO_TOP);
			moveRowToTopCommandModel.setNotExecutableImage(Icons.MOVE_ROW_TO_TOP_DISABLED);
			return new ButtonControl(moveRowToTopCommandModel, ImageButtonRenderer.NO_REASON_INSTANCE);
		}
		
		/**
		 * Creates the button to move a row up.
		 */
		protected ButtonControl createRowUp(TableListControl table) {
			CommandModel moveRowUpCommandModel = newTableCommandModel(table, MOVE_ROW_UP_COMMAND);
			moveRowUpCommandModel.setImage(Icons.MOVE_ROW_UP);
			moveRowUpCommandModel.setNotExecutableImage(Icons.MOVE_ROW_UP_DISABLED);
			return new ButtonControl(moveRowUpCommandModel, ImageButtonRenderer.NO_REASON_INSTANCE);
		}

		/**
		 * Updates the buttons due to change of table structure or selection.
		 */
		protected void updateButtons(TableListControl table,
				ButtonControl top,
				ButtonControl up,
				ButtonControl down,
				ButtonControl bottom) {
			TableData tableData = table.getTableData();
			int selected = TableUtil.getSingleSelectedRow(tableData);
			int size = table.getViewModel().getRowCount();

			updateUpButton(top, selected, I18NConstants.MOVE_ROW_TO_TOP_DISABLED);
			updateUpButton(up, selected, I18NConstants.MOVE_ROW_UP_DISABLED);
			updateDownButton(down, size, selected, I18NConstants.MOVE_ROW_DOWN_DISABLED);
			updateDownButton(bottom, size, selected, I18NConstants.MOVE_ROW_TO_BOTTOM_DISABLED);
		}

		private void updateUpButton(ButtonControl upButton, int selected, ResKey firstRowReason) {
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

		private void updateDownButton(ButtonControl downButton, int size, int selected, ResKey lastRowReason) {
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

	}

	/**
	 * Command to move a row in the list.
	 */
	public static abstract class MoveRowCommand extends TableCommand {

		/**
		 * Creates a new {@link MoveRowCommand}.
		 */
		public MoveRowCommand(String aCommand) {
			super(aCommand);
		}

		/**
		 * Triggers a selection changed notification, to use when a row was moved.
		 * 
		 * This is necessary because when moving a row the selection index changes but not the
		 * selected object. Thus no notify is triggered when moving a row. Therefore it has to be
		 * triggered manually.
		 */
		protected void notifyRowMoved(TableControl table) {
			SelectionModel selectionModel = table.getSelectionModel();
			table.notifySelectionChanged(selectionModel,
				new MultiSelectionEvent(selectionModel, Collections.emptySet(), selectionModel.getSelection()));
		}

	}
	
	/**
	 * Command to move a row up in the list.
	 *
	 * @author <a href=mailto:CBR@top-logic.com>CBR</a>
	 */
	public static class MoveRowUpCommand extends MoveRowCommand {

		private final static String COMMAND_ID = "MoveRowUpCommand";

        /**
         * Creates a {@link MoveRowUpCommand}.
         */
        public MoveRowUpCommand() {
			super(COMMAND_ID);
        }

        @Override
		protected HandlerResult execute(DisplayContext aContext, TableControl aTable, Map<String, Object> aArguments) {
			EditableRowTableModel theTableModel = (EditableRowTableModel) aTable.getApplicationModel();
			int theSelectedRow = aTable.getSelectedRow();
			if (theSelectedRow < 1)
				return HandlerResult.DEFAULT_RESULT;
			theTableModel.moveRowUp(theSelectedRow);
			aTable.setSelectedRow(theSelectedRow - 1);
			notifyRowMoved(aTable);
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
	public static class MoveRowDownCommand extends MoveRowCommand {

		private final static String COMMAND_ID = "MoveRowDownCommand";

        /**
         * Creates a {@link MoveRowDownCommand}.
         */
        public MoveRowDownCommand() {
			super(COMMAND_ID);
        }

        @Override
		protected HandlerResult execute(DisplayContext aContext, TableControl aTable, Map<String, Object> aArguments) {
			EditableRowTableModel theTableModel = (EditableRowTableModel) aTable.getApplicationModel();
			int theSelectedRow = aTable.getSelectedRow();
			if (theSelectedRow < 0 || theSelectedRow > theTableModel.getRowCount() - 2)
				return HandlerResult.DEFAULT_RESULT;
			theTableModel.moveRowDown(theSelectedRow);
			aTable.setSelectedRow(theSelectedRow + 1);
			notifyRowMoved(aTable);
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
	public static class MoveRowToTopCommand extends MoveRowCommand {

		private final static String COMMAND_ID = "MoveRowToTopCommand";

        /**
         * Creates a {@link MoveRowToTopCommand}.
         */
        public MoveRowToTopCommand() {
			super(COMMAND_ID);
        }

        @Override
		protected HandlerResult execute(DisplayContext aContext, TableControl aTable, Map<String, Object> aArguments) {
			EditableRowTableModel theTableModel = (EditableRowTableModel) aTable.getApplicationModel();
			int theSelectedRow = aTable.getSelectedRow();
			if (theSelectedRow < 1)
				return HandlerResult.DEFAULT_RESULT;
			theTableModel.moveRowToTop(theSelectedRow);
			aTable.setSelectedRow(0);
			notifyRowMoved(aTable);
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
	public static class MoveRowToBottomCommand extends MoveRowCommand {

		private final static String COMMAND_ID = "MoveRowToBottomCommand";

        /**
         * Creates a {@link MoveRowToBottomCommand}.
         */
		public MoveRowToBottomCommand() {
			super(COMMAND_ID);
        }

        @Override
		protected HandlerResult execute(DisplayContext aContext, TableControl aTable, Map<String, Object> aArguments) {
            EditableRowTableModel theTableModel = (EditableRowTableModel)aTable.getApplicationModel();
            int theSelectedRow = aTable.getSelectedRow();
            if (theSelectedRow < 0 || theSelectedRow > theTableModel.getRowCount() - 2) return HandlerResult.DEFAULT_RESULT;
            theTableModel.moveRowToBottom(theSelectedRow);
            aTable.setSelectedRow(theTableModel.getRowCount() - 1);
			notifyRowMoved(aTable);
            return HandlerResult.DEFAULT_RESULT;
        }

        @Override
		public ResKey getI18NKey() {
            return I18NConstants.MOVE_ROW_TO_BOTTOM;
        }

    }

	/**
	 * {@link ToolbarFragment} creating a button to remove the selected row.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class RemoveRowFragment extends SelectionObservingFragment<ButtonControl> {

		private RowObjectRemover _remover;

		/**
		 * Creates a new {@link RemoveRowFragment}.
		 */
		public RemoveRowFragment(RowObjectRemover remover) {
			_remover = remover;
		}

		@Override
		protected ButtonControl createButton(TableListControl table) {
			CommandModel removeRowCommandModel = newTableCommandModel(table, new RemoveRowCommand(_remover));
			removeRowCommandModel.setImage(Icons.DELETE_TOOLBAR);
			removeRowCommandModel.setNotExecutableImage(Icons.DELETE_TOOLBAR_DISABLED);
			return new ButtonControl(removeRowCommandModel);
		}

		@Override
		protected void updateButton(ButtonControl button, TableListControl table) {
			TableData tableData = table.getTableData();
			int selected = TableUtil.getSingleSelectedRow(tableData);
			boolean nothingSelected = selected < 0;
			if (nothingSelected) {
				button.disable(I18NConstants.NO_ROW_SELECTED);
				return;
			}
			ResKey disabledReason = _remover.allowRemoveRow(selected, tableData, table);
			if (disabledReason == null) {
				button.enable();
			} else {
				button.disable(disabledReason);
			}
		}

	}

    /**
     * Command to remove a row from the list.
     *
     * @author <a href=mailto:CBR@top-logic.com>CBR</a>
     */
    public static class RemoveRowCommand extends TableCommand {

		private final static String COMMAND_ID = "RemoveRowCommand";

		private RowObjectRemover _remover;

        /**
         * Creates a {@link RemoveRowCommand}.
         */
		public RemoveRowCommand(RowObjectRemover remover) {
			super(COMMAND_ID);
			_remover = remover;
        }

        @Override
		protected HandlerResult execute(DisplayContext aContext, TableControl aTable, Map<String, Object> aArguments) {
            EditableRowTableModel theTableModel = (EditableRowTableModel)aTable.getApplicationModel();
			if (_remover == null)
				return HandlerResult.DEFAULT_RESULT;
            int theSelectedRow = aTable.getSelectedRow();
            if (theSelectedRow < 0) return HandlerResult.DEFAULT_RESULT;
			int theApplicationRow = aTable.getViewModel().getApplicationModelRow(theSelectedRow);
            Object theRowObject = theTableModel.getRowObject(theApplicationRow);
            theTableModel.removeRow(theApplicationRow);
			int theRowCount = aTable.getViewModel().getRowCount();
            if (theRowCount > 0) {
                aTable.setSelectedRow(theSelectedRow >= theRowCount ? theRowCount - 1 : theSelectedRow);
            }
			_remover.removeRow(theRowObject, aTable);
            return HandlerResult.DEFAULT_RESULT;

        }

        @Override
		public ResKey getI18NKey() {
            return I18NConstants.REMOVE_ROW;
        }

    }
}
