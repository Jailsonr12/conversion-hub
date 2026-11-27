# Estudo de integrações — conversion-hub

> Estudo feito em nov/2026, antes da primeira migração. Objetivo: definir quais
> plataformas atender, o que cada API de destino exige e como desenhar o banco
> (log, idempotência e retry).
>
> Legenda: **[confirmado]** = visto em documentação oficial (via busca);
> **[a confirmar]** = fonte secundária ou não verificado; checar na doc oficial
> ou com um webhook de teste antes de codar.

## 1. Resumo e decisões

| Tema | Decisão proposta |
|---|---|
| Origens do MVP | **Hotmart** e **Shopify**. Kiwify e Nuvemshop entram na sequência, pela mesma interface de adaptador. |
| Destino Meta | Conversions API (Graph API, `/{pixel_id}/events`). |
| Destino Google | **Data Manager API** (`events.ingest`), e **não** o `ConversionUploadService` da Google Ads API. Desde 15/06/2026 a Google Ads API bloqueia novos usuários de importação offline. |
| Idempotência na entrada | `UNIQUE (source, source_event_key)` na tabela de eventos recebidos. |
| Idempotência na saída | `event_id` (Meta) e `transactionId` (Google) **determinísticos**, derivados da origem. Assim o próprio destino também deduplica. |
| Retry | Tabela de entregas por destino (padrão outbox), com worker usando `FOR UPDATE SKIP LOCKED`, backoff exponencial e prazo máximo por destino. |
| Resposta ao webhook | Valida assinatura, grava e responde 200 em seguida. O envio é assíncrono (a Shopify espera só ~5 s). |

## 2. Mercado: quais plataformas atender

Nenhuma fonte traz market share auditado. São números de rastreadores e de blogs
com interesse comercial, então servem só como indicação.

**Infoprodutos (checkout):**
- **Hotmart**: descrita de forma unânime como a maior e mais antiga, com a maior rede de afiliados.
- **Kiwify**: citada como a segunda do mercado (~9,5 mi visitas/mês em jan/2026, segundo fonte fraca).
- **Eduzz**: madura e forte em afiliados, mas sem ranking.
- Outras que aparecem: Monetizze, Ticto, Braip, Perfect Pay. Avaliar depois, conforme a demanda dos gestores.

**E-commerce:** contagem de lojas no Brasil pela Store Leads (out/2026):

| Plataforma | Lojas | Observação |
|---|---|---|
| Nuvemshop | ~126,7 mil | Líder em pequenos lojistas |
| Shopify | ~30,2 mil | Melhor documentação; vale para portfólio internacional |
| Tray | ~29,8 mil | Médios |
| Loja Integrada | ~18,2 mil | Pequenos |
| VTEX | ~2,7 mil | Enterprise (fora do público-alvo inicial) |

Também aparecem com frequência: WooCommerce e Yampi (checkout).

**CRMs:** não existe ranking confiável. Os mais citados para PMEs brasileiras são
**RD Station CRM**, **Pipedrive**, **HubSpot** e **Kommo** (este pela integração com WhatsApp).

**Por que Hotmart + Shopify no MVP:** a Hotmart cobre o maior volume do nicho de
infoprodutos, em que gestor de tráfego é o cliente típico. A Shopify tem a
documentação mais completa de webhooks (assinatura, deduplicação), então serve
de referência para o desenho. A Nuvemshop é a maior em lojas, mas o webhook dela
é "magro" (ver 4.4) e exige um app com token para buscar o pedido. Por isso fica
para a fase 2.

## 3. Destinos

### 3.1 Meta Conversions API

- **Envio:** `POST /{pixel_id}/events` com `data: [...]`. São **até 1.000 eventos por requisição** e não há rate limit específico, porque conta como chamada da Marketing API **[confirmado]**.
- **`event_time` com no máximo 7 dias:** se *um* evento do lote for mais antigo, a requisição **inteira** é rejeitada **[confirmado]**. Consequências:
  - o lote nunca deve misturar eventos perto do limite;
  - o retry precisa de prazo (desistir antes dos 7 dias e marcar como `EXPIRED`).
