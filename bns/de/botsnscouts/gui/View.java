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
import de.botsnscouts.widgets.LogFloatPane;
import org.apache.log4j.Category;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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

    private HotKeyMan keyMan; 
  //  private KeyListener hotkeyListener;
    private JSplitPane sp;
    private final JComponent contentpane= new JPanel();
    private AusgabeView ausgabeView;
    private  HumanView humanView;

    protected final boolean NURAUSGABE = true;
    protected final boolean MENSCHAUSGABE = false;

    public View(AusgabeView av) {
        this.setContentPane( contentpane );
        this.setTitle(Message.say("AusgabeFrame","gameName"));		
		ausgabeView=av;
		initView();
        ColoredComponent comp = new ColoredComponent();
        comp.setLayout(new BorderLayout());
        comp.add(av, BorderLayout.CENTER);
        comp.add(av.getNorthPanel(), BorderLayout.NORTH);
        contentpane.add(comp);       
        initHotKeys(av);
        makeVisible();

    }

    public View(HumanView hv) {
        this.setContentPane( contentpane );
		this.setTitle(Message.say("AusgabeFrame","gameName"));
		humanView = hv;
		initView();
        hv.setMinimumSize(new Dimension(0, 0));
        sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        sp.setRightComponent(hv);
        sp.setOneTouchExpandable(true);
        sp.setOpaque(false);
        contentpane.add ( sp);
        // not AusgabeView yet :(
        // => will call initHotKeys in addAusgabeView()
        //initHotKeys();
        this.getContentPane().add(sp, BorderLayout.CENTER);
      
 
    }
    
    

  private void initChatLine(java.awt.Point p) {
        if (humanView == null) {
          CAT.warn("tried to activate Chat for a GUI without an active player!");
          return;
        }
        chatLine = new ChatLine(/*ausgabeView*/this, humanView.getHumanPlayer());
        chatLine.setSize( chatLine.getPreferredSize() );
        chatLine.setLocation( p.x + 2, p.y + 2 );
        chatLine.setVisible( false );
  }

  public void requestFocus(){
      if (contentpane!=null){
          contentpane.requestFocus(); 
     }
      else
          super.requestFocus();
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
     // creating HotKey(Action) for showing the Chatline
    HotKeyAction showChat = new HotKeyAction(){
         public void actionPerformed(ActionEvent ae){                           
             showChatLine();
         }
     };
   
    HotKey key = new HotKey(HotKeyConf.HOTKEY_SHOW_CHATLINE,
                    								KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                    								showChat);
    keyMan.addHotKey(key);
    
    // creating HotKey(Action)s for sending prepared Chatmessages
    // and the GUI-components to edit their values
    String [] preparedChatMessages = HotKeyConf.GROUP_MESSAGES;
    int chatMsgCount = HotKeyConf.GROUP_MESSAGES.length;
    for (int i = 0; i < chatMsgCount; i++) {
            CAT.debug("creating Hotkey for: " + preparedChatMessages[i]);
            String chatMessageName = preparedChatMessages[i];
            ChatMessageEditor editPanel = ChatMessageEditor.createEditorForMessage(chatMessageName, keyMan);

            HotKeyAction sendChatlineAction = new ChatMessageHotKeyActionAdapter(editPanel) {
                public void actionPerformed(ActionEvent act) {
                    ChatMessageEditor edit = this.getEditor();
                    if (edit.isAutoCommit())
                        humanView.sendChatMessage(edit.getMessage());
                    else {
                        chatLine.text.setText(edit.getMessage());
                        showChatLine();
                    }

                }
            };
            keyMan.addHotKey(new HotKey(preparedChatMessages[i], sendChatlineAction));
      }
     if (CAT.isDebugEnabled())
        CAT.debug(keyMan.dump());
  }


  /** 
  *       Hotkeys that will be used by both players and spectators will be defined
  *       in AusgabeView
  */
    private void initHotKeys(AusgabeView av) {
        CAT.debug("initHotKeys()");
        if (keyMan == null) {
            	keyMan = new HotKeyMan(contentpane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT),
            	                								contentpane.getActionMap());
        }
        
        if (humanView != null) {
            initPlayerOnlyHotKeys();
        }
        if ( av != null) {
            av.initHotKeysAndAddToHotkeyman(keyMan);
        }
    }


    synchronized private void initView() {

	// set window size to fullscreen 
	
    Toolkit tk=Toolkit.getDefaultToolkit();   
	this.setSize(tk.getScreenSize().width-8,tk.getScreenSize().height-8);	
	this.setLocation(4,4);

	// handle window closing 
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
	// create layout
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
        initHotKeys(av);
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




