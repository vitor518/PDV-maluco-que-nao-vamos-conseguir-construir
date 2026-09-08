# PDV-maluco-que-n-o-vamos-conseguir-construir
# Projeto LEV — Sistema de Gestão para Restaurantes
### Pesquisa sobre o mercado (NoxMob e concorrentes) + Arquitetura + Plano de Ação

---

## 1. O que descobri sobre o NoxMob (referência que vocês querem superar)

O NoxMob (da empresa THINK Serviços de Informática / Nox Automação) é hoje um dos sistemas mais usados por pequenos e médios restaurantes no Brasil. A empresa não divulga publicamente a stack técnica (código fechado, sem GitHub público, sem documentação de arquitetura), então não dá pra "ver o código deles" — mas dá pra mapear muito bem o que o produto entrega, como ele é estruturado e onde ele falha, a partir da documentação de suporte e relatos de clientes. Resumo do que encontrei:

**Estrutura do produto (3 frentes, exatamente como a de vocês):**
- **NoxMob PDV** — app de frente de caixa, com versões para Windows (desktop) e para maquininhas Android (SmartPOS), incluindo integração nativa com a maquininha Laranjinha (Rede/Itaú) e outras adquirentes.
- **Painel Administrativo Web** — backoffice em nuvem: cadastro de produtos, fichas técnicas, estoque, financeiro, relatórios, multi-loja, multiusuário.
- **Oba.Menu** — cardápio digital via QR Code/link (ex: `suaempresa.nox.com.br`), integrado nativamente ao PDV, com delivery próprio, fila de pedidos, KDS (tela de cozinha) e notificações via WhatsApp. É oferecido "de graça" porque na verdade ele já está embutido na mesma plataforma — não é um produto separado, é uma camada web que lê o mesmo cadastro de produtos do PDV.
- **Módulo fiscal**: emissão de NF-e, NFC-e e NFS-e, com fila de "pendentes" para reenvio quando a SEFAZ está fora do ar, e um "modo de contingência" para vender mesmo sem autorização fiscal em tempo real.
- Funciona **online e offline**: o PDV grava localmente e sincroniza quando a internet volta — ponto crítico para restaurante, porque queda de internet não pode parar o caixa.
- Existe um "DS (DataSnap) Local" — um computador na loja que funciona como servidor local para mesas/comandas, ou seja, mesmo em nuvem, eles mantêm uma camada de servidor local para latência e resiliência.

**Onde eles pecam (oportunidades reais para o LEV):**
1. **Instabilidade e travamentos** relatados por usuários, especialmente em maquininhas com conexão móvel (3G/4G) — a própria documentação deles admite que "a conexão por dados móveis não entrega performance adequada" e recomenda Wi-Fi.
2. **Suporte lento e processo de cancelamento/retenção contestado** (reclamações registradas no Reclame Aqui sobre cobrança indevida e dificuldade de cancelamento) — isso é uma oportunidade de posicionamento: contrato claro, sem fidelidade agressiva, suporte ágil.
3. **Dependência forte de um único computador local (DS)** para mesas/comandas — se essa máquina falha, a loja toda trava. É um ponto único de falha que dá pra resolver com arquitetura mais moderna (sync distribuído, fallback automático).
4. **UX datada** em algumas telas do PDV desktop (aparência de sistema legado Windows).
5. Não há evidência pública de app nativo multiplataforma (parece ser algo mais próximo de Delphi/Windows Forms para desktop) — abre espaço para vocês fazerem algo mais moderno, leve e com deploy mais rápido de atualizações.

---

## 2. Comparação com outros players (Brasil e mundo)

| Sistema | Pontos fortes | Pontos fracos / limitações |
|---|---|---|
| **NoxMob** | Completo, fiscal + cardápio + PDV integrados, preço acessível | Instabilidade relatada, UX datada, suporte contestado |
| **Consumer** | Popular, TEF fácil | TEF só com PinPad fixo USB, sem SmartPOS integrado nativamente |
| **Toast (EUA)** | Hardware Android proprietário robusto, ecossistema enorme | Caro, focado no mercado americano, sem fiscal brasileiro |
| **TouchBistro** | Funciona offline muito bem, UX cuidada | iPad-only, sem fiscal BR |
| **Square** | Fácil de configurar, plano grátis para começar | Fiscal BR limitado, menos profundo em ficha técnica/insumos |
| **Clover** | Hardware + software integrados, app marketplace | Ecossistema fechado, fiscal BR via parceiros |

