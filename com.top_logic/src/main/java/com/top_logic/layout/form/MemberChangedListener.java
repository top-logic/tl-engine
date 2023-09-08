/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.listener.PropertyListener;

/**
 * {@link PropertyListener} that handles adding and removal of {@link FormMember} in a
 * {@link FormContainer}.
 * 
 * @see FormField#MANDATORY_PROPERTY
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface MemberChangedListener extends PropertyListener {

	/**
	 * Handles adding of the given member to the {@link FormContainer}.
	 * 
	 * @param parent
	 *        {@link FormContainer} who got a new member.
	 * @param member
	 *        {@link FormMember} added to the given container.
	 * @return Whether this event shall bubble.
	 * 
	 * @see FormContainer#addMember(FormMember)
	 */
	Bubble memberAdded(FormContainer parent, FormMember member);

	/**
	 * Handles removal of the given member from the {@link FormContainer}.
	 * 
	 * @param parent
	 *        {@link FormContainer} who lost a member.
	 * @param member
	 *        {@link FormMember} removed from the given container.
	 * @return Whether this event shall bubble.
	 * 
	 * @see FormContainer#addMember(FormMember)
	 */
	Bubble memberRemoved(FormContainer parent, FormMember member);

}

