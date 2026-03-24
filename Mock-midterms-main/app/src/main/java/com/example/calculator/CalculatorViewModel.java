package com.example.calculator;

import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

public class CalculatorViewModel extends ViewModel {

    public String operandA = "";
    public String operandB = "";
    public String operator = "";
    public String currentDisplay = "0";
    public ArrayList<String> history = new ArrayList<>();

    /**
     * Performs the arithmetic operation using the stored operands and operator.
     *
     * @return formatted result string, or an error message for division by zero.
     */
    public String calculate() {
        if (operandA.isEmpty() || operator.isEmpty() || operandB.isEmpty()) {
            return currentDisplay;
        }

        double a;
        double b;
        try {
            a = Double.parseDouble(operandA);
            b = Double.parseDouble(operandB);
        } catch (NumberFormatException e) {
            return "Error";
        }

        double result;
        switch (operator) {
            case "+":
                result = a + b;
                break;
            case "-":
                result = a - b;
                break;
            case "×":
                result = a * b;
                break;
            case "÷":
                if (b == 0) {
                    return "Cannot divide by zero";
                }
                result = a / b;
                break;
            default:
                return currentDisplay;
        }

        return formatResult(result);
    }

    /**
     * Applies the custom operator: multiplies the value by 3.31 (Student ID 331).
     *
     * @param value the current display value
     * @return formatted result string
     */
    public String applyCustomOperator(double value) {
        double result = value * 3.31;
        return formatResult(result);
    }

    /**
     * Appends an equation string to the history list.
     *
     * @param entry the equation string (e.g., "5 + 3 = 8")
     */
    public void addToHistory(String entry) {
        history.add(0, entry);
    }

    /**
     * Formats a double result: removes trailing ".0" for whole numbers.
     */
    private String formatResult(double result) {
        if (result == (long) result) {
            return String.valueOf((long) result);
        } else {
            return String.valueOf(result);
        }
    }
}
