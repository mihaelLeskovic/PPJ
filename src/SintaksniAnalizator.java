import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;

public class SintaksniAnalizator {

    final static HashSet<String> keyWords = new HashSet<>(Arrays.asList(new String[]{"az", "za", "od", "do"}));
    final static HashSet<String> operators = new HashSet<>(Arrays.asList(new String[]{"=", "\\+", "-", "\\*", "/"}));
    final static HashSet<String> parentheses = new HashSet<>(Arrays.asList(new String[]{"\\(", "\\)"}));
    enum UniChars {
        IDN, BROJ,
        OP_PRIDRUZI, OP_PLUS, OP_MINUS, OP_PUTA, OP_DIJELI,
        L_ZAGRADA, D_ZAGRADA,
        KR_ZA, KR_OD, KR_DO, KR_AZ
    }

    static UniChars wordToUni(String word){
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

    static class GenerativeTree {
        TreeNode root;

        public TreeNode getRoot() {
            return root;
        }

        public GenerativeTree(TreeNode root) {
            this.root = root;
        }

        public void printTree(){
            recPrint(getRoot(), 0);
        }

        public void recPrint(TreeNode node, int spaces){
            for(int i=0; i<spaces; i++){
                System.out.print(" ");
            }
            System.out.println(node.content);
            for(TreeNode child : node.children){
                recPrint(child, spaces+1);
            }
        }
    }

    static class TreeNode {
        String content;
        boolean variable;
        LinkedList<TreeNode> children;

        public TreeNode(String content, boolean variable) {
            this.content = content;
            this.variable = variable;
            children = new LinkedList<>();
        }

        public void addChild(String cContent, boolean cVariable){
            children.add(new TreeNode(cContent, cVariable));
        }

        public void addChild(TreeNode cNode){
            children.add(cNode);
        }

        void print(int depth){
            for(int i=0; i<depth; i++){
                System.out.print(" ");
            }

            System.out.println(this.content);

            if(children.isEmpty()) return;

            for(TreeNode child : children){
                child.print(depth+1);
            }
        }
    }

    static class TokenListConverter {
        TokenList tokenList;
        GenerativeTree generativeTree;
        TokenNode currTokenNode;
        TreeNode currTreeNode;

        public GenerativeTree getGenerativeTree() {
            return generativeTree;
        }

        void run(TokenList tokenList) throws Exception{
            this.tokenList = tokenList;
            this.currTokenNode = tokenList.head;
            program();
        }

        void throwException() throws Exception {
            if(currTokenNode==null) throw new Exception("err kraj");
            throw new Exception("err " + currTokenNode.toString());
        }

        void checkAndAdd(UniChars uniChar) throws Exception{
            if(currTokenNode == null) throw new Exception("err kraj");
            if(currTokenNode.uniChar == uniChar){
                currTreeNode.addChild(currTokenNode.toString(), false);
                currTokenNode = currTokenNode.getNext();
            } else throwException();
        }

        void program() throws Exception{
            generativeTree = new GenerativeTree(new TreeNode("<program>", true));
            currTreeNode = generativeTree.getRoot();

            listaNaredbi();
        }

        void listaNaredbi() throws Exception{
            currTreeNode.addChild("<lista_naredbi>", true);
            currTreeNode = currTreeNode.children.getLast();
            TreeNode localTreeNode = currTreeNode;


            if(currTokenNode == null || currTokenNode.uniChar.equals(UniChars.KR_AZ)){
                currTreeNode.addChild("$", false);
            } else if(currTokenNode.getUniChar()==UniChars.KR_ZA
                        || currTokenNode.getUniChar()==UniChars.IDN){
                naredba();
                currTreeNode = localTreeNode;
                listaNaredbi();
            } else {
                throwException();
            }

        }

        void naredba() throws Exception{
            currTreeNode.addChild("<naredba>", true);
            currTreeNode = currTreeNode.children.getLast();

            if(currTokenNode.uniChar == UniChars.KR_ZA){
                za_petlja();
            } else if(currTokenNode.uniChar == UniChars.IDN){
                naredba_pridruzivanja();
            } else {
                throwException();
            }
        }

        void naredba_pridruzivanja() throws Exception{
            currTreeNode.addChild("<naredba_pridruzivanja>", true);
            currTreeNode = currTreeNode.children.getLast();

            checkAndAdd(UniChars.IDN);
            checkAndAdd(UniChars.OP_PRIDRUZI);
            e();
        }

        void za_petlja() throws Exception{
            currTreeNode.addChild("<za_petlja>", true);
            currTreeNode = currTreeNode.children.getLast();
            TreeNode localTreeNode = currTreeNode;

            checkAndAdd(UniChars.KR_ZA);
            checkAndAdd(UniChars.IDN);
            checkAndAdd(UniChars.KR_OD);

            e();

            currTreeNode = localTreeNode;

            checkAndAdd(UniChars.KR_DO);

            e();

            currTreeNode = localTreeNode;

            listaNaredbi();

            currTreeNode = localTreeNode;

            checkAndAdd(UniChars.KR_AZ);
        }

        void e() throws Exception{
            currTreeNode.addChild("<E>", true);
            currTreeNode = currTreeNode.children.getLast();
            TreeNode localTreeNode = currTreeNode;

            t();

            currTreeNode = localTreeNode;

            e_lista();
        }

        void e_lista() throws Exception {
            currTreeNode.addChild("<E_lista>", true);
            currTreeNode = currTreeNode.children.getLast();

            if(currTokenNode == null){
                currTreeNode.addChild("$", false);
                return;
            }

            switch(currTokenNode.uniChar) {
                case OP_PLUS:
                    checkAndAdd(UniChars.OP_PLUS);
                    e();
                    break;
                case OP_MINUS:
                    checkAndAdd(UniChars.OP_MINUS);
                    e();
                    break;
                default:
                    currTreeNode.addChild("$", false);
            }
        }

        void t() throws Exception{
            currTreeNode.addChild("<T>", true);
            currTreeNode = currTreeNode.children.getLast();
            TreeNode localTreeNode = currTreeNode;

            p();

            currTreeNode = localTreeNode;

            t_lista();
        }

        void t_lista() throws Exception{
            currTreeNode.addChild("<T_lista>", true);
            currTreeNode = currTreeNode.children.getLast();

            if(currTokenNode == null){
                currTreeNode.addChild("$", false);
                return;
            }

            switch(currTokenNode.uniChar){
                case OP_PUTA:
                    checkAndAdd(UniChars.OP_PUTA);
                    t();
                    break;
                case OP_DIJELI:
                    checkAndAdd(UniChars.OP_DIJELI);
                    t();
                    break;
                default:
                    currTreeNode.addChild("$", false);
            }
        }

        void p() throws Exception{
            currTreeNode.addChild("<P>", true);
            currTreeNode = currTreeNode.children.getLast();
            TreeNode localTreeNode = currTreeNode;

            if(currTokenNode == null){
                throwException();
            }

            switch(currTokenNode.uniChar){
                case IDN:
                    checkAndAdd(UniChars.IDN);
                    break;
                case BROJ:
                    checkAndAdd(UniChars.BROJ);
                    break;
                case OP_PLUS:
                    checkAndAdd(UniChars.OP_PLUS);
                    p();
                    break;
                case OP_MINUS:
                    checkAndAdd(UniChars.OP_MINUS);
                    p();
                    break;
                case L_ZAGRADA:
                    checkAndAdd(UniChars.L_ZAGRADA);
                    e();
                    currTreeNode = localTreeNode;
                    checkAndAdd(UniChars.D_ZAGRADA);
                    break;
                default:
                    throwException();
            }
        }


    }

    static class TokenList {
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

    static class TokenNode {
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

    static TokenNode parseStandardizedLine(String line){
        String[] split = line.split(" ");
        return new TokenNode(split[2], Integer.parseInt(split[1]));
    }

    public static void main(String[] args) throws Exception {
//        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("src/example.txt")));
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        String line = reader.readLine();

        TokenList tokenList = new TokenList();

        while(line!=null){
            tokenList.add(parseStandardizedLine(line));
            line = reader.readLine();
        }

        TokenListConverter tlc = new TokenListConverter();
        try {
            tlc.run(tokenList);
            tlc.getGenerativeTree().printTree();
        } catch (Exception e){
            System.out.println(e.getMessage());
        }

    }

}
