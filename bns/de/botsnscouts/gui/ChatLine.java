package de.botsnscouts.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.StringTokenizer;
import de.botsnscouts.util.OptionPane;
import org.apache.log4j.Category;

public class ChatLine extends ColoredComponent implements ActionListener, ComponentListener, KeyListener  {
    static final Category CAT = Category.getInstance(ChatLine.class);
    AusgabeView ausgabeview;
    public JTextField text;
    Timer timer;
    HumanPlayer humanPlayer;

    ChatLine(AusgabeView ausgabeview, HumanPlayer humanPlayer) {
        this.ausgabeview = ausgabeview;
        this.humanPlayer = humanPlayer;
        setLayout( new BorderLayout() );
        add( new JLabel("Chat: "), BorderLayout.WEST );
        text = new JTextField(45);
        text.setBorder( BorderFactory.createEmptyBorder(0, 10, 0, 10) );
        text.setOpaque( false );
        add( text, BorderLayout.CENTER );
        text.addActionListener( this );
        text.addKeyListener( this );

        JButton tb = OptionPane.getTransparentButton("Send", 12);
        add( tb, BorderLayout.EAST );
        tb.addActionListener( this );
        setBorder( OptionPane.etchedBorder );
        timer = new Timer( 5000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible( false );
            }
        });
        addComponentListener( this );
    }
    private boolean autoHide;
    public boolean isAutoHide() {
        return autoHide;
    }
    public void setAutoHide(boolean newAutoHide) {
        autoHide = newAutoHide;
    }

    public void actionPerformed(ActionEvent parm1) {
        String s = text.getText();
        if( s != null && s.trim().length() > 0)
            humanPlayer.sendChat( filter(s) );

        text.setText("");
        setVisible( false );
    }

    public void componentShown(ComponentEvent parm1) {
        CAT.error(parm1);
        text.requestFocus();
        timer.start();
    }
    public void componentHidden(ComponentEvent parm1) {
        CAT.error(parm1);
        ausgabeview.requestFocus();
        text.setText("");
        timer.stop();
    }
    public void componentResized(ComponentEvent parm1) {    }
    public void componentMoved(ComponentEvent parm1) {    }

    public void keyTyped(KeyEvent parm1) {}
    public void keyPressed(KeyEvent parm1) {
        CAT.error( parm1 );
        if( parm1.getKeyCode() == KeyEvent.VK_ESCAPE ) {
            setVisible( false );
        }
        else
            timer.restart();
    }
    public void keyReleased(KeyEvent parm1) {}

    private String filter(String in){
        StringBuffer out=new StringBuffer();
        StringTokenizer st=new StringTokenizer(in, "$,");
        while (st.hasMoreTokens())
            out.append(st.nextToken());
        return out.toString();
    }

}