**Conclusão da pesquisa:** o padrão de mercado (mesmo fora do Brasil) é: **PDV Android/iOS + backoffice web em nuvem + cardápio digital + KDS**, com o diferencial brasileiro sendo o **módulo fiscal (NFC-e/NF-e/NFS-e)**, que é o ponto mais delicado tecnicamente (homologação com SEFAZ de cada estado) e o ponto onde mais vale a pena **não reinventar a roda**, usando uma API fiscal especializada por trás (ver seção 4.4).

---

## 3. Restrições reais que precisam guiar as decisões técnicas

1. **Hardware fraco**: maquininhas SmartPOS Android rodam processadores ARM de entrada (quad-core baixo clock, 1-2GB RAM), Android customizado (geralmente Android 9-11 "GMS ou não"), tela pequena (5"-6"), sem Google Play em alguns modelos (usam loja própria da adquirente).
2. **PDV precisa funcionar offline** (venda não pode parar por causa de internet) e sincronizar depois.
3. **Homologação com adquirente**: para o app rodar dentro da maquininha Android do Itaú (Rede), é preciso se cadastrar no **Portal do Desenvolvedor Rede** (`developer.userede.com.br`) e passar por processo de homologação — isso é obrigatório e tem prazo (semanas/meses).
4. **Fiscal é regulado por estado** (NFC-e é estadual, SEFAZ de cada UF tem regras próprias) — construir isso do zero é o maior risco técnico e de manutenção contínua do projeto.
5. **LGPD**: dados de clientes (CPF na nota, telefone no cardápio digital, etc.) exigem cuidado — política de privacidade, criptografia, controle de acesso.

---

## 4. Arquitetura proposta do LEV

### 4.1 Visão geral (5 componentes)

```
┌─────────────────────┐     ┌──────────────────────┐
│  LEV PDV Mobile      │     │  LEV PDV Desktop      │
│  (Android SmartPOS)  │     │  (Windows/Linux)      │
└──────────┬───────────┘     └───────────┬───────────┘
           │        REST/GraphQL + WebSocket (sync)  │
           └───────────────┬───────────────────────┘
                            │
                 ┌──────────▼───────────┐
                 │   LEV API Central     │  ← Backend em nuvem
                 │ (Node.js/NestJS ou    │
                 │  Go, PostgreSQL)      │
                 └──────┬───────┬────────┘
                        │       │
        ┌───────────────┘       └────────────────┐
┌───────▼────────┐                      ┌─────────▼─────────┐
│ LEV Web Admin   │                      │ LEV Menu (cardápio │
│ (painel gestor) │                      │ digital + delivery)│
└─────────────────┘                      └────────────────────┘
                        │
              ┌─────────▼─────────┐
              │  Serviço Fiscal    │ → API terceirizada (Focus NFe/
              │  (NFC-e/NF-e)      │    Nuvem Fiscal/PlugNotas)
              └────────────────────┘
```

### 4.2 PDV Android (maquininha Itaú/Rede)

- **Linguagem/framework recomendado**: **Kotlin nativo** (não Flutter/React Native aqui). Justificativa: hardware fraco, SDK da adquirente (Rede) é nativo Android, e apps de PDV fiscal se beneficiam de baixo overhead, boot rápido e menor consumo de RAM — nativo Kotlin é o que roda melhor nesses aparelhos.
- Banco local embarcado: **SQLite (via Room)** para funcionar 100% offline, com fila de sincronização (outbox pattern) para quando a conexão voltar.
- Comunicação com o backend: REST para operações normais + WebSocket para eventos em tempo real (novo pedido chegando do cardápio digital, status de mesa mudando, etc.).
- Integração de pagamento: usar o **SDK oficial da Rede (Itaú)**, via cadastro no Portal do Desenvolvedor Rede. Alternativa mais rápida para MVP: contratar uma **TEF House multi-adquirente** (ex: Connect TEF) que já é homologada com Rede/Itaú e várias outras — isso permite lançar mais rápido e depois, se fizer sentido, migrar para integração direta.
- UI: leve, poucos elementos animados, otimizada para toque com uma mão só (o operador segura a maquininha).

### 4.3 PDV Desktop (computador da loja/caixa fixo)

- **Framework recomendado**: **Electron com backend local em Node.js + SQLite**, ou alternativa mais leve **Tauri (Rust + WebView)** se performance em máquinas fracas for prioridade — Tauri consome muito menos RAM que Electron, o que é relevante porque muitos restaurantes usam PCs antigos como caixa.
- Reaproveita o **mesmo motor de regras de negócio** do app mobile (idealmente em uma lib compartilhada — TypeScript no core, compilada para os dois lados) para evitar manter duas lógicas de venda divergentes, que é uma fonte clássica de bugs.
- Também funciona offline com sincronização posterior, e pode atuar como "servidor local" de mesas/comandas para os terminais da loja (substituindo o "DS Local" do NoxMob, mas de forma mais resiliente — se cair, os PDVs continuam operando offline e sincronizam depois, sem travar a loja toda).

