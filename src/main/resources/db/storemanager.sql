DROP DATABASE IF EXISTS storemanager;
CREATE DATABASE storemanager CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE storemanager;

CREATE TABLE roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(150),
    activo TINYINT(1) NOT NULL DEFAULT 1
);

CREATE TABLE usuarios (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rol_id BIGINT NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(64) NOT NULL,
    nombre_completo VARCHAR(120) NOT NULL,
    activo TINYINT(1) NOT NULL DEFAULT 1,
    ultimo_acceso DATETIME NULL,
    creado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_usuarios_roles FOREIGN KEY (rol_id)
        REFERENCES roles (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

CREATE TABLE categorias (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(80) NOT NULL UNIQUE,
    descripcion VARCHAR(150),
    activo TINYINT(1) NOT NULL DEFAULT 1
);

CREATE TABLE proveedores (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(120) NOT NULL,
    telefono VARCHAR(30),
    correo VARCHAR(120),
    direccion VARCHAR(180),
    activo TINYINT(1) NOT NULL DEFAULT 1
);

CREATE TABLE clientes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(120) NOT NULL,
    telefono VARCHAR(30),
    documento VARCHAR(30) UNIQUE,
    direccion VARCHAR(180),
    activo TINYINT(1) NOT NULL DEFAULT 1
);

CREATE TABLE productos (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    categoria_id BIGINT NOT NULL,
    proveedor_id BIGINT NULL,
    codigo VARCHAR(40) NOT NULL UNIQUE,
    nombre VARCHAR(120) NOT NULL,
    descripcion VARCHAR(200),
    precio_compra DECIMAL(12, 2) NOT NULL,
    precio_venta DECIMAL(12, 2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    stock_minimo INT NOT NULL DEFAULT 0,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    creado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_productos_categorias FOREIGN KEY (categoria_id)
        REFERENCES categorias (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_productos_proveedores FOREIGN KEY (proveedor_id)
        REFERENCES proveedores (id)
        ON UPDATE CASCADE
        ON DELETE SET NULL
);

CREATE TABLE ventas (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    usuario_id BIGINT NOT NULL,
    cliente_id BIGINT NULL,
    fecha_venta DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    subtotal DECIMAL(12, 2) NOT NULL,
    impuesto DECIMAL(12, 2) NOT NULL,
    total DECIMAL(12, 2) NOT NULL,
    metodo_pago VARCHAR(30) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'COMPLETADA',
    observacion VARCHAR(200),
    CONSTRAINT fk_ventas_usuarios FOREIGN KEY (usuario_id)
        REFERENCES usuarios (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_ventas_clientes FOREIGN KEY (cliente_id)
        REFERENCES clientes (id)
        ON UPDATE CASCADE
        ON DELETE SET NULL
);

CREATE TABLE detalle_ventas (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    venta_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(12, 2) NOT NULL,
    impuesto_unitario DECIMAL(12, 2) NOT NULL DEFAULT 0,
    subtotal_linea DECIMAL(12, 2) NOT NULL,
    total_linea DECIMAL(12, 2) NOT NULL,
    CONSTRAINT fk_detalle_ventas_ventas FOREIGN KEY (venta_id)
        REFERENCES ventas (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_detalle_ventas_productos FOREIGN KEY (producto_id)
        REFERENCES productos (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

CREATE TABLE documentos_stock (
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

CREATE TABLE movimientos_inventario (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    producto_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    tipo_movimiento VARCHAR(20) NOT NULL,
    cantidad INT NOT NULL,
    stock_anterior INT NOT NULL,
    stock_nuevo INT NOT NULL,
    costo_unitario DECIMAL(12, 2) NULL,
    subtotal_movimiento DECIMAL(12, 2) NULL,
    lote VARCHAR(60) NULL,
    fecha_vencimiento DATE NULL,
    motivo VARCHAR(255),
    referencia_tipo VARCHAR(30) NULL,
    referencia_id BIGINT NULL,
    fecha_movimiento DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_movimientos_productos FOREIGN KEY (producto_id)
        REFERENCES productos (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_movimientos_usuarios FOREIGN KEY (usuario_id)
        REFERENCES usuarios (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

CREATE INDEX idx_usuarios_username ON usuarios (username);
CREATE INDEX idx_productos_codigo ON productos (codigo);
CREATE INDEX idx_productos_nombre ON productos (nombre);
CREATE INDEX idx_clientes_nombre_documento ON clientes (nombre, documento);
CREATE INDEX idx_ventas_fecha ON ventas (fecha_venta);
CREATE INDEX idx_documentos_stock_fecha ON documentos_stock (creado_en);
CREATE INDEX idx_movimientos_fecha_producto ON movimientos_inventario (fecha_movimiento, producto_id);
CREATE INDEX idx_movimientos_referencia ON movimientos_inventario (referencia_tipo, referencia_id);

INSERT INTO roles (nombre, descripcion) VALUES
('admin', 'Administrador del sistema'),
('cajero', 'Usuario de caja');

INSERT INTO usuarios (rol_id, username, password_hash, nombre_completo) VALUES
(1, 'admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'Administrador Principal'),
(2, 'cajero', '1ed4353e845e2e537e017c0fac3a0d402d231809b7989e90da15191c1148a93f', 'Cajero General');

INSERT INTO categorias (nombre, descripcion) VALUES
('Abarrotes', 'Productos de consumo diario'),
('Bebidas', 'Gaseosas, jugos y agua'),
('Limpieza', 'Productos de aseo');

INSERT INTO proveedores (nombre, telefono, correo, direccion) VALUES
('Distribuidora Central', '987456321', 'ventas@distribuidoracentral.pe', 'Av. Abancay 542, Lima'),
('Bebidas del Norte', '989114725', 'contacto@bebidasdelnorte.pe', 'Av. Tupac Amaru 1450, Los Olivos');

INSERT INTO clientes (nombre, telefono, documento, direccion) VALUES
('Consumidor Final', '', '00000000', 'Venta de mostrador'),
('Ana Morales', '956782341', '74215638', 'Jr. Los Cedros 245, Cercado de Lima');

INSERT INTO productos (categoria_id, proveedor_id, codigo, nombre, descripcion, precio_compra, precio_venta, stock, stock_minimo, estado) VALUES
(1, 1, 'PRD-001', 'Arroz 1Kg', 'Arroz blanco premium', 3.20, 4.20, 35, 10, 'ACTIVO'),
(2, 2, 'PRD-002', 'Gaseosa Cola 500ml', 'Bebida gaseosa', 1.80, 2.80, 24, 8, 'ACTIVO'),
(3, 1, 'PRD-003', 'Detergente 500g', 'Detergente en polvo', 4.50, 6.90, 12, 5, 'ACTIVO');

INSERT INTO documentos_stock (
    tipo_registro, proveedor_id, usuario_id, tipo_documento, serie, correlativo,
    almacen_destino, tipo_ajuste, motivo_general, observacion, total_lineas, total_unidades, monto_total
) VALUES (
    'AJUSTE', NULL, 1, NULL, NULL, NULL,
    'Almacen principal', 'Regularizacion', 'Carga inicial', 'Carga inicial del inventario base', 3, 71, 209.20
);

INSERT INTO movimientos_inventario (
    producto_id, usuario_id, tipo_movimiento, cantidad, stock_anterior, stock_nuevo,
    costo_unitario, subtotal_movimiento, lote, fecha_vencimiento, motivo, referencia_tipo, referencia_id
) VALUES
(1, 1, 'ENTRADA', 35, 0, 35, 3.20, 112.00, NULL, NULL, 'Ajuste Regularizacion - Carga inicial', 'DOCUMENTO_STOCK', 1),
(2, 1, 'ENTRADA', 24, 0, 24, 1.80, 43.20, NULL, NULL, 'Ajuste Regularizacion - Carga inicial', 'DOCUMENTO_STOCK', 1),
(3, 1, 'ENTRADA', 12, 0, 12, 4.50, 54.00, NULL, NULL, 'Ajuste Regularizacion - Carga inicial', 'DOCUMENTO_STOCK', 1);
