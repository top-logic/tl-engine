/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.config.annotation.Inspectable;
import com.top_logic.layout.Control;
import com.top_logic.layout.ResourceView;
import com.top_logic.layout.SingleSelectionModel;
import com.top_logic.layout.basic.ControlRenderer;
import com.top_logic.layout.component.model.DefaultDeckPaneModel;
import com.top_logic.layout.component.model.FormDeckPaneModel;
import com.top_logic.layout.component.model.FormDeckPaneModel.FormDeckPaneCard;
import com.top_logic.layout.component.model.SingleSelectionListener;
import com.top_logic.layout.tabbar.TabBarModelAdapter;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.FormMemberVisitor;
import com.top_logic.layout.form.control.DeckPaneModel;
import com.top_logic.layout.form.control.DefaultSimpleCompositeControlRenderer;
import com.top_logic.layout.form.control.OnVisibleControl;
import com.top_logic.layout.form.template.AbstractFormFieldControlProvider;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.renderers.TabBarRenderer;
import com.top_logic.layout.scripting.action.CardSelection;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.tabbar.TabBarControl;
import com.top_logic.layout.tabbar.TabBarModel;
import com.top_logic.mig.html.layout.Card;
import com.top_logic.util.Utils;

/**
 * {@link FormContainer} to group a from into several tabs.
 * 
 * <p>
 * A {@link DeckField} is a {@link FormContainer} representing a {@link FormDeckPaneModel} that only
 * allows {@link FormContainer}s as selectable objects.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DeckField extends AbstractFormContainer {

	@Inspectable
	private final FormDeckPaneModel formDeckPane;
	
	/**
	 * Creates a {@link DeckField}.
	 *
	 * @param name
	 *        See {@link #getName()}.
	 * @param labelRessource
	 *        See {@link #getResources()}.
	 */
	public DeckField(String name, ResourceView labelRessource) {
		super(name, labelRessource);
		this.formDeckPane = new FormDeckPaneModel();
		this.setControlProvider(CP.INSTANCE);
	}

	@Override
	protected FormMember internalGetMember(String name) {
		List<Card> selectableCards = formDeckPane.getSelectableCards();
		for (int index = 0, size = selectableCards.size(); index < size; index++) {
			FormMember currentFormMember = (FormMember) selectableCards.get(index).getContent();
			if (Utils.equals(currentFormMember.getName(), name)) {
				return currentFormMember;
			}
		}
		return null;
	}

	/**
	 * The {@link DeckField} can just contain {@link FormContainer}, so an
	 * {@link IllegalArgumentException} is thrown if <code>member</code> is not a
	 * {@link FormContainer}.
	 * 
	 * @see FormContainer#addMember(FormMember)
	 * @see AbstractFormContainer#internalAddMember(FormMember)
	 */
	@Override
	protected void internalAddMember(FormMember member) {
		if (!(member instanceof FormContainer)) {
			throw new IllegalArgumentException("The member of DeckFields must be FormContainer!");
		}
		boolean emptyBefore = formDeckPane.getSelectableCards().isEmpty();
		Card newCard = new FormDeckPaneCard(member);
		formDeckPane.addSelectableCard(newCard);
		if (emptyBefore) {
			formDeckPane.setSelected(newCard, true);
		}
		((AbstractFormMember) member).setParent(this);
	}

	@Override
	protected boolean internalRemoveMember(FormMember member) {
		boolean result = formDeckPane.removeSelectableCard(new FormDeckPaneCard(member));
		if (result) {
			((AbstractFormMember) member).setParent(null);
		}
		return result;
	}

	@Override
	public int size() {
		return formDeckPane.getSelectableCards().size();
	}

	@Override
	public Iterator<? extends FormMember> getMembers() {
		return formDeckPane.getContentIterator();
	}

	@Override
	public boolean focus() {
		FormMember activeElement = (FormMember) formDeckPane.getSingleSelection();
		if (activeElement != null) {
			return activeElement.focus();
		} else {
			return false;
		}
	}

	@Override
	public <R, A> R visit(FormMemberVisitor<R, A> v, A arg) {
		return v.visitFormContainer(this, arg);
	}

	/**
	 * This method the {@link FormDeckPaneModel} this field based on.
	 */
	public FormDeckPaneModel getModel() {
		return formDeckPane;
	}

	/**
	 * {@link ControlProvider} creating a {@link TabBarControl} view of a {@link DeckField}.
	 */
	public static class CP extends AbstractFormFieldControlProvider {

		/**
		 * Property to link the adapter {@link DeckPaneModel} to {@link TabBarModel} to the
		 * displayed {@link DeckField}.
		 */
		@FrameworkInternal
		public static final Property<DeckFieldAdapter> TAB_BAR_MODEL =
			TypedAnnotatable.property(DeckFieldAdapter.class, "tabbarModel");

		/**
		 * Optional {@link TabBarRenderer} specified on a {@link DeckField} displayed by a
		 * {@link DeckField.CP}.
		 */
		@SuppressWarnings("unchecked")
		public static final Property<ControlRenderer<? super TabBarControl>> TAB_BAR_RENDERER =
			TypedAnnotatable.propertyRaw(ControlRenderer.class, "renderer");

		/**
		 * Singleton {@link CP} instance.
		 */
		public static final CP INSTANCE = new CP();

		private CP() {
			// Singleton constructor.
		}

		@Override
		protected Control createInput(FormMember member) {
			DeckFieldAdapter adapter = member.get(TAB_BAR_MODEL);
			if (adapter == null) {
				DeckField deck = (DeckField) member;
				adapter = new DeckFieldAdapter(deck);
				member.set(TAB_BAR_MODEL, adapter);
				if (ScriptingRecorder.isEnabled()) {
					/* Do not add listener at construction of the field, because the selection may
					 * be changed before the field is added to the form context (e.g. when a card is
					 * added) and no ModelName can be constructed at this time. */
					deck.getModel().addSingleSelectionListener(new SingleSelectionListener() {

						@Override
						public void notifySelectionChanged(SingleSelectionModel model, Object formerlySelectedObject,
								Object selectedObject) {
							if (ScriptingRecorder.isRecordingActive()) {
								CardSelection.recordCardSelection(deck);
							}
						}
					});
				}
			}
			TabBarControl tabBarControl = new TabBarControl(adapter);
			ControlRenderer<? super TabBarControl> renderer = member.get(TAB_BAR_RENDERER);
			if (renderer != null) {
				tabBarControl.setRenderer(renderer);
			}

			final OnVisibleControl wrappingControl = DefaultFormFieldControlProvider.createOnVisibleControl(member);
			wrappingControl.setRenderer(DefaultSimpleCompositeControlRenderer.DIV_INSTANCE);
			wrappingControl.addChild(tabBarControl);
			return wrappingControl;
		}

		/**
		 * Adapter that converts a {@link DeckField} to a {@link TabBarModel} to allow displaying in
		 * a {@link TabBarControl}.
		 * 
		 * @see CP#TAB_BAR_MODEL
		 */
		@FrameworkInternal
		public static final class DeckFieldAdapter extends TabBarModelAdapter {
			private final DeckField deckField;

			DeckFieldAdapter(DeckField deckField) {
				this.deckField = deckField;
			}

			@Override
			public DefaultDeckPaneModel getDeck() {
				return deckField.getModel();
			}

			@Override
			public ModelName getModelName() {
				return ModelResolver.buildModelName(this);
			}

			/**
			 * Returns the {@link DeckField} of this {@link DeckFieldAdapter}.
			 */
			public DeckField getField() {
				return deckField;
			}
		}
	}

}
