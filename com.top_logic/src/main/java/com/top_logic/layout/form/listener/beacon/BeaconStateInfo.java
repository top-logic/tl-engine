/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.listener.beacon;

import java.util.Collection;

import com.top_logic.layout.form.FormField;

/**
 * The {@link BeaconStateInfo} works together with the {@link BeaconMandatoryValueListener}.
 * A {@link BeaconStateInfo} contains form fields for a beacon state that should be 
 * mandatory if the state is set and NOT mandatory if an other state is set. 
 * 
 * @author    <a href=mailto:tdi@top-logic.com>tdi</a>
 */
public class BeaconStateInfo implements BeaconStateConstants {

	private Integer state;
	private Collection formFields;

	/**
	 * Creates a new BeaconStateInfo.
	 * 
	 * @param state
	 *            A beacon state (see constants).
	 * @param formFields
	 *            A collection of {@link FormField}s must NOT be <code>null</code>.
	 */
	public BeaconStateInfo(Integer state, Collection formFields) {
		this.state = state;
		
		if (formFields == null || formFields.isEmpty()) {
			throw new IllegalArgumentException("Null or an empty collection isn't allow for the form fields");
		}
		
		this.formFields = formFields;
	}

	/**
	 * Returns the state.
	 */
	public Integer getState() {
		return this.state;
	}

	/**
	 * Returns the formFields.
	 */
	public Collection getFormFields() {
		return this.formFields;
	}
	
}
