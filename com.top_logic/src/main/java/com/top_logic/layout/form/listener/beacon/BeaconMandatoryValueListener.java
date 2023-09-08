/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.listener.beacon;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.util.FormFieldConstants;
import com.top_logic.util.Utils;

/**
 * @author    <a href=mailto:tdi@top-logic.com>tdi</a>
 */
public class BeaconMandatoryValueListener implements ValueListener, BeaconStateConstants, FormFieldConstants {

	/**
	 * Key: beacon state ({@link Integer} - See {@link BeaconStateConstants}).
	 * Value: beacon state info ({@link BeaconStateInfo}). 
	 */
	private Map beaconInfos;
	
	/** 
	 * Creates a new BeaconMandatoryValueListener. 
	 * 
	 * @param beaconInfos See {@link #beaconInfos}.
	 */
	public BeaconMandatoryValueListener(Map beaconInfos) {
		this.beaconInfos = beaconInfos;
	}

	/**
	 * @see #BeaconMandatoryValueListener(BeaconStateInfo, BeaconStateInfo, BeaconStateInfo,
	 *      BeaconStateInfo)
	 */
	public BeaconMandatoryValueListener(BeaconStateInfo info1) {
		this(info1, null, null, null);
	}
	
	/**
	 * @see #BeaconMandatoryValueListener(BeaconStateInfo, BeaconStateInfo, BeaconStateInfo,
	 *      BeaconStateInfo)
	 */
	public BeaconMandatoryValueListener(BeaconStateInfo info1, BeaconStateInfo info2) {
		this(info1, info2, null, null);
	}
	
	/**
	 * @see #BeaconMandatoryValueListener(BeaconStateInfo, BeaconStateInfo, BeaconStateInfo,
	 *      BeaconStateInfo)
	 */
	public BeaconMandatoryValueListener(BeaconStateInfo info1, BeaconStateInfo info2, BeaconStateInfo info3) {
		this(info1, info2, info3, null);
	}
	
	/**
	 * Creates a new BeaconMandatoryValueListener with the given
	 * {@link BeaconStateInfo}s. Min one of the {@link BeaconStateInfo} must be
	 * NOT <code>null</code>. This constructor can be used to set all beacon
	 * states.
	 * 
	 * @param info1
	 *            A {@link BeaconStateInfo} or <code>null</code>.
	 * @param info2
	 *            A {@link BeaconStateInfo} or <code>null</code>.
	 * @param info3
	 *            A {@link BeaconStateInfo} or <code>null</code>.
	 * @param info4
	 *            A {@link BeaconStateInfo} or <code>null</code>.
	 */
	public BeaconMandatoryValueListener(BeaconStateInfo info1, BeaconStateInfo info2, BeaconStateInfo info3, BeaconStateInfo info4) {
		this(new HashMap());
		
		addBeaconInfo(info1);
		addBeaconInfo(info2);
		addBeaconInfo(info3);
		addBeaconInfo(info4);
	}

	@Override
	public void valueChanged(FormField field, Object oldValue, Object newValue) {
		// Handle only if the value was changed.
		if (Utils.equals(oldValue, newValue)) return;
	
		setMandatoryValueFor(getFieldsFor(oldValue), !MANDATORY);
		setMandatoryValueFor(getFieldsFor(newValue), MANDATORY);
	}
	
	/**
	 * This method sets the mandatory value of all {@link FormField}s in the
	 * given collection.
	 * 
	 * @param formFields
	 *            A collection of {@link FormField}s must NOT be <code>null</code>.
	 * @param isMandatory
	 *            A mandatory flag to be set.
	 */
	protected void setMandatoryValueFor(Collection formFields, boolean isMandatory) {
		for (Iterator iterator = formFields.iterator(); iterator.hasNext();) {
			FormField relatedField = (FormField) iterator.next();
			relatedField.setMandatory(isMandatory);
		}
	}
	
	/**
	 * This method returns the {@link FormField}s for the given beacon state as
	 * {@link Integer}. Returns NEVER <code>null</code>.
	 * 
	 * @param stateInCollection
	 *            A beacon state as {@link Integer} in a {@link Collections}. 
	 *            See {@link BeaconStateConstants}.
	 * @return Returns the {@link FormField}s for the beacon state.
	 */
	protected Collection getFieldsFor(Object stateInCollection) {
		BeaconStateInfo beaconStateInfo = (BeaconStateInfo) getBeaconInfos().get(CollectionUtil.getSingleValueFrom(stateInCollection));
		
		return beaconStateInfo != null ? beaconStateInfo.getFormFields() : Collections.EMPTY_LIST;
	}
	
	/**
	 * This method adds the {@link BeaconStateInfo} to the intern map
	 * {@link #beaconInfos} except the info is <code>null</code>.
	 * 
	 * @param beaconInfo
	 *            A {@link BeaconStateInfo} or <code>null</code>.
	 */
	protected void addBeaconInfo(BeaconStateInfo beaconInfo) {
		if (beaconInfo != null) {
			getBeaconInfos().put(beaconInfo.getState(), beaconInfo);
		}
	}

	/**
	 * This method returns an iterator of the {@link BeaconStateInfo}s and
	 * NEVER <code>null</code>.
	 * 
	 * @return Returns an iterator of the {@link BeaconStateInfo}s.
	 */
	public Iterator getBeaconStateInfos() {
		return getBeaconInfos().values().iterator();
	}
	
	/**
	 * Returns the beaconInfos.
	 */
	protected Map getBeaconInfos() {
		return this.beaconInfos;
	}

	/**
	 * See get-method.
	 */
	protected void setBeaconInfos(Map beaconInfos) {
		this.beaconInfos = beaconInfos;
	}
	
}
