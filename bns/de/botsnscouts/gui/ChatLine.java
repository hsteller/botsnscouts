package de.botsnscouts.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import org.apache.log4j.Category;

public class ChatLine extends ColoredComponent implements ActionListener, ComponentListener, KeyListener  {
    static final Category CAT = Category.getInstance(ChatLine.class);
    AusgabeView ausgabeview;
    public JTextField text;
    Timer timer;

    ChatLine(AusgabeView ausgabeview) {
        this.ausgabeview = ausgabeview;
        setLayout( new BorderLayout() );
        add( new JLabel("Chat: "), BorderLayout.WEST );
        text = new JTextField(45);
        text.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10) );
        text.setOpaque( false );
        add( text, BorderLayout.CENTER );
        text.addActionListener( this );
        text.addKeyListener( this );

        JButton tb = de.botsnscouts.util.OptionPane.getTransparentButton("Send", 12);
        add( tb, BorderLayout.EAST );
        tb.addActionListener( this );
        setBorder( BorderFactory.createCompoundBorder(
            BorderFactory.createEtchedBorder( new Color(0, 255,0, 128), new Color(0,128,0,128) ),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
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
        if( s == null || s.trim().length() == 0)
            return;

        ausgabeview.showActionMessage( text.getText() );
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
        timer.restart();
    }
    public void keyReleased(KeyEvent parm1) {}

}