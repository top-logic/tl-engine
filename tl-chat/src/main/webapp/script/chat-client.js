/**
 * tl-chat client — agentic chat UI.
 *
 * Single-file vanilla JS using namespace pattern.
 * Connects to push-based SSE for real-time agent events.
 */
(function () {
	'use strict';
	if (window._tlChatLoaded) return;
	window._tlChatLoaded = true;

	// ========================================================================
	// Utils
	// ========================================================================
	const Utils = {
		escapeHtml(s) {
			if (!s) return '';
			const d = document.createElement('div');
			d.textContent = s;
			return d.innerHTML;
		},

		formatDuration(ms) {
			if (ms == null) return '';
			if (ms < 1000) return ms + 'ms';
			if (ms < 60000) return (ms / 1000).toFixed(1) + 's';
			const m = Math.floor(ms / 60000);
			const s = Math.round((ms % 60000) / 1000);
			return m + 'm ' + s + 's';
		},

		formatTokens(n) {
			if (n == null || n === 0) return '0';
			if (n < 1000) return '' + n;
			if (n < 100000) return (n / 1000).toFixed(1) + 'k';
			return Math.round(n / 1000) + 'k';
		},

		formatTime(instant) {
			if (!instant) return '';
			const d = new Date(instant);
			return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' });
		},

		debounce(fn, ms) {
			let timer;
			return function (...args) {
				clearTimeout(timer);
				timer = setTimeout(() => fn.apply(this, args), ms);
			};
		},

		contextPath() {
			const path = window.location.pathname;
			const idx = path.indexOf('/servlet/');
			return idx >= 0 ? path.substring(0, idx) : '';
		},

		icon(name) {
			const icons = {
				brain: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9.5 2A5.5 5.5 0 0 0 4 7.5c0 1.58.67 3 1.74 4.01A5.5 5.5 0 0 0 9.5 22h5a5.5 5.5 0 0 0 3.76-10.49A5.5 5.5 0 0 0 14.5 2"/></svg>',
				chevronRight: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m9 18 6-6-6-6"/></svg>',
				alertCircle: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>',
				agent: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="3"/><path d="M12 2v4m0 12v4M2 12h4m12 0h4"/></svg>',
				tool: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"/></svg>',
				x: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 6 6 18"/><path d="m6 6 12 12"/></svg>',
			};
			const span = document.createElement('span');
			span.className = 'tl-chat-icon';
			span.innerHTML = icons[name] || '';
			return span;
		}
	};

	const CTX = Utils.contextPath();
	const URLS = {
		send: CTX + '/chat/send',
		events: CTX + '/chat/events',
		sessions: CTX + '/chat/sessions',
		respond: CTX + '/chat/respond',
		cancel: CTX + '/chat/cancel',
		approvalMode: CTX + '/chat/approval-mode',
		updates: CTX + '/chat/updates',
	};

	// ========================================================================
	// State — Client-side state machine
	// ========================================================================
	const State = {
		IDLE: 'idle',
		STREAMING: 'streaming',
		AWAITING_APPROVAL: 'awaiting_approval',
		COMPLETED: 'completed',
		ERROR: 'error',
		CANCELLED: 'cancelled',

		_current: 'idle',
		_listeners: [],

		get() { return this._current; },

		set(newState) {
			const old = this._current;
			this._current = newState;
			this._listeners.forEach(fn => fn(newState, old));
		},

		onChange(fn) { this._listeners.push(fn); },

		isActive() {
			return this._current === this.STREAMING || this._current === this.AWAITING_APPROVAL;
		}
	};

	// ========================================================================
	// Store — Conversation data
	// ========================================================================
	const Store = {
		messages: [],
		currentRun: null,
		sessionApprovals: new Set(),
		cumulativeUsage: null,

		resetRun() {
			this.currentRun = {
				runId: null,
				model: null,
				textBuffer: '',
				thinkingBuffer: '',
				toolCalls: new Map(),
				delegations: new Map(),
				turnUsage: null,
				totalUsage: null,
			};
		},

		addToolCall(id, name, argsJson) {
			const isDelegation = name.startsWith('delegate_to_');
			if (isDelegation) {
				this.currentRun.delegations.set(id, {
					fromAgent: 'orchestrator',
					toAgent: name.replace('delegate_to_', '').replace(/_/g, '-'),
					childToolCallIds: [],
					status: 'running',
					durationMs: null,
				});
			}
			this.currentRun.toolCalls.set(id, {
				name, argsJson, status: 'running', result: null, durationMs: null, error: null,
				isDelegation
			});
		},

		completeToolCall(id, result, durationMs) {
			const tc = this.currentRun.toolCalls.get(id);
			if (tc) {
				tc.status = 'success';
				tc.result = result;
				tc.durationMs = durationMs;
			}
			const del = this.currentRun.delegations.get(id);
			if (del) {
				del.status = 'success';
				del.durationMs = durationMs;
			}
		},

		failToolCall(id, error, durationMs) {
			const tc = this.currentRun.toolCalls.get(id);
			if (tc) {
				tc.status = 'error';
				tc.error = error;
				tc.durationMs = durationMs;
			}
			const del = this.currentRun.delegations.get(id);
			if (del) {
				del.status = 'error';
				del.durationMs = durationMs;
			}
		},

		denyToolCall(id, reason) {
			const tc = this.currentRun.toolCalls.get(id);
			if (tc) {
				tc.status = 'denied';
				tc.error = reason;
			}
		},
	};

	// ========================================================================
	// SSE — Push-based event source
	// ========================================================================
	const SSE = {
		_source: null,
		_sessionId: null,
		_reconnectTimer: null,

		connect(sessionId) {
			this.disconnect();
			this._sessionId = sessionId;
			const url = URLS.events + '?sessionId=' + encodeURIComponent(sessionId || '');
			this._source = new EventSource(url);

			const handlers = {
				connected: Handlers.onConnected,
				run_started: Handlers.onRunStarted,
				run_completed: Handlers.onRunCompleted,
				turn_started: Handlers.onTurnStarted,
				turn_completed: Handlers.onTurnCompleted,
				text_delta: Handlers.onTextDelta,
				thinking_delta: Handlers.onThinkingDelta,
				tool_started: Handlers.onToolStarted,
				tool_completed: Handlers.onToolCompleted,
				tool_failed: Handlers.onToolFailed,
				tool_denied: Handlers.onToolDenied,
				approval_required: Handlers.onApprovalRequired,
				approval_resolved: Handlers.onApprovalResolved,
				lock_acquired: Handlers.onLockAcquired,
				tx_began: Handlers.onTxBegan,
				tx_committed: Handlers.onTxCommitted,
				tx_rollback: Handlers.onTxRollback,
				compaction_started: Handlers.onCompactionStarted,
				provider_error: Handlers.onProviderError,
				message_complete: Handlers.onMessageComplete,
				user_interaction: Handlers.onUserInteraction,
				approval_mode: Handlers.onApprovalMode,
			};

			for (const [type, handler] of Object.entries(handlers)) {
				this._source.addEventListener(type, (e) => {
					try {
						handler(JSON.parse(e.data));
					} catch (err) {
						console.error('[tl-chat] SSE handler error for ' + type + ':', err);
					}
				});
			}

			this._source.onerror = () => {
				this._source.close();
				this._reconnectTimer = setTimeout(() => this.connect(this._sessionId), 2000);
			};
		},

		disconnect() {
			if (this._source) {
				this._source.close();
				this._source = null;
			}
			if (this._reconnectTimer) {
				clearTimeout(this._reconnectTimer);
				this._reconnectTimer = null;
			}
		}
	};

	// ========================================================================
	// Handlers — SSE event handlers
	// ========================================================================
	const Handlers = {
		onConnected(data) {
			if (data.processing) {
				State.set(State.STREAMING);
				Store.resetRun();
			} else {
				State.set(State.IDLE);
			}
			Render.updateInputState();
		},

		onRunStarted(data) {
			Store.resetRun();
			Store.currentRun.runId = data.runId;
			Store.currentRun.model = data.model;
			State.set(State.STREAMING);
			Render.appendAssistantBubble();
			Render.updateInputState();
		},

		onRunCompleted(data) {
			if (Store.currentRun) {
				Store.currentRun.totalUsage = data.totalUsage;
			}
			if (data.reason === 'ABORTED') {
				State.set(State.CANCELLED);
				Render.showCancelledPrompt();
			} else if (data.reason === 'ERROR') {
				State.set(State.ERROR);
			} else {
				State.set(State.COMPLETED);
			}
			Render.finalizeAssistantBubble();
			Render.updateInputState();
			Tokens.showRunUsage(data.totalUsage);
		},

		onTurnStarted(data) { /* Turn boundary — no UI action needed */ },

		onTurnCompleted(data) {
			if (Store.currentRun) {
				Store.currentRun.turnUsage = data.turnUsage;
			}
		},

		onTextDelta(data) {
			if (Store.currentRun) {
				Store.currentRun.textBuffer += data.text;
				Render.updateStreamingText();
			}
		},

		onThinkingDelta(data) {
			if (Store.currentRun) {
				Store.currentRun.thinkingBuffer += data.text;
				Thinking.updateStreaming();
			}
		},

		onToolStarted(data) {
			Store.addToolCall(data.toolCallId, data.toolName, data.argsJson);
			Tools.renderToolCall(data.toolCallId);
		},

		onToolCompleted(data) {
			Store.completeToolCall(data.toolCallId, data.result, data.durationMs);
			Tools.updateToolCall(data.toolCallId);
		},

		onToolFailed(data) {
			Store.failToolCall(data.toolCallId, data.errorMessage, data.durationMs);
			Tools.updateToolCall(data.toolCallId);
		},

		onToolDenied(data) {
			Store.denyToolCall(data.toolCallId, data.reason);
			Tools.updateToolCall(data.toolCallId);
		},

		onApprovalRequired(data) {
			State.set(State.AWAITING_APPROVAL);
			Approval.showPrompt(data);
			Render.updateInputState();
		},

		onApprovalResolved(data) {
			Approval.resolvePrompt(data);
			if (State.get() === State.AWAITING_APPROVAL) {
				State.set(State.STREAMING);
				Render.updateInputState();
			}
		},

		onLockAcquired(data) { Timeline.addPill('Lock acquired', ''); },
		onTxBegan(data) { /* No UI */ },
		onTxCommitted(data) {
			if (data.revisionNumber >= 0) {
				Timeline.addPill('Committed (rev ' + data.revisionNumber + ')', 'committed');
			}
		},
		onTxRollback(data) { Timeline.addPill('Rolled back: ' + (data.reason || ''), 'rollback'); },

		onCompactionStarted(data) {
			Timeline.addPill('Compacting (' + Utils.formatTokens(data.tokensBefore) + ' tokens, ' + data.trigger + ')', '');
		},

		onProviderError(data) { Errors.show(data); },

		onMessageComplete(data) {
			Render.swapToRenderedHtml(data.contentHtml);
		},

		onUserInteraction(data) { Approval.showInteraction(data); },

		onApprovalMode(data) { /* Track mode changes */ },
	};

	// ========================================================================
	// Render — DOM rendering
	// ========================================================================
	const Render = {
		_root: null,
		_messagesEl: null,
		_inputEl: null,
		_sendBtn: null,
		_cancelBtn: null,
		_currentBubble: null,
		_currentTextEl: null,
		_currentToolsEl: null,
		_scrollThreshold: 300,
		_rafId: null,

		init(root) {
			this._root = root;
			this._messagesEl = root.querySelector('.chat-messages');
			this._inputEl = root.querySelector('.chat-input');
			this._sendBtn = root.querySelector('.chat-button');
			this._setupCancelButton();
			this._setupInput();
			this._setupAutoScroll();
		},

		_setupCancelButton() {
			const btn = document.createElement('button');
			btn.className = 'tl-chat-cancel-btn';
			btn.textContent = 'Cancel';
			btn.style.display = 'none';
			btn.addEventListener('click', () => {
				fetch(URLS.cancel, { method: 'POST' })
					.catch(err => console.error('[tl-chat] Cancel request failed:', err));
			});
			if (this._sendBtn && this._sendBtn.parentNode) {
				this._sendBtn.parentNode.insertBefore(btn, this._sendBtn.nextSibling);
			}
			this._cancelBtn = btn;
		},

		_setupInput() {
			if (!this._inputEl) return;
			this._inputEl.addEventListener('keydown', (e) => {
				if (e.key === 'Enter' && !e.shiftKey) {
					e.preventDefault();
					this._send();
				}
			});
			if (this._sendBtn) {
				this._sendBtn.addEventListener('click', () => this._send());
			}
		},

		_send() {
			const text = this._inputEl.value.trim();
			if (!text || State.isActive()) return;

			this.appendUserMessage(text);
			this._inputEl.value = '';

			const userBubble = this._messagesEl.lastElementChild;
			fetch(URLS.send, {
				method: 'POST',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify({
					text: text,
					sessionId: SSE._sessionId || '',
					attachments: []
				})
			}).catch(err => {
				console.error('[tl-chat] Send failed:', err);
				if (userBubble && userBubble.parentNode) {
					userBubble.remove();
				}
				this._inputEl.value = text;
			});
		},

		_setupAutoScroll() {
			this._shouldAutoScroll = true;
			if (this._messagesEl) {
				this._messagesEl.addEventListener('scroll', () => {
					const el = this._messagesEl;
					this._shouldAutoScroll =
						(el.scrollHeight - el.scrollTop - el.clientHeight) < this._scrollThreshold;
				});
			}
		},

		_scrollToBottom() {
			if (this._shouldAutoScroll && this._messagesEl) {
				this._messagesEl.scrollTop = this._messagesEl.scrollHeight;
			}
		},

		appendUserMessage(text) {
			const div = document.createElement('div');
			div.className = 'chat-message chat-message-user';
			div.innerHTML = '<div class="chat-bubble chat-bubble-user">' +
				Utils.escapeHtml(text) + '</div>';
			this._messagesEl.appendChild(div);
			this._scrollToBottom();
		},

		appendAssistantBubble() {
			const div = document.createElement('div');
			div.className = 'chat-message chat-message-agent';

			div.innerHTML = '<div class="chat-avatar">AI</div>';

			const content = document.createElement('div');
			content.className = 'chat-bubble chat-bubble-agent';

			const thinkingEl = document.createElement('div');
			thinkingEl.className = 'tl-chat-thinking';
			thinkingEl.style.display = 'none';
			content.appendChild(thinkingEl);

			const textEl = document.createElement('div');
			textEl.className = 'chat-agent-text tl-chat-content-swap';
			content.appendChild(textEl);

			const toolsEl = document.createElement('div');
			toolsEl.className = 'tl-chat-tools-container';
			content.appendChild(toolsEl);

			div.appendChild(content);
			this._messagesEl.appendChild(div);

			this._currentBubble = content;
			this._currentTextEl = textEl;
			this._currentToolsEl = toolsEl;
			this._scrollToBottom();
		},

		updateStreamingText() {
			if (!this._currentTextEl || !Store.currentRun) return;
			if (this._rafId) return;
			this._rafId = requestAnimationFrame(() => {
				this._rafId = null;
				if (!this._currentTextEl || !Store.currentRun) return;
				const html = Markdown.basicFormat(Store.currentRun.textBuffer);
				this._currentTextEl.innerHTML = html +
					'<span class="tl-chat-streaming-cursor"></span>';
				this._scrollToBottom();
			});
		},

		swapToRenderedHtml(html) {
			if (!this._currentTextEl) return;
			this._currentTextEl.style.opacity = '0';
			setTimeout(() => {
				this._currentTextEl.innerHTML = html;
				this._currentTextEl.style.opacity = '1';
			}, 150);
		},

		finalizeAssistantBubble() {
			if (this._currentTextEl) {
				const cursor = this._currentTextEl.querySelector('.tl-chat-streaming-cursor');
				if (cursor) cursor.remove();
			}
			if (this._currentBubble && Store.currentRun && Store.currentRun.totalUsage) {
				const badge = Tokens.createBadge(Store.currentRun.totalUsage);
				this._currentBubble.appendChild(badge);
			}
			this._currentBubble = null;
			this._currentTextEl = null;
			this._currentToolsEl = null;
		},

		showCancelledPrompt() {
			if (!this._messagesEl) return;
			const div = document.createElement('div');
			div.className = 'chat-message chat-message-system';
			div.innerHTML = '<div class="chat-notifier">Interrupted — What should the agent do instead?</div>';
			this._messagesEl.appendChild(div);
			this._scrollToBottom();
		},

		updateInputState() {
			const active = State.isActive();
			if (this._inputEl) this._inputEl.disabled = active;
			if (this._sendBtn) this._sendBtn.style.display = active ? 'none' : '';
			if (this._cancelBtn) this._cancelBtn.style.display = active ? '' : 'none';
			if (!active && this._inputEl) this._inputEl.focus();
		}
	};

	// ========================================================================
	// Markdown — Basic inline formatting for streaming
	// ========================================================================
	const Markdown = {
		basicFormat(text) {
			if (!text) return '';
			let html = Utils.escapeHtml(text);

			// Code fences: ```lang\n...\n```
			html = html.replace(/```(\w*)\n([\s\S]*?)```/g,
				(_, lang, code) => '<pre><code class="language-' + (lang || 'text') + '">' + code + '</code></pre>');

			// Inline code: `code`
			html = html.replace(/`([^`]+)`/g, '<code>$1</code>');

			// Bold: **text**
			html = html.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>');

			// Italic: *text*
			html = html.replace(/\*([^*]+)\*/g, '<em>$1</em>');

			// Paragraphs: double newlines
			html = html.replace(/\n\n/g, '</p><p>');
			html = '<p>' + html + '</p>';

			// Single newlines → <br> inside paragraphs
			html = html.replace(/\n/g, '<br>');

			return html;
		}
	};

	// ========================================================================
	// Thinking — Thinking block rendering
	// ========================================================================
	const Thinking = {
		_el: null,

		updateStreaming() {
			if (!Render._currentBubble || !Store.currentRun) return;
			const thinkingEl = Render._currentBubble.querySelector('.tl-chat-thinking');
			if (!thinkingEl) return;

			if (thinkingEl.style.display === 'none') {
				thinkingEl.style.display = '';
				this._buildStructure(thinkingEl);
			}
			this._el = thinkingEl;

			const preview = thinkingEl.querySelector('.tl-chat-thinking-preview');
			const body = thinkingEl.querySelector('.tl-chat-thinking-body');
			const text = Store.currentRun.thinkingBuffer;

			const expanded = thinkingEl.dataset.expanded === 'true';
			if (preview && !expanded) {
				preview.textContent = text.substring(0, 80) + (text.length > 80 ? '...' : '');
			}
			if (body && expanded) {
				body.textContent = text;
			}
			Render._scrollToBottom();
		},

		_buildStructure(el) {
			const header = document.createElement('div');
			header.className = 'tl-chat-thinking-header';
			header.innerHTML =
				'<span class="tl-chat-thinking-chevron">&#9654;</span>' +
				'<span>Thinking</span>' +
				'<span class="tl-chat-thinking-preview"></span>';
			header.addEventListener('click', () => this._toggle(el));
			el.appendChild(header);

			const body = document.createElement('div');
			body.className = 'tl-chat-thinking-body';
			body.style.display = 'none';
			el.appendChild(body);
		},

		_toggle(el) {
			const wasExpanded = el.dataset.expanded === 'true';
			const nowExpanded = !wasExpanded;
			el.dataset.expanded = '' + nowExpanded;
			const chevron = el.querySelector('.tl-chat-thinking-chevron');
			const preview = el.querySelector('.tl-chat-thinking-preview');
			const body = el.querySelector('.tl-chat-thinking-body');
			if (chevron) chevron.classList.toggle('expanded', nowExpanded);
			if (preview) preview.style.display = nowExpanded ? 'none' : '';
			if (body) {
				body.style.display = nowExpanded ? '' : 'none';
				if (nowExpanded && Store.currentRun) {
					body.textContent = Store.currentRun.thinkingBuffer;
				}
			}
		}
	};

	// ========================================================================
	// Tools — Tool call rendering
	// ========================================================================
	const Tools = {
		renderToolCall(toolCallId) {
			const container = Render._currentToolsEl;
			if (!container) return;
			const tc = Store.currentRun.toolCalls.get(toolCallId);
			if (!tc) return;

			if (tc.isDelegation) {
				this._renderDelegation(container, toolCallId, tc);
			} else {
				this._renderTool(container, toolCallId, tc);
			}
			Render._scrollToBottom();
		},

		_renderTool(container, id, tc) {
			const el = document.createElement('div');
			el.className = 'tl-chat-tool';
			el.dataset.toolCallId = id;

			const header = document.createElement('div');
			header.className = 'tl-chat-tool-header';
			header.innerHTML =
				'<span class="tl-chat-status-dot running"></span>' +
				'<span class="tl-chat-tool-name">' + Utils.escapeHtml(tc.name) + '</span>' +
				'<span class="tl-chat-tool-duration"></span>' +
				'<span class="tl-chat-tool-chevron">&#9654;</span>';

			const body = document.createElement('div');
			body.className = 'tl-chat-tool-body';
			body.style.display = 'none';

			if (tc.argsJson) {
				body.innerHTML =
					'<div class="tl-chat-tool-label">Arguments</div>' +
					'<pre>' + Utils.escapeHtml(this._prettyJson(tc.argsJson)) + '</pre>';
			}

			let expanded = false;
			header.addEventListener('click', () => {
				expanded = !expanded;
				body.style.display = expanded ? '' : 'none';
				header.querySelector('.tl-chat-tool-chevron').classList.toggle('expanded', expanded);
			});

			el.appendChild(header);
			el.appendChild(body);
			container.appendChild(el);
		},

		_renderDelegation(container, id, tc) {
			const del = Store.currentRun.delegations.get(id);
			if (!del) return;

			const el = document.createElement('div');
			el.className = 'tl-chat-delegation';
			el.dataset.toolCallId = id;

			const header = document.createElement('div');
			header.className = 'tl-chat-delegation-header';
			header.innerHTML =
				'<span class="tl-chat-status-dot running"></span>' +
				'<span>Agent: ' + Utils.escapeHtml(del.toAgent) + '</span>' +
				'<span class="tl-chat-tool-duration"></span>';
			el.appendChild(header);

			const childContainer = document.createElement('div');
			childContainer.className = 'tl-chat-delegation-tools';
			el.appendChild(childContainer);

			container.appendChild(el);
		},

		updateToolCall(toolCallId) {
			const tc = Store.currentRun.toolCalls.get(toolCallId);
			if (!tc) return;
			const root = Render._root || document;
			const el = root.querySelector('[data-tool-call-id="' + toolCallId + '"]');
			if (!el) return;

			const dot = el.querySelector('.tl-chat-status-dot');
			if (dot) {
				dot.className = 'tl-chat-status-dot ' + tc.status;
			}

			const dur = el.querySelector('.tl-chat-tool-duration');
			if (dur && tc.durationMs != null) {
				dur.textContent = Utils.formatDuration(tc.durationMs);
			}

			if (tc.status === 'denied') {
				const name = el.querySelector('.tl-chat-tool-name');
				if (name) name.classList.add('denied');
			}

			const body = el.querySelector('.tl-chat-tool-body');
			if (body) {
				if (tc.result) {
					const resultDiv = document.createElement('div');
					resultDiv.innerHTML =
						'<div class="tl-chat-tool-label">Result</div>' +
						'<pre>' + Utils.escapeHtml(this._truncate(tc.result, 2000)) + '</pre>';
					body.appendChild(resultDiv);
				}
				if (tc.error) {
					const errorDiv = document.createElement('div');
					errorDiv.innerHTML =
						'<div class="tl-chat-tool-label" style="color:var(--chat-status-error)">' +
						(tc.status === 'denied' ? 'Denied' : 'Error') + '</div>' +
						'<pre style="color:var(--chat-status-error)">' +
						Utils.escapeHtml(tc.error) + '</pre>';
					body.appendChild(errorDiv);
				}
			}
		},

		_prettyJson(jsonStr) {
			try {
				return JSON.stringify(JSON.parse(jsonStr), null, 2);
			} catch (e) {
				return jsonStr;
			}
		},

		_truncate(s, max) {
			if (!s || s.length <= max) return s;
			return s.substring(0, max) + '\n... (' + (s.length - max) + ' more characters)';
		}
	};

	// ========================================================================
	// Approval — Approval prompt rendering
	// ========================================================================
	const Approval = {
		_currentPrompt: null,

		showPrompt(data) {
			const container = Render._currentToolsEl || Render._messagesEl;
			if (!container) return;

			const el = document.createElement('div');
			el.className = 'tl-chat-approval';
			el.dataset.approvalId = data.approvalRequestId;

			el.innerHTML =
				'<div class="tl-chat-approval-header">Approval required: ' +
				Utils.escapeHtml(data.toolName) + '</div>' +
				'<div class="tl-chat-approval-reason">' +
				Utils.escapeHtml(data.displayReason || '') + '</div>' +
				'<div class="tl-chat-approval-actions">' +
				'<button class="tl-chat-approval-btn allow" data-action="APPROVED">Allow</button>' +
				'<button class="tl-chat-approval-btn" data-action="APPROVED_FOR_SESSION">Allow for Session</button>' +
				'<button class="tl-chat-approval-btn deny" data-action="REJECTED">Deny</button>' +
				'</div>' +
				'<div class="tl-chat-approval-comment" style="display:none">' +
				'<input type="text" placeholder="Reason (optional)...">' +
				'<button class="tl-chat-approval-btn deny">Send</button>' +
				'</div>';

			el.querySelectorAll('[data-action]').forEach(btn => {
				btn.addEventListener('click', () => {
					const action = btn.dataset.action;
					if (action === 'REJECTED') {
						el.querySelector('.tl-chat-approval-comment').style.display = '';
						el.querySelector('.tl-chat-approval-actions').style.display = 'none';
						const sendBtn = el.querySelector('.tl-chat-approval-comment .tl-chat-approval-btn');
						const input = el.querySelector('.tl-chat-approval-comment input');
						sendBtn.addEventListener('click', () => {
							this._respond(data.approvalRequestId, 'REJECTED', data.toolName, input.value);
							this._dismiss(el);
						});
						input.addEventListener('keydown', (e) => {
							if (e.key === 'Enter') sendBtn.click();
						});
						input.focus();
					} else {
						this._respond(data.approvalRequestId, action, data.toolName, null);
						this._dismiss(el);
					}
				});
			});

			container.appendChild(el);
			this._currentPrompt = el;
			Render._scrollToBottom();
		},

		_respond(requestId, outcome, toolName, comment) {
			const body = { requestId, response: outcome };
			if (toolName) body.toolName = toolName;
			if (comment) body.comment = comment;
			fetch(URLS.respond, {
				method: 'POST',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify(body)
			}).catch(err => console.error('[tl-chat] Approval response failed:', err));
			if (outcome === 'APPROVED_FOR_SESSION') {
				Store.sessionApprovals.add(toolName);
			}
		},

		resolvePrompt(data) {
			const el = this._currentPrompt;
			if (!el) return;
			const status = data.outcome === 'APPROVED' ? 'Approved' :
				data.outcome === 'APPROVED_FOR_SESSION' ? 'Approved for session' :
				data.outcome === 'REJECTED' ? 'Denied' : 'Timed out';
			el.innerHTML = '<div class="tl-chat-status-pill">' + status + '</div>';
			el.className = 'tl-chat-approval-resolved';
			this._currentPrompt = null;
		},

		_dismiss(el) {
			el.querySelector('.tl-chat-approval-actions').style.display = 'none';
			el.querySelector('.tl-chat-approval-comment').style.display = 'none';
		},

		showInteraction(data) {
			const container = Render._currentToolsEl || Render._messagesEl;
			if (!container) return;

			const el = document.createElement('div');
			el.className = 'tl-chat-approval';

			if (data.type === 'text') {
				el.innerHTML =
					'<div class="tl-chat-approval-header">' + Utils.escapeHtml(data.header || 'Input requested') + '</div>' +
					'<div class="tl-chat-approval-reason">' + (data.questionHtml || Utils.escapeHtml(data.question)) + '</div>' +
					'<div class="tl-chat-approval-comment" style="display:flex">' +
					'<input type="text" placeholder="Your response...">' +
					'<button class="tl-chat-approval-btn allow">Send</button>' +
					'</div>';
				const input = el.querySelector('input');
				const btn = el.querySelector('.tl-chat-approval-btn');
				const respond = () => {
					fetch(URLS.respond, {
						method: 'POST',
						headers: { 'Content-Type': 'application/json' },
						body: JSON.stringify({ requestId: data.requestId, response: input.value })
					}).catch(err => console.error('[tl-chat] Interaction response failed:', err));
					el.innerHTML = '<div class="tl-chat-status-pill">Responded</div>';
				};
				btn.addEventListener('click', respond);
				input.addEventListener('keydown', (e) => { if (e.key === 'Enter') respond(); });
			} else if (data.type === 'choice') {
				let optionsHtml = '<div class="tl-chat-approval-actions">';
				(data.options || []).forEach(opt => {
					const label = typeof opt === 'string' ? opt : opt.label;
					optionsHtml += '<button class="tl-chat-approval-btn" data-value="' +
						Utils.escapeHtml(label) + '">' + Utils.escapeHtml(label) + '</button>';
				});
				optionsHtml += '</div>';
				el.innerHTML =
					'<div class="tl-chat-approval-header">' + Utils.escapeHtml(data.header || 'Choose') + '</div>' +
					'<div class="tl-chat-approval-reason">' + (data.questionHtml || Utils.escapeHtml(data.question)) + '</div>' +
					optionsHtml;
				el.querySelectorAll('[data-value]').forEach(btn => {
					btn.addEventListener('click', () => {
						fetch(URLS.respond, {
							method: 'POST',
							headers: { 'Content-Type': 'application/json' },
							body: JSON.stringify({ requestId: data.requestId, response: btn.dataset.value })
						}).catch(err => console.error('[tl-chat] Choice response failed:', err));
						el.innerHTML = '<div class="tl-chat-status-pill">Selected: ' +
							Utils.escapeHtml(btn.dataset.value) + '</div>';
					});
				});
			} else {
				this.showPrompt({
					approvalRequestId: data.requestId,
					toolName: data.header || 'Action',
					displayReason: data.question
				});
				return;
			}

			container.appendChild(el);
			Render._scrollToBottom();
		}
	};

	// ========================================================================
	// Tokens — Token usage display
	// ========================================================================
	const Tokens = {
		createBadge(usage) {
			if (!usage) return document.createTextNode('');
			const total = (usage.inputTokens || 0) + (usage.outputTokens || 0) + (usage.thinkingTokens || 0);
			const badge = document.createElement('span');
			badge.className = 'tl-chat-token-badge';
			badge.textContent = Utils.formatTokens(total) + ' tokens';
			badge.addEventListener('click', (e) => this._showPopover(e, usage));
			return badge;
		},

		showRunUsage(usage) {
			if (usage) {
				Store.cumulativeUsage = usage;
			}
		},

		_showPopover(e, usage) {
			const existing = document.querySelector('.tl-chat-token-popover');
			if (existing) existing.remove();

			const pop = document.createElement('div');
			pop.className = 'tl-chat-token-popover';
			pop.innerHTML =
				this._row('Input', usage.inputTokens) +
				this._row('Output', usage.outputTokens) +
				this._row('Thinking', usage.thinkingTokens) +
				this._row('Cache read', usage.cacheReadTokens) +
				this._row('Cache write', usage.cacheWriteTokens) +
				this._row('Total',
					(usage.inputTokens || 0) + (usage.outputTokens || 0) + (usage.thinkingTokens || 0),
					true);

			document.body.appendChild(pop);

			const rect = e.target.getBoundingClientRect();
			pop.style.left = rect.left + 'px';
			pop.style.top = (rect.bottom + 4) + 'px';

			const close = (ev) => {
				if (!pop.contains(ev.target) && ev.target !== e.target) {
					pop.remove();
					document.removeEventListener('click', close);
				}
			};
			setTimeout(() => document.addEventListener('click', close), 0);
		},

		_row(label, value, isTotal) {
			return '<div class="tl-chat-token-row' + (isTotal ? ' total' : '') + '">' +
				'<span>' + label + '</span>' +
				'<span class="tl-chat-token-value">' + Utils.formatTokens(value || 0) + '</span></div>';
		}
	};

	// ========================================================================
	// Timeline — Status pills for transactions/compaction
	// ========================================================================
	const Timeline = {
		addPill(text, variant) {
			const container = Render._currentToolsEl || Render._messagesEl;
			if (!container) return;
			const pill = document.createElement('div');
			pill.className = 'tl-chat-status-pill' + (variant ? ' ' + variant : '');
			pill.textContent = text;
			container.appendChild(pill);
		}
	};

	// ========================================================================
	// Errors — Provider error display
	// ========================================================================
	const Errors = {
		_messages: {
			RATE_LIMITED: 'Rate limited. The agent will retry automatically.',
			OVERLOADED: 'Provider overloaded. Retrying...',
			CONTEXT_WINDOW_EXCEEDED: 'Context window full. Compacting conversation...',
			AUTHENTICATION: 'Authentication error. Check your API key configuration.',
			INVALID_REQUEST: 'Request error',
			UNKNOWN: 'An error occurred',
		},

		_transient: new Set(['RATE_LIMITED', 'OVERLOADED', 'CONTEXT_WINDOW_EXCEEDED']),

		show(data) {
			const container = Render._currentToolsEl || Render._messagesEl;
			if (!container) return;
			const isTransient = this._transient.has(data.kind);
			const msg = this._messages[data.kind] || this._messages.UNKNOWN;

			const el = document.createElement('div');
			el.className = 'tl-chat-error-block' + (isTransient ? ' transient' : '');
			el.innerHTML =
				'<div class="tl-chat-error-header">' +
				'<span>&#9888;</span> ' + Utils.escapeHtml(msg) +
				'</div>' +
				(data.errorMessage ? '<div class="tl-chat-error-message">' +
					Utils.escapeHtml(data.errorMessage) + '</div>' : '');
			container.appendChild(el);
			Render._scrollToBottom();
		}
	};

	// ========================================================================
	// Session — Session management (delegates to existing sidebar logic)
	// ========================================================================
	const Session = {
		currentId: null,

		switchTo(sessionId) {
			this.currentId = sessionId;
			SSE.connect(sessionId);
		}
	};

	// ========================================================================
	// Init — Bootstrap
	// ========================================================================
	function initChat(root) {
		Render.init(root);

		const sessionId = root.dataset.sessionId || '';
		Session.switchTo(sessionId);

		State.onChange((newState) => {
			Render.updateInputState();
		});
	}

	// ========================================================================
	// Bootstrap — DOM observation and initialization
	// ========================================================================
	function run() {
		const roots = document.querySelectorAll('[data-tl-chat="v1"]');
		roots.forEach(initChat);

		const observer = new MutationObserver((mutations) => {
			for (const m of mutations) {
				for (const node of m.addedNodes) {
					if (node.nodeType !== 1) continue;
					if (node.matches && node.matches('[data-tl-chat="v1"]')) {
						initChat(node);
					}
					const nested = node.querySelectorAll && node.querySelectorAll('[data-tl-chat="v1"]');
					if (nested) nested.forEach(initChat);
				}
				for (const node of m.removedNodes) {
					if (node.nodeType !== 1) continue;
					if (node.matches && node.matches('[data-tl-chat="v1"]')) {
						SSE.disconnect();
					}
				}
			}
		});
		observer.observe(document.body, { childList: true, subtree: true });
	}

	if (document.readyState === 'loading') {
		document.addEventListener('DOMContentLoaded', run);
	} else {
		run();
	}

	window.addEventListener('beforeunload', () => SSE.disconnect());
})();
