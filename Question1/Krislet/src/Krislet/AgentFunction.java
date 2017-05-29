
package Krislet;

//
//	File:		AgentFunciton.java
//	Author:		Yvan Muheto Ntsinzi (#101059056)
//	Date:		May 23rd, 2017


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class AgentFunction
{
    public AgentFunction(String strFilePath)
    {
        m_strFilePath = strFilePath;
    }
    
    public boolean Parse() throws IOException
    {
        boolean bRes = false;
        try(BufferedReader br = new BufferedReader(new FileReader(m_strFilePath)))
        {
            String strLine = br.readLine();
            while(strLine != null)
            {
                // 
                Rule r = new Rule(strLine);
                if (r.BuildRule())
                {
                    rules.add(r);
                    bRes = true;
                }
                strLine = br.readLine();
            }
        }
        return bRes;
    }
    
    public Action GetAction(ObjectInfo ball, ObjectInfo goal)
    {
        int nNumObjects = 0;
        if(ball != null)
            nNumObjects++;
        if(goal != null)
            nNumObjects++;
        
        Action Ac = new Action("");
        
        for(Rule r : rules)
        {
            if(r.m_state.m_states.size() != nNumObjects)
                continue;
            State tmp = new State("");
            if(tmp.BuildState(ball, goal))
            {
                if(r.CompareState(tmp))
                {
                    //for(ObjectState objSt : tmp.m_states)
                    //    System.out.println("\nObject state: " + objSt.m_strObject + ", " + objSt.m_strDistance + ", " + objSt.m_strDirection);
                    Ac = r.m_action;
                    Ac.UpdateAction(ball, goal);
                    break;
                }
            }
        }      
        
        return Ac;
    }
    
    public List<Rule> rules = new ArrayList<>();
    public String m_strFilePath;
}

class Rule
{
    public Rule(String strRule)
    {
        m_strRule = strRule.trim();
    }
    
    public boolean BuildRule()
    {
        boolean bRes = false;
        if(';' == m_strRule.charAt(0))
            return bRes;
        String strChunks[] = m_strRule.split(m_strSeparator);
        if (2 == strChunks.length)
        {
            m_state = new State(strChunks[0].trim());
            m_action = new Action(strChunks[1].trim());
            if (m_state.BuildState() && m_action.BuildAction())
                bRes = true;
        }
        return bRes;
    }
    
    public boolean CompareState(State st)
    {
        boolean bRes = false;
        if (st.m_states.size() != m_state.m_states.size())
            return bRes;
        
        for (ObjectState objSt1 : st.m_states)
        {
            boolean bFound = false;
            for(ObjectState objSt2 : m_state.m_states)
            {
                if(objSt1.Compare(objSt2))
                {
                    bFound = true;
                    break;
                }
            }
            bRes = bFound;
            if(!bFound)
                break;
        }
        return bRes;
    }
    
    public State m_state;
    public Action m_action;
    private final String m_strRule;
    private final String m_strSeparator = "->";
}

class State
{
    public State(String strState)
    {
        m_strState = strState;
    }
    
    public boolean BuildState()
    {
        boolean bRes = false;
        String strObjStateArrary[] = m_strState.split(m_strDelimiter);
        for (String strObjState : strObjStateArrary) 
        {
            ObjectState st = new ObjectState(strObjState.trim());
            if(st.BuildState())
            {
                m_states.add(st);
                bRes = true;
            }
        }
        return bRes;
    }
    
    public boolean BuildState(ObjectInfo ball, ObjectInfo goal)
    {
        boolean bRes = false;
        if(ball != null)
        {
            ObjectState objSt = new ObjectState(ball.m_type);
            if (objSt.BuildState(ball))
            {        
                m_states.add(objSt);
            }
        }
        if(goal != null)
        {
            ObjectState objSt = new ObjectState(goal.m_type);
            if (objSt.BuildState(goal))
            {
                m_states.add(objSt);
            }
        }
        if(!m_states.isEmpty())
            bRes = true;
        
        return bRes;
    }
    
    public List<ObjectState> m_states = new ArrayList<>();
    private final String m_strDelimiter = "\\|";
    private final String m_strState;
}

