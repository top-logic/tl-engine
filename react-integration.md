# React Integration for TopLogic Applications

## Specification: `com.top_logic.layout.react`

**Status**: Draft
**Target**: TopLogic 7.10.x

---

## 1. Overview

This specification defines how React-based UI components ("React views") can be embedded
within TopLogic application layouts. The approach follows the established "island
architecture" pattern: React takes ownership of a DOM subtree within a TopLogic
`LayoutComponent`, while the surrounding layout, navigation, security, and lifecycle remain
managed by the TopLogic framework.

### Goals

- Application developers can write React components for specific views (tables, dashboards,
  forms, visualizations) without touching the TopLogic rendering pipeline.
- React views communicate with the TopLogic server through a **dedicated JSON-based
  servlet** designed for structured, typed data exchange -- not the existing XML/SOAP AJAX
  protocol.
- The integration reuses TopLogic's security, session management, knowledge base, and
  component channel infrastructure.
- React assets are built with standard tooling (Vite/npm) and packaged into the Maven module
  for deployment.

### Non-Goals (for this iteration)

- Replacing the entire TopLogic UI with React.
- Supporting server-side rendering (SSR) of React components.
- A generic React component library for all TopLogic form field types.

---

## 2. Architecture

```
+------------------------------------------------------------------+
|  Browser                                                          |
|                                                                   |
|  +------------------+          +------------------------------+   |
|  | TopLogic Layout  |          |  React Island                |   |
|  | (server-rendered |          |  (client-rendered SPA)       |   |
|  |  HTML + AJAX)    |          |                              |   |
|  |                  |   mount  |  React App                   |   |
|  |  <div id="react- +--------->  - fetches data via JSON      |   |
|  |   mount-{id}"/>  |          |  - sends commands via JSON   |   |
|  |                  |          |  - receives push updates     |   |
|  +--------+---------+          +-------+----------------------+   |
|           |                            |                          |
+-----------+----------------------------+--------------------------+
            | SOAP/XML (existing)        | JSON (new)
            v                            v
+------------------------------------------------------------------+
|  TopLogic Server (Servlet Container)                              |
|                                                                   |
|  +------------------+          +------------------------------+   |
|  | AJAXServlet      |          | ReactServlet                |   |
|  | /ajax            |          | /react-api/*                |   |
|  |                  |          |                              |   |
|  | - XML protocol   |          | - JSON protocol             |   |
|  | - DOM actions    |          | - Component data endpoint   |   |
|  | - Command        |          | - Command dispatch          |   |
|  |   handlers       |          | - Channel subscriptions     |   |
|  +--------+---------+          +-------+----------------------+   |
|           |                            |                          |
|           +------------+---------------+                          |
|                        |                                          |
|              +---------+---------+                                |
|              | LayoutComponent   |                                |
|              | Knowledge Base    |                                |
|              | Security          |                                |
|              | Component         |                                |
|              |   Channels        |                                |
|              +-------------------+                                |
+------------------------------------------------------------------+
```

### Key Principle

The `ReactServlet` is **component-scoped**: every request targets a specific
`LayoutComponent` instance (identified by its component name). The servlet validates the
session, resolves the component, checks security, and then delegates to
component-specific handlers. This keeps React views within the TopLogic security and
lifecycle model.

---

## 3. Module Structure

