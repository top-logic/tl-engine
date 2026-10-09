import { React, useTLState, TLChild, FillBarrier, rootClassName } from 'tl-react-bridge';
import type { TLCellProps, CardStateJson, ChildControlJson } from 'tl-react-bridge';
import Card from '@mui/material/Card';
import CardHeader from '@mui/material/CardHeader';
import CardContent from '@mui/material/CardContent';
import Stack from '@mui/material/Stack';
import type { SxProps, Theme } from '@mui/material/styles';

/** The elevation of an elevated card, the one MUI gives a card by default. */
const ELEVATION = 1;

/**
 * The card keeps the height of its content in a column that is short of room, as TLCard does,
 * instead of being squeezed below it.
 */
const CARD_SX: SxProps<Theme> = { minHeight: 'min-content' };

/** The title of the header: one size below the MUI default, the size of a card in a page. */
const TITLE_VARIANT = 'subtitle1';

/**
 * The space around the content per padding, in units of the MUI spacing; also below the content,
 * where `CardContent` would otherwise add space of its own.
 */
const PADDING_SX: Record<CardStateJson.Padding, SxProps<Theme>> = {
  default: { p: 2, '&:last-child': { pb: 2 } },
  compact: { p: 1, '&:last-child': { pb: 1 } },
  none: { p: 0, '&:last-child': { pb: 0 } },
};

/**
 * Renders the state of a TopLogic card (module name `TLCard`) with the MUI `Card`, `CardHeader`
 * and `CardContent`.
 *
 * <p>Mapping from the control state:</p>
 * <ul>
 * <li>variant `outlined` (default) → `variant="outlined"`; `elevated` → the elevation of an MUI
 *     card;</li>
 * <li>title → the title of the `CardHeader`; headerActions → its `action`, each rendered through
 *     {@link TLChild}; the header is shown while there is a title or an action;</li>
 * <li>padding → the padding of the `CardContent`: default → 2, compact → 1, none → 0 units of the
 *     MUI spacing;</li>
 * <li>child → the content, rendered through {@link TLChild} behind a {@link FillBarrier}, as in
 *     TLCard: the card grows with its content and does not take part in the fill contract;</li>
 * <li>hidden → nothing is rendered; the configured CSS class → className of the `Card`.</li>
 * </ul>
 */
const MuiCardAdapter: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<CardStateJson>>();

  if (state.hidden === true) {
    return null;
  }

  const title = state.title || undefined;
  const headerActions: ChildControlJson[] = state.headerActions ?? [];
  const hasHeader = title !== undefined || headerActions.length > 0;
  const elevated = state.variant === 'elevated';

  return (
    <Card id={controlId} className={rootClassName(state)} sx={CARD_SX}
      variant={elevated ? 'elevation' : 'outlined'} elevation={elevated ? ELEVATION : undefined}>
      {hasHeader && (
        <CardHeader
          title={title}
          slotProps={{ title: { variant: TITLE_VARIANT } }}
          action={headerActions.length > 0
            ? (
              <Stack direction="row" spacing={0.5} sx={{ alignItems: 'center' }}>
                {headerActions.map(action => <TLChild key={action.controlId} control={action} />)}
              </Stack>
            )
            : undefined}
        />
      )}
      <CardContent sx={PADDING_SX[state.padding ?? 'default']}>
        <FillBarrier>
          <TLChild control={state.child} />
        </FillBarrier>
      </CardContent>
    </Card>
  );
};

export default MuiCardAdapter;
