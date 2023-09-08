/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.error;

import com.top_logic.basic.util.ResKey;

/**
 * {@link TopLogicException} that reports an access right violation.
 * 
 * @author <a href="mailto:kbu@top-logic.com">Karsten Buch</a>
 */
public class TopLogicSecurityException extends TopLogicException {

	/**
	 * Creates a {@link TopLogicSecurityException}.
	 * 
	 * @param errorKey
	 *        See {@link TopLogicException#TopLogicException(ResKey, Throwable)}.
	 */
	public TopLogicSecurityException(ResKey errorKey) {
		super(errorKey);
	}

	/**
	 * Creates a {@link TopLogicSecurityException}.
	 * 
	 * @param errorKey
	 *        See {@link TopLogicException#TopLogicException(ResKey, Throwable)}.
	 * @param cause
	 *        See {@link TopLogicException#TopLogicException(ResKey, Throwable)}.
	 */
	public TopLogicSecurityException(ResKey errorKey, Throwable cause) {
		super(errorKey, cause);
	}

	/**
	 * Create a new "standalone" TopLogicSecurityException, not wrapping others.
	 * 
	 * Parameters may be passed to be inserted into the corresponding resource.
	 * 
	 * @param aClass
	 *        where Exception occured, classname will be used as ResourcePrefix.
	 * @param aKey
	 *        identifying the failed action, completing the resource-key
	 * @param aParams
	 *        optional parameter, may be null (use e.g. for messages, use {0}{1} in translations to
	 *        refer to them)
	 */
	@Deprecated
	public TopLogicSecurityException(Class aClass, String aKey, Object[] aParams) {
		super(aClass, aKey, aParams);
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
     */
	@Deprecated
	public TopLogicSecurityException(Class aClass, String aKey) {
		super(aClass, aKey);
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
     */
	@Deprecated
	public TopLogicSecurityException(Class aClass, String aKey,
			Object[] aParams, Throwable cause) {
		super(aClass, aKey, aParams, cause);
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
     */
	@Deprecated
	public TopLogicSecurityException(Class aClass, String aKey, Throwable cause) {
		super(aClass, aKey, cause);
	}

    /**
     * Create a TopLogicException wrapping a TopLogicException.
     * 
     * Parameters may be passed to be inserted into the corresponding resource.
     * 
     * (Easier variant when cause is known to be a TopLogicException)
     * 
     * @param aClass  where Exception occured, 
     *                classname will be used as ResourcePrefix.
     * @param aKey    identifying the failed action, 
     *                completing the resource-key 
     * @param aParams optional parameter, may be null (use e.g. for messages, 
     *                use {0}{1} in translations to  refer to them)
     * @param aCause  A TopLogicException, must not be null.
     * 
     * @throws NullPointerException if cause is null
     */
	@Deprecated
	public TopLogicSecurityException(Class aClass, String aKey,
			Object[] aParams, TopLogicException aCause)
			throws NullPointerException {
		super(aClass, aKey, aParams, aCause);
	}

}
