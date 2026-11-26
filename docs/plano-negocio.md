# Plano: renda extra com Ad Tech

> PRIVADO. Tirar este arquivo antes de deixar o repositório público (semana 3).

## Objetivo
Renda extra como dev especializado em integrações técnicas de tráfego pago
(Meta Ads e Google Ads), para gestores de tráfego, agências pequenas e e-commerces.

## Perfil
Dev backend Java/Spring Boot, integrações entre sistemas, APIs REST, PostgreSQL.
Portfólio: jrweb.com.br.

## Serviços
1. Rastreamento server-side: Meta Conversions API (CAPI), Google Ads Enhanced/Offline
   Conversions, deduplicação (event_id), hash SHA-256 dos dados do usuário.
2. Integração CRM/checkout → plataformas de anúncio (RD Station, Pipedrive, HubSpot,
   Hotmart, Shopify), devolvendo vendas/leads qualificados para otimizar o leilão.
3. Dashboards: Google Ads API e Meta Marketing API em PostgreSQL + relatório automático.
4. Automações: alerta de orçamento, pausa de campanha ruim, upload de públicos.
5. Setup GTM + GA4 (porta de entrada).

## Preço (estimativa inicial)
| Serviço | Valor |
|---|---|
| Setup simples (GTM + CAPI via gateway) | R$ 300–800 |
| Integração custom | R$ 1.500–5.000 por projeto |
| Manutenção + dashboard | R$ 300–1.000/mês por cliente |

Meta: R$ 3–6 mil/mês após ~6 meses de carteira.

## Projeto de portfólio: conversion-hub
- Endpoint que recebe webhook de compra/lead
- Normaliza e faz hash dos dados
- Envia para Meta CAPI e Google Ads Offline Conversions
- Log em PostgreSQL + retry em falhas
- Validação no Test Events do Meta Events Manager

## Plano de 4 semanas
| Semana | O quê |
|---|---|
| S1 | Meta Business + Pixel de teste, conta Google Ads; estudar CAPI; GTM + GA4 no jrweb. **Ajuste:** já conversar com 2 gestores de tráfego (descoberta, sem vender). |
| S2 | Desenvolver o conversion-hub |
| S3 | README com diagrama e prints, página de serviço no jrweb, post no LinkedIn. Repositório vira público. |
| S4 | Prospectar gestores/agências; 1º projeto com desconto em troca de depoimento; pacotes Básico e Custom + Manutenção |

## Estratégia
Parceria com 2–3 gestores de tráfego: eles têm os clientes, eu sou o "dev de confiança".
Não competir por preço com setup barato de Fiverr; focar em integração custom de backend.
