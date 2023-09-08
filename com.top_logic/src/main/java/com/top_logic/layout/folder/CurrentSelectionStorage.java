/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.folder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.top_logic.basic.Named;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.SingleSelectionModel;
import com.top_logic.layout.component.model.SingleSelectionListener;
import com.top_logic.layout.tree.breadcrumb.BreadcrumbData;
import com.top_logic.layout.tree.model.TLTreeNode;

/**
 * {@link SingleSelectionListener} that stores the currently selected path of a given
 * {@link BreadcrumbData} in the {@link PersonalConfiguration}.
 * 
 * <p>
 * The tree of the {@link BreadcrumbData} is expected to contain {@link Named}
 * {@link TLTreeNode#getBusinessObject() business objects}. Their names are used to identify the
 * nodes of the selected path.
 * </p>
 * 
 * @see #attach()
 * @see #restore()
 */
public class CurrentSelectionStorage implements SingleSelectionListener {

	private final BreadcrumbData _breadcrumbData;

	private String _key;

	/**
	 * Creates a {@link CurrentSelectionStorage}.
	 * 
	 * @param key
	 *        The key in the {@link PersonalConfiguration} to use for storage, see
	 *        {@link PersonalConfiguration#setJSONValue(String, Object)}.
	 */
	public CurrentSelectionStorage(BreadcrumbData breadcrumbData, String key) {
		_breadcrumbData = breadcrumbData;
		_key = key;
	}

	@Override
	public void notifySelectionChanged(SingleSelectionModel model, Object formerlySelectedObject,
			Object selectedObject) {
		PersonalConfiguration.getPersonalConfiguration().setJSONValue(_key, buildPath(selectedObject));
	}

	private List<String> buildPath(Object selectedObject) {
		if (selectedObject == null) {
			return Collections.emptyList();
		}

		@SuppressWarnings("unchecked")
		final List<?> path = _breadcrumbData.getTree().createPathToRoot(selectedObject);
		Collections.reverse(path);
		List<String> namePath = path.stream()
			.skip(1)
			.map(n -> ((TLTreeNode<?>) n).getBusinessObject())
			.map(n -> ((Named) n).getName())
			.collect(Collectors.toList());
		return namePath;
	}

	/**
	 * Attaches this {@link SingleSelectionListener} to the current selection of the
	 * {@link BreadcrumbData}.
	 * 
	 * @see #detach()
	 */
	public void attach() {
		currentFolder().addSingleSelectionListener(this);
	}

	/**
	 * Removes this as listener from the {@link BreadcrumbData}.
	 */
	public void detach() {
		currentFolder().removeSingleSelectionListener(this);
	}

	/**
	 * Restores the last stored selection.
	 */
	public void restore() {
		@SuppressWarnings("unchecked")
		List<String> path = (List<String>) PersonalConfiguration.getPersonalConfiguration().getJSONValue(_key);
		if (path != null && !path.isEmpty()) {
			Object current = _breadcrumbData.getTree().getRoot();
			for (String name : path) {
				current = getNamedContent(current, name);
			}
			currentFolder().setSingleSelection(current);
		}
	}

	private SingleSelectionModel currentFolder() {
		return _breadcrumbData.getDisplayModel();
	}

	private Object getNamedContent(Object current, String name) {
		TLTreeNode<?> currentNode = (TLTreeNode<?>) current;
		Optional<? extends TLTreeNode<?>> result = currentNode.getChildren()
			.stream()
			.filter(n -> name.equals(((Named) n.getBusinessObject()).getName()))
			.findFirst();
		TLTreeNode<?> child = result.orElse(null);
		return child != null ? child : current;
	}

}
