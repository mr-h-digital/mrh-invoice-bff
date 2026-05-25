-- Seed clients
INSERT INTO clients (id, company_name, contact_name, email, phone, address, created_at, updated_at)
VALUES
(
    'client-tveco-001',
    'Timeline Vehicle Export Company (Pty) Ltd',
    'Thabo Seabi',
    'thabo@tveco.co.za',
    '+27 72 266 3988',
    '7 Blinkblaar St, Zwartkop, Centurion, 0157',
    NOW(),
    NOW()
),
(
    'client-rock-001',
    'R.O.C.K. Mission Ministries',
    'Pastor Chernay Hildebrandt',
    'info@rockmission.co.za',
    NULL,
    'Mitchell''s Plain, Cape Town',
    NOW(),
    NOW()
),
(
    'client-kt-001',
    'K&T Transport',
    'Contact',
    'info@ktransport.co.za',
    NULL,
    'Cape Town',
    NOW(),
    NOW()
);

-- Seed invoice INV-2026-001 for TVECO
INSERT INTO invoices (
    id, invoice_number, status, issue_date, due_date, client_id,
    snapshot_company_name, snapshot_contact_name, snapshot_email, snapshot_phone, snapshot_address,
    subtotal, discount_type, discount_value, discount_amount,
    vat_enabled, vat_rate, vat_amount, total,
    notes,
    payment_bank, payment_account_name, payment_account_number, payment_account_type, payment_branch_code, payment_reference,
    created_at, updated_at
) VALUES (
    'invoice-tveco-001',
    'INV-2026-001',
    'SENT',
    '2026-05-22',
    '2026-06-01',
    'client-tveco-001',
    'Timeline Vehicle Export Company (Pty) Ltd',
    'Thabo Seabi',
    'thabo@tveco.co.za',
    '+27 72 266 3988',
    '7 Blinkblaar St, Zwartkop, Centurion, 0157',
    7400.00,
    'AMOUNT',
    1200.00,
    1200.00,
    FALSE,
    0.15,
    0.00,
    6200.00,
    'Thank you for your business. Please use invoice number as payment reference.',
    'FNB',
    'Mr H Digital',
    '62123456789',
    'Cheque',
    '250655',
    'INV-2026-001',
    NOW(),
    NOW()
);

-- Seed line items for INV-2026-001
INSERT INTO line_items (id, invoice_id, name, description, quantity, unit_price, amount, sort_order, created_at, updated_at)
VALUES
(
    'li-tveco-001-1',
    'invoice-tveco-001',
    'Website Design & Development',
    'Full responsive website design and development using React and Tailwind CSS',
    1,
    3500.00,
    3500.00,
    0,
    NOW(),
    NOW()
),
(
    'li-tveco-001-2',
    'invoice-tveco-001',
    'Logo Design',
    'Professional logo design with 3 revision rounds',
    1,
    1200.00,
    1200.00,
    1,
    NOW(),
    NOW()
),
(
    'li-tveco-001-3',
    'invoice-tveco-001',
    'Domain & Hosting Setup',
    'Domain registration and 12 months hosting configuration',
    1,
    800.00,
    800.00,
    2,
    NOW(),
    NOW()
),
(
    'li-tveco-001-4',
    'invoice-tveco-001',
    'SEO Setup',
    'On-page SEO optimisation and Google Search Console setup',
    1,
    900.00,
    900.00,
    3,
    NOW(),
    NOW()
),
(
    'li-tveco-001-5',
    'invoice-tveco-001',
    'Social Media Integration',
    'Facebook, Instagram and LinkedIn social media integration',
    1,
    1000.00,
    1000.00,
    4,
    NOW(),
    NOW()
);
