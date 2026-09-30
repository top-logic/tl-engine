# Field Control Service Design

**Ticket:** #29108
**Date:** 2026-03-12
**Module:** `com.top_logic.layout.view`

## Problem

`FieldControlFactory` is a static utility with a hardcoded `switch` on `TLPrimitive.Kind`. It cannot be extended without modifying source code. The new `ReactIconSelectControl` exists but cannot be activated because the factory has no mapping for `ThemeImage` custom types. There is no way for applications or in-app users to configure which control renders a given attribute or type.

## Design

### Overview

Replace `FieldControlFactory` with a `FieldControlService` (`ConfiguredManagedClass`) that encapsulates a multi-level resolution chain. Introduce a `TLAnnotation` (`TLInputControl`) for per-attribute and per-type control provider configuration, usable both in model XML and in-app. All classes live in `com.top_logic.layout.view.form`.

### Resolution chain (hidden inside the service)

1. **Attribute/type annotation:** `part.getAnnotation(TLInputControl.class)` -- attribute-level annotation, falls through to the type-level annotation automatically via `@DefaultStrategy(VALUE_TYPE)`.
2. **Service map:** Lookup `part.getType()` in the service's `TLModelPartRef`-keyed map.
3. **Primitive-kind fallback:** Built-in switch for unmapped `TLPrimitive.Kind` values.

Callers see only `FieldControlService.getInstance().createFieldControl(context, part, model)`.

### New classes

#### `ReactFieldControlProvider`

Functional interface for creating a React input control for a model attribute.

```java
@FunctionalInterface
public interface ReactFieldControlProvider {
    ReactControl createControl(ReactContext context, TLStructuredTypePart part, FieldModel model);
}
```

#### `TLInputControl`

TLAnnotation for configuring the control provider on a type or attribute.

```java
@TagName("input-control")
@InApp
@DefaultStrategy(Strategy.VALUE_TYPE)
public interface TLInputControl extends TLAttributeAnnotation, TLTypeAnnotation {
    @Mandatory
    PolymorphicConfiguration<? extends ReactFieldControlProvider> getImpl();
}
```

- Extends both `TLAttributeAnnotation` and `TLTypeAnnotation`.
- `@InApp` makes it available in the in-app editor.
- `@DefaultStrategy(VALUE_TYPE)` means an attribute without this annotation inherits it from its value type.

#### `FieldControlService`

`ConfiguredManagedClass` with a global type-to-provider map and the resolution chain.

```java
public class FieldControlService extends ConfiguredManagedClass<FieldControlService.Config> {

    public interface Config extends ConfiguredManagedClass.Config<FieldControlService> {
        @Key(ProviderMapping.TYPE)
        Map<TLModelPartRef, ProviderMapping> getProviders();
    }

    public interface ProviderMapping extends ConfigurationItem {
        String TYPE = "type";

        @Name(TYPE)
        TLModelPartRef getType();

        @Mandatory
        PolymorphicConfiguration<? extends ReactFieldControlProvider> getImpl();
    }

    /** Resolves the control for the given attribute via the resolution chain. */
    public ReactControl createFieldControl(ReactContext context, TLStructuredTypePart part, FieldModel model);

    public static FieldControlService getInstance();

    public static final class Module extends TypedRuntimeModule<FieldControlService> {
        public static final Module INSTANCE = new Module();
    }
}
```

#### Built-in provider implementations

All providers are stateless. They inspect the `part` parameter as needed.

| Class | Handles |
|---|---|
| `CheckboxControlProvider` | BOOLEAN |
| `NumberInputControlProvider` | INT, FLOAT (reads primitive kind from `part` to determine decimal places) |
| `DatePickerControlProvider` | DATE |
| `TextInputControlProvider` | STRING, TRISTATE, BINARY, and default fallback |
| `ColorInputControlProvider` | Color custom type |
| `IconSelectControlProvider` | ThemeImage custom type |

### Modified classes

- **`AttributeFieldControl`:** Changes `FieldControlFactory.createFieldControl(context, part, model)` to `FieldControlService.getInstance().createFieldControl(context, part, model)`.

### Removed classes

- **`FieldControlFactory`:** Replaced entirely by `FieldControlService`.

### Configuration

Default XML configuration in `com.top_logic.layout.view`:

```xml
<config service-class="com.top_logic.layout.view.form.FieldControlService">
  <instance>
    <providers>
      <provider type="tl.core:Icon">
        <impl class="com.top_logic.layout.view.form.IconSelectControlProvider"/>
      </provider>
      <provider type="tl.util:Color">
        <impl class="com.top_logic.layout.view.form.ColorInputControlProvider"/>
      </provider>
    </providers>
  </instance>
</config>
```

Primitive kinds (BOOLEAN, INT, FLOAT, DATE, STRING, etc.) are handled by the built-in fallback and do not need map entries.

The configuration file goes in `src/main/webapp/WEB-INF/conf/` of the `com.top_logic.layout.view` module.

### Fallback behavior

When no provider matches at any level, `TextInputControlProvider` is used as the ultimate default. No exceptions are thrown for unmapped types.

### Model XML usage

Per-attribute override:
```xml
<property name="icon" type="tl.core:Icon">
  <annotations>
    <input-control>
      <impl class="com.top_logic.layout.view.form.IconSelectControlProvider"/>
    </input-control>
  </annotations>
</property>
```

Per-type default (all attributes of this type use this control):
```xml
<datatype name="Icon" ...>
  <annotations>
    <input-control>
      <impl class="com.top_logic.layout.view.form.IconSelectControlProvider"/>
    </input-control>
  </annotations>
</datatype>
```
