package governance.plugin.services;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by jayanga on 2/9/14.
 */
public class ServiceJavaFileParser {
    private static ASTParser parser = ASTParser.newParser(AST.JLS3);

    public static List<Object> parse(File file){
        List<Object> serviceInfoList = new LinkedList();

        String stringFile = readFileToString(file);

        parser.setSource(stringFile.toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);

        final CompilationUnit cu = (CompilationUnit) parser.createAST(null);

        ServiceAnnotationVisitor av = new ServiceAnnotationVisitor(serviceInfoList);

        cu.accept(av);

        return serviceInfoList;
    }

    private static String readFileToString(File file){
        FileInputStream fis = null;
        String s = "";
        try {
            fis = new FileInputStream(file);
            byte[] data = new byte[(int)file.length()];
            try {
                fis.read(data);
                fis.close();
                s = new String(data, "UTF-8");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return s;

        //
        /*StringBuilder fileData = new StringBuilder(1000);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));

            char[] buf = new char[10];
            int numRead = 0;
            try {
                while ((numRead = reader.read(buf)) != -1) {
                    String readData = String.valueOf(buf, 0, numRead);
                    fileData.append(readData);
                    buf = new char[1024];
                }

                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return  fileData.toString();
        */

    }
}
