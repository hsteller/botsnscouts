package de.botsnscouts.board;

/**
 *  Description of the Class
 *
 *@author     enno
 *@created    21. April 2001
 */

import de.botsnscouts.util.*;

import java.util.HashMap;

public class Wall {
    static org.apache.log4j.Category CAT = org.apache.log4j.Category.getInstance( Wall.class );
    private boolean da;

    // NW = left or upper, SE = right or bottom
    private int deviceTypeNW, deviceTypeSE;
    private int deviceInfoNW, deviceInfoSE;

    public final static int NONE = 0;
    public final static int TYPE_LASER = 1;
    public final static int TYPE_PUSHER = 2;

    public final static int W_NORTH_OR_WEST = 0;
    public final static int W_SOUTH_OR_EAST = 1;
    public final static int W_NORTH = 0;
    public final static int W_WEST  = 0;
    public final static int W_SOUTH = 1;
    public final static int W_EAST  = 1;


    private static HashMap cache = new HashMap();

    public static Wall getWall( String wallString ) throws FormatException {
        Wall w = (Wall)cache.get( wallString );
        if( w == null ) {
            w = parseWall( wallString );
            if( ! wallString.equals( w.toString() ) ) {
                CAT.error( "assertion failed: " + wallString + " == " + w.toString(), new Exception() );
            }
            cache.put(wallString, w);
        }
        return w;
    }

    public static Wall getWall(int nwType, int nwInfo, int seType, int seInfo) {
        try {
            StringBuffer sb = new StringBuffer(10);
            Wall.write(sb, nwType, nwInfo, seType, seInfo);
            return Wall.getWall( sb.toString() );
        }
        catch (FormatException ex) {
            CAT.fatal("getWall triggers after constructing string itself");
            return null;
        }
    }

    public static Wall getNonExistingWall() {
        try {
            return Wall.getWall("_");
        } catch( FormatException fe ) {
            CAT.fatal( "'_' as wallstring triggers: ", fe );
            return null;
        }
    }

    public static Wall getEmptyWall() {
        try {
            return Wall.getWall("#");
        } catch( FormatException fe ) {
            CAT.fatal( "'_' as wallstring triggers: ", fe );
            return null;
        }
    }

    private Wall() {
        deviceTypeNW = NONE;
        deviceTypeSE = NONE;
        da = false;
    }

    public Wall getWithNWLaser(int strength) {
        if( strength == 0 )
            return Wall.getWall( NONE, 0, deviceTypeSE, deviceInfoSE );
        else
            return Wall.getWall( TYPE_LASER, strength, deviceTypeSE, deviceInfoSE );
    }

    public Wall getWithSELaser(int strength) {
        if( strength == 0 )
            return Wall.getWall( deviceTypeNW, deviceInfoNW, NONE, 0 );
        else
            return Wall.getWall( deviceTypeNW, deviceInfoNW, TYPE_LASER, strength );
    }

    public Wall getWithNWPusher(int phases) {
        if( phases == 0 )
            return Wall.getWall( NONE, 0, deviceTypeSE, deviceInfoSE );
        else
            return Wall.getWall( TYPE_PUSHER, phases, deviceTypeSE, deviceInfoSE );
    }

    public Wall getWithSEPusher(int phases) {
        if( phases == 0 )
            return Wall.getWall( deviceTypeNW, deviceInfoNW, NONE, 0 );
        else
            return Wall.getWall( deviceTypeNW, deviceInfoNW, TYPE_PUSHER, phases );
    }

    public Wall getWithNWDevice(Wall wall) {
        return Wall.getWall( wall.deviceTypeNW, wall.deviceInfoNW, deviceTypeSE, deviceInfoSE );
    }

    public Wall getWithSEDevice(Wall wall) {
        return Wall.getWall( deviceTypeNW, deviceInfoNW, wall.deviceTypeSE, wall.deviceInfoSE );
    }

    private void setPusher(int index, int phases) {
        if(index == W_NORTH_OR_WEST) {
            deviceTypeNW = TYPE_PUSHER;
            deviceInfoNW = phases;
        } else {
            deviceTypeSE = TYPE_PUSHER;
            deviceInfoSE = phases;
        }
    }

