/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.component;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import com.top_logic.basic.col.DescendantDFSIterator;
import com.top_logic.layout.WrappedModel;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.FormMemberVisitor;
import com.top_logic.layout.form.control.DeckPaneModel;
import com.top_logic.layout.form.model.AbstractFormMemberVisitor;
import com.top_logic.layout.form.model.DeckField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormMemberTreeView;
import com.top_logic.layout.form.model.FormTree;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.form.model.TreeField;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.model.PagingModel;
import com.top_logic.layout.tree.model.ExpansionState;
import com.top_logic.layout.tree.model.TreeTableModel;
import com.top_logic.layout.tree.model.TreeUIModel;

/**
 * {@link FormMemberVisitor} that extracts transient state from {@link FormMember}s that must
 * survive a mode switch in an edit component.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FormStateBuilder extends AbstractFormMemberVisitor<Void, Void> {

	/**
	 * The state that can be re-applied to a newly built {@link FormContainer} that is structurally
	 * equivalent to the container, from which the state was extracted.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static abstract class FormState {
		/**
		 * Applies this recorded state to the given container.
		 */
		public abstract void applyTo(FormContainer context);
	}

	List<FormState> _states = new ArrayList<>();

	/**
	 * Retrieve the created state.
	 * 
	 * <p>
	 * This {@link FormStateBuilder} must not be reused, after this method is called.
	 * </p>
	 */
	public FormState getState() {
		final List<FormState> states = _states;
		_states = null;

		return new FormState() {
			@Override
			public void applyTo(FormContainer context) {
				for (FormState memberState : states) {
					memberState.applyTo(context);
				}
			}
		};
	}

	@Override
	public Void visitTreeField(TreeField member, Void arg) {
		recordTreeField(member);
		return null;
	}

	private void recordTreeField(TreeField member) {
		TreeUIModel treeModel = member.getTreeModel();
		String qualifiedName = member.getQualifiedName();
		addState(new TreeState(qualifiedName, treeModel) {
			@Override
			protected boolean isCompatible(FormMember correspondingMember) {
				return correspondingMember instanceof TreeField;
			}

			@Override
			protected TreeUIModel extractTreeModel(FormMember correspondingMember) {
				return ((TreeField) correspondingMember).getTreeModel();
			}
		});
	}

	@Override
	public Void visitFormTree(FormTree member, Void arg) {
		recordFormField(member);
		return null;
	}

	private void recordFormField(FormTree member) {
		TreeUIModel treeModel = member.getTreeApplicationModel();
		String qualifiedName = member.getQualifiedName();
		addState(new TreeState(qualifiedName, treeModel) {
			@Override
			protected boolean isCompatible(FormMember correspondingMember) {
				return correspondingMember instanceof FormTree;
			}

			@Override
			protected TreeUIModel extractTreeModel(FormMember correspondingMember) {
				return ((FormTree) correspondingMember).getTreeApplicationModel();
			}
		});
	}

	@Override
	public Void visitSelectField(SelectField member, Void arg) {
		recordSelectField(member);
		return null;
	}

	private void recordSelectField(SelectField member) {
		TableData tableData = member.getTableDataOrNull();
		if (tableData == null) {
			return;
		}

		TableViewModel viewModel = tableData.getViewModel();
		if (viewModel == null) {
			return;
		}

		PagingModel pagingModel = viewModel.getPagingModel();
		int currentPage = pagingModel.getPage();
		if (currentPage > 0) {
			int currentPageSizeSpec = pagingModel.getPageSizeSpec();
			addState(new TableState(member.getQualifiedName(), currentPage, currentPageSizeSpec) {
				@Override
				protected boolean isCompatible(FormMember correspondingMember) {
					return correspondingMember instanceof SelectField;
				}

				@Override
				protected TableData extractTableData(FormMember correspondingMember) {
					return ((SelectField) correspondingMember).getTableData();
				}
			});
		}
	}

	@Override
	public Void visitTableField(TableField member, Void arg) {
		recordTableField(member);
		return null;
	}

	private void recordTableField(TableField member) {
		TableViewModel viewModel = member.getViewModel();
		if (viewModel == null) {
			return;
		}

		PagingModel pagingModel = viewModel.getPagingModel();
		final int currentPage = pagingModel.getPage();
		if (currentPage > 0) {
			int currentPageSizeSpec = pagingModel.getPageSizeSpec();
			addState(new TableState(member.getQualifiedName(), currentPage, currentPageSizeSpec) {
				@Override
				protected boolean isCompatible(FormMember correspondingMember) {
					return correspondingMember instanceof TableField;
				}

				@Override
				protected TableData extractTableData(FormMember correspondingMember) {
					return (TableField) correspondingMember;
				}
			});
		}

		TableModel model = unwrap(member.getTableModel());
		if (model instanceof TreeTableModel) {
			TreeUIModel<?> tree = ((TreeTableModel) model).getTreeModel();

			addState(new TreeState(member.getQualifiedName(), tree) {
				@Override
				protected boolean isCompatible(FormMember correspondingMember) {
					if (!(correspondingMember instanceof TableField)) {
						return false;
					}
					TableModel correspondingModel = unwrap(((TableField) correspondingMember).getTableModel());
					return correspondingModel instanceof TreeTableModel;
				}

				@Override
				protected TreeUIModel extractTreeModel(FormMember correspondingMember) {
					TableModel correspondingModel = unwrap(((TableField) correspondingMember).getTableModel());
					TreeUIModel<?> correspondingTree = ((TreeTableModel) correspondingModel).getTreeModel();
					return correspondingTree;
				}
			});
		}
	}

	static TableModel unwrap(TableModel tableModel) {
		if (tableModel instanceof WrappedModel) {
			return unwrap((TableModel) ((WrappedModel) tableModel).getWrappedModel());
		}
		return tableModel;
	}

	@Override
	public Void visitFormMember(FormMember member, Void arg) {
		// Ignore.
		return null;
	}

	@Override
	public Void visitFormContainer(FormContainer member, Void arg) {
		if (member instanceof DeckField) {
			recordDeckField((DeckField) member);
		}
		return super.visitFormContainer(member, arg);
	}

	private void recordDeckField(DeckField member) {
		addState(new MemberState(member.getQualifiedName()) {

			DeckPaneModel _origDeck = member.getModel();

			@Override
			protected boolean isCompatible(FormMember correspondingMember) {
				return correspondingMember instanceof DeckField;
			}

			@Override
			public void directApply(FormMember correspondingMember) {
				int selectedIndex = _origDeck.getSelectedIndex();
				DeckPaneModel model = ((DeckField) correspondingMember).getModel();
				if (selectedIndex < model.getSelectableCards().size()) {
					model.setSelectedIndex(selectedIndex);
				}
			}

		});
	}

	private void addState(FormState state) {
		_states.add(state);
	}

	public static FormState extractState(FormContext context) {
		FormStateBuilder builder = new FormStateBuilder();
	
		DescendantDFSIterator it = new DescendantDFSIterator(FormMemberTreeView.INSTANCE, context);
		while (it.hasNext()) {
			FormMember member = (FormMember) it.next();
	
			member.visit(builder, null);
		}
	
		return builder.getState();
	}

	abstract static class MemberState extends FormState {
	
		private String _qualifiedName;
	
		protected MemberState(String qualifiedName) {
			_qualifiedName = qualifiedName;
		}
	
		@Override
		public void applyTo(FormContainer context) {
			FormMember member;
			try {
				member = FormContext.getMemberByQualifiedName(context, _qualifiedName);
			} catch (NoSuchElementException ex) {
				// Ignore member to does no longer exist.
				return;
			}
	
			if (isCompatible(member)) {
				directApply(member);
			}
		}
	
		protected abstract boolean isCompatible(FormMember correspondingMember);

		public abstract void directApply(FormMember correspondingMember);
	}

	abstract class TableState extends MemberState {
	
		private final int _currentPage;

		private final int _currentPageSizeSpec;
	
		protected TableState(String qualifiedName, int currentPage, int currentPageSizeSpec) {
			super(qualifiedName);
	
			_currentPage = currentPage;
			_currentPageSizeSpec = currentPageSizeSpec;
		}
	
		@Override
		public final void directApply(FormMember correspondingMember) {
			applyTableDataState(extractTableData(correspondingMember));
		}
	
		protected abstract TableData extractTableData(FormMember correspondingMember);
	
		private void applyTableDataState(TableData otherTableData) {
			if (otherTableData == null) {
				return;
			}
	
			TableViewModel otherViewModel = otherTableData.getViewModel();
			if (otherViewModel == null) {
				return;
			}
	
			PagingModel pagingModel = otherViewModel.getPagingModel();

			// Note: The current page size must be stored transiently even if it is also stored in
			// the personal configuration: Because the personal table configuration is applied after
			// the transient form state, the current page could not be restored, if the default page
			// size is smaller than the currently chosen page size without also restoring the page
			// size.
			pagingModel.setPageSizeSpec(_currentPageSizeSpec);

			pagingModel.setPage(_currentPage);
		}
	
	}

	static abstract class TreeState extends MemberState {
	
		private ExpansionState _expansion;
	
		protected TreeState(String qualifiedName, TreeUIModel treeModel) {
			super(qualifiedName);
	
			Object root = treeModel.getRoot();
			if (treeModel.isExpanded(root) || (!treeModel.isRootVisible())) {
				_expansion = ExpansionState.createExpansionState(treeModel, root);
			}
		}
	
		@Override
		public void directApply(FormMember correspondingMember) {
			applyTreeModelState(extractTreeModel(correspondingMember));
		}
	
		private void applyTreeModelState(TreeUIModel treeModel) {
			if (_expansion != null) {
				_expansion.apply(treeModel, treeModel.getRoot());
			}
		}
	
		protected abstract TreeUIModel extractTreeModel(FormMember correspondingMember);
	
	}

}
