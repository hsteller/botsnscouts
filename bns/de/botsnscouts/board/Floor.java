/*
 *******************************************************************
 *        Bots 'n' Scouts - Multi-Player networked Java game       *
 *                                                                 *
 * Copyright (C) 2001 scouties.                                    *
 * Contact botsnscouts-devel@sf.net                                *
 *******************************************************************

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, in version 2 of the License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program, in a file called COPYING in the top
 directory of the Bots 'n' Scouts distribution; if not, write to
 the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 Boston, MA  02111-1307  USA

 *******************************************************************/

package de.botsnscouts.board;

import de.botsnscouts.util.FormatException;
import de.botsnscouts.util.Message;
import de.botsnscouts.util.ParseUtils;

import java.util.HashMap;

public class Floor implements FloorConstants {
    private int type;
    // generelle Bodenart
    private int spec;

    static org.apache.log4j.Category CAT = org.apache.log4j.Category.getInstance(Floor.class);

    final static String[] RUECK = {"N", "E", "S", "W"};

    final static String wallChars = "#_[";

    // spec can be:
    // crusherphases (Field is crusher when spec >0 and belt type)
    // direction of rotating gears
    // strength of repairfield


    ///////////////////////////////////////////////////
    // Factory methods
    ///////////////////////////////////////////////////

    public static Floor getPit() {
        try {
            return Floor.getFloor("G");
        } catch (FormatException fe) {
            CAT.fatal("'G' as floorstring triggers: ", fe);
            return null;
        }
    }

    public static Floor getEmptyFloor() {
        try {
            return Floor.getFloor("B");
        } catch (FormatException fe) {
            CAT.fatal("'B' as floorstring triggers: ", fe);
            return null;
        }
    }

    public static Floor getFloor(String floorString) throws FormatException {
        Floor w = (Floor) cache.get(floorString);
        if (w == null) {
            w = Floor.parseFloor(floorString);
            if (!floorString.equals(w.toString())) {
                CAT.warn("getFloor: Assertion failed: " + floorString + " == " + w.toString());
            }
            cache.put(floorString, w);
        }
        return w;
    }

    public static Floor getFloor(int type, int info) {
        try {
            StringBuffer sb = new StringBuffer(10);
            Floor.write(sb, type, info);
            return Floor.getFloor(sb.toString());
        } catch (FormatException ex) {
            CAT.fatal("getFloor triggers after constructing string itself", ex);
            return null;
        }
    }

    private static HashMap cache = new HashMap();

    ///////////////////////////////////////////////////
    private Floor() {
        this.spec = 0;
        this.type = Board.FL_NORMAL;
    }

    public Floor getWithCrusher(int phases) {
        //CAT.assert(isBelt(), "requesting crusher on non-belt-floor");
        if (!isBelt())
          CAT.warn("ASSERT: requesting crusher on non-belt-floor!");
        return Floor.getFloor(type, phases);
    }

    public int getType() {
        return type;
    }

    public int getInfo() {
        return spec;
    }

    public int getBeltSpeed() {
        return isExpressBelt()?2:1;
    }

    public boolean isPit() {
        return type == FL_PIT;
    }

    public boolean isBelt() {
        return type >= 100;
    }

    public boolean isExpressBelt() {
        return type >= 200;
    }

    public boolean isRepairing() {
        return type == FL_REPAIR;
    }

    public boolean isRotator() {
        return type == FL_ROTGEAR;
    }

    public int getBeltInfo() {
       // CAT.assert(isBelt(), "getBeltInfo() called on non-belt-floor");
        if (!isBelt())
          CAT.warn("ASSERT: getBeltInfo() called on non-belt-floor!");
        return type % 100;
    }

    public int getBeltDirection() {
       // CAT.assert(isBelt(), "getBeltDirection() called on non-belt-floor");
       if (!isBelt())
          CAT.warn("ASSERT: getBeltDirection() called on non-belt-floor!");
        return type % 10;
    }

    public boolean isCrusherActive(int phase) {
        return type >= 100 && ((spec >> (phase - 1)) % 2 == 1);
    }

