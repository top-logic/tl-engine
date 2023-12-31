/* Generated By:JavaCC: Do not edit this line. TemplateParserTokenManager.java */
package com.top_logic.template.parser;

/** Token Manager. */
public class TemplateParserTokenManager implements TemplateParserConstants
{

  /** Debug output. */
  public  java.io.PrintStream debugStream = System.out;
  /** Set debug output. */
  public  void setDebugStream(java.io.PrintStream ds) { debugStream = ds; }
private final int jjStopStringLiteralDfa_0(int pos, long active0)
{
   switch (pos)
   {
      case 0:
         if ((active0 & 0x2L) != 0L)
            return 17;
         return -1;
      case 1:
         if ((active0 & 0x2L) != 0L)
         {
            jjmatchedKind = 30;
            jjmatchedPos = 1;
            return 13;
         }
         return -1;
      case 2:
         if ((active0 & 0x2L) != 0L)
         {
            if (jjmatchedPos < 1)
            {
               jjmatchedKind = 30;
               jjmatchedPos = 1;
            }
            return -1;
         }
         return -1;
      case 3:
         if ((active0 & 0x2L) != 0L)
         {
            if (jjmatchedPos < 1)
            {
               jjmatchedKind = 30;
               jjmatchedPos = 1;
            }
            return -1;
         }
         return -1;
      default :
         return -1;
   }
}
private final int jjStartNfa_0(int pos, long active0)
{
   return jjMoveNfa_0(jjStopStringLiteralDfa_0(pos, active0), pos + 1);
}
private int jjStopAtPos(int pos, int kind)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   return pos + 1;
}
private int jjMoveStringLiteralDfa0_0()
{
   switch(curChar)
   {
      case 60:
         return jjMoveStringLiteralDfa1_0(0x2L);
      default :
         return jjMoveNfa_0(4, 0);
   }
}
private int jjMoveStringLiteralDfa1_0(long active0)
{
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(0, active0);
      return 1;
   }
   switch(curChar)
   {
      case 37:
         return jjMoveStringLiteralDfa2_0(active0, 0x2L);
      default :
         break;
   }
   return jjStartNfa_0(0, active0);
}
private int jjMoveStringLiteralDfa2_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(0, old0);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(1, active0);
      return 2;
   }
   switch(curChar)
   {
      case 33:
         return jjMoveStringLiteralDfa3_0(active0, 0x2L);
      default :
         break;
   }
   return jjStartNfa_0(1, active0);
}
private int jjMoveStringLiteralDfa3_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(1, old0);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(2, active0);
      return 3;
   }
   switch(curChar)
   {
      case 45:
         return jjMoveStringLiteralDfa4_0(active0, 0x2L);
      default :
         break;
   }
   return jjStartNfa_0(2, active0);
}
private int jjMoveStringLiteralDfa4_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(2, old0);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(3, active0);
      return 4;
   }
   switch(curChar)
   {
      case 45:
         if ((active0 & 0x2L) != 0L)
            return jjStopAtPos(4, 1);
         break;
      default :
         break;
   }
   return jjStartNfa_0(3, active0);
}
static final long[] jjbitVec0 = {
   0xfffffffffffffffeL, 0xffffffffffffffffL, 0xffffffffffffffffL, 0xffffffffffffffffL
};
static final long[] jjbitVec2 = {
   0x0L, 0x0L, 0xffffffffffffffffL, 0xffffffffffffffffL
};
private int jjMoveNfa_0(int startState, int curPos)
{
   int startsAt = 0;
   jjnewStateCnt = 17;
   int i = 1;
   jjstateSet[0] = startState;
   int kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         long l = 1L << curChar;
         do
         {
            switch(jjstateSet[--i])
            {
               case 4:
                  if ((0xefffffdfffffffffL & l) != 0L)
                  {
                     if (kind > 46)
                        kind = 46;
                     jjCheckNAddStates(0, 2);
                  }
                  else if (curChar == 60)
                     jjCheckNAddStates(3, 8);
                  else if (curChar == 37)
                     jjCheckNAddTwoStates(2, 3);
                  if ((0x3fe000000000000L & l) != 0L)
                  {
                     if (kind > 47)
                        kind = 47;
                     jjCheckNAddTwoStates(5, 6);
                  }
                  else if (curChar == 48)
                  {
                     if (kind > 47)
                        kind = 47;
                     jjCheckNAddStates(9, 11);
                  }
                  break;
               case 17:
                  if ((0xefffffdfffffffffL & l) != 0L)
                  {
                     if (kind > 46)
                        kind = 46;
                     jjCheckNAddStates(0, 2);
                  }
                  else if (curChar == 60)
                     jjCheckNAddTwoStates(1, 0);
                  else if (curChar == 37)
                  {
                     if (kind > 30)
                        kind = 30;
                  }
                  if (curChar == 60)
                     jjCheckNAddTwoStates(15, 16);
                  else if (curChar == 37)
                     jjstateSet[jjnewStateCnt++] = 13;
                  if (curChar == 60)
                     jjCheckNAddTwoStates(12, 14);
                  break;
               case 0:
                  if ((0xefffffdfffffffffL & l) == 0L)
                     break;
                  if (kind > 46)
                     kind = 46;
                  jjCheckNAddStates(0, 2);
                  break;
               case 1:
                  if (curChar == 60)
                     jjCheckNAddTwoStates(1, 0);
                  break;
               case 2:
                  if (curChar == 37)
                     jjCheckNAddTwoStates(2, 3);
                  break;
               case 3:
                  if ((0xbfffffdfffffffffL & l) == 0L)
                     break;
                  if (kind > 46)
                     kind = 46;
                  jjCheckNAddStates(0, 2);
                  break;
               case 5:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 47)
                     kind = 47;
                  jjCheckNAddTwoStates(5, 6);
                  break;
               case 7:
                  if (curChar != 48)
                     break;
                  if (kind > 47)
                     kind = 47;
                  jjCheckNAddStates(9, 11);
                  break;
               case 9:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 47)
                     kind = 47;
                  jjCheckNAddTwoStates(9, 6);
                  break;
               case 10:
                  if ((0xff000000000000L & l) == 0L)
                     break;
                  if (kind > 47)
                     kind = 47;
                  jjCheckNAddTwoStates(10, 6);
                  break;
               case 11:
                  if (curChar == 60)
                     jjCheckNAddStates(3, 8);
                  break;
               case 12:
                  if (curChar == 60)
                     jjCheckNAddTwoStates(12, 14);
                  break;
               case 13:
                  if (curChar == 47 && kind > 29)
                     kind = 29;
                  break;
               case 14:
                  if (curChar == 37)
                     jjstateSet[jjnewStateCnt++] = 13;
                  break;
               case 15:
                  if (curChar == 60)
                     jjCheckNAddTwoStates(15, 16);
                  break;
               case 16:
                  if (curChar == 37 && kind > 30)
                     kind = 30;
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         do
         {
            switch(jjstateSet[--i])
            {
               case 4:
               case 0:
               case 3:
                  if (kind > 46)
                     kind = 46;
                  jjCheckNAddStates(0, 2);
                  break;
               case 17:
                  if (kind > 46)
                     kind = 46;
                  jjCheckNAddStates(0, 2);
                  break;
               case 6:
                  if ((0x100000001000L & l) != 0L && kind > 47)
                     kind = 47;
                  break;
               case 8:
                  if ((0x100000001000000L & l) != 0L)
                     jjCheckNAdd(9);
                  break;
               case 9:
                  if ((0x7e0000007eL & l) == 0L)
                     break;
                  if (kind > 47)
                     kind = 47;
                  jjCheckNAddTwoStates(9, 6);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int hiByte = (int)(curChar >> 8);
         int i1 = hiByte >> 6;
         long l1 = 1L << (hiByte & 077);
         int i2 = (curChar & 0xff) >> 6;
         long l2 = 1L << (curChar & 077);
         do
         {
            switch(jjstateSet[--i])
            {
               case 4:
               case 0:
               case 3:
                  if (!jjCanMove_0(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 46)
                     kind = 46;
                  jjCheckNAddStates(0, 2);
                  break;
               case 17:
                  if (!jjCanMove_0(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 46)
                     kind = 46;
                  jjCheckNAddStates(0, 2);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 17 - (jjnewStateCnt = startsAt)))
         return curPos;
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
private final int jjStopStringLiteralDfa_2(int pos, long active0)
{
   switch (pos)
   {
      case 0:
         if ((active0 & 0x31de00000000L) != 0L)
         {
            jjmatchedKind = 51;
            return 0;
         }
         return -1;
      case 1:
         if ((active0 & 0x18200000000L) != 0L)
            return 0;
         if ((active0 & 0x305c00000000L) != 0L)
         {
            jjmatchedKind = 51;
            jjmatchedPos = 1;
            return 0;
         }
         return -1;
      case 2:
         if ((active0 & 0x4000000000L) != 0L)
            return 0;
         if ((active0 & 0x301c00000000L) != 0L)
         {
            jjmatchedKind = 51;
            jjmatchedPos = 2;
            return 0;
         }
         return -1;
      case 3:
         if ((active0 & 0x100c00000000L) != 0L)
            return 0;
         if ((active0 & 0x201000000000L) != 0L)
         {
            if (jjmatchedPos != 3)
            {
               jjmatchedKind = 51;
               jjmatchedPos = 3;
            }
            return 0;
         }
         return -1;
      case 4:
         if ((active0 & 0x200000000000L) != 0L)
            return 0;
         if ((active0 & 0x1400000000L) != 0L)
         {
            jjmatchedKind = 51;
            jjmatchedPos = 4;
            return 0;
         }
         return -1;
      case 5:
         if ((active0 & 0x400000000L) != 0L)
            return 0;
         if ((active0 & 0x1000000000L) != 0L)
         {
            jjmatchedKind = 51;
            jjmatchedPos = 5;
            return 0;
         }
         return -1;
      default :
         return -1;
   }
}
private final int jjStartNfa_2(int pos, long active0)
{
   return jjMoveNfa_2(jjStopStringLiteralDfa_2(pos, active0), pos + 1);
}
private int jjMoveStringLiteralDfa0_2()
{
   switch(curChar)
   {
      case 33:
         jjmatchedKind = 16;
         return jjMoveStringLiteralDfa1_2(0x200L);
      case 34:
         return jjStopAtPos(0, 41);
      case 35:
         return jjStopAtPos(0, 28);
      case 36:
         return jjStopAtPos(0, 27);
      case 37:
         return jjMoveStringLiteralDfa1_2(0x100000000L);
      case 38:
         return jjMoveStringLiteralDfa1_2(0x4000L);
      case 40:
         return jjStopAtPos(0, 17);
      case 41:
         return jjStopAtPos(0, 18);
      case 42:
         return jjStopAtPos(0, 54);
      case 44:
         return jjStopAtPos(0, 24);
      case 46:
         return jjStopAtPos(0, 25);
      case 47:
         return jjMoveStringLiteralDfa1_2(0x80000000L);
      case 58:
         return jjStopAtPos(0, 26);
      case 59:
         return jjStopAtPos(0, 23);
      case 60:
         jjmatchedKind = 13;
         return jjMoveStringLiteralDfa1_2(0x800L);
      case 61:
         jjmatchedKind = 37;
         return jjMoveStringLiteralDfa1_2(0x100L);
      case 62:
         jjmatchedKind = 12;
         return jjMoveStringLiteralDfa1_2(0x400L);
      case 91:
         return jjStopAtPos(0, 21);
      case 93:
         return jjStopAtPos(0, 22);
      case 97:
         return jjMoveStringLiteralDfa1_2(0x10000000000L);
      case 100:
         return jjMoveStringLiteralDfa1_2(0x4000000000L);
      case 101:
         return jjMoveStringLiteralDfa1_2(0xc00000000L);
      case 102:
         return jjMoveStringLiteralDfa1_2(0x201000000000L);
      case 105:
         return jjMoveStringLiteralDfa1_2(0x8200000000L);
      case 116:
         return jjMoveStringLiteralDfa1_2(0x100000000000L);
      case 123:
         return jjStopAtPos(0, 19);
      case 124:
         return jjMoveStringLiteralDfa1_2(0x8000L);
      case 125:
         return jjStopAtPos(0, 20);
      default :
         return jjMoveNfa_2(0, 0);
   }
}
private int jjMoveStringLiteralDfa1_2(long active0)
{
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_2(0, active0);
      return 1;
   }
   switch(curChar)
   {
      case 37:
         return jjMoveStringLiteralDfa2_2(active0, 0x80000000L);
      case 38:
         if ((active0 & 0x4000L) != 0L)
            return jjStopAtPos(1, 14);
         break;
      case 61:
         if ((active0 & 0x100L) != 0L)
            return jjStopAtPos(1, 8);
         else if ((active0 & 0x200L) != 0L)
            return jjStopAtPos(1, 9);
         else if ((active0 & 0x400L) != 0L)
            return jjStopAtPos(1, 10);
         else if ((active0 & 0x800L) != 0L)
            return jjStopAtPos(1, 11);
         break;
      case 62:
         if ((active0 & 0x100000000L) != 0L)
            return jjStopAtPos(1, 32);
         break;
      case 97:
         return jjMoveStringLiteralDfa2_2(active0, 0x200000000000L);
      case 101:
         return jjMoveStringLiteralDfa2_2(active0, 0x4000000000L);
      case 102:
         if ((active0 & 0x200000000L) != 0L)
            return jjStartNfaWithStates_2(1, 33, 0);
         break;
      case 108:
         return jjMoveStringLiteralDfa2_2(active0, 0xc00000000L);
      case 110:
         if ((active0 & 0x8000000000L) != 0L)
            return jjStartNfaWithStates_2(1, 39, 0);
         break;
      case 111:
         return jjMoveStringLiteralDfa2_2(active0, 0x1000000000L);
      case 114:
         return jjMoveStringLiteralDfa2_2(active0, 0x100000000000L);
      case 115:
         if ((active0 & 0x10000000000L) != 0L)
            return jjStartNfaWithStates_2(1, 40, 0);
         break;
      case 124:
         if ((active0 & 0x8000L) != 0L)
            return jjStopAtPos(1, 15);
         break;
      default :
         break;
   }
   return jjStartNfa_2(0, active0);
}
private int jjMoveStringLiteralDfa2_2(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_2(0, old0);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_2(1, active0);
      return 2;
   }
   switch(curChar)
   {
      case 62:
         if ((active0 & 0x80000000L) != 0L)
            return jjStopAtPos(2, 31);
         break;
      case 102:
         if ((active0 & 0x4000000000L) != 0L)
            return jjStartNfaWithStates_2(2, 38, 0);
         break;
      case 108:
         return jjMoveStringLiteralDfa3_2(active0, 0x200000000000L);
      case 114:
         return jjMoveStringLiteralDfa3_2(active0, 0x1000000000L);
      case 115:
         return jjMoveStringLiteralDfa3_2(active0, 0xc00000000L);
      case 117:
         return jjMoveStringLiteralDfa3_2(active0, 0x100000000000L);
      default :
         break;
   }
   return jjStartNfa_2(1, active0);
}
private int jjMoveStringLiteralDfa3_2(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_2(1, old0);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_2(2, active0);
      return 3;
   }
   switch(curChar)
   {
      case 101:
         if ((active0 & 0x800000000L) != 0L)
         {
            jjmatchedKind = 35;
            jjmatchedPos = 3;
         }
         else if ((active0 & 0x100000000000L) != 0L)
            return jjStartNfaWithStates_2(3, 44, 0);
         return jjMoveStringLiteralDfa4_2(active0, 0x1400000000L);
      case 115:
         return jjMoveStringLiteralDfa4_2(active0, 0x200000000000L);
      default :
         break;
   }
   return jjStartNfa_2(2, active0);
}
private int jjMoveStringLiteralDfa4_2(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_2(2, old0);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_2(3, active0);
      return 4;
   }
   switch(curChar)
   {
      case 97:
         return jjMoveStringLiteralDfa5_2(active0, 0x1000000000L);
      case 101:
         if ((active0 & 0x200000000000L) != 0L)
            return jjStartNfaWithStates_2(4, 45, 0);
         break;
      case 105:
         return jjMoveStringLiteralDfa5_2(active0, 0x400000000L);
      default :
         break;
   }
   return jjStartNfa_2(3, active0);
}
private int jjMoveStringLiteralDfa5_2(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_2(3, old0);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_2(4, active0);
      return 5;
   }
   switch(curChar)
   {
      case 99:
         return jjMoveStringLiteralDfa6_2(active0, 0x1000000000L);
      case 102:
         if ((active0 & 0x400000000L) != 0L)
            return jjStartNfaWithStates_2(5, 34, 0);
         break;
      default :
         break;
   }
   return jjStartNfa_2(4, active0);
}
private int jjMoveStringLiteralDfa6_2(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_2(4, old0);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_2(5, active0);
      return 6;
   }
   switch(curChar)
   {
      case 104:
         if ((active0 & 0x1000000000L) != 0L)
            return jjStartNfaWithStates_2(6, 36, 0);
         break;
      default :
         break;
   }
   return jjStartNfa_2(5, active0);
}
private int jjStartNfaWithStates_2(int pos, int kind, int state)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) { return pos + 1; }
   return jjMoveNfa_2(state, pos + 1);
}
private int jjMoveNfa_2(int startState, int curPos)
{
   int startsAt = 0;
   jjnewStateCnt = 1;
   int i = 1;
   jjstateSet[0] = startState;
   int kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         long l = 1L << curChar;
         do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  kind = 51;
                  jjstateSet[jjnewStateCnt++] = 0;
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         do
         {
            switch(jjstateSet[--i])
            {
               case 0:
                  if ((0x7fffffe87fffffeL & l) == 0L)
                     break;
                  kind = 51;
                  jjstateSet[jjnewStateCnt++] = 0;
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int hiByte = (int)(curChar >> 8);
         int i1 = hiByte >> 6;
         long l1 = 1L << (hiByte & 077);
         int i2 = (curChar & 0xff) >> 6;
         long l2 = 1L << (curChar & 077);
         do
         {
            switch(jjstateSet[--i])
            {
               default : break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 1 - (jjnewStateCnt = startsAt)))
         return curPos;
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
private final int jjStopStringLiteralDfa_3(int pos, long active0)
{
   switch (pos)
   {
      default :
         return -1;
   }
}
private final int jjStartNfa_3(int pos, long active0)
{
   return jjMoveNfa_3(jjStopStringLiteralDfa_3(pos, active0), pos + 1);
}
private int jjMoveStringLiteralDfa0_3()
{
   switch(curChar)
   {
      case 34:
         return jjStopAtPos(0, 42);
      default :
         return jjMoveNfa_3(6, 0);
   }
}
private int jjMoveNfa_3(int startState, int curPos)
{
   int startsAt = 0;
   jjnewStateCnt = 6;
   int i = 1;
   jjstateSet[0] = startState;
   int kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         long l = 1L << curChar;
         do
         {
            switch(jjstateSet[--i])
            {
               case 6:
               case 0:
                  if ((0xfffffffbffffffffL & l) == 0L)
                     break;
                  if (kind > 43)
                     kind = 43;
                  jjCheckNAddTwoStates(0, 1);
                  break;
               case 2:
                  if ((0x8400000000L & l) == 0L)
                     break;
                  if (kind > 43)
                     kind = 43;
                  jjCheckNAddTwoStates(0, 1);
                  break;
               case 3:
                  if ((0xf000000000000L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 4;
                  break;
               case 4:
                  if ((0xff000000000000L & l) != 0L)
                     jjstateSet[jjnewStateCnt++] = 5;
                  break;
               case 5:
                  if ((0xff000000000000L & l) == 0L)
                     break;
                  if (kind > 43)
                     kind = 43;
                  jjCheckNAddTwoStates(0, 1);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         do
         {
            switch(jjstateSet[--i])
            {
               case 6:
                  if ((0xffffffffefffffffL & l) != 0L)
                  {
                     if (kind > 43)
                        kind = 43;
                     jjCheckNAddTwoStates(0, 1);
                  }
                  else if (curChar == 92)
                     jjAddStates(12, 13);
                  break;
               case 0:
                  if ((0xffffffffefffffffL & l) == 0L)
                     break;
                  if (kind > 43)
                     kind = 43;
                  jjCheckNAddTwoStates(0, 1);
                  break;
               case 1:
                  if (curChar == 92)
                     jjAddStates(12, 13);
                  break;
               case 2:
                  if ((0x14404410000000L & l) == 0L)
                     break;
                  if (kind > 43)
                     kind = 43;
                  jjCheckNAddTwoStates(0, 1);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int hiByte = (int)(curChar >> 8);
         int i1 = hiByte >> 6;
         long l1 = 1L << (hiByte & 077);
         int i2 = (curChar & 0xff) >> 6;
         long l2 = 1L << (curChar & 077);
         do
         {
            switch(jjstateSet[--i])
            {
               case 6:
               case 0:
                  if (!jjCanMove_0(hiByte, i1, i2, l1, l2))
                     break;
                  if (kind > 43)
                     kind = 43;
                  jjCheckNAddTwoStates(0, 1);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 6 - (jjnewStateCnt = startsAt)))
         return curPos;
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
private int jjMoveStringLiteralDfa0_1()
{
   switch(curChar)
   {
      case 45:
         return jjMoveStringLiteralDfa1_1(0x4L);
      default :
         return 1;
   }
}
private int jjMoveStringLiteralDfa1_1(long active0)
{
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      return 1;
   }
   switch(curChar)
   {
      case 45:
         return jjMoveStringLiteralDfa2_1(active0, 0x4L);
      default :
         return 2;
   }
}
private int jjMoveStringLiteralDfa2_1(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return 2;
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      return 2;
   }
   switch(curChar)
   {
      case 37:
         return jjMoveStringLiteralDfa3_1(active0, 0x4L);
      default :
         return 3;
   }
}
private int jjMoveStringLiteralDfa3_1(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return 3;
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      return 3;
   }
   switch(curChar)
   {
      case 62:
         if ((active0 & 0x4L) != 0L)
            return jjStopAtPos(3, 2);
         break;
      default :
         return 4;
   }
   return 4;
}
static final int[] jjnextStates = {
   0, 1, 2, 12, 14, 15, 16, 1, 0, 8, 10, 6, 2, 3, 
};
private static final boolean jjCanMove_0(int hiByte, int i1, int i2, long l1, long l2)
{
   switch(hiByte)
   {
      case 0:
         return ((jjbitVec2[i2] & l2) != 0L);
      default :
         if ((jjbitVec0[i1] & l1) != 0L)
            return true;
         return false;
   }
}

/** Token literal values. */
public static final String[] jjstrLiteralImages = {
"", null, null, null, null, null, null, null, "\75\75", "\41\75", "\76\75", 
"\74\75", "\76", "\74", "\46\46", "\174\174", "\41", "\50", "\51", "\173", "\175", 
"\133", "\135", "\73", "\54", "\56", "\72", "\44", "\43", null, null, "\57\45\76", 
"\45\76", "\151\146", "\145\154\163\145\151\146", "\145\154\163\145", 
"\146\157\162\145\141\143\150", "\75", "\144\145\146", "\151\156", "\141\163", "\42", "\42", null, 
"\164\162\165\145", "\146\141\154\163\145", null, null, null, null, null, null, null, null, "\52", };

/** Lexer state names. */
public static final String[] lexStateNames = {
   "DEFAULT",
   "WithinComment",
   "Syntax",
   "Stringmode",
};

/** Lex State array. */
public static final int[] jjnewLexState = {
   -1, 1, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 
   -1, -1, -1, -1, 2, 2, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, 3, 2, -1, -1, -1, -1, -1, -1, -1, 
   -1, -1, -1, -1, -1, 
};
static final long[] jjtoToken = {
   0x48ffffffffff01L, 
};
static final long[] jjtoSkip = {
   0xf6L, 
};
static final long[] jjtoMore = {
   0x8L, 
};
protected SimpleCharStream input_stream;
private final int[] jjrounds = new int[17];
private final int[] jjstateSet = new int[34];
protected char curChar;
/** Constructor. */
public TemplateParserTokenManager(SimpleCharStream stream){
   if (SimpleCharStream.staticFlag)
      throw new Error("ERROR: Cannot use a static CharStream class with a non-static lexical analyzer.");
   input_stream = stream;
}

/** Constructor. */
public TemplateParserTokenManager(SimpleCharStream stream, int lexState){
   this(stream);
   SwitchTo(lexState);
}

/** Reinitialise parser. */
public void ReInit(SimpleCharStream stream)
{
   jjmatchedPos = jjnewStateCnt = 0;
   curLexState = defaultLexState;
   input_stream = stream;
   ReInitRounds();
}
private void ReInitRounds()
{
   int i;
   jjround = 0x80000001;
   for (i = 17; i-- > 0;)
      jjrounds[i] = 0x80000000;
}

/** Reinitialise parser. */
public void ReInit(SimpleCharStream stream, int lexState)
{
   ReInit(stream);
   SwitchTo(lexState);
}

/** Switch to specified lex state. */
public void SwitchTo(int lexState)
{
   if (lexState >= 4 || lexState < 0)
      throw new TokenMgrError("Error: Ignoring invalid lexical state : " + lexState + ". State unchanged.", TokenMgrError.INVALID_LEXICAL_STATE);
   else
      curLexState = lexState;
}

protected Token jjFillToken()
{
   final Token t;
   final String curTokenImage;
   final int beginLine;
   final int endLine;
   final int beginColumn;
   final int endColumn;
   String im = jjstrLiteralImages[jjmatchedKind];
   curTokenImage = (im == null) ? input_stream.GetImage() : im;
   beginLine = input_stream.getBeginLine();
   beginColumn = input_stream.getBeginColumn();
   endLine = input_stream.getEndLine();
   endColumn = input_stream.getEndColumn();
   t = Token.newToken(jjmatchedKind, curTokenImage);

   t.beginLine = beginLine;
   t.endLine = endLine;
   t.beginColumn = beginColumn;
   t.endColumn = endColumn;

   return t;
}

int curLexState = 0;
int defaultLexState = 0;
int jjnewStateCnt;
int jjround;
int jjmatchedPos;
int jjmatchedKind;

/** Get the next Token. */
public Token getNextToken() 
{
  Token matchedToken;
  int curPos = 0;

  EOFLoop :
  for (;;)
  {
   try
   {
      curChar = input_stream.BeginToken();
   }
   catch(java.io.IOException e)
   {
      jjmatchedKind = 0;
      matchedToken = jjFillToken();
      return matchedToken;
   }

   for (;;)
   {
     switch(curLexState)
     {
       case 0:
         jjmatchedKind = 0x7fffffff;
         jjmatchedPos = 0;
         curPos = jjMoveStringLiteralDfa0_0();
         break;
       case 1:
         jjmatchedKind = 0x7fffffff;
         jjmatchedPos = 0;
         curPos = jjMoveStringLiteralDfa0_1();
         if (jjmatchedPos == 0 && jjmatchedKind > 3)
         {
            jjmatchedKind = 3;
         }
         break;
       case 2:
         try { input_stream.backup(0);
            while (curChar <= 32 && (0x100002600L & (1L << curChar)) != 0L)
               curChar = input_stream.BeginToken();
         }
         catch (java.io.IOException e1) { continue EOFLoop; }
         jjmatchedKind = 0x7fffffff;
         jjmatchedPos = 0;
         curPos = jjMoveStringLiteralDfa0_2();
         break;
       case 3:
         jjmatchedKind = 0x7fffffff;
         jjmatchedPos = 0;
         curPos = jjMoveStringLiteralDfa0_3();
         break;
     }
     if (jjmatchedKind != 0x7fffffff)
     {
        if (jjmatchedPos + 1 < curPos)
           input_stream.backup(curPos - jjmatchedPos - 1);
        if ((jjtoToken[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L)
        {
           matchedToken = jjFillToken();
       if (jjnewLexState[jjmatchedKind] != -1)
         curLexState = jjnewLexState[jjmatchedKind];
           return matchedToken;
        }
        else if ((jjtoSkip[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L)
        {
         if (jjnewLexState[jjmatchedKind] != -1)
           curLexState = jjnewLexState[jjmatchedKind];
           continue EOFLoop;
        }
      if (jjnewLexState[jjmatchedKind] != -1)
        curLexState = jjnewLexState[jjmatchedKind];
        curPos = 0;
        jjmatchedKind = 0x7fffffff;
        try {
           curChar = input_stream.readChar();
           continue;
        }
        catch (java.io.IOException e1) { }
     }
     int error_line = input_stream.getEndLine();
     int error_column = input_stream.getEndColumn();
     String error_after = null;
     boolean EOFSeen = false;
     try { input_stream.readChar(); input_stream.backup(1); }
     catch (java.io.IOException e1) {
        EOFSeen = true;
        error_after = curPos <= 1 ? "" : input_stream.GetImage();
        if (curChar == '\n' || curChar == '\r') {
           error_line++;
           error_column = 0;
        }
        else
           error_column++;
     }
     if (!EOFSeen) {
        input_stream.backup(1);
        error_after = curPos <= 1 ? "" : input_stream.GetImage();
     }
     throw new TokenMgrError(EOFSeen, curLexState, error_line, error_column, error_after, curChar, TokenMgrError.LEXICAL_ERROR);
   }
  }
}

private void jjCheckNAdd(int state)
{
   if (jjrounds[state] != jjround)
   {
      jjstateSet[jjnewStateCnt++] = state;
      jjrounds[state] = jjround;
   }
}
private void jjAddStates(int start, int end)
{
   do {
      jjstateSet[jjnewStateCnt++] = jjnextStates[start];
   } while (start++ != end);
}
private void jjCheckNAddTwoStates(int state1, int state2)
{
   jjCheckNAdd(state1);
   jjCheckNAdd(state2);
}

private void jjCheckNAddStates(int start, int end)
{
   do {
      jjCheckNAdd(jjnextStates[start]);
   } while (start++ != end);
}

}
