package com.facoffee.financeService.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class FinanceController {

    @GetMapping("/health")
    public Map<String, String> healthCheck() {
        return Map.of(
                "status", "UP",
                "service", "FACOFFEE-Finance",
                "message", "I'm alive!"
        );
    }

    // Exemplo de rota protegida que qualquer usuário autenticado acessa
    @GetMapping("/pending")
    public Map<String, String> getMyPendingFinance() {
        return Map.of("message", "Lista de pendências do usuário logado (Simulação)");
    }
}