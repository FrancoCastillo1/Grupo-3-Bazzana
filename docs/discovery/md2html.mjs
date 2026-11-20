// Conversor mínimo de Markdown a HTML para el informe de Discovery (sin dependencias).
// Uso: node md2html.mjs <entrada.md> <salida.html>
import fs from "node:fs";

const [, , inPath, outPath] = process.argv;
const src = fs.readFileSync(inPath, "utf8").replace(/\r\n/g, "\n");

const esc = (s) => s.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
function inline(s) {
  s = esc(s);
  s = s.replace(/`([^`]+)`/g, "<code>$1</code>");
  s = s.replace(/\[([^\]]+)\]\((https?:[^)]+)\)/g, '<a href="$2">$1</a>');
  s = s.replace(/(^|[\s(·])(https?:\/\/[^\s<)|·]+)/g, '$1<a href="$2">$2</a>');
  s = s.replace(/\*\*([^*]+)\*\*/g, "<strong>$1</strong>");
  s = s.replace(/(^|[^*])\*([^*\s][^*]*)\*/g, "$1<em>$2</em>");
  return s;
}

const lines = src.split("\n");
const out = [];
let i = 0;
let firstH2 = true;
while (i < lines.length) {
  const l = lines[i];
  if (/^\s*$/.test(l)) { i++; continue; }
  if (/^---\s*$/.test(l)) { out.push("<hr>"); i++; continue; }
  const h = l.match(/^(#{1,4})\s+(.*)$/);
  if (h) {
    const lvl = h[1].length;
    if (lvl === 2 && !firstH2) out.push('<div class="pb"></div>');
    if (lvl === 2) firstH2 = false;
    out.push(`<h${lvl}>${inline(h[2])}</h${lvl}>`);
    i++; continue;
  }
  if (l.startsWith("|")) {
    const rows = [];
    while (i < lines.length && lines[i].startsWith("|")) { rows.push(lines[i]); i++; }
    const cells = (r) => r.replace(/^\||\|$/g, "").split("|").map((c) => c.trim());
    let html = "<table><thead><tr>" + cells(rows[0]).map((c) => `<th>${inline(c)}</th>`).join("") + "</tr></thead><tbody>";
    for (const r of rows.slice(2)) html += "<tr>" + cells(r).map((c) => `<td>${inline(c)}</td>`).join("") + "</tr>";
    out.push(html + "</tbody></table>");
    continue;
  }
  if (l.startsWith(">")) {
    const buf = [];
    while (i < lines.length && lines[i].startsWith(">")) { buf.push(lines[i].replace(/^>\s?/, "")); i++; }
    out.push(`<blockquote>${inline(buf.join(" "))}</blockquote>`);
    continue;
  }
  if (/^\s*([-*]|\d+\.)\s+/.test(l)) {
    // listas con un nivel de anidamiento (sangría >= 2 espacios)
    const ordered = /^\s*\d+\./.test(l);
    let html = ordered ? "<ol>" : "<ul>";
    let inSub = false;
    while (i < lines.length && /^\s*([-*]|\d+\.)\s+/.test(lines[i])) {
      const m = lines[i].match(/^(\s*)([-*]|\d+\.)\s+(.*)$/);
      const sub = m[1].length >= 2;
      if (sub && !inSub) { html = html.replace(/<\/li>$/, "") + "<ul>"; inSub = true; }
      if (!sub && inSub) { html += "</ul></li>"; inSub = false; }
      html += `<li>${inline(m[3])}</li>`;
      i++;
    }
    if (inSub) html += "</ul></li>";
    out.push(html + (ordered ? "</ol>" : "</ul>"));
    continue;
  }
  const buf = [];
  while (i < lines.length && !/^\s*$/.test(lines[i]) && !/^(#|\||>|---|\s*([-*]|\d+\.)\s)/.test(lines[i])) { buf.push(lines[i]); i++; }
  out.push(`<p>${buf.map(inline).join("<br>")}</p>`);
}

const css = `
@page { size: A4 landscape; margin: 14mm 12mm; }
body { font-family: "Segoe UI", Arial, sans-serif; font-size: 10pt; color: #1a1a1a; line-height: 1.4; background: #fff; }
h1 { font-size: 22pt; margin: 30mm 0 4mm; color: #0b3d5c; }
h2 { font-size: 15pt; color: #0b3d5c; border-bottom: 2px solid #0b3d5c; padding-bottom: 2px; }
h3 { font-size: 12pt; color: #134e6f; margin-top: 10px; }
.pb { break-after: page; }
table { border-collapse: collapse; width: 100%; font-size: 7.2pt; margin: 6px 0; }
th, td { border: 1px solid #9aa9b3; padding: 3px 4px; vertical-align: top; text-align: left; word-break: break-word; }
th { background: #e3edf3; }
td:first-child, th:first-child { white-space: nowrap; word-break: normal; }
tr { break-inside: avoid; }
blockquote { border-left: 4px solid #c8a200; background: #fff8dc; margin: 8px 0; padding: 6px 10px; }
code { font-family: Consolas, monospace; font-size: 9pt; background: #f2f2f2; padding: 0 2px; }
a { color: #0b5c8a; text-decoration: none; }
hr { border: 0; border-top: 1px solid #ccc; }
`;
fs.writeFileSync(outPath, `<!doctype html><html lang="es"><head><meta charset="utf-8"><title>Informe de Discovery</title><style>${css}</style></head><body>${out.join("\n")}</body></html>`);
console.log("ok", outPath);
