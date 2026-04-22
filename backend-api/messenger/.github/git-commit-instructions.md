# 🚀 Commit Message Rules (Messenger Project)

This project uses **Conventional Commits** with issue tracking.

---

## 📌 Format


<type>(#<issue>): <short description>


### Example:


feat(#123): add login API
fix(#45): validate email input
refactor(#78): optimize user service


---

## 📂 Branch Naming Convention

Branches must include issue ID:


feature/<issue>-<name>
fix/<issue>-<name>
refactor/<issue>-<name>


### Example:


feature/123-login-api
fix/45-auth-error


---

## 🏷 Commit Types

| Type      | Description |
|----------|------------|
| feat     | Add new feature |
| fix      | Fix bug |
| refactor | Improve code without changing behavior |
| docs     | Documentation changes |
| chore    | Minor changes (config, build, etc.) |
| style    | Formatting/UI changes (no logic impact) |
| perf     | Performance improvements |
| build    | Dependency or build system changes |
| test     | Add or update tests |

---

## ⚠️ Rules

- Must include **issue ID** from branch name
- Must follow format: `type(#issue): message`
- Message should be short and clear (max ~72 characters)
- Use English for commit messages

---

## ✅ Good Examples


feat(#123): add register API
fix(#45): handle null pointer in login
test(#67): add user service test


---

## ❌ Bad Examples


update code
fix bug
abc xyz


---

## 🤖 Copilot Instructions

When generating commit messages:

- Always follow: `<type>(#issue): message`
- Extract issue ID from branch name
- Use appropriate type (feat, fix, refactor, etc.)
- Keep message concise and meaningful

---

## 🔥 Notes

- Commits not following this format may be rejected by Git hooks
- This ensures clean history and easy issue tracking