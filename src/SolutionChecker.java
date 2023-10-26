import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class SolutionChecker {

    static void javaFileToClass(String solutionClassName, String sourceDir, String outDir) throws Exception{
        String sourceFile = solutionClassName + ".java";

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        if(compiler==null) throw new RuntimeException("Compiler not working properly!");

        File source = new File(sourceDir, sourceFile);

        if(compiler.run(null, null, null, source.getPath(), "-d", outDir) != 0)
            throw new RuntimeException("Compilation failed!");
    }

    static void runClass(String solutionClassName, String classDir, String inputFilePath, String outputFilePath) throws Exception{
        String classFilePath = classDir + "\\" + solutionClassName + ".class";
        // Create a ProcessBuilder to run the Java class
        ProcessBuilder processBuilder = new ProcessBuilder("java", "-cp", ".", classFilePath);
        processBuilder.directory(new File(classFilePath).getParentFile());

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
            System.out.println("Program executed successfully.");
        } else {
            System.err.println("Program execution failed with exit code " + exitCode);
        }
    }

    public static void main(String[] args) {

        String solutionClassName = "LeksickiAnalizator";
        String currDir = System.getProperty("user.dir");

        String javaSourceDir = currDir + "\\src";
        String classDir = currDir + "\\test_cases\\compiled_class";
        String testCaseDir = currDir + "\\test_cases";

        try{
            javaFileToClass(solutionClassName, javaSourceDir, classDir);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            runClass(solutionClassName, classDir, testCaseDir+"\\input.txt", testCaseDir+"\\myout.d");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
