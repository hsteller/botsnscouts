package de.botsnscouts.board;

import de.botsnscouts.util.*;

/**
 *  Description of the Class
 *
 *@author     enno
 *@created    22. April 2001
 */
public class Boden implements FloorConstants {
    public int typ;
    // generelle Bodenart
    public int spez;

    //  spezielle Eigenschaften:

    //   Crusherphasen (Feld ist Crusher wenn spez>0 und Feld Fliessband)
    //   Drehrichtung von Drehelementen
    //   Staerke des Reparaturfeldes
    Boden() {
        spez = 0;
        typ = Spielfeld.BDNORMAL;
    }


    final static String[] RUECK = {"N", "E", "S", "W"};
    public void write(StringBuffer s, boolean drehen) {

        switch (typ) {
            case BDGRUBE:
                s.append('G');
                break;
            case BDNORMAL:
                s.append('B');
                break;
            case BDREPA:
                s.append("R(");
                s.append(spez);
                s.append(')');
                break;
            case BDDREHEL:
                s.append("D(");
                s.append((spez == DUHRZ) ? 'R' : 'L');
                s.append(')');
                break;
            default:
                int btyp = typ;
                if(drehen) {
                    btyp = (typ / 10) * 10;
                    btyp += (((typ % 10) + 3) % 4);
                }
                s.append("F(");
                switch (btyp % 10) {
                    case FNORD:
                        s.append("N,");
                        break;
                    case FOST:
                        s.append("E,");
                        break;
                    case FSUED:
                        s.append("S,");
                        break;
                    case FWEST:
                        s.append("W,");
                        break;
                }
                //switch richtung
                s.append(btyp / 100);
                s.append(",(");
                if(((btyp / 10) % 10) == 2) {
                    // gegen den Uhrzeigersinn
                    s.append('(');
                    s.append(RUECK[((btyp % 10) + 3) % 4]);
                    s.append(",D(L))");
                } else if(((btyp / 10) % 10) == 3) {
                    // im Uhrzeigersinn
                    s.append('(');
                    s.append(RUECK[((btyp % 10) + 1) % 4]);
                    s.append(",D(R))");
                } else if(((btyp / 10) % 10) == 5) {
                    // beides
                    if(drehen) {
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
                if(spez > 0) {
                    for(int i = 1; i < 6; i++) {
                        if(isCrusherActive(i)) {
                            s.append(i);
                            s.append(',');
                        }
                    }
                }
                s.append("))");
                break;
        }
        //switch typ
    }

    public static int parseBoden(int pos, String s, Boden neu) throws FormatException {
        if(ParseUtils.is(s, pos, 'B')) {
            neu.typ = BDNORMAL;
            pos++;
        } else if(ParseUtils.is(s, pos, 'G')) {
            neu.typ = BDGRUBE;
            pos++;
        } else if(ParseUtils.is(s, pos, 'D')) {
            neu.typ = BDDREHEL;
            pos++;
            ParseUtils.assert(s, pos++, '(');
            if(ParseUtils.is(s, pos, 'L')) {
                neu.spez = DGGUHRZ;
            } else if(ParseUtils.is(s, pos, 'R')) {
                neu.spez = DUHRZ;
            } else {
                // Keines der erlaubten Zeichen 'LR' in Position "pos"
                throw new FormatException(Message.say("Spielfeld", "xCharNotAllowed", pos, "LR"));
            }
            pos++;
            ParseUtils.assert(s, pos++, ')');
        } else if(ParseUtils.is(s, pos, 'R')) {
            neu.typ = BDREPA;
            pos++;
            ParseUtils.assert(s, pos++, '(');
            neu.spez = java.lang.Character.digit(s.charAt(pos++), 10);
            ParseUtils.assert(s, pos++, ')');
        } else if(ParseUtils.is(s, pos, 'F')) {
            pos++;
            ParseUtils.assert(s, pos++, '(');
            int typus;
            if(ParseUtils.is(s, pos, 'N')) {
                typus = FNORD;
            } else if(ParseUtils.is(s, pos, 'E')) {
                typus = FOST;
            } else if(ParseUtils.is(s, pos, 'S')) {
                typus = FSUED;
            } else if(ParseUtils.is(s, pos, 'W')) {
                typus = FWEST;
            } else {
                //Keines der erlaubten Zeichen 'NEWS' in Position "pos"
                throw new FormatException(Message.say("Spielfeld", "xCharNotAllowed", pos, "NEWS"));
            }
            pos++;
            ParseUtils.assert(s, pos++, ',');
            typus += 100 * (java.lang.Character.digit(s.charAt(pos++), 10));
            ParseUtils.assert(s, pos++, ',');
            ParseUtils.assert(s, pos++, '(');
            if(ParseUtils.is(s, pos, '(')) {
                pos++;
                char fromR = s.charAt(pos++);
                ParseUtils.assert(s, pos++, ',');
                ParseUtils.assert(s, pos++, 'D');
                ParseUtils.assert(s, pos++, '(');
                char drehR = s.charAt(pos++);
                typus = drehungLegal(typus, fromR, drehR, pos);
                ParseUtils.assert(s, pos++, ')');
                ParseUtils.assert(s, pos++, ')');
            }
            if(ParseUtils.is(s, pos, '(')) {
                pos++;
                char fromR = s.charAt(pos++);
                ParseUtils.assert(s, pos++, ',');
                ParseUtils.assert(s, pos++, 'D');
                ParseUtils.assert(s, pos++, '(');
                char drehR = s.charAt(pos++);
                typus = drehungLegal(typus, fromR, drehR, pos);
                ParseUtils.assert(s, pos++, ')');
                ParseUtils.assert(s, pos++, ')');
            }
            ParseUtils.assert(s, pos++, ')');
            ParseUtils.assert(s, pos++, '(');
            int crusher = 0;
            while(java.lang.Character.isDigit(s.charAt(pos))) {
                crusher += (int) java.lang.Math.pow(2, java.lang.Character.digit(s.charAt(pos++), 10) - 1);
                //d("parseFließbandCrusher: read "+s.charAt(pos-1)+"; crusher="+crusher);
                ParseUtils.assert(s, pos++, ',');
                //if(((typus%100)/10)>1)
                //  throw new FormatException("Keine Crusher auf Drehfliessbaendern! Problem nahe Zeichen "+pos);
                //else if (((typus%100)/10)==0) //sonst ist's schon erhöht
                //  typus+=10;
            }
            ParseUtils.assert(s, pos++, ')');
            ParseUtils.assert(s, pos++, ')');
            neu.typ = typus;
            if(crusher != 0) {
                neu.spez = crusher;
            }
        } else {
            // Keines der erlaubten Zeichen 'BGDRF' in Position "pos"
            throw new FormatException(Message.say("Spielfeld", "xCharNotAllowed", pos, "BGDRF"));
        }
        return pos;
    }

    private static int drehungLegal(int typus, char from, char dreh, int pos) throws FormatException {
        int t = typus;
        switch (typus % 10) {
            case FNORD:
                switch (from) {
                    case 'W':
                        t += 20;
                        break;
                    case 'E':
                        t += 30;
                        break;
                    default:
                        // Es werden nur vernuenftige Drehfliessbaender unterstuetzt. Problem bei Zeichen "pos"
                        throw new FormatException(Message.say("Spielfeld", "xTurnConvBeltNoSense", pos));
                }
                break;
            case FOST:
                switch (from) {
                    case 'N':
                        t += 20;
                        break;
                    case 'S':
                        t += 30;
                        break;
                    default:
                        // Es werden nur vernuenftige Drehfliessbaender unterstuetzt. Problem bei Zeichen "pos"
                        throw new FormatException(Message.say("Spielfeld", "xTurnConvBeltNoSense", pos));
                }
                break;
            case FSUED:
                switch (from) {
                    case 'E':
                        t += 20;
                        break;
                    case 'W':
                        t += 30;
                        break;
                    default:
                        // Es werden nur vernuenftige Drehfliessbaender unterstuetzt. Problem bei Zeichen "pos"
                        throw new FormatException(Message.say("Spielfeld", "xTurnConvBeltNoSense", pos));
                }
                break;
            case FWEST:
                switch (from) {
                    case 'S':
                        t += 20;
                        break;
                    case 'N':
                        t += 30;
                        break;
                    default:
                        // Es werden nur vernuenftige Drehfliessbaender unterstuetzt. Problem bei Zeichen "pos"
                        throw new FormatException(Message.say("Spielfeld", "xTurnConvBeltNoSense", pos));
                }
                break;
        }
        return t;
    }

    public boolean isCrusherActive(int phase)
    {
        return typ>=100 && ((spez>>(phase-1))%2==1);
    }

}
