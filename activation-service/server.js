import express from 'express';
import helmet from 'helmet';
import rateLimit from 'express-rate-limit';
import crypto from 'node:crypto';

const app = express();
app.disable('x-powered-by');
app.use(helmet());
app.use(express.json({ limit: '16kb' }));
app.use(rateLimit({ windowMs: 60_000, limit: 60, standardHeaders: 'draft-8', legacyHeaders: false }));

const PORT = Number(process.env.PORT || 3000);
const ACTIVATION_PEPPER = process.env.ACTIVATION_PEPPER || '';

function digest(value) {
  if (!ACTIVATION_PEPPER) throw new Error('ACTIVATION_PEPPER is not configured');
  return crypto.createHmac('sha256', ACTIVATION_PEPPER).update(String(value)).digest('hex');
}

function clean(value, max = 256) {
  return typeof value === 'string' ? value.trim().slice(0, max) : '';
}

app.get('/health', (_req, res) => {
  res.status(200).json({ ok: true, service: 'activation-service', version: '1.0.0' });
});

app.post('/api/activate', (req, res) => {
  const code = clean(req.body?.code, 128);
  const installationId = clean(req.body?.installationId, 256);
  if (!code || !installationId) return res.status(400).json({ ok: false, error: 'invalid_request' });
  if (!ACTIVATION_PEPPER) return res.status(503).json({ ok: false, error: 'service_not_configured' });

  // No real activation codes are stored in source code. Database persistence is added next.
  const codeHash = digest(code);
  const installationHash = digest(installationId);
  void codeHash; void installationHash;
  return res.status(503).json({ ok: false, error: 'activation_store_not_configured' });
});

app.post('/api/verify', (req, res) => {
  const installationId = clean(req.body?.installationId, 256);
  if (!installationId) return res.status(400).json({ ok: false, error: 'invalid_request' });
  if (!ACTIVATION_PEPPER) return res.status(503).json({ ok: false, error: 'service_not_configured' });
  void digest(installationId);
  return res.status(503).json({ ok: false, error: 'activation_store_not_configured' });
});

app.use((_req, res) => res.status(404).json({ ok: false, error: 'not_found' }));

app.listen(PORT, '0.0.0.0', () => {
  console.log(`activation-service listening on port ${PORT}`);
});
