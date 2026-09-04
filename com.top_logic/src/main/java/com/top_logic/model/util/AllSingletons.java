/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.func.Function0;
import com.top_logic.layout.LabelComparator;
import com.top_logic.layout.form.model.utility.DefaultTreeOptionModel;
import com.top_logic.layout.form.model.utility.OptionModel;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.tree.model.AbstractTreeModel;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLModuleSingleton;
import com.top_logic.model.TLModuleSingletons;
import com.top_logic.model.TLObject;
import com.top_logic.util.model.ModelService;

/**
 * Option provider delivering the singletons of all {@link TLModule}s.
 *
 * <p>
 * The options are presented as tree of the {@link TLModule}s that have singletons with those
 * singletons as leafs. Only the singletons are selectable, the modules are pure structure.
 * </p>
 *
 * @see Options#fun()
 * @see TLModelUtil#findSingleton(TLModule, String)
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class AllSingletons extends Function0<OptionModel<TLObject>> {

	@Override
	public OptionModel<TLObject> apply() {
		return new DefaultTreeOptionModel<>(new SingletonsTree(), new IsSingleton());
	}

	/**
	 * Tree model of {@link TLModule} options with their singletons as leafs.
	 */
	public static class SingletonsTree extends AbstractTreeModel<TLObject> {

		@Override
		public boolean isFinite() {
			return true;
		}

		@Override
		public TLObject getRoot() {
			return ModelService.getApplicationModel();
		}

		@Override
		public List<? extends TLObject> getChildren(TLObject parent) {
			if (parent instanceof TLModel model) {
				List<TLModule> result = new ArrayList<>();
				for (TLModule module : model.getModules()) {
					if (!module.getSingletons().isEmpty()) {
						result.add(module);
					}
				}
				Collections.sort(result, LabelComparator.newCachingInstance());
				return result;
			}
			if (parent instanceof TLModule module) {
				List<TLObject> result = new ArrayList<>();
				for (TLModuleSingleton link : module.getSingletons()) {
					result.add(link.getSingleton());
				}
				Collections.sort(result, LabelComparator.newCachingInstance());
				return result;
			}
			return Collections.emptyList();
		}

		@Override
		public boolean isLeaf(TLObject node) {
			return getChildren(node).isEmpty();
		}

		@Override
		public TLObject getParent(TLObject node) {
			if (node instanceof TLModule module) {
				return module.getModel();
			}
			TLModuleSingleton link = singletonLink(node);
			return link == null ? null : link.getModule();
		}

		@Override
		public Object getBusinessObject(TLObject node) {
			return node;
		}

		@Override
		public boolean childrenInitialized(TLObject parent) {
			return true;
		}

		@Override
		public void resetChildren(TLObject parent) {
			// Ignore, not lazily initialized.
		}

	}

	/**
	 * {@link Filter} accepting the singleton of some {@link TLModule}.
	 */
	public static class IsSingleton implements Filter<Object> {

		@Override
		public boolean accept(Object anObject) {
			return anObject instanceof TLObject object && singletonLink(object) != null;
		}

	}

	/**
	 * The singleton assignment of the given object, or <code>null</code>, if the given object is no
	 * singleton of a {@link TLModule}.
	 */
	protected static TLModuleSingleton singletonLink(TLObject object) {
		TLModel model = ModelService.getApplicationModel();
		return model.getQuery(TLModuleSingletons.class).getModuleAndName(object);
	}

}
