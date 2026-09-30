# Chart Demo Model Data - Implementation Plan

> **For agentic workers:** Use superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the static inline chart data in the demo with model-based data. Define a `demo.charts` module with Verkauf/Einkauf types, populate via setup XML, and write TL-Script expressions that compute chart configurations from model queries.

**Architecture:** New model module `demo.charts` with types `Transaktion` (abstract), `Verkauf`, `Einkauf`, and `Kategorie`. Initial data loaded via `.objects.xml`. Chart views use TL-Script expressions that query these objects and build Chart.js configs.

---

## File Structure

### New Files

```
com.top_logic.demo/
  src/main/webapp/
    WEB-INF/
      model/DemoCharts.model.xml              # Model: demo.charts module
      data/Z01-chart-demo.objects.xml         # Initial demo data (Kategorien, Transaktionen)
    views/demo/
      chart-demo.view.xml                     # Updated: model-based TL-Script expressions
      chart-detail-dialog.view.xml            # Updated: show Transaktion details
```

### Modified Files

```
com.top_logic.demo/
  src/main/webapp/
    WEB-INF/views/demo/chart-demo.view.xml    # Replace static data with TL-Script queries
    WEB-INF/views/demo/chart-detail-dialog.view.xml  # Show clicked Transaktion in form
```

---

## Task 1: Define model types

**File:** `com.top_logic.demo/src/main/webapp/WEB-INF/model/DemoCharts.model.xml`

### Model Design

```
demo.charts module:

  Kategorie (class)
    - name: String (mandatory)

  Transaktion (class, abstract)
    - datum: tl.core:Date (mandatory)
    - wert: tl.core:Double (mandatory)
    - kategorie: Kategorie (mandatory, reference)
    - beschreibung: String

  Verkauf extends Transaktion

  Einkauf extends Transaktion
```

- [ ] **Step 1: Create DemoCharts.model.xml**

```xml
<?xml version="1.0" encoding="utf-8" ?>

<model xmlns="http://www.top-logic.com/ns/dynamic-types/6.0">
    <module name="demo.charts">
        <annotations>
            <display-group value="demo"/>
        </annotations>

        <class name="Kategorie">
            <annotations>
                <table name="DemoCharts"/>
                <main-properties properties="name"/>
            </annotations>
            <attributes>
                <property name="name"
                    type="tl.core:String"
                    mandatory="true"
                />
            </attributes>
        </class>

        <class name="Transaktion"
            abstract="true"
        >
            <annotations>
                <table name="DemoCharts"/>
                <main-properties properties="datum, wert, kategorie"/>
            </annotations>
            <attributes>
                <property name="datum"
                    type="tl.core:Date"
                    mandatory="true"
                />
                <property name="wert"
                    type="tl.core:Double"
                    mandatory="true"
                />
                <reference name="kategorie"
                    type="Kategorie"
                    mandatory="true"
                    kind="forwards"
                />
                <property name="beschreibung"
                    type="tl.core:String"
                />
            </attributes>
        </class>

        <class name="Verkauf">
            <annotations>
                <table name="DemoCharts"/>
            </annotations>
            <generalizations>
                <generalization type="Transaktion"/>
            </generalizations>
        </class>

        <class name="Einkauf">
            <annotations>
                <table name="DemoCharts"/>
            </annotations>
            <generalizations>
                <generalization type="Transaktion"/>
            </generalizations>
        </class>
    </module>
</model>
```

**Note:** The `<table name="DemoCharts"/>` annotation maps all types to a single DB table. Check the existing `DemoTypes.model.xml` for the exact table annotation syntax — it may use a different format. Also check if a corresponding `DemoChartsMeta.xml` kbase file is needed, or if the model XML auto-creates the table.

- [ ] **Step 2: Verify model syntax by building**

```bash
mvn compile -DskipTests=true -pl com.top_logic.demo
```

- [ ] **Step 3: Commit**

```
Ticket #29108: Add demo.charts model with Kategorie, Verkauf, Einkauf types.
```

---

## Task 2: Create initial data

**File:** `com.top_logic.demo/src/main/webapp/WEB-INF/data/Z01-chart-demo.objects.xml`

Create data that produces interesting charts:

**5 Kategorien:** Elektronik, Kleidung, Lebensmittel, Software, Dienstleistung

**~30 Transaktionen** spanning 6 months (Jan-Jun 2025), mix of Verkauf and Einkauf:

| Monat | Verkauf (gesamt ca.) | Einkauf (gesamt ca.) |
|-------|---------------------|---------------------|
| Jan   | ~12k                | ~8k                 |
| Feb   | ~19k                | ~14k                |
| Mar   | ~15k                | ~10k                |
| Apr   | ~25k                | ~18k                |
| May   | ~22k                | ~16k                |
| Jun   | ~30k                | ~22k                |

Spread across categories. This produces the same visual shape as the current static bar chart but with real objects.

- [ ] **Step 1: Create Z01-chart-demo.objects.xml**

Follow the exact format from `X01-world.objects.xml`:
```xml
<?xml version="1.0" encoding="utf-8" ?>

<objects>
    <!-- Kategorien -->
    <object id="cat-1" type="demo.charts:Kategorie">
        <attribute name="name" value="Elektronik"/>
    </object>
    <object id="cat-2" type="demo.charts:Kategorie">
        <attribute name="name" value="Kleidung"/>
    </object>
    <!-- ... -->

    <!-- Verkauf Jan -->
    <object id="v-1" type="demo.charts:Verkauf">
        <attribute name="datum" value="2025-01-15"/>
        <attribute name="wert" value="5000.0"/>
        <attribute name="kategorie"><ref id="cat-1"/></attribute>
        <attribute name="beschreibung" value="Laptops Q1"/>
    </object>
    <!-- ... more Verkauf and Einkauf objects ... -->
</objects>
```

