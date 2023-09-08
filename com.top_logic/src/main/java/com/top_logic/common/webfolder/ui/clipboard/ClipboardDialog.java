/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.ui.clipboard;

import static com.top_logic.layout.DisplayDimension.*;
import static com.top_logic.layout.DisplayUnit.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.Named;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.gui.layout.clipboard.FindWebFolderVisitor;
import com.top_logic.knowledge.gui.layout.webfolder.ClearClipboardHandler;
import com.top_logic.knowledge.gui.layout.webfolder.WebFolderAware;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.Clipboard;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.knowledge.wrap.WrapperAccessor;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.IdentityAccessor;
import com.top_logic.layout.SimpleAccessor;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.ResourceText;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.control.IconControl;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.component.ComponentUtil;
import com.top_logic.layout.component.model.SelectionListener;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormHandler;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.messagebox.AbstractFormPageDialog;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.SimpleTableDialog;
import com.top_logic.layout.structure.DefaultDialogModel;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.filter.LabelFilterProvider;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ColumnCustomization;
import com.top_logic.layout.table.model.Enabled;
import com.top_logic.layout.table.model.FieldProvider;
import com.top_logic.layout.table.model.FormTableModel;
import com.top_logic.layout.table.model.Header;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableModelEvent;
import com.top_logic.layout.table.model.TableModelListener;
import com.top_logic.mig.html.DefaultMultiSelectionModel;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.SelectionModelOwner;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLNamed;
import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Utils;

