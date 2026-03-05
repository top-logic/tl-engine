# View Channels Design — Value Channels

## Goal

Add value channel infrastructure to the view system: declare named channels at the
view level, get/set/listen on them at runtime, and provide a ChannelRef config type
for elements to bind to channels.

## Scope

- Value channels only (mutable, named, observable)
- No derived channels, no cross-view references, no TL-Script
- No existing elements are modified — infrastructure only

## Design Decisions

1. **Channels on ViewContext**: Per-session channel instances live in a flat map on
   ViewContext. ViewElement creates them during `createControl()` and registers them
   before creating child controls.

2. **Polymorphic ChannelConfig**: `ChannelConfig` extends
   `PolymorphicConfiguration<ViewChannel>` so that `DerivedChannelConfig` can be added
   later without changing the list type on ViewElement.

3. **Minimal ViewChannel**: `get()`, `set(Object)`, `addListener()`,
   `removeListener()`. No veto pattern. No generics.

4. **ChannelRef config type**: A value type with a `ConfigurationValueProvider` so it
   can be used in XML attributes via `@Format`. Currently holds just a channel name.
   Later gains a path component for cross-view references.

5. **No `@DefaultContainer`** on the channels list. The `<channels>` wrapper tag stays
   explicit — the main content of `<view>` is `<content>`.

## Types

### ViewChannel (interface)

```java
public interface ViewChannel {

    Object get();

    boolean set(Object newValue);

    void addListener(ChannelListener listener);

    void removeListener(ChannelListener listener);

    interface ChannelListener {
        void handleNewValue(ViewChannel sender, Object oldValue, Object newValue);
    }
}
```

### DefaultViewChannel (implementation)

- Holds `_name` (for error messages) and `_value`
- `CopyOnWriteArrayList<ChannelListener>` for thread-safe listeners
- `set()` does equality check, fires listeners only on change
- Constructor: `DefaultViewChannel(InstantiationContext, ValueChannelConfig)`

### ChannelConfig (polymorphic base)

```java
public interface ChannelConfig extends PolymorphicConfiguration<ViewChannel> {

    @Name("name")
    @Mandatory
    String getName();
}
```

### ValueChannelConfig (concrete)

```java
@TagName("channel")
@ClassDefault(DefaultViewChannel.class)
public interface ValueChannelConfig extends ChannelConfig {
    // No extra properties for now.
}
```

### ChannelRef (value type)

```java
public class ChannelRef {

    private final String _channelName;

    public ChannelRef(String channelName) { _channelName = channelName; }

    public String getChannelName() { return _channelName; }
}
```

### ChannelRefFormat (ConfigurationValueProvider)

Parses a string to ChannelRef and back. Registered via `@Format` on config properties.

### ViewElement.Config changes

Gains:

```java
@Name("channels")
List<ChannelConfig> getChannels();
```

### ViewContext changes

Gains:

```java
void registerChannel(String name, ViewChannel channel);

ViewChannel resolveChannel(ChannelRef ref);
```

## XML Format

```xml
<view>
  <channels>
    <channel name="selectedCustomer" />
    <channel name="editMode" />
  </channels>

  <content class="...">
    ...
  </content>
</view>
```

## Runtime Flow

1. ViewServlet loads `.view.xml`, creates shared UIElement tree (unchanged)
2. `ViewElement.createControl(ViewContext)`:
   a. For each `ChannelConfig`: `context.getInstance(config)` → `ViewChannel`
   b. `viewContext.registerChannel(config.getName(), channel)`
   c. `_content.createControl(viewContext)` — children can now resolve channels
3. Any element with a `ChannelRef` config property resolves it:
   `viewContext.resolveChannel(ref)` → `ViewChannel`

## Extension Points

- **Derived channels**: Add `DerivedChannelConfig extends ChannelConfig` with `inputs`
  and `expr`. `@TagName("derived-channel")`. Implementation computes value from inputs
  via TL-Script.
- **Cross-view references**: `ChannelRef` gains a `_viewPath` field. `ChannelRefFormat`
  parses `path/to/view.view.xml#channelName`. ViewContext delegates to a view registry.
- **Type validation**: `ChannelConfig` gains a `type` property. ViewChannel validates
  values against the declared type on `set()`.

## Files to Create/Modify

| File | Action |
|------|--------|
| `ViewChannel.java` | Create — interface |
| `DefaultViewChannel.java` | Create — implementation |
| `ChannelConfig.java` | Create — polymorphic base config |
| `ValueChannelConfig.java` | Create — value channel config |
| `ChannelRef.java` | Create — value type |
| `ChannelRefFormat.java` | Create — config value provider |
| `ViewElement.java` | Modify — add channels list, create channels in createControl() |
| `ViewContext.java` | Modify — add channel registry |
| `TestViewChannel.java` | Create — unit test for get/set/listen |
| `TestChannelRef.java` | Create — unit test for parsing |
| `test-channels.view.xml` | Create — test view with channel declarations |
