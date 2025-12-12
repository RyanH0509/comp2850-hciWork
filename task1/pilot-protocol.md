# Pilot Protocol — Task 1 (Peer Pilots)

This protocol is designed to keep each pilot consistent while still feeling natural.

**Participants**: 3–5  
**Target time**: ~15 minutes per participant  
**Modes**:
- P1/P3/P5 → **JS-on (HTMX)**
- P2/P4 → **JS-off (No-JS)**

---

## 1) Before the first pilot (5 minutes)

- Start the server and open `/tasks`
- Confirm the four flows work: add, filter, edit, delete
- Confirm event logging is enabled (`data/metrics.csv` receives new rows)
- Prepare a notes file: `pilot-notes/Px_notes.md`
- Have a timer ready (metrics are the backup, not the only source of timing)

---

## 2) Per-participant flow (≈15–20 minutes)

### 2.1 Consent (2 min)
- Run `consent-script.md`
- Record: participant label (P#), mode (JS-on/JS-off), session code (sid)

### 2.2 Configure the session (1–2 min)
- Set sid cookie: `sid=P#_xxxx`
- If JS-off: disable JavaScript in the browser

### 2.3 Warm-up (1–2 min, not timed)
Prompt:
> “Please take a moment to look around the page. Let me know when you’re ready to start the first task.”

### 2.4 Run the tasks (timed)
For each task:
1) Read the scenario  
2) Start timing when they begin acting  
3) Observe silently (no coaching)  
4) Stop timing on success or when they give up  
5) Ask: “**From 1–5, how confident are you that you completed that correctly?**”  
6) Record any errors + notable quotes

**If they are stuck**: wait ~3 minutes, then ask what they’re trying to do. If still stuck, end the task and move on. Record it as a failure and note why.

### 2.5 Debrief (2–3 min)
Ask:
- “Which task felt hardest?”
- “Was anything unclear or unexpected?”
- “At any point, were you unsure whether something worked?”
- “Any accessibility issues (keyboard, focus, no-JS)?”

---

## 3) Task scenarios (read aloud)

> Suggested order: Add → Filter → Edit → Delete (then debrief)

### T1 — Add a task (≤60s)
“You need to remember to **Call supplier about delivery**. Add this as a new task.”

What to watch for:
- Do they notice the confirmation/status message?
- Do they accidentally submit an empty title?

### T2 — Filter tasks (≤120s)
“Filter the list to show tasks containing **report**, then tell me how many remain.”

What to watch for:
- Can they find the filter quickly?
- Do they rely on the UI’s count/status, or manually count?

### T3 — Edit a task (≤90s)
“Edit **Submit invoices** to **Submit invoices by Friday** and save.”

What to watch for:
- Do they find the edit control?
- If they trigger an error (blank title), is recovery obvious?

### T4 — Delete a task (≤45s)
“Delete the task named **Test entry**.”

What to watch for:
- Do they expect a confirmation step?
- After deletion, do they feel confident it worked?

---

## 4) Notes template (copy into each participant file)

```markdown
# Pilot Notes — P1

Mode: JS-on (HTMX) / JS-off (No-JS)  
Session code (sid): P1_7a9f  
Date/time: YYYY-MM-DD HH:MM  
Consent: yes

## T3 Add
Time: __s | Success: Y/N | Errors: __ | Confidence: __/5
Notes/quotes:

## T1 Filter
Time: __s | Success: Y/N | Errors: __ | Confidence: __/5
Notes/quotes:

## T2 Edit
Time: __s | Success: Y/N | Errors: __ | Confidence: __/5
Notes/quotes:

## T4 Delete
Time: __s | Success: Y/N | Errors: __ | Confidence: __/5
Notes/quotes:

## Debrief
Hardest task:
Unclear moments:
Accessibility notes:
Quotes:
```

---

## 5) Between pilots (3–5 minutes)

- Save the notes file
- Quick sanity check: `data/metrics.csv` has rows for that sid
- Clear cookies / generate a new sid for the next participant
- Swap roles (if working in a pair)
