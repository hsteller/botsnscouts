package de.spline.rr;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.image.*;
import java.awt.geom.*;
import javax.swing.border.*;
import java.net.*;

public class StartSpielfeldEditor extends JPanel implements  ActionListener, KachelClickListener,ListSelectionListener{

    public static final int MODE_FLAGGE_SETZEN=0;
    public static final int MODE_FLAGGE_ENTFERNEN=1;
    public static final int MODE_FLAGGE_VERSCHIEBEN=2;

    public static final int MODE_KACHEL_SETZEN=3;
    public static final int MODE_KACHEL_ENTFERNEN=4;
    public static final int MODE_KACHEL_DREHEN=5;

    public static final int CURSOR_DEFAULT = 0,
	CURSOR_FLAGGE_SETZBAR = 1,
	CURSOR_FLAGGE_NICHT_SETZBAR = 2,
	CURSOR_FLAGGE_VERSCHIEBEN = 3,
	CURSOR_FLAGGE_NICHT_OK = 4,
	CURSOR_FLAGGE_LOESCHEN = 5;


    StartSpielfeldSpf spf;
    Start parent;
    Paint paint;

    JToggleButton modeFlaggeSetzen;
    JToggleButton modeFlaggeEntfernen;
    JToggleButton modeFlaggeVerschieben;

    JToggleButton modeKachelSetzen;
    JToggleButton modeKachelEntfernen;
    JToggleButton modeKachelDrehen;

    ButtonGroup kachelnGroup;

    JButton ok;
    JButton zurueck;

    JPanel flaggenButtons;
    JPanel fuerSpf;
    JScrollPane fuerfuerSpf;

    JScrollPane fuerKachelListe;
    JList kachelListe;
    JPanel okZur;

    int currentMode;
    int currentKachel;

    boolean kannFlaggeSetzen;
    boolean flaggeGewaehlt;
    boolean istFlagge;
    String istFlaggeGut;
    int   flaggeX,flaggeY;

    Image[] images;
    KachelInfo[] kachelInfos;
    Cursor[] cursors;

