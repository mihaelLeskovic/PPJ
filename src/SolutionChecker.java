import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SolutionChecker {

    public static void main(String[] args) {

        //OBAVEZNO: ovu klasu staviti u ISTI src folder gdje je i klasa koju testirate
        //testirano dobro radi na jdk17 i jdk21

        //ime klase koju se testira
        String solutionClassName = "SintaksniAnalizator";

        //folder u koji se compile-a .java file u .class
        //moze se ostaviti ovakav kakav je
        String classFolder = "\\test_cases\\compiled_class";


        //ime i sufiks file-a u koji kod generira svoj output
        String myOutputFile = "test.gen";

        //ime i sufiks file-a u koji kod generira svoj output
        String inputFile = "test.in";

        //direktorij koji sadrzi sve direktorije s test case-vima
        String masterDirectory = "C:\\Users\\example\\Documents\\GitHub\\ppj\\test_cases\\ULTIMATIVNO-TESTIRANJE2";

        //-----------------------------------------------------
        //DALJE NE MIJENJATI
        //-----------------------------------------------------

        String[] testDirs = directoryCrawler(masterDirectory, inputFile);

        long startTime = System.currentTimeMillis();

        String currDir = System.getProperty("user.dir");
        String javaSourceDir = currDir + "\\src";
        String classDir = currDir + classFolder;


        //compilation
        try{
            javaFileToClass(solutionClassName, javaSourceDir, classDir);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //---------------------------

        //running all the code on all the input files

        LinkedList<String> runErrors = new LinkedList<>();
        for(String testDir : testDirs){
            runErrors.addAll(massRun(solutionClassName, classDir, testDir, myOutputFile));
        }

        System.out.println("RUN ERRORS: ");
        if(runErrors.isEmpty()) System.out.println("no errors :)");
        for(String err : runErrors){
            System.out.println(err);
        }
        System.out.println("--------------------------");

        //---------------------------

        //comparing all the code-generated files and all the pre-generated files

        LinkedList<String> compareErrors = new LinkedList<>();
        for(String testDir : testDirs){
            compareErrors.addAll(massCompare(testDir, myOutputFile));
        }

        System.out.println("COMPARE ERRORS: ");
        if(compareErrors.isEmpty()) System.out.println("no errors :)");
        for(String err : compareErrors){
            System.out.println(err);
        }
        System.out.println("--------------------------");

        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;

        System.out.println("Time taken: " + elapsedTime + "milliseconds");
    }

    /*
    javaFileToClass
    ----------------
    solutionClassName - String of the name of the compiled file
    sourceDir - where the .java file we want to compile is
    outDir - where the compiled .class file will end up
     */
    static void javaFileToClass(String solutionClassName, String sourceDir, String outDir) throws Exception{
        String sourceFile = solutionClassName + ".java";

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        if(compiler==null) throw new RuntimeException("Compiler not working properly!");

        File source = new File(sourceDir, sourceFile);

        if(compiler.run(null, null, null, source.getPath(), "-d", outDir) != 0)
            throw new RuntimeException("Compilation failed!");
    }

    /*
    runClass method
    ----------------
    solutionClassName - String of the name of the tested file
    classDir - String of the directory of the compiled class of the tested file
    inputFilePath - String of full path ending WITH a filename of the input file
    outputFilePath - String of full path ending WITH a filename of the output file (myOutput which will later be compared to the pre-generated output)
     */
    static void runClass(String solutionClassName, String classDir, String inputFilePath, String outputFilePath) throws Exception{

        ProcessBuilder processBuilder = new ProcessBuilder("java", "-cp", classDir, solutionClassName);
        processBuilder.directory(new File(classDir).getParentFile());

        processBuilder.inheritIO();

        // Redirect input from the input file
        File inputFile = new File(inputFilePath);
        processBuilder.redirectInput(inputFile);

        // Redirect output to the output file
        File outputFile = new File(outputFilePath);
        processBuilder.redirectOutput(outputFile);

        // Start the process
        Process process = processBuilder.start();

        // Wait for the process to complete
        int exitCode = process.waitFor();

        if (exitCode == 0) {
//            System.out.println("Program executed successfully.");
        } else {
            System.err.println("Program execution failed with exit code " + exitCode);
        }
    }

    /*
    findWorkFileName
    ----------------
    returns the name of the file with the wanted suffix in currDir
     */
    static String findWorkFileName(String currDir, String suffix) throws Exception{
        String[] files = new File(currDir).list();
        for(String file : files){
            if(file.endsWith(suffix)) return file.replace(suffix, "");
        }
        throw new Exception("missing \"" + suffix + "\" file");
    }

    public static String[] directoryCrawler(String origin, String inputName) {
        List<String> result = new ArrayList<>();
        crawlDirectory(new File(origin), result, inputName);
        return result.toArray(new String[0]);
    }
    private static void crawlDirectory(File directory, List<String> result, String inputName) {
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    // Recursively crawl subdirectories
                    crawlDirectory(file, result, inputName);
                } else if (file.getName().equals(inputName)) {
                    // Found a file named "test.in", add its parent directory to the result
                    String parentDir = file.getParent();
                    String parentParentDir = new File(parentDir).getParent();
                    if (!result.contains(parentParentDir)) {
                        result.add(parentParentDir);
                    }
                }
            }
        }
    }

    /*
    massRun
    ----------------
    solutionClassName - string of the name of the tested file
    classDir - string location of compiled classes
    testDir - string location of where the tests are
    outputFileName - full file (including the suffix) where the output will be
    returns linked list of errors caught
     */
    static LinkedList<String> massRun(String solutionClassName, String classDir, String testDir, String outputFileName){
        LinkedList<String> errorList = new LinkedList<>();
        String[] files = new File(testDir).list();

        for(String file : files){
            String currDir = testDir + "\\" + file;
            if(new File(testDir).isDirectory()){
                try {
                    String workFileName = findWorkFileName(currDir, ".in");

//                    VERY COOL TO WATCH HOMIE
//                    System.out.println(currDir + "\\" + workFileName);

                    runClass(solutionClassName,
                            classDir,
                            currDir + "\\" + workFileName + ".in",
                            currDir + "\\" + outputFileName
                    );
                } catch (Exception e){
                    errorList.add(e.getMessage() + "||" + currDir);
                }
            }
        }

        return errorList;
    }

    /*
    compareFile
    ----------------
    compares file1 and file2 line by line and throws exception with message telling the first line with a mismatch
     */
    static void compareFile(String file1, String file2) throws Exception {
        BufferedReader br1 = new BufferedReader(new InputStreamReader(new FileInputStream(file1)));
        BufferedReader br2 = new BufferedReader(new InputStreamReader(new FileInputStream(file2)));

        int count = 1;
        String line1 = br1.readLine();
        String line2 = br2.readLine();

        while(line1!=null || line2!=null){
            if(!line1.equals(line2)) {
                String testDir = new File(file1).getParentFile().toString();
                throw new Exception("mismatch in line " + count + " || " + testDir);
            }
            line1 = br1.readLine();
            line2 = br2.readLine();
        }

        br1.close();
        br2.close();
    }

    /*
    massCompare
    ----------------
    in testDir, opens folder by folder, finding the .out file and comparing it to the set outputFileName file we generated
     */
    static LinkedList<String> massCompare(String testDir, String outputFileName){
        LinkedList<String> errors = new LinkedList<>();
        String[] files = new File(testDir).list();

        for(String file : files){
            String currDir = testDir + "\\" + file;
            if(new File(testDir).isDirectory()){
                try {
                    String workFileName = findWorkFileName(currDir, ".out");

//                    System.out.println(currDir + "\\" + workFileName);

                    compareFile(currDir+"\\"+workFileName+".out", currDir+"\\"+outputFileName);
                } catch (Exception e){
                    errors.add(e.getMessage() + "||" + currDir);
                }
            }
        }

        return errors;
    }
}