    private void setLaser(int index, int strength) {
        if(index == W_NORTH_OR_WEST) {
            deviceTypeNW = TYPE_LASER;
            deviceInfoNW = strength;
        } else {
            deviceTypeSE = TYPE_LASER;
            deviceInfoSE = strength;
        }
    }

    public boolean isExisting() {
        return da;
    }

    public int getNWDeviceType() {
        return deviceTypeNW;
    }

    public int getEastDeviceType() {
        return deviceTypeSE;
    }
    public int getNorthDeviceType() {
        return deviceTypeNW;
    }
    public int getWestDeviceType() {
        return deviceTypeNW;
    }
    public int getSouthDeviceType() {
        return deviceTypeSE;
    }

    public int getEastDeviceInfo() {
        return deviceInfoSE;
    }
    public int getNorthDeviceInfo() {
        return deviceInfoNW;
    }
    public int getWestDeviceInfo() {
        return deviceInfoNW;
    }
    public int getSouthDeviceInfo() {
        return deviceInfoSE;
    }

    public int getSEDeviceType() {
        return deviceTypeSE;
    }

    public int getNWDeviceInfo() {
        return deviceInfoNW;
    }

    public int getSEDeviceInfo() {
        return deviceInfoSE;
    }

    /// new style "intelligent" methods
    public boolean isSouthPusherActive(int phase) {
        return deviceTypeSE == TYPE_PUSHER && Wall.checkPusherActivity(deviceInfoSE, phase);
    }
    public boolean isEastPusherActive(int phase) {
        return deviceTypeSE == TYPE_PUSHER && Wall.checkPusherActivity(deviceInfoSE, phase);
    }
    public boolean isNorthPusherActive(int phase) {
        return deviceTypeNW == TYPE_PUSHER && Wall.checkPusherActivity(deviceInfoNW, phase);
    }
    public boolean isWestPusherActive(int phase) {
        return deviceTypeNW == TYPE_PUSHER && Wall.checkPusherActivity(deviceInfoNW, phase);
    }




    public void write(StringBuffer s) {
        if(deviceTypeNW != NONE) {
            s.append('[');
            writeDevice(s, deviceTypeNW, deviceInfoNW);
        }
        s.append(da ? '#' : '_');
        if(deviceTypeSE != NONE) {
            writeDevice(s, deviceTypeSE, deviceInfoSE);
            s.append(']');
        }
    }

    private static void write(StringBuffer s, int nwType, int nwInfo, int seType, int seInfo) {
        if(nwType != NONE) {
            s.append('[');
            writeDevice(s, nwType, nwInfo);
        }
        s.append('#');
        if(seType != NONE) {
            writeDevice(s, seType, seInfo);
            s.append(']');
        }
    }

