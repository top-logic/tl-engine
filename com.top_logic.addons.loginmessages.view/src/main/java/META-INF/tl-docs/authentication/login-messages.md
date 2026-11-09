---
description: Read before showing messages to users at their login in a React application, or administering them - the module tl-addons-loginmessages-view (the view System -> Login messages, the login step login-messages contributed to LoginStepsConfig, the dialog login-messages/login-messages.view.xml), the model type LoginMessages:LoginMessage of tl-addons-loginmessages-core, the TL-Script functions loginMessagesDue, loginMessagesDisplay and loginMessagesConfirm, how a confirmation is stored in the personal configuration of the account, and why the login messages are marked internal.
---

# Login messages

A login message is a text an account is shown when it logs in - a notice of maintenance, changed
terms of use - until it confirms it. The type `LoginMessages:LoginMessage` and the logic deciding
which messages are due come from the UI-independent module `tl-addons-loginmessages-core`;
`tl-addons-loginmessages-view` adds both to the React UI and needs no Java of its own. An application
adds the module as a dependency. The classic UI's login hook and administration are in
`tl-addons-loginmessages`, which depends on the same core module.

## Administration

The overlay `WEB-INF/views/admin/admin.view.xml` adds the view "Login messages" to the section
"System" of the administration (tab `login-messages` in `system-views`, after `maintenance`), see
[overlays](doc:view-layer/overlays). It lists the messages and edits the selected one:

- `name` - the designation under which the message is administered; the messages are shown sorted
  by name in descending order.
- `message` - the rich text shown, in every language, with images.
- `active` - only an active message is shown.
- `startDate`, `endDate` - the days on which the message is shown; empty means unbounded.
- `confirmDuration` - after how many days a confirmation expires; empty means a confirmation
  lasts forever, `0` that the message is shown at every login.

"New" creates a message, "Delete" removes it, and "Ask again for confirmation" sets
`confirmExpirationDate` to now: every confirmation given before counts as expired, so every account
is shown the message again.

## The login step

`WEB-INF/autoconf/loginmessages.view.config.xml` adds a `<dialog-step name="login-messages">` to
the [login steps](doc:authentication#login-steps), after the second factor: only an account that
has proven itself is shown the messages. Its condition `!loginMessagesDue($account).isEmpty()`
lets accounts without a due message pass without a dialog. The dialog
`login-messages/login-messages.view.xml` shows the due messages one below the other; "OK" confirms
them and continues the login, closing the dialog cancels it. The session starts only after the
confirmation.

The dialog runs before the session of the account exists, for the anonymous user, who may not read
the login messages. It therefore does not display the model objects but the value of
`loginMessagesDisplay($account)`: the messages rendered on the server, the text in the language of
the visitor with its images inlined as `data:` URIs, shown with `<html>`. The value also records
which messages were shown, so that `loginMessagesConfirm($display)` confirms exactly these for the
account, not a message that became due in between.

## TL-Script

| Function | Result |
|---|---|
| `loginMessagesDue(account)` | The messages due for the account: active, today within their dates, not confirmed or the confirmation expired; sorted by name descending. Model objects, so a secured script of a user who may not read them gets none. |
| `loginMessagesDisplay(account)` | The due messages as the login shows them: HTML for `<html>`, and the messages shown. No model object. |
| `loginMessagesConfirm(display)` | Records the confirmation of the messages of the display, for its account, at the current time. |

The functions take the account as given; asking for or confirming the messages of another account
affects nothing but which messages that account is shown.

## Where a confirmation is stored

A confirmation is the date stored in the personal configuration of the account, under the name
`confirmedLoginMessage_<id of the message>` - the same place the classic UI's login hook uses, so
both UIs share the confirmations. Since the account has no session when it confirms, the functions
write the persistent personal configuration (`PersonalConfigurationWrapper`) directly. The session
started afterwards builds its personal configuration from the persistent one (setting the user of
the window resets it), so it carries the confirmation and writes it back at its logout. A session
of the same account that was already open before keeps its older copy; when it writes its personal
configuration back, a confirmation given meanwhile is lost and the message is shown again at the
next login.

## Access

The module marks the model module `LoginMessages` `internal` (see
[access configuration](doc:access-configuration#the-two-marks)): the messages are read by the
functions above, which are no access-checked sources, and administered by administrators, who are
not checked. An application that lets other users administer the messages grants the operations on
the module `LoginMessages` instead.
