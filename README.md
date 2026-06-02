## Serviço de Finanças do FACOFFEE
#### Professor: Hudson Silva Borges
#### Participantes: Gabriel Gordo Machado, Henrique Dias Albernaz, José Cláudio Soares Roland, Vitor Dias Martins

## Informações sobre o trabalho
Este repositório implementa o serviço de finanças do FACOFFEE, que consiste em:
- Gerir pendências, despesas e comprovantes.
- Consolidar estado financeiro dos participantes.
- Integrar com eventos dos demais domínios.
### Escopo funcional
- Criar e listar pendências financeiras.
- Consultar pendência por identificador.
- Registrar comprovantes de pagamento.
- Validar ou rejeitar comprovantes.
- Cadastrar e consultar despesas do domínio.
### Endpoints sob responsabilidade
Todos os endpoints do grupo Finance definidos em [api-docs.yaml](https://github.com/getHenrique/FACOFFEE-finace-service/blob/main/facofee-docs/api-docs.yaml).  
Referências obrigatórias:
- [api-docs.yaml](https://github.com/getHenrique/FACOFFEE-finace-service/blob/main/facofee-docs/api-docs.yaml)
- [async-docs.yaml](https://github.com/getHenrique/FACOFFEE-finace-service/blob/main/facofee-docs/async-docs.yaml)
### Regras de negócio mínimas
- Pendência deve refletir corretamente competência, valor e status.
- Comprovante só pode ser validado/rejeitado por papel autorizado.
- Mudanças de status financeiro devem ser consistentes e auditáveis.
- Regras de prazo e reprocessamento (se previstas) devem ser respeitadas.
### Segurança e autorização
- Validar JWT e papéis da claim roles.
- Aplicar regras de acesso conforme x-authorization.
- Operações de validação financeira restritas a MANAGER quando indicado no contrato.
### Eventos e integração
- Consumir eventos de participação que impactem cobrança.
- Publicar eventos financeiros relevantes conforme async-docs.yaml.
- Tratar reentrega de mensagens com idempotência.
- Registrar falhas de processamento com estratégia de retry/erro técnico.
### Persistência e modelo
- Entidades mínimas esperadas: FinancialPending, PaymentProof, Expense (ou equivalentes).
- Manter histórico de transições de status e data/hora de processamento.
- Banco de dados isolado de outros serviços.
### Testes obrigatórios
- Testes unitários de cálculo e transição de status.
- Testes de integração dos endpoints de pendência e comprovante.
- Testes de autorização e acesso negado.
- Testes de consumo/publicação de eventos críticos.
### Critérios de aceite
- Endpoints do domínio financeiro aderentes ao contrato.
- Fluxo de comprovantes completo (registro, validação, rejeição).
- Integração assíncrona confiável com os demais domínios.
- Cobertura de testes adequada ao risco das regras financeiras.
### Entregáveis
- Código-fonte do serviço Finance.
- Testes automatizados (unitários e integração).
- README com instruções de execução local.
- Evidências de cenários financeiros ponta a ponta.

## Contribuindo neste repositório:
#### Este repositório organiza suas branches por GitHub Flow.
Para isso, seguiremos os seguintes conceitos:
- A branch main protegida representa a configuração revisada e aprovada;
- Trabalhe apenas sob premissa e demanda de Issues. Ou seja, crie issues que representem o que deve ser desenvolvido antes de começar a desenvolver. Uma boa issue consiste em:
   - Título claro e conciso. Por exemplo: Feature request - Nova funcionalidade
   - Descrição clara com contexto, problema e mudanças (ou novidades) necessárias bem estabelecidos.
   - Caso se trate de reportar um defeito, descreva os passos de como reproduzí-lo.
   - Carga de trabalho baixa.
- Para trabalhar em algo novo, crie uma nova branch com nome descritivo, por exemplo: feature/novo-modulo;
- Comprometa-se com essa ramificação localmente e envie regularmente seu trabalho para a origem;
- Evite conflitos: não altere linhas de código fora de seu escopo, caso encontre algum problema, abra uma Issue ou comunique ao grupo;
- Quando achar que a ramificação está pronta para a fusão, abra um pull request, pedindo a outro participante para que revise seu trabalho;
- Caso a revisão seja aprovada, a branch será mesclada ao seu destino e excluída;
#### Siga uma semântica de commits adequada.
Por exemplo:  
`Feat (módulo)` - Introduzir nova funcionalidade  
`Fix (módulo)` - Corrigir um defeito  
`Docs (documento)` - Atualização de documentação  
`Test (módulo)` - Adicionar ou corrigir testes automatizados  
`Perf (módulo)` - Alterações de melhoria de performance  
`Style (módulo)` - Alterações de formatação/estrutura que não impactam na lógica  
`Comments (módulo)` - Adicionar ou atualizar comentários  
`Cleanup (módulo)` - Limpeza do código fonte (Ex: remover código morto ou comentários desnecessários)  
`Removal (módulo)` - Remover arquivos ou código obsoleto  
`Work in Progress (módulo)` - Trabalho em andamento  
`Chore (módulo)` - Mudanças arquiteturais  
`Revert (commit)` - Reverter mudanças de um commit  
`Merge (Origem & Destino)` - Mesclar branches  
