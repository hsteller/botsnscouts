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

package de.botsnscouts.gui;

import de.botsnscouts.util.*;
import de.botsnscouts.gui.hotkey.*;

import java.awt.*;
import java.io.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.HashMap;


import org.apache.log4j.Category;

/**
 * Helferklasse, die die Komponenten auf dem Bildschirm plaziert
 * @author Lukasz Pekacki
 */

public class View extends JFrame {
    static Category CAT = Category.getInstance(View.class);

    private ChatPane chatpane;
    private JMenuBar menus;

    private HotKeyMan keyMan = new HotKeyMan();

    JSplitPane sp;

    AusgabeView ausgabeView;
    HumanView humanView;

    protected final boolean NURAUSGABE = true;
    protected final boolean MENSCHAUSGABE = false;

    public View(AusgabeView av) {
	this.setTitle(Message.say("AusgabeFrame","gameName"));
	ausgabeView=av;
	initView();
        JComponent content = new ColoredComponent();
        content.setLayout(new BorderLayout());
	content.add(av, BorderLayout.CENTER);
        content.add(av.getNorthPanel(), BorderLayout.NORTH);
        this.setContentPane( content );
        initHotKeys();
	makeVisible();
    }

    public View(HumanView hv) {
	this.setTitle(Message.say("AusgabeFrame","gameName"));
	humanView = hv;
	initView();
        hv.setMinimumSize(new Dimension(0, 0));

        sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        sp.setRightComponent(hv);
        sp.setOneTouchExpandable(true);
        sp.setOpaque(false);
        // not AusgabeView yet :(
        // => will call initHotKeys in addAusgabeView()
        //initHotKeys();
	this.getContentPane().add(sp, BorderLayout.CENTER);
    }



  /** Create hotkeys that will be enabled if this is a GUI of a
   *  participating player.
   *  (Only players should be able to chat)
   */

  private void initPlayerOnlyHotKeys(){
    CAT.debug("initPlayerOnlyHotKeys()");
     String [] preparedChatMessages = HotKeyConf.MSGS;
      for (int i=0;i<HotKeyConf.MSGS.length;i++) {
        CAT.debug("creating Hotkey for: "+preparedChatMessages [i]);
        String s = HotKeyConf.getOptinalValue(preparedChatMessages[i]);
          HotKeyAction act = new ChatMessageHotKeyActionAdapter(new JTextField(s)){
          public void execute() {
            humanView.sendChatMessage(getOptionalValue());
          }
        };

        keyMan.setHotKey(preparedChatMessages [i], act);

      }
     if (CAT.isDebugEnabled())
        CAT.debug(keyMan.dump());
  }


  /** Note: the hotkey for showing the chatline is initialized in makeVisible()
  *       because the chatline is created there and the hotkey's HotKeyAction.execute();
  *       needs a reference of the chatline.
  *       Hotkeys that will be used by both players and spectators will be defined
  *       in AusgabeView
  */
    private void initHotKeys() {
        CAT.debug("initHotKeys()");
        keyMan = ausgabeView.getHotKeyManager();
        if (humanView != null) {
            initPlayerOnlyHotKeys();
        }
        CAT.debug("adding Keylistener");

        // !! IBM's JDK1.3 KeyEvent is buggy/crap, but this this trial&error approach
        // of mixing keyCode/KeyChar seems to work for the beginning !!
        this.addKeyListener(new KeyAdapter() {
              public void keyTyped(KeyEvent e) {
                 if (CAT.isDebugEnabled()) {
                  CAT.debug("KEYTyped!");
                  dumpEvent(e);
                 }
                  keyMan.invoke(e.getKeyChar());
              }
              public void keyPressed(KeyEvent e){
                 if (CAT.isDebugEnabled()) {
                   CAT.debug("KEYPressed!");
                   dumpEvent(e);
                 }
                 keyMan.invoke(e.getKeyCode());
              }
              public void dumpEvent(KeyEvent e) {
               CAT.debug(" keychar: "+e.getKeyChar()+"\nkeycode: "+e.getKeyCode()
                          +"\nnumValue: "+Character.getNumericValue(e.getKeyChar()));

                int mods = e.getModifiers();
                String ms = e.getKeyModifiersText(mods);
                CAT.debug("mods="+mods+"\tmodString="+ms);
                CAT.debug("keyText="+e.getKeyText(e.getKeyCode()));
                CAT.debug("ID="+e.getID());
                CAT.debug("paramString="+e.paramString());
                CAT.debug("consumed?"+e.isConsumed());
                CAT.debug("actionKey?"+e.isActionKey());
                KeyStroke stroke = KeyStroke.getKeyStrokeForEvent(e);
                CAT.debug("stroke stuff: ");
                CAT.debug("\t"+stroke.getKeyChar());
                CAT.debug("\t"+stroke.getKeyCode());
                CAT.debug("\tchar#:"+Character.getNumericValue(stroke.getKeyChar()));
                CAT.debug("STROKE="+stroke.toString());


              }
              public void keyReleased(KeyEvent e) {}
          });
    }


