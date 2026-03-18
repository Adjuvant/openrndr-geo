---
name: git-collaboration
description: Universal standards for version control, branching, and team collaboration. git, collaboration, commits, branching, commit, branch, merge, pull-request
---

# Git & Collaboration - High-Density Standards

Universal standards for version control, branching, and team collaboration.

## **Priority: P0 (OPERATIONAL)**

Universal standards for effective version control, branching strategies, and team collaboration.

## 📝 Commit Messages (Conventional Commits)

- **Format**: `<type>(<scope>): <description>` (e.g., `feat(auth): add login validation`).
- **Types**: `feat` (new feature), `fix` (bug fix), `docs`, `style`, `refactor`, `perf`, `test`, `chore`.
- **Atomic Commits**: One commit = One logical change. Avoid "mega-commits".
- **Imperative Mood**: Use "add feature" instead of "added feature" or "adds feature".

## 🌿 Branching & History Management

- **Main Branch Protection**: Never push directly to `main` or `develop`. Use Pull Requests.
- **History Changes Are User Actions**: Pushing, merging, and rebasing are user-controlled actions and must not be performed or implied by the agent without explicit permission.

## 🤝 Pull Request (PR) Standards

- **Small PRs**: Limit to < 300 lines of code for effective review.
- **Commit Atomicness**: Each commit should represent a single, complete logical change.
- **Description**: State what changed, why, and how to test. Link issues (`Closes #123`).
- **Self-Review**: Review your own code for obvious errors/formatting before requesting peers.
- **CI/CD**: PRs must pass all automated checks (lint, test, build) before merging.
- **Merge Permission**: Merging is a user action and requires explicit user permission.
- **Rebase Permission**: Rebasing is a user action and requires explicit user permission.

## 🛡 Security & Metadata

- **No Secrets**: Never commit `.env`, keys, or certificates. Use `.gitignore` strictly.
- **Git Hooks**: Use tools like `husky` or `lefthook` to enforce standards locally.
- **Tags**: Use SemVer (`vX.Y.Z`) for releases. Update `CHANGELOG.md` accordingly.

## 📚 References

- [Clean Linear History & Rebase Examples](references/CLEAN_HISTORY.md)
