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
		activeInteractionRequestId: null,

		resetRun() {
			this.currentRun = {
				runId: null,
				model: null,
				textBuffer: '',
				thinkingBuffer: '',
				pendingOutputText: null,
				toolCalls: new Map(),
				delegations: new Map(),
				turnUsage: null,
				totalUsage: null,
				cumulativeTokens: 0,
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
				isDelegation, progressMessage: null
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

		updateToolProgress(id, message) {
			const tc = this.currentRun?.toolCalls.get(id);
			if (tc) {
				tc.progressMessage = message;
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
				tool_progress: Handlers.onToolProgress,
				approval_required: Handlers.onApprovalRequired,
				approval_resolved: Handlers.onApprovalResolved,
				tx_began: Handlers.onTxBegan,
				tx_committed: Handlers.onTxCommitted,
				tx_rollback: Handlers.onTxRollback,
				compaction_started: Handlers.onCompactionStarted,
				provider_error: Handlers.onProviderError,
				message_complete: Handlers.onMessageComplete,
				user_interaction: Handlers.onUserInteraction,
				delegation_event: Handlers.onDelegationEvent,
				approval_mode: Handlers.onApprovalMode,
				task_update: Handlers.onTaskUpdate,
			};

			for (const [type, handler] of Object.entries(handlers)) {
				this._source.addEventListener(type, (e) => {
					try {
						StaleRecovery.touch();
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
			StaleRecovery.start();
		},

		onRunCompleted(data) {
			StaleRecovery.stop();
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
				if (data.turnUsage) {
					Store.currentRun.cumulativeTokens +=
						(data.turnUsage.inputTokens || 0) +
						(data.turnUsage.outputTokens || 0) +
						(data.turnUsage.thinkingTokens || 0);
					Render._updateTraceSummary();
				}
			}
		},

		onTextDelta(data) {
			if (Store.currentRun) {
				// New text arriving — flush any pending output from a previous turn into the trace
				if (Store.currentRun.pendingOutputText) {
					Tools.renderOutput(Store.currentRun.pendingOutputText);
					Store.currentRun.pendingOutputText = null;
					Render.clearStreamingText();
				}
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
			// Stash text for later — it stays visible until the next turn's text arrives
			if (Store.currentRun && Store.currentRun.textBuffer.length > 0) {
				Store.currentRun.pendingOutputText = Store.currentRun.textBuffer;
				Store.currentRun.textBuffer = '';
			}
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

		onToolProgress(data) {
			Store.updateToolProgress(data.toolCallId, data.message);
			Tools.updateToolProgress(data.toolCallId);
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

		onProviderError(data) {
			StaleRecovery.stop();
			Errors.show(data);
			// If no run_completed will follow (error outside AgentLoop), reset UI state
			if (State.isActive()) {
				State.set(State.ERROR);
				Render.finalizeAssistantBubble();
				Render.updateInputState();
			}
		},

		onMessageComplete(data) {
			Render.swapToRenderedHtml(data.contentHtml);
		},

		onUserInteraction(data) {
			console.log('[tl-chat] user_interaction:', JSON.stringify(data));
			Approval.showInteraction(data);
		},

		onApprovalMode(data) { /* Track mode changes */ },

		onTaskUpdate(data) {
			if (data.tasksJson) {
				try {
					const taskData = JSON.parse(data.tasksJson);
					TaskPanel.update(taskData.tasks || []);
				} catch (e) {
					console.error('[tl-chat] Failed to parse task update:', e);
				}
			}
		},

		onDelegationEvent(data) {
			const delId = data.delegationToolCallId;
			const del = Store.currentRun ? Store.currentRun.delegations.get(delId) : null;
			if (!del) return;

			const innerType = data.eventType;
			const inner = typeof data.eventData === 'string' ? JSON.parse(data.eventData) : data.eventData;

			if (innerType === 'tool_started') {
				del.childToolCallIds.push(inner.toolCallId);
				Store.addToolCall(inner.toolCallId, inner.toolName, inner.argsJson);
				Tools.renderChildToolCall(delId, inner.toolCallId);
			} else if (innerType === 'tool_completed') {
				Store.completeToolCall(inner.toolCallId, inner.result, inner.durationMs);
				Tools.updateToolCall(inner.toolCallId);
			} else if (innerType === 'tool_failed') {
				Store.failToolCall(inner.toolCallId, inner.errorMessage, inner.durationMs);
				Tools.updateToolCall(inner.toolCallId);
			} else if (innerType === 'tool_progress') {
				Store.updateToolProgress(inner.toolCallId, inner.message);
				Tools.updateToolProgress(inner.toolCallId);
			} else if (innerType === 'text_delta') {
				if (!del.textBuffer) del.textBuffer = '';
				del.textBuffer += inner.text;
				Render._updateDelegationText(delId);
			} else if (innerType === 'thinking_delta') {
				if (!del.thinkingBuffer) del.thinkingBuffer = '';
				del.thinkingBuffer += inner.text;
			} else if (innerType === 'approval_required') {
				State.set(State.AWAITING_APPROVAL);
				Approval.showPrompt(inner);
				Render.updateInputState();
			} else if (innerType === 'approval_resolved') {
				Approval.resolvePrompt(inner);
				if (State.get() === State.AWAITING_APPROVAL) {
					State.set(State.STREAMING);
					Render.updateInputState();
				}
			} else if (innerType === 'turn_completed' && inner.turnUsage) {
				if (!del.tokenUsage) del.tokenUsage = { inputTokens: 0, outputTokens: 0, thinkingTokens: 0 };
				del.tokenUsage.inputTokens += inner.turnUsage.inputTokens || 0;
				del.tokenUsage.outputTokens += inner.turnUsage.outputTokens || 0;
				del.tokenUsage.thinkingTokens += inner.turnUsage.thinkingTokens || 0;
				Render._updateDelegationHeader(delId);
			} else if (innerType === 'run_completed' && inner.totalUsage) {
				del.totalUsage = inner.totalUsage;
				Render._updateDelegationHeader(delId);
			}
		},
	};

	// ========================================================================
	// TaskPanel — persistent task panel displayed above the input area
	// ========================================================================

	/** Persistent task panel displayed above the input area. */
	const TaskPanel = {
		_el: null,
		_tasks: [],
		_manualToggle: false,
		_collapsed: false,

		init(root) {
			this._el = root.querySelector('.chat-task-panel');
		},

		reset() {
			this._tasks = [];
			this._manualToggle = false;
			this._collapsed = false;
			if (this._el) {
				this._el.classList.remove('visible');
				this._el.innerHTML = '';
			}
		},

		update(tasks) {
			this._tasks = tasks;
			if (!this._el) return;

			if (tasks.length === 0) {
				this._el.classList.remove('visible');
				return;
			}

			// Auto-collapse when all completed (unless user manually toggled)
			const allDone = tasks.every(t => t.status === 'completed');
			if (!this._manualToggle) {
				this._collapsed = allDone;
			}

			this._el.classList.add('visible');
			this._render(tasks);
		},

		_render(tasks) {
			const completed = tasks.filter(t => t.status === 'completed').length;
			const total = tasks.length;

			// Build visible items with truncation
			const visible = this._truncate(tasks);

			let html = '';

			// Header
			html += '<div class="chat-task-panel-header">';
			html += '<span class="chat-task-panel-toggle">\u25BC</span>';
			html += '<span>Tasks</span>';
			html += '<span class="chat-task-panel-count">' + completed + '/' + total + ' completed</span>';
			html += '</div>';

			// Body
			html += '<div class="chat-task-panel-body">';
			for (const item of visible) {
				if (item.collapsed) {
					html += '<div class="chat-task-panel-collapsed-row">';
					html += '\u00B7\u00B7\u00B7 ' + item.count + ' more task' + (item.count > 1 ? 's' : '') + ' \u00B7\u00B7\u00B7';
					html += '</div>';
				} else {
					const icon = item.status === 'completed' ? '\u2713'
					           : item.status === 'in_progress' ? '\u25C9'
					           : '\u25CB';
					html += '<div class="chat-task-panel-item" data-status="' + item.status + '">';
					html += '<span class="task-icon">' + icon + '</span>';
					html += '<span class="task-label">' + Utils.escapeHtml(item.subject) + '</span>';
					html += '</div>';
				}
			}
			html += '</div>';

			this._el.innerHTML = html;
			this._el.classList.toggle('collapsed', this._collapsed);

			const header = this._el.querySelector('.chat-task-panel-header');
			if (header) {
				header.addEventListener('click', () => TaskPanel.toggle());
			}
		},

		_truncate(tasks) {
			if (tasks.length <= 5) {
				return tasks;
			}

			// Find in-progress task index
			const inProgressIdx = tasks.findIndex(t => t.status === 'in_progress');

			// Last 4 tasks by array position
			const lastFourStart = tasks.length - 4;

			// Determine which indices to show
			const showIndices = new Set();

			// Always show the in-progress task
			if (inProgressIdx >= 0) {
				showIndices.add(inProgressIdx);
			}

			// Always show the last 4
			for (let i = lastFourStart; i < tasks.length; i++) {
				showIndices.add(i);
			}

			// If we have fewer than 5 visible, add from the end until we have 5
			if (showIndices.size < 5) {
				for (let i = tasks.length - 1; i >= 0 && showIndices.size < 5; i--) {
					showIndices.add(i);
				}
			}

			// Build result with collapse marker
			const result = [];
			let collapsedCount = 0;
			let collapseInserted = false;

			for (let i = 0; i < tasks.length; i++) {
				if (showIndices.has(i)) {
					if (collapsedCount > 0 && !collapseInserted) {
						result.push({ collapsed: true, count: collapsedCount });
						collapseInserted = true;
					}
					result.push(tasks[i]);
				} else {
					collapsedCount++;
				}
			}

			// Safety: if collapsed items at the end
			if (collapsedCount > 0 && !collapseInserted) {
				result.push({ collapsed: true, count: collapsedCount });
			}

			return result;
		},

		toggle() {
			this._manualToggle = true;
			this._collapsed = !this._collapsed;
			if (this._el) {
				this._el.classList.toggle('collapsed', this._collapsed);
			}
		},

	};

	// ========================================================================
	// Attachments — file selection, drag-and-drop, preview, and encoding
	// ========================================================================
	const Attachments = {
		_MAX_FILES: 5,
		_MAX_SIZE: 10 * 1024 * 1024,
		_ALLOWED_TYPES: new Set([
			'image/png', 'image/jpeg', 'image/gif', 'image/webp', 'application/pdf'
		]),
		_pending: [],

		get() { return this._pending; },

		clear() {
			this._pending = [];
			this._renderAll();
		},

		addFiles(files, previewEl) {
			for (const file of files) {
				if (this._pending.length >= this._MAX_FILES) break;
				if (!this._ALLOWED_TYPES.has(file.type)) continue;
				if (file.size > this._MAX_SIZE) continue;
				this._pending.push(file);
			}
			this._renderAll(previewEl);
		},

		remove(index, previewEl) {
			this._pending.splice(index, 1);
			this._renderAll(previewEl);
		},

		/** Encode all pending files as base64 AttachmentDto objects. */
		async encode() {
			const result = [];
			for (const file of this._pending) {
				const data = await this._readBase64(file);
				result.push({
					fileName: file.name,
					mimeType: file.type,
					base64Data: data,
					size: file.size
				});
			}
			return result;
		},

		_readBase64(file) {
			return new Promise((resolve, reject) => {
				const reader = new FileReader();
				reader.onload = () => {
					// Strip the data-URL prefix (e.g. "data:image/png;base64,")
					resolve(reader.result.split(',')[1]);
				};
				reader.onerror = reject;
				reader.readAsDataURL(file);
			});
		},

		_renderAll(previewEl) {
			// Try to find the visible preview element
			const el = previewEl || this._findVisiblePreview();
			if (!el) return;
			el.innerHTML = '';
			if (this._pending.length === 0) {
				el.classList.remove('has-items');
				return;
			}
			el.classList.add('has-items');
			this._pending.forEach((file, i) => {
				const item = document.createElement('div');
				item.className = 'chat-attachment-preview-item';

				if (file.type.startsWith('image/')) {
					const img = document.createElement('img');
					img.src = URL.createObjectURL(file);
					item.appendChild(img);
				} else {
					const icon = document.createElement('span');
					icon.textContent = '\uD83D\uDCC4'; // page icon
					icon.style.fontSize = '1.5em';
					item.appendChild(icon);
				}

				const name = document.createElement('span');
				name.className = 'chat-attachment-name';
				name.textContent = file.name;
				item.appendChild(name);

				const removeBtn = document.createElement('button');
				removeBtn.className = 'chat-attachment-remove';
				removeBtn.textContent = '\u00D7';
				removeBtn.addEventListener('click', () => this.remove(i, el));
				item.appendChild(removeBtn);

				el.appendChild(item);
			});
		},

		_findVisiblePreview() {
			// Welcome preview or main input preview — whichever is visible
			const welcome = document.querySelector('.chat-welcome-attachment-preview');
			if (welcome && welcome.offsetParent !== null) return welcome;
			const main = Render._previewEl;
			return main || null;
		},

		/** Wire up attach button, file input, and drag-and-drop on a container. */
		setup(attachBtn, fileInput, dropTarget, previewEl) {
			if (attachBtn && fileInput) {
				attachBtn.addEventListener('click', () => fileInput.click());
				fileInput.addEventListener('change', () => {
					this.addFiles(fileInput.files, previewEl);
					fileInput.value = '';
				});
			}
			if (dropTarget) {
				dropTarget.addEventListener('dragover', (e) => {
					e.preventDefault();
					dropTarget.classList.add('drag-over');
				});
				dropTarget.addEventListener('dragleave', () => {
					dropTarget.classList.remove('drag-over');
				});
				dropTarget.addEventListener('drop', (e) => {
					e.preventDefault();
					dropTarget.classList.remove('drag-over');
					if (e.dataTransfer.files.length > 0) {
						this.addFiles(e.dataTransfer.files, previewEl);
					}
				});
			}
		}
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
		_previewEl: null,
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
			this._inputSection = root.querySelector('.chat-input-section');
			this._previewEl = document.getElementById(root.dataset.chatPreviewId);
			this._ensureParentHeight(root);
			this._setupCancelButton();
			this._setupInput();
			this._setupAutoScroll();
			this._setupAttachments(root);
			TaskPanel.init(root);
		},

		_ensureParentHeight(root) {
			// Walk up the DOM to find the TL content container and ensure height propagation
			let el = root.parentElement;
			while (el && el !== document.body) {
				const pos = getComputedStyle(el).position;
				if (pos === 'relative' || pos === 'absolute') {
					// Found a positioned ancestor — make sure root fills it
					root.style.position = 'absolute';
					root.style.inset = '0';
					break;
				}
				el = el.parentElement;
			}
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

		async _send() {
			const text = this._inputEl.value.trim();
			if (!text && Attachments.get().length === 0) return;

			// During an active askUser interaction, route input as the tool response
			if (Store.activeInteractionRequestId) {
				this._inputEl.value = '';
				const reqId = Store.activeInteractionRequestId;
				Store.activeInteractionRequestId = null;
				Approval._removeInteractionUI();
				if (this._inputEl) { this._inputEl.disabled = false; this._inputEl.placeholder = 'Type a message...'; }
				fetch(URLS.respond, {
					method: 'POST',
					headers: { 'Content-Type': 'application/json' },
					body: JSON.stringify({ requestId: reqId, response: text })
				}).catch(err => console.error('[tl-chat] Interaction response failed:', err));
				return;
			}

			if (State.isActive()) return;
			this._inputEl.value = '';

			try {
				const attachments = await Attachments.encode();
				Attachments.clear();
				// Ensure session exists before appending message (createNew clears the DOM)
				const sessionId = await Session._ensureSession();
				this.appendUserMessage(text, attachments);
				await fetch(URLS.send, {
					method: 'POST',
					headers: { 'Content-Type': 'application/json' },
					body: JSON.stringify({
						text: text,
						sessionId: sessionId || '',
						attachments: attachments
					})
				});
			} catch (err) {
				console.error('[tl-chat] Send failed:', err);
				this._inputEl.value = text;
			}
		},

		_setupAttachments(root) {
			const fileInput = document.getElementById(root.dataset.chatFileInputId);
			const attachBtn = document.getElementById(root.dataset.chatAttachBtnId);
			Attachments.setup(attachBtn, fileInput, this._inputSection, this._previewEl);
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

		appendUserMessage(text, attachments) {
			const div = document.createElement('div');
			div.className = 'chat-message chat-message-user';
			let html = '<div class="chat-bubble chat-bubble-user">' +
				Utils.escapeHtml(text);
			if (attachments && attachments.length > 0) {
				html += '<div class="chat-message-attachments">';
				for (const att of attachments) {
					const icon = att.mimeType === 'application/pdf'
						? '<svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>'
						: '<svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>';
					html += '<span class="chat-attachment-badge">' + icon +
						Utils.escapeHtml(att.fileName) + '</span>';
				}
				html += '</div>';
			}
			html += '</div>';
			div.innerHTML = html;
			this._messagesEl.appendChild(div);
			this._scrollToBottom();
		},

		appendAssistantBubble() {
			const div = document.createElement('div');
			div.className = 'chat-message chat-message-agent';

			const content = document.createElement('div');
			content.className = 'chat-bubble chat-bubble-agent';

			// Collapsible trace wrapper (tools + thinking)
			const traceWrapper = document.createElement('div');
			traceWrapper.className = 'tl-chat-trace-wrapper';
			traceWrapper.style.display = 'none';

			const traceHeader = document.createElement('div');
			traceHeader.className = 'tl-chat-trace-header';
			const traceTokensEl = document.createElement('span');
			traceTokensEl.className = 'tl-chat-trace-tokens';
			traceTokensEl.addEventListener('click', (e) => {
				e.stopPropagation();
				if (traceTokensEl._usage) {
					Tokens._showPopover(e, traceTokensEl._usage);
				}
			});

			traceHeader.innerHTML =
				'<span class="tl-chat-trace-chevron">&#9654;</span>' +
				'<span class="tl-chat-trace-summary"></span>';
			traceHeader.appendChild(traceTokensEl);

			const traceBody = document.createElement('div');
			traceBody.className = 'tl-chat-trace-body';
			traceBody.style.display = 'none';

			let traceExpanded = false;
			traceHeader.addEventListener('click', () => {
				traceExpanded = !traceExpanded;
				traceBody.style.display = traceExpanded ? '' : 'none';
				traceHeader.querySelector('.tl-chat-trace-chevron').classList.toggle('expanded', traceExpanded);
			});

			const thinkingEl = document.createElement('div');
			thinkingEl.className = 'tl-chat-thinking';
			thinkingEl.style.display = 'none';
			traceBody.appendChild(thinkingEl);

			const toolsEl = document.createElement('div');
			toolsEl.className = 'tl-chat-tools-container';
			traceBody.appendChild(toolsEl);

			traceWrapper.appendChild(traceHeader);
			traceWrapper.appendChild(traceBody);
			content.appendChild(traceWrapper);

			// Answer text (always visible)
			const textEl = document.createElement('div');
			textEl.className = 'chat-agent-text tl-chat-content-swap';
			content.appendChild(textEl);

			div.appendChild(content);
			this._messagesEl.appendChild(div);

			this._currentBubble = content;
			this._currentTextEl = textEl;
			this._currentToolsEl = toolsEl;
			this._currentTraceWrapper = traceWrapper;
			this._toolCount = 0;
			this._scrollToBottom();
		},

		clearStreamingText() {
			if (this._currentTextEl) {
				this._currentTextEl.innerHTML = '';
			}
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
			// _currentTextEl may already be null if run_completed arrived before message_complete
			const el = this._currentTextEl ||
				(this._messagesEl && this._messagesEl.querySelector(
					'.chat-message-agent:last-child .tl-chat-content-swap'));
			if (!el) return;
			el.style.opacity = '0';
			setTimeout(() => {
				if (!el.parentNode) return;
				el.innerHTML = html;
				el.style.opacity = '1';
			}, 150);
		},

		_updateTraceSummary() {
			if (!this._currentTraceWrapper) return;
			const summary = this._currentTraceWrapper.querySelector('.tl-chat-trace-summary');
			if (!summary) return;
			const n = this._toolCount || 0;
			summary.textContent = n > 0 ? n + ' tool' + (n > 1 ? 's' : '') + ' used' : 'Thinking';
			const tokensEl = this._currentTraceWrapper.querySelector('.tl-chat-trace-tokens');
			if (tokensEl && Store.currentRun && Store.currentRun.cumulativeTokens > 0) {
				tokensEl.textContent = Utils.formatTokens(Store.currentRun.cumulativeTokens) + ' tokens';
				tokensEl._usage = Store.currentRun.turnUsage;
			}
		},

		_updateDelegationText(delegationToolCallId) {
			const root = this._root || document;
			const delEl = root.querySelector('[data-tool-call-id="' + delegationToolCallId + '"]');
			if (!delEl) return;
			const textEl = delEl.querySelector('.tl-chat-delegation-text');
			if (!textEl) return;
			const del = Store.currentRun.delegations.get(delegationToolCallId);
			if (!del || !del.textBuffer) return;
			textEl.innerHTML = Markdown.basicFormat(del.textBuffer);
			this._scrollToBottom();
		},

		_updateDelegationHeader(delegationToolCallId) {
			const root = this._root || document;
			const delEl = root.querySelector('[data-tool-call-id="' + delegationToolCallId + '"]');
			if (!delEl) return;
			const tokensEl = delEl.querySelector('.tl-chat-delegation-tokens');
			if (!tokensEl) return;
			const del = Store.currentRun.delegations.get(delegationToolCallId);
			if (!del) return;
			const usage = del.totalUsage || del.tokenUsage;
			if (usage) {
				const total = (usage.inputTokens || 0) + (usage.outputTokens || 0) + (usage.thinkingTokens || 0);
				tokensEl.textContent = Utils.formatTokens(total) + ' tokens';
			}
		},

		finalizeAssistantBubble() {
			// Flush any pending output into the trace before finalizing
			if (Store.currentRun && Store.currentRun.pendingOutputText) {
				Tools.renderOutput(Store.currentRun.pendingOutputText);
				Store.currentRun.pendingOutputText = null;
			}
			if (this._currentTextEl) {
				const cursor = this._currentTextEl.querySelector('.tl-chat-streaming-cursor');
				if (cursor) cursor.remove();
			}
			// Final token count update
			if (Store.currentRun && Store.currentRun.totalUsage) {
				const usage = Store.currentRun.totalUsage;
				const total = (usage.inputTokens || 0) + (usage.outputTokens || 0) + (usage.thinkingTokens || 0);
				const traceVisible = this._currentTraceWrapper && this._currentTraceWrapper.style.display !== 'none';
				if (traceVisible) {
					// Trace is visible (tools/thinking used) — update token count in trace header
					const tokensEl = this._currentTraceWrapper.querySelector('.tl-chat-trace-tokens');
					if (tokensEl && total > 0) {
						tokensEl.textContent = Utils.formatTokens(total) + ' tokens';
						tokensEl._usage = usage;
					}
				} else if (this._currentBubble && total > 0) {
					// No trace visible (text-only message) — add standalone token badge
					const badge = Tokens.createBadge(usage);
					if (badge.nodeType === Node.ELEMENT_NODE) {
						this._currentBubble.insertBefore(badge, this._currentBubble.firstChild);
					}
				}
			}
			this._currentBubble = null;
			this._currentTextEl = null;
			this._currentToolsEl = null;
			this._currentTraceWrapper = null;
			this._toolCount = 0;
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
				// Show trace wrapper when thinking starts
				if (Render._currentTraceWrapper) {
					Render._currentTraceWrapper.style.display = '';
				}
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
		renderOutput(text) {
			const container = Render._currentToolsEl;
			if (!container || !text) return;

			if (Render._currentTraceWrapper) {
				Render._currentTraceWrapper.style.display = '';
			}

			const el = document.createElement('div');
			el.className = 'tl-chat-tool';

			const header = document.createElement('div');
			header.className = 'tl-chat-tool-header';
			const stripped = text.replace(/\s+/g, ' ').trim();
			const preview = stripped.length > 60 ? stripped.substring(0, 60) + '...' : stripped;
			header.innerHTML =
				'<span class="tl-chat-status-dot success"></span>' +
				'<span class="tl-chat-tool-name-fixed">Output</span>' +
				'<span class="tl-chat-output-summary">' + Utils.escapeHtml(preview) + '</span>' +
				'<span class="tl-chat-tool-tokens">~' + Utils.formatTokens(this._estimateTokens(text)) + ' tokens</span>' +
				'<span class="tl-chat-tool-chevron">&#9654;</span>';

			const body = document.createElement('div');
			body.className = 'tl-chat-tool-body';
			body.style.display = 'none';
			body.innerHTML = '<div class="tl-chat-output-content">' + Markdown.basicFormat(text) + '</div>';

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

		renderToolCall(toolCallId) {
			const container = Render._currentToolsEl;
			if (!container) return;
			const tc = Store.currentRun.toolCalls.get(toolCallId);
			if (!tc) return;

			// Show trace wrapper and update summary
			if (Render._currentTraceWrapper) {
				Render._currentTraceWrapper.style.display = '';
				Render._toolCount = (Render._toolCount || 0) + 1;
				Render._updateTraceSummary();
			}

			if (tc.isDelegation) {
				this._renderDelegation(container, toolCallId, tc);
			} else {
				this._renderTool(container, toolCallId, tc);
			}
			Render._scrollToBottom();
		},

		_estimateTokens(text) {
			if (!text) return 0;
			return Math.ceil(text.length / 4);
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
				'<span class="tl-chat-tool-tokens">~' + Utils.formatTokens(this._estimateTokens(tc.argsJson)) + ' tokens</span>' +
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
				'<span class="tl-chat-delegation-tokens"></span>' +
				'<span class="tl-chat-tool-duration"></span>' +
				'<span class="tl-chat-tool-chevron">&#9654;</span>';
			el.appendChild(header);

			const childContainer = document.createElement('div');
			childContainer.className = 'tl-chat-delegation-tools';
			el.appendChild(childContainer);

			const textEl = document.createElement('div');
			textEl.className = 'tl-chat-delegation-text';
			childContainer.appendChild(textEl);

			let expanded = true;
			header.addEventListener('click', () => {
				expanded = !expanded;
				childContainer.style.display = expanded ? '' : 'none';
				header.querySelector('.tl-chat-tool-chevron').classList.toggle('expanded', expanded);
			});

			container.appendChild(el);
		},

		renderChildToolCall(delegationToolCallId, childToolCallId) {
			const root = Render._root || document;
			const delEl = root.querySelector('[data-tool-call-id="' + delegationToolCallId + '"]');
			if (!delEl) return;
			const childContainer = delEl.querySelector('.tl-chat-delegation-tools');
			if (!childContainer) return;

			const tc = Store.currentRun.toolCalls.get(childToolCallId);
			if (!tc) return;

			this._renderTool(childContainer, childToolCallId, tc);
			Render._scrollToBottom();
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

			const tokensEl = el.querySelector('.tl-chat-tool-tokens');
			if (tokensEl) {
				const total = this._estimateTokens(tc.argsJson) + this._estimateTokens(tc.result) + this._estimateTokens(tc.error);
				tokensEl.textContent = '~' + Utils.formatTokens(total) + ' tokens';
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

		updateToolProgress(toolCallId) {
			const tc = Store.currentRun?.toolCalls.get(toolCallId);
			if (!tc || !tc.progressMessage) return;
			const root = Render._root || document;
			const el = root.querySelector('[data-tool-call-id="' + toolCallId + '"]');
			if (!el) return;
			const header = el.querySelector('.tl-chat-tool-header');
			if (!header) return;
			let progress = header.querySelector('.tl-chat-tool-progress');
			if (!progress) {
				progress = document.createElement('span');
				progress.className = 'tl-chat-tool-progress';
				const name = header.querySelector('.tl-chat-tool-name');
				if (name) {
					name.after(progress);
				}
			}
			progress.textContent = tc.progressMessage;
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
			const section = Render._inputSection;
			if (!section) return;

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

			// Insert at the top of the input section, above the input row
			section.insertBefore(el, section.firstChild);
			this._currentPrompt = el;
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
			// Remove resolved prompt from input section after brief display
			setTimeout(() => { if (el.parentNode) el.parentNode.removeChild(el); }, 2000);
		},

		_dismiss(el) {
			el.querySelector('.tl-chat-approval-actions').style.display = 'none';
			el.querySelector('.tl-chat-approval-comment').style.display = 'none';
		},

		showInteraction(data) {
			const section = Render._inputSection;
			if (!section) return;

			// Track active interaction so the chat input can respond
			Store.activeInteractionRequestId = data.requestId;
			this._interactionEl = null;

			// Enable the input field so user can type a custom answer
			if (Render._inputEl) {
				Render._inputEl.disabled = false;
				Render._inputEl.placeholder = data.type === 'choice'
					? 'Type your answer and press Enter or pick above...'
					: 'Type your answer and press Enter...';
				Render._inputEl.focus();
			}

			const el = document.createElement('div');
			el.className = 'tl-chat-approval';
			this._interactionEl = el;

			const dismiss = () => {
				Store.activeInteractionRequestId = null;
				el.remove();
				this._interactionEl = null;
				if (Render._inputEl) { Render._inputEl.disabled = false; Render._inputEl.placeholder = 'Type a message...'; }
			};

			const respond = (response) => {
				fetch(URLS.respond, {
					method: 'POST',
					headers: { 'Content-Type': 'application/json' },
					body: JSON.stringify({ requestId: data.requestId, response: response })
				}).catch(err => console.error('[tl-chat] Interaction response failed:', err));
				dismiss();
			};

			if (data.type === 'text') {
				el.innerHTML =
					'<div class="tl-chat-approval-header">' + Utils.escapeHtml(data.header || 'Input requested') + '</div>' +
					'<div class="tl-chat-approval-reason">' + (data.questionHtml || Utils.escapeHtml(data.question)) + '</div>';
			} else if (data.type === 'choice') {
				const multiSelect = data.multiSelect === true;

				if (multiSelect) {
					let optionsHtml = '<div class="tl-chat-choice-options">';
					(data.options || []).forEach(opt => {
						const label = typeof opt === 'string' ? opt : opt.label;
						optionsHtml += '<label class="tl-chat-choice-label">' +
							'<input type="checkbox" value="' + Utils.escapeHtml(label) + '"> ' +
							Utils.escapeHtml(label) + '</label>';
					});
					optionsHtml += '</div>';

					el.innerHTML =
						'<div class="tl-chat-approval-header">' + Utils.escapeHtml(data.header || 'Choose') + '</div>' +
						'<div class="tl-chat-approval-reason">' + (data.questionHtml || Utils.escapeHtml(data.question)) + '</div>' +
						optionsHtml +
						'<div class="tl-chat-approval-actions">' +
						'<button class="tl-chat-approval-btn allow tl-chat-submit-btn">Submit</button>' +
						'</div>';

					el.querySelector('.tl-chat-submit-btn').addEventListener('click', () => {
						const checked = Array.from(el.querySelectorAll('input[type="checkbox"]:checked'))
							.map(cb => cb.value);
						if (checked.length === 0) return;
						respond(checked.join(', '));
					});
				} else {
					// Single-select: original button layout
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
						btn.addEventListener('click', () => respond(btn.dataset.value));
					});
				}
			} else {
				console.warn('[tl-chat] Unknown interaction type:', data.type);
				return;
			}

			section.insertBefore(el, section.firstChild);
		},

		_removeInteractionUI() {
			if (this._interactionEl) {
				this._interactionEl.remove();
				this._interactionEl = null;
			}
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
	// StaleRecovery — polls /chat/updates if SSE goes silent during a run
	// ========================================================================
	const StaleRecovery = {
		_timer: null,
		_lastEventTime: 0,
		_TIMEOUT_MS: 8000,
		_POLL_MS: 3000,

		start() {
			this.touch();
			this.stop();
			this._timer = setInterval(() => this._check(), this._POLL_MS);
		},

		stop() {
			if (this._timer) {
				clearInterval(this._timer);
				this._timer = null;
			}
		},

		touch() {
			this._lastEventTime = Date.now();
		},

		async _check() {
			if (!State.isActive()) {
				this.stop();
				return;
			}
			if (Date.now() - this._lastEventTime < this._TIMEOUT_MS) return;

			try {
				const resp = await fetch(URLS.updates);
				if (!resp.ok) return;
				const data = await resp.json();
				if (!data.processing && State.isActive()) {
					this.stop();
					// Server finished but SSE didn't deliver the events — recover
					if (data.messages && data.messages.length > 0) {
						const last = data.messages[data.messages.length - 1];
						if (last.role === 'system' && last.content && last.content.startsWith('Error:')) {
							Errors.show({ kind: 'UNKNOWN', errorMessage: last.content.substring(7) });
						}
					}
					State.set(State.ERROR);
					Render.finalizeAssistantBubble();
					Render.updateInputState();
				}
			} catch (e) {
				// Poll failed — will retry
			}
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

			// Always show errors in a visible container — either the current
			// bubble's text area or the main messages list. Never in the
			// collapsed trace/tools section where they'd be invisible.
			const container = Render._currentTextEl || Render._messagesEl;
			if (!container) return;
			container.appendChild(el);
			Render._scrollToBottom();
		}
	};

	// ========================================================================
	// Session — Full session management with sidebar
	// ========================================================================
	const Session = {
		currentId: null,
		_sidebarEl: null,
		_listEl: null,

		init(root) {
			this._sidebarEl = root.querySelector('.chat-history-panel');
			this._listEl = root.querySelector('.chat-history-list');
			if (!this._sidebarEl) return;

			const newBtn = this._sidebarEl.querySelector('.chat-new-session-btn');
			if (newBtn) {
				newBtn.addEventListener('click', () => this.createNew());
			}

			const toggleBtn = this._sidebarEl.querySelector('.chat-sidebar-toggle');
			if (toggleBtn) {
				toggleBtn.addEventListener('click', () => {
					this._sidebarEl.classList.toggle('open');
				});
			}

			this.loadList();
		},

		async loadList() {
			if (!this._listEl) return;
			try {
				const resp = await fetch(URLS.sessions);
				const data = await resp.json();
				this._renderList(data.sessions || []);
			} catch (e) {
				console.error('[tl-chat] Failed to load sessions:', e);
			}
		},

		_formatRelativeTime(isoString) {
			if (!isoString) return '';
			const date = new Date(isoString);
			const diffMs = Date.now() - date.getTime();
			const diffMin = Math.floor(diffMs / 60000);
			const diffHr = Math.floor(diffMs / 3600000);
			const diffDay = Math.floor(diffMs / 86400000);
			if (diffMin < 1) return 'just now';
			if (diffMin < 60) return diffMin + 'm ago';
			if (diffHr < 24) return diffHr + 'h ago';
			if (diffDay < 7) return diffDay + 'd ago';
			return date.toLocaleDateString();
		},

		_createSessionItem(session) {
			const item = document.createElement('div');
			item.className = 'chat-history-item' + (session.id === this.currentId ? ' active' : '');
			item.dataset.sessionId = session.id;

			const titleRow = document.createElement('div');
			titleRow.className = 'chat-history-title-row';

			const title = document.createElement('span');
			title.className = 'chat-history-title';
			title.textContent = session.title || 'Untitled';

			let editing = false;

			const startEditing = (e) => {
				if (e) e.stopPropagation();
				editing = true;
				title.setAttribute('contenteditable', 'true');
				title.classList.add('editing');
				editBtn.classList.add('editing');
				title.focus();
				const range = document.createRange();
				range.selectNodeContents(title);
				const sel = window.getSelection();
				sel.removeAllRanges();
				sel.addRange(range);
			};

			const stopEditing = (e) => {
				if (e) e.stopPropagation();
				if (!editing) return;
				editing = false;
				title.removeAttribute('contenteditable');
				title.classList.remove('editing');
				editBtn.classList.remove('editing');
				const newTitle = title.textContent.trim();
				if (newTitle && newTitle !== session.title) {
					session.title = newTitle;
					fetch(URLS.sessions + '/' + encodeURIComponent(session.id) + '/title', {
						method: 'PUT',
						headers: { 'Content-Type': 'application/json' },
						body: JSON.stringify({ title: newTitle })
					}).catch(err => console.error('[tl-chat] Rename failed:', err));
				} else if (!newTitle) {
					title.textContent = session.title || 'Untitled';
				}
			};

			title.addEventListener('keydown', (e) => {
				if (e.key === 'Enter') { e.preventDefault(); stopEditing(e); }
				if (e.key === 'Escape') { e.preventDefault(); title.textContent = session.title || 'Untitled'; stopEditing(e); }
			});
			title.addEventListener('blur', () => { setTimeout(() => { if (editing) stopEditing(); }, 150); });

			const editBtn = document.createElement('button');
			editBtn.className = 'chat-history-edit-btn';
			editBtn.title = 'Rename';
			editBtn.innerHTML = '&#9998;';
			editBtn.addEventListener('click', (e) => {
				e.stopPropagation();
				if (editing) stopEditing(e); else startEditing(e);
			});

			const deleteBtn = document.createElement('button');
			deleteBtn.className = 'chat-history-delete-btn';
			deleteBtn.textContent = '\u00D7';
			deleteBtn.addEventListener('click', (e) => {
				e.stopPropagation();
				if (confirm('Delete this chat?')) {
					this.deleteSession(session.id, item);
				}
			});

			item.addEventListener('click', (e) => {
				if (e.target === deleteBtn || e.target === editBtn || editing) return;
				this.switchTo(session.id);
			});

			const timeEl = document.createElement('span');
			timeEl.className = 'chat-history-time';
			timeEl.textContent = this._formatRelativeTime(session.lastUpdated);

			titleRow.appendChild(title);
			titleRow.appendChild(editBtn);
			titleRow.appendChild(deleteBtn);
			item.appendChild(titleRow);
			item.appendChild(timeEl);
			return item;
		},

		_renderList(sessions) {
			if (!this._listEl) return;
			this._listEl.innerHTML = '';
			sessions.forEach(s => {
				this._listEl.appendChild(this._createSessionItem(s));
			});
		},

		_resetState() {
			State.set(State.IDLE);
			Store.currentRun = null;
			if (Render._messagesEl) Render._messagesEl.innerHTML = '';
			TaskPanel.reset();
			Render.updateInputState();
		},

		async switchTo(sessionId, skipLoad) {
			if (sessionId === this.currentId) return;

			this._resetState();
			this.currentId = sessionId;
			SSE.connect(sessionId);

			if (this._listEl) {
				this._listEl.querySelectorAll('.chat-history-item').forEach(el => {
					el.classList.toggle('active', el.dataset.sessionId === sessionId);
				});
			}

			if (sessionId && !skipLoad) {
				try {
					const resp = await fetch(URLS.sessions + '/' + encodeURIComponent(sessionId));
					const data = await resp.json();
					this._renderLoadedSession(data);
				} catch (e) {
					console.error('[tl-chat] Failed to load session:', e);
				}
			}
		},

		_renderLoadedSession(data) {
			if (Render._messagesEl) {
				Render._messagesEl.innerHTML = '';
			}

			// V2 format: turn-based
			const turns = data.turns || [];
			if (turns.length > 0) {
				Welcome.hide();
				this._renderTurns(turns);
				Render._scrollToBottom();
				return;
			}

			// V1 fallback: entry-based
			const entries = data.entries || [];
			if (entries.length > 0) {
				Welcome.hide();
			}
			// Group consecutive entries: task_snapshot(s) followed by agent message form one bubble
			let i = 0;
			while (i < entries.length) {
				const entry = entries[i];
				if (entry.type === 'message') {
					const msg = entry.messageData;
					if (msg) {
						if (msg.role === 'user') {
							Render.appendUserMessage(msg.content || msg.text || '');
						} else {
							this._renderPersistedAgentMessage(msg);
						}
					}
					i++;
				} else if (entry.type === 'task_snapshot') {
					// Collect consecutive task snapshots (usually just one per agent turn)
					const snapshots = [];
					while (i < entries.length && entries[i].type === 'task_snapshot') {
						snapshots.push(entries[i].taskData);
						i++;
					}
					// Next entry should be the agent message
					let agentMsg = null;
					if (i < entries.length && entries[i].type === 'message') {
						const msg = entries[i].messageData;
						if (msg && msg.role !== 'user') {
							agentMsg = msg;
							i++;
						}
					}
					this._renderPersistedTrace(snapshots, agentMsg);
				} else {
					i++;
				}
			}
			Render._scrollToBottom();
		},

		_renderTurns(turns) {
			for (const turn of turns) {
				// Render user message(s)
				const userParts = turn.userParts || [];
				for (const part of userParts) {
					if (part.type === 'text' && part.textData) {
						Render.appendUserMessage(part.textData.text || '');
					}
				}

				// Categorize assistant parts
				const assistantParts = turn.assistantParts || [];
				const toolCalls = [];
				const approvals = [];
				const interactions = [];
				const delegations = [];
				let textContent = '';
				let thinkingText = '';
				let wasCancelled = false;

				for (const part of assistantParts) {
					switch (part.type) {
						case 'tool_call': toolCalls.push(part.toolCallData); break;
						case 'approval': approvals.push(part.approvalData); break;
						case 'interaction': interactions.push(part.interactionData); break;
						case 'delegation': delegations.push(part.delegationData); break;
						case 'text': textContent += (part.textData ? part.textData.text : ''); break;
						case 'thinking': thinkingText += (part.thinkingData ? part.thinkingData.text : ''); break;
						case 'system':
							if (part.systemData && part.systemData.eventType === 'CANCELLED') {
								wasCancelled = true;
							}
							break;
						case 'error':
							if (part.errorData) {
								textContent += 'Error: ' + (part.errorData.errorMessage || part.errorData.kind || 'Unknown error');
							}
							break;
					}
				}

				const hasTrace = toolCalls.length > 0 || approvals.length > 0 ||
					interactions.length > 0 || delegations.length > 0 || thinkingText;

				const contentHtml = turn.contentHtml || '';

				if (hasTrace) {
					this._renderTurnTrace(toolCalls, approvals, interactions, delegations, thinkingText, turn.usage, textContent, contentHtml);
				} else if (textContent) {
					this._renderPersistedAgentMessage({ content: textContent, contentHtml: contentHtml, usage: turn.usage });
				}

				if (wasCancelled) {
					const cancelDiv = document.createElement('div');
					cancelDiv.className = 'chat-message chat-message-agent';
					cancelDiv.innerHTML = '<div class="chat-bubble chat-bubble-agent">' +
						'<div class="chat-agent-text"><em>Processing was cancelled</em></div></div>';
					if (Render._messagesEl) {
						Render._messagesEl.appendChild(cancelDiv);
					}
				}
			}
		},

		_renderTurnTrace(toolCalls, approvals, interactions, delegations, thinkingText, usage, textContent, contentHtml) {
			const div = document.createElement('div');
			div.className = 'chat-message chat-message-agent';
			const content = document.createElement('div');
			content.className = 'chat-bubble chat-bubble-agent';

			// Build trace wrapper
			const traceWrapper = document.createElement('div');
			traceWrapper.className = 'tl-chat-trace-wrapper';
			const traceHeader = document.createElement('div');
			traceHeader.className = 'tl-chat-trace-header';

			const totalEvents = toolCalls.length + delegations.length;
			const traceTokensEl = document.createElement('span');
			traceTokensEl.className = 'tl-chat-trace-tokens';
			if (usage) {
				const total = (usage.inputTokens || 0) + (usage.outputTokens || 0) + (usage.thinkingTokens || 0);
				if (total > 0) {
					traceTokensEl.textContent = Utils.formatTokens(total) + ' tokens';
					traceTokensEl.addEventListener('click', (e) => {
						e.stopPropagation();
						Tokens._showPopover(e, usage);
					});
				}
			}

			const summary = totalEvents > 0
				? totalEvents + ' tool' + (totalEvents > 1 ? 's' : '') + ' used'
				: 'Thinking';
			traceHeader.innerHTML =
				'<span class="tl-chat-trace-chevron">&#9654;</span>' +
				'<span class="tl-chat-trace-summary">' + summary + '</span>';
			traceHeader.appendChild(traceTokensEl);

			const traceBody = document.createElement('div');
			traceBody.className = 'tl-chat-trace-body';
			traceBody.style.display = 'none';

			let traceExpanded = false;
			traceHeader.addEventListener('click', () => {
				traceExpanded = !traceExpanded;
				traceBody.style.display = traceExpanded ? '' : 'none';
				traceHeader.querySelector('.tl-chat-trace-chevron').classList.toggle('expanded', traceExpanded);
			});

			// Render thinking
			if (thinkingText) {
				const thinkingEl = document.createElement('div');
				thinkingEl.className = 'tl-chat-thinking';
				const thinkingHeader = document.createElement('div');
				thinkingHeader.className = 'tl-chat-thinking-header';
				const preview = thinkingText.substring(0, 80) + (thinkingText.length > 80 ? '...' : '');
				thinkingHeader.innerHTML =
					'<span class="tl-chat-thinking-chevron">&#9654;</span>' +
					'<span>Thinking</span>' +
					'<span class="tl-chat-thinking-preview">' + Utils.escapeHtml(preview) + '</span>';
				const thinkingBody = document.createElement('div');
				thinkingBody.className = 'tl-chat-thinking-body';
				thinkingBody.style.display = 'none';
				thinkingBody.textContent = thinkingText;
				thinkingHeader.addEventListener('click', () => {
					const expanded = thinkingEl.dataset.expanded === 'true';
					thinkingEl.dataset.expanded = '' + !expanded;
					thinkingHeader.querySelector('.tl-chat-thinking-chevron').classList.toggle('expanded', !expanded);
					thinkingHeader.querySelector('.tl-chat-thinking-preview').style.display = !expanded ? 'none' : '';
					thinkingBody.style.display = !expanded ? '' : 'none';
				});
				thinkingEl.appendChild(thinkingHeader);
				thinkingEl.appendChild(thinkingBody);
				traceBody.appendChild(thinkingEl);
			}

			const toolsEl = document.createElement('div');
			toolsEl.className = 'tl-chat-tools-container';

			// Render tool calls
			for (const tc of toolCalls) {
				this._renderToolCallPart(toolsEl, tc);
			}

			// Render delegations
			for (const del of delegations) {
				this._renderDelegationPart(toolsEl, del);
			}

			// Render approvals
			for (const ap of approvals) {
				this._renderApprovalPart(toolsEl, ap);
			}

			// Render interactions
			for (const inter of interactions) {
				this._renderInteractionPart(toolsEl, inter);
			}

			traceBody.appendChild(toolsEl);
			traceWrapper.appendChild(traceHeader);
			traceWrapper.appendChild(traceBody);
			content.appendChild(traceWrapper);

			// Agent response text
			if (textContent) {
				const textEl = document.createElement('div');
				textEl.className = 'chat-agent-text';
				textEl.innerHTML = contentHtml || Markdown.basicFormat(textContent);
				content.appendChild(textEl);
			}

			div.appendChild(content);
			if (Render._messagesEl) {
				Render._messagesEl.appendChild(div);
			}
		},

		_renderToolCallPart(container, tc) {
			const el = document.createElement('div');
			el.className = 'tl-chat-tool';

			const isDenied = tc.status === 'DENIED';
			const isFailed = tc.status === 'FAILED';
			const statusClass = (isDenied || isFailed) ? 'error' : 'success';

			const header = document.createElement('div');
			header.className = 'tl-chat-tool-header';
			header.innerHTML =
				'<span class="tl-chat-status-dot ' + statusClass + '"></span>' +
				'<span class="tl-chat-tool-name">' + Utils.escapeHtml(tc.toolName || '') + '</span>' +
				(isDenied ? '<span class="tl-chat-tool-denied">denied</span>' : '') +
				'<span class="tl-chat-tool-tokens">~' + Utils.formatTokens(Tools._estimateTokens(tc.argsJson) + Tools._estimateTokens(tc.result)) + ' tokens</span>' +
				'<span class="tl-chat-tool-duration">' + (tc.durationMs ? Utils.formatDuration(tc.durationMs) : '') + '</span>' +
				'<span class="tl-chat-tool-chevron">&#9654;</span>';

			const body = document.createElement('div');
			body.className = 'tl-chat-tool-body';
			body.style.display = 'none';

			if (tc.argsJson) {
				body.innerHTML =
					'<div class="tl-chat-tool-label">Arguments</div>' +
					'<pre>' + Utils.escapeHtml(Tools._prettyJson(tc.argsJson)) + '</pre>';
			}
			if (isDenied && tc.denyReason) {
				const deniedDiv = document.createElement('div');
				deniedDiv.innerHTML =
					'<div class="tl-chat-tool-label">Denied</div>' +
					'<pre>' + Utils.escapeHtml(tc.denyReason) + '</pre>';
				body.appendChild(deniedDiv);
			} else if (isFailed && tc.errorMessage) {
				const errorDiv = document.createElement('div');
				errorDiv.innerHTML =
					'<div class="tl-chat-tool-label">Error</div>' +
					'<pre>' + Utils.escapeHtml(tc.errorMessage) + '</pre>';
				body.appendChild(errorDiv);
			} else if (tc.result) {
				const resultDiv = document.createElement('div');
				resultDiv.innerHTML =
					'<div class="tl-chat-tool-label">Result</div>' +
					'<pre>' + Utils.escapeHtml(Tools._truncate(tc.result, 2000)) + '</pre>';
				body.appendChild(resultDiv);
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

		_renderDelegationPart(container, del) {
			const el = document.createElement('div');
			el.className = 'tl-chat-delegation';
			const header = document.createElement('div');
			header.className = 'tl-chat-delegation-header';
			header.innerHTML =
				'<span class="tl-chat-status-dot success"></span>' +
				'<span>Agent: ' + Utils.escapeHtml(del.toAgent || '') + '</span>' +
				(del.durationMs ? '<span class="tl-chat-tool-duration">' + Utils.formatDuration(del.durationMs) + '</span>' : '') +
				'<span class="tl-chat-tool-chevron">&#9654;</span>';
			el.appendChild(header);

			const childContainer = document.createElement('div');
			childContainer.className = 'tl-chat-delegation-tools';
			childContainer.style.display = 'none';

			if (del.childParts && del.childParts.length > 0) {
				for (const childPart of del.childParts) {
					const childType = childPart.type;
					if (childType === 'tool_call' && childPart.toolCallData) {
						this._renderToolCallPart(childContainer, childPart.toolCallData);
					} else if (childType === 'text' && childPart.textData && childPart.textData.text) {
						const textEl = document.createElement('div');
						textEl.className = 'tl-chat-delegation-result';
						textEl.innerHTML = Markdown.basicFormat(childPart.textData.text);
						childContainer.appendChild(textEl);
					} else if (childType === 'thinking' && childPart.thinkingData && childPart.thinkingData.text) {
						const thinkEl = document.createElement('div');
						thinkEl.className = 'tl-chat-thinking';
						thinkEl.textContent = childPart.thinkingData.text;
						childContainer.appendChild(thinkEl);
					} else if (childType === 'error' && childPart.errorData) {
						const errEl = document.createElement('div');
						errEl.className = 'tl-chat-error';
						errEl.textContent = childPart.errorData.errorMessage || childPart.errorData.kind;
						childContainer.appendChild(errEl);
					}
				}
			}

			if (del.resultSummary) {
				const resultEl = document.createElement('div');
				resultEl.className = 'tl-chat-delegation-result';
				resultEl.textContent = del.resultSummary;
				childContainer.appendChild(resultEl);
			}

			el.appendChild(childContainer);

			let expanded = false;
			header.addEventListener('click', () => {
				expanded = !expanded;
				childContainer.style.display = expanded ? '' : 'none';
				header.querySelector('.tl-chat-tool-chevron').classList.toggle('expanded', expanded);
			});

			container.appendChild(el);
		},

		_renderApprovalPart(container, ap) {
			const el = document.createElement('div');
			el.className = 'tl-chat-tool';

			const decision = ap.decision || '';
			let icon, label;
			switch (decision) {
				case 'APPROVED': icon = '\u2713'; label = 'Approved'; break;
				case 'APPROVED_FOR_SESSION': icon = '\u2713\u2713'; label = 'Approved for session'; break;
				case 'DENIED': icon = '\u2717'; label = 'Denied'; break;
				case 'TIMED_OUT': icon = '\u23F1'; label = 'Timed out'; break;
				default: icon = '?'; label = 'Pending'; break;
			}
			const statusClass = decision === 'DENIED' ? 'error' : (decision === 'TIMED_OUT' ? 'warning' : 'success');

			const header = document.createElement('div');
			header.className = 'tl-chat-tool-header';
			header.innerHTML =
				'<span class="tl-chat-status-dot ' + statusClass + '"></span>' +
				'<span class="tl-chat-tool-name">' + Utils.escapeHtml(ap.toolName || 'Approval') + '</span>' +
				'<span class="tl-chat-approval-decision">' + icon + ' ' + label + '</span>';

			if (ap.userComment || ap.displayReason) {
				const body = document.createElement('div');
				body.className = 'tl-chat-tool-body';
				body.style.display = 'none';
				if (ap.displayReason) {
					body.innerHTML += '<div class="tl-chat-tool-label">Reason</div><pre>' + Utils.escapeHtml(ap.displayReason) + '</pre>';
				}
				if (ap.userComment) {
					body.innerHTML += '<div class="tl-chat-tool-label">Comment</div><pre>' + Utils.escapeHtml(ap.userComment) + '</pre>';
				}
				let expanded = false;
				header.addEventListener('click', () => {
					expanded = !expanded;
					body.style.display = expanded ? '' : 'none';
				});
				header.innerHTML += '<span class="tl-chat-tool-chevron">&#9654;</span>';
				el.appendChild(header);
				el.appendChild(body);
			} else {
				el.appendChild(header);
			}

			container.appendChild(el);
		},

		_renderInteractionPart(container, interaction) {
			const el = document.createElement('div');
			el.className = 'tl-chat-tool';

			const header = document.createElement('div');
			header.className = 'tl-chat-tool-header';
			header.innerHTML =
				'<span class="tl-chat-status-dot ' + (interaction.userResponse ? 'success' : 'warning') + '"></span>' +
				'<span class="tl-chat-tool-name">User Input</span>' +
				'<span class="tl-chat-interaction-type">' + Utils.escapeHtml(interaction.interactionType || '') + '</span>';

			const body = document.createElement('div');
			body.className = 'tl-chat-tool-body';
			body.style.display = 'none';

			if (interaction.question) {
				body.innerHTML += '<div class="tl-chat-tool-label">Question</div><pre>' + Utils.escapeHtml(interaction.question) + '</pre>';
			}
			if (interaction.userResponse) {
				body.innerHTML += '<div class="tl-chat-tool-label">Response</div><pre>' + Utils.escapeHtml(interaction.userResponse) + '</pre>';
			} else {
				body.innerHTML += '<div class="tl-chat-tool-label">Response</div><pre>No response</pre>';
			}

			let expanded = false;
			header.addEventListener('click', () => {
				expanded = !expanded;
				body.style.display = expanded ? '' : 'none';
			});
			header.innerHTML += '<span class="tl-chat-tool-chevron">&#9654;</span>';

			el.appendChild(header);
			el.appendChild(body);
			container.appendChild(el);
		},

		_renderPersistedAgentMessage(msg) {
			const div = document.createElement('div');
			div.className = 'chat-message chat-message-agent';

			const bubble = document.createElement('div');
			bubble.className = 'chat-bubble chat-bubble-agent';

			if (msg.usage) {
				const tokenBadge = Tokens.createBadge(msg.usage);
				if (tokenBadge.nodeType === Node.ELEMENT_NODE) {
					bubble.appendChild(tokenBadge);
				}
			}

			const textEl = document.createElement('div');
			textEl.className = 'chat-agent-text';
			textEl.innerHTML = msg.contentHtml || Markdown.basicFormat(msg.content || '');
			bubble.appendChild(textEl);

			div.appendChild(bubble);
			if (Render._messagesEl) {
				Render._messagesEl.appendChild(div);
			}
		},

		_renderPersistedTrace(snapshots, agentMsg) {
			const div = document.createElement('div');
			div.className = 'chat-message chat-message-agent';

			const content = document.createElement('div');
			content.className = 'chat-bubble chat-bubble-agent';

			// Build trace wrapper (same structure as live Render.appendAssistantBubble)
			const traceWrapper = document.createElement('div');
			traceWrapper.className = 'tl-chat-trace-wrapper';

			const traceHeader = document.createElement('div');
			traceHeader.className = 'tl-chat-trace-header';

			// Count total tool/agent calls across all snapshots
			let totalEvents = 0;
			let totalUsage = null;
			let thinkingText = '';
			for (const task of snapshots) {
				if (task && task.timeline) {
					totalEvents += task.timeline.length;
				}
				if (task && task.totalUsage) {
					totalUsage = task.totalUsage;
				}
				if (task && task.thinkingText) {
					thinkingText = task.thinkingText;
				}
			}

			const traceTokensEl = document.createElement('span');
			traceTokensEl.className = 'tl-chat-trace-tokens';
			if (totalUsage) {
				const total = (totalUsage.inputTokens || 0) + (totalUsage.outputTokens || 0) + (totalUsage.thinkingTokens || 0);
				if (total > 0) {
					traceTokensEl.textContent = Utils.formatTokens(total) + ' tokens';
					traceTokensEl.addEventListener('click', (e) => {
						e.stopPropagation();
						Tokens._showPopover(e, totalUsage);
					});
				}
			}

			const summary = totalEvents > 0
				? totalEvents + ' tool' + (totalEvents > 1 ? 's' : '') + ' used'
				: 'Thinking';
			traceHeader.innerHTML =
				'<span class="tl-chat-trace-chevron">&#9654;</span>' +
				'<span class="tl-chat-trace-summary">' + summary + '</span>';
			traceHeader.appendChild(traceTokensEl);

			const traceBody = document.createElement('div');
			traceBody.className = 'tl-chat-trace-body';
			traceBody.style.display = 'none';

			let traceExpanded = false;
			traceHeader.addEventListener('click', () => {
				traceExpanded = !traceExpanded;
				traceBody.style.display = traceExpanded ? '' : 'none';
				traceHeader.querySelector('.tl-chat-trace-chevron').classList.toggle('expanded', traceExpanded);
			});

			// Render timeline events
			const toolsEl = document.createElement('div');
			toolsEl.className = 'tl-chat-tools-container';

			for (const task of snapshots) {
				if (!task || !task.timeline) continue;
				for (const ev of task.timeline) {
					if (ev.type === 'output') {
						this._renderPersistedOutput(toolsEl, ev);
					} else if (ev.type === 'agent_call') {
						this._renderPersistedDelegation(toolsEl, ev);
					} else {
						this._renderPersistedToolCall(toolsEl, ev);
					}
				}
			}

			if (thinkingText) {
				const thinkingEl = document.createElement('div');
				thinkingEl.className = 'tl-chat-thinking';
				const thinkingHeader = document.createElement('div');
				thinkingHeader.className = 'tl-chat-thinking-header';
				const preview = thinkingText.substring(0, 80) + (thinkingText.length > 80 ? '...' : '');
				thinkingHeader.innerHTML =
					'<span class="tl-chat-thinking-chevron">&#9654;</span>' +
					'<span>Thinking</span>' +
					'<span class="tl-chat-thinking-preview">' + Utils.escapeHtml(preview) + '</span>';
				const thinkingBody = document.createElement('div');
				thinkingBody.className = 'tl-chat-thinking-body';
				thinkingBody.style.display = 'none';
				thinkingBody.textContent = thinkingText;
				thinkingHeader.addEventListener('click', () => {
					const expanded = thinkingEl.dataset.expanded === 'true';
					thinkingEl.dataset.expanded = '' + !expanded;
					thinkingHeader.querySelector('.tl-chat-thinking-chevron').classList.toggle('expanded', !expanded);
					thinkingHeader.querySelector('.tl-chat-thinking-preview').style.display = !expanded ? 'none' : '';
					thinkingBody.style.display = !expanded ? '' : 'none';
				});
				thinkingEl.appendChild(thinkingHeader);
				thinkingEl.appendChild(thinkingBody);
				traceBody.appendChild(thinkingEl);
			}
			traceBody.appendChild(toolsEl);
			traceWrapper.appendChild(traceHeader);
			traceWrapper.appendChild(traceBody);
			content.appendChild(traceWrapper);

			// Agent response text
			const textEl = document.createElement('div');
			textEl.className = 'chat-agent-text';
			if (agentMsg) {
				textEl.innerHTML = agentMsg.contentHtml || Markdown.basicFormat(agentMsg.content || '');
			}
			content.appendChild(textEl);

			div.appendChild(content);
			if (Render._messagesEl) {
				Render._messagesEl.appendChild(div);
			}
		},

		_renderPersistedToolCall(container, ev) {
			const el = document.createElement('div');
			el.className = 'tl-chat-tool';

			const statusClass = ev.status === 'error' ? 'error' : 'success';

			const header = document.createElement('div');
			header.className = 'tl-chat-tool-header';
			header.innerHTML =
				'<span class="tl-chat-status-dot ' + statusClass + '"></span>' +
				'<span class="tl-chat-tool-name">' + Utils.escapeHtml(ev.toolName || ev.name || '') + '</span>' +
				'<span class="tl-chat-tool-tokens">~' + Utils.formatTokens(Tools._estimateTokens(ev.argsJson) + Tools._estimateTokens(ev.result)) + ' tokens</span>' +
				'<span class="tl-chat-tool-duration">' + (ev.durationMs ? Utils.formatDuration(ev.durationMs) : '') + '</span>' +
				'<span class="tl-chat-tool-chevron">&#9654;</span>';

			const body = document.createElement('div');
			body.className = 'tl-chat-tool-body';
			body.style.display = 'none';

			if (ev.argsJson) {
				body.innerHTML =
					'<div class="tl-chat-tool-label">Arguments</div>' +
					'<pre>' + Utils.escapeHtml(Tools._prettyJson(ev.argsJson)) + '</pre>';
			}
			if (ev.result) {
				const resultDiv = document.createElement('div');
				resultDiv.innerHTML =
					'<div class="tl-chat-tool-label">Result</div>' +
					'<pre>' + Utils.escapeHtml(Tools._truncate(ev.result, 2000)) + '</pre>';
				body.appendChild(resultDiv);
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

		_renderPersistedDelegation(container, ev) {
			const el = document.createElement('div');
			el.className = 'tl-chat-delegation';

			const header = document.createElement('div');
			header.className = 'tl-chat-delegation-header';
			header.innerHTML =
				'<span class="tl-chat-status-dot success"></span>' +
				'<span>Agent: ' + Utils.escapeHtml(ev.toAgent || '') + '</span>';
			el.appendChild(header);
			container.appendChild(el);
		},

		_renderPersistedOutput(container, ev) {
			const text = ev.result || '';
			if (!text) return;
			const el = document.createElement('div');
			el.className = 'tl-chat-tool';

			const header = document.createElement('div');
			header.className = 'tl-chat-tool-header';
			const stripped = text.replace(/\s+/g, ' ').trim();
			const preview = stripped.length > 60 ? stripped.substring(0, 60) + '...' : stripped;
			header.innerHTML =
				'<span class="tl-chat-status-dot success"></span>' +
				'<span class="tl-chat-tool-name-fixed">Output</span>' +
				'<span class="tl-chat-output-summary">' + Utils.escapeHtml(preview) + '</span>' +
				'<span class="tl-chat-tool-tokens">~' + Utils.formatTokens(Tools._estimateTokens(text)) + ' tokens</span>' +
				'<span class="tl-chat-tool-chevron">&#9654;</span>';

			const body = document.createElement('div');
			body.className = 'tl-chat-tool-body';
			body.style.display = 'none';
			body.innerHTML = '<div class="tl-chat-output-content">' + Markdown.basicFormat(text) + '</div>';

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

		_moveToTop(sessionId) {
			if (!this._listEl) return;
			const item = this._listEl.querySelector('[data-session-id="' + sessionId + '"]');
			if (item && item !== this._listEl.firstChild) {
				this._listEl.insertBefore(item, this._listEl.firstChild);
			}
			if (item) {
				const timeEl = item.querySelector('.chat-history-time');
				if (timeEl) timeEl.textContent = 'just now';
			}
		},

		async createNew() {
			try {
				const resp = await fetch(URLS.sessions, {
					method: 'POST',
					headers: { 'Content-Type': 'application/json' },
					body: JSON.stringify({ title: 'New Chat' })
				});
				const data = await resp.json();
				if (data.id) {
					const item = this._createSessionItem(data);
					if (this._listEl) {
						if (this._listEl.firstChild) {
							this._listEl.insertBefore(item, this._listEl.firstChild);
						} else {
							this._listEl.appendChild(item);
						}
					}
					// Skip the GET — session was just created, nothing to load
					await this.switchTo(data.id, true);
					Welcome.show();
				}
			} catch (e) {
				console.error('[tl-chat] Failed to create session:', e);
			}
		},

		/**
		 * Ensures a session exists before sending a message.
		 * Creates one if Session.currentId is falsy.
		 * Returns the session ID, or null if creation failed.
		 */
		async _ensureSession() {
			if (this.currentId) return this.currentId;
			// No session yet — create one and fully activate it via switchTo
			await this.createNew();
			return this.currentId;
		},

		async deleteSession(sessionId, itemEl) {
			try {
				await fetch(URLS.sessions + '/' + encodeURIComponent(sessionId), { method: 'DELETE' });
				if (itemEl) itemEl.remove();
				if (sessionId === this.currentId) {
					this.currentId = null;
					this._resetState();
					const next = this._listEl && this._listEl.querySelector('.chat-history-item');
					if (next) {
						this.switchTo(next.dataset.sessionId);
					} else {
						Welcome.show();
					}
				}
			} catch (e) {
				console.error('[tl-chat] Failed to delete session:', e);
			}
		}
	};

	// ========================================================================
	// Init — Bootstrap
	// ========================================================================
	function initChat(root) {
		Render.init(root);
		Session.init(root);
		Welcome.init(root);

		// Hide welcome if messages were server-rendered
		const msgs = root.querySelector('.chat-messages');
		if (msgs && msgs.children.length > 0) {
			Welcome.hide();
		}

		const sessionId = root.dataset.sessionId || '';
		Session.switchTo(sessionId);

		State.onChange((newState) => {
			Render.updateInputState();
		});
	}

	// ========================================================================
	// Welcome — Welcome screen input, send, and prompt chips
	// ========================================================================
	const Welcome = {
		_el: null,

		init(root) {
			this._el = root.querySelector('.chat-welcome');
			if (!this._el) return;

			const textarea = this._el.querySelector('.chat-welcome-input');
			const sendBtn = this._el.querySelector('.chat-welcome-send-btn');
			const chips = this._el.querySelectorAll('.chat-prompt-chip');

			if (textarea && sendBtn) {
				sendBtn.addEventListener('click', () => this._sendFromWelcome(textarea));
				textarea.addEventListener('keydown', (e) => {
					if (e.key === 'Enter' && !e.shiftKey) {
						e.preventDefault();
						this._sendFromWelcome(textarea);
					}
				});
			}

			// Wire up welcome-screen file upload
			const fileInput = this._el.querySelector('.chat-welcome-file-input');
			const attachBtn = this._el.querySelector('.chat-welcome-attach-btn');
			const inputSection = this._el.querySelector('.chat-welcome-input-section');
			const previewEl = this._el.querySelector('.chat-welcome-attachment-preview');
			Attachments.setup(attachBtn, fileInput, inputSection, previewEl);

			chips.forEach(chip => {
				chip.addEventListener('click', () => {
					const prompt = chip.dataset.prompt || chip.textContent;
					if (textarea) textarea.value = prompt;
					this._sendFromWelcome(textarea);
				});
			});
		},

		async _sendFromWelcome(textarea) {
			if (!textarea) return;
			const text = textarea.value.trim();
			if (!text && Attachments.get().length === 0) return;
			if (State.isActive()) return;
			textarea.value = '';

			try {
				const attachments = await Attachments.encode();
				Attachments.clear();
				// Ensure session exists before touching DOM (createNew clears messages)
				const sessionId = await Session._ensureSession();
				this.hide();
				Render.appendUserMessage(text, attachments);
				await fetch(URLS.send, {
					method: 'POST',
					headers: { 'Content-Type': 'application/json' },
					body: JSON.stringify({
						text: text,
						sessionId: sessionId || '',
						attachments: attachments
					})
				});
			} catch (err) {
				console.error('[tl-chat] Send failed:', err);
			}
		},

		hide() {
			if (this._el) this._el.classList.add('hidden');
		},

		show() {
			if (this._el) this._el.classList.remove('hidden');
		}
	};

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
