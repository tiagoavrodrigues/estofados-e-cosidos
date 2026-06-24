insert into role (code, name, sort_order, active, created_at) values
    ('CUSTOMER', 'Customer', 1, true, now()),
    ('ADMIN', 'Admin', 2, true, now()),
    ('ORDER_MANAGER', 'Order Manager', 3, true, now()),
    ('PRODUCTION_MANAGER', 'Production Manager', 4, true, now()),
    ('WAREHOUSE_MANAGER', 'Warehouse Manager', 5, true, now()),
    ('CUTTER', 'Cutter', 6, true, now()),
    ('CUTTING_TEAM_LEAD', 'Cutting Team Lead', 7, true, now()),
    ('LOGISTICS_OPERATOR', 'Logistics Operator', 8, true, now()),
    ('PRODUCTION_TEAM_LEAD', 'Production Team Lead', 9, true, now()),
    ('QUALITY_CONTROLLER', 'Quality Controller', 10, true, now()),
    ('PACKAGING_OPERATOR', 'Packaging Operator', 11, true, now());

insert into part_type (code, name, sort_order, active, created_at) values
    ('SEMI_FINISHED', 'Semi-finished', 1, true, now()),
    ('FINISHED_PRODUCT', 'Finished Product', 2, true, now());

insert into raw_material_order_status (code, name, sort_order, active, created_at) values
    ('PENDING', 'Pending', 1, true, now()),
    ('CONFIRMED', 'Confirmed', 2, true, now()),
    ('REJECTED', 'Rejected', 3, true, now()),
    ('PARTIALLY_RECEIVED', 'Partially Received', 4, true, now()),
    ('CANCELLED', 'Cancelled', 5, true, now());

insert into stock_status (code, name, sort_order, active, created_at) values
    ('AVAILABLE', 'Available', 1, true, now()),
    ('BLOCKED', 'Blocked', 2, true, now()),
    ('CLOSED', 'Closed', 3, true, now());

insert into stock_reservation_status (code, name, sort_order, active, created_at) values
    ('RESERVED', 'Reserved', 1, true, now()),
    ('PARTIALLY_CONSUMED', 'Partially Consumed', 2, true, now()),
    ('CONSUMED', 'Consumed', 3, true, now()),
    ('RELEASED', 'Released', 4, true, now()),
    ('CANCELLED', 'Cancelled', 5, true, now());

insert into customer_order_status (code, name, sort_order, active, created_at) values
    ('RECEIVED', 'Received', 1, true, now()),
    ('VALIDATED', 'Validated', 2, true, now()),
    ('WAITING_FOR_MATERIAL', 'Waiting for Material', 3, true, now()),
    ('READY_FOR_PRODUCTION', 'Ready for Production', 4, true, now()),
    ('IN_PREPARATION', 'In Preparation', 5, true, now()),
    ('IN_PRODUCTION', 'In Production', 6, true, now()),
    ('IN_QUALITY_CONTROL', 'In Quality Control', 7, true, now()),
    ('PACKAGED', 'Packaged', 8, true, now()),
    ('SHIPPED', 'Shipped', 9, true, now()),
    ('CANCELLED', 'Cancelled', 10, true, now());

insert into manufacturing_order_status (code, name, sort_order, active, created_at) values
    ('OPEN', 'Open', 1, true, now()),
    ('IN_CUTTING', 'In Cutting', 2, true, now()),
    ('CUT', 'Cut', 3, true, now()),
    ('TRANSFORMED', 'Transformed', 4, true, now()),
    ('PREPARED', 'Prepared', 5, true, now()),
    ('SEQUENCED', 'Sequenced', 6, true, now()),
    ('IN_PRODUCTION', 'In Production', 7, true, now()),
    ('IN_QUALITY_CONTROL', 'In Quality Control', 8, true, now()),
    ('PRODUCED', 'Produced', 9, true, now()),
    ('PACKAGED', 'Packaged', 10, true, now()),
    ('SHIPPED', 'Shipped', 11, true, now()),
    ('CANCELLED', 'Cancelled', 12, true, now());

insert into quality_record_type (code, name, sort_order, active, created_at) values
    ('DEFECT', 'Defect', 1, true, now()),
    ('SCRAP', 'Scrap', 2, true, now()),
    ('PRODUCED', 'Produced', 3, true, now());
