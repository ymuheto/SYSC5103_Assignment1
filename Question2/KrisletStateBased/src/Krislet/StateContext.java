package Krislet;


//	File:		StateContext.java
//	Author:		Yvan Muheto Ntsinzi (#101059056)
//      Course:         SYSC 5103
//      Assignment:     1, Question 2
//	Date:		May 23rd, 2017


interface SoccerPlayerState
{
    Action GetAction(ObjectInfo ball, ObjectInfo goal);
    void UpdateState(StateContext context, ObjectInfo ball, ObjectInfo goal);
}

class StateContext
{
    private SoccerPlayerState m_myState;
    public StateContext()
    {
        setState(new StateLookingForBall());
    }
    
    public final void setState(final SoccerPlayerState newState)
    {
        m_myState = newState;
    }
    
    public Action GetAction(ObjectInfo ball, ObjectInfo goal)
    {
        return m_myState.GetAction(ball, goal);
    }
    
    public void UpdateState(ObjectInfo ball, ObjectInfo goal)
    {
        m_myState.UpdateState(this, ball, goal);
    }
}

class StateLookingForBall implements SoccerPlayerState
{
    @Override
    public Action GetAction(ObjectInfo ball, ObjectInfo goal)
    {
        Action Ac = new Action();
        Ac.m_strCommand = "turn";
        Ac.m_fDirection = 40;
        Ac.m_bWait = true;
        return Ac;
    }

    @Override
    public void UpdateState(StateContext context, ObjectInfo ball, ObjectInfo goal)
    {
        if(ball != null)
        {
            if (ball.m_distance > 1.0)
                context.setState(new StateBallTooFar());
            else
                context.setState(new StateLookingForOpponentGoal());
        }
    }
}

class StateBallTooFar implements SoccerPlayerState
{
    @Override
    public Action GetAction(ObjectInfo ball, ObjectInfo goal)
    {
        Action Ac = new Action();
        if(ball.m_direction != 0)
        {
            Ac.m_strCommand = "turn";
            Ac.m_fDirection = ball.m_direction;
        }
        else
        {
            Ac.m_strCommand = "dash";
            Ac.m_fPower = 10 * ball.m_distance;
        }

        return Ac;
    }
    
    @Override
    public void UpdateState(StateContext context, ObjectInfo ball, ObjectInfo goal)
    {
        // if can't see the ball go to StateLookingForBall
        if(ball == null)
        {
            context.setState(new StateLookingForBall());
            return;
        }

        // If the ball is within kicking distance go to StateLookingForOpponentGoal
        if(ball.m_distance <= 1.0)
            context.setState(new StateLookingForOpponentGoal());
    }
}

class StateLookingForOpponentGoal implements SoccerPlayerState
{
    @Override
    public Action GetAction(ObjectInfo ball, ObjectInfo goal)
    {
        Action Ac = new Action();
        Ac.m_strCommand = "turn";
        Ac.m_fDirection = 40;
        Ac.m_bWait = true;
        return Ac;
    }
    
    @Override
    public void UpdateState(StateContext context, ObjectInfo ball, ObjectInfo goal)
    {
        if(ball == null)
        {
            context.setState(new StateLookingForBall());
            return;
        }
        
        if(goal != null)
            context.setState(new StateReadyToStrike());
    }
}

class StateReadyToStrike implements SoccerPlayerState
{
    @Override
    public Action GetAction(ObjectInfo ball, ObjectInfo goal)
    {
        Action Ac = new Action();
        Ac.m_strCommand = "kick";
        Ac.m_fDirection = goal.m_direction;
        Ac.m_fPower = 100;
        return Ac;
    }

    @Override
    public void UpdateState(StateContext context, ObjectInfo ball, ObjectInfo goal)
    {
        if(ball == null)
        {
            context.setState(new StateLookingForBall());
            return;
        }
        
        if(goal == null)
        {
            context.setState(new StateLookingForOpponentGoal());
            return;
        }
        
        if(ball.m_distance > 1.0)
            context.setState(new StateBallTooFar());
    }
}

class Action
{
    public Action()
    {
        m_strCommand = "";
        m_bWait = false;
        m_fPower = 0.0;
        m_bPower = false;
        m_fDirection = 0.0;
        m_bDirection = false;
    }
    
    public String m_strCommand;
    public boolean m_bWait;
    public double m_fPower;
    public boolean m_bPower;
    public double m_fDirection;
    public boolean m_bDirection;
}