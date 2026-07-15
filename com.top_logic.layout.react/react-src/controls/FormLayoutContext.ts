import { React } from 'tl-react-bridge';

export interface FormLayoutContextValue {
  readOnly: boolean;
  resolvedLabelPosition: 'side' | 'top';
}

const defaultContext: FormLayoutContextValue = {
  readOnly: false,
  resolvedLabelPosition: 'side',
};

export const FormLayoutContext = React.createContext<FormLayoutContextValue>(defaultContext);
