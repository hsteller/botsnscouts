package de.botsnscouts.gui.hotkey;

import javax.swing.JComponent;

import org.apache.log4j.Category;

public abstract class HotKeyAction{

  Category CAT = Category.getInstance(HotKeyAction.class);

  private String description;
  private String optionalValues [];
  private JComponent optionalComponent;

  public HotKeyAction(){
  }

  public HotKeyAction(String description,
                      JComponent optionalComponent, String [] optionalValues){

    this.description = description;
    this.optionalComponent = optionalComponent;
    this.optionalValues = optionalValues;
  }

   public HotKeyAction(JComponent optionalComponent, String [] optionalValues){
    this ( null, optionalComponent, optionalValues);

  }

  public JComponent getOptionalComponent(){
    return optionalComponent;
  }

  public String [] getOptionalValues(){
    return optionalValues;
  }

  public String getDescription(){
    return description;
  }

  protected void setDescription (String desc){
    if (this.description == null)
        this.description = desc;
    else
       CAT.warn("may not set HotKey description twice!");
  }

  protected void setOptionalValues (String [] values) {
    optionalValues = values;
  }

  public abstract void execute();

}