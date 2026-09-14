import { React, useTLState } from 'tl-react-bridge'
import type { TLCellProps } from 'tl-react-bridge'
import { App, DefaultCitationPopover, bundle, toLanguage } from 'tl-chat-seams'
import type { ChatExtensions, Citation } from 'tl-chat-seams'
import { INVOICES, InvoiceCite, InvoiceList, InvoicePane, type InvoiceFixture } from './invoices'

const { useCallback, useMemo, useState } = React

/**
 * Host composition of the chat: a Rechnungen rail, a read-only invoice pane, and one quote chip.
 *
 * The control still binds the session. This component only registers as {@code DemoChat}.
 */
export function DemoChat(props: TLCellProps) {
  const state = useTLState()
  const labels = bundle(toLanguage(state.language))
  const [createNote, setCreateNote] = useState('')

  const onCreate = useCallback(() => {
    setCreateNote('Das Anlegen eines Dokuments ist in dieser Demo nicht angebunden.')
  }, [])

  const extensions: ChatExtensions = useMemo(
    () => ({
      railSections: [
        {
          id: 'invoices',
          title: 'Rechnungen',
          contributor: 'Demo',
          accent: true,
          badge: () => 1,
          render: (ctx) => (
            <>
              <InvoiceList
                invoices={INVOICES}
                onOpen={(invoice) =>
                  ctx.openInPane(<InvoicePane invoice={invoice} />, {
                    title: invoice.name,
                    backLabel: 'Rechnungen',
                    sectionId: 'invoices',
                  })
                }
                onCreate={onCreate}
              />
              {createNote ? <p className="demo-invoice-note">{createNote}</p> : null}
            </>
          ),
        },
      ],
      composerAddons: {
        chips: [
          {
            id: 'quote-1',
            label: '§ 3 Abs. 1',
            payload: 'Die Rechnung wird innerhalb von 14 Tagen fällig.',
          },
        ],
      },
      messageAnnotations: {
        render(_marker, citation, _message, openInPane) {
          if (citation?.kind === 'invoice') {
            return <InvoiceCite invoice={invoiceOf(citation)} openInPane={openInPane} />
          }
          return <DefaultCitationPopover citation={citation} labels={labels} />
        },
      },
    }),
    [createNote, labels, onCreate],
  )

  return <App {...props} extensions={extensions} />
}

function invoiceOf(citation: Citation): InvoiceFixture {
  return (
    INVOICES.find((item) => item.id === citation.id) ?? {
      id: citation.id,
      name: citation.label || citation.id,
      meta: citation.id,
      state: '',
    }
  )
}
