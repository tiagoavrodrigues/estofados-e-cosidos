insert into user_profile (name, email, active, created_at) values
    ('Marta Alves', 'marta.alves@autolux-interiors.pt', true, now()),
    ('Sofia Martins', 'sofia.martins@estofadosecosidos.pt', true, now()),
    ('Duarte Ferreira', 'duarte.ferreira@estofadosecosidos.pt', true, now());

insert into customer (code, user_profile_id, name, tax_number, phone, address_line, city, postal_code, active, created_at) values
    ('CUST-AUTOLUX', (select id from user_profile where email = 'marta.alves@autolux-interiors.pt'), 'AutoLux Interiors', 'PT509874321', '+351 213 450 890', 'Rua da Industria Automovel 42', 'Palmela', '2950-402', true, now());

insert into employee (code, user_profile_id, name, job_title, active, created_at) values
    ('EMP-SOFIA-MARTINS', (select id from user_profile where email = 'sofia.martins@estofadosecosidos.pt'), 'Sofia Martins', 'Production Manager', true, now()),
    ('EMP-DUARTE-FERREIRA', (select id from user_profile where email = 'duarte.ferreira@estofadosecosidos.pt'), 'Duarte Ferreira', 'Warehouse Manager', true, now());

insert into user_profile_role (user_profile_id, role_id, created_at) values
    ((select id from user_profile where email = 'marta.alves@autolux-interiors.pt'), (select id from role where code = 'CUSTOMER'), now()),
    ((select id from user_profile where email = 'sofia.martins@estofadosecosidos.pt'), (select id from role where code = 'PRODUCTION_MANAGER'), now()),
    ((select id from user_profile where email = 'sofia.martins@estofadosecosidos.pt'), (select id from role where code = 'PRODUCTION_TEAM_LEAD'), now()),
    ((select id from user_profile where email = 'duarte.ferreira@estofadosecosidos.pt'), (select id from role where code = 'WAREHOUSE_MANAGER'), now()),
    ((select id from user_profile where email = 'duarte.ferreira@estofadosecosidos.pt'), (select id from role where code = 'LOGISTICS_OPERATOR'), now());

insert into raw_material (code, name, default_unit, active, created_at) values
    ('AUTO_FABRIC_CHARCOAL', 'Charcoal automotive woven fabric', 'm', true, now()),
    ('SYNTHETIC_LEATHER_BLACK', 'Black synthetic leather', 'm', true, now()),
    ('FOAM_5MM_SHEET', '5 mm upholstery foam sheet', 'm2', true, now()),
    ('FOAM_10MM_SHEET', '10 mm comfort foam sheet', 'm2', true, now()),
    ('THREAD_BLACK_TEX70', 'Black bonded polyester thread TEX 70', 'm', true, now()),
    ('THREAD_SILVER_TEX70', 'Silver bonded polyester thread TEX 70', 'm', true, now()),
    ('CONTACT_ADHESIVE_SPRAY', 'Automotive contact adhesive spray', 'l', true, now()),
    ('REINFORCEMENT_WEBBING_25MM', '25 mm reinforcement webbing', 'm', true, now()),
    ('ZIPPER_COIL_BLACK', 'Black coil zipper tape', 'm', true, now()),
    ('CARE_LABEL_WOVEN', 'Woven care and batch label', 'unit', true, now());

