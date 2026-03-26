USE storemanager;

CREATE TABLE IF NOT EXISTS documentos_stock (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tipo_registro VARCHAR(20) NOT NULL,
    proveedor_id BIGINT NULL,
    usuario_id BIGINT NOT NULL,
    tipo_documento VARCHAR(20) NULL,
    serie VARCHAR(30) NULL,
    correlativo VARCHAR(30) NULL,
    almacen_destino VARCHAR(80) NOT NULL,
    tipo_ajuste VARCHAR(30) NULL,
    motivo_general VARCHAR(180) NULL,
    observacion VARCHAR(250) NULL,
    total_lineas INT NOT NULL DEFAULT 0,
    total_unidades INT NOT NULL DEFAULT 0,
    monto_total DECIMAL(12, 2) NOT NULL DEFAULT 0,
    creado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_documentos_stock_proveedor FOREIGN KEY (proveedor_id)
        REFERENCES proveedores (id)
        ON UPDATE CASCADE
        ON DELETE SET NULL,
    CONSTRAINT fk_documentos_stock_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

ALTER TABLE movimientos_inventario
    MODIFY motivo VARCHAR(255) NULL;

SET @col_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'movimientos_inventario'
      AND COLUMN_NAME = 'costo_unitario'
);
SET @sql := IF(
    @col_exists = 0,
    'ALTER TABLE movimientos_inventario ADD COLUMN costo_unitario DECIMAL(12, 2) NULL AFTER stock_nuevo',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'movimientos_inventario'
      AND COLUMN_NAME = 'subtotal_movimiento'
);
SET @sql := IF(
    @col_exists = 0,
    'ALTER TABLE movimientos_inventario ADD COLUMN subtotal_movimiento DECIMAL(12, 2) NULL AFTER costo_unitario',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'movimientos_inventario'
      AND COLUMN_NAME = 'lote'
);
SET @sql := IF(
    @col_exists = 0,
    'ALTER TABLE movimientos_inventario ADD COLUMN lote VARCHAR(60) NULL AFTER subtotal_movimiento',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'movimientos_inventario'
      AND COLUMN_NAME = 'referencia_tipo'
);
SET @sql := IF(
    @col_exists = 0,
    'ALTER TABLE movimientos_inventario ADD COLUMN referencia_tipo VARCHAR(30) NULL AFTER motivo',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'movimientos_inventario'
      AND COLUMN_NAME = 'fecha_vencimiento'
);
SET @sql := IF(
    @col_exists = 0,
    'ALTER TABLE movimientos_inventario ADD COLUMN fecha_vencimiento DATE NULL AFTER lote',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @fk_exists := (
    SELECT COUNT(*)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
      AND TABLE_NAME = 'movimientos_inventario'
      AND CONSTRAINT_NAME = 'fk_movimientos_documentos_stock'
);
SET @sql := IF(
    @fk_exists > 0,
    'ALTER TABLE movimientos_inventario DROP FOREIGN KEY fk_movimientos_documentos_stock',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'movimientos_inventario'
      AND INDEX_NAME = 'idx_movimientos_referencia'
);
SET @sql := IF(
    @idx_exists = 0,
    'CREATE INDEX idx_movimientos_referencia ON movimientos_inventario (referencia_tipo, referencia_id)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'documentos_stock'
      AND INDEX_NAME = 'idx_documentos_stock_fecha'
);
SET @sql := IF(
    @idx_exists = 0,
    'CREATE INDEX idx_documentos_stock_fecha ON documentos_stock (creado_en)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
