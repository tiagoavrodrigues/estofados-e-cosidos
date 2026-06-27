-- Demo data for testing POST /api/manufacturing-orders/{paId}/supply-components
-- Uses EXECUTIVE_FULL_CABIN_KIT (BOM: FRONT_SEAT_BASE_COVER x2, FRONT_BACKREST_COVER x2,
--   REAR_SEAT_BASE_COVER x1, REAR_BACKREST_COVER x1)
-- No manufacturing_order_component records created for the SA orders (intentionally unassigned)

insert into rack (code, name, active, created_at) values
    ('RACK-B1', 'Cut Parts Rack B1', true, now()),
    ('RACK-B2', 'Cut Parts Rack B2', true, now());

insert into customer_order (code, customer_id, status_id, order_date, validated_at, ready_for_production_at, notes, created_at) values
    ('CO-2026-SUPPLY-001',
     (select id from customer where code = 'CUST-AUTOLUX'),
     (select id from customer_order_status where code = 'READY_FOR_PRODUCTION'),
     current_date - 1,
     now() - interval '2 hours',
     now() - interval '1 hour',
     'Executive full cabin kit order for supply-components demo testing.',
     now());

insert into customer_order_line (customer_order_id, part_id, quantity, created_at) values
    ((select id from customer_order where code = 'CO-2026-SUPPLY-001'),
     (select id from part where code = 'EXECUTIVE_FULL_CABIN_KIT'),
     1.000,
     now());

-- PA manufacturing order (FINISHED_PRODUCT, OPEN)
insert into manufacturing_order (code, status_id, customer_order_id, customer_order_line_id, part_id, quantity, opened_at, completed_at, created_at) values
    ('MO-2026-SUPPLY-PA-001',
     (select id from manufacturing_order_status where code = 'OPEN'),
     (select id from customer_order where code = 'CO-2026-SUPPLY-001'),
     (select id from customer_order_line
        where customer_order_id = (select id from customer_order where code = 'CO-2026-SUPPLY-001')
          and part_id = (select id from part where code = 'EXECUTIVE_FULL_CABIN_KIT')),
     (select id from part where code = 'EXECUTIVE_FULL_CABIN_KIT'),
     1.000,
     now() - interval '30 minutes',
     null,
     now());

-- SA manufacturing order 1: FRONT_SEAT_BASE_COVER (in BOM of EXECUTIVE_FULL_CABIN_KIT, qty x2)
-- Status PREPARED, will be located in RACK-B1, NOT in manufacturing_order_component
insert into manufacturing_order (code, status_id, customer_order_id, customer_order_line_id, part_id, quantity, opened_at, completed_at, created_at) values
    ('MO-2026-SUPPLY-SA-001',
     (select id from manufacturing_order_status where code = 'PREPARED'),
     (select id from customer_order where code = 'CO-2026-SUPPLY-001'),
     (select id from customer_order_line
        where customer_order_id = (select id from customer_order where code = 'CO-2026-SUPPLY-001')
          and part_id = (select id from part where code = 'EXECUTIVE_FULL_CABIN_KIT')),
     (select id from part where code = 'FRONT_SEAT_BASE_COVER'),
     2.000,
     now() - interval '30 minutes',
     null,
     now());

-- SA manufacturing order 2: REAR_SEAT_BASE_COVER (in BOM of EXECUTIVE_FULL_CABIN_KIT, qty x1)
-- Status PREPARED, will be located in RACK-B2, NOT in manufacturing_order_component
insert into manufacturing_order (code, status_id, customer_order_id, customer_order_line_id, part_id, quantity, opened_at, completed_at, created_at) values
    ('MO-2026-SUPPLY-SA-002',
     (select id from manufacturing_order_status where code = 'PREPARED'),
     (select id from customer_order where code = 'CO-2026-SUPPLY-001'),
     (select id from customer_order_line
        where customer_order_id = (select id from customer_order where code = 'CO-2026-SUPPLY-001')
          and part_id = (select id from part where code = 'EXECUTIVE_FULL_CABIN_KIT')),
     (select id from part where code = 'REAR_SEAT_BASE_COVER'),
     1.000,
     now() - interval '30 minutes',
     null,
     now());

-- Active rack locations (removed_at IS NULL = currently occupied)
insert into rack_location (rack_id, manufacturing_order_id, located_at, removed_at, created_at) values
    ((select id from rack where code = 'RACK-B1'),
     (select id from manufacturing_order where code = 'MO-2026-SUPPLY-SA-001'),
     now() - interval '15 minutes',
     null,
     now());

insert into rack_location (rack_id, manufacturing_order_id, located_at, removed_at, created_at) values
    ((select id from rack where code = 'RACK-B2'),
     (select id from manufacturing_order where code = 'MO-2026-SUPPLY-SA-002'),
     now() - interval '15 minutes',
     null,
     now());
