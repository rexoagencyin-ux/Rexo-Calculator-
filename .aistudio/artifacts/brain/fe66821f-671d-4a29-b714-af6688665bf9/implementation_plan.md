# Rexo Calculator - Implementation Plan

A modern, offline-first Material 3 Android calculator engineered for everyday retail, tax, grocery, and discount math with instant reactive calculations and Google Play policy compliance.

### User Review & Critical Decisions

> [!IMPORTANT]
> The following architectural and UX choices have been confirmed based on your requirements and initial feedback:

- **Confirmed - Currency Selection**: Default currency is Indian Rupee (₹), with a top-bar currency switcher allowing seamless toggling between ₹ (INR), $ (USD), € (EUR), £ (GBP), and ¥ (JPY).
- **Confirmed - Navigation Style**: Bottom navigation bar featuring distinct icons and active pill indicators for switching effortlessly between the 5 calculator tools.
- **Confirmed - Calculation History Log**: A local calculation history log accessible via an app bar action sheet, with one-tap copy and clear actions.
- **Confirmed - Privacy & Data Policy**: Zero user login or authentication, zero telemetry/tracking, zero remote data collection. The app operates completely offline for calculations. Dedicated Privacy Policy and Terms & Conditions screen accessible from the top app bar.
- **Confirmed - AdMob Banner**: Safe-area anchored banner ad placed directly above the bottom navigation bar with test ad unit `ca-app-pub-3940256099942544/6300978111`.

---

### 1. Overview & Core Concept

- **What It Does**: Rexo Calculator is a retail, grocery, and financial utility app providing five dedicated calculation modules: Standard & Quick Percentage, Bill & GST/Tax, Weight/Price (Sugar/Vegetables), Volume/Liquid Price (Milk/Oil/Fuel), and Discount & Margin.
- **Target Audience / Persona**: Everyday shoppers, local shopkeepers, merchants, and budget-conscious consumers who need rapid price-per-quantity or tax calculations without mental arithmetic friction or internet dependency.
- **Key Value**: Delivers zero-latency, real-time recalculations on every keystroke with bidirectional conversions (Price by Weight vs. Weight by Budget; Price by Volume vs. Volume by Budget) and clear savings breakdowns.

---

### 2. User Experience & Visual Design

- **Key User Flows**:
  1. *Tab Navigation*: User selects one of the 5 tabs on the bottom navigation bar (Percentage, Tax/GST, Weight, Volume, Discount).
  2. *Interactive Calculation*: User inputs base values via clean numeric text fields with instant recalculation. Tapping pre-set chips (e.g., GST 5%, 12%, 18%, 28%) immediately updates the breakdown.
  3. *Unit / Mode Switching*: In Weight and Volume tabs, user switches modes via Segmented Buttons ("Find Price" vs. "Find Quantity by Budget") and toggles units (kg/g, L/ml).
  4. *Copy & History*: User taps the copy icon on any result card to copy the formatted answer to the clipboard. The calculation is logged in the History sheet.
  5. *Privacy & Terms*: User taps the Info icon in the Top App Bar to review the full Google Play-compliant offline privacy policy and terms.

- **Visual Identity & Theme**:
  - *Aesthetic Direction*: Clean, minimal, modern utility aesthetic with high contrast and structured card surfaces.
  - *Color Palette & Tokens*:
    - Background: Crisp off-white (`#F8F9FA` Light / `#121417` Dark)
    - Surface & Cards: Pure elevated white (`#FFFFFF` Light / `#1E2229` Dark)
    - Primary Brand Accent: Vivid Indigo Blue (`#3B82F6` / `#2563EB`)
    - Secondary Accent / Highlights: Emerald Green (`#10B981`) for savings and tax deductions
    - Text Primary: Deep charcoal slate (`#0F172A` Light / `#F1F5F9` Dark)
    - Text Muted: Neutral cool slate (`#64748B` Light / `#94A3B8` Dark)
  - *Typography & Hierarchy*: Bold, legible tabular figures for calculation outputs (`HeadlineMedium` and `TitleLarge`), medium-weight labels for unit inputs, and compact caption badges for percentage chips.
  - *Component Styling & Layout*:
    - Rounded surface cards (`16.dp` to `20.dp` corner radius) with subtle borders and M3 elevation.
    - Full edge-to-edge support with `enableEdgeToEdge()` respecting status bar and navigation bar insets.
    - Touch targets calibrated to a minimum of `48.dp x 48.dp`.

