/*
 * Created on 02.11.2004
 */
package de.botsnscouts.gui;

/**
 * @author hendrik

 */
public class AnimationConfig {

    private static AnimationConfig singletonConfig=null;
    
    public static AnimationConfig getGlobalAnimationConfig(){
        if (singletonConfig == null)
            singletonConfig = new AnimationConfig();
        return singletonConfig;
    }
    
    public static void setGlobalAnimationConfig(AnimationConfig ac){
        singletonConfig = ac;
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
    private int laserDelayBetweenStartOfSoundAndAnimation=200; // delay/4 // TODO default value
    
    /** Number of ms to wait between drawing of two single steps during a laser shot animation*/
    private int laserDelayPerAnimationStep = 10;
    
    /** Number of ms to wait after the shot animation was drawn (to let the sound catch up/ to create a delay between
     *  this shot and another following animation.
     */
    private int laserDelayAfterEndOfAnimation = 1000; // deleay/2 TODO default value
    
    
    
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
    
    
    
    
    
}