```
com.top_logic.layout.react/
├── pom.xml
├── package.json                          # npm project for React build tooling
├── vite.config.ts                        # Vite bundler configuration
├── tsconfig.json
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/top_logic/layout/react/
│   │   │       ├── ReactComponent.java           # LayoutComponent for React islands
│   │   │       ├── ReactServlet.java              # JSON API servlet
│   │   │       ├── ReactControlProvider.java      # Renders the mount <div>
│   │   │       ├── ReactDataProvider.java         # Interface: provides data to React
│   │   │       ├── ReactCommandHandler.java       # Base class for React-invoked commands
│   │   │       ├── I18NConstants.java
│   │   │       └── package-info.java
│   │   ├── webapp/
│   │   │   ├── WEB-INF/
│   │   │   │   ├── web-fragment.xml               # Servlet registration
│   │   │   │   ├── conf/react.conf.xml
│   │   │   │   └── themes/core/theme.xml          # CSS registration
│   │   │   └── react/                             # Built React assets (output of Vite)
│   │   │       ├── react-bridge.js                # TL<->React bridge library
│   │   │       └── vendor.js                      # React runtime (bundled)
│   │   └── resources/
│   │       └── META-INF/
│   │           └── messages_en.properties
│   └── test/
│       └── java/
│           └── test/com/top_logic/layout/react/
│               └── ...
├── react-src/                             # React application source (TypeScript)
│   ├── bridge/
│   │   ├── tl-react-bridge.ts             # Client-side bridge API
│   │   └── types.ts                       # Shared type definitions
│   └── example/
│       └── ExampleView.tsx                # Example React view
└── README.md
```

### Maven Integration

The `pom.xml` uses `frontend-maven-plugin` to run npm/Vite during the Maven build:

```xml
<plugin>
    <groupId>com.github.eirslett</groupId>
    <artifactId>frontend-maven-plugin</artifactId>
    <configuration>
        <nodeVersion>v20.11.0</nodeVersion>
        <workingDirectory>${project.basedir}</workingDirectory>
    </configuration>
    <executions>
        <execution>
            <id>install-node-and-npm</id>
            <goals><goal>install-node-and-npm</goal></goals>
        </execution>
        <execution>
            <id>npm-install</id>
            <goals><goal>npm</goal></goals>
            <configuration>
                <arguments>ci</arguments>
            </configuration>
        </execution>
        <execution>
            <id>vite-build</id>
            <goals><goal>npm</goal></goals>
            <configuration>
                <arguments>run build</arguments>
            </configuration>
        </execution>
    </executions>
</plugin>
```

Vite outputs into `src/main/webapp/react/`, which is then packaged into the WAR overlay.

---

## 4. Server-Side Components

### 4.1 `ReactComponent`

A `LayoutComponent` subclass that hosts a React island.

```java
public class ReactComponent extends LayoutComponent {

    public interface Config extends LayoutComponent.Config {

        String TAG_NAME = "reactView";

        /** Path to the React entry module (relative to /react/). */
        @Name("react-module")
        @Mandatory
        String getReactModule();

        /** Data provider that supplies the component's model as JSON. */
        @Name("data-provider")
        @InstanceFormat
        ReactDataProvider getDataProvider();

        /** Additional React-invokable commands beyond the defaults. */
        @Name("commands")
        @Key(ReactCommandHandler.Config.NAME_ATTRIBUTE)
        Map<String, ReactCommandHandler.Config> getCommands();
    }
}
```

**Layout XML usage:**

```xml
<reactView name="myReactView"
           react-module="my-app/MyDashboard"
           data-provider="com.example.app.MyDashboardDataProvider">
    <commands>
        <command name="saveItem"
                 class="com.example.app.SaveItemCommand"/>
    </commands>
    <modelChannel>
        <channelLinking name="selection"
                        channel="myTable.component#selection"/>
    </modelChannel>
</reactView>
```

### 4.2 `ReactControlProvider`

Renders the mount point that React attaches to:

```java
public class ReactControlProvider implements LayoutControlProvider {

    @Override
    public LayoutControl createLayoutControl(
            Strategy strategy, LayoutComponent component) {
        // Returns a control that writes:
        //   <div id="react-mount-{componentId}"
        //        data-react-module="{reactModule}"
        //        data-component-id="{componentId}">
        //   </div>
        //   <script>
        //     TLReact.mount("{componentId}", "{reactModule}");
        //   </script>
    }
}
```

