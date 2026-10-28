---
description: Read before configuring input fields of a <form> or <field> (display variants of input fields, files as chips or a list with <binary-display> and <accepted-types>, normalizing an entered value with <value-normalizer>, fields of derived attributes, scripts following the entered values), when a channel write must ask about unsaved changes, when saving, cancelling or switching the edit mode of a form with an action chain (<save-action>, <cancel-action>, <store-form-state>, <enter-edit-mode>, <leave-edit-mode>, <discard-form-state>, mode-switch="false"), and for the controls of a <config-form>.
order: 40
---

# Forms and input fields

## Display variants of input fields

A control provider takes options, so the same value is entered in a different shape without a new
element and without a new control: a selection as a cloud of toggles or as a bar of segments, a
number as a handle on a track, a truth value as a switch.

**A selection** — `SelectControlProvider`, option `display` (`SelectDisplay`): `dropdown` (the
default) offers the options in a list that opens on demand and is searched by typing, `chips` draws
every option as a toggle, `segmented` draws them as a bar of segments with a marker sliding to the
chosen one. One server control (`ReactDropdownSelectControl`) serves all three - it keeps the option
index and the value protocol and names the client component to draw the shape with
(`TLDropdownSelect`, `TLOptionChips`, `TLSegmentedChoice`, `TLChoiceGroup`) - so a shape showing
every option is handed the complete option list right away, having nothing to open at which it could
ask for it. The shapes showing every option suit a handful of options; a long list belongs in a
dropdown. `radio` offers every option as a radio button (a checkbox where several values can be
chosen), laid out by `orientation` (`vertical`, the default, or `horizontal`). `filter="false"`
drops the filter input of the dropdown, which then jumps to an option by typing the beginning of its
label. `order` (`OptionOrder`) states the order the options are offered in, whatever the shape:
`by-label` sorts them by their labels, `as-given` keeps the order their source gives them.

Where `display` and `orientation` say nothing, the field decides: `FieldSpec.getSelectDisplay()` and
`FieldSpec.getSelectOrientation()`, which `FieldControlService` takes from the model annotations
`<classification-display value="radio|radio-inline|checklist"/>` (an enumeration) and
`<reference-display value="radio|radio-inline"/>` (a reference) - at the attribute, else at its
type. `radio` and `checklist` give vertical radio buttons, `radio-inline` horizontal ones, every other
presentation the dropdown.

Where `order` says nothing, the source of the options decides through `FieldSpec.getOptionOrder()`
(`null` for the order as given): the classifiers of an enumeration and the constants of an enum
datatype are offered as given, the options of an expression (`<value-input options="…">`) in the
order it computes (unless the `<value-input>` states an `option-order` of its own), those of an `options-by-expression` annotation as its `alphabetical-order` says
(by label unless it is `false`), the instances of a class and the values of an option provider
configured at the control by label.

```xml
<field attribute="priority">
	<input-control class="com.top_logic.layout.view.form.SelectControlProvider"
		display="segmented"
	/>
</field>
```

**A number** — `NumberInputControlProvider`, option `display` (`NumberDisplay`): `input` (the
default) takes any number the format of the field reads, `slider` drags a handle along a track
between `min` and `max`, snapping to `step` (whole numbers where nothing is stated). A slider needs
its bounds, which `@MandatoryIf(other = @Ref(DISPLAY), value = NumberDisplay.SLIDER_NAME)` demands of
the configuration - `min` and `max` are mandatory as soon as `display` is `slider` and ignored
otherwise. The slider exchanges its value as a number rather than as formatted text, so no locale
format reads it. `unit` is written after the value and after both bounds (`12,5 %`).

`min`, `max`, `step` and `unit` are TL-Script expressions. A literal is a constant range:

```xml
<value-input
	type="tl.core:Integer"
	value="level"
>
	<input-control class="com.top_logic.layout.view.form.NumberInputControlProvider"
		display="slider"
		max="10.0"
		min="0.0"
		step="1.0"
	/>
</value-input>
```

