/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package facom.ufms.ScoreAPIProxy;

import java.util.Random;

/**
 *
 * @author 202219060150
 */
public class APIClient implements ScoreClient {
    
    @Override
    public int score(String cpf) {
        
        Random rng = new Random();
        
        return rng.nextInt(1000);
        
    }
    
}
