/*
 * Created on 06.11.2004
 *
 */
package de.botsnscouts.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.log4j.Category;

import de.botsnscouts.util.Message;
import de.botsnscouts.widgets.ColoredPanel;
import de.botsnscouts.widgets.GreenTheme;
import de.botsnscouts.widgets.OptionPane;
import de.botsnscouts.widgets.PaintPanel;
import de.botsnscouts.widgets.TJButton;
import de.botsnscouts.widgets.TJCheckBox;
import de.botsnscouts.widgets.TJLabel;
import de.botsnscouts.widgets.TJPanel;
import de.botsnscouts.widgets.TJTextField;

/**
 * @author hendrik
 
 */
public class AnimationsSettingEditor extends JFrame {
    
    private static  Category CAT = Category.getInstance(AnimationsSettingEditor.class);
    
    private static final String msgSec = "AnimationSettings";
    
    protected final static TJLabel labelSpeedSlow = new TJLabel(Message.say("AusgabeFrame", "mSlow"), JLabel.CENTER);
    protected final static TJLabel labelSpeedMedium= new TJLabel(Message.say("AusgabeFrame", "mMiddle"), JLabel.CENTER);
    protected final static  TJLabel labelSpeedFast= new TJLabel(Message.say("AusgabeFrame", "mFast"), JLabel.CENTER);        
    
    
    private static JLabel labelMoveOffset = new TJLabel(Message.say(msgSec,"moveOffset"));
    private static JLabel labelMoveDelay= new TJLabel(Message.say(msgSec,"moveDelay")); 
    private static JLabel labelTurnSteps= new TJLabel(Message.say(msgSec,"turnSteps"));
    private static JLabel labelTurnDelayPerStep= new TJLabel(Message.say(msgSec,"turnDelay"));
    private static JLabel labelLaserSoundBeforeAnimations= new TJLabel(Message.say(msgSec,"laserSoundOffset"));
    private static JLabel labelLaserDelayPerStep= new TJLabel(Message.say(msgSec,"laserDelay"));
    private static JLabel labelLaserDelayAfterAnimation= new TJLabel(Message.say(msgSec,"laserPause"));
    private static JLabel labelDelayBetweenActions = new TJLabel(Message.say(msgSec,"actionDelay"));
    private static JLabel labelDelayRevealingCards = new TJLabel(Message.say(msgSec,"cardRevealDelay"));
    private static JLabel labelDelayCardHighlight = new TJLabel(Message.say(msgSec,"cardHighlightDelay"));
    
    private JLabel labelHeading  = new TJLabel(Message.say(msgSec, "heading"), JLabel.CENTER);
    private JButton applyButton   = new TJButton(Message.say(msgSec,"apply"), 16);
    private JButton saveButton    = new TJButton(Message.say(msgSec,"save"),16);
    private JButton cancelButton = new TJButton(Message.say(msgSec, "cancel"),16);

    /** checked=enabled*/
    private JCheckBox toggleMovementAnimation = new TJCheckBox();
    private JLabel labelEnableAnimation = new TJLabel(Message.say(msgSec,"enableAnimations"));
    private  JComponent [] movementWidgets;
    
    
    private static final int CONFIG_COUNT = 3;
    
    /** If you change this array you also have to change #EditorPanel.textFields  so that this Labelarray still
     *  matches the Textfieldarray of EditorPanel (both arrays must have the same order). */
    private static final JLabel [] SETTING_NAMES = new JLabel[]{
                    labelMoveOffset, labelMoveDelay, labelTurnSteps, labelTurnDelayPerStep,
                    labelLaserSoundBeforeAnimations, labelLaserDelayPerStep, labelLaserDelayAfterAnimation,
                    labelDelayBetweenActions, labelDelayRevealingCards, labelDelayCardHighlight
                    };
    private static final int NUM_OF_SETTINGS = SETTING_NAMES.length;
    private EditorPanel [] editors = new EditorPanel[CONFIG_COUNT];

