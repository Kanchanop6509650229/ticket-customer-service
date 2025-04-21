# Git Hooks

This directory contains Git hooks to help prevent accidental commits of sensitive information.

## Installation

To install these hooks, run the following command from the repository root:

```bash
git config core.hooksPath .git-hooks
chmod +x .git-hooks/*
```

## Available Hooks

### pre-commit

This hook checks for:

1. Sensitive files that might contain secrets (like application properties files)
2. Common patterns that might indicate secrets in any staged files

If any potential issues are found, the commit will be blocked.

## Bypassing Hooks

In rare cases where you need to bypass these checks (e.g., when committing template files), you can use:

```bash
git commit --no-verify
```

**IMPORTANT**: Only use this when you're absolutely sure you're not committing sensitive information!
