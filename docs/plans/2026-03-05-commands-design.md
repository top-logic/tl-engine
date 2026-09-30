# View Command System Design

## Context

The view system (Ticket #29108) currently has no command infrastructure. The legacy
`CommandHandler` interface is tightly coupled to `LayoutComponent` — it takes
`LayoutComponent` as a parameter in 6 core methods (handleCommand, isExecutable,
getConfirmKey, checkSecurity, getResourceKey, getTargetModel). A clean break is
needed.

This document designs the `ViewCommand` system: a complete replacement for
`CommandHandler` in the view layer, with no `LayoutComponent` dependency.

**Module:** `com.top_logic.layout.view` (artifact `tl-layout-view`)

**Spec reference:** `docs/plans/TL-VIEWS-SPEC.md`, Section 9

## Design Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Legacy bridge | Clean break | ViewCommand is independent. No adapter wrapping legacy CommandHandlers. |
| Execute signature | `execute(ViewDisplayContext, Object input)` | Single input covers 90%+ of commands. Multi-input handled by ScriptedViewCommand subclass. No args map — all data flows through channels. |
| Execution context | `ViewDisplayContext` | ViewContext is a factory context (setup time). ViewDisplayContext provides SSE and HTTP access at runtime. Channels resolved at setup time via closures. |
| Executability rule input | `isExecutable(Object input)` | Rules inspect the same single input the command operates on. |
| Security | Deferred | Security configuration will be redesigned separately. Not part of this design. |
| Module location | `com.top_logic.layout.view` | Already depends on tl-core for BoundCommandGroup etc. |
| Architecture | Stateless command pattern | ViewCommand is a stateless handler (singleton per panel). CommandModel bridges to UI. |

## 1. ViewCommand Interface

```java
/**
 * A command in the view system.
 *
 * <p>Replaces the legacy {@code CommandHandler} which depends on
 * {@code LayoutComponent}. ViewCommand is a stateless handler instantiated
 * once from configuration. All data flows through channels, not through
 * untyped argument maps.</p>
 */
public interface ViewCommand {

    /**
     * Configuration for a {@link ViewCommand}.
     */
    interface Config extends PolymorphicConfiguration<ViewCommand> {

        // --- Identity and display ---

        /** Command name for referencing from {@code <button>} or {@code <command-ref>}. */
        @Name("name")
        @Nullable
        String getName();

        /** Internationalized label (button text, menu entry text). */
        @Name("label")
        @Nullable
        ResKey getLabel();

        /** Icon displayed on the command button. */
        @Name("image")
        @Nullable
        ThemeImage getImage();

        /** CSS classes for the command button. */
        @Name("css-classes")
        @Nullable
        String getCssClasses();

        // --- Placement and grouping ---

        /** Where this command's button appears. Default defined by implementation. */
        @Name("placement")
        CommandPlacement getPlacement();

        /** Which clique this command belongs to (ordering and visual grouping). */
        @Name("clique")
        @Nullable
        String getClique();

        // --- Input ---

        /**
         * Channel reference providing the input value this command operates on.
         *
         * <p>Replaces the legacy {@code target} property which used
         * component-relative navigation expressions. In the view system,
         * the input is an explicit channel reference.</p>
         *
         * <p>Null means the command operates without input (e.g., "create new").</p>
         */
        @Name("input")
        @Nullable
        @Format(ChannelRefFormat.class)
        ChannelRef getInput();

        // --- Executability ---

        /**
         * Rules determining if the command is executable (enabled/disabled/hidden).
         *
         * <p>Same concept as legacy {@code ExecutabilityRule}, but implementations
         * receive a single input value instead of {@code LayoutComponent}.</p>
         */
        @Name("executability")
        @EntryTag("rule")
        List<PolymorphicConfiguration<? extends ViewExecutabilityRule>> getExecutability();

        // --- Confirmation ---

        /** Confirmation dialog shown before command execution. Null means no confirmation. */
        @Name("confirmation")
        @Nullable
        PolymorphicConfiguration<? extends ViewCommandConfirmation> getConfirmation();

        // --- Dirty handling ---

        /** Scope of dirty checking before command execution. Default: SELF. */
        @Name("check-dirty")
        DirtyCheckScope getCheckDirty();

        // --- Security ---

        /** Security command group (READ, WRITE, DELETE, ...) for access control. */
        @Name("group")
        @Nullable
        CommandGroupReference getGroup();
    }

    /**
     * Execute the command.
     *
     * @param context The display context for SSE updates and HTTP access.
     * @param input   The resolved input value (from the configured input channel),
     *                or null if no input is configured.
     * @return The command result.
     */
    HandlerResult execute(ViewDisplayContext context, Object input);
}
```

### Input Resolution

The `input` property on `ViewCommand.Config` is a `ChannelRef`. At setup time (when
the panel creates its `ViewCommandModel`), the channel reference is resolved to a
`ViewChannel` instance. At execution time, the `ViewCommandModel` reads the current
value from the channel and passes it as the `input` parameter to `execute()`.

For multi-input commands (rare), a `ScriptedViewCommand` subclass declares its own
`List<ChannelRef> getInputs()` property and resolves them internally.

### Properties Carried Over from Legacy

| View System Property | Legacy Property       | Change |
|---------------------|-----------------------|--------|
| `name`              | `id`                  | Renamed for clarity |
| `label`             | `resourceKey`         | Renamed |
| `image`             | `image`               | Unchanged |
| (removed)           | `disabledImage`       | Obsolete (PNG-era) |
| `css-classes`       | `cssClasses`          | Unchanged |
| `placement`         | (new)                 | New in view system |
| `clique`            | `clique`              | String (extensible for local cliques) |
| `input`             | `target`              | Single channel ref replaces component navigation |
| `executability`     | `executability`       | New ViewExecutabilityRule interface |
| `confirmation`      | `confirmation`        | New ViewCommandConfirmation interface |
| `group`             | `group`               | Unchanged (BoundCommandGroup) |
| `check-dirty`       | `checkScopeProvider`  | Declarative enum replaces pluggable tree walk |
| (deferred)          | `securityObject`      | Security redesign is separate |

## 2. Executability Rules

```java
/**
 * Determines whether a command is executable.
 *
 * <p>Replaces the legacy {@code ExecutabilityRule} which received a
 * {@code LayoutComponent}. The new interface receives the resolved
 * command input value directly.</p>
 */
public interface ViewExecutabilityRule {

    interface Config extends PolymorphicConfiguration<ViewExecutabilityRule> {
    }

    /**
     * Determine if the command is executable.
     *
     * @param input The resolved input value (same as what the command receives).
     * @return The executability state.
     */
    ExecutableState isExecutable(Object input);
}
```

The existing `ExecutableState` class (with `EXECUTABLE`, `NOT_EXEC_HIDDEN`,
`NO_EXEC_NO_MODEL`, etc.) is reused as-is — it has no LayoutComponent dependency.

### Standard Implementations

| Rule | Behavior |
|------|----------|
| `NullInputDisabled` | Disables if input is null (replaces `NullModelDisabled`) |
| `NullInputHidden` | Hides if input is null (replaces `NullModelHidden`) |
| `AlwaysExecutable` | Always enabled |

### Rule Combination

Rules are combined via `CombinedViewExecutabilityRule` (short-circuit AND). The
command's intrinsic executability (from its implementation class via an overridable
method) is combined with the configured rules, same as the legacy system.

