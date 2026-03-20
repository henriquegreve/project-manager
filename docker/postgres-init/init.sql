-- init.sql
-- Remove constraints antigas que apontavam para a tabela `members`.
-- O project-manager-api atual não mantém entidade/tabela `members`,
-- armazenando apenas os IDs dos membros via integração com members-api.

DO $$
BEGIN
  IF to_regclass('members') IS NOT NULL THEN
    -- Remove FKs em projects que referenciam members
    FOR r IN
      SELECT conname
      FROM pg_constraint
      WHERE contype = 'f'
        AND conrelid = to_regclass('projects')
        AND confrelid = to_regclass('members')
    LOOP
      EXECUTE format('ALTER TABLE projects DROP CONSTRAINT %I;', r.conname);
    END LOOP;
  END IF;
END $$;

DO $$
BEGIN
  IF to_regclass('members') IS NOT NULL THEN
    -- Remove FKs em project_members que referenciam members
    FOR r IN
      SELECT conname
      FROM pg_constraint
      WHERE contype = 'f'
        AND conrelid = to_regclass('project_members')
        AND confrelid = to_regclass('members')
    LOOP
      EXECUTE format('ALTER TABLE project_members DROP CONSTRAINT %I;', r.conname);
    END LOOP;
  END IF;
END $$;

-- Remove a tabela se ainda existir
DROP TABLE IF EXISTS members CASCADE;

