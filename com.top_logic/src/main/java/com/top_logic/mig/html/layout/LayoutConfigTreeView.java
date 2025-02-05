/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.col.FilterIterator;
import com.top_logic.basic.col.TreeView;
import com.top_logic.mig.html.layout.LayoutComponent.Config;

/**
 * TreeView for {@link LayoutComponent.Config}.
 * 
 * <p>
 * The children of a {@link Config} are the {@link Config#getDialogs() dialogs} and (if
 * {@link LayoutContainer.Config layout container}) the
 * {@link LayoutContainer.Config#getChildConfigurations() child configurations}.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class LayoutConfigTreeView implements TreeView<Config> {

	/** Singleton {@link LayoutConfigTreeView} instance. */
	public static final LayoutConfigTreeView INSTANCE = new LayoutConfigTreeView();

	private LayoutConfigTreeView() {
		// singleton instance
	}

	@Override
	public boolean isLeaf(Config node) {
		List<Config> dialogs = node.getDialogs();
		if (!dialogs.isEmpty()) {
			return false;
		}
		if (node instanceof LayoutContainer.Config) {
			return ((LayoutContainer.Config) node).getChildConfigurations().isEmpty();
		} else {
			return true;
		}
	}

	@Override
	public Iterator<? extends Config> getChildIterator(Config node) {
		Iterator<? extends Config> result = configChildren(node);

		// Note: In case of errors in the configuration, some children could not have been loaded.
		// This should not completely crash the application, but would do so if returning null
		// elements (that cannot be checked for being leafs above).
		return new FilterIterator<>(result, Objects::nonNull);
	}

	private Iterator<? extends Config> configChildren(Config node) {
		List<Config> dialogs = node.getDialogs();
		if (node instanceof LayoutContainer.Config) {
			List<? extends Config> children = ((LayoutContainer.Config) node).getChildConfigurations();
			int numberChildren = children.size();
			switch (numberChildren) {
				case 0:
					return dialogs.iterator();
				default: {
					int numberDialogs = dialogs.size();
					switch (numberDialogs) {
						case 0:
							return children.iterator();
						default:
							Config[] tmp = new Config[numberChildren + numberDialogs];
							int i=0;
							for (Config config : children) {
								tmp[i++] = config;
							}
							for (Config config : dialogs) {
								tmp[i++] = config;
							}
							return ArrayUtil.iterator(tmp);
					}

				}
			}
		} else {
			return dialogs.iterator();
		}
	}

	@Override
	public boolean isFinite() {
		return true;
	}

}

