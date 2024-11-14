/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.selection;

import static com.top_logic.layout.form.selection.TreeSelectorContextUtil.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.ComparableComparator;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.basic.col.filter.SetFilter;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.ResourceRenderer;
import com.top_logic.layout.component.model.SelectionListener;
import com.top_logic.layout.form.FormMemberVisitor;
import com.top_logic.layout.form.I18NConstants;
import com.top_logic.layout.form.model.AbstractFormField;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.model.ConstantField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormFieldInternals;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.ListField;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.form.model.TreeField;
import com.top_logic.layout.form.model.utility.TreeOptionModel;
import com.top_logic.layout.form.selection.TreeModifySelection.ModifyMode;
import com.top_logic.layout.list.model.DefaultRestrictedListSelectionModel;
import com.top_logic.layout.list.model.ListModelUtilities;
import com.top_logic.layout.provider.CombinedResourceProvider;
import com.top_logic.layout.provider.LabelResourceProvider;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.layout.scripting.recorder.DynamicRecordable;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.tree.TreeRenderer;
import com.top_logic.layout.tree.model.DefaultStructureTreeUIModel;
import com.top_logic.layout.tree.model.TreeUIModel;
import com.top_logic.layout.tree.model.TreeUIModelUtil;
import com.top_logic.layout.tree.renderer.MarkupConfigurableTreeRenderer;
import com.top_logic.mig.html.DefaultReferencedSelectionModel;
import com.top_logic.mig.html.DefaultSingleSelectionModel;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.ReferencedSelectionModel;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.SelectionModelOwner;
import com.top_logic.util.Resources;