The rendered `<div>` acts as the React root. After rendering, the bridge script
`TLReact.mount()` initializes the React application into that element.

### 4.3 `ReactDataProvider`

Interface for supplying data to a React view. Implementations convert TopLogic models
into JSON-serializable structures.

```java
public interface ReactDataProvider
        extends ConfiguredInstance<ReactDataProvider.Config> {

    interface Config extends PolymorphicConfiguration<ReactDataProvider> {
        // Configuration properties
    }

    /**
     * Produce the JSON data for the given component's current state.
     *
     * @param component the hosting ReactComponent
     * @param arguments optional request arguments (filters, pagination, etc.)
     * @return a value that can be serialized to JSON (Map, List, String, Number,
     *         Boolean, null, or a ConfigurationItem with a JsonBinding)
     */
    Object getData(ReactComponent component, Map<String, Object> arguments);
}
```

### 4.4 `ReactCommandHandler`

Base class for commands that React views can invoke:

```java
public abstract class ReactCommandHandler
        extends AJAXCommandHandler {

    public interface Config extends AJAXCommandHandler.Config {
        // Inherits NAME_ATTRIBUTE for command identification
    }

    /**
     * Handle a command from the React UI.
     *
     * @param context    display context
     * @param component  the hosting ReactComponent
     * @param payload    parsed JSON payload from the React client
     * @return result object to serialize back as JSON response
     */
    public abstract Object handleReactCommand(
            DisplayContext context,
            ReactComponent component,
            Map<String, Object> payload);
}
```

---

## 5. The React Servlet (`ReactServlet`)

### 5.1 Registration

```xml
<!-- web-fragment.xml -->
<web-fragment>
    <servlet>
        <servlet-name>ReactServlet</servlet-name>
        <servlet-class>com.top_logic.layout.react.ReactServlet</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>ReactServlet</servlet-name>
        <url-pattern>/react-api/*</url-pattern>
    </servlet-mapping>
</web-fragment>
```

### 5.2 URL Scheme

All endpoints are scoped to a component instance:

```
POST /react-api/{component-name}/data      → Fetch component data
POST /react-api/{component-name}/command    → Execute a command
POST /react-api/{component-name}/subscribe  → (Future) Subscribe to channel updates
```

The `{component-name}` corresponds to the `name` attribute in the layout XML (the
`ComponentName`). The servlet resolves this to the live `LayoutComponent` instance via
`MainLayout.getComponentByName()`.

### 5.3 Protocol

**Content-Type**: `application/json; charset=utf-8` (both request and response)

**Common request structure:**

```json
{
    "componentId": "myReactView",
    "type": "data | command",
    "...payload"
}
```

**Common response structure:**

```json
{
    "success": true,
    "data": { ... },
    "error": null
}
```

On error:

```json
{
    "success": false,
    "data": null,
    "error": {
        "code": "UNAUTHORIZED | NOT_FOUND | VALIDATION | INTERNAL",
        "message": "Human-readable error description"
    }
}
```

### 5.4 Endpoints

#### `POST /react-api/{component}/data`

Fetches the current data for the React view.

**Request:**

```json
{
    "arguments": {
        "page": 1,
        "pageSize": 50,
        "filter": { "status": "active" }
    }
}
```

**Response:**

```json
{
    "success": true,
    "data": {
        "items": [ ... ],
        "totalCount": 1234,
        "channels": {
            "selection": { "type": "MyType", "id": "obj-123" }
        }
    }
}
```

The `channels` field reflects the current state of component channels that the React view
should be aware of (e.g., the current selection from a linked table component).

#### `POST /react-api/{component}/command`

Executes a named command.

**Request:**

```json
{
    "command": "saveItem",
    "payload": {
        "id": "obj-123",
        "name": "Updated Name",
        "status": "active"
    }
}
```

**Response:**

```json
{
    "success": true,
    "data": {
        "result": "saved",
        "updatedItem": { "id": "obj-123", "name": "Updated Name" }
    }
}
```