### 4.4 Backend / API Central

- **Stack sugerida**: **Node.js com NestJS + TypeScript** (produtividade alta, tipagem forte, ótimo para equipe pequena) ou **Go** (se performance e baixo custo de servidor forem prioridade desde o início). Para dois desenvolvedores começando do zero, **NestJS** tende a ser mais rápido de entregar com qualidade.
- **Banco de dados**: **PostgreSQL** (multi-tenant: um schema/coluna `empresa_id` para separar cada restaurante cliente do LEV).
- **Arquitetura**: multi-tenant SaaS desde o início (mesmo pensando em vender para poucas lojas no começo, arquitetar multi-tenant evita retrabalho quando o negócio crescer).
- **Infraestrutura**: containers (Docker) + orquestração simples no início (ex: um único servidor com Docker Compose ou serviço gerenciado tipo Railway/Render) evoluindo para Kubernetes só quando o volume justificar. Evitem complexidade prematura.
- **Fila de eventos**: Redis (cache + fila leve) para sincronização em tempo real entre PDV, cardápio digital e KDS.

### 4.5 Módulo Fiscal (NFC-e / NF-e / NFS-e)

Este é o ponto onde eu recomendo fortemente **não reinventar a roda no início**. Construir a integração direta com SEFAZ de 27 estados (cada um com particularidades, certificado digital A1, contingência, homologação) é um projeto técnico e jurídico gigantesco por si só.

**Recomendação**: usar uma **API fiscal white-label** por trás do LEV (o cliente final nem precisa saber que existe) — as principais opções do mercado:
- **Focus NFe** — mais madura, cobre NFC-e/NF-e/NFS-e em milhares de municípios, boa documentação REST.
- **Nuvem Fiscal** e **PlugNotas (TecnoSpeed)** — alternativas robustas, focadas em multi-tenant/SaaS.

Isso permite lançar o módulo fiscal em semanas em vez de meses/anos, e reduz drasticamente o risco de vocês ficarem responsáveis por manter compliance fiscal estadual — que muda com frequência.

### 4.6 LEV Menu (cardápio digital — equivalente ao Oba.Menu)

- É essencialmente uma **aplicação web (Next.js/React)** que lê o mesmo catálogo de produtos do backend central — não é um sistema separado, é uma "vitrine" pública por loja (`nomedaloja.lev.app` ou domínio próprio).
- Funcionalidades: QR Code por mesa, pedido direto pelo cliente (opcional, configurável por loja), delivery próprio sem taxa de marketplace, acompanhamento de status do pedido, notificação via WhatsApp (API oficial do WhatsApp Business ou provedores como Twilio/Z-API).
- Deve ser **Server-Side Rendered** para carregar rápido em celular de cliente com internet ruim, e ser leve o suficiente pra rodar bem mesmo em conexões 3G — esse é justamente um ponto fraco comum em cardápios digitais concorrentes.

### 4.7 KDS (tela de cozinha) e demais telas internas

- App web leve (React) rodando em modo "quiosque" em qualquer tablet/monitor touch da cozinha, recebendo pedidos via WebSocket em tempo real.

---

## 5. Diferenciais que o LEV pode ter sobre o NoxMob (e o mercado em geral)

1. **Resiliência real**: cada PDV (mobile ou desktop) opera 100% offline com fila de sincronização, sem depender de um único "servidor local" da loja — elimina o ponto único de falha que o NoxMob tem hoje.
2. **Observabilidade desde o dia 1**: monitoramento de erros (ex: Sentry) e métricas de uso, para vocês descobrirem travamentos antes do cliente reclamar — resolvendo diretamente a maior queixa relatada sobre o concorrente.
3. **Contrato e cancelamento transparentes**: diferencial comercial direto contra as reclamações de retenção agressiva do concorrente.
4. **Atualizações OTA (over-the-air) mais ágeis**: como o core de regras de negócio pode ser compartilhado entre mobile/desktop, correções de bugs chegam mais rápido em todas as pontas.
5. **IA aplicada com propósito claro** (não só "recurso de marketing"): sugestão de reposição de estoque, previsão de demanda por dia da semana/clima, sugestão de preço de venda (semelhante à "MobIA" do concorrente, mas com foco em algo mensurável: redução de ruptura de estoque e de desperdício).
6. **Relatórios contábeis de verdade**: exportação direta em formatos que o contador já usa (ex: SPED, ou pelo menos CSV/Excel estruturado para conciliação bancária e DRE simplificado) — muitos sistemas de PDV entregam "relatórios bonitos" mas fracos para uso contábil real.

