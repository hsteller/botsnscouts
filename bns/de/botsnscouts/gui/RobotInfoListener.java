package de.botsnscouts.gui;

import java.util.*;

/**
 * Title:        
 * Description:  
 * Copyright:    Copyright (c) 2001
 * Company:      
 * @author 
 * @version 1.0
 */

public interface RobotInfoListener extends EventListener {
    public void robotClicked(RobotInfoEvent e);

    public void flagClicked(RobotInfoEvent e);

    public void diskClicked(RobotInfoEvent e);
}