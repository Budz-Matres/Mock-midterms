# Custom Calculator App
**CIS 2203N – Mobile Development | Mock Midterm**
**Student ID:** 22100331

## Milestone Commits
| Milestone | Commit Message |
|---|---|
| M1 – UI Grid | `feat: implement UI grid and styling` |
| M2 – Core Logic | `feat: implement core math logic and zero-division handling` |
| M3 – State & Custom Op | `feat: add state preservation and custom operator` |
| Bonus | `feat: add scrolling history log` |

## Implementation Choices

### Milestone 1 – UI & Layout
I chose `ConstraintLayout` as the root because it allows flat, non-nested view hierarchies which improves rendering performance. Inside it, I used `GridLayout` for the button pad because it natively supports row/column spanning (used for the `0` button spanning 2 columns) without needing nested `LinearLayouts`. The dark/light theme toggle is implemented by calling `AppCompatDelegate.setDefaultNightMode()` at runtime and storing the preference in `SharedPreferences` so it persists across launches.

### Milestone 2 – Core Logic
All logic is in Java inside `CalculatorViewModel`. I separated the logic from `MainActivity` following the **MVVM pattern** (as referenced in the Modern Android App Architecture docs) so that the ViewModel survives configuration changes like screen rotation. Division by zero is caught by a conditional check before performing the division — it returns the string `"Cannot divide by zero"` which is then displayed on the screen.

### Milestone 3 – State Preservation
I used `ViewModel` as the **primary** state store (it survives rotation automatically). I also implemented `onSaveInstanceState` / `onRestoreInstanceState` as a secondary fallback (e.g., for process death). This dual approach is the most robust pattern recommended in the Android documentation.

### Custom Operator
My Student ID ends in **331**, so my custom operator multiplies the current display value by **3.31**. The button is labeled `× 3.31 (ID)` and is placed below the main numpad.

### Bonus – History Log
I implemented a `RecyclerView` with a custom `HistoryAdapter` inside a `ScrollView`-constrained container. Every time `=` or the custom operator is pressed, the equation string is appended to the `ViewModel`'s history list and the adapter is notified to refresh.