    public StartSpielfeldEditor(Start par,StartSpielfeldSpf spf){
	parent=par;
	this.spf=spf;
	currentMode=3;
	flaggeGewaehlt=false;
	initCursors();

	images=ImageMan.getImages(ImageMan.STARTKNOEPFE);

	kachelInfos=parent.fassade.getKachelInfos();

	BorderLayout lay=new BorderLayout();

//	lay.setHgap(50);
//	lay.setVgap(50);

//	setBorder(new EmptyBorder(50,50,50,50));

	setLayout(lay);
	setOpaque(false);

// 	modeFlaggeSetzen=new JToggleButton("Flagge setzen");
// 	modeFlaggeEntfernen=new JToggleButton("Flagge entfernen");
// 	modeFlaggeVerschieben=new JToggleButton("Flagge verschieben");
	modeFlaggeSetzen=new JToggleButton(new ImageIcon(images[0]));
	modeFlaggeEntfernen=new JToggleButton(new ImageIcon(images[2]));
	modeFlaggeVerschieben=new JToggleButton(new ImageIcon(images[1]));
	modeFlaggeSetzen.setOpaque(false);
	modeFlaggeEntfernen.setOpaque(false);
	modeFlaggeVerschieben.setOpaque(false);
	modeFlaggeSetzen.setToolTipText(Message.say("Start","mTTFlaggeSetzen"));
	modeFlaggeEntfernen.setToolTipText(Message.say("Start","mTTFlaggeEntfernen"));
	modeFlaggeVerschieben.setToolTipText(Message.say("Start","mTTFlaggeVerschieben"));
	modeFlaggeSetzen.setActionCommand("flSetzen");
	modeFlaggeEntfernen.setActionCommand("flEntfernen");
	modeFlaggeVerschieben.setActionCommand("flVerschieben");
	modeFlaggeSetzen.addActionListener(this);
	modeFlaggeEntfernen.addActionListener(this);
	modeFlaggeVerschieben.addActionListener(this);

// 	modeKachelSetzen=new JToggleButton("Kachel setzen");
// 	modeKachelEntfernen=new JToggleButton("Kachel entfernen");
// 	modeKachelDrehen=new JToggleButton("Kachel drehen");
	modeKachelSetzen=new JToggleButton(new ImageIcon(images[5]));
	modeKachelEntfernen=new JToggleButton(new ImageIcon(images[4]));
	modeKachelDrehen=new JToggleButton(new ImageIcon(images[3]));
	modeKachelSetzen.setOpaque(false);
	modeKachelEntfernen.setOpaque(false);
	modeKachelDrehen.setOpaque(false);
	modeKachelSetzen.setToolTipText(Message.say("Start","mTTKachelSetzen"));
	modeKachelEntfernen.setToolTipText(Message.say("Start","mTTKachelEntfernen"));
	modeKachelDrehen.setToolTipText(Message.say("Start","mTTKachelDrehen"));
	modeKachelSetzen.setActionCommand("kachSetzen");
	modeKachelEntfernen.setActionCommand("kachEntfernen");
	modeKachelDrehen.setActionCommand("kahcDrehen");
	modeKachelSetzen.addActionListener(this);
	modeKachelEntfernen.addActionListener(this);
	modeKachelDrehen.addActionListener(this);

	kachelnGroup=new ButtonGroup();
	kachelnGroup.add(modeKachelSetzen);
	kachelnGroup.add(modeKachelEntfernen);
	kachelnGroup.add(modeKachelDrehen);
	kachelnGroup.add(modeFlaggeSetzen);
	kachelnGroup.add(modeFlaggeEntfernen);
	kachelnGroup.add(modeFlaggeVerschieben);
	kachelnGroup.setSelected(modeKachelSetzen.getModel(),true);

	flaggenButtons=new JPanel();
	flaggenButtons.setLayout(new FlowLayout());
	flaggenButtons.setOpaque(false);
	flaggenButtons.add(modeFlaggeSetzen);
	flaggenButtons.add(modeFlaggeEntfernen);
	flaggenButtons.add(modeFlaggeVerschieben);
	flaggenButtons.add(Box.createRigidArea(new Dimension(100,20)));
	flaggenButtons.add(modeKachelSetzen);
	flaggenButtons.add(modeKachelEntfernen);
	flaggenButtons.add(modeKachelDrehen);
	add(BorderLayout.NORTH,flaggenButtons);

	kachelListe=new JList(kachelInfos);
//	kachelListe.setCellRenderer(new ListItemComponent(kachelInfos));
	kachelListe.setCellRenderer(new ThumbsCellRenderer());
	kachelListe.setOpaque(false);
	fuerKachelListe=new JScrollPane();
	fuerKachelListe.getViewport().setView(kachelListe);
//	add(BorderLayout.EAST,kachelListe);
	add(BorderLayout.EAST,fuerKachelListe);
	kachelListe.addListSelectionListener(this);
// 	kachelnButtons=new JPanel();
// 	kachelnButtons.setLayout(new GridLayout(1,2));
// 	kachelnButtons.add(modeKachelSetzen);
// 	kachelnButtons.add(modeKachelEntfernen);
// 	add(kachelnButtons);

	ok=new TransparentButton(Message.say("Start","mOK"));
	zurueck=new TransparentButton(Message.say("Start","mAbbr"));
	ok.setActionCommand("ok");
	ok.addActionListener(this);
	zurueck.addActionListener(this);
	zurueck.setActionCommand("abbrechen");

	okZur=new JPanel();
	okZur.setLayout(new FlowLayout());
	okZur.setOpaque(false);
	okZur.add(ok);
	okZur.add(zurueck);
	add(BorderLayout.SOUTH,okZur);
	
	fuerSpf=new JPanel();
	fuerSpf.setOpaque(false);
	fuerSpf.add(spf);

	fuerfuerSpf=new JScrollPane();
	fuerfuerSpf.setOpaque(false);
	fuerfuerSpf.getViewport().setView(fuerSpf);
	add(BorderLayout.CENTER,fuerfuerSpf);

	spf.addKachelClickListener(this);

	URL url = getClass().getResource(Message.say("Start","mBG"));
	ImageIcon icon = new ImageIcon( url );
	BufferedImage bgimg = new BufferedImage( icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB );
	Graphics g = bgimg.getGraphics();
	icon.paintIcon(this, g, 0,0);
	g.dispose();
	Rectangle2D anchor = new Rectangle2D.Float(0f,0f, icon.getIconWidth(), icon.getIconHeight());
	paint = new TexturePaint( bgimg, anchor );
    }

