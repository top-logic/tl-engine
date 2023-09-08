/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.tree.compare;

import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.TreeView;
import com.top_logic.layout.form.decorator.CompareInfo;
import com.top_logic.layout.table.model.TableConfigurationProvider;

/**
 * Configuration, that holds information about two tree structures, that shall be compared.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class TreeCompareConfiguration {

	private TreeView<Object> _firstTree;

	private TreeView<Object> _secondTree;

	private Object _firstTreeRoot;

	private Object _secondTreeRoot;

	private TableConfigurationProvider _firstTableConfigProvider;

	private TableConfigurationProvider _secondTableConfigProvider;

	private Mapping<Object, ?> _identifierMapping;

	/**
	 * Create a new {@link TreeCompareConfiguration} with empty default columns and
	 * {@link CompareInfo#identifierMapping()} as default identifier mapping.
	 */
	public TreeCompareConfiguration() {
		_identifierMapping = CompareInfo.identifierMapping();
	}

	/**
	 * {@link TreeView}, that is the base of comparison
	 */
	public TreeView<Object> getFirstTree() {
		return _firstTree;
	}

	/**
	 * @see #getFirstTree()
	 */
	@SuppressWarnings("unchecked")
	public void setFirstTree(TreeView<?> firstTree) {
		_firstTree = (TreeView<Object>) firstTree;
	}

	/**
	 * {@link TreeView}, that is compared to {@link #getFirstTree()}
	 */
	public TreeView<Object> getSecondTree() {
		return _secondTree;
	}

	/**
	 * @see #getSecondTree()
	 */
	@SuppressWarnings("unchecked")
	public void setSecondTree(TreeView<?> secondTree) {
		_secondTree = (TreeView<Object>) secondTree;
	}

	/**
	 * root node of {@link #getFirstTree()}.
	 */
	public Object getFirstTreeRoot() {
		return _firstTreeRoot;
	}

	/**
	 * @see #getFirstTreeRoot()
	 */
	public void setFirstTreeRoot(Object firstTreeRoot) {
		_firstTreeRoot = firstTreeRoot;
	}

	/**
	 * root node of {@link #getSecondTree()}.
	 */
	public Object getSecondTreeRoot() {
		return _secondTreeRoot;
	}

	/**
	 * @see #getSecondTreeRoot()
	 */
	public void setSecondTreeRoot(Object secondTreeRoot) {
		_secondTreeRoot = secondTreeRoot;
	}

	/**
	 * Mapping, that will be applied to tree nodes of {@link #getFirstTree()} and
	 *         {@link #getSecondTree()}, before node identity comparison will be performed.
	 */
	public Mapping<Object, ?> getIdentifierMapping() {
		return _identifierMapping;
	}

	/**
	 * {@link TableConfigurationProvider}, that shall provide table configuration for the
	 *         first tree in compare table view.
	 */
	public TableConfigurationProvider getFirstTableConfigurationProvider() {
		return _firstTableConfigProvider;
	}

	/**
	 * @see #getFirstTableConfigurationProvider()
	 */
	public void setFirstTableConfigurationProvider(TableConfigurationProvider tableConfigProvider) {
		_firstTableConfigProvider = tableConfigProvider;
	}

	/**
	 * {@link TableConfigurationProvider}, that shall provide table configuration for the
	 *         second tree in compare table view.
	 */
	public TableConfigurationProvider getSecondTableConfigurationProvider() {
		return _secondTableConfigProvider;
	}

	/**
	 * @see #getSecondTableConfigurationProvider()
	 */
	public void setSecondTableConfigurationProvider(TableConfigurationProvider tableConfigProvider) {
		_secondTableConfigProvider = tableConfigProvider;
	}

	/**
	 * @see TreeCompareConfiguration#getIdentifierMapping()
	 */
	public void setIdentifierMapping(Mapping<Object, ?> identifierMapping) {
		_identifierMapping = identifierMapping;
	}

}
