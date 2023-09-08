/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.locking.token;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.base.locking.token.Token.Kind;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.model.TLObject;
import com.top_logic.util.error.TopLogicException;

/**
 * In-memory (single-node) {@link TokenService} implementation.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class EphemeralTokenService extends ConfiguredTokenService<ConfiguredTokenService.Config<?>> {

	private long _lockId = 1;

	private Object _monitor = new Object();

	private final Map<Token.Key, Semaphore> _semaphores = new HashMap<>();

	/**
	 * Creates a {@link EphemeralTokenService} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public EphemeralTokenService(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public void acquire(Date expireDate, Collection<Token> tokens) throws TopLogicException {
		DisplayContext context = DefaultDisplayContext.getDisplayContext();
		String operation = getOperation(context);
		TLObject owner = context.getSubSessionContext().getPerson();

		synchronized (_monitor) {
			long id = _lockId++;
			checkLock(tokens);
			for (Token token : tokens) {
				token.setId(id);

				TokenRef ref = new TokenRef(token, expireDate, owner, operation);
				makeSemaphore(token).add(ref);
			}
		}
	}

	private String getOperation(DisplayContext context) {
		ComponentName componentName = context.getProcessingInfo().getComponentName();
		String operation;
		if (componentName != null) {
			operation = componentName.qualifiedName();
		} else {
			operation = null;
		}
		return operation;
	}

	private Semaphore makeSemaphore(Token token) {
		Semaphore semaphore = getSemaphore(token);
		if (semaphore == null) {
			semaphore = allocateSemaphore(token);
		}
		return semaphore;
	}

	private Semaphore getSemaphore(Token token) {
		return _semaphores.get(token.getKey());
	}

	private Semaphore allocateSemaphore(Token token) {
		Semaphore semaphore = new Semaphore(token.getKey(), token.getKind());
		_semaphores.put(token.getKey(), semaphore);
		return semaphore;
	}

	private void checkLock(Collection<Token> tokens) {
		long now = System.currentTimeMillis();
		for (Token token : tokens) {
			Semaphore existingSemaphore = getSemaphore(token);
			if (existingSemaphore != null) {
				if (existingSemaphore.validate(now)) {
					existingSemaphore.checkLock(token);
				} else {
					drop(existingSemaphore);
				}
			}
		}
	}

	/**
	 * Whether none of the given {@link Token}s have expired.
	 *
	 * @return Whether all {@link Token}s are still valid.
	 */
	@Override
	public boolean allValid(Collection<Token> tokens) {
		synchronized (_monitor) {
			return check(tokens);
		}
	}

	private boolean check(Collection<Token> tokens) {
		long now = System.currentTimeMillis();
		for (Token token : tokens) {
			TokenRef ref = getRef(token);
			if (ref == null) {
				return false;
			}
			if (ref.getTimeout().getTime() < now) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean renew(Date expireDate, Collection<Token> tokens) {
		synchronized (_monitor) {
			if (!check(tokens)) {
				release(tokens);
				return false;
			}

			for (Token token : tokens) {
				getRef(token).setTimeout(expireDate);
			}
		}
		return true;
	}

	@Override
	public void release(Collection<Token> tokens) {
		synchronized (_monitor) {
			long now = System.currentTimeMillis();
			for (Token token : tokens) {
				Semaphore semaphore = getSemaphore(token);
				if (semaphore == null) {
					// Already timed out.
					continue;
				}

				semaphore.validate(now);
				semaphore.remove(token);
				if (semaphore.isEmpty()) {
					drop(semaphore);
				}
			}
		}
	}

	private TokenRef getRef(Token token) {
		Semaphore semaphore = getSemaphore(token);
		if (semaphore == null) {
			return null;
		}
		return semaphore.getRef(token);
	}

	private void drop(Semaphore semaphore) {
		_semaphores.remove(semaphore.getKey());
	}

	@Override
	public Collection<LockInfo> getAllLocks() {
		Map<Long, LockInfo> result = new HashMap<>();
		synchronized (_monitor) {
			for (Semaphore s : _semaphores.values()) {
				for (TokenRef r : s.getRefs()) {
					long id = r.getToken().getId();
					LockInfo lockInfo = result.get(id);
					if (lockInfo == null) {
						lockInfo = new LockInfo();
						result.put(id, lockInfo);
					}

					lockInfo.addToken(r.getToken());
					Date timeout = lockInfo.getTimeout();
					Date tokenTimeout = r.getTimeout();
					if (timeout == null || (tokenTimeout != null && tokenTimeout.before(timeout))) {
						lockInfo.setTimeout(tokenTimeout);
					}

					TLObject owner = r.getOwner();
					if (owner != null) {
						lockInfo.setOwner(owner);
					}

					String operation = r.getOperation();
					if (operation != null && !operation.isEmpty()) {
						lockInfo.setOperation(operation);
					}
				}
			}
		}
		return result.values();
	}

	static final class TokenRef {

		private final Token _token;

		private Date _timeout;

		private final TLObject _owner;

		private String _operation;

		public TokenRef(Token token, Date timeout, TLObject owner, String operation) {
			super();
			_token = token;
			_timeout = timeout;
			_owner = owner;
			_operation = operation;
		}

		/**
		 * The owner that acquired this token.
		 */
		public TLObject getOwner() {
			return _owner;
		}

		/**
		 * The name of the operation that requested the lock.
		 */
		public String getOperation() {
			return _operation;
		}

		/**
		 * The time when this {@link Token} is automatically released.
		 */
		public Date getTimeout() {
			return _timeout;
		}

		/**
		 * Updates the {@link #getTimeout()} value.
		 */
		public void setTimeout(Date timeout) {
			_timeout = timeout;
		}

		/**
		 * Whether this {@link Token} is still valid at the given time.
		 *
		 * @param now
		 *        The time relative to which check {@link Token} validity.
		 * @return Whether this {@link Token} is valid at the given time.
		 */
		public boolean isValid(long now) {
			return now < _timeout.getTime();
		}

		/**
		 * @see Token#getKind()
		 */
		public Kind getKind() {
			return _token.getKind();
		}

		public Token getToken() {
			return _token;
		}

	}

	final class Semaphore {

		private final Kind _kind;

		private final Map<Token, TokenRef> _tokens = new HashMap<>();

		private Token.Key _key;

		public Semaphore(Token.Key key, Kind kind) {
			_key = key;
			_kind = kind;
		}

		public Token.Key getKey() {
			return _key;
		}

		public TokenRef getRef(Token token) {
			return _tokens.get(token);
		}

		public Collection<TokenRef> getRefs() {
			return _tokens.values();
		}

		public boolean isEmpty() {
			return _tokens.isEmpty();
		}

		public void remove(Token token) {
			_tokens.remove(token);
		}

		public boolean validate(long now) {
			for (Iterator<Entry<Token, TokenRef>> it = _tokens.entrySet().iterator(); it.hasNext();) {
				TokenRef ref = it.next().getValue();
				if (!ref.isValid(now)) {
					it.remove();
				}
			}
			return !_tokens.isEmpty();
		}

		public void add(TokenRef ref) {
			_tokens.put(ref.getToken(), ref);
		}

		public void checkLock(Token token) {
			if (hasConflict(token)) {
				if (getConfig().getReportLockOwnerDetails()) {
					Iterator<TokenRef> it = _tokens.values().iterator();
					if (it.hasNext()) {
						TokenRef ref = it.next();
						throw createDetailedLockConflictError(ref.getOwner(), ref.getTimeout().getTime(), null);
					}
				}
				throw createGenericLockConflictError();
			}
		}

		private boolean hasConflict(Token token) {
			return _kind == Kind.EXCLUSIVE || token.getKind() == Kind.EXCLUSIVE;
		}

	}

}
