/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import static com.top_logic.layout.DisplayDimension.*;
import static com.top_logic.layout.DisplayUnit.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.WindowScope;
import com.top_logic.layout.basic.AbstractControl;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.ResourceText;
import com.top_logic.layout.buttonbar.ButtonBarFactory;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.SelectFieldUtils;
import com.top_logic.layout.form.model.utility.OptionModel;
import com.top_logic.layout.form.model.utility.OptionModelListener;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.structure.DefaultDialogModel;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.DialogModel;
import com.top_logic.layout.structure.DialogWindowControl;
import com.top_logic.layout.structure.FixedFlowLayoutControl;
import com.top_logic.layout.structure.LayoutControl;
import com.top_logic.layout.structure.LayoutControlAdapter;
import com.top_logic.layout.structure.OrientationAware.Orientation;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link Control} displaying the options of a {@link SelectField} as check-list.
 * 
 * <p>
 * In view mode, the number of selected and total options is displayed. In edit-mode, a dialog can
 * be opened that shows all options each with a checkbox to select the option.
 * </p>
 */
public class ChecklistControl extends AbstractControl implements ValueListener, OptionModelListener {

    private static final DisplayDimension DEFAULT_DIALOG_HEIGHT = dim(400, PIXEL);

	private static final DisplayDimension DEFAULT_DIALOG_WIDTH = dim(500, PIXEL);

	private static final Map<String, ControlCommand> COMMANDS = createCommandMap(new ControlCommand[] {
		OpenPopup.INSTANCE, ChecklistInspector.INSTANCE
    });
    
	private SelectField _selectField;

	private OptionModel<?> _optionModel;

	private HTMLFragment _dialogTitle;

	private DisplayDimension _dialogWidth = DEFAULT_DIALOG_WIDTH;

	private DisplayDimension _dialogHeight = DEFAULT_DIALOG_HEIGHT;

	private String _dialogContentClass = FormConstants.FORM_BODY_CSS_CLASS;

    /** Creates a {@link ChecklistControl}. */
    public ChecklistControl(SelectField aSelectField) {
    	super(COMMANDS);
    	
		_selectField = aSelectField;
    }
    
	/**
	 * Sets a custom dialog title.
	 */
	public void setDialogTitle(HTMLFragment dialogTitle) {
		_dialogTitle = dialogTitle;
	}

	/**
	 * Sets a custom dialog width for the checklist dialog.
	 */
	public void setDialogWidth(DisplayDimension dialogWidth) {
		_dialogWidth = dialogWidth;
	}

	/**
	 * Sets a custom dialog height for the checklist dialog.
	 */
	public void setDialogHeight(DisplayDimension dialogHeight) {
		_dialogHeight = dialogHeight;
	}

	/**
	 * Sets a custom CSS class for the checklist dialog content.
	 */
	public void setDialogContentClass(String cssClass) {
		_dialogContentClass = cssClass;
	}

	@Override
	public SelectField getModel() {
		return _selectField;
	}

    @Override
	protected void attachRevalidated() {
    	super.attachRevalidated();
    	
		_selectField.addValueListener(this);
    }

    @Override
	protected void detachInvalidated() {
    	super.detachInvalidated();

		resetOptionModel();
		_selectField.removeValueListener(this);
    }

    @Override
	public void valueChanged(FormField aField, Object aOldValue, Object aNewValue) {
        requestRepaint();
    }
    
    @Override
	protected String getTypeCssClass() {
		return "cCheckList";
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		SelectField field = _selectField;
		OptionModel<?> optionModel = field.getOptionModel();
		initOptions(optionModel);

		out.beginBeginTag(SPAN);
		writeControlAttributes(context, out);
        out.endBeginTag();

		if (field.isVisible()) {
            // Begin label span
            out.beginBeginTag(SPAN);
            out.writeAttribute(ID_ATTR, this.getShortStateID());
			out.writeAttribute(CLASS_ATTR, "cCheckList-counts");
            out.endBeginTag();
            
            if (field.isMultiple()) {
                // Write the short info (10/20)
        		int totalOptions = optionModel.getOptionCount();
        		int selectedOptions = field.getSelection().size();
        		
				out.append(context.getResources()
					.getString(I18NConstants.CHECKLIST_DISPLAY__CNT_TOTAL.fill(selectedOptions, totalOptions)));
            } else {
				SelectFieldUtils.getOptionRenderer(field).write(context, out, field.getSingleSelection());
            }
            out.endTag(SPAN);
            
			if (!field.isImmutable()) {
				SelectionControl.writePopupButton(context, out, this, field.isDisabled(),
					this.getOpenPopupId(), OpenPopup.INSTANCE, field.getLabel());
	        }
        }

        out.endTag(SPAN);
    }

	private void initOptions(OptionModel<?> optionModel) {
		resetOptionModel();

		_optionModel = optionModel;
		_optionModel.addOptionListener(this);
	}

	@Override
	public void notifyOptionsChanged(OptionModel<?> sender) {
		requestRepaint();
		resetOptionModel();
	}

	private void resetOptionModel() {
		if (_optionModel != null) {
			_optionModel.removeOptionListener(this);
			_optionModel = null;
		}
	}

	private String getOpenPopupId() {
		return getID() + "-open";
	}

	private String getShortStateID() {
		return getID() + "-state";
	}

    @Override
	public boolean isVisible() {
		return _selectField.isVisible();
    }

    /** This method returns the select field. */
    public SelectField getSelectField() {
		return _selectField;
    }

