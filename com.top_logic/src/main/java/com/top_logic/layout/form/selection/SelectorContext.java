/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.selection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListModel;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.KeyCode;
import com.top_logic.layout.KeyEvent;
import com.top_logic.layout.KeyEventListener;
import com.top_logic.layout.KeySelector;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.LabelFilter;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormMemberVisitor;
import com.top_logic.layout.form.I18NConstants;
import com.top_logic.layout.form.model.AbstractFormField;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.model.ConstantField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.ListField;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.form.selection.ListModifySelection.ModifyMode;
import com.top_logic.layout.list.model.ListModelUtilities;
import com.top_logic.layout.list.model.RestrictedListSelectionModel;
import com.top_logic.layout.provider.LowerCaseLabelProvider;
import com.top_logic.layout.scripting.recorder.DynamicRecordable;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;

/**
 * Base class for {@link FormContext} implementations for changing the selection of an
 * {@link SelectField} in a dialog.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class SelectorContext extends FormContext implements DynamicRecordable {

	/** Constant to indicate that all options are shown on one page. */
	public static int ALL_OPTIONS_ON_ONE_PAGE = -1;

	private static final String MOVE_OPTION_CSS_CLASS = "moveOption";
	public static final String REMOVE_FROM_SELECTION = "removeFromSelection";
	public static final String REMOVE_ALL_FROM_SELECTION = "removeAllFromSelection";

	public static final String ADD_TO_SELECTION = "addToSelection";
	public static final String ADD_ALL_TO_SELECTION = "addAllToSelection";

	public static final String MOVE_UP = "moveUp";

	public static final String MOVE_DOWN = "moveDown";

	public static final String BUTTONS = "buttons";


	public static final String CONTEXT_NAME = "selector";
	
	public static final String OPTIONS_FIELD_NAME = "options";
	public static final String SELECTION_FIELD_NAME = "selection";
	public static final String TITLE_FIELD_NAME = "titleFieldName";
	
	public static final String PATTERN_FIELD_NAME = "pattern";
	public static final String PAGE_FIELD_NAME = "page";

	protected static final List<Page> SINGLE_EMPTY_PAGE_LIST = Collections.singletonList(Page.EMPTY_PAGE);

	private static final String ACCEPT_ON_ENTER_MEMBER = "acceptMember";

	static final String ACCEPT_SELECTION = "acceptSelection";

	static final String CANCEL_SELECTION = "cancelSelection";

	protected static final KeySelector[] PATTERN_KEYS = {
		new KeySelector(KeyCode.RETURN, false, false, false)
	};
	
	protected final LabelProvider optionLabels;
	protected final LabelProvider pageLabels;
	
	private final SelectField _targetSelectField;
	
	protected ListField optionList;
	protected ListField selectionList;

	protected SelectField optionPages;

	protected final StringField pattern;

	private Command closeAction;

	private CommandField moveDown;

	private CommandField moveUp;

	private CommandField add;

	private CommandField remove;

	private CommandField addAll;

	private CommandField removeAll;

	private CommandField accept;

	private FormGroup _buttons;

	private ListSelectionListener _selectButtonUpdater;

	private ListDataListener _listButtonUpdater;

	/**
	 * Creates a {@link SelectorContext}.
	 * 
	 * @param targetSelectField
	 *        The {@link SelectField} modified by this {@link SelectorContext}.
	 * @param optionsPerPage
	 *        How many options shall be shown on one page?
	 *        {@link SelectorContext#ALL_OPTIONS_ON_ONE_PAGE} indicates that all options are shown
	 *        on the sole page.
	 * @param closeAction
	 *        Command that finally closes the dialog showing this {@link SelectorContext}.
	 * @param isLeftToRight
	 *        whether the options are shown left and the selection on the right side of the dialog.
	 */
	public SelectorContext(SelectField targetSelectField, LabelProvider newPageLabels, int optionsPerPage,
			Command closeAction, boolean isLeftToRight) {
		super(CONTEXT_NAME, ResPrefix.legacyPackage(SelectorContext.class));
		this.closeAction = closeAction;
		String targetFieldLabel = targetSelectField.getLabel();
		
		this._targetSelectField = targetSelectField;
		this.optionLabels = targetSelectField.getOptionLabelProvider();
		if (newPageLabels != null) {
			this.pageLabels = newPageLabels;
		} else {
			// Use the same labels for the page selector as for the option
			// list.
			this.pageLabels = this.optionLabels;
		}
		this.pattern = FormFactory.newStringField(PATTERN_FIELD_NAME);
		pattern.setLabel(I18NConstants.POPUP_SELECT_FILTER__FIELD.fill(targetFieldLabel));
		addMember(pattern);

		init(targetSelectField, optionsPerPage);

		// Check the contract of this class.
		final boolean multiSelect = isMultiSelect();
		if (multiSelect) {
			assert SELECTION_FIELD_NAME.equals(selectionList.getName());
			addMember(selectionList);
			selectionList.setLabel(I18NConstants.POPUP_SELECT_SELECTED__FIELD.fill(targetFieldLabel));
		}
		assert PAGE_FIELD_NAME.equals(optionPages.getName());
		addMember(optionPages);

		assert OPTIONS_FIELD_NAME.equals(optionList.getName());
		addMember(optionList);
		optionList.setLabel(I18NConstants.POPUP_SELECT_OPTIONS__FIELD.fill(targetFieldLabel));
		
		Resources resources = Resources.getInstance();
		if (multiSelect) {
			String rightArrowLabel = resources.getString(I18NConstants.SELECT_ARROW_RIGHT, HTMLConstants.RIGHT_ARROW);
			String rightArrowDoubleLabel =
				resources.getString(I18NConstants.SELECT_ARROW_DOUBLE_RIGHT, HTMLConstants.RIGHT_ARROW
					+ HTMLConstants.RIGHT_ARROW);
			String leftArrowLabel = resources.getString(I18NConstants.SELECT_ARROW_LEFT, HTMLConstants.LEFT_ARROW);
			String leftArrowDoubleLabel =
				resources.getString(I18NConstants.SELECT_ARROW_DOUBLE_LEFT, HTMLConstants.LEFT_ARROW
					+ HTMLConstants.LEFT_ARROW);

			String addLabel, addAllLabel, removeLabel, removeAllLabel;
			if (isLeftToRight) {
				addLabel = rightArrowLabel;
				addAllLabel = rightArrowDoubleLabel;
				removeLabel = leftArrowLabel;
				removeAllLabel = leftArrowDoubleLabel;
			} else {
				addLabel = leftArrowLabel;
				addAllLabel = leftArrowDoubleLabel;
				removeLabel = rightArrowLabel;
				removeAllLabel = rightArrowDoubleLabel;
			}

			add = addButton(ADD_TO_SELECTION, addLabel, ModifyMode.ADD);
			remove = addButton(REMOVE_FROM_SELECTION, removeLabel, ModifyMode.REMOVE);
			addAll = addButton(ADD_ALL_TO_SELECTION, addAllLabel, ModifyMode.ADD_ALL);
			removeAll = addButton(REMOVE_ALL_FROM_SELECTION, removeAllLabel, ModifyMode.REMOVE_ALL);

			if (targetSelectField.hasCustomOrder()) {
				moveUp = addButton(MOVE_UP, HTMLConstants.UP_ARROW, ModifyMode.UP);
				moveDown = addButton(MOVE_DOWN, HTMLConstants.DOWN_ARROW, ModifyMode.DOWN);
			}

			_selectButtonUpdater = new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent e) {
					updateButtonsState();
				}
			};

			_listButtonUpdater = new ListDataAdapter() {
				@Override
				protected void listChanged(ListDataEvent e) {
					updateButtonsState();
				}
			};

			registerListListeners();
		}
		addMember(FormFactory.newCommandField(ACCEPT_ON_ENTER_MEMBER, new ListAcceptSelection(this, targetSelectField, true)));
		
		_buttons = createButtons();
		addMember(_buttons);

		ConstantField title = new ConstantField(TITLE_FIELD_NAME, !AbstractFormField.IMMUTABLE) {
			
			@Override
			public Object visit(FormMemberVisitor v, Object arg) {
				return v.visitFormMember(this, arg);
			}
		};
		title.setLabel(I18NConstants.POPUP_SELECT_TITLE__FIELD.fill(targetFieldLabel));
		addMember(title);

		pattern.addKeyListener(new KeyEventListener(PATTERN_KEYS) {
			@Override
			public HandlerResult handleKeyEvent(DisplayContext commandContext, KeyEvent event) {
				switch (event.getKeyCode()) {
					case RETURN: {
						HandlerResult result;
						Set selectionSet = optionList.getSelectionSet();
						int selectionSize = selectionSet.size();
						if (selectionSize == 1) {
							if (multiSelect) {
								result = getAddCommand().executeCommand(commandContext);
							} else {
								result = getAcceptCommand().executeCommand(commandContext);
							}
							clearPattern();
						} else if (selectionSize == 0) {
							CommandModel accept = getAcceptCommand();
							if (accept.isExecutable()) {
								result = accept.executeCommand(commandContext);
							} else {
								result = HandlerResult.DEFAULT_RESULT;
							}
						} else {
							result = HandlerResult.DEFAULT_RESULT;
						}
						return result;
					}

					default: {
						// Ignore.
						return HandlerResult.DEFAULT_RESULT;
					}
				}
			}
		});

		// Initial state.
		updateButtonsState();

		initConstraints();
	}

	private void initConstraints() {
		if (isMultiSelect()) {
			addConstraintForMultiSelect();
		} else {
			addConstraintForSingleSelect();
		}

	}

	private void addConstraintForSingleSelect() {
		ListField listField = getOptionList();
		addSelectionConstraints(listField);
		addListSelectionListener(listField);
	}

	private void addSelectionConstraints(ListField listField) {
		List<Constraint> constraints = getSelectField().getConstraints();
		for (Constraint c : constraints) {
			listField.addWarningConstraint(new SelectionCheckingConstraint(c, listField));
		}
	}

	private void addListSelectionListener(final ListField listField) {
		ListSelectionModel model = listField.getSelectionModel();
		ListSelectionListener listener = new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				listField.check();
			}
		};
		model.addListSelectionListener(listener);
	}

	private void addConstraintForMultiSelect() {
		final ListField listField = getSelectionList();
		addListDataConstraints(listField);
		addListDataListener(listField);
	}

	private void addListDataConstraints(ListField listField) {
		List<Constraint> constraints = getSelectField().getConstraints();
		for (Constraint c : constraints) {
			listField.addWarningConstraint(new ContentCheckingConstraint(c, listField));
		}
	}

	private void addListDataListener(final ListField listField) {
		ListModel model = listField.getListModel();
		ListDataListener listener = new ListDataAdapter() {

			@Override
			protected void listChanged(ListDataEvent e) {
				listField.check();
			}
		};
		model.addListDataListener(listener);
	}

	/**
	 * Create the buttons Group with the Buttons needed.
	 */
	protected FormGroup createButtons() {
		CommandField acceptButton = createAcceptCommand();
		CommandField cancelButton = createCancelCommand();

		return SelectorContextUtil.createButtonGroup(acceptButton, cancelButton, getResources());
	}

	/**
	 * Create {@link #ACCEPT_SELECTION} as {@link ListAcceptSelection} command.
	 */
	protected CommandField createAcceptCommand() {
		Command command = new ListAcceptSelection(this, getSelectField(), false);
		CommandField acceptButton = SelectorContextUtil.createAcceptButton(command);

		this.accept = acceptButton;
		if (isMultiSelect()) {
			addAcceptEnableListener();
			updateUnmodifiedState(true);
		}

		return acceptButton;
	}

	/**
	 * Create {@link #CANCEL_SELECTION} for the {@link #closeAction}.
	 */
	protected CommandField createCancelCommand() {
		CommandField cancelButton = SelectorContextUtil.createCancelButton(closeAction);

		return cancelButton;
	}

	protected void updateButtonsState() {
		if (isMultiSelect()) {
			ListSelectionModel selectionModel = selectionList.getSelectionModel();
			ListModel<?> listModel = selectionList.getListModel();
			boolean nothingSelectedInSelection = selectionModel.isSelectionEmpty();
			boolean isFirstElementSelected = selectionModel.getMinSelectionIndex() == 0;
			boolean isLastElementSelected = selectionModel.isSelectedIndex(Math.max(listModel.getSize() - 1, 0));
			setDisabled(moveUp, nothingSelectedInSelection || isFirstElementSelected);
			setDisabled(moveDown, nothingSelectedInSelection || isLastElementSelected);

			setDisabled(remove, hasFixedSelectionOnly());

			boolean nothingSelected = !containsNonFixedOptions(
				getAllSelectableItems(listModel, selectionModel));
			setDisabled(removeAll, nothingSelected);

			boolean nothingSelectedInOptions = optionList.getSelectionModel().isSelectionEmpty();
			setDisabled(add, nothingSelectedInOptions);

			setDisabled(addAll, hasFixedOptionsOnly());
		}
	}

	private boolean hasFixedSelectionOnly() {
		List<?> selection = selectionList.getSelectionList();
		return !containsNonFixedOptions(selection);
	}

	private boolean hasFixedOptionsOnly() {
		List<?> selection = ListModelUtilities.asList(optionList.getListModel());
		return !containsNonFixedOptions(selection);
	}

	@SuppressWarnings("unchecked")
	private boolean containsNonFixedOptions(List<?> selection) {
		if (!selection.isEmpty()) {
			Filter<? super Object> fixedOptionsFilter = _targetSelectField.getFixedOptionsNonNull();
			for (Object selectedObject : selection) {
				if (!fixedOptionsFilter.accept(selectedObject)) {
					return true;
				}
			}
		}
		return false;
	}

	private void setDisabled(CommandField button, boolean disabled) {
		if (button != null) {
			button.setDisabled(disabled);
		}
	}

	private CommandField addButton(String name, String label, ModifyMode mode) {
		CommandField field = FormFactory.newCommandField(name, new ListModifySelection(this, mode));
		field.setLabel(label);
		field.setCssClasses(MOVE_OPTION_CSS_CLASS);
		addMember(field);

		return field;
	}

	private void addAcceptEnableListener() {
		selectionList.getListModel().addListDataListener(new ListDataAdapter() {

			private List oldSelection = ListModelUtilities.asList(selectionList.getListModel());

			@Override
			protected void listChanged(ListDataEvent e) {
				boolean unmodified =
					CollectionUtil.equals(oldSelection, ListModelUtilities.asList(selectionList.getListModel()));
				updateUnmodifiedState(unmodified);
			}
		});
	}

	protected void updateUnmodifiedState(boolean unmodified) {
		if (!getSelectField().hasValue()) {
			accept.setExecutable();
			return;
		}
		if (unmodified) {
			accept.setNotExecutable(I18NConstants.POPUP_SELECT_SUBMIT_NO_CHANGE);
		} else {
			accept.setExecutable();
		}
	}

	/**
	 * Initialises this {@link SelectorContext}.
	 * 
	 * @param targetSelectField
	 *        the {@link SelectField} modified by this {@link SelectorContext}
	 * @param optionsPerPage
	 *        number of options shown on a page or {@link #ALL_OPTIONS_ON_ONE_PAGE} to indicate that
	 *        all options are shown on one page
	 */
	protected abstract void init(SelectField targetSelectField, int optionsPerPage);
	
	public final boolean isMultiSelect() {
		return _targetSelectField.isMultiple();
	}
	
	public final SelectField getSelectField() {
		return this._targetSelectField;
	}
	
	public FormGroup getButtons() {
		return _buttons;
	}

	public StringField getPattern() {
		return pattern;
	}
	
	public ListField getOptionList() {
		return optionList;
	}

	public ListField getSelectionList() {
		assert isMultiSelect() : 
			"In single selection, the selected options handled by the selection model of the options list.";
		return selectionList;
	}
	
	protected Filter createPatternFilter(SelectField targetSelectField, String somePattern) {
		if (somePattern.length() == 0) {
			return FilterFactory.trueFilter();
		}
		
		Locale userLocale = TLContext.getLocale();
		Filter f;
		if (targetSelectField.matchSubstring()) {
			f = new SubstringFilter(somePattern.toLowerCase(userLocale));
		} else {
			f = new WordPrefixFilter(somePattern.toLowerCase(userLocale));
		}
		return 
			new LabelFilter(
				new LowerCaseLabelProvider(optionLabels, userLocale), f);
	}
	
	/**
	 * Creates a list of {@link Page}s for the given list of objects.
	 * 
	 * @param list
	 *        The objects to create pages for.
	 * 
	 * @see #createPageList(ListModel, int, Comparator, LabelProvider)
	 */
	protected static List<Page> createPageList(final List<?> list, int elementsPerPage,
			Comparator<Object> elementComparator, LabelProvider labels) {
		return createPageList(new AbstractListModel() {
			@Override
			public Object getElementAt(int index) {
				return list.get(index);
			}

			@Override
			public int getSize() {
				return list.size();
			}
		}, elementsPerPage, elementComparator, labels);
	}

	/**
	 * Creates a list of {@link Page}s for the given objects.
	 * 
	 * <p>
	 * The pages represent a partition of the list
	 * </p>
	 * 
	 * @param listModel
	 *        The objects to create pages for.
	 * @param elementsPerPage
	 *        Number of elements that are displayed at each page (maximal).
	 * @param elementComparator
	 *        {@link Comparator} for the list elements.
	 * @param labels
	 *        {@link LabelProvider} of the list elements.
	 */
	protected static List<Page> createPageList(ListModel listModel, int elementsPerPage,
			Comparator<Object> elementComparator, LabelProvider labels) {
		int listSize = listModel.getSize();
		assert listSize >= 0 : "Listmodel has negative size.";
		if (listSize == 0) {
			/* There are no entries in the base list model, but at least one page (with empty
			 * contents) is required. */
			return SINGLE_EMPTY_PAGE_LIST;
		}
		if (elementsPerPage < 1) {
			/* There are options. If there are less than one element on each page there are infinite
			 * pages. */
			throw new IllegalArgumentException("There must at least one element on the page.");
		}
		// ensure that pageCnt is always >= 0
		int pageCnt = (listSize + elementsPerPage - 1) / elementsPerPage;
		ArrayList<Page> result = new ArrayList<>(pageCnt);
		
		for (int pageStartIndex = 0; pageStartIndex < listSize; pageStartIndex += elementsPerPage) {
			int pageStopIndex = Math.min(listSize, pageStartIndex + elementsPerPage);
			int pageSize = pageStopIndex - pageStartIndex;
			Object first = listModel.getElementAt(pageStartIndex);
			Object last = listModel.getElementAt(pageStopIndex - 1);
			DefaultPage page = new DefaultPage(pageStartIndex, pageSize, first, last, elementComparator, labels);
			result.add(page);
		}
		
		return result;
	}

	public final void addSelection(List delta) {
		boolean selectDeltaItems = true;
		addSelection(delta, selectDeltaItems);
	}

	private void addSelection(List delta, boolean selectDeltaItems) {
		unregisterListListeners();
		internalAddSelection(delta);
		registerListListeners();
		if (selectDeltaItems) {
			setFieldSelection(getSelectionList(), delta);
		}
		updateButtonsState();
	}

	protected abstract void internalAddSelection(List delta);

	public final void removeSelection(List delta) {
		boolean selectDeltaItems = true;
		removeSelection(delta, selectDeltaItems);
	}

	private void removeSelection(List delta, boolean selectDeltaItems) {
		unregisterListListeners();
		internalRemoveSelection(delta);
		registerListListeners();
		if (selectDeltaItems) {
			setFieldSelection(getOptionList(), delta);
		}
		updateButtonsState();
	}

	public abstract void internalRemoveSelection(List delta);
	
	/**
	 * Sets the selection list to it's default value, defined by the underlying {@link SelectField}.
	 */
	@SuppressWarnings({ "rawtypes", "hiding" })
	public void restoreDefaultSelection() {
		List<?> defaultSelection = (List<?>) _targetSelectField.getDefaultValue();
		DefaultListModel selectionList = (DefaultListModel) getSelectionList().getListModel();
		DefaultListModel optionList = (DefaultListModel) getOptionList().getListModel();
		getSelectionList().getSelectionModel().clearSelection();
		removeAllFromSelection();
		ListModelUtilities.replaceAll(selectionList, defaultSelection);
		ListModelUtilities.removeElements(optionList, defaultSelection);
	}

	public void moveSelection(int direction) {
		unregisterListListeners();
		ListModelUtilities.moveSelection(selectionList, direction);
		registerListListeners();
		updateButtonsState();
	}

	private void registerListListeners() {
		optionList.getSelectionModel().addListSelectionListener(_selectButtonUpdater);
		optionList.getListModel().addListDataListener(_listButtonUpdater);
		selectionList.getSelectionModel().addListSelectionListener(_selectButtonUpdater);
		selectionList.getListModel().addListDataListener(_listButtonUpdater);
	}

	private void unregisterListListeners() {
		optionList.getSelectionModel().removeListSelectionListener(_selectButtonUpdater);
		optionList.getListModel().removeListDataListener(_listButtonUpdater);
		selectionList.getSelectionModel().removeListSelectionListener(_selectButtonUpdater);
		selectionList.getListModel().removeListDataListener(_listButtonUpdater);
	}

	private void setFieldSelection(ListField listField, List<?> addedOptions) {
		ListSelectionModel selectionModel = listField.getSelectionModel();
		ListModel<?> listModel = listField.getListModel();
		HashSet<?> movedOptions = new HashSet<Object>(addedOptions);
		for (int i = 0; i < listModel.getSize(); i++) {
			if (movedOptions.contains(listModel.getElementAt(i))) {
				selectionModel.addSelectionInterval(i, i);
			}
		}

	}

	public void addAllToSelection() {
		boolean selectDeltaItems = false;
		addSelection(getAllSelectableItems(getOptionList().getListModel(), getOptionList().getSelectionModel()),
			selectDeltaItems);
	}

	public void removeAllFromSelection() {
		boolean selectDeltaItems = false;
		removeSelection(
			getAllSelectableItems(getSelectionList().getListModel(), getSelectionList().getSelectionModel()),
			selectDeltaItems);
	}
	
	/**
	 * Returns a {@link List} of all items in the list model which are
	 * selectable.
	 */
	private static List<?> getAllSelectableItems(ListModel listModel, ListSelectionModel selectionModel) {
		if (selectionModel instanceof RestrictedListSelectionModel) {
			RestrictedListSelectionModel resSelectionModel = (RestrictedListSelectionModel) selectionModel;
			ArrayList<Object> result = new ArrayList<>(listModel.getSize());
			for (int cnt = listModel.getSize(), n = 0; n < cnt; n++) {
				if (resSelectionModel.isSelectableIndex(n)) {
					result.add(listModel.getElementAt(n));
				}
			}
			return result;
		} else {
			return ListModelUtilities.asList(listModel);
		}
	}

	public CommandModel getAcceptCommand() {
		return (CommandField) ((FormGroup) getMember(BUTTONS)).getMember(ACCEPT_SELECTION);
	}
	
	public CommandModel getAddCommand() {
		return (CommandField) getMember(ADD_TO_SELECTION);
	}

	Command getCloseCommand() {
		return closeAction;
	}

	void clearPattern() {
		pattern.setValue("");
	}

}