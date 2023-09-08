/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.state;

import com.top_logic.base.merge.Validator;

/**
 * Status of an element (which can be a Wrapper).
 *
 * @author <a href="mailto:tgi@top-logic.com></a>
 */
public interface ElementState extends State, Comparable {

    /** State change validation type. */
    public static final int VALIDATE_FOR_STATE_CHANGE = 0;

    // KHA disabled until I understand what this is needed for
    // public static final int VALIDATE_FOR_MSPROJECT_REIMPORT =1;

    /**
     * The "natural" number in case states are linear.
     *
     * @return the "natural" number in case states are linear
     */
    public int getStepNo();


    /**
     * Gets a Validator to validate anything you like.
     *
     * @param validationType
     *            one of the VALIDATE_FOR_ constants.
     * @param validationRequest
     *            as specified by some Validator (? Well ?)
     * @param anObject
     *            The object to validate
     * @param additionalValidationInfo
     *            Opaque parameter for the Validator
     *
     * @return A Validaor for the actual validate()
     */
    public Validator validate(int validationType, int validationRequest, StatefullElement anObject, Object additionalValidationInfo);


    /**
     * Checks if the given state is a valid successor state of this state.
     *
     * @param state
     *            the state to check
     * @return <code>true</code>, if <code>state</code> is a valid successor state
     */
    public boolean isValidSuccessor(ElementState state);

}
