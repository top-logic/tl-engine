# FAQ: React view layer (`com.top_logic.layout.view` and the React table)

## The `.view.xml` layer is a composition layer, not a place for new React components

`com.top_logic.layout.view` is declarative: a `.view.xml` assembles existing React controls (`TLPanel`, `TLWindow`, `TLForm` / `TLFormField`, `TLTextInput`, `TLButton`, `TLText`, `TLDialog`, …) through `UIElement` configs (each a `@TagName`) and view commands / actions. To build a feature (a login dialog, a user menu, …), compose these via XML plus small Java `ViewCommand` / `ViewAction` / `ViewExecutabilityRule` / `UIElement` classes that reuse existing controls. Do **not** hand-roll bespoke monolithic React components. A new React control is justified only for a genuinely new generic widget (e.g. a `type=password` input), not for assembling forms / buttons that already exist.

- **Forms** bind to a model object: `<form input="ch"><field attribute="x"/>`. A value that belongs to no object is entered with a `<value-input>` (next bullet), not by wrapping it in a transient model type; a field's control is chosen by a `<input-control><impl class="…Provider"/></input-control>` annotation on the attribute, or — where the choice belongs to one place in the user interface rather than to the model — by `<field attribute="x"><input-control class="…Provider" …/></field>` in the view itself (`FieldElement.Config.getInputControl()`), whose `class=` names the same `ReactFieldControlProvider` and carries its configuration.
- **View-owned values: `<value-input value="ch" type="…"/>`** (`ValueInputElement`). A value that belongs to the view rather than to an object - the term a table filters by, the status a list is narrowed to - needs no form and no object: the element binds the channel to an input control in both directions (what the user enters becomes the channel value, a value the channel receives from elsewhere appears in the input).
  - `type` is a `TLModelPartRef` and decides the input; it defaults to `tl.core:String`. The control is resolved by `FieldControlService.createFieldControl(context, type, spec, model)` - the same chain that picks the control for a `<field>` over an attribute of that type, which only adds the lookup of the attribute's own `<input-control>` annotation. So `tl.core:Integer` is a number input, `tl.core:Date` a date picker, `tl.core:Boolean` a checkbox, `tl.core:Text` a text area, and an enumeration or a class a dropdown.
  - A value of an enumeration or a class is chosen from options: without an `options` expression these are the classifiers respectively the instances of the type (`AttributeOptions.optionsFor(type)`). `options` is a TL-Script function whose arguments are the values of the `<inputs><input channel="…"/></inputs>` channels, in declaration order - the same shape as `<execute-script>` - and the options are recomputed whenever one of those channels changes. Every `inputs` property of the view layer - here, a `<table>`'s `<rows>`, a column declaration, an action - reads both notations: the nested `<inputs><input channel="…"/></inputs>` and the comma-separated attribute `inputs="a, b"` (`Inputs`). `multiple="true"` makes the channel carry a collection instead of a single value; a single-valued selection is unwrapped, so the channel holds the value itself and not a list of one.
  - Further properties: `label` (a `ResKey`; without it the input stands alone, without the label chrome), `label-position` and `readonly`.
  - **An input standing without a visible label** - in an app bar, in a toolbar, above a list - says what it is for in two places. `placeholder` (a `ResKey`) is the text shown inside the input while it is empty: "Search" in a search box, `name@example.com` in a mail address; it disappears with the value entered. It is a property of the field description (`FieldSpec.setPlaceholder(…)`) rather than of one control, so every control `FieldControlService` builds from such a description carries it - the text and number inputs render it, a control that has nothing to show an empty box in ignores it. `label-position="hide-label"` then takes the label out of the display but keeps it as the *name* of the input: the input area is a `label` element holding the label text in a visually hidden span (`.tlVisuallyHidden` in `TLFormField`), so every native input inside takes its accessible name from HTML's implicit label association, and a click anywhere in the area focuses it. An input that keeps its `label` and hides it is named for a screen reader; one that drops the `label` altogether is not. A radio group names its options by `id` / `for` instead (`TLBooleanChoice`), since no label may contain another.
  - **Layout: `<fields>`** (`FieldsElement`). A `<value-input>` renders the label-and-input chrome of a field but, standing outside a `<form>`, gets none of the grid a form renders around its fields. `<fields>` is that grid on its own (`ReactFormLayoutControl`, `TLFormLayout`): the `var(--page-inset)` padding content owns in the spacing model, the auto-fit columns (`max-columns`, 3 by default), and the `FormLayoutContext` a `TLFormField` reads to move a label from beside its input to above it when the column is narrower than 320px (`label-position` `auto` by default, or fixed `side` / `top`; the field-level `after` / `hidden` are rejected). A `<form>` needs no `<fields>`, being such a grid already; a `<field>` still needs a `<form>`, since `<fields>` carries no object.
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
  - The tag is `value-input`, not `input`: `input` is the name of the channel property ~21 element configs declare, and a content tag that shadows a property name of the same config makes that config invalid outright - "Ambiguous content tag name 'input': May either represent the property getInput(), or a content element of the default container" - which would take out `<form>`, `<anchor>`, `<switch>` and every other element that has both an `input` channel and children.
- **Dialogs** open a `.view.xml` via `<open-dialog dialog-view="…">`; close via `CancelDialogCommand` / `DialogManager.closeTopDialog`. `currentUser()` is a TL-Script function usable in `<derived-channel expr="…">`.
- **Referencing a `UIElement` impl by `class=` in view content.** View content lists resolve entries by `@TagName`, so an app-specific element that should not claim a global tag is placed via the content property's *entry tag* plus `class=`. The `children` content property (`ContainerElement.Config`) is `@EntryTag("child")`, so write `<child class="fq.MyElement"/>` inside a `<panel>` / container. If a cell provider is reusable, make it public rather than justifying a separate element; justify a separate element by genuinely different data / behavior.
- **Standalone form-field controls bind to a `FieldModel`.** For a standalone field control (e.g. a checkbox cell), use the concrete `com.top_logic.layout.form.model.AbstractFieldModel` + `FieldModelListener` — not `FormContext` / `FormField` / `FormFieldAdapter`, which are legacy-compat shims. `AbstractFieldModel` is editable by default, needs no `FormContext` parent, and triggers no label resource lookup in `ReactFormFieldControl`.
- Modifying persistent state from a control's value listener needs a transaction; the listener has no ambient one, so open `beginTransaction()` there (or buffer changes and apply them under one transaction on save).

## A command is a chain of actions, and the chain can branch

`<generic-command>` (`GenericViewCommand`) runs the `<execute-script>`, `<store-form-state>`, `<confirm>`, `<notify>`, `<verify-identity>`, `<with-transaction>`, `<open-dialog>`, `<write-channel>`, … actions written inside it as one chain (`ViewActionChain`): each action's result is the next action's input, the first action gets the command's input. An action that has to wait — `<confirm>`, which opens a dialog — suspends the chain and resumes it from the dialog's answer, or aborts it on cancel; `<verify-identity>` (`VerifyIdentityAction`) is the same shape with the answer being the proof that the person at the keyboard still is the holder of the session's own account, given the way the session was established: a session established at an external identity provider re-authenticates there in a second browser window — the provider's answer returns to the authentication servlet, which completes the pending `IdentityVerifications` entry and resumes the chain in the window that asked (a failure of the resumed chain is shown in that window like any other command failure, through `CommandErrors`, and leaves the confirmed identity itself standing) — while every other session is asked for its password, which the account's `AuthenticationDevice` checks. It fails closed: a cancelled prompt, an account that neither a provider nor a device can confirm, or a context without a dialog all abort; an abort skips the remaining actions and runs the compensations the executed actions registered, newest first, as does a failure. A script over the chain's value takes further arguments from channels: `<inputs><input channel="context"/></inputs>` puts those channel values in front of the chain's value (`ActionScript`, shared by every action that takes a script).

`<notify>` (`NotifyAction`) tells the user something from inside the chain. Its `expr` computes the message over the chain's value, with channel values in front of it through `<inputs>`, exactly like every other script of an action; a result of `null` or an empty text is nothing to say, and the chain passes its value on untouched — so the message itself decides whether the user hears anything. `kind="info|warning|error"` (`info` by default) says how serious the notice is, `display="snackbar|dialog"` (`snackbar` by default) where it is read: a snackbar passes by beside the user's work and is shown synchronously, so it may sit inside a `<with-transaction>`, while a dialog is a single-OK message that suspends the chain until it is acknowledged, exactly as a `<confirm>` does, and therefore may not (a chain running headless has no dialog to open and falls back to the snackbar). `stop="true"` ends the chain after the notice: the compensations of the actions before it run, the remaining actions are skipped, and nothing is logged or reported beyond the message — the notice *is* the outcome. That is what separates it from a failure raised by the TL-Script `throw(#('…'@en, '…'@de))` inside an `<execute-script>`, which travels the error path: it is logged and reported through `CommandErrors` like any other failure of the command.

```xml
<execute-script function="name -> all(`demo.tickets:Ticket`).filter(t -> $t.get(`demo.tickets:Ticket#name`) == $name).firstElement()"/>
<if test="t -> $t != null">
  <then>
    <write-channel name="ticket"/>
  </then>
  <else>
    <notify kind="warning" stop="true" expr="term -> x -> #('No ticket {0}.'@en, 'Kein Ticket {0}.'@de).fill($term)">
      <inputs>
        <input channel="jump"/>
      </inputs>
    </notify>
  </else>
</if>
```

Two actions branch the chain by a TL-Script function over its current value. `<if>` decides between two chains; `<switch>` computes a switch value with its `value` function (the chain's own value when no `value` is configured) and gives it to the `<case>`s, each of which either names the value it stands for with `match` or decides with a `test` predicate:

```xml
<if test="ticket -> $ticket != null">
  <then>
    <write-channel name="ticket"/>
  </then>
  <else>
    <execute-script function="x -> new(`demo.tickets:Ticket`, transient: true)"/>
    <open-dialog bind-input-to="model" dialog-view="tickets-create.view.xml"/>
  </else>
</if>

<switch value="t -> $t.get(`demo.tickets:Ticket#status`)">
  <case match="`demo.tickets:TicketStatus#closed`">
    <with-transaction>
      <execute-script function="t -> $t.set(`demo.tickets:Ticket#status`, `demo.tickets:TicketStatus#open`)"/>
    </with-transaction>
  </case>
  <case test="s -> $s == null">
    <confirm expr="x -> #('The ticket has no status.'@en)"/>
  </case>
  <default>
    <with-transaction>
      <execute-script function="t -> $t.set(`demo.tickets:Ticket#status`, `demo.tickets:TicketStatus#closed`)"/>
    </with-transaction>
  </default>
</switch>
```

- The chosen branch runs as a *nested* chain (`ViewActionChain.nest`): it starts with the chain's current value, and the result of its last action becomes the value the enclosing chain continues with. A branch that is not configured — a missing `<else>`, a `<switch>` without a matching case and without a `<default>` — passes the value through unchanged, so a branch never breaks the chain.
- An abort inside a branch aborts the whole command, and a compensation registered inside a branch takes its place in the enclosing chain's unwind: it runs whenever the command is later aborted or fails, before the compensations of the actions preceding the branch. A `<confirm>` therefore works inside a branch exactly as beside it, including the suspension: the branch may resume long after the command returned.
- `<if>` reads its condition in the fuzzy sense of TL-Script, so an object stands for a true condition and nothing (`null`, an empty list, an empty text) for a false one. A `<case match="…">` holds a TL-Script expression without parameters and matches when the switch value equals its value under the TL-Script comparison (`==`), so a classifier is written as `` `module:Enumeration#literal` ``, a text as `'text'` and a number as the number it is. A `<case test="…">` holds a predicate that is called with the switch value and read in the same fuzzy sense as an `<if>` condition; a case configures exactly one of the two. Without a `value` function the switch value is the chain's own value, so `<switch><case test="t -> $t == null">` decides on what the chain carries.
- A command whose chain applies the entered form values is disabled while the form has errors — a branch reports that for the actions of *all* its branches, taken or not, because the button's state cannot depend on the decision.
- **`<executability>` guards the command with rules over its input**: `<visible-if expr="…"/>` hides the command while its predicate does not return `true`; `<disabled-if expr="…"/>` keeps it visible but disabled and takes the reason from its function — no value or `false` means executable, `true` disables it with a generic reason, a resource key or a text disables it with that reason, which the button shows as its tooltip. A rule that inspects objects beyond the input object needs those types in the command's `observed-types`, otherwise their changes do not re-evaluate it.

## Unsaved changes are asked before a channel write, transitively

A form with unsaved input blocks the write of the channel it is bound to: it registers a `ViewChannel.VetoListener` there, and a veto listener answers with the `List<StateHandler>` it objects with, so the dialog asks about every form blocking the change at once (`ViewChannel.dirtyHandlers()` collects the answers, each handler once). A component that writes a channel *from a `ChannelListener` of another one* hands the question on with `VetoForwarder.forward(source, target)` and drops the forwarder in a cleanup action, so the unsaved changes blocking the target are reported when the source is asked — before the source is written. The object list forwards from each of its inputs to the channel holding the draft of the `<new-element>` content, the `<adaptive-detail>` from each `reset-on` master to its selection, the flow diagram from each input to the selection a rebuild drops, and a `<derived-channel>` from its inputs to the derived value a form may be bound to. The `ChannelVetoException` carries a continuation that retries the write of the *source*, so after a discard every listener of the source runs, including the one writing the target. A veto raised from inside a notification is a programming error: the notifying channel already holds its new value, the listeners after the writing one are never told, and the continuation retries the nested write alone.

