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
 
package de.botsnscouts.editor;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import de.botsnscouts.board.SpielfeldSim;
import de.botsnscouts.board.FlaggenException;
import de.botsnscouts.util.Message;
import de.botsnscouts.util.FormatException;

import org.apache.log4j.Category;
import com.keypoint.PngEncoder;


class ButtonBar extends JPanel implements ActionListener{
    static final Category CAT = Category.getInstance( ButtonBar.class );

    private JButton feldSpeich=null;
    private JButton feldLaden=null;
    private JButton feldClear=null;
    private JButton exit=null;
    private JButton export=null;
    protected BoardEditor editor=null;
    JFileChooser chooser;
    FileFilter rraFilter = new FileFilter() {
            public boolean accept(File file) {
                String name = file.getName();
                return file.isDirectory() || name.toLowerCase().endsWith(".rra");
            }

            public String getDescription() {
                return "rra";
            }
        };

    FileFilter pngFilter = new FileFilter() {
            public boolean accept(File file) {
                String name = file.getName();
                return file.isDirectory() || name.toLowerCase().endsWith(".png");
            }

            public String getDescription() {
                return "png";
            }
        };


    void makeChooser() {
	    chooser = new JFileChooser("kacheln");
	    chooser.setFileFilter(rraFilter);
    }


    public ButtonBar(BoardEditor p){
	editor=p;
	setLayout( new BorderLayout() );
	JToolBar tb = new JToolBar();
	//	setLayout(new GridLayout(1,3));

	feldLaden = new JButton(Message.say("BoardEditor","bLaden"));
	feldLaden.addActionListener(this);
	feldLaden.setActionCommand("Laden");
	tb.add(feldLaden);

	feldSpeich = new JButton(Message.say("BoardEditor","bSpeichern"));
	feldSpeich.addActionListener(this);
	feldSpeich.setActionCommand("Speichern");
	tb.add(feldSpeich);

	feldClear = new JButton(Message.say("BoardEditor","bClear"));
	feldClear.addActionListener(this);
	feldClear.setActionCommand("Clear");
	tb.add(feldClear);


	exit= new JButton(Message.say("BoardEditor","bBeenden"));
	exit.addActionListener(this);
	exit.setActionCommand("Beenden");
	tb.add(exit);
	add( tb, BorderLayout.NORTH );
	JMenuBar jb = new JMenuBar();
	JMenu jm = new JMenu("File");
	JMenuItem mi;
	mi = new JMenuItem(Message.say("BoardEditor","bLaden"));
	mi.addActionListener(this);
	mi.setActionCommand("Laden");
	jm.add( mi );
	mi = new JMenuItem(Message.say("BoardEditor","bSpeichern"));
	mi.addActionListener(this);
	mi.setActionCommand("Speichern");
	jm.add( mi );
	mi = new JMenuItem(Message.say("BoardEditor","bExport"));

        ActionListener exportListener = new ActionListener() {
            public void actionPerformed( ActionEvent ae ) {
                doExport();
            }
        };

	mi.addActionListener( exportListener );
	export= new JButton(Message.say("BoardEditor","bExport"));
	export.addActionListener( exportListener );
	export.setActionCommand("Export");
        tb.add(export);

	mi.setActionCommand("Export");
	jm.add( mi );
	jm.addSeparator();
	mi = new JMenuItem(Message.say("BoardEditor","bBeenden"));
	mi.addActionListener(this);
	mi.setActionCommand("Beenden");
	jm.add( mi );
	jb.add( jm );
	editor.setJMenuBar( jb );
	makeChooser();

    }