**Important:** Check how date values are formatted in existing objects.xml files. The format might be epoch millis or ISO date — verify against the TopLogic data import format.

- [ ] **Step 2: Delete `tmp/` to force data reload, then rebuild and start**

Since we're adding new model types AND initial data, the H2 database needs re-initialization:

```bash
rm -rf com.top_logic.demo/tmp/
mvn compile -DskipTests=true -pl com.top_logic.demo
```

Then start the app and verify the data exists by logging in and checking.

- [ ] **Step 3: Commit**

```
Ticket #29108: Add initial demo data for chart model (5 categories, ~30 transactions).
```

---

## Task 3: Update chart-demo.view.xml with TL-Script queries

Replace the static inline data with TL-Script expressions that query the model.

### Bar Chart: Monthly Revenue vs. Costs

TL-Script query pattern:
```
{
  'type': 'bar',
  'data': {
    'labels': ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'],
    'datasets': [
      {
        'label': 'Verkauf',
        'data': [
          all(`demo.charts:Verkauf`).filter(v -> $v.get(`datum`).dateMonth() == 0).sum(v -> $v.get(`wert`)),
          ... per month ...
        ],
        'metadata': [ ... aggregated objects per month ... ],
        'onClick': 'showDetails'
      },
      {
        'label': 'Einkauf',
        'data': [ ... same pattern ... ]
      }
    ]
  }
}
```

**Note:** The exact TL-Script syntax for date filtering and aggregation needs to be verified. Key functions to check:
- `all(type)` — query all instances of a type
- `.filter(predicate)` — filter objects
- `.sum(accessor)` — aggregate values
- Date functions: how to extract month from a date

The metadata array should contain the filtered transaction lists (as TL objects) so click handlers can show details.

- [ ] **Step 1: Research TL-Script date functions and aggregation**

Check existing TL-Script usage in the codebase for date filtering patterns.

- [ ] **Step 2: Write the TL-Script expressions for each chart**

**Bar Chart:** Monthly aggregation of Verkauf/Einkauf amounts.

**Doughnut Chart:** Category distribution of all Verkauf transactions.

**Line Chart:** Monthly trend (same data as bar but as lines).

**Combined Chart:** Revenue (bar) vs. profit margin (line, computed as Verkauf - Einkauf per month).

- [ ] **Step 3: Update chart-demo.view.xml**

Replace the four charts with model-based TL-Script expressions. Each chart's `data` attribute contains the query expression. No `<inputs>` channels needed if querying all instances globally.

- [ ] **Step 4: Test**

Start app, navigate to Chart Demo, verify all charts show data from the model objects.

- [ ] **Step 5: Commit**

```
Ticket #29108: Replace static chart demo data with TL-Script model queries.
```

---

## Task 4: Update chart-detail-dialog for Transaktion objects

When clicking a bar in the bar chart, the metadata should be a list of Transaktion objects for that month. The dialog should show them in a form or table.

- [ ] **Step 1: Update chart-detail-dialog.view.xml**

If the metadata per data point is a list of Transaktion objects, the dialog could show a simple table of those transactions. Use `<form>` with `<field>` elements if the metadata is a single object, or a `<table>` element with row expression if it's a list.

Alternatively, if metadata is the aggregated sum (a Number), keep the existing `<demo-value-list>`. But for real model data, passing the actual Transaktion objects is more meaningful.

**Decision:** The metadata for each monthly bar should be the list of Transaktion objects for that month. The dialog displays them as a table.

```xml
<view>
    <channels>
        <channel name="transactions"/>
    </channels>
    <window title="Transaktionen" width="600px">
        <table types="demo.charts:Transaktion"
            rows="transactions -> $transactions"
        >
            <inputs>
                <input channel="transactions"/>
            </inputs>
        </table>
        <actions>
            <button>
                <action class="...CancelDialogCommand" label="Close"/>
            </button>
        </actions>
    </window>
</view>
```

**Note:** The `<table>` UIElement might need adjustments to work inside a dialog. Verify with the existing table element implementation.

- [ ] **Step 2: Update bar chart handler to use `bind-input-to="transactions"`**

- [ ] **Step 3: Test click → dialog shows transaction list**

- [ ] **Step 4: Commit**

```
Ticket #29108: Show Transaktion table in chart click dialog.
```

---

## Summary

| Task | Description | Dependencies |
|------|-------------|-------------|
| 1 | Define demo.charts model (Kategorie, Verkauf, Einkauf) | — |
| 2 | Create initial data (~30 transactions, 5 categories) | Task 1 |
| 3 | Replace static chart data with TL-Script queries | Task 2 |
| 4 | Update dialog to show Transaktion table | Task 3 |

**Risk:** TL-Script date functions and aggregation syntax may need investigation. The exact API for `dateMonth()`, `all()`, `filter()`, `sum()` etc. should be verified against the TL-Script documentation or existing usage in the codebase.

**Important:** After adding model types, the `tmp/` directory must be deleted to force DB re-initialization. This is a one-time operation during development.