With `inputs` (channels, optionally renamed with `as`), the expressions read the channel values as
variables without declaring them, and the range is computed again whenever one of the channels
changes - here from a dictionary the channel `target` holds:

```xml
<input-control class="com.top_logic.layout.view.form.NumberInputControlProvider"
	display="slider"
	inputs="target"
	max="$target['max']"
	min="$target['min']"
	unit="$target['unit']"
/>
```

A range a slider cannot travel - a bound that is no number, `min` not below `max`, `step` not above
zero - is refused when the configuration is instantiated if it is constant. A range computed from
inputs may be unusable for a while, e.g. while a channel is still empty: the slider is then
disabled until the channels yield a usable range. A slider with inputs resolves its channels in
the view it is created in; created outside of a view, it is refused.

**A truth value** — `BooleanControlProvider`, option `display` (`BooleanPresentation`): the box that
is ticked by default, `switch` for a handle sliding between the two states, `radio` and `select` for
a choice between labelled values. `switch` is also a presentation of the model, so an attribute that
is a switch everywhere says so once:

```xml
<property name="active"
	type="tl.core:Boolean"
>
	<annotations>
		<boolean-display presentation="switch"/>
	</annotations>
</property>
```

A value that may also be unknown (`tl.core:Tristate`, a tri-state field) stays a checkbox even where
a switch is asked for: a switch has no third position for "no value".

**A point in time** — `DatePickerControlProvider`: a date, a time of day or both
(`tl.core:Date`, `tl.core:Time`, `tl.core:DateTime`) are each edited in the browser's own input for
that part. All three are stored as a `Date`; the `ConfigType` annotation of the type says which part
a value holds (`ReactDatePickerControl.Kind`). The field exchanges its value in ISO form, so no
locale format reads it. The display format of the field decides both how a value reads in view mode
and the precision of the input: a format that writes seconds gives an input editing seconds, a
format without seconds an input editing minutes. The display format is the `<format>` annotation of
the attribute, else the default the engine gives attributes of that type - `short-time` for
`tl.core:Time` (attribute settings) and `medium-date-time` for `tl.core:DateTime` (the type's own
annotation). Neither writes seconds, so an attribute edits minutes unless its format writes them:

```xml
<property name="timeSeconds"
	type="tl.core:Time"
>
	<annotations>
		<format>
			<time style="medium"/>
		</format>
	</annotations>
</property>
```

A `<value-input type="tl.core:Time">` without a `<format>` takes the type's `short-time` format as
well and edits minutes; a `<format>` inside the `<value-input>` sets display and precision there.

**A text** — `TextInputControlProvider`, option `suggestions`: a TL-Script function
`self -> text -> caret -> entries` offering suggestions while the user types. `self` is the edited
object (empty in a field editing no attribute of an object, such as a `<value-input>`), `text` the
text typed so far, `caret` the position of the caret in it. An entry is a dictionary
`{"label": …, "text": …, "from": …, "to": …}` - its text replaces the characters from `from` up to
(excluding) `to`, the list shows its label - or a plain string inserted at the caret; the label
defaults to the text, the positions to the caret, and a malformed entry is skipped. The function
alone decides what fits: it filters by what is typed and names the range a suggestion replaces, the
field shows its entries as given, in their order. Typing asks for suggestions after a short pause,
Alt+ArrowDown and Ctrl+Space ask at once; ArrowUp and ArrowDown move through the list, Enter or a
click picks the active entry, Escape closes the list. Picking an entry enters its text as if it
were typed - the field treats it like any other input - and the value is never restricted to
the suggestions. The list is the part `Listbox` (see [parts](doc:view-layer/parts)), so a component
library draws it in its own style.

```xml
<field attribute="alarmCondition">
	<input-control class="com.top_logic.layout.view.form.TextInputControlProvider">
		<suggestions><![CDATA[self -> text -> caret -> {
	before = $text.subString(0, $caret);
	// An unfinished sensor reference: @ or @" followed by a name without closing quote.
	reference = regex('@"?([^"@]*)$').regexSearch($before).firstElement();
	if($reference == null, [],
		$self.get(`demo.temperature:DemoSite#sensors`)
			.map(sensor -> $sensor.get(`demo.temperature:DemoSensor#name`))
			.filter(name -> $name.stringContains($reference.regexGroup(1)))
			.map(name -> {
				"label": $name,
				"text": '@"' + $name + '"',
				"from": $reference.regexStart(),
				"to": $caret
			}))
}]]></suggestions>
	</input-control>