    public void writeReversed(StringBuffer s) {
        if(deviceTypeSE != NONE) {
            s.append('[');
            writeDevice(s, deviceTypeSE, deviceInfoSE);
        }
        s.append(da ? '#' : '_');
        if(deviceTypeNW != NONE) {
            writeDevice(s, deviceTypeNW, deviceInfoNW);
            s.append(']');
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        write(sb);
        return sb.toString();
    }


//    private int getDeviceType(int i) {
//        if(i == 0) {
//            return deviceTypeNW;
//        }
//        if(i == 1) {
//            return deviceTypeSE;
//        }
//        throw new RuntimeException("accessed unallowed wand deviceType (get)");
//    }
//
//    private int getDeviceInfo(int i) {
//        if(i == 0) {
//            return deviceInfoNW;
//        }
//        if(i == 1) {
//            return deviceInfoSE;
//        }
//        throw new RuntimeException("accessed unallowed wand deviceType (get)");
//    }
//

    private static boolean checkPusherActivity(int spez, int phase) {
        return ((spez >> (phase - 1)) % 2 == 1);
    }


    private static void writeDevice(StringBuffer s, int deviceType, int deviceInfo) {
        switch (deviceType) {
            case TYPE_LASER:
                s.append("L(");
                s.append(deviceInfo);
                s.append(')');
                break;
            case TYPE_PUSHER:
                s.append("S(");
                for(int i = 1; i < 6; i++) {
                    if(Wall.checkPusherActivity(deviceInfo, i)) {
                        s.append(i);
                        s.append(',');
                    }
                }
                s.append(')');
                break;
            default:
            // nothing;
        }
    }

    private static Wall parseWall(String wallString) throws FormatException {
        Wall neu = new Wall();
        int strpos = 0;
        if(ParseUtils.is(wallString, strpos, '#')) {
            neu.da = true;
            strpos++;
        } else if(ParseUtils.is(wallString, strpos, '_')) {
            neu.da = false;
            return neu;
        } else if(ParseUtils.is(wallString, strpos, '[')) {
            strpos++;
            neu.da = true;
            if(ParseUtils.is(wallString, strpos, 'L')) {
                strpos = parseL(++strpos, wallString, neu, Wall.W_NORTH_OR_WEST);
            } else if(ParseUtils.is(wallString, strpos, 'S')) {
                strpos = parseS(++strpos, wallString, neu, Wall.W_NORTH_OR_WEST);
            }
            ParseUtils.assert(wallString, strpos++, '#');
        } else {
            // "Fand keinen der erlaubten Chars '#_[' in Position "strpos"; da ist:"wallString.charAt(strpos)"
            throw new FormatException(Message.say("Spielfeld", "xCharsNotFound", "#_[", strpos, "" + wallString.charAt(strpos)));
        }
        if( strpos == wallString.length() ) return neu;
        if(ParseUtils.is(wallString, strpos, 'L')) {
            strpos = parseL(++strpos, wallString, neu, Wall.W_SOUTH_OR_EAST);
            ParseUtils.assert(wallString, strpos++, ']');
        } else if(ParseUtils.is(wallString, strpos, 'S')) {
            strpos = parseS(++strpos, wallString, neu, Wall.W_SOUTH_OR_EAST);
            ParseUtils.assert(wallString, strpos++, ']');
        }
        return neu;
    }

    private static int parseS(int pos, String s, Wall it, int index) throws FormatException {
        ParseUtils.assert(s, pos++, '(');
        int tmp = 0;
        while(!ParseUtils.is(s, pos, ')')) {
            int digit = java.lang.Character.digit(s.charAt(pos++), 10);
            tmp += (int) java.lang.Math.pow(2, digit - 1);
            ParseUtils.assert(s, pos++, ',');
        }
        ParseUtils.assert(s, pos++, ')');
        it.setPusher(index, tmp);
        return pos;
    }

    private static int parseL(int pos, String s, Wall it, int index) throws FormatException {
        ParseUtils.assert(s, pos++, '(');
        int str = java.lang.Character.digit(s.charAt(pos++), 10);
        ParseUtils.assert(s, pos++, ')');
        it.setLaser(index, str);
        return pos;
    }

    static int skipWallDef(int strpos,String kacheln) throws FormatException
    {
      // return pointer to the first char after wall-definition
      // no wall?
      if(ParseUtils.is(kacheln,strpos,'_')) {
        return strpos+1;
      }

      // read up to the '#'
      while( kacheln.charAt(strpos) != '#' )
        strpos++;

      // first char after '#'
      strpos++;

      if(ParseUtils.is(kacheln,strpos,'L') || ParseUtils.is(kacheln,strpos,'S')){ // device?
          while( kacheln.charAt(strpos) != ']' )
            strpos++;

          strpos++;
      }
      return strpos;
    }


    public static void main(String[] args) throws FormatException {
        org.apache.log4j.BasicConfigurator.configure();
        getWall("_");
        getWall("#");
        getWall("[L(2)#");
        getWall("#L(1)]");
        getWall("[S(1,2,3,)#L(3)]");
        getWall("#S(1,5,)]");
        getWall("[L(4)#S(1,5,)]");
        getWall("_");
        getWall("#");
        getWall("[L(2)#");
        getWall("#L(1)]");
        getWall("[S(1,2,3,)#L(3)]");
        getWall("#S(1,5,)]");
        getWall("[L(4)#S(1,5,)]");
        getWall("#S(1,5,)]");
        getWall("[L(4)#S(1,5,)]");
        getWall("_");
        getWall("#");
        getWall("[L(2)#");
    }
}
