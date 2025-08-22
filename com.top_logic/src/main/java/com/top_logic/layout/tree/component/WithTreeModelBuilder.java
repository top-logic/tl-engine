/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.component;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.func.Function1;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;

/**
 * Configuration mix-in that declares a {@link TreeModelBuilder} implementation.
 */
@Abstract
public interface WithTreeModelBuilder extends ConfigurationItem {

	/**
	 * @see #getModelBuilder()
	 */
	String MODEL_BUILDER = "modelBuilder";

	/**
	 * Algorithm creating the tree structure to be displayed.
	 */
	@Name(MODEL_BUILDER)
	@Label("Tree builder")
	@Mandatory
	@Options(fun = AllInAppImplementations.class)
	PolymorphicConfiguration<? extends TreeModelBuilder<?>> getModelBuilder();

	/**
	 * Whether a corresponding selection model can use tree-semantics allowing whole sub-trees to be
	 * selected.
	 */
	@Name("tree-semantics-supported")
	@Hidden
	@Derived(fun = TreeSemanticsSupported.class, args = @Ref(MODEL_BUILDER))
	boolean isTreeSemanticsSupported();

	/**
	 * Algorithm for analyzing a {@link TreeModelBuilder} configuration.
	 * 
	 * @see WithTreeModelBuilder#isTreeSemanticsSupported()
	 */
	class TreeSemanticsSupported extends Function1<Boolean, PolymorphicConfiguration<? extends TreeModelBuilder<?>>> {
		@Override
		public Boolean apply(PolymorphicConfiguration<? extends TreeModelBuilder<?>> arg) {
			if (arg instanceof TreeModelBuilderBase.Config<?> config) {
				// Only only in finite trees, whole subtrees can be selected.
				return config.isFinite();
			}
			return false;
		}
	}

}
