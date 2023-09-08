/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component.model;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.HasErrorChanged;
import com.top_logic.layout.form.MemberChangedListener;
import com.top_logic.layout.form.control.Icons;
import com.top_logic.layout.form.model.AbstractFormContainer;
import com.top_logic.layout.form.model.DeckField;
import com.top_logic.mig.html.layout.Card;
import com.top_logic.util.Utils;

/**
 * {@link DefaultDeckPaneModel} whose {@link Card}s {@link Card#getContent() contain}
 * {@link FormMember}s.
 * 
 * <p>
 * A {@link FormDeckPaneModel} is used as model of a {@link DeckField} to group members of a form
 * into several tabs.
 * </p>
 * 
 * <p>
 * For cards whose content is a {@link FormContainer} an event is fired, if the if the error state
 * changes.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class FormDeckPaneModel extends DefaultDeckPaneModel implements HasErrorChanged, MemberChangedListener {

	private Map<FormContainer, Set<FormField>> errorMemberByChild = new HashMap<>();

	/**
	 * Creates an empty {@link FormDeckPaneModel}.
	 */
	public FormDeckPaneModel() {
		super();
	}

	/**
	 * Creates a {@link FormDeckPaneModel}.
	 *
	 * @param selectableCards
	 *        See {@link #getSelectableCards()}.
	 */
	public FormDeckPaneModel(List<? extends Card> selectableCards) {
		super(selectableCards);
		for (int index = 0, size = selectableCards.size(); index < size; index++) {
			Card currentCard = selectableCards.get(index);
			initCardToAdd(currentCard);
		}
	}

	private void initCardToAdd(Card aCard) {
		if (aCard.getContent() instanceof FormContainer) {
			FormContainer tab = (FormContainer) aCard.getContent();
			registerAsListener(tab);
			Iterator<? extends FormField> descendantFields = tab.getDescendantFields();
			Set<FormField> errorFields = new HashSet<>();
			while (descendantFields.hasNext()) {
				FormField theDescendant = descendantFields.next();
				if (theDescendant.hasError()) {
					errorFields.add(theDescendant);
				}
			}
			if (!errorFields.isEmpty()) {
				errorMemberByChild.put(tab, errorFields);
			}

		}
	}

	private void registerAsListener(FormContainer container) {
		container.addListener(FormField.HAS_ERROR_PROPERTY, this);
		container.addListener(FormContainer.MEMBER_ADDED_PROPERTY, this);
		container.addListener(FormContainer.MEMBER_REMOVED_PROPERTY, this);
	}

	private void deregisterAsListener(FormContainer container) {
		container.removeListener(FormContainer.MEMBER_REMOVED_PROPERTY, this);
		container.removeListener(FormContainer.MEMBER_ADDED_PROPERTY, this);
		container.removeListener(FormField.HAS_ERROR_PROPERTY, this);
	}

	@Override
	public boolean addSelectableCard(Card aCard) {
		boolean result = super.addSelectableCard(aCard);
		if (result) {
			initCardToAdd(aCard);
		}
		return result;
	}

	@Override
	public boolean removeSelectableCard(Card aCard) {
		boolean result = super.removeSelectableCard(aCard);
		Object cardContent = aCard.getContent();
		if (result && cardContent instanceof FormContainer) {
			deregisterAsListener((FormContainer) cardContent);
			errorMemberByChild.remove(cardContent);
		}
		return result;
	}

	/**
	 * If the <i>has error state</i> of the "FormContainer" content of some card has changed, i.e.
	 * from "no child has an error" and "one child has an error", then a model changed event is
	 * fired with <code>oldValue == newValue == {@link #getSelectableCards()}</code>
	 */
	@Override
	public Bubble hasErrorChanged(FormField sender, Boolean oldError, Boolean newError) {
		if (!Utils.equals(oldError, newError)) {
			newErrorState(sender, newError);
		}
		return Bubble.BUBBLE;
	}

	@Override
	public Bubble memberAdded(FormContainer parent, FormMember member) {
		if (member instanceof FormField && ((FormField) member).hasError()) {
			newErrorState((FormField) member, true);
		}
		return Bubble.BUBBLE;
	}

	@Override
	public Bubble memberRemoved(FormContainer parent, FormMember member) {
		if (member instanceof FormField && ((FormField) member).hasError()) {
			newErrorState((FormField) member, false);
		}
		return Bubble.BUBBLE;
	}

	/**
	 * This method removes or adds <code>aMember</code> as error members to
	 * <code>errorMemberByChild</code> depending on <code>errorState</code>.
	 */
	private void newErrorState(FormField aMember, boolean errorState) {
		FormContainer ancestor = getAncestorForErrorField(aMember);
		if (ancestor == null) {
			return;
		}
		Set<FormField> errorDescendants = errorMemberByChild.get(ancestor);
		if (!errorState) {
			if (!CollectionUtil.isEmptyOrNull(errorDescendants)) {
				errorMemberByChild.remove(ancestor);
				fireModelChangedEvent(getSelectableCards(), getSelectableCards());
			}
		} else {
			if (!CollectionUtil.isEmptyOrNull(errorDescendants)) {
				errorDescendants.add(aMember);
			} else {
				errorDescendants = new HashSet<>();
				errorDescendants.add(aMember);
				errorMemberByChild.put(ancestor, errorDescendants);
				fireModelChangedEvent(getSelectableCards(), getSelectableCards());
			}
		}
	}

	/**
	 * This method returns the {@link FormContainer} whose card was added to this
	 * {@link FormDeckPaneModel} and who is an ancestor of the given {@link FormMember}.
	 * 
	 * @return an ancestor of <code>aMember</code> or <code>null</code> if no
	 *         {@link FormContainer}, whose card was added as selectable object, is an ancestor of
	 *         <code>aMember</code>.
	 */
	private FormContainer getAncestorForErrorField(FormMember aMember) {
		List<Card> selectableCards = getSelectableCards();
		while (aMember.getParent() != null && !memberIsCardContent(aMember, selectableCards)) {
			aMember = aMember.getParent();
		}
		return (FormContainer) aMember;
	}

	/**
	 * This method decides whether a card in the given {@link List} has the given {@link FormMember}
	 * as content.
	 * 
	 * @param aMember
	 *            the {@link FormMember} to check
	 * @param someCards
	 *            a list of {@link Card} to be checked.
	 * @return <code>true</code> iff there is a {@link Card} in <code>someCards</code> which
	 *         have <code>aMember</code> as content.
	 */
	private boolean memberIsCardContent(FormMember aMember, List<Card> someCards) {
		for (int index = 0, size = someCards.size(); index < size; index++) {
			Object cardContent = someCards.get(index).getContent();
			if (Utils.equals(cardContent, aMember)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * The class {@link FormDeckPaneModel.FormDeckPaneCard} is a {@link Card} implementation for {@link DeckField}s
	 * which wraps a {@link FormMember}. Two {@link FormDeckPaneModel.FormDeckPaneCard} are equal iff the wrapped
	 * {@link FormMember} are equal.
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	public static class FormDeckPaneCard implements Card{
		
		private final FormMember member;
	
		/**
		 * Creates a {@link FormDeckPaneCard}.
		 *
		 * @param member
		 *        See {@link #getContent()}.
		 */
		public FormDeckPaneCard(FormMember member) {
			if (member == null) {
				throw new IllegalArgumentException("'member' must not be 'null'.");
			}
			this.member = member;
		}
	
		@Override
		public Object getCardInfo() {
			return member.getLabel();
		}
	
		@Override
		public FormMember getContent() {
			return member;
		}
	
		@Override
		public String getName() {
			return member.getQualifiedName();
		}
	
		@Override
		public void writeCardInfo(DisplayContext context, Appendable out) throws IOException {
			out.append(member.getLabel());
			if (member instanceof AbstractFormContainer && out instanceof TagWriter) {
				TagWriter tagWriter = (TagWriter) out;
				final ThemeImage img;
				if (((AbstractFormContainer) member).hasError()) {
					img = Icons.ALERT;
				} else {
					img = Icons.ALERT_SPACER;
				}
				img.write(context, tagWriter);
			}
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof FormDeckPaneCard)) {
				return false;
			}
			return member.equals(((FormDeckPaneCard) obj).member);
		}
		
		@Override
		public int hashCode() {
			return member.hashCode();
		}
		
	}

}