### Configuration

```xml
<!-- Shorthand: comma-separated rule references -->
<command class="..." executability="NullInputDisabled"/>

<!-- Explicit: polymorphic rule configs -->
<command class="...">
    <executability>
        <rule class="com.example.CustomRule"/>
    </executability>
</command>
```

## 3. Confirmation

```java
/**
 * Computes a confirmation message before command execution.
 *
 * <p>Replaces the legacy {@code CommandConfirmation} which received a
 * {@code LayoutComponent}.</p>
 */
public interface ViewCommandConfirmation {

    interface Config extends PolymorphicConfiguration<ViewCommandConfirmation> {
    }

    /**
     * Compute the confirmation message.
     *
     * @param commandLabel The command's label.
     * @param input        The resolved input value (may be null).
     * @return The confirmation message, or null to skip confirmation.
     */
    ResKey getConfirmation(ResKey commandLabel, Object input);
}
```

### Standard Implementations

| Implementation | Behavior |
|---------------|----------|
| `DefaultViewConfirmation` | "Do you really want to execute '{0}'?" |
| `DefaultDeleteConfirmation` | "Do you really want to delete '{0}'?" (handles collections) |
| `ScriptedViewConfirmation` | TL-Script expression returning ResKey or null |

### Runtime Flow

1. User clicks command button
2. Framework calls `getConfirmation(commandLabel, input)`
3. If non-null: show confirmation dialog
   - OK: proceed to execution
   - Cancel: abort