- **`event_id` + `event_name`:** deduplicam com o Pixel (o `eventID` do browser) numa janela de 7 dias **[confirmado]**. A recomendação é mandar `event_id` e `external_id` em todos os eventos.
  - Proposta: `event_id = "{source}:{order_id}:{event_type}"`. Para casar com o Pixel, o checkout precisa disparar o Pixel com o mesmo id, e isso nem sempre dá para controlar em Hotmart/Kiwify. Documentar isso para o gestor.
- **`action_source`:** `website` para compra em checkout; `system_generated` para eventos de CRM/lead qualificado **[confirmado]**. Para lead de CRM, o `event_time` precisa ser posterior à geração do lead.
- **Hash (`user_data`):** SHA-256 em todos os campos de cliente, **exceto** `client_ip_address`, `client_user_agent`, `fbc` e `fbp`. Campo de PII que exige hash e chega sem hash é rejeitado **[confirmado]**. Campos com hash: `em`, `ph`, `fn`, `ln`, `ge`, `db`, `ct`, `st`, `zp`, `country`; `external_id` com hash recomendado.
- **Normalização antes do hash [a confirmar na seção oficial "normalize and hash"]:**
  - `em`: trim + minúsculas
  - `ph`: só dígitos, **com DDI** (`55` + DDD + número)
  - `fn`/`ln`: minúsculas, sem pontuação
  - `ct`: minúsculas, sem espaços
  - `st`: sigla de 2 letras minúscula (`sp`)
  - `zp`: só dígitos (CEP)
  - `country`: ISO-2 minúsculo (`br`)
- **Teste:** usar `test_event_code` para validar no Test Events do Events Manager.
- **`fbc`/`fbp`:** melhoram muito o match, mas só existem se a origem repassar o `fbclid`/cookies. Hotmart e Kiwify têm campos de tracking (ver seção 4); vale checar se carregam o `fbclid`.

### 3.2 Google: Data Manager API (mudança importante)

O plano original fala em "Google Ads Offline Conversions" pela Google Ads API.
**Isso mudou:**

- Desde **15/06/2026**, importação offline e *enhanced conversions for leads* foram migradas para a **Data Manager API** e estão **bloqueadas na Google Ads API para novos usuários**. Só quem já importava entre dez/2025 e mai/2026 continua, temporariamente **[confirmado]**. Um projeto novo como este já nasce na Data Manager API.
- **Método:** `events.ingest` (`IngestEventsRequest`) **[confirmado]**.
- **Destino:** `destinations[]`, com `operatingAccount` (a conta Google Ads dona da ação de conversão) e `productDestinationId` = **ID da ação de conversão** **[confirmado]**. Não existe a lógica de conta-mãe/filha da Google Ads API: o `operatingAccount` precisa ser o dono da conversão.
- **Campos do evento:**
  - `transactionId`: id único do evento, base da deduplicação. Proposta: o mesmo valor do `event_id` da Meta.
  - `eventTimestamp`
  - `conversionValue`, `currency`
  - `adIdentifiers.gclid` (ou `gbraid`/`wbraid`)
  - `userData.userIdentifiers` (e-mail, telefone, endereço)
- **Encoding:** `encoding: HEX` no nível da requisição para os identificadores com hash **[confirmado]**.
- **Auth:** OAuth com escopo próprio da Data Manager API. Segundo fonte secundária, **não usa developer token** **[a confirmar]**, o que simplifica bastante (o developer token da Google Ads exige aprovação). Ver se aceita service account.
- **GCLID x enhanced conversions for leads:** o Google trata o import só com GCLID como legado e recomenda *enhanced conversions for leads* (dados de usuário com hash, `gclid` junto quando houver).
  - Janela: ~90 dias do clique com GCLID **[a confirmar]**; ~63 dias para EC for leads **[a confirmar]**.
- **Normalização Google [a confirmar]:** e-mail em minúsculas sem espaços (o Google também remove pontos antes do `@` em gmail/googlemail); telefone em E.164 (`+55...`); hash SHA-256 em hex.
- **Consentimento:** existe campo de consentimento no evento. É obrigatório para EEA. Para o Brasil (LGPD) avaliar o preenchimento **[a confirmar]**.
- **Pendências:** limite de eventos por requisição, existência de `validateOnly` e formato do diagnóstico de erro (síncrono ou status consultado depois). Ler a referência de `events.ingest` antes de implementar.

### 3.3 Consequência para o código

