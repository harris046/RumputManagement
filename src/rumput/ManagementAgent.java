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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author harris046
 */
public class ManagementAgent extends Agent{
    static final Base64 base64 = new Base64();
    private int threshold = 10;
    private Grass grass;
    private LengthSensorGUI lengthGUI = null;
    private AID cutGrassAgentAID = null;
    
    //object to string
    public String serializeObjectToString(Object object) throws IOException 
    {
        String s = null;
        
        try 
        {
            ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
            GZIPOutputStream gzipOutputStream = new GZIPOutputStream(arrayOutputStream);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(gzipOutputStream);         
        
            objectOutputStream.writeObject(object);
            objectOutputStream.flush();
            gzipOutputStream.close();
            
            objectOutputStream.flush();
            objectOutputStream.close();
            
            s = new String(base64.encode(arrayOutputStream.toByteArray()));
            arrayOutputStream.flush();
            arrayOutputStream.close();
        }
        catch(Exception ex){}
        
        return s;
    }
    
    //string to object
    public Object deserializeObjectFromString(String objectString) throws IOException, ClassNotFoundException 
    {
        Object obj = null;
        try
        {    
            ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(base64.decode(objectString));
            GZIPInputStream gzipInputStream = new GZIPInputStream(arrayInputStream);
            ObjectInputStream objectInputStream = new ObjectInputStream(gzipInputStream);
            obj =  objectInputStream.readObject();
            
            objectInputStream.close();
            gzipInputStream.close();
            arrayInputStream.close();
        }
        catch(Exception ex){}
        return obj;
    }
    
    protected void setup(){
        //set this agent df
        String serviceName = "ManagementAgent";
        
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
                
                ACLMessage msg = receive();
                
                if(msg!=null){
                    //get cut grass agent df
                    try {
                        String serviceName = "CutGrassAgent";

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
                            cutGrassAgentAID = dfd.getName();
                        } else if (lengthGUI != null) {
                            lengthGUI.appendLog("\n[ManagementAgent] CutGrassAgent not found!");
                        }
                    } catch (FIPAException fe) {
                        fe.printStackTrace();
                    }
                    
                    
                    String msgContent = msg.getContent();
                    
                    try{
                        if (msg.getUserDefinedParameter("from").equals("lsa")) { //************************msg from lsa
                            lengthGUI.appendLog("\n[ManagementAgent] Message from LengthSensorAgent received");

                            //deserialize
                            try {
                                grass = (Grass) deserializeObjectFromString(msgContent);
                            } catch (Exception e) {
                                System.out.println("[ManagementAgent] StrToObj conversion error: " + e.getMessage());
                            }

                            lengthGUI.appendLog("[ManagementAgent] length => " + grass.getLength());

                            //process if long
                            if (grass.getLength() >= threshold) {
                                lengthGUI.appendLog("[ManagementAgent] Grass is long.");
                                grass.setIsLong(true);

                                //serialize
                                String strObj = "";
                                try {
                                    strObj = serializeObjectToString(grass);
                                } catch (Exception e) {
                                    System.out.println("[ManagementAgent] ObjToStr conversion error: " + e.getMessage());
                                }

                                //send req to cga
                                ACLMessage send = new ACLMessage(ACLMessage.REQUEST);
                                send.addReceiver(cutGrassAgentAID);
                                send.setContent(strObj);
                                send(send);
                            } else {
                                lengthGUI.appendLog("[ManagementAgent] Grass is short.");
                                grass.setIsLong(false);
                            }
                        } else if (msg.getUserDefinedParameter("from").equals("cga")) { //************************msg from cga
                            Grass.cutting = false;
                            lengthGUI.appendLog("\n[ManagementAgent] Message from CutGrassAgent received");
                            lengthGUI.appendLog("[ManagementAgent] Cut finished");
                        } else if (msg.getUserDefinedParameter("from").equals("wsa")) {
                        }
                    }
                    catch(Exception e){
                        
                    }
                }
                else{
                    block();
                }
               
            }
        });
    }
}
