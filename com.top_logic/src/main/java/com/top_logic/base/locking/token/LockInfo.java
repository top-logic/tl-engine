/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.locking.token;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.top_logic.base.locking.token.Token.Kind;
import com.top_logic.model.TLObject;

/**
 * Info about a lock retrieved from the {@link TokenService}.
 * 
 * @see TokenService#getAllLocks()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LockInfo {

	private TLObject _owner;

	private List<Token> _tokens = new ArrayList<>();

	private Date _timeout;

	private Long _nodeId;

	private String _operation;

	/**
	 * The timeout of the lock.
	 */
	public Date getTimeout() {
		return _timeout;
	}

	/**
	 * @see #getTimeout()
	 */
	public void setTimeout(Date timeout) {
		_timeout = timeout;
	}

	/**
	 * The owner of the lock (the account that acquired the lock).
	 */
	public TLObject getOwner() {
		return _owner;
	}

	/**
	 * @see #getOwner()
	 */
	public void setOwner(TLObject owner) {
		_owner = owner;
	}

	/**
	 * All {@link Token}s this lock is composed of.
	 */
	public List<Token> getTokens() {
		return _tokens;
	}

	/**
	 * Adds another {@link Token} to {@link #getTokens()}.
	 */
	public void addToken(Token token) {
		_tokens.add(token);
	}

	/**
	 * All objects for which an exclusive token exists in this lock.
	 */
	public List<TLObject> getObjects() {
		List<TLObject> result = new ArrayList<>();
		for (Token t : getTokens()) {
			if (t.getKind() == Kind.SHARED) {
				continue;
			}
			result.add(t.getObject());
		}
		return result;
	}

	/**
	 * All aspects of exclusively locked objects.
	 */
	public List<String> getAspects() {
		List<String> result = new ArrayList<>();
		for (Token t : getTokens()) {
			if (t.getKind() == Kind.SHARED) {
				continue;
			}
			result.add(t.getName());
		}
		return result;
	}

	/**
	 * The ID of the cluster node that aquired this lock.
	 */
	public Long getClusterNodeId() {
		return _nodeId;
	}

	/**
	 * @see #getClusterNodeId()
	 */
	public void setClusterNodeId(Long nodeId) {
		_nodeId = nodeId;
	}

	/**
	 * Technical name of the operation that triggered the lock acquisition.
	 */
	public String getOperation() {
		return _operation;
	}

	/**
	 * @see #getOperation()
	 */
	public void setOperation(String operation) {
		_operation = operation;
	}

}
