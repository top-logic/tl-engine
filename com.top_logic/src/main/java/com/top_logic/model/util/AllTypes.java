/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
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
import com.top_logic.layout.tree.model.TLTreeModel;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModelVisitor;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLProperty;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.TLTypeVisitor;
import com.top_logic.util.model.ModelService;

/**
 * Option provider function computing all available {@link TLType}s.
 * 
 * @see Options#fun()
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class AllTypes extends Function0<OptionModel<TLModelPart>> {

	@Override
	public OptionModel<TLModelPart> apply() {
		return options(tree(), modelFilter());
	}

	/**
	 * Providing a {@link TLTreeModel} of {@link TLType} options.
	 * 
	 * @param tree
	 *        Tree model of options.
	 * @param modelFilter
	 *        Filter, which determines, which nodes are selectable.
	 */
	protected OptionModel<TLModelPart> options(AllTypes.TypesTree tree, Filter<? super TLModelPart> modelFilter) {
		return new DefaultTreeOptionModel<>(tree, modelFilter);
	}

	/**
	 * The tree model of options.
	 * 
	 * @see TypesTree
	 */
	protected AllTypes.TypesTree tree() {
		return new TypesTree();
	}

	/**
	 * Filter, which determines, which nodes are selectable.
	 */
	protected Filter<? super TLModelPart> modelFilter() {
		return new IsType();
	}

	/**
	 * Tree model of {@link TLType} options accepted by {@link TypeFilter}.
	 * 
	 * Only {@link TLClass}es and {@link TLModule}s have children.
	 * 
	 * @see TypeFilter
	 *
	 * @author <a href=mailto:sfo@top-logic.com>sfo</a>
	 */
	public static class TypesTree extends AbstractTreeModel<TLModelPart>
			implements TLModelVisitor<List<? extends TLModelPart>, Void> {

		/**
		 * Visitor accepting all {@link TLType}s excluding {@link TLAssociation} and {@link TLClass}
		 * with an direct super class inside the given {@link TLModule}.
		 *
		 * @author <a href=mailto:sfo@top-logic.com>sfo</a>
		 */
		protected static final class TypeFilter implements TLTypeVisitor<Boolean, TLModule> {

			static final TLTypeVisitor<Boolean, TLModule> INSTANCE = new TypeFilter();

			@Override
			public Boolean visitPrimitive(TLPrimitive model, TLModule arg) {
				return true;
			}

			@Override
			public Boolean visitEnumeration(TLEnumeration model, TLModule arg) {
				return true;
			}

			@Override
			public Boolean visitClass(TLClass model, TLModule arg) {
				for (TLClass baseClass : model.getGeneralizations()) {
					if (baseClass.getModule() == arg) {
						return false;
					}
				}
				return true;
			}

			@Override
			public Boolean visitAssociation(TLAssociation model, TLModule arg) {
				return false;
			}
		}

		@Override
		public boolean isFinite() {
			return true;
		}

		@Override
		public TLModelPart getRoot() {
			return ModelService.getApplicationModel();
		}

		@Override
		public List<? extends TLModelPart> getChildren(TLModelPart parent) {
			return parent.visit(this, null);
		}

		@Override
		public List<? extends TLModelPart> visitModule(TLModule model, Void arg) {
			ArrayList<TLType> result = new ArrayList<>();
			for (TLType type : model.getTypes()) {
				if (!acceptType(model, type)) {
					continue;
				}
				result.add(type);
			}
			Collections.sort(result, LabelComparator.newCachingInstance());
			return result;
		}

		/**
		 * Checks if the given {@link TLType} inside the given {@link TLModule} is accepted by
		 * {@link TypeFilter}.
		 */
		protected boolean acceptType(TLModule module, TLType type) {
			return type.visitType(TypeFilter.INSTANCE, module);
		}

		@Override
		public List<TLModelPart> visitClass(TLClass model, Void arg) {
			ArrayList<TLModelPart> result = new ArrayList<>();
			subClasses:
			for (TLClass subClass : model.getSpecializations()) {
				if (!inSameModule(subClass, model)) {
					// Do not show module-spawning specialization.
					continue subClasses;
				}
				for (TLClass superClass : subClass.getGeneralizations()) {
					if (inSameModule(superClass, model)) {
						if (superClass != model) {
							// Only structure types according to their primary
							// generalization.
							continue subClasses;
						} else {
							// Accept sub-class.
							break;
						}
					}
				}
				result.add(subClass);
			}
			Collections.sort(result, LabelComparator.newCachingInstance());
			return result;
		}

		@Override
		public List<? extends TLModelPart> visitModel(TLModel model, Void arg) {
			ArrayList<TLModule> result = new ArrayList<>(model.getModules());
			Collections.sort(result, LabelComparator.newCachingInstance());
			return result;
		}

		@Override
		public List<? extends TLModelPart> visitPrimitive(TLPrimitive model, Void arg) {
			return Collections.emptyList();
		}

		@Override
		public List<? extends TLModelPart> visitEnumeration(TLEnumeration model, Void arg) {
			return Collections.emptyList();
		}

		@Override
		public List<? extends TLModelPart> visitAssociation(TLAssociation model, Void arg) {
			return Collections.emptyList();
		}

		@Override
		public List<? extends TLModelPart> visitClassifier(TLClassifier model, Void arg) {
			return Collections.emptyList();
		}

		@Override
		public List<? extends TLModelPart> visitProperty(TLProperty model, Void arg) {
			return Collections.emptyList();
		}

		@Override
		public List<? extends TLModelPart> visitReference(TLReference model, Void arg) {
			return Collections.emptyList();
		}

		@Override
		public List<? extends TLModelPart> visitAssociationEnd(TLAssociationEnd model, Void arg) {
			return Collections.emptyList();
		}

		private boolean inSameModule(TLClass baseClass, TLClass subClass) {
			return subClass.getModule() == baseClass.getModule();
		}

		@Override
		public Object getBusinessObject(TLModelPart node) {
			return node;
		}

		@Override
		public boolean childrenInitialized(TLModelPart parent) {
			return true;
		}

		@Override
		public void resetChildren(TLModelPart parent) {
			// Ignore, not lazily initialized.
		}

		@Override
		public boolean isLeaf(TLModelPart node) {
			return getChildren(node).isEmpty();
		}

		@Override
		public TLModelPart getParent(TLModelPart node) {
			if (node instanceof TLModule) {
				return ((TLModule) node).getModel();
			}
			else if (node instanceof TLClass) {
				TLClass clazz = (TLClass) node;
				for (TLClass superClass : clazz.getGeneralizations()) {
					if (inSameModule(superClass, clazz)) {
						return superClass;
					}
				}
				return clazz.getModule();
			} else if (node instanceof TLType) {
				return ((TLType) node).getModule();
			} else if (node instanceof TLTypePart) {
				return ((TLTypePart) node).getOwner();
			} else {
				return null;
			}
		}
	}


	/**
	 * Filters objects based on their type.
	 * 
	 * Only those objects are accepted by this filter that are instances of {@link TLType}.
	 * 
	 * @author <a href=mailto:sfo@top-logic.com>sfo</a>
	 */
	public static class IsType implements Filter<Object> {
		@Override
		public boolean accept(Object anObject) {
			return anObject instanceof TLType;
		}
	}
}