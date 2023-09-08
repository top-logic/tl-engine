/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.state;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.top_logic.base.merge.Validator;

/**
 * TODO TGI Document your code
 *
 * @author    <a href="mailto:tgi@top-logic.com>Thomas der Gegrillte</a>
 */
public abstract class AbstractState implements ElementState {

    private List successors;

    private int step;

    private String key;



    protected AbstractState(String aKey, List aSuccessorList, Integer theStep, Set theOwners) {
        super();
        if (aKey == null || theStep == null) {
            throw new IllegalStateException("AbstractState Constructor: Arguments must not be null");
        }
        key = aKey;
        step = theStep.intValue();
        if (aSuccessorList == null) {
            setSuccessors(new ArrayList(0));
            return;
        }
        if (checkSuccessorList(aSuccessorList)) {
            throw new IllegalStateException(
                "AbstractState Constructor: Argument \"aSuccessorList\" must not contain elements of type: "
                    + aSuccessorList.get(0));
        }
        setSuccessors(aSuccessorList);
    }

    private boolean checkSuccessorList(List aSuccessorList) {
        return aSuccessorList != null
            && aSuccessorList.size() > 0
            && !(aSuccessorList.get(0) instanceof ElementState);
    }

    @Override
	public String getKey() {
        return key;
    }

    /**
     * Add another State as successor for this one.
     */
    protected void addSuccessor(ElementState aSucc) {
        successors.add(aSucc);
    }

    public boolean isSuccessor(ElementState state) {
        return getSuccessors().contains(state);
    }

    private void setSuccessors(List somSuccessors) {
        this.successors = somSuccessors;
    }

    @Override
	public Collection getSuccessors() {
        return successors;
    }

    @Override
	public int getStepNo() {
        return step;
    }

    @Override
	public boolean isFinal() {
        return successors == null || successors.isEmpty();
    }

    @Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
        if (!(obj instanceof ElementState)) {
            return false;
        }
        ElementState state = (ElementState) obj;
        return getKey().equals(state.getKey()) && getStepNo() == state.getStepNo();
    }

    @Override
	public int hashCode() {
        return key.hashCode() | step;
    }

    @Override
	public String toString() {
        return getClass().getName() + ":" + getKey();
    }

    @Override
	public int compareTo(Object o) {
        if (!(o instanceof ElementState)) {
            return -1;
        }
        ElementState state = (ElementState) o;
        int res = step - state.getStepNo();
        if (res == 0) {
            res = key.compareTo(state.getKey());
        }
        return res;
    }

    @Override
	public Validator validate(int aValidationType, int validationRequest, StatefullElement anObject, Object additionalValidationInfo){
        switch (aValidationType) {
        case VALIDATE_FOR_STATE_CHANGE:
            return validateForStateChange();
//        case VALIDATE_FOR_MSPROJECT_REIMPORT: {
//            if (aValidationSource instanceof Map) {
//                return validateForProjectImport(aValidationRequest, (Map)aValidationSource);
//            }
        default: return null;
        }
    }

    /**
     * Returns a validator for a state change if needed by this state
     *
     * @return null if no Validation is needed.
     */
    protected Validator validateForStateChange() {
        return null;
    }

    @Override
	public boolean isValidSuccessor(ElementState state) {
        return getSuccessors().contains(state);
    }

    @Override
	public Collection getRoles() {
        return null;
    }

}