/**
 * Clipboard dialog providing the {@link TableField} for adding objects to the folder.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class ClipboardDialog extends AbstractFormPageDialog {

    public static final String CHECK_BOX = "_check";

	/**
	 * Name of the clipboard table field.
	 * 
	 * <p>
	 * For compatibility with existing scripts, this should be the same as
	 * {@link SimpleTableDialog#INPUT_FIELD}
	 * </p>
	 */
	private static final String TABLE_FIELD = SimpleTableDialog.INPUT_FIELD;

	public static ClipboardDialog createDialog(WebFolder webFolder) {
		return new ClipboardDialog(null, webFolder);
	}

	public static ClipboardDialog createDialog(LayoutComponent anOpener) {
		WebFolder webFolder;
		if (anOpener instanceof WebFolderAware) {
			webFolder = ((WebFolderAware) anOpener).getWebFolder();
		} else {
			webFolder = null;
		}
		return new ClipboardDialog(anOpener, webFolder);
	}

	private final WebFolder webFolder;

	private LayoutComponent opener;

	private CommandModel _deleteFromClipboard;

	private CommandModel _clearClipboard;

	private CommandModel _addFromClipboard;

	final SelectionModel _selection;

	private ClipboardDialog(LayoutComponent anOpener, WebFolder webFolder) {
		super(new DefaultDialogModel(
			new DefaultLayoutData(dim(600, PIXEL), 100, dim(500, PIXEL), 100, Scrolling.NO),
			new ResourceText(I18NConstants.CLIPBOARD_DIALOG.key("dialogTitle")), true, true, null),
			I18NConstants.CLIPBOARD_DIALOG.key(HEADER_RESOURCE_SUFFIX),
			I18NConstants.CLIPBOARD_DIALOG.key(MESSAGE_RESOURCE_SUFFIX));
		_selection = createSelectionModel();
		this.opener = anOpener;
		this.webFolder = webFolder;
		initButtons();
	}

	private SelectionModel createSelectionModel() {
		SelectionModel selection = new DefaultMultiSelectionModel(SelectionModelOwner.NO_OWNER);
		selection.addSelectionListener(new SelectionListener() {

			@Override
			public void notifySelectionChanged(SelectionModel model, Set<?> formerlySelectedObjects, Set<?> selectedObjects) {
				if (formerlySelectedObjects.isEmpty() == selectedObjects.isEmpty()) {
					// No change.
					return;
				}
				ClipboardDialog.this.setNoRowSelected(selectedObjects.isEmpty());
			}
		});
		return selection;
	}

	private void initButtons() {
		_clearClipboard = newClearClipboardButton();
		_deleteFromClipboard = newDeleteFromClipboardButton();
		_addFromClipboard = newAddFromClipboardButton();
		setNoRowSelected(true);
	}

	void setNoRowSelected(boolean noRowSelected) {
		if (noRowSelected) {
			_deleteFromClipboard.setNotExecutable(I18NConstants.NO_ROW_SELECTED);
			if (_addFromClipboard != null) {
				_addFromClipboard.setNotExecutable(I18NConstants.NO_ROW_SELECTED);
			}
		} else {
			_deleteFromClipboard.setExecutable();
			if (_addFromClipboard != null) {
				_addFromClipboard.setExecutable();
			}
		}
	}

	void setNoRows(boolean noRows) {
		if (noRows) {
			_clearClipboard.setNotExecutable(I18NConstants.DISABLED_EMPTY_CLIPBOARD);
		} else {
			_clearClipboard.setExecutable();
		}
	}

	/**
	 * Creates a {@link CommandModel} to clear the clipboard.
	 */
	private CommandModel newClearClipboardButton() {
		ClearClipboardCommand ccc = ClearClipboardCommand.newInstance(this);
		return MessageBox.forwardStyleButton(ccc.getResourceKey(null), ccc);
	}

	/**
	 * Creates a {@link CommandModel} to remove the selected objects from the clipboard.
	 */
	private CommandModel newDeleteFromClipboardButton() {
		return MessageBox.forwardStyleButton(I18NConstants.DELETE, new DeleteFromClipboardCommand(this));
	}

	private CommandModel newAddFromClipboardButton() {
		// there are scenarios where the clipboard is displayed without a WebFolder but in those
		// cases it is not possible to add from the clipboard
		WebFolder theFolder = webFolder;
		if (theFolder == null) {
			theFolder = findVisibleWebFolder();
		}
		if (theFolder == null) {
			return null;
        }
		return newAddFromClibboardButton(theFolder);
	}

	/**
	 * Creates a {@link CommandModel} to add the selected objects to the given {@link WebFolder}.
	 */
	private CommandModel newAddFromClibboardButton(WebFolder folder) {
		AddFromClipboardCommand addCmd = new AddFromClipboardCommand(this, folder, getDiscardClosure());
		return MessageBox.forwardStyleButton(I18NConstants.ADD, addCmd);
	}

	/**
	 * Search for a visible {@link WebFolderAware} component, and return its web folder; if more
	 * than one such component is found, return <code>null</code>.
	 */
	protected WebFolder findVisibleWebFolder() {
		if (this.opener == null) {
			return null;
		}
		FindWebFolderVisitor theVisitor = new FindWebFolderVisitor();
		this.opener.getMainLayout().acceptVisitorRecursively(theVisitor);
		Set<WebFolder> theFound = theVisitor.getWebFolders();
		if (theFound != null && !theFound.isEmpty()) {
			if (theFound.size() > 1) {
				Logger.debug("Found more than one potential targets for clip board.", this);
				return null;
			}
			return theFound.iterator().next();
		}
		return null;
	}

	@Override
	protected HTMLFragment createTitleContent() {
		return Fragments.message(I18NConstants.CLIPBOARD_DIALOG_TITLE);
	}

	@Override
	protected HTMLFragment createSubtitleContent() {
		return Fragments.message(I18NConstants.CLIPBOARD_DIALOG.key("title"));
	}

	@Override
	protected IconControl createTitleIcon() {
		return IconControl.iconTheme(ThemeImage.typeIconLarge(Clipboard.OBJECT_NAME));
	}

	@Override
	protected HTMLFragment createBodyContent() {
		return DefaultFormFieldControlProvider.INSTANCE.createControl(
			getFormContext().getMember(TABLE_FIELD));
	}

    @Override
    protected void fillFormContext(FormContext context) {
		
    	FieldProvider booleanFieldProvider = new FieldProvider() {
            @Override
			public FormMember createField(final Object aModel, Accessor anAccessor, String aProperty) {
            	BooleanField newBooleanField = FormFactory.newBooleanField(aProperty, false, false);
				newBooleanField.addValueListener(new ChangeSelectionListener(aModel));
				return newBooleanField;
            }
        };

		String[] theColumns = new String[] { ClipboardDialog.CHECK_BOX, TLNamed.NAME_ATTRIBUTE };
		TableConfiguration tableConfiguration = TableConfiguration.table();
		tableConfiguration.setDefaultFilterProvider(null);
		tableConfiguration.setResPrefix(I18NConstants.CLIPBOARD_DIALOG.append(TABLE_FIELD));
		tableConfiguration.setColumnCustomization(ColumnCustomization.NONE);
		tableConfiguration.setMultiSort(Enabled.never);
		tableConfiguration.getDefaultColumn().setAccessor(WrapperAccessor.INSTANCE);
		tableConfiguration.setIDColumn(TLNamed.NAME_ATTRIBUTE);
		{
			ColumnConfiguration column = tableConfiguration.declareColumn(ClipboardDialog.CHECK_BOX);
			column.setAccessor(SimpleAccessor.INSTANCE);
			column.setFieldProvider(booleanFieldProvider);
			column.setFilterProvider(null);
			column.setControlProvider(DefaultFormFieldControlProvider.INSTANCE);
			column.setDefaultColumnWidth("50px");
			column.setMandatory(true);
		}
		{
			ColumnConfiguration column = tableConfiguration.declareColumn(TLNamed.NAME_ATTRIBUTE);
			column.setFieldProvider(null);
			column.setAccessor(IdentityAccessor.INSTANCE);
			column.setFilterProvider(LabelFilterProvider.INSTANCE);
			column.setComparator(new ClipboardComparator());
		}
		
		List<? extends TLObject> content = this.getContent();
		setNoRows(content.isEmpty());
		ObjectTableModel theModel = new ObjectTableModel(theColumns, tableConfiguration, content);
		
		FormTableModel tableModel = new FormTableModel(theModel, context);
		tableModel.addTableModelListener(new TableModelListener() {

			@Override
			public void handleTableModelEvent(TableModelEvent event) {
				TableModel source = (TableModel) event.getSource();
				ClipboardDialog.this.setNoRows(source.getRowCount() == 0);
			}
		});
		TableField theField = FormFactory.newTableField(TABLE_FIELD, tableModel);
        theField.setSelectable(false);

        context.addMember(theField);
    }
    
    @Override
    protected void fillButtons(List<CommandModel> buttons) {
		buttons.add(_deleteFromClipboard);
		buttons.add(_clearClipboard);
		if (_addFromClipboard != null) {
			buttons.add(_addFromClipboard);
		}
		buttons.add(MessageBox.forwardStyleButton(I18NConstants.CLOSE, getDiscardClosure()));
    }

	protected List<? extends TLObject> getContent() {
		return CollectionUtil.toList(Clipboard.getInstance().getContent());
    }

	public static class ClearClipboardCommand extends ClearClipboardHandler {
		
		private FormHandler handler;
		
		public ClearClipboardCommand(InstantiationContext context, Config config) {
			super(context, config);
		}
		
		public static ClearClipboardCommand newInstance(ClipboardDialog clipboardDialog) {
			ClearClipboardCommand result = newInstance(ClearClipboardCommand.class, COMMAND);
			result.handler = clipboardDialog;
			return result;
		}

		/**
		 * Overridden to {@link FormTableModel#clear() clear} the model.
		 */
		@Override
		public HandlerResult executeCommand(DisplayContext context) {
			HandlerResult theResult = super.executeCommand(context);
			if (theResult.isSuccess()) {
				FormContext theContext = handler.getFormContext();
				FormField theField = theContext.getField(TABLE_FIELD);
				FormTableModel theModel = (FormTableModel) ((TableField) theField).getTableModel();
				theModel.clear();
			}

			return theResult;
		}
	}

		

	/**
	 * Delete the selected objects from the clip board. 
	 * 
	 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
	public static class DeleteFromClipboardCommand implements Command {
	
		private final ClipboardDialog _dialog;
		
		public DeleteFromClipboardCommand(ClipboardDialog clipboardDialog) {
			this._dialog = clipboardDialog;
		}
		
		@Override
		public HandlerResult executeCommand(DisplayContext context) {
	        HandlerResult theResult  = new HandlerResult();
			FormContext theContext = _dialog.getFormContext();
			FormField theField = theContext.getField(TABLE_FIELD);
	
	        if (theField instanceof TableField) {
	            FormTableModel theModel = (FormTableModel) ((TableField) theField).getTableModel();
	            Transaction    theTX    = KnowledgeBaseFactory.getInstance().getDefaultKnowledgeBase().beginTransaction();
	
	            try {
	                Clipboard     theBoard   = Clipboard.getInstance();
	
					Header header = theModel.getHeader();
					int checkBoxColumn = header.getColumn(ClipboardDialog.CHECK_BOX).getIndex();
					for (int rowCount = theModel.getRowCount(), rowIdx = rowCount - 1; rowIdx >= 0; rowIdx--) {
						BooleanField theCheckbox = (BooleanField) theModel.getValueAt(rowIdx, checkBoxColumn);
	
	                    if (Utils.isTrue((Boolean) theCheckbox.getValue())) {
							TLObject toBeRemoved = (TLObject) theModel.getRowObject(rowIdx);
	
							// Invalid objects are not returned from a _self column.
							if (toBeRemoved != null && toBeRemoved.tValid()) {
								theBoard.remove(toBeRemoved);
	                        }
	
							theModel.removeRow(rowIdx);
							_dialog._selection.setSelected(toBeRemoved, false);
	                    }
	                }
	
					theTX.commit();
				} catch (KnowledgeBaseException ex) {
					Logger.error("Failed to remove objects from clipboard", ex, this);
					theResult.addErrorMessage(I18NConstants.REMOVE_ERROR);
				} finally {
	                theTX.rollback();
	            }
	        }
	
	        return theResult;
	    }
	}

	/**
	 * Command for adding the selected objects from the clip board to the folder.
	 * 
	 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
	public static class AddFromClipboardCommand implements Command {
	    
	    // Attributes
	
	    /** The web folder to add the objects to. */
	    private final WebFolder folder;
		private final FormHandler handler;
		private final Command continuation;
	    
	    // Constructors
	    
	    /** 
	     * Creates a {@link AddFromClipboardCommand}.
	     * 
	     * @param    aFolder    The web folder to add the objects to, must not be <code>null</code>.
	     */
	    public AddFromClipboardCommand(FormHandler handler, WebFolder aFolder, Command continuation) {
	        this.handler = handler;
			this.folder = aFolder;
			this.continuation = continuation;
	    }
	
	    // Implementation of interface Executable
	
	    @Override
		public HandlerResult executeCommand(DisplayContext context) {
			if (!ComponentUtil.isValid(folder)) {
				return ComponentUtil.errorObjectDeleted(context);
			}
	
			HandlerResult theResult = new HandlerResult();
	        FormContext   theContext = handler.getFormContext();
			FormField theField = theContext.getField(TABLE_FIELD);
	
	        if (theField instanceof TableField) {
	            FormTableModel theModel = (FormTableModel) ((TableField) theField).getTableModel();
	            Transaction    theTX    = KnowledgeBaseFactory.getInstance().getDefaultKnowledgeBase().beginTransaction();
	
	            try {
	                int           theRows    = theModel.getRowCount();
					List<TLObject> theObjects = new ArrayList<>();
	
					Header header = theModel.getHeader();
					int checkBoxColumn = header.getColumn(ClipboardDialog.CHECK_BOX).getIndex();
	                for (int theRow = 0; theRow < theRows; theRow++) {
						BooleanField theCheckbox = (BooleanField) theModel.getValueAt(theRow, checkBoxColumn);
	
	                    if (Utils.isTrue((Boolean) theCheckbox.getValue())) {
							theObjects.add((TLObject) theModel.getRowObject(theRow));
	                    }
	                }
	
	                if (!theObjects.isEmpty()) {
						for (TLObject theWrapper : theObjects) {
							if (!this.folder.hasChild(((Named) theWrapper).getName())) {
								this.folder.add(theWrapper);
	                        }
	                    }
	
	                    theTX.commit();
	                }
	
	                return continuation.executeCommand(context);
	            }
	            catch (Exception ex) {
	                Logger.error("Failed to add objects to folder " + this.folder, ex, this);
	
					theResult.addErrorMessage(I18NConstants.ADD_ERROR);
	            }
	            finally {
	                theTX.rollback();
	            }
	        }
	
	        return theResult;
	    }
	}

	class ChangeSelectionListener implements ValueListener{

		private final Object _clipboardEntry;

		public ChangeSelectionListener(Object clipboardEntry) {
			_clipboardEntry = clipboardEntry;
		}

		@Override
		public void valueChanged(FormField field, Object oldValue, Object newValue) {
			ClipboardDialog.this._selection.setSelected(_clipboardEntry, Utils.isTrue((Boolean) newValue));
		}
	}
}
