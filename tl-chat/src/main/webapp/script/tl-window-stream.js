/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

/*
 * The window's events by polling, where a proxy holds the event stream back.
 *
 * Temporary workaround (#940) for an engine without its own fallback. Every result of a React
 * command arrives on the window's event stream (react-api/events). A corporate proxy that inspects
 * content (Zscaler, Netskope) may hold a text/event-stream response back until it ends, forward it
 * only in blocks of some kilobytes, or refuse it - and the page then does not react at all: no
 * login dialog, no navigation, no chat answer.
 *
 * This script is loaded as a classic script, before the engine's bridge, and puts a stand-in for
 * EventSource in place for the window's stream only. The stand-in uses the real stream while it
 * delivers, and polls window-poll (WindowPollServlet) from the moment
 *  - the stream brings no event within 3 s after it was opened,
 *  - the stream is refused (a closed EventSource; the first poll tells a lost session apart and
 *    passes it on to the bridge, which reloads the page as before),
 *  - a probe does not arrive: after each React command, and when the chat calls
 *    TLWindowStream.expect(), the server puts a small patch on the stream; a stream that does not
 *    bring it within 3 s is held back somewhere.
 * A tab that had to poll polls at once after a reload. The bridge sees the polled events as the
 * messages of its EventSource.
 *
 * Remove this script, with WindowPollServlet and PolledWindow, when the engine polls by itself.
 * It relies on what the bridge does with its EventSource today: it creates it with
 * `new EventSource(url)` when it connects, and reads onmessage, onerror, readyState and close().
 */
