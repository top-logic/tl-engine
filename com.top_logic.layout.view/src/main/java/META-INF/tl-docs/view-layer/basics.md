---
description: Read before writing any .view.xml - when composing existing controls instead of writing React components, choosing panel insets (spacing model, with-inset), making content fill the available height (the fill contract), or styling one element with css-class.
order: 10
---

# Basics: composition, spacing, height and styling

## The `.view.xml` layer is a composition layer, not a place for new React components

`com.top_logic.layout.view` is declarative: a `.view.xml` assembles existing React controls (`TLPanel`, `TLWindow`, `TLForm` / `TLFormField`, `TLTextInput`, `TLButton`, `TLText`, `TLDialog`, …) through `UIElement` configs (each a `@TagName`) and view commands / actions. To build a feature (a login dialog, a user menu, …), compose these via XML plus small Java `ViewCommand` / `ViewAction` / `ViewExecutabilityRule` / `UIElement` classes that reuse existing controls. Do **not** hand-roll bespoke monolithic React components. A new React control is justified only for a genuinely new generic widget (e.g. a `type=password` input), not for assembling forms / buttons that already exist.

- **Forms** bind to a model object: `<form input="ch"><field attribute="x"/>`. A value that belongs to no object is entered with a `<value-input>` (next bullet), not by wrapping it in a transient model type; a field's control is chosen by a `<input-control><impl class="…Provider"/></input-control>` annotation on the attribute, or — where the choice belongs to one place in the user interface rather than to the model — by `<field attribute="x"><input-control class="…Provider" …/></field>` in the view itself (`FieldElement.Config.getInputControl()`), whose `class=` names the same `ReactFieldControlProvider` and carries its configuration.
- **View-owned values: `<value-input value="ch" type="…"/>`** (`ValueInputElement`). A value that belongs to the view rather than to an object - the term a table filters by, the status a list is narrowed to - needs no form and no object: the element binds the channel to an input control in both directions (what the user enters becomes the channel value, a value the channel receives from elsewhere appears in the input).
  - `type` is a `TLModelPartRef` and decides the input; it defaults to `tl.core:String`. The control is resolved by `FieldControlService.createFieldControl(context, type, spec, model)` - the same chain that picks the control for a `<field>` over an attribute of that type, which only adds the lookup of the attribute's own `<input-control>` annotation. So `tl.core:Integer` is a number input, `tl.core:Date` a date picker, `tl.core:Boolean` a checkbox, `tl.core:Text` a text area, and an enumeration or a class a dropdown.
  - A value of an enumeration or a class is chosen from options: without an `options` expression these are the classifiers respectively the instances of the type (`AttributeOptions.optionsFor(type)`). `options` is a TL-Script function whose arguments are the values of the `<inputs><input channel="…"/></inputs>` channels, in declaration order - the same shape as `<execute-script>` - and the options are recomputed whenever one of those channels changes. Every `inputs` property of the view layer - here, a `<table>`'s `<rows>`, a column declaration, an action - reads both notations: the nested `<inputs><input channel="…"/></inputs>` and the comma-separated attribute `inputs="a, b"` (`Inputs`). `multiple="true"` makes the channel carry a collection instead of a single value; a single-valued selection is unwrapped, so the channel holds the value itself and not a list of one.
  - Further properties: `label` (a `ResKey`; without it the input stands alone, without the label chrome), `label-position` and `readonly`.
  - **An input standing without a visible label** - in an app bar, in a toolbar, above a list - says what it is for in two places. `placeholder` (a `ResKey`) is the text shown inside the input while it is empty: "Search" in a search box, `name@example.com` in a mail address; it disappears with the value entered. It is a property of the field description (`FieldSpec.setPlaceholder(…)`) rather than of one control, so every control `FieldControlService` builds from such a description carries it - the text and number inputs render it, a control that has nothing to show an empty box in ignores it. `label-position="hide-label"` then takes the label out of the display but keeps it as the *name* of the input: `TLFormField` keeps the label text in a visually hidden `label` (`tl-visually-hidden`) that refers to the input by `for` and names it through `aria-labelledby`, so the input keeps its accessible name without a visible label. An input that keeps its `label` and hides it is named for a screen reader; one that drops the `label` altogether is not. A group of radio buttons or checkboxes (`TLChoiceGroup`) names each option by a `label` of its own wrapping the input and its text, which makes the text part of the hit area; the field's label, which may not contain those labels, names the group as a whole through `aria-labelledby`.
  - **A search field out of the box.** An input that narrows what a view shows is three properties on top of the submit hook, so an application needs no element of its own: `icon` draws a `ThemeImage` inside the input ahead of what is typed, `clearable="true"` adds the button that empties it (shown only while the input holds something, writing the empty value at once), and `debounce` says how long the input waits after the last keystroke before the typed value reaches the channel, written as a duration (`@Format(MillisFormat.class)`). All three ride on the field description (`FieldSpec.setIcon(…)` / `setClearable(…)` / `setDebounce(…)`) and are applied by `ReactFieldControlProvider.createField(…)`, like the `placeholder`. `TLTextInput` renders icon and clear button - a search field is a text - while `TLNumberInput` and `TLPasswordInput` take only the delay; the icon and the button sit in the same `tl-field-group` as the link that opens a `url` / `email` / `tel` value, in the order `[icon] input [clear] [link]`.
    ```xml
    <view>
      <channels>
        <channel name="q"/>
      </channels>
      <query-bindings>
        <bind
          channel="q"
          query-param="q"
        />
      </query-bindings>
      <value-input
        clearable="true"
        debounce="300ms"
        icon="css:fa-solid fa-magnifying-glass"
        label-position="hide-label"
        type="tl.core:String"
        value="q"
      >
        <label>
          <en>Search</en>
        </label>
        <placeholder>
          <en>Search tickets</en>
        </placeholder>
      </value-input>
    </view>
    ```
    The delay is the span a *typed* value is held back; a value that is picked - a dropdown, a date picker, a checkbox - reaches the channel with the choice and waits for nothing. An input whose value the server rewrites as it stores it (a number, an internationalized text) holds the value back until the input is left (`setSendValueOnBlur(true)`) and ignores the delay altogether - what it costs is server-side feedback while typing, which is the trade that behaviour is for. Without a stated delay a typed input uses the one span every typed input shares, `VALUE_DEBOUNCE_MS` (300 ms) exported from the bridge. The three are rendering-only: `ReactFormFieldControl.scriptingPresentationKeys()` keeps `icon`, `clearable` and `debounceMs` out of the headless projection, while the `placeholder` stays in it, being the text a label-less input names itself by. The three hand-rolled search boxes elsewhere in the layer - the table filter bar, the dropdown search, the icon-select popup - are controls of their own and are unaffected.
  - **Layout: `<fields>`** (`FieldsElement`). A `<value-input>` renders the label-and-input chrome of a field but, standing outside a `<form>`, gets none of the grid a form renders around its fields. `<fields>` is that grid on its own (`ReactFormLayoutControl`, `TLFormLayout`): the auto-fit columns (`max-columns`, 3 by default), and the `FormLayoutContext` a `TLFormField` reads to move a label from beside its input to above it when the column is narrower than 320px (`label-position` `auto` by default, or fixed `side` / `top`; the field-level `after` / `hidden` are rejected). A `<fields>` also stands inside a `<form>`: it lays out the fields of one area of a form whose areas are panels or split panes, and then follows the form's edit mode (the grid is read-only while the form is not being edited, so its fields show the read-only chrome; `FormLayoutEditModeBinding`). A plain `<form>` needs no `<fields>`, being such a grid already; a `<field>` still needs a `<form>`, since `<fields>` carries no object. The grid of a `<form>` or `<fields>` is a plain layout that reaches up to its container border; where it stands and how it keeps its distance is described in "Spacing model" below.
  - **The same grid options on `<form>`** (`FormLayoutOptions`, shared by `FormElement.Config` and `FieldsElement.Config`). A `<form max-columns="1" label-position="side">` states the greatest number of columns its fields are laid out in and where their labels stand, exactly as `<fields>` does — `max-columns` is a ceiling, not a count, so a narrow display still shows fewer columns and a phone a single one. This is what a detail pane narrower than the three columns of the default asks for: left to itself it fills the width it has with two columns whose labels sit above the inputs, while `max-columns="1"` plus `label-position="side"` keeps one column with the labels beside the inputs at every width. A `<field>` stating a `label-position` of its own keeps it.
    ```xml
    <form input="selectedMilestone" label-position="side" max-columns="1">
      <field attribute="name"/>
      <field attribute="dueDate"/>
    </form>
    ```
  - **Sections: `<group>`** (`GroupElement` → `ReactFormGroupControl`, `TLFormGroup`). A group gathers the fields that belong together under a heading of its own — Text, Numbers, References — and takes part in the grid of the enclosing `<form>` or `<fields>` instead of opening a grid of its own, so the fields inside a section line up with the fields outside it, column for column. `<label><en>…</en><de>…</de></label>` is the heading; a group without one is set apart by its frame alone. `border` draws that frame (`none` by default, `subtle` a thin line in the subtle border color, `outlined` one in the strong border color), `collapsible="true"` lets the user fold the section away from its heading and `collapsed="true"` starts it folded. `full-line` (`true` by default) spans the section over the whole width of the grid and distributes its fields over the columns; `full-line="false"` confines it to a single column, where its content stacks one item below the other. A group holds any view content — fields, texts, further groups — so sections nest.
    ```xml
    <group border="subtle" collapsible="true" collapsed="true">
      <label>
        <en>Source code</en>
        <de>Quelltext</de>
      </label>
      <field attribute="xmlSource"/>
      <field attribute="jsonSource"/>
    </group>
    ```
    The demo is the "Edit demo object" dialog of `com.top_logic.demo.react`'s Attributes page (`WEB-INF/views/attributes-detail.view.xml`), whose fifty fields stand in eight sections.
  - **Submit hook**: `<on-submit>` names a `ViewCommand` run over the value the user finishes entering - one input plus one command over what was entered, which is what a search field or a jump-to box is. The value reaches the channel first, then the command as its input, so the command's `<execute-script function="entered -> …">` receives it directly and needs no `input` channel of its own. The property defaults to `GenericViewCommand` (`@ImplementationClassDefault`), so the actions stand inside the element:
    ```xml
    <fields>
      <value-input value="jump">
        <on-submit>
          <execute-script function="name -> all(`demo.tickets:Ticket`).filter(t -> $t.get(`demo.tickets:Ticket#name`) == $name).firstElement()"/>
          <write-channel name="ticket"/>
        </on-submit>
      </value-input>
    </fields>
    ```
    Another command is `<on-submit class="fq.MyCommand" .../>`. What counts as a submit depends on the control: a field the user *types* in (`ReactTextInputControl` single-line, `ReactNumberInputControl`) reports `ReactFormFieldControl.hasSubmitGesture() == true` and submits on **Enter** - the client sends the `submit` command (`FieldSubmitArguments`, carrying the text, so one recorded step both stores and submits it) from the shared `useTLSubmitOnEnter()` bridge hook, enabled by the `submitOnEnter` state the server sets in `setSubmitListener(…)`. A field that is *picked* from - a dropdown, a date picker, a checkbox - has no such gesture, so **every choice is a submit**; there the element follows `ChannelFieldBinding.setCommitListener(…)`, which reports only values the user produced (a value pushed in from the channel is not a commit). A multi-line text area has no submit gesture at all, since Enter is part of the text.
  - **`input-control`** names the control the value is entered in, overriding the one the type leads to - the same property `<field>` carries, resolved by the same chain: `<value-input type="demo:Priority" value="p"><input-control class="com.top_logic.layout.view.form.SelectControlProvider" display="segmented"/></value-input>`. It reaches `FieldControlService.createFieldControl(context, type, spec, model, control)`, whose first step builds the named provider and skips the type-based resolution entirely. See "Display variants of input fields" below.
  - The tag is `value-input`, not `input`: `input` is the name of the channel property ~21 element configs declare, and a content tag that shadows a property name of the same config makes that config invalid outright - "Ambiguous content tag name 'input': May either represent the property getInput(), or a content element of the default container" - which would take out `<form>`, `<anchor>`, `<switch>` and every other element that has both an `input` channel and children.
- **Dialogs** open a `.view.xml` via `<open-dialog dialog-view="…">`; close via `CancelDialogCommand` / `DialogManager.closeTopDialog`. `currentUser()` is a TL-Script function usable in `<derived-channel expr="…">`.
- **Referencing a `UIElement` impl by `class=` in view content.** View content lists resolve entries by `@TagName`, so an app-specific element that should not claim a global tag is placed via the content property's *entry tag* plus `class=`. The `children` content property (`ContainerElement.Config`) is `@EntryTag("child")`, so write `<child class="fq.MyElement"/>` inside a `<panel>` / container. If a cell provider is reusable, make it public rather than justifying a separate element; justify a separate element by genuinely different data / behavior.
- **Standalone form-field controls bind to a `FieldModel`.** For a standalone field control (e.g. a checkbox cell), use the concrete `com.top_logic.layout.form.model.AbstractFieldModel` + `FieldModelListener` — not `FormContext` / `FormField` / `FormFieldAdapter`, which are legacy-compat shims. `AbstractFieldModel` is editable by default, needs no `FormContext` parent, and triggers no label resource lookup in `ReactFormFieldControl`.
- Modifying persistent state from a control's value listener needs a transaction; the listener has no ambient one, so open `beginTransaction()` there (or buffer changes and apply them under one transaction on save).

## Spacing model

Containers are flush: a `<panel>`, a `<tab>`, a `<pane>`, a dialog window and the simple layouts `<stack>` / `<grid>` lay their content out up to their border and add no padding. The content owns its breathing room:

- **Content that fills** - a `<table>`, a `<flow-diagram>`, a `<split-panel>`, a `<tab-bar>` of its own panels, an `<adaptive-detail>`, an `<html display="document">` - stays edge to edge. Nothing is set.
- **Content that flows** - texts, stacks, grids, alerts, buttons, cards, a form that does not span its container - keeps the page inset (`--page-inset`) as distance to the border. Where it stands decides how:
  - directly in a `<panel>`: `with-inset="true"` on the panel (first choice). It insets the panel body, not the title and the toolbar (`PanelElement.Config` extends `InsetOptions`).
  - a `<form>` or `<fields>` that is the content of a dialog, a tab or a pane, or the sole content of a panel: `with-inset="true"` on the form / fields (`FormLayoutOptions` extends `InsetOptions`).
  - any other content not directly in a panel (a tab, a pane, a wizard step, a `<visible-if>` that must not leave an empty padded box behind): wrap it in `<inset>` (`InsetElement`).

All three render the same `ReactInsetControl` (`.tlInset`). Content is inset **once**: a panel with `with-inset` holds no form / fields with `with-inset` and no `<inset>`. A nested `<panel>` is a container of its own and decides for its own body. A card (`appearance="card"`, `<card>`) reduces `--page-inset`, so an inset inside it is compact.

```xml
<panel with-inset="true">
  <title>
    <en>Alerts</en>
  </title>
  <text>
    <label>
      <en>A highlighted message in the content of a view.</en>
    </label>
  </text>
  <alert severity="info">…</alert>
  <form input="obj">
    <field attribute="name"/>
  </form>
