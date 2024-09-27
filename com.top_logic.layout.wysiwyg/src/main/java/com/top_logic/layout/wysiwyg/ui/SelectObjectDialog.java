/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.wysiwyg.ui;

import static com.top_logic.layout.DisplayDimension.*;
import static com.top_logic.layout.DisplayUnit.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

import org.w3c.dom.Document;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.base.services.simpleajax.JSSnipplet;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.element.layout.meta.search.quick.AbstractSearchCommand;
import com.top_logic.element.layout.meta.search.quick.AbstractSearchCommand.SearchConfig;
import com.top_logic.element.layout.meta.search.quick.QuickSearchCommand;
import com.top_logic.element.layout.meta.search.quick.QuickSearchCommand.Config;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DynamicText;
import com.top_logic.layout.WindowScope;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.buttonbar.ButtonBarFactory;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.FormMemberVisitor;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.control.TextInputControl;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.model.ConstantField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.ListField;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.form.selection.OptionRenderer;
import com.top_logic.layout.form.selection.PatternInput;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.form.template.FormGroupControl;
import com.top_logic.layout.form.template.FormTemplate;
import com.top_logic.layout.list.DoubleClickCommand;
import com.top_logic.layout.list.ListControl;
import com.top_logic.layout.list.model.ListModelUtilities;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.structure.DefaultDialogModel;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.DialogModel;
import com.top_logic.layout.structure.DialogWindowControl;
import com.top_logic.layout.structure.LayoutControlAdapter;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * Dialog to select a top-logic object from full-text search.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class SelectObjectDialog {
	private static final int MINIMUM_SEARCH_STRING_LENGTH = 3;

	private static final String ACCEPT_COMMAND_NAME = "acceptCommand";

	private static final String CANCEL_COMMAND_NAME = "cancelCommand";

	private static final String CONTROL_NAME = "control";

	private static final String BUTTONS_GROUP_NAME = "buttons";

	private static final String CREATE_LINK_GROUP_NAME = "createTLLink";

	private static final String TITLE_FIELD_NAME = "searchTLObjectTitle";

	private static final String OPTIONS_FIELD_NAME = "tlObjectOptions";

	private static final String SEARCH_FIELD_NAME = "searchTLObject";

	private static final int MAX_RESULTS = 100;

	private static final String SEARCH_FIELD_STYLE = "width:100%;float:left;";

	static final Property<Control> CONTROL = TypedAnnotatable.property(Control.class, CONTROL_NAME);

	private final int _width = 720;

	private final int _height = 470;

	StructuredTextControl _editor;

	/**
	 * Creates the {@link SelectObjectDialog} for the given WYSIWYG instance.
	 * 
	 * @param editor
	 *        CKEditor instance.
	 */
	public SelectObjectDialog(StructuredTextControl editor) {
		_editor = editor;
	}

	/**
	 * Opens this dialog.
	 * 
	 * @param windowScope
	 *        The window in which to open the dialog.
	 * @return The {@link DialogModel} of the opened dialog.
	 */
	public DialogModel open(WindowScope windowScope) {
		DefaultLayoutData layoutData = getLayoutData();
		HTMLFragment title = getTitle();
		DefaultDialogModel dialogModel = getDialogModel(layoutData, title);
		DialogWindowControl dialogControl = new DialogWindowControl(dialogModel);
		dialogControl.setChildControl(new LayoutControlAdapter(getFormGroupControl(dialogModel)));
		windowScope.openDialog(dialogControl);

		return dialogModel;
	}

	private DefaultLayoutData getLayoutData() {
		return new DefaultLayoutData(dim(_width, PIXEL), 100, dim(_height, PIXEL), 100, Scrolling.AUTO);
	}

	private DefaultDialogModel getDialogModel(DefaultLayoutData layoutData, HTMLFragment title) {
		return new DefaultDialogModel(layoutData, title, true, true, null);
	}

	private HTMLFragment getTitle() {
		return new HTMLFragment() {

			@Override
			public void write(DisplayContext context, TagWriter out) throws IOException {
				out.writeText(Resources.getInstance().getString(I18NConstants.TITLE_FIELD_LABEL));
			}
		};
	}


	/**
	 * Creates the actual content of the given {@link DialogWindowControl}.
	 * 
	 * @return The content which will be set as content of the given {@link DialogWindowControl}.
	 * 
	 */
	protected HTMLFragment getFormGroupControl(DialogModel dialog) {
		FormContext formContext = new FormContext(CREATE_LINK_GROUP_NAME, I18NConstants.CREATE_TL_OBJECT_LINK_PREFIX);

		ListField optionField = getOptionField();
		StringField searchField = getSearchField(optionField);
		FormGroup buttonGroup = getButtonGroup(dialog, optionField);
		ConstantField titleField = getTitleField();

		formContext.addMember(searchField);
		formContext.addMember(optionField);
		formContext.addMember(buttonGroup);
		formContext.addMember(titleField);

		FormTemplate template = getFormTemplate();
		FormGroupControl control = new FormGroupControl(formContext, template);

		return control;
	}

	private ValueListener getSearchValueListener(Command searchCommand, ListField optionField) {
		return new ValueListener() {

			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				String patternString = (String) newValue;

				int length = patternString.length();

				if (length < MINIMUM_SEARCH_STRING_LENGTH) {
					@SuppressWarnings("unchecked")
					ListModel<Wrapper> listModel = optionField.getListModel();
					ListModelUtilities.replaceAll((DefaultListModel<Wrapper>) listModel, Collections.EMPTY_LIST);
				} else {
					searchCommand.executeCommand(DefaultDisplayContext.getDisplayContext());
				}
			}
		};
	}

	private ConstantField getTitleField() {
		ConstantField constantField = getConstantTitleField();
		constantField.setLabel(I18NConstants.TITLE_FIELD_LABEL);

		return constantField;
	}

	private ConstantField getConstantTitleField() {
		return new ConstantField(TITLE_FIELD_NAME, false) {

			@Override
			public Object visit(FormMemberVisitor v, Object arg) {
				return v.visitFormMember(this, arg);
			}
		};
	}

	private FormGroup getButtonGroup(DialogModel dialog, ListField optionField) {
		FormGroup buttons = new FormGroup(BUTTONS_GROUP_NAME, I18NConstants.CREATE_TL_OBJECT_LINK_PREFIX);
		Command closeCommand = dialog.getCloseAction();
		CommandField acceptCommand = getAcceptCommand(closeCommand, optionField);
		dialog.setDefaultCommand(acceptCommand);
		CommandField cancelCommand = getCancelCommand(closeCommand);

		buttons.addMember(acceptCommand);
		buttons.addMember(cancelCommand);

		buttons.setControlProvider(getButtonControlProvider(buttons));

		return buttons;
	}

	private ControlProvider getButtonControlProvider(FormGroup buttonGroup) {
		ControlProvider controlProvider = (Object model, String style) -> {
			ArrayList<? extends FormMember> buttons = CollectionUtil.toList(buttonGroup.getDescendants());
			return ButtonBarFactory.createButtonBar(CollectionUtil.dynamicCastView(CommandModel.class, buttons));
		};

		return controlProvider;
	}

	private CommandField getAcceptCommand(Command closeCommand, ListField optionField) {
		CommandField accept = FormFactory.newCommandField(ACCEPT_COMMAND_NAME, createLinkCommand(closeCommand, optionField));
		accept.setLabel(I18NConstants.ACCEPT_COMMAND_LABEL);
		accept.setShowProgress();

		setDoubleClickCommand(optionField, accept);

		return accept;
	}

	private void setDoubleClickCommand(ListField optionField, CommandField accept) {
		DoubleClickCommand dblClickCommand = new DoubleClickCommand(accept);
		dblClickCommand.setIsWaitPaneRequested(true);
		((ListControl) optionField.get(CONTROL)).setDblClickAction(dblClickCommand);
	}

	private Command createLinkCommand(Command closeCommand, ListField optionField) {
		return new Command() {

			@Override
			public HandlerResult executeCommand(DisplayContext context) {
				List<?> selectionList = optionField.getSelectionList();

				if (selectionList.size() == 1) {
					Wrapper object = (Wrapper) selectionList.get(0);
					String linkText = MetaLabelProvider.INSTANCE.getLabel(object);
					final String link = TLObjectLinkUtil.getLink(object, linkText, null);

					_editor.getFrameScope().addClientAction(new JSSnipplet(new DynamicText() {

						@Override
						public void append(DisplayContext innerContext, Appendable out) throws IOException {
							String controlId = _editor.getID();
							String contentId = controlId + "-content";
							CharSequence insertLink = "services.wysiwyg.StructuredText.insertLink('" + controlId
								+ "', '" + contentId + "', '" + link + "' );";
							out.append(insertLink);
						}
					}));
				} else {
					Logger.error(Resources.getInstance().getString(I18NConstants.MULTIPLE_OBJECTS_SELECTED_ERROR),
						this);
				}

				return closeCommand.executeCommand(context);
			}
		};
	}

	private CommandField getCancelCommand(Command closeCommand) {
		CommandField cancel = FormFactory.newCommandField(CANCEL_COMMAND_NAME, closeCommand);
		cancel.setLabel(I18NConstants.CANCEL_COMMAND_LABEL);

		return cancel;
	}

	@SuppressWarnings("unchecked")
	private Command getSearchCommand(StringField searchField, ListField optionField) {
		Config quickSearchConfig = QuickSearchCommand.quickSearchConfig();
		AbstractSearchCommand.SearchConfig searchConfig = new AbstractSearchCommand.SearchConfig();
		searchConfig.setMaxResults(MAX_RESULTS);
		searchConfig.setMinSearchStringLength(MINIMUM_SEARCH_STRING_LENGTH);
		searchConfig.getExcludeTypes().addAll(SearchConfig.resolveTypes(quickSearchConfig.getExcludeTypes()));
		searchConfig.getIncludeTypes().addAll(SearchConfig.resolveTypes(quickSearchConfig.getIncludeTypes()));
		searchConfig.getSearchAttributes().putAll(SearchConfig.resolveAttributes(quickSearchConfig.getSearchAttributes()));
		return new ObjectSearchCommand(searchField, optionField.getListModel(), searchConfig);
	}

	private FormTemplate getFormTemplate() {
		return new FormTemplate(I18NConstants.CREATE_TL_OBJECT_LINK_PREFIX,
			DefaultFormFieldControlProvider.INSTANCE, true,
			getTemplateDocument());
	}

	private ListField getOptionField() {
		ListField optionField = FormFactory.newListField(OPTIONS_FIELD_NAME, new DefaultListModel<>());

		optionField.setLabel(I18NConstants.OPTIONS_FIELD_LABEL);
		optionField.set(CONTROL, getOptionControl(optionField));
		optionField.setControlProvider(getOptionControlProvider());

		return optionField;
	}

	private ControlProvider getOptionControlProvider() {
		return new ControlProvider() {

			@Override
			public Control createControl(Object model, String style) {
				return ((FormField) model).get(CONTROL);
			}
		};
	}

	private ListControl getOptionControl(ListField optionField) {
		ListControl listControl = new ListControl(optionField);
		listControl.setListRenderer(new OptionRenderer());
		listControl.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		return listControl;
	}

	private StringField getSearchField(ListField optionField) {
		StringField searchField = FormFactory.newStringField(SEARCH_FIELD_NAME);

		searchField.setControlProvider(getSearchControlProvider(searchField, optionField));
		searchField.setLabel(I18NConstants.SEARCH_FIELD_LABEL);
		searchField.addValueListener(getSearchValueListener(getSearchCommand(searchField, optionField), optionField));

		return searchField;
	}

	private ControlProvider getSearchControlProvider(StringField searchField, ListField optionField) {
		return (Object model, String style) -> {
			TextInputControl textControl = new TextInputControl(searchField);
			textControl.setInputStyle(SEARCH_FIELD_STYLE);

			ListControl optionControl = (ListControl) optionField.get(CONTROL);
			textControl.setOnInput(new PatternInput(textControl, optionControl, null, null));

			return textControl;
		};
	}

	private Document getTemplateDocument() {
		return new SearchTemplateBuilder().get();
	}
}
