package AlexThesis;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by Robert Maier on 06.07.2016.
 */
public class DataFile {

    private String file;
    private int currentLine;
    private boolean hasHeader;
    private ArrayList<String[]> data;
    private String separator;


    public DataFile(String fileURL, boolean hasHeader){
        this.hasHeader = hasHeader;
        this.currentLine = (this.hasHeader) ? 1 : 0;
        this.data = loadFile(fileURL);
        this.separator = ";";
    }

    private ArrayList<String[]> loadFile(String fileURL){
        BufferedReader br = null;
        String line = "";
        String csvSplitBy = ";";
        ArrayList<String[]> data = new ArrayList<String[]>();


        try {

            br = new BufferedReader(new FileReader(fileURL));
            int i = 0;
            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] temp = line.split(csvSplitBy);
                data.add(temp);
            }

        } catch (FileNotFoundException e) {
            System.out.println("File not found!");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("Done");
        return data;
    }

    public String[] getNextLine() {

        this.currentLine++;
        return data.get(currentLine-1);
    }

    public String[] getPreviousLine(){

        this.currentLine--;
        return data.get(currentLine+1);
    }

    public String[] getCurrentLine(){

         return data.get(currentLine);
    }

    private int countLines(String filename) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(filename));
        try {
            byte[] c = new byte[1024];
            int count = 0;
            int readChars = 0;
            boolean empty = true;
            while ((readChars = is.read(c)) != -1) {
                empty = false;
                for (int i = 0; i < readChars; ++i) {
                    if (c[i] == '\n') {
                        ++count;
                    }
                }
            }
            return (count == 0 && !empty) ? 1 : count;
        } finally {
            is.close();
        }
    }

    public int length(){
        return data.size();
    }

}