The servlet dispatches to the `ReactCommandHandler` registered under the given command
name in the component's configuration.

### 5.5 Request Lifecycle

```
1. Parse JSON request body
2. Extract session from HTTP session (same cookie as TopLogic main app)
3. Validate session is active, user is authenticated
4. Resolve LayoutComponent by name from the user's MainLayout
5. Verify component is visible and accessible to the current user (security check)
6. Route to handler:
   - "data"    → call ReactDataProvider.getData()
   - "command" → look up ReactCommandHandler, execute in transaction
7. Serialize result to JSON
8. Return response with appropriate HTTP status
```

### 5.6 Session and Security

The `ReactServlet` participates in the same HTTP session as the main TopLogic application.
Since the React island is rendered inside a TopLogic page, the browser already has the
session cookie. No separate authentication is needed.

Security checks:

- The component's `BoundChecker` is consulted for every request (same as for AJAX
  commands).
- Command handlers can define their own `CommandGroup` for fine-grained access control.
- If the session is invalid or expired, the servlet returns HTTP 401 with an error JSON.

### 5.7 Concurrency

Unlike the existing AJAX servlet's SOAP protocol with sequence numbers and writer locks,
the React servlet uses a simpler model:

- **Data requests** are read-only and can execute concurrently.
- **Command requests** acquire a component-level lock (similar to the existing
  `RequestLock.enterWriter()`), ensuring sequential command processing per component.
- No sequence numbering is required -- the React client handles its own optimistic UI
  updates and can retry failed commands.

---

## 6. Client-Side Bridge (`TLReact`)

### 6.1 Bridge API

The bridge library (`react-src/bridge/tl-react-bridge.ts`) provides the API that React
components use to communicate with TopLogic:

```typescript
// --- Types ---

interface TLComponentContext {
    /** The TopLogic component name this React view is mounted in. */
    componentId: string;

    /** Base URL for the React API servlet. */
    apiBase: string;
}

interface TLDataResponse<T = unknown> {
    success: boolean;
    data: T | null;
    error: { code: string; message: string } | null;
}

// --- API ---

/**
 * Fetch data from the component's ReactDataProvider.
 */
async function fetchData<T>(
    ctx: TLComponentContext,
    args?: Record<string, unknown>
): Promise<T>;

/**
 * Execute a named command on the component.
 */
async function executeCommand<T>(
    ctx: TLComponentContext,
    command: string,
    payload?: Record<string, unknown>
): Promise<T>;

/**
 * React hook: use component data with automatic loading state.
 */
function useComponentData<T>(
    args?: Record<string, unknown>,
    deps?: unknown[]
): { data: T | null; loading: boolean; error: string | null; refetch: () => void };

/**
 * React hook: execute a command with loading/error state.
 */
function useCommand<T>(
    command: string
): {
    execute: (payload?: Record<string, unknown>) => Promise<T>;
    loading: boolean;
    error: string | null;
};
```

### 6.2 Mounting

When `TLReact.mount(componentId, reactModule)` is called (from the server-rendered
inline script):

1. The bridge resolves the React module from the bundle (Vite dynamic import).
2. It creates a `TLComponentContext` with the component ID and the servlet base URL.
3. It wraps the React component in a `<TLContext.Provider>` so all child components
   can access the bridge API via `useContext`.
4. It calls `ReactDOM.createRoot(mountElement).render(...)`.

```typescript
// Simplified mount logic
async function mount(componentId: string, reactModule: string): Promise<void> {
    const mountEl = document.getElementById(`react-mount-${componentId}`);
    const module = await import(`/react/${reactModule}.js`);
    const App = module.default;

    const ctx: TLComponentContext = {
        componentId,
        apiBase: `${contextPath}/react-api`,
    };

    const root = ReactDOM.createRoot(mountEl);
    root.render(
        <TLContext.Provider value={ctx}>
            <App />
        </TLContext.Provider>
    );
}
```