As duas APIs pedem as mesmas entradas (dados de usuário normalizados + id
determinístico + valor/moeda + click id), mas com **normalizações ligeiramente
diferentes**. Por isso o desenho fica assim:

1. Modelo interno canônico (`ConversionEvent`) com os dados **normalizados e sem hash**, só em memória.
2. Um `Hasher`/serializador por destino, aplicando as regras específicas de cada um.
3. Persistir **apenas hashes + payload bruto da origem**, com política de retenção (LGPD).

## 4. Origens (webhooks)

### 4.1 Hotmart (Webhook 2.0)
- **Autenticidade:** header `X-HOTMART-HOTTOK`, comparado com o hottok da conta. Usar comparação em tempo constante **[confirmado]**.
- **Estrutura:** envelope com `event` (ex.: `PURCHASE_APPROVED`), `creation_date` (epoch **em ms**) e `data`, que contém `buyer`, `product` e `purchase` **[confirmado parcialmente]**.
  - Um `id` único de entrega no envelope é **[a confirmar]**: capturar um webhook de teste.
- **Id de negócio:** `data.purchase.transaction` (ex.: `HP17715690036014`) **[confirmado]**.
- **Tracking:** `src` (UTM da página de vendas) e `sck` (UTM do checkout) **[confirmado]**. Combinar com o gestor para mandar `fbclid`/`gclid` via `sck`/`src` quando possível.
- **Eventos relevantes:** compra aprovada/completa, reembolso, chargeback, cancelamento, boleto impresso, abandono de carrinho.
- **Cuidados:**
  - a Hotmart **pode enviar o mesmo webhook duas vezes**;
  - se a URL responder erro, a Hotmart **desativa a configuração automaticamente**. Responder 2xx rápido é crítico; erro interno nosso não pode virar 5xx para a Hotmart sem necessidade.

### 4.2 Kiwify
- **Eventos (nomes oficiais em PT):** `compra_aprovada`, `compra_reembolsada`, `chargeback`, eventos de boleto/Pix, carrinho abandonado e assinatura **[confirmado]**.
  - Nomes como `order_approved` vêm do Zapier.
- **Autenticidade:** o webhook tem `token` na configuração **[confirmado]**, mas não está claro como ele chega na requisição **[a confirmar]**.
  - A API "banking" da Kiwify assina com Ed25519 (`x-kiwify-digital-signature` + `x-kiwify-timestamp`), mas é **outro produto**.
- **Payload** (`order_id`, tracking etc.): **não documentado publicamente** no que achei. Capturar com webhook de teste.

### 4.3 Shopify
- **Autenticidade:** `X-Shopify-Hmac-Sha256` = HMAC-SHA256 em base64 do **corpo bruto** com o client secret do app **[confirmado]**. Ler os bytes antes de qualquer parse de JSON.
- **Duplicatas:** podem acontecer. Deduplicar a **entrega** por `X-Shopify-Webhook-Id` **[confirmado]**.
  - **Não** deduplicar por `X-Shopify-Event-Id`: entregas distintas podem compartilhar esse id.
- **Atenção:** `orders/paid` **dispara de novo em pagamentos recorrentes** do mesmo pedido. Deduplicar só por `order_id` descartaria vendas legítimas.
- **Prazo de resposta:** ~5 s.

### 4.4 Nuvemshop (fase 2)
- **Autenticidade:** `x-linkedstore-hmac-sha256` = HMAC-SHA256 **hex** do corpo com o secret do app **[confirmado]**.
- **Payload magro:** só `store_id`, `event` e `id`. É preciso chamar a API da loja para pegar o pedido, o que exige o app instalado + token por loja. Isso muda o fluxo: recebimento → busca → normalização.

### 4.5 CRMs (fase 2/3)
RD Station CRM, Pipedrive, HubSpot e Kommo têm webhooks de mudança de negócio/etapa.
O caso de uso é outro: **lead qualificado / venda fechada offline** → Meta
(`action_source: system_generated`) e Google (EC for leads). Aqui o click id
(`gclid`/`fbclid`) precisa ter sido gravado no lead lá na captura, o que é um
requisito de setup para o cliente.

## 5. Arquitetura e banco

### 5.1 Idempotência em duas camadas

**Entrada** (não processar o mesmo webhook duas vezes): chave
`(source, source_event_key)` com UNIQUE. O `source_event_key` depende da origem:

