/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rumput;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.*;

/**
 *
 * @author harris046
 */
public class WaterSprinklerAgent extends Agent{
    private int time = 10;
    private LengthSensorGUI lengthGUI;
    
    protected void setup(){
        addBehaviour(new CyclicBehaviour(this){
            public void action(){
                lengthGUI = LengthSensorGUI.getInstance();
                
                if(!Grass.isCutting()){
                    lengthGUI.appendLog("\n[WaterSprinklerAgent] Water sprinkler every " + time + "s");
                    
                    //inform ma
                    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                    msg.addReceiver(new AID("ma", AID.ISLOCALNAME));
                    
                    send(msg);
                }
                else{
                    lengthGUI.appendLog("\n[WaterSprinklerAgent] Water sprinkler blocked.");
                }
                block(time*1000);
            }
        });
    }
}
