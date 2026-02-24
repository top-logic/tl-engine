import { register } from 'tl-react-bridge';

import TLTextInput from './controls/TLTextInput';
import TLNumberInput from './controls/TLNumberInput';
import TLDatePicker from './controls/TLDatePicker';
import TLSelect from './controls/TLSelect';
import TLCheckbox from './controls/TLCheckbox';
import TLTable from './controls/TLTable';
import TLButton from './controls/TLButton';
import TLCounter from './controls/TLCounter';

register('TLButton', TLButton);
register('TLTextInput', TLTextInput);
register('TLNumberInput', TLNumberInput);
register('TLDatePicker', TLDatePicker);
register('TLSelect', TLSelect);
register('TLCheckbox', TLCheckbox);
register('TLTable', TLTable);
register('TLCounter', TLCounter);