| Origem | `source_event_key` |
|---|---|
| Shopify | `X-Shopify-Webhook-Id` |
| Hotmart | id do envelope, se existir; senão `transaction + ":" + event` **[a confirmar]** |
| Kiwify | a definir após capturar o payload (provável `order_id + ":" + evento`) |

Inserção com `INSERT ... ON CONFLICT (source, source_event_key) DO NOTHING`.
Se não inseriu, é duplicata: responder 200 e não fazer nada.

**Saída** (o destino não contar a conversão duas vezes): `event_id`/`transactionId`
determinísticos, por exemplo `hotmart:HP17715690036014:purchase`. Mesmo que
algo escape da camada de entrada, Meta (janela de 7 dias) e Google deduplicam.

> Ponto do CLAUDE.md confirmado e refinado: a deduplicação é pelo id na
> origem, mas o UNIQUE precisa incluir a `source`, porque ids de plataformas
> diferentes podem colidir. Na Shopify, o id certo é o da *entrega*, não o do pedido.

### 5.2 Retry: outbox por destino

Um evento recebido gera N entregas (uma por destino configurado). Cada entrega
tem estado próprio, porque a Meta pode aceitar enquanto o Google falha.

- O worker agendado busca `PENDING` com `next_attempt_at <= now()` usando `FOR UPDATE SKIP LOCKED`, o que permite várias instâncias sem fila externa.
- Backoff exponencial com jitter (ex.: 1 min, 5 min, 30 min, 2 h, 12 h).
- **Prazo por destino:** Meta precisa de `event_time` < 7 dias, então parar antes e marcar `EXPIRED`. Google tem janela maior.
- Erro 4xx de validação → `FAILED` direto, sem retry. 5xx/timeout/429 → retry.
- Lote da Meta: só agrupar eventos do mesmo pixel e longe do limite de 7 dias.

### 5.3 Esboço da V1 (para discutir, ainda não é a migração final)

```sql
CREATE TABLE inbound_event (
    id                BIGSERIAL PRIMARY KEY,
    source            VARCHAR(32)  NOT NULL,   -- HOTMART, SHOPIFY, KIWIFY...
    source_event_key  VARCHAR(255) NOT NULL,   -- id de entrega/evento na origem
    event_type        VARCHAR(64)  NOT NULL,   -- PURCHASE, REFUND, LEAD...
    source_order_id   VARCHAR(255),            -- transação/pedido (busca e suporte)
    occurred_at       TIMESTAMPTZ  NOT NULL,   -- quando aconteceu na origem
    received_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    payload           JSONB        NOT NULL,   -- bruto, para reprocessar (retenção LGPD!)
    CONSTRAINT uk_inbound_event_source_key UNIQUE (source, source_event_key)
);

CREATE TABLE delivery (
    id                BIGSERIAL PRIMARY KEY,
    inbound_event_id  BIGINT       NOT NULL REFERENCES inbound_event(id),
    destination       VARCHAR(32)  NOT NULL,   -- META_CAPI, GOOGLE_DATA_MANAGER
    external_event_id VARCHAR(255) NOT NULL,   -- event_id / transactionId enviado
    status            VARCHAR(16)  NOT NULL,   -- PENDING, SENT, FAILED, EXPIRED
    attempts          INT          NOT NULL DEFAULT 0,
    next_attempt_at   TIMESTAMPTZ,
    last_error        TEXT,
    sent_at           TIMESTAMPTZ,
    CONSTRAINT uk_delivery_event_destination UNIQUE (inbound_event_id, destination)
);

CREATE INDEX ix_delivery_pending ON delivery (next_attempt_at) WHERE status = 'PENDING';
```

Pontos em aberto para a V1:
- Guardar o payload bruto com PII é útil para reprocessar, mas pesa na LGPD. Alternativas: job de expurgo (ex.: 30 dias) ou guardar só o canônico com hash.
- Multi-cliente: o hub vai atender vários clientes (pixel/conta por cliente). Uma tabela `tenant`/`connection` provavelmente entra já na V1 ou na V2, com `tenant_id` também no UNIQUE.
- Guardar o request/response de cada tentativa (tabela `delivery_attempt`) ajuda no suporte e nos prints do README.

## 6. Próximos passos sugeridos