</field>
```

**Three ways to choose the provider**, in the order `FieldControlService` tries them:

1. `<input-control class="…Provider" …/>` inside a `<field>` (`FieldElement.Config.getInputControl()`)
   or inside a `<value-input>` (`ValueInputElement.Config.getInputControl()`) — the view decides, for
   this one place in the user interface.
2. The `<input-control>` annotation of the model attribute — the model decides, for every place the
   attribute is shown. `<boolean-display presentation="switch"/>` is the same decision said in the
   model's own vocabulary: it reaches the field description as
   `FieldSpec.getBooleanPresentation()`, which `BooleanControlProvider` follows where its own
   `display` says nothing; `<classification-display>` and `<reference-display>` reach
   `SelectControlProvider` the same way.
3. The type map of `FieldControlService` and, failing that, the `FieldControlRegistry` entry for the
   kind of value the type holds.

**Files** — a value of `tl.core:Binary` is a file. A single file is one file field
(`FieldControlRegistry.BINARY`, `ReactBinaryFieldControl`): an upload area while the field is
editable, a download link otherwise. The files of a multi-valued binary attribute are shown as file
chips by default (`FieldControlRegistry.FILE_CHIPS`, `ReactFileChipsControl`, client `TLFileChips`):
one chip per file, a button and a drop area adding several files at once. The model annotation
`<binary-display presentation="list"/>` (`BinaryDisplay.BinaryPresentation.LIST`) shows them as a
list with one file field per file instead (`ReactValueListControl` over `FieldControlRegistry.BINARY`),
whose add button uploads several files at once, one value per file. `data-item` (the default) and
`image-upload` both give the chips. The annotation `<accepted-types value="image/*, *.pdf"/>`
(`TLAcceptedTypes`, MIME types or file patterns) limits the files the file picker offers, for a
single and a multi-valued value alike. The annotation `<max-upload-size value="5m"/>`
(`TLMaxUploadSize`) limits the size of each file uploaded to the attribute; the application-wide
`max-upload-size` of the upload configuration still caps it, so the smaller of both applies. These
annotations are read at the attribute, else at its type, and act on a `<value-input>` of type
`tl.core:Binary` through its `<annotations>` as well. A
mandatory multi-valued binary value shows the required marker at the chips and, after a refused
save, the message of the mandatory check under them. In a table cell the chips render inline, while
the list presentation shows the file names and opens its file fields in a dialog. A multi-valued
binary attribute is persistent: its files are stored one row per file in the generic table
`BinaryAttributeValue` by `BinaryCollectionStorage`, the storage chosen automatically for a
multi-valued `tl.core:Binary` property.

```xml
<property name="attachments"
	mandatory="true"
	multiple="true"
	type="tl.core:Binary"
>
	<annotations>
		<accepted-types value="*.pdf"/>
		<binary-display presentation="list"/>
	</annotations>
