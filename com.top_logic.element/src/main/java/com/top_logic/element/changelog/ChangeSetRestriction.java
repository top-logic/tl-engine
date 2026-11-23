/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.changelog;

import java.util.ArrayList;
import java.util.Collection;

import com.top_logic.element.changelog.model.Change;
import com.top_logic.element.changelog.model.ChangeSet;
import com.top_logic.element.changelog.model.Creation;
import com.top_logic.element.changelog.model.Deletion;
import com.top_logic.element.changelog.model.Modification;
import com.top_logic.element.changelog.model.Update;
import com.top_logic.element.changelog.model.trans.TransientChangeSet;
import com.top_logic.element.changelog.model.trans.TransientCreation;
import com.top_logic.element.changelog.model.trans.TransientDeletion;
import com.top_logic.element.changelog.model.trans.TransientModification;
import com.top_logic.element.changelog.model.trans.TransientUpdate;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.model.security.DiscoverRight;
import com.top_logic.model.security.ModelAccessRights;

/**
 * Restricts {@link ChangeSet}s of the change log to what a given user may see.
 *
 * <p>
 * The restriction of a change set is a copy with the same revision, parent revision, date and
 * message, which contains exactly the changes the user may see:
 * </p>
 * <ul>
 * <li>A {@link Creation} or {@link Deletion} is kept if the user may read the created or deleted
 * object.</li>
 * <li>An {@link Update} is kept with the {@link Modification}s of the attributes the user may read
 * on the changed object. An update without such a modification is removed.</li>
 * <li>An object in the old or new value of a modification is kept if the user may discover it.</li>
 * <li>The author is kept if the user may discover it, otherwise the copy has no author.</li>
 * </ul>
 *
 * <p>
 * If a change, a modification or an object in a value is removed, the copy is marked
 * {@link ChangeSet#getIncomplete() incomplete}, since it does not describe the commit completely
 * and must not be reverted. A hidden author does not make the copy incomplete: reverting a commit
 * does not depend on its author. A change set without visible changes is restricted to an empty
 * change set, not dropped.
 * </p>
 *
 * <p>
 * The changes of the copy are computed when they are accessed first, so that restricting a lazily
 * analyzed change set does not analyze it before its changes are needed.
 * </p>
 */
public class ChangeSetRestriction {

	private final ModelAccessRights _rights;

	private final Person _user;

	/**
	 * Creates a {@link ChangeSetRestriction}.
	 *
	 * @param user
	 *        The user whose access rights decide what is kept.
	 */
	public ChangeSetRestriction(Person user) {
		_rights = ModelAccessRights.getInstance();
		_user = user;
	}

	/**
	 * The user whose access rights decide what is kept.
	 */
	public Person getUser() {
		return _user;
	}

	/**
	 * Creates a copy of the given change set restricted to what {@link #getUser()} may see.
	 *
	 * <p>
	 * The {@link ChangeSet#getRevertedBy() revert} of the given change set is restricted as well.
	 * </p>
	 *
	 * @param changeSet
	 *        The change set to restrict.
	 * @return A new change set, see the class description.
	 */
	public TransientChangeSet restrict(ChangeSet changeSet) {
		TransientChangeSet result = new TransientChangeSet(
			copy -> copyChanges(changeSet, copy),
			TransientChangeSet.CHANGES_ATTR, TransientChangeSet.INCOMPLETE_ATTR);
		result.setRevision(changeSet.getRevision());
		result.setParentRev(changeSet.getParentRev());
		result.setDate(changeSet.getDate());
		result.setMessage(changeSet.getMessage());

		Person author = changeSet.getAuthor();
		if (DiscoverRight.isDiscoverable(_user, author)) {
			result.setAuthor(author);
		}

		ChangeSet revertedBy = changeSet.getRevertedBy();
		if (revertedBy != null) {
			result.setRevertedBy(restrict(revertedBy));
		}
		return result;
	}

	private void copyChanges(ChangeSet changeSet, TransientChangeSet copy) {
		Copy state = new Copy(changeSet.getIncomplete());
		for (Change change : changeSet.getChanges()) {
			Change restricted = state.restrict(change);
			if (restricted == null) {
				state.removed();
			} else {
				copy.addChange(restricted);
			}
		}
		copy.setIncomplete(state.isIncomplete());
	}

	/**
	 * Copy of the changes of a single change set, recording whether anything was removed.
	 */
	private class Copy {

		private boolean _incomplete;

		Copy(boolean incomplete) {
			_incomplete = incomplete;
		}

		boolean isIncomplete() {
			return _incomplete;
		}

		void removed() {
			_incomplete = true;
		}

		/**
		 * The visible part of the given change, <code>null</code> if nothing of it is visible.
		 */
		Change restrict(Change change) {
			TLObject object = change.getObject();
			if (!_rights.isReadAllowed(_user, object)) {
				return null;
			}

			Change result;
			if (change instanceof Creation) {
				result = new TransientCreation();
			} else if (change instanceof Deletion) {
				result = new TransientDeletion();
			} else if (change instanceof Update update) {
				TransientUpdate restricted = new TransientUpdate();
				for (Modification modification : update.getModifications()) {
					if (_rights.isReadAllowed(_user, object, modification.getPart())) {
						restricted.addModification(restrict(modification));
					} else {
						removed();
					}
				}
				if (restricted.getModifications().isEmpty()) {
					return null;
				}
				result = restricted;
			} else {
				// A kind of change that cannot be copied.
				return null;
			}
			result.setObject(object);
			result.setOldObject(change.getOldObject());
			result.setImplicit(change.getImplicit());
			return result;
		}

		private TransientModification restrict(Modification modification) {
			TransientModification result = new TransientModification();
			result.setPart(modification.getPart());
			result.setOldValue(discoverable(modification.getOldValue()));
			result.setNewValue(discoverable(modification.getNewValue()));
			return result;
		}

		@SuppressWarnings("unchecked")
		private Collection<Object> discoverable(Collection<Object> value) {
			return new ArrayList<>((Collection<Object>) DiscoverRight.filter(_user, value, dropped -> removed()));
		}

	}

}