    synchronized private void initView() {

	// Fenstergr”ÿe auf Vollbild setzen
	Toolkit tk=Toolkit.getDefaultToolkit();
	this.setSize(tk.getScreenSize().width-8,tk.getScreenSize().height-8);
	this.setLocation(4,4);

	// Fentster-Schlieÿen behandeln
	this.addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e){
		      if (ausgabeView!=null){
                          ausgabeView.quit(false); // will tell all the others to quit, using
                                                   // the same functions like the Quit-game-button;
                                                   // quitHumanPlayer() will be called, too
                      }
                      else {
                          CAT.fatal("ausgabeView is null!!!");
                      }
                     //shutup()
		}});

	// Layout erzeugen
	this.getContentPane().setLayout(new BorderLayout());
        addMenuBar();

    }

    void traverseMenu( MenuElement menu ) {
        Component c = menu.getComponent();
        c.setBackground(Color.black);
        c.setForeground(Color.lightGray);
        MenuElement[] submenu = menu.getSubElements();
        for (int i = 0; i < submenu.length; i++) {
            traverseMenu( submenu[i] );
        }
    }

    private void addMenuBar(){
      if (menus==null){
        CAT.debug("menus==null; getting menus..");
        if (ausgabeView!=null){
           menus = ausgabeView.getMenuBar();
           traverseMenu( menus );
           this.setJMenuBar(menus);
           menus.setVisible(true);
        }
        else {
          CAT.debug("unable to install MenuBar! No AusgabeView found!");
        }
      }
      else {
        CAT.debug("menus already loaded!\n Now setting them..");
        this.setJMenuBar(menus);
      }
      CAT.debug("Leaving addMenubar");
    }

    protected void makeVisible() {
        CAT.debug("makeVisible called");
        addMenuBar();


	this.setVisible(true);

        JComponent board = ausgabeView.getBoardView();
        Point p = board.getLocationOnScreen();

        logFloatPane = new LogFloatPane(board, this.getLayeredPane());
        logFloatPane.setExpandedSize( new Dimension( 400, 300 ) );
        logFloatPane.setNormalSize( new Dimension( 400, 26 ) );
        logFloatPane.setExpanded( false );
        this.getLayeredPane().add( logFloatPane, JLayeredPane.MODAL_LAYER );

        if (humanView!=null) {
          final ChatLine cp = new ChatLine( ausgabeView, humanView.getHumanPlayer() );


           HotKeyAction  act = new HotKeyActionAdapter() {
            public void execute(){
               if( humanView == null )
                 return;
               cp.setVisible( true );
               cp.text.requestFocus();

            }
          };
          // this strange way of creating a hotkey for "enter" is needed because
          // of the crappy IBM KeyEvent..
          HotKey k = new HotKey(HotKeyConf.HOTKEY_SHOW_CHATLINE,
                                new Integer(HotKeyConf.SHOW_CHATLINE),
                                act);
          keyMan.setHotKey(k);

          cp.setSize( cp.getPreferredSize() );
          cp.setLocation( p.x + 2, p.y + 2 );
          cp.setVisible( false );


          this.getLayeredPane().add( cp, JLayeredPane.MODAL_LAYER );
        }
	this.validate();

    }




    public LogFloatPane logFloatPane;

    protected void showGameStatusMessage(String s) {
        if(humanView != null) {
            humanView.showMessageToPlayer(s);
        }
    }

    public void addAusgabeView(AusgabeView av) {
        CAT.debug("addAusgabeView called");
	if (ausgabeView==null) {
          CAT.debug("ausgabeView is null");
          ausgabeView=av;
          this.getContentPane().add(ausgabeView.getNorthPanel(), BorderLayout.NORTH);
          this.addMenuBar();
        }
        if (humanView!=null){
          initHotKeys();
          Toolkit tk = Toolkit.getDefaultToolkit();
          int w = tk.getScreenSize().width-8;
          int h = tk.getScreenSize().height-8;
          int leftPanelWidth=Math.max(w-250, 400);
          int leftPanelHeight=Math.max(h-250, 350);
          if (CAT.isDebugEnabled()){
            CAT.debug("Setting minimum and preferred width of left Splitpane to: "+leftPanelWidth);
            CAT.debug("Setting minimum height of left Splitpane to: "+leftPanelHeight);
          }

          JPanel p = humanView.getWiseAndScoutPanel();
          av.addWiseAndScout(p);
          av.setPreferredSize(new Dimension(leftPanelWidth,leftPanelHeight));
          av.setMinimumSize(new Dimension(leftPanelWidth,leftPanelHeight));
          humanView.setPreferredSize(new Dimension(250, 400));
          sp.setLeftComponent(av);
         // sp.setDividerLocation(0.75);
        }
        else{
          this.getContentPane().add(av, BorderLayout.CENTER);
        }
    }

    public void addChatPane(ChatPane cp){
        chatpane = cp;
        this.getContentPane().add(cp, BorderLayout.SOUTH);

    }

    public void removeChatPane() {
        this.getContentPane().remove(chatpane);
        chatpane = null;
    }

    protected void quitHumanPlayer() {
        CAT.debug("View is telling the human player to quit..");
        if (humanView!=null) {
          humanView.quitHumanPlayer();
          humanView=null;
          CAT.debug(".. done");
        }
        else {
          CAT.debug("There seems to be no human player, I'm propably a standalone Ausgabe(view)..");
        }
        CAT.debug("Disposing the this..");
        this.setVisible(false);
        CAT.debug("View is now invisible");

    }
}