insert into part (code, name, description, part_type_id, active, created_at) values
    ('FRONT_SEAT_BASE_COVER', 'Front Seat Base Cover', 'Semi-finished cover for front seat base cushion.', (select id from part_type where code = 'SEMI_FINISHED'), true, now()),
    ('FRONT_BACKREST_COVER', 'Front Backrest Cover', 'Semi-finished cover for front backrest.', (select id from part_type where code = 'SEMI_FINISHED'), true, now()),
    ('HEADREST_COVER', 'Headrest Cover', 'Semi-finished cover for individual headrest.', (select id from part_type where code = 'SEMI_FINISHED'), true, now()),
    ('REAR_SEAT_BASE_COVER', 'Rear Seat Base Cover', 'Semi-finished cover for rear bench base.', (select id from part_type where code = 'SEMI_FINISHED'), true, now()),
    ('REAR_BACKREST_COVER', 'Rear Backrest Cover', 'Semi-finished cover for rear bench backrest.', (select id from part_type where code = 'SEMI_FINISHED'), true, now()),
    ('SIDE_BOLSTER_COVER', 'Side Bolster Cover', 'Semi-finished side bolster cover.', (select id from part_type where code = 'SEMI_FINISHED'), true, now()),
    ('ARMREST_COVER', 'Armrest Cover', 'Semi-finished central armrest cover.', (select id from part_type where code = 'SEMI_FINISHED'), true, now()),
    ('PREMIUM_FRONT_SEAT_KIT', 'Premium Front Seat Upholstery Kit', 'Finished upholstery kit for two front seats.', (select id from part_type where code = 'FINISHED_PRODUCT'), true, now()),
    ('PREMIUM_REAR_BENCH_KIT', 'Premium Rear Bench Upholstery Kit', 'Finished upholstery kit for rear bench.', (select id from part_type where code = 'FINISHED_PRODUCT'), true, now()),
    ('EXECUTIVE_FULL_CABIN_KIT', 'Executive Full Cabin Upholstery Kit', 'Finished upholstery kit for full vehicle cabin.', (select id from part_type where code = 'FINISHED_PRODUCT'), true, now()),
    ('SPORT_HEADREST_PAIR', 'Sport Headrest Pair', 'Finished pair of sport headrest covers.', (select id from part_type where code = 'FINISHED_PRODUCT'), true, now());

insert into bill_of_material_item (parent_part_id, component_part_id, quantity, active, created_at) values
    ((select id from part where code = 'PREMIUM_FRONT_SEAT_KIT'), (select id from part where code = 'FRONT_SEAT_BASE_COVER'), 2.000, true, now()),
    ((select id from part where code = 'PREMIUM_FRONT_SEAT_KIT'), (select id from part where code = 'FRONT_BACKREST_COVER'), 2.000, true, now()),
    ((select id from part where code = 'PREMIUM_FRONT_SEAT_KIT'), (select id from part where code = 'HEADREST_COVER'), 2.000, true, now()),
    ((select id from part where code = 'PREMIUM_FRONT_SEAT_KIT'), (select id from part where code = 'SIDE_BOLSTER_COVER'), 4.000, true, now()),
    ((select id from part where code = 'PREMIUM_REAR_BENCH_KIT'), (select id from part where code = 'REAR_SEAT_BASE_COVER'), 1.000, true, now()),
    ((select id from part where code = 'PREMIUM_REAR_BENCH_KIT'), (select id from part where code = 'REAR_BACKREST_COVER'), 1.000, true, now()),
    ((select id from part where code = 'PREMIUM_REAR_BENCH_KIT'), (select id from part where code = 'HEADREST_COVER'), 3.000, true, now()),
    ((select id from part where code = 'PREMIUM_REAR_BENCH_KIT'), (select id from part where code = 'ARMREST_COVER'), 1.000, true, now()),
    ((select id from part where code = 'EXECUTIVE_FULL_CABIN_KIT'), (select id from part where code = 'FRONT_SEAT_BASE_COVER'), 2.000, true, now()),
    ((select id from part where code = 'EXECUTIVE_FULL_CABIN_KIT'), (select id from part where code = 'FRONT_BACKREST_COVER'), 2.000, true, now()),
    ((select id from part where code = 'EXECUTIVE_FULL_CABIN_KIT'), (select id from part where code = 'REAR_SEAT_BASE_COVER'), 1.000, true, now()),
    ((select id from part where code = 'EXECUTIVE_FULL_CABIN_KIT'), (select id from part where code = 'REAR_BACKREST_COVER'), 1.000, true, now()),
    ((select id from part where code = 'SPORT_HEADREST_PAIR'), (select id from part where code = 'HEADREST_COVER'), 2.000, true, now());

