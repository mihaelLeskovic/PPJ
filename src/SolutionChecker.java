import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.nio.file.Path;
import java.util.LinkedList;

public class SolutionChecker {

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

    static String findWorkFileName(String currDir, String suffix) throws Exception{
        String[] files = new File(currDir).list();
        for(String file : files){
            if(file.endsWith(suffix)) return file.replace(suffix, "");
        }
        throw new Exception("missing \"" + suffix + "\" file");
    }

    /*
    massRun
    ---------------
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

    public static void main(String[] args) {

        String solutionClassName = "LeksickiAnalizator";
        String currDir = System.getProperty("user.dir");

        String javaSourceDir = currDir + "\\src";
        String classDir = currDir + "\\test_cases\\compiled_class";
        String testCaseDir = currDir + "\\test_cases";

        String[] testDirs = new String[]{
                "C:\\Users\\mih\\Documents\\GitHub\\ppj\\test_cases\\MASNO-TESTIRANJE1\\2014-15\\1-l",
                "C:\\Users\\mih\\Documents\\GitHub\\ppj\\test_cases\\MASNO-TESTIRANJE1\\2014-15\\2-l",
                "C:\\Users\\mih\\Documents\\GitHub\\ppj\\test_cases\\MASNO-TESTIRANJE1\\2016-17\\1-t",
                "C:\\Users\\mih\\Documents\\GitHub\\ppj\\test_cases\\MASNO-TESTIRANJE1\\2016-17\\2-t"
        };
        String myOutputFile = "test.gen";

        try{
            javaFileToClass(solutionClassName, javaSourceDir, classDir);
        } catch (Exception e) {
            e.printStackTrace();
        }

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
    }
}