### 6.3 Lifecycle

- **Mount**: When the TopLogic component is rendered (page load or AJAX navigation).
- **Unmount**: When the TopLogic component is hidden or removed (tab switch, navigation).
  The `ReactControlProvider` generates a `JSSnipplet` action that calls
  `TLReact.unmount(componentId)` to clean up the React root.
- **Update**: When the TopLogic component's model changes (e.g., channel update from
  another component), the server can send a `JSSnipplet` action that calls
  `TLReact.notify(componentId, "channelName", newValue)`, which triggers a re-fetch or
  direct state update in the React tree.

---

## 7. Channel Integration

React views can participate in TopLogic's component channel system for cross-component
communication.

### 7.1 Receiving Channel Updates

When a linked component (e.g., a table) changes its selection channel, the
`ReactComponent` receives the channel event on the server side. It then enqueues a
`JSSnipplet` client action:

```javascript
TLReact.notify("myReactView", "selection", {"type":"MyType","id":"obj-123"});
```

On the React side, the bridge dispatches this as a custom event or updates a React
context, causing the view to re-render or re-fetch data.

```typescript
// React hook for channel updates
function useChannel<T>(channelName: string): T | null;
```

### 7.2 Sending Channel Updates

When the React view wants to update a channel (e.g., the user selects an item in the
React view and this should propagate to other TopLogic components):

```typescript
// In React component
const { execute } = useCommand("setChannel");
await execute({ channel: "selection", value: { type: "MyType", id: "obj-456" } });
```

The `ReactServlet` handles a built-in `setChannel` command that calls
`component.getChannel("selection").set(resolvedObject)` on the server side, triggering
the normal TopLogic channel propagation.

---

## 8. Developing a React View

### 8.1 Step-by-Step

1. **Create the React component** in `react-src/`:

```tsx
// react-src/my-app/MyDashboard.tsx
import { useComponentData, useCommand } from '../bridge/tl-react-bridge';

interface DashboardData {
    items: Array<{ id: string; name: string; status: string }>;
    totalCount: number;
}

export default function MyDashboard() {
    const { data, loading, error, refetch } = useComponentData<DashboardData>();
    const { execute: saveItem, loading: saving } = useCommand('saveItem');

    if (loading) return <div>Loading...</div>;
    if (error) return <div>Error: {error}</div>;

    async function handleSave(id: string, name: string) {
        await saveItem({ id, name });
        refetch();
    }

    return (
        <div>
            <h2>Dashboard ({data.totalCount} items)</h2>
            <ul>
                {data.items.map(item => (
                    <li key={item.id}>
                        {item.name} - {item.status}
                        <button onClick={() => handleSave(item.id, 'New Name')}>
                            Rename
                        </button>
                    </li>
                ))}
            </ul>
        </div>
    );
}
```

2. **Create the data provider** (Java):

```java
public class MyDashboardDataProvider
        extends AbstractConfiguredInstance<MyDashboardDataProvider.Config>
        implements ReactDataProvider {

    public interface Config extends ReactDataProvider.Config {
        // custom config if needed
    }

    @Override
    public Object getData(ReactComponent component, Map<String, Object> arguments) {
        KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
        // Query items, build result map
        List<Map<String, Object>> items = ...;
        return Map.of(
            "items", items,
            "totalCount", items.size()
        );
    }
}
```

3. **Create the command handler** (Java):

```java
public class SaveItemCommand extends ReactCommandHandler {

    @Override
    public Object handleReactCommand(
            DisplayContext context,
            ReactComponent component,
            Map<String, Object> payload) {
        String id = (String) payload.get("id");
        String name = (String) payload.get("name");

        KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
        try (Transaction tx = kb.beginTransaction()) {
            KnowledgeObject item = ... // look up by id
            item.setAttributeValue("name", name);
            tx.commit();
        }
        return Map.of("result", "saved");
    }
}
```