    public void actionPerformed(ActionEvent e){ 
	if(e.getActionCommand().equals("ok")){
	    try{
		parent.fassade.checkSpielfeld();
		if(parent.startSpielfeld==null){
		    parent.startSpielfeld=new StartSpielfeld(parent);
		}
		spf.removeKachelClickListener();
		parent.startSpielfeld.pnl.add(spf);
		parent.current=parent.startSpielfeld;
		parent.setContentPane(parent.current);
		parent.show();
	    }catch(OneFlagException ex){
		JOptionPane.showMessageDialog(this,Message.say("Start","mZweiFlaggen"),Message.say("Start","mError"),JOptionPane.ERROR_MESSAGE);

	    }catch(NichtZusSpfException ex){
		JOptionPane.showMessageDialog(this,Message.say("Start","mNichtZus"),Message.say("Start","mError"),JOptionPane.ERROR_MESSAGE);

	    }

	}else if(e.getActionCommand().equals("abbrechen")){
	    if(parent.startSpielfeld==null){
		parent.startSpielfeld=new StartSpielfeld(parent);
	    }
	    parent.fassade.restorKachelRaster();
	    spf.removeKachelClickListener();
	    parent.startSpielfeld.pnl.add(spf);
	    parent.current=parent.startSpielfeld;
	    parent.setContentPane(parent.current);
	    spf.rasterChanged();

	    parent.startSpielfeld.buttons.save.getModel().setRollover(false);
	    parent.startSpielfeld.buttons.bearbeiten.getModel().setRollover(false);
	    parent.startSpielfeld.unten.ok.getModel().setRollover(false);
	    parent.startSpielfeld.unten.zurueck.getModel().setRollover(false);
	    
	    parent.show();
	}else if(e.getActionCommand().equals("flSetzen")){
	    currentMode=MODE_FLAGGE_SETZEN;
	}else if(e.getActionCommand().equals("flEntfernen")){
	    currentMode=MODE_FLAGGE_ENTFERNEN;
	}else if(e.getActionCommand().equals("flVerschieben")){
	    currentMode=MODE_FLAGGE_VERSCHIEBEN;
	    flaggeGewaehlt=false;
	}else if(e.getActionCommand().equals("kachSetzen")){
	    currentMode=MODE_KACHEL_SETZEN;
	}else if(e.getActionCommand().equals("kachEntfernen")){
	    currentMode=MODE_KACHEL_ENTFERNEN;
	}else if(e.getActionCommand().equals("kahcDrehen")){
	    currentMode=MODE_KACHEL_DREHEN;
	}
    }

    public void paint(Graphics g) {
	Graphics2D g2d = (Graphics2D) g;
	Dimension d = getSize();
	g2d.setPaint( paint );
	g2d.fillRect(0,0, d.width, d.height);
	paintChildren(g);
    }

