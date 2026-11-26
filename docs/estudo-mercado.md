# Estudo de mercado — rastreamento server-side e integrações de conversão

> Pesquisa feita em nov/2026 só com fontes públicas da internet. A maior parte
> vem de blogs de fornecedores (que têm interesse comercial) e não existe dado
> auditado sobre o tamanho deste nicho. Trate os números como **indicativos**.
> A validação de verdade são as conversas com gestores de tráfego (seção 7).

## 1. Conclusão em uma página

| Pergunta | Resposta curta |
|---|---|
| Existe dinheiro no setor? | Sim. Publicidade digital no Brasil: **R$ 42,7 bi em 2025** (+12,7%). |
| Existe demanda pelo serviço? | Sim. Há projetos de freela pedindo exatamente "CAPI + server-side + Hotmart" e vários freelancers oferecendo isso. |
| O caso simples está ocupado? | **Sim, bastante.** Utmify (com plano grátis), WeTrackr, Stape (US$ 10/pixel) e integrações nativas de CRMs/chatbots já cobrem "checkout → Meta". |
| E o cliente típico do gestor? | É pequeno: **~65% dos clientes investem até R$ 5 mil/mês** em anúncio. Isso limita quanto ele paga. |
| Onde sobra espaço? | Setup e diagnóstico (fazer a ferramenta funcionar direito) e **integração custom** em empresas médias: venda fechada fora do site (CRM, WhatsApp, ERP), alto ticket, B2B. |

**Leitura geral:** o mercado existe, mas ninguém compra "um hub em Java". O
gestor e o cliente compram **o resultado**: a venda aparecendo na Meta e no
Google, e a campanha otimizando melhor. No caso comum, o mais barato é
configurar uma ferramenta pronta. O código próprio se paga onde a ferramenta
não chega. O conversion-hub continua útil como **prova técnica** no portfólio
e como base reaproveitável para esses casos custom.

## 2. Tamanho do mercado

- **Publicidade digital no Brasil:** R$ 42,7 bi em 2025, contra R$ 37,9 bi em 2024. Fonte: IAB Brasil + Kantar IBOPE Media, *Digital AdSpend 2026*.
  - A maior parte vai para as grandes plataformas (Google, Meta). A busca do Google sozinha teria ~26% dos aportes. A fatia exata de cada uma não estava nos resultados.
  - Comércio responde por ~25% do investimento.
- **Gestores de tráfego:** não há contagem oficial. Sinais encontrados:
  - **81%** trabalham sozinhos ou em equipes de até 3 pessoas (pesquisa Reportei, via Carta Capital).
  - **~65%** dos clientes desses profissionais investem **até R$ 5 mil/mês** em anúncios (mesma pesquisa).
  - **66%** já usam automação/integração (n8n, Make, Zapier). Há familiaridade com o tema, mas eles resolvem sozinhos o que é simples.
  - A mensuração aparece como **gargalo recorrente** do setor (mesma reportagem).
  - Há ~700 vagas de "gestor de tráfego" abertas no LinkedIn, e o CAGED registra ~8,3 mil admissões/desligamentos CLT em 12 meses. Isso não conta os autônomos, que são a maioria.

**Implicação:** o público é grande e fragmentado (muitos gestores solo), mas a
maioria dos clientes finais é pequena. O cliente que interessa está no topo:
quem investe dezenas de milhares por mês, onde 1% de melhora na otimização vale mais
que o serviço.

## 3. Concorrência

### 3.1 Ferramentas prontas (SaaS)

| Ferramenta | Foco | Preço encontrado |
|---|---|---|
| **Utmify** | Infoprodutos; ~130 integrações (Hotmart, Kiwify...); pixel que reenvia eventos | Grátis até 30 vendas/mês; R$ 119,90 (1.000 vendas); R$ 249,90 (2.500); R$ 399,90 (5.000); planos Scale até R$ 5.399,90 |
| **WeTrackr** | Tracker "operação completa", antifraude, WhatsApp | A partir de R$ 155,90/mês (até 3.000 vendas) |
| **Decisa.app** | Atribuição multicanal, server-side | Sem preço público |
| **Stape** | Hospedagem de GTM server-side + gateways (Meta, Google, TikTok) | A partir de ~US$ 8–10/mês por pixel/domínio |
| **Tintim** | Atribuição de vendas no WhatsApp (sem server-side) | — |

### 3.2 Integrações nativas que também competem

- **CRMs e chatbots de WhatsApp** já enviam eventos para a CAPI sozinhos: Kommo (API de Conversão do WhatsApp), respond.io, SleekFlow, SendPulse, Wati.
  - O caso "venda fechada no WhatsApp → Meta" já tem solução de prateleira **quando o cliente usa um desses CRMs com a API oficial do WhatsApp**.
