package com.example.calculator;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    private static final String PREF_NAME = "calc_prefs";
    private static final String PREF_THEME = "night_mode";

    private CalculatorViewModel viewModel;
    private HistoryAdapter historyAdapter;

    private TextView tvDisplay;
    private TextView tvSecondary;

    // Tracks whether the last action was '=' so next digit starts fresh
    private boolean resultShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Restore theme preference before setContentView
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        int nightMode = prefs.getInt(PREF_THEME, AppCompatDelegate.MODE_NIGHT_YES);
        AppCompatDelegate.setDefaultNightMode(nightMode);

        setContentView(R.layout.activity_main);

        viewModel = new ViewModelProvider(this).get(CalculatorViewModel.class);

        tvDisplay = findViewById(R.id.tvDisplay);
        tvSecondary = findViewById(R.id.tvSecondary);

        // RecyclerView for history
        RecyclerView recyclerHistory = findViewById(R.id.recyclerHistory);
        historyAdapter = new HistoryAdapter(viewModel.history);
        recyclerHistory.setLayoutManager(new LinearLayoutManager(this));
        recyclerHistory.setAdapter(historyAdapter);

        // Restore display from ViewModel (survives rotation)
        tvDisplay.setText(viewModel.currentDisplay);
        updateSecondaryDisplay();

        // --- Digit buttons ---
        int[] digitButtonIds = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9
        };
        String[] digitValues = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};

        for (int i = 0; i < digitButtonIds.length; i++) {
            final String digit = digitValues[i];
            findViewById(digitButtonIds[i]).setOnClickListener(v -> onDigitPressed(digit));
        }

        // --- Decimal button ---
        findViewById(R.id.btnDecimal).setOnClickListener(v -> onDecimalPressed());

        // --- Operator buttons ---
        findViewById(R.id.btnAdd).setOnClickListener(v -> onOperatorPressed("+"));
        findViewById(R.id.btnSubtract).setOnClickListener(v -> onOperatorPressed("-"));
        findViewById(R.id.btnMultiply).setOnClickListener(v -> onOperatorPressed("×"));
        findViewById(R.id.btnDivide).setOnClickListener(v -> onOperatorPressed("÷"));

        // --- Equals button ---
        findViewById(R.id.btnEquals).setOnClickListener(v -> onEqualsPressed());

        // --- Clear button ---
        findViewById(R.id.btnClear).setOnClickListener(v -> onClearPressed());

        // --- Negate button ---
        findViewById(R.id.btnNegate).setOnClickListener(v -> onNegatePressed());

        // --- Percent button ---
        findViewById(R.id.btnPercent).setOnClickListener(v -> onPercentPressed());

        // --- Custom Operator button (× 3.31) ---
        findViewById(R.id.button).setOnClickListener(v -> onCustomOperatorPressed());

        // --- Toggle Theme button ---
        findViewById(R.id.btnToggleTheme).setOnClickListener(v -> onToggleTheme());
    }

    // -------------------------------------------------------------------------
    // Button handlers
    // -------------------------------------------------------------------------

    private void onDigitPressed(String digit) {
        // If we just showed a result, start a new number
        if (resultShown) {
            viewModel.currentDisplay = "0";
            resultShown = false;
        }

        String current = viewModel.currentDisplay;

        if (current.equals("0") && !digit.equals(".")) {
            viewModel.currentDisplay = digit;
        } else {
            // Limit display length
            if (current.length() < 15) {
                viewModel.currentDisplay = current + digit;
            }
        }
        tvDisplay.setText(viewModel.currentDisplay);
    }

    private void onDecimalPressed() {
        if (resultShown) {
            viewModel.currentDisplay = "0";
            resultShown = false;
        }
        if (!viewModel.currentDisplay.contains(".")) {
            viewModel.currentDisplay = viewModel.currentDisplay + ".";
            tvDisplay.setText(viewModel.currentDisplay);
        }
    }

    private void onOperatorPressed(String op) {
        resultShown = false;
        // Store operandA from current display
        viewModel.operandA = viewModel.currentDisplay;
        viewModel.operator = op;
        // Clear display for operandB entry
        viewModel.currentDisplay = "0";
        tvDisplay.setText(viewModel.currentDisplay);
        updateSecondaryDisplay();
    }

    private void onEqualsPressed() {
        if (viewModel.operator.isEmpty() || viewModel.operandA.isEmpty()) {
            return;
        }
        viewModel.operandB = viewModel.currentDisplay;

        String equation = viewModel.operandA + " " + viewModel.operator + " "
                + viewModel.operandB;

        String result = viewModel.calculate();
        viewModel.currentDisplay = result;
        tvDisplay.setText(result);

        // Add to history: "operandA op operandB = result"
        String historyEntry = equation + " = " + result;
        viewModel.addToHistory(historyEntry);
        historyAdapter.notifyDataSetChanged();

        // Reset operator state
        viewModel.operandA = result;
        viewModel.operandB = "";
        viewModel.operator = "";
        updateSecondaryDisplay();
        resultShown = true;
    }

    private void onClearPressed() {
        viewModel.operandA = "";
        viewModel.operandB = "";
        viewModel.operator = "";
        viewModel.currentDisplay = "0";
        tvDisplay.setText("0");
        updateSecondaryDisplay();
        resultShown = false;
    }

    private void onNegatePressed() {
        String current = viewModel.currentDisplay;
        if (current.equals("0") || current.equals("Cannot divide by zero")) {
            return;
        }
        if (current.startsWith("-")) {
            viewModel.currentDisplay = current.substring(1);
        } else {
            viewModel.currentDisplay = "-" + current;
        }
        tvDisplay.setText(viewModel.currentDisplay);
    }

    private void onPercentPressed() {
        String current = viewModel.currentDisplay;
        try {
            double value = Double.parseDouble(current);
            value = value / 100.0;
            viewModel.currentDisplay = formatResult(value);
            tvDisplay.setText(viewModel.currentDisplay);
        } catch (NumberFormatException e) {
            // ignore if display is in error state
        }
    }

    private void onCustomOperatorPressed() {
        String current = viewModel.currentDisplay;
        try {
            double value = Double.parseDouble(current);
            String result = viewModel.applyCustomOperator(value);
            viewModel.currentDisplay = result;
            tvDisplay.setText(result);

            String historyEntry = current + " × 3.31 = " + result;
            viewModel.addToHistory(historyEntry);
            historyAdapter.notifyDataSetChanged();

            viewModel.operandA = result;
            viewModel.operandB = "";
            viewModel.operator = "";
            updateSecondaryDisplay();
            resultShown = true;
        } catch (NumberFormatException e) {
            // ignore if display is in error state
        }
    }

    private void onToggleTheme() {
        int currentMode = AppCompatDelegate.getDefaultNightMode();
        int newMode;
        if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
            newMode = AppCompatDelegate.MODE_NIGHT_NO;
        } else {
            newMode = AppCompatDelegate.MODE_NIGHT_YES;
        }
        // Save preference
        getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                .edit()
                .putInt(PREF_THEME, newMode)
                .apply();
        AppCompatDelegate.setDefaultNightMode(newMode);
    }

    // -------------------------------------------------------------------------
    // State Preservation (Milestone 3)
    // -------------------------------------------------------------------------

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("currentDisplay", viewModel.currentDisplay);
        outState.putString("operandA", viewModel.operandA);
        outState.putString("operandB", viewModel.operandB);
        outState.putString("operator", viewModel.operator);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        viewModel.currentDisplay = savedInstanceState.getString("currentDisplay", "0");
        viewModel.operandA = savedInstanceState.getString("operandA", "");
        viewModel.operandB = savedInstanceState.getString("operandB", "");
        viewModel.operator = savedInstanceState.getString("operator", "");
        tvDisplay.setText(viewModel.currentDisplay);
        updateSecondaryDisplay();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void updateSecondaryDisplay() {
        if (!viewModel.operandA.isEmpty() && !viewModel.operator.isEmpty()) {
            tvSecondary.setText(viewModel.operandA + " " + viewModel.operator);
        } else {
            tvSecondary.setText("");
        }
    }

    private String formatResult(double result) {
        if (result == (long) result) {
            return String.valueOf((long) result);
        } else {
            return String.valueOf(result);
        }
    }
}
