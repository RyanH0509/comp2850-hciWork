# Pilot Protocol (Task 1) — Ktor + Pebble + HTMX Task Manager

## 1) Purpose
Evaluate usability + accessibility of a server-rendered task manager with progressive enhancement:
- JS-on: HTMX partial updates
- No-JS: full-page fallback routes (PRG redirects)

Primary measures: completion, time, errors, hints, and perceived clarity of feedback.

## 2) Participants & Conditions (n=4)
- P1: Mouse + JS-on (HTMX)
- P2: Keyboard-only + JS-on (HTMX)
- P3: Mouse + No-JS (Disable JS + hard refresh)
- P4: Keyboard-only + No-JS (Disable JS + hard refresh)

## 3) Materials & Setup
- Start page (full page): GET /tasks
- HTMX list fragment: GET /tasks/fragment
- Add: POST /tasks
- Edit (open): GET /tasks/{id}/edit
- Edit (submit): POST /tasks/{id}/edit
- Delete (HTMX): DELETE /tasks/{id}
- Delete (no-JS fallback): POST /tasks/{id}/delete
- Delete confirm page (no-JS): GET /tasks/{id}/delete/confirm

Browser/OS: [填写]
Prototype URL: http://localhost:8080

### Data reset (before each participant)
Reset dataset so every pilot starts from the same baseline:
- Ensure at least one task for: search/filter, edit, delete, and optionally enough items to paginate.
- Clear metrics file if you want a clean dataset for this pilot run.

## 4) Moderator script (before tasks)
1) Read consent-script.md; obtain **verbal consent**.
2) Say: “I’m testing the website, not you. Please tell me what you expect to happen.”
3) Confirm condition:
   - JS-on: normal browsing (HTMX active)
   - No-JS: disable JS in DevTools + hard refresh
4) Navigate to /tasks and begin.

## 5) Tasks (T1–T4) + success criteria + what to record
For each task:
- Start timer when participant begins acting
- Stop timer when success criteria met OR participant gives up
- Record: completion (Y/N), time_s, errors, hints_used, notable quotes

### T1 — Filter/Search tasks (task_code will log as T1_filter)
**Start**: /tasks
**Instruction**:
- “Use the search/filter box to find the task titled ‘[基线任务名A]’.”
- If results update dynamically (HTMX), continue; if no-JS, submit and wait full reload.
**Success**:
- The filtered list shows the target task (or “no results” is clearly communicated).
**What it tests**:
- GET /tasks?q=... and/or GET /tasks/fragment?q=...

### T2 — Add a task (task_code logs as T3_add)
**Start**: /tasks
**Instruction**:
- “Add a new task titled ‘Book dentist appointment’ (priority optional if available).”
**Success**:
- The new task appears in the list and feedback is perceivable.
**Important constraint**:
- Title cannot be blank and is limited in length (validation exists). :contentReference[oaicite:0]{index=0}

### T3 — Edit a task (task_code logs as T4_edit)
**Start**: /tasks
**Instruction**:
- “Edit the task ‘[基线任务名A]’ and change it to ‘[基线任务名A - 修改版]’. Save.”
**Success**:
- Updated title appears and persists after refresh.
**What it tests**:
- GET /tasks/{id}/edit (HTMX partial vs no-JS full page)
- POST /tasks/{id}/edit (handles validation_error vs success)

### T4 — Delete with safe confirmation in no-JS (task_code logs as T3_delete)
**Start**: /tasks
**Instruction**:
- “Delete the task ‘Book dentist appointment’.”
- If no-JS and you land on a confirmation page, confirm deletion.
**Success**:
- Task is removed from list; user understands what happened.
**What it tests**:
- JS-on: DELETE /tasks/{id}
- No-JS: GET /tasks/{id}/delete/confirm then POST /tasks/{id}/delete

## 6) Hinting rules (consistency)
- If stuck > 30 seconds: “What would you try next?”
- Then 1 neutral hint: “Look near the task row for Edit/Delete.”
- Max 2 hints per task; record hint count.

## 7) Post-test questions (2–3 min)
1) “What was the most confusing moment?”
2) “Was it clear when actions succeeded/failed?”
3) “In no-JS mode, did anything feel missing or harder?”

## 8) Data recording (what files will be produced)
- metrics.csv: exported from system logs (anonymised)
- pilot notes: one file per participant (P1–P4) with timestamps + quotes