    public static void write(StringBuffer s, int type, int info, boolean drehen) {
        switch (type) {
            case FL_PIT:
                s.append('G');
                break;
            case FL_NORMAL:
                s.append('B');
                break;
            case FL_REPAIR:
                s.append("R(");
                s.append(info);
                s.append(')');
                break;
            case FL_ROTGEAR:
                s.append("D(");
                s.append((info == GEAR_CLOCKWISE) ? 'R' : 'L');
                s.append(')');
                break;
            default:
                int btyp = type;
                if (drehen) {
                    btyp = (type / 10) * 10;
                    btyp += (((type % 10) + 3) % 4);
                }
                s.append("F(");
                switch (btyp % 10) {
                    case BELT_NORTH:
                        s.append("N,");
                        break;
                    case BELT_EAST:
                        s.append("E,");
                        break;
                    case BELT_SOUTH:
                        s.append("S,");
                        break;
                    case BELT_WEST:
                        s.append("W,");
                        break;
                }
                //switch richtung
                s.append(btyp / 100);
                s.append(",(");
                if (((btyp / 10) % 10) == 2) {
                    // gegen den Uhrzeigersinn
                    s.append('(');
                    s.append(RUECK[((btyp % 10) + 3) % 4]);
                    s.append(",D(L))");
                } else if (((btyp / 10) % 10) == 3) {
                    // im Uhrzeigersinn
                    s.append('(');
                    s.append(RUECK[((btyp % 10) + 1) % 4]);
                    s.append(",D(R))");
                } else if (((btyp / 10) % 10) == 5) {
                    // beides
                    if (drehen) {
                        s.append('(');
                        s.append(RUECK[((btyp % 10) + 3) % 4]);
                        s.append(",D(R))");
                        s.append('(');
                        s.append(RUECK[((btyp % 10) + 1) % 4]);
                        s.append(",D(L))");
                    } else {
                        s.append("(");
                        s.append(RUECK[((btyp % 10) + 1) % 4]);
                        s.append(",D(R))");
                        s.append('(');
                        s.append(RUECK[((btyp % 10) + 3) % 4]);
                        s.append(",D(L))");
                    }
                }
                s.append(")(");
                //crushers
                if (info > 0) {
                    for (int i = 1; i < 6; i++) {
                        if (isCrusherActive(i, type, info)) {
                            s.append(i);
                            s.append(',');
                        }
                    }
                }
                s.append("))");
                break;
        }
        //switch type
    }


    public static void write(StringBuffer s, int type, int info) {
        Floor.write(s, type, info, false);
    }

    public void write(StringBuffer s, boolean drehen) {
        Floor.write(s, type, spec, drehen);
    }

    public static String extractFloorDef(int pos, String s) throws FormatException {
        int i = skipFloorDef(pos, s);
        return s.substring(pos, i);
    }

    // static variant
    private static boolean isCrusherActive(int phase, int type, int info) {
        return type >= 100 && ((info >> (phase - 1)) % 2 == 1);
    }

    private static int skipFloorDef(int pos, String s) throws FormatException {
        // there always comes a wall after a floor element, so we only have to look
        // for the next char that starts a wall defintion
        int idx = pos + 1;
        int l = s.length();
        // look for the first following char thats in 'wallChars'
        while ((idx < l) && (wallChars.indexOf(s.charAt(idx)) == -1)) {
            idx++;
        }
        return idx;
    }

