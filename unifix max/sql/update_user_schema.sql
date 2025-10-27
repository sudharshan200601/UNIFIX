USE unifix_db;

-- Add register_no, address, and phone columns to users table if they don't exist
-- For MySQL 5.7 and later that support IF NOT EXISTS
ALTER TABLE users
ADD COLUMN IF NOT EXISTS register_no VARCHAR(20) AFTER email,
ADD COLUMN IF NOT EXISTS address VARCHAR(255) AFTER register_no,
ADD COLUMN IF NOT EXISTS phone VARCHAR(20) AFTER address;

-- For MySQL 5.6 and earlier that don't support IF NOT EXISTS
-- Uncomment the following block if needed
/*
-- Check and add register_no
SET @dbname = 'unifix_db';
SET @tablename = 'users';
SET @columnname = 'register_no';
SET @columntype = 'VARCHAR(20)';

SET @preparedStatement = (
    SELECT IF(
        (
            SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = @dbname
            AND TABLE_NAME = @tablename
            AND COLUMN_NAME = @columnname
        ) > 0,
        'SELECT 1',
        CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' ', @columntype, ' AFTER email')
    )
);

PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Check and add address
SET @columnname = 'address';
SET @columntype = 'VARCHAR(255)';

SET @preparedStatement = (
    SELECT IF(
        (
            SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = @dbname
            AND TABLE_NAME = @tablename
            AND COLUMN_NAME = @columnname
        ) > 0,
        'SELECT 1',
        CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' ', @columntype, ' AFTER register_no')
    )
);

PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Check and add phone
SET @columnname = 'phone';
SET @columntype = 'VARCHAR(20)';

SET @preparedStatement = (
    SELECT IF(
        (
            SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_SCHEMA = @dbname
            AND TABLE_NAME = @tablename
            AND COLUMN_NAME = @columnname
        ) > 0,
        'SELECT 1',
        CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' ', @columntype, ' AFTER address')
    )
);

PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;
*/