insert into part_raw_material_requirement (part_id, raw_material_id, quantity, unit, waste_percentage, active, created_at) values
    ((select id from part where code = 'FRONT_SEAT_BASE_COVER'), (select id from raw_material where code = 'SYNTHETIC_LEATHER_BLACK'), 1.600, 'm', 5.00, true, now()),
    ((select id from part where code = 'FRONT_SEAT_BASE_COVER'), (select id from raw_material where code = 'FOAM_10MM_SHEET'), 0.900, 'm2', 3.00, true, now()),
    ((select id from part where code = 'FRONT_SEAT_BASE_COVER'), (select id from raw_material where code = 'THREAD_BLACK_TEX70'), 18.000, 'm', 2.00, true, now()),
    ((select id from part where code = 'FRONT_BACKREST_COVER'), (select id from raw_material where code = 'SYNTHETIC_LEATHER_BLACK'), 1.900, 'm', 5.00, true, now()),
    ((select id from part where code = 'FRONT_BACKREST_COVER'), (select id from raw_material where code = 'FOAM_5MM_SHEET'), 0.800, 'm2', 3.00, true, now()),
    ((select id from part where code = 'FRONT_BACKREST_COVER'), (select id from raw_material where code = 'THREAD_BLACK_TEX70'), 21.000, 'm', 2.00, true, now()),
    ((select id from part where code = 'HEADREST_COVER'), (select id from raw_material where code = 'SYNTHETIC_LEATHER_BLACK'), 0.450, 'm', 4.00, true, now()),
    ((select id from part where code = 'HEADREST_COVER'), (select id from raw_material where code = 'FOAM_5MM_SHEET'), 0.200, 'm2', 2.00, true, now()),
    ((select id from part where code = 'HEADREST_COVER'), (select id from raw_material where code = 'THREAD_SILVER_TEX70'), 6.000, 'm', 2.00, true, now()),
    ((select id from part where code = 'SIDE_BOLSTER_COVER'), (select id from raw_material where code = 'AUTO_FABRIC_CHARCOAL'), 0.700, 'm', 4.00, true, now()),
    ((select id from part where code = 'SIDE_BOLSTER_COVER'), (select id from raw_material where code = 'REINFORCEMENT_WEBBING_25MM'), 1.200, 'm', 1.00, true, now()),
    ((select id from part where code = 'REAR_SEAT_BASE_COVER'), (select id from raw_material where code = 'AUTO_FABRIC_CHARCOAL'), 2.400, 'm', 5.00, true, now()),
    ((select id from part where code = 'REAR_BACKREST_COVER'), (select id from raw_material where code = 'AUTO_FABRIC_CHARCOAL'), 2.700, 'm', 5.00, true, now()),
    ((select id from part where code = 'ARMREST_COVER'), (select id from raw_material where code = 'ZIPPER_COIL_BLACK'), 0.500, 'm', 1.00, true, now()),
    ((select id from part where code = 'ARMREST_COVER'), (select id from raw_material where code = 'CARE_LABEL_WOVEN'), 1.000, 'unit', 0.00, true, now());

insert into raw_material_order (code, status_id, order_date, confirmed_at, notes, created_at) values
    ('RMO-2026-0001', (select id from raw_material_order_status where code = 'CONFIRMED'), current_date - 20, now() - interval '19 days', 'Initial textile and foam replenishment for premium upholstery line.', now()),
    ('RMO-2026-0002', (select id from raw_material_order_status where code = 'CONFIRMED'), current_date - 12, now() - interval '11 days', 'Thread, adhesive and trim replenishment for scheduled cabin kits.', now());

insert into raw_material_order_line (raw_material_order_id, raw_material_id, quantity, unit, created_at) values
    ((select id from raw_material_order where code = 'RMO-2026-0001'), (select id from raw_material where code = 'SYNTHETIC_LEATHER_BLACK'), 120.000, 'm', now()),
    ((select id from raw_material_order where code = 'RMO-2026-0001'), (select id from raw_material where code = 'AUTO_FABRIC_CHARCOAL'), 80.000, 'm', now()),
    ((select id from raw_material_order where code = 'RMO-2026-0001'), (select id from raw_material where code = 'FOAM_5MM_SHEET'), 60.000, 'm2', now()),
    ((select id from raw_material_order where code = 'RMO-2026-0001'), (select id from raw_material where code = 'FOAM_10MM_SHEET'), 45.000, 'm2', now()),
    ((select id from raw_material_order where code = 'RMO-2026-0002'), (select id from raw_material where code = 'THREAD_BLACK_TEX70'), 1000.000, 'm', now()),
    ((select id from raw_material_order where code = 'RMO-2026-0002'), (select id from raw_material where code = 'THREAD_SILVER_TEX70'), 500.000, 'm', now()),
    ((select id from raw_material_order where code = 'RMO-2026-0002'), (select id from raw_material where code = 'CONTACT_ADHESIVE_SPRAY'), 25.000, 'l', now()),
    ((select id from raw_material_order where code = 'RMO-2026-0002'), (select id from raw_material where code = 'REINFORCEMENT_WEBBING_25MM'), 150.000, 'm', now()),
    ((select id from raw_material_order where code = 'RMO-2026-0002'), (select id from raw_material where code = 'ZIPPER_COIL_BLACK'), 60.000, 'm', now()),
    ((select id from raw_material_order where code = 'RMO-2026-0002'), (select id from raw_material where code = 'CARE_LABEL_WOVEN'), 300.000, 'unit', now());

