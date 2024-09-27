/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.action;

import java.util.List;

import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.component.model.FormDeckPaneModel;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.DeckField;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.AbstractApplicationActionOp;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;
import com.top_logic.mig.html.layout.Card;

/**
 * {@link AbstractFormAction} for changing the selection of a {@link DeckField}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface CardSelection extends AbstractFormAction {

	@Override
	@ClassDefault(Op.class)
	Class<? extends AbstractApplicationActionOp<?>> getImplementationClass();

	/**
	 * The index of the card to select. Is just used when {@link #getCardName()} is
	 * <code>null</code>. <code>-1</code> means "deselect".
	 */
	int getCardIndex();

	/**
	 * Setter for {@link #getCardIndex()}
	 */
	void setCardIndex(int value);

	/**
	 * Name of the card to select. May be <code>null</code>, in which case {@link #getCardIndex()}
	 * is used.
	 */
	@Nullable
	ResKey getCardName();

	/**
	 * Setter for {@link #getCardName()}
	 */
	void setCardName(ResKey value);

	/**
	 * Records a {@link CardSelection} for the given {@link DeckField} if possible.
	 */
	static void recordCardSelection(DeckField deck) {
		Maybe<? extends ModelName> modelName = ModelResolver.buildModelNameIfAvailable(deck);
		if (!modelName.hasValue()) {
			return;
		}
		CardSelection config = TypedConfiguration.newConfigItem(CardSelection.class);
		config.setField(modelName.get());
		FormDeckPaneModel model = deck.getModel();
		int selectedIndex = model.getSelectedIndex();
		config.setCardIndex(selectedIndex);
		if (selectedIndex >= 0) {
			Card card = model.getSelectableCards().get(selectedIndex);
			Object content = card.getContent();
			if (content instanceof FormMember && ((FormMember) content).hasLabel()) {
				config.setCardName(((FormMember) content).getLabel());
			}
		}
		ScriptingRecorder.recordAction(config);
	}

	/**
	 * Default implementation of {@link CardSelection}.
	 */
	class Op<S extends CardSelection> extends AbstractApplicationActionOp<S> {

		/**
		 * Creates a {@link CardSelection.Op} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		public Op(InstantiationContext context, S config) {
			super(context, config);
		}

		@Override
		protected Object processInternal(ActionContext context, Object argument) throws Throwable {
			DeckField field = (DeckField) ModelResolver.locateModel(context, getConfig().getField());
			ResKey cardName = getConfig().getCardName();
			FormDeckPaneModel model = field.getModel();
			if (cardName != null) {
				List<Card> cards = model.getSelectableCards();
				for (Card card : cards) {
					Object content = card.getContent();
					if (content instanceof FormMember && ((FormMember) content).hasLabel()
						&& cardName.equals(((FormMember) content).getLabel())) {
						model.setSingleSelection(card);
						return argument;
					}
				}
				throw ApplicationAssertions.fail(getConfig(), "No card with label " + cardName + " available.");
			} else {
				model.setSelectedIndex(getConfig().getCardIndex());
				return argument;
			}
		}
	}

}

