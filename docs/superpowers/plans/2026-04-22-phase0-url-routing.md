# Phase 0: URL-Routing Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** URL-Routing als Querschnitts-Feature bestehender Navigations-Controls, mit Param/Query-Bindings und bidirektionalen Kanaelen, verifiziert in der Demo-App.

**Architecture:** RoutingParticipant SPI in `tl-layout-react`, implementiert von Sidebar/TabBar. RouteManager im ReactContext koordiniert segment-weise URL-Aufloesung. Bidirektionale Kanaele (reverse-Attribut auf derived-channel) ermoeglichen TLObject-zu-ID-Uebersetzung. ViewServlet extrahiert Route aus URL-Pfad, Window-Name per replaceState versteckt.

**Tech Stack:** Java 17, TypeScript/React (tl-react-bridge), msgbuf (SSE protocol), TL-Script (channel expressions), Vite, Maven

**Spec:** `docs/superpowers/specs/2026-04-22-phase0-url-routing-design.md`

**Build-Konventionen:**
- Immer vom Projekt-Root bauen: `mvn install -pl <module-dir>`
- Tests: `mvn test -DskipTests=false -pl <module-dir> -Dtest=ClassName`
- Encoding: ISO-8859-1
- Member-Variablen: `_`-Prefix
- Packages: `com.top_logic.layout.react.routing` (neu), Test-Packages: `test.com.top_logic.layout.react.routing`

**Modul-Abhaengigkeiten:**
```
tl-layout-react (SPI, RouteManager, Controls, SSE, Bridge)
    ^
    |
tl-layout-view  (ParamBindingConfig, QueryBindingConfig, bidirektionaler Channel)
    ^
    |
tl-demo         (Demo-Views mit Routing)
```

---

## File Structure

### New files in `com.top_logic.layout.react`

```
src/main/java/com/top_logic/layout/react/routing/
  RoutePattern.java           -- Immutable: pattern string, param names, segment matching
  RouteMatch.java             -- Immutable: matched pattern + extracted param values
  RouteSegment.java           -- Immutable: concrete URL segment from active route
  RoutingParticipant.java     -- SPI interface
  RouteChangeListener.java    -- Functional interface
  RouteManager.java           -- Service in ReactContext

src/main/java/com/top_logic/layout/react/protocol/
  sse.proto                   -- Extended with RouteChangeEvent, RouteVetoEvent (regenerate)

react-src/bridge/
  route-sync.ts               -- Client-side pushState/popstate handler

src/test/java/test/com/top_logic/layout/react/routing/
  TestRoutePattern.java       -- Unit tests for pattern matching
  TestRouteManager.java       -- Unit tests for URL composition + resolution
```

### Modified files in `com.top_logic.layout.react`

```
src/main/java/com/top_logic/layout/react/
  ReactContext.java                      -- Add getRouteManager()
  DefaultReactContext.java               -- Hold RouteManager instance
  control/ReactControl.java             -- onAttach/onDetach: RoutingParticipant registration
  control/sidebar/NavigationItem.java   -- Add route, hidden properties
  control/sidebar/ReactSidebarControl.java -- Implement RoutingParticipant
  control/tabbar/ReactTabBarControl.java   -- Implement RoutingParticipant

react-src/bridge/
  sse-client.ts              -- Handle RouteChangeEvent, RouteVetoEvent
  bridge-entry.ts            -- Init routeSync on load
```

### Modified files in `com.top_logic.layout.view`

```
src/main/java/com/top_logic/layout/view/
  ViewElement.java           -- Add paramBindings, queryBindings properties
  ViewServlet.java           -- Extract route from URL path, pass to RouteManager

src/main/java/com/top_logic/layout/view/channel/
  DerivedChannelConfig.java  -- Add reverse property
  DerivedViewChannel.java    -- Support bidirectional propagation

src/test/java/test/com/top_logic/layout/view/channel/
  TestBidirectionalChannel.java  -- Tests for reverse propagation
```

### New/modified files in `com.top_logic.demo`

```
src/main/webapp/WEB-INF/views/
  app.view.xml                           -- Add route attributes to nav-items
  demo/routing-demo.view.xml             -- Basic routing demo
  demo/routing-param-demo.view.xml       -- Param-binding + bidirectional channel
  demo/routing-nested-demo.view.xml      -- Nested routes via TabBar
  demo/routing-query-demo.view.xml       -- Query-binding demo
```

---

## Task 1: Route Data Types + Pattern Matching

**Files:**
- Create: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/routing/RoutePattern.java`
- Create: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/routing/RouteMatch.java`
- Create: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/routing/RouteSegment.java`
- Create: `com.top_logic.layout.react/src/test/java/test/com/top_logic/layout/react/routing/TestRoutePattern.java`

- [ ] **Step 1: Write the failing test for RoutePattern**

```java
package test.com.top_logic.layout.react.routing;

import java.util.Map;
import junit.framework.TestCase;
import com.top_logic.layout.react.routing.RoutePattern;
import com.top_logic.layout.react.routing.RouteMatch;

public class TestRoutePattern extends TestCase {

    public void testStaticMatch() {
        RoutePattern pattern = RoutePattern.compile("/explore", "explore");
        RouteMatch match = pattern.match("explore");
        assertNotNull(match);
        assertEquals("explore", match.itemId());
        assertTrue(match.params().isEmpty());
    }

    public void testStaticNoMatch() {
        RoutePattern pattern = RoutePattern.compile("/explore", "explore");
        assertNull(pattern.match("listings"));
    }

    public void testParamMatch() {
        RoutePattern pattern = RoutePattern.compile("/property/:estateId", "property-detail");
        RouteMatch match = pattern.match("property/42");
        assertNotNull(match);
        assertEquals("property-detail", match.itemId());
        assertEquals("42", match.params().get("estateId"));
    }

    public void testParamNoMatch() {
        RoutePattern pattern = RoutePattern.compile("/property/:estateId", "property-detail");
        assertNull(pattern.match("listings"));
    }

    public void testWildcardMatch() {
        RoutePattern pattern = RoutePattern.compile("/*", "not-found");
        RouteMatch match = pattern.match("anything/here");
        assertNotNull(match);
        assertEquals("not-found", match.itemId());
    }