insert into stock (code, raw_material_id, raw_material_order_id, stock_status_id, initial_quantity, available_quantity, unit, received_at, created_at) values
    ('STK-LEATHER-BLACK-2026-01', (select id from raw_material where code = 'SYNTHETIC_LEATHER_BLACK'), (select id from raw_material_order where code = 'RMO-2026-0001'), (select id from stock_status where code = 'AVAILABLE'), 120.000, 117.000, 'm', now() - interval '19 days', now()),
    ('STK-FABRIC-CHARCOAL-2026-01', (select id from raw_material where code = 'AUTO_FABRIC_CHARCOAL'), (select id from raw_material_order where code = 'RMO-2026-0001'), (select id from stock_status where code = 'AVAILABLE'), 80.000, 80.000, 'm', now() - interval '19 days', now()),
    ('STK-FOAM5-2026-01', (select id from raw_material where code = 'FOAM_5MM_SHEET'), (select id from raw_material_order where code = 'RMO-2026-0001'), (select id from stock_status where code = 'AVAILABLE'), 60.000, 60.000, 'm2', now() - interval '19 days', now()),
    ('STK-FOAM10-2026-01', (select id from raw_material where code = 'FOAM_10MM_SHEET'), (select id from raw_material_order where code = 'RMO-2026-0001'), (select id from stock_status where code = 'AVAILABLE'), 45.000, 45.000, 'm2', now() - interval '19 days', now()),
    ('STK-THREAD-BLACK-2026-01', (select id from raw_material where code = 'THREAD_BLACK_TEX70'), (select id from raw_material_order where code = 'RMO-2026-0002'), (select id from stock_status where code = 'AVAILABLE'), 1000.000, 940.000, 'm', now() - interval '11 days', now()),
    ('STK-THREAD-SILVER-2026-01', (select id from raw_material where code = 'THREAD_SILVER_TEX70'), (select id from raw_material_order where code = 'RMO-2026-0002'), (select id from stock_status where code = 'AVAILABLE'), 500.000, 500.000, 'm', now() - interval '11 days', now()),
    ('STK-ADHESIVE-2026-01', (select id from raw_material where code = 'CONTACT_ADHESIVE_SPRAY'), (select id from raw_material_order where code = 'RMO-2026-0002'), (select id from stock_status where code = 'AVAILABLE'), 25.000, 25.000, 'l', now() - interval '11 days', now()),
    ('STK-WEBBING-2026-01', (select id from raw_material where code = 'REINFORCEMENT_WEBBING_25MM'), (select id from raw_material_order where code = 'RMO-2026-0002'), (select id from stock_status where code = 'AVAILABLE'), 150.000, 150.000, 'm', now() - interval '11 days', now()),
    ('STK-ZIPPER-BLACK-2026-01', (select id from raw_material where code = 'ZIPPER_COIL_BLACK'), (select id from raw_material_order where code = 'RMO-2026-0002'), (select id from stock_status where code = 'AVAILABLE'), 60.000, 60.000, 'm', now() - interval '11 days', now()),
    ('STK-CARE-LABEL-2026-01', (select id from raw_material where code = 'CARE_LABEL_WOVEN'), (select id from raw_material_order where code = 'RMO-2026-0002'), (select id from stock_status where code = 'AVAILABLE'), 300.000, 300.000, 'unit', now() - interval '11 days', now());

insert into customer_order (code, customer_id, status_id, order_date, validated_at, ready_for_production_at, notes, created_at) values
    ('CO-2026-0001', (select id from customer where code = 'CUST-AUTOLUX'), (select id from customer_order_status where code = 'READY_FOR_PRODUCTION'), current_date - 5, now() - interval '4 days', now() - interval '3 days', 'Premium front seat kits for AutoLux showroom vehicle preparation.', now()),
    ('CO-2026-0002', (select id from customer where code = 'CUST-AUTOLUX'), (select id from customer_order_status where code = 'VALIDATED'), current_date - 2, now() - interval '1 day', null, 'Rear bench upholstery kit pending production scheduling.', now());

