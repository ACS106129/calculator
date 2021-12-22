package calc.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.ResourceBundle;

import calc.controller.calculator.Calculator;
import calc.controller.calculator.Operator;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.Light.Point;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class AppController implements Initializable {
    public static final String ATTACH_NAME = "App.fxml";
    private final Point dragStartPoint = new Point();
    private final ArrayList<Button> numButtons = new ArrayList<>();
    private final Calculator calc = new Calculator();
    private Stage primaryStage;
    @FXML
    private ImageView background;
    @FXML
    private Button btn0, btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9,
            btnAdd, btnC, btnCE, btnDiv, btnEq, btnMul, btnSub;
    @FXML
    private Pane contentPane;
    @FXML
    private TextField mainScreenField;
    @FXML
    private Label subScreenField;

    @Override
    public void initialize(final URL url, final ResourceBundle resourceBundle) {
        final Text text = new Text();
        final Font defaultFont = mainScreenField.getFont();
        mainScreenField.textProperty().addListener((ob, oldVal, newVal) -> {
            text.setFont(defaultFont);
            text.setText(newVal);
            final double maxWidth = mainScreenField.getBoundsInParent().getMaxX() - 30;
            final double width = text.getLayoutBounds().getWidth();
            final Font font = width <= maxWidth ? defaultFont
                    : Font.font(defaultFont.getFamily(), FontWeight.findByName(defaultFont.getStyle()),
                            defaultFont.getSize() * maxWidth / width);
            text.setFont(font);
            mainScreenField.setFont(font);
        });
        calc.addControlButtons(Arrays.asList(btnAdd, btnDiv, btnMul, btnSub));
        numButtons.addAll(Arrays.asList(btn0, btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9));
    }

    public void blurBackground() {
        background.setImage(copyBackground(primaryStage).orElseThrow());
    }

    private Optional<WritableImage> copyBackground(final Stage stage) {
        final int x = (int) stage.getX();
        final int y = (int) stage.getY();
        final int width = (int) stage.getWidth();
        final int height = (int) stage.getHeight();
        java.awt.image.BufferedImage image = null;
        try {
            image = new java.awt.Robot().createScreenCapture(new java.awt.Rectangle(x, y, width, height));
        } catch (final java.awt.AWTException e) {
            e.printStackTrace();
        }
        return Optional.ofNullable(SwingFXUtils.toFXImage(image, null));
    }

    private Optional<Button> getPressedButton(final String text, final KeyCode code, final boolean isShiftDown) {
        if (!isShiftDown) {
            if (text.matches("^[0-9]$"))
                return Optional.of(numButtons.get(Integer.parseInt(text)));
            else
                switch (code) {
                    case ADD:
                        return Optional.of(btnAdd);
                    case DIVIDE:
                    case SLASH:
                        return Optional.of(btnDiv);
                    case DELETE:
                        return Optional.of(btnCE);
                    case EQUALS:
                    case ENTER:
                        return Optional.of(btnEq);
                    case ESCAPE:
                        return Optional.of(btnC);
                    case MULTIPLY:
                        return Optional.of(btnMul);
                    case MINUS:
                    case SUBTRACT:
                        return Optional.of(btnSub);
                    default:
                        break;
                }
        }
        switch (code) {
            case DIGIT8:
                return Optional.of(btnMul);
            case EQUALS:
                return Optional.of(btnAdd);
            default:
                return Optional.empty();
        }
    }

    public void setPrimaryStage(final Stage s) {
        primaryStage = s;
    }

    @FXML
    private void close(final ActionEvent e) {
        System.exit(0);
    }

    @FXML
    private void inputNumber(final ActionEvent e) {
        calc.inputNumber(((Button) e.getSource()).getText());
        updateField();
    }

    @FXML
    private void clearAllResult(final ActionEvent e) {
        calc.clearAll();
        updateField();
    }

    @FXML
    private void clearMainResult(final ActionEvent e) {
        calc.clearEntry();
        updateField();
    }

    @FXML
    private void contentPaneOnKeyPressed(final KeyEvent e) {
        if (e.isAltDown() || e.isControlDown() || e.isMetaDown() || e.isShortcutDown())
            return;
        getPressedButton(e.getText(), e.getCode(), e.isShiftDown()).ifPresent(b -> b.fire());
    }

    @FXML
    private void divide(final ActionEvent e) {
        calc.operate(Operator.Divide, ((Button) e.getSource()).getText());
        updateField();
    }

    @FXML
    private void equal(final ActionEvent e) {
        calc.operate(Operator.Equal, ((Button) e.getSource()).getText());
        updateField();
    }

    @FXML
    private void add(final ActionEvent e) {
        calc.operate(Operator.Add, ((Button) e.getSource()).getText());
        updateField();
    }

    @FXML
    private void mainScreenOnMouseExited(final MouseEvent e) {
        contentPane.requestFocus();
    }

    @FXML
    private void multiply(final ActionEvent e) {
        calc.operate(Operator.Multiply, ((Button) e.getSource()).getText());
        updateField();
    }

    @FXML
    private void subtract(final ActionEvent e) {
        calc.operate(Operator.Subtract, ((Button) e.getSource()).getText());
        updateField();
    }

    @FXML
    private void minimize(final ActionEvent e) {
        primaryStage.setIconified(true);
    }

    @FXML
    private void windowOnMouseDragged(final MouseEvent e) {
        primaryStage.setX(e.getScreenX() + dragStartPoint.getX());
        primaryStage.setY(e.getScreenY() + dragStartPoint.getY());
    }

    @FXML
    private void windowOnMousePressed(final MouseEvent e) {
        dragStartPoint.setX(primaryStage.getX() - e.getScreenX());
        dragStartPoint.setY(primaryStage.getY() - e.getScreenY());
    }

    private void updateField() {
        mainScreenField.setText(calc.getInputResult());
        subScreenField.setText(calc.getExpressionResult());
    }
}