    public AnimationsSettingEditor(AnimationConfig slow, AnimationConfig medium, AnimationConfig fast){
        super(Message.say(msgSec, "heading"));
        this.setContentPane(new PaintPanel(OptionPane.getBackgroundPaint(this),true));
        //this.setBackground(GreenTheme.getBnsBackgroundColor());
       
     //   this.setContentPane(new ColoredPanel(GreenTheme.getBnsBackgroundColor()));
        editors [0] = new EditorPanel(slow, labelSpeedSlow);
        editors [1] = new EditorPanel(medium, labelSpeedMedium);
        editors [2] = new EditorPanel(fast, labelSpeedFast);        
        int numOfMovementWidgets = CONFIG_COUNT*4+4;
        movementWidgets = new JComponent[numOfMovementWidgets];
        int counter = 0;
        movementWidgets[counter++]=labelMoveDelay;
        movementWidgets[counter++]=labelMoveOffset;
        movementWidgets[counter++]=labelTurnDelayPerStep;
        movementWidgets[counter++]=labelTurnSteps;        
        for (int i=0;i<CONFIG_COUNT;i++){
            movementWidgets[counter++] = editors[i].animationDelayMoveRob;
            movementWidgets[counter++] = editors[i].animationDelayTurnRob;
            movementWidgets[counter++] = editors[i].animationOffsetMoveRob;
            movementWidgets[counter++] = editors[i].animationStepsTurnRob;
        }       
        toggleMovementAnimation.setSelected(AnimationConfig.areMovementAnimationsEnabled());
        layoutStuff();
        initListeners();
    }
    
    
    private void layoutStuff(){
       
        GridBagLayout layout = new GridBagLayout();
        JPanel textfieldPanel = new TJPanel();
        textfieldPanel.setLayout(layout);
        JLabel empty = new TJLabel();
       
        GridBagConstraints cons = new GridBagConstraints();
        cons.insets = new Insets(7,5,10,5);
       cons.anchor = GridBagConstraints.WEST;
       
       cons.gridy=0;
       cons.gridx=0;
       textfieldPanel.add(labelEnableAnimation, cons);
       cons.gridx++;
       textfieldPanel.add(toggleMovementAnimation, cons);
       // first row with column headlines
       cons.gridy++;
       cons.gridx=0;
       cons.insets = new Insets(5,5,0,5);
       cons.anchor = GridBagConstraints.CENTER;
       textfieldPanel.add(empty, cons);       
       for (int i=0;i<CONFIG_COUNT;i++){
           cons.gridx=i+1;
           textfieldPanel.add(editors[i].configName, cons);
       }
       // rows with textfields
       cons.anchor = GridBagConstraints.WEST;
       for (int row=0;row<NUM_OF_SETTINGS;row++){
            cons.gridy=row+2;
            cons.gridx=0;
            textfieldPanel.add(SETTING_NAMES[row], cons);
            cons.fill = GridBagConstraints.HORIZONTAL;
            for (int col=0;col<CONFIG_COUNT;col++){
                cons.gridx = col+1;
                textfieldPanel.add((editors[col].getTextFields())[row], cons);
            }
            cons.fill = GridBagConstraints.NONE;
        }
        //this.setContentPane();
        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(labelHeading, BorderLayout.NORTH);
        this.getContentPane().add(textfieldPanel, BorderLayout.CENTER);
       
        JPanel buttonPanel = new TJPanel(){
            public Insets getInsets() {
                Insets ins = super.getInsets();
                ins.left=10;
                ins.top=20;
                ins.right=10;
                ins.bottom=10;
                return ins;
            }
        };
      
       
        buttonPanel.setLayout(new GridLayout(1,3, 5,10));
        buttonPanel.add(saveButton);
        buttonPanel.add(applyButton);
        buttonPanel.add(cancelButton);
        this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        this.pack();
    }
    
    
    
