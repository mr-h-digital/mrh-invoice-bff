<p align="center">
	<img src="assets/logo/mrhdigital_logo_green.png" alt="Mr. H Digital Logo" width="120" />
</p>

<h1 align="center">Mr. H Digital — Invoice Service</h1>

<p align="center">
	<strong>Production-ready Spring Boot 3 BFF API for the Mr. H Digital Invoice Generator.</strong>
</p>

<p align="center">
	<img src="https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white" alt="Java 21" />
	<img src="https://img.shields.io/badge/Spring%20Boot-3.3.0-brightgreen?logo=springboot&logoColor=white" alt="Spring Boot" />
	<img src="https://img.shields.io/badge/PostgreSQL-16-blue?logo=postgresql&logoColor=white" alt="PostgreSQL" />
	<img src="https://img.shields.io/badge/Flyway-migrations-red?logo=flyway&logoColor=white" alt="Flyway" />
	<img src="https://img.shields.io/badge/license-proprietary-orange" alt="License" />
</p>

---

## Overview

This service is the backend-for-frontend (BFF) API powering the Mr. H Digital Invoice Generator web application. It replaces the localStorage layer in the React frontend with a persistent, production-grade REST API backed by PostgreSQL.

It manages clients, invoices, line items, and dashboard statistics — with all financial totals (subtotal, discount, VAT, total) calculated server-side on every create and update.

## Highlights

- Full client and invoice CRUD with validation and consistent error responses
- Server-side financial calculations: line items → subtotal → discount → VAT → total
- Invoice duplication — creates a new DRAFT with today's date and a new invoice number
- Auto-generated invoice numbers in `INV-YYYY-NNN` format
- Paginated, filterable invoice list (`?status=PAID&search=TVECO&sort=dueDate,desc`)
- Client snapshot embedded in each invoice (survives client edits/deletion)
- Dashboard statistics endpoint with totals, counts, and 5 most recent invoices
- SpringDoc OpenAPI with Swagger UI at `/swagger-ui.html`
- Flyway database migrations with seed data for 3 clients and 1 invoice
- JUnit 5 unit tests (Mockito) + integration tests (Testcontainers)

## Technology

| Layer | Choice |
|-------|--------|
| Language | Java 21 |
| Framework | Spring Boot 3.3 |
| ORM | Spring Data JPA + Hibernate |
| Database | PostgreSQL 16 |
| Migrations | Flyway |
| Docs | SpringDoc OpenAPI (Swagger UI) |
| Build | Maven 3.9 |
| Tests | JUnit 5 + Mockito + Testcontainers |

## Project Structure

```text
mrh-invoice-service/
├── pom.xml
├── docker-compose.yml
├── src/
│   ├── main/
│   │   ├── java/co/za/mrhdigital/invoiceservice/
│   │   │   ├── MrhInvoiceServiceApplication.java
│   │   │   ├── config/          # CORS, OpenAPI, JPA auditing, Jackson
│   │   │   ├── common/          # ApiResponse<T>, PageResponse<T>, AuditableEntity
│   │   │   ├── exception/       # GlobalExceptionHandler, custom exceptions
│   │   │   └── domain/
│   │   │       ├── client/      # Client entity, service, controller, DTOs
│   │   │       ├── invoice/     # Invoice + LineItem entities, service, controller, DTOs
│   │   │       └── dashboard/   # Stats aggregation service + controller
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-local.yml
│   │       ├── application-prod.yml
│   │       └── db/migration/
│   │           ├── V1__create_clients_table.sql
│   │           ├── V2__create_invoices_table.sql
│   │           ├── V3__create_line_items_table.sql
│   │           └── V4__seed_initial_data.sql
│   └── test/
│       └── java/co/za/mrhdigital/invoiceservice/
│           ├── invoice/         # InvoiceServiceTest, InvoiceControllerTest
│           ├── client/          # ClientServiceTest, ClientControllerTest
│           └── dashboard/       # DashboardControllerTest
└── assets/
    └── logo/
```

## Prerequisites

- Java 21 (JDK)
- Docker Desktop
- Maven 3.9+

## Quick Start

### 1. Start the database

```bash
docker-compose up -d
```

### 2. Run the application

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### 3. Open Swagger UI

```
http://localhost:8080/swagger-ui.html
```

### 4. Health check

```
http://localhost:8080/actuator/health
```

## Running Tests

Unit tests (Mockito — no Docker needed):

```bash
mvn test -Dtest="InvoiceServiceTest,ClientServiceTest"
```

Full suite including integration tests (requires Docker or a running PostgreSQL on port 5432):

```bash
mvn test
```

## Connecting the React Frontend

Set this environment variable in your Vite project's `.env.local`:

```
VITE_API_URL=http://localhost:8080/api
```

## API Endpoints

### Clients — `/api/clients`

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/clients` | List all clients (`?search=`) |
| `POST` | `/api/clients` | Create client |
| `GET` | `/api/clients/{id}` | Get client by ID |
| `PUT` | `/api/clients/{id}` | Update client |
| `DELETE` | `/api/clients/{id}` | Delete client (409 if has invoices) |
| `GET` | `/api/clients/{id}/invoices` | List all invoices for a client |

### Invoices — `/api/invoices`

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/invoices` | List invoices (`?status=&search=&page=&size=&sort=`) |
| `POST` | `/api/invoices` | Create invoice |
| `GET` | `/api/invoices/{id}` | Get invoice by ID |
| `PUT` | `/api/invoices/{id}` | Update invoice (recalculates totals) |
| `DELETE` | `/api/invoices/{id}` | Delete invoice |
| `POST` | `/api/invoices/{id}/duplicate` | Duplicate as new DRAFT with today's date |
| `PATCH` | `/api/invoices/{id}/status` | Update status only |
| `GET` | `/api/invoices/next-number` | Next available invoice number (`INV-YYYY-NNN`) |

### Dashboard — `/api/dashboard`

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/dashboard/stats` | Totals, counts, and 5 most recent invoices |

## Response Format

All endpoints return:

```json
{
  "success": true,
  "data": { ... },
  "message": null,
  "timestamp": "2026-05-22T10:00:00Z"
}
```

Errors:

```json
{
  "success": false,
  "data": null,
  "message": "Invoice not found with id: abc-123",
  "timestamp": "2026-05-22T10:00:00Z"
}
```

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_URL` | `jdbc:postgresql://localhost:5432/mrhdigital_invoices` | PostgreSQL JDBC URL |
| `DB_USERNAME` | `postgres` | Database username |
| `DB_PASSWORD` | `postgres` | Database password |
| `PORT` | `8080` | Server port |
| `FRONTEND_URL` | _(empty)_ | Production frontend origin for CORS |

## Seed Data

Flyway migration `V4` seeds:

| Client | Contact | Email |
|--------|---------|-------|
| Timeline Vehicle Export Company (Pty) Ltd | Thabo Seabi | thabo@tveco.co.za |
| R.O.C.K. Mission Ministries | Pastor Chernay Hildebrandt | info@rockmission.co.za |
| K&T Transport | Contact | info@ktransport.co.za |

Plus invoice `INV-2026-001` for TVECO — status `SENT`, 5 line items, R1 200 negotiated discount, total R6 200.

---

<p align="center">
	<strong>Development Signature</strong>
</p>

<p align="center">
	<img src="assets/logo/mrhdigital_logo_green.png" alt="Mr. H Digital Logo" width="120" />
</p>

<p align="center">
	Designed and developed by <a href="https://mrhdigital.co.za" target="_blank" rel="noopener noreferrer"><strong>Mr. H Digital</strong></a>
</p>