- As próprias plataformas (Shopify, Hotmart etc.) oferecem integração com Pixel/CAPI em graus variados. Conferir caso a caso.

### 3.3 Prestadores de serviço

| Perfil | Preço encontrado |
|---|---|
| Freelancer de rastreamento (pagamento único, planos Essencial/Profissional/Completo) | A partir de ~R$ 390, 7–15 dias úteis |
| Agência B2B de tracking server-side | R$ 5 mil a R$ 30 mil de implementação + a partir de R$ 1,5 mil/mês de operação |
| Freelancers no 99Freelas/Workana | Muitos anunciam o serviço (um cita 90+ projetos entregues); sem preço público |

**Implicação:** existe uma ponta barata e lotada (setup de ferramenta, centenas
de reais) e uma ponta cara e menos disputada (projeto com escopo, milhares de
reais + mensalidade). A diferença entre as duas não é a tecnologia, é **o porte
do cliente e a complexidade do caso**.

## 4. Sinais de demanda

Exemplos reais de pedidos em plataformas de freela:

- **99Freelas:** reconstruir o rastreamento de infoproduto na **Hotmart** com GTM server-side via Stape, Pixel e CAPI.
  - Meta: **Event Match Quality acima de 7/10**, com eventos testados e **deduplicados entre web e servidor**.
  - É exatamente o assunto do `estudo-integracoes.md`.
- **Workana:** configurar Stape para enviar à CAPI eventos de *postback* com event ID, valor, data/hora e click ID, ou seja, integrar uma origem que manda webhook.
- **Dor recorrente:** venda fechada fora do site (WhatsApp, transferência, loja física) que não volta para a Meta. Sem CAPI, a campanha de clique para WhatsApp otimiza para "quem manda mensagem", não para "quem compra". Vários fornecedores escrevem sobre isso, o que indica que a dor é comum.

O que **não** encontrei: orçamento médio desses projetos, volume de pedidos por
mês e taxa de churn de clientes de manutenção. Isso só sai das conversas.

## 5. Onde um dev backend se diferencia

Comparando com as ferramentas e os freelancers de GTM:

1. **Setup + diagnóstico bem feito:** a ferramenta existe, mas o gestor não sabe deixá-la com EMQ alto, deduplicação certa e eventos completos. É a porta de entrada barata, que gera relacionamento.
2. **Origem sem conector pronto:** ERP próprio, sistema interno, checkout pouco comum, CRM sem integração nativa com a CAPI, planilha/banco do cliente. Aqui o código próprio (o conversion-hub) se paga.
3. **Funil longo / alto ticket / B2B:** lead → qualificação → venda dias depois. Pede guardar o click id na captura, ligar ao CRM e mandar a etapa certa para a Meta e o Google. As ferramentas de infoproduto não são feitas para isso.
4. **Google pela Data Manager API:** quem tem integração própria com a Google Ads API precisa migrar (ver `estudo-integracoes.md`, seção 3.2). É uma demanda técnica com prazo.
5. **Dados e relatório:** juntar gasto + vendas de vários canais (inclusive Mercado Livre) num banco e num relatório automático. Quase nenhuma ferramenta de infoproduto faz isso para e-commerce multicanal.

**Onde não competir:** "integrar Hotmart com Meta" para um infoprodutor pequeno.
A Utmify grátis ou a R$ 119,90 ganha.

## 6. Riscos

- **Comoditização:** ferramentas e integrações nativas avançam todo ano. O que é custom hoje pode virar checkbox amanhã.
- **Cliente pequeno:** a maioria investe pouco em anúncio, então a mensalidade precisa caber nessa conta, ou o foco fica nos maiores.
- **Dependência do gestor:** no modelo de parceria, o cliente é do gestor. É preciso combinar quem cobra o quê e quem dá o suporte.
- **Plataformas mudam:** a migração do Google mostra que as APIs mudam com pouco aviso. Isso gera trabalho (bom para manutenção), mas também risco de quebra.
- **LGPD:** quem opera os dados de compradores assume responsabilidade. Precisa de contrato com o cliente definindo os papéis (controlador/operador).

## 7. O que validar nas conversas com gestores

Perguntas sugeridas para as primeiras conversas (descoberta, sem vender):

1. Quantos clientes você tem e quanto cada um investe por mês, mais ou menos?
2. Como mede as vendas hoje? Usa Utmify, Stape ou outra ferramenta? Está satisfeito?
3. Algum cliente vende **fora do site** (WhatsApp, vendedor, loja)? Como essa venda volta para a Meta e o Google?
4. Já perdeu cliente ou verba por "a Meta não mostra as vendas"?
5. Algum cliente tem sistema próprio, ERP ou CRM sem integração pronta?
6. Quando precisou de um dev para isso, quanto pagou ou quanto pagaria?
7. Preferiria pagar um valor único ou uma mensalidade com monitoramento?