(function () {
  'use strict';

  var Native = window.EventSource;
  if (!Native || window.TLWindowStream) return;

  var FIRST_MESSAGE_MS = 3000;
  var PROBE_MS = 3000;
  /** A probe that arrived makes a further probe within this unnecessary. */
  var CONFIRMED_MS = 5000;
  var MAX_BACKOFF_MS = 10000;
  var POLLING_KEY = 'tl-window-stream-polling';
  var PROBE_LISTENER = 'tl-window-probe';
  var STREAM_PATH = 'react-api/events';

  var nativeFetch = window.fetch.bind(window);

  /** The window stream of this page, if one is open. */
  var active = null;

  /**
   * Set once the page is being left: the browser then closes the stream, which must not count as
   * a refused stream.
   */
  var leaving = false;
  window.addEventListener('beforeunload', function () {
    leaving = true;
  });
  window.addEventListener('pagehide', function () {
    leaving = true;
  });

  function readFlag() {
    try {
      return window.sessionStorage.getItem(POLLING_KEY) === '1';
    } catch (e) {
      return false;
    }
  }

  function writeFlag() {
    try {
      window.sessionStorage.setItem(POLLING_KEY, '1');
    } catch (e) {
      // No storage: the next page finds out again.
    }
  }

  function sleep(ms) {
    return new Promise(function (resolve) {
      setTimeout(resolve, ms);
    });
  }

  function WindowStream(url, init) {
    url = String(url);
    if (url.indexOf(STREAM_PATH) < 0) {
      // Not the window's stream: the real thing.
      return new Native(url, init);
    }
    var match = /[?&]windowName=([^&]*)/.exec(url);
    this.url = url;
    this.readyState = WindowStream.CONNECTING;
    this.onopen = null;
    this.onmessage = null;
    this.onerror = null;
    this._listeners = {};
    this._windowName = match ? decodeURIComponent(match[1]) : '';
    this._pollUrl = url.substring(0, url.indexOf(STREAM_PATH)) + 'window-poll';
    this._native = null;
    this._timer = 0;
    this._closed = false;
    this._polling = false;
    this._probe = null;
    this._confirmed = 0;
    this._token = '';
    this._after = 0;
    active = this;
    if (readFlag()) {
      this._poll('this tab had to poll before');
    } else {
      this._stream();
    }
  }

  WindowStream.CONNECTING = 0;
  WindowStream.OPEN = 1;
  WindowStream.CLOSED = 2;

  var proto = WindowStream.prototype;
  proto.CONNECTING = 0;
  proto.OPEN = 1;
  proto.CLOSED = 2;

  proto.addEventListener = function (type, listener) {
    (this._listeners[type] = this._listeners[type] || []).push(listener);
  };

  proto.removeEventListener = function (type, listener) {
    var list = this._listeners[type] || [];
    var index = list.indexOf(listener);
    if (index >= 0) list.splice(index, 1);
  };

  proto.close = function () {
    this._closed = true;
    this.readyState = WindowStream.CLOSED;
    clearTimeout(this._timer);
    if (this._probe) clearTimeout(this._probe.timer);
    if (this._native) this._native.close();
    if (active === this) active = null;
  };

  proto._emit = function (type, event) {
    var handler = this['on' + type];
    if (typeof handler === 'function') handler.call(this, event);
    var list = (this._listeners[type] || []).slice();
    for (var i = 0; i < list.length; i++) list[i].call(this, event);
  };

  proto._stream = function () {
    var self = this;
    var source = new Native(this.url);
    this._native = source;
    this._timer = setTimeout(function () {
      self._poll('the event stream brought nothing');
    }, FIRST_MESSAGE_MS);
    source.onopen = function (event) {
      if (self._polling || self._closed) return;
      self.readyState = WindowStream.OPEN;
      self._emit('open', event);
    };
    source.onmessage = function (event) {
      if (self._polling || self._closed) return;
      clearTimeout(self._timer);
      self.readyState = WindowStream.OPEN;
      var probe = self._probe;
      if (probe && event.data.indexOf(PROBE_LISTENER) >= 0 && event.data.indexOf(probe.nonce) >= 0) {
        self._probeArrived();
      }
      self._emit('message', event);
    };
    source.onerror = function (event) {
      if (self._polling || self._closed) return;
      if (source.readyState === Native.CLOSED) {
        // Refused: a lost session, or a proxy that blocks event streams. The first poll tells.
        self._poll('the event stream was refused');
        return;
      }
      self.readyState = source.readyState;
      self._emit('error', event);
    };
  };

  /** The server is producing events now: check that the stream brings them. */
  proto._expect = function () {
    if (this._polling || this._closed || !this._windowName) return;
    if (this._probe) {
      this._probe.again = true;
      return;
    }
    if (Date.now() - this._confirmed < CONFIRMED_MS) return;
    var self = this;
    var nonce = Math.random().toString(36).slice(2, 12);
    var probe = { nonce: nonce, again: false, timer: 0 };
    this._probe = probe;
    nativeFetch(this._pollUrl + '?windowName=' + encodeURIComponent(this._windowName) + '&probe=' + nonce, {
      method: 'POST',
      credentials: 'same-origin',
    }).catch(function () {
      // Asking failed; the timer decides.
    });
    probe.timer = setTimeout(function () {
      if (self._probe === probe) self._poll('a probe did not arrive on the event stream');
    }, PROBE_MS);
  };

  proto._probeArrived = function () {
    var probe = this._probe;
    clearTimeout(probe.timer);
    this._probe = null;
    this._confirmed = Date.now();
    if (probe.again) this._expect();
  };

  proto._poll = function (reason) {
    if (this._polling || this._closed || leaving) return;
    this._polling = true;
    writeFlag();
    console.warn('[TLReact] polling: ' + reason + ' (tl-window-stream)');
    clearTimeout(this._timer);
    if (this._probe) {
      clearTimeout(this._probe.timer);
      this._probe = null;
    }
    if (this._native) {
      this._native.close();
      this._native = null;
    }
    this.readyState = WindowStream.OPEN;
    this._loop();
  };

  proto._loop = async function () {
    var start = true;
    var backoff = 0;
    while (!this._closed) {
      try {
        var url = start
          ? this._pollUrl + '?start=true&windowName=' + encodeURIComponent(this._windowName)
          : this._pollUrl + '?token=' + encodeURIComponent(this._token) + '&after=' + this._after;
        var response = await nativeFetch(url, {
          cache: 'no-store',
          // Later polls leave the cookie out, so that polling does not keep the session alive.
          credentials: start ? 'same-origin' : 'omit',
        });
        if (this._closed) return;
        if (response.status === 401 || response.status === 410) {
          if (start) {
            // The session or the window is gone: the bridge reloads the page, as for a closed stream.
            this.readyState = WindowStream.CLOSED;
            this._emit('error', new Event('error'));
            return;
          }
          start = true;
          continue;
        }
        if (!response.ok) throw new Error('poll failed (' + response.status + ')');
        if (start) {
          this._token = response.headers.get('X-TL-Window-Token') || '';
          this._after = 0;
        }
        var answer = await response.json();
        if (this._closed) return;
        start = false;
        backoff = 0;
        this._after = answer.last;
        for (var i = 0; i < answer.events.length && !this._closed; i++) {
          this._emit('message', new MessageEvent('message', { data: JSON.stringify(answer.events[i]) }));
        }
        if (answer.ended || answer.gap) {
          // Another connection replaced this one, or events were dropped unread: start anew, which
          // brings the full state.
          start = true;
        }
      } catch (e) {
        if (this._closed) return;
        backoff = Math.min(backoff ? backoff * 2 : 1000, MAX_BACKOFF_MS);
        console.warn('[TLReact] poll failed, trying again in ' + backoff + ' ms (tl-window-stream):', e);
        await sleep(backoff);
      }
    }
  };

  window.EventSource = WindowStream;

  // A React command's result arrives on the stream: check that it does.
  window.fetch = function (input, init) {
    var result = nativeFetch(input, init);
    var url = typeof input === 'string' ? input : (input && input.url) || '';
    if (url.indexOf('react-api/command') >= 0) {
      result.then(function () {
        if (active) active._expect();
      }, function () {
        // A failed command produces nothing to wait for.
      });
    }
    return result;
  };

  window.TLWindowStream = {
    /** Tells that the server is producing events for this window now, such as a chat's frames. */
    expect: function () {
      if (active) active._expect();
    },
  };
})();