    private void initListeners(){
        applyButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                for (int i=0;i<CONFIG_COUNT;i++){
                    editors[i].applyValues();
                }
            }
        });
        saveButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                for (int i=0;i<CONFIG_COUNT;i++){
                    editors[i].applyValues();
                    editors[i].saveValues();
                }
            }
        });
        cancelButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                AnimationsSettingEditor.this.setVisible(false);
            }
        });
        toggleMovementAnimation.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                  boolean newSetting = toggleMovementAnimation.isSelected();              
                   toggleMovementAnimation.setSelected(newSetting);
                   int max = movementWidgets.length;
                   for (int i=0;i<max;i++){
                       movementWidgets[i].setEnabled(newSetting);
                   }
                   // to be consistent, this has to be done if s.o. clicks "apply"
                   //AnimationConfig.setMovementAnimationsEnabled(true);
               }
            
        });
        this.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e) {
               AnimationsSettingEditor.this.setVisible(false);
            }
        });
        
    }
    
    
    
 
    
    
    
    
    
    
    
    
    
    
    public class EditorPanel {
        
        private AnimationConfig config;
        
        private TJTextField animationOffsetMoveRob;  
        private TJTextField animationDelayMoveRob;   
        private TJTextField animationStepsTurnRob;
        private TJTextField  animationDelayTurnRob;   
        private TJTextField laserDelayBetweenStartOfSoundAndAnimation; 
        private TJTextField laserDelayPerAnimationStep;
        private TJTextField  laserDelayAfterEndOfAnimation; 
        private TJTextField delayBetweenActions;
        private TJTextField delayAfterRevealingCardsForPhase;
        private TJTextField delayAfterCardHighlight;
        
        private TJLabel configName = new TJLabel("unknown");

        /** If you change this array you also have to change #AnimationSettingsEditor.SETTING_NAMES because
         * this JTextField-Array has to match the JLabel-Array SETTING_NAMES.         
         */
        private TJTextField [] textFields;
       
        
        /**       
         * Contains Textfields that will set new values to the underlying animation config.
         * To match the names of the speeds in the speed menu one should use AnimationsSettingEditor.labelSpeedXYZ
         * as the second parameter
         *
         * @param conf The actual settings that should be edited by this panel
         * @param configName The name of these settings; should match the speed menu.
         *                 
         */
        public EditorPanel (AnimationConfig conf, TJLabel configName){
            config = conf;           
            this.configName = configName; 
            animationOffsetMoveRob = new TJTextField(""+conf.getAnimationOffsetMoveRob(), 6);
            animationDelayMoveRob = new TJTextField(""+conf.getAnimationDelayMoveRob(), 6);
            animationStepsTurnRob = new TJTextField(""+conf.getAnimationStepsTurnRob(), 6);
            animationDelayTurnRob = new TJTextField(""+conf.getAnimationDelayTurnRob(), 6);
            laserDelayBetweenStartOfSoundAndAnimation= new TJTextField(""+conf.getLaserDelayBetweenStartOfSoundAndAnimation(), 6);
            laserDelayPerAnimationStep = new TJTextField(""+conf.getLaserDelayPerAnimationStep(), 6);
            laserDelayAfterEndOfAnimation= new TJTextField(""+conf.getLaserDelayAfterEndOfAnimation(), 6);
            delayBetweenActions = new TJTextField(""+conf.getDelayBetweenActions(), 6);
            delayAfterRevealingCardsForPhase = new TJTextField(""+conf.getDelayAfterRevealingCardsForPhase(),6);
            delayAfterCardHighlight = new TJTextField(""+conf.getDelayAfterHighlightingCurrentCard());
            // If you change this array you also have to change #AnimationSettingsEditor.SETTING_NAMES because
            // this JTextField-Array has to match the JLabel-Array SETTING_NAMES.         
            
            textFields =  new TJTextField[] {
                            animationOffsetMoveRob, animationDelayMoveRob, animationStepsTurnRob, animationDelayTurnRob,
                            laserDelayBetweenStartOfSoundAndAnimation, laserDelayPerAnimationStep, laserDelayAfterEndOfAnimation,
                            delayBetweenActions, delayAfterRevealingCardsForPhase, delayAfterCardHighlight
            };
        }
    
        public JTextField [] getTextFields() {
            return textFields;
        }
        
        private int getIntValue(JTextField textField, int minValue, int maxValue, int defaultValue){
            int value = -100;
            try {
               String svalue = textField.getText();
               value = Integer.parseInt(svalue);               
            }
            catch (NumberFormatException nfe ){
               CAT.error(nfe);
               value = defaultValue;
            }
            if (value<minValue){
                value = minValue;
            }
            else if (value > maxValue){
                value = maxValue;
            }
            return value;
           
        }
        

        
        protected void applyValues(){
            int moveRob = getIntValue(animationOffsetMoveRob,0,Integer.MAX_VALUE, 
							 								config.getAnimationOffsetMoveRob());
            config.setAnimationOffsetMoveRob(moveRob);
            
            int moveDelay = getIntValue(animationDelayMoveRob,0, 5000, 
                            									config.getAnimationDelayMoveRob());
            config.setAnimationDelayMoveRob(moveDelay);
            
            int turnSteps = getIntValue(animationStepsTurnRob, 1, 90, 
                            								config.getAnimationStepsTurnRob());
            config.setAnimationStepsTurnRob(turnSteps);
            
            int turnDelay = getIntValue(animationDelayTurnRob,0,2000,
                                                            config.getAnimationDelayTurnRob());
            config.setAnimationDelayTurnRob(turnDelay);
            
            int laserOffset = getIntValue(laserDelayBetweenStartOfSoundAndAnimation,0,5000,
                                                              config.getLaserDelayBetweenStartOfSoundAndAnimation());
            config.setLaserDelayBetweenStartOfSoundAndAnimation(laserOffset);
            
            int laserDelay = getIntValue(laserDelayPerAnimationStep,0, 5000,
                            									config.getLaserDelayPerAnimationStep());
            config.setLaserDelayPerAnimationStep(laserDelay);
            
            int laserWait = getIntValue(laserDelayAfterEndOfAnimation,0,5000, 
                            								config.getLaserDelayAfterEndOfAnimation());       
            config.setLaserDelayAfterEndOfAnimation(laserWait);
        
            int actionDelay = getIntValue(delayBetweenActions, 0, 5000,
                            				config.getDelayBetweenActions());
            config.setDelayBetweenActions(actionDelay);
            
            config.setMovementAnimationsEnabled(toggleMovementAnimation.isSelected());
            
        }
        
        protected void saveValues(){
            applyValues();
            config.saveToProperties();
        }
        
        
        
    }
    
}