    public void kachelClick(int rx,int ry,int fx,int fy){
	switch(currentMode){
	case  MODE_FLAGGE_SETZEN:{
	    try{
		if(kannFlaggeSetzen){
		      if(!istFlaggeGut.equals("")){
			  int ret=JOptionPane.showConfirmDialog(this,istFlaggeGut+Message.say("Start","mWirklich"),Message.say("Start","mKachelSetzenTitel"),JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE,new ImageIcon(images[0]));
			if(ret!=JOptionPane.YES_OPTION){
			    return;
			}
		    }
		    parent.fassade.addFlagge(rx*12+fx,ry*12+fy);
		    spf.rasterChanged();
		}else{
		    //System.err.println("You cannot place a flag here!");
		}
	    }catch (FlaggenException ex){
		System.err.println("You cannot place a flag here!");
	    }

	    break;
	}
	case  MODE_FLAGGE_ENTFERNEN:{
	    parent.fassade.delFlagge(rx*12+fx,ry*12+fy);
	    spf.rasterChanged();
	    break;
	}
	case  MODE_FLAGGE_VERSCHIEBEN:{
	    if((!flaggeGewaehlt)&&istFlagge){
		flaggeX=rx*12+fx;
		flaggeY=ry*12+fy;
		flaggeGewaehlt=true;
	    }else if(flaggeGewaehlt&&kannFlaggeSetzen){
		if(!istFlaggeGut.equals("")){
		    int ret=JOptionPane.showConfirmDialog(this,istFlaggeGut+Message.say("Start","mWirklich"),Message.say("Start","mKachelSetzenTitel"),JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE,new ImageIcon(images[0]));
		    if(ret!=JOptionPane.YES_OPTION){
			return;
		    }
		}
		try{
		    parent.fassade.moveFlagge(flaggeX,flaggeY,rx*12+fx,ry*12+fy);
		}catch (FlaggenException ex){
		    System.err.println("You cannot place a flag here!");
		}
		flaggeGewaehlt=false;
		spf.rasterChanged();
	    }
	    break;
	}
	case  MODE_KACHEL_SETZEN:{
	    if(parent.fassade.sindFlaggen(rx,ry)){
		int ret=JOptionPane.showConfirmDialog(this,Message.say("Start","mKachelSetzen"),Message.say("Start","mKachelSetzenTitel"),JOptionPane.YES_NO_OPTION,JOptionPane.DEFAULT_OPTION,new ImageIcon(images[4]));
		//fragen,ob kachel mit den flaggen gelöscht werden soll
		if(ret!=JOptionPane.YES_OPTION){
		    return;
		}
	    }
	    try{
		parent.fassade.delKachel(rx,ry);
		parent.fassade.setKachel(rx,ry,kachelInfos[currentKachel].toString());
	    }catch (FlaggenVorhandenException ex){
		System.err.println("You cannot place here a field!");
	    }
	    spf.rasterChanged();
	    break;
	}
	case  MODE_KACHEL_ENTFERNEN:{
	    if(parent.fassade.sindFlaggen(rx,ry)){
		//fragen,ob kachel mit den flaggen gelöscht werden soll
		int ret=JOptionPane.showConfirmDialog(this,Message.say("Start","mKachelLoeschen"),Message.say("Start","mKachelSetzenTitel"),JOptionPane.YES_NO_OPTION,JOptionPane.DEFAULT_OPTION,new ImageIcon(images[4]));
		if(ret==JOptionPane.YES_OPTION){
		    parent.fassade.delKachel(rx,ry);
		    spf.rasterChanged();
		}
	    }else{
		parent.fassade.delKachel(rx,ry);
		spf.rasterChanged();
	    }

	    break;
	}
	case  MODE_KACHEL_DREHEN:{
	    parent.fassade.rotKachel(rx,ry);
	    spf.rasterChanged();
	    
	    break;
	}
	}
    }

