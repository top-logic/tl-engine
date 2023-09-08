/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.progress;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.NamedConstant;

/**
 * The ProgressResult holds info messages, warnings and errors of an progress.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class ProgressResult {

    /** Key for default result value. */
    private static final NamedConstant DEFAULT_VALUE = new NamedConstant("default_value");


    /** Stores result values. */
    private Map values;

    /** Flag whether the progress was successful or not. */
    private Boolean success;

    /** Stores the info messages. */
    private List infos;

    /** Stores the warnings. */
    private List warnings;

    /** Stores the errors. */
    private List errors;



    /**
     * Creates a new instance of this class.
     */
    public ProgressResult() {
        clear();
    }



    /**
     * Clears all results.
     */
    public void clear() {
        values = new HashMap();
        success = null;
        infos = new ArrayList();
        warnings = new ArrayList();
        errors = new ArrayList();
    }



    /**
     * Checks whether a result value for the given key was set.
     *
     * @param key
     *        the key of the result value
     * @return <code>true</code> if a result value for the given key was set,
     *         <code>false</code> otherwise
     */
    public boolean hasValue(Object key) {
        return values.containsKey(key);
    }

    /**
     * Gets a result value of the progress.
     *
     * @param key
     *        the key of the result value
     * @return the result value of the progress for the given key
     */
    public Object getValue(Object key) {
        return values.get(key);
    }

    /**
     * Sets a result value to the progress.
     *
     * @param key
     *        the key of the result value
     * @param value
     *        the result value to set
     */
    public void setValue(Object key, Object value) {
        this.values.put(key, value);
    }


    /**
     * Checks whether the progress has set a default result value.
     *
     * @return <code>true</code>, if the progress has a default result,
     *         <code>false</code> otherwise
     */
    public boolean hasValue() {
        return hasValue(DEFAULT_VALUE);
    }

    /**
     * Gets the default result value of the progress.
     *
     * @return the default result value of the progress
     */
    public Object getValue() {
        return getValue(DEFAULT_VALUE);
    }

    /**
     * Sets the default result value of the progress.
     *
     * @param value
     *        the default result value to set; may be <code>null</code>
     */
    public void setValue(Object value) {
        setValue(DEFAULT_VALUE, value);
    }



    /**
     * Checks whether the progress was successful. If this value wasn't set explicitly to a
     * value, a progress is successful if it has no errors.
     *
     * @return <code>true</code>, if the progress was finished successful,
     *         <code>false</code> otherwise
     */
    public boolean isSuccess() {
        return success == null ? !hasErrors() : success.booleanValue();
    }

    /**
     * Sets whether the progress is successful or not
     *
     * @param success
     *        the success state of the progress
     */
    public void setSuccess(boolean success) {
        this.success = Boolean.valueOf(success);
    }



    /**
     * Checks whether this result has reported infos.
     *
     * @return <code>true</code>, if this result has reported infos, <code>false</code>
     *         otherwise
     */
    public boolean hasInfos() {
        return !infos.isEmpty();
    }

    /**
     * Gets the list with reported infos.
     *
     * @return the list with reported infos; may be empty but not <code>null</code>
     */
    public List getInfos() {
        return infos;
    }

    /**
     * Adds an info to the error list.
     *
     * @param info
     *        the info to add to the info list
     */
    public void addInfo(String info) {
        if (info != null) infos.add(info);
    }

    /**
     * Sets the info list of this result.
     *
     * @param infos
     *        the info list to set
     */
    public void setInfos(List infos) {
        this.infos = infos;
    }



    /**
     * Checks whether this result has reported warnings.
     *
     * @return <code>true</code>, if this result has reported warnings,
     *         <code>false</code> otherwise
     */
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    /**
     * Gets the list with reported warnings.
     *
     * @return the list with reported warnings; may be empty but not <code>null</code>
     */
    public List getWarnings() {
        return warnings;
    }

    /**
     * Adds a warning to the error list.
     *
     * @param warning
     *        the warning to add to the warning list
     */
    public void addWarning(String warning) {
        if (warning != null) warnings.add(warning);
    }

    /**
     * Sets the warning list of this result.
     *
     * @param warnings
     *        the warning list to set
     */
    public void setWarnings(List warnings) {
        this.warnings = warnings;
    }



    /**
     * Checks whether this result has reported errors.
     *
     * @return <code>true</code>, if this result has reported errors, <code>false</code>
     *         otherwise
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * Gets the list with reported errors.
     *
     * @return the list with reported errors; may be empty but not <code>null</code>
     */
    public List getErrors() {
        return errors;
    }

    /**
     * Adds an error to the error list.
     *
     * @param error
     *        the error to add to the error list
     */
    public void addError(String error) {
        if (error != null) errors.add(error);
    }

    /**
     * Sets the error list of this result.
     *
     * @param errors
     *        the error list to set
     */
    public void setErrors(List errors) {
        this.errors = errors;
    }

}
