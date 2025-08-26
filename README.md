# Sphuta TMS — Freelancer (Community Edition)

Time & invoicing made simple for solo builders. This repo contains the Freelancer edition of Sphuta TMS: database schema, seed data, and API usage guides (cURL cheat-sheets).

> What’s inside
> - PostgreSQL schema (plain DDL, no triggers) aligned to the current UI and cURLs  
> - Seed data for quick demos  
> - Docs & examples for Clients, Projects, Timesheets, Invoices, Profile/Settings, Help

---

## Features

- Clients: rich contact fields, structured address, per-client preferences, active/archive
- Projects: code, hourly rate, start/end dates, description, active/archive
- Time: weekly timesheets and daily entries (hours or start/end), edits allowed after “submit”
- Invoices: from time entries *or* manual lines (rate × qty), taxes/discounts, payments, status flow
- Settings & Profile: invoicing, time-tracking, branding, logo upload URL, profile basics
- Support: simple local ticket/messages tables (optional)
- Recurrence: storage for recurring invoices (optional)

---

## Repo Layout

```

/db
├─ sphuta\_tms\_freelancer\_schema\_v5.sql   # full schema (PostgreSQL)
└─ sphuta\_tms\_freelancer\_seed\_v5.sql     # demo data (run after schema)
/docs
├─ clients\_curl.md
├─ projects\_curl.md
├─ timesheets\_curl.md
└─ invoices\_curl.md

````

*(If you keep everything in one repo root, ensure the filenames match the ones above.)*

---

## Quick Start

### 1) Requirements
- PostgreSQL 13+ (with `pgcrypto` extension)
- A running API service that exposes endpoints under `http://localhost:8080/api/v1`  
  (Any stack is fine; sample cURL assumes a Bearer token.)

### 2) Create database & load schema

```bash
createdb sphuta_tms_dev
psql -d sphuta_tms_dev -c "CREATE EXTENSION IF NOT EXISTS pgcrypto;"
psql -d sphuta_tms_dev -f db/sphuta_tms_freelancer_schema_v5.sql
````

### 3) (Optional) Load seed data

```bash
psql -d sphuta_tms_dev -f db/sphuta_tms_freelancer_seed_v5.sql
```

### 4) Configure your API (example env)

```bash
export PGHOST=localhost
export PGPORT=5432
export PGDATABASE=sphuta_tms_dev
export PGUSER=<your_pg_user>
export PGPASSWORD=<your_pg_password>
# If your app uses DATABASE_URL:
# export DATABASE_URL="postgres://<user>:<pass>@localhost:5432/sphuta_tms_dev"
```

Start your API, then smoke-test with one of the endpoints (replace `<TOKEN>`):

```bash
curl -H "Authorization: Bearer <TOKEN>" \
  http://localhost:8080/api/v1/clients?active=true&page=0&size=10
```

---

## Data Model (high level)

**Schemas**

* `sphuta_freelancer` (only)

**Core tables**

* `clients` — person/org, phones, address, preferences, `is_active`
* `projects` — `client_id`, `name` (UI: `projectName`), `code`, `hourly_rate`, `start_date`, `end_date`, `description`, `is_active`
* `timesheets` — `project_id`, `period_start`, `period_end`, `status`
* `time_entries` — `timesheet_id`, `entry_date`, `description`, `hours` (or `start_time`/`end_time`), `rate_at_entry`, `cost_at_entry`
* `invoices` — header + `reference`, `subtotal`, `discount_amount`, `tax_amount`, `total_amount`, `status`, `accept_online_payments`
* `invoice_line_items` — supports **time-entry** lines *and* **manual** lines (`time_entry_id` nullable; `hours`=qty, `unit_rate`=rate)
* `invoice_payments` — partial/full payments
* `app_settings` — Profile + Settings (invoicing/time-tracking/branding)
* *(optional)* `support_tickets`, `support_ticket_messages`, `invoice_recurrences`

**Enums**

* `timesheet_status_enum`: `DRAFT`, `APPROVED`, `LOCKED`
* `invoice_status_enum`: `DRAFT`, `SENT`, `PARTIAL`, `PAID`, `VOID`

**Views**

* `v_invoice_amounts` — derives `amount_paid` & `amount_due` from payments

---

## API Usage (cheat-sheets)

See `/docs/*.md` or use these highlights:

* **Clients**

  * `GET /clients?active=true|false&search=&page=&size=`
  * `POST /clients` (create), `PATCH /clients/{id}`, `POST /clients/{id}/archive|unarchive`

* **Projects**

  * Owner dropdown = `GET /clients?active=true`
  * `POST /projects` with `projectName`, `code`, `hourlyRate`, `startDate`, `endDate`, `description`, `isActive`
  * `PATCH /projects/{id}`, `POST /projects/{id}/archive|unarchive`

* **Timesheets**

  * `POST /timesheets` (create week)
  * `GET /timesheets/{id}` (week grid), `POST /time-entries` (cell add), `PUT /timesheets/{id}/entries` (bulk upsert)
  * **Freelancer Submit**: `POST /timesheets/{id}/submit` ⇒ sets `APPROVED` (edits allowed unless `LOCKED`)

* **Invoices**

  * Wizard (approved, uninvoiced entries): `GET /time-entries/uninvoiced?...` → `POST /invoices` (`timeEntryIds`)
  * Manual lines: `POST /invoices` with `manualLines: [{description, qty, rate}]`
  * Edit/send/pay: `PATCH /invoices/{id}`, `POST /invoices/{id}/send`, `POST /invoices/{id}/payments`

---

## Contributing

1. Open an issue with the change proposal and context (UI/cURL mapping if applicable).
2. For schema changes:

   * Keep the **Freelancer** edition self-contained (no Business schema).
   * Prefer additive changes (nullable columns) to avoid data loss.
   * Update the seed and cURL docs if behavior changes.
3. Submit a PR with:

   * SQL changes under `/db`
   * Updated docs under `/docs`
   * A brief migration note in the PR description

---

## License

MIT License

Copyright (c) 2025 POMMALA LLC

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.


## Notes & Tips

* Dates use ISO `YYYY-MM-DD`. If the UI shows `dd-MM-yyyy`, convert at the API layer.
* Invoice totals should be calculated server-side:

  * `subtotal = Σ(line.hours * line.unit_rate)`
  * `total = subtotal - discount_amount + tax_amount`
  * `amount_due = total - Σ(payments.amount)` (see `v_invoice_amounts`)
* **Manual lines** work because `invoice_line_items.time_entry_id` is **nullable** and has a partial unique index for non-null values.


If you want, I can also drop this into a `README.md` in a canvas file so you can tweak and export directly.
```
