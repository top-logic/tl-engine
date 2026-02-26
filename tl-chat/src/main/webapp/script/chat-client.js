/**
 * Chat client for the agentic chat component (v1).
 * Uses dedicated JSON API: POST /chat/send, SSE /chat/events for real-time updates.
 * Incremental DOM updates only (no full repaint).
 */
(function () {
  "use strict";

  // Debug logging - set window.TL_CHAT_DEBUG = true to enable
  function log(msg, data) {
    if (window.TL_CHAT_DEBUG) {
      console.log("[chat-client] " + msg, data || "");
    }
  }

  log("Script executing");

  // Prevent multiple script executions from causing duplicate connections
  if (window._tlChatClientLoaded) {
    log("Script already loaded, calling init()");
    if (typeof window._tlChatInit === "function") {
      window._tlChatInit();
    }
    return;
  }
  window._tlChatClientLoaded = true;
  log("First load, initializing");

  // Global event delegation for sidebar toggle. Registered once on document
  // so it survives TopLogic's DOM cloning/replacement of the chat control.
  document.addEventListener("click", function (e) {
    var toggle = e.target.closest(".chat-sidebar-toggle");
    if (toggle) {
      var root = toggle.closest('[data-tl-chat="v1"]');
      if (root) {
        var panel = root.querySelector(".chat-history-panel");
        if (panel) {
          panel.classList.toggle("open");
        }
      }
    }
  });

  var SSE_RECONNECT_DELAY_MS = 2000;

  // Use TL_CHAT_BASE if set; else infer from pathname
  if (
    typeof window !== "undefined" &&
    window.TL_CHAT_BASE === undefined &&
    window.location &&
    window.location.pathname
  ) {
    var parts = window.location.pathname.split("/").filter(Boolean);
    window.TL_CHAT_BASE = parts.length > 0 ? "/" + parts[0] : "";
  }
  var BASE = (typeof window !== "undefined" && window.TL_CHAT_BASE) || "";
  var CHAT_SEND_URL = BASE + "/chat/send";
  var CHAT_EVENTS_URL = BASE + "/chat/events";
  var CHAT_SESSIONS_URL = BASE + "/chat/sessions";

  // Track active SSE connections per chat root
  var activeConnections = new WeakMap();

  // ==================== Utility Functions ====================

  function escapeHtml(text) {
    if (text == null) return "";
    var div = document.createElement("div");
    div.textContent = text;
    return div.innerHTML;
  }

  function prettyPrintJson(str) {
    if (!str) return str;
    try {
      var obj = typeof str === "string" ? JSON.parse(str) : str;
      return JSON.stringify(obj, null, 2);
    } catch (e) {
      return str;
    }
  }

  function formatDuration(ms) {
    return (ms / 1000).toFixed(1) + "s";
  }

  function formatTime(isoString) {
    if (!isoString) return "";
    try {
      var d = new Date(isoString);
      if (isNaN(d.getTime())) return isoString;
      var h = String(d.getHours()).padStart(2, "0");
      var m = String(d.getMinutes()).padStart(2, "0");
      var s = String(d.getSeconds()).padStart(2, "0");
      return h + ":" + m + ":" + s;
    } catch (e) {
      return isoString;
    }
  }

  function getTaskIcon(status) {
    switch (status) {
      case "completed":
        return "\u2713"; // ✓
      case "error":
        return "\u2717"; // ✗
      case "in_progress":
        return "\u23f3"; // ⏳
      default:
        return "\u25cb"; // ○
    }
  }

  // ==================== Toggle Functions (global for onclick) ====================

  window.toggleTaskExpand = function (bodyId, arrowId) {
    var body = document.getElementById(bodyId);
    var arrow = document.getElementById(arrowId);
    if (body && arrow) {
      if (body.style.display === "none" || body.style.display === "") {
        body.style.display = "block";
        arrow.textContent = "\u25bc"; // ▼
      } else {
        body.style.display = "none";
        arrow.textContent = "\u25b6"; // ▶
      }
    }
  };

  window.toggleToolExpand = function (resultId, arrowId) {
    var result = document.getElementById(resultId);
    var arrow = document.getElementById(arrowId);
    if (result && arrow) {
      if (result.style.display === "none" || result.style.display === "") {
        result.style.display = "block";
        arrow.textContent = "\u25be"; // ▾
      } else {
        result.style.display = "none";
        arrow.textContent = "\u203a"; // ›
      }
    }
  };

  // ==================== DOM Helpers ====================

  function getElements(root) {
    var doc = root.ownerDocument || document;
    var messagesId =
      root.getAttribute("data-chat-messages-id") ||
      (root.id && root.id + "-messages");
    var inputId =
      root.getAttribute("data-chat-input-id") ||
      (root.id && root.id + "-input");
    var buttonId =
      root.getAttribute("data-chat-button-id") ||
      (root.id && root.id + "-button");
    var messages =
      (messagesId && doc.getElementById(messagesId)) ||
      root.querySelector(".chat-messages");
    var input =
      (inputId && doc.getElementById(inputId)) ||
      root.querySelector(".chat-input");
    var button =
      (buttonId && doc.getElementById(buttonId)) ||
      root.querySelector(".chat-button");
    var fileInput =
      (root.getAttribute("data-chat-file-input-id") &&
        doc.getElementById(root.getAttribute("data-chat-file-input-id"))) ||
      root.querySelector(".chat-file-input");
    var attachBtn =
      (root.getAttribute("data-chat-attach-btn-id") &&
        doc.getElementById(root.getAttribute("data-chat-attach-btn-id"))) ||
      root.querySelector(".chat-attach-button");
    var preview =
      (root.getAttribute("data-chat-preview-id") &&
        doc.getElementById(root.getAttribute("data-chat-preview-id"))) ||
      root.querySelector(".chat-attachment-preview");
    return {
      messages: messages,
      input: input,
      button: button,
      fileInput: fileInput,
      attachBtn: attachBtn,
      preview: preview,
    };
  }

  function appendMessage(container, msg) {
    var existingMsg = container.querySelector(
      '[data-message-id="' + (msg.id || "") + '"]',
    );
    if (existingMsg) return;

    // If this is a server-delivered user message, absorb it into the pending optimistic message.
    // Update the canonical message ID (for deduplication) and the displayed user name
    // to the server-authoritative value.
    if (msg.role === "user" && !msg.optimistic) {
      var optimistic = container.querySelector(
        '.chat-message[data-optimistic="true"]',
      );
      if (optimistic) {
        optimistic.setAttribute(
          "data-message-id",
          msg.id || "m" + container.children.length,
        );
        optimistic.removeAttribute("data-optimistic");
        // Update displayed user name to server-authoritative value
        var userSpan = optimistic.querySelector(".chat-user");
        if (userSpan && msg.userName) {
          userSpan.textContent = msg.userName;
        }
        return;
      }
    }

    var role = msg.role || "user";
    var div = document.createElement("div");
    div.className = "chat-message chat-message-" + role;
    div.setAttribute(
      "data-message-id",
      msg.id || "m" + container.children.length,
    );
    if (msg.optimistic) {
      div.setAttribute("data-optimistic", "true");
    }

    var user = document.createElement("span");
    user.className = "chat-user";
    user.textContent = msg.userName || msg.role || "";

    var bubble = document.createElement("div");
    bubble.className = "chat-bubble";

    var text = document.createElement("span");
    text.className = "chat-text";
    // Use server-pre-rendered HTML for agent messages when available
    if (msg.contentHtml) {
      text.innerHTML = msg.contentHtml;
    } else {
      text.innerHTML = escapeHtml(msg.content || "");
    }
    bubble.appendChild(text);

    // Render attachments in bubble
    if (msg.attachments && msg.attachments.length > 0) {
      var attachDiv = document.createElement("div");
      attachDiv.className = "chat-message-attachments";
      msg.attachments.forEach(function (att) {
        if (att.previewUrl) {
          var img = document.createElement("img");
          img.src = att.previewUrl;
          img.alt = att.fileName || att.name || "image";
          img.style.maxWidth = "200px";
          img.style.maxHeight = "150px";
          img.style.borderRadius = "8px";
          attachDiv.appendChild(img);
        } else {
          var badge = document.createElement("span");
          badge.className = "chat-attachment-badge";
          badge.textContent =
            "\uD83D\uDCC4 " + (att.fileName || att.name || "file");
          attachDiv.appendChild(badge);
        }
      });
      bubble.appendChild(attachDiv);
    }

    var time = document.createElement("span");
    time.className = "chat-time";
    time.textContent = formatTime(msg.time);

    div.appendChild(user);
    div.appendChild(bubble);
    div.appendChild(time);
    container.appendChild(div);
  }

  function ensureTaskCard(container, taskId, description, status, startTime) {
    var existingTaskEl = container.querySelector(
      '.chat-task[data-task-id="' + (taskId || "") + '"]',
    );

    if (existingTaskEl) {
      existingTaskEl.setAttribute("data-status", (status || "").toLowerCase());
      var desc = existingTaskEl.querySelector(".chat-task-description");
      if (desc && description) {
        desc.textContent = description;
      }
      var icon = existingTaskEl.querySelector(".chat-task-icon");
      if (icon) icon.textContent = getTaskIcon(status);

      // Update duration if completed
      if (status === "completed" || status === "error") {
        var st = parseInt(
          existingTaskEl.getAttribute("data-start-time") || Date.now(),
        );
        var dur = existingTaskEl.querySelector(".chat-task-time");
        if (dur) {
          dur.textContent = formatDuration(Date.now() - st);
        } else {
          dur = document.createElement("span");
          dur.className = "chat-task-time";
          dur.textContent = formatDuration(Date.now() - st);
          var header = existingTaskEl.querySelector(".chat-task-header");
          if (header) {
            header.appendChild(dur);
          }
        }
      }

      return existingTaskEl.querySelector(".chat-task-body") || existingTaskEl;
    }

    // Create new task card (borderless inline activity feed)
    var taskDivId = "task-" + (taskId || "current");
    var bodyId = taskDivId + "-body";
    var now = startTime || Date.now();

    var wrap = document.createElement("div");
    wrap.className = "chat-task";
    wrap.setAttribute("data-task-id", taskId || "");
    wrap.setAttribute("data-status", (status || "in_progress").toLowerCase());
    wrap.setAttribute("data-start-time", now);

    // Subtle status header (no box, just icon + text)
    var header = document.createElement("div");
    header.className = "chat-task-header";

    var icon = document.createElement("span");
    icon.className = "chat-task-icon";
    icon.textContent = getTaskIcon(status || "in_progress");

    var descSpan = document.createElement("span");
    descSpan.className = "chat-task-description";
    descSpan.textContent = description || "Processing...";

    // Expand arrow (hidden while in_progress, shown when task finishes)
    var arrow = document.createElement("span");
    arrow.className = "chat-task-expand";
    arrow.id = taskDivId + "-arrow";
    arrow.textContent = "\u25b6"; // ▶ (collapsed indicator)

    header.appendChild(icon);
    header.appendChild(descSpan);
    header.appendChild(arrow);
    wrap.appendChild(header);

    // Activity body (always visible, no collapse)
    var body = document.createElement("div");
    body.className = "chat-task-body";
    body.id = bodyId;
    wrap.appendChild(body);

    container.appendChild(wrap);
    return body;
  }

  function appendTimelineEvent(body, ev) {
    if (!body) return;

    var existingEv = body.querySelector(
      '[data-event-id="' + (ev.id || "") + '"]',
    );
    if (existingEv) {
      // Update existing agent call if result has changed
      if (ev.type === "agent_call" && ev.resultSummary) {
        var resultSection = existingEv.querySelector(".chat-agent-call-result");
        if (!resultSection) {
          // Result section doesn't exist yet, create it
          var agentBody = existingEv.querySelector(".chat-agent-call-body");
          if (agentBody) {
            var resultLabel = document.createElement("div");
            resultLabel.className = "chat-agent-call-result-label";
            resultLabel.textContent = "Result:";
            agentBody.appendChild(resultLabel);
            var resultPre = document.createElement("pre");
            resultPre.className = "chat-agent-call-result";
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

    var uniqueId = "ev-" + (ev.id || "") + "-" + Date.now();

    if (ev.type === "tool_call") {
      var toolWrap = document.createElement("div");
      toolWrap.className = "chat-tool-call";
      toolWrap.setAttribute("data-event-id", ev.id || "");
      toolWrap.setAttribute(
        "data-status",
        ev.result != null ? "success" : "running",
      );

      var arrowId = uniqueId + "-arrow";
      var resultId = uniqueId + "-result";

      // Clickable header
      var toolHeader = document.createElement("div");
      toolHeader.className = "chat-tool-header";
      toolHeader.setAttribute(
        "onclick",
        "toggleToolExpand('" + resultId + "', '" + arrowId + "')",
      );

      var toolName = document.createElement("span");
      toolName.className = "chat-tool-name";
      toolName.textContent = ev.toolName || ev.name || "Tool";

      var toolArgs = document.createElement("span");
      toolArgs.className = "chat-tool-args";
      var parsedArgs = null;
      if (ev.argsJson && ev.argsJson !== "{}") {
        try {
          parsedArgs = JSON.parse(ev.argsJson);
        } catch (e) {
          parsedArgs = null;
        }
      }
      if (parsedArgs && Object.keys(parsedArgs).length) {
        var argsStr = Object.keys(parsedArgs)
          .map(function (k) {
            var v = parsedArgs[k];
            if (typeof v === "string" && v.length > 50) {
              return k + '="' + v.substring(0, 47) + '..."';
            }
            return k + "=" + JSON.stringify(v);
          })
          .join(", ");
        toolArgs.textContent = "(" + argsStr + ")";
      }

      var toolArrow = document.createElement("span");
      toolArrow.className = "chat-tool-expand";
      toolArrow.id = arrowId;
      toolArrow.textContent = "\u203a"; // › (collapsed by default)

      toolHeader.appendChild(toolName);
      toolHeader.appendChild(toolArgs);
      toolHeader.appendChild(toolArrow);
      toolWrap.appendChild(toolHeader);

      // Collapsible result
      var toolResult = document.createElement("div");
      toolResult.className = "chat-tool-result";
      toolResult.id = resultId;
      toolResult.style.display = "none";

      if (ev.result != null) {
        var pre = document.createElement("pre");
        var code = document.createElement("code");
        code.className = "language-json";
        code.textContent = prettyPrintJson(ev.result);
        pre.appendChild(code);
        toolResult.appendChild(pre);
      }

      toolWrap.appendChild(toolResult);
      body.appendChild(toolWrap);
    } else if (ev.type === "agent_call") {
      var agentWrap = document.createElement("div");
      agentWrap.className = "chat-agent-call";
      agentWrap.setAttribute("data-event-id", ev.id || "");

      var arrowId = uniqueId + "-arrow";
      var bodyId = uniqueId + "-body";

      // Clickable header
      var agentHeader = document.createElement("div");
      agentHeader.className = "chat-agent-call-header";
      agentHeader.setAttribute(
        "onclick",
        "toggleToolExpand('" + bodyId + "', '" + arrowId + "')",
      );

      var agentLabel = document.createElement("span");
      agentLabel.className = "chat-agent-call-label";
      agentLabel.textContent =
        (ev.fromAgent || "") + " \u2192 " + (ev.toAgent || "");

      var agentArrow = document.createElement("span");
      agentArrow.className = "chat-agent-call-expand";
      agentArrow.id = arrowId;
      agentArrow.textContent = "\u25be"; // ▾ (expanded by default)

      agentHeader.appendChild(agentLabel);
      agentHeader.appendChild(agentArrow);
      agentWrap.appendChild(agentHeader);

      // Collapsible body (expanded by default so nested tools are visible)
      var agentBody = document.createElement("div");
      agentBody.className = "chat-agent-call-body";
      agentBody.id = bodyId;

      if (ev.inputSummary) {
        var inputLabel = document.createElement("div");
        inputLabel.className = "chat-agent-call-input-label";
        inputLabel.textContent = "Input:";
        agentBody.appendChild(inputLabel);
        var inputPre = document.createElement("pre");
        inputPre.className = "chat-agent-call-input";
        inputPre.textContent = ev.inputSummary;
        agentBody.appendChild(inputPre);
      }

      // Container for nested tool calls from this agent
      var toolsContainer = document.createElement("div");
      toolsContainer.className = "chat-agent-call-tools";
      agentBody.appendChild(toolsContainer);

      if (ev.resultSummary) {
        var resultLabel = document.createElement("div");
        resultLabel.className = "chat-agent-call-result-label";
        resultLabel.textContent = "Result:";
        agentBody.appendChild(resultLabel);
        var resultPre = document.createElement("pre");
        resultPre.className = "chat-agent-call-result";
        resultPre.textContent = ev.resultSummary;
        agentBody.appendChild(resultPre);
      }

      agentWrap.appendChild(agentBody);
      body.appendChild(agentWrap);
    }
  }

  function renderTaskSnapshot(container, taskDto) {
    if (!taskDto || !taskDto.taskId) return;

    var taskBody = ensureTaskCard(
      container,
      taskDto.taskId,
      taskDto.description || "Completed",
      taskDto.status || "completed",
      null,
    );

    // Render timeline events (tool calls and agent calls)
    if (taskDto.timeline && taskDto.timeline.length > 0) {
      taskDto.timeline.forEach(function (ev, idx) {
        ev.id = ev.id || "e" + idx;
        appendTimelineEvent(taskBody, ev);
      });
    }

    // Collapse the task (it's historical, show collapsed by default)
    var taskEl = taskBody.closest
      ? taskBody.closest(".chat-task")
      : taskBody.parentElement;
    if (taskEl) {
      var bodyId = taskBody.id;
      var arrowId = bodyId ? bodyId.replace(/-body$/, "-arrow") : null;
      taskBody.style.display = "none";
      var header = taskEl.querySelector(".chat-task-header");
      if (header && bodyId) {
        header.setAttribute(
          "onclick",
          "toggleTaskExpand('" + bodyId + "', '" + arrowId + "')",
        );
        header.style.cursor = "pointer";
      }
    }
  }

  function scrollToBottom(container) {
    if (container) container.scrollTop = container.scrollHeight;
  }

  function fetchJson(url, options) {
    options = options || {};
    options.headers = options.headers || {};
    options.headers["Content-Type"] =
      options.headers["Content-Type"] || "application/json";
    if (
      options.body &&
      typeof options.body === "object" &&
      !(options.body instanceof FormData)
    ) {
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

  // ── Session Management ──────────────────────────────────────

  function fetchSessions() {
    return fetchJson(CHAT_SESSIONS_URL, { method: "GET" });
  }

  function createSessionApi(title) {
    return fetchJson(CHAT_SESSIONS_URL, {
      method: "POST",
      body: { title: title || "New Chat" },
    });
  }

  function loadSessionApi(sessionId) {
    return fetchJson(CHAT_SESSIONS_URL + "/" + sessionId, { method: "GET" });
  }

  function renameSessionApi(sessionId, newTitle) {
    return fetchJson(CHAT_SESSIONS_URL + "/" + sessionId + "/title", {
      method: "PUT",
      body: { title: newTitle },
    });
  }

  function deleteSessionApi(sessionId) {
    return fetch(CHAT_SESSIONS_URL + "/" + sessionId, {
      method: "DELETE",
    }).then(function (res) {
      if (!res.ok) throw new Error(res.statusText || res.status);
    });
  }

  // ==================== Main Initialization ====================

  function initOne(root) {
    log("initOne called", { rootId: root.id });

    var existingConn = activeConnections.get(root);
    if (
      existingConn &&
      existingConn.eventSource &&
      existingConn.eventSource.readyState !== EventSource.CLOSED
    ) {
      log("Already initialized and connected, skipping");
      return;
    }

    cleanupConnection(root);
    root.setAttribute("data-tl-chat-inited", "true");

    var el = getElements(root);
    var pendingAttachments = [];
    var MAX_ATTACHMENTS = 5;
    var MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
    if (!el.messages || !el.input || !el.button) {
      log("Missing elements", {
        messages: !!el.messages,
        input: !!el.input,
        button: !!el.button,
      });
      return;
    }
    var userName = root.getAttribute("data-chat-user-name") || "User";
    el.welcome = root.querySelector(".chat-welcome");
    el.welcomeInput = root.querySelector(".chat-welcome-input");
    el.welcomeSendBtn = root.querySelector(".chat-welcome-send-btn");
    el.promptChips = root.querySelectorAll(".chat-prompt-chip");
    el.sidebarToggle = root.querySelector(".chat-sidebar-toggle");
    el.historyPanel = root.querySelector(".chat-history-panel");
    log("Elements found, initializing");

    function showWelcome() {
      if (el.welcome) el.welcome.classList.remove("hidden");
      if (el.messages) el.messages.style.display = "none";
      var inputSection = root.querySelector(".chat-input-section");
      if (inputSection) inputSection.style.display = "none";
      currentSessionId = null;
      setActiveItem(null);
    }

    function showChat() {
      if (el.welcome) el.welcome.classList.add("hidden");
      if (el.messages) el.messages.style.display = "";
      var inputSection = root.querySelector(".chat-input-section");
      if (inputSection) inputSection.style.display = "";
    }

    var welcomeSending = false;

    function sendFromWelcome(text) {
      if (welcomeSending) return;
      welcomeSending = true;
      createSessionApi("New Chat")
        .then(function (session) {
          welcomeSending = false;
          if (session && session.id) {
            var item = createSessionItem(session);
            if (historyList && historyList.firstChild) {
              historyList.insertBefore(item, historyList.firstChild);
            } else if (historyList) {
              historyList.appendChild(item);
            }
            currentSessionId = session.id;
            setActiveItem(session.id);
            showChat();
            if (el.welcomeInput) el.welcomeInput.value = "";
            if (el.messages) el.messages.innerHTML = "";
            connectSSE();

            // Send the message via the normal input path
            el.input.value = text;
            sendMessage();
          }
        })
        .catch(function (err) {
          welcomeSending = false;
          console.error("Failed to create session:", err);
        });
    }

    if (el.promptChips) {
      for (var i = 0; i < el.promptChips.length; i++) {
        (function (chip) {
          chip.addEventListener("click", function () {
            var text = chip.getAttribute("data-prompt");
            if (text) sendFromWelcome(text);
          });
        })(el.promptChips[i]);
      }
    }

    if (el.welcomeSendBtn && el.welcomeInput) {
      el.welcomeSendBtn.addEventListener("click", function () {
        var text = (el.welcomeInput.value || "").trim();
        if (text) sendFromWelcome(text);
      });
      el.welcomeInput.addEventListener("keydown", function (e) {
        if (e.key === "Enter" && !e.shiftKey) {
          e.preventDefault();
          var text = (el.welcomeInput.value || "").trim();
          if (text) sendFromWelcome(text);
        }
      });
    }

    var state = {
      eventSource: null,
      reconnectTimer: null,
      currentTaskId: null,
      currentTaskBody: null,
      currentTaskStartTime: null,
      currentAgentCallToolsBody: null,
      processing: false,
      destroyed: false,
    };
    activeConnections.set(root, state);

    function getCurrentTaskBody() {
      if (state.currentTaskId && !state.currentTaskBody) {
        var taskEl = el.messages.querySelector(
          '.chat-task[data-task-id="' + state.currentTaskId + '"]',
        );
        if (taskEl) {
          state.currentTaskBody = taskEl.querySelector(".chat-task-body");
        }
      }
      return state.currentTaskBody;
    }

    function connectSSE() {
      log("connectSSE called", { destroyed: state.destroyed });
      if (state.destroyed) return;

      if (!document.body.contains(root)) {
        log("Root not in DOM, destroying");
        state.destroyed = true;
        cleanupConnection(root);
        return;
      }

      if (state.eventSource) {
        state.eventSource.close();
        state.eventSource = null;
      }

      log("Creating EventSource", { url: CHAT_EVENTS_URL });
      var eventSource = new EventSource(CHAT_EVENTS_URL);
      state.eventSource = eventSource;

      eventSource.addEventListener("connected", function (e) {
        log("SSE connected event", e.data);
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
          log("SSE connected parse error", err);
        }
      });

      eventSource.addEventListener("message", function (e) {
        log("SSE message event", e.data);
        if (state.destroyed) return;
        try {
          var msg = JSON.parse(e.data);
          appendMessage(el.messages, msg);
          scrollToBottom(el.messages);
        } catch (err) {
          log("SSE message parse error", err);
        }
      });

      eventSource.addEventListener("tool_call", function (e) {
        if (state.destroyed) return;
        try {
          var ev = JSON.parse(e.data);
          ev.type = "tool_call";
          // Nest inside current agent call if one is active, else use task body
          var targetBody =
            state.currentAgentCallToolsBody || getCurrentTaskBody();
          if (targetBody) {
            appendTimelineEvent(targetBody, ev);
            scrollToBottom(el.messages);
          }
        } catch (err) {
          log("SSE tool_call parse error", err);
        }
      });

      eventSource.addEventListener("agent_call", function (e) {
        if (state.destroyed) return;
        try {
          var ev = JSON.parse(e.data);
          ev.type = "agent_call";
          var body = getCurrentTaskBody();
          if (body) {
            appendTimelineEvent(body, ev);
            scrollToBottom(el.messages);

            // Track the current agent call's tools container for nesting
            var agentEl = body.querySelector(
              '.chat-agent-call[data-event-id="' + (ev.id || "") + '"]',
            );
            if (agentEl) {
              var toolsContainer = agentEl.querySelector(
                ".chat-agent-call-tools",
              );
              if (toolsContainer && !ev.resultSummary) {
                // New agent call starting - track its tools container
                state.currentAgentCallToolsBody = toolsContainer;
              } else if (ev.resultSummary) {
                // Agent call completed - only clear if THIS agent's
                // container is still the active one. An older agent_call
                // re-emitted with its result must not wipe the container
                // of a newer, still-active agent.
                if (state.currentAgentCallToolsBody === toolsContainer) {
                  state.currentAgentCallToolsBody = null;
                }
              }
            }
          }
        } catch (err) {
          log("SSE agent_call parse error", err);
        }
      });

      eventSource.addEventListener("task_status", function (e) {
        log("SSE task_status event", e.data);
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
              state.currentTaskStartTime,
            );
            state.currentTaskBody = taskBody;
          }

          if (status.status === "completed" || status.status === "error") {
            state.processing = false;
            state.currentTaskId = null;
            state.currentTaskStartTime = null;
            state.currentAgentCallToolsBody = null;
            el.input.disabled = false;
            el.button.disabled = false;

            // Collapse the task trace and make it expandable
            var collapsedTaskEl = null;
            if (state.currentTaskBody) {
              var taskEl = state.currentTaskBody.closest(".chat-task");
              if (taskEl) {
                collapsedTaskEl = taskEl;
                var bodyId = state.currentTaskBody.id;
                var arrowId = bodyId.replace(/-body$/, "-arrow");
                // Collapse the body
                state.currentTaskBody.style.display = "none";
                // Make header clickable
                var header = taskEl.querySelector(".chat-task-header");
                if (header) {
                  header.setAttribute(
                    "onclick",
                    "toggleTaskExpand('" + bodyId + "', '" + arrowId + "')",
                  );
                  header.style.cursor = "pointer";
                }
                // Show the expand arrow
                var arrowEl = document.getElementById(arrowId);
                if (arrowEl) {
                  arrowEl.style.display = "";
                }
              }
            }
            state.currentTaskBody = null;

            // Scroll so the collapsed task header is visible at the top,
            // giving the user a clear view of the completion status and
            // the agent response that follows.
            if (collapsedTaskEl) {
              collapsedTaskEl.scrollIntoView({
                behavior: "smooth",
                block: "start",
              });
            } else {
              scrollToBottom(el.messages);
            }
          } else {
            scrollToBottom(el.messages);
          }
        } catch (err) {
          log("SSE task_status parse error", err);
        }
      });

      eventSource.addEventListener("heartbeat", function (e) {
        // Heartbeat received - connection is alive
      });

      eventSource.onerror = function (err) {
        log("SSE error", {
          readyState: eventSource.readyState,
          destroyed: state.destroyed,
        });
        if (state.destroyed) return;
        eventSource.close();
        state.eventSource = null;

        if (!document.body.contains(root)) {
          log("Root not in DOM after error, destroying");
          state.destroyed = true;
          cleanupConnection(root);
          return;
        }

        log("Scheduling reconnect in " + SSE_RECONNECT_DELAY_MS + "ms");
        state.reconnectTimer = setTimeout(connectSSE, SSE_RECONNECT_DELAY_MS);
      };
    }

    function isAllowedFile(file) {
      if (!file.type.startsWith("image/") && file.type !== "application/pdf") {
        return "Unsupported file type: " + file.type;
      }
      if (file.size > MAX_FILE_SIZE) {
        return "File too large: " + file.name + " (max 10 MB)";
      }
      return null;
    }

    function addFiles(files) {
      var remaining = MAX_ATTACHMENTS - pendingAttachments.length;
      var toAdd = Array.prototype.slice.call(files, 0, remaining);
      toAdd.forEach(function (file) {
        var error = isAllowedFile(file);
        if (error) {
          log("File rejected: " + error);
          return;
        }
        var reader = new FileReader();
        reader.onload = function (e) {
          var base64Full = e.target.result;
          var base64Data = base64Full.split(",")[1];
          var previewUrl = file.type.startsWith("image/") ? base64Full : null;
          pendingAttachments.push({
            file: file,
            name: file.name,
            mimeType: file.type,
            base64: base64Data,
            previewUrl: previewUrl,
            size: file.size,
          });
          renderAttachmentPreview();
        };
        reader.readAsDataURL(file);
      });
    }

    function removeAttachment(index) {
      pendingAttachments.splice(index, 1);
      renderAttachmentPreview();
    }

    function renderAttachmentPreview() {
      if (!el.preview) return;
      el.preview.innerHTML = "";
      if (pendingAttachments.length === 0) {
        el.preview.classList.remove("has-items");
        return;
      }
      el.preview.classList.add("has-items");
      pendingAttachments.forEach(function (att, idx) {
        var item = document.createElement("div");
        item.className = "chat-attachment-preview-item";
        if (att.previewUrl) {
          var img = document.createElement("img");
          img.src = att.previewUrl;
          img.alt = att.name;
          item.appendChild(img);
        } else {
          var icon = document.createElement("span");
          icon.textContent = "\uD83D\uDCC4";
          icon.style.fontSize = "1.5em";
          item.appendChild(icon);
        }
        var nameSpan = document.createElement("span");
        nameSpan.className = "chat-attachment-name";
        nameSpan.textContent = att.name;
        item.appendChild(nameSpan);
        var removeBtn = document.createElement("button");
        removeBtn.className = "chat-attachment-remove";
        removeBtn.textContent = "\u00D7";
        (function (capturedIdx) {
          removeBtn.onclick = function () {
            removeAttachment(capturedIdx);
          };
        })(idx);
        item.appendChild(removeBtn);
        el.preview.appendChild(item);
      });
    }

    function sendMessage() {
      var text = (el.input.value || "").trim();
      if (!text && pendingAttachments.length === 0) return;
      el.input.disabled = true;
      el.button.disabled = true;

      // Capture attachment metadata for the optimistic message
      var optimisticAttachments = pendingAttachments.map(function (att) {
        return {
          fileName: att.name,
          mimeType: att.mimeType,
          size: att.size,
          previewUrl: att.previewUrl,
        };
      });

      // Optimistically add user message to UI immediately (replaced by server message via SSE)
      var tempMsgId = "user-" + Date.now();
      appendMessage(el.messages, {
        id: tempMsgId,
        role: "user",
        userName: userName,
        content: text,
        time: new Date().toISOString(),
        optimistic: true,
        attachments: optimisticAttachments,
      });
      scrollToBottom(el.messages);

      var sendBody = { text: text || " " };
      if (currentSessionId) sendBody.sessionId = currentSessionId;
      if (pendingAttachments.length > 0) {
        sendBody.attachments = pendingAttachments.map(function (att) {
          return {
            fileName: att.name,
            mimeType: att.mimeType,
            base64Data: att.base64,
            size: att.size,
          };
        });
      }
      pendingAttachments = [];
      renderAttachmentPreview();
      fetchJson(CHAT_SEND_URL, { method: "POST", body: sendBody })
        .then(function (data) {
          if (data && data.status === "busy") {
            el.input.disabled = false;
            el.button.disabled = false;
            appendMessage(el.messages, {
              id: "busy-" + Date.now(),
              role: "system",
              userName: "System",
              content: "Please wait for the current response to complete.",
              time: "",
            });
            scrollToBottom(el.messages);
            return;
          }
          el.input.value = "";
          state.processing = true;
          state.currentTaskId = data.taskId;
          state.currentTaskBody = null;
          state.currentTaskStartTime = Date.now();

          if (data.taskId) {
            var taskBody = ensureTaskCard(
              el.messages,
              data.taskId,
              null,
              "in_progress",
              state.currentTaskStartTime,
            );
            state.currentTaskBody = taskBody;
          }
          scrollToBottom(el.messages);
        })
        .catch(function (err) {
          el.input.disabled = false;
          el.button.disabled = false;
          appendMessage(el.messages, {
            id: "err-" + Date.now(),
            role: "system",
            userName: "System",
            content: "Failed to send: " + (err.message || "Unknown error"),
            time: "",
          });
          scrollToBottom(el.messages);
        });
    }

    el.button.onclick = null;
    el.input.onkeydown = null;

    el.button.addEventListener("click", sendMessage);
    el.input.addEventListener("keydown", function (e) {
      if (e.key === "Enter" && !e.shiftKey) {
        e.preventDefault();
        sendMessage();
      }
    });

    // Attach button click
    if (el.attachBtn && el.fileInput) {
      el.attachBtn.addEventListener("click", function () {
        el.fileInput.click();
      });
      el.fileInput.addEventListener("change", function () {
        if (el.fileInput.files.length > 0) {
          addFiles(el.fileInput.files);
          el.fileInput.value = "";
        }
      });
    }

    // Drag and drop on input section
    var inputSection = root.querySelector(".chat-input-section");
    if (inputSection) {
      inputSection.addEventListener("dragover", function (e) {
        e.preventDefault();
        inputSection.classList.add("drag-over");
      });
      inputSection.addEventListener("dragleave", function (e) {
        e.preventDefault();
        inputSection.classList.remove("drag-over");
      });
      inputSection.addEventListener("drop", function (e) {
        e.preventDefault();
        inputSection.classList.remove("drag-over");
        if (e.dataTransfer.files.length > 0) {
          addFiles(e.dataTransfer.files);
        }
      });
    }

    // ── History Panel ──────────────────────────────────────────

    var historyList = root.querySelector(".chat-history-list");
    var newChatBtn = root.querySelector(".chat-new-session-btn");
    var currentSessionId = null;

    function renderSessionList(sessions) {
      if (!historyList) return;
      historyList.innerHTML = "";
      sessions.forEach(function (s) {
        historyList.appendChild(createSessionItem(s));
      });
    }

    function createSessionItem(session) {
      var item = document.createElement("div");
      item.className =
        "chat-history-item" +
        (session.id === currentSessionId ? " active" : "");
      item.setAttribute("data-session-id", session.id);

      var title = document.createElement("span");
      title.className = "chat-history-title";
      title.setAttribute("contenteditable", "true");
      title.textContent = session.title || "Untitled";
      title.addEventListener("blur", function () {
        var newTitle = title.textContent.trim();
        if (newTitle && newTitle !== session.title) {
          session.title = newTitle;
          renameSessionApi(session.id, newTitle);
        }
      });
      title.addEventListener("keydown", function (e) {
        if (e.key === "Enter") {
          e.preventDefault();
          title.blur();
        }
      });

      var deleteBtn = document.createElement("button");
      deleteBtn.className = "chat-history-delete-btn";
      deleteBtn.textContent = "\u00D7";
      deleteBtn.addEventListener("click", function (e) {
        e.stopPropagation();
        if (confirm("Delete this chat?")) {
          deleteSessionApi(session.id).then(function () {
            item.remove();
            if (session.id === currentSessionId) {
              var next = historyList.querySelector(".chat-history-item");
              if (next) {
                switchToSession(next.getAttribute("data-session-id"));
              } else {
                showWelcome();
              }
            }
          });
        }
      });

      item.addEventListener("click", function (e) {
        if (e.target === deleteBtn || e.target === title) return;
        switchToSession(session.id);
      });

      item.appendChild(title);
      item.appendChild(deleteBtn);
      return item;
    }

    function setActiveItem(sessionId) {
      if (!historyList) return;
      var items = historyList.querySelectorAll(".chat-history-item");
      items.forEach(function (item) {
        item.classList.toggle(
          "active",
          item.getAttribute("data-session-id") === sessionId,
        );
      });
    }

    function switchToSession(sessionId) {
      if (sessionId === currentSessionId) {
        // Deselect: clicking active session returns to welcome
        showWelcome();
        return;
      }
      currentSessionId = sessionId;
      setActiveItem(sessionId);
      showChat();

      // Clear current messages
      if (el.messages) {
        el.messages.innerHTML = "";
      }

      // Close existing SSE connection
      if (state.eventSource) {
        state.eventSource.close();
        state.eventSource = null;
      }
      if (state.reconnectTimer) {
        clearTimeout(state.reconnectTimer);
        state.reconnectTimer = null;
      }

      // Load session messages
      loadSessionApi(sessionId)
        .then(function (data) {
          // Try structured entries first (includes task snapshots)
          if (data && data.entries && data.entries.length > 0) {
            data.entries.forEach(function (entry) {
              if (entry.type === "message" && entry.messageData) {
                var msg = entry.messageData;
                appendMessage(el.messages, {
                  id: msg.id,
                  role: msg.role || "user",
                  content: msg.content,
                  contentHtml: msg.contentHtml,
                  userName: msg.userName,
                  time: msg.time || "",
                });
              } else if (entry.type === "task_snapshot" && entry.taskData) {
                renderTaskSnapshot(el.messages, entry.taskData);
              }
            });
            scrollToBottom(el.messages);
          } else if (data && data.messagesJson) {
            // Fallback: try parsing messagesJson as structured format
            try {
              var parsed = JSON.parse(data.messagesJson);
              if (parsed.entries) {
                parsed.entries.forEach(function (entry) {
                  if (entry.type === "message" && entry.messageData) {
                    var msg = entry.messageData;
                    appendMessage(el.messages, {
                      id: msg.id,
                      role: msg.role || "user",
                      content: msg.content,
                      contentHtml: msg.contentHtml,
                      userName: msg.userName,
                      time: msg.time || "",
                    });
                  } else if (entry.type === "task_snapshot" && entry.taskData) {
                    renderTaskSnapshot(el.messages, entry.taskData);
                  }
                });
              } else if (Array.isArray(parsed)) {
                // Legacy format: [{role, text, timestamp}, ...]
                parsed.forEach(function (msg) {
                  appendMessage(el.messages, {
                    role:
                      msg.role === "Agent" || msg.role === "AI"
                        ? "agent"
                        : msg.role.toLowerCase(),
                    content: msg.text,
                    userName: msg.role,
                    time: msg.timestamp || "",
                  });
                });
              }
              scrollToBottom(el.messages);
            } catch (e) {
              console.error("Failed to parse session messages:", e);
            }
          }
          // Reconnect SSE
          connectSSE();
        })
        .catch(function (err) {
          console.error("Failed to load session:", err);
          connectSSE();
        });
    }

    function handleNewChat() {
      createSessionApi("New Chat").then(function (session) {
        if (session && session.id) {
          var item = createSessionItem(session);
          if (historyList && historyList.firstChild) {
            historyList.insertBefore(item, historyList.firstChild);
          } else if (historyList) {
            historyList.appendChild(item);
          }
          switchToSession(session.id);
        }
      });
    }

    // Wire up New Chat button
    if (newChatBtn) {
      newChatBtn.addEventListener("click", handleNewChat);
    }

    // Load sessions on init — always start on welcome screen
    fetchSessions()
      .then(function (data) {
        var sessions = data && data.sessions ? data.sessions : [];
        if (sessions.length > 0) {
          renderSessionList(sessions);
        }
        // Always start on welcome screen
        showWelcome();
        connectSSE();
      })
      .catch(function (err) {
        console.error("Failed to load sessions:", err);
        showWelcome();
        connectSSE();
      });
  }

  function init() {
    var roots = document.querySelectorAll('[data-tl-chat="v1"]');
    log("init() found " + roots.length + " chat roots");
    for (var i = 0; i < roots.length; i++) {
      initOne(roots[i]);
    }
  }

  window._tlChatInit = init;

  function startObserving() {
    log("startObserving called");
    var body = document.body;
    if (!body) {
      log("No document.body, aborting");
      return;
    }

    if (window._tlChatObserver) {
      log("Observer already exists, calling init()");
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
          if (node.getAttribute && node.getAttribute("data-tl-chat") === "v1") {
            initOne(node);
          }
          var roots = node.querySelectorAll
            ? node.querySelectorAll('[data-tl-chat="v1"]')
            : [];
          for (var r = 0; r < roots.length; r++) {
            initOne(roots[r]);
          }
        }

        var removed = mutation.removedNodes;
        for (var x = 0; x < removed.length; x++) {
          var removedNode = removed[x];
          if (removedNode.nodeType !== 1) continue;
          if (
            removedNode.getAttribute &&
            removedNode.getAttribute("data-tl-chat") === "v1"
          ) {
            cleanupConnection(removedNode);
          }
          var removedRoots = removedNode.querySelectorAll
            ? removedNode.querySelectorAll('[data-tl-chat="v1"]')
            : [];
          for (var rr = 0; rr < removedRoots.length; rr++) {
            cleanupConnection(removedRoots[rr]);
          }
        }
      }
    });
    observer.observe(body, { childList: true, subtree: true });
    window._tlChatObserver = observer;
    log("MutationObserver set up");

    init();
  }

  function run() {
    log("run() called", { readyState: document.readyState });
    if (document.readyState === "loading") {
      document.addEventListener("DOMContentLoaded", function () {
        startObserving();
      });
    } else {
      startObserving();
    }
  }

  window.addEventListener("beforeunload", function () {
    var roots = document.querySelectorAll('[data-tl-chat="v1"]');
    for (var i = 0; i < roots.length; i++) {
      cleanupConnection(roots[i]);
    }
  });

  run();
})();
