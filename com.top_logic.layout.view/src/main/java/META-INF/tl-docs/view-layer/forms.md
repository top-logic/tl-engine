---
description: Read before configuring input fields of a <form> or <field> (display variants of input fields, normalizing an entered value with <value-normalizer>, fields of derived attributes, scripts following the entered values) or when a channel write must ask about unsaved changes.
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
label.

Where `display` and `orientation` say nothing, the field decides: `FieldSpec.getSelectDisplay()` and
`FieldSpec.getSelectOrientation()`, which `FieldControlService` takes from the model annotations
`<classification-display value="radio|radio-inline|checklist"/>` (an enumeration) and
`<reference-display value="radio|radio-inline"/>` (a reference) - at the attribute, else at its
type. `radio` and `checklist` give vertical radio buttons, `radio-inline` horizontal ones, every other
presentation the dropdown.

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
otherwise - and `@ComparisonDependency` keeps `min` below `max`. The slider exchanges its value as a
number rather than as formatted text, so no locale format reads it.

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

An attribute annotated with `<value-normalizer>` (`TLValueNormalizer`) brings an entered value into its canonical form before it reaches the form's overlay: a code is trimmed, a rule is printed in its canonical notation. The field shows the canonical form once the input is left. A value the normalizer cannot canonicalize - a rule text that does not parse - stays as it was typed, and the constraints of the attribute report it.

```xml
<property name="code"
	type="tl.core:String"
>
	<annotations>
		<value-normalizer>
			<normalize-by-expression function="value -> obj -> $value.trim()"/>
		</value-normalizer>
	</annotations>
</property>
```

The function receives the entered value and the form's overlay of the edited object as `obj`, so it reads the values entered for the other attributes, not the stored ones. Only a value the user enters is normalized: the stored value is shown as it is, even if it is not in canonical form.

A `<value-input>` declares the normalizer in its own `<annotations>`; its function receives `null` as `obj`, since the input is bound to no object.

A text or password field with a normalizer sends its value when the input is left, not while the user types: a canonical form sent back mid-typing would replace what is being typed (a number field always does so).

A normalizer written in Java implements `ValueNormalizer`; it receives the edited object and the attribute (both `null` for a `<value-input>`) and returns the value unchanged when it cannot canonicalize it, instead of throwing.

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