class ObjectState
{
    public ObjectState(String strState)
    {
        m_strState = strState.replaceAll("\\[*\\]*", "");
        m_strObject = "";
        m_strDistance = "";
        m_strDirection = "";
    }
    
    public boolean BuildState()
    {
        String strFields[] = m_strState.split(m_strDelimiter);
        for(int i = 0; i < strFields.length; i++)
        {
            switch(i)
            {
                case 0:
                    m_strObject = strFields[i];
                    break;
                case 1:
                    m_strDistance = strFields[i];
                    break;
                case 2:
                    m_strDirection = strFields[i];
                    break;
            }
        }
        return true;
    }
    
    public boolean BuildState(ObjectInfo object)
    {
        boolean bRes = false;
        
        if (object != null)
        {
            m_strObject = object.m_type;
            if(m_strObject.compareToIgnoreCase("goal r") == 0 || m_strObject.compareToIgnoreCase("goal l") == 0)
            {
                m_strObject = "goal";
                bRes = true;
            }
                
            if(m_strObject.compareToIgnoreCase("ball") == 0)
            {
                if(object.m_distance > 1.0)
                    m_strDistance = "far";
                else
                    m_strDistance = "close";
                if(object.m_direction != 0)
                    m_strDirection = "different";
                else
                    m_strDirection = "same";
                bRes = true;
            }
        }
        
        return bRes;
    }
    
    public boolean Compare(ObjectState objSt)
    {
        boolean bRes = false;
        if(objSt.m_strObject.compareToIgnoreCase("ball") == 0)
        {
        if (m_strObject.compareToIgnoreCase(objSt.m_strObject) == 0 &&
            m_strDistance.compareToIgnoreCase(objSt.m_strDistance) == 0 &&
            m_strDirection.compareToIgnoreCase(objSt.m_strDirection) == 0)
            bRes = true;
        }
        else if(objSt.m_strObject.compareToIgnoreCase("goal") == 0)
        {
            if (m_strObject.compareToIgnoreCase(objSt.m_strObject) == 0)
                bRes = true;
        }
        return bRes;
    }
    
    public String m_strObject;
    public String m_strDistance;
    public String m_strDirection;
    private final String m_strState;
    private final String m_strDelimiter = ",";
}

class Action
{
    public Action(String strAction)
    {
        m_strAction = strAction.replaceAll("\\[*\\]*", "");
        m_bWait = false;
        m_strCommand = "";
        m_fPower = 0.0;
        m_bPower = false;
        m_fDirection = 0.0;
        m_bDirection = false;
    }
    
    public boolean BuildAction()
    {
        String strFields[] = m_strAction.split(m_strDelimiter);
        for(int i = 0; i < strFields.length; i++)
        {
            switch(i)
            {
                case 0:
                    m_strCommand = strFields[i];
                    break;
                case 1:
                    m_fDirection = 40.0;
                    try{
                        m_fDirection = new Float(strFields[i]).doubleValue();
                        m_bDirection = true;
                    }catch(NumberFormatException e){}
                    break;
                case 2:
                    m_fPower = 100.0;
                    try{
                        m_fPower = new Float(strFields[i]).doubleValue();
                        m_bPower = true;
                    }catch(NumberFormatException e){}
                    break;
                case 3:
                    if (strFields[i].compareToIgnoreCase("wait") == 0)
                        m_bWait = true;
                    break;
            }
        }
        return true;
    }
    
    public void UpdateAction(ObjectInfo ball, ObjectInfo goal)
    {
        if(m_strCommand.compareToIgnoreCase("turn") == 0)
        {
            if(!m_bDirection && ball != null)
                m_fDirection = ball.m_direction;
        }
        else if(m_strCommand.compareToIgnoreCase("dash") == 0)
        {
            if(m_bPower && ball != null)
                m_fPower = m_fPower * ball.m_distance;
        }
        else if(m_strCommand.compareToIgnoreCase("kick") == 0)
        {
            if(!m_bPower)
                m_fPower = 100.0;
            if(!m_bDirection && goal != null)
                m_fDirection = goal.m_direction;
        }
    }
    
    private final String m_strAction;
    public String m_strCommand;
    public boolean m_bWait;
    public double m_fPower;
    public boolean m_bPower;
    public double m_fDirection;
    public boolean m_bDirection;
    private final String m_strDelimiter = ",";
}