</property>
```


The display is how the field looks, not what it says: it is a rendering-only state key, kept out of
the headless projection, so a scripted test reads the same options and the same value in every shape.

## Unsaved changes are asked before a channel write, transitively

A form with unsaved input blocks the write of the channel it is bound to: it registers a `ViewChannel.VetoListener` there, and a veto listener answers with the `List<StateHandler>` it objects with, so the dialog asks about every form blocking the change at once (`ViewChannel.dirtyHandlers()` collects the answers, each handler once). A component that writes a channel *from a `ChannelListener` of another one* hands the question on with `VetoForwarder.forward(source, target)` and drops the forwarder in a cleanup action, so the unsaved changes blocking the target are reported when the source is asked — before the source is written. The object list forwards from each of its inputs to the channel holding the draft of the `<new-element>` content, the `<adaptive-detail>` from each `reset-on` master to its selection, the flow diagram from each input to the selection a rebuild drops, and a `<derived-channel>` from its inputs to the derived value a form may be bound to. The `ChannelVetoException` carries a continuation that retries the write of the *source*, so after a discard every listener of the source runs, including the one writing the target. A veto raised from inside a notification is a programming error: the notifying channel already holds its new value, the listeners after the writing one are never told, and the continuation retries the nested write alone.

## Validation

A `<form>` in edit mode checks the constraints of the model on the server: it creates a `FormValidationModel` over the object being edited, which derives the checks of every attribute of the object's type and runs them on every value change. There is no client-side validation, and a view declares nothing for it - the constraints come from the model:

- `mandatory="true"` of the attribute (`MandatoryConstraintCheck`),
- `<size-constraint lower-bound upper-bound/>` (`SizeConstraintCheck`) and `<value-range min max/>` (`RangeConstraintCheck`),
- every `ConstraintCheck` of the attribute's `<constraints>` annotation, e.g. `<unique/>` or a `<constraint-by-expression>`.

```xml
<property name="sizedString"
	type="tl.core:String"
>
	<annotations>
		<size-constraint
			lower-bound="3"
			upper-bound="50"
		/>
	</annotations>
</property>

<property name="warningField"
	type="tl.core:String"
>
	<annotations>
		<constraints>
			<constraint-by-expression type="warning">
				<check><![CDATA[value -> obj -> {
	if ($value != null and $value.length() > 20,
		#("Consider keeping text under 20 characters."@en, "Text sollte unter 20 Zeichen bleiben."@de),
		null)
}]]></check>
			</constraint-by-expression>
		</constraints>
	</annotations>
</property>
```

A check reads the values entered in the form, not the stored ones: it receives the form's overlay of the object, and an expression navigating to another object that the form also edits reads that object's entered values as well (`OverlayLookup`). Which attributes a check reads is recorded each time it runs, so a check depending on another field - a total compared with a limit, a code unique within a category - runs again when that field changes, including dependencies that only arise from the values currently entered.

**Errors and warnings.** A check is of type `error` (the default) or `warning`. All checks of an attribute run and their results are collected, except that a failed mandatory check skips the others (a length or a range of an empty value says nothing useful). The field displays its first error and all of its warnings. Model checks report at a field only once it is revealed - after the user changed its value, or after an attempt to save - so a create form does not open covered with "mandatory" errors; an input the field cannot parse (a number field holding text) is reported at once.

**Only errors block saving.** `<store-form-state/>` (and the form's own save) runs every check once more - a check consulting stored data, such as `<unique/>`, may have a different outcome since the value was entered - reveals all fields and stores nothing while an error remains; warnings never prevent saving. Commands guard against the errors in advance with the executability rule `<form-valid/>` (`FormValid`), which disables a command while the form displays an error. A command in the `<commands>` of a `<form>` whose action chain applies the form state gets the rule automatically; a command outside the form's commands - a button in a wizard step, for example - states it in its `<executability>`:

```xml
<button appearance="primary">
	<action class="com.top_logic.layout.view.command.GenericViewCommand">
		<label>
			<en>Next</en>
			<de>Weiter</de>
		</label>
		<executability>
			<wizard-has-next/>
			<form-valid/>
		</executability>
		<store-form-state/>
		<wizard-next/>
	</action>