4. If null: proceed directly to execution

The command handler never sees the confirmation — it's handled by the
`ViewCommandModel` / dispatcher layer.

## 4. Dirty Handling

```java
public enum DirtyCheckScope {
    /** Check forms within the enclosing panel (default). */
    SELF,
    /** Check all forms in the entire view. */
    VIEW,
    /** Skip dirty checking (e.g., for save/cancel commands). */
    NONE
}
```

### Configuration

```xml
<command class="...SaveHandler" check-dirty="none"/>      <!-- Save IS the apply -->
<command class="...DeleteHandler"/>                         <!-- SELF by default -->
<command class="...NavigateHandler" check-dirty="view"/>   <!-- Check whole view -->
```

### Runtime Flow

1. Command triggered
2. Resolve `DirtyCheckScope`:
   - `SELF`: collect `ChangeHandler` instances from the enclosing panel's subtree
   - `VIEW`: collect from the entire view
   - `NONE`: skip
3. If any `ChangeHandler.isChanged() == true`:
   - Show apply/discard/cancel dialog
   - "Apply Changes": call `getApplyClosure()` on each dirty handler, then execute
   - "Discard Changes": call `getDiscardClosure()`, then execute
   - "Cancel": abort
4. If none dirty: execute directly

The `ChangeHandler` interface is reused from the existing system.

## 5. Placement and Cliques

### CommandPlacement

```java
public enum CommandPlacement {
    /** Toolbar above panel content (default for most action commands). */
    TOOLBAR,
    /** Button bar below panel content (default for commit/cancel commands). */
    BUTTON_BAR,
    /** Context menu on data elements (right-click rows). */
    CONTEXT_MENU,
    /** Not displayed automatically; only via explicit <button>. */
    NONE
}
```

Each command implementation defines a default placement. The `placement` attribute
in configuration overrides it.

### Standard Cliques

Cliques are strings (not enum) to support local panel-specific cliques:

```java
public final class CommandCliques {
    public static final String CREATE = "create";
    public static final String EDIT = "edit";
    public static final String DELETE = "delete";
    public static final String COMMIT = "commit";
    public static final String NAVIGATE = "navigate";
    public static final String VIEW = "view";      // menu display
    public static final String EXPORT = "export";   // menu display
    public static final String MORE = "more";       // menu display
}
```

### Clique Display and Ordering

| Clique | Default Display | Order |
|--------|----------------|-------|
| `create` | inline | 1 |
| `edit` | inline | 2 |
| `delete` | inline | 3 |
| `commit` | inline | 4 |
| `navigate` | inline | 5 |
| `view` | menu | 6 |
| `export` | menu | 7 |
| `more` | menu | 8 |

```
Toolbar:  [New] [Import]  |  [Edit]  |  [Delete]  |  [View v]  [Export v]
           --- create ---     - edit -    - delete -    - view -    - export -
```

### Local Cliques

Panels can define local cliques for application-specific grouping:

```xml
<panel toolbar="true">
    <cliques>
        <clique name="workflow" after="edit" display="inline"/>
    </cliques>
    <commands>
        <command class="...ApproveHandler" clique="workflow"/>
        <command class="...RejectHandler" clique="workflow"/>
    </commands>
    ...
</panel>
```

Local cliques use `after` or `before` to position relative to standard cliques.

## 6. ViewCommandModel (Runtime Bridge)

`ViewCommandModel` bridges a stateless `ViewCommand` to a reactive UI button. Created
by the panel at setup time, one per command.

