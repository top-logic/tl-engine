# Derived Channels Design

**Ticket**: #29108
**Date**: 2026-03-10
**Module**: `com.top_logic.layout.view` (`tl-layout-view`)

## Overview

Derived channels are read-only channels whose values are computed from other channels via a
TL-Script expression. They follow the same `inputs` + expression pattern already used by
`TableElement` for row queries. When any input channel changes, the derived channel
re-evaluates its expression and notifies its own listeners.

## XML Syntax

```xml
<view>
  <channels>
    <channel name="selectedCustomer" />
    <derived-channel name="hasSelection"
        inputs="selectedCustomer"
        expr="c -> $c != null" />
    <derived-channel name="detailTitle"
        inputs="selectedCustomer"
        expr="customer -> if($customer != null,
                 $customer.get(`tl.customers:Customer#name`),
                 `No selection`)" />
  </channels>
  ...
</view>
```

Channels are created in declaration order. A derived channel may reference any channel
declared before it, including other derived channels.

## Key Design Decision: ChannelFactory

The central insight is separating **config-time compilation** (shared across sessions) from
**per-session channel wiring**. This is achieved by changing `ChannelConfig` from
`PolymorphicConfiguration<ViewChannel>` to `PolymorphicConfiguration<ChannelFactory>`.

The `ChannelFactory` is instantiated once at configuration parse time. Expensive operations
like TL-Script expression compilation happen in its constructor. The `createChannel()` method
is called per-session to produce wired channel instances.

This preserves full extensibility: any module can introduce new channel types by implementing
`ChannelFactory` and a corresponding `ChannelConfig` subinterface, without modifying
`ViewElement`.

## Architecture

### ChannelFactory Interface

```java
/**
 * Config-time factory for creating per-session {@link ViewChannel} instances.
 *
 * <p>Instantiated once via {@link PolymorphicConfiguration} at configuration parse time.
 * Expensive operations (expression compilation) happen in the constructor.
 * {@link #createChannel} is called per-session to produce wired channel instances.</p>
 */
public interface ChannelFactory {
    ViewChannel createChannel(ViewContext context);
}
```

### ChannelConfig (Modified)

```java
public interface ChannelConfig extends PolymorphicConfiguration<ChannelFactory> {
    @Name("name")
    @Mandatory
    String getName();
}
```

### Value Channels (Refactored)

`ValueChannelConfig` gets `@ClassDefault(ValueChannelFactory.class)` instead of
`@ClassDefault(DefaultViewChannel.class)`.

```java
@TagName("channel")
public interface ValueChannelConfig extends ChannelConfig {
    @Override
    @ClassDefault(ValueChannelFactory.class)
    Class<? extends ChannelFactory> getImplementationClass();
}
```

`ValueChannelFactory` is a trivial factory that creates `DefaultViewChannel` instances:

```java
public class ValueChannelFactory implements ChannelFactory {
    private final String _name;

    public ValueChannelFactory(InstantiationContext context, ValueChannelConfig config) {
        _name = config.getName();
    }

    @Override
    public ViewChannel createChannel(ViewContext context) {
        return new DefaultViewChannel(_name);
    }
}
```

### Derived Channels (New)

**DerivedChannelConfig**:

```java
@TagName("derived-channel")
public interface DerivedChannelConfig extends ChannelConfig {
    @Override
    @ClassDefault(DerivedChannelFactory.class)
    Class<? extends ChannelFactory> getImplementationClass();

    @ListBinding(format = ChannelRefFormat.class, tag = "input", attribute = "channel")
    List<ChannelRef> getInputs();

    @Mandatory @NonNullable
    Expr getExpr();
}
```

**DerivedChannelFactory** compiles the expression once in its constructor, then creates
per-session `DerivedViewChannel` instances:

```java
public class DerivedChannelFactory implements ChannelFactory {
    private final String _name;
    private final List<ChannelRef> _inputRefs;
    private final QueryExecutor _executor;

    public DerivedChannelFactory(InstantiationContext context, DerivedChannelConfig config) {
        _name = config.getName();
        _inputRefs = config.getInputs();
        _executor = QueryExecutor.compile(config.getExpr());
    }

    @Override
    public ViewChannel createChannel(ViewContext context) {
        DerivedViewChannel channel = new DerivedViewChannel(_name);
        List<ViewChannel> inputs = _inputRefs.stream()
            .map(context::resolveChannel)
            .toList();
        channel.bind(inputs, _executor);
        return channel;
    }
}
```

**DerivedViewChannel** is a lightweight per-session object holding mutable state:

```java
public class DerivedViewChannel implements ViewChannel {
    private final String _name;
    private Object _value;
    private final CopyOnWriteArrayList<ChannelListener> _listeners = new CopyOnWriteArrayList<>();

