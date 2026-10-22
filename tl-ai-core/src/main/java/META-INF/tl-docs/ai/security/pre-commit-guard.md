---
description: Read before changing how a turn's writes are committed, or when a turn is rolled back with "not permitted to make all of them" - layer L4: PreCommitGuard / ModelSecurityPreCommitGuard in AgentLoop.commitTransaction, the change set ToolChangeSet read from the engine's transaction (attributes stored outside the object's row), the checks per created / updated / deleted object, canSetInitial, the committed view, and a commit the application refuses (TransactionException, rollbackTurn).
order: 50
---

# Pre-commit guard

The pre-commit guard is the layer that catches tools that never had a check: remote MCP tools, TL-Script tools authored at runtime, and any Java tool whose author forgot. Every other layer assumes the tool cooperates; this one does not.

**Where.** `AgentLoop.commitTransaction`, immediately before `scope.commit()`. There is exactly one commit per turn, so there is exactly one place to guard.

```java
public interface PreCommitGuard {

    /** Accepts every change. A test seam; forRun never answers with it. */
    PreCommitGuard PERMISSIVE = ...;

    /** The violations found; empty means the commit may proceed. */
    List<Violation> check(SecurityContext security, ToolChangeSet changes);

    /** Whether this guard checks anything at all - asked before the change set is built. */
    default boolean enforces() { return true; }

    /** Always a real guard: every turn that opened a transaction is checked. */
    static PreCommitGuard forRun() { ... }

    record Violation(TLObject object, String typeName, BoundCommandGroup operation,
        TLStructuredTypePart attribute, String reason) {}
}
```

`ModelSecurityPreCommitGuard` is the implementation. It adds **no policy of its own**: every question goes to the run's `ModelSecurity`, hence to the engine's `ModelAccessRights`, so a tool that bypassed its own check ends up bounded by exactly the rights the acting user has in the UI.

## The change set

`TransactionScope.changes()` reads the engine's own transaction bookkeeping - `DefaultDBContext.getNewObjects()` / `getChangedObjects()` / `getDroppedObjects()`, plus the per-item `ChangeInspectable.getChange()` record the engine also uses to build its commit event. Nothing asks a tool what it wrote: a tool that forgot to check would equally forget to report. The values in the change record are only compared - the engine reports the key columns of a table with every change of a row, and a key column whose value did not change is no write - and never kept, logged or quoted.

**An attribute is often not stored in the row of the object holding it.** Changing it books no change on that object; the engine creates, changes and drops the rows that store its values instead:

- the links of a reference - every reference the model does not configure into a column: an enumeration value, a reference of many values, a composition, a group's members;
- the rows that keep the translations of an `I18NString` or `I18NHtml` attribute, one per language;
- the rows of the parts of a composition that keeps the container in each part's own row - an inline storage, which is how `tl.doc` keeps the children of a page. Adding, removing, moving or reordering a part changes the part's row, not the container's.

`ToolChangeSet.pending` reads each such row as what it is - a write of the attribute whose value it stores - and books it on the object holding that attribute, so the guard asks `canWrite(holder, attribute)`. Which object and which attribute a row stores a value of is the engine's own description of its storages (`ModelTables.getDescriptorsForTable`, which its change log reads for the same question), so every `SeparateTableStorage` is covered at once, an application's own included. The row of a part kept in its own row is checked as a write of the part as well; a change of the part's own attributes, such as renaming a page, is no write of its container. Left out are the rows that go with the creation or the deletion of an object, which is checked as that: the rows of an object created or deleted in the turn, and a row dropped together with an object it refers to or with the part it is. Deleting an object therefore needs no right to write the objects that referred to it.

## The checks

| Change | Check |
|---|---|
| created | `canCreate(type, container)`, and `canCreate(parent, compositionAttr)` when created into a composition; then `canSetInitial(obj, attr)` for every attribute the turn gave a value |
| updated | `canWrite(obj)` and `canWrite(obj, attr)` for every touched attribute - an attribute kept outside the object's row included, on the object holding it |
| deleted | `canDelete(obj)` |

**On a violation** the whole turn is rolled back through `AgentLoop.rollbackTurn` - the *same* route a failed tool call takes, so all-or-nothing means the same thing in both cases - the guard writes one log record per violation, and the model is given a framework notice naming the operation and the type but **not** the values. The `Violation` names the attribute an attribute-level refusal was for; the notice and the log line name it only for the initial value of a new object, which is a value the turn gave itself:

> `[Framework notice] The data changes of this turn were rolled back and NOT persisted, because your account is not permitted to make all of them: you attempted 'Create on objects of type 'myapp:Contract'' for 3 objects without permission. The rollback is all-or-nothing, so the permitted changes of this turn are gone as well. ...`

**The checks run in the committed view** (`KnowledgeBase.withoutModifications`), forced by the engine in two ways. An object deleted in the current transaction refuses attribute access, while `ModelAccessRights` has to read its type and its security parent to decide - in the committed view the object is visible again, which is where its DELETE right is meaningful anyway. And an object created in the current transaction is allowed everything by construction, so asking about it would answer nothing.

**The initial values of a new object are asked in the transaction's own view**, after its creation passed: `canSetInitial` (engine `isAllowedInitial`) is the question the create dialogs ask about the object being created, and that object exists only there. An attribute counts as given a value unless its value is the one the engine creates the object with: the default the model gives it, or an empty value where the model gives none. The value is compared with the default first, so `false` given to an attribute whose default is `true` is a given value. A default that differs every time it is computed (a UUID, the current time) is computed once more, and the attribute is asked unless the value equals that result - in practice it is always asked. A default the engine computes in the transaction (a sequence number) cannot be computed again without drawing the next number, so such an attribute is always asked. An account that may not set such an attribute on a new object therefore cannot have an agent create that object, although the create dialog lets it.

**The rights of a freshly created object itself cannot be validated.** `ModelAccessRights.isAllowed` returns `true` for objects that are not committed yet (`SecurityConfigurationService.isCommitted`), because rule-derived roles are computed at commit time. What the guard validates for a creation is CREATE on the type in the committed parent context and WRITE on the composition attribute of the committed parent - the check the engine itself performs. A parent created in the same turn is covered by *its* creation being checked against *its* container, up to the first committed ancestor. Updates and deletes of existing objects are checked fully.

**What the layer does not cover:**

- The object-level rights of an object created in the same turn, per the paragraph above. Its creation and the initial values of its attributes are checked.
- Change-set items that carry no application type and store no value of an attribute: the engine's own bookkeeping items. A storage-level attribute that maps to no model attribute costs attribute granularity, not the check - the object-level right is always asked as well.
- Side effects outside the knowledge base. A tool that wrote a file, sent a mail or called an HTTP endpoint is not undone by a rollback. That is the business of L1 (`egress`) and of the approval prompt.

**Cost.** One `ModelAccessRights` call per touched object and per touched attribute, once per turn, plus one pass over the transaction's own change lists against the engine's description of its storages, which its model cache keeps. Turns that write nothing - the vast majority - skip the guard entirely, because `scope.isActive()` is false: `ensureTransaction` only begins a transaction for tools that are not READ.

## The commit path

```java
private void commitTransaction(ToolContext context, Conversation conversation,
        AgentEventListener listener, String runId, int turn) {
    TransactionScope scope = context.transaction();
    if (!scope.isActive()) {
        // No write tool ran this turn: nothing to commit and nothing to guard.
        return;
    }
    if (!preCommitGuardPasses(context, conversation, listener, runId, turn)) {
        return;
    }
    // ... commit ...
}
```

`preCommitGuardPasses` asks `PreCommitGuard.forRun(security)`, checks the change set, and on violations calls `rollbackTurn` with the notice above. `rollbackTurn` rolls the transaction back, emits `AgentEvent.TransactionRolledBack`, and appends the `[Framework notice]` that corrects the picture the model was left with. There is deliberately **one** such route - a second one would let "all-or-nothing" mean two different things.

### A commit the application refuses

A commit can also be refused by the application rather than by the guard: a model constraint, a mandatory attribute nobody filled, a `CommitChecker` of its own. That is no security decision, but it ends the turn the same way, through the same route. What makes it reachable is that **`TransactionScope.commit()` wraps every failure the engine can raise into a `TransactionException`** - the engine throws a `KnowledgeBaseException` for a poisoned or failed transaction, but rethrows the `TopLogicException` of a veto (`ModelService.checkCommit`) unwrapped.

| | |
|---|---|
| **Log** | `AgentLoop.commitTransaction` writes one ERROR line naming run, turn and cause. |
| **Model** | the `[Framework notice]` of `rollbackTurn`, naming the refusal, stating that every tool result of the turn is void, and telling it not to retry the identical change. |
| **User** | the run completes normally and the turn is reported rolled back. A failure that reaches the chat's catch-all is classified `ChatProcessor.APPLICATION_ERROR` rather than as a provider error, and the chat says the application could not complete the request - not that the model erred. |
| **Transcript** | `ConversationLogListener` marks the turn's writes `ToolCallStatus.ROLLED_BACK` with the reason, so a reloaded conversation does not replay a success no revision holds. |

The `StopReason` stays honest: a refused commit ends the turn, not the run, so the run still ends `COMPLETE` and no `ProviderError` event is emitted.

## Tests

`TestPreCommitGuard` and the transaction cases of `TestAgentLoop` in `tl-ai-core`; `TestToolChangeSet` (what a real transaction books for an attribute kept outside the object's row: links, translations, a composition kept in its parts' rows); `TestPreCommitGuardInATurn` (the guard at the end of a real agent turn); `TestToolChangeSetOnDocPages` in `tl-ai-demo` (the same reading on the pages `tl.doc` ships); `SecDemoPreCommitGuardTest` against a real boot of the test bed. A refused commit: `TestCommitFailureReason.testVetoedCommitFailsAsATransactionFailure`, `TestAgentLoop.testEngineCommitFailureIsNotAProviderError` and `TestChatErrorKind` in `tl-chat`.