- **Interactive Feedback & Motion**:
  - Instant reactive state changes on numeric text entry.
  - Smooth animated transitions (`AnimatedContent` / crossfades) between navigation tabs.
  - Material ripple animations on all interactive chips, segmented buttons, and copy actions.

---

### 3. Key Product Decisions & Trade-Offs

- **Decision 1: Bidirectional Mode Switching in Weight & Volume Calculators**
  - *Chosen Approach*: Single screen per tool equipped with an M3 `SingleChoiceSegmentedButtonRow` to toggle between "By Quantity/Weight" and "By Budget/Price".
  - *Why*: Prevents screen clutter while allowing users buying groceries (e.g., "Give me ₹20 worth of sugar" vs. "I need 350 grams of sugar") to get the exact answer in a single tap.
  - *Alternatives Considered*: Separate sub-tabs (would crowd the navigation bar) or separate bottom sheets (adds unnecessary modal friction).

- **Decision 2: Local Persistence & Zero Authentication**
  - *Chosen Approach*: In-memory reactive StateFlow combined with local Room / SharedPreferences for currency preferences and calculation history, requiring no user accounts, no login, and zero network transmission.
  - *Why*: Adheres strictly to the Google Play zero-tracking policy requirement and guarantees offline reliability in areas with weak cellular reception (e.g., street markets, grocery basements).

- **Decision 3: Non-Intrusive Ad Placement**
  - *Chosen Approach*: Anchored AdMob banner widget positioned directly above the `NavigationBar`, contained inside the main `Scaffold` content area without obscuring action buttons or keyboard.
  - *Why*: Complies with Google AdMob and Google Play policies regarding non-interfering ad placements and accidental click prevention.

---

### 4. Technical Architecture & Data Strategy

```
┌──────────────────────────────────────────────────────────────────┐
│                          MainActivity                            │
│           (Edge-to-Edge, Theme Setup, Top/Bottom Bars)           │
└─────────────────────────────────┬────────────────────────────────┘
                                  │
         ┌────────────────────────┴────────────────────────┐
         ▼                                                 ▼
┌───────────────────────────────┐           ┌───────────────────────────────┐
│     TopAppBar & Actions       │           │     BottomNavigationBar       │
│  - Currency Switcher Dialog   │           │  - 5 Tab Destinations        │
│  - History Action Sheet       │           │  - Active Pill Indicators     │
│  - Privacy / Terms Sheet      │           │                               │
└───────────────────────────────┘           └───────────────────────────────┘
                                  │
                                  ▼
┌──────────────────────────────────────────────────────────────────┐
│                      Calculator Screen Host                      │
│                  (Animated Screen Transition)                    │
├─────────────────┬───────────────┬────────────────┬───────────────┤
│ 1. Percentage   │ 2. GST & Tax  │ 3. Weight/Price│ 4. Vol/Price  │
│    Calculator   │    Calculator │    Calculator  │    Calculator │
├─────────────────┴───────────────┴────────────────┴───────────────┤
│ 5. Discount & Margin Calculator                                  │
└─────────────────────────────────┬────────────────────────────────┘
                                  │
                                  ▼
┌──────────────────────────────────────────────────────────────────┐
│                      AdMob Banner Container                      │
│        (Safe Area Anchored, Test Unit ID, Graceful Fallback)     │
└─────────────────────────────────┬────────────────────────────────┘
                                  │
                                  ▼
┌──────────────────────────────────────────────────────────────────┐
│                      CalculatorViewModel                         │
│            (StateFlow, Pure Domain Math, History Log)            │
└──────────────────────────────────────────────────────────────────┘
```

- **Data Model & State**:
  - `CalculatorTab`: Enum representing the 5 calculators.
  - `Currency`: Enum/Data class supporting `₹`, `$`, `€`, `£`, `¥`.
  - `GstMode`: `ADD_TAX` vs. `REMOVE_TAX` with standard tax rates (5%, 12%, 18%, 28%, Custom).
  - `CalculationRecord`: Data class capturing timestamp, module name, input formula, and formatted result for the history log.
  - `CalculationState`: Unified state holding active inputs, calculated outputs, active currency, and history items.

- **Interactive Component & State Mapping**:
  - `CurrencySelector`: Updates active symbol and dynamically reformats output currency labels.
  - `ClearableNumberField`: Outlined text field with digit filter, prefix symbol, and trailing clear button.
  - `PresetChipRow`: Single-selection chips for rapid GST rate or discount percentage selection.
  - `ResultDisplayCard`: High-contrast card with primary total, detailed sub-totals, and a one-tap clipboard copy button.
  - `AdBannerView`: AndroidView hosting Google Mobile Ads `AdView` with test ad credentials.