Sinal verde: **2 ou mais gestores** com cliente nos casos 2, 3 ou 4 da seção 5 e
disposição de pagar valor de projeto. Se todos responderem "uso a Utmify e está
ótimo", o foco muda para setup e diagnóstico, ou para relatórios.

## Fontes

**Mercado e setor**
- [Exame — Publicidade digital no Brasil cresce 12,7% e atinge R$ 42,7 bi em 2025 (IAB Brasil)](https://exame.com/marketing/publicidade-digital-no-brasil-cresce-127-e-atinge-r-427-bi-em-2025-diz-iab-brasil/)
- [Meio & Mensagem — IAB: publicidade digital atinge R$ 42,7 bilhões em 2025](https://www.meioemensagem.com.br/midia/iab-publicidade-digital-atinge-r-427-bilhoes-em-2025)
- [Conversion — IAB Brasil publicidade digital 42 bilhões](https://www.conversion.com.br/blog/iab-brasil-publicidade-digital-42-bilhoes-2025)
- [Seu Dinheiro — R$ 42,7 bilhões em publicidade digital em 2025](https://www.seudinheiro.com/2026/seu-negocio/presenca-no-youtube-google-e-sites-empresas-investiram-r-427-bilhoes-em-publicidade-digital-em-2025-um-unico-setor-foi-responsavel-por-um-quarto-da-cifra-giov/)
- [Carta Capital — Tráfego pago no Brasil é negócio de pequeno porte (pesquisa Reportei)](https://www.cartacapital.com.br/do-micro-ao-macro/trafego-pago-brasil-pequenos-anunciantes-meta-google/)
- [Relatório do Gestor de Tráfego no Brasil 2026 — Tintim](https://conteudo.tintim.app/)
- [Salário.com.br — Gerente de Tráfego (CAGED)](https://www.salario.com.br/profissao/gerente-de-trafego-cbo-141605/)
- [LinkedIn — Vagas de Gestor de Tráfego](https://br.linkedin.com/jobs/gestor-de-tr%C3%A1fego-vagas)

**Concorrência**
- [Utmify — Preços](https://www.utmify.com.br/precos/)
- [Escalafy — Utmify preços 2026](https://www.escalafy.com/pt/blog/utmify-precos)
- [WeTrackr — Concorrentes do Utmify](https://www.wetrackr.com.br/concorrentes-utmify)
- [UTM Pixel — UTMify vs Decisa.app](https://utmpixel.com/comparativos/utmify-vs-decisa)
- [Nação Digital — Ferramentas de trackeamento: 7 opções](https://nacao.digital/blog/ferramentas-de-trackeamento/)
- [MyMetric — Alternativa ao UTMify para e-commerce](https://mymetric.com.br/blog/alternativa-utmify/)
- [Stape — Gateways](https://stape.io/pt/gateways)
- [Stape — Meta server-side setup options](https://stape.io/blog/meta-conversions-api-gateway-versus-conversion-api)
- [Kommo — API de Conversão do WhatsApp](https://www.kommo.com/br/whatsapp/api-de-conversao-do-whatsapp/)
- [respond.io — Enviar evento para a API de Conversões](https://respond.io/pt/help/workflows/step-send-conversions-api-event)
- [SleekFlow — Meta ad performance tracking](https://help.sleekflow.io/pt_BR/meta-ad-performance-tracking-tools-integration)

**Serviços e preços**
- [PEI Digital — Tracking server-side para e-commerce](https://peidigital.com.br/tracking-conversao-para-ecommerce/)
- [Paulo Sabbanelli — Rastreamento server-side e CAPI](https://contrate-me.paulosabbanelli.com.br/rastreamento-server-side/)
- [Cometly — Conversion API setup cost 2026](https://www.cometly.com/post/conversion-api-setup-cost)

**Demanda**
- [99Freelas — Especialista em rastreamento e Meta Ads para Hotmart e WordPress](https://www.99freelas.com.br/project/especialista-em-rastreamento-e-meta-ads-para-hotmart-e-wordpress-697795)
- [Workana — Integração de postback Stape GTM para Meta API](https://www.workana.com/job/desenvolvimento-de-integracao-de-postback-stape-gtm-para-meta-api)
- [Clint — Erros que encarecem anúncios Click to WhatsApp](https://www.clint.digital/blog/?p=3691)
- [Chatfuel — Why WhatsApp attribution tools keep failing](https://chatfuel.com/blog/why-whatsapp-attribution-tools-fail)
- [WhatsApp Business — Conversions API for messaging](https://whatsappbusiness.com/pt-br/blog/conversions-api-messaging/)
