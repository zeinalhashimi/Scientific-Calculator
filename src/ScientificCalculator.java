import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ScientificCalculator extends JFrame {
    private final DisplayPanel displayPanel;
    private final Memory memory;
    private final HistoryPanel historyPanel;
    private final JButton equalsButton;

    public ScientificCalculator() {
        getContentPane().setBackground(Color.BLACK);
        setTitle("Scientific Calculator");
        setSize(800, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        displayPanel = new DisplayPanel();
        memory = new Memory();
        historyPanel = new HistoryPanel();
        equalsButton = new JButton("=");
        equalsButton.setEnabled(false);

        add(displayPanel, BorderLayout.NORTH);
        add(new ButtonPanel(this, displayPanel, memory, historyPanel, equalsButton), BorderLayout.CENTER);
        add(historyPanel, BorderLayout.EAST);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ScientificCalculator().setVisible(true));
    }
}

class DisplayPanel extends JPanel {
    private final JTextField display;

    public DisplayPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);
        display = new JTextField();
        display.setEditable(false);
        display.setFont(new Font("Calibri", Font.BOLD, 32));
        display.setPreferredSize(new Dimension(500, 100));
        display.setBackground(Color.BLACK);
        display.setForeground(Color.WHITE);
        display.setHorizontalAlignment(JTextField.RIGHT);

        add(display, BorderLayout.CENTER);
    }

    public String getText() {
        return display.getText();
    }

    public void setText(String text) {
        display.setText(text);
    }

    public void appendText(String text) {
        display.setText(display.getText() + text);
    }

    public void clear() {
        display.setText("");
    }
}

class ButtonPanel extends JPanel {
    public ButtonPanel(JFrame parent, DisplayPanel displayPanel, Memory memory, HistoryPanel historyPanel, JButton equalsButton) {
        setLayout(new GridLayout(9, 4, 10, 10));
        setBackground(Color.BLACK);

        String[] buttons = {
            "C", "±", "^", "/",
            "7", "8", "9", "*",
            "4", "5", "6", "-",
            "1", "2", "3", "+",
            "√", "0", ".", "=",
            "sin", "cos", "tan", "n!",
            "asin", "acos", "log", "ln",
            "M+", "M-", "MR", "MC",
            "MS", "e^x", "10^x", "Finish"
        };

        for (String text : buttons) {
            JButton button = text.equals("=") ? equalsButton : new JButton(text);
            button.setFont(new Font("Calibri", Font.BOLD, 18));
            button.setFocusPainted(false);
            button.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            button.setBackground(new Color(51, 51, 51));
            button.setForeground(Color.WHITE);

            switch (text) {
                case "C", "±", "^" -> button.setBackground(new Color(165, 165, 165));
                case "/",  "*", "-", "+", "=" -> button.setBackground(new Color(255, 159, 10));
                case "Finish" -> button.setBackground(new Color(255, 69, 58));
                default -> {}
            }

            button.addActionListener(new CalculatorAction(text, displayPanel, memory, historyPanel, parent, equalsButton));
            add(button);
        }
    }
}

class CalculatorAction implements ActionListener {
    private final String command;
    private final DisplayPanel displayPanel;
    private final Memory memory;
    private final HistoryPanel historyPanel;
    private final JFrame parent;
    private final JButton equalsButton;

    public CalculatorAction(String command, DisplayPanel displayPanel, Memory memory, HistoryPanel historyPanel, JFrame parent, JButton equalsButton) {
        this.command = command;
        this.displayPanel = displayPanel;
        this.memory = memory;
        this.historyPanel = historyPanel;
        this.parent = parent;
        this.equalsButton = equalsButton;
    }

