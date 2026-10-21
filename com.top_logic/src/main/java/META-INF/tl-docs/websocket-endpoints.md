---
description: Read before serving a Jakarta websocket endpoint from an application - the jakarta.websocket-api dependency, an @ServerEndpoint class, its registration through the WebSocketEndpointListener and the context parameter websocket.endpoints in web.xml, the ServerContainer of the development start (com.top_logic.ide.jetty.Bootstrap), and a client example.
order: 60
---

# Websocket endpoints

An application serves a Jakarta websocket endpoint by implementing an `@ServerEndpoint` class and
registering it with the `ServerContainer` of the servlet container through
`com.top_logic.util.WebSocketEndpointListener`.

## Dependency

The servlet container provides the websocket implementation; the application compiles against the
API only:

```xml
<dependency>
  <groupId>jakarta.websocket</groupId>
  <artifactId>jakarta.websocket-api</artifactId>
  <scope>provided</scope>
</dependency>
```

The engine's root POM manages the version.

## Endpoint

An endpoint is a class annotated with `jakarta.websocket.server.ServerEndpoint`. The path of the
annotation is relative to the context path of the application:

```java
@ServerEndpoint(EchoEndpoint.PATH)
public class EchoEndpoint {

    public static final String PATH = "/ws/echo";

    @OnMessage
    public String onMessage(String message) {
        return message;
    }
}
```

## Registration

The `WebSocketEndpointListener` registers the classes named in the context parameter
`websocket.endpoints` (fully qualified class names separated by whitespace or commas) through
`ServerContainer.addEndpoint(Class)`:

```xml
<context-param>
    <param-name>websocket.endpoints</param-name>
    <param-value>my.app.EchoEndpoint</param-value>
</context-param>

<listener>
    <listener-class>com.top_logic.util.WebSocketEndpointListener</listener-class>
</listener>
```

The registration uses the standard API only, so it works on every servlet container providing
Jakarta websocket: Tomcat, Jetty with its websocket module, and the development start. The
automatic discovery of `@ServerEndpoint` classes depends on a container that scans the class path of
the web application; the development start scans nothing, so an endpoint relying on discovery is
missing there. The listener logs each registered endpoint (`Registered WebSocket endpoint ...`) and
logs an error for a class it cannot load or register, without stopping the application.

An endpoint registered this way requires no login: the handshake is a plain request that is not
bound to a TopLogic session. An endpoint serving user data authenticates the connection itself.

## Development start

The development start (`com.top_logic.ide.jetty.Bootstrap` in `tl-ide-jetty`) configures the Jakarta
websocket support of Jetty for the application, so the `ServerContainer` is available as servlet
context attribute `jakarta.websocket.server.ServerContainer` and the listener registers the endpoints
as in a deployed application.

## Client

In the browser:

```javascript
const socket = new WebSocket(`ws://${location.host}/my-app/ws/echo`);
socket.onmessage = event => console.log(event.data);
socket.onopen = () => socket.send("hello");
```

In Java, with `java.net.http.WebSocket`:

```java
WebSocket socket = HttpClient.newHttpClient().newWebSocketBuilder()
    .buildAsync(URI.create("ws://localhost:8080/my-app/ws/echo"), new WebSocket.Listener() {
        @Override
        public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
            System.out.println(data);
            return WebSocket.Listener.super.onText(ws, data, last);
        }
    })
    .join();
socket.sendText("hello", true).join();
```

## Examples

- `com.top_logic.demo.react.websocket.EchoEndpoint` in the React demo (`tl-demo-react`, see
  [the demo apps](doc:engine/demo-apps)) is registered in its `web.xml` and answers at
  `/tl-demo-react/ws/echo`.
- `test.com.top_logic.util.TestWebSocketEndpointListener` registers an echo endpoint in an embedded
  Jetty and talks to it with `java.net.http.WebSocket`.