    public void testConsumedSegments() {
        RoutePattern pattern = RoutePattern.compile("/property/:id", "detail");
        RouteMatch match = pattern.match("property/42/overview");
        assertNotNull(match);
        assertEquals("42", match.params().get("id"));
        assertEquals("overview", match.remainingPath());
    }

    public void testSegmentProduction() {
        RoutePattern pattern = RoutePattern.compile("/property/:id", "detail");
        String segment = pattern.produce(Map.of("id", "42"));
        assertEquals("property/42", segment);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -DskipTests=false -pl com.top_logic.layout.react -Dtest=TestRoutePattern`
Expected: Compilation error (classes don't exist yet)

- [ ] **Step 3: Implement RoutePattern, RouteMatch, RouteSegment**

`RoutePattern.java`:
```java
package com.top_logic.layout.react.routing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Immutable route pattern that matches URL path segments.
 * Patterns are always relative (no leading slash stored internally).
 * Example: "/property/:estateId" matches "property/42".
 */
public class RoutePattern {

    private final String _patternString;
    private final String _itemId;
    private final List<String> _segments;
    private final List<String> _paramNames;
    private final boolean _wildcard;

    private RoutePattern(String patternString, String itemId,
            List<String> segments, List<String> paramNames, boolean wildcard) {
        _patternString = patternString;
        _itemId = itemId;
        _segments = segments;
        _paramNames = paramNames;
        _wildcard = wildcard;
    }

    /**
     * Compile a pattern string into a RoutePattern.
     *
     * @param pattern Pattern with optional :param placeholders, e.g. "/property/:id"
     * @param itemId  The navigation item ID this pattern maps to
     */
    public static RoutePattern compile(String pattern, String itemId) {
        String normalized = pattern.startsWith("/") ? pattern.substring(1) : pattern;
        if (normalized.equals("*")) {
            return new RoutePattern(pattern, itemId, List.of(), List.of(), true);
        }
        String[] parts = normalized.split("/");
        List<String> segments = new ArrayList<>();
        List<String> paramNames = new ArrayList<>();
        for (String part : parts) {
            if (part.startsWith(":")) {
                segments.add(null); // null = param placeholder
                paramNames.add(part.substring(1));
            } else {
                segments.add(part);
            }
        }
        return new RoutePattern(pattern, itemId, segments, paramNames, false);
    }

    /**
     * Try to match the given path against this pattern.
     *
     * @param path Path without leading slash, e.g. "property/42/overview"
     * @return Match with extracted params and remaining path, or null
     */
    public RouteMatch match(String path) {
        if (_wildcard) {
            return new RouteMatch(this, _itemId, Map.of(), "");
        }
        String[] pathParts = path.split("/", -1);
        if (pathParts.length < _segments.size()) {
            return null;
        }
        Map<String, String> params = new HashMap<>();
        int paramIdx = 0;
        for (int i = 0; i < _segments.size(); i++) {
            String expected = _segments.get(i);
            if (expected == null) {
                params.put(_paramNames.get(paramIdx++), pathParts[i]);
            } else if (!expected.equals(pathParts[i])) {
                return null;
            }
        }
        String remaining = "";
        if (pathParts.length > _segments.size()) {
            StringBuilder sb = new StringBuilder();
            for (int i = _segments.size(); i < pathParts.length; i++) {
                if (sb.length() > 0) sb.append('/');
                sb.append(pathParts[i]);
            }
            remaining = sb.toString();
        }
        return new RouteMatch(this, _itemId, Collections.unmodifiableMap(params), remaining);
    }

    /**
     * Produce a concrete path segment from this pattern with given param values.
     */
    public String produce(Map<String, String> params) {
        if (_wildcard) return "";
        StringBuilder sb = new StringBuilder();
        int paramIdx = 0;
        for (int i = 0; i < _segments.size(); i++) {
            if (i > 0) sb.append('/');
            String seg = _segments.get(i);
            if (seg == null) {
                sb.append(params.getOrDefault(_paramNames.get(paramIdx++), ""));
            } else {
                sb.append(seg);
            }
        }
        return sb.toString();
    }

    /** The original pattern string. */
    public String patternString() { return _patternString; }

    /** The navigation item ID. */
    public String itemId() { return _itemId; }

    /** Parameter names declared in the pattern. */
    public List<String> paramNames() { return _paramNames; }

    /** Whether this is a wildcard pattern. */
    public boolean isWildcard() { return _wildcard; }
}
```

`RouteMatch.java`:
```java
package com.top_logic.layout.react.routing;

import java.util.Map;

/**
 * Result of matching a URL path against a {@link RoutePattern}.
 */
public class RouteMatch {

    private final RoutePattern _pattern;
    private final String _itemId;
    private final Map<String, String> _params;
    private final String _remainingPath;

    public RouteMatch(RoutePattern pattern, String itemId,
            Map<String, String> params, String remainingPath) {
        _pattern = pattern;
        _itemId = itemId;
        _params = params;
        _remainingPath = remainingPath;
    }

    public RoutePattern pattern() { return _pattern; }
    public String itemId() { return _itemId; }
    public Map<String, String> params() { return _params; }
    public String remainingPath() { return _remainingPath; }
}
```

`RouteSegment.java`:
```java
package com.top_logic.layout.react.routing;

import java.util.Map;

/**
 * A concrete URL segment produced by an active route.
 */
public class RouteSegment {

    private final String _path;
    private final Map<String, String> _queryParams;

    public RouteSegment(String path, Map<String, String> queryParams) {
        _path = path;
        _queryParams = queryParams;
    }

    public RouteSegment(String path) {
        this(path, Map.of());
    }

    public String path() { return _path; }
    public Map<String, String> queryParams() { return _queryParams; }
}
```

- [ ] **Step 4: Run tests**

Run: `mvn test -DskipTests=false -pl com.top_logic.layout.react -Dtest=TestRoutePattern`
Expected: All 7 tests PASS

- [ ] **Step 5: Commit**

```bash
git add com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/routing/
git add com.top_logic.layout.react/src/test/java/test/com/top_logic/layout/react/routing/
git commit -m "Ticket #29108: Add RoutePattern, RouteMatch, RouteSegment with pattern matching."
```

---

## Task 2: RoutingParticipant SPI

**Files:**
- Create: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/routing/RoutingParticipant.java`
- Create: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/routing/RouteChangeListener.java`

- [ ] **Step 1: Create RoutingParticipant interface**

```java
package com.top_logic.layout.react.routing;

import java.util.List;

/**
 * Controls that contribute URL segments implement this interface.
 *
 * <p>Registration/deregistration with the {@link RouteManager} happens
 * automatically via the {@link com.top_logic.layout.react.control.ReactControl}
 * attach/detach lifecycle.</p>
 *
 * <p>Implementations: {@link com.top_logic.layout.react.control.sidebar.ReactSidebarControl},
 * {@link com.top_logic.layout.react.control.tabbar.ReactTabBarControl}.</p>
 */
public interface RoutingParticipant {

    /**
     * Route patterns for this participant's direct children.
     * Only returns patterns for children that have a {@code route} attribute declared.
     */
    List<RoutePattern> declaredRoutes();

    /**
     * Activate the matching child route. May trigger lazy materialization.
     * Called by {@link RouteManager} during deep-link resolution or back-navigation.
     *
     * @param match the matched route pattern with extracted parameters
     */
    void activateRoute(RouteMatch match);

    /**
     * Currently active route segment, or {@code null} if no route-forming
     * item is active.
     */
    RouteSegment activeRouteSegment();

    /**
     * Subscribe to route-forming navigation events.
     */
    void addRouteChangeListener(RouteChangeListener listener);

    /**
     * Unsubscribe from route-forming navigation events.
     */
    void removeRouteChangeListener(RouteChangeListener listener);
}
```

- [ ] **Step 2: Create RouteChangeListener**

```java
package com.top_logic.layout.react.routing;

/**
 * Listener for route-forming navigation events within a {@link RoutingParticipant}.
 */
@FunctionalInterface
public interface RouteChangeListener {

    /**
     * Called when a route-forming navigation happened.
     *
     * @param participant the participant that changed
     * @param newSegment the new active route segment
     */
    void onRouteChange(RoutingParticipant participant, RouteSegment newSegment);
}
```

- [ ] **Step 3: Build**

Run: `mvn install -pl com.top_logic.layout.react`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/routing/
git commit -m "Ticket #29108: Add RoutingParticipant SPI and RouteChangeListener."
```

---

## Task 3: Bidirectional Derived Channel

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/channel/DerivedChannelConfig.java`
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/channel/DerivedViewChannel.java`
- Create: `com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/channel/TestBidirectionalChannel.java`

- [ ] **Step 1: Write the failing test**

```java
package test.com.top_logic.layout.view.channel;

import junit.framework.TestCase;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.DerivedViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import java.util.List;
import java.util.function.Function;

public class TestBidirectionalChannel extends TestCase {

    public void testForwardPropagation() {
        DefaultViewChannel source = new DefaultViewChannel("source");
        DerivedViewChannel derived = new DerivedViewChannel("derived");
        derived.bind(
            List.of(source),
            args -> args[0] != null ? "id-" + args[0] : null,
            value -> value != null ? ((String) value).substring(3) : null
        );

        source.set("42");
        assertEquals("id-42", derived.get());
    }

    public void testReversePropagation() {
        DefaultViewChannel source = new DefaultViewChannel("source");
        DerivedViewChannel derived = new DerivedViewChannel("derived");
        derived.bind(
            List.of(source),
            args -> args[0] != null ? "id-" + args[0] : null,
            value -> value != null ? ((String) value).substring(3) : null
        );

        derived.set("id-99");
        assertEquals("99", source.get());
        assertEquals("id-99", derived.get());
    }

    public void testReverseWithoutReverseFunction() {
        DefaultViewChannel source = new DefaultViewChannel("source");
        DerivedViewChannel derived = new DerivedViewChannel("derived");
        derived.bind(List.of(source), args -> args[0] != null ? "id-" + args[0] : null);

        // Without reverse function, set on derived should throw or be ignored
        try {
            derived.set("id-99");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {
            // OK: unidirectional channel rejects writes
        }
    }

    public void testListenerOnReverse() {
        DefaultViewChannel source = new DefaultViewChannel("source");
        DerivedViewChannel derived = new DerivedViewChannel("derived");
        derived.bind(
            List.of(source),
            args -> args[0] != null ? "id-" + args[0] : null,
            value -> value != null ? ((String) value).substring(3) : null
        );

        boolean[] sourceChanged = {false};
        source.addListener(ch -> sourceChanged[0] = true);

        derived.set("id-77");
        assertTrue("Source listener must fire on reverse propagation", sourceChanged[0]);
        assertEquals("77", source.get());
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -DskipTests=false -pl com.top_logic.layout.view -Dtest=TestBidirectionalChannel`
Expected: Compilation error (new `bind` overload and `set` method don't exist)

- [ ] **Step 3: Extend DerivedViewChannel with reverse support**

Read `DerivedViewChannel.java` first, then add:
- A new `bind(List<ViewChannel> inputs, Function forward, Function reverse)` overload
- A `_reverseFunction` field
- Override `set(Object value)` to propagate via reverse function to the first input

The exact changes depend on the current `DerivedViewChannel` implementation. Key additions:

```java
// New field
private Function<Object, Object> _reverseFunction;

// New bind overload
public void bind(List<ViewChannel> inputs,
        Function<Object[], Object> forward,
        Function<Object, Object> reverse) {
    bind(inputs, forward);
    _reverseFunction = reverse;
}

// Override set to support bidirectional writes
@Override
public void set(Object value) {
    if (_reverseFunction == null) {
        throw new UnsupportedOperationException(
            "Cannot write to unidirectional derived channel: " + getName());
    }
    Object sourceValue = _reverseFunction.apply(value);
    _inputs.get(0).set(sourceValue);
    // Forward propagation from source will update this channel
}
```

- [ ] **Step 4: Extend DerivedChannelConfig with reverse property**

In `DerivedChannelConfig.java`, add:

```java
/**
 * Optional reverse expression. If set, the channel becomes bidirectional.
 * The expression receives the derived value and must return the source value.
 * Only the first input channel is written to.
 */
@Name("reverse")
Expr getReverse();
```

- [ ] **Step 5: Update the factory that creates DerivedViewChannel from config**

Find where `DerivedChannelConfig` is instantiated into a `DerivedViewChannel`
(likely in `ViewElement.createContext()` or a `DerivedChannelFactory`).
Add handling for the `reverse` expression:

```java
if (config.getReverse() != null) {
    Function<Object, Object> reverseFunction = compileReverse(config.getReverse());
    channel.bind(inputChannels, forwardFunction, reverseFunction);
} else {
    channel.bind(inputChannels, forwardFunction);
}
```

- [ ] **Step 6: Run tests**

Run: `mvn test -DskipTests=false -pl com.top_logic.layout.view -Dtest=TestBidirectionalChannel`
Expected: All 4 tests PASS

Also run existing derived channel tests to verify no regressions:
Run: `mvn test -DskipTests=false -pl com.top_logic.layout.view -Dtest=TestDerivedViewChannel`
Expected: All existing tests PASS

- [ ] **Step 7: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/channel/
git add com.top_logic.layout.view/src/test/java/test/com/top_logic/layout/view/channel/
git commit -m "Ticket #29108: Add bidirectional derived channels (reverse attribute)."
```

---

## Task 4: ParamBindingConfig + QueryBindingConfig on ViewElement

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/routing/ParamBindingConfig.java`
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/routing/QueryBindingConfig.java`
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewElement.java`

- [ ] **Step 1: Create ParamBindingConfig**

```java
package com.top_logic.layout.view.routing;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;

/**
 * Binds a view channel to a URL route parameter.
 *
 * <p>Example: {@code <bind channel="estateId" route-param="estateId"/>}</p>
 */
@TagName("bind")
public interface ParamBindingConfig extends ConfigurationItem {

    String CHANNEL = "channel";
    String ROUTE_PARAM = "route-param";

    @Name(CHANNEL)
    @Mandatory
    String getChannel();

    @Name(ROUTE_PARAM)
    @Mandatory
    String getRouteParam();
}
```

- [ ] **Step 2: Create QueryBindingConfig**

```java
package com.top_logic.layout.view.routing;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;

/**
 * Binds a view channel to a URL query parameter.
 *
 * <p>Example: {@code <bind channel="typeFilter" query-param="type"/>}</p>
 */
@TagName("bind")
public interface QueryBindingConfig extends ConfigurationItem {

    String CHANNEL = "channel";
    String QUERY_PARAM = "query-param";

    @Name(CHANNEL)
    @Mandatory
    String getChannel();

    @Name(QUERY_PARAM)
    @Mandatory
    String getQueryParam();
}
```

- [ ] **Step 3: Extend ViewElement.Config**

Add to `ViewElement.java` Config interface:

```java
String PARAM_BINDINGS = "param-bindings";
String QUERY_BINDINGS = "query-bindings";

@Name(PARAM_BINDINGS)
List<ParamBindingConfig> getParamBindings();

@Name(QUERY_BINDINGS)
List<QueryBindingConfig> getQueryBindings();
```

- [ ] **Step 4: Build**

Run: `mvn install -pl com.top_logic.layout.view`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/routing/
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewElement.java
git commit -m "Ticket #29108: Add ParamBindingConfig and QueryBindingConfig on ViewElement."
```

---

## Task 5: RouteManager

**Files:**
- Create: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/routing/RouteManager.java`
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/ReactContext.java`
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/DefaultReactContext.java`
- Create: `com.top_logic.layout.react/src/test/java/test/com/top_logic/layout/react/routing/TestRouteManager.java`

- [ ] **Step 1: Write failing test for RouteManager**

```java
package test.com.top_logic.layout.react.routing;

import java.util.List;
import java.util.Map;
import junit.framework.TestCase;
import com.top_logic.layout.react.routing.*;

public class TestRouteManager extends TestCase {

    /** Mock RoutingParticipant for testing. */
    static class MockParticipant implements RoutingParticipant {
        private final List<RoutePattern> _routes;
        private RouteSegment _activeSegment;
        private RouteMatch _lastActivation;
        private RouteChangeListener _listener;

        MockParticipant(List<RoutePattern> routes) {
            _routes = routes;
        }

        @Override
        public List<RoutePattern> declaredRoutes() { return _routes; }

        @Override
        public void activateRoute(RouteMatch match) {
            _lastActivation = match;
            _activeSegment = new RouteSegment(
                match.pattern().produce(match.params()));
        }

        @Override
        public RouteSegment activeRouteSegment() { return _activeSegment; }

        @Override
        public void addRouteChangeListener(RouteChangeListener l) { _listener = l; }

        @Override
        public void removeRouteChangeListener(RouteChangeListener l) {
            if (_listener == l) _listener = null;
        }

        void simulateNavigation(String itemId, Map<String, String> params) {
            RoutePattern pattern = _routes.stream()
                .filter(r -> r.itemId().equals(itemId)).findFirst().orElseThrow();
            _activeSegment = new RouteSegment(pattern.produce(params));
            if (_listener != null) _listener.onRouteChange(this, _activeSegment);
        }

        RouteMatch lastActivation() { return _lastActivation; }
    }

    public void testUrlComposition() {
        RouteManager rm = new RouteManager();
        MockParticipant sidebar = new MockParticipant(List.of(
            RoutePattern.compile("/explore", "explore"),
            RoutePattern.compile("/listings", "listings")
        ));
        rm.register(sidebar);
        sidebar.simulateNavigation("explore", Map.of());
        assertEquals("explore", rm.currentUrl());
    }

    public void testPendingUrlResolution() {
        RouteManager rm = new RouteManager();
        rm.setPendingUrl("explore");

        MockParticipant sidebar = new MockParticipant(List.of(
            RoutePattern.compile("/explore", "explore"),
            RoutePattern.compile("/listings", "listings")
        ));
        rm.register(sidebar);

        assertNotNull(sidebar.lastActivation());
        assertEquals("explore", sidebar.lastActivation().itemId());
    }

    public void testParamResolution() {
        RouteManager rm = new RouteManager();
        rm.setPendingUrl("property/42");

        MockParticipant sidebar = new MockParticipant(List.of(
            RoutePattern.compile("/property/:id", "detail")
        ));
        rm.register(sidebar);

        assertNotNull(sidebar.lastActivation());
        assertEquals("42", sidebar.lastActivation().params().get("id"));
    }

    public void testNestedComposition() {
        RouteManager rm = new RouteManager();
        MockParticipant sidebar = new MockParticipant(List.of(
            RoutePattern.compile("/explore", "explore")
        ));
        MockParticipant tabs = new MockParticipant(List.of(
            RoutePattern.compile("/featured", "featured"),
            RoutePattern.compile("/nearby", "nearby")
        ));
        rm.register(sidebar);
        sidebar.simulateNavigation("explore", Map.of());
        rm.register(tabs);
        tabs.simulateNavigation("featured", Map.of());

        assertEquals("explore/featured", rm.currentUrl());
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -DskipTests=false -pl com.top_logic.layout.react -Dtest=TestRouteManager`
Expected: Compilation error (RouteManager doesn't exist)

- [ ] **Step 3: Implement RouteManager**

```java
package com.top_logic.layout.react.routing;

import java.util.ArrayList;
import java.util.List;

/**
 * Central routing service, held per-session in {@link com.top_logic.layout.react.ReactContext}.
 *
 * <p>Coordinates segment-wise URL resolution (deep-links, back-navigation) and
 * URL composition (forward navigation). Discovers {@link RoutingParticipant}s
 * reactively via the control attach/detach lifecycle.</p>
 */
public class RouteManager {

    private final List<RoutingParticipant> _participants = new ArrayList<>();
    private String _pendingUrl;
    private RouteUrlChangeHandler _urlChangeHandler;

    /**
     * Functional interface for URL change notifications (sent to client via SSE).
     */
    @FunctionalInterface
    public interface RouteUrlChangeHandler {
        void onUrlChange(String url, boolean replace);
    }

    /**
     * Set the handler that delivers URL changes to the client.
     */
    public void setUrlChangeHandler(RouteUrlChangeHandler handler) {
        _urlChangeHandler = handler;
    }

    /**
     * Set a pending URL for resolution. Called by ViewServlet when the
     * initial HTTP request contains a route path.
     */
    public void setPendingUrl(String url) {
        _pendingUrl = url;
    }

    /**
     * Register a new RoutingParticipant. Called from ReactControl.onAttach().
     * If a pending URL has unresolved segments, attempts resolution immediately.
     */
    public void register(RoutingParticipant participant) {
        _participants.add(participant);
        participant.addRouteChangeListener(this::onParticipantRouteChange);

        if (_pendingUrl != null && !_pendingUrl.isEmpty()) {
            tryResolvePending(participant);
        }
    }

    /**
     * Unregister a RoutingParticipant. Called from ReactControl.onDetach().
     */
    public void unregister(RoutingParticipant participant) {
        participant.removeRouteChangeListener(this::onParticipantRouteChange);
        _participants.remove(participant);
    }

    /**
     * Navigate to a URL. Called when the client sends a popstate-triggered
     * navigateToRoute command.
     */
    public void navigateToRoute(String url) {
        _pendingUrl = url;
        for (RoutingParticipant participant : new ArrayList<>(_participants)) {
            if (_pendingUrl != null && !_pendingUrl.isEmpty()) {
                tryResolvePending(participant);
            }
        }
    }

    /**
     * Current full URL assembled from all active route segments (top-down).
     */
    public String currentUrl() {
        StringBuilder sb = new StringBuilder();
        for (RoutingParticipant p : _participants) {
            RouteSegment seg = p.activeRouteSegment();
            if (seg != null && !seg.path().isEmpty()) {
                if (sb.length() > 0) sb.append('/');
                sb.append(seg.path());
            }
        }
        return sb.toString();
    }

    private void tryResolvePending(RoutingParticipant participant) {
        for (RoutePattern pattern : participant.declaredRoutes()) {
            RouteMatch match = pattern.match(_pendingUrl);
            if (match != null) {
                _pendingUrl = match.remainingPath();
                participant.activateRoute(match);
                return;
            }
        }
    }

    private void onParticipantRouteChange(RoutingParticipant participant,
            RouteSegment newSegment) {
        String url = currentUrl();
        if (_urlChangeHandler != null) {
            _urlChangeHandler.onUrlChange(url, false);
        }
    }
}
```

- [ ] **Step 4: Add RouteManager to ReactContext**

In `ReactContext.java`, add:
```java
/** The route manager for this session. */
RouteManager getRouteManager();
```

In `DefaultReactContext.java`, add field + constructor parameter + getter:
```java
private final RouteManager _routeManager;

// In constructor, add:
_routeManager = new RouteManager();

@Override
public RouteManager getRouteManager() {
    return _routeManager;
}
```

- [ ] **Step 5: Run tests**

Run: `mvn test -DskipTests=false -pl com.top_logic.layout.react -Dtest=TestRouteManager`
Expected: All 4 tests PASS

- [ ] **Step 6: Commit**

```bash
git add com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/routing/RouteManager.java
git add com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/ReactContext.java
git add com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/DefaultReactContext.java
git add com.top_logic.layout.react/src/test/java/test/com/top_logic/layout/react/routing/
git commit -m "Ticket #29108: Add RouteManager service with URL composition and pending resolution."
```

---

## Task 6: SSE Protocol Extension

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/protocol/sse.proto`
- Regenerate: msgbuf-generated Java classes

- [ ] **Step 1: Add RouteChangeEvent and RouteVetoEvent to sse.proto**

Read the existing `sse.proto` file first. Add new message types following the existing pattern:

```protobuf
message RouteChangeEvent extends SSEEvent {
    /** The new URL to display in the browser address bar. */
    string url;

    /** If true, use replaceState instead of pushState. */
    boolean replace;
}

message RouteVetoEvent extends SSEEvent {
    /** The current (correct) URL to restore after a vetoed navigation. */
    string currentUrl;
}
```

Add `ROUTE_CHANGE_EVENT` and `ROUTE_VETO_EVENT` to the `TypeKind` enum in `SSEEvent`.

- [ ] **Step 2: Regenerate Java classes from proto**

Run: `mvn de.haumacher.msgbuf:msgbuf-generator-maven-plugin:1.1.11:generate -pl com.top_logic.layout.react`

Verify generated files: `RouteChangeEvent.java`, `RouteVetoEvent.java` exist in
`com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/protocol/`.

- [ ] **Step 3: Build to verify**

Run: `mvn install -pl com.top_logic.layout.react`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/protocol/
git commit -m "Ticket #29108: Add RouteChangeEvent and RouteVetoEvent to SSE protocol."
```

---

## Task 7: NavigationItem Route + Hidden Attributes

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/sidebar/NavigationItem.java`

- [ ] **Step 1: Read NavigationItem.java**

Read the full file to understand existing constructor, fields, and `toStateMap()`.

- [ ] **Step 2: Add route and hidden fields**

Add to `NavigationItem.java`:

```java
private final String _route;     // null = not route-forming
private final boolean _hidden;   // true = not shown in sidebar UI

// Extend constructor (or add builder/fluent methods)
public NavigationItem withRoute(String route) {
    // Return new instance or set field, depending on immutability pattern
}

public NavigationItem withHidden(boolean hidden) {
    // Return new instance or set field
}

public String getRoute() { return _route; }
public boolean isHidden() { return _hidden; }
```

In `toStateMap()`, add hidden flag so the React component can hide it:
```java
if (_hidden) {
    map.put("hidden", Boolean.TRUE);
}
```

- [ ] **Step 3: Build**

Run: `mvn install -pl com.top_logic.layout.react`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/sidebar/NavigationItem.java
git commit -m "Ticket #29108: Add route and hidden attributes to NavigationItem."
```

---

## Task 8: ReactSidebarControl Implements RoutingParticipant

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/sidebar/ReactSidebarControl.java`

- [ ] **Step 1: Read ReactSidebarControl.java fully**

Understand `_items`, `_activeItemId`, `selectItem()`, `handleSelectItem()`.

- [ ] **Step 2: Implement RoutingParticipant**

Add `implements RoutingParticipant` to the class declaration. Add:

```java
private final List<RouteChangeListener> _routeChangeListeners = new ArrayList<>();

@Override
public List<RoutePattern> declaredRoutes() {
    List<RoutePattern> routes = new ArrayList<>();
    for (NavigationItem item : _items) {
        String route = item.getRoute();
        if (route != null) {
            routes.add(RoutePattern.compile(route, item.getId()));
        }
    }
    return routes;
}

@Override
public void activateRoute(RouteMatch match) {
    selectItem(match.itemId());
}

@Override
public RouteSegment activeRouteSegment() {
    NavigationItem activeItem = findNavItem(_activeItemId, _items);
    if (activeItem != null && activeItem.getRoute() != null) {
        // For parameterized routes, we need the current param values
        // For now, return the concrete segment from the active item
        return new RouteSegment(_activeItemId);
    }
    return null;
}

@Override
public void addRouteChangeListener(RouteChangeListener listener) {
    _routeChangeListeners.add(listener);
}

@Override
public void removeRouteChangeListener(RouteChangeListener listener) {
    _routeChangeListeners.remove(listener);
}
```

- [ ] **Step 3: Fire route change in selectItem()**

In the existing `selectItem(String itemId)` method, after the successful selection,
add notification to route change listeners:

```java
// After the existing state update logic:
NavigationItem newItem = findNavItem(itemId, _items);
if (newItem != null && newItem.getRoute() != null) {
    RouteSegment segment = new RouteSegment(
        RoutePattern.compile(newItem.getRoute(), itemId)
            .produce(Map.of())); // params filled by caller if needed
    for (RouteChangeListener listener : _routeChangeListeners) {
        listener.onRouteChange(this, segment);
    }
}
```

- [ ] **Step 4: Update TLSidebar.tsx to handle hidden items**

In `react-src/controls/TLSidebar.tsx`, filter out items with `hidden: true`
from the rendered list:

```typescript
// In the render function, filter items:
const visibleItems = state.items.filter((item: any) => !item.hidden);
// Render only visibleItems
```

- [ ] **Step 5: Build and verify**

Run: `mvn install -pl com.top_logic.layout.react`
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
git add com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/sidebar/
git add com.top_logic.layout.react/react-src/controls/TLSidebar.tsx
git commit -m "Ticket #29108: ReactSidebarControl implements RoutingParticipant."
```

---

## Task 9: ReactControl Lifecycle Integration + TabBar

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/ReactControl.java`
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/tabbar/ReactTabBarControl.java`

- [ ] **Step 1: Add RoutingParticipant registration to ReactControl lifecycle**

In `ReactControl.java`, modify `onAttach()` and `onDetach()`:

```java
@Override
protected void onAttach() {
    super.onAttach();
    if (this instanceof RoutingParticipant rp) {
        ReactContext ctx = getReactContext();
        if (ctx != null) {
            ctx.getRouteManager().register(rp);
        }
    }
}

@Override
protected void onDetach() {
    if (this instanceof RoutingParticipant rp) {
        ReactContext ctx = getReactContext();
        if (ctx != null) {
            ctx.getRouteManager().unregister(rp);
        }
    }
    super.onDetach();
}
```

- [ ] **Step 2: Implement RoutingParticipant on ReactTabBarControl**

Read `ReactTabBarControl.java` first. Then add `implements RoutingParticipant`
following the same pattern as Task 8 (Sidebar), but using tab IDs instead of
nav-item IDs. The `TabDefinition` class needs a `route` property (analogous to
`NavigationItem.route`).

- [ ] **Step 3: Build and verify**

Run: `mvn install -pl com.top_logic.layout.react`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/ReactControl.java
git add com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/tabbar/
git commit -m "Ticket #29108: ReactControl lifecycle auto-registers RoutingParticipants. TabBar implements RoutingParticipant."
```

---

## Task 10: Client-Side routeSync Module

**Files:**
- Create: `com.top_logic.layout.react/react-src/bridge/route-sync.ts`
- Modify: `com.top_logic.layout.react/react-src/bridge/sse-client.ts`
- Modify: `com.top_logic.layout.react/react-src/bridge/bridge-entry.ts`

- [ ] **Step 1: Create route-sync.ts**

```typescript
import { sendCommand } from './sse-client';

/**
 * Client-side route synchronization.
 * Handles pushState/replaceState from server events and
 * popstate from browser back/forward buttons.
 */

export interface RouteChangeEvent {
    url: string;
    replace: boolean;
}

export interface RouteVetoEvent {
    currentUrl: string;
}

let initialized = false;

export function initRouteSync(
    onRouteChange: (event: RouteChangeEvent) => void,
    onRouteVeto: (event: RouteVetoEvent) => void
): void {
    if (initialized) return;
    initialized = true;

    // Browser Back/Forward -> Server
    window.addEventListener('popstate', () => {
        const basePath = getViewBasePath();
        const routePath = window.location.pathname.substring(basePath.length);
        const query = window.location.search;
        sendCommand('navigateToRoute', {
            url: routePath + query
        });
    });
}

export function handleRouteChangeEvent(event: RouteChangeEvent): void {
    const basePath = getViewBasePath();
    const fullUrl = basePath + event.url;
    if (event.replace) {
        history.replaceState(null, '', fullUrl);
    } else {
        history.pushState(null, '', fullUrl);
    }
}

export function handleRouteVetoEvent(event: RouteVetoEvent): void {
    const basePath = getViewBasePath();
    history.replaceState(null, '', basePath + event.currentUrl);
}

function getViewBasePath(): string {
    // Extract the base path up to and including /view/
    const path = window.location.pathname;
    const viewIdx = path.indexOf('/view/');
    if (viewIdx >= 0) {
        // Check if next segment is window name (starts with 'v')
        const afterView = path.substring(viewIdx + 6);
        const firstSlash = afterView.indexOf('/');
        if (firstSlash > 0) {
            const segment = afterView.substring(0, firstSlash);
            if (segment.startsWith('v')) {
                // Window name segment — include it in base path
                return path.substring(0, viewIdx + 6 + firstSlash + 1);
            }
        }
        return path.substring(0, viewIdx + 6);
    }
    return '/';
}
```

- [ ] **Step 2: Register SSE event handlers in sse-client.ts**

In `sse-client.ts`, add handling for the new event types in the dispatch function.
Find the existing switch/map on event type codes and add:

```typescript
case 'RouteChangeEvent':
    handleRouteChangeEvent(payload as RouteChangeEvent);
    break;
case 'RouteVetoEvent':
    handleRouteVetoEvent(payload as RouteVetoEvent);
    break;
```

Import from route-sync.ts.

- [ ] **Step 3: Initialize routeSync in bridge-entry.ts**

Add after the existing initialization code:

```typescript
import { initRouteSync, handleRouteChangeEvent, handleRouteVetoEvent } from './bridge/route-sync';

// In the initialization function:
initRouteSync(handleRouteChangeEvent, handleRouteVetoEvent);
```

- [ ] **Step 4: Hide window name via replaceState after initial render**

In `bridge-entry.ts` or `tl-react-bridge.ts`, after the first mount:

```typescript
// After initial mount, hide window name from URL
const path = window.location.pathname;
const viewIdx = path.indexOf('/view/');
if (viewIdx >= 0) {
    const afterView = path.substring(viewIdx + 6);
    const firstSlash = afterView.indexOf('/');
    if (firstSlash > 0 && afterView.substring(0, firstSlash).startsWith('v')) {
        // Remove window name segment from visible URL
        const cleanPath = path.substring(0, viewIdx + 6) + afterView.substring(firstSlash + 1);
        history.replaceState(null, '', cleanPath + window.location.search);
    }
}
```

- [ ] **Step 5: Build**

Run: `mvn install -pl com.top_logic.layout.react`
Expected: BUILD SUCCESS (Vite builds the TypeScript)

- [ ] **Step 6: Commit**

```bash
git add com.top_logic.layout.react/react-src/bridge/
git commit -m "Ticket #29108: Add client-side routeSync module for pushState/popstate handling."
```

---

## Task 11: ViewServlet Route Extraction

**Files:**
- Modify: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewServlet.java`

- [ ] **Step 1: Read ViewServlet.java fully**

Understand the URL parsing logic, window name extraction, bootstrap page generation.

- [ ] **Step 2: Extract route from URL path**

After the existing window name extraction, add route extraction:

```java
// Existing: windowName is extracted from first path segment
// New: everything after the window name segment is the route
String routePath = extractRoutePath(pathInfo, windowName);

// Pass to RouteManager before building control tree
if (routePath != null && !routePath.isEmpty()) {
    reactContext.getRouteManager().setPendingUrl(routePath);
}
```

Add helper method:
```java
private String extractRoutePath(String pathInfo, String windowName) {
    // pathInfo is like "/v1a2b3c/property/42"
    // windowName is "v1a2b3c"
    // route is "property/42"
    if (pathInfo == null) return null;
    String normalized = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
    if (normalized.startsWith(windowName)) {
        String afterWindow = normalized.substring(windowName.length());
        if (afterWindow.startsWith("/")) afterWindow = afterWindow.substring(1);
        return afterWindow;
    }
    return normalized;
}
```

- [ ] **Step 3: Wire RouteManager URL change handler to SSE**

After creating the RouteManager (in DefaultReactContext), connect its URL
change handler to the SSE queue:

```java
routeManager.setUrlChangeHandler((url, replace) -> {
    RouteChangeEvent event = RouteChangeEvent.create()
        .setUrl(url)
        .setReplace(replace);
    sseQueue.send(event);
});
```

- [ ] **Step 4: Add navigateToRoute command handler**

Register a global command handler (or a command on a top-level control) that
receives `navigateToRoute` from the client and delegates to `RouteManager`:

```java
// In ReactServlet or a dedicated command handler:
if ("navigateToRoute".equals(command)) {
    String url = (String) arguments.get("url");
    try {
        routeManager.navigateToRoute(url);
    } catch (ChannelVetoException e) {
        // Send RouteVetoEvent
        RouteVetoEvent veto = RouteVetoEvent.create()
            .setCurrentUrl(routeManager.currentUrl());
        sseQueue.send(veto);
    }
}
```

- [ ] **Step 5: Build**

Run: `mvn install -pl com.top_logic.layout.view`
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ViewServlet.java
git commit -m "Ticket #29108: ViewServlet extracts route from URL path, wires RouteManager to SSE."
```

---

## Task 12: Demo Views with Routing

**Files:**
- Modify: `com.top_logic.demo/src/main/webapp/WEB-INF/views/app.view.xml`
- Create: `com.top_logic.demo/src/main/webapp/WEB-INF/views/demo/routing-demo.view.xml`
- Create: `com.top_logic.demo/src/main/webapp/WEB-INF/views/demo/routing-nested-demo.view.xml`
- Modify: `com.top_logic.demo/src/main/java/com/top_logic/demo/react/DemoReactSidebarComponent.java`

- [ ] **Step 1: Add route attributes to demo sidebar**

In `DemoReactSidebarComponent.java`, add `withRoute()` to existing nav-items:

```java
items.add(new NavigationItem("dashboard", "Dashboard", "bi bi-speedometer2",
    this::createDashboard).withRoute("/dashboard"));
items.add(new NavigationItem("forms", "Forms", "bi bi-input-cursor",
    this::createForms).withRoute("/forms"));
items.add(new NavigationItem("tables", "Tables", "bi bi-table",
    this::createTables).withRoute("/tables"));
```

Or, if the demo uses declarative views (`app.view.xml`), add `route` attributes
to the `<nav-item>` elements:

```xml
<nav-item id="dashboard" icon="bi bi-speedometer2" route="/dashboard">
  <view-ref view="demo/dashboard.view.xml"/>
  <label><en>Dashboard</en></label>
</nav-item>
```

- [ ] **Step 2: Create basic routing-demo.view.xml**

```xml
<?xml version="1.0" encoding="utf-8" ?>
<view>
  <card variant="elevated">
    <title><en>Routing Demo</en></title>
    <stack direction="column" gap="md">
      <panel>
        <title><en>This view is routed</en></title>
      </panel>
    </stack>
  </card>
</view>
```

- [ ] **Step 3: Create nested routing demo with TabBar**

```xml
<?xml version="1.0" encoding="utf-8" ?>
<view>
  <tab-bar>
    <tab id="featured" route="/featured">
      <label><en>Featured</en></label>
      <card variant="elevated">
        <title><en>Featured Tab</en></title>
      </card>
    </tab>
    <tab id="nearby" route="/nearby">
      <label><en>Nearby</en></label>
      <card variant="elevated">
        <title><en>Nearby Tab</en></title>
      </card>
    </tab>
    <tab id="internal">
      <label><en>Internal (no route)</en></label>
      <card variant="elevated">
        <title><en>This tab does not produce a URL segment</en></title>
      </card>
    </tab>
  </tab-bar>
</view>
```

Wire this into the sidebar:
```xml
<nav-item id="routing-nested" icon="bi bi-signpost" route="/nested">
  <view-ref view="demo/routing-nested-demo.view.xml"/>
  <label><en>Nested Routing</en></label>
</nav-item>
```

- [ ] **Step 4: Build demo module**

Run: `mvn install -pl com.top_logic.demo`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add com.top_logic.demo/
git commit -m "Ticket #29108: Add demo views with route attributes for routing verification."
```

---

## Task 13: Browser Verification

**Files:** None (manual testing)

This task verifies all 6 demo scenarios from the spec (Section 5) using the
running demo app and Playwright.

- [ ] **Step 1: Start the demo app**

Use the `tl-app` skill to start the demo application.

- [ ] **Step 2: Verify basic routing (Spec 5.1)**

1. Navigate to the demo app in the browser
2. Click a sidebar item with a route attribute
3. Verify the URL in the address bar changes (e.g., to `/view/dashboard`)
4. Click another sidebar item → URL changes
5. Press Back button → previous sidebar item is active, URL matches
6. Press Forward button → URL returns
7. Enter a routed URL directly → correct view loads

- [ ] **Step 3: Verify nested routing (Spec 5.5)**

1. Navigate to the "Nested Routing" sidebar item (URL: `/view/nested`)
2. Click "Featured" tab → URL becomes `/view/nested/featured`
3. Click "Nearby" tab → URL becomes `/view/nested/nearby`
4. Click "Internal" tab → URL stays at `/view/nested/nearby` (no route on this tab)
5. Press Back → URL returns to `/view/nested/featured`, Featured tab active
6. Bookmark `/view/nested/nearby` → opening it shows Nested view with Nearby tab

- [ ] **Step 4: Verify window name is hidden**

1. Navigate to any routed view
2. Verify the URL does NOT contain the window name (no `v1a2b3c` segment)
3. Press F5 → same view reloads correctly (second round-trip through bootstrap)

- [ ] **Step 5: Document results**

Record which scenarios pass/fail. If any fail, create follow-up tasks.

- [ ] **Step 6: Commit any fixes**

```bash
git add -A
git commit -m "Ticket #29108: Fix issues found during routing browser verification."
```

---

## Deferred to Later Tasks

The following items from the spec are **not implemented** in this plan and will be
addressed in follow-up work:

1. **Param-binding with bidirectional channel end-to-end** (Spec 5.2):
   Requires a demo view with a table selection that navigates to `/item/:id`.
   The bidirectional channel runtime (Task 3) is in place, but wiring it
   through param-bindings to the RouteManager requires integration work in
   the ViewElement instantiation pipeline.

2. **Query-binding end-to-end** (Spec 5.3):
   QueryBindingConfig is defined (Task 4), but the runtime wiring (channel
   observation → RouteManager → replaceState) needs implementation.

3. **Dirty-veto on back-navigation** (Spec 5.4):
   The design is clear (RouteManager catches ChannelVetoException, sends
   RouteVetoEvent), but requires integration testing with a dirty form.

4. **Hidden route with programmatic navigation** (Spec 5.6):
   NavigationItem.hidden is added (Task 7), but the navigation action
   that triggers `routeManager.navigate("/item/42")` from a table row click
   needs a new command/action type.

These are follow-up tasks once the basic routing loop (Tasks 1-13) is verified
and stable.