    public void actionPerformed(ActionEvent e) {
        try {
            switch (command) {
                case "=":
                    String expression = displayPanel.getText();
                    double result = ExpressionEvaluator.evaluate(expression);
                    if (Double.isInfinite(result) || Double.isNaN(result)) {
                        throw new ArithmeticException("Division by Zero");
                    }
                    displayPanel.setText(Double.toString(result));
                    historyPanel.addEntry(expression + " = " + result);
                    break;
                case "C":
                    displayPanel.clear();
                    equalsButton.setEnabled(false);
                    break;
                case "±":
                    String current = displayPanel.getText();
                    if (current.startsWith("-")) displayPanel.setText(current.substring(1));
                    else displayPanel.setText("-" + current);
                    break;
                case "M+":
                    memory.add(Double.parseDouble(displayPanel.getText()));
                    break;
                case "M-":
                    memory.subtract(Double.parseDouble(displayPanel.getText()));
                    break;
                case "MR":
                    displayPanel.setText(Double.toString(memory.recall()));
                    break;
                case "MC":
                    memory.clear();
                    break;
                case "MS":
                    memory.store(Double.parseDouble(displayPanel.getText()));
                    break;
                case "Finish":
                    parent.dispose();
                    break;
                case "sin": case "cos": case "tan":
                case "asin": case "acos": case "√":
                case "log": case "ln": case "n!":
                case "e^x": case "10^x":
                    double input = Double.parseDouble(displayPanel.getText());
                    double output = Operations.applyFunction(command, input);
                    displayPanel.setText(Double.toString(output));
                    historyPanel.addEntry(command + "(" + input + ") = " + output);
                    break;
                default:
                    displayPanel.appendText(command);
                    equalsButton.setEnabled(true);
            }
        } catch (ArithmeticException ex) {
            displayPanel.setText("Division by Zero");
            equalsButton.setEnabled(false);
        } catch (NumberFormatException ex) {
            displayPanel.setText("Invalid Input");
            equalsButton.setEnabled(false);
        }
    }
}

class Memory {
    private double memory;

    public void add(double value) {
        memory += value;
    }

    public void subtract(double value) {
        memory -= value;
    }

    public void store(double value) {
        memory = value;
    }

    public double recall() {
        return memory;
    }

    public void clear() {
        memory = 0;
    }
}

class Operations {
    public static double applyFunction(String func, double x) {
        return switch (func) {
            case "sin" -> Math.sin(Math.toRadians(x));
            case "cos" -> Math.cos(Math.toRadians(x));
            case "tan" -> Math.tan(Math.toRadians(x));
            case "asin" -> Math.toDegrees(Math.asin(x));
            case "acos" -> Math.toDegrees(Math.acos(x));
            case "√" -> Math.sqrt(x);
            case "log" -> Math.log10(x);
            case "ln" -> Math.log(x);
            case "n!" -> factorial((int) x);
            case "e^x" -> Math.exp(x);
            case "10^x" -> Math.pow(10, x);
            default -> throw new IllegalArgumentException("Unknown function");
        };
    }

    private static long factorial(int n) {
        if (n < 0) throw new ArithmeticException("Negative factorial");
        long result = 1;
        for (int i = 2; i <= n; i++) result *= i;
        return result;
    }
}

class ExpressionEvaluator {
    public static double evaluate(String expression) {
        try {
            return new Object() {
                int pos = -1;
                int ch;

                void nextChar() {
                    ch = (++pos < expression.length()) ? expression.charAt(pos) : -1;
                }

                boolean eat(int charToEat) {
                    while (ch == ' ') nextChar();
                    if (ch == charToEat) {
                        nextChar();
                        return true;
                    }
                    return false;
                }

                double parse() {
                    nextChar();
                    double x = parseExpression();
                    if (pos < expression.length()) throw new RuntimeException("Unexpected: " + (char) ch);
                    return x;
                }

                double parseExpression() {
                    double x = parseTerm();
                    while (true) {
                        if      (eat('+')) x += parseTerm();
                        else if (eat('-')) x -= parseTerm();
                        else return x;
                    }
                }

                double parseTerm() {
                    double x = parseFactor();
                    while (true) {
                        if      (eat('*')) x *= parseFactor();
                        else if (eat('/')) x /= parseFactor();
                        else return x;
                    }
                }

                double parseFactor() {
                    if (eat('+')) return parseFactor();
                    if (eat('-')) return -parseFactor();

                    double x;
                    int startPos = this.pos;
                    if (eat('(')) {
                        x = parseExpression();
                        eat(')');
                    } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                        while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                        x = Double.parseDouble(expression.substring(startPos, this.pos));
                    } else {
                        throw new RuntimeException("Unexpected: " + (char) ch);
                    }

                    if (eat('^')) x = Math.pow(x, parseFactor());

                    return x;
                }
            }.parse();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid expression");
        }
    }
}

class HistoryPanel extends JPanel {
    private final JTextArea historyArea;

    public HistoryPanel() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(200, 0));
        setBackground(Color.BLACK);

        historyArea = new JTextArea();
        historyArea.setEditable(false);
        historyArea.setBackground(Color.BLACK);
        historyArea.setForeground(Color.LIGHT_GRAY);
        historyArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(historyArea);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void addEntry(String entry) {
        historyArea.append(entry + "\n");
    }
}
