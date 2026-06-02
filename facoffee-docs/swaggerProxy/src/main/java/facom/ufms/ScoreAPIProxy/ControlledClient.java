/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package facom.ufms.ScoreAPIProxy;

/**
 *
 * @author 202219060150
 */
public class ControlledClient extends DecoratorClient {
    
    @Override
    public int score(String cpf) {
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            System.getLogger(ControlledClient.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
        
        return this.client.score(cpf);
        
    }
    
    public ControlledClient(ScoreClient client) {
        super(client);
    }
    
}