    public DerivedViewChannel(String name) {
        _name = name;
    }

    public void bind(List<ViewChannel> inputs, QueryExecutor executor) {
        // Eager initial evaluation.
        _value = evaluate(executor, inputs);

        // Re-evaluate when any input changes.
        ChannelListener refresh = (sender, oldVal, newVal) -> {
            Object newValue = evaluate(executor, inputs);
            Object oldValue = _value;
            if (!Objects.equals(oldValue, newValue)) {
                _value = newValue;
                for (ChannelListener l : _listeners) {
                    l.handleNewValue(this, oldValue, newValue);
                }
            }
        };
        for (ViewChannel input : inputs) {
            input.addListener(refresh);
        }
    }

    @Override
    public Object get() { return _value; }

    @Override
    public boolean set(Object newValue) {
        throw new IllegalStateException("Derived channel '" + _name + "' is read-only.");
    }

    @Override
    public void addListener(ChannelListener l) { _listeners.add(l); }
    @Override
    public void removeListener(ChannelListener l) { _listeners.remove(l); }

    private static Object evaluate(QueryExecutor executor, List<ViewChannel> inputs) {
        Object[] args = new Object[inputs.size()];
        for (int i = 0; i < args.length; i++) {
            args[i] = inputs.get(i).get();
        }
        return executor.execute(args);
    }
}
```

### ViewElement (Simplified)

`ViewElement` becomes fully generic with no type-specific channel knowledge:

```java
// Constructor (config time): instantiate factories via PolymorphicConfiguration
_channelEntries = config.getChannels().stream()
    .map(cc -> Map.entry(cc.getName(), context.getInstance(cc)))
    .toList();

// createControl() (per-session): create channels via factories
for (var entry : _channelEntries) {
    String name = entry.getKey();
    if (!context.hasChannel(name)) {
        ChannelFactory factory = entry.getValue();
        context.registerChannel(name, factory.createChannel(context));
    }
}
```

## Behavioral Semantics

- **Read-only**: `set()` on a derived channel throws `IllegalStateException`.
- **Eager initial evaluation**: The expression is evaluated immediately during `bind()` so
  the channel has a value from the start. No null flicker.
- **Change detection**: `Objects.equals` comparison before notifying listeners (same as
  `DefaultViewChannel`).
- **Thread-safe listeners**: `CopyOnWriteArrayList` (same as `DefaultViewChannel`).
- **Declaration-order dependencies**: Channels are created in declaration order. A derived
  channel can reference any channel declared before it.

## Testing Plan

1. **DerivedViewChannelTest** - Unit test:
   - Initial value computed eagerly from inputs
   - Recomputation when input changes
   - `set()` throws `IllegalStateException`
   - Listeners notified on derived value change
   - No notification when recomputed value equals current (Objects.equals)
   - Multi-input derivation

2. **DerivedChannelConfigTest** - Configuration parsing:
   - Parse `<derived-channel>` from XML
   - Verify `DerivedChannelFactory` instantiated with correct inputs and compiled expression

3. **ViewElementDerivedChannelTest** - Integration:
   - Full view XML with value + derived channels
   - Derived channel resolves correctly via ViewContext
   - Declaration-order dependency (derived depending on earlier derived)

## Files to Create/Modify

**New files:**
- `ChannelFactory.java` - interface
- `ValueChannelFactory.java` - factory for value channels
- `DerivedChannelConfig.java` - configuration interface
- `DerivedChannelFactory.java` - factory with expression compilation
- `DerivedViewChannel.java` - per-session read-only channel

**Modified files:**
- `ChannelConfig.java` - change from `PolymorphicConfiguration<ViewChannel>` to `PolymorphicConfiguration<ChannelFactory>`
- `ValueChannelConfig.java` - `@ClassDefault(ValueChannelFactory.class)`
- `ViewElement.java` - use factory pattern instead of direct `getInstance(channelConfig)`
- `DefaultViewChannel.java` - remove `(InstantiationContext, ValueChannelConfig)` constructor (no longer instantiated via config)

**New test files:**
- `DerivedViewChannelTest.java`
- `DerivedChannelConfigTest.java`
- `ViewElementDerivedChannelTest.java`
