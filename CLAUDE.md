# Project
Android app for unit economics of a handmade jewelry store.

# Current stack
- Kotlin
- Jetpack Compose
- Room
- ViewModel
- Repository
- Material 3

# Current architecture
- UI in Compose
- ViewModel for state and actions
- Repository for data access
- Room for local persistence

# Product goal
The app should allow the user to:
1. Add materials
2. Add sales
3. Select materials used in each sale
4. Calculate material cost automatically
5. Calculate profit automatically
6. Store data locally and keep it after restart

# Rules for Claude
- Make changes in small safe steps
- Do not rewrite unrelated code
- Prefer simple MVP solutions
- Keep code production-like but not overengineered
- Before large refactors, propose a short plan first
- After each change, list changed files
- Use Room + ViewModel + Repository
- Use Jetpack Compose only for UI
- Do not add server, Firebase, or auth
- Preserve Russian UI labels unless asked otherwise

# Coding preferences
- Keep names clear and explicit
- Avoid unnecessary abstractions
- Prefer readable code over clever code
- Do not add new libraries unless required