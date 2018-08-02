import java.io.*;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class Tester {

    private static final Class<?> testClass = TextInfo.class;
    private static final Class<?> solutionClass = TextInfoSol.class;

    //used when testing output
    private static final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private static final PrintStream origOutStream = System.out; //have to save the original System.out before it is overwritten

    private static final int numTests = 0; //set once all tests have been written
    private static int numTestsPassed = 0; //set automatically throughout testing


    /*
     * Redirects System.out to the ByteArrayOutputStream so we can collect output.
     */
    public static void redirectOutput(){
        System.setOut(new PrintStream(outputStream));
    }

    /*
     * Resets System.out back to its original state (to the PrintStream).
     */
    public static void restoreOutput(){
        System.setOut(origOutStream);
    }

    /*
     * Compares the printed output to the expected output (specifically for tests that have printed output).
     * Prints results of tests and updated the number of passed tests if test passes.
     * Prints stack trace is exception is thrown when invoking the method.
     *
     * Parameters:
     * 	Method m: Specified method to call tests on.
     * 	Method mSolution: Solution method that has correct output.
     * 	Object... params: Classes of parameters required for the method (ex: int.class, String.class).
     *
     * Returns:
     * 	void
     */
    private static boolean testOutput(Method m, Method mSolution, Object... params){
        try{
            //This involves a lot of redirecting/resetting output to both capture the output and then print to the screen.
            //It's a bit messy, I'd like a cleaner way of doing this.

            System.out.println("\nStudent output: ");
            redirectOutput();
            m.invoke(null, params);
            String studentOutput = outputStream.toString();
            restoreOutput();
            System.out.println(studentOutput);
            outputStream.reset();
            System.out.println("Expected output: ");
            redirectOutput();
            mSolution.invoke(null, params);
            String expectedOutput = outputStream.toString();
            restoreOutput();
            System.out.println(expectedOutput);
            outputStream.reset();
            boolean test = expectedOutput.equals(studentOutput);
            System.out.println("MATCHES EXPECTED RESULTS?: " + test + "\n\n");
            if (test){
                numTestsPassed++;
                return true;
            }
        }
        catch(Exception e){
            System.out.println("\nAN EXCEPTION WAS THROWN. VIEW THE STACK TRACE BELOW:");
            e.printStackTrace();
        }
        return false;
    }

    
    /*
     * Gets a specified method from a specified class. If there is no method, prints to the screen and returns null.
     *
     * Parameters:
     *  Class <?> c: Class that this method belongs to.
     *  String methodName: Name of the method to be obtained.
     *  Class<?>... paramClasses: Classes of parameters required for the method (ex: int.class, String.class).
     *
     * Returns:
     *  Method: if found
     *  Null: if not found
     */
    public static Method findMethod(Class<?> c, String methodName, Class<?>... paramClasses) {
        Method studentMethod = null;
        try {
            studentMethod = c.getMethod(methodName, paramClasses);
        } catch (NoSuchMethodException e) {
            System.out.println("No method with correct header called " + methodName + " exists.");
        }

        return studentMethod;
    }

    /*
     * Checks to see if the return type of the specified method matches the expected return type.
     *
     * Parameters:
     *  Method m: Specified method.
     *  Class <?> expectedReturnType: Class of the the expected return type.
     *
     * Returns:
     *  true: if it does match
     *  false: if it does not match
     */
    public static boolean checkReturnType(Method m, Class<?> expectedReturnType) {
        Class<?> returnType = m.getReturnType();
        if (!returnType.equals(expectedReturnType)) {
            System.out.println("\nSTUDENT DOES HAVE NOT CORRECT RETURN TYPE. "
                    + "Manually review code and deduct points for this issue as indicated in criteria.");
            System.out.println("\tRequired return type: " + expectedReturnType);
            System.out.println("\tGiven return type: " + returnType);
            return false;
        }
        return true;
    }


    /*
     * Compares given results and prints result of test. Updates the number of tests passed if appropriate.
     *
     * Parameters:
     *  Object studentRes: value returned by the student's method
     *  Object expectedRes: value returned by the solution method
     *
     * Returns:
     *  void
     */
    public static boolean testResults(Object studentRes, Object expectedRes, boolean isArray) {

        boolean test;
        if (isArray) {
            String studentArr = Arrays.toString((Object[])studentRes);
            String solutionArr = Arrays.toString((Object[]) expectedRes);
            System.out.println("\nStudent output: \n" + studentArr);
            System.out.println("Expected output: \n" + solutionArr);
            test = solutionArr.equals(studentArr);
        }
        else {
            System.out.println("\nStudent output: \n" + studentRes);
            System.out.println("Expected output: \n" + expectedRes);
            if(expectedRes != null){
                test = expectedRes.equals(studentRes);
            }
            else if (expectedRes == null && studentRes == null){
                test = true;
            }
            else{
                System.out.println("Expected result is null but student result is not.");
                test = false;
            }
        }

        System.out.println("\nMATCHES EXPECTED RESULTS?: " + test + "\n\n");
        if (test) {
            numTestsPassed++;
        }
        return test;
    }


    /*
     * Attempts to find a specified method with the correct header (name, return type, parameters).
     *
     * Parameters:
     *  String methodName: Name of specified method.
     *  Class<?> returnType: Class of the type returned by the specified method.
     *  String[] otherPossibleNames: Array containing possible strings the method may have been named if incorrectly named.
     *  Class<?>... paramClasses: Classes of all of the parameters of the specified method.
     *
     * Returns:
     *  Method: if desired Method is found
     *  null: if desired Method is not found
     */
    public static Method getMethodForTests(String methodName, Class<?> returnType, String[] otherPossibleNames, Class<?>... paramClasses) {
        System.out.println("\n--- Testing method " + methodName + " ---");

        Method studentMethod = findMethod(testClass, methodName, paramClasses);
        boolean incorrectName = false;

        if (studentMethod == null) {
            incorrectName = true;
            for (String name : otherPossibleNames) {
                if (studentMethod == null) {
                    studentMethod = findMethod(testClass, name, paramClasses);
                } else {
                    break;
                }
            }
        }

        //if never found desired method
        if (studentMethod == null) {
            System.out.println("Cannot find method. Check if student has an incorrect method header or put "
                    + "it in the incorrect class.");
            return null;
        }

        if (incorrectName) {
            System.out.println("Method has slightly incorrect name:");
            System.out.println("\tRequired name: " + methodName);
            System.out.println("\tGiven name: " + studentMethod.getName());
        }

        //if method returns nothing and we're just testing output to the print stream
        if (returnType == void.class) {
            checkReturnType(studentMethod, returnType); //we can still run tests even if return type is incorrect but they printed
        } else {
            if (!checkReturnType(studentMethod, returnType)) {
                System.out.println("Tests cannot be run on this method because of incorrect return type.");
                return null;
            }
        }
        return studentMethod;
    }



    private static void sampleTests(){
        String[] otherPossibleNames = {"sampleMethod", "samplemethod", "SampleMethod"};
        Method studentMethod = getMethodForTests("sampleMethod", String[].class, otherPossibleNames, int.class);

        Method solutionMethod = findMethod(solutionClass, "sampleMethod", int.class);

        if (studentMethod != null && solutionMethod != null) {
            // run tests

            try {
                //insert test 1
            } catch (Exception e) {
                System.out.println("AN EXCEPTION WAS THROWN. VIEW THE STACK TRACE BELOW:");
                e.printStackTrace();
            }
        }
    }




    /*
     * Runs all necessary tests. Prints number of tests passed.
     */
    private static void runTests() {
        System.out.println("******************** BEGIN TESTING ********************\n");

        sampleTests();

        System.out.println("-------------------------------");
        System.out.println("NUMBER OF TESTS PASSED: " + numTestsPassed + "/" + numTests);
        System.out.println("-------------------------------");
        System.out.println("\n\n******************** DONE TESTING ********************\n\n");

    }

    public static void main(String[] args) {
        runTests();
    }

}
