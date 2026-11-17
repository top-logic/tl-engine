---
description: Read before defining model types of an application beyond the built-in ones - an application datatype (<datatype kind="Custom">) with its own value class, storage, label and input control - or before declaring in the model how the application reacts to changes of persistent objects (<change-handlers>, <lifecycle-handlers>).
order: 30
---

# Model types in applications

An application describes its data in `*.model.xml`: classes, enumerations and datatypes. The built-in datatypes of `tl.core` (strings, numbers, dates, …) cover most attributes. The articles of this chapter describe what an application does when a value needs a type of its own - how the value is stored, read, displayed and entered, and which engine extension points take part in that. The model also declares reactions to changes of persistent objects that run within the commit, whatever the source of the change.
