/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.meta.form.discover;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.kbbased.filtergen.Generator;
import com.top_logic.layout.form.model.utility.DefaultTreeOptionModel;
import com.top_logic.layout.form.model.utility.OptionModel;
import com.top_logic.layout.tree.model.AbstractTreeModel;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;

/**
 * Java {@link Generator} offering the objects of the value type of the edited attribute as a tree,
 * without any access check of its own.
 *
 * <p>
 * The root is the constant {@link #ROOT}, its children are the objects no other object holds in
 * its {@link #CHILDREN} reference, and the children of an object are the objects of its
 * {@link #CHILDREN} reference.
 * </p>
 */
public class TestTargetTreeGenerator implements Generator {

	/** The root node of the tree, no business object. */
	public static final String ROOT = "root";

	/** The reference holding the children of an object. */
	public static final String CHILDREN = "children";

	@Override
	public OptionModel<?> generate(EditContext editContext) {
		TLClass type = (TLClass) editContext.getValueType();
		return new DefaultTreeOptionModel<>(new TargetTree(type), node -> node instanceof TLObject, false);
	}

	private static final class TargetTree extends AbstractTreeModel<Object> {

		private final TLClass _type;

		TargetTree(TLClass type) {
			_type = type;
		}

		@Override
		public boolean isFinite() {
			return true;
		}

		@Override
		public Object getRoot() {
			return ROOT;
		}

		@Override
		public List<?> getChildren(Object parent) {
			if (ROOT.equals(parent)) {
				List<TLObject> all = MetaElementUtil.getAllInstancesOf(_type, TLObject.class);
				Set<Object> contained = new HashSet<>();
				for (TLObject object : all) {
					contained.addAll(children(object));
				}
				List<Object> result = new ArrayList<>(all);
				result.removeAll(contained);
				return result;
			}
			if (parent instanceof TLObject object) {
				return new ArrayList<>(children(object));
			}
			return Collections.emptyList();
		}

		private static Collection<?> children(TLObject object) {
			return (Collection<?>) object.tValueByName(CHILDREN);
		}

		@Override
		public boolean isLeaf(Object node) {
			return getChildren(node).isEmpty();
		}

		@Override
		public Object getParent(Object node) {
			if (ROOT.equals(node)) {
				return null;
			}
			for (TLObject object : MetaElementUtil.getAllInstancesOf(_type, TLObject.class)) {
				if (children(object).contains(node)) {
					return object;
				}
			}
			return ROOT;
		}

		@Override
		public Object getBusinessObject(Object node) {
			return node;
		}

		@Override
		public boolean childrenInitialized(Object parent) {
			return true;
		}

		@Override
		public void resetChildren(Object parent) {
			// Not lazily initialized.
		}

	}

}
