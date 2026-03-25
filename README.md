# Latent Tagger

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Vue.js](https://img.shields.io/badge/Vue.js-3-4FC08D?style=for-the-badge&logo=vue.js&logoColor=white)
![Vite](https://img.shields.io/badge/Vite-5-646CFF?style=for-the-badge&logo=vite&logoColor=white)
![Electron](https://img.shields.io/badge/Electron-31-47848F?style=for-the-badge&logo=electron&logoColor=white)

A high-performance desktop utility for translating natural language descriptions into Danbooru-style prompt tags for Stable Diffusion. It provides **instant in-memory tag search** across 280,000+ tags, **natural language to tag translation**, and **per-model quality presets**—all wrapped in a premium, multi-themed glassmorphism interface.

---

## 📸 Interface

<p align="center">
  <img src="frontend/src/assets/screenshots/main-lt.png" width="800" alt="Main Interface">
  <br>
  <i>Sleek, frameless desktop UI with tag editor, autocomplete search, and translation panel.</i>
</p>

<p align="center">
  <img src="frontend/src/assets/screenshots/history-lt.png" width="800" alt="Prompt History">
  <br>
  <i><b>History:</b> Saves and restores past prompts instantly.</i>
</p>

---

## 🔐 Fast, Portable & Offline

Engineered from the ground up for AI artists who want precision prompting without friction.

* **Standalone Desktop App:** Runs as a single `.exe` (Windows), `.AppImage` (Linux), or `.dmg` (macOS). No installer required.
* **Bundled Runtime:** Includes a self-contained Java 21 environment. No system-wide Java installation is required.
* **Fully Offline Tag Engine:** The entire 280,000+ tag database runs in memory. No network requests, no cloud, no API keys required for tag search and validation.
* **Instant Autocomplete:** Binary search over sorted in-memory arrays delivers sub-millisecond tag lookup regardless of database size.
* **Alias Resolution:** Automatically resolves deprecated or alternate tag names to their canonical Danbooru equivalent.
* **Implication Hints:** When a tag is added, suggested implied tags are surfaced so you never miss a required companion tag.
* **Model-Aware Quality Presets:** One-click quality tag injection tuned specifically for Pony Diffusion, Illustrious / NoobAI, and generic SDXL / SD 1.5 models.

---

## ✨ Key Features

* **In-Memory Tag Database:** The full Danbooru tag list is loaded into three parallel sorted arrays at startup and never touches disk again. Prefix search uses `Arrays.binarySearch` with a forward scan — no SQLite, no FTS5, no query overhead.
* **Natural Language Translation:** Describe your scene in plain English. The translation engine normalises your input, strips stop words, and validates each token against the live tag database, returning only tags that Stable Diffusion will actually recognise.
* **Tag Validation with Visual Feedback:** Every tag in your prompt is validated in real time. Green means confirmed valid, red means unknown, amber means unvalidated — so you know exactly what will work before you generate.
* **Searchable Tag Database:** Search 280,000+ tags directly from the input field. Each result shows the tag name, category, and post count so you can judge popularity and specificity at a glance.
* **Quality Presets:** One-click positive and negative tag bundles for:
  * **Pony Diffusion** — `score_9`, `score_8_up`, `score_7_up` and matching negatives.
  * **Illustrious / NoobAI** — `masterpiece`, `best quality`, `very aesthetic` and matching negatives.
  * **Generic (SDXL / SD 1.5)** — `masterpiece`, `best quality`, `highly detailed` and matching negatives.
* **Prompt History:** Every completed prompt is saved to a local SQLite database. Load any past prompt with a single click and continue editing where you left off.
* **Copy-Ready Output:** The assembled prompt is always one click away from your clipboard, formatted exactly as a comma-separated tag string ready to paste into ComfyUI, Automatic1111, or any other frontend.

---

## 💻 System Requirements

* **OS:** Windows 10/11 (64-bit), Linux (AppImage), or macOS (11+).
* **Memory:** Efficient by design. The tag database occupies approximately 80–120 MB of RAM. Minimum 2 GB RAM overall.
* **Storage:** ~160 MB for the application including the bundled JRE and tag database.
* **Network:** Not required for normal use. An active connection is only needed if AI-assisted translation (cloud model) is enabled in Settings.

---

## 🛠️ Technical Architecture

Latent Tagger bypasses heavy frameworks to deliver raw native performance, combining an ultra-lightweight backend with a modern reactive frontend.

* **Backend (Java 21):**
  * **Raw HttpServer:** Uses `com.sun.net.httpserver` with zero heavy dependencies. No Spring Boot, no external HTTP libraries.
  * **Project Loom:** `VirtualThreadPerTaskExecutor` handles concurrent API requests with negligible overhead.
  * **In-Memory Tag Engine:** Three parallel primitive arrays (`String[]`, `int[]`, `int[]`) sorted at load time. `HashMap<String, String>` for alias resolution. `HashMap<String, List<String>>` for implication graph traversal.
  * **Ephemeral Port + Token IPC:** The backend binds to a random OS-assigned port on startup, writes `port:token` to a temp file, and prints `TAGGER_PORT=port:token` to stdout. Electron reads this signal and injects it into the renderer via ContextBridge — no hardcoded ports, no shared config files.
  * **SQLite History:** Prompt history is persisted to `~/.latenttagger/history.db` via `sqlite-jdbc`. Tag search and validation never touch the database.

* **Frontend (Vue 3 + Vite):**
  * **Composition API with Pinia:** Five dedicated stores (`backend`, `prompt`, `tag`, `translate`, `history`) with clean separation of concerns. No cross-store state leakage.
  * **Dynamic Theme Engine:** Hot-swappable CSS custom properties injected at runtime.
  * **Glassmorphism CSS:** Centralised, premium design system utilising `backdrop-filter`.

* **Desktop (Electron):**
  * **ContextBridge IPC:** Secure communication between the Vue renderer and native OS APIs (clipboard, window management).
  * **Process Orchestration:** Gracefully spins up and tears down the Java backend as a background child process with a clean shutdown signal on exit.

---

## 🚀 Getting Started

[![Download Latest Release](https://img.shields.io/badge/Download-Latest_Release-2ea44f?style=for-the-badge&logo=github&logoColor=white)](https://github.com/erroralex/latent-tagger/releases/latest)

1. **Download** the appropriate file for your OS:
   * **Windows:** `Latent Tagger X.X.X.exe`
   * **Linux:** `Latent Tagger-X.X.X.AppImage` (mark as executable with `chmod +x`)
   * **macOS:** `Latent Tagger-X.X.X.dmg`
2. **Run** the application. No installation is required.
3. **Type a description** in the translation panel, e.g. `"woman in red dress, forest background"`.
4. **Click Translate** to convert your description to validated Danbooru tags.
5. **Refine** using the tag search panel to add, remove, or swap individual tags.
6. **Click Copy** and paste your prompt directly into ComfyUI or Automatic1111.

> **🍎 macOS Users:**
> Because this app is not yet signed with an Apple Developer Certificate, you may see an error saying the app is **"damaged and can't be opened."** This is a standard macOS security message for unsigned apps.
>
> **To fix this:**
> 1. Move the app to your **Applications** folder.
> 2. Open **Terminal**.
> 3. Run the following command to clear the quarantine attribute:
>    ```bash
>    sudo xattr -cr "/Applications/Latent Tagger.app"
>    ```
> 4. You can now open the app normally.

---

### 🔄 State Persistence
Latent Tagger automatically remembers your selected model family and UI preferences via `localStorage`. Your prompt history is persisted to a local SQLite database. You can safely close the app and pick up right where you left off.

---

## 📜 License

Distributed under the **MIT License**. Free for personal use.

---

## 💖 Support the Project

If **Latent Tagger** has improved your Stable Diffusion workflow, consider supporting its ongoing development.

[![GitHub Sponsors](https://img.shields.io/badge/Sponsor-GitHub-ea4aaa?style=for-the-badge&logo=github-sponsors)](https://github.com/sponsors/erroralex)
[![Ko-fi](https://img.shields.io/badge/Ko--fi-F16061?style=for-the-badge&logo=ko-fi&logoColor=white)](https://ko-fi.com/error_alex)

---

<p align="center">
  <b>Developed by</b><br>
  <img src="frontend/src/assets/alx_logo_neon.png" width="120" alt="Alexander Nilsson Logo"><br>
  Copyright (c) 2026 Alexander Nilsson
</p>