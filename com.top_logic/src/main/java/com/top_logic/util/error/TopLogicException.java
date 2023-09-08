/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.error;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.exception.ErrorSeverity;
import com.top_logic.basic.exception.I18NException;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.util.ResKey;

/**
 * {@link RuntimeException} carrying an internationalizable error description that can be displayed
 * to the end user.
 * 
 * @author    <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public class TopLogicException extends I18NRuntimeException {

    /** prefix for all error-keys */
	@Deprecated
    public static final String PREFIX = "error_code_";
    
    /** Used as resourcePrefix when null is given */
	@Deprecated
    public static final String NULL_PREFIX = "TopLogicException.null.";
    
	/**
	 * Create a new "stand alone" TopLogicException, not wrapping others.
	 * 
	 * Parameters may be passed to be inserted into the corresponding resource.
	 * 
	 * @param aClass
	 *        where Exception occurred, class name will be used as ResourcePrefix.
	 * @param aKey
	 *        identifying the failed action, completing the resource-key
	 * @param aParams
	 *        optional parameter, may be null (use e.g. for messages, use {0}{1} in translations to
	 *        refer to them)
	 * 
	 * @deprecated Use {@link #TopLogicException(ResKey)}
	 */
	@Deprecated
	public TopLogicException(Class<?> aClass, String aKey, Object[] aParams) {
		this(aClass, aKey, aParams, null);
    }

    /**
     * Create a new "standalone" TopLogicException, not wrapping others.
     * 
     * Parameters may be passed to be inserted into the corresponding resource.
     * 
     * @param aClass  where Exception occured, 
     *                classname will be used as ResourcePrefix.
     * @param aKey    identifying the failed action, 
     *                completing the resource-key 
     *                
	 * @deprecated Use {@link #TopLogicException(ResKey)}
     */
	@Deprecated
	public TopLogicException(Class<?> aClass, String aKey) {
        this (aClass, aKey, (Object[]) null );
    }

    /**
     * Create a TopLogicException wrapping a Throwable.
     * If Throwable is known to be a TopLogicException, use corresponding CTor
     * instead.
     * 
     * Parameters may be passed to be inserted into the corresponding i18n-message.
     * 
     * @param aClass  where Exception occured, 
     *                classname will be used as ResourcePrefix.
     * @param aKey    identifying the failed action, 
     *                completing the resource-key 
     * @param aParams optional parameter, may be null(use e.g. for messages, 
     *                use {0}{1} in translations to  refer to them)
     * @param cause   original cause, must not ba null.
     *                
     * @throws NullPointerException if cause is null
     * 
	 * @deprecated Use {@link #TopLogicException(ResKey, Throwable)}
     */
	@Deprecated
	public TopLogicException(Class<?> aClass, String aKey, Object[] aParams, Throwable cause) {
		this(ResKey.message(ResKey.legacy(resolveMessage(aClass, aKey)), aParams), cause);
    }

	/**
	 * Creates a {@link TopLogicException}.
	 * 
	 * @param errorKey
	 *        The resource key for the failure message.
	 * 
	 * @see #TopLogicException(ResKey, Throwable)
	 */
	public TopLogicException(ResKey errorKey) {
		super(errorKey);
	}
	
	/**
	 * Creates a {@link TopLogicException}.
	 * 
	 * @param errorKey
	 *        The resource key for the failure message.
	 * @param cause
	 *        See {@link #getCause()}.
	 * 
	 * @see #TopLogicException(ResKey)
	 */
	public TopLogicException(ResKey errorKey, Throwable cause) {
		super(errorKey, cause);
	}

	/**
     * Create a TopLogicException wrapping a Throwable without params.
     * 
     * Parameters may be passed to be inserted into the corresponding i18n-message.
     * 
     * @param aClass  where Exception occured, 
     *                classname will be used as ResourcePrefix.
     * @param aKey    identifying the failed action, 
     *                completing the resource-key 
     * @param cause   original cause, must not ba null.
     *                
     * @throws NullPointerException if cause is null
     * 
	 * @deprecated Use {@link #TopLogicException(ResKey, Throwable)}
     */
	@Deprecated
	public TopLogicException(Class<?> aClass, String aKey, Throwable cause) {
        this (aClass, aKey, /* params */ null, cause);
    }

    /** Helper for Ctor to resolve message-parameter for super CTor */
	@Deprecated
	private static String resolveMessage(Class<?> aClass, String aKey) {
        String className = NULL_PREFIX;
        if (aClass != null)
            className = aClass.getName();
		if (StringServices.isEmpty(aKey)) {
			return PREFIX + className;
		} else {
			return PREFIX + className + '.' + aKey;
		}
    }
    
	/**
	 * A nested {@link TopLogicException}, or <code>null</code> if there is no {@link #getCause()},
	 * or it is a "regular" {@link Throwable}.
	 */
	public TopLogicException nested() {
		Throwable cause = getCause();
		if (cause instanceof TopLogicException) {
			return (TopLogicException) cause;
		} else {
			return null;
		}
	}

	@Override
	protected ErrorSeverity defaultSeverity() {
		return hasTechnicalCause() ? ErrorSeverity.SYSTEM_FAILURE : ErrorSeverity.ERROR;
	}

	private boolean hasTechnicalCause() {
		Throwable cause = getCause();
		return cause != null && !(cause instanceof I18NException);
	}

	/**
	 * Factory for {@link TopLogicException}s with severity {@link ErrorSeverity#WARNING}.
	 */
	public static TopLogicException warning(ResKey message) {
		TopLogicException result = new TopLogicException(message);
		result.initSeverity(ErrorSeverity.WARNING);
		return result;
	}

}