    public void kachelMouseMove(int rx,int ry,int fx,int fy){
	switch(currentMode){
	case  MODE_FLAGGE_SETZEN:{
	    kannFlaggeSetzen=parent.fassade.checkFlaggePos(rx*12+fx,ry*12+fy); 
	    if (kannFlaggeSetzen){
		istFlaggeGut=parent.fassade.getFlaggeKomment(rx*12+fx,ry*12+fy);
		if (istFlaggeGut.equals("")){
		    setCursor(cursors[CURSOR_FLAGGE_SETZBAR]);
		}else{
		    setCursor(cursors[CURSOR_FLAGGE_NICHT_OK]);
		}
	    }else{
		setCursor(cursors[CURSOR_FLAGGE_NICHT_SETZBAR]);
	    }
	    break;
	}
	case  MODE_FLAGGE_ENTFERNEN:{
	    setCursor(cursors[CURSOR_FLAGGE_LOESCHEN]);
	    break;
	}
	case  MODE_FLAGGE_VERSCHIEBEN:{
	    if(!flaggeGewaehlt){
		istFlagge=parent.fassade.istFlagge(rx*12+fx,ry*12+fy);
		if (istFlagge){
		    setCursor(cursors[CURSOR_FLAGGE_VERSCHIEBEN]);
		}else{
		    setCursor(cursors[CURSOR_DEFAULT]);
		}
	    }else{
		kannFlaggeSetzen=parent.fassade.checkFlaggeMovePos(rx*12+fx,ry*12+fy);
		if (kannFlaggeSetzen){
		    istFlaggeGut=parent.fassade.getFlaggeKomment(rx*12+fx,ry*12+fy);
		    if (istFlaggeGut.equals("")){
			setCursor(cursors[CURSOR_FLAGGE_SETZBAR]);
		    }else{
			setCursor(cursors[CURSOR_FLAGGE_NICHT_OK]);
		    }
		}else{
		    setCursor(cursors[CURSOR_FLAGGE_NICHT_SETZBAR]);
		}
	    }
	    break;
	}
	case  MODE_KACHEL_SETZEN:{
	    break;
	}
	case  MODE_KACHEL_ENTFERNEN:{

	    break;
	}
	case  MODE_KACHEL_DREHEN:{

	    break;
	}
	}
    }

    public void kachelMouseLeave(){
	//Global.debug(this,"Mouse left");
	setCursor(cursors[CURSOR_DEFAULT]);
    }

    public void valueChanged(ListSelectionEvent e) {
	//Global.debug(this,"selected kachel "+kachelListe.getSelectedIndex());
	currentKachel=kachelListe.getSelectedIndex();
	kachelnGroup.setSelected(modeKachelSetzen.getModel(),true);
	currentMode=MODE_KACHEL_SETZEN;
	setCursor(cursors[CURSOR_DEFAULT]);
	flaggenButtons.repaint();
	//repaint();
    }

    private void initCursors(){
	Image[] cursorImages=CursorMan.getImages(CursorMan.CURSOR);
	cursors=new Cursor[cursorImages.length+1];
	Toolkit tk = Toolkit.getDefaultToolkit();
	for(int i=1;i<cursors.length-1;i++){
	    cursors[i]=tk.createCustomCursor(cursorImages[i-1],new Point(5,20),"cursor"+i);

	}
	int last=cursors.length-1;
	cursors[last]=tk.createCustomCursor(cursorImages[last-1],new Point(12,12),"cursor"+last);
    }

}






class ThumbsCellRenderer extends JPanel implements ListCellRenderer {

    JLabel image = new JLabel();
    JLabel text = new JLabel();
    Border selectedBorder = new MatteBorder( 3,3,3,3,Color.green.darker() );
    Border normalBorder = new EmptyBorder( 3,3,3,3 );

    public ThumbsCellRenderer () {
	setOpaque( false );
	image.setOpaque( false );
	text.setOpaque( false );
	text.setBackground(Color.black);
	text.setForeground(Color.green.darker());
	setLayout( new BorderLayout() );
	add( image, BorderLayout.CENTER );
	add( text, BorderLayout.SOUTH );
	//setBackground( Color.black );
	//setForeground( Color.green );
    }
    
    public Component getListCellRendererComponent(
						  JList list,
						  Object value,            // value to display
						  int index,               // cell index
						  boolean isSelected,      // is the cell selected
						  boolean cellHasFocus)    // the list and the cell have the focus
    {	
	String name=value.toString().substring(0,value.toString().indexOf(".rra"));
	setOpaque(false);
	text.setText(name);
	image.setIcon(new ImageIcon(((KachelInfo)value).getImage()));
	if (isSelected) {
	    text.setText(name.toUpperCase());
	    image.setBorder( selectedBorder );
	}
	else {
// 	    setBackground(list.getBackground());
// 	    setForeground(list.getForeground());
	    text.setText(name);
	    image.setBorder( normalBorder );
	}
	setEnabled(list.isEnabled());
	setFont(list.getFont());
	return this;
    }

}


