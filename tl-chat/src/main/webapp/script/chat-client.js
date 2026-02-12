/**
 * Chat client for the agentic chat component (v1).
 * Uses dedicated JSON API: POST /chat/send, SSE /chat/events for real-time updates.
 * Incremental DOM updates only (no full repaint).
 */
(function () {
	'use strict';

	// Debug logging - set window.TL_CHAT_DEBUG = true to enable
	function log(msg, data) {
		if (window.TL_CHAT_DEBUG) {
			console.log('[chat-client] ' + msg, data || '');
		}
	}

	log('Script executing');

	// Prevent multiple script executions from causing duplicate connections
	if (window._tlChatClientLoaded) {
		log('Script already loaded, calling init()');
		if (typeof window._tlChatInit === 'function') {
			window._tlChatInit();
		}
		return;
	}
	window._tlChatClientLoaded = true;
	log('First load, initializing');

	var SSE_RECONNECT_DELAY_MS = 2000;

	// Use TL_CHAT_BASE if set; else infer from pathname
	if (typeof window !== 'undefined' && window.TL_CHAT_BASE === undefined && window.location && window.location.pathname) {
		var parts = window.location.pathname.split('/').filter(Boolean);
		window.TL_CHAT_BASE = parts.length > 0 ? '/' + parts[0] : '';
	}
	var BASE = (typeof window !== 'undefined' && window.TL_CHAT_BASE) || '';
	var CHAT_SEND_URL = BASE + '/chat/send';
	var CHAT_EVENTS_URL = BASE + '/chat/events';

	// Track active SSE connections per chat root
	var activeConnections = new WeakMap();

	// ==================== Utility Functions ====================

	function escapeHtml(text) {
		if (text == null) return '';
		var div = document.createElement('div');
		div.textContent = text;
		return div.innerHTML;
	}

	function prettyPrintJson(str) {
		if (!str) return str;
		try {
			var obj = typeof str === 'string' ? JSON.parse(str) : str;
			return JSON.stringify(obj, null, 2);
		} catch (e) {
			return str;
		}
	}

	function formatDuration(ms) {
		return (ms / 1000).toFixed(1) + 's';
	}

	function getTaskIcon(status) {
		switch (status) {
			case 'completed': return '\u2713'; // ✓
			case 'error': return '\u2717'; // ✗
			case 'in_progress': return '\u23f3'; // ⏳
			default: return '\u25cb'; // ○
		}
	}

	// Toggle functions (toggleTaskExpand, toggleToolExpand) are defined by the server-rendered
	// inline script in ChatControl.writeTaskToggleScript() and are available globally.

	// ==================== DOM Helpers ====================

	function getElements(root) {
		var doc = root.ownerDocument || document;
		var messagesId = root.getAttribute('data-chat-messages-id') || (root.id && root.id + '-messages');
		var inputId = root.getAttribute('data-chat-input-id') || (root.id && root.id + '-input');
		var buttonId = root.getAttribute('data-chat-button-id') || (root.id && root.id + '-button');
		var messages = (messagesId && doc.getElementById(messagesId)) || root.querySelector('.chat-messages');
		var input = (inputId && doc.getElementById(inputId)) || root.querySelector('.chat-input');
		var button = (buttonId && doc.getElementById(buttonId)) || root.querySelector('.chat-button');
		return { messages: messages, input: input, button: button };
	}

	function appendMessage(container, msg) {
		var existingMsg = container.querySelector('[data-message-id="' + (msg.id || '') + '"]');
		if (existingMsg) return;

		// If this is a server-delivered user message, absorb it into the pending optimistic message.
		// Only update the canonical message ID (for deduplication); keep the client-provided
		// userName and time which are already correct on the optimistic element.
		if (msg.role === 'user' && !msg.optimistic) {
			var optimistic = container.querySelector('.chat-message[data-optimistic="true"]');
			if (optimistic) {
				optimistic.setAttribute('data-message-id', msg.id || ('m' + container.children.length));
				optimistic.removeAttribute('data-optimistic');
				return;
			}
		}

		var div = document.createElement('div');
		div.className = 'chat-message';
		div.setAttribute('data-message-id', msg.id || ('m' + container.children.length));
		if (msg.optimistic) {
			div.setAttribute('data-optimistic', 'true');
		}

		var user = document.createElement('span');
		user.className = 'chat-user';
		user.textContent = msg.userName || msg.role || '';

		var time = document.createElement('span');
		time.className = 'chat-time';
		time.textContent = msg.time ? '(' + msg.time + ')' : '';

		var text = document.createElement('span');
		text.className = 'chat-text';
		// Use server-pre-rendered HTML for agent messages when available
		if (msg.contentHtml) {
			text.innerHTML = msg.contentHtml;
		} else {
			text.innerHTML = escapeHtml(msg.content || '');
		}

		div.appendChild(user);
		div.appendChild(document.createTextNode(' '));
		div.appendChild(time);
		div.appendChild(document.createTextNode(': '));
		div.appendChild(text);
		container.appendChild(div);
	}

	function ensureTaskCard(container, taskId, description, status, startTime) {
		var existingTaskEl = container.querySelector('.chat-task[data-task-id="' + (taskId || '') + '"]');

		if (existingTaskEl) {
			existingTaskEl.setAttribute('data-status', (status || '').toLowerCase());
			var desc = existingTaskEl.querySelector('.chat-task-description');
			if (desc && description) {
				desc.textContent = description;
			}
			var icon = existingTaskEl.querySelector('.chat-task-icon');
			if (icon) icon.textContent = getTaskIcon(status);

			// Update duration if completed
			if (status === 'completed' || status === 'error') {
				var st = parseInt(existingTaskEl.getAttribute('data-start-time') || Date.now());
				var dur = existingTaskEl.querySelector('.chat-task-time');
				if (dur) {
					dur.textContent = formatDuration(Date.now() - st);
				} else {
					dur = document.createElement('span');
					dur.className = 'chat-task-time';
					dur.textContent = formatDuration(Date.now() - st);
					var header = existingTaskEl.querySelector('.chat-task-header');
					if (header) {
						var arrow = header.querySelector('.chat-task-expand');
						if (arrow) {
							header.insertBefore(dur, arrow);
						} else {
							header.appendChild(dur);
						}
					}
				}
			}

			return existingTaskEl.querySelector('.chat-task-body') || existingTaskEl;
		}

		// Create new task card
		var taskDivId = 'task-' + (taskId || 'current');
		var arrowId = taskDivId + '-arrow';
		var bodyId = taskDivId + '-body';
		var now = startTime || Date.now();

		var wrap = document.createElement('div');
		wrap.className = 'chat-task';
		wrap.setAttribute('data-task-id', taskId || '');
		wrap.setAttribute('data-status', (status || 'in_progress').toLowerCase());
		wrap.setAttribute('data-start-time', now);

		var header = document.createElement('div');
		header.className = 'chat-task-header';
		header.setAttribute('onclick', "toggleTaskExpand('" + bodyId + "', '" + arrowId + "')");

		var icon = document.createElement('span');
		icon.className = 'chat-task-icon';
		icon.textContent = getTaskIcon(status || 'in_progress');

		var descSpan = document.createElement('span');
		descSpan.className = 'chat-task-description';
		descSpan.textContent = description || 'Processing...';

		var arrow = document.createElement('span');
		arrow.className = 'chat-task-expand';
		arrow.id = arrowId;
		arrow.textContent = '\u25bc'; // ▼ (expanded by default)

		header.appendChild(icon);
		header.appendChild(descSpan);
		header.appendChild(arrow);
		wrap.appendChild(header);

		var body = document.createElement('div');
		body.className = 'chat-task-body';
		body.id = bodyId;
		wrap.appendChild(body);

		container.appendChild(wrap);
		return body;
	}

	function appendTimelineEvent(body, ev) {
		if (!body) return;

		var existingEv = body.querySelector('[data-event-id="' + (ev.id || '') + '"]');
		if (existingEv) {
			// Update existing agent call if result has changed
			if (ev.type === 'agent_call' && ev.resultSummary) {
				var resultSection = existingEv.querySelector('.chat-agent-call-result');
				if (!resultSection) {
					// Result section doesn't exist yet, create it
					var agentBody = existingEv.querySelector('.chat-agent-call-body');
					if (agentBody) {
						var resultLabel = document.createElement('div');
						resultLabel.className = 'chat-agent-call-result-label';
						resultLabel.textContent = 'Result:';
						agentBody.appendChild(resultLabel);
						var resultPre = document.createElement('pre');
						resultPre.className = 'chat-agent-call-result';
						resultPre.textContent = ev.resultSummary;
						agentBody.appendChild(resultPre);
					}
				} else {
					// Update existing result
					resultSection.textContent = ev.resultSummary;
				}
			}
			return;
		}

		var uniqueId = 'ev-' + (ev.id || '') + '-' + Date.now();

		if (ev.type === 'tool_call') {
			var toolWrap = document.createElement('div');
			toolWrap.className = 'chat-tool-call';
			toolWrap.setAttribute('data-event-id', ev.id || '');
			toolWrap.setAttribute('data-status', ev.result != null ? 'success' : 'running');

			var arrowId = uniqueId + '-arrow';
			var resultId = uniqueId + '-result';

			// Clickable header
			var toolHeader = document.createElement('div');
			toolHeader.className = 'chat-tool-header';
			toolHeader.setAttribute('onclick', "toggleToolExpand('" + resultId + "', '" + arrowId + "')");

			var toolName = document.createElement('span');
			toolName.className = 'chat-tool-name';
			toolName.textContent = ev.toolName || ev.name || 'Tool';

			var toolArgs = document.createElement('span');
			toolArgs.className = 'chat-tool-args';
			if (ev.args && Object.keys(ev.args).length) {
				var argsStr = Object.keys(ev.args).map(function(k) {
					var v = ev.args[k];
					if (typeof v === 'string' && v.length > 50) {
						return k + '="' + v.substring(0, 47) + '..."';
					}
					return k + '=' + JSON.stringify(v);
				}).join(', ');
				toolArgs.textContent = '(' + argsStr + ')';
			}

			var toolArrow = document.createElement('span');
			toolArrow.className = 'chat-tool-expand';
			toolArrow.id = arrowId;
			toolArrow.textContent = '\u25b6'; // ▶ (collapsed by default)

			toolHeader.appendChild(toolName);
			toolHeader.appendChild(toolArgs);
			toolHeader.appendChild(toolArrow);
			toolWrap.appendChild(toolHeader);

			// Collapsible result
			var toolResult = document.createElement('div');
			toolResult.className = 'chat-tool-result';
			toolResult.id = resultId;
			toolResult.style.display = 'none';

			if (ev.result != null) {
				var pre = document.createElement('pre');
				var code = document.createElement('code');
				code.className = 'language-json';
				code.textContent = prettyPrintJson(ev.result);
				pre.appendChild(code);
				toolResult.appendChild(pre);
			}

			toolWrap.appendChild(toolResult);
			body.appendChild(toolWrap);

		} else if (ev.type === 'agent_call') {
			var agentWrap = document.createElement('div');
			agentWrap.className = 'chat-agent-call';
			agentWrap.setAttribute('data-event-id', ev.id || '');

			var arrowId = uniqueId + '-arrow';
			var bodyId = uniqueId + '-body';

			// Clickable header
			var agentHeader = document.createElement('div');
			agentHeader.className = 'chat-agent-call-header';
			agentHeader.setAttribute('onclick', "toggleToolExpand('" + bodyId + "', '" + arrowId + "')");

			var agentLabel = document.createElement('span');
			agentLabel.className = 'chat-agent-call-label';
			agentLabel.textContent = (ev.fromAgent || '') + ' \u2192 ' + (ev.toAgent || '');

			var agentArrow = document.createElement('span');
			agentArrow.className = 'chat-agent-call-expand';
			agentArrow.id = arrowId;
			agentArrow.textContent = '\u25b6'; // ▶ (collapsed by default)

			agentHeader.appendChild(agentLabel);
			agentHeader.appendChild(agentArrow);
			agentWrap.appendChild(agentHeader);

			// Collapsible body
			var agentBody = document.createElement('div');
			agentBody.className = 'chat-agent-call-body';
			agentBody.id = bodyId;
			agentBody.style.display = 'none';

			if (ev.inputSummary) {
				var inputLabel = document.createElement('div');
				inputLabel.className = 'chat-agent-call-input-label';
				inputLabel.textContent = 'Input:';
				agentBody.appendChild(inputLabel);
				var inputPre = document.createElement('pre');
				inputPre.className = 'chat-agent-call-input';
				inputPre.textContent = ev.inputSummary;
				agentBody.appendChild(inputPre);
			}
			if (ev.resultSummary) {
				var resultLabel = document.createElement('div');
				resultLabel.className = 'chat-agent-call-result-label';
				resultLabel.textContent = 'Result:';
				agentBody.appendChild(resultLabel);
				var resultPre = document.createElement('pre');
				resultPre.className = 'chat-agent-call-result';
				resultPre.textContent = ev.resultSummary;
				agentBody.appendChild(resultPre);
			}

			agentWrap.appendChild(agentBody);
			body.appendChild(agentWrap);
		}
	}

	function scrollToBottom(container) {
		if (container) container.scrollTop = container.scrollHeight;
	}

	function fetchJson(url, options) {
		options = options || {};
		options.headers = options.headers || {};
		options.headers['Content-Type'] = options.headers['Content-Type'] || 'application/json';
		if (options.body && typeof options.body === 'object' && !(options.body instanceof FormData)) {
			options.body = JSON.stringify(options.body);
		}
		return fetch(url, options).then(function (res) {
			if (!res.ok) throw new Error(res.statusText || res.status);
			return res.json();
		});
	}

	function cleanupConnection(root) {
		var conn = activeConnections.get(root);
		if (conn) {
			if (conn.eventSource) {
				conn.eventSource.close();
			}
			if (conn.reconnectTimer) {
				clearTimeout(conn.reconnectTimer);
			}
			activeConnections.delete(root);
		}
	}

	// ==================== Main Initialization ====================

	function initOne(root) {
		log('initOne called', { rootId: root.id });

		var existingConn = activeConnections.get(root);
		if (existingConn && existingConn.eventSource && existingConn.eventSource.readyState !== EventSource.CLOSED) {
			log('Already initialized and connected, skipping');
			return;
		}

		cleanupConnection(root);
		root.setAttribute('data-tl-chat-inited', 'true');

		var el = getElements(root);
		if (!el.messages || !el.input || !el.button) {
			log('Missing elements', { messages: !!el.messages, input: !!el.input, button: !!el.button });
			return;
		}
		log('Elements found, initializing');

		var state = {
			eventSource: null,
			reconnectTimer: null,
			currentTaskId: null,
			currentTaskBody: null,
			currentTaskStartTime: null,
			processing: false,
			destroyed: false
		};
		activeConnections.set(root, state);

		function getCurrentTaskBody() {
			if (state.currentTaskId && !state.currentTaskBody) {
				var taskEl = el.messages.querySelector('.chat-task[data-task-id="' + state.currentTaskId + '"]');
				if (taskEl) {
					state.currentTaskBody = taskEl.querySelector('.chat-task-body');
				}
			}
			return state.currentTaskBody;
		}

		function connectSSE() {
			log('connectSSE called', { destroyed: state.destroyed });
			if (state.destroyed) return;

			if (!document.body.contains(root)) {
				log('Root not in DOM, destroying');
				state.destroyed = true;
				cleanupConnection(root);
				return;
			}

			if (state.eventSource) {
				state.eventSource.close();
				state.eventSource = null;
			}

			log('Creating EventSource', { url: CHAT_EVENTS_URL });
			var eventSource = new EventSource(CHAT_EVENTS_URL);
			state.eventSource = eventSource;

			eventSource.addEventListener('connected', function (e) {
				log('SSE connected event', e.data);
				if (state.destroyed) return;
				try {
					var data = JSON.parse(e.data);
					state.processing = data.processing;
					if (data.processing && data.taskId) {
						state.currentTaskId = data.taskId;
					}
					if (!state.processing) {
						el.input.disabled = false;
						el.button.disabled = false;
					}
				} catch (err) {
					log('SSE connected parse error', err);
				}
			});

			eventSource.addEventListener('message', function (e) {
				log('SSE message event', e.data);
				if (state.destroyed) return;
				try {
					var msg = JSON.parse(e.data);
					appendMessage(el.messages, msg);
					scrollToBottom(el.messages);
				} catch (err) {
					log('SSE message parse error', err);
				}
			});

			eventSource.addEventListener('tool_call', function (e) {
				if (state.destroyed) return;
				try {
					var ev = JSON.parse(e.data);
					ev.type = 'tool_call';
					var body = getCurrentTaskBody();
					if (body) {
						appendTimelineEvent(body, ev);
						scrollToBottom(el.messages);
					}
				} catch (err) {
					log('SSE tool_call parse error', err);
				}
			});

			eventSource.addEventListener('agent_call', function (e) {
				if (state.destroyed) return;
				try {
					var ev = JSON.parse(e.data);
					ev.type = 'agent_call';
					var body = getCurrentTaskBody();
					if (body) {
						appendTimelineEvent(body, ev);
						scrollToBottom(el.messages);
					}
				} catch (err) {
					log('SSE agent_call parse error', err);
				}
			});

			eventSource.addEventListener('task_status', function (e) {
				log('SSE task_status event', e.data);
				if (state.destroyed) return;
				try {
					var status = JSON.parse(e.data);
					state.currentTaskId = status.taskId;

					if (status.taskId) {
						var taskBody = ensureTaskCard(
							el.messages,
							status.taskId,
							status.description,
							status.status,
							state.currentTaskStartTime
						);
						state.currentTaskBody = taskBody;
					}

					if (status.status === 'completed' || status.status === 'error') {
						state.processing = false;
						state.currentTaskId = null;
						state.currentTaskBody = null;
						state.currentTaskStartTime = null;
						el.input.disabled = false;
						el.button.disabled = false;
					}
					scrollToBottom(el.messages);
				} catch (err) {
					log('SSE task_status parse error', err);
				}
			});

			eventSource.addEventListener('heartbeat', function (e) {
				// Heartbeat received - connection is alive
			});

			eventSource.onerror = function (err) {
				log('SSE error', { readyState: eventSource.readyState, destroyed: state.destroyed });
				if (state.destroyed) return;
				eventSource.close();
				state.eventSource = null;

				if (!document.body.contains(root)) {
					log('Root not in DOM after error, destroying');
					state.destroyed = true;
					cleanupConnection(root);
					return;
				}

				log('Scheduling reconnect in ' + SSE_RECONNECT_DELAY_MS + 'ms');
				state.reconnectTimer = setTimeout(connectSSE, SSE_RECONNECT_DELAY_MS);
			};
		}

	function sendMessage() {
		var text = (el.input.value || '').trim();
		if (!text) return;
		el.input.disabled = true;
		el.button.disabled = true;

		// Optimistically add user message to UI immediately (replaced by server message via SSE)
		var tempMsgId = 'user-' + Date.now();
		appendMessage(el.messages, {
			id: tempMsgId,
			role: 'user',
			userName: 'User',
			content: text,
			time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: false }),
			optimistic: true
		});
		scrollToBottom(el.messages);

		fetchJson(CHAT_SEND_URL, { method: 'POST', body: { text: text } })
				.then(function (data) {
					if (data && data.status === 'busy') {
						el.input.disabled = false;
						el.button.disabled = false;
						appendMessage(el.messages, {
							id: 'busy-' + Date.now(),
							role: 'system',
							userName: 'System',
							content: 'Please wait for the current response to complete.',
							time: ''
						});
						scrollToBottom(el.messages);
						return;
					}
					el.input.value = '';
					state.processing = true;
					state.currentTaskId = data.taskId;
					state.currentTaskBody = null;
					state.currentTaskStartTime = Date.now();

					if (data.taskId) {
						var taskBody = ensureTaskCard(el.messages, data.taskId, null, 'in_progress', state.currentTaskStartTime);
						state.currentTaskBody = taskBody;
					}
					scrollToBottom(el.messages);
				})
				.catch(function (err) {
					el.input.disabled = false;
					el.button.disabled = false;
					appendMessage(el.messages, {
						id: 'err-' + Date.now(),
						role: 'system',
						userName: 'System',
						content: 'Failed to send: ' + (err.message || 'Unknown error'),
						time: ''
					});
					scrollToBottom(el.messages);
				});
		}

		el.button.onclick = null;
		el.input.onkeydown = null;

		el.button.addEventListener('click', sendMessage);
		el.input.addEventListener('keydown', function (e) {
			if (e.key === 'Enter' && !e.shiftKey) {
				e.preventDefault();
				sendMessage();
			}
		});

		connectSSE();
	}

	function init() {
		var roots = document.querySelectorAll('[data-tl-chat="v1"]');
		log('init() found ' + roots.length + ' chat roots');
		for (var i = 0; i < roots.length; i++) {
			initOne(roots[i]);
		}
	}

	window._tlChatInit = init;

	function startObserving() {
		log('startObserving called');
		var body = document.body;
		if (!body) {
			log('No document.body, aborting');
			return;
		}

		if (window._tlChatObserver) {
			log('Observer already exists, calling init()');
			init();
			return;
		}

		var observer = new MutationObserver(function (mutations) {
			for (var m = 0; m < mutations.length; m++) {
				var mutation = mutations[m];

				var added = mutation.addedNodes;
				for (var a = 0; a < added.length; a++) {
					var node = added[a];
					if (node.nodeType !== 1) continue;
					if (node.getAttribute && node.getAttribute('data-tl-chat') === 'v1') {
						initOne(node);
					}
					var roots = node.querySelectorAll ? node.querySelectorAll('[data-tl-chat="v1"]') : [];
					for (var r = 0; r < roots.length; r++) {
						initOne(roots[r]);
					}
				}

				var removed = mutation.removedNodes;
				for (var x = 0; x < removed.length; x++) {
					var removedNode = removed[x];
					if (removedNode.nodeType !== 1) continue;
					if (removedNode.getAttribute && removedNode.getAttribute('data-tl-chat') === 'v1') {
						cleanupConnection(removedNode);
					}
					var removedRoots = removedNode.querySelectorAll ? removedNode.querySelectorAll('[data-tl-chat="v1"]') : [];
					for (var rr = 0; rr < removedRoots.length; rr++) {
						cleanupConnection(removedRoots[rr]);
					}
				}
			}
		});
		observer.observe(body, { childList: true, subtree: true });
		window._tlChatObserver = observer;
		log('MutationObserver set up');

		init();
	}

	function run() {
		log('run() called', { readyState: document.readyState });
		if (document.readyState === 'loading') {
			document.addEventListener('DOMContentLoaded', function () {
				startObserving();
			});
		} else {
			startObserving();
		}
	}

	window.addEventListener('beforeunload', function () {
		var roots = document.querySelectorAll('[data-tl-chat="v1"]');
		for (var i = 0; i < roots.length; i++) {
			cleanupConnection(roots[i]);
		}
	});

	run();
})();