	/**
	 * Opens the dialog showing the checklist for edit.
	 */
	final HandlerResult openChecklistDialog(WindowScope windowScope) {
		DefaultDialogModel dialogModel = createDialogModel();
		DialogWindowControl dialogControl = createDialogControl(dialogModel);
		windowScope.openDialog(dialogControl);
		return HandlerResult.DEFAULT_RESULT;
	}

	private DefaultDialogModel createDialogModel() {
		DefaultLayoutData layoutData =
			new DefaultLayoutData(_dialogWidth, 100, _dialogHeight, 100, Scrolling.NO);

		HTMLFragment title = getDialogTitle();

		DefaultDialogModel dialogModel =
			new DefaultDialogModel(layoutData, title, /* resizable */true, /* closable */true, null,
				_selectField.getConfigKey());
		return dialogModel;
	}

	private HTMLFragment getDialogTitle() {
		if (_dialogTitle != null) {
			return _dialogTitle;
		}

		return new ResourceText(
			com.top_logic.layout.form.I18NConstants.POPUP_SELECT_TITLE__FIELD.fill(_selectField.getLabel()));
	}

	private DialogWindowControl createDialogControl(DefaultDialogModel dialogModel) {
		DialogWindowControl result = new DialogWindowControl(dialogModel);
		result.setChildControl(createContentView(dialogModel));
		return result;
	}

	private LayoutControl createContentView(DialogModel dialogModel) {
		FixedFlowLayoutControl result = new FixedFlowLayoutControl(Orientation.VERTICAL);
		
		result.addChild(createChecklistView());

		List<CommandModel> commands = new ArrayList<>();
		if (_selectField.isMultiple()) {
			commands.add(createSelectAllButton());
			commands.add(createSelectNoneButton());
		}

		commands.add(createOkButton(dialogModel));
		commands.add(createCancelButton(dialogModel));

		LayoutControlAdapter buttonBar = new LayoutControlAdapter(ButtonBarFactory.createButtonBar(commands));
		buttonBar.setConstraint(new DefaultLayoutData(HUNDERED_PERCENT, 100, DisplayDimension.px(50), 0, Scrolling.NO));
		result.addChild(buttonBar);
		
		return result;
	}

	private CommandModel createSelectNoneButton() {
		CommandModel result = MessageBox.button(I18NConstants.CHECKLIST_NONE, Icons.CHECKLIST_NONE, (context) -> {
			List<?> newSelection = Collections.emptyList();
			_selectField.setAsSelection(newSelection);
			if (ScriptingRecorder.isRecordingActive()) {
				ScriptingRecorder.recordFieldInput(_selectField, newSelection);
			}
			return HandlerResult.DEFAULT_RESULT;
		});
		ScriptingRecorder.annotateAsDontRecord(result);
		return result;
	}

	private CommandModel createSelectAllButton() {
		CommandModel result = MessageBox.button(I18NConstants.CHECKLIST_ALL, Icons.CHECKLIST_ALL, (context) -> {
			List<?> newSelection = _selectField.getOptions();
			_selectField.setAsSelection(newSelection);
			if (ScriptingRecorder.isRecordingActive()) {
				ScriptingRecorder.recordFieldInput(_selectField, newSelection);
			}
			return HandlerResult.DEFAULT_RESULT;
		});
		ScriptingRecorder.annotateAsDontRecord(result);
		return result;
	}

	private CommandModel createCancelButton(DialogModel dialogModel) {
		@SuppressWarnings("unchecked")
		List<Object> oldSelection = new ArrayList<>(_selectField.getSelection());

		CommandModel result = MessageBox.button(ButtonType.CANCEL, (context) -> {
			_selectField.setAsSelection(oldSelection);
			if (ScriptingRecorder.isRecordingActive()) {
				ScriptingRecorder.recordFieldInput(_selectField, oldSelection);
			}
			return dialogModel.getCloseAction().executeCommand(context);
		});
		ScriptingRecorder.annotateAsDontRecord(result);
		return result;
	}

	private CommandModel createOkButton(DialogModel dialogModel) {
		CommandModel result = MessageBox.button(ButtonType.OK, dialogModel.getCloseAction());
		dialogModel.setDefaultCommand(dialogModel.getCloseAction());
		ScriptingRecorder.annotateAsDontRecord(result);
		return result;
	}

	private LayoutControlAdapter createChecklistView() {
		LayoutControlAdapter result = new LayoutControlAdapter(createChecklistForm());
		result.setCssClass(_dialogContentClass);
		result.setConstraint(new DefaultLayoutData(HUNDERED_PERCENT, 100, HUNDERED_PERCENT, 100, Scrolling.AUTO));
		return result;
	}

	/**
	 * Creates the content of the checklist dialog.
	 */
	protected HTMLFragment createChecklistForm() {
		ChoiceControl choice = new ChoiceControl(getSelectField());
		choice.setOrientation(Orientation.VERTICAL);
		return choice;
	}

	/** This command opens a separate window for a checklist. */
    public static class OpenPopup extends ControlCommand {

        /** The unique command id. */
        public static final String    COMMAND  = "openPopup";
        /** The single instance of this class. */
        public static final OpenPopup INSTANCE = new OpenPopup();
        
        /** Creates a {@link OpenPopup}. */
        private OpenPopup() {
            super(COMMAND);
        }

        @Override
		protected HandlerResult execute(DisplayContext aCommandContext, Control aControl, Map<String, Object> arguments) {
			ChecklistControl self = (ChecklistControl) aControl;
			return self.openChecklistDialog(aCommandContext.getWindowScope());
        }

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.OPEN_CHECKLIST_DIALOG;
		}
    }

    
}

