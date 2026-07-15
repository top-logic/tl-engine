# ViewContext Interface Extraction Design

## Motivation

Decouple UIElement implementations from the concrete ViewContext class by extracting an interface.
UIElement and its 20+ implementations should depend on an abstraction, not a concrete class.

## Approach: In-place Extraction

Convert `ViewContext` from a class to an interface (same file, same name). Create
`DefaultViewContext` as the concrete implementation. This minimizes call-site changes because all
callers already reference the `ViewContext` type.

## Interface: ViewContext

All current public methods become interface methods:

- `ViewContext childContext(String segment)` - hierarchical child context creation
- `ViewContext withCommandScope(CommandScope scope)` - derived context with command scope
- `String getPersonalizationKey()` - current personalization path
- `ReactDisplayContext getDisplayContext()` - session display context
- `CommandScope getCommandScope()` - nearest enclosing command scope
- `FormControl getFormControl()` - nearest enclosing form control
- `void setFormControl(FormControl formControl)` - set form scope
- `void registerChannel(String name, ViewChannel channel)` - register a named channel
- `boolean hasChannel(String name)` - check channel existence
- `ViewChannel resolveChannel(ChannelRef ref)` - resolve channel reference

## Implementation: DefaultViewContext

New class in the same package. Contains all fields and logic from the current ViewContext:

- Fields: `_displayContext`, `_personalizationPath`, `_channels`, `_commandScope`, `_formControl`
- Public constructor: `DefaultViewContext(ReactDisplayContext)` for root context
- Private constructor for internal derivation (childContext, withCommandScope)
- All method implementations unchanged

## Caller Impact

| Category | Files | Change |
|---|---|---|
| UIElement implementations | ~20 | None |
| UIElement interface | 1 | None |
| ViewServlet | 1 | `new ViewContext(...)` -> `new DefaultViewContext(...)` |
| Test code | 1 | Constructor call update |

## File Changes

1. `ViewContext.java` - Replace class with interface
2. `DefaultViewContext.java` - New file with all implementation
3. `ViewServlet.java` - Update constructor call
4. Any test that directly constructs ViewContext - Update constructor call
