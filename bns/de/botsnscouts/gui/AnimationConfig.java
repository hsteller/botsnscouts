/*
 *******************************************************************
 *        Bots 'n' Scouts - Multi-Player networked Java game       *
 *                                                                 *
 * Copyright (C) 2005 scouties.                                *
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


/*
 * Created on 02.11.2004
 */
package de.botsnscouts.gui;

import java.util.StringTokenizer;

import org.apache.log4j.Category;

import de.botsnscouts.util.Conf;

/**
 * @author hendrik

 */
public class AnimationConfig {

    private static Category CAT = Category.getInstance(AnimationConfig.class);
    
    public static final String ANIMATION_SLOW="animationSettingsSlow";
    public static final String ANIMATION_MEDIUM="animationsSettingsMedium";
    public static final String ANIMATION_FAST="animationSettingsFast";
    
    private static  boolean areMovementAnimationsEnabled=true;
    private static final String PROP_NAME_ANIMATIONSENABLED = "areMovementAnimationsEnabled";
    private String configName;
    
    public AnimationConfig(String name, boolean tryToLoadFromProperties){
        configName = name;
        if (tryToLoadFromProperties) {
            loadFromProperties();
        }
    }
    
    /**
     * Number of pixels a robot will be moved in a single animation step. 
     * Has to be between 1 and scaledFieldSize 
     * => Number of steps a one-field-move is drawn = scaledFieldSize/MOVE_ROB_ANIMATION_OFFSET
     */
    private int animationOffsetMoveRob = 1;
    /**
     * Amount of time (in ms) we wait after a single animation step of (pixel-)length MOVE_ROB_ANIMATION_OFFSET.
     */
    private int animationDelayMoveRob = 1;
    /**
     * Number of steps a 90 degree turn is animated with
     */
    private int animationStepsTurnRob = 45;
   
    /** Number of ms that the animation "sleeps" in each animation step*/
    private int animationDelayTurnRob = 5;

    /** Number of ms we sleep between starting to play the laser sound and starting to animate the shot*/
    private int laserDelayBetweenStartOfSoundAndAnimation=200; 
    
    /** Number of ms to wait between drawing of two single steps during a laser shot animation*/
    private int laserDelayPerAnimationStep = 10;
    
    /** Number of ms to wait after the shot animation was drawn (to let the sound catch up/ to create a delay between
     *  this shot and another following animation.
     */
    private int laserDelayAfterEndOfAnimation = 1000; 
    
    /** Will be used everytime something has happend in BoardView*/
    private int delayBetweenActions = 1000;
    
    /** How long to wait at the start of a new evaluation phase */
    private int delayAfterRevealingCards = 2500;
    
    /** We will wait this long before starting the animation after a new card is evaluated*/ 
    private int delayAfterCardHighlight = 1000;
    
    
    
    public void setAnimationOffsetMoveRob(int animationOffsetMoveRob) {
        this.animationOffsetMoveRob = animationOffsetMoveRob;
    }

    public int getAnimationOffsetMoveRob() {
        return animationOffsetMoveRob;
    }

    public void setAnimationDelayMoveRob(int animationDelayMoveRob) {
        this.animationDelayMoveRob = animationDelayMoveRob;
    }

    public int getAnimationDelayMoveRob() {
        return animationDelayMoveRob;
    }

    public void setAnimationStepsTurnRob(int animationStepsTurnRob) {
        this.animationStepsTurnRob = animationStepsTurnRob;
    }

    public int getAnimationStepsTurnRob() {
        return animationStepsTurnRob;
    }

    public void setAnimationDelayTurnRob(int animationDelayTurnRob) {
        this.animationDelayTurnRob = animationDelayTurnRob;
    }

    public int getAnimationDelayTurnRob() {
        return animationDelayTurnRob;
    }

    public void setLaserDelayBetweenStartOfSoundAndAnimation(int laserDelayBetweenStartOfSoundAndAnimation) {
        this.laserDelayBetweenStartOfSoundAndAnimation = laserDelayBetweenStartOfSoundAndAnimation;
    }

    public int getLaserDelayBetweenStartOfSoundAndAnimation() {
        return laserDelayBetweenStartOfSoundAndAnimation;
    }

    public void setLaserDelayPerAnimationStep(int laserDelayPerAnimationStep) {
        this.laserDelayPerAnimationStep = laserDelayPerAnimationStep;
    }

    public int getLaserDelayPerAnimationStep() {
        return laserDelayPerAnimationStep;
    }

