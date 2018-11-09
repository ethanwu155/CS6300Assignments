package edu.gatech.seclass.encode;

import static org.junit.Assert.*;

import org.apache.commons.cli.AlreadySelectedException;
import org.apache.commons.cli.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MyMainTest {

/*
Place all  of your tests in this class, optionally using MainTest.java as an example.
*/

    private ByteArrayOutputStream outStream;
    private ByteArrayOutputStream errStream;
    private PrintStream outOrig;
    private PrintStream errOrig;
    private Charset charset = StandardCharsets.UTF_8;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        outStream = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outStream);
        errStream = new ByteArrayOutputStream();
        PrintStream err = new PrintStream(errStream);
        outOrig = System.out;
        errOrig = System.err;
        System.setOut(out);
        System.setErr(err);
    }

    @After
    public void tearDown() throws Exception {
        System.setOut(outOrig);
        System.setErr(errOrig);
    }

    /*
     *  TEST UTILITIES
     */

    // Create File Utility
    private File createTmpFile() throws Exception {
        File tmpfile = temporaryFolder.newFile();
        tmpfile.deleteOnExit();
        return tmpfile;
    }

    // Write File Utility
    private File createInputFile(String input) throws Exception {
        File file =  createTmpFile();

        OutputStreamWriter fileWriter =
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);

        fileWriter.write(input);

        fileWriter.close();
        return file;
    }


    //Read File Utility
    private String getFileContent(String filename) {
        String content = null;
        try {
            content = new String(Files.readAllBytes(Paths.get(filename)), charset);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    /*
     * TEST FILE CONTENT
     */
    private static final String FILE1 = "abcxyz";
    private static final String FILE2 = "Howdy Billy,\n" +
            "I am going to take cs6300 and cs6400 next semester.\n" +
            "Did you take cs 6300 last semester? I want to\n" +
            "take 2 courses so that I will graduate Asap!";
    private static final String FILE3 = "abc123";
    private static final String FILE4 = "";

    // test cases

    /*
     *   TEST CASES
     */

    // Purpose: -n Test Case Error for integer greater than 25
    // Frame #: 6
    @Test
    public void encodeTest1() throws Exception {
        File inputFile1 = createInputFile(FILE3);
        String args[] = {"-n", "26", inputFile1.getPath()};
        Main.main(args);
        assertEquals("Usage: encode [-n [int]] [-r int | -l int] [-c string] <filename>", errStream.toString().trim());
    }

    // Purpose: Testing for -c <string> -n<integer> operation
    // Frame #: 27
    @Test
    public void encodeTest2() throws Exception {
        File inputFile2 = createInputFile(FILE1);
        String args[] = {"-n", "1", "-c", "ab", inputFile2.getPath()};
        Main.main(args);
        String expected2 = "020304252601";
        String actual2 = getFileContent(inputFile2.getPath());
        assertEquals("The files differ!", expected2, actual2);
    }

    // Purpose: Testing for -l <integer>
    // Frame #: 38
    @Test
    public void encodeTest3() throws Exception {
        File inputFile3 = createInputFile(FILE1);
        String args[] = {"-l", "1", inputFile3.getPath()};
        Main.main(args);
        String expected3 = "bcxyza";
        String actual3 = getFileContent(inputFile3.getPath());
        assertEquals("The files differ!", expected3, actual3);
    }

    // Purpose: Testing for -c <string> special characters error
    // Frame #: 17
    @Test
    public void encodeTest4() throws Exception {
        File inputFil4 = createInputFile(FILE1);
        String args[] = {"-c", "?!", inputFil4.getPath()};
        Main.main(args);
        String actual = getFileContent(inputFil4.getPath());
        assertEquals("Usage: encode [-n [int]] [-r int | -l int] [-c string] <filename>", errStream.toString().trim());

    }

    // Purpose: Testing for -r <integer> no integer error (if -r || -l present no integer error)
    // Frame #: 8
    @Test
    public void encodeTest5() throws Exception {
        File inputFile5 = createInputFile(FILE1);
        String args[] = {"-r", inputFile5.getPath()};
        Main.main(args);
        assertEquals("Usage: encode [-n [int]] [-r int | -l int] [-c string] <filename>", errStream.toString().trim());

    }

    // Purpose: Testing for -r -l mutual exclusivity (cannot exist at the same time)
    // Frame #: 7
    @Test (expected = AlreadySelectedException.class)
    public void encodeTest6() throws Exception {
        File inputFile6 = createInputFile(FILE1);
        String args[] = {"-r", "5", "-l", "5", inputFile6.getPath()};
        Main.main(args);
        assertEquals("Usage: encode [-n [int]] [-r int | -l int] [-c string] <filename>", errStream.toString().trim());

    }

    // Purpose: Testing for -c <string> missing string input error
    // Frame #: 12
    @Test
    public void encodeTest7() throws Exception {
        File inputFile7 = createInputFile(FILE1);
        String args[] = {"-c", inputFile7.getPath()};
        Main.main(args);
        assertEquals("Usage: encode [-n [int]] [-r int | -l int] [-c string] <filename>", errStream.toString().trim());
    }

    // Purpose: Testing for -n without optional integer value (integer value set to 13)
    // Frame #: 48
    @Test
    public void encodeTest8() throws Exception {
        File inputFile8 = createInputFile(FILE2);
        String args[] = {"-n", "-r", "1", inputFile8.getPath()};
        Main.main(args);
        String expected8 = "!2102101712 1522252512,\n" +
                "22 1426 2002220120 0702 07142418 16066300 140117 16066400 01181107 0618261806071805.\n" +
                "172217 120208 07142418 1606 6300 25140607 0618261806071805? 22 10140107 0702\n" +
                "07142418 2 16020805061806 0602 07211407 22 10222525 2005141708140718 14061403";
        String actual8 = getFileContent(inputFile8.getPath());
        assertEquals("The files differ!", expected8, actual8);
    }

    // Purpose: Testing for unique case string containing Capital Letters (assume that method ignores captialization and does modification all the same)
    // Frame #: 20
    @Test
    public void encodeTest9() throws Exception {
        File inputFile9 = createInputFile(FILE2);
        String args[] = {"-c", "AeI", inputFile9.getPath()};
        Main.main(args);
        String expected9 = "Howdy BIlly,\n" +
                "i Am goIng to tAkE cs6300 And cs6400 nExt sEmEstEr.\n" +
                "DId you tAkE cs 6300 lAst sEmEstEr? i wAnt to\n" +
                "tAkE 2 coursEs so thAt i wIll grAduAtE asAp!\n";
        String actual9 = getFileContent(inputFile9.getPath());
        assertEquals("The files differ!", expected9, actual9);
    }

    // Purpose: Testing for -c <string> with string length max length 26 (assume that program works converting just matching characters)
    // Frame #: 15
    @Test
    public void encodeTest10() throws Exception {
        File inputFile10 = createInputFile(FILE3);
        String args[] = {"-c", "abcdefghijklmnopqrstuvwxyz", inputFile10.getPath()};
        Main.main(args);
        String expected10 = "ABC123\n";
        String actual10 = getFileContent(inputFile10.getPath());
        assertEquals("The files differ!", expected10, actual10);
    }

    // Purpose: Testing for all OPT tags utilized
    // Frame #: 23
    @Test
    public void encodeTest11() throws Exception {
        File inputFile11 = createInputFile(FILE1);
        String args[] = {"-n", "1", "-c", "ab", "-l", "2", inputFile11.getPath()};
        Main.main(args);
        String expected11 = "042526010203";
        String actual11 = getFileContent(inputFile11.getPath());
        assertEquals("The files differ!", expected11, actual11);
    }

    // Purpose: Testing all OPT tags utilized with no optional integer for -n
    // Frame #: 29
    @Test
    public void encodeTest12() throws Exception {
        File inputFile12 = createInputFile(FILE1);
        String args[] = {"-n", "-c", "a", "-l", "2", inputFile12.getPath()};
        Main.main(args);
        String expected12 = "161112131415";
        String actual12 = getFileContent(inputFile12.getPath());
        assertEquals("The files differ!", expected12, actual12);
    }

    // Purpose: Testing for valid integer input error for -r, -l operations (integer value set to 0)
    // Frame #: 9
    @Test
    public void encodeTest13() throws Exception {
        File inputFile13 = createInputFile(FILE2);
        String args[] = {"-r", "0", inputFile13.getPath()};
        Main.main(args);
        assertEquals("Usage: encode [-n [int]] [-r int | -l int] [-c string] <filename>", errStream.toString().trim());
    }

    // Purpose: Testing for edge case int = 0 for -n, ERROR
    // Frame #: 4
    @Test
    public void encodeTest14() throws Exception {
        File inputFile14 = createInputFile(FILE3);
        String args[] = {"-n", "-1", inputFile14.getPath()};
        Main.main(args);
        assertEquals("Usage: encode [-n [int]] [-r int | -l int] [-c string] <filename>", errStream.toString().trim());
    }

    // Purpose: Error case for File not corresponding to filename
    // Frame #: 21
    @Test (expected = NullPointerException.class)
    public void encodeTest15() throws ParseException {
        String args[] = new String[1];
        Main.main(args);
        assertEquals("File name not present", errStream.toString().trim());
    }

    // Purpose: Testing for No Words in File
    // Frame #: 2
    @Test
    public void encodeTest16() throws Exception {
        File inputFile16 = createInputFile(FILE4);
        String args[] = {"-n", inputFile16.getPath()};
        Main.main(args);
        String expected16 = "";
        String actual16 = getFileContent(inputFile16.getPath());
        assertEquals(expected16, actual16);
    }

    // Purpose: Testing for 0 integer input for -n
    // Frame #: 3
    @Test
    public void encodeTest17() throws Exception {
        File inputFile17 = createInputFile(FILE1);
        String args[] = {"-n", "0", inputFile17.getPath()};
        Main.main(args);
        assertEquals("Usage: encode [-n [int]] [-r int | -l int] [-c string] <filename>", errStream.toString().trim());
    }

    // Purpose: Testing for invalid integer input less than 0 -n
    // Frame #: 44
    @Test
    public void encodeTest18() throws Exception {
        File inputFile18 = createInputFile(FILE2);
        String args[] = {"-n", "10", "-l", "1", inputFile18.getPath()};
        Main.main(args);
        String expected18 = "25071409 1219222209,\n" +
                "19 1123 1725192417 0425 04112115 13036300 112414 13036400 24150804 0315231503041502.\n" +
                "141914 092505 04112115 1303 6300 22110304 0315231503041502? 19 07112404 0425\n" +
                "04112115 2 13250502031503 0325 04181104 19 07192222 1702111405110415 11031126!18";
        String actual18 = getFileContent(inputFile18.getPath());
        assertEquals("Files differ!", expected18, actual18);

    }

    // Purpose: Testing for invalid integer input less than 0 -r
    // Frame #: 10
    @Test
    public void encodeTest19() throws Exception {
        File inputFile19 = createInputFile(FILE1);
        String args[] = {"-r", "-5", inputFile19.getPath()};
        Main.main(args);
        assertEquals("Usage: encode [-n [int]] [-r int | -l int] [-c string] <filename>", errStream.toString().trim());
    }

    // Purpose: Testing for -c with string length of 1
    // Frame #: 14
    @Test
    public void encodeTest20() throws Exception {
        File inputFile20 = createInputFile(FILE1);
        String args[] = {"-c", "a", inputFile20.getPath()};
        Main.main(args);
        String expected20 = "Abcxyz\n";
        String actual20 = getFileContent(inputFile20.getPath());
        assertEquals("The files differ", expected20, actual20);
    }

    // Purpose: Testing for only spaces in string input for -c
    // Frame #: 16
    @Test
    public void encodeTest21() throws Exception {
        File inputFile21 = createInputFile(FILE1);
        String args[] = {"-c", "   ", inputFile21.getPath()};
        Main.main(args);
        assertEquals("Usage: encode [-n [int]] [-r int | -l int] [-c string] <filename>", errStream.toString().trim());
    }

    // Purpose: Testing for alphanumeric characters in string input for -c
    // Frame #: 18
    @Test
    public void encodeTest22() throws Exception {
        File inputFile22 = createInputFile(FILE1);
        String args[] = {"-c", "ab123cd", inputFile22.getPath()};
        Main.main(args);
        assertEquals("Usage: encode [-n [int]] [-r int | -l int] [-c string] <filename>", errStream.toString().trim());
    }

    // Purpose: Testing for duplicate characters in string input for -c
    // Frame #: 19
    @Test
    public void encodeTest23() throws Exception {
        File inputFile23 = createInputFile(FILE1);
        String args[] = {"-c", "aaaaaa", inputFile23.getPath()};
        Main.main(args);
        assertEquals("Usage: encode [-n [int]] [-r int | -l int] [-c string] <filename>", errStream.toString().trim());
    }

    // Purpose: One Word File, -n <int> >0, -l <int> >0
    // Frame #: 26
    @Test
    public void encodeTest24() throws Exception {
        File inputFile24 = createInputFile(FILE1);
        String args[] = {"-n", "1", "-l", "1", inputFile24.getPath()};
        Main.main(args);
        String expected24 = "030425260102";
        String actual24 = getFileContent(inputFile24.getPath());
        assertEquals("The files differ", expected24, actual24);
    }

    // Purpose: One Word File, -n <int> >0
    // Frame #: 28
    @Test
    public void encodeTest25() throws Exception {
        File inputFile25 = createInputFile(FILE1);
        String args[] = {"-n", "1", inputFile25.getPath()};
        Main.main(args);
        String expected25 = "020304252601";
        String actual25 = getFileContent(inputFile25.getPath());
        assertEquals("The files differ!", expected25, actual25);
    }

    // Purpose: One Word File, -n no optional integer, -r <int>
    // Frame #: 30
    @Test
    public void encodeTest26() throws Exception {
        File inputFile26 = createInputFile(FILE1);
        String args[] = {"-n", "-r", "1", inputFile26.getPath()};
        Main.main(args);
        String expected26 = "131415161112";
        String actual26 = getFileContent(inputFile26.getPath());
        assertEquals("The files differ!", expected26, actual26);
    }

    // Purpose: One Word File, all parameters, -n with no optional integer
    // Frame #: 31
    @Test
    public void encodeTest27() throws Exception {
        File inputFile27 = createInputFile(FILE1);
        String args[] = {"-n", "-r", "1", "-c", "abc", inputFile27.getPath()};
        Main.main(args);
        String expected27 = "131415161112";
        String actual27 = getFileContent(inputFile27.getPath());
        assertEquals("The files differ!", expected27, actual27);
    }

    // Purpose: One word file, -n with no integer, -1 <int>
    // Frame #: 32
    @Test
    public void encodeTest28() throws Exception {
        File inputFile28 = createInputFile(FILE1);
        String args[] = {"-n", "-l", "1", inputFile28.getPath()};
        Main.main(args);
        String expected28 = "151611121314";
        String actual28 = getFileContent(inputFile28.getPath());
        assertEquals("The files differ!", expected28, actual28);
    }

    // Purpose: One word file, -n with no integer, -c<string>
    // Frame #: 33
    @Test
    public void encodeTest29() throws Exception {
        File inputFile29 = createInputFile(FILE1);
        String args[] = {"-n", "-c", "abc", inputFile29.getPath()};
        Main.main(args);
        String expected29 = "141516111213";
        String actual29 = getFileContent(inputFile29.getPath());
        assertEquals("The files differ!", expected29, actual29);
    }

    // Purpose: One word with -c and -r paramters
    // Frame #: 35
    @Test
    public void encodeTest30() throws Exception {
        File inputFIle30 = createInputFile(FILE1);
        String args[] = {"-c", "ab", "-r", "1", inputFIle30.getPath()};
        Main.main(args);
        String expected30 = "zABcxy";
        String actual30 = getFileContent(inputFIle30.getPath());
        assertEquals("The files differ!", expected30, actual30);
    }
}
