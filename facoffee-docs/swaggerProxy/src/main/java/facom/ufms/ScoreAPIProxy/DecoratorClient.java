/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package facom.ufms.ScoreAPIProxy;

/**
 *
 * @author 202219060150
 */
public abstract class DecoratorClient implements ScoreClient {
    
    public ScoreClient client;
    
    @Override
    public abstract int score(String cpf);
    
    public DecoratorClient(ScoreClient client){
        this.client = client;
    }
    
}