    private static Floor parseFloor(String floorDef) throws FormatException {
        Floor neu = new Floor();
        String s = floorDef;
        int pos = 0;
        if (ParseUtils.is(s, pos, 'B')) {
            neu.type = FL_NORMAL;
            pos++;
        } else if (ParseUtils.is(s, pos, 'G')) {
            neu.type = FL_PIT;
            pos++;
        } else if (ParseUtils.is(s, pos, 'D')) {
            neu.type = FL_ROTGEAR;
            pos++;
            ParseUtils.assertTrue(s, pos++, '(');
            if (ParseUtils.is(s, pos, 'L')) {
                neu.spec = GEAR_COUNTERCLOCKWISE;
            } else if (ParseUtils.is(s, pos, 'R')) {
                neu.spec = GEAR_CLOCKWISE;
            } else {
                // Keines der erlaubten Zeichen 'LR' in Position "pos"
                throw new FormatException(Message.say("Board", "xCharNotAllowed", pos, "LR"));
            }
            pos++;
            ParseUtils.assertTrue(s, pos++, ')');
        } else if (ParseUtils.is(s, pos, 'R')) {
            neu.type = FL_REPAIR;
            pos++;
            ParseUtils.assertTrue(s, pos++, '(');
            neu.spec = java.lang.Character.digit(s.charAt(pos++), 10);
            ParseUtils.assertTrue(s, pos++, ')');
        } else if (ParseUtils.is(s, pos, 'F')) {
            pos++;
            ParseUtils.assertTrue(s, pos++, '(');
            int typus;
            if (ParseUtils.is(s, pos, 'N')) {
                typus = BELT_NORTH;
            } else if (ParseUtils.is(s, pos, 'E')) {
                typus = BELT_EAST;
            } else if (ParseUtils.is(s, pos, 'S')) {
                typus = BELT_SOUTH;
            } else if (ParseUtils.is(s, pos, 'W')) {
                typus = BELT_WEST;
            } else {
                //Keines der erlaubten Zeichen 'NEWS' in Position "pos"
                throw new FormatException(Message.say("Board", "xCharNotAllowed", pos, "NEWS"));
            }
            pos++;
            ParseUtils.assertTrue(s, pos++, ',');
            typus += 100 * (java.lang.Character.digit(s.charAt(pos++), 10));
            ParseUtils.assertTrue(s, pos++, ',');
            ParseUtils.assertTrue(s, pos++, '(');
            if (ParseUtils.is(s, pos, '(')) {
                pos++;
                char fromR = s.charAt(pos++);
                ParseUtils.assertTrue(s, pos++, ',');
                ParseUtils.assertTrue(s, pos++, 'D');
                ParseUtils.assertTrue(s, pos++, '(');
                char drehR = s.charAt(pos++);
                typus = drehungLegal(typus, fromR, drehR, pos);
                ParseUtils.assertTrue(s, pos++, ')');
                ParseUtils.assertTrue(s, pos++, ')');
            }
            if (ParseUtils.is(s, pos, '(')) {
                pos++;
                char fromR = s.charAt(pos++);
                ParseUtils.assertTrue(s, pos++, ',');
                ParseUtils.assertTrue(s, pos++, 'D');
                ParseUtils.assertTrue(s, pos++, '(');
                char drehR = s.charAt(pos++);
                typus = drehungLegal(typus, fromR, drehR, pos);
                ParseUtils.assertTrue(s, pos++, ')');
                ParseUtils.assertTrue(s, pos++, ')');
            }
            ParseUtils.assertTrue(s, pos++, ')');
            ParseUtils.assertTrue(s, pos++, '(');
            int crusher = 0;
            while (java.lang.Character.isDigit(s.charAt(pos))) {
                crusher += (int) java.lang.Math.pow(2, java.lang.Character.digit(s.charAt(pos++), 10) - 1);
                //d("parseFließbandCrusher: read "+s.charAt(pos-1)+"; crusher="+crusher);
                ParseUtils.assertTrue(s, pos++, ',');
                //if(((typus%100)/10)>1)
                //  throw new FormatException("Keine Crusher auf Drehfliessbaendern! Problem nahe Zeichen "+pos);
                //else if (((typus%100)/10)==0) //sonst ist's schon erhöht
                //  typus+=10;
            }
            ParseUtils.assertTrue(s, pos++, ')');
            ParseUtils.assertTrue(s, pos++, ')');
            neu.type = typus;
            if (crusher != 0) {
                neu.spec = crusher;
            }
        } else {
            // Keines der erlaubten Zeichen 'BGDRF' in Position "pos"
            throw new FormatException(Message.say("Board", "xCharNotAllowed", pos, "BGDRF"));
        }
        return neu;
    }


    private static int drehungLegal(int typus, char from, char dreh, int pos) throws FormatException {
        int t = typus;
        switch (typus % 10) {
            case BELT_NORTH:
                switch (from) {
                    case 'W':
                        t += 20;
                        break;
                    case 'E':
                        t += 30;
                        break;
                    default:
                        // Es werden nur vernuenftige Drehfliessbaender unterstuetzt. Problem bei Zeichen "pos"
                        throw new FormatException(Message.say("Board", "xTurnConvBeltNoSense", pos));
                }
                break;
            case BELT_EAST:
                switch (from) {
                    case 'N':
                        t += 20;
                        break;
                    case 'S':
                        t += 30;
                        break;
                    default:
                        // Es werden nur vernuenftige Drehfliessbaender unterstuetzt. Problem bei Zeichen "pos"
                        throw new FormatException(Message.say("Board", "xTurnConvBeltNoSense", pos));
                }
                break;
            case BELT_SOUTH:
                switch (from) {
                    case 'E':
                        t += 20;
                        break;
                    case 'W':
                        t += 30;
                        break;
                    default:
                        // Es werden nur vernuenftige Drehfliessbaender unterstuetzt. Problem bei Zeichen "pos"
                        throw new FormatException(Message.say("Board", "xTurnConvBeltNoSense", pos));
                }
                break;
            case BELT_WEST:
                switch (from) {
                    case 'S':
                        t += 20;
                        break;
                    case 'N':
                        t += 30;
                        break;
                    default:
                        // Es werden nur vernuenftige Drehfliessbaender unterstuetzt. Problem bei Zeichen "pos"
                        throw new FormatException(Message.say("Board", "xTurnConvBeltNoSense", pos));
                }
                break;
        }
        return t;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        write(sb, false);
        return sb.toString();
    }
}
