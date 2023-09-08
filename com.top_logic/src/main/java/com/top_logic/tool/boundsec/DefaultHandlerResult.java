/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import java.util.List;

import com.top_logic.basic.exception.ErrorSeverity;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.Command;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.util.error.TopLogicException;

/**
 * An immutable default HandlerResult indicating success. Can be used if
 * processing will not fail and creating a new one is too much work.
 * 
 * @author <a href="mailto:dkh@top-logic.com">Dirk K&ouml;hlhoff</a>
 */
public class DefaultHandlerResult extends HandlerResult {

    /**
	 * Creates a new {@link DefaultHandlerResult}.
	 */
	DefaultHandlerResult() {
        super();
    }

    /**
	 * Immutable, must not be called.
	 */
    @Override
	protected void internalAddError(ResKey encodedMessage) {
		throw errorUnmodifyable();
    }

	/**
	 * Immutable, must not be called.
	 */
	@Override
	@Deprecated
	public void setCloseDialog(boolean value) {
		throw errorUnmodifyable();
	}

	/**
	 * Immutable, must not be called.
	 */
	@Override
	@Deprecated
	public void addProcessed(Object obj) {
		throw errorUnmodifyable();
	}

    /**
	 * Immutable, must not be called.
	 */
    @Override
	@Deprecated
	public void addAllProcessed(List<?> list) {
		throw errorUnmodifyable();
    }

    /**
	 * Immutable, must not be called.
	 */
	@Override
	@Deprecated
	public <T> T set(Property<T> property, T value) {
		throw errorUnmodifyable();
	}

	@Deprecated
	@Override
	public void setErrorSeverity(ErrorSeverity value) {
		throw errorUnmodifyable();
	}

	@Deprecated
	@Override
	public void setErrorTitle(ResKey errorTitle) {
		throw errorUnmodifyable();
	}

	@Deprecated
	@Override
	public void setErrorMessage(ResKey errorMessage) {
		throw errorUnmodifyable();
	}

    /**
	 * Immutable, must not be called.
	 */
    @Override
	@Deprecated
	public void setEncodedErrors(List<ResKey> encodedErrors) {
		throw errorUnmodifyable();
    }

    /**
	 * Immutable, must not be called.
	 */
    @Override
	@Deprecated
	public void setException(TopLogicException anEx) {
		throw errorUnmodifyable();
    }

	/**
	 * Immutable, must not be called.
	 */
    @Override
	@Deprecated
	public void appendResult(HandlerResult aHandlerResult) {
		throw errorUnmodifyable();
    }

	@Override
	public void init(LayoutComponent aComponent, BoundCommand aCommand) {
		// Ignore.
	}

	@Override
	@Deprecated
	public void setErrorContinuation(Command continuation) {
		throw errorUnmodifyable();
	}
	
	@Override
	@Deprecated
	public void addErrorContinuation(Command command) {
		throw errorUnmodifyable();
	}

	private RuntimeException errorUnmodifyable() {
		return new UnsupportedOperationException(DefaultHandlerResult.class.getSimpleName()
			+ " is immutable, create new " + HandlerResult.class.getSimpleName() + " instead.");
	}

}
