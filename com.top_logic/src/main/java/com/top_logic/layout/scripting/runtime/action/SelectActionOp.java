/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import static com.top_logic.basic.util.Utils.*;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.SingleSelectionModel;
import com.top_logic.layout.SingleSelectionModelProvider;
import com.top_logic.layout.channel.SelectionChannelSPI;
import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.scripting.action.SelectAction;
import com.top_logic.layout.scripting.action.SelectAction.SelectionChangeKind;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.recorder.ref.misc.SelectionRef;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.table.TableData;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.SelectionModelOwner;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Handles both kinds of selections: {@link Selectable} and {@link SelectionModel}
 * 
 * @author <a href=mailto:Jan.Stolzenburg@top-logic.com>Jan.Stolzenburg</a>
 */
public class SelectActionOp extends AbstractApplicationActionOp<SelectAction> {

	private Object _selection;

	private boolean _doSelect;

	/** {@link TypedConfiguration} constructor for {@link SelectActionOp}. */
	public SelectActionOp(InstantiationContext context, SelectAction config) {
		super(context, config);
	}

	@Override
	public Object processInternal(ActionContext actionContext, Object argument) {
		Object model = findModel(actionContext);
		_selection = getSelection(actionContext, model);
		_doSelect = getSelectionState(actionContext);
		if (model instanceof Selectable) {
			selectSelectable((Selectable) model);
		} else if (model instanceof SelectionModel) {
			selectSelectionModel((SelectionModel) model);
		} else if (model instanceof TableData) {
			selectTableData((TableData) model);
		} else if (model instanceof SelectionModelOwner) {
			selectSelectionModelOwner((SelectionModelOwner) model);
		} else if (model instanceof SingleSelectionModelProvider) {
			selectSingleSelectionModelProvider((SingleSelectionModelProvider) model);
		} else {
			throw new UnsupportedOperationException("The model is not supported for 'Select' actions. Supported are "
					+ Selectable.class + ", " + SelectionModelOwner.class + ", " + SingleSelectionModelProvider.class
				+ " and " + TableData.class + ". But the model is of type: "
				+ StringServices.getClassNameNullsafe(model));
		}
		/* Allow the GC to remove that object. This SelectActionOp might be hard referenced until
		 * _all_ scripted tests are finished. If _selection is large and there are many
		 * SelectActionOps, that might lead to problems. */
		_selection = null;
		return argument;
	}

	private Object findModel(ActionContext actionContext) {
		ModelName modelName = getConfig().getSelectionModelName();
		return ModelResolver.locateModel(actionContext, modelName);
	}

	private Object getSelection(ActionContext actionContext, Object model) {
		return actionContext.resolve(getSelectionRef().getSelectee(), model);
	}

	private Boolean getSelectionState(ActionContext actionContext) {
		return (Boolean) actionContext.resolve(getSelectionRef().getSelectionState());
	}

	private SelectionRef getSelectionRef() {
		return getConfig().getSelection();
	}

	private void selectSelectionModelOwner(SelectionModelOwner selectionModelOwner) {
		SelectionModel selectionModel = selectionModelOwner.getSelectionModel();
		selectSelectionModel(selectionModel);
	}

	private void selectSelectionModel(SelectionModel selectionModel) {
		if (_doSelect) {
			for (Object element : getSelectionSet()) {
				assertTrue("The object to select is not selectable! Object to select: "
					+ StringServices.getObjectDescription(_selection),
					selectionModel.isSelectable(element));
			}
		}
		applySelectionChange(selectionModel);

	}

	private void applySelectionChange(SelectionModel selectionModel) {
		switch (getConfig().getChangeKind()) {
			case LEGACY: {
				selectionModel.setSelected(_selection, _doSelect);
				checkIncrementalSelection(selectionModel);
				return;
			}
			case INCREMENTAL: {
				selectionModel.setSelected(_selection, _doSelect);
				checkIncrementalSelection(selectionModel);
				return;
			}
			case ABSOLUTE: {
				if (_doSelect) {
					selectionModel.setSelection(getSelectionSet());
				} else {
					selectionModel.clear();
				}
				checkAbsoluteSelection(selectionModel);
				return;
			}
		}
	}

	private void checkIncrementalSelection(SelectionModel selectionModel) {
		boolean isSelected = selectionModel.getSelection().contains(_selection);
		if (_doSelect) {
			assertTrue("Object was not selected. Expected: " + StringServices.getObjectDescription(_selection)
				+ "; Actual: " + StringServices.getObjectDescription(selectionModel.getSelection()), isSelected);
		} else {
			assertFalse("Object was not de-selected. Expected: " + StringServices.getObjectDescription(_selection)
				+ "; Actual: " + StringServices.getObjectDescription(selectionModel.getSelection()), isSelected);
		}
	}

