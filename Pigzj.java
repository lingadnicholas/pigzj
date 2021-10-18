import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap; 
import java.util.zip.Deflater;

class Pigzj { 

    
    protected static int readSize = 131072; // block size 
    protected static int kib = 32768;    // Dictionary size 
    //protected static int readSize = 2; // block size 
    //protected static int kib = 1;    // Dictionary size 
    protected static byte[] data; // holds stdin 
    protected static int size; // size of data array 
    protected static PigzjOutputStream pigzjOutputStream; 
    protected static int nThreads;
    protected static int numBlocks; 
    private static Thread[] threads; // holds threads
    protected static int lastWrite; 
    protected static int lastCur; 
    protected static int lastCount;
    /*HashMap: 
    /* Index = block index that will use this prev 
    /* Value = points to last portion of block */
    protected static HashMap<Integer, byte[]> prevDict; // Hashmap that holds last 32 dic size

    public static void main(String args[]) {
        Pigzj mainclass = new Pigzj(); 

        //Parse arguments
        if (args.length > 1) {
            System.err.println("Too many arguments.");
            mainclass.usage();
        }

        // If arg, get # processes
        if (args.length != 0) {
            String option = args[0]; 
            
            if (option.length() < 3 || (option.charAt(0) != '-' && option.charAt(1) != 'p')) {
                System.err.println("Invalid argument");
                mainclass.usage(); 
            }
            
            try {
                nThreads = Integer.parseInt(option.substring(2, option.length()));
            } catch (NumberFormatException e) {
                System.err.println("Invalid # of processes");
                mainclass.usage(); 
            }
            if (nThreads > Runtime.getRuntime().availableProcessors() || nThreads < 1) {
                System.err.println("Out of range request detected.\n"
                    + "Number of available processors is " + 
                    Runtime.getRuntime().availableProcessors()); 
                System.exit(1); 
            }
        }
        else { // default # of processes 
            nThreads = Runtime.getRuntime().availableProcessors(); 
        }

        try { 
            mainclass.readInput();
            /*Before we compress, we will use a dictionary to store "prev"*/
            int dictionarySize = numBlocks; 
            prevDict = new HashMap<Integer, byte[]>(dictionarySize); 
            prevDict.put(0, null); // The 0th block doesn't have a prev. 
            threads = new Thread[nThreads]; 
            mainclass.setupDictionary(); 

            /* Create threads */ 
            threads = new Thread[nThreads]; 
            for (int i = 0; i < nThreads; i++) {
                Runnable runnable = new dictThreads(i); 
                threads[i] = new Thread(runnable); 
            }
            for (int i = 0; i < nThreads; i++) {
                threads[i].start(); 
            }
            for (int i = 0; i < nThreads; i++) {
                threads[i].join(); 
            }

            for (int i = 0; i < nThreads; i++) {
                Runnable runnable = new CompressThreads(i); 
                threads[i] = new Thread(runnable); 
            }
            for (int i = 0; i < nThreads; i++) {
                threads[i].start(); 
            }
            for (int i = 0; i < nThreads; i++) {
                threads[i].join(); 
            }
            //All threads done. Last block will be done by a single thread. 
            pigzjOutputStream.finish(data, lastCur, lastWrite, prevDict.get(lastCount), lastCount); 
        } catch (IOException e) {
            mainclass.handleIO(); 
        } catch (InterruptedException ie) {
            System.err.println("Interrupted exception");
            System.exit(1);
        }

    }
    
    private void readInput() throws IOException {
        /* Put file into a byte array */ 
        data = System.in.readAllBytes(); 
        size = data.length; 
        //System.err.println(data.length);
        if (size%readSize != 0) {
            numBlocks = size/readSize + 1; 
        }
        else {
            numBlocks = size/readSize; 
        }
        //System.err.println(numBlocks);
        pigzjOutputStream = new PigzjOutputStream(System.out, size, numBlocks, readSize, data);
    }

    private void setupDictionary() {
   
    }

    private void handleIO() {
        System.err.println("IO exception caught. Exiting");
        System.exit(1);
    }
    private void usage() {
        System.err.println("Usage: Pigzj OPTIONAL: -p[numthreads]");
        System.exit(1);
    }

    private static class dictThreads implements Runnable {
        private int threadID;
        public dictThreads(int id){
            this.threadID = id; 
        }
        public void run() {
            int cur = threadID * readSize; 
            int writeSize = readSize; 
            int count = threadID + 1;
            byte[] prev = null; 
            int blockSize = kib; 
            while (cur < size) {
    
                //Smallest block may not be a full block size 
                if (cur + writeSize > size) {
                    writeSize = size-cur; 
                }
    
                //Smallest block may not have a full 32 KiB available 
                if (cur + kib > size) {
                    blockSize = size-cur; 
                }
                prev = new byte[blockSize]; 
                prev = Arrays.copyOfRange(data, (cur+writeSize-blockSize), (cur+writeSize)); 
                prevDict.put(count, prev); 
                count += nThreads;
                cur += nThreads * writeSize; 
            }
        }
    }
    private static class CompressThreads implements Runnable {  

        private int threadID;
        private Deflater def; 
        public CompressThreads(int id){
            this.threadID = id; 
            this.def = new Deflater(Deflater.DEFAULT_COMPRESSION, true); 
        }

         // Idea for threading:
            // Example with 3 threads: 
            // data blocks: 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15
            // Thread 0 gets 0, 3, 6, 9, 12, 15 
            // Thread 1 gets 1, 4, 7, 10, 13 
            // Thread 2 gets 2, 5, 8, 11, 14 
            // Each thread starts at: cur += (threadNum) * readSize 
            // Each thread increments += (numThreads) * readSize 
            // hmmm im thinking we have some way to buffer writes until we have sequential
            // threads done? 

            // NOTE: Use AtomicInteger... etc when possible

        public void run() {
        int cur = threadID * readSize; 
        int writeSize = readSize; 
        int count = threadID;
        while (cur < size){
            if (cur + writeSize >= size) { 
                    writeSize = size - cur; 
                    lastCur = cur; 
                    lastWrite = writeSize; 
                    lastCount = count;
                    break;
            } 
         
            try {

                pigzjOutputStream.compress(def, data, cur, writeSize, prevDict.get(count), count); 
            } catch (IOException e) {
                System.err.println("IO Excception"); 
                System.exit(1);
            }
            count += nThreads;
            cur+= nThreads * writeSize;   
            }
        }
    }
}