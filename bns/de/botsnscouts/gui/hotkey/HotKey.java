package de.botsnscouts.gui.hotkey;

//?? getter and setter for keycode synchronizen ??
public class HotKey{

  public static final int UNDEFINED = -1;

  private Integer keyCode;
  private HotKeyAction action;
  private String keyName;


   public HotKey (String keyName, Integer keyCode,  HotKeyAction action) {
    this.keyCode =keyCode;
    this.keyName = keyName;
    this.action = action;
    action.setDescription(HotKeyConf.getDescription(keyName));
    action.setOptionalValues(HotKeyConf.getOptinalValues(keyName));

  }

  public HotKey (String keyName, HotKeyAction action) {
    this(keyName, HotKeyConf.getKeyCode(keyName),action);

  }


  public Integer getKeyCodeI(){
    return keyCode;
  }

  public int getKeyCode(){
    if ( keyCode != null)
      return keyCode.intValue();
    else
      return UNDEFINED;
  }


  protected void setKeyCode(Integer key) throws KeyReserved{
      if (keyCode.equals(key))
        return;
      if (key != null && HotKeyConf.isReserved(key.intValue()))
        throw new KeyReserved();
      this.keyCode = key;
      HotKeyConf.setHotKey(this);

  }

  public void executeAction() {
    action.execute();
  }

  public HotKeyAction getAction(){
    return action;
  }

  protected void setAction(HotKeyAction action){
    String [] values = action.getOptionalValues();
    this.action = action;
   // HotKeyConf.setOptionalValues(keyName, action.getOptionalValues());
    HotKeyConf.setHotKey(this);
  }

  public String getName() {
    return keyName;
  }

  public String getKeyText (){
    //hack! strange KeyEvents (no text for Enter) => crappy HotKeys :-(
    // ?? setting keytext at HotKey creation better??
    if (keyName.equals(HotKeyConf.HOTKEY_SHOW_CHATLINE))
      return HotKeyConf.SHOW_CHATLINE_TEXT;
    else
      return java.awt.event.KeyEvent.getKeyText(keyCode.intValue());

  }

  public String toString() {
    StringBuffer sb = new StringBuffer("Hotkey: code= ").append(keyCode);
    sb.append("   name=").append(keyName);
    sb.append("  action=").append(action.toString());
    sb.append("  optional=");
    String [] opts = action.getOptionalValues();
    if (opts != null)
      for (int i=0;i<opts.length;i++)
        sb.append(opts[i]).append(',');
    return sb.toString();
  }

  public static String dumpEvent(java.awt.event.KeyEvent e) {
      StringBuffer sb = new StringBuffer(200);
      sb.append("KEYEVENT\n keychar: "+e.getKeyChar()+"\nkeycode: "+e.getKeyCode()
                +"\nnumValue: "+Character.getNumericValue(e.getKeyChar()));

      int mods = e.getModifiers();
      String ms = e.getKeyModifiersText(mods);
      sb.append("\nmods="+mods+"\tmodString="+ms);
      sb.append("\nkeyText="+e.getKeyText(e.getKeyCode()));
      sb.append("\nID="+e.getID());
      sb.append("\nparamString="+e.paramString());
      sb.append("\nconsumed?"+e.isConsumed());
      sb.append("\nactionKey?"+e.isActionKey());
      javax.swing.KeyStroke stroke = javax.swing.KeyStroke.getKeyStrokeForEvent(e);
      sb.append("\nstroke stuff: ");
      sb.append("\n\t"+stroke.getKeyChar());
      sb.append("\n\t"+stroke.getKeyCode());
      sb.append("\n\tchar#:"+Character.getNumericValue(stroke.getKeyChar()));
      sb.append("\n\tSTROKE="+stroke.toString());
      return sb.toString();

   }

}