```java
public class ViewCommandModel {

    private final ViewCommand _command;
    private final ViewCommand.Config _config;
    private final ViewChannel _inputChannel;  // Resolved from config.getInput()
    private final List<ViewExecutabilityRule> _rules;
    private final ViewCommandConfirmation _confirmation;

    // Reactive UI state
    private ExecutableState _executableState;

    /**
     * Resolve the current input value from the channel.
     */
    public Object resolveInput() {
        return _inputChannel != null ? _inputChannel.get() : null;
    }

    /**
     * Called when user clicks the button.
     */
    public HandlerResult executeCommand(ViewDisplayContext context) {
        Object input = resolveInput();

        // 1. Check executability
        ExecutableState state = computeExecutableState(input);
        if (!state.isExecutable()) {
            return HandlerResult.DEFAULT_RESULT;
        }

        // 2. Dirty check (delegated to CommandScope)
        // 3. Confirmation dialog
        if (_confirmation != null) {
            ResKey confirmKey = _confirmation.getConfirmation(
                _config.getLabel(), input);
            if (confirmKey != null) {
                // Show dialog, resume on OK
                return showConfirmation(context, confirmKey, input);
            }
        }

        // 4. Execute
        return _command.execute(context, input);
    }

    /**
     * Re-evaluate executability based on current input.
     * Called when the input channel value changes.
     */
    public ExecutableState computeExecutableState(Object input) {
        for (ViewExecutabilityRule rule : _rules) {
            ExecutableState state = rule.isExecutable(input);
            if (!state.isExecutable()) {
                return state;
            }
        }
        return ExecutableState.EXECUTABLE;
    }

    /**
     * Subscribe to input channel changes and push UI updates.
     */
    public void attach() {
        if (_inputChannel != null) {
            _inputChannel.addListener((sender, oldValue, newValue) -> {
                updateExecutableState();
            });
        }
        updateExecutableState();
    }
}
```

## 7. ButtonElement with Inline Commands

`ButtonElement.Config` cannot extend both `UIElement.Config` and `ViewCommand.Config`
(both are `PolymorphicConfiguration` with different type bounds). Instead, the button
has a sub-configuration property for its action:

```java
@TagName("button")
public interface Config extends UIElement.Config {

    @Override
    @ClassDefault(ButtonElement.class)
    Class<? extends UIElement> getImplementationClass();

    /**
     * The command this button executes.
     *
     * <p>Either an inline command definition (any ViewCommand implementation)
     * or a CommandReference pointing to a named panel command.</p>
     */
    @Name("action")
    @Mandatory
    @DefaultContainer
    PolymorphicConfiguration<? extends ViewCommand> getAction();

    /** Button display style. */
    @Name("style")
    ButtonStyle getStyle();

    /** Button size. */
    @Name("size")
    ButtonSize getSize();
}
```

### Two Modes

**Inline command** (common case): The button declares its command directly.

```xml
<button style="primary">
    <command class="com.example.ApproveHandler"
            input="selectedItem" label="Approve"/>
</button>
```

With `@DefaultContainer` on `action`, the `<command>` tag goes directly inside
`<button>` without a wrapper.

**Command reference** (special case): The button references a named command from
the enclosing panel scope.

```xml
<button>
    <command-ref name="approve"/>
</button>
```

`CommandReference` is a ViewCommand implementation with `@TagName("command-ref")`
that delegates to the named command from the enclosing `CommandScope`.

### ButtonElement Implementation

```java
public class ButtonElement implements UIElement {

    private final Config _config;
    private final ViewCommand _command;

    public ButtonElement(InstantiationContext context, Config config) {
        _config = config;
        _command = context.getInstance(config.getAction());
    }

    @Override
    public ViewControl createControl(ViewContext context) {
        ViewCommandModel model = createCommandModel(context);
        model.attach();
        return new ReactButtonControl(model);
    }

    private ViewCommandModel createCommandModel(ViewContext context) {
        // Resolve input channel from command config
        ChannelRef inputRef = _command.getConfig().getInput();
        ViewChannel inputChannel = inputRef != null
            ? context.resolveChannel(inputRef) : null;

        // Instantiate executability rules and confirmation
        // ... create and return ViewCommandModel
    }
}
```

## 8. Command Scopes and Implicit Commands

### CommandScope

A `CommandScope` is the runtime container for commands owned by a panel or dialog.
It holds both explicit commands (from `<commands>`) and implicit commands
(contributed by child elements).

```java
public class CommandScope {

    private final List<ViewCommandModel> _explicitCommands;
    private final List<ViewCommandModel> _implicitCommands;
    private final List<Runnable> _listeners;

    /** Add an implicit command (called by child elements). */
    public void addCommand(ViewCommandModel command) {
        _implicitCommands.add(command);
        fireChanged();
    }

    /** Remove an implicit command (called when child is destroyed). */
    public void removeCommand(ViewCommandModel command) {
        _implicitCommands.remove(command);
        fireChanged();
    }

    /** All commands ordered by clique, explicit first then implicit. */
    public List<ViewCommandModel> getAllCommands() {
        List<ViewCommandModel> all = new ArrayList<>();
        all.addAll(_explicitCommands);
        all.addAll(_implicitCommands);
        all.sort(CliqueComparator.INSTANCE);
        return all;
    }

    /** Listen for changes to the command list (for toolbar re-rendering). */
    public void addListener(Runnable listener) {
        _listeners.add(listener);
    }
}
```

