create table if not exists webhook_events (
  id uuid primary key,
  tenant_id text not null,
  event_id text not null,
  payload jsonb not null,
  status text not null,
  attempts int not null default 0,
  last_error text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  processed_at timestamptz null,
  unique (tenant_id, event_id)
);
create index if not exists idx_webhook_events_tenant_event on webhook_events(tenant_id, event_id);
