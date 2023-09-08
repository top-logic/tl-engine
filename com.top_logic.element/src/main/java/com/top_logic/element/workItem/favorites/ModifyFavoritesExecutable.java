/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.workItem.favorites;

import com.top_logic.base.workItem.WorkItem;
import com.top_logic.element.workItem.wrap.PersistentWrapperWorkItem;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.component.ComponentUtil;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.model.ConstantExecutabilityModel;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;

/**
 * Add or remove an object to the personal favorites.
 * 
 * @author <a href="mailto:fsc@top-logic.com">Friedemann Schneider</a>
 */
public class ModifyFavoritesExecutable extends CommandField {

    /* package protected */static enum State {
		IS_FAVORITE,
		IS_NOT_FAVORITE,
		FAVORITES_DISABLED
	}

	/** The object to be added as favorite. */
	private final Wrapper model;

	/** current state of the executable */
	private State state;

	/**
	 * Creates a {@link ModifyFavoritesExecutable}.
	 * 
	 * @param aModel
	 *        The object to be added as favorite, must not be <code>null</code>.
	 * @throws IllegalArgumentException
	 *         If given object is <code>null</code> or invalid.
	 */
	public ModifyFavoritesExecutable(String name, Wrapper aModel, State initialState, ExecutableState executability) {
		super(name, new ConstantExecutabilityModel(executability));
		if (aModel == null) {
			throw new IllegalArgumentException("Given wrapper is null.");
		}

		this.model = aModel;
		this.state = initialState;
	}

	@Override
	public HandlerResult executeCommand(DisplayContext aContext) {
		if (!ComponentUtil.isValid(model)) {
			return ComponentUtil.errorObjectDeleted(aContext);
		}

		HandlerResult theResult = new HandlerResult();

		// do nothing if an error occurred while creation of the field
		if (this.state == State.FAVORITES_DISABLED) {
			return theResult;
		}

		
		Person      currentPerson = TLContext.getContext().getCurrentPersonWrapper();
		WorkItem    favorite      = FavoritesUtils.getFavorite(this.model, currentPerson);
        
		Resources theRes = Resources.getInstance();
		{
			boolean isFavorite = favorite != null;

			// check if internal state is consistent with the favorite list. There might be
			// differences because the wrapper might be added or removed from favorites somewhere
			// else and we have no good possibility to be informed about that
			if (this.state == State.IS_FAVORITE) {
				if (isFavorite) {
					this.removeFromFavoritesCommit(favorite);
				}
				setImage(Icons.FALSE);
				setTooltip(theRes.getString(I18NConstants.ADD_TOOLTIP));
				this.state = State.IS_NOT_FAVORITE;
			} else if (this.state == State.IS_NOT_FAVORITE) {
				if (!isFavorite) {
					this.addToFavoritesCommit(this.model, currentPerson);
				}
				setImage(Icons.TRUE);
				setTooltip(theRes.getString(I18NConstants.REMOVE_TOOLTIP));
				this.state = State.IS_FAVORITE;
			}
		}
		
		return theResult;

	}

	private void addToFavoritesCommit(Wrapper model, Person currentPerson) {
		try (Transaction theTX = KnowledgeBaseFactory.getInstance().getDefaultKnowledgeBase().beginTransaction()) {
			PersistentWrapperWorkItem.createWorkItem(model.getName(), model, FavoritesUtils.WORK_ITEM_TYPE_FAVORITE,
				currentPerson, null);
			theTX.commit();
		}
	}

	private void removeFromFavoritesCommit(WorkItem favorite) {
		Transaction transaction = KnowledgeBaseFactory.getInstance().getDefaultKnowledgeBase().beginTransaction();
		((Wrapper) favorite).tDelete();
		transaction.commit();
	}

	/**
	 * Return the {@link State}
	 */
	/* package protected */State getState() {
		return this.state;
	}

	/**
	 * Return the default {@link ExecutableState} for a {@link Wrapper}.
	 * 
	 * It will be {@link Command} for current versions of a wrapper, and hidden for invalid or
	 * historic versions.
	 */
	public static ExecutableState getDefaultExecutability(Wrapper aModel) {

		if (!aModel.tValid()) {
			return ExecutableState.NOT_EXEC_HIDDEN;
		}

		// historic wrappers cannot be added as favorites
		if (WrapperHistoryUtils.getRevision(aModel).isCurrent()) {
			return ExecutableState.EXECUTABLE;
		} else {
			return ExecutableState.NOT_EXEC_HIDDEN;
		}
	}

	/**
	 * Create a standard {@link CommandField} that contains a {@link ModifyFavoritesExecutable}
	 * combined with {@link #getDefaultExecutability(Wrapper)}.
	 * 
	 * @see #createField(Wrapper, State, String, ExecutableState)
	 */
	public static CommandField createField(Wrapper aWrapper, State state, String aName) {
		return createField(aWrapper, state, aName, getDefaultExecutability(aWrapper));
	}

	/**
	 * Check if wrapper is marked as favorite of the current user.
	 */
	public static State getFavoriteState(Wrapper wrapper) {
		Person currentPerson = TLContext.getContext().getCurrentPersonWrapper();

		if (FavoritesUtils.getFavorite(wrapper, currentPerson) != null) {
			return State.IS_FAVORITE;
		}
		else {
			return State.IS_NOT_FAVORITE;
		}
	}

	/**
	 * Create a standard {@link CommandField} that contains a {@link ModifyFavoritesExecutable}. The
	 * {@link ExecutableState} is directly passed into the {@link Command}.
	 */
	public static CommandField createField(Wrapper aWrapper, State state, String aName,
			ExecutableState executability) {

		Resources theRes = Resources.getInstance();

		ThemeImage theImage;
		String theTool;
		
		// Init the field status which depends if the wrapper is favorite or not
		if (State.IS_FAVORITE == state) {
			theImage = Icons.TRUE;
			theTool = theRes.getString(I18NConstants.REMOVE_TOOLTIP);
		} else {
			theImage = Icons.FALSE;
			theTool = theRes.getString(I18NConstants.ADD_TOOLTIP);
		}

		CommandField theField = new ModifyFavoritesExecutable(aName, aWrapper, state, executability);
		theField.setImage(theImage);
		theField.setResources(I18NConstants.EXECUTABLE_COMMAND);
		theField.setTooltip(theTool);
		theField.setNotExecutableImage(Icons.FALSE);
		theField.setNotExecutableReasonKey(I18NConstants.DISABLED);

		return theField;
	}
}