insert into customer_order_line (customer_order_id, part_id, quantity, created_at) values
    ((select id from customer_order where code = 'CO-2026-0001'), (select id from part where code = 'PREMIUM_FRONT_SEAT_KIT'), 4.000, now()),
    ((select id from customer_order where code = 'CO-2026-0002'), (select id from part where code = 'PREMIUM_REAR_BENCH_KIT'), 2.000, now());

insert into manufacturing_order (code, status_id, customer_order_id, customer_order_line_id, part_id, quantity, opened_at, completed_at, created_at) values
    ('MO-2026-0001', (select id from manufacturing_order_status where code = 'OPEN'), (select id from customer_order where code = 'CO-2026-0001'), (select id from customer_order_line where customer_order_id = (select id from customer_order where code = 'CO-2026-0001') and part_id = (select id from part where code = 'PREMIUM_FRONT_SEAT_KIT')), (select id from part where code = 'PREMIUM_FRONT_SEAT_KIT'), 4.000, now() - interval '3 days', null, now()),
    ('MO-2026-0002', (select id from manufacturing_order_status where code = 'IN_CUTTING'), (select id from customer_order where code = 'CO-2026-0001'), (select id from customer_order_line where customer_order_id = (select id from customer_order where code = 'CO-2026-0001') and part_id = (select id from part where code = 'PREMIUM_FRONT_SEAT_KIT')), (select id from part where code = 'FRONT_SEAT_BASE_COVER'), 8.000, now() - interval '3 days', null, now()),
    ('MO-2026-0003', (select id from manufacturing_order_status where code = 'OPEN'), (select id from customer_order where code = 'CO-2026-0001'), (select id from customer_order_line where customer_order_id = (select id from customer_order where code = 'CO-2026-0001') and part_id = (select id from part where code = 'PREMIUM_FRONT_SEAT_KIT')), (select id from part where code = 'FRONT_BACKREST_COVER'), 8.000, now() - interval '3 days', null, now()),
    ('MO-2026-0004', (select id from manufacturing_order_status where code = 'PREPARED'), (select id from customer_order where code = 'CO-2026-0001'), (select id from customer_order_line where customer_order_id = (select id from customer_order where code = 'CO-2026-0001') and part_id = (select id from part where code = 'PREMIUM_FRONT_SEAT_KIT')), (select id from part where code = 'HEADREST_COVER'), 8.000, now() - interval '3 days', null, now()),
    ('MO-2026-0005', (select id from manufacturing_order_status where code = 'PREPARED'), (select id from customer_order where code = 'CO-2026-0001'), (select id from customer_order_line where customer_order_id = (select id from customer_order where code = 'CO-2026-0001') and part_id = (select id from part where code = 'PREMIUM_FRONT_SEAT_KIT')), (select id from part where code = 'SIDE_BOLSTER_COVER'), 16.000, now() - interval '3 days', null, now()),
    ('MO-2026-0006', (select id from manufacturing_order_status where code = 'OPEN'), (select id from customer_order where code = 'CO-2026-0002'), (select id from customer_order_line where customer_order_id = (select id from customer_order where code = 'CO-2026-0002') and part_id = (select id from part where code = 'PREMIUM_REAR_BENCH_KIT')), (select id from part where code = 'PREMIUM_REAR_BENCH_KIT'), 2.000, now() - interval '1 day', null, now());

insert into manufacturing_order_component (parent_manufacturing_order_id, component_manufacturing_order_id, created_at) values
    ((select id from manufacturing_order where code = 'MO-2026-0001'), (select id from manufacturing_order where code = 'MO-2026-0002'), now()),
    ((select id from manufacturing_order where code = 'MO-2026-0001'), (select id from manufacturing_order where code = 'MO-2026-0003'), now()),
    ((select id from manufacturing_order where code = 'MO-2026-0001'), (select id from manufacturing_order where code = 'MO-2026-0004'), now()),
    ((select id from manufacturing_order where code = 'MO-2026-0001'), (select id from manufacturing_order where code = 'MO-2026-0005'), now());

