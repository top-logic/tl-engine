---
description: Read before building a user interface for working on processes (tl.bpe.execution:ProcessExecution, tl.bpe.execution:Token) from Java (ProcessExecutions, Decision, GuiEngine, FlowConditions) or TL-Script (bpeTasks, bpeStartEvents, bpeStartEvent, bpeStartEventLabel, bpeModelType, bpeStart, bpeStartTask, bpeOptions, bpeDefaultOption, bpeOptionLabel, bpeErrors, bpeWarnings, bpeFormType, bpeFinish) - listing the open tasks of a user, starting a process, choosing how to continue after a task and completing it.
order: 20
---

# Working on processes

A running process is a `tl.bpe.execution:ProcessExecution`; each of its open steps is an active `tl.bpe.execution:Token` on a node of the workflow. A *task* is a token a user works on (`userRelevant`), the user being an *actor* of the lane the node lies in (`GuiEngine.isActor`: a member of one of the lane's `actorGroups` or a result of its `actorRule`).

`ProcessExecutions` (`com.top_logic.bpe.execution.engine`) offers the operations of a task list and a task screen; the classic execution UI of `tl-bpe-classic` and the TL-Script functions of `BPEFunctions` (`com.top_logic.bpe.execution.script`) build on it.

The label of an execution is given by the `<label>` annotation of `tl.bpe.execution:ProcessExecution`
(*workflow - participant*); the model type of a participant gives its processes a label of their own
with a `<label>` annotation, e.g. a `LabelProviderByExpression` naming the data of the process.

## Transactions

As for [workflow definitions](doc:bpe/workflow-definitions#transactions), no operation opens a transaction: a `.view.xml` calls the modifying functions (`bpeStart`, `bpeFinish`) inside a `<with-transaction>`.

## Operations

| Java (`ProcessExecutions`) | TL-Script | Effect |
|---|---|---|
| `tasks(person)` | `bpeTasks(person: currentUser())` | the open tasks of a user, over all running processes |
| `startEvents(person)`, `mayStart(person, event)` | `bpeStartEvents(person: currentUser())` | the manual start events of all released workflows the user may start a process with: the user is an actor of the process or of the lane holding the event |
| `startEvent(workflowName, eventName, person)` | `bpeStartEvent(workflowName, eventName: null)` | the start event of the released workflow of that name (the event name may be omitted for a single manual start event) the user may start a process with; `null` if there is none, so a command starting a process of that workflow is disabled; refused if several match |
| `startEventLabel(event)` | `bpeStartEventLabel(event)` | the name of the event's workflow, followed by the event's name if the workflow has several manual start events: the label of a start event in a choice among all |
| `ExecutionEngine.getModelType(event)` | `bpeModelType(event)` | the type of the execution a start event starts: the participant's `modelType`, `tl.bpe.execution:ProcessExecution` without one |
| `start(event, process, person)` | `bpeStart(event, process: null)` | makes `process` the execution - a transient object of the model type is made persistent with its values, a persistent one not started yet is taken as it is; without `process` a new one is created -, binds it to the process and runs it to its first task; refused if the user may not start it or `process` is not of the model type, see [starting with a creation form](#starting-with-a-creation-form) |
| `startTask(execution)` | `bpeStartTask(execution)` | the single active token of a just started process, `null` if there is none or more than one |
| `decisions(token)`, `decision(token, option)`, `decisionOrFail(token, option)` | `bpeOptions(token)`, `bpeDefaultOption(token)` | the choices for completing a task, see below |
| `Decision.getLabel()` | `bpeOptionLabel(token, option)` | title or name of the option's edge, else the name of its target, else a generic text |
| `Decision.getErrors()` | `bpeErrors(token, option)` | messages of the failing blocking conditions on the option's path |
| `Decision.getWarnings()` | `bpeWarnings(token, option)` | messages of the failing warning conditions on the option's path, to be confirmed |
| `Decision.getFormType()` | `bpeFormType(token, option)` | type of additional data entered with the option (edge form type of the last, then the first edge, then of the participant) |
| `finish(token, option, additional, person)` | `bpeFinish(token, option: null, additional: null)` | walks the option's path, handing `additional` to its operations (see [additional data](#additional-data)); results in the single task of the same process the user is an actor for, `null` otherwise |

`bpeErrors` and `bpeWarnings` result in texts already in the user's language, so a view joins them for a `<confirm>` or `<notify>`: `$warnings.join('\n')`.

## Options

An *option* is a `tl.bpe.bpml:Edge`, the last edge of the path it stands for (`Decision.getLastEdge()`):

- an outgoing edge of the task leading to a task or an end event;
- an outgoing edge of the task leading to an automatic gateway (`automatic` exclusive gateway, parallel gateway);
- for an outgoing edge leading to an exclusive gateway the user decides at (`GuiEngine.needsDecision`), each outgoing edge of the gateway whose `HIDDEN` conditions hold. The path is the edge into the gateway followed by the chosen edge.

`bpeOptions` orders the options by the `weight` of their edge first, then by label. Options blocked by errors are listed, so the user sees why they are impossible. `bpeDefaultOption` is the only option, or the default flow of the gateway. `bpeFinish` without an option takes the only option and is refused for a task with several.

## Additional data

An option whose `bpeFormType` is not `null` asks the user for additional data: an object of that
type, usually a transient one a creation form has filled (the classic user interface creates it with
`TransientModelFactory`, the React UI with `new(type, transient: true)`). `finish` hands it to the
operations walking the path - the `defaultOperation` of the participant (unless the last flow
`skipStandardOperation`) and the `operation` of each sequence flow -, which get the process
execution, the finished token, the new token and the additional object:

```
process -> finished -> next -> additional -> $process.set(`my:Request#rejection`, $additional.copy(transient: false))
```

The engine stores the object nowhere itself; an operation keeps what the process needs of it. The
form type of a sequence flow is written as `<tl:formType value="my:Rejection"/>` in a BPML file.

## Conditions

The conditions of a sequence flow (`rule` of `tl.bpe.bpml:SequenceFlow`, `SequenceFlowRule` with `ConfigurableCondition`s) are evaluated by `FlowConditions`: a failing `DEFAULT` condition blocks the edge, a failing `HIDDEN` condition hides it from the options, a failing `WARNING` condition asks for a confirmation. An automatic exclusive gateway takes the first outgoing edge whose `DEFAULT` and `HIDDEN` conditions hold, else its default flow.

## Refusals

`start` and `finish` fail with a `TopLogicException` the user can read when the user may not start the process, the task is no longer active, the user may not complete it, the option is not offered (or none is given for a task with several), or errors block the option. A task may be completed by its actors and, as the first task of a process shown in the start dialog, by the user who started the process.

## Starting with a creation form

A process whose model type has mandatory attributes is started from a creation form: the view
creates a transient object of `bpeModelType(event)`, the form edits it and checks its constraints,
and `bpeStart(event, $draft)` makes it persistent as the execution and starts the process in the
same transaction. An object that is not an instance of the model type, or that is already the
execution of a process, is refused. The persistent object is created without a check of the right
to create an object of the type - whether the user may start the process is decided by the actors
of the start event - and gets the defaults computed in the creating transaction, such as the
`executionNumber`.

## Starting with the first task

A start event whose `requireReview` is `false` merges its first task into the start dialog: the dialog edits the new execution with the form of that task, and completes the task right after the start. A view does the same with the following chain: `bpeStart($event)` in a `<with-transaction>`, then `bpeStartTask($execution)`, the choice among `bpeOptions($task)` (with the confirmation of `bpeWarnings`), and `bpeFinish($task, $option)` in a second transaction. A start event with `requireReview` leaves the first task in the task list of its actors.
