USE storemanager;
SET time_zone = '-05:00';

DELIMITER $$

DROP PROCEDURE IF EXISTS seed_demo_bulk $$
CREATE PROCEDURE seed_demo_bulk()
BEGIN
    DECLARE v_admin_id BIGINT;
    DECLARE v_cajero_id BIGINT;
    DECLARE v_supplier_id BIGINT;
    DECLARE v_customer_id BIGINT;
    DECLARE v_category_id BIGINT;
    DECLARE v_product_id BIGINT;
    DECLARE v_document_id BIGINT;
    DECLARE v_sale_id BIGINT;
    DECLARE v_n INT DEFAULT 0;
    DECLARE v_m INT DEFAULT 0;
    DECLARE v_slot INT DEFAULT 0;
    DECLARE v_product_seq INT DEFAULT 0;
    DECLARE v_quantity INT DEFAULT 0;
    DECLARE v_total_lines INT DEFAULT 0;
    DECLARE v_total_units INT DEFAULT 0;
    DECLARE v_current_stock INT DEFAULT 0;
    DECLARE v_new_stock INT DEFAULT 0;
    DECLARE v_stock_minimum INT DEFAULT 0;
    DECLARE v_target_stock INT DEFAULT 0;
    DECLARE v_document_total DECIMAL(12, 2) DEFAULT 0;
    DECLARE v_purchase_price DECIMAL(12, 2) DEFAULT 0;
    DECLARE v_sale_price DECIMAL(12, 2) DEFAULT 0;
    DECLARE v_line_subtotal DECIMAL(12, 2) DEFAULT 0;
    DECLARE v_line_tax DECIMAL(12, 2) DEFAULT 0;
    DECLARE v_line_total DECIMAL(12, 2) DEFAULT 0;
    DECLARE v_sale_subtotal DECIMAL(12, 2) DEFAULT 0;
    DECLARE v_sale_tax DECIMAL(12, 2) DEFAULT 0;
    DECLARE v_sale_total DECIMAL(12, 2) DEFAULT 0;
    DECLARE v_entry_mode VARCHAR(20);
    DECLARE v_product_name VARCHAR(120);
    DECLARE v_product_description VARCHAR(200);
    DECLARE v_category_name VARCHAR(80);
    DECLARE v_series VARCHAR(30);
    DECLARE v_correlative VARCHAR(30);
    DECLARE v_sale_date DATETIME;

    SELECT id INTO v_admin_id
    FROM usuarios
    WHERE username = 'admin'
    LIMIT 1;

    SELECT id INTO v_cajero_id
    FROM usuarios
    WHERE username = 'cajero'
    LIMIT 1;

    DELETE dv
    FROM detalle_ventas dv
    INNER JOIN ventas v ON v.id = dv.venta_id
    WHERE v.observacion LIKE '[DEMO] Venta %';

    DELETE mi
    FROM movimientos_inventario mi
    INNER JOIN ventas v
        ON mi.referencia_tipo = 'VENTA'
       AND mi.referencia_id = v.id
    WHERE v.observacion LIKE '[DEMO] Venta %';

    DELETE FROM ventas
    WHERE observacion LIKE '[DEMO] Venta %';

    DELETE mi
    FROM movimientos_inventario mi
    INNER JOIN documentos_stock ds
        ON mi.referencia_tipo = 'DOCUMENTO_STOCK'
       AND mi.referencia_id = ds.id
    WHERE ds.observacion LIKE '[DEMO] Entrada %'
       OR ds.observacion LIKE '[DEMO] Ajuste %';

    DELETE FROM documentos_stock
    WHERE observacion LIKE '[DEMO] Entrada %'
       OR observacion LIKE '[DEMO] Ajuste %';

    DELETE FROM productos
    WHERE codigo LIKE 'DEM-%';

    DELETE FROM clientes
    WHERE documento LIKE 'DEM-C%';

    DELETE FROM proveedores
    WHERE correo LIKE 'demo.prov%@storemanager.local';

    INSERT INTO categorias (nombre, descripcion, activo) VALUES
        ('Snacks', 'Pasabocas, galletas y golosinas', 1),
        ('Lacteos', 'Leche, queso y yogures', 1),
        ('Cuidado personal', 'Higiene y uso diario', 1),
        ('Mascotas', 'Alimento y accesorios basicos', 1),
        ('Congelados', 'Productos refrigerados y congelados', 1),
        ('Panaderia', 'Panificados y reposteria', 1),
        ('Desechables', 'Bolsas, vasos y utensilios', 1)
    ON DUPLICATE KEY UPDATE
        descripcion = VALUES(descripcion),
        activo = VALUES(activo);

    INSERT INTO proveedores (nombre, telefono, correo, direccion, activo) VALUES
        ('Proveedor Demo Hogar', '3001002101', 'demo.prov01@storemanager.local', 'Avenida 12 #44-18', 1),
        ('Proveedor Demo Frescos', '3001002102', 'demo.prov02@storemanager.local', 'Calle 48 #15-77', 1),
        ('Proveedor Demo Snacks', '3001002103', 'demo.prov03@storemanager.local', 'Carrera 19 #27-35', 1),
        ('Proveedor Demo Congelados', '3001002104', 'demo.prov04@storemanager.local', 'Zona Industrial Bodega 8', 1),
        ('Proveedor Demo Mascotas', '3001002105', 'demo.prov05@storemanager.local', 'Avenida Mascotas 14-62', 1),
        ('Proveedor Demo Cuidado', '3001002106', 'demo.prov06@storemanager.local', 'Calle 90 #11-23', 1),
        ('Proveedor Demo Panaderia', '3001002107', 'demo.prov07@storemanager.local', 'Pasaje del Trigo 3-21', 1),
        ('Proveedor Demo Mixto', '3001002108', 'demo.prov08@storemanager.local', 'Parque Comercial Local 4', 1);

    SET v_n = 1;
    WHILE v_n <= 36 DO
        INSERT INTO clientes (nombre, telefono, documento, direccion, activo)
        VALUES (
            CASE MOD(v_n - 1, 12)
                WHEN 0 THEN CONCAT('Carlos Mendoza ', v_n)
                WHEN 1 THEN CONCAT('Laura Pineda ', v_n)
                WHEN 2 THEN CONCAT('Jorge Ramirez ', v_n)
                WHEN 3 THEN CONCAT('Paula Castro ', v_n)
                WHEN 4 THEN CONCAT('Miguel Rojas ', v_n)
                WHEN 5 THEN CONCAT('Diana Herrera ', v_n)
                WHEN 6 THEN CONCAT('Samuel Duarte ', v_n)
                WHEN 7 THEN CONCAT('Luisa Parra ', v_n)
                WHEN 8 THEN CONCAT('Andres Solis ', v_n)
                WHEN 9 THEN CONCAT('Valentina Cifuentes ', v_n)
                WHEN 10 THEN CONCAT('Camilo Pena ', v_n)
                ELSE CONCAT('Daniela Suarez ', v_n)
            END,
            CONCAT('31', LPAD(5000000 + (v_n * 173), 8, '0')),
            CONCAT('DEM-C', LPAD(v_n, 4, '0')),
            CASE MOD(v_n - 1, 10)
                WHEN 0 THEN 'Barrio Centro'
                WHEN 1 THEN 'Sector San Miguel'
                WHEN 2 THEN 'Urbanizacion El Prado'
                WHEN 3 THEN 'Conjunto Los Pinos'
                WHEN 4 THEN 'Barrio La Esperanza'
                WHEN 5 THEN 'Avenida Principal 45-10'
                WHEN 6 THEN 'Calle Comercial 12-33'
                WHEN 7 THEN 'Zona Norte Etapa 2'
                WHEN 8 THEN 'Residencial Mirador'
                ELSE 'Mercado local y mostrador'
            END,
            IF(MOD(v_n, 11) = 0, 0, 1)
        );
        SET v_n = v_n + 1;
    END WHILE;

    SET v_n = 1;
    WHILE v_n <= 72 DO
        SET v_slot = MOD(v_n - 1, 10);
        SET v_category_name = CASE v_slot
            WHEN 0 THEN 'Abarrotes'
            WHEN 1 THEN 'Bebidas'
            WHEN 2 THEN 'Limpieza'
            WHEN 3 THEN 'Snacks'
            WHEN 4 THEN 'Lacteos'
            WHEN 5 THEN 'Cuidado personal'
            WHEN 6 THEN 'Mascotas'
            WHEN 7 THEN 'Congelados'
            WHEN 8 THEN 'Panaderia'
            ELSE 'Desechables'
        END;

        SELECT id INTO v_category_id
        FROM categorias
        WHERE nombre = v_category_name
        LIMIT 1;

        SET v_supplier_id = CASE v_slot
            WHEN 0 THEN (SELECT id FROM proveedores WHERE nombre = 'Distribuidora Central' LIMIT 1)
            WHEN 1 THEN (SELECT id FROM proveedores WHERE nombre = 'Bebidas del Norte' LIMIT 1)
            WHEN 2 THEN (SELECT id FROM proveedores WHERE nombre = 'Proveedor Demo Hogar' LIMIT 1)
            WHEN 3 THEN (SELECT id FROM proveedores WHERE nombre = 'Proveedor Demo Snacks' LIMIT 1)
            WHEN 4 THEN (SELECT id FROM proveedores WHERE nombre = 'Proveedor Demo Frescos' LIMIT 1)
            WHEN 5 THEN (SELECT id FROM proveedores WHERE nombre = 'Proveedor Demo Cuidado' LIMIT 1)
            WHEN 6 THEN (SELECT id FROM proveedores WHERE nombre = 'Proveedor Demo Mascotas' LIMIT 1)
            WHEN 7 THEN (SELECT id FROM proveedores WHERE nombre = 'Proveedor Demo Congelados' LIMIT 1)
            WHEN 8 THEN (SELECT id FROM proveedores WHERE nombre = 'Proveedor Demo Panaderia' LIMIT 1)
            ELSE (SELECT id FROM proveedores WHERE nombre = 'Proveedor Demo Mixto' LIMIT 1)
        END;

        SET v_product_name = CASE v_slot
            WHEN 0 THEN CONCAT('Arroz premium ', v_n, 'Kg')
            WHEN 1 THEN CONCAT('Bebida frutal ', v_n, 'ml')
            WHEN 2 THEN CONCAT('Detergente liquido ', v_n, 'ml')
            WHEN 3 THEN CONCAT('Galletas surtidas ', v_n, 'g')
            WHEN 4 THEN CONCAT('Yogur bebible ', v_n, 'ml')
            WHEN 5 THEN CONCAT('Jabon antibacterial ', v_n, 'g')
            WHEN 6 THEN CONCAT('Croquetas caninas ', v_n, 'g')
            WHEN 7 THEN CONCAT('Nuggets congelados ', v_n, 'g')
            WHEN 8 THEN CONCAT('Pan tajado integral ', v_n)
            ELSE CONCAT('Vasos desechables ', v_n, 'u')
        END;

        SET v_product_description = CONCAT('Producto demo para pruebas visuales de ', LOWER(v_category_name), '.');
        SET v_purchase_price = ROUND(1200 + (v_n * 135) + (MOD(v_n, 5) * 85), 2);
        SET v_sale_price = ROUND(v_purchase_price * (1.34 + (MOD(v_n, 4) * 0.05)), 2);

        INSERT INTO productos (
            categoria_id, proveedor_id, codigo, nombre, descripcion, precio_compra, precio_venta, stock, stock_minimo, estado
        ) VALUES (
            v_category_id,
            v_supplier_id,
            CONCAT('DEM-', LPAD(v_n, 3, '0')),
            v_product_name,
            v_product_description,
            v_purchase_price,
            v_sale_price,
            0,
            4 + MOD(v_n, 9),
            'ACTIVO'
        );

        SET v_n = v_n + 1;
    END WHILE;

    SET v_n = 1;
    WHILE v_n <= 12 DO
        SET v_supplier_id = CASE MOD(v_n - 1, 8)
            WHEN 0 THEN (SELECT id FROM proveedores WHERE nombre = 'Distribuidora Central' LIMIT 1)
            WHEN 1 THEN (SELECT id FROM proveedores WHERE nombre = 'Bebidas del Norte' LIMIT 1)
            WHEN 2 THEN (SELECT id FROM proveedores WHERE nombre = 'Proveedor Demo Hogar' LIMIT 1)
            WHEN 3 THEN (SELECT id FROM proveedores WHERE nombre = 'Proveedor Demo Snacks' LIMIT 1)
            WHEN 4 THEN (SELECT id FROM proveedores WHERE nombre = 'Proveedor Demo Frescos' LIMIT 1)
            WHEN 5 THEN (SELECT id FROM proveedores WHERE nombre = 'Proveedor Demo Cuidado' LIMIT 1)
            WHEN 6 THEN (SELECT id FROM proveedores WHERE nombre = 'Proveedor Demo Congelados' LIMIT 1)
            ELSE (SELECT id FROM proveedores WHERE nombre = 'Proveedor Demo Mixto' LIMIT 1)
        END;

        SET v_series = LPAD(1300 + v_n, 4, '0');
        SET v_correlative = LPAD(42000 + v_n, 6, '0');
        SET v_total_lines = 0;
        SET v_total_units = 0;
        SET v_document_total = 0;

        INSERT INTO documentos_stock (
            tipo_registro, proveedor_id, usuario_id, tipo_documento, serie, correlativo,
            almacen_destino, tipo_ajuste, motivo_general, observacion, total_lineas, total_unidades, monto_total
        ) VALUES (
            'COMPRA', v_supplier_id, v_admin_id, 'FACTURA', v_series, v_correlative,
            'Almacen principal', NULL, 'Carga demo inicial', CONCAT('[DEMO] Entrada ', v_n), 0, 0, 0
        );

        SET v_document_id = LAST_INSERT_ID();
        SET v_m = 1;

        WHILE v_m <= 6 DO
            SET v_product_seq = ((v_n - 1) * 6) + v_m;

            SELECT id, stock, precio_compra
            INTO v_product_id, v_current_stock, v_purchase_price
            FROM productos
            WHERE codigo = CONCAT('DEM-', LPAD(v_product_seq, 3, '0'))
            LIMIT 1;

            SET v_quantity = 12 + MOD((v_product_seq * 5), 24);
            SET v_new_stock = v_current_stock + v_quantity;
            SET v_line_subtotal = ROUND(v_purchase_price * v_quantity, 2);

            UPDATE productos
            SET stock = v_new_stock
            WHERE id = v_product_id;

            INSERT INTO movimientos_inventario (
                producto_id, usuario_id, tipo_movimiento, cantidad, stock_anterior, stock_nuevo,
                costo_unitario, subtotal_movimiento, lote, fecha_vencimiento, motivo, referencia_tipo, referencia_id
            ) VALUES (
                v_product_id,
                v_admin_id,
                'ENTRADA',
                v_quantity,
                v_current_stock,
                v_new_stock,
                v_purchase_price,
                v_line_subtotal,
                CONCAT('L', v_n, '-', LPAD(v_m, 2, '0')),
                DATE_ADD(CURDATE(), INTERVAL (90 + v_product_seq) DAY),
                CONCAT('Compra demo lote ', v_n),
                'DOCUMENTO_STOCK',
                v_document_id
            );

            SET v_total_lines = v_total_lines + 1;
            SET v_total_units = v_total_units + v_quantity;
            SET v_document_total = v_document_total + v_line_subtotal;
            SET v_m = v_m + 1;
        END WHILE;

        UPDATE documentos_stock
        SET total_lineas = v_total_lines,
            total_unidades = v_total_units,
            monto_total = v_document_total
        WHERE id = v_document_id;

        SET v_n = v_n + 1;
    END WHILE;

    INSERT INTO documentos_stock (
        tipo_registro, proveedor_id, usuario_id, tipo_documento, serie, correlativo,
        almacen_destino, tipo_ajuste, motivo_general, observacion, total_lineas, total_unidades, monto_total
    ) VALUES (
        'AJUSTE', NULL, v_admin_id, NULL, NULL, NULL,
        'Almacen principal', 'Conteo', 'Ajuste demo de stock bajo', '[DEMO] Ajuste stock bajo', 0, 0, 0
    );

    SET v_document_id = LAST_INSERT_ID();
    SET v_total_lines = 0;
    SET v_total_units = 0;
    SET v_document_total = 0;
    SET v_n = 1;

    WHILE v_n <= 8 DO
        SET v_product_seq = 4 + ((v_n - 1) * 7);

        SELECT id, stock, stock_minimo, precio_compra
        INTO v_product_id, v_current_stock, v_stock_minimum, v_purchase_price
        FROM productos
        WHERE codigo = CONCAT('DEM-', LPAD(v_product_seq, 3, '0'))
        LIMIT 1;

        SET v_target_stock = GREATEST(v_stock_minimum - MOD(v_n, 3), 0);

        IF v_current_stock > v_target_stock THEN
            SET v_quantity = v_current_stock - v_target_stock;

            UPDATE productos
            SET stock = v_target_stock
            WHERE id = v_product_id;

            INSERT INTO movimientos_inventario (
                producto_id, usuario_id, tipo_movimiento, cantidad, stock_anterior, stock_nuevo,
                costo_unitario, subtotal_movimiento, lote, fecha_vencimiento, motivo, referencia_tipo, referencia_id
            ) VALUES (
                v_product_id,
                v_admin_id,
                'SALIDA',
                v_quantity,
                v_current_stock,
                v_target_stock,
                v_purchase_price,
                ROUND(v_purchase_price * v_quantity, 2),
                NULL,
                NULL,
                'Ajuste demo para stock bajo',
                'DOCUMENTO_STOCK',
                v_document_id
            );

            SET v_total_lines = v_total_lines + 1;
            SET v_total_units = v_total_units + v_quantity;
            SET v_document_total = v_document_total + ROUND(v_purchase_price * v_quantity, 2);
        END IF;

        SET v_n = v_n + 1;
    END WHILE;

    UPDATE documentos_stock
    SET total_lineas = v_total_lines,
        total_unidades = v_total_units,
        monto_total = v_document_total
    WHERE id = v_document_id;

    SET v_n = 1;
    WHILE v_n <= 48 DO
        SET v_sale_subtotal = 0;
        SET v_sale_tax = 0;
        SET v_sale_total = 0;
        SET v_customer_id = NULL;

        IF MOD(v_n, 5) <> 0 THEN
            SELECT id INTO v_customer_id
            FROM clientes
            WHERE documento = CONCAT('DEM-C', LPAD((MOD(v_n - 1, 36) + 1), 4, '0'))
            LIMIT 1;
        END IF;

        SET v_sale_date = DATE_ADD(
            DATE_ADD(
                CAST(DATE_SUB(CURDATE(), INTERVAL MOD(v_n - 1, 18) DAY) AS DATETIME),
                INTERVAL (8 + MOD(v_n * 3, 10)) HOUR
            ),
            INTERVAL MOD(v_n * 7, 55) MINUTE
        );

        INSERT INTO ventas (
            usuario_id, cliente_id, fecha_venta, subtotal, impuesto, total, metodo_pago, estado, observacion
        ) VALUES (
            IF(MOD(v_n, 4) = 0, v_admin_id, v_cajero_id),
            v_customer_id,
            v_sale_date,
            0,
            0,
            0,
            CASE MOD(v_n, 4)
                WHEN 0 THEN 'Efectivo'
                WHEN 1 THEN 'Tarjeta'
                WHEN 2 THEN 'Transferencia'
                ELSE 'Nequi'
            END,
            'COMPLETADA',
            CONCAT('[DEMO] Venta ', LPAD(v_n, 3, '0'))
        );

        SET v_sale_id = LAST_INSERT_ID();
        SET v_m = 1;

        WHILE v_m <= 3 DO
            SET v_product_seq = MOD(((v_n - 1) * 5) + (v_m * 11), 72) + 1;

            SELECT id, stock, precio_venta
            INTO v_product_id, v_current_stock, v_sale_price
            FROM productos
            WHERE codigo = CONCAT('DEM-', LPAD(v_product_seq, 3, '0'))
            LIMIT 1;

            SET v_quantity = 1 + MOD(v_n + v_m, 3);

            IF v_current_stock < v_quantity THEN
                SET v_quantity = GREATEST(1, LEAST(v_current_stock, 1));
            END IF;

            IF v_current_stock > 0 THEN
                SET v_line_subtotal = ROUND(v_sale_price * v_quantity, 2);
                SET v_line_tax = ROUND(v_line_subtotal * 0.19, 2);
                SET v_line_total = v_line_subtotal + v_line_tax;
                SET v_new_stock = v_current_stock - v_quantity;

                INSERT INTO detalle_ventas (
                    venta_id, producto_id, cantidad, precio_unitario, impuesto_unitario, subtotal_linea, total_linea
                ) VALUES (
                    v_sale_id,
                    v_product_id,
                    v_quantity,
                    v_sale_price,
                    ROUND(v_line_tax / v_quantity, 2),
                    v_line_subtotal,
                    v_line_total
                );

                UPDATE productos
                SET stock = v_new_stock
                WHERE id = v_product_id;

                INSERT INTO movimientos_inventario (
                    producto_id, usuario_id, tipo_movimiento, cantidad, stock_anterior, stock_nuevo,
                    costo_unitario, subtotal_movimiento, lote, fecha_vencimiento, motivo, referencia_tipo, referencia_id
                ) VALUES (
                    v_product_id,
                    IF(MOD(v_n, 4) = 0, v_admin_id, v_cajero_id),
                    'SALIDA',
                    v_quantity,
                    v_current_stock,
                    v_new_stock,
                    NULL,
                    NULL,
                    NULL,
                    NULL,
                    CONCAT('Venta demo #', LPAD(v_n, 3, '0')),
                    'VENTA',
                    v_sale_id
                );

                SET v_sale_subtotal = v_sale_subtotal + v_line_subtotal;
                SET v_sale_tax = v_sale_tax + v_line_tax;
                SET v_sale_total = v_sale_total + v_line_total;
            END IF;

            SET v_m = v_m + 1;
        END WHILE;

        UPDATE ventas
        SET subtotal = v_sale_subtotal,
            impuesto = v_sale_tax,
            total = v_sale_total
        WHERE id = v_sale_id;

        SET v_n = v_n + 1;
    END WHILE;
END $$

CALL seed_demo_bulk() $$
DROP PROCEDURE seed_demo_bulk $$

DELIMITER ;
