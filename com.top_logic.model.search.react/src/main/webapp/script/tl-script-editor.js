import { React as R, useTLState as b, useTLCommand as y, register as C } from "tl-react-bridge";
import { styleTags as Y, tags as O, LRParser as Z, LRLanguage as E, LanguageSupport as D, CodeEditor as x } from "tl-code-editor";
const f = Y({
  Number: O.number,
  String: O.string,
  Boolean: O.bool,
  Null: O.null,
  Variable: O.variableName,
  ModelConstant: O.typeName,
  ResourceKey: O.special(O.string),
  LanguageTag: O.annotation,
  LineComment: O.lineComment,
  BlockComment: O.blockComment,
  identifier: O.variableName,
  "FunctionCall/identifier": O.function(O.variableName),
  "MethodAccess/identifier": O.function(O.propertyName),
  Arrow: O.punctuation,
  '"(" ")" "[" "]" "{" "}"': O.paren,
  '"." ".."': O.derefOperator,
  "orOp andOp": O.logicOperator,
  compareOp: O.compareOperator,
  "Plus Minus multiplicativeOp": O.arithmeticOperator,
  NotOp: O.logicOperator,
  '"switch" "default" "tuple"': O.keyword,
  '"?" ":"': O.punctuation
}), W = { __proto__: null, true: 40, false: 42, null: 46, tuple: 62, switch: 68, default: 74 }, $ = Z.deserialize({
  version: 14,
  states: "3OO]QPOOO!aQQO'#DWO!iQPO'#CwO!nQPO'#DSO]QPO'#DVOOQO'#Cl'#ClO$yQSO'#CkO%QQPO'#ChOOQO'#Ch'#ChO&|QSO'#CgO'xQSO'#CfO(VQQO'#CeO(zQPO'#CdO)lQPO'#CcO*ZQPO'#CbOOQO'#Dk'#DkQOQPOOOOQO'#Co'#CoOOQO'#Cr'#CrO*uQPO'#CzO*zQPO'#C}O]QPO,58zO+SQPO'#DXOOQO,59r,59rO+ZQPO'#CxO+`QPO'#DnO+hQPO,59cO+mQWO'#DWOOQO'#DU'#DUO+xQPO'#DTO,QQPO,59nO,VQPO,59qO,[QPO'#DYO,aQPO'#DZO]QPO'#D[OOQO'#Dc'#DcO-mQSO,59VO!dQPO'#DWOOQO,59S,59SO%QQPO'#DdO.lQSO,59RO%QQPO'#DeO/hQSO,59QO/uQPO,59PO%QQPO'#DfO0yQPO,59OO%QQPO'#DgO1kQPO,58}O]QPO,58|O2YQPO,59fO2_QPO,59iO]QPO,59iOOQO1G.f1G.fO2iQPO'#DxOOQO,59s,59sO2qQPO,59sOOQO,59d,59dO!iQPO'#D^O2vQPO,5:YOOQO1G.}1G.}O]QPO,59pO3OQPO,59oO3VQPO,59oOOQO1G/Y1G/YOOQO1G/]1G/]OOQO,59t,59tOOQO,59u,59uO3_QPO,59vOOQO-E7a-E7aOOQO,5:O,5:OOOQO-E7b-E7bOOQO,5:P,5:POOQO-E7c-E7cO#rQPO'#CkO3dQPO'#CgO3kQPO'#CfOOQO1G.k1G.kOOQO,5:Q,5:QOOQO-E7d-E7dOOQO,5:R,5:ROOQO-E7e-E7eO3uQPO1G.hO3zQQO'#C|O4PQPO'#DrO4XQPO1G/QO4^QPO'#DPOOQO'#D`'#D`O4cQPO1G/TO4mQPO'#DQOOQO1G/T1G/TO4rQPO1G/TO4wQPO1G/TO]QPO'#DbO4|QPO,5:dOOQO1G/_1G/_OOQO,59x,59xOOQO-E7[-E7[OOQO1G/[1G/[OOQO,59{,59{O5UQPO1G/ZOOQO-E7_-E7_OOQO1G/b1G/bO,fQPO,59VO5]QPO,59RO5dQPO,59QO]QPO7+$SO]QPO,59hO2YQPO'#D_O5nQPO,5:^OOQO7+$l7+$lO]QPO,59kOOQO-E7^-E7^OOQO7+$o7+$oO5vQPO7+$oO]QPO,59lO5{QPO7+$oOOQO,59|,59|OOQO-E7`-E7`P!nQPO'#DaOOQO<<Gn<<GnOOQO1G/S1G/SOOQO,59y,59yOOQO-E7]-E7]O6QQPO1G/VOOQO<<HZ<<HZO6VQPO1G/WO6_QPO<<HZOOQO7+$q7+$qOOQO7+$r7+$rO6iQPOAN=uOOQOAN=uAN=uO6sQPOAN=uOOQOG23aG23aO6xQPOG23aOOQOLD({LD({O/uQPO'#ChO/uQPO'#DdO/uQPO'#De",
  stateData: "7X~O!^OSPOSQOS~O]VO^VOaTObTOdaOeaOgbOhTOiTOjTOocOrdO!`PO!aQO!eSO!gRO~OTeO!efO~ObhO~O]VO^VOaTObTOdaOeaOgbOhTOiTOjTOocOrdO!`kO!aQO!eSO!gRO~O!efO!mpO!nqO!orO^_X!P_X![_X!q_X!s_X!t_X!u_X!i_X!j_X!d_X!c_X!p_X!h_X~O!r_X~P#rO]VO^VOaTObTOdaOeaOgbOhTOiTOjTOocOrdO!`uO!aQO!eSO!gRO~O^ZX!PZX![ZX!sZX!tZX!uZX!iZX!jZX!dZX!cZX!pZX!hZX~O!qwO!rZX~P&UO![YX!sYX!tYX!uYX!iYX!jYX!dYX!cYX!pYX!hYX~O^yO!PyO!rYX~P'WO!r{O![XX!sXX!tXX!uXX!iXX!jXX!dXX!cXX!pXX!hXX~O!s|O![WX!tWX!uWX!iWX!jWX!dWX!cWX!pWX!hWX~O!t!OO![VX!uVX!iVX!jVX!dVX!cVX!pVX!hVX~O!u!QO![UX!iUX!jUX!dUX!cUX!pUX!hUX~O!e!RO~O!e!TO!g!SO~O!d!WO~P]Om!YO~O!c!ZO!d!bX~O!d!]O~OTeO!efO!k!^O~O!i!_O!jwX~O!j!aO~O!d!bO~O!`!cO~O!`!dO~O!efO!mpO!nqO!orO^_a!P_a![_a!q_a!s_a!t_a!u_a!i_a!j_a!d_a!c_a!p_a!h_a~O!r_a~P,fO^Za!PZa![Za!sZa!tZa!uZa!iZa!jZa!dZa!cZa!pZa!hZa~O!qwO!rZa~P-tO![Ya!sYa!tYa!uYa!iYa!jYa!dYa!cYa!pYa!hYa~O^yO!PyO!rYa~P.vO]#{O^#{OaTObTOdaOeaOgbOhTOiTOjTOocOrdO!`uO!aQO!eSO!gRO~O!s|O![Wa!tWa!uWa!iWa!jWa!dWa!cWa!pWa!hWa~O!t!OO![Va!uVa!iVa!jVa!dVa!cVa!pVa!hVa~O!`!tO~Ou!zO!j!{O~P]O!c#OO!d!lX~O!d#QO~O!c!ZO!d!ba~O!jwa~P!nO!i#VO!jwa~O!p#XO~O!q#|O~P&UO^#}O!P#}O~P'WO!h#]O~OT#^O~O!c#_O!d!fX~O!d#aO~O!h#bO~Ou!zO!j#dO~P]O!h#fO~O!j#dO~O!d#gO~O!c#OO!d!la~O!jwi~P!nO!q#|O~P-tO^#}O!P#}O~P.vO!c#_O!d!fa~O!j#pO~O!g#rO~O!i#sO~O!i#tO!jti~Ou!zO!j#vO~P]Ou!zO!j#xO~P]O!j#xO~O!j#zO~OQP!qaT!`!t!sT~",
  goto: "-V!mPPPP!nP!n#V#n$Y$u%c&TPP&{'mPP(aPP(aPPPP(a)RP(aP)X(aP)_)eP(a)q)t(a(a)|*R*R*RP*X*_*e*o*u*{+V+a+k+qPPP+wPP,|PPP-PPPPPP-S{_ORSefr!Q!S!T!^!_!y#O#V#]#^#b#f#j#r#u{^ORSefr!Q!S!T!^!_!y#O#V#]#^#b#f#j#r#uz]ORSefr!Q!S!T!^!_!y#O#V#]#^#b#f#j#r#uR!q!O|[ORSefr!O!Q!S!T!^!_!y#O#V#]#^#b#f#j#r#uR!o|!OZORSefr|!O!Q!S!T!^!_!y#O#V#]#^#b#f#j#r#uR!n{!OYORSefr|!O!Q!S!T!^!_!y#O#V#]#^#b#f#j#r#uS!iy#}R!m{!QXORSefry|!O!Q!S!T!^!_!y#O#V#]#^#b#f#j#r#uSvV#{S!gw#|T!l{#}!_WORSVefrwy{|!O!Q!S!T!^!_!y#O#V#]#^#b#f#j#r#u#{#|#}!UUORSVefrwy|!O!Q!S!T!^!_!y#O#V#]#^#b#f#j#r#uX!k{#{#|#}!_TORSVefrwy{|!O!Q!S!T!^!_!y#O#V#]#^#b#f#j#r#u#{#|#}QiQR#R!ZQ!u!RR#m#_X!x!S!y#r#uQ!|!SQ#e!yQ#w#rR#y#uRnRQmRV#U!_#V#jUgPkuXsUt!k#YQ![iR#S![Q#`!uR#n#`Q!y!SS#c!y#uR#u#rQ!`mR#W!`Q#P!VR#i#PQtUS!ft#YR#Y!kQxXS!hx#ZR#Z!lQzYS!jz#[R#[!mQ}[R!p}Q!P]R!r!PQ`OWlR!_#V#jQoSQ!UeQ!VfQ!erQ!s!QW!w!S!y#r#uQ!}!TQ#T!^Q#h#OQ#k#]Q#l#^Q#o#bR#q#fRjQR!v!RR!Xf",
  nodeNames: "⚠ LineComment BlockComment Script Lambda Arrow TernaryExpr OrExpr AndExpr CompareExpr AdditiveExpr MultiplicativeExpr UnaryExpr NotOp Minus AccessExpr AtomicExpr Number String Boolean true false Null null Variable ModelConstant ResourceKey I18NLiteral I18NEntry LanguageTag TupleExpr tuple TupleEntry SwitchExpr switch SwitchCase SwitchDefault default Block BlockContent Statement ParenExpr FunctionCall CallArgs MethodAccess DescendAccess ArrayAccess Plus",
  maxTerm: 83,
  propSources: [f],
  skippedNodes: [0, 1, 2],
  repeatNodeCount: 10,
  tokenData: ":S~RxXY#oYZ#o]^#opq#oqr$Qrs$_st(ptu,Wuv,uvw,zwx-Vxy.nyz.sz{,u{|.x|}.}}!O/S!O!P/a!P!Q/n!Q!R1c!R![2q![!]3S!]!^3X!^!_3^!_!`3f!`!a3^!a!b3n!b!c3s!c!}4[!}#O4m#P#Q4r#R#S4[#S#T4w#T#U7^#U#c4[#c#d8q#d#o4[#o#p9m#p#q9r#q#r9}~#tS!^~XY#oYZ#o]^#opq#oV$VP]P!_!`$YU$_O!rU~$bVOr$wrs&es#O$w#O#P%f#P;'S$w;'S;=`&_<%lO$w~$zVOr$wrs%as#O$w#O#P%f#P;'S$w;'S;=`&_<%lO$w~%fOb~~%iRO;'S$w;'S;=`%r;=`O$w~%uWOr$wrs%as#O$w#O#P%f#P;'S$w;'S;=`&_;=`<%l$w<%lO$w~&bP;=`<%l$w~&jPb~rs&m~&pTOr&mrs'Ps;'S&m;'S;=`(Z;=`O&m~'STOr&mrs'cs;'S&m;'S;=`(Z;=`O&m~'fTOr&mrs'us;'S&m;'S;=`(Z;=`O&m~'zTb~Or&mrs'us;'S&m;'S;=`(Z;=`O&m~(^UOr&mrs'Ps;'S&m;'S;=`(Z;=`<%l&m<%lO&m~(sRrs(|wx*jxy,R~)PVOr(|rs)fs#O(|#O#P)k#P;'S(|;'S;=`*d<%lO(|~)kOj~~)nRO;'S(|;'S;=`)w;=`O(|~)zWOr(|rs)fs#O(|#O#P)k#P;'S(|;'S;=`*d;=`<%l(|<%lO(|~*gP;=`<%l(|~*mVOw*jwx)fx#O*j#O#P+S#P;'S*j;'S;=`+{<%lO*j~+VRO;'S*j;'S;=`+`;=`O*j~+cWOw*jwx)fx#O*j#O#P+S#P;'S*j;'S;=`+{;=`<%l*j<%lO*j~,OP;=`<%l*j~,WO!a~~,ZR!c!},d#R#S,d#T#o,d~,iSh~!Q![,d!c!},d#R#S,d#T#o,d~,zO!q~~,}Pvw-Q~-VO!s~~-YVOw-Vwx%ax#O-V#O#P-o#P;'S-V;'S;=`.h<%lO-V~-rRO;'S-V;'S;=`-{;=`O-V~.OWOw-Vwx%ax#O-V#O#P-o#P;'S-V;'S;=`.h;=`<%l-V<%lO-V~.kP;=`<%l-V~.sO!e~~.xO!d~~.}O!P~~/SO!c~_/XP^T!`!a/[Y/aOTY~/fP!m~!O!P/i~/nO!n~~/sQ!q~z{/y!P!Q0z~/|TOz/yz{0]{;'S/y;'S;=`0t<%lO/y~0`TO!P/y!P!Q0o!Q;'S/y;'S;=`0t<%lO/y~0tOQ~~0wP;=`<%l/y~1PSP~OY0zZ;'S0z;'S;=`1]<%lO0z~1`P;=`<%l0z~1hRa~!O!P1q!g!h2V#X#Y2V~1tP!Q![1w~1|Ra~!Q![1w!g!h2V#X#Y2V~2YR{|2c}!O2c!Q![2i~2fP!Q![2i~2nPa~!Q![2i~2vSa~!O!P1q!Q![2q!g!h2V#X#Y2V~3XO!h~~3^O!i~U3cP!rU!_!`$Y^3kP!kW!_!`$Y~3sO!u~~3vQ!c!}3|#T#o3|~4RRm~}!O3|!c!}3|#T#o3|~4aS!`~!Q![4[!c!}4[#R#S4[#T#o4[~4rO!o~~4wO!p~~4zR!c!}5T#R#S5T#T#o5T~5WWst5p!O!P5T!Q![5T![!]6h!c!}5T#R#S5T#S#T6c#T#o5T~5sR!c!}5|#R#S5|#T#o5|~6PU!O!P5|!Q![5|!c!}5|#R#S5|#S#T6c#T#o5|~6hOi~~6kR!c!}6t#R#S6t#T#o6t~6wVst5p!O!P6t!Q![6t!c!}6t#R#S6t#S#T6c#T#o6t~7cU!`~!Q![4[!c!}4[#R#S4[#T#b4[#b#c7u#c#o4[~7zU!`~!Q![4[!c!}4[#R#S4[#T#W4[#W#X8^#X#o4[~8eS!`~!s~!Q![4[!c!}4[#R#S4[#T#o4[~8vU!`~!Q![4[!c!}4[#R#S4[#T#f4[#f#g9Y#g#o4[~9aS!`~!t~!Q![4[!c!}4[#R#S4[#T#o4[~9rO!g~~9uP#p#q9x~9}O!t~~:SO!j~",
  tokenizers: [0, 1, 2, 3],
  topRules: { Script: [0, 3] },
  specialized: [{ term: 62, get: (i) => W[i] || -1 }],
  tokenPrec: 1011
}), k = E.define({
  parser: $.configure({
    props: [f]
  }),
  languageData: {
    commentTokens: { line: "//", block: { open: "/*", close: "*/" } },
    closeBrackets: { brackets: ["(", "[", "{", "'", '"', "`"] }
  }
});
function q() {
  return new D(k);
}
const { useRef: v, useEffect: z, useCallback: T, useMemo: G } = R, U = 3e3, N = ({ controlId: i, state: S }) => {
  const p = b(), o = y(), P = v(null), V = S.value ?? "", _ = S.readOnly === !0, g = p.diagnostics ?? [], h = G(() => q(), []), w = T(
    (e) => {
      const a = e.pos, r = e.state.doc.lineAt(a);
      if (!e.matchBefore(/[\w$`.:]+/) && !e.explicit) return Promise.resolve(null);
      const t = e.matchBefore(/\$?[\w]*/), u = (t == null ? void 0 : t.text) ?? "", Q = (t == null ? void 0 : t.from) ?? a, d = r.text.substring(0, a - r.from), c = e.state.sliceDoc(0, a), n = String(Date.now()) + Math.random();
      return o("complete", { line: d, prefix: u, textToCursor: c, requestId: n }), new Promise((X) => {
        P.current = { requestId: n, resolve: X, from: Q }, setTimeout(() => {
          var m;
          ((m = P.current) == null ? void 0 : m.requestId) === n && (P.current = null, X(null));
        }, U);
      });
    },
    [o]
  ), s = p.completionResponse;
  z(() => {
    const e = P.current;
    if (!e || !s || s.requestId !== e.requestId)
      return;
    const a = s.completions ?? [];
    if (P.current = null, a.length === 0) {
      e.resolve(null);
      return;
    }
    e.resolve({
      from: e.from,
      options: a.map((r) => {
        const l = r.replacement ?? r.name, t = r.cursorOffset, u = t == null ? l : (Q, d, c, n) => {
          Q.dispatch({
            changes: { from: c, to: n, insert: l },
            selection: { anchor: c + t }
          });
        };
        return {
          label: r.name,
          apply: u,
          detail: r.value !== r.name ? r.value : void 0,
          info: r.docHTML ? () => {
            const Q = document.createElement("div");
            return Q.innerHTML = r.docHTML, Q;
          } : void 0,
          boost: r.score ?? 0
        };
      })
    });
  }, [s]);
  const j = T(
    (e) => {
      o("valueChanged", { value: e }), o("validate", { text: e });
    },
    [o]
  );
  return /* @__PURE__ */ R.createElement(
    x,
    {
      controlId: i,
      value: V,
      readOnly: _,
      languageSupport: h,
      completionSource: w,
      diagnostics: g,
      onChange: j,
      className: "tlScriptEditor"
    }
  );
};
C("TLScriptEditor", N);