## Repeating a template over a computed list: `<object-list>`

`<object-list>` (`ObjectListElement`) instantiates its `<item>` content once per object of a list it computes itself, with the object published on a channel (`element-channel`, `element` by default). What the list holds is decided by TL-Script over channel values, not by a containment the element knows about, so the same element serves the comments of a ticket, a catalogue of products and the result of a search.

- **Inputs.** `inputs` names the channels the functions read, either as the comma-separated attribute `inputs="catalogue, term"` or as nested `<inputs><input channel="catalogue"/></inputs>` (`Inputs`, the same notation every `inputs` property of the view layer takes). Their values are the leading positional arguments, in declaration order: `items` is `...inputs -> elements`, `link` and `remove` are `...inputs -> element -> ...`, the element coming last. A list with no inputs is a repeater over a plain query — `items="all(\`test.flowchart:FlowNode\`)"` — and follows the model through its `observed-types`.
- **With and without a container.** A read-only list configures `items` alone. A list that composes objects adds `element-type` plus the `<new-element>` content bound to `new-element-channel`, whose command chain persists the draft with `<link-element>`, and `<remove-element>` inside the `<item>` content detaches one. The composer appears only while *every* input holds a value, because an element is composed to be attached somewhere; `link` is what attaches it, so any containment style works — a composite reference, a back-reference, an association.
- **Arrangement.** `layout="list"` (the default) stacks the elements in a column; `layout="grid"` places as many next to each other as fit, taking the shared grid options `min-column-width` (16rem by default), `max-columns` and `gap` — the same options `<grid>` takes. `gap` applies to either arrangement, the other two to a grid.
- **Item wrapper and staggered entrance.** The client wraps every element in `<div class="tlItem tlObjectList__item" style="--tl-item-index: 0">` carrying the 0-based position (`ObjectListElement.ITEM_CSS_CLASS`). The engine ships the position, not the animation: an application composes a per-item delay from it. The arrangement is what an application's rule addresses next to that class — a grid puts its items into a `.tlGrid`, a list into a `.tlStack` — so an entrance can be given to the cards of a grid while the rows of a list keep appearing at once. Items are reused by key, so an element that stays through a change of the inputs keeps its DOM node and does not animate again — only the ones that appear do.

```xml
<object-list
	gap="default"
	inputs="search"
	items="search -> all(`test.flowchart:FlowNode`).filter(n -> $search.isEmpty() || $n.get(`test.flowchart:FlowNode#name`).stringContains($search))"
	layout="grid"
	max-columns="3"
	min-column-width="16rem"
	observed-types="test.flowchart:FlowNode"
>
	<item>
		<card>
			<text input="element"/>
		</card>
	</item>
	<empty-text>
		<en>No flow node matches the search.</en>
	</empty-text>
</object-list>
```

```css
@keyframes tlDemoRepeaterFadeUp {
	from { opacity: 0; transform: translateY(8px); }
	to { opacity: 1; transform: none; }
}

/* The items of a grid arrangement; a list arranges its items in a `.tlStack` instead. */
.tlGrid > .tlObjectList__item {
	animation: tlDemoRepeaterFadeUp 300ms ease-out both;
	animation-delay: calc(var(--tl-item-index) * 60ms);
}

@media (prefers-reduced-motion: reduce) {
	.tlGrid > .tlObjectList__item {
		animation: none;
	}
}
```

The demo is `com.top_logic.demo.react`'s `WEB-INF/views/demo/repeater-demo.view.xml` with `style/tl-demo-react.css`.

## A page of weighted columns: `<columns>`

`<columns breakpoint="48rem" gap="default">` (`ColumnsElement`) lays a page out in columns of unequal width and reflows it to a single column when the space gets narrow. Each child is a `<column weight="2">` (`ColumnElement`); a column takes a share of the width in proportion to its weight (weight 1 by default), and its own children stand below each other over the full width of the column. Below the breakpoint the columns stack in the order they are written — the main column first, the side column below it.

- **The breakpoint is the width of the element itself**, not the width of the browser window. The same page therefore stacks inside a narrow pane of a wide window exactly as it does on a phone. No measurement is involved: the client gives each column `flex: <weight> 1 calc((<breakpoint> - 100%) * 999)` in a wrapping flex row, so the browser layout decides.
- **The page scrolls, the columns do not.** A column is as tall as its content and is not stretched to the height of a taller neighbour; the layout is as tall as its tallest column. A long main column beside a short side column reads as one page.

Which of the arrangement elements fits:

| Element | Use it for |
| --- | --- |
| `<columns>` | A page of a few columns of *deliberately different* width that folds to one column when narrow. |
| `<grid>` | Many elements built alike, placed in as many equal columns as fit (`min-column-width`, `max-columns`). |
| `<group>` | A section of the fields of a `<form>` or `<fields>`, under a heading and optionally folded away; it keeps the columns of that grid. |
| `<stack direction="row">` | A row of elements that neither grow to a share of the width nor wrap. |
| `<split-panel>` | Panes with splitters the user drags; fills its box, scrolls per pane, and never folds. |
| `<dashboard>` | Tiles of definite row height whose order the user personalizes. |

```xml
<columns
	breakpoint="48rem"
	gap="default"
>
	<column weight="2">
		<card variant="outlined">
			<title>
				<en>Main</en>
			</title>
			<text>
				<label>
					<en>The wide column.</en>
				</label>
			</text>
		</card>
	</column>
	<column>
		<card variant="outlined">
			<title>
				<en>Side</en>
			</title>
			<text>
				<label>
					<en>Half as wide as the main column.</en>
				</label>
			</text>
		</card>
	</column>
</columns>
```

The client classes an application styles against are `.tlColumns`, `.tlColumns--gap-<gap>` and `.tlColumns__column`. The demo is `com.top_logic.demo.react`'s `WEB-INF/views/demo/columns-demo.view.xml`.

## Pictures: `<image>`, `<overlay>`, `<avatar>`

`<image>` (`ImageElement`) shows one picture, and it takes that picture from either of two places.

- **From a channel** (`input`). The channel value is either **picture data** — a `BinaryData` whose content type starts with `image/`, e.g. the binary attribute of a model object or the result of an upload — or a **text naming an address**. Who serves the bytes differs: picture data is served by the control itself through its data endpoint (`ImageSource`, `hasData` plus a `dataRevision` the client appends so a replaced picture is not taken from the browser cache), while an address is loaded by the browser directly. Any other value — no value, binary data that is no picture, an unrelated object — shows no picture.
- **From a resource of the web application** (`resource`, e.g. `/images/logo.svg`, resolved against the context path). On its own it *is* the picture; together with `input` it is the placeholder shown as long as the channel holds no picture.

The box the picture is shown in is described by `aspect-ratio` (`16/9`, so a row of pictures of differing originals stays even), `width` and `height` (CSS lengths); with none of them the box takes the size of the picture, limited to the width available. `fit` decides what a picture whose proportions differ from the box does with it: `cover` (the default) crops it to fill the box, `contain` fits the whole picture into it. `lazy="true"` lets the browser postpone the loading until the box comes close to the visible part of the page — right for the thumbnails of a long card grid, wrong for a picture the user sees at once. `alt` says what the picture shows for a reader who cannot see it, and `css-class` adds a class to the box.

`<overlay>` stacks content over a base: its **first child is the base**, every further child is a layer over it. The base gives the overlay its height; its width is what the surrounding layout grants, and a base sized relative to it (`width="100%"`) fills it. A base of fixed width wants a container that does not stretch its items (`<stack align="start">`), or the overlay is stretched past the base and anchors its layers to the free space beside it. A `<layer position="fill|top-left|top|top-right|left|center|right|bottom-left|bottom|bottom-right" css-class="…">` brings the position its content takes and a class of its own; a child written without a layer covers the base as a whole. A layer passes the pointer through wherever it shows nothing (`.tlOverlay__layer` is `pointer-events: none`, its content `auto`), so the base stays usable below the free space of a layer that only anchors a badge. Placement comes from the element, the look from application CSS on the layer's class — a badge pill, a caption scrim.

`<avatar input="ch" image="photoCh" size="small|default|large|x-large"/>` shows the picture of the `image` channel circle-cropped, and the initials of the `input` value's label over a color derived from it while there is none. The picture follows its channel, so a photo replaced elsewhere appears without the avatar being built anew.

```xml
<overlay>
	<image
		aspect-ratio="4/3"
		fit="cover"
		input="photo"
		resource="/images/no-picture.svg"
		width="20rem"
	/>
	<layer
		css-class="tlDemoBadge"
		position="top-left"
	>
		<text>
			<label>
				<en>Preview</en>
			</label>
		</text>
	</layer>
	<layer
		css-class="tlDemoCaption"
		position="bottom"
	>
		<text input="node"/>
	</layer>
</overlay>
```

The client classes an application styles against are `.tlImage` / `.tlImage__image`, `.tlOverlay` / `.tlOverlay__layer` / `.tlOverlay__layer--<anchor>` and `.tlAvatar--<size>` / `.tlAvatar__image`. The demo is `com.top_logic.demo.react`'s `WEB-INF/views/demo/image-demo.view.xml` with `style/tl-demo-react.css`.

## Content shown under one condition: `<visible-if>`

`<visible-if input="ch" expr="x -> …">` (`VisibleIfElement`) shows its children while a TL-Script predicate over the value of the `input` channel holds, and nothing while it does not. It is the short form of a `<switch>` with a single case and no default, and it is that literally: the element builds the `ReactSwitchControl` of such a switch, so the re-evaluation, the observation and the disposal are the ones of a `<switch>`. Several alternatives of which one is shown at a time stay a `<switch>`.

- The condition is re-evaluated on a new channel value and on a change of the object the channel holds - a condition usually decides by an attribute of that object, which is edited without the channel value changing. A condition reaching beyond the input object (deciding by an attribute of its container, say) names the types it navigates to in `observed-types`, since a change of another object is invisible to the input's own observation.
- Content that is hidden is disposed rather than kept alive, so it leaves no contributions - a form's edit / save / cancel commands, a slot contribution - behind in the enclosing scope. While the condition keeps holding, the content stays as it is and follows its own channels.
- Not to be confused with the `<visible-if>` inside a command's `<executability>` (`com.top_logic.layout.view.command.VisibleIf`), which carries the same tag name and decides whether a *command* is offered for its input.

**Conditional content takes the size of what it shows.** A `<switch>` and a `<visible-if>` render through the deck pane (`TLDeckPane`, `.tlDeckPane`), a column flex box that states no size of its own: a deck showing content of its own size is exactly as wide and as high as that content, so conditional content stands inline - in an app bar, beside a breadcrumb, in a row of buttons - instead of claiming the box around it. A deck whose content fills takes part in the fill contract as a container (see below) and is then the bounded box the child's height resolves against.

The app bar of `com.top_logic.demo.react` shows both this and the two ways an unlabelled input says what it is for. `WEB-INF/views/tickets.view.xml` raises its jump-to box into the shell's app bar with `<slot-content to="appbar-content">` - the contribution belongs to the view that owns the `ticket` channel, and the shell knows nothing of it - and puts the name of the selected ticket beside it, shown only while a ticket is selected:

```xml
<slot-content to="appbar-content">
	<stack
		align="center"
		direction="row"
		gap="compact"
	>
		<value-input
			label-position="hide-label"
			value="jump"
		>
			<label>
				<en>Jump to ticket</en>
			</label>
			<placeholder>
				<en>Jump to ticket</en>
			</placeholder>
			<on-submit>
				<execute-script function="name -> all(`demo.tickets:Ticket`).filter(t -> $t.get(`demo.tickets:Ticket#name`) == $name).firstElement()"/>
				<write-channel name="ticket"/>
			</on-submit>
		</value-input>
		<visible-if
			expr="t -> $t != null"
			input="ticket"
		>
			<text
				input="ticket"
				overflow="ellipsis"
			/>
		</visible-if>
	</stack>
</slot-content>
```

## Ready-made HTML: `<html>`

`<html input="ch">` (`HtmlElement`) displays HTML the application did not compose itself — the answer of an agent, a generated exposé, an imported page. The channel carries that content in one of three shapes, and `HtmlValues` reduces all of them to the source text to display:

- a `String` holding the source,
- an `HTMLFragment` — what an HTML literal `{{{ … }}}` of a script expression evaluates to — rendered to its source,
- a `BinaryData` of content type `text/html`, read with the charset that content type declares. `binary('expose.html', $source, 'text/html')` builds one in TL-Script, and an uploaded file arrives as one anyway.

A value of any other type has no HTML representation; the element reports that in its place, the same way it reports content a check refuses. Nothing on the channel is no content and no failure.

`display` decides how the content is shown, and with it what stands between it and the reader.

**`inline`** (the default) inserts the fragment into the page where the element stands, after `SafeHTML` has checked it against the application's whitelist — a script, an attribute carrying one, anything else the check refuses is not inserted, and the message of the check takes its place. The fragment brings its structure and the page gives it typography: the stylesheet gives `.tlHtml--inline` the theme's text color and font and spaces the elements a fragment is made of, so an answer reads as a section of the page it lands in. `css-class` adds a class of the application's own beside it.

```xml
<html input="answer"/>
```

**`document`** shows the content as a page of its own, in a sandboxed frame filling the space the element is given. The frame is not handed the source: it fetches it from the control's data endpoint (`ReactHtmlControl` is the `DataProvider`), with the current `dataRevision` in its URL, so the document crosses the wire once and a replaced one is fetched rather than taken from the browser cache. The sandbox runs no script, which is why a document needs no whitelist check — it keeps the styles it brings along and takes none of the page's. `print="true"` puts a button on it that hands the frame to the browser's print dialog, which is also where the browser offers saving the document as a PDF file, with the document's own styles.

```xml
<html
	display="document"
	input="current"
	print="true"
