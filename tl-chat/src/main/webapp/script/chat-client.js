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
  var CHAT_RESPOND_URL = BASE + "/chat/respond";
  var CHAT_APPROVAL_MODE_URL = BASE + "/chat/approval-mode";
  var CHAT_REVIEW_URL = BASE + "/chat/review";

  // Track active SSE connections per chat root ID (not DOM reference, which changes on re-render)
  var activeConnections = {};

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
    var reviewBtn =
      (root.getAttribute("data-chat-review-btn-id") &&
        doc.getElementById(root.getAttribute("data-chat-review-btn-id"))) ||
      root.querySelector(".chat-review-button");
    var reviewAlerts = root.querySelector(".chat-review-alerts");
    return {
      messages: messages,
      input: input,
      button: button,
      fileInput: fileInput,
      attachBtn: attachBtn,
      preview: preview,
      reviewBtn: reviewBtn,
      reviewAlerts: reviewAlerts,
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
    if (msg.interactionTemp) {
      div.setAttribute("data-interaction-temp", "true");
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
      agentArrow.textContent = "\u203a"; // › (collapsed by default)

      agentHeader.appendChild(agentLabel);
      agentHeader.appendChild(agentArrow);
      agentWrap.appendChild(agentHeader);

      // Collapsible body (collapsed by default)
      var agentBody = document.createElement("div");
      agentBody.className = "chat-agent-call-body";
      agentBody.id = bodyId;
      agentBody.style.display = "none";

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

  function cleanupConnection(rootId) {
    var conn = activeConnections[rootId];
    if (conn) {
      log("Cleaning up connection for " + rootId);
      if (conn.eventSource) {
        conn.eventSource.close();
      }
      if (conn.reconnectTimer) {
        clearTimeout(conn.reconnectTimer);
      }
      conn.destroyed = true;
      delete activeConnections[rootId];
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
    var rootId = root.id || root.getAttribute("data-tl-chat");
    log("initOne called", { rootId: rootId });

    // Guard: skip if this exact DOM element was already initialized.
    // This prevents duplicate event listeners when both the MutationObserver
    // and the script-reload guard (window._tlChatInit) call initOne on the
    // same newly added element.
    if (root.getAttribute("data-tl-chat-inited") === "true") {
      log("Already initialized, skipping", { rootId: rootId });
      return;
    }

    // Clean up any existing connection for this root ID (handles DOM replacement)
    cleanupConnection(rootId);
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
    el.welcomeFileInput = root.querySelector(".chat-welcome-file-input");
    el.welcomeAttachBtn = root.querySelector(".chat-welcome-attach-btn");
    el.welcomePreview = root.querySelector(".chat-welcome-attachment-preview");
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
      if (!text && pendingAttachments.length === 0) return;
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

            // Send the message via the normal input path (attachments carried via pendingAttachments)
            el.input.value = text || "";
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
        if (text || pendingAttachments.length > 0) sendFromWelcome(text);
      });
      el.welcomeInput.addEventListener("keydown", function (e) {
        if (e.key === "Enter" && !e.shiftKey) {
          e.preventDefault();
          var text = (el.welcomeInput.value || "").trim();
          if (text || pendingAttachments.length > 0) sendFromWelcome(text);
        }
      });
    }
    // Welcome page attachment button
    if (el.welcomeAttachBtn && el.welcomeFileInput) {
      el.welcomeAttachBtn.addEventListener("click", function () {
        el.welcomeFileInput.click();
      });
      el.welcomeFileInput.addEventListener("change", function () {
        if (el.welcomeFileInput.files.length > 0) {
          addFiles(el.welcomeFileInput.files);
          el.welcomeFileInput.value = "";
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
    activeConnections[rootId] = state;

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

    /**
     * Returns the container element for agent task panels.
     * Prefers the current task card (.chat-task) so that each processing round
     * gets its own panel. Falls back to the messages area.
     */
    function getAgentTaskContainer() {
      var taskBody = getCurrentTaskBody();
      if (taskBody) {
        var taskCard = taskBody.closest(".chat-task");
        if (taskCard) return taskCard;
      }
      return el.messages;
    }

    /**
     * Ensures the agent task panel container exists inside the given container.
     * Uses :scope > .chat-agent-tasks to avoid finding panels in child elements
     * (e.g. from a previous processing round's task card).
     */
    function ensureAgentTaskPanel(container) {
      var children = container.children;
      for (var i = 0; i < children.length; i++) {
        if (children[i].classList.contains("chat-agent-tasks")) {
          return children[i];
        }
      }
      var panel = document.createElement("div");
      panel.className = "chat-agent-tasks";
      container.appendChild(panel);
      return panel;
    }

    /**
     * Appends a new agent task to the task panel.
     */
    function appendAgentTask(container, task) {
      var panel = ensureAgentTaskPanel(container);
      var el = document.createElement("div");
      el.className = "chat-agent-task";
      el.setAttribute("data-task-id", task.taskId);
      var icon = document.createElement("span");
      icon.className = "chat-agent-task-icon";
      icon.textContent = getAgentTaskIcon(task.status);
      var text = document.createElement("span");
      text.className = "chat-agent-task-text";
      text.textContent = task.activeForm || task.subject || "";
      el.appendChild(icon);
      el.appendChild(text);
      panel.appendChild(el);
    }

    /**
     * Updates an existing agent task in the task panel.
     */
    function updateAgentTask(container, task) {
      var panel = container.querySelector(".chat-agent-tasks");
      var taskEl = panel ? panel.querySelector('.chat-agent-task[data-task-id="' + task.taskId + '"]') : null;
      if (!taskEl) {
        appendAgentTask(container, task);
        return;
      }
      var icon = taskEl.querySelector(".chat-agent-task-icon");
      var text = taskEl.querySelector(".chat-agent-task-text");
      if (icon) icon.textContent = getAgentTaskIcon(task.status);
      if (text) text.textContent = task.activeForm || task.subject || "";
      if (task.status === "completed") {
        taskEl.classList.add("chat-agent-task-done");
      }
      if (task.status === "deleted") {
        taskEl.style.display = "none";
      }
    }

    /**
     * Returns the status icon for an agent task.
     */
    function getAgentTaskIcon(status) {
      switch (status) {
        case "in_progress": return "\u25F4";
        case "completed": return "\u2713";
        case "deleted": return "";
        default: return "\u25CB";
      }
    }

    // ==================== Inline Interaction Banner ====================

    // Active interaction state (null when no interaction active)
    var activeInteraction = null;

    function showInteractionBanner(request) {
      // 1. Render question as a temporary agent message bubble (with markdown rendering)
      appendMessage(el.messages, {
        id: "interaction-q-" + request.requestId,
        role: "agent",
        userName: "Agent",
        content: request.question,
        contentHtml: request.questionHtml,
        time: new Date().toISOString(),
        interactionTemp: true,
      });
      scrollToBottom(el.messages);

      // 2. Remove any existing banner
      removeInteractionBanner();

      // 3. Build banner element
      var banner = document.createElement("div");
      banner.className = "interaction-banner";
      banner.id = "interaction-banner-" + request.requestId;
      banner.setAttribute("data-request-id", request.requestId);

      var headerDiv = document.createElement("div");
      headerDiv.className = "interaction-banner-header";
      headerDiv.textContent =
        request.header ||
        (request.type === "approval"
          ? "Confirm"
          : request.type === "text"
            ? "Clarify"
            : "Choose");
      banner.appendChild(headerDiv);

      if (request.type === "approval") {
        var btnRow = document.createElement("div");
        btnRow.className = "interaction-banner-buttons";
        btnRow.innerHTML =
          '<button class="approval-btn reject" data-response="rejected">Reject</button>' +
          '<button class="approval-btn approve" data-response="approved">Approve</button>' +
          '<button class="approval-btn approve-all" data-response="approved" data-approve-all="true">Approve All</button>';
        banner.appendChild(btnRow);

        btnRow.addEventListener("click", function (e) {
          var btn = e.target.closest(".approval-btn");
          if (!btn) return;
          var response = btn.getAttribute("data-response");
          var approveAll = btn.getAttribute("data-approve-all") === "true";
          handleInteractionResponse(request.requestId, response, approveAll);
        });
      } else if (request.type === "choice") {
        var opts = [];
        try {
          opts =
            typeof request.options === "string"
              ? JSON.parse(request.options)
              : request.options || [];
        } catch (e) {
          opts = [];
        }
        var multiSelect = request.multiSelect === true;

        var chipsDiv = document.createElement("div");
        chipsDiv.className = "interaction-chips";

        opts.forEach(function (opt) {
          var chip = document.createElement("span");
          chip.className = "interaction-chip";
          chip.setAttribute("data-value", opt.label);
          if (opt.description) {
            chip.setAttribute("title", opt.description);
          }

          if (multiSelect) {
            var cb = document.createElement("span");
            cb.className = "interaction-chip-checkbox";
            cb.textContent = "\u2713";
            chip.appendChild(cb);
          }

          var labelSpan = document.createElement("span");
          labelSpan.textContent = opt.label;
          chip.appendChild(labelSpan);

          chip.addEventListener("click", function () {
            if (multiSelect) {
              chip.classList.toggle("selected");
            } else {
              chipsDiv
                .querySelectorAll(".interaction-chip")
                .forEach(function (c) {
                  c.classList.remove("selected");
                });
              chip.classList.add("selected");
            }
          });

          chipsDiv.appendChild(chip);
        });
        banner.appendChild(chipsDiv);

        var submitRow = document.createElement("div");
        submitRow.className = "interaction-banner-buttons";
        submitRow.style.justifyContent = "flex-end";
        var submitBtn = document.createElement("button");
        submitBtn.className = "approval-btn approve";
        submitBtn.textContent = "Submit";
        submitBtn.addEventListener("click", function () {
          var selected = chipsDiv.querySelectorAll(
            ".interaction-chip.selected",
          );
          if (selected.length === 0) return;
          var values = [];
          selected.forEach(function (c) {
            values.push(c.getAttribute("data-value"));
          });
          var response = multiSelect ? JSON.stringify(values) : values[0];
          handleInteractionResponse(request.requestId, response, false);
        });
        submitRow.appendChild(submitBtn);
        banner.appendChild(submitRow);

        // Also enable the text input so the user can type a freeform answer
        // instead of picking one of the choices.
        activeInteraction = {
          requestId: request.requestId,
          type: "choice",
          originalPlaceholder: el.input.placeholder,
        };
        el.input.placeholder = "Or type your own answer...";
        el.input.disabled = false;
      } else if (request.type === "text") {
        // Text: banner is minimal, chat input is re-purposed
        activeInteraction = {
          requestId: request.requestId,
          type: "text",
          originalPlaceholder: el.input.placeholder,
        };
        el.input.placeholder = "Reply to agent...";
        el.input.disabled = false;
        el.input.focus();
      }

      // 4. Insert banner between chat-messages and chat-input-section
      var inputSection = root.querySelector(".chat-input-section");
      if (inputSection && inputSection.parentNode) {
        inputSection.parentNode.insertBefore(banner, inputSection);
      }

      // Store active interaction for types that don't set it themselves
      if (request.type !== "text" && request.type !== "choice") {
        activeInteraction = {
          requestId: request.requestId,
          type: request.type,
        };
      }

      // Auto-remove banner after 5 minutes (matches server timeout)
      var timeoutId = setTimeout(
        function () {
          if (root.querySelector("#interaction-banner-" + request.requestId)) {
            removeInteractionBanner();
            appendMessage(el.messages, {
              id: "timeout-" + request.requestId,
              role: "system",
              userName: "System",
              content: "Interaction timed out.",
              time: new Date().toISOString(),
            });
            scrollToBottom(el.messages);
          }
        },
        5 * 60 * 1000,
      );
      banner.setAttribute("data-timeout-id", timeoutId);

      scrollToBottom(el.messages);
    }

    function handleInteractionResponse(requestId, response, approveAll) {
      // Add user's response as a temporary message bubble
      appendMessage(el.messages, {
        id: "interaction-r-" + requestId,
        role: "user",
        userName: userName || "You",
        content: response,
        time: new Date().toISOString(),
        interactionTemp: true,
      });
      scrollToBottom(el.messages);

      submitInteraction(requestId, response, approveAll);
      removeInteractionBanner();
    }

    function removeInteractionBanner() {
      var banners = root.querySelectorAll(".interaction-banner");
      banners.forEach(function (b) {
        var tid = b.getAttribute("data-timeout-id");
        if (tid) clearTimeout(parseInt(tid, 10));
        b.remove();
      });

      // Restore chat input state
      if (
        activeInteraction &&
        (activeInteraction.type === "text" ||
          activeInteraction.type === "choice")
      ) {
        el.input.placeholder =
          activeInteraction.originalPlaceholder || "Type a message...";
      }
      activeInteraction = null;
    }

    function submitInteraction(requestId, response, approveAll) {
      fetch(CHAT_RESPOND_URL, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ requestId: requestId, response: response }),
      })
        .then(function (resp) {
          if (resp.ok) {
            if (approveAll) {
              setApprovalMode("AUTO_APPROVE");
            }
          } else {
            log("Interaction submit failed", resp.status);
          }
        })
        .catch(function (err) {
          log("Interaction submit error", err);
        });
    }

    // Current approval mode state
    var approvalMode = "MANUAL";

    function setApprovalMode(mode) {
      fetch(CHAT_APPROVAL_MODE_URL, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ mode: mode }),
      })
        .then(function (resp) {
          if (resp.ok) {
            approvalMode = mode;
            updateApprovalModeIndicator();
          } else {
            log("setApprovalMode failed", resp.status);
          }
        })
        .catch(function (err) {
          log("setApprovalMode error", err);
        });
    }

    function updateApprovalModeIndicator() {
      var indicator = root.querySelector(".approval-mode-indicator");
      if (!indicator) return;
      if (approvalMode === "AUTO_APPROVE") {
        indicator.classList.add("active");
        indicator.title = "Auto-approve is ON — click to disable";
      } else {
        indicator.classList.remove("active");
        indicator.title = "Auto-approve is OFF — click to enable";
      }
    function showReviewAlert(alert) {
      var container = el.reviewAlerts;
      if (!container) {
        container = root.querySelector(".chat-review-alerts");
      }
      if (!container) return;

      var banner = document.createElement("div");
      banner.className = "chat-review-alert";
      if (alert.severity === "warning" || alert.severity === "high") {
        banner.classList.add("chat-review-alert-warning");
      } else if (alert.severity === "error" || alert.severity === "critical") {
        banner.classList.add("chat-review-alert-error");
      }

      var textSpan = document.createElement("span");
      textSpan.className = "chat-review-alert-text";
      textSpan.textContent = alert.title || "Review alert";
      banner.appendChild(textSpan);

      if (alert.category) {
        var catSpan = document.createElement("span");
        catSpan.className = "chat-review-alert-category";
        catSpan.textContent = alert.category;
        banner.appendChild(catSpan);
      }

      var closeBtn = document.createElement("button");
      closeBtn.type = "button";
      closeBtn.className = "chat-review-alert-close";
      closeBtn.textContent = "\u00D7"; // x
      closeBtn.addEventListener("click", function () {
        banner.classList.add("chat-review-alert-dismissed");
        setTimeout(function () {
          if (banner.parentNode) banner.parentNode.removeChild(banner);
        }, 300);
      });
      banner.appendChild(closeBtn);

      container.appendChild(banner);
    }

    function connectSSE() {
      log("connectSSE called", { destroyed: state.destroyed });
      if (state.destroyed) return;

      if (!document.body.contains(root)) {
        log("Root not in DOM, destroying");
        state.destroyed = true;
        cleanupConnection(rootId);
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

            // Remove temporary interaction messages (question + response bubbles)
            el.messages
              .querySelectorAll('[data-interaction-temp="true"]')
              .forEach(function (tempEl) {
                tempEl.remove();
              });
            // Also remove any lingering interaction banner
            removeInteractionBanner();

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

      eventSource.addEventListener("agent_task_created", function (e) {
        if (state.destroyed) return;
        try {
          var task = JSON.parse(e.data);
          appendAgentTask(getAgentTaskContainer(), task);
          scrollToBottom(el.messages);
        } catch (err) {
          log("SSE agent_task_created parse error", err);
        }
      });

      eventSource.addEventListener("agent_task_updated", function (e) {
        if (state.destroyed) return;
        try {
          var task = JSON.parse(e.data);
          updateAgentTask(getAgentTaskContainer(), task);
        } catch (err) {
          log("SSE agent_task_updated parse error", err);
        }
      });

      eventSource.addEventListener("user_interaction", function (e) {
        log("SSE user_interaction event", e.data);
        try {
          var request = JSON.parse(e.data);
          showInteractionBanner(request);
        } catch (err) {
          log("SSE user_interaction parse error", err);
        }
      });

      eventSource.addEventListener("approval_mode", function (e) {
        log("SSE approval_mode event", e.data);
        try {
          var data = JSON.parse(e.data);
          approvalMode = data.mode || "MANUAL";
          updateApprovalModeIndicator();
          if (data.mode === "AUTO_APPROVE") {
            removeInteractionBanner();
          }
        } catch (err) {
          log("SSE approval_mode parse error", err);
        }
      });

      eventSource.addEventListener("background_alert", function (e) {
        log("SSE background_alert event", e.data);
        if (state.destroyed) return;
        try {
          var alert = JSON.parse(e.data);
          showReviewAlert(alert);
        } catch (err) {
          log("SSE background_alert parse error", err);
        }
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
          cleanupConnection(rootId);
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
      // Render into the active preview container (welcome or chat)
      var isWelcomeVisible =
        el.welcome && !el.welcome.classList.contains("hidden");
      var container = isWelcomeVisible ? el.welcomePreview : el.preview;
      // Clear both containers
      if (el.preview) {
        el.preview.innerHTML = "";
        el.preview.classList.remove("has-items");
      }
      if (el.welcomePreview) {
        el.welcomePreview.innerHTML = "";
        el.welcomePreview.classList.remove("has-items");
      }
      if (!container || pendingAttachments.length === 0) return;
      container.classList.add("has-items");
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
        container.appendChild(item);
      });
    }

    function sendMessage() {
      if (state.destroyed) return;

      // If a text or choice interaction is active, route typed input to interaction response
      if (
        activeInteraction &&
        (activeInteraction.type === "text" ||
          activeInteraction.type === "choice")
      ) {
        var interactionText = (el.input.value || "").trim();
        if (!interactionText) return;
        el.input.value = "";
        handleInteractionResponse(
          activeInteraction.requestId,
          interactionText,
          false,
        );
        return;
      }

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
          // Move active session to top of history list
          if (currentSessionId) {
            moveSessionToTop(currentSessionId);
          }
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

    // Auto-approve toggle click
    var approvalToggle = root.querySelector(".approval-mode-indicator");
    if (approvalToggle) {
      approvalToggle.addEventListener("click", function () {
        var newMode =
          approvalMode === "AUTO_APPROVE" ? "MANUAL" : "AUTO_APPROVE";
        setApprovalMode(newMode);
      });
    }

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

    // Review button click
    if (el.reviewBtn) {
      el.reviewBtn.addEventListener("click", function () {
        if (el.reviewBtn.disabled) return;
        el.reviewBtn.disabled = true;
        el.reviewBtn.classList.add("chat-review-loading");
        fetchJson(CHAT_REVIEW_URL, { method: "POST", body: {} })
          .then(function (data) {
            log("Review started", data);
            // Show brief indicator on button
            el.reviewBtn.classList.remove("chat-review-loading");
            el.reviewBtn.classList.add("chat-review-started");
            setTimeout(function () {
              el.reviewBtn.classList.remove("chat-review-started");
              el.reviewBtn.disabled = false;
            }, 3000);
          })
          .catch(function (err) {
            log("Review failed", err);
            el.reviewBtn.classList.remove("chat-review-loading");
            el.reviewBtn.disabled = false;
          });
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

    // Drag and drop on welcome input section
    var welcomeInputSection = root.querySelector(".chat-welcome-input-section");
    if (welcomeInputSection) {
      welcomeInputSection.addEventListener("dragover", function (e) {
        e.preventDefault();
        welcomeInputSection.classList.add("drag-over");
      });
      welcomeInputSection.addEventListener("dragleave", function (e) {
        e.preventDefault();
        welcomeInputSection.classList.remove("drag-over");
      });
      welcomeInputSection.addEventListener("drop", function (e) {
        e.preventDefault();
        welcomeInputSection.classList.remove("drag-over");
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

    function formatRelativeTime(isoString) {
      if (!isoString) return "";
      var date = new Date(isoString);
      var now = new Date();
      var diffMs = now - date;
      var diffMin = Math.floor(diffMs / 60000);
      var diffHr = Math.floor(diffMs / 3600000);
      var diffDay = Math.floor(diffMs / 86400000);
      if (diffMin < 1) return "just now";
      if (diffMin < 60) return diffMin + "m ago";
      if (diffHr < 24) return diffHr + "h ago";
      if (diffDay < 7) return diffDay + "d ago";
      return date.toLocaleDateString();
    }

    function moveSessionToTop(sessionId) {
      if (!historyList) return;
      var item = historyList.querySelector(
        '[data-session-id="' + sessionId + '"]',
      );
      if (item && item !== historyList.firstChild) {
        historyList.insertBefore(item, historyList.firstChild);
      }
      // Update the timestamp display
      if (item) {
        var timeEl = item.querySelector(".chat-history-time");
        if (timeEl) {
          timeEl.textContent = "just now";
        }
      }
    }

    function createSessionItem(session) {
      var item = document.createElement("div");
      item.className =
        "chat-history-item" +
        (session.id === currentSessionId ? " active" : "");
      item.setAttribute("data-session-id", session.id);

      var titleRow = document.createElement("div");
      titleRow.className = "chat-history-title-row";

      var title = document.createElement("span");
      title.className = "chat-history-title";
      title.textContent = session.title || "Untitled";

      var editing = false;

      function startEditing(e) {
        if (e) e.stopPropagation();
        editing = true;
        title.setAttribute("contenteditable", "true");
        title.classList.add("editing");
        editBtn.classList.add("editing");
        title.focus();
        // Select all text
        var range = document.createRange();
        range.selectNodeContents(title);
        var sel = window.getSelection();
        sel.removeAllRanges();
        sel.addRange(range);
      }

      function stopEditing(e) {
        if (e) e.stopPropagation();
        if (!editing) return;
        editing = false;
        title.removeAttribute("contenteditable");
        title.classList.remove("editing");
        editBtn.classList.remove("editing");
        var newTitle = title.textContent.trim();
        if (newTitle && newTitle !== session.title) {
          session.title = newTitle;
          renameSessionApi(session.id, newTitle);
        } else if (!newTitle) {
          title.textContent = session.title || "Untitled";
        }
      }

      title.addEventListener("keydown", function (e) {
        if (e.key === "Enter") {
          e.preventDefault();
          stopEditing(e);
        }
        if (e.key === "Escape") {
          e.preventDefault();
          title.textContent = session.title || "Untitled";
          stopEditing(e);
        }
      });

      title.addEventListener("blur", function () {
        // Delay to allow save button click to register
        setTimeout(function () {
          if (editing) stopEditing();
        }, 150);
      });

      // Edit/Save toggle button
      var editBtn = document.createElement("button");
      editBtn.className = "chat-history-edit-btn";
      editBtn.title = "Rename";
      editBtn.innerHTML = "&#9998;"; // Pencil icon
      editBtn.addEventListener("click", function (e) {
        e.stopPropagation();
        if (editing) {
          stopEditing(e);
        } else {
          startEditing(e);
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
        if (e.target === deleteBtn || e.target === editBtn || editing) return;
        switchToSession(session.id);
      });

      var timeEl = document.createElement("span");
      timeEl.className = "chat-history-time";
      timeEl.textContent = formatRelativeTime(session.lastUpdated);

      titleRow.appendChild(title);
      titleRow.appendChild(editBtn);
      titleRow.appendChild(deleteBtn);
      item.appendChild(titleRow);
      item.appendChild(timeEl);
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

      // Reset processing state so the "thinking" indicator from the previous
      // session doesn't bleed into the new one. The SSE connected event from
      // the new connection will set the correct state.
      state.processing = false;
      state.currentTaskId = null;
      state.currentTaskBody = null;
      state.currentTaskStartTime = null;
      state.currentAgentCallToolsBody = null;
      el.input.disabled = false;
      el.button.disabled = false;

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
                  attachments: msg.attachments,
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
                      attachments: msg.attachments,
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
            var removedId =
              removedNode.id || removedNode.getAttribute("data-tl-chat");
            cleanupConnection(removedId);
          }
          var removedRoots = removedNode.querySelectorAll
            ? removedNode.querySelectorAll('[data-tl-chat="v1"]')
            : [];
          for (var rr = 0; rr < removedRoots.length; rr++) {
            var rrId =
              removedRoots[rr].id ||
              removedRoots[rr].getAttribute("data-tl-chat");
            cleanupConnection(rrId);
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
      var rid = roots[i].id || roots[i].getAttribute("data-tl-chat");
      cleanupConnection(rid);
    }
  });

  run();
})();
