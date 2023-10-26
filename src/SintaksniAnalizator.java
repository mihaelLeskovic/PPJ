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


    //Converting these into methods:
    //<program> ::= <lista_naredbi>
    //<lista_naredbi> ::= <naredba> <lista_naredbi>
    //<lista_naredbi> ::= $
    //<naredba> ::= <naredba_pridruzivanja>
    //<naredba> ::= <za_petlja>
    //<naredba_pridruzivanja> ::= IDN OP_PRIDRUZI <E>
    //<za_petlja> ::= KR_ZA IDN KR_OD <E> KR_DO <E> <lista_naredbi> KR_AZ
    //<E> ::= <T> <E_lista>
    //<E_lista> ::= OP_PLUS <E>
    //<E_lista> ::= OP_MINUS <E>
    //<E_lista> ::= $
    //<T> ::= <P> <T_lista>
    //<T_lista> ::= OP_PUTA <T>
    //<T_lista> ::= OP_DIJELI <T>
    //<T_lista> ::= $
    //<P> ::= OP_PLUS <P>
    //<P> ::= OP_MINUS <P>
    //<P> ::= L_ZAGRADA <E> D_ZAGRADA
    //<P> ::= IDN
    //<P> ::= BROJ

    static class TokenListConverter {
        TokenList tokenList;
        GenerativeTree generativeTree;
        TokenNode currTokenNode;
        TreeNode currTreeNode;

        void throwException() throws Exception {
            throw new Exception("err " + currTokenNode.toString());
        }

        void program() throws Exception{
            generativeTree = new GenerativeTree(new TreeNode("<program>", true));
            currTreeNode = generativeTree.getRoot();

            listaNaredbi();
        }

        void listaNaredbi() throws Exception{
            currTreeNode.children.add(new TreeNode("<lista_naredbi>", true));
            currTreeNode = currTreeNode.children.getLast();
            TreeNode localTreeNode = currTreeNode;

//            naredba();

            currTreeNode = localTreeNode;

            if(currTokenNode != null){
                listaNaredbi();
            } else {
                currTreeNode.children.add(new TreeNode("$", false));
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
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("src/example.txt")));
//        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        String line = reader.readLine();

        TokenList tokenList = new TokenList();

        while(line!=null){
            tokenList.add(parseStandardizedLine(line));
            line = reader.readLine();
        }


    }

}