    public void setLaserDelayAfterEndOfAnimation(int laserDelayAfterEndOfAnimation) {
        this.laserDelayAfterEndOfAnimation = laserDelayAfterEndOfAnimation;
    }

    public int getLaserDelayAfterEndOfAnimation() {
        return laserDelayAfterEndOfAnimation;
    }
    
    public int getDelayAfterRevealingCardsForPhase(){
        return delayAfterRevealingCards;
    }
    
    public int getDelayAfterHighlightingCurrentCard(){
        return delayAfterCardHighlight;
    }
    
    public void setDelayAfterRevealingCardsForPhase(int delay){
        delayAfterRevealingCards = delay;
    }
    
    public void setDelayAfterHighlightingCurrentCard(int delay){
        delayAfterCardHighlight = delay;
    }
    
    public String getConfigName(){
        return configName;
    }
    
    public void saveToProperties() {
        StringBuffer saveString = new StringBuffer(30);
        saveString.append(animationOffsetMoveRob).append('+');
        saveString.append(animationDelayMoveRob).append('+');
        saveString.append(animationStepsTurnRob).append('+');
        saveString.append(animationDelayTurnRob).append('+');
        saveString.append(laserDelayBetweenStartOfSoundAndAnimation).append('+');
        saveString.append(laserDelayAfterEndOfAnimation).append('+');
        saveString.append(laserDelayPerAnimationStep).append('+');
        saveString.append(delayBetweenActions).append('+');
        saveString.append(delayAfterRevealingCards).append('+');
        saveString.append(delayAfterCardHighlight);
        String s = saveString.toString();
        CAT.debug("saving for "+configName+":  "+s);
        Conf.setProperty(configName, s);
        Conf.setProperty(PROP_NAME_ANIMATIONSENABLED, ""+areMovementAnimationsEnabled);
        Conf.saveProperties();
    }
    
    
    public void loadFromProperties(){
        String animationsEnabled = Conf.getProperty(PROP_NAME_ANIMATIONSENABLED);
        areMovementAnimationsEnabled = animationsEnabled != null && animationsEnabled.equals("true");
                           
        String saveString = Conf.getProperty(configName);
        CAT.debug("loading for "+configName+":  "+saveString);
        if (saveString != null && saveString.length()>0){
            	int [] values = new int [10]; 
            	try {
	                StringTokenizer st = new StringTokenizer(saveString,"+");               
	                values[0] = Integer.parseInt(st.nextToken());
	                values[1] = Integer.parseInt(st.nextToken());
	                values[2] = Integer.parseInt(st.nextToken());
	                values[3] = Integer.parseInt(st.nextToken());
	                values[4] = Integer.parseInt(st.nextToken());
	                values[5] = Integer.parseInt(st.nextToken());
	                values[6] = Integer.parseInt(st.nextToken());
	                values[7] = Integer.parseInt(st.nextToken());
	                values[8] =  Integer.parseInt(st.nextToken());
	                values[9] =  Integer.parseInt(st.nextToken());
            	}
            	catch (Exception e){
            	    CAT.error("Exception loading the animation settings for: "+configName);
            	    CAT.error("\tusing default values");
            	    CAT.error(e.getMessage(), e);
            	    return;
            	}
            	animationOffsetMoveRob=values[0];
                animationDelayMoveRob=values[1];
                animationStepsTurnRob=values[2];
                animationDelayTurnRob=values[3];
                laserDelayBetweenStartOfSoundAndAnimation=values[4];
                laserDelayAfterEndOfAnimation=values[5];
                laserDelayPerAnimationStep=values[6];          
                delayBetweenActions = values[7];
                delayAfterRevealingCards = values[8];
                delayAfterCardHighlight = values[9];
        }
        
    }

    /**
     * @param delayBetweenActions The delayBetweenActions to set.
     */
    public void setDelayBetweenActions(int delayBetweenActions) {
        this.delayBetweenActions = delayBetweenActions;
    }

    /**
     * @return Returns the delayBetweenActions.
     */
    public int getDelayBetweenActions() {
        return delayBetweenActions;
    }
    // TODO shouldn't be static..maybe create a meta-object that contains the config settings
    // for the three different speed levels and this one boolean
    public  static void setMovementAnimationsEnabled(boolean isEnabled){
        areMovementAnimationsEnabled = isEnabled;
    }
    
    public static boolean areMovementAnimationsEnabled(){
        return areMovementAnimationsEnabled;
    }
    
    
}

