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

import de.botsnscouts.gui.hotkey.*;
import de.botsnscouts.util.Message;
import de.botsnscouts.widgets.ColoredComponent;
import org.apache.log4j.Category;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Helper class for placing components
 * @author Lukasz Pekacki
 */

public class View extends JFrame {
    static Category CAT = Category.getInstance(View.class);

    private ChatLine chatLine;
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

  private void initChatLine(java.awt.Point p) {
        if (humanView == null) {
          CAT.warn("tried to activate Chat for a GUI without an active plaxer!");
          return;
        }
        chatLine = new ChatLine(ausgabeView, humanView.getHumanPlayer());
        chatLine.setSize( chatLine.getPreferredSize() );
        chatLine.setLocation( p.x + 2, p.y + 2 );
        chatLine.setVisible( false );
  }

  private void showChatLine() {
     chatLine.text.requestFocus();
     chatLine.setVisible( true );

  }

  /** Create hotkeys that will be enabled if this is a GUI of a
   *  participating player.
   *  (Only players should be able to chat)
   */

  private void initPlayerOnlyHotKeys(){
     CAT.debug("initPlayerOnlyHotKeys()");
     HotKeyAction  act = new HotKeyActionAdapter() {
            public void execute(){
                showChatLine();
            }
    };
    // this strange way of creating a hotkey for "enter" is needed because
    // of the crappy IBM KeyEvent..
    HotKey k = new HotKey(HotKeyConf.HOTKEY_SHOW_CHATLINE,
                          new Integer(HotKeyConf.SHOW_CHATLINE),
                          act);
    keyMan.addHotKey(k);


     String [] preparedChatMessages = HotKeyConf.GROUP_MESSAGES;
      for (int i=0;i<HotKeyConf.GROUP_MESSAGES.length;i++) {
        CAT.debug("creating Hotkey for: "+preparedChatMessages [i]);
        ChatMessageEditor editPanel;
        String [] s = HotKeyConf.getOptinalValues(preparedChatMessages[i]);
        if (s == null || s.length==0) {
          CAT.debug("no message properties found");
          editPanel = new ChatMessageEditor(keyMan, preparedChatMessages[i]);
        }
        else if (s.length==1){
          CAT.debug("found chatmessage: "+s[0]);
          CAT.debug("did not find autoCommit");
          editPanel = new ChatMessageEditor(s[0], false,
                      keyMan, preparedChatMessages[i]);
        }
        else { //s.length>1
          CAT.debug("found chatmessage: "+s[0]);
          CAT.debug("found autoCommit property: "+s[1]);
          editPanel = new ChatMessageEditor(s[0], new Boolean(s[1]).booleanValue(),
                      keyMan, preparedChatMessages[i]);
        }
          HotKeyAction act2 = new ChatMessageHotKeyActionAdapter(editPanel){
          public void execute() {
            ChatMessageEditor edit = this.getEditor();
            if (edit.isAutoCommit())
              humanView.sendChatMessage(edit.getMessage());
            else {
              chatLine.text.setText(edit.getMessage());
              showChatLine();
            }

          }
        };
        keyMan.addHotKey(new HotKey(preparedChatMessages [i], act2));

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
        this.addKeyListener(new AbstractHotKeyListener() {
              public void doStuff(KeyEvent e, int hotkeyCode){
                if (CAT.isDebugEnabled()) {
                  CAT.debug("KEYEVENT:");
                  CAT.debug("code="+e.getKeyCode()+"\tchar="+e.getKeyChar()
                          +"\t="+e.getKeyText(e.getKeyCode()));
                  CAT.debug("chatline showing?"+chatLine.isShowing());
                }
               // <hack alert>
                if (chatLine.isShowing()) {
                  if (chatLine.text.hasFocus())
                    CAT.debug("chatline has focus, ignoring key event");
                  else {
                    CAT.debug("forcing focus to chatline!"); //s.o. typing and chatline lost focus!
                    chatLine.text.requestFocus();
                  }
                }
                else {
                  if (chatLine.hasFocus())
                    ausgabeView.requestFocus();
                  keyMan.invoke(hotkeyCode);
                }
                //</hack alert>
              }

          });
    }


    synchronized private void initView() {

	// Fenstergr”ÿe auf Vollbild setzen
	Toolkit tk=Toolkit.getDefaultToolkit();
	this.setSize(tk.getScreenSize().width-8,tk.getScreenSize().height-8);
	this.setLocation(4,4);

	// Fentster-Schliessen behandeln
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
            initChatLine(p);
            this.getLayeredPane().add( chatLine, JLayeredPane.MODAL_LAYER );
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




