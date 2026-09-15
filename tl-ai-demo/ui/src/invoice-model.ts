/**
 * The demo's invoice model: what the control puts into the state, and how a citation finds one.
 *
 * Separate from `invoices.tsx` so it can be tested - that file renders React and imports the host's
 * bridge, which no test has.
 */

/** One invoice of the demo model, as `DemoChatControl` puts it into the control state. */
export interface Invoice {
  /** The object's own id in the application's model. */
  id: string
  /** `description` of the invoice, or a fallback when it carries none. */
  name: string
  /** Formatted `date`, or empty. */
  meta: string
  /** Formatted `amount`, or empty. */
  state: string
}

/**
 * Reads the invoice list out of the control state.
 *
 * The list is the application's own data; when the type is not deployed, or nobody is signed in,
 * it is empty and the section says so rather than showing invented rows.
 */
export function parseInvoices(raw: unknown): Invoice[] {
  if (typeof raw !== 'string' || raw.trim() === '') return []
  try {
    const data = JSON.parse(raw) as Invoice[]
    if (!Array.isArray(data)) return []
    return data.filter((item) => typeof item?.id === 'string')
  } catch {
    return []
  }
}

/**
 * The invoice a citation points at.
 *
 * Matched on the object id first, then on the name: the id the agent quotes is whatever the tool it
 * read the invoice with put in its result, and for a TL-Script read of the demo model that is the
 * description rather than the internal id.
 */
export function invoiceOf(invoices: Invoice[], id: string, label: string): Invoice | undefined {
  const wanted = key(id)
  if (!wanted) return undefined
  return (
    invoices.find((item) => key(item.id) === wanted) ??
    invoices.find((item) => item.name.toLowerCase() === wanted) ??
    invoices.find((item) => item.name.toLowerCase() === label.trim().toLowerCase())
  )
}

/**
 * The comparable part of an object id.
 *
 * The same object is written three ways in this application: `ID(5371)` by the control's own
 * `tIdLocal()`, `GenericObject:5371` by a model tool, and `5371` by a script. What they share is the
 * number, so that is what is compared.
 */
function key(id: string): string {
  const trimmed = id.trim().toLowerCase()
  const digits = /(\d+)\D*$/.exec(trimmed)
  return digits ? digits[1] : trimmed
}