/>
```

**`thumbnail`** shows that same isolated document as a picture of itself: a card-sized preview for a grid of documents. The frame is laid out at `thumbnail-width` × `thumbnail-height` CSS pixels — 800 × 1130 by default, a portrait page at that width — and scaled down to the width the preview box measures (a `ResizeObserver` on the box, `transform: scale(…)` from its top left corner), so the document appears in its own proportions instead of being reflowed into a small one. The box keeps the aspect ratio of the two sizes and cuts off what is taller, as a page does. A preview is looked at rather than used: it takes no clicks, no focus and no print button, and it is fetched only once it comes near the viewport, so a grid loads the previews the reader actually reaches. Selecting one is therefore the business of whatever carries it — a button in the card writing the element to a channel, for instance.

```xml
<object-list inputs="exposes" items="exposes -> $exposes" layout="grid" max-columns="3">
	<item>
		<card padding="compact">
			<html display="thumbnail" input="element"/>
			<text css-class="tlText--strong" input="element"/>
			<button appearance="link">
				<action class="com.top_logic.layout.view.command.GenericViewCommand" input="element">
					<label><en>Show</en></label>
					<write-channel name="expose"/>
				</action>
			</button>
		</card>
	</item>
</object-list>
```

The demo is `com.top_logic.demo.react`'s `WEB-INF/views/demo/html-demo.view.xml`: an inline agent answer and a refused fragment above, the three exposés as a grid of previews beside the selected one below.

**PDF, server-side.** The print dialog is the reader's way to a PDF. An application that produces the file itself — to store it, mail it, attach it — converts the HTML in TL-Script instead: `pdfFile($html, name: "expose.pdf")` yields a `BinaryData` that `<pdf input="ch"/>` displays and a download hands out. That conversion is Flying Saucer rather than a browser, so it takes well-formed XHTML and CSS 2.1 and renders a document written for it, not any page a browser shows.

## `TableViewControl` is the sole React table control

`TableViewControl` / `com.top_logic.table.TableView` (#29108) is the only React table control. Everything renders through this stack: the `<table>` element (`TableElement`; sort, per-column `<filter>`, type-derived default columns, width personalization, shared `ColumnsConfig` / `ColumnConfig`), the access-control permission matrix (`SecurityMatrixElement`), the in-form `<composition-table>` (`CompositionTableControl`), and the technical React-table demo (`DemoReactTableComponent`: flat `ListRowSource` + `TreeRowSource` tree).

- Each `TableViewControl` cell is a registered child control, so interactive cell controls work. Cell rendering (`CellContentReactAdapter`): `CellContent.Text` / `Labeled` → text; `CellContent.Editable` backed by a *boolean* `FieldModel` → interactive `ReactCheckboxControl` (other field types read-only); **`CellContent.Raw` whose payload is a `CellControlFactory` → whatever control the factory builds** — the escape hatch for bespoke cells. The composition table uses it to put typed field inputs (`FieldControlService.createFieldControl(part, fieldModel)`) and detail / delete action buttons in cells; a column's `value(R)=row` plus a renderer returning `Raw(ctx -> buildControl(ctx, row, …))` gives the factory the row.
- **`TLPanel` renders a single `toolbar` child control (a `ReactToolbarControl`), not a `toolbarButtons` list.** Push a panel toolbar via `putState("toolbar", new ReactToolbarControl(ctx))` + `addGroup(name, ToolbarGroupDisplay.INLINE, …, List.of(button))`.
- **A column's display width**: `<column width="220"/>` on a `<table>` or a `<composition-table>` is the width in pixels a column is shown with until the user resizes it, from then on their own width is remembered; `0` (the default) keeps the width the column's type derives (`ColumnProviderService` config: boolean 80, number 100, date 110, time 90, date-time 160, enumeration 120, string and everything else 150, each retunable per application). The configured width is applied uniformly after the `ColumnBinding` built the column — `ColumnSetup.buildColumn()` wraps it through `DelegatingColumn.withDefaultWidth(…)`, the general `Column` decorator for overriding single aspects of a column — so no binding knows about widths.
- Mutable rows: `ListRowSource.setElements(list)` then `TableViewControl.refreshData()`. `DefaultTableView.create(columns, source[, ViewStateStore, TableId])` — the 2-arg form skips personalization; pass a stable `TableId` to persist column width / order.

### Column declarations

The `<columns>` of a `<table>` (and of a `<composition-table>`) is a list of column declarations (`ColumnsConfig` / `ColumnDeclaration` in `com.top_logic.layout.view.table`), tag-dispatched and resolved in display order. A row is any object a TL-Script function yields — a model object, a transient one, a dictionary — so a declaration says what a cell holds rather than which attribute it reads. Four kinds:

- **`<column attribute="title"/>`** (`AttributeColumn`) — the attribute decides everything: display, sort, filter, search, width, header, and what an edited cell writes.
- **`<computed-column name="total" type="tl.core:Double">`** (`ComputedColumn`) with `<value>row -> …</value>` — a value computed per row. The declared `type` (plus `multiple`) gives the column the same type-derived display, sort, filter and width an attribute column gets; a column declaring no type shows its values by their display label. `<update>row -> value -> …</update>` makes the column editable and `<can-update>row -> …</can-update>` decides per row whether the edit is offered; an edited column must declare its type, because the type picks the input control. `<aggregate>rows -> …</aggregate>` is what the column shows in a group header row.
- **`<embedded-columns reference="assignee">`** (`EmbeddedColumns`) — the columns of an object the row points to, shown as columns of the row. `reference` is a reference name or a dotted path (`milestone.tickets`), and a step holding several objects makes every embedded column collect the values of all of them; alternatively `<object>row -> …</object>` plus `name`, `type` and `<label>` computes the embedded object. Nested declarations are of any kind and resolve against the embedded type; an embedding declaring nothing shows that type's main properties. Names are prefixed with the path (`assignee.name`) and labels with the reference labels (`Assignee / Name`, `I18NConstants.EMBEDDED_COLUMN_LABEL__PREFIX_COLUMN`), so a column of the row and one of the embedded object stay apart in the header and in the column selection. Embedded columns are read-only — an embedded object is changed where that object itself is edited.
- **`<dynamic-columns name="milestones" type="tl.core:Integer">`** (`DynamicColumns`) — one column per element of the set `<columns>…</columns>` computes, for a table of tasks with a column per milestone. `<value>row -> col -> …</value>` fills a cell from the row and the object its column stands for; `<column-name>` and `<column-label>` name and label a column (default: the object's display label), `<column-type>` computes the type per column where `type` does not fit all of them, and `<update>` / `<can-update>` / `<aggregate>` take the column object as an extra argument. A column's name is the declaration name, a dot and the column object's name, so a user's arrangement of it survives; the filter is the one the type derives, as there is no single column to declare one for. The columns are those the function yields when the table is built.

Further rules that hold across the kinds:

- **Input channels lead.** Every declaration may carry `<inputs><input channel="…"/></inputs>`; the channel values become the leading positional arguments of *all* of its functions, ahead of the row — `<value>factor -> row -> …</value>`. A `<computed-column>`'s `<aggregate>` is the exception: it takes the group's rows alone.
- **Default columns.** A table without `<columns>` shows the main properties of its row type (`<main-properties properties="name, dueDate"/>` on the type), and all non-hidden attributes of that type when it names none.
- **Offered columns.** Whatever the declared columns do not cover, the table offers in its column selection, hidden until the user picks it — the remaining attributes of the configured `types`, and the remaining attributes of an embedded type under their prefixed labels. Only an explicitly configured `types` produces this offering; a type guessed from the first row would vary with the data.
- **A row need not be a model object.** A table whose rows are dictionaries declares no `types` at all and computes every column (`$row["name"]`); transient objects are displayed by the same attribute columns persistent ones are, and are skipped when the table observes its row types for changes.
- `com.top_logic.demo.react` shows all of this under the *Table columns* nav item (`WEB-INF/views/demo/table-columns.view.xml`), one tab per kind.

## Colored values

A value's color is part of the model: two annotations say where it comes from, and one seam answers it.

- **`<color>`** on an enumeration literal (`TLColor`) gives that literal its color, stated either as a fixed color (`<color value="#04a38d"/>`) or as the name of a UI-theme design token (`<color token="support-success"/>`), which follows the theme the user has active. A literal without the annotation has no color.
- **`<dynamic-color>`** on a type (`TLDynamicColor`) holds the `ValueColorProvider` computing the color of its instances, in the shape `<dynamic-icon>` and `<label>` use for icons and labels. `ColorByExpression` states that algorithm as a TL-Script expression over the object: the demo ticket takes its color from its status, `<dynamic-color><color-by-expression color="t -> $t.get(`demo.tickets:Ticket#status`)"/></dynamic-color>`, so a ticket appears in the color of the status literal it holds. A color value as the result is the color itself, an enumeration literal is colored by its own `<color>`, any other result and `null` leave the object uncolored. Specializations inherit the annotation.
- **`AnnotationValueColorProvider.INSTANCE`** (a `ValueColorProvider`) answers both: `colorOf(value)` returns the `ValueColor` of a classifier or an object, the color a color value itself is, and `null` for everything the model gives no color to. `ValueColor.cssValue()` is the CSS to apply it with — the fixed color, or `var(--<token>)` against the custom properties `UIThemeService` emits per theme.

A colored value is displayed as a **pill** in its color, an uncolored one as plain text. The color travels as the single state / descriptor field `ReactValueColor.COLOR`, filled by `ReactValueColor.putColor(descriptor, value)` / `cssColorOf(value)`, and the client hands it to the stylesheet as the inline custom property `--tlPill-color` — there is no class per color. One shared presentational component `TLPill` (`react-src/controls/pill/TLPill.tsx`, `.tlPill` in `tlReactControls.css`) draws it everywhere; the tint is composed with `color-mix()` from the color and the `color-surface` / `text-primary` tokens, so one declaration stays legible on a light and a dark theme.

The sites that fill the field:

- **`ReactDropdownSelectControl`** — every option and every selected value passes through its one descriptor factory, so the pill appears in the read-only display of a reference or enumeration attribute (a `<table>` cell, a view-mode `<form>` field), on the chips of the selection while editing, and on the rows of the open dropdown.
- **`ReactResourceCellControl`** — resource cells and tree nodes, beside the type icon.
- **`ReactTextControl`** / `<text>` (`TextElement`) — a channel value rendered through `MetaLabelProvider`; `setText(text, cssColor)` updates both in one patch.

## Progress

A fraction between 0 and 1 is displayed as a bar with an optional label beside it. `ReactProgressControl` (`TLProgress`) holds the two state entries `FRACTION` and `LABEL` and nothing else - what the fraction counts is the caller's business. A number outside the range is drawn at the end it exceeds, so two counts that disagree give a full or an empty bar rather than one running past its track; `setProgress(fraction, label)` updates both in one patch.

`<progress>` (`ProgressElement`) states the bar one of two ways, never both:

- `<progress input="ch" fraction="x -> …"/>` — the filled part directly. Such a bar carries no label unless `label="x -> …"` gives it one.
  A fraction expression that answers nothing at all leaves the bar without a share: the client sweeps a partial fill over the track (`tlProgress--indeterminate`) instead of filling a share of it, which is how a bar over an operation that does not know how far it has come is written — and an operation that learns its share later switches between the two displays by reporting a number again.
- `<progress input="ch" done="x -> …" total="x -> …"/>` — the two counts the fraction is the ratio of, which are also the label (`3 / 7`) unless `label=` replaces it. A total of zero leaves the bar empty.

Every expression is called with the current value of the `input` channel, which is optional: a bar counting the model as a whole needs none. The bar recomputes on a new channel value, on a change of the object the channel holds, and on a create / change / delete of an `observed-types` type — the last is what a bar counting all objects of a type needs, since no channel value changes when one is added. The observation is the shared `ChannelObjectObserver`, attached and detached with the control.

A table cell needs nothing new: a `CellRenderer` yields `new CellContent.Raw((CellControlFactory) ctx -> new ReactProgressControl(ctx, fraction, label))`, the escape hatch `CellContentReactAdapter` already resolves.

## Long-running jobs: `<start-job>` and `<job-status>`

Work that takes longer than a request may take does not belong in the request. `<start-job>` (`StartJobAction`) is the action that hands it to a worker thread, publishes what it reports on a channel, and **suspends the command** until the work has ended — the same suspension a `<confirm>` uses, so the chain simply continues afterwards:

```xml
<action class="com.top_logic.layout.view.command.GenericViewCommand" input="job">
  <executability>
    <disabled-if expr="s -> if(jobIsRunning($s), #('A job is already running.'@en), null)"/>
  </executability>
  <start-job job="job" cancelable="true" update-interval="200">
    <phases>
      <phase name="read"><label><en>Reading</en><de>Lesen</de></label></phase>
      <phase name="check"><label><en>Checking</en><de>Prüfen</de></label></phase>
    </phases>
    <function><![CDATA[job -> x -> {
	$job.jobPhase('read');
	$job.jobMessage(#('Reading the records.'@en));
	count(1, 6).foreach(i -> { sleep(400); $job.jobProgress($i, 5); });
	$job.jobPhase('check');
	$job.jobIndeterminate();
	sleep(1500);
	#('5 records processed.'@en);
}]]></function>
  </start-job>
  <write-channel name="report"/>
</action>
```

- **The channel carries immutable snapshots.** The `job` channel holds a `JobState` from the moment the job starts, and a *new* one on every report, so nothing a display would have to observe ever changes. `update-interval` is the shortest time in milliseconds between two published snapshots: a job counting thousands of items is followed at that pace instead of flooding the browser, the last report of a burst is never lost, and the snapshot that ends the job is always delivered. The body runs in the sub-session of the starting request and publishes under the window's interaction, so the channel write, the controls updating from it and the updates reaching the browser are serialized against the requests of the same session exactly like a command is.
- **The work runs outside any transaction, on a thread that serves no request.** Persisting what the job produced is the business of the actions *after* it: the command continues where it left off with the job's result as its value, so a `<with-transaction><execute-script .../></with-transaction>` or a `<write-channel>` behind the `<start-job>` is where the result lands. A job that fails or is cancelled **aborts** the command instead — the remaining actions are skipped, the compensations of the ones before it run, and the failure stays visible in the last state of the job rather than in a snackbar.
- **The body is a TL-Script `function=` or a Java `<body class="…"/>`**, exactly one of the two. The function is called with the **monitor of the job as its first argument**, followed by the `inputs` channel values in declaration order and the command's own value last. It reports with `$job.jobPhase('name')` (entering a step marks the steps passed over as done; naming a step that was never declared ends the job with an error), `$job.jobPhases([…])` or `$job.jobPhases({name: label})` for a job that learns its steps only while running, `$job.jobProgress(done, total)`, `$job.jobIndeterminate()` and `$job.jobMessage(text)`. What the function returns is the result of the job.
- **Reading a snapshot** is `jobIsRunning($s)`, `jobIsFinished($s)`, `jobStatus($s)` (the texts `running`, `completed`, `failed`, `cancelled`, so a `<switch><case match="'completed'">` decides on it), `jobResult($s)` and `jobError($s)`. Each of them answers over no job at all as well, which is what the channel holds before the first start — so a start button guards itself with `input="job"` plus `<disabled-if expr="s -> jobIsRunning($s)"/>` and needs no case of its own for the time before the first run.
- **Cancellation is cooperative.** `cancelable="true"` offers the reader a cancel button; pressing it marks the job and interrupts the worker. `sleep()` keeps the interrupt it was woken by, so a sleeping job wakes at once and ends at the next point it *reports* from — which is what makes a loop of `sleep` + `jobProgress` stop within one step. Every report a Java body makes on its `JobMonitor` checks the same way, and `JobMonitor.checkCancelled()` is that check on its own for a stretch of work that reports nothing. Only declare it for work that may be given up half-done: a cancelled job has done part of what it was started for.
- **`<job-status input="job"/>`** (`JobStatusElement` → `ReactJobStatusControl` / `TLJobStatus`) is the display, bound to the channel alone and holding no state of its own. It shows the status, the declared steps as done / active / pending, the bar (determinate or indeterminate), the message, the elapsed time — counted in the browser, so it ticks without a server round trip and freezes when the job ends — and at the end the result or the error. A channel holding anything that is not a job state displays nothing. Every text is resolved for the reader on the server: the phases and the message by their `ResKey`, the result through `MetaLabelProvider`, so a body returning an i18n literal `#('…'@en, '…'@de)` is displayed in the reader's language.
- **CSS hooks**: the BEM block `tlJobStatus` with the status modifier `tlJobStatus--running|completed|failed|cancelled` and the elements `__header`, `__state`, `__elapsed`, `__cancel`, `__phases`, `__phase` (`--done`, `--active`, `--pending`), `__bar`, `__message`, `__error`, `__result` (`tlReactControls.css`). An application restyles the display through these classes; the bar inside it is the shared `tlProgress` block.
- **Demo**: `com.top_logic.demo.react/…/views/demo/long-job-demo.view.xml` — a three-phase job with a determinate loop, an indeterminate phase and a result written to a second channel, a failing job, and a standalone indeterminate `<progress>`.

## Drag and drop of table rows

A `<table>` declares that its rows may be dragged, and what it accepts a drop of. Both are declarations of the table, so a drag between two tables needs no code on either side:

```xml
<table rows="…" selection="ticket" types="demo.tickets:Ticket">
    <drag/>
    <drop accept="demo.tickets:Ticket">
        <with-transaction>
            <execute-script function="tickets -> $tickets.foreach(t -> $t.set(`demo.tickets:Ticket#status`, `demo.tickets:TicketStatus#closed`))"/>
        </with-transaction>
    </drop>
</table>
<table rows="all(`tl.accounts:Person`)" types="tl.accounts:Person">
    <drop accept="demo.tickets:Ticket" target="row" target-channel="dropPerson">
        <with-transaction>
            <execute-script function="person -> tickets -> …">
                <inputs><input channel="dropPerson"/></inputs>
            </execute-script>
        </with-transaction>
    </drop>
</table>
```

- **`<drag/>`** makes the rows draggable. Dragging a selected row drags the whole selection, an unselected row drags itself — and the selection is read on the server, so a selection reaching beyond the rendered row window is dragged completely. The rows are announced under a type tag: `type` when the drag declares one, otherwise the table's first `types` entry. A table declaring neither is a configuration error — nothing would say what its rows are.
- **`<drop>`** is a list, so a table can accept several kinds of object, and accept one kind on its rows and another as a whole. `accept` names the types (a subtype of an accepted type is accepted as well); `target` is `table` (default) or `row`; `target-channel` is written with the row dropped on — `null` for a table drop — *before* the actions run, which is how the chain reads what was dropped on. The element content is the action chain, declared exactly as a `<generic-command>` declares its actions, and its first action receives the **list of dropped objects** as its input. Nothing about a drop is implicit: a drop that changes persistent state wraps its script in `<with-transaction>`, as any other command does.
- **Which drop applies**: the first declared one that accepts the drag. A drop made on a row is offered to the `row` drops first and falls back to a `table` drop when none of them accepts it, so a table whose rows are targets for one kind of object still accepts another kind wherever the pointer was.

**Acceptance is decided twice, on purpose.** The server expands each accepted type to its own qualified name plus those of all its subtypes and sends that set of tags to the client. While a drag moves, the client compares the drag's tag against those tags alone — no round trip — and highlights the table, or the row under the pointer for a table whose rows are targets. When the drop arrives, the server matches it again, per declared drop, and the receiving table refuses a drop of a tag it never offered: the client-side check narrows the gesture for the user, it does not decide it. The dragged objects are resolved by the control the drag started in, from its own row keys, so no wire value can designate an object neither table displays.

**Recording**: a drop is recorded as a `dropObjects` step naming the dragged objects and the target row by their business identity, so it replays after sorting, filtering and in a fresh session. A replayed drop names no source control — it names the objects instead — and is matched by their type.

`<drag>` and `<drop>` apply to the read-only table. A table in edit mode (`row-edit`) renders through `RowSetTableControl`, a control of its own that carries no drag-and-drop seam, so declaring either there is reported as a configuration error.

## Selecting rows and nodes

A `<table>` and a `<tree>` write what the user selects to the channel named by `selection`, and `selection-mode` says how much may be selected at a time — `single` (the default) or `multi`:

```xml
<table selection="selected" selection-mode="multi" types="demo.react:Demo">…</table>
<tree selection="selected" selection-mode="multi">…</tree>
```

- **A `single` table** replaces the selection with every click, and a click with `Ctrl` on the selected row gives it up again. **A `multi` table** puts a checkbox in front of every row and one in the header selecting and deselecting all of them; a click with `Ctrl` adds a row to the selection or takes it out again, a click with `Shift` selects the range from the row selected last, and `Ctrl+A` selects every row.
- **In a `multi` tree** a plain click still replaces the selection, a click with `Ctrl` adds a node or takes it out again, and a click with `Shift` selects the range from the node the selection started at; the keyboard gestures are described under [Row activation](#row-activation), where the cursor is.
- **The channel holds the selection, never a wrapper around it**: the selected object while exactly one row or node is selected, the `Set` of the selected objects while there are several, and `null` while there is none. A display or a command bound to the channel therefore works with either mode, and only one that is to show or process several objects at once has to expect a set. A tree writes the *business objects* of the selected nodes, not the nodes (`TreeSelectionBinding`).
- **A table also reads its channel** (`TableSelectionBinding`), and how it answers a collection depends on its mode: a `multi` table selects the rows it has for those objects, a `single` table cannot display such a value at all and shows no selection. Either way a value the table has no row for is "nothing selected here" and is **left alone** — clearing it would destroy what another writer put there, the row a second table over a different row set selected or the object a create command wrote before this table's rows caught up. **A tree does not read the channel**: the binding writes it.
- **A command working on one object binds to a derived channel rather than to the selection**, since the selection may be several: `<derived-channel name="selectedSingle" inputs="selected" expr="sel -> if($sel.size() == 1, $sel.singleElement(), null)"/>` is the selection while it consists of exactly one object and nothing otherwise — `size()` counts nothing as zero, a single object as one and a set as the number of its elements. With `<null-input-disabled/>` that is the whole of "enabled for one selected object". A command working on the whole selection needs nothing: `delete()` and the other collection-valued script functions accept a single object as well as a set of them.
- Demos in `com.top_logic.demo.react`: the *Attributes* table (`views/attributes.view.xml`) selects several rows — Delete works on all of them, Edit on the single selection through such a derived channel — and *Tree Demo* (`views/demo/tree-demo.view.xml`) shows the selected nodes and the activated one side by side.

## Row activation

A `<table>` and a `<tree>` open a row / node on a **double-click**, and on **Enter** while the row or node carries the keyboard cursor. The gesture is one command, `activate`, carrying the row index (the node id):

```xml
<table selection="selected" types="demo.react:Demo">
	<columns>…</columns>
	<rows>all(`demo.react:Demo`)</rows>
	<on-activate class="com.top_logic.layout.view.command.GenericViewCommand">
		<open-dialog bind-input-to="model" dialog-view="attributes-detail.view.xml"/>
	</on-activate>
</table>
```

- `<on-activate>` holds an ordinary `ViewCommand` configuration — the same thing `<button><action>` holds, hence the `class=` form. The server selects the activated row first (so the table's `selection` channel holds it), then runs the command **with that row as its input**; the command's own `<executability>` rules decide over the row, so a row the rules reject activates nothing.
- The element instantiates the configured command in its constructor (with the element's `InstantiationContext`, so a bad configuration is reported at load time) and keeps command plus config, exactly as `ButtonElement` does. `ViewCommandModel.forCommand(context, command, config)` then builds the model for it — resolve the input channel, build the executability rule, pick the matching model — and is the single construction path shared by `ButtonElement`, `TableElement`, `TreeElement`, `CommandCarrierElement` (panels, menus) and `AppBarElement`. `ViewCommandModel.execute(context, input)` runs the command with an input the caller supplies instead of the channel value; that is the one path for "run this configured command with this value".
- The control-level seam is `TableViewControl.setActivationHandler(…)` / `ReactTreeControl.setActivationHandler(…)` — one handler, called with the row business object (the tree node) after the row became the selection, returning the `HandlerResult` the gesture reports. Without a handler, activating a row only selects it.
- An activation is recorded for scripted tests the way a selection is: the index-based `activate` becomes an `activateByKey` naming the row's business object, so the step survives sorting, filtering and a fresh session.
- A double-click inside an interactive cell element (a text input of an editable cell) belongs to that element and opens nothing; Enter is likewise declined while the focus sits in a cell input or action button.
- **In a `<tree>` the selection follows the keyboard cursor.** A single-selection tree selects the node that the up/down arrows, `Home` and `End` move the cursor onto; a multi-selection tree moves the cursor alone, grows the selected range from its anchor with `Shift` and an arrow, and adds the cursor node to the selection or takes it out again with `Space`. The right and left arrows expand and collapse the cursor node, stepping to its first child and back to its parent where there is nothing to open or close, and `Enter` activates it in either mode.
- **A table with an `<on-activate>` offers the same command as a button on every row**, in a pinned column at the right edge — the gestures are invisible, and a touch device has neither of them. Each button is icon-only (the command's `<image>`, else a chevron), takes the command's label as its tooltip, and follows the command's executability *for its own row*: a row the rules reject shows a disabled button, a row they hide shows an empty cell. `<table activation-button="false">` opts out, for a table whose rows are opened another way.
- The column itself is general, not an activation column: `RowCommandColumn.columns(context, List.of(new RowCommandColumn.RowCommand(model, image, label)))` builds one pinned column per `ViewCommandModel` a caller wants to offer per row, and `ViewCommandModel.executability(row)` is what a button decides its state by. `TableElement` feeds the activation command into it — one model serving both the gesture and the buttons — and `RowSetTableControl.setTrailingColumns(…)` is where the editable variant appends them, behind the columns it builds itself.
- Demos in `com.top_logic.demo.react`: the *Attributes* table opens the object's detail dialog, *Tree Demo* (`demo/tree-demo.view.xml`) writes the activated node to a channel displayed next to the selected one, and the *Tiles Demo* account tables (`demo/tiles-demo/overview.view.xml`, `demo/tiles-demo/person-detail.view.xml`) carry an `<on-activate class="…NavigatePushCommand">` that drills into the activated person.

## Grouping

A `<table>` buckets its rows by the value of one column: one collapsible header row per value, showing that value, how many rows it holds, and what every other column aggregates over them. `<table group-by="status">` is the grouping a table *starts* with; the user regroups it from a column header's context menu ("Group by this column" / "Remove grouping") or from the column selection (the group icon on each row of the dialog, applied together with the columns), and that choice is kept under the table's personalization key exactly like the sort order.

- **The model does the grouping** (`com.top_logic.table`): `TableView.group(GroupSpec)` sets `TableViewState.getGrouping()`, persists it and hands it to `RowSource.withGrouping`. `ListRowSource` filters, then sorts, then buckets — so a filter decides what a group holds and the sort decides the order *within* it — and emits a `GroupRow` (`RowKind.GROUP_HEADER`, `expandable()`, `depth() == 0`, keyed by its `GroupKey`) followed by its members at `depth() == 1`. `DefaultTableView.cell(row, column)` renders the group value in the first column and the column's `Aggregator` result in the others.
- **The UI follows that representation**: `TableViewControl` pushes the group rows through the same `treeMode` / `treeDepth` / `expandable` / `expanded` state the tree table already uses, plus a `groupCount` present exactly on a group header, so collapsing a group *is* collapsing a node (`expand` command) and needs no second mechanism. A table becomes `treeMode` while it is grouped, whatever it was built as.
- **A group header stands for no object**: the gesture that would select it (a click, or `Enter`) collapses or expands it instead, the selection channel never receives a group, and range selection, select-all and keyboard navigation only ever collect `RowKind.DATA` rows. Changing the grouping clears the selection, since the rows it named are gone.
- **One column at a time** — `ListRowSource` rejects a multi-column `GroupSpec`, and the UI offers a single column accordingly.
- Grouping is offered for the columns the user may choose at all (the `columnOptions()`, i.e. the selectable ones): an action column carries the row itself and would yield one group per row.
- Demo: *Object list* (`tickets.view.xml`) groups its first ticket list by status and leaves the second flat; *Attributes → Table* starts ungrouped and is grouped from the header menu.

## Filtering: the filter bar and its presets

`<table filter-bar="true">` puts a bar above the table: the named filters it offers as chips, a search field examining the displayed columns, and saving the criteria the table currently shows under a name of the user's own. A table declaring `<presets>` shows the bar in any case — that is where the presets are offered.

```xml
<view>
    <channels>
        <channel name="activeFilter"/>
        <channel name="searchTerm"/>
    </channels>
    <query-bindings>
        <bind channel="activeFilter" query-param="filter"/>
        <bind channel="searchTerm" query-param="q"/>
    </query-bindings>
    <panel>
        <fields>
            <value-input type="tl.core:String" value="searchTerm">
                <label><en>Search</en></label>
            </value-input>
        </fields>
        <table active-preset="activeFilter" search-term="searchTerm"
            personalization-key="demo-tickets-open" types="demo.tickets:Ticket"
        >
            <presets initial="mine">
                <preset name="mine">
                    <label><en>Commented by me</en></label>
                    <criterion column="commentedByMe" expr="true"/>
                </preset>
                <preset name="discussed">
                    <label><en>Discussed</en></label>
                    <criterion column="comments"><range operator="GT" primary="0"/></criterion>
                </preset>
            </presets>
            <columns>…</columns>
            <rows>all(`demo.tickets:Ticket`)</rows>
        </table>
    </panel>
</view>
```

Such a page is linkable: `/view/tickets?filter=discussed` opens the list filtered by that preset, `?q=login` opens it searched for that text, and `?filter=discussed&q=login` opens it filtered by the preset *and* searched within it — the two parameters describe the displayed rows together, so every address the page leaves behind opens the rows it was taken from.

- **What the bar filters by.** The search is a plain case-insensitive substring over the **displayed** columns (`TableView.search`, `SearchSpec` over `TableViewState.getColumnOrder()`), so it finds what the user sees — a column they hide is no longer searched, one they show is — and it narrows the rows in addition to the per-column `<filter>`s. The matching flags of a column's own text filter stay that column's business.
- **`<presets>`** is a list of `<preset name="mine">` entries, each with a `<label>` and one `<criterion>` per column. `name` is the technical identity the user's choice is remembered — and linked — under, so it must not change once the table is in use. A criterion says what it selects in one of two ways, never both: `expr="currentUser()"` names a **value** the column's own filter translates (a text filter matches its text, a selection filter takes one value or a list of them, a boolean filter `true` / `false`, a range filter a single value as an exact match or a list of two as inclusive bounds), or it is written in the **form of that filter** — `<text pattern="'^Value [135]$'" regexp="true" case-sensitive="true" whole-field="true"/>`, `<range operator="GT" primary="0"/>`, `<options selected="sel -> $sel.get(\`demo.react:Demo#priority\`)"/>`, `<boolean accept="true"/>` — which is how a criterion says what a single value cannot. `inverted="true"` makes the column accept exactly the rows the criterion does not select.
- **Criteria follow the inputs.** Every value expression is evaluated with the table's `<inputs>` as its arguments, the same ones the rows are computed from, and re-evaluated whenever one of them changes (`TableElement` then calls `DefaultTableView.setDeclaredFilters`) — so `currentUser()` yields a preset that means something different to every user, and an expression over an input yields one that follows what is displayed elsewhere. A chip the user has applied goes on being the active one and filters by what it now means.
- **A criterion that does not materialize** — an unknown column, a column that cannot be filtered, a form of another kind than the column's filter, a value the filter cannot express, a selected option the column does not offer — is a declaration error (`DeclaredFilters`): it is reported and *the whole preset* is not offered, rather than offered filtering by less than its name says. A criterion selecting **nothing** is no error but no criterion either (an input nothing is selected in leaves its column unfiltered); a preset whose every criterion selects nothing is withheld and offered again as soon as its inputs select something.
- **`initial="mine"`** on the `<presets>` container names the preset the table starts out filtered by. It is part of the table's *initial state*, like the sort order and the grouping, so it takes effect only as long as no personalization is stored for this table (`DefaultTableView.restore()`): a user who applied other criteria keeps them, one who cleared the filter keeps the table unfiltered — and so does one who merely resized a column, since the persisted state replaces the initial one as a whole. Applying it persists nothing itself, so it is what every fresh session of an unpersonalized table shows. A name none of the declared presets carries is a configuration error at startup; a preset that is withheld at runtime leaves the table unfiltered instead of failing. A table offering presets should carry a `personalization-key`, so that the choices made about it survive an edit of its columns.
- **`active-preset="ch"`** publishes the `NamedFilter.id()` of the named filter the table matches, and nothing while it matches none. That is a declared preset's `name` as well as the generated identifier (a UUID) of a filter the user saved on the bar, so a saved filter is linkable exactly like a preset. Which one is active is not a flag the table keeps but a comparison of its live criteria with the offered filters (`TableView.activeNamedFilter()`), so the channel follows every filter the user applies, edits or clears — from the bar, from a column's own filter dialog, or from the channel — and it follows a change of the filters the table *offers* as well.
- **`search-term="ch"`** publishes the text the table searches for, and nothing while it searches for none. The search narrows the rows *within* whatever the table is filtered by and says nothing about that filtering, so a preset the table matches stays the active one while the user searches — the chip keeps its mark, and `filter` and `q` stand in the address side by side.
- **Both are two-way** (`TableFilterBinding`). An identifier written to `active-preset` filters the table by that filter; a text written to `search-term` is searched for. What no offered filter carries leaves the table unfiltered, and the state the table ends up in is written back — so a stale link, a mistyped identifier, or a preset that has since been removed corrects itself instead of describing something nobody sees. At creation a channel holding nothing takes the table's state rather than unfiltering it (the `initial` preset survives a page that binds the channel), while a channel that does hold a value describes the table someone asked for and is applied.
- **A write to either channel applies both.** Every writer sets one channel at a time — `QueryBindingParticipant` writes one bound parameter after the other, in the order the `<bind>` elements are declared in — and applying a named filter replaces the whole filtering, the search term included. The binding therefore brings the table to what *both* channels say on every write: the preset first, then the term. That is order-independent and repeatable, so `?filter=mine&q=Export` opens the same rows whichever parameter is bound first; applying only the channel that changed would let the term be wiped by a preset arriving after it. Once the table and the channels are together, nothing on `active-preset` means "by no named filter" and leaves the term in effect, and nothing on `search-term` means "searched for nothing" and leaves the preset applied. A filter the user saved *with* a term is named by the two channels together, which is exactly what the binding publishes when it is applied.
- **Which term is compared** (`NamedFilter.matches`): a named filter's search term takes part in the match only when the filter *defines* one. A `<preset>` never does — `DeclaredFilters` builds every declaration with no term — so it matches on its column criteria alone and survives a search. A filter the *user* saved on the bar captures the text that was searched when they saved it, and therefore matches only while exactly that text is searched for again: the term is one of the things it was saved as. Both can match at once — saving "the preset, searched for X" yields a filter whose columns are the preset's — and then the one naming the term wins, because it describes the displayed rows completely while the preset describes only their columns; among equally specific matches the offered order decides, so a preset still wins over a saved filter with the same criteria and no term. The other way round, picking a preset replaces the whole filtering, the search term included — which for a preset means the search field is cleared. A preset declaring no criterion at all (the "All" chip) matches exactly while no column is filtered, whether or not a search is running.
- **The binding follows the `TableView`, not the bar**, so both channels work for a table that displays no bar at all: an input elsewhere — a `<value-input value="searchTerm" type="tl.core:String"/>` — searches it, and several tables bound to one search channel are searched together.
- Demo: *Object list* (`views/tickets.view.xml` in `com.top_logic.demo.react`) — the Open list declares three presets over its computed `comments` / `commentedByMe` columns, opens filtered by `initial="mine"`, and binds both channels to the query parameters `filter` and `q`; the Closed list beside it shows no bar and is searched through the same channel. *Attributes → Table* (`views/attributes.view.xml`) shows the criterion forms: a value, a case-sensitive regular expression, an inverted range, and a selection following the table's own selection channel.

## Pinned columns

`Column.pinnedEnd()` keeps a column at the end of the table: it is rendered behind every other column and stays fixed to the right edge while the table scrolls horizontally — the place for what acts on a row (the per-row buttons) rather than for what the row *is*. `DefaultColumn.builder(name, value).pinnedEnd(true)` is how a column author asks for it; there is no `.view.xml` attribute.

- A pinned column is the table's own, not part of the arrangement the user makes: it is neither `frozenEligible()` nor `selectable()` (`DefaultColumn` enforces both, whatever they are set to), so the column selection does not offer it, `setColumnOrder` / `moveColumn` leave it where it is, `resizeColumn` and `setColumnVisible` decline it, the frozen prefix counts only the columns in front of it, and the rows cannot be grouped by it.
- `DefaultTableView` normalizes the column order whenever it is set — by the caller, by a restored personalization, by a column selection — so the pinned columns trail it. Every consumer sees that one order: `columns()` lists them last with `ColumnView.pinnedEnd()` set, and `frozenColumnCount()` never reaches into them.
- `TableViewControl` pushes the flag as the per-column `pinnedEnd` state. The client (`TLTableView.tsx`) renders such a cell `position: sticky` with a `right` offset of the widths of the pinned columns behind it plus the reserve the row ends with — in the header the measured scrollbar width, which the header has no vertical scrollbar of its own for, and where the table does not end in a pinned column the column button's width in header and body alike. A table ending in a pinned column has a heading without a label there, so the column selector's cog sits in that heading and the pinned column reaches the right edge. Rows and header row fill the table when the columns are narrower than it; the last *unpinned* column grows into the space left over, in the heading exactly as in the rows. A pinned cell keeps its width, offers no resize handle and no drag, and the header menu offers it neither the freeze boundary, nor a grouping, nor the fit to content.
- `Column.cssClass()` (`DefaultColumn.Builder.cssClass(String)`) names a class the client puts on every cell of the column, its heading included — how the column presents its cells, as opposed to the per-row `cssClass(R row)`. `RowCommandColumn` uses it to drop the text padding and center its frameless button.
- Every unpinned column can be fitted to its content: "Fit width to content" in the header menu, or a double-click on the column's resize handle, sets the width the heading and the rendered cells need and persists it through the same `columnResize` command a drag ends with. Only the rows currently in the DOM are measured, so the fit follows what is displayed, as the virtual scroller renders it.

## Drill-down navigation with `<tile-stack>`

`com.top_logic.layout.view.tiles` provides drill-down navigation. A `<tile-stack path="navPath" initial="products/overview.view.xml"/>` displays the last frame of a path of `TileFrame`s, the `initial` view when the path is empty, and keeps the frames the displayed one covers (see below). The path itself lives on a normal channel of the enclosing view (`List<TileFrame>`), which is the single source of truth: every navigation is a write to that channel.

- **Push**: `<navigate-push view="products/detail.view.xml" input="selectedProduct" bind-input-to="product">` (`NavigatePushCommand`) mounts a view as a new frame. `bind-input-to` maps the command's input onto a channel of the pushed view; further `<bind>` entries capture the *current value* of caller-scope channels at push time. `frame-label` supplies the breadcrumb label — `<static>` for fixed text, `<scripted expr="p -> $p.get(\`tl.accounts:Person#name\`)" inputs="selectedPerson"/>` for one derived from channels at the push moment. Where the stack declares a `<label>` for the pushed view (see below), the push needs no `frame-label` of its own.
- **Pop**: `<navigate-pop>` drops the top frame, `<navigate-pop-to depth="1">` truncates to a given depth. Each of the two is a `ViewCommand` *and*, under the same tag, a `ViewAction` (`NavigatePopAction` / `NavigatePopToAction`) — the command delegates to the action, and the tag is resolved per list type, so the same `<navigate-pop/>` names the command in a `<commands>` list and the action in a `<generic-command>` chain. Leaving a frame is therefore the last step of a longer command: `<generic-command><execute-script .../><navigate-pop/></generic-command>` deletes the displayed object and then leaves the frame that displayed it. Both actions pass the chain's object through as their result, so an action after them still sees it.
- **The path inside a frame**: `<tile-stack bind-path-to="navPath">` registers the stack's own path channel under that name in every mounted frame's context, so a frame view declaring `<channel name="navPath"/>` reads the live path — the declaration is pre-bound and not instantiated locally. `<derived-channel name="depth" inputs="navPath" expr="p -> $p.size()"/>` turns it into the frame's depth, which a command tests via `<executability><visible-if expr="d -> $d > 0"/></executability>`: that is how a Back button hides itself on the initial frame and shows on every pushed one. Without `bind-path-to` a frame sees only the parameters it was pushed with.
- `<tile-breadcrumb path="navPath">` reads the same channel and renders a crumb per frame; clicking a crumb truncates the path. Wrapping the breadcrumb in `<slot-content to="appbar-content">` raises it to the app bar instead of showing it inside the frame.
- A frame's parameters are a snapshot, not live channels: `TileFrame` is the view reference, the label, and the parameter values captured at push time, which become the mounted view's channels. The path channel a `bind-path-to` adds is the exception — that one is the stack's own.
- **The path in the URL**: a `<frame route="person/:person" view="accounts/person-detail.view.xml">` entry inside `<tile-stack>` declares the segments a frame of that view occupies, so the drill-down path becomes part of the address: each frame on the path contributes its route, in the order it was drilled down (`…/person/<id>`). A `<param name="person" expr="p -> objectId($p)" reverse="id -> objectResolve(`tl.accounts:Person`, $id)"/>` converts between the parameter value and the text the URL carries — a parameter without a `<param>` entry is carried as text, which is what a string parameter needs. A `<label class="…ScriptedTileLabel" expr="p -> $p.get(`tl.accounts:Person#name`)" inputs="person"/>` inside the `<frame>` names every frame of that view, evaluated over its parameters as channels: the frame a URL restores and the one a `<navigate-push>` pushes without a `frame-label` of its own — so a frame is named once, at the stack, for every place that pushes it.
- **Restoring, and coming back out**: opening such an address restores the path frame by frame — the participant takes up the route of the next frame, puts it on the path, and is offered the rest of the URL again for the frame above it (`TileFrameRouteParticipant`, one per stack, anchored at the stack's control, so that the path enters the URL where the stack sits in the display). A route naming something that cannot be displayed — a deleted object, a mistyped identifier — restores no frame, and the address bar is corrected to the path that could be restored; a frame view without a `<frame>` declaration has no address, and neither has anything drilled into from it. A URL that names fewer frames than the display shows — the back button out of a drill-down, or a bookmark of the bare view — returns the stack to the view it started in (`RoutingParticipant.resetRoute`). Drilling down and coming back out are one history entry each, whatever the exchanged frames bring and take with them.
- Both demos live in `com.top_logic.demo.react`: `demo/tiles-demo.view.xml` (accounts overview, drill-down to a person detail) and `demo/tiles-multi-demo.view.xml` (one stack per tab, on the channels `navPathA` / `navPathB`). Both stacks bind their path as `navPath`, declare the person frame's route and label, and the two shared frame views carry the depth-guarded Back command. A push is the frame view's row activation: the account table holds the `NavigatePushCommand` as its `<on-activate>`, so a double-click, `Enter`, or the row's chevron button drills into that person.

## Multi-step flows with `<wizard>`

`com.top_logic.layout.view.wizard` leads through a sequence of steps and displays one of them at a time. `<wizard current-step="currentStep" step-list="true">` holds the sources the sequence is built from; the channel referenced by `current-step` holds the **key of the step displayed** and is the single source of truth: every move is a write to it, and a value no step carries — `null` included — displays the first step, so a flow starts at its beginning without anyone writing the channel first.

- **The sources.** The sequence is the concatenation of what the wizard's children contribute, in configuration order, and the two kinds mix in one wizard. A `<step id="welcome" label="…" icon="css:bi bi-flag">…</step>` (`StaticStepSource`) contributes exactly the one step it is written as, keyed by its `id`. A `<dynamic-steps steps="questions" element-channel="question" label="q -> …" icon="q -> …">…</dynamic-steps>` (`DynamicStepsSource`) contributes one step per element of the list its `steps` channel holds, keyed by the element itself, with the content template instantiated per displayed step and the element published on the `element-channel` (default `element`) — so the template typically is a `<switch input="element">` choosing the display by the kind of element. Without a `label` function an element names its step the way it is named everywhere else (`MetaLabelProvider`). Extending the set is a matter of implementing `WizardStepSource`: `steps(ViewContext)` answers the steps of one source, `observedChannels(ViewContext)` names what it decides by, `childGroups()` reports its content to the designer.
- **The sequence is live.** The wizard follows every channel its sources name and expands the sequence anew whenever one takes a new value — *inside* that channel notification, so an action chain that writes such a channel and then moves on already sees the step it created: `<generic-command><execute-script inputs="questions" function="qs -> x -> …"/><write-channel name="questions"/><wizard-next/></generic-command>` appends an element and goes to the step it became, in one command. What the user is looking at survives a re-expansion where its key is still in the sequence: the content control is kept as it is and only its position is corrected; a step whose key has gone falls back to the first step, and its content is disposed.
- **Moving.** `<wizard-next/>`, `<wizard-back/>` and `<wizard-goto step="summary"/>` are each a `ViewCommand` *and*, under the same tag, a `ViewAction`, exactly like the tile stack's `<navigate-pop/>`: the same element names the command in a `<commands>` list or a `<button>`'s `<action>`, and the action inside a `<generic-command>` chain. The actions pass the chain's object through, so storing what was entered and then moving on is one command. `<wizard-goto>` without a `step` takes the chain value (the command's input) as the key. A key no step carries leaves the wizard where it is.
- **Guarding.** `<wizard-has-next/>` and `<wizard-has-back/>` are executability rules that hide a command where the move would do nothing — a Back button is absent on the first step and appears again after the first Next. They find the wizard the same way the commands do and report every move through `ObservableRule` — the mix-in by which a rule deciding by more than the command's own input says when its answer may have changed, so the command's `ViewCommandModel` re-evaluates it (the same mix-in carries `<form-valid/>`, which reports the form's validation state changing).
- **Where the buttons live.** The wizard renders the indicator and the content of the step and *no navigation buttons of its own*: which buttons a flow offers, and where, is composed in the view. They have to sit **inside a step's content**, because that is where the wizard installs the `WizardScope` that the commands and rules resolve. To get one footer below the whole wizard rather than a button bar per step, put a `<slot name="wizard-actions"/>` beside the `<wizard>` (a sibling in the enclosing `<stack>`) and let every step raise its own buttons with `<slot-content to="wizard-actions">` — slot routing is by tree distance, so a sibling slot is the nearest match from inside a step. Placing the contribution inside the step's `<form>` additionally lets `<form-valid/>` resolve that form, which is how a Next is disabled while a mandatory field is empty.
- **The indicator.** Three independent options on the element: `counter` (default on) writes the position against the total, zero-padded (`02 — 05`); `progress` (default on) renders a `ReactProgressControl` filled to `(index + 1) / total`; `step-list` (default off) lists the steps by name, marking each one done, current or upcoming. A step already done is a button that jumps back to it; a step still ahead is not, because the way there leads through the steps in between. The control registers itself as a container of the reveal protocol (`ChildRevealer`, keyed by step), so bringing a step into view is available to whoever displays something nested in it.
- **The step in the URL.** Nothing special: `<param-bindings><bind channel="currentStep" route-param="step"/></param-bindings>` on the `<view>` binds the step channel to one path segment like any other channel, yielding `/view/wizard/profile`. The segment is the channel value's text, and the value a deep link writes back is that text, so a step whose key is a **string** round-trips — which is why keying dynamic steps by strings makes them addressable too (`/view/wizard/phone` in the demo). A key that is a model object puts its `toString()` into the segment and does not resolve back, so such a link displays the first step; bind a bidirectional `<derived-channel>` (`expr` to an identifier, `reverse` back to the object, as for any other object in the URL) where those steps should be addressable. A deep link into a dynamic step also needs the list that produced it: in a fresh session the list is empty, so the wizard has no such step yet and shows its first one.
- **Transitions.** A step change is drawn by the stylesheet; the component only says what is happening. `ReactWizardControl` publishes the way the display moved (state key `direction`), which the root carries as `tlWizard--forward` / `tlWizard--backward`; the step arriving carries `tlWizard__step--entering` until its animation ends, and over it lies an inert copy of the step left behind, `tlWizard__step--exiting`, removed when its animation ends. The copy is DOM rather than a control, because the control of the step left behind is disposed on the server the moment the wizard moves — a picture is what is still there to animate out. It is `aria-hidden`, `inert`, takes no pointer input, carries no ids of what it copied, and is positioned absolutely inside `.tlWizard__body`, so the step arriving already sits where it will stay. Only one copy exists at a time; a second move replaces it. The engine ships a subtle default — fade plus a 12px slide in the direction moved, 200ms — and an application restyles the change by redefining `.tlWizard__step--entering`, `.tlWizard__step--exiting` and their `.tlWizard--backward` variants. `@media (prefers-reduced-motion: reduce)` turns the animation off, and because nothing then reports an `animationend`, the component drops both classes on a one-second fallback timer instead.
- **Auto-advance.** A step can carry its own time: `<step id="ready" auto-advance="2s">` (a duration in the usual `MillisFormat` notation) for an interstitial the user only watches, and `<dynamic-steps auto-advance="q -> …">` for one computed per element — an element the function answers nothing for is a step the user leaves. It reaches the runtime as `WizardStep.autoAdvanceMillis()` and the client as the state key `autoAdvance` of the step displayed, where it becomes a timer; the timer is cleared whenever the step changes. When it fires it reports back naming the step it belongs to, and the wizard moves on **only while that step is still the one displayed** — the user may have moved on themselves in the meantime, and a timer that outlived its step must not carry the display past what they chose. The time runs while the flow *leads through* the step: it is started for a step entered going forward, and not for one the user came back to — a Back out of the step behind an interstitial would otherwise be answered by being sent forward again. A re-expansion that carries the displayed step along does not restart it either; it keeps counting.
- The demo is `demo/wizard-demo.view.xml` in `com.top_logic.demo.react` (sidebar **Wizard** / **Assistent**, `/view/wizard`): an onboarding flow whose written-out Welcome, Profile and Summary steps enclose a `<dynamic-steps>` over a `questions` channel that grows while the flow is walked, with the Back/Next footer raised out of every step into one slot.

## URL routing: what ends up in the address bar

**What a URL may load: the entry points.** `/view/<windowName>/some.view.xml` names the view a browser tab displays, and only a view the application declares as an entry point can be named there: the `default-view` of `ViewConfig` (implicitly) or one of its `<entry-points><entry-point view="demo/pdf-demo.view.xml"/></entry-points>` (keyed by the view, so the registrations of several modules merge). Every other view file is a fragment of a display — a dialog, a menu, a page of a tab — which the view enclosing it supplies with the channels it reads, and which alone in a tab shows nothing or fails; `ViewServlet.resolveViewPath` answers such a URL with 404. A remainder that does not end in `.view.xml` is a route, not a view, and loads the default view with the route resolved inside it (see below).

`RouteManager` composes the URL below the view servlet path from the segments of the `RoutingParticipant`s the display contains, in the order in which it contains them — sidebar item, then tab, then route parameters — and pushes each change to the browser over SSE (`RouteChangeEvent`, applied by `react-src/bridge/route-sync.ts` via `pushState` / `replaceState`). The displayed participants come from a walk of the control tree (`ReactControl.displayedChildren()`), so a participant that has left the display contributes nothing, whenever it happened to register. `popstate` (browser back / forward) is sent back as a `navigateToRoute` command.

Adopting a URL — a deep link, a reload, a back navigation — works in two steps: `RouteManager.adoptUrl` records it, and its segments are consumed either by participants as they register (a display being built for the first time) or by `resolvePending()` over those already registered (a page rendered into the control tree its window still holds). Activating a route materializes the display below it, which is how nested segments reach their participants. Once the page is written, `finishAdoption()` drops whatever remained unresolved, corrects the address bar to what the display composes, and asks every displayed participant that shows a route the URL did not name — neither activated by the URL nor brought into the display by one of its activations — to `resetRoute()`, which is how a URL naming fewer segments than the display shows returns it. `RouteManager.adoptionId()` identifies the adoption in progress, so that a participant restoring several levels from one URL tells the levels of this URL from those of the last. Every adoption ends: `finishAdoption()` is the normal end and the only place the composed URL is reported — a URL the display settles on that the browser already shows is not reported at all — while `cancelAdoption()` ends one whose URL the display refused (a dirty-form veto), leaving the display untouched and the address bar to the caller's `RouteVetoEvent`. Nothing is reported in between: a participant that leaves or enters the display while a URL is adopted (a page loaded into the control tree its window still holds re-attaches that tree; an activated route replaces the content beside it) reports no address of its own, because the URL a half-built display composes is nobody's address — the walk of the tree it is composed from does not even reach the participants yet. An adoption left open would report the user's next navigation as a replacement instead of a history entry. None of this pushes a history entry: adopting a URL the client already shows is not a navigation, so only a route change reported by a displayed participant does.

- **Sidebar**: every `<nav-item>` contributes its `id` as a segment; `route="custom"` overrides it, `route="none"` opts out (`SidebarElement.resolveRoute`).
- **Tabs**: `ReactTabBarControl` contributes the active tab. A tab bar shown for one case of a `<switch>` removes its segment again when the case changes (via `replaceState`, so the disappearance is not a history step of its own).
- **Tile stack**: the drill-down path of a `<tile-stack>`, one route per frame, for every frame view the stack declares a `<frame>` for (see above). It is the one participant that takes up route after route of a single URL (`RoutingParticipant.acceptsRouteSequence`) and the one that has a state without a route to return to (`resetRoute` empties the path). Give a frame route a static prefix (`person/:person`, not `:person`): a bare parameter pattern matches any single segment and would take up whatever follows the stack in the URL.
- **Route parameters**: `<param-bindings><bind channel="ticketKey" route-param="ticket"/></param-bindings>` on `<view>` binds a channel to one path segment: a channel change writes the segment, a deep link writes the segment's value into the channel. The segment is always the value the channel holds, so a link naming an object that does not exist leaves no segment behind. A URL without the segment says nothing about the value rather than saying there is none: the view keeps what it establishes itself — a default selection, or the selection the session still holds — and the address bar is completed with it, as a replacement rather than a history entry. The value is a string, so a model object is bound through a bidirectional derived channel: `expr="t -> objectId($t)"` maps the object onto its identifier, `reverse="id -> objectResolve(\`mod:Type\`, $id)"` maps the identifier back to the object (`views/tickets.view.xml` in `com.top_logic.demo.react` shows this). `objectId` yields nothing for a transient object and `objectResolve` nothing for an identifier no object of that type carries, so an unusable link simply leaves no segment. A business key works the same way where the URL should be readable — `expr` to the attribute, `reverse` a lookup over `all(...)` — at the price of the uniqueness and URL-safety the key then has to have. An optional `prefix` puts static segments in front of the value — `<bind channel="ticketId" prefix="ticket" route-param="ticket"/>` compiles to `ticket/:ticket` and yields `/view/tickets/ticket/<id>` — which names what the value identifies; without a prefix the compiled pattern is a bare `:param` matching any single segment, and the position of the binding in the URL is what identifies it. The prefix appears exactly when the value does, and a URL carrying the prefix alone matches nothing, so it says nothing about the value. Values are percent-encoded as path segments (`RouteEncoding`, applied by `RoutePattern.produce` and undone by `RoutePattern.match`), so a value containing a slash, a space or a `%` stays the one segment it was written into; static segments of a pattern are used verbatim. Both entry points therefore deliver the route encoded: the client sends `window.location.pathname` on `popstate`, and `ViewServlet` reads the route from the raw request URI rather than from the container-decoded path info.
- **Query parameters**: `<query-bindings><bind channel="filter" query-param="q"/></query-bindings>` on `<view>` binds a channel to one query parameter (`QueryBindingParticipant`). It works like a route parameter, with two differences that follow from the query naming its parameters instead of placing them: the binding occupies no path segment, so it neither depends on nor disturbs the position of anything else in the URL and simply disappears when the channel holds nothing; and the query of an adopted URL is offered to *every* participant (`RoutingParticipant.activateQuery`), each taking the parameters it declares. A change that leaves the path as it is and alters only the query is always reported with `replaceState`, whatever the participant asked for: refining what a page shows - narrowing a filter, picking a sorting - stays on that page, so the back button leaves it rather than walking the terms typed on it. A URL without the parameter says nothing about the value, exactly as for a route parameter: the view keeps what it establishes and the address bar is completed with it. A displayed element list bound to such a channel (a `<table>`'s `<rows>` over its `<inputs>`, an `<object-list>`, a `<calendar>`) is re-read when it starts observing (`RowSourceObserver.attach`), because the URL writes the channel while the display is still being attached - and equally when a display comes back after being detached, which ignored every change meanwhile. Keys and values are `application/x-www-form-urlencoded` (UTF-8), and the parameters follow the display order of the participants contributing them. The value is text, so a `<value-input value="searchTerm" type="tl.core:String"/>` writes it, a `<table>` reads it through an `<inputs><input channel="status"/></inputs>` of its `<rows>` expression, and a `<table>` bound with `active-preset` / `search-term` publishes its own filtering on such a channel (see [Filtering](#filtering-the-filter-bar-and-its-presets)) - `views/tickets.view.xml` in `com.top_logic.demo.react` binds both as `/view/tickets/ticket/<id>?filter=<preset>&q=<term>`.

An encoded slash (`%2F`) in a route value needs a servlet container that accepts such a path — containers reject it by default, as "ambiguous". The embedded Jetty of `tl-ide-jetty` does accept it: `Bootstrap` allows `UriCompliance.Violation.AMBIGUOUS_PATH_SEPARATOR` on the connector's `HttpConfiguration` and sets `ServletHandler.setDecodeAmbiguousURIs(true)`. A standalone Jetty needs the same two settings (`jetty.httpConfig.uriCompliance`, `jetty.servlet.decodeAmbiguousURIs`), a Tomcat 10.1 `encodedSolidusHandling="decode"` on its `<Connector>` (the default `reject` answers 400). Object identifiers never contain a slash, so this concerns bindings on a business key only.

## Login, anonymous sessions and the login view

Every visitor reaches the application under an anonymous session, so who is let in is a question the application answers rather than one the servlet refuses. An application that shows itself to visitors offers the login from its account area: `user-menu.view.xml` offers an anonymous session (`account -> $account.accountIsAnonymous()`) a Login button that opens `login.view.xml` as a dialog, and the parts that are not for visitors are gated in place — `<access-control scope="administration"/>` on a `<nav-item>`, `<authenticated-only/>` in a command's `<executability>`.

An application that has nothing to show a visitor names a login view in its `ViewConfig` instead: `<config config:interface="com.top_logic.layout.view.ViewConfig" login-view="login-page.view.xml">`. `ViewServlet` then renders that view for every request of a session that belongs to no account, whatever the URL names — a route, the default view, an entry point, or a view that is none of them — so the visitor sees the login and nothing else. `login-page.view.xml` is what the framework ships for this: the credentials form on a centered `<panel appearance="card" width="380px">` and nothing else, where `width` is the general option of `<panel>` for a panel that is not to take the width it is offered. An application with a login display of its own names that one. Both displays reference the same `login-form.view.xml` — the fields, the login action, the `<login-commands/>` a feature module contributes to and the `<login-methods/>` buttons of the configured SSO providers — so the dialog and the page offer the same login.

**The requested URL survives the login.** The login page is not the application the URL addresses, so it takes the URL up not at all: the route manager *holds* the requested route (`RouteManager.holdUrl`) instead of adopting it. Nothing is composed from the display and nothing is corrected, so the address bar keeps showing `/view/my-refunds` while the form is displayed. The login swaps the session and reloads the page (`PendingSessionAction`), and the reload carries that same URL into the application: a URL that names a route always gets that page, and the personal start page (`StartPage`, written by the user menu's "Start on this page") applies only to a URL that names no route. The redirects that establish the session in the first place — the cookie check, the anonymous login — return to the page that was asked for as well (`ViewServlet.requestedPage`), and an SSO button hands its provider the held route as the return path, so an external login lands on the requested page too.

Leaving works the other way round: `<logout-command>` swaps the session back and sends the browser to the root of the view application (`ViewServlet.ROOT_PATH`), because the page the departing user was on is theirs and not the landing page of whoever sits down at the browser next. `com.top_logic.demo.react` configures a login view, `com.top_logic.demo` does not — its React view is browsed anonymously and logged into through the account area.

## Filling the available height: the fill contract

A control that should span the height its container offers - instead of growing with its content - carries the CSS class `tlFill`, which the layout CSS turns into `flex: 1; min-height: 0`. That bounds the control only while its container has a definite height itself, so the decision is not local: a container hosting a filling child has to fill in turn, up to the first box that is bounded anyway (the viewport-high mount point, a tab-content region, a scrolling panel body). This chain is a property of the path through the control tree, which is why it lives in the React bridge (`react-src/bridge/fill.ts`) rather than in a CSS selector enumerating the child types allowed to grow.

A container component therefore declares how it takes part:

- `useFill(fills)` — a fixed decision by the control's own configuration: `<panel fill="true">` reports filling, a plain panel does not. Returns the class for the root element.
- `useFillHost(alwaysFills?)` — a container: it fills while it always does (a split panel, whose panes are sized proportionally; a tab bar, whose strip stays pinned) or while one of its children reports filling, and reports that on to its own container. Returns the class plus the host to provide to the children (`<FillProvider host={…}>`).
- `<FillBarrier>` — ends the chain: a region bounded on its own that scrolls what does not fit (a panel body, a tab content area), or a surface laid out apart from the page (a dialog, a window, a drawer). A filling control inside resolves its height against that region, and nothing outside reacts to it.

A container that declares nothing is opaque: a filling control inside it does not grow it, so a new container element that lays out vertical space adds its declaration.

A `<dashboard>` bounds its tiles instead of following them. Its grid has a definite row unit - `row-height`, a CSS length defaulting to `16rem` - and a tile is as many of those units tall as its `row-span`, plus the gaps between them. Each tile is a barrier: a panel that fills or a table inside it resolves its height against the tile and scrolls there, and the request reaches neither the dashboard nor whatever hosts it. Content that does not fill and is taller than its tile scrolls inside the tile as well.

## A tile-stack frame is kept, not rebuilt

`<tile-stack>` holds one frame per stack position (`ReactTileStackControl`): the `initial` view, followed by the frames of the path channel. The frame at the end of the path is the displayed one; the frames it covers keep their control tree and their layout box (`.tlTileStack__frame--covered` takes them out of the flow at the size of the stack and makes them invisible). Returning to a frame - a breadcrumb click, a `<navigate-pop>` - therefore shows it as the user left it: the selected tab, the selected table row, the values being edited and the scroll offsets included. A path change keeps the frames of the longest common prefix and disposes the ones it drops, so a path reconstructed from a URL keeps the frames it names (`TileFrame` compares by view, label and params). A covered frame is rendered but not seen, and the stack reports only the active frame as `visibleChildren()`: the URL is composed from the participants below the visible children, and only those take up a route of an adopted URL, so a tab bar inside a covered frame neither names its tab in the address nor is offered the segment meant for the tab bar of the frame on top.

The hiding sits on a wrapper element the stack renders itself: a frame's content renders its own root element, and a style set on that from the outside is overwritten the next time the content re-renders.

## Object navigation: display targets, the reveal protocol, `<show-object>` / `<show-view>`

"Show this business object where the application displays objects of its type" is the view-layer counterpart of the classic `GotoHandler` / `LayoutComponent.makeVisible()`. It consists of three generic parts in `com.top_logic.layout.view.navigation`; none of them knows sidebars, tab bars or tile stacks in particular.

### Display targets are declared globally, per type

`DisplayTargetService` (a configured service, `<config service-class="com.top_logic.layout.view.navigation.DisplayTargetService">`) holds the application's targets: per model type an **ordered list of views to show**, each with the channel values to set. The application decides which of the places that display a type is *the* place an object of it is shown at; a view file stays reusable and does not know it is a target, and a library module contributes targets for its own types in its configuration fragment.

```xml
<target type="tl.demo.projectManagement:Ticket">
  <show view="projects/overview.view.xml"><bind channel="project" expr="t -> $t.container()"/></show>
  <show view="projects/milestones.view.xml"><bind channel="project" expr="t -> $t.container()"/></show>
  <show view="projects/milestone-list.view.xml"><bind channel="milestone" expr="t -> $t.get(`…:Ticket#milestone`)"/></show>
  <show view="projects/ticket-detail.view.xml"><bind channel="ticket"/></show>
</target>
<target type="tl.demo.projectManagement:Contributor">
  <show view="projects/contributor-dialog.view.xml" dialog="true"><bind channel="contributor"/></show>
</target>
```

A `<show>` entry is carried out in one of three ways, decided by how its view is reached. Where it is looked for is decided first: **within the view the show before it displayed**, and only if it sits nowhere in there, within the window as a whole. A frame the preceding show pushed onto a tile stack is therefore descended into, although the frame itself is part of no statically scanned mount path.

- **Mounted view** (reachable through sidebar items, tabs, `<view-ref>`, `<adaptive-detail>` panes or the initial view of a `<tile-stack>` — from the view the show before it displayed, or else from the root view): the mount is revealed and the bindings are written to the view's channels in declared order.
- **Unmounted view**: it is a drill-down frame, pushed onto the tile stack that hosts the previously shown view (the bindings become the frame's channel values, exactly like `<navigate-push bind-input-to=…>`). A chain of such entries rebuilds a drill-down path; frames already on the stack with the same view and values are kept (pop to the longest matching prefix, push the rest), so the frames a target pushes must use the same view refs and channel names as the user's own drill-down.
- **`dialog="true"`**: the view is opened as a dialog with the bindings as initial channel values (the same seam as `<open-dialog>`). It must be the last entry.

A reveal can show views and write channels and nothing else, so revealing a tab — or any other keyed place — inside a frame a preceding show pushed works by naming a view that sits on it: the content of the tab gets a view file of its own, and a `<show>` for that file both brings the tab up and receives the bindings. That file is reachable from no root view, which is exactly why it is found within the frame.

A `<bind expr>` is a TL-Script function of the object being shown and defaults to the object itself. A frame entry carries its breadcrumb label as `<label>` or, computed from the shown object, as `label-expr` — the same expression the drill-down's `<frame-label>` uses, because `TileFrame` equality includes the label. Resolution (`DisplayTargets.resolve`): the most specific type wins (an exact class beats a generalization, following `TLClass.getGeneralizations()`); among targets for the same type, the one whose first view is mounted **nearest** to the view that triggered the navigation (longest common mount prefix), then the one flagged `default="true"`, then the first declared. `hasTarget(type)` is the question "can objects of this type be shown at all?" — it decides whether a value is rendered as a link. At startup the service checks every `<bind channel>` against the channels the view declares and logs a configuration error for a mismatch (a typo in a channel name is found without clicking through the app).

### Where a view is mounted is known statically

Sidebar items and tabs create their content lazily, so the mount of a view cannot be read from the control tree. `UIElement.getChildGroups()` reports an element's content **statically**, as `ChildGroup`s: a keyed group for each child a container addresses by a key of its own (sidebar item id, tab id, `AdaptiveDetailElement.Config.SELECTOR` / `DETAIL`, `TileStackElement.Config.INITIAL`), an unkeyed group for content a container shows unconditionally, and an `EmbeddedView` for a `<view-ref>`. `ViewMounts.forRootView(rootViewRef)` walks these groups over all reachable view files and yields, per view file, its `MountPath`s: the sequence of `MountStep(container element, key)` from the root. The scan is cached per root view and invalidated together with the view files. **A container element that holds child elements must implement `getChildGroups()`** — a container that does not report its children hides every view below it from navigation.

### The reveal protocol

Every control that shows one of several children implements `com.top_logic.layout.react.reveal.ChildRevealer` — `revealChild(key)` makes the child addressed by `key` the displayed one, creating it if needed, and throws `ChannelVetoException` when unsaved changes stand in the way: `ReactSidebarControl` (item id), `ReactTabBarControl` (tab id), `ReactAdaptiveDetailControl` (selector/detail), `ReactTileStackControl` (`initial`, or `frame<n>` via `frameKey(n)` = pop to that frame), and a `DialogRevealer` around a `DialogHandle` (closes the dialogs above it). Every keyed container appends a `RevealStep` to the **`RevealPath`** scope when it derives a child's `ViewContext` (`context.withScope(RevealPath.class, path.append(this, key))`), and every view instance and every revealing control announces itself in the window's **`RevealRegistry`** (`ViewContext.getRevealRegistry()`, one per root context and inherited like the slot registry) under that path, unregistering when its control is cleaned up (cached hidden content stays registered while alive). Revealing a mounted view walks its `MountPath` from the root: at each step the registered container at the current prefix reveals the next key — which creates lazily built content, whose own containers and views register on the way — and the view instance found at the full path finally receives the bindings. Vetoes are handled as `<write-channel>` handles them: the dirty-confirm dialog, then the step is retried; cancelling aborts the chain.

### Entry points

- **`<show-object/>`** (`ShowObjectAction`) in a `<generic-command>` chain shows the chain's input object and passes it on; no input passes through unchanged; a selection of exactly one object shows that object. `<generic-command input="selection"><show-object/></generic-command>` is the whole configuration of a "go to" button. Java code calls `ObjectNavigation.show(context, object, continuation)`.
- **`<show-view view="…">`** (`ShowViewAction`) brings one view into view and writes the values its channels receive, without a business object being involved. It carries exactly the attributes and `<bind>` children of a display target's `<show>` — `view` (mandatory), `dialog`, `label`, `label-expr` — and each `<bind expr>` is a TL-Script function of the chain's current value (a `<bind>` without `expr` receives that value itself). The view is reached the same way a target's view is: the containers on the way are opened, or it is drilled down to as a frame, or opened as a dialog. The chain continues with the value it had.

  ```xml
  <show-view view="tickets.view.xml">
    <bind channel="activeFilter" expr="term -> 'all'"/>
    <bind channel="searchTerm" expr="term -> $term"/>
  </show-view>
  ```

- **`<show-views>`** (`ShowViewsAction`) holds a list of `<show>` entries carried out in **one** request, so each entry is looked for within the view the entry before it displayed — a display target's `<show>` list, written in the command chain instead of declared per type. Java code calls `ObjectNavigation.show(context, shows, value, continuation)` for either.

  ```xml
  <show-views>
    <show view="projects/overview.view.xml"><bind channel="project" expr="t -> $t.container()"/></show>
    <show view="projects/ticket-detail.view.xml"><bind channel="ticket"/></show>
  </show-views>
  ```

- **`ReactContext.getObjectNavigator()`** (`com.top_logic.layout.react.navigation.ObjectNavigator`: `canShow(value)`, `show(context, value)`) is the seam for controls in `com.top_logic.layout.react`, which cannot depend on the view layer; the view layer answers it with `DisplayTargetNavigator`. Through it, **object values displayed read-only are links automatically** wherever a target exists for their type: `ReactResourceCellControl` (tree nodes via `MetaResourceControlProvider`, and any cell built with `useLink`), the read-only values of `ReactDropdownSelectControl` (which is what reference attributes in `<table>` cells and view-mode `<form>` fields render as), and `tlObject` anchors in read-only structured text (`ReactWysiwygControl`, command `showObjectLink`, resolved with `TLObjectLinkUtil` like the classic `OpenTLObjectLink`). A tree hands its node content provider the business object a node stands for (`TreeUIModel.getBusinessObject`), which is why a node is a link exactly like the same object in a cell. The TL-Script functions `htmlObjectLink(object, label)`, `htmlSource(content)` and `htmlText(source)` (`HtmlFunctions` in `com.top_logic.layout.wysiwyg`) write such an anchor and read or write the HTML source of a structured-text attribute, e.g. to append an object reference to a comment.

- **The WYSIWYG editor carries configured commands and inserts what they write.** The editor is chosen for a field by `<input-control class="com.top_logic.layout.react.wysiwyg.WysiwygControlProvider">`, which takes `<commands>` — ordinary view commands (`ViewCommand.Config`, e.g. `<generic-command>`) — and an optional `insert-channel`. The commands run in a child `ViewContext` of the field's view context: they see the channels of the surrounding view, so they take their input from it and hand it on to the dialogs they open, and beside those channels they see the insertion channel the editor declares. Markup written to that channel is inserted at the cursor of the editor (`ReactWysiwygControl.insertAtCursor`, state `insert` = `{seq, html}`; the client inserts it once per `seq`, reports the resulting text, and the request is taken back). Commands placed in a toolbar — the default placement — are rendered as a `ReactToolbarControl` in state `toolbar`, which the client renders beside the formatting buttons.

  ```xml
  <field attribute="content">
    <input-control class="com.top_logic.layout.react.wysiwyg.WysiwygControlProvider"
      insert-channel="insert"
    >
      <commands>
        <generic-command image="css:ri-links-line" input="ticket">
          <label><en>Reference ticket...</en></label>
          <executability><null-input-disabled/></executability>
          <open-dialog dialog-view="tickets/reference-ticket.view.xml">
            <bind channel="context" to="ticket"/>
            <bind channel="result" to="insert"/>
          </open-dialog>
        </generic-command>
      </commands>
    </input-control>
  </field>
  ```

  The dialog picks whatever it likes in whatever way it likes (a table, a search, a tree) and publishes the markup on its result channel: `<generic-command input="ticket"><execute-script function="t -> htmlObjectLink($t)"/><write-channel name="result"/><close-dialog/></generic-command>` is the whole contract, and `context` gives it the object the text is written for. Nothing about the editor knows what an object link is: it inserts the markup it is handed.

The demo (`com.top_logic.demo.react`): the *Projects* drill-down (project → milestone → ticket → detail) with targets for its types in `demoReactConf.config.xml`, the contributor dialog target, `tl.accounts:Person` shown in the tiles demo, and the object-list comments, whose editor offers a "Reference ticket…" command opening `tickets/reference-ticket.view.xml`. The drill-down's second frame is the project home, a tab bar over the project's own data and its milestones; the milestone tab holds `projects/milestone-list.view.xml`, and a milestone target names that file to have the tab revealed inside the frame it pushed.

## UI inspector, diagnostics and assertions

The UI inspector is a side-window that shows the control behind an element of the application window as the *headless projection* sees it — the same view of the display the agent API observes and a recorded assertion is verified against. It is opened by a generic `<open-window>` command configured with its view, gated by `<inspect-enabled/>`:

```xml
<open-window
  height="640"
  image="css:bi bi-search"
  placement="TOOLBAR"
  singleton-key="ui-inspector"
  view="ui-inspector.view.xml"
  width="720"
>
  <title><en>UI inspector</en></title>
  <label><en>Inspect</en></label>
  <executability><inspect-enabled/></executability>
</open-window>
```

`InspectEnabled` (`com.top_logic.layout.view.command`) asks `GuiInspectorUtil.isGuiInspectorEnabled()`, so the command appears under exactly the configuration that enables the inspection of the classic UI: `DebuggingConfig.inspectEnabled`, wired in `top-logic.config.xml` to the `%tl_enable_debug%` alias (the `tl_enable_debug` environment variable, `false` unless `enable-debug.xml` is in effect), or an enabled scripting recorder. That is a deployment switch, not a permission — whoever works on a system that has it on sees the tooling regardless of their roles. The app bar of `com.top_logic.demo.react` carries it beside the script recorder (`script-recorder.view.xml`, the side-window that starts and stops a recording, lists its steps live and replays them one by one).

**Picking and navigating.** *Pick element* starts a pick on the window that opened the side-window (`ElementPicker`, `PickKind.CONTROL`): the user's next click in the application window reports the control that was hit, the inspector comes back to the front, and its `node` channel holds an `InspectedNode` — the window, the control's semantic address (`/appShell/panel/table[Demo]`) and the projection taken at it. *Parent* inspects the enclosing element (the address without its last segment), *Refresh* takes the same address anew, which is what shows a state that changed meanwhile: the projection is a snapshot, not a live binding.

**What the state table shows.** The header names the address and the control's role, name and React module; the table below lists the projected state flattened to one row per leaf value, addressed by the dotted path an assertion names it by (`diagnostics.hiddenByAccess.count`). It is the projection, not the React props: the keys a control declares as presentation-only are omitted, and a nested object contributes its leaves rather than one opaque row. The second table lists the actions the control offers — the commands a script may dispatch at that address, with their argument schema.

**Diagnostics never reach the browser.** `ReactControl.putDiagnostic(key, value)` records an observation *about* a control — how the displayed state came about, where the display itself cannot say so. Such observations appear in the projection under the `diagnostics` key (hence in the inspector, and capturable by an assertion) and never in the state sent to the client, because they are information for whoever inspects the UI, not for whoever uses it. The value is plain data (string, number, boolean, list or map of those), so it serializes as JSON like any projected state.

**`hiddenByAccess`.** A `<table>` records what the current user's read rights removed from its rows: `{count, byType}` under `diagnostics.hiddenByAccess`, absent when nothing was removed. The rows expression runs through a `QueryExecutor`, whose *result* is filtered for read rights — `TableElement` attaches a `SecurityFilterReport` to the `EvalContext` (`EvalContext.setSecurityReport`), which makes the filter record what it drops instead of dropping it silently. The filter sits on the value leaving the execution, not on every value an expression touches: a `get()` inside the expression yields what it yields, and only the result is filtered. A table showing fewer rows than its query found is therefore explained in the inspector, while the user sees only the rows.

**Recording an assertion.** The state table is multi-select, and the selected paths reach the view's `selection` channel (one path as that path, several as the set of them, none as `null`). *Record assertion* (`RecordAssertionAction`) turns them into a step of the recording running on the inspected window: it takes the part of the projected state those paths address (`InspectedNode.stateSubset`, which keeps the shape of the state, so paths sharing a prefix contribute to one nested object and a path the state no longer has is skipped) and appends an `AssertCommand` through `ScriptRecorder.recordAssertion` — the one place an assertion step is built, shared with the agent endpoint's `record/assert`. The step appears in the recorder side-window as `Check state of '<address>'`, between the interaction steps it belongs between. Without a running recording nothing is recorded and the inspector says to start the recorder first: an assertion is a step of a script.

On replay — the recorder's *Step* button (`ReactWindowReplay.verify`) or the agent's replay endpoint — such a step is verified rather than dispatched. `AssertCommand.mismatchingKeys(expected, actual)` compares the recorded entries against the node's live state as a *subset*: state the assertion does not name is ignored, values are compared by canonical JSON, and where both sides hold a map the expected map is compared entry by entry, so an assertion on `diagnostics.hiddenByAccess.count` still holds when a sibling entry of that group changes. The paths it reports are the ones the inspector showed, so a failure names the row the user selected.
