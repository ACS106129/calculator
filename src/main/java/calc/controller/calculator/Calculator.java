package calc.controller.calculator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javafx.scene.control.Button;
import javafx.util.Pair;

public class Calculator {
    public static final String INIT_INPUT = "0";
    public static final String DIVIDED_BY_ZERO_ERROR = "無法除以零";
    public static final String UNDEFINED_ERROR = "未定義結果";
    public static final int MAX_EXPRESSION_SIZES = 2;
    public static final int MAX_INPUT_SIZE = 16;
    private final DecimalFormat commaFormat = new DecimalFormat("#,###.###############");
    private final DecimalFormat scientificFormat = new DecimalFormat("#.###############E0");
    private final List<Pair<String, Pair<BigDecimal, Operator>>> expressions = new ArrayList<>();
    private final List<Button> controlButtons = new ArrayList<>();
    private String inputString = INIT_INPUT;
    // if user input operator, the prompt will be turn on to listen input number
    private boolean isOperandPromptted = false;

    public Calculator() {
        commaFormat.setRoundingMode(RoundingMode.HALF_UP);
        commaFormat.setMaximumIntegerDigits(MAX_INPUT_SIZE - 1);
        scientificFormat.setRoundingMode(RoundingMode.HALF_UP);
        scientificFormat.setMaximumFractionDigits(MAX_INPUT_SIZE - 1);
        scientificFormat.setMaximumFractionDigits(MAX_INPUT_SIZE - 1);
    }

    public void addControlButtons(final List<Button> btns) {
        controlButtons.addAll(btns);
    }

    public void operate(final Operator op, final String ch) {
        if (inputString.equals(DIVIDED_BY_ZERO_ERROR) || inputString.equals(UNDEFINED_ERROR)) {
            clearAll();
            return;
        }
        try {
            final BigDecimal target = new BigDecimal(inputString);
            if (expressions.isEmpty())
                expressions.add(new Pair<>(ch, new Pair<>(target, op)));
            else {
                final Pair<BigDecimal, Operator> lastExpr = expressions.get(expressions.size() - 1).getValue();
                if (expressions.size() < MAX_EXPRESSION_SIZES
                        && (lastExpr.getValue() == Operator.Equal || (isOperandPromptted && op != Operator.Equal))) {
                    expressions.set(expressions.size() - 1, new Pair<>(ch, new Pair<>(target, op)));
                }
                // expression completed
                else if (lastExpr.getValue() == Operator.Equal && expressions.size() >= 2) {
                    // continue origin expression
                    if (op == Operator.Equal) {
                        final Pair<String, Pair<BigDecimal, Operator>> origin = expressions.get(expressions.size() - 2);
                        final String originCh = origin.getKey();
                        final Operator originOp = origin.getValue().getValue();
                        // change origin value by input
                        expressions.set(expressions.size() - 2, new Pair<>(originCh, new Pair<>(target, originOp)));
                        // compute result
                        inputString = compute(originOp, target, lastExpr.getKey()).orElseThrow().toString();
                        // make an new expression
                    } else {
                        expressions.clear();
                        expressions.add(new Pair<>(ch, new Pair<>(target, op)));
                    }
                } else if (lastExpr.getValue() != Operator.Equal && expressions.size() < MAX_EXPRESSION_SIZES) {
                    // make completed
                    if (op == Operator.Equal) {
                        final BigDecimal result = compute(lastExpr.getValue(), lastExpr.getKey(), target).orElseThrow();
                        expressions.add(new Pair<>(ch, new Pair<>(target, op)));
                        inputString = result.toString();
                    } else {
                        expressions.add(new Pair<>(ch, new Pair<>(target, op)));
                        final BigDecimal result = compute(lastExpr.getValue(), lastExpr.getKey(), target).orElseThrow();
                        expressions.set(expressions.size() - 1, new Pair<>(ch, new Pair<>(result, op)));
                        expressions.remove(expressions.size() - 2);
                        inputString = result.toString();
                    }
                }
            }
        } catch (final ArithmeticException e) {
            final int dividendIndex = op == Operator.Equal ? expressions.size() - 1 : expressions.size() - 2;
            inputString = expressions.get(dividendIndex).getValue().getKey().toString().equals("0") ? UNDEFINED_ERROR
                    : DIVIDED_BY_ZERO_ERROR;
            controlButtons.forEach(b -> b.setDisable(true));
        }
        isOperandPromptted = true;
    }

    public void clearAll() {
        isOperandPromptted = false;
        inputString = INIT_INPUT;
        expressions.clear();
        controlButtons.forEach(b -> b.setDisable(false));
    }

    public void clearEntry() {
        isOperandPromptted = false;
        if (inputString.equals(DIVIDED_BY_ZERO_ERROR) || inputString.equals(UNDEFINED_ERROR))
            clearAll();
        else
            inputString = INIT_INPUT;
    }

    public String getInputResult() {
        if (inputString.equals(DIVIDED_BY_ZERO_ERROR) || inputString.equals(UNDEFINED_ERROR))
            return inputString;
        return getOutput(new BigDecimal(inputString), true);
    }

    public String getExpressionResult() {
        if (!isOperandPromptted && expressions.size() >= MAX_EXPRESSION_SIZES)
            return "";
        final StringBuffer sb = new StringBuffer();
        expressions.forEach(ex -> {
            sb.append(getOutput(ex.getValue().getKey(), false));
            sb.append(' ');
            sb.append(ex.getKey());
            sb.append(' ');
        });
        return sb.toString();
    }

    public void inputNumber(final String number) {
        if (inputString.equals(DIVIDED_BY_ZERO_ERROR) || inputString.equals(UNDEFINED_ERROR))
            clearAll();
        if (inputString.length() < MAX_INPUT_SIZE)
            inputString = (isOperandPromptted || inputString.equals(INIT_INPUT)) ? number : inputString + number;
        else if (isOperandPromptted)
            inputString = number;
        isOperandPromptted = false;
    }

    private Optional<BigDecimal> compute(final Operator op, final BigDecimal a, final BigDecimal b) {
        switch (op) {
            case Add:
                return Optional.of(a.add(b));
            case Divide:
                return Optional.of(a.divide(b, 1024, RoundingMode.HALF_UP).stripTrailingZeros());
            case Multiply:
                return Optional.of(a.multiply(b));
            case Subtract:
                return Optional.of(a.subtract(b));
            default:
                return Optional.empty();
        }
    }

    private String getOutput(final BigDecimal n, final boolean isCommaRequired) {
        final String[] numLens = n.toPlainString().split("\\.");
        final int fractionLength = numLens.length == 2 ? numLens[1].length() : 0;
        final int intLength = numLens[0].length();
        final int totalLength = fractionLength + intLength;
        BigDecimal result = n;
        if (intLength <= MAX_INPUT_SIZE && intLength > 1 || intLength == 1 && fractionLength < MAX_INPUT_SIZE) {
            if (totalLength > MAX_INPUT_SIZE)
                commaFormat.setMaximumFractionDigits(MAX_INPUT_SIZE - intLength);
            return commaFormat.format(result);
        }
        return scientificFormat.format(result);
    }
}
