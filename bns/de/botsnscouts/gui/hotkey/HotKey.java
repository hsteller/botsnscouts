package de.botsnscouts.gui.hotkey;


public class HotKey{

  public static final int UNDEFINED = -1;

  private Integer keyCode;
  private HotKeyAction action;
  private String keyName;

 /* public HotKey (Integer keyCode, HotKeyAction action) {
    this.keyCode = keyCode;
    this.action = action;
  }
*/
   public HotKey (String keyName, Integer keyCode,  HotKeyAction action) {
    this.keyCode =keyCode;
    this.keyName = keyName;
    this.action = action;
    action.setDescription(HotKeyConf.getDescription(keyName));
    action.setOptionalValue(HotKeyConf.getOptinalValue(keyName));

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
      HotKeyConf.setKeyCode(keyName, key);

  }

  public void executeAction() {
    action.execute();
  }

  public HotKeyAction getAction(){
    return action;
  }

  public void setAction(HotKeyAction action){
    boolean save = !action.getOptionalValue().equals(action.getOptionalValue());
    this.action = action;
    if (save)
      HotKeyConf.setOptionalValue(keyName, action.getOptionalValue());
  }

  public String toString() {
    StringBuffer sb = new StringBuffer("Hotkey: code= ").append(keyCode);
    sb.append("   name=").append(keyName);
    sb.append("  action=").append(action.toString());
    sb.append("  optional=").append(action.getOptionalValue());
    return sb.toString();
  }



}