import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class SyntaxHighlighterApp {
    // Token sınıfı
    static class Token {
        String type;
        String value;
        int start;
        int end;

        Token(String type, String value, int start, int end) {
            this.type = type;
            this.value = value;
            this.start = start;
            this.end = end;
        }
    }

    // Leksikal Analiz
    static class Lexer {
        private final Set<String> keywords = new HashSet<>(Arrays.asList(
                "if", "while", "else", "for", "return",
                "int", "float", "double", "boolean", "char", "byte", "short", "long",
                "void", "class", "public", "private", "protected", "static", "final"
        ));
        private final Set<String> operators = new HashSet<>(Arrays.asList("+", "-", "*", "/", "=", "=="));

        public List<Token> tokenize(String code) {
            List<Token> tokens = new ArrayList<>();
            int pos = 0;
            while (pos < code.length()) {
                char ch = code.charAt(pos);

                // Boşluk veya yeni satır
                if (Character.isWhitespace(ch)) {
                    pos++;
                    continue;
                }

                // Yorumlar
                if (ch == '/' && pos + 1 < code.length() && code.charAt(pos + 1) == '/') {
                    int start = pos;
                    StringBuilder comment = new StringBuilder();
                    while (pos < code.length() && code.charAt(pos) != '\n') {
                        comment.append(code.charAt(pos));
                        pos++;
                    }
                    tokens.add(new Token("COMMENT", comment.toString(), start, pos));
                    continue;
                }

                // Operatörler
                if (operators.contains(String.valueOf(ch))) {
                    tokens.add(new Token("OPERATOR", String.valueOf(ch), pos, pos + 1));
                    pos++;
                    continue;
                }

                // Sayılar
                if (Character.isDigit(ch)) {
                    int start = pos;
                    StringBuilder num = new StringBuilder();
                    while (pos < code.length() && (Character.isDigit(code.charAt(pos)) || code.charAt(pos) == '.')) {
                        num.append(code.charAt(pos));
                        pos++;
                    }
                    tokens.add(new Token("NUMBER", num.toString(), start, pos));
                    continue;
                }

                // Tanımlayıcılar ve anahtar kelimeler
                if (Character.isLetter(ch) || ch == '_') {
                    int start = pos;
                    StringBuilder ident = new StringBuilder();
                    while (pos < code.length() && (Character.isLetterOrDigit(code.charAt(pos)) || code.charAt(pos) == '_')) {
                        ident.append(code.charAt(pos));
                        pos++;
                    }
                    String type = keywords.contains(ident.toString()) ? "KEYWORD" : "IDENTIFIER";
                    tokens.add(new Token(type, ident.toString(), start, pos));
                    continue;
                }

                pos++;
            }
            return tokens;
        }
    }

    // Top-Down parsers
    static class Parser {
        private final List<Token> tokens;
        private int pos;

        Parser(List<Token> tokens) {
            this.tokens = tokens;
            this.pos = 0;
        }

        public boolean parse() {
            while (pos < tokens.size()) {
                Token token = tokens.get(pos);
                if (token.type.equals("KEYWORD") && token.value.equals("if")) {
                    pos++;
                    if (pos < tokens.size() && tokens.get(pos).type.equals("IDENTIFIER")) {
                        pos++;
                    }
                } else {
                    pos++;
                }
            }
            return true;
        }
    }

    // GUI ve Vurgulama
    static class SyntaxHighlighterGUI {
        private final Lexer lexer = new Lexer();
        private final JTextPane textPane;
        private final StyledDocument doc;
        private final JTextArea lineNumbers;

        SyntaxHighlighterGUI() {
            JFrame frame = new JFrame("Gerçek Zamanlı Sözdizimi Vurgulayıcı");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(600, 400);
            frame.setLayout(new BorderLayout());

            // Satır numaraları için JTextArea
            lineNumbers = new JTextArea("1");
            lineNumbers.setBackground(new Color(30, 30, 30));
            lineNumbers.setForeground(Color.LIGHT_GRAY);
            lineNumbers.setEditable(false);
            lineNumbers.setFont(new Font("Monospaced", Font.PLAIN, 12));
            lineNumbers.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

            // Metin alanı
            textPane = new JTextPane();
            textPane.setBackground(new Color(30, 30, 30)); // Siyah-yakın gri (#1E1E1E)
            textPane.setForeground(Color.WHITE); // Metin rengi beyaz
            textPane.setCaretColor(Color.WHITE); // İmleç rengi beyaz
            textPane.setFont(new Font("Monospaced", Font.PLAIN, 12));
            doc = textPane.getStyledDocument();

            // Satır numaralarını ve metin alanını senkronize etme
            textPane.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    highlight();
                    updateLineNumbers();
                }
            });

            // Scroll pane ile metin alanı ve satır numaralarını birleştirme
            JScrollPane scrollPane = new JScrollPane(textPane);
            scrollPane.setRowHeaderView(lineNumbers);
            frame.add(scrollPane, BorderLayout.CENTER);

            // Renk stilleri
            Style keywordStyle = textPane.addStyle("KEYWORD", null);
            StyleConstants.setForeground(keywordStyle, new Color(100, 149, 237)); // Açık mavi (#6495ED)

            Style operatorStyle = textPane.addStyle("OPERATOR", null);
            StyleConstants.setForeground(operatorStyle, new Color(255, 64, 64)); // Açık kırmızı (#FF4040)

            Style numberStyle = textPane.addStyle("NUMBER", null);
            StyleConstants.setForeground(numberStyle, new Color(140, 90, 140)); // Mor

            Style commentStyle = textPane.addStyle("COMMENT", null);
            StyleConstants.setForeground(commentStyle, Color.GREEN);

            Style identifierStyle = textPane.addStyle("IDENTIFIER", null);
            StyleConstants.setForeground(identifierStyle, Color.WHITE);

            // Renk göstergesi paneli
            JPanel legendPanel = new JPanel();
            legendPanel.setLayout(new GridLayout(5, 1));
            legendPanel.setBackground(new Color(30, 30, 30));
            legendPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            JLabel keywordLabel = new JLabel("Anahtar Kelimeler: ");
            keywordLabel.setForeground(new Color(100, 149, 237)); // Açık mavi
            JLabel operatorLabel = new JLabel("Operatörler: ");
            operatorLabel.setForeground(new Color(255, 64, 64)); // Açık kırmızı
            JLabel numberLabel = new JLabel("Sayılar: ");
            numberLabel.setForeground(new Color(140, 90, 140)); // Mor
            JLabel commentLabel = new JLabel("Yorumlar: ");
            commentLabel.setForeground(Color.GREEN);
            JLabel identifierLabel = new JLabel("Tanımlayıcılar: ");
            identifierLabel.setForeground(Color.WHITE);

            legendPanel.add(keywordLabel);
            legendPanel.add(operatorLabel);
            legendPanel.add(numberLabel);
            legendPanel.add(commentLabel);
            legendPanel.add(identifierLabel);

            JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            southPanel.setBackground(new Color(30, 30, 30));
            southPanel.add(legendPanel);
            frame.add(southPanel, BorderLayout.SOUTH);

            frame.setVisible(true);
        }

        private void highlight() {
            try {
                String code = doc.getText(0, doc.getLength());
                List<Token> tokens = lexer.tokenize(code);

                doc.setCharacterAttributes(0, code.length(), textPane.getStyle("IDENTIFIER"), true);

                // Token'ları vurgula
                for (Token token : tokens) {
                    doc.setCharacterAttributes(token.start, token.end - token.start, textPane.getStyle(token.type), false);
                }

                // Sözdizimi kontrolü
                Parser parser = new Parser(tokens);
                parser.parse();
            } catch (Exception e) {
            }
        }

        private void updateLineNumbers() {
            String text = textPane.getText();
            int lineCount = text.isEmpty() ? 1 : text.split("\n").length;
            StringBuilder numbers = new StringBuilder();
            for (int i = 1; i <= lineCount; i++) {
                numbers.append(i).append("\n");
            }
            lineNumbers.setText(numbers.toString());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SyntaxHighlighterGUI::new);
    }
}