</panel>
```

A body mixing both - an explanation above a table - is a judgment call: leave the panel flush to keep the table edge to edge and wrap the text in `<inset>`, or nest the table in a panel of its own.

Checklist: content glued to the border of its container → `with-inset="true"` on the panel, or on the form / fields, or an `<inset>` around it.

## Filling the available height: the fill contract

A control that should span the height its container offers - instead of growing with its content - carries the CSS class `tlFill`, which the layout CSS turns into `flex: 1; min-height: 0`. That bounds the control only while its container has a definite height itself, so the decision is not local: a container hosting a filling child has to fill in turn, up to the first box that is bounded anyway (the viewport-high mount point, a tab-content region, a scrolling panel body). This chain is a property of the path through the control tree, which is why it lives in the React bridge (`react-src/bridge/fill.ts`) rather than in a CSS selector enumerating the child types allowed to grow.

A container component therefore declares how it takes part:

- `useFill(fills)` — a fixed decision by the control's own configuration: `<panel fill="true">` reports filling, a plain panel does not. Returns the class for the root element.
- `useFillHost(alwaysFills?)` — a container: it fills while it always does (a split panel, whose panes are sized proportionally; a tab bar, whose strip stays pinned) or while one of its children reports filling, and reports that on to its own container. Returns the class plus the host to provide to the children (`<FillProvider host={…}>`).
- `<FillBarrier>` — ends the chain: a region bounded on its own that scrolls what does not fit (a panel body, a tab content area), or a surface laid out apart from the page (a dialog, a window, a drawer). A filling control inside resolves its height against that region, and nothing outside reacts to it.

A container that declares nothing is opaque: a filling control inside it does not grow it, so a new container element that lays out vertical space adds its declaration.

**A `<form>` takes part as a fill-following container.** A `<split-panel>` or a `<panel fill="true">` inside a form fills the frame the form sits in, and the form asks its own container for that height in turn; around content of its own size the form is exactly as high as that content. A page-spanning `<form>` around a split panel is therefore the way to get a single Edit/Save/Cancel set for a whole page over one object: the panes are sized proportionally by the frame, not by the fields they hold. While it hosts a filling region the form lays itself out as a column (`.tl-form-layout--fill`). A frameless editable table (`<table row-edit="…">`, `RowSetTableControl`) is such a filling region: it fills the height the form is offered, so the table scrolls internally.

The areas inside such a page-spanning form therefore put their fields in `<fields>` grids: neither the form nor a panel pads its content, so the grid lays out the fields of an area responsively, following the form's edit mode, and sets `with-inset="true"` to keep them off the panel border.

```xml
<form input="selected">
  <split-panel orientation="horizontal"><panes>
    <pane size="50" unit="%"><panel><title>…</title>
      <fields>
        <field attribute="name"/>
        <field attribute="description"/>
      </fields>
    </panel></pane>
    …
  </panes></split-panel>