---

## 6. Funcionalidades completas do LEV (escopo do produto)

**PDV (mobile e desktop)**
- Abertura/fechamento de caixa, sangria, suprimento
- Vendas balcão, mesas, comandas, delivery, retirada
- Split de conta, taxa de serviço, descontos com alçada
- Personalização de itens (ex: "sem cebola", "ponto da carne", adicionais pagos, meio a meio de pizza)
- Pagamento: dinheiro, cartão (débito/crédito via SDK Rede/Itaú ou TEF multi-adquirente), Pix, vale-refeição, cashback/fidelidade
- Modo offline com sincronização automática
- Emissão de NFC-e/cupom não fiscal, com contingência

**Backoffice Web**
- Cadastro de produtos, categorias, combos, kits, variações
- Ficha técnica (insumos por produto, custo, margem)
- Controle de estoque (entrada, saída, inventário, alertas de ruptura)
- Financeiro (contas a pagar/receber, fluxo de caixa, conciliação)
- Multi-loja e multiusuário com permissões granulares
- Relatórios gerenciais (vendas por período/produto/canal/operador) e **relatórios contábeis** (DRE simplificado, exportação para contador)
- Configuração fiscal por loja (regime tributário, CST, NCM, certificado digital)

**LEV Menu (cardápio digital)**
- QR Code por mesa, link público por loja
- Delivery próprio + integração opcional com iFood/Rappi
- KDS integrado
- Fila/senha com notificação
- Promoções e cupons

---

## 7. Plano de ação (fases sugeridas)

### Fase 0 — Fundação (2-4 semanas)
- Definir nome definitivo, registrar domínio e marca
- Escolher stack final e montar repositórios (monorepo recomendado: `apps/pdv-mobile`, `apps/pdv-desktop`, `apps/api`, `apps/web-admin`, `apps/menu`, `packages/core-business-rules`)
- Modelar banco de dados (produtos, pedidos, estoque, financeiro, multi-tenant)
- Abrir cadastro no Portal do Desenvolvedor Rede (Itaú) — processo de homologação demora, então iniciar cedo é crítico
- Escolher e contratar a API fiscal (recomendo começar testando a Focus NFe em ambiente de homologação)

### Fase 1 — MVP PDV + Backoffice básico (8-12 semanas)
- Backend com autenticação multi-tenant, cadastro de produtos, categorias
- PDV desktop funcional (venda, caixa, formas de pagamento simuladas)
- Web admin com relatórios básicos de venda
- Emissão de NFC-e em ambiente de homologação da SEFAZ (via API fiscal)

### Fase 2 — PDV mobile na maquininha + pagamento real (6-10 semanas, em paralelo com homologação Rede)
- App Android nativo homologado na maquininha Itaú/Rede
- Sincronização online/offline entre desktop e mobile
- Testes de carga em hardware real de baixo desempenho

### Fase 3 — Cardápio digital (LEV Menu) + KDS (6-8 semanas)
- Cardápio público com pedido e delivery próprio
- Painel de cozinha em tempo real
- Notificações via WhatsApp

### Fase 4 — Módulos avançados (contínuo)
- Ficha técnica e controle de insumos
- Financeiro completo e relatórios contábeis
- IA para estoque/demanda
- Programa de fidelidade

### Fase 5 — Piloto e lançamento comercial
- Rodar em 1-2 restaurantes reais (idealmente de alguém próximo, disposto a dar feedback direto) por 30-60 dias antes de vender publicamente
- Ajustar com base em bugs e fricções reais de operação (é aqui que aparecem 90% dos problemas que só usuário real revela)
- Só depois, abrir venda para o mercado

---

## 8. Pontos de atenção que não são só técnicos

- **Homologação com adquirente (Rede/Itaú)** e com cada **SEFAZ estadual** (via API fiscal) são os dois maiores gargalos de prazo — comecem por eles.
- **Certificado digital A1** da empresa cliente é obrigatório para emissão fiscal — o sistema precisa de um fluxo seguro de upload/renovação disso.
- Avaliem se querem abrir uma empresa (CNPJ) antes de comercializar — necessário para nota fiscal do próprio serviço de vocês (SaaS B2B) e para se cadastrar como parceiro nos portais de desenvolvedor.
- **LGPD**: politica de privacidade, criptografia de dados de clientes, e cuidado redobrado com dados de pagamento (nunca armazenar dado de cartão — isso é sempre responsabilidade do SDK da adquirente/TEF).

---

Se quiser, no próximo passo posso detalhar o modelo de dados (schema do banco), o roadmap técnico do MVP em formato de backlog (com épicos e tarefas), ou uma estimativa de custos de infraestrutura e das APIs mencionadas.