insert into stock_reservation (code, customer_order_id, manufacturing_order_id, raw_material_id, stock_id, status_id, reserved_quantity, consumed_quantity, unit, reserved_at, released_at, created_at) values
    ('SR-2026-0001', (select id from customer_order where code = 'CO-2026-0001'), (select id from manufacturing_order where code = 'MO-2026-0002'), (select id from raw_material where code = 'SYNTHETIC_LEATHER_BLACK'), (select id from stock where code = 'STK-LEATHER-BLACK-2026-01'), (select id from stock_reservation_status where code = 'PARTIALLY_CONSUMED'), 28.000, 3.000, 'm', now() - interval '3 days', null, now()),
    ('SR-2026-0002', (select id from customer_order where code = 'CO-2026-0001'), (select id from manufacturing_order where code = 'MO-2026-0002'), (select id from raw_material where code = 'FOAM_10MM_SHEET'), (select id from stock where code = 'STK-FOAM10-2026-01'), (select id from stock_reservation_status where code = 'RESERVED'), 8.000, 0.000, 'm2', now() - interval '3 days', null, now()),
    ('SR-2026-0003', (select id from customer_order where code = 'CO-2026-0001'), (select id from manufacturing_order where code = 'MO-2026-0002'), (select id from raw_material where code = 'THREAD_BLACK_TEX70'), (select id from stock where code = 'STK-THREAD-BLACK-2026-01'), (select id from stock_reservation_status where code = 'RESERVED'), 160.000, 0.000, 'm', now() - interval '3 days', null, now());

insert into stock_consumption (code, manufacturing_order_id, raw_material_id, stock_id, stock_reservation_id, quantity, unit, consumed_at, created_at) values
    ('SC-2026-0001', (select id from manufacturing_order where code = 'MO-2026-0002'), (select id from raw_material where code = 'SYNTHETIC_LEATHER_BLACK'), (select id from stock where code = 'STK-LEATHER-BLACK-2026-01'), (select id from stock_reservation where code = 'SR-2026-0001'), 3.000, 'm', now() - interval '1 day', now());

insert into rack (code, name, active, created_at) values
    ('RACK-A1', 'Cut Parts Rack A1', true, now());

insert into rack_location (rack_id, manufacturing_order_id, located_at, removed_at, created_at) values
    ((select id from rack where code = 'RACK-A1'), (select id from manufacturing_order where code = 'MO-2026-0004'), now() - interval '1 day', null, now());

insert into production_cart (code, name, active, created_at) values
    ('CART-ALPHA-01', 'Alpha Production Cart 01', true, now());

insert into sequencing (production_cart_id, manufacturing_order_id, sequence_order, sequenced_at, created_at) values
    ((select id from production_cart where code = 'CART-ALPHA-01'), (select id from manufacturing_order where code = 'MO-2026-0004'), 1, now() - interval '12 hours', now()),
    ((select id from production_cart where code = 'CART-ALPHA-01'), (select id from manufacturing_order where code = 'MO-2026-0005'), 2, now() - interval '12 hours', now());

insert into team (code, name, active, created_at) values
    ('TEAM-UPHOLSTERY-A', 'Upholstery Team A', true, now());

insert into team_location (team_id, code, name, active, created_at) values
    ((select id from team where code = 'TEAM-UPHOLSTERY-A'), 'CELL-SEWING-01', 'Sewing Cell 01', true, now());

insert into production_plan (manufacturing_order_id, team_id, team_location_id, planned_start_at, planned_end_at, created_at) values
    ((select id from manufacturing_order where code = 'MO-2026-0004'), (select id from team where code = 'TEAM-UPHOLSTERY-A'), (select id from team_location where code = 'CELL-SEWING-01'), now() + interval '1 day', now() + interval '2 days', now());

insert into label (code, manufacturing_order_id, created_at) values
    ('LBL-MO-2026-0004-001', (select id from manufacturing_order where code = 'MO-2026-0004'), now());

insert into quality_record (code, quality_record_type_id, manufacturing_order_id, label_id, quantity, notes, recorded_at, created_at) values
    ('QR-2026-0001', (select id from quality_record_type where code = 'PRODUCED'), (select id from manufacturing_order where code = 'MO-2026-0004'), (select id from label where code = 'LBL-MO-2026-0004-001'), 2.000, 'First headrest covers completed with approved stitching.', now(), now()),
    ('QR-2026-0002', (select id from quality_record_type where code = 'DEFECT'), (select id from manufacturing_order where code = 'MO-2026-0004'), (select id from label where code = 'LBL-MO-2026-0004-001'), 1.000, 'Minor seam tension deviation detected during final inspection.', now(), now());
