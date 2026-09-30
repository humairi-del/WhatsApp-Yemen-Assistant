import express from 'express';
import helmet from 'helmet';
import rateLimit from 'express-rate-limit';
import crypto from 'node:crypto';
import pg from 'pg';

const { Pool } = pg;
const app = express();
app.disable('x-powered-by');
app.use(helmet());
app.use(express.json({ limit: '16kb' }));
app.use(rateLimit({ windowMs: 60_000, limit: 60, standardHeaders: 'draft-8', legacyHeaders: false }));

const PORT = Number(process.env.PORT || 3000);
const ACTIVATION_PEPPER = process.env.ACTIVATION_PEPPER || '';
const DATABASE_URL = process.env.DATABASE_URL || '';
const pool = DATABASE_URL ? new Pool({ connectionString: DATABASE_URL }) : null;

function digest(value) {
  if (!ACTIVATION_PEPPER) throw new Error('ACTIVATION_PEPPER is not configured');
  return crypto.createHmac('sha256', ACTIVATION_PEPPER).update(String(value)).digest('hex');
}

function clean(value, max = 256) {
  return typeof value === 'string' ? value.trim().slice(0, max) : '';
}

async function initDatabase() {
  if (!pool) throw new Error('DATABASE_URL is not configured');
  await pool.query(`
    CREATE TABLE IF NOT EXISTS activation_codes (
      id BIGSERIAL PRIMARY KEY,
      code_hash TEXT UNIQUE NOT NULL,
      enabled BOOLEAN NOT NULL DEFAULT TRUE,
      max_devices INTEGER NOT NULL DEFAULT 1 CHECK (max_devices > 0),
      expires_at TIMESTAMPTZ,
      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );
    CREATE TABLE IF NOT EXISTS activations (
      id BIGSERIAL PRIMARY KEY,
      code_id BIGINT NOT NULL REFERENCES activation_codes(id) ON DELETE CASCADE,
      installation_hash TEXT NOT NULL,
      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
      last_verified_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
      UNIQUE (code_id, installation_hash)
    );
    CREATE INDEX IF NOT EXISTS activations_installation_idx ON activations (installation_hash);
  `);
}

app.get('/health', async (_req, res) => {
  try {
    if (!pool) throw new Error('database_not_configured');
    await pool.query('SELECT 1');
    res.status(200).json({ ok: true, service: 'activation-service', version: '1.1.0', database: 'ok' });
  } catch {
    res.status(503).json({ ok: false, service: 'activation-service', database: 'unavailable' });
  }
});

app.post('/api/activate', async (req, res) => {
  const code = clean(req.body?.code, 128);
  const installationId = clean(req.body?.installationId, 256);
  if (!code || !installationId) return res.status(400).json({ ok: false, error: 'invalid_request' });
  if (!ACTIVATION_PEPPER || !pool) return res.status(503).json({ ok: false, error: 'service_not_configured' });

  const codeHash = digest(code);
  const installationHash = digest(installationId);
  const client = await pool.connect();
  try {
    await client.query('BEGIN');
    const codeResult = await client.query(
      `SELECT id, enabled, max_devices, expires_at
       FROM activation_codes WHERE code_hash = $1 FOR UPDATE`,
      [codeHash]
    );
    if (!codeResult.rowCount) {
      await client.query('ROLLBACK');
      return res.status(401).json({ ok: false, error: 'invalid_code' });
    }
    const row = codeResult.rows[0];
    if (!row.enabled) {
      await client.query('ROLLBACK');
      return res.status(403).json({ ok: false, error: 'code_disabled' });
    }
    if (row.expires_at && new Date(row.expires_at) <= new Date()) {
      await client.query('ROLLBACK');
      return res.status(403).json({ ok: false, error: 'code_expired' });
    }

    const existing = await client.query(
      'SELECT id FROM activations WHERE code_id = $1 AND installation_hash = $2',
      [row.id, installationHash]
    );
    if (!existing.rowCount) {
      const count = await client.query('SELECT COUNT(*)::int AS count FROM activations WHERE code_id = $1', [row.id]);
      if (count.rows[0].count >= row.max_devices) {
        await client.query('ROLLBACK');
        return res.status(409).json({ ok: false, error: 'device_limit_reached' });
      }
      await client.query(
        'INSERT INTO activations (code_id, installation_hash) VALUES ($1, $2)',
        [row.id, installationHash]
      );
    } else {
      await client.query(
        'UPDATE activations SET last_verified_at = NOW() WHERE code_id = $1 AND installation_hash = $2',
        [row.id, installationHash]
      );
    }
    await client.query('COMMIT');
    return res.status(200).json({ ok: true, activated: true });
  } catch (error) {
    await client.query('ROLLBACK').catch(() => {});
    console.error('activation request failed');
    return res.status(500).json({ ok: false, error: 'server_error' });
  } finally {
    client.release();
  }
});

app.post('/api/verify', async (req, res) => {
  const installationId = clean(req.body?.installationId, 256);
  if (!installationId) return res.status(400).json({ ok: false, error: 'invalid_request' });
  if (!ACTIVATION_PEPPER || !pool) return res.status(503).json({ ok: false, error: 'service_not_configured' });

  try {
    const installationHash = digest(installationId);
    const result = await pool.query(
      `SELECT a.id
       FROM activations a
       JOIN activation_codes c ON c.id = a.code_id
       WHERE a.installation_hash = $1
         AND c.enabled = TRUE
         AND (c.expires_at IS NULL OR c.expires_at > NOW())
       LIMIT 1`,
      [installationHash]
    );
    if (!result.rowCount) return res.status(401).json({ ok: false, active: false });
    await pool.query('UPDATE activations SET last_verified_at = NOW() WHERE id = $1', [result.rows[0].id]);
    return res.status(200).json({ ok: true, active: true });
  } catch {
    console.error('verification request failed');
    return res.status(500).json({ ok: false, error: 'server_error' });
  }
});

app.use((_req, res) => res.status(404).json({ ok: false, error: 'not_found' }));

initDatabase()
  .then(() => {
    app.listen(PORT, '0.0.0.0', () => console.log(`activation-service listening on port ${PORT}`));
  })
  .catch(() => {
    console.error('activation-service database initialization failed');
    process.exit(1);
  });