</button>
```

The rule counts only errors on display: an error the user cannot see yet leaves the command enabled, and the attempt to execute it is what reveals the error.

**A value without an object.** A `<value-input mandatory="true">` reports a missing value without a form: its field is marked mandatory and shows the message of the mandatory check (`MandatoryConstraintCheck`) once the user empties it, and its `<on-submit>` command does not run on the empty value (see [view-owned values](doc:view-layer/basics)). The other constraints - `<constraints>`, `<size-constraint>`, `<value-range>` - are checks over an object and need a `<form>` over one.

**A field the object does not have is hidden.** A `<field attribute="x"/>` whose attribute the type of the displayed object lacks - a form over a selection that may hold objects of several types - is not displayed, label included, and appears again when an object with that attribute is displayed. A field is equally hidden while the form displays no object or a deleted one, and when the attribute is hidden by its visibility annotation.

## Normalizing an entered value

An attribute annotated with `<value-normalizer>` (`TLValueNormalizer`) brings an entered value into its canonical form before it reaches the form's overlay: a code is trimmed, a rule is printed in its canonical notation. The annotation lists normalizers, applied in the given order, each one to the result of the one before. The field shows the canonical form once the input is left. A value a normalizer cannot canonicalize - a rule text that does not parse - stays as it was typed, and the constraints of the attribute report it.

```xml
<property name="code"
	type="tl.core:String"
>
	<annotations>
		<value-normalizer>
			<trim/>
			<upper-case/>
		</value-normalizer>
	</annotations>
</property>
```

The built-in normalizers (`com.top_logic.model.normalize`) rewrite a text:

| Tag | Result |
|---|---|
| `<trim/>` | the text without white space at its beginning and end |
| `<collapse-whitespace/>` | the trimmed text, each inner run of white space (line breaks and tabs included) replaced by one space |
| `<lower-case/>`, `<upper-case/>` | the text in lower / upper case, independent of the user's language |
| `<empty-to-null/>` | no value for an empty text; after `<trim/>`, a blank entry is stored as no value |

A value that is not a text - a number, a date, an object, no value - is kept. The values of a multiple attribute are normalized one by one: a list stays a list in its order, a set stays a set, and an element normalized to no value stays in the collection as `null`.

Any other normalization is a script, one entry of the list like the built-ins:

```xml
<value-normalizer>
	<normalize-by-expression function="value -> obj -> $value.trim()"/>
</value-normalizer>
```

The function receives the entered value and the form's overlay of the edited object as `obj`, so it reads the values entered for the other attributes, not the stored ones. Only a value the user enters is normalized: the stored value is shown as it is, even if it is not in canonical form.

A `<value-input>` declares the normalizers in its own `<annotations>`; a script receives `null` as `obj`, since the input is bound to no object.

A text or password field with a normalizer sends its value when the input is left, not while the user types: a canonical form sent back mid-typing would replace what is being typed (a number field always does so).

A normalizer written in Java implements `ValueNormalizer`; it receives the edited object and the attribute (both `null` for a `<value-input>`) and returns the value unchanged when it cannot canonicalize it, instead of throwing. A text normalizer extends `TextNormalizer`, which handles non-text values and collections.

## Saving with an action chain

A `<form>` offers Edit, Save and Cancel in the toolbar of the enclosing panel by itself. Save stores the input if there is any and ends the edit session (`FormControl.executeSave()`), Cancel drops the input and ends it (`FormControl.executeCancel()`). `<save-action>` and `<cancel-action>` replace what the two buttons do with an action chain, written like the actions of a `<generic-command>` (see [commands](doc:view-layer/commands#a-command-is-a-chain-of-actions-over-named-arguments)). The chain runs in the form's context without arguments; `<read-form-object/>` results in the object the form displays, and `<store-form-state/>` in the object it stored, which their follow-up actions in `<with-result>` see (see [actions with a result](doc:view-layer/commands#actions-with-a-result-with-result)). Save stays disabled while the form displays an error.

Four actions work on the edit session of the form they run in:

- `<store-form-state/>` (`StoreFormStateAction`) checks the input, reveals every error and aborts the chain while one remains; otherwise it stores the input and results in the stored object. The edit session goes on, now without unsaved changes. Inside a `<with-transaction>`, the storing joins that transaction, and the scripts of its `<with-result>` and after it in the same `<with-transaction>` are committed together with the stored values - or not at all.
- `<leave-edit-mode/>` (`LeaveEditModeAction`) ends the edit session without storing or dropping anything. A form that still holds unsaved changes refuses: the chain aborts and the form stays in edit mode with its input.
- `<discard-form-state/>` (`DiscardFormStateAction`) drops the input and ends the edit session.
- `<enter-edit-mode/>` (`EnterEditModeAction`) starts the edit session like the form's own Edit button: it locks the object and is subject to the edit permission, the right to write the object and the rules of `<edit-executability>`. A form that shows no object, or whose permission refuses, aborts the chain, so no later action runs on a form that cannot be edited. A form already in edit mode keeps its session.

A save that post-processes the stored values stores them and runs its script over the stored object in one transaction, and leaves the edit mode after it:

```xml
<form input="ticket">
	<save-action>
		<with-transaction>
			<store-form-state>
				<with-result as="ticket">
					<execute-script function="... $ticket ..."/>
				</with-result>
			</store-form-state>
		</with-transaction>
		<leave-edit-mode/>
	</save-action>
	...
