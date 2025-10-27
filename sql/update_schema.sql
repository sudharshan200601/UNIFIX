USE unifix_db;

-- Add image_path column if it doesn't exist
-- For MySQL 5.7 and later that support IF NOT EXISTS
ALTER TABLE complaints
ADD COLUMN IF NOT EXISTS image_path VARCHAR(255) AFTER description;

-- For MySQL 5.6 and earlier that don't support IF NOT EXISTS
-- Uncomment the following block if needed
/*
SET @dbname = 'unifix_db';
SET @tablename = 'complaints';
SET @columnname = 'image_path';
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
        CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' ', @columntype, ' AFTER description')
    )
);

PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;
*/