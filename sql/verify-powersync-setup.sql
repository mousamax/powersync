-- PowerSync PostgreSQL Verification Script
-- Run this manually in pgAdmin or psql to verify the setup

-- ============================================
-- 1. CHECK LOGICAL REPLICATION
-- ============================================
SHOW wal_level;
-- Expected: logical


-- ============================================
-- 2. CHECK POWERSYNC USER
-- ============================================
SELECT 
    rolname as username,
    rolreplication as has_replication,
    rolbypassrls as bypass_rls,
    rolcanlogin as can_login
FROM pg_roles 
WHERE rolname = 'powersync_role';
-- Expected: 1 row with all values = true


-- ============================================
-- 3. CHECK POWERSYNC PUBLICATION
-- ============================================
SELECT 
    pubname,
    puballtables,
    pubinsert,
    pubupdate,
    pubdelete
FROM pg_publication 
WHERE pubname = 'powersync';
-- Expected: 1 row with pubname = 'powersync'


-- ============================================
-- 4. LIST TABLES IN PUBLICATION
-- ============================================
SELECT 
    schemaname,
    tablename
FROM pg_publication_tables 
WHERE pubname = 'powersync'
ORDER BY tablename;
-- Expected: family, member, task, task_list


-- ============================================
-- 5. CHECK REPLICATION SLOTS
-- ============================================
SELECT 
    slot_name,
    slot_type,
    database,
    active,
    restart_lsn
FROM pg_replication_slots;
-- This will show slots created by PowerSync service


-- ============================================
-- 6. CHECK POWERSYNC USER PERMISSIONS
-- ============================================
-- Check table-level permissions
SELECT 
    grantee,
    table_schema,
    table_name,
    privilege_type
FROM information_schema.table_privileges
WHERE grantee = 'powersync_role'
ORDER BY table_name;
-- Expected: SELECT on all tables


-- Check schema-level permissions  
SELECT 
    nspname as schema_name,
    r.rolname as role_name,
    has_schema_privilege(r.oid, n.oid, 'USAGE') as usage_privilege
FROM pg_namespace n
CROSS JOIN pg_roles r
WHERE r.rolname = 'powersync_role'
  AND n.nspname = 'public';
-- Expected: usage_privilege = true


-- ============================================
-- 7. CHECK DEFAULT PRIVILEGES
-- ============================================
SELECT 
    defaclrole::regrole as grantor,
    defaclnamespace::regnamespace as schema,
    defaclobjtype as object_type,
    defaclacl as privileges
FROM pg_default_acl
WHERE defaclrole::regrole::text = 'powersync_role'
   OR 'powersync_role' = ANY(string_to_array(defaclacl::text, ','));
-- This shows if future tables will get SELECT permission


-- ============================================
-- 8. MONITOR REPLICATION LAG
-- ============================================
SELECT 
    slot_name,
    pg_size_pretty(pg_wal_lsn_diff(pg_current_wal_lsn(), restart_lsn)) as replication_lag,
    pg_size_pretty(pg_wal_lsn_diff(pg_current_wal_lsn(), confirmed_flush_lsn)) as write_lag,
    active
FROM pg_replication_slots
WHERE slot_type = 'logical';
-- Monitor for growing lag


-- ============================================
-- 9. CHECK DATABASE TABLES
-- ============================================
SELECT 
    schemaname,
    tablename,
    tableowner
FROM pg_tables 
WHERE schemaname = 'public'
ORDER BY tablename;
-- Expected: family, member, task, task_list, databasechangelog, databasechangeloglock


-- ============================================
-- 10. TEST POWERSYNC USER CONNECTION
-- ============================================
-- Run this from command line:
-- psql "postgresql://powersync_role:powersync_secure_password_change_in_production@localhost:4321/postgres" -c "SELECT current_user, session_user;"
-- Expected: Should connect successfully and show powersync_role


-- ============================================
-- TROUBLESHOOTING COMMANDS
-- ============================================

-- If you need to recreate the publication:
-- DROP PUBLICATION IF EXISTS powersync;
-- CREATE PUBLICATION powersync FOR TABLE family, member, task_list, task;

-- If you need to add a table to publication:
-- ALTER PUBLICATION powersync ADD TABLE new_table_name;

-- If you need to remove a table from publication:
-- ALTER PUBLICATION powersync DROP TABLE table_name;

-- If you need to change PowerSync user password:
-- ALTER ROLE powersync_role WITH PASSWORD 'new_password';

-- View active WAL senders (replication connections):
-- SELECT * FROM pg_stat_replication;

-- View publication details:
-- \dRp+ powersync