</form>
```

`<leave-edit-mode/>` belongs outside the `<with-transaction>`: inside it, the form would show view mode before the commit, and a commit that fails would have ended the edit session already. Like `<enter-edit-mode/>` and `<discard-form-state/>`, it changes the state of the view, so placing it inside the transaction is reported as a configuration error (see [transactions](doc:view-layer/commands#transactions-with-transaction)). After it, a failed commit aborts the chain before the form leaves edit mode, so the form stays in edit mode.

The actions find the form only where the form runs them: in its `<save-action>` and `<cancel-action>`, and in the commands of its own `<commands>`. A command in the `<commands>` of a surrounding `<panel>` runs outside the form, and the four actions have no effect there. A form with `mode-switch="false"` offers no Edit, Save and Cancel of its own, so its `<commands>` can provide them with these actions. The form publishes its mode on the channel named by `editMode`, and the commands decide on it whether they are shown. The tickets demo derives the ticket in view mode and in edit mode from that channel and hides each command while its main argument is empty:

```xml
<channels>
	<channel name="ticket"/>
	<channel name="ticketEditMode"/>
	<derived-channel name="viewedTicket"
		expr="if($editing == true, null, $ticket)"
		inputs="ticket, ticketEditMode as editing"
	/>
	<derived-channel name="editedTicket"
		expr="if($editing == true, $ticket, null)"
		inputs="ticket, ticketEditMode as editing"
	/>
</channels>
...
<form editMode="ticketEditMode" input="ticket" mode-switch="false">
	<field attribute="name"/>
	<commands>
		<generic-command inputs="viewedTicket" placement="TOOLBAR">
			<label><en>Edit</en></label>
			<executability><null-input-hidden/></executability>
			<enter-edit-mode/>
		</generic-command>
		<generic-command inputs="editedTicket as ticket" placement="TOOLBAR">
			<label><en>Save</en></label>
			<executability><null-input-hidden/></executability>
			<with-transaction>
				<store-form-state/>
				<execute-script function="... $ticket ..."/>
			</with-transaction>
			<leave-edit-mode/>
		</generic-command>
		<generic-command inputs="editedTicket" placement="TOOLBAR">
			<label><en>Cancel</en></label>
			<executability><null-input-hidden/></executability>
			<discard-form-state/>
		</generic-command>
	</commands>
</form>
```

A command of the form's `<commands>` that stores the form state is disabled while the form displays an error (`<form-valid/>`, see [validation](#validation)) without stating it.

## Derived fields

A `<field>` of a derived attribute - one computed by a `<storage-algorithm>`, such as a `<query>` - follows the values entered in the form. While the form is edited, the field computes the attribute over the form's edits instead of the stored values, and computes it again when a field it reads changes. This holds through a chain of derived attributes as well: a `headline` reading a derived `statusLabel` that reads the `status` reference changes as soon as another status is selected, before anything is saved. A derived field is never dirty - it holds no input of its own and is not stored - but it announces each change of its value, so whatever depends on it follows in turn.

A derived attribute may also be implemented in Java, by a storage derived from `AbstractDerivedStorage` configured as `<storage-algorithm><implementation class="..."/></storage-algorithm>`. In a form, the storage is called with the edited object itself, not with the form's overlay, so it may cast it to the generated interface and use the generated getters; its reads are traced and redirected to the form's edits all the same. The demo's `HeadlineStorage` computes the `headline` of a ticket from `getName()` and `getStatusLabel()`, the latter a derived attribute computed by a script, so selecting another status changes the headline through both kinds of derived attributes.

The same applies to every script a form evaluates for an object it edits: a `dynamic-visibility` mode selector, an options expression, a constraint check. Each records the attributes it reads (directly or through derived attributes), reads their entered values and is evaluated again when one of them changes, so a field appears, disappears or changes its options while the user is still typing:

```xml
<property name="resolution"
	type="tl.core:String"