1. Capturar **webhooks reais de teste** (Hotmart sandbox, loja dev da Shopify, Kiwify) e salvar como fixtures em `src/test/resources`. Isso resolve a maioria dos **[a confirmar]**.
2. Ler na doc oficial: a seção "normalize and hash" da Meta e a referência de `events.ingest` (limites, erros, auth).
3. Fechar o modelo multi-cliente e a retenção de PII, e então escrever a `V1__`.
4. Atualizar o README/plano: trocar "Google Ads Offline Conversions" por "Google Data Manager API".

## Fontes

**Meta**
- [Customer Information Parameters](https://developers.facebook.com/docs/marketing-api/conversions-api/parameters/customer-information-parameters)
- [Server Event Parameters](https://developers.facebook.com/docs/marketing-api/conversions-api/parameters/server-event)
- [Handling Duplicate Pixel and Conversions API Events](https://developers.facebook.com/docs/marketing-api/conversions-api/deduplicate-pixel-and-server-events)
- [Using the API (limites, 7 dias)](https://developers.facebook.com/docs/marketing-api/conversions-api/using-the-api)
- [Payload Helper](https://developers.facebook.com/documentation/ads-commerce/conversions-api/payload-helper)
- [Best Practices](https://developers.facebook.com/docs/marketing-api/conversions-api/best-practices)
- [Conversion Leads payload spec](https://developers.facebook.com/documentation/ads-commerce/conversions-api/conversion-leads-integration/payload-specification)

**Google**
- [Changes to Offline Click Conversion Import Support (blog, mai/2026)](http://ads-developers.googleblog.com/2026/05/changes-to-offline-click-conversion.html)
- [Upgrade from the Google Ads API — Data Manager API](https://developers.google.com/data-manager/api/devguides/events/google-ads/offline/upgrade)
- [events.ingest — referência](https://developers.google.com/data-manager/api/reference/rest/v1/events/ingest)
- [Data Manager API — release notes](https://developers.google.com/data-manager/api/reference)
- [About offline conversion imports — Google Ads Help](https://support.google.com/google-ads/answer/2998031?hl=en)
- [Search Engine Land: Google is moving offline conversion imports out of the Google Ads API](https://searchengineland.com/google-is-moving-offline-conversion-imports-out-of-the-google-ads-api-477669)
- [Attribi: UploadClickConversions vs Data Manager API](https://attribi.com/blog/google-ads-data-manager-api-conversions)

**Origens**
- [Hotmart — Purchase webhook 2.0](https://developers.hotmart.com/docs/en/2.0.0/webhook/purchase-webhook/)
- [Hotmart — Webhook (Postback), central de ajuda](https://help.hotmart.com/pt-br/article/como-configurar-sua-api-atraves-do-webhook-postback-/360001491352)
- [Kiwify API — Criar webhook](https://docs.kiwify.com.br/api-reference/webhooks/create)
- [Kiwify banking — webhook headers](https://docs.kiwify.com.br/en/api-reference/banking/webhook-headers)
- [Shopify — Verify deliveries](https://shopify.dev/docs/apps/build/webhooks/verify-deliveries.md)
- [Shopify — Ignore duplicates](https://shopify.dev/apps/build/webhooks/ignore-duplicates)
- [Hookdeck — duplicate Shopify webhook events](https://hookdeck.com/webhooks/platforms/how-to-handle-duplicate-shopify-webhook-events.md)
- [Nuvemshop/Tiendanube — Webhook](https://tiendanube.github.io/api-documentation/resources/webhook)

**Mercado**
- [Comparativo de plataformas de e-commerce 2026 (dados Store Leads)](https://vitrinecreative.com/blog/ecommerce-platform-comparison-brazil-2026/)
- [As plataformas de e-commerce mais usadas em 2026](https://ecommercenapratica.com/blog/plataformas-de-ecommerce/)
- [Hotmart, Kiwify ou Eduzz: comparativo 2026](https://www.freelasemcrise.com.br/blog/hotmart-kiwify-eduzz-comparativo)
- [Hotmart vs Kiwify vs Eduzz](https://www.crazystack.com.br/2026/hotmart-vs-kiwify-vs-eduzz-comparativo)
- [Melhor CRM para pequenas empresas (SocialHub)](https://www.socialhub.pro/blog/melhor-crm-para-pequenas-empresas/)
