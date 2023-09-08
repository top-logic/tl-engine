/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.folder.impl;

import java.util.ArrayList;
import java.util.Collection;

import com.top_logic.basic.Named;
import com.top_logic.common.folder.FolderDefinition;

/**
 * {@link FolderDefinition} representing a list of {@link Named}.
 * <p>
 * The {@link TransientFolderDefinition} is created with its own name, {@link Named} objects can be
 * added. Can be used for example to display a list of files.
 * </p>
 * 
 * @author <a href="mailto:fma@top-logic.com">fma</a>
 */
public class TransientFolderDefinition implements FolderDefinition {

	private final String name;

	private ArrayList<Named> contents;

	public TransientFolderDefinition(String aName) {
		name = aName;
		contents = new ArrayList<>();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Collection<Named> getContents() {
		return contents;
	}

	public void add(Named content) {
		contents.add(content);
	}

	@Override
	public boolean isLinkedContent(Named content) {
		return false;
	}

}
