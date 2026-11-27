# Decisões — 26 e 27/11/2026

> Registro das conversas de planejamento. Fica só neste repositório privado:
> não vai para o repositório público do portfólio.

## Produto e oferta
- **Posicionamento:** você é o dev técnico do gestor de tráfego. O "nicho" é o gestor, não o segmento do cliente final. A página mostra exemplos por situação, não por setor.
- **Não competir** no caso simples (Hotmart → Meta para infoprodutor pequeno). Utmify, Stape e similares ganham por preço. Ver `estudo-mercado.md`.
- **Três ofertas:**
  1. **Diagnóstico:** porta de entrada.
  2. **Integração sob medida:** oferta principal. Venda fechada fora do site (CRM, WhatsApp, ERP) voltando para a Meta e o Google.
  3. **Monitoramento mensal:** renda recorrente.
- **Preços:** continuam como `[VALOR]` até as conversas com gestores validarem as faixas.
- **Escala:** serviço, não SaaS. Uma instalação atende vários clientes por configuração. Alertas, retry e relatório automáticos reduzem a manutenção por cliente.

## conversion-hub (MVP enxuto)
- **Origem:** só Hotmart. **Destino:** só Meta CAPI. **Cliente:** um só, com configuração por variável de ambiente.
- **Banco:** uma tabela de eventos com status e tentativas na própria linha.
- **Retry:** `@Scheduled`, até ~5 tentativas, respeitando o limite de 7 dias da Meta.
- **Depois do MVP, nesta ordem:** alertas → relatório mensal → multi-cliente → um CRM como 2ª origem (Pipedrive ou RD Station) → Google (Data Manager API) → Shopify.
- O desenho completo, para as fases seguintes, está em `estudo-integracoes.md`.

## Site e portfólio
- **jrweb.com.br continua portfólio pessoal de carreira.** O conversion-hub entra lá só como projeto técnico, sem preço nem chamada comercial.
- **O serviço ganha site próprio**, com marca e domínio novos (nome ainda a definir). Hospedagem na Vercel.
- **Layout escolhido:** A · claro, minimalista. Está em `docs/design/pagina-servico-a.html`, e o canvas com as 3 opções em https://claude.ai/artifact/3VJKvf2vb2PT3rQTvCcHqG.
  - "jrweb" no layout é provisório.

## Repositório
- Este repositório fica **privado**, como rascunho.
- Na hora de publicar o portfólio, criar um **repositório novo e limpo** com código, README e `estudo-integracoes.md`.
- **Não vão para o público:** `plano-negocio.md`, `estudo-mercado.md`, este arquivo e `docs/design/`.

## Cuidados
- Conferir o contrato CLT (exclusividade, não concorrência, propriedade intelectual) antes de anunciar o serviço publicamente.
- Não pegar cliente que use o ERP do empregador sem checar antes.
- Nunca usar horário, equipamento ou código da empresa.

## Pendências
- [ ] Conversar com 2 ou 3 gestores (roteiro na seção 7 de `estudo-mercado.md`)
- [ ] Escolher nome da marca e registrar o domínio
- [ ] Migração `V1__` do MVP
- [ ] Capturar webhook real da Hotmart como fixture de teste
