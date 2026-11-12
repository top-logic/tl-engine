---
description: Read before reacting in the model to changes of persistent objects, independent of the source of the change (form, import, service, script) - <change-handlers> on an attribute (TLChangeHandlers, TLChangeHandler, the TL-Script <script operation="..."> of ChangeHandlerByExpression), <lifecycle-handlers> on a type (TLLifecycleHandlers, TLLifecycleHandler with created() and the two-phase deleting(), the TL-Script <script created="..." deleting="..."> of LifecycleHandlerByExpression), their timing within the commit, reporting to the user with an AfterCommitAction, and the PendingChanges of a hand-written ModificationListener.
order: 20
---

# Reacting to changes in the commit

## When to use which mechanism

An application often keeps data consistent by a rule of the form "when X changes, adjust Y": a status change stamps a date, a new object gets a number, deleting an item updates a summary on its container. The model offers several places for such a rule. They differ in *when* they run and *which* changes they see:

| Mechanism | Declared at | Runs | Sees |
|---|---|---|---|
| `<value-listeners>` | attribute | while the user edits a form, before anything is stored | only edits in forms, see [reacting to a field change](doc:view-layer/forms#reacting-to-a-field-change-value-listeners) |
| `<object-initializers>` | type | in the commit of a creation, before all other reactions | only creations |
| `<change-handlers>` | attribute | in the commit of a transaction changing the value | every stored change of an existing object |
| `<lifecycle-handlers>` | type | in the commit of a creation, at each deletion | every creation and deletion |
| `ModificationListener` | Java, registered at the `KnowledgeBase` | in every commit | all changed rows of all tables |

Use a form-level `<value-listeners>` when the user should see the consequence while editing (resetting a dependent field) and may still change it. Use `<change-handlers>` and `<lifecycle-handlers>` when the rule must hold however the data is changed: by a form, an import, a REST call, a scheduled task or a TL-Script. Write a `ModificationListener` only for a rule that is not tied to one attribute or type, e.g. one that inspects all changes of a commit at once.

## Change handlers on an attribute

A `<change-handlers>` annotation (`TLChangeHandlers`) on an attribute lists handlers (`TLChangeHandler`) that are called when a transaction changing the attribute's value of an existing object is committed. The TL-Script handler `<script>` (`ChangeHandlerByExpression`) gets the changed object, the new value, the old value and the attribute, in the same order as a `<value-listeners>` script:

```xml
<property name="status"
	type="my.module:Status"
>
	<annotations>
		<change-handlers>
			<script>
				<operation><![CDATA[ticket -> newValue -> oldValue -> attribute ->
  $ticket.set(`my.module:Ticket#statusChanged`, now())]]></operation>
			</script>
		</change-handlers>
	</annotations>
</property>
```

A handler written in Java implements `TLChangeHandler` and is configured with its class:

```xml
<change-handlers>
	<handler class="my.app.StampStatusChange"/>
</change-handlers>
```

```java
public class StampStatusChange implements TLChangeHandler {
	@Override
	public void changed(TLObject object, TLStructuredTypePart attribute, Object oldValue,
			Object newValue, CommitActions commitActions) {
		object.tUpdateByName("statusChanged", new Date());
	}
}
```

The handlers of an attribute include those of the attributes it overrides, unless `ignore-inherited="true"` is set.

### Which attributes can have change handlers

A change is detected from the rows the transaction changes, so the attribute's values must be stored in the database: a property or reference stored in a column of the object's table, a reference stored in a link table or in the rows of the referenced objects, the reverse of such a reference, or a formatted text. A handler on a reverse reference is called for the referenced object when a forward reference to it is set or removed. An abstract attribute is accepted, its handlers are inherited by the attributes overriding it.

A derived (query), transient or otherwise computed attribute cannot have change handlers: loading the model rejects the annotation with "Changes of the values of '...' cannot be detected, since its values are not stored in the database."

## Lifecycle handlers on a type

A `<lifecycle-handlers>` annotation (`TLLifecycleHandlers`) on a class lists handlers (`TLLifecycleHandler`) that are informed about the creation and deletion of objects of the class and its specializations. The handlers of the generalizations are called first. The TL-Script handler `<script>` (`LifecycleHandlerByExpression`) has two optional scripts:

```xml
<class name="Item">
	<annotations>
		<lifecycle-handlers>
			<script>
				<created><![CDATA[item -> $item.set(`my.module:Item#log`, "created")]]></created>
				<deleting><![CDATA[item -> {
  name = $item.get(`my.module:Item#name`);
  owner = $item.get(`my.module:Item#owner`);
  if ($owner == null,
    null,
    x -> $owner.set(`my.module:Owner#lastDeleted`, $name)
  );
}]]></deleting>
			</script>
		</lifecycle-handlers>
	</annotations>
	...
</class>
```

In Java, a handler implements the methods it needs:

```java
public class UpdateOwner implements TLLifecycleHandler {
	@Override
	public void created(TLObject object, CommitActions commitActions) {
		object.tUpdateByName("log", "created");
	}

	@Override
	public Modification deleting(TLObject object, CommitActions commitActions) {
		String name = (String) object.tValueByName("name");
		TLObject owner = (TLObject) object.tValueByName("owner");
		if (owner == null) {
			return Modification.NONE;
		}
		return () -> owner.tUpdateByName("lastDeleted", name);
	}
}
```

### The two phases of `deleting`

A deletion is reported when the object is deleted, before the deletion is executed. At that moment the transaction is locked: the handler can read the object and its values, but must not modify anything. It returns the changes to make as a `Modification` (Java) or a function (TL-Script), which runs after the object and the objects deleted with it (e.g. the parts of a composition) have been removed, still within the deleting transaction. Everything the modification needs from the deleted object must be read in the first phase and captured, since the deleted object can no longer be read in the second.

TL-Script has no functions without parameter, so the returned function is called with `null` as its single argument. A script that changes nothing returns nothing (`null`); any other result that is not a function fails the deletion with an error.

## Timing

All handlers run within the transaction whose changes they react to. Their changes become part of it and are rolled back with it if the commit fails.

- **Creation and change** are reported once, in the commit, before the commit checks (e.g. the model constraints) run. The order is: the `<object-initializers>` of all created objects, then the `created` handlers, then the change handlers. A rule established by a handler is therefore checked by the constraints of the same commit.
- **The old value** of a change is the committed value, the value before the transaction. Setting a value and setting it back within the transaction is no change. For a multiple reference, old and new value are the whole collections.
- **Created and deleted objects** are not reported to change handlers. An object that is created and deleted in the same transaction is reported neither to `created` nor to `deleting`.
- **Changes made by handlers** do not trigger change or `created` handlers again; nothing loops. Deletions are the exception: they are reported with each delete, so an object a handler deletes is reported to the `deleting` handlers of its type as well.
- **Deletion** is reported immediately with each delete, not in the commit (see [the two phases of deleting](#the-two-phases-of-deleting)).
- **The model** that defines the change handlers and the `deleting` handlers is the model as committed before the transaction, since the changes of the transaction are not yet checked when they are reported (a deletion that a reference vetoes is only rejected later in the commit). A transaction that annotates or removes such a handler changes the handlers called from the next transaction on. The `created` handlers are those of the types of the created objects in the transaction.

The TL-Script handlers run without security checks, like the model constraints checked in the same commit: they are consistency logic declared in the model and may read and modify any object, independent of the rights of the user committing the transaction (an unsecured evaluation in the sense of R12 of [the Discover specification](doc:access-discover-spec#r12---secured-and-unsecured-script-kinds)).

## Reporting to the user

A handler cannot report to the user directly: the commit may still fail after it ran. It registers an `AfterCommitAction` with the `CommitActions` it is given, which runs only after the outermost transaction committed successfully, see [reporting implicit changes after the commit](doc:view-layer/commands#reporting-implicit-changes-after-the-commit):

```java
commitActions.afterCommit(changes -> InfoService.showInfo(I18NConstants.OWNER_UPDATED));
```

TL-Script has no access to the `CommitActions`. A TL-Script handler showing a message with `info()` shows it immediately, also if the commit fails afterwards.

## Writing a `ModificationListener`

A rule that spans several types or needs all changes of a commit at once is a `ModificationListener` added to the `KnowledgeBase` (`addModificationListener()`). Its `createModification(KnowledgeBase, CommitActions, PendingChanges)` receives the changes of the commit on the level of database rows:

- `getCreated()`, `getUpdated()`, `getRemoved()` - the items by their key, and `getCreated(table)`, `getUpdated(table)`, `getRemoved(table)` for the items of one table (`getTables()` names the tables with changes);
- `getChangedAttributes(item)` and `isChanged(item, name)` - the attributes of an updated item whose value differs from the committed one;
- `getCommittedValue(item, name)` - the value before the transaction of an updated or removed item.

Like a handler's `deleting`, the listener must not change anything while it is called: it collects what to do and returns it as a `Modification`, which the commit executes afterwards. `notifyUpcomingDeletion(KnowledgeBase, CommitActions, KnowledgeItem)` is the corresponding call for each deleted item. The answers of the `PendingChanges` are only valid during the call.
