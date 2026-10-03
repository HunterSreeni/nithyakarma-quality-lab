// Builds site/index.html: one page comparing the latest Playwright and Selenium runs.
// Inputs:  results/playwright.json (Playwright json reporter)
//          selenium-java/target/surefire-reports/TEST-TestSuite.xml (Surefire)
//          dashboard/test-cases.json (TC id -> test name in each framework)
// Usage:   node dashboard/build.mjs   (from the repo root)
import fs from 'fs';
import path from 'path';

const root = path.resolve(path.dirname(new URL(import.meta.url).pathname.replace(/^\/([A-Za-z]:)/, '$1')), '..');
const read = p => fs.existsSync(path.join(root, p)) ? fs.readFileSync(path.join(root, p), 'utf8') : null;
const esc = s => String(s).replace(/[&<>"]/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' }[c]));

const cases = JSON.parse(read('dashboard/test-cases.json'));

// ---- Playwright: title -> { status, ms } ----
const pw = new Map();
let pwMeta = null;
const pwJson = read('results/playwright.json');
if (pwJson) {
    const report = JSON.parse(pwJson);
    const walk = suite => {
        for (const spec of suite.specs ?? []) {
            for (const t of spec.tests) {
                const last = t.results.at(-1);
                const status = t.status === 'skipped' ? 'skipped' : t.status === 'flaky' ? 'flaky' : last?.status === 'passed' ? 'passed' : 'failed';
                pw.set(spec.title, { status, ms: last?.duration ?? 0 });
            }
        }
        (suite.suites ?? []).forEach(walk);
    };
    report.suites.forEach(walk);
    pwMeta = { wallMs: report.stats.duration, started: report.stats.startTime };
}

// ---- Selenium (Surefire XML): Class.method[params] -> { status, ms } ----
const se = new Map();
let seMeta = null;
const seXml = read('selenium-java/target/surefire-reports/TEST-TestSuite.xml');
if (seXml) {
    const suite = seXml.match(/<testsuite [^>]*time="([\d.]+)"/);
    seMeta = { wallMs: suite ? Math.round(parseFloat(suite[1]) * 1000) : 0 };
    const re = /<testcase name="([^"]*)" classname="([^"]*)" time="([\d.]+)"(\/>|>([\s\S]*?)<\/testcase>)/g;
    for (const m of seXml.matchAll(re)) {
        const key = `${m[2].split('.').pop()}.${m[1].replace(/&amp;/g, '&')}`;
        const body = m[5] ?? '';
        const status = /<skipped/.test(body) ? 'skipped' : /<(failure|error)/.test(body) ? 'failed' : 'passed';
        se.set(key, { status, ms: Math.round(parseFloat(m[3]) * 1000) });
    }
}

const findSe = name => {
    for (const [key, value] of se) if (key === name || key.startsWith(name)) return value;
    return null;
};

const rows = cases.map(c => ({ ...c, pw: pw.get(c.playwright) ?? null, se: findSe(c.selenium) }));

const tally = side => {
    const t = { passed: 0, failed: 0, skipped: 0, flaky: 0, missing: 0 };
    for (const r of rows) t[r[side]?.status ?? 'missing']++;
    return t;
};
const pwT = tally('pw');
const seT = tally('se');
const both = rows.filter(r => r.pw && r.se);
const agree = both.filter(r => r.pw.status === r.se.status).length;

const secs = ms => `${(ms / 1000).toFixed(ms < 10000 ? 1 : 0)}s`;
const pill = res => res
    ? `<span class="pill ${res.status}">${res.status}</span><span class="dur">${secs(res.ms)}</span>`
    : '<span class="pill missing">not run</span>';

const bar = t => {
    const total = Object.values(t).reduce((a, b) => a + b, 0) || 1;
    return `<div class="bar">${['passed', 'flaky', 'failed', 'skipped', 'missing']
        .filter(k => t[k]).map(k => `<span class="${k}" style="width:${(t[k] / total) * 100}%" title="${k}: ${t[k]}"></span>`).join('')}</div>`;
};

const card = (name, t, meta, report) => `
  <section class="card">
    <h2>${name}</h2>
    <div class="big">${t.passed}<small> / ${rows.length} passed</small></div>
    ${bar(t)}
    <dl>
      <div><dt>Failed</dt><dd>${t.failed}</dd></div>
      <div><dt>Skipped</dt><dd>${t.skipped}</dd></div>
      <div><dt>Wall time</dt><dd>${meta ? secs(meta.wallMs) : '-'}</dd></div>
    </dl>
    <a class="report" href="${report}" target="_blank" rel="noopener">Full ${name} report</a>
  </section>`;

let lastArea = '';
const tableRows = rows.map(r => {
    const head = r.area !== lastArea ? `<tr class="area"><th colspan="4">${esc(r.area)}</th></tr>` : '';
    lastArea = r.area;
    const differs = r.pw && r.se && r.pw.status !== r.se.status ? ' class="differs"' : '';
    return `${head}<tr${differs}><td class="id">${r.id}</td><td>${esc(r.title)}</td><td>${pill(r.pw)}</td><td>${pill(r.se)}</td></tr>`;
}).join('\n');

const repo = process.env.GITHUB_REPOSITORY;
const runUrl = repo && process.env.GITHUB_RUN_ID ? `${process.env.GITHUB_SERVER_URL}/${repo}/actions/runs/${process.env.GITHUB_RUN_ID}` : null;
const sha = (process.env.GITHUB_SHA ?? '').slice(0, 7);
const target = process.env.BASE_URL ? new URL(process.env.BASE_URL).host : null;
const when = new Date().toLocaleString('en-IN', { timeZone: 'Asia/Kolkata', dateStyle: 'medium', timeStyle: 'short' });

const html = `<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Nithyakarma Quality Lab</title>
<meta name="description" content="Playwright (TypeScript) vs Selenium (Java) running the same 39 test cases against the live Nithyakarma app.">
<style>
  :root { --bg:#f7f6f2; --panel:#fff; --ink:#1d1d1b; --muted:#6b6a65; --line:#e4e2db;
          --pass:#2f7d4f; --fail:#c0392b; --skip:#a3a29b; --flaky:#d68910; --accent:#8a4b12; }
  @media (prefers-color-scheme: dark) {
    :root { --bg:#141413; --panel:#1e1e1c; --ink:#ecebe6; --muted:#9d9b94; --line:#2f2e2b;
            --pass:#4cae74; --fail:#e5604f; --skip:#6f6e68; --flaky:#e8a33d; --accent:#e0a463; }
  }
  * { box-sizing: border-box; }
  body { margin:0; background:var(--bg); color:var(--ink); font:15px/1.5 system-ui, -apple-system, "Segoe UI", sans-serif; }
  main { max-width: 980px; margin: 0 auto; padding: 32px 16px 64px; }
  header p { color: var(--muted); margin: 4px 0 0; }
  h1 { font-size: 1.7rem; margin: 0; letter-spacing: -0.01em; }
  h1 span { color: var(--accent); }
  .meta { font-size: .85rem; color: var(--muted); margin-top: 10px; }
  .meta a { color: inherit; }
  .cards { display:grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap:14px; margin:24px 0; }
  .card { background:var(--panel); border:1px solid var(--line); border-radius:12px; padding:18px; }
  .card h2 { font-size:.8rem; text-transform:uppercase; letter-spacing:.08em; color:var(--muted); margin:0 0 6px; }
  .big { font-size:2.2rem; font-weight:650; font-variant-numeric: tabular-nums; }
  .big small { font-size:.95rem; font-weight:400; color:var(--muted); }
  .bar { display:flex; height:8px; border-radius:4px; overflow:hidden; background:var(--line); margin:10px 0 12px; }
  .bar span { display:block; }
  .bar .passed { background:var(--pass); } .bar .failed { background:var(--fail); }
  .bar .skipped, .bar .missing { background:var(--skip); } .bar .flaky { background:var(--flaky); }
  dl { display:flex; gap:18px; margin:0; } dl div { min-width:0; }
  dt { font-size:.75rem; color:var(--muted); } dd { margin:0; font-weight:600; font-variant-numeric: tabular-nums; }
  .report { display:inline-block; margin-top:14px; font-size:.85rem; color:var(--accent); }
  h3 { margin: 36px 0 10px; font-size: 1.1rem; }
  .table-wrap { overflow-x:auto; background:var(--panel); border:1px solid var(--line); border-radius:12px; }
  table { width:100%; border-collapse:collapse; font-size:.9rem; }
  th, td { text-align:left; padding:9px 12px; border-bottom:1px solid var(--line); vertical-align:middle; }
  thead th { font-size:.75rem; text-transform:uppercase; letter-spacing:.06em; color:var(--muted); }
  tr.area th { background:var(--bg); font-size:.8rem; color:var(--accent); }
  tr.differs td { background: color-mix(in srgb, var(--flaky) 10%, transparent); }
  td.id { font-family: ui-monospace, Consolas, monospace; color:var(--muted); white-space:nowrap; }
  .pill { display:inline-block; padding:1px 8px; border-radius:999px; font-size:.75rem; font-weight:600; color:#fff; }
  .pill.passed { background:var(--pass); } .pill.failed { background:var(--fail); }
  .pill.skipped, .pill.missing { background:var(--skip); } .pill.flaky { background:var(--flaky); }
  .dur { margin-left:8px; color:var(--muted); font-size:.8rem; font-variant-numeric: tabular-nums; }
  .compare td:first-child { font-weight:600; white-space:nowrap; }
  footer { margin-top:40px; font-size:.8rem; color:var(--muted); }
</style>
</head>
<body>
<main>
<header>
  <h1>Nithyakarma <span>Quality Lab</span></h1>
  <p>The same ${rows.length} test cases, written twice: Playwright (TypeScript) and Selenium (Java + TestNG), against the live app.</p>
  <div class="meta">Last run ${esc(when)} IST${sha ? ` · commit ${sha}` : ''}${runUrl ? ` · <a href="${runUrl}" target="_blank" rel="noopener">CI run</a>` : ''}</div>
  ${target ? `<div class="meta">Target: ${esc(target)}${target.endsWith('.netlify.app') ? ' (Netlify origin: the production build without the Cloudflare proxy, used as the CI test URL)' : ''}</div>` : ''}
</header>

<div class="cards">
  ${card('Playwright', pwT, pwMeta, 'playwright/index.html')}
  ${card('Selenium', seT, seMeta, 'selenium/index.html')}
  <section class="card">
    <h2>Parity</h2>
    <div class="big">${agree}<small> / ${both.length} agree</small></div>
    ${bar({ passed: agree, flaky: both.length - agree })}
    <dl><div><dt>Test cases</dt><dd>${rows.length}</dd></div><div><dt>Same result in both</dt><dd>${both.length ? Math.round((agree / both.length) * 100) : 0}%</dd></div></dl>
  </section>
</div>

<h3>Test cases</h3>
<div class="table-wrap">
<table>
  <thead><tr><th>ID</th><th>Test case</th><th>Playwright</th><th>Selenium</th></tr></thead>
  <tbody>
${tableRows}
  </tbody>
</table>
</div>

<h3>How the two suites differ</h3>
<div class="table-wrap">
<table class="compare">
  <thead><tr><th></th><th>Playwright (TS)</th><th>Selenium (Java)</th></tr></thead>
  <tbody>
    <tr><td>Login</td><td>Setup project logs in once via Supabase magic link, saves storageState</td><td>Same magic link, session injected into localStorage per test</td></tr>
    <tr><td>Waiting</td><td>Auto-waiting locators and web-first assertions</td><td>Explicit WebDriverWait conditions in page objects</td></tr>
    <tr><td>Locators</td><td>Role and label based (getByRole, getByLabel)</td><td>CSS and XPath (no accessibility-tree locators)</td></tr>
    <tr><td>Execution</td><td>Parallel workers, projects for ordering</td><td>Sequential, TestNG &lt;test&gt; blocks for ordering</td></tr>
    <tr><td>Data-driven</td><td>Loop over a cases array</td><td>TestNG @DataProvider</td></tr>
    <tr><td>API tests</td><td>Built-in request fixture</td><td>REST Assured</td></tr>
    <tr><td>Captcha</td><td colspan="2">Not bypassed in the UI: kept as a negative test. Logged-in tests use the admin magic link instead.</td></tr>
  </tbody>
</table>
</div>

<footer>Source: <a href="https://github.com/HunterSreeni/nithyakarma-quality-lab">github.com/HunterSreeni/nithyakarma-quality-lab</a>. Opt-in data-writing tests are skipped in CI.</footer>
</main>
</body>
</html>
`;

fs.mkdirSync(path.join(root, 'site'), { recursive: true });
fs.writeFileSync(path.join(root, 'site/index.html'), html);
console.log(`site/index.html: Playwright ${pwT.passed}/${rows.length}, Selenium ${seT.passed}/${rows.length}, parity ${agree}/${both.length}`);
