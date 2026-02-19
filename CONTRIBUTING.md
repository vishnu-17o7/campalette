# Contributing to Campalette

Thank you for your interest in contributing to Campalette! This document provides guidelines and instructions for contributing.

## Code of Conduct

Be respectful, inclusive, and constructive in all interactions.

## How to Contribute

### Reporting Bugs

If you find a bug, please create an issue with:
- Clear bug description
- Steps to reproduce
- Expected vs actual behavior
- Screenshots if applicable
- Device/Android version details

### Suggesting Features

Feature requests are welcome! Please include:
- Clear feature description
- Use case and benefits
- Mockups or examples if applicable

### Pull Requests

1. **Fork the repository**
   ```bash
   git clone https://github.com/vishnu-17o7/campalette.git
   ```

2. **Create a feature branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

3. **Make your changes**
   - Follow the existing code style
   - Add tests if applicable
   - Update documentation

4. **Commit your changes**
   ```bash
   git commit -m "Add: your feature description"
   ```
   
   Use conventional commits:
   - `Add:` for new features
   - `Fix:` for bug fixes
   - `Update:` for updates to existing features
   - `Refactor:` for code refactoring
   - `Docs:` for documentation changes

5. **Push and create PR**
   ```bash
   git push origin feature/your-feature-name
   ```

## Development Setup

### Prerequisites
- Android Studio Hedgehog or later
- JDK 17+
- Android SDK 34
- Git

### Setup Steps
1. Clone the repository
2. Open in Android Studio
3. Let Gradle sync complete
4. Run on emulator or device

### Code Style

- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Add comments for complex logic
- Keep functions small and focused

### Compose Guidelines

- Use `@Composable` for UI components
- Prefer `remember` for state in composables
- Extract reusable composables
- Use Material 3 components

### Testing

- Write unit tests for business logic
- Add UI tests for user flows
- Ensure tests pass before submitting PR

## Areas for Contribution

### High Priority
- [ ] Save and share color palettes
- [ ] Gallery image picker
- [ ] Copy hex codes to clipboard
- [ ] Palette export as image

### Medium Priority
- [ ] Custom color naming
- [ ] Palette history
- [ ] Color harmony suggestions
- [ ] Accessibility improvements

### Low Priority
- [ ] Animations and transitions
- [ ] App widget
- [ ] Multiple camera support
- [ ] Color blindness modes

### Documentation
- [ ] Code documentation
- [ ] Tutorial videos
- [ ] Blog posts
- [ ] Translations

## Getting Help

- Create an issue for questions
- Check existing issues and discussions
- Review documentation files:
  - [README.md](README.md)
  - [ARCHITECTURE.md](ARCHITECTURE.md)
  - [BUILD.md](BUILD.md)

## Review Process

1. PRs will be reviewed for:
   - Code quality
   - Test coverage
   - Documentation
   - Adherence to guidelines

2. Feedback will be provided constructively

3. Once approved, PR will be merged

## License

By contributing, you agree that your contributions will be licensed under the MIT License.

## Recognition

Contributors will be recognized in:
- README.md contributors section
- Release notes for their contributions
- GitHub contributors page

Thank you for contributing to Campalette! 🎨
