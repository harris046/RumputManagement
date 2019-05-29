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

import java.util.concurrent.TimeUnit;

/**
 *
 * @author harris046
 */
public class WaterSprinklerAgent extends Agent{
    private int time = 10;
    private LengthSensorGUI lengthGUI = null;
    private AID managementAgentAID = null;
    
    protected void setup(){
        //set this agent df
        String serviceName = "WaterSprinklerAgent";
        
        try {
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            ServiceDescription sd = new ServiceDescription();
            sd.setName(serviceName);
            sd.setType(serviceName);
            dfd.addServices(sd);
  		
            DFService.register(this, dfd);
  	}
  	catch (FIPAException fe) {
            fe.printStackTrace();
  	}
        
        addBehaviour(new CyclicBehaviour(this){
            public void action(){
                lengthGUI = LengthSensorGUI.getInstance();
                
                //get management agent df
                try {
                    String serviceName = "ManagementAgent";
//            String serviceType = "";

                    // Build the description used as template for the search
                    DFAgentDescription template = new DFAgentDescription();

                    ServiceDescription templateSd = new ServiceDescription();
                    templateSd.setName(serviceName);
                    template.addServices(templateSd);

                    SearchConstraints sc = new SearchConstraints();
                    // We want to receive 1 results at most
                    sc.setMaxResults(new Long(1));

                    //search df
                    DFAgentDescription[] results = DFService.search(this.myAgent, template, sc);

                    if (results.length > 0) {
                        DFAgentDescription dfd = results[0];
                        managementAgentAID = dfd.getName();
                    } else {
//                        if(lengthGUI!=null) lengthGUI.appendLog("\n[WaterSprinklerAgent] MangementAgent not found!");
                    }
                    
                    if (!Grass.isCutting()) {
                        if (lengthGUI != null) {
                            lengthGUI.appendLog("\n[WaterSprinklerAgent] Water sprinkler every " + time + "s");
                        }
                        
                        //inform ma
                        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                        msg.addUserDefinedParameter("from", "wsa");
                        msg.addReceiver(managementAgentAID);

                        send(msg);
                    } else {
                        lengthGUI.appendLog("\n[WaterSprinklerAgent] Water sprinkler blocked.");
                    }
                    
                    try{
                        TimeUnit.SECONDS.sleep(time);
                    }
                    catch(InterruptedException ie){
                        Thread.currentThread().interrupt();
                    }
                    
                    
                } catch (Exception fe) {
                    fe.printStackTrace();
                }
            }
        });
    }
}
