package de.botsnscouts.board;

/**
 *  Description of the Class
 *
 *@author     enno
 *@created    21. April 2001
 */
public final class Wand {

    public boolean da;
    //private final int[] wandEl = new int[2];      // 0: links oder oben
    private int elementTypeLU, elementTypeRB;

    //  1:rechts oder unten
    //public final int[] spez = new int[2];      //   Laserstärke / Pusherphasen
    private int elementSpecLU, elementSpecRB;

    public static org.apache.log4j.Category CAT = org.apache.log4j.Category.getInstance( Wand.class );

    public final static int WKEINS = 0;
    public final static int WLASER = 1;
    public final static int WPUSHER = 2;
    public final static int W_LEFT_OR_UPPER = 0;
    public final static int W_WRIGHT_OR_BOTTOM = 1;

    public Wand() {
//	wandEl[0]=Spielfeld.WKEINS;
//	wandEl[1]=Spielfeld.WKEINS;
        elementTypeLU = WKEINS;
        elementTypeRB = WKEINS;
        da = false;
    }

    public void setLaser( int index, int strength ) {
        wandEl( index, WLASER );
        spez( index, strength );
    }

    public void setPusher( int index, int ph ) {
        wandEl( index, WPUSHER );
        spez( index, ph );
    }

    public int getElemType( int index ) {
        return wandEl( index );
    }

    public int getElemTypeLU() {
        return elementTypeLU;
    }

    public int getElemTypeBR() {
        return elementTypeRB;
    }

    public int getElemSpecial( int index ) {
        return spez( index );
    }

    public int getElemSpecialLU() {
        return elementSpecLU;
    }

    public int getElemSpecialBR() {
        return elementSpecRB;
    }

    public int wandEl( int i ) {
        if( i == 0 ) {
            return elementTypeLU;
        }
        if( i == 1 ) {
            return elementTypeRB;
        }
        throw new RuntimeException( "accessed unallowed wand elementType (get)" );
    }

    public void wandEl0( int val ) {
        elementTypeLU = val;
    }

    public void wandEl1( int val ) {
        elementTypeRB = val;
    }

    public int wandEl0() {
        return elementTypeLU;
    }

    public int wandEl1() {
        return elementTypeRB;
    }

    public void wandEl( int i, int val ) {
        if( i == 0 ) {
            elementTypeLU = val;
        } else if( i == 1 ) {
            elementTypeRB = val;
        } else {
            throw new RuntimeException( "accessed unallowed wand elementType (set)" );
        }
    }

    public int spez( int i ) {
        if( i == 0 ) {
            return elementSpecLU;
        }
        if( i == 1 ) {
            return elementSpecRB;
        }
        throw new RuntimeException( "accessed unallowed wand elementType (get)" );
    }

    public void spez( int i, int val ) {
        if( i == 0 ) {
            elementSpecLU = val;
        } else if( i == 1 ) {
            elementSpecRB = val;
        } else {
            throw new RuntimeException( "accessed unallowed wand elementType (set)" );
        }
    }

    public void spez0( int val ) {
        elementSpecLU = val;
    }

    public void spez1( int val ) {
        elementSpecRB = val;
    }

    public int spez0() {
        return elementSpecLU;
    }

    public int spez1() {
        return elementSpecRB;
    }

    public boolean isPusherActive( int index, int phase ) {
        return Wand.checkPusherActivity( getElemSpecial( index ), phase );
    }

    public boolean isLUPusherActive( int phase ) {
        return Wand.checkPusherActivity( elementSpecLU, phase );
    }

    public boolean isRBPusherActive( int phase ) {
        return Wand.checkPusherActivity( elementSpecRB, phase );
    }

    public void write( StringBuffer s ) {
        if( elementTypeLU != WKEINS ) {
            s.append('[');
            writeElement( s, elementTypeLU, elementSpecLU );
        }
        s.append( da ? '#' : '_' );
        if( elementTypeRB != WKEINS ) {
            writeElement( s, elementTypeRB, elementSpecRB );
            s.append(']');
        }
    }

    public void writeReversed( StringBuffer s ) {
        if( elementTypeRB != WKEINS ) {
            s.append('[');
            writeElement( s, elementTypeRB, elementSpecRB );
        }
        s.append( da ? '#' : '_' );
        if( elementTypeLU != WKEINS ) {
            writeElement( s, elementTypeLU, elementSpecLU );
            s.append(']');
        }
    }

    private static void writeElement( StringBuffer s, int elementType, int elementSpec ) {
        switch( elementType ) {
            case WLASER:
                s.append( "L(" );
                s.append( elementSpec );
                s.append( ')' );
                break;
            case WPUSHER:
                s.append( "S(" );
                for( int i = 1; i < 6; i++ ) {
                    if( Wand.checkPusherActivity( elementSpec, i ) ) {
                        s.append( i );
                        s.append( ',' );
                    }
                }
                s.append( ')' );
                break;
            default:
                // nothing;
        }
    }

    private static boolean checkPusherActivity( int spez, int phase ) {
        return ( ( spez >> ( phase - 1 ) ) % 2 == 1 );
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        write( sb );
        return sb.toString();
    }
}