	private void checkAbsoluteSelection(SelectionModel selectionModel) {
		Set<?> actualSelection = selectionModel.getSelection();
		if (_doSelect) {
			Set<?> expectedSelectionSet = getSelectionSet();
			assertEquals(
				"Selection was not Changed. Expected: " + StringServices.getObjectDescription(expectedSelectionSet)
					+ "; Actual: " + StringServices.getObjectDescription(actualSelection),
				expectedSelectionSet, actualSelection);
		} else {
			assertTrue("Selection was not cleared. Actual: " + StringServices.getObjectDescription(actualSelection),
				actualSelection.isEmpty());
		}
	}

	private Set<?> getSelectionSet() {
		return _selection instanceof Collection ? CollectionUtil.toSet((Collection<?>) _selection)
			: Collections.singleton(_selection);
	}

	private void selectSingleSelectionModelProvider(SingleSelectionModelProvider singleSelectionModelProvider) {
		assert _doSelect;
		assert getConfig().getChangeKind() != SelectionChangeKind.INCREMENTAL :
			"A change for a single selection cannot be incremental.";
		SingleSelectionModel selectionModel = singleSelectionModelProvider.getSingleSelectionModel();
		assertTrue("The object to select is not selectable! Object to select: "
			+ StringServices.getObjectDescription(_selection), selectionModel.isSelectable(_selection));
		selectionModel.setSingleSelection(_selection);
		Object actualSelection = selectionModel.getSingleSelection();
		assertEquals("Selection change failed!", _selection, actualSelection);
	}

	private void selectSelectable(Selectable selectable) {
		assert _doSelect;
		assert getConfig().getChangeKind() != SelectionChangeKind.INCREMENTAL :
			"Selectable has only a single selection. Therefore, a selection change cannot be incremental.";

		Object newSelection;
		if (selectable instanceof LayoutComponent
				&& SelectionChannelSPI.isMultiSelection((LayoutComponent) selectable)) {
			newSelection = CollectionUtil.singletonOrEmptySet(_selection);
		} else {
			newSelection = _selection;
		}
		selectable.setSelected(newSelection);

		assertEquals("Selection change failed!", newSelection, selectable.getSelected());
	}

	private void selectTableData(TableData tableData) {
		assert getConfig()
			.getChangeKind() != SelectionChangeKind.INCREMENTAL : "TableData only supports absolute selection changes. Therefore, a selection change cannot be incremental.";
		if (_doSelect) {
			Set<?> selectionCollection = asSet(_selection);
			assertAllElementsExist(tableData, selectionCollection);
			tableData.getSelectionModel().setSelection(selectionCollection);
			assertTrue("Selection change failed!",
				CollectionUtil.containsSame(selectionCollection, tableData.getSelectionModel().getSelection()));
		} else {
			assert _selection == ScriptingRecorder.NO_SELECTION;
			tableData.getSelectionModel().clear();
			assertTrue("Removing the selection failed!", tableData.getSelectionModel().getSelection().isEmpty());
		}
	}

	private Set<?> asSet(Object selection) {
		if (selection instanceof Collection) {
			return CollectionUtil.toSet((Collection<?>) selection);
		} else {
			return CollectionUtil.intoSet(selection);
		}
	}

	private void assertAllElementsExist(TableData tableData, Set<?> selectionCollection) {
		Collection<?> allKnownRows = tableData.getViewModel().getAllRows();
		Collection<?> unknownRows = new HashSet<>(selectionCollection);
		unknownRows.removeAll(allKnownRows);
		if (!unknownRows.isEmpty()) {
			StringBuilder errorMessage = new StringBuilder();
			errorMessage.append("Following table rows have not been found: ");
			boolean isNotFirst = false;
			for (Object unknownRow : unknownRows) {
				if (isNotFirst) {
					errorMessage.append(", ");
				} else {
					isNotFirst = true;
				}
				errorMessage.append(debug(unknownRow));
			}
			errorMessage.append("; ");
			errorMessage.append("Available rows: ");
			errorMessage.append(allKnownRows);
			throw new AssertionError(errorMessage);
		}
	}

	/**
	 * @see ApplicationAssertions#assertEquals(com.top_logic.basic.config.ConfigurationItem, String,
	 *      Object, Object)
	 */
	protected void assertEquals(String message, Object expected, Object actual) {
		ApplicationAssertions.assertEquals(getConfig(), message, expected, actual);
	}

	/**
	 * @see ApplicationAssertions#assertTrue(com.top_logic.basic.config.ConfigurationItem, String,
	 *      boolean)
	 */
	protected void assertTrue(String message, boolean value) {
		ApplicationAssertions.assertTrue(getConfig(), message, value);
	}

	/**
	 * @see ApplicationAssertions#assertFalse(com.top_logic.basic.config.ConfigurationItem, String,
	 *      boolean)
	 */
	protected void assertFalse(String message, boolean value) {
		ApplicationAssertions.assertFalse(getConfig(), message, value);
	}

}
