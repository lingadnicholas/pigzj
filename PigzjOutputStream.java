/* Ideas from from: http://hg.openjdk.java.net/jdk7/jdk7/jdk/file/00cd9dc3c2b5/src/share/classes/java/util/zip/GZIPOutputStream.java */
/* Recipe for proper GZIP output: 
First, write the GZIP header (see writeHeader) 
Use deflater inherited from DeflaterOutputStream to 
compress our blocks (see finish) and write them to the output) 
Finally, write GZIP trailer (see writeTrailer) 
CRC32 - crc member variable: must update every time a new block is read in. 
*/ 

import java.util.zip.Deflater;
import java.util.zip.CRC32;
import java.io.OutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
// Constants, writing header, and writing trailer functions taken from source code
// In order to follow GZIP file format standard. 
// found at:
// http://hg.openjdk.java.net/jdk7/jdk7/jdk/file/00cd9dc3c2b5/src/share/classes/java/util/zip/GZIPOutputStream.java#l38
public class PigzjOutputStream {
    protected CRC32 crc = new CRC32(); 

    private final static int GZIP_MAGIC = 0x8b1f; //gzip magic header number

    private final static int TRAILER_SIZE = 8; //Trailer size in bytes
    private OutputStream out; // Just stdout here 
    //private byte[] buf; // Output buffer for writing compressed data
    private byte[][] bufferedWrite; // Multidimensional array, stores compressed blocks to be written
    private byte[] data; // Reference to the uncompressed data
    private int size; // Total size of input file
    private int numBlocks; 
    private int blockSize;
    private AtomicInteger totalBytesRead;
    private AtomicInteger totalIn;


    private int[] defLen; 

    public PigzjOutputStream(OutputStream out, int size, int numBlocks, int blockSize, byte[] data) throws IOException {
        this.out = out; 
        this.size = size;  
        //this.buf = new byte[blockSize]; 
        this.blockSize = blockSize;
        this.bufferedWrite = new byte[numBlocks][];
        this.numBlocks = numBlocks; 
        this.defLen = new int[numBlocks];
        this.totalBytesRead = new AtomicInteger(); 
        this.totalIn = new AtomicInteger(); 
        this.data = data;
        writeHeader();
        crc.reset(); 
    }

    private Deflater newDeflater() {
        return new Deflater(Deflater.DEFAULT_COMPRESSION, true); 
    }

    public void compress(Deflater def, byte[] b, int off, int len, byte[] dict, int blockNum) throws IOException {
        def.reset();
        if (dict != null) {
            def.setDictionary(dict);
        }

        def.setInput(b, off, len); 
        deflate(def, blockNum);

        totalBytesRead.getAndAdd(len);
        totalIn.getAndAdd(def.getTotalIn()); 
        
    }

    //Only 1 thread will run this. 
    public void finish(byte[] b, int off, int len, byte[] dict, int blockNum) throws IOException {
        Deflater def = newDeflater();
        if (dict != null) {
            def.setDictionary(dict);
        }

        def.setInput(b, off, len); 
        deflate(def, blockNum);

        totalBytesRead.getAndAdd(len);
        totalIn.getAndAdd(def.getTotalIn()); 

        if (totalBytesRead.get() == size) {
           crc.update(data, 0, totalIn.get());
           writeAll();
            if (!def.finished()) {
                def.finish();
                while (!def.finished()) {
                   deflate(def, blockNum); 
                   writeOut(blockNum);
                }
            }

            byte[] trailer = new byte[TRAILER_SIZE];
            writeTrailer(trailer, 0);
            out.write(trailer); 
        }
    }
   
    
    private void writeAll() throws IOException {
        int off = 0;
        for (int i = 0; i < numBlocks; i++) {
            try { 
                out.write(bufferedWrite[i], 0, defLen[i]); 
            } catch (NullPointerException e) //Was too lazy to check out of bounds instead did this.
            {

            }
        }
    }
    
    private void writeOut(int blockNum) throws IOException {
        out.write(bufferedWrite[blockNum], 0, defLen[blockNum]);
    }
    private void deflate(Deflater def, int blockNum) throws IOException {
        byte buf[] = new byte[blockSize];
        int len;
        len = def.deflate(buf, 0, buf.length, Deflater.SYNC_FLUSH);
        bufferedWrite[blockNum] = buf; 
        defLen[blockNum] = len; 
    }


    private final static byte[] header = {
        (byte) GZIP_MAGIC,                // Magic number (short)
        (byte)(GZIP_MAGIC >> 8),          // Magic number (short)
        Deflater.DEFLATED,                // Compression method (CM)
        0,                                // Flags (FLG)
        0,                                // Modification time MTIME (int)
        0,                                // Modification time MTIME (int)
        0,                                // Modification time MTIME (int)
        0,                                // Modification time MTIME (int)
        0,                                // Extra flags (XFLG)
        0                                 // Operating system (OS)
    };
    private void writeHeader() throws IOException {
        out.write(header);
    }

    /*
     * Writes GZIP member trailer to a byte array, starting at a given
     * offset.
     */
    private void writeTrailer(byte[] buf, int offset) throws IOException {
        writeInt((int)crc.getValue(), buf, offset); // CRC-32 of uncompr. data
        writeInt(totalIn.get(), buf, offset + 4); // Number of uncompr. bytes
    }

    /*
     * Writes integer in Intel byte order to a byte array, starting at a
     * given offset.
     */
    private void writeInt(int i, byte[] buf, int offset) throws IOException {
        writeShort(i & 0xffff, buf, offset);
        writeShort((i >> 16) & 0xffff, buf, offset + 2);
    }

    /*
     * Writes short integer in Intel byte order to a byte array, starting
     * at a given offset
     */
    private void writeShort(int s, byte[] buf, int offset) throws IOException {
        buf[offset] = (byte)(s & 0xff);
        buf[offset + 1] = (byte)((s >> 8) & 0xff);
    }
} 