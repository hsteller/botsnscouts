package de.botsnscouts.gui.hotkey;

import javax.swing.AbstractAction;
import javax.swing.JComponent;

import org.apache.log4j.Category;

public abstract class HotKeyAction extends AbstractAction{

  Category CAT = Category.getInstance(HotKeyAction.class);

  private String description;
  private String optionalValues [];
  private JComponent optionalComponents [];

  public HotKeyAction(){
  }

  public HotKeyAction(String description,
                      JComponent [] optionalComponent, String [] optionalValues){

    this.description = description;
    this.optionalComponents = optionalComponent;
    this.optionalValues = optionalValues;
  }

   public HotKeyAction(JComponent [] optionalComponent, String [] optionalValues){
    this ( null, optionalComponent, optionalValues);

  }

  public JComponent[] getOptionalComponents(){
    return optionalComponents;
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



}