4. **Configure in layout XML:**

```xml
<reactView name="myDashboard"
           react-module="my-app/MyDashboard"
           data-provider="com.example.app.MyDashboardDataProvider">
    <commands>
        <command name="saveItem"
                 class="com.example.app.SaveItemCommand"/>
    </commands>
</reactView>
```

5. **Build:**

```bash
cd com.top_logic.layout.react
npm run build          # Vite builds react-src/ → src/main/webapp/react/
mvn install            # Packages everything into the module JAR
```

---

## 9. JSON Serialization

The `ReactServlet` uses TopLogic's existing JSON utilities
(`com.top_logic.common.json.gstream.JsonWriter`) for serialization. The data returned by
`ReactDataProvider.getData()` and `ReactCommandHandler.handleReactCommand()` must be
JSON-compatible:

| Java Type | JSON Type |
|---|---|
| `String` | string |
| `Number` (int, long, double) | number |
| `Boolean` | boolean |
| `null` | null |
| `Map<String, ?>` | object |
| `List<?>` / `Collection<?>` | array |
| `ConfigurationItem` with `@JsonBinding` | per binding |

For knowledge base objects (`TLObject`, `KnowledgeObject`), a default serialization
strategy converts them to `{"type": "module:TypeName", "id": "objectId"}` references. The
`ReactDataProvider` is responsible for expanding these into the shape needed by the React
view.

---

## 10. Error Handling

### Server-Side

- `ReactDataProvider` and `ReactCommandHandler` can throw `TopLogicException` for
  user-facing errors. The servlet catches these and returns:

```json
{
    "success": false,
    "error": {
        "code": "VALIDATION",
        "message": "The localized error message from the ResKey"
    }
}
```

- Unexpected exceptions result in `"code": "INTERNAL"` with a generic message (details
  logged server-side).

### Client-Side

- The `useComponentData` and `useCommand` hooks expose `error` state that React
  components can render.
- HTTP 401 responses trigger a redirect to the TopLogic login page (handled by the
  bridge).
- Network errors are retried once, then surfaced to the React component.

---

## 11. Application Developer Modules

Application-specific React views live in their own module (e.g.,
`com.example.myapp.react`), which depends on `com.top_logic.layout.react`. The
application module contains:

- Additional `react-src/` components (built by Vite in that module)
- Java `ReactDataProvider` and `ReactCommandHandler` implementations
- Layout XML referencing the React views

The `com.top_logic.layout.react` module provides only the framework: the servlet, bridge
library, `ReactComponent`, and base classes.

---

## 12. Future Extensions

These are explicitly out of scope for the initial implementation but should be considered
in the architecture:

- **Server-Sent Events (SSE)** or **WebSocket** endpoint for real-time channel push
  (replacing the polling/notification approach in Section 7).
- **Schema generation**: Auto-generate TypeScript types from TopLogic model definitions.
- **Form binding**: A React component library that maps TopLogic form field types
  (StringField, SelectField, DateField, etc.) to React form controls.
- **Hot module replacement**: During development, Vite's HMR dev server proxied through
  TopLogic for instant React reload without full page refresh.

---

## 13. Open Questions

1. **Transaction scope for commands**: Should each command run in its own transaction (as
   specified above), or should there be a way to batch multiple commands in one
   transaction?

2. **Pagination/streaming**: For large data sets, should the data endpoint support
   cursor-based pagination, or is offset/limit sufficient?

3. **Caching**: Should the servlet support ETags or cache headers for data responses, or
   is this left to the data provider?

4. **Multi-language**: How should I18N work for React views? Options:
   - The data endpoint includes all display labels (server-resolved).
   - A separate endpoint serves the resource bundle for the current locale.
   - React views manage their own i18n independently.

5. **Theme integration**: Should React views have access to the TopLogic theme's CSS
   variables/design tokens, or are they styled independently?