>
	<annotations>
		<dynamic-visibility>
			<mode-selector class="com.top_logic.model.search.providers.ModeSelectorByExpression"
				function="t -> editMode -> if(demoTicketsIsClosed($t), if($editMode, 'editable', 'read-only'), 'hidden')"
			/>
		</dynamic-visibility>
	</annotations>
</property>
```

A TL-Script function implemented in Java, such as `demoTicketsIsClosed` above, takes part in this without any cooperation: it reads the attributes of its arguments through `tValue(...)`, `tValueByName(...)` or generated getters like any other Java code. The form evaluates its scripts in a `TracingScope`, which reports every attribute read made on the evaluating thread as a dependency and answers it from the values entered in the form. In edit mode, the function receives the form's overlay of an object, so it declares its parameter as `TLObject` rather than a generated interface.

## Configuration forms

A `<config-form>` edits a `ConfigurationItem` instead of a model object. The control of each property is chosen by the annotations stating what its value means (`@MultiLine`, `@Code`, `@Html`, ...) and by its value type, through maps of the `ConfigControlService` that modules extend; see [controls of configuration forms](doc:config-form-controls).

The item comes from the channel named by `input`, or, with `type` and no value on the channel, is a fresh item of that configuration interface. With `withEditMode="true"` (the default) the form is read-only until Edit starts a working copy of the item; the commands go into the toolbar of the enclosing panel. Without a `<save-action>`, Apply checks the copy and carries it over into the item, Cancel drops it. `withEditMode="false"` writes every change straight into the item, for a caller with a save cycle of its own (e.g. a dialog whose command runs `<check-config-form/>` before saving, guarded by `<config-form-valid/>`).

A `<save-action>` persists what was edited: the command is then called Save, and the chain runs over the working copy. The copy is its argument, named after the `input` channel, or `config` for a form without one, so a Java `ViewAction` receives it as input and a script reads its properties with `$config['name']`. Save first checks the copy as Apply does and is disabled while a field shows an error. Only once the chain ran through is the copy carried over into the item and edit mode left; a chain that fails (an exception, a `TopLogicException`) or is aborted (e.g. by `<notify stop="true"/>`) leaves the form in edit mode with the edits intact, and the error is reported like that of any command. A `<cancel-action>` runs with the same argument before Cancel drops the copy.

```xml
<config-form input="service">
	<save-action>
		<action class="my.app.StoreServiceConfiguration"/>
		<notify expr="#('Saved.'@en, 'Gespeichert.'@de)"/>
	</save-action>
</config-form>
```

Whether Edit is offered is decided by `<edit-executability>` rules, as for a `<form>`: they decide over the displayed item, which a `<visible-if>` sees under the name of the `input` channel (`config` without one); a hidden or disabled verdict hides or disables Edit, and the form refuses a client's request to start editing nonetheless. A view that shows a configuration only where it may be changed puts the two cases under two `<visible-if>` elements, the read-only form with `<edit-executability><visible-if expr="false"/></edit-executability>` (see `admin/system/services/service-detail.view.xml`).

A configuration form takes part in the [unsaved-changes protocol](#unsaved-changes-are-asked-before-a-channel-write-transitively) like a `<form>`: it holds unsaved changes while its working copy differs from the item (compared by value). Then it vetoes the write of its `input` channel and reports itself to the dirty-tracked scope enclosing it, e.g. the frame of a `<tile-stack>`, whose pop it blocks (`ConfigFormStateHandler`). In the question that follows, Save runs the form's save including its `<save-action>`, Discard drops the copy.
