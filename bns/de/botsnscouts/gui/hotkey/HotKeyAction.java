package de.botsnscouts.gui.hotkey;

import javax.swing.JComponent;

import org.apache.log4j.Category;

public abstract class HotKeyAction{

  Category CAT = Category.getInstance(HotKeyAction.class);

  private String description;
  private String optionalValue;
  private JComponent optionalComponent;

  public HotKeyAction(){
  }

  public HotKeyAction(String description,
                      JComponent optionalComponent, String optionalValue){

    this.description = description;
    this.optionalComponent = optionalComponent;
    this.optionalValue = optionalValue;
  }

   public HotKeyAction(JComponent optionalComponent, String optionalValue){
    this (null, optionalComponent, optionalValue);

  }

  public JComponent getOptionalComponent(){
    return optionalComponent;
  }

  public String getOptionalValue(){
    return optionalValue;
  }

  protected void setDescription (String desc){
    if (this.description == null)
        this.description = desc;
    else
       CAT.warn("may not set HotKey description twice!");
  }

  protected void setOptionalValue (String value) {
    optionalValue = value;
  }

  public abstract void execute();

}