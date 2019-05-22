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
public class CutGrassAgent extends Agent{
    static final Base64 base64 = new Base64();
    private Grass grass;
    private LengthSensorGUI lengthGUI;
    
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
        addBehaviour(new CyclicBehaviour(this){
            public void action(){
                lengthGUI = LengthSensorGUI.getInstance();
                ACLMessage msg = receive();
                
                if(msg!=null){
                    lengthGUI.appendLog("\n[CutGrassAgent] Message from ma received");
                    String msgContent = msg.getContent();
                    
                    //deserialize
                    try{
                        grass = (Grass) deserializeObjectFromString(msgContent);
                    
                        if(grass.isIsLong()){
                            Grass.cutting = true;
                            
                            lengthGUI.appendLog("[CutGrassAgent] Cutting grass.");
                            for (int i = 0; i < 10; i++) {
                                lengthGUI.appendLog(".");
                                try {
                                    TimeUnit.SECONDS.sleep(1);
                                } catch (InterruptedException ex) {
                                }
                            }
                            
                            grass.setIsLong(false);
                            
                            //serialize
                            String strObj = "";
                            try {
                                strObj = serializeObjectToString(grass);
                            } catch (Exception e) {
                                System.out.println("[CutGrassAgent] ObjToStr conversion error: " + e.getMessage());
                            }
                            
                            //send req to cga
                            ACLMessage send = new ACLMessage(ACLMessage.INFORM);
                            send.addReceiver(new AID("ma", AID.ISLOCALNAME));
                            send.setContent(strObj);
                            send(send);
                        }
                    }
                    catch(Exception e){
                        System.out.println("[CutGrassAgent] ObjToStr conversion error: " + e.getMessage());
                    }
                }
                else{
                    block();
                }
            }
        });
    }
}
