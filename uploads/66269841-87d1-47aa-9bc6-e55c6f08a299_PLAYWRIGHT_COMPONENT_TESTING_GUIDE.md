# Playwright Component Automation Testing Guide

## Overview

This guide describes how to add browser-based automated tests for individual components in the
CE Component Building Block library. Tests should exercise a component in isolation using
deterministic Storybook stories and Storybook `play` functions.

The repository's current browser test runner is **Vitest Browser Mode with the Playwright
provider**. It runs Storybook stories in headless Chromium. This is distinct from a standalone
`@playwright/test` suite: this repository does not currently define a `playwright.config.ts`,
an E2E test directory, or a `test:e2e` script. Do not use application routes, authentication,
or external QA test-case files as prerequisites for component tests.

**Scope:** One component at a time, covering its rendered states, user interactions, callbacks,
and accessibility-relevant behavior.  
**Story location:** Next to the component source, using a `*.stories.tsx` file.  
**Browser runner:** Vitest Browser Mode, Playwright provider, Chromium.  
**Storybook configuration:** `.storybook/main.ts` and `vite.config.mts`.  
**Test setup:** `.storybook/vitest.setup.ts`.

---

## How Testing Is Wired in This Repository

- `.storybook/main.ts` discovers stories under `src/**/*.stories.ts` and
  `src/**/*.stories.tsx`.
- `vite.config.mts` defines the Vitest `storybook` project, enables browser mode, and selects
  Playwright with Chromium.
- `.storybook/vitest.setup.ts` applies Storybook project and accessibility annotations.
- A story's `play` function is run as a browser interaction test by the Storybook Vitest addon.
- Standalone `*.test.tsx` files, such as `DataGrid.test.tsx`, use Vitest and Testing Library;
  they are useful component tests but are not the Storybook browser-story workflow described
  here.

Keep browser component automation in the component's Storybook file unless the project
configuration is intentionally changed. Do not add a second Playwright runner or duplicate
browser configuration for these tests.

---

## Component Story and Test Structure

Add or extend a story file beside the component:

```text
src/components/<category>/<ComponentName>/
├── ComponentName.tsx
├── ComponentName.stories.tsx
└── ...
```

Organize stories around meaningful component states, for example:

```text
ComponentName.stories.tsx
├── Default
├── Disabled
├── WithValidationError
├── WithRepresentativeData
└── Interaction stories with play functions
```

Stories should render the real component with explicit, stable props and local fixture data.
Use Storybook args for props and callback spies where behavior needs to be observed. Avoid
depending on network services, application state, user accounts, environment-specific data, or
the order in which other stories run.

---

## Component Test Coverage

Choose scenarios that match the component's public API and behavior. Not every component needs
every category; document any category that does not apply.

| Coverage area | What to verify |
| --- | --- |
| Initial render | Default content, labels, values, and expected controls render from the supplied props. |
| Prop variants | Important public options, such as disabled, read-only, size, variant, or configuration props, produce the documented behavior. |
| User interactions | Click, type, keyboard, selection, expansion, dismissal, and other supported interactions update the UI correctly. |
| Callback behavior | Callbacks fire for the intended action with the expected value or payload, and do not fire for unrelated actions. |
| Validation and boundaries | Invalid, empty, minimum/maximum, loading, or error states behave as specified by the component. |
| Accessibility and keyboard | Controls expose accessible roles/names, support expected keyboard behavior, and convey disabled or invalid state. |
| Regression scenarios | Previously fixed component-level bugs have a focused, repeatable assertion. |

Prefer a small number of focused scenarios with observable outcomes over tests that merely visit
every prop combination.

---

## Writing Story Interaction Tests

Use Storybook's test utilities from `storybook/test` in story `play` functions. Query within
`canvasElement` so the test is scoped to the story, and prefer accessible roles and names over
implementation-specific selectors.

```tsx
import { expect, fn, userEvent, within } from "storybook/test";

export const SelectsAnOption = {
  args: {
    onChange: fn(),
    // Supply the other required component props and deterministic options.
  },
  play: async ({ canvasElement, args }) => {
    const canvas = within(canvasElement);
    const user = userEvent.setup();

    await user.click(canvas.getByRole("button", { name: /choose option/i }));
    await user.click(await canvas.findByRole("option", { name: "Option A" }));

    await expect(args.onChange).toHaveBeenCalledWith("option-a");
    await expect(canvas.getByRole("button", { name: /option a/i })).toBeVisible();
  },
};
```

Adapt the accessible names, roles, expected callback arguments, and interactions to the
component under test. Use `findByRole` or an explicit `waitFor` when the expected result is
asynchronous. Avoid fixed sleeps.

### Selector preference

1. Accessible role and name, such as `getByRole("button", { name: "Save" })`.
2. Label or visible text, such as `getByLabelText("Email")`.
3. A stable `data-testid` only when the element has no suitable accessible query.
4. CSS selectors or positional selectors only as a last resort.

If a test cannot locate an important control accessibly, consider whether the component needs
an accessible name or semantic role rather than immediately adding a test-only selector.

### Reliable test behavior

- Assert observable behavior, not private state, generated class names, or internal component
  structure.
- Give every story its own deterministic args and fixture data; do not rely on another story.
- Use callback spies such as `fn()` to assert public callback behavior.
- Do not conditionally skip an assertion because an expected control or fixture is missing.
  Missing expected behavior should fail the test.
- Keep the story usable as both a component example and a test fixture.
- Use realistic, minimal fixture data and do not call external services.
- Cover empty or exceptional states explicitly when they are part of the component contract.

---

## Per-Component Test Documentation Template

Use this structure in a component's test documentation when a separate component-level record
is useful. Keep it focused on the component; no QA case references or external QA test files
are required.

```md
# [Component Name] - Browser Component Tests

## Overview

**Component:** `[ComponentName]`
**Source:** `src/components/<category>/<path>`
**Stories and tests:** `src/components/<category>/<path>/<ComponentName>.stories.tsx`
**Primary behavior under test:** [Brief description]

## Test Scenarios

| Scenario | Expected behavior | Story / interaction | Status |
| --- | --- | --- | --- |
| Default render | [Observable expected result] | `Default` | Planned |
| [Interaction or state] | [Observable expected result] | `[StoryName]` | Planned |

## Run

```bash
npx vitest run --project storybook src/components/<category>/<path>/<ComponentName>.stories.tsx
```
```

For a small component, the stories and their `play` tests may be sufficient documentation; do
not create a separate test-plan file solely to repeat the story names.

---

## Running the Browser Component Tests

Install the repository dependencies if they are not already installed:

```bash
npm install
```

Run the Storybook browser-test project:

```bash
npx vitest run --project storybook
```

Run tests for a single component story file:

```bash
npx vitest run --project storybook src/components/<category>/<path>/<ComponentName>.stories.tsx
```

Open Storybook to inspect the component and interact with its stories:

```bash
npm run storybook
```

If Playwright reports that the Chromium browser is not installed, install the browser used by
the configured provider:

```bash
npx playwright install chromium
```

There is currently no npm `test` or `test:e2e` script in `package.json`; use the Vitest
commands above unless project scripts are deliberately added.

---

## Completion Checklist

- [ ] The component has deterministic stories for the behavior being tested.
- [ ] Each intended interaction is asserted in a story `play` function.
- [ ] Assertions verify visible outcomes and relevant public callback payloads.
- [ ] Queries use accessible roles and names wherever possible.
- [ ] Tests do not require authentication, a route, remote data, or a separate QA case file.
- [ ] The component's story test passes with the `storybook` Vitest project.
