/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package facom.ufms.ScoreAPIProxy;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
/**
 *
 * @author 202219060150
 */
public class CachedClient extends DecoratorClient {
   
    private final Map<String, Integer> cache;
    
    @Override
    public int score(String cpf) {
      if (this.cache.containsKey(cpf)) {
        return this.cache.get(cpf);
      }

      return client.score(cpf);
      
    }
    
    public CachedClient(ScoreClient client) {
        super(client);
        this.cache = new HashMap<>();
    }
    
}