/**
 * Base class for {@link FormContext} implementations of a OptionTreeSelector view.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TreeSelectorContext extends FormContext implements DynamicRecordable {

	public static final String CONTEXT_NAME = "selector";
	
	public static final String OPTIONS_FIELD_NAME = "options";
	public static final String SELECTION_FIELD_NAME = "selection";

	public static final String PATTERN_FIELD_NAME = "pattern";

	private static final String MOVE_OPTION_CSS_CLASS = "moveOption";
	public static final String REMOVE_FROM_SELECTION = "removeFromSelection";
	public static final String REMOVE_ALL_FROM_SELECTION = "removeAllFromSelection";

	public static final String ADD_TO_SELECTION = "addToSelection";
	public static final String ADD_ALL_TO_SELECTION = "addAllToSelection";
	
	private static final String ACCEPT_ON_ENTER_MEMBER = "acceptMember";
	
	public static final String TITLE_FIELD_NAME = "titleFieldName";
	
	protected final LabelProvider optionLabels;
	
	private final boolean multiSelect;
	
	protected TreeField optionTree;
	protected ListField selectionList;

	private StringField pattern;
	
	private Comparator<Object> selectionOrder;
	
	private Command closeAction;
	
	final SelectField targetSelectField;

	private CommandField _addToSelection;

	private CommandField _removeFromSelection;

	private CommandField _addAllToSelection;

	private CommandField _removeAllFromSelection;

	private TreeSelectionListener _selectButtonUpdater;

	private ListDataAdapter _listButtonUpdater;

	private SelectDialogConfig _config;

	@SuppressWarnings("unchecked")
	public TreeSelectorContext(SelectField targetSelectField, Command closeAction, SelectDialogConfig config) {
		super(CONTEXT_NAME, ResPrefix.legacyPackage(SelectorContext.class));
		
		this.targetSelectField = targetSelectField;
		_config = config;
		
		String targetFieldLabel = targetSelectField.getLabel();
		
		createPatternField(targetFieldLabel);

		this.closeAction = closeAction;
		
		this.multiSelect = targetSelectField.isMultiple();
		this.optionLabels = targetSelectField.getOptionLabelProvider();
		selectionOrder = ((selectionOrder = targetSelectField.getOptionComparator()) != null) ? selectionOrder : ComparableComparator.INSTANCE;
		init();
		
		Resources resources = Resources.getInstance();
		
		// Check the contract of this class.
		if (multiSelect) {
			assert SELECTION_FIELD_NAME.equals(selectionList.getName());
			addMember(selectionList);
		}

		assert OPTIONS_FIELD_NAME.equals(optionTree.getName());
		addMember(optionTree);
		
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
			if (config.isLeftToRight()) {
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
		    
			_addToSelection = FormFactory.newCommandField(ADD_TO_SELECTION, new TreeModifySelection(this, ModifyMode.ADD));
			_addToSelection.setLabel(addLabel);
			_addToSelection.setCssClasses(MOVE_OPTION_CSS_CLASS);
			addMember(_addToSelection);
	
			_removeFromSelection = FormFactory.newCommandField(REMOVE_FROM_SELECTION, new TreeModifySelection(this, ModifyMode.REMOVE));
			_removeFromSelection.setLabel(removeLabel);
			_removeFromSelection.setCssClasses(MOVE_OPTION_CSS_CLASS);
			addMember(_removeFromSelection);

			_addAllToSelection = FormFactory.newCommandField(ADD_ALL_TO_SELECTION, new TreeModifySelection(this, ModifyMode.ADD_ALL));
			_addAllToSelection.setLabel(addAllLabel);
			_addAllToSelection.setCssClasses(MOVE_OPTION_CSS_CLASS);
			addMember(_addAllToSelection);
			
			_removeAllFromSelection = FormFactory.newCommandField(REMOVE_ALL_FROM_SELECTION, new TreeModifySelection(this, ModifyMode.REMOVE_ALL));
			_removeAllFromSelection.setLabel(removeAllLabel);
			_removeAllFromSelection.setCssClasses(MOVE_OPTION_CSS_CLASS);
			addMember(_removeAllFromSelection);

			_selectButtonUpdater = new TreeSelectionListener();

			_listButtonUpdater = new ListDataAdapter() {
				@Override
				protected void listChanged(ListDataEvent e) {
					updateButtonsState();
				}
			};

			registerSelectionListeners();
		}
		addMember(FormFactory.newCommandField(ACCEPT_ON_ENTER_MEMBER, new TreeAcceptSelection(this, targetSelectField, true)));
		
		createButtons();
		
		ConstantField title = new ConstantField(TITLE_FIELD_NAME, !AbstractFormField.IMMUTABLE) {
			
			@Override
			public Object visit(FormMemberVisitor v, Object arg) {
				return v.visitFormMember(this, arg);
			}
		};
		title.setLabel(resources.getString(I18NConstants.POPUP_SELECT_TITLE__FIELD.fill(targetFieldLabel)));
		addMember(title);
		
		updateButtonsState();
	}

	private void createButtons() {
		final CommandField acceptButton = createAcceptCommand();
		CommandField cancelButton = createCancelCommand();
		FormGroup buttons = SelectorContextUtil.createButtonGroup(acceptButton, cancelButton, getResources());
		addMember(buttons);
	}

	private CommandField createAcceptCommand() {
		Command command = new TreeAcceptSelection(this, targetSelectField, false);
		CommandField acceptButton = SelectorContextUtil.createAcceptButton(command);
		setExecutability(acceptButton, false);
		addSelectionChangedListener(acceptButton);

		return acceptButton;
	}

	private CommandField createCancelCommand() {
		CommandField cancelButton = SelectorContextUtil.createCancelButton(closeAction);

		return cancelButton;
	}

	@SuppressWarnings("unchecked")
	protected void init() {
		final Filter<Object> selectableOptionsFilter = getSelectableOptionsFilter(targetSelectField);
		
		Resources resources = Resources.getInstance();
		String targetFieldLabel = targetSelectField.getLabel();
		
		// Create a renderer that only renders plain labels, even if the
		// (converted) label provider would offer image, links or tool tips.
		ResourceProvider resourceProvider =
			LabelResourceProvider.toResourceProvider(targetSelectField.getOptionLabelProvider());
		Renderer<Object> optionLabelRenderer = ResourceRenderer.newResourceRenderer(resourceProvider,
			!ResourceRenderer.USE_LINK, !ResourceRenderer.USE_TOOLTIP, !ResourceRenderer.USE_IMAGE);
		
		// Definition of the selection list
		DefaultListModel displayedSelection = new DefaultListModel();
		ListSelectionModel selectedSelection =
			new DefaultRestrictedListSelectionModel(displayedSelection, selectableOptionsFilter);
		selectionList = FormFactory.newListField(SELECTION_FIELD_NAME, displayedSelection, selectedSelection);
		selectionList.setItemRenderer(optionLabelRenderer);
		selectionList.setLabel(resources.getString(I18NConstants.POPUP_SELECT_SELECTED__FIELD.fill(targetFieldLabel)));
		
		// Ensure that the list of selected options uses the same sort order
		// as the list of all options. This is necessary to have a newly
		// selected option inserted at a defined place.
		List<Object> selectedOptions =
			new ArrayList<>((List<?>) FormFieldInternals.getStoredValue(targetSelectField));
		if (!targetSelectField.hasCustomOrder()) {
			Collections.sort(selectedOptions, targetSelectField.getOptionComparator());
		}

		ListModelUtilities.replaceAll(displayedSelection, selectedOptions);
		
		optionTree = createOptionsTree(selectableOptionsFilter);
	}

	/**
	 * Creates the {@link TreeField} to select options from.
	 * 
	 * @param selectableOptionsFilter
	 *        Filter that accepts the options that can be selected.
	 */
	protected TreeField createOptionsTree(Filter<Object> selectableOptionsFilter) {
		// Definition of the option tree
		SelectionModel selectionModel;
		if (multiSelect) {
			selectionModel =
				new DefaultReferencedSelectionModel(selectableOptionsFilter, selectionList.getListModel(),
					SelectionModelOwner.NO_OWNER);
		} else {
			selectionModel = new DefaultSingleSelectionModel(selectableOptionsFilter, SelectionModelOwner.NO_OWNER);
			List<?> currentSelection = (List<?>) FormFieldInternals.getStoredValue(targetSelectField);
			if (!currentSelection.isEmpty()) {
				selectionModel.setSelected(currentSelection.get(0), true);
			}
		}
		TreeRenderer renderer = new MarkupConfigurableTreeRenderer(getTreeResourceProvider());

		TreeOptionModel<?> optionsAsTree = targetSelectField.getOptionsAsTree();
		TreeUIModel<?> treeModel =
			new DefaultStructureTreeUIModel<>(optionsAsTree.getBaseModel(), optionsAsTree.showRootNode());

		TreeField optionTree = FormFactory.newTreeField(OPTIONS_FIELD_NAME, treeModel, selectionModel, renderer);
		optionTree.setLabel(
			Resources.getInstance().getString(I18NConstants.POPUP_SELECT_OPTIONS__FIELD.fill(targetSelectField.getLabel())));
		ScriptingRecorder.annotateAsDontRecord(optionTree);
		// Markup referenced selection in option tree
		List<?> currentSelection = (List<?>) FormFieldInternals.getStoredValue(targetSelectField);

		expandNodes(treeModel, _config.getInitialTreeExpansionDepth(), currentSelection);
		return optionTree;
	}

	final void updateButtonsState() {
		if (isMultiSelect()) {
			ListModel<?> listModel = selectionList.getListModel();
			setDisabled(_removeFromSelection, hasFixedSelectionOnly());

			boolean nothingSelected = !containsNonFixedOptions(
				ListModelUtilities.asList(listModel));
			setDisabled(_removeAllFromSelection, nothingSelected);

			boolean nothingSelectedInOptions = optionTree.getSelectionModel().getSelection().isEmpty();
			setDisabled(_addToSelection, nothingSelectedInOptions);
			setDisabled(_addAllToSelection, hasFixedOptionsOnly());
		}
	}

	private void setDisabled(CommandField button, boolean disabled) {
		if (button != null) {
			button.setDisabled(disabled);
		}
	}

	private boolean hasFixedSelectionOnly() {
		List<?> selection = selectionList.getSelectionList();
		return !containsNonFixedOptions(selection);
	}

	@SuppressWarnings("unchecked")
	private boolean hasFixedOptionsOnly() {
		TreeUIModel<Object> model = optionTree.getTreeModel();
		if (!model.isFinite()) {
			return false;
		}

		List<?> treeLevelObjects;
		if (model.isRootVisible()) {
			treeLevelObjects = Collections.singletonList(model.getRoot());
		} else {
			treeLevelObjects = model.getChildren(model.getRoot());
		}
		return checkForFixedOptionsRecursively(model, treeLevelObjects);
	}

	private <N> boolean checkForFixedOptionsRecursively(TreeUIModel<N> model, List<? extends N> treeLevelObjects) {
		boolean hasFixedOptionsOnly = true;
		SelectionModel selectionModel = optionTree.getSelectionModel();

		for (int i = 0, size = treeLevelObjects.size(); i < size; i++) {
			N node = treeLevelObjects.get(i);
			if (selectionModel.isSelectable(node)) {
				return false;
			}
			hasFixedOptionsOnly &= checkForFixedOptionsRecursively(model, model.getChildren(node));
		}

		return hasFixedOptionsOnly;
	}

	@SuppressWarnings("unchecked")
	private boolean containsNonFixedOptions(List<?> selection) {
		if (!selection.isEmpty()) {
			Filter<? super Object> fixedOptionsFilter = targetSelectField.getFixedOptionsNonNull();
			for (Object selectedObject : selection) {
				if (!fixedOptionsFilter.accept(selectedObject)) {
					return true;
				}
			}
		}
		return false;
	}

	private void registerSelectionListeners() {
		optionTree.getSelectionModel().addSelectionListener(_selectButtonUpdater);
		selectionList.getSelectionModel().addListSelectionListener(_selectButtonUpdater);
		selectionList.getListModel().addListDataListener(_listButtonUpdater);
	}

	private void unregisterSelectionListeners() {
		optionTree.getSelectionModel().removeSelectionListener(_selectButtonUpdater);
		selectionList.getSelectionModel().removeListSelectionListener(_selectButtonUpdater);
		selectionList.getListModel().removeListDataListener(_listButtonUpdater);
	}

	private ResourceProvider getTreeResourceProvider() {
		LabelProvider optionLabelProvider = targetSelectField.getOptionLabelProvider();
		if (optionLabelProvider instanceof ResourceProvider) {
			return (ResourceProvider) optionLabelProvider;
		} else {
			return new CombinedResourceProvider(MetaResourceProvider.INSTANCE, optionLabelProvider);
		}
	}

	private void createPatternField(String targetFieldLabel) {
		this.pattern = FormFactory.newStringField(PATTERN_FIELD_NAME);
		Resources resources = Resources.getInstance();
		pattern.setLabel(resources.getString(I18NConstants.POPUP_SELECT_FILTER__FIELD.fill(targetFieldLabel)));
		addMember(pattern);
	}

	private Filter<Object> getSelectableOptionsFilter(SelectField targetSelectField) {
		Filter fixedOptions = FilterFactory.not(targetSelectField.getFixedOptionsNonNull());
		Filter validOptions = targetSelectField.getOptionsAsTree().getSelectableOptionsFilter();
		Filter selectedOptions = new SetFilter((List<?>) FormFieldInternals.getStoredValue(targetSelectField));
		return FilterFactory.and(fixedOptions, FilterFactory.or(validOptions, selectedOptions));
	}
	
	public final boolean isMultiSelect() {
		return multiSelect;
	}
	
	public TreeField getOptionTree() {
		return optionTree;
	}

	public ListField getSelection() {
		assert multiSelect : 
			"In single selection, the selected options handled by the selection model of the options list.";
		return selectionList;
	}
	
	public void addSelection(List<Object> delta) {
		
		unregisterSelectionListeners();

		// Clear option selection
		optionTree.getSelectionModel().clear();
		
		/*
		 *  Iterate over delta list and add selected options at proper position
		 *  in the selection list
		 */
		if(!delta.isEmpty()) {
			DefaultListModel allSelectedItems = (DefaultListModel)selectionList.getListModel();
			for (int i = 0, size = delta.size(); i < size; i++) {
				Object newlySelectedItem = delta.get(i);
				int insertPosition = allSelectedItems.getSize();
												
				while((insertPosition > 0) &&
					  (selectionOrder.compare(newlySelectedItem, allSelectedItems.get(insertPosition - 1)) < 0)) {
					insertPosition--;
				}
				
				allSelectedItems.add(insertPosition, newlySelectedItem);
			}
		}

		registerSelectionListeners();
		updateButtonsState();
	}

	public void removeSelection(List<Object> delta) {
		
		unregisterSelectionListeners();

		/*
		 * Iterate over delta list and remove its items from selection list
		 */
		if(!delta.isEmpty()) {
			DefaultListModel allSelectedItems = (DefaultListModel)selectionList.getListModel();
			for (int i = 0, size = delta.size(); i < size; i++) {				
				allSelectedItems.removeElement(delta.get(i));
			}
		}
		
		// Determine, if the model is multi-markup-capable
		SelectionModel globalModel = optionTree.getSelectionModel();
		if(globalModel instanceof ReferencedSelectionModel) {
			ReferencedSelectionModel selectionModel = (ReferencedSelectionModel) globalModel;
			
			// Remove markup of referenced selection in option tree
			selectionModel.propagateReferencedSelectionChange(new HashSet<>(delta));
		}

		registerSelectionListeners();
		updateButtonsState();
	}
	
	@SuppressWarnings("unchecked")
	public void addAllToSelection() {
		TreeUIModel model = optionTree.getTreeModel();
		List<Object> options = new LinkedList<>();
		List<?> treeLevelObjects;
		if (model.isRootVisible()) {
			treeLevelObjects = Collections.singletonList(model.getRoot());
		} else {
			treeLevelObjects = model.getChildren(model.getRoot());
		}
		traverseTree(model, treeLevelObjects, options);

		addSelection(options);
	}
	
	private <N> void traverseTree(TreeUIModel<N> model, List<? extends N> treeLevelObjects, List<Object> options) {
		SelectionModel selectionModel = optionTree.getSelectionModel();

		for (int i = 0, size = treeLevelObjects.size(); i < size; i++) {
			N node = treeLevelObjects.get(i);
			if (selectionModel.isSelectable(node)) {
				DefaultListModel allSelectedItems = (DefaultListModel) selectionList.getListModel();
				if (!allSelectedItems.contains(node)) {
					options.add(node);
					selectionModel.setSelected(node, true);
				}

				// Expand parent nodes
				TreeUIModelUtil.expandParents(model, node);
			}
			traverseTree(model, model.getChildren(node), options);
		}
	}
	
	public void removeAllFromSelection() {
		
		unregisterSelectionListeners();

		// Create standard list out of selection
		List<Object> selectionCopy = new LinkedList<>();
		DefaultListModel allSelectedItems = (DefaultListModel)selectionList.getListModel();
		for (int i = 0, size = allSelectedItems.size(); i < size; i++) {				
			selectionCopy.add(allSelectedItems.get(i));
		}
		
		// Clear selection list
		((DefaultListModel)selectionList.getListModel()).clear();
				
		// Determine, if the model is multi-markup-capable
		SelectionModel globalModel = optionTree.getSelectionModel();
		if(globalModel instanceof ReferencedSelectionModel) {
			ReferencedSelectionModel selectionModel = (ReferencedSelectionModel) globalModel;
			
			// Remove markup of referenced selection in option tree
			selectionModel.propagateReferencedSelectionChange(new HashSet<>(selectionCopy));
		}

		registerSelectionListeners();
		updateButtonsState();
	}
	
	public CommandModel getAcceptCommand() {
		return (CommandField) getButtons().getMember(SelectorContext.ACCEPT_SELECTION);
	}
	
	/**
	 * {@link FormGroup}, that holds the button {@link CommandField}s of this context.
	 */
	public FormGroup getButtons() {
		return (FormGroup) getMember(SelectorContext.BUTTONS);
	}

	Command getCloseCommand() {
		return closeAction;
	}
	
	private void addSelectionChangedListener(final CommandField accept) {
		if (multiSelect) {
			selectionList.getListModel().addListDataListener(new ListDataListener() {
				
				private List oldSelection = ListModelUtilities.asList(selectionList.getListModel());

				@Override
				public void intervalRemoved(ListDataEvent e) {
					checkChanged((ListModel) e.getSource());
				}

				@Override
				public void intervalAdded(ListDataEvent e) {
					checkChanged((ListModel) e.getSource());
				}

				@Override
				public void contentsChanged(ListDataEvent e) {
					checkChanged((ListModel) e.getSource());
				}
				
				private void checkChanged(ListModel model) {
					boolean changed = !CollectionUtil.equals(oldSelection, ListModelUtilities.asList(model));
					setExecutability(accept, changed);
				}

			});
		} else {
			optionTree.getSelectionModel().addSelectionListener(new SelectionListener() {

				private List<?> oldSelection = (List<?>) FormFieldInternals.getStoredValue(targetSelectField);

				@Override
				public void notifySelectionChanged(SelectionModel model, Set<?> formerlySelectedObjects,
						Set<?> selectedObjects) {
					boolean changed = !CollectionUtil.equals(oldSelection, CollectionUtil.toList(selectedObjects));
					setExecutability(accept, changed);
				}
			});
		}
	}
	
	void setExecutability(CommandField accept, boolean changed) {
		if (!getSelectField().hasValue()) {
			accept.setExecutable();
			return;
		}
		if (changed) {
			accept.setExecutable();
		} else {
			accept.setNotExecutable(I18NConstants.POPUP_SELECT_SUBMIT_NO_CHANGE);
		}
	}

	private boolean isExecutable(boolean changed) {
		return !getSelectField().hasValue() || changed;
	}

	public SelectField getSelectField() {
		return targetSelectField;
	}

	private final class TreeSelectionListener implements ListSelectionListener, SelectionListener {
		@Override
		public void valueChanged(ListSelectionEvent e) {
			updateButtonsState();
		}

		@Override
		public void notifySelectionChanged(SelectionModel model, Set<?> formerlySelectedObjects, Set<?> selectedObjects) {
			updateButtonsState();
		}
	}
}