### ViewContext Integration

`ViewContext` carries a reference to the nearest `CommandScope`:

```java
// In ViewContext:
public CommandScope getCommandScope() { ... }
public ViewContext withCommandScope(CommandScope scope) { ... }
```

Panels create a `CommandScope`, register it in a derived `ViewContext`, and pass
it to children. Children call `getCommandScope().addCommand(...)` to contribute
implicit commands.

### Reactive Toolbar Updates

When the command list changes (implicit command added/removed), the panel's toolbar
re-renders via SSE. The `CommandScope` listener notifies the panel control, which
pushes updated toolbar state to the client.

## 9. PanelElement with Commands

The existing `PanelElement` gains a `<commands>` section:

```java
public interface Config extends ContainerElement.Config {

    @Name("title")
    @Nullable
    ResKey getTitle();

    @Name("toolbar")
    boolean getToolbar();

    @Name("button-bar")
    boolean getButtonBar();

    @Name("collapsible")
    boolean getCollapsible();

    /** Local clique definitions. */
    @Name("cliques")
    List<LocalCliqueConfig> getCliques();

    /** Commands scoped to this panel. */
    @Name("commands")
    @DefaultContainer
    List<PolymorphicConfiguration<? extends ViewCommand>> getCommands();
}
```

### Panel Control Creation

During `createControl()`, the panel:

1. Instantiates all configured commands as `ViewCommandModel` instances
2. Creates a `CommandScope` with the explicit commands
3. Creates a derived `ViewContext` with the command scope
4. Creates child controls (passing the derived context)
5. Children may add implicit commands to the scope
6. Renders toolbar/button-bar based on command placements

## 10. Example Usage

### Simple Panel with Commands

```xml
<panel title="Customer Detail" toolbar="true" button-bar="true">
    <commands>
        <command class="com.example.EditHandler"
                 input="selectedCustomer" label="Edit"/>
        <command class="com.example.DeleteHandler"
                 input="selectedCustomer" label="Delete"/>
        <command class="com.example.SaveHandler"
                 input="selectedCustomer" label="Save"
                 check-dirty="none"/>
        <command class="com.example.CancelHandler"
                 label="Cancel" check-dirty="none"/>
    </commands>

    <form model="selectedCustomer">
        <field attribute="name"/>
        <field attribute="email"/>
    </form>
</panel>
```

### Inline Button Command

```xml
<grid min-column-width="16rem">
    <card title="Active Tasks">
        <demo-counter label="Tasks"/>
    </card>
    <button style="primary">
        <command class="com.example.CreateTaskHandler" label="+ New Task"/>
    </button>
</grid>
```

### Panel with Context Menu

```xml
<panel toolbar="true">
    <commands>
        <command class="com.example.CreateHandler" label="New"/>
        <command class="com.example.EditRowHandler"
                 input="selectedCustomer" placement="context-menu"/>
        <command class="com.example.DeleteRowHandler"
                 input="selectedCustomer" placement="context-menu">
            <confirmation class="...DefaultDeleteConfirmation"/>
        </command>
    </commands>

    <table types="Contacts:CompanyContact" selection="selectedCustomer">
        <rows>all(`Contacts:CompanyContact`)</rows>
    </table>
</panel>
```

### Command Reference from Button

```xml
<panel toolbar="true">
    <commands>
        <command name="approve" class="com.example.ApproveHandler"
                 input="selectedItem" label="Approve"/>
    </commands>

    <form model="selectedItem">
        <field attribute="name"/>
        <field attribute="status"/>
        <!-- Also render as inline button -->
        <button style="primary">
            <command-ref name="approve"/>
        </button>
    </form>
</panel>
```

## Execution Flow

```
User clicks button
    |
    v
ViewCommandModel.executeCommand(ViewDisplayContext)
    |
    v
Resolve input: inputChannel.get()
    |
    v
Check executability rules (short-circuit AND)
    |--- not executable --> return (no-op)
    v
Check dirty scope
    |--- dirty forms found --> show apply/discard/cancel dialog
    |                           |-- Apply: apply all, continue
    |                           |-- Discard: discard all, continue
    |                           |-- Cancel: abort
    v
Check confirmation
    |--- confirmation message --> show confirm dialog
    |                              |-- OK: continue
    |                              |-- Cancel: abort
    v
ViewCommand.execute(ViewDisplayContext, input)
    |
    v
HandlerResult
```