</form>
```

A `<dashboard>` bounds its tiles instead of following them. Its grid has a definite row unit - `row-height`, a CSS length defaulting to `16rem` - and a tile is as many of those units tall as its `row-span`, plus the gaps between them. Each tile is a barrier: a panel that fills or a table inside it resolves its height against the tile and scrolls there, and the request reaches neither the dashboard nor whatever hosts it. Content that does not fill and is taller than its tile scrolls inside the tile as well.

## Styling a single element: `css-class`

Every element of a view takes a `css-class`, because the property is declared once on
`UIElement.Config` and thereby inherited by every element configuration — a `<stack>`, a `<panel>`,
a `<table>`, a `<button>`, an element an application brings itself. The class is written on the root
element of the control displaying that element, beside the classes the control's kind brings itself:

```xml
<stack css-class="tlDemoHero">
	<text
		css-class="tlDemoHeroTitle"
		label="Welcome"
	/>
</stack>
```

```html
<div class="tlStack tlStack--column tlStack--gap-normal tlDemoHero">
  <span class="tlText tlDemoHeroTitle">Welcome</span>
</div>
```

Several classes are written separated by spaces, as in HTML.

The stylesheet the classes are defined in belongs to the application and is announced through the
`ClientResources` service, next to the application's own bundles:

```xml
<config service-class="com.top_logic.layout.react.resource.ClientResources">
  <instance class="com.top_logic.layout.react.resource.ClientResources">
    <resources>
      <stylesheet name="tl-demo-react-css"
        resource="/style/tl-demo-react.css"
      />
    </resources>
  </instance>
</config>
```

A modifier class of a kind — `tlText--ellipsis`, `tlCard--outlined` — is not the way to reach one
element. Those classes are what a control writes for the display options of its own kind, so writing
one in `css-class` styles that element by a rule the engine owns and may change; and it reaches the
one element only by accident, since the engine writes the same class on every element that carries
that option. A display option that is part of the element is a configuration property of its own —
`<text overflow="ellipsis">` is such a property, not a class — and everything else is an application
class of the application's own naming.

On the server, the class travels as one state key of `ReactControl` (`setCssClass(String)`); on the
client, the component composes its root `className` from it with `rootClassName(state, …)`. Both are
described in [A new `UIElement`](doc:view-layer/new-ui-element), which an element of an application follows.
