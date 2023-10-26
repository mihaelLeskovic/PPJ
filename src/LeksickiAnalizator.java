import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;

public class LeksickiAnalizator {

    private final static HashSet<String> keyWords = new HashSet<>(Arrays.asList(new String[]{"az", "za", "od", "do"}));
    private final static HashSet<String> operators = new HashSet<>(Arrays.asList(new String[]{"=", "\\+", "-", "\\*", "/"}));
    private final static HashSet<String> parentheses = new HashSet<>(Arrays.asList(new String[]{"\\(", "\\)"}));
    private enum UniChars {
        IDN, BROJ,
        OP_PRIDRUZI, OP_PLUS, OP_MINUS, OP_PUTA, OP_DIJELI,
        L_ZAGRADA, D_ZAGRADA,
        KR_ZA, KR_OD, KR_DO, KR_AZ
    }

    private static UniChars wordToUni(String word){
        switch(word){
            case "=":
                return UniChars.OP_PRIDRUZI;
            case "+":
                return UniChars.OP_PLUS;
            case "-":
                return UniChars.OP_MINUS;
            case "*":
                return UniChars.OP_PUTA;
            case "/":
                return UniChars.OP_DIJELI;
            case "(":
                return UniChars.L_ZAGRADA;
            case ")":
                return UniChars.D_ZAGRADA;
            case "az":
                return UniChars.KR_AZ;
            case "za":
                return UniChars.KR_ZA;
            case "od":
                return UniChars.KR_OD;
            case "do":
                return UniChars.KR_DO;
            default:
                if(word==null) return null;
                try {
                    int check = Integer.parseInt(word);
                    return UniChars.BROJ;
                } catch (NumberFormatException e){
                    return UniChars.IDN;
                }
        }
    }


    // TokenListManager omogucava pretvaranje linije koda u slijed tokena i dodavanje tog slijeda u neku token listu

    private static class TokenListManager {
        String line;
        int lineNum;
        TokenList tokenList;

        public void processNextLine(String nextLine, int lineNum){
            setLine(nextLine);
            setLineNum(lineNum);

            if(getLine().startsWith("//")) return;

            prepareLine();
            parseTokens();
        }

        public void parseTokens(){
            String[] words = Arrays.stream(line.split(" "))
                    .filter(word -> !word.isEmpty())
                    .toArray(String[]::new);
            for(String word : words){
                TokenNode curr = new TokenNode(word, getLineNum());
                getTokenList().add(curr);
            }
        }

        public void prepareLine(){
            removeComment();
            line.replaceAll("\t", " ");
            line.replaceAll("\n", " ");

            for(String word : operators){
                setLine(line.replaceAll(word, " " + word + " "));
            }
            for(String word : parentheses){
                setLine(line.replaceAll(word, " " + word + " "));
            }
        }

        public void removeComment(){
            if(!getLine().contains("//")) return;
            setLine(getLine().substring(0, getLine().indexOf("//")));
        }

        public TokenListManager(TokenList tokenList) {
            this.tokenList = tokenList;
        }

        public TokenListManager(String line, TokenList tokenList, int lineNum) {
            this.line = line;
            this.tokenList = tokenList;
            this.lineNum = lineNum;
        }

        public int getLineNum() {
            return lineNum;
        }

        public void setLineNum(int lineNum) {
            this.lineNum = lineNum;
        }

        public void setLine(String line) {
            this.line = line;
        }

        public String getLine() {
            return line;
        }

        public TokenList getTokenList() {
            return tokenList;
        }
    }

    private static class TokenList {
        TokenNode head;
        TokenNode tail;

        public void add(TokenNode node){
            if(head ==null){
                head = node;
                tail = node;
            } else {
                tail.setNext(node);
                node.setPrev(tail);
                tail = node;
            }
        }

        public void printAll(){
            recPrint(head);
        }

        public void recPrint(TokenNode curr){
            if(curr == null) return;
            System.out.println(curr.toString());
            recPrint(curr.getNext());
        }
    }

    private static class TokenNode {
        TokenNode prev;
        UniChars uniChar;
        String word;
        int line;
        TokenNode next;

        public String getWord() {
            return word;
        }

        public int getLine() {
            return line;
        }

        public TokenNode(String word, int line) {
            this.word = word;
            this.line = line;
            this.uniChar = wordToUni(word);
            this.prev = null;
            this.next = null;
        }

        public TokenNode getPrev() {
            return prev;
        }

        public void setPrev(TokenNode prev) {
            this.prev = prev;
        }

        public TokenNode getNext() {
            return next;
        }

        public void setNext(TokenNode next) {
            this.next = next;
        }

        public UniChars getUniChar() {
            return uniChar;
        }

        @Override
        public String toString() {
            return getUniChar() + " " + getLine() + " " + getWord();
        }
    }

    public static void main(String[] args) {
        try {
            System.out.println(System.getProperty("user.dir"));
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("src/example.txt")));
//            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            String line = reader.readLine();
            int lineCounter = 1;
            TokenList tokenList = new TokenList();
            TokenListManager tokenListManager = new TokenListManager(tokenList);

            while (line != null) {
                tokenListManager.processNextLine(line, lineCounter);
                line = reader.readLine();
                lineCounter++;
            }

            tokenList.printAll();
        } catch (Exception e){
            e.printStackTrace();
        }

    }
}