    public void doExport() {
        BufferedImage bi = editor.getBufferedImage();
        byte[] pngBytes = (new PngEncoder(bi)).pngEncode();
        chooser.setFileFilter( pngFilter );
        chooser.rescanCurrentDirectory();
        int returnVal = chooser.showSaveDialog(editor);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            String filename = file.getName();
            if(file.getName().equals("")) return;
            if (!filename.endsWith(".png")){
                file=new File(file.getParent(),filename+".png");
                CAT.debug("File "+file);
            }
            if(file.exists()) {
                Object[] options = { Message.say("Start","mOK"),
                                     Message.say("Start","mAbbr") };

                String msg  = Message.say("BoardEditor", "mDatEx", filename );
                String warn = Message.say("BoardEditor", "mWarnung");
                int r = JOptionPane.showOptionDialog(null, msg, warn,
                                             JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                                             null, options, options[0]);
                if( r != 0 ) {
                    return;
                }
            }

            CAT.debug("Speichere "+ file + " " + file.getName()+" (" + file.getName()+")");
            //File to=new File("kacheln/"+name.getText()+".rra");
            CAT.debug("File opened");
            try{
                FileOutputStream fop=new FileOutputStream(file);
                fop.write(pngBytes);
                fop.flush();
                fop.close();
                CAT.debug("File closed");
            }catch(IOException i){
                System.err.println(Message.say("BoardEditor","mDateiErr") + file + i);
            }
        }
    }

    public void actionPerformed(ActionEvent e){
	if(e.getActionCommand().compareTo("Speichern") == 0){
	    CAT.debug("Speichern geclicked, gete String");
	    String tmp=editor.board.getComputedString();
	    CAT.debug( "String bekommen:\n"+tmp);
	    editor.spfString=tmp;
	    CAT.debug( "Starte FileDialog");

	    //	    new NameDialog(editor,Message.say("BoardEditor","mKachelSave"),true);
            chooser.setFileFilter(rraFilter);
	    chooser.rescanCurrentDirectory();
	    int returnVal = chooser.showSaveDialog(editor);
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
		File file = chooser.getSelectedFile();
		String filename = file.getName();
		if(file.getName().equals("")) return;
		if (!filename.endsWith(".rra")){
		    file=new File(file.getParent(),filename+".rra");
		    CAT.debug("File "+file);
		}
		if(file.exists()) {
		    Object[] options = { Message.say("Start","mOK"),
					 Message.say("Start","mAbbr") };

		    String msg  = Message.say("BoardEditor", "mDatEx", filename );
		    String warn = Message.say("BoardEditor", "mWarnung");
		    int r = JOptionPane.showOptionDialog(null, msg, warn,
						 JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
						 null, options, options[0]);
		    if( r != 0 ) {
			return;
		    }
		}

		CAT.debug("Speichere "+ file + " " + file.getName()+" ("+"kacheln/"+file.getName()+")");
		//File to=new File("kacheln/"+name.getText()+".rra");
		CAT.debug("File opened");
		try{
		    FileOutputStream fop=new FileOutputStream(file);
		    CAT.debug("FileOutputStream opened");
		    PrintWriter pw=new PrintWriter(fop);
		    CAT.debug("PrintWriter opened");
		    pw.println(editor.spfString);
		    CAT.debug("String in File written");
		    pw.close();
		    CAT.debug("File closed");
		}catch(IOException i){
		    System.err.println(Message.say("BoardEditor","mDateiErr") + file + i);
		}
	    }
	}
	else if(e.getActionCommand().compareTo("Beenden") == 0){
	    editor.dispose();
	    System.exit(0);
	}
	else if(e.getActionCommand().compareTo("Clear") == 0){
	    CAT.debug("Clearing field");
	    editor.initSpF();
	    editor.sp.getViewport().setView(editor.sac);
	}
	else if(e.getActionCommand().compareTo("Laden") == 0){
	    chooser.rescanCurrentDirectory();
	    int returnVal = chooser.showOpenDialog(editor);
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
		File file = chooser.getSelectedFile();
		String name = file.getName();

		if( name.equals("") || !file.exists() || !file.canRead() || file.isDirectory() ) {
		    String fehler = Message.say("Start", "mError");
		    String msg    = Message.say("BoardEditor", "eDateiErr");

		    JOptionPane.showMessageDialog(null, msg, fehler, JOptionPane.ERROR_MESSAGE);
		    return;
		}

		String save=editor.spfString;
		try{
		    FileInputStream istream=new FileInputStream( file );
		    //FileInputStream istream=new FileInputStream("kacheln" + File.separator + name);
		    BufferedReader kachReader =new BufferedReader(new InputStreamReader(istream));
		    StringBuffer str=new StringBuffer();
		    String tmp=null;
		    //und lese Spielfeld aus
		    while((tmp=kachReader.readLine())!=null)
			str.append(tmp+"\n");
		    //CAT.debug(str.toString());
		    editor.spfString=str.toString();
		    //Leo's Code
		    editor.board=new EditableBoard(12,12,editor.spfString,null);
		    CAT.debug( "Spielfeld erzeugt");

		    editor.sp.remove(editor.sac);
		    CAT.debug( "sac removed");
		    editor.sac=new BoardPanel(editor.board,editor);
		    CAT.debug( "sac neu erzeugt");
		    editor.sp.getViewport().setView(editor.sac);

		    CAT.debug( "sac added");
		}catch(FormatException ex){
		    System.err.println(Message.say("BoardEditor","eDatNotEx")+ex);
		    editor.spfString=save;
		}catch(FlaggenException ex){
		    System.err.println(Message.say("BoardEditor","eDatNotEx")+ex);
		    editor.spfString=save;
		}catch(IOException ex){
		    System.err.println(Message.say("BoardEditor","eDateiErr")+ex);
		    editor.spfString=save;
		}/*catch(Exception ex){
		   System.err.println(Message.say("BoardEditor","eDatNotEx")+ex);
		   }*/
	    }

	    //new LadenDialog(editor,Message.say("BoardEditor","mKachelSave"),true);

	}
    }

}//ende class ButtonBar
