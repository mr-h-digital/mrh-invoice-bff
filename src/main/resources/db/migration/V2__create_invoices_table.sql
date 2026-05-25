CREATE TABLE invoices (
    id VARCHAR(36) PRIMARY KEY,
    invoice_number VARCHAR(20) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    client_id VARCHAR(36) REFERENCES clients(id) ON DELETE SET NULL,
    -- client snapshot fields
    snapshot_company_name VARCHAR(255),
    snapshot_contact_name VARCHAR(255),
    snapshot_email VARCHAR(255),
    snapshot_phone VARCHAR(50),
    snapshot_address TEXT,
    -- totals
    subtotal NUMERIC(12,2) NOT NULL DEFAULT 0,
    discount_type VARCHAR(10),
    discount_value NUMERIC(12,2) NOT NULL DEFAULT 0,
    discount_amount NUMERIC(12,2) NOT NULL DEFAULT 0,
    vat_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    vat_rate NUMERIC(5,4) NOT NULL DEFAULT 0.15,
    vat_amount NUMERIC(12,2) NOT NULL DEFAULT 0,
    total NUMERIC(12,2) NOT NULL DEFAULT 0,
    notes TEXT,
    -- payment details
    payment_bank VARCHAR(100),
    payment_account_name VARCHAR(100),
    payment_account_number VARCHAR(50),
    payment_account_type VARCHAR(50),
    payment_branch_code VARCHAR(20),
    payment_reference VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_invoices_status ON invoices(status);
CREATE INDEX idx_invoices_client_id ON invoices(client_id);
CREATE INDEX idx_invoices_due_date ON invoices(due_date);
CREATE INDEX idx_invoices_invoice_number ON invoices(invoice_number);
