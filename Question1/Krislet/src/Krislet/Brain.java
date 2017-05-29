package Krislet;

//
//	File:			Brain.java
//	Author:		Krzysztof Langner
//	Date:			1997/04/28
//
//    Modified by:	Paul Marlow

//    Modified by:      Edgar Acosta
//    Date:             March 4, 2008

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.*;

class Brain extends Thread implements SensorInput
{
    //---------------------------------------------------------------------------
    // This constructor:
    // - stores connection to krislet
    // - starts thread for this object
    public Brain(SendCommand krislet, 
		 String team, 
		 char side, 
		 int number, 
		 String playMode,
                 String strAgentFunction)
    {
	m_timeOver = false;
	m_krislet = krislet;
	m_memory = new Memory();
	//m_team = team;
	m_side = side;
	// m_number = number;
	m_playMode = playMode;
        m_strAgentFunction = strAgentFunction;
	start();
    }

    //---------------------------------------------------------------------------
    // This is main brain function used to make decision
    // In each cycle we decide which command to issue based on
    // current situation. the rules are:
    //
    //	1. If you don't know where is ball then turn right and wait for new info
    //
    //	2. If ball is too far to kick it then
    //		2.1. If we are directed towards the ball then go to the ball
    //		2.2. else turn to the ball
    //
    //	3. If we dont know where is opponent goal then turn wait 
    //				and wait for new info
    //
    //	4. Kick ball
    //
    //	To ensure that we don't send commands to often after each cycle
    //	we waits one simulator steps. (This of course should be done better)

    // ***************  Improvements ******************
    // Allways know where the goal is.
    // Move to a place on my side on a kick_off
    // ************************************************


    @Override
    public void run()
    {
	ObjectInfo object_ball;
        ObjectInfo object_goal;
        
        AgentFunction agFunc = new AgentFunction(m_strAgentFunction);
        try {
            agFunc.Parse();
        } catch (IOException ex) {}

	// first put it somewhere on my side
	if(Pattern.matches("^before_kick_off.*",m_playMode))
	    m_krislet.move( -Math.random()*52.5 , 34 - Math.random()*68.0 );

	while( !m_timeOver )
	    {
                object_ball = m_memory.getObject("ball");
                if (m_side == 'l')
                    object_goal = m_memory.getObject("goal r");
                else
                    object_goal = m_memory.getObject("goal l");
                
//                if(object_ball != null && object_ball.m_distance < 1.0 && object_goal != null)
//                    System.out.println("\nball: " + object_ball.m_distance + " , " + object_ball.m_direction);
//                else
//                    System.out.println("\nball: null");
//                if(object_goal != null)
//                    System.out.println("\ngoal: " + object_goal.m_direction);
//                else
//                    System.out.println("\ngoal: null");
                
                Action ac = agFunc.GetAction(object_ball, object_goal);
                if(ac.m_strCommand.compareToIgnoreCase("") == 0)
                    ac = agFunc.GetAction(object_ball, null);
                if(ac.m_strCommand.compareToIgnoreCase("") == 0)
                    ac = agFunc.GetAction(null, object_goal);
                
//                if(object_ball != null && object_ball.m_distance < 1.0 && object_goal != null)
//                    System.out.println("\n Action: " + ac.m_strCommand + ", " + ac.m_fDirection + ", " + ac.m_fPower);
                
                if (ac.m_strCommand.compareToIgnoreCase("dash") == 0)
                {
                    m_krislet.dash(ac.m_fPower);
                }
                else if (ac.m_strCommand.compareToIgnoreCase("turn") == 0)
                {
                    m_krislet.turn(ac.m_fDirection);
                    if(ac.m_bWait)
                        m_memory.waitForNewInfo();
                }
                else if (ac.m_strCommand.compareToIgnoreCase("kick") == 0)
                {
                    m_krislet.kick(ac.m_fPower, ac.m_fDirection);
                }
                else if(ac.m_strCommand.compareToIgnoreCase("") == 0)
                {
                    m_krislet.turn(40.0);
                    m_memory.waitForNewInfo();
                }

		// sleep one step to ensure that we will not send
		// two commands in one cycle.
		try{
		    Thread.sleep(2*SoccerParams.simulator_step);
		}catch(Exception e){}
	    }
	m_krislet.bye();
    }


    //===========================================================================
    // Here are suporting functions for implement logic


    //===========================================================================
    // Implementation of SensorInput Interface

    //---------------------------------------------------------------------------
    // This function sends see information
    @Override
    public void see(VisualInfo info)
    {
	m_memory.store(info);
    }


    //---------------------------------------------------------------------------
    // This function receives hear information from player
    @Override
    public void hear(int time, int direction, String message)
    {
    }

    //---------------------------------------------------------------------------
    // This function receives hear information from referee
    @Override
    public void hear(int time, String message)
    {						 
	if(message.compareTo("time_over") == 0)
	    m_timeOver = true;

    }


    //===========================================================================
    // Private members
    private final SendCommand	                m_krislet;			// robot which is controled by this brain
    private final Memory			m_memory;				// place where all information is stored
    private final char			m_side;
    volatile private boolean		m_timeOver;
    private final String                      m_playMode;
    private String                      m_strAgentFunction;
}
