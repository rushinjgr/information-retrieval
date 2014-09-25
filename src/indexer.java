import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;

/**
 * CS 1655 -- ASSIGNMENT 1
 * INFORMATION RETRIEVAL
 * SUBMITTED 9/24/14
 * Created by Justin Rushin III
 */
public class indexer {
    public static void main(String[] args) {
        Index index = new Index();
        File folder = new File("./");
        File[] listOfFiles = folder.listFiles();
        ArrayList<File> targets = new ArrayList<File>();
        String temp;
        boolean found = false;
        for (File file : listOfFiles) {
            //checks to see if file exists
            if (file.isFile()) {
                temp = file.getName();
                //if we find a .counts file, add it
                if((temp.substring(temp.lastIndexOf("."),temp.length()).compareToIgnoreCase(".counts"))==0){
                    targets.add(file);
                    found = true;
                }
            }
        }
        //if no existing files were found output an error message and quit
        if(!found){
            System.out.println("ERROR: No count files found.");
            return;
        }
        //process through each counts file line by line
        for(File target : targets){
            try {
                BufferedReader br = new BufferedReader(new FileReader(target));
                String line;
                String name = target.getName();
                name = name.substring(0,name.lastIndexOf("."));
                while ((line = br.readLine()) != null) {
                    //the first part of the line is the term
                    line = line.substring(0,line.indexOf(","));
                    //we don't care about the count, just add the term and the file name to the index
                    index.termFileAdd(line,name);
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //out output: writing the index to the file
        index.writeToFile("inverted.index");
    }

    //stores a search term and strings of the filepath of files containing that string
    //linked list is used because addition is low cost and any time we retrieve we iterate through
    public static class Term{
        public String term;
        public LinkedList<String> files;
        public Term(String t){
            term = t;
            files = new LinkedList<String>();
        }
    }

    //larger data structure to contain terms
    public static class Index{
        public ArrayList<Term> termIndex;

        public Index(){
            termIndex = new ArrayList<Term>();
        }
        //input a search term and a file and either
        //1. create a new Term object and add the first file to it
        //2. find the existing Term object and add the file to it
        public boolean termFileAdd(String str,String fname){
            boolean contained = false;
            Iterator<Term> it = termIndex.iterator();
            Term temp;
            while(!contained && it.hasNext()){
                temp = it.next();
                if(temp.term.compareToIgnoreCase(str)==0){
                    temp.files.add(fname);
                    contained = true;
                }
            }
            if(!contained){
                temp = new Term(str);
                temp.files.add(fname);
                termIndex.add(temp);
                contained = true;
            }
            return contained;
        }

        //an output function to write the data structure to a file
        //takes intended filename as input
        public void writeToFile(String fname) {
            FileWriter fstream = null;
            try {
                fstream = new FileWriter(fname, false);
                BufferedWriter out = new BufferedWriter(fstream);
                for (Term term : termIndex) {
                    out.write(term.term);
                    for (String fil : term.files) {
                        out.write("," + fil);
                    }
                    out.write("\n");
                }
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
