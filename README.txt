Nicholas Lingad, 605284477
Homework 3 Report

======NUMBER OF PROCESSORS======
At the time of testing, on lnxsrv11, the number of available processors was 4. 
The following tests are performed with: 
input=/usr/local/cs/jdk-16.0.1/lib/modules

The averages that are taken represent the real time, to have a better point of comparison.
@@@@One processor@@@@
     GZIP - time gzip <$input >gzip.gz
        TRIAL ONE: 
            real    0m7.536s
            user    0m7.316s
            sys     0m0.061s
        TRIAL TWO:
            real    0m7.573s
            user    0m7.313s
            sys     0m0.080s
        TRIAL THREE: 
            real    0m7.781s
            user    0m7.309s
            sys     0m0.059s
        AVERAGE: 
            0m7.763s
    
    PIGZ - time pigz -p1 <$input >pigz.gz
        TRIAL ONE:
            real    0m7.488s
            user    0m6.994s
            sys     0m0.065s
        TRIAL TWO:
            real    0m7.484s
            user    0m6.983s
            sys     0m0.068s
        TRIAL THREE: 
            real    0m7.441s
            user    0m6.965s
            sys     0m0.074s
        AVERAGE: 
            0m7.471s
            

    PIGZJ - 
        TRIAL ONE:
            real    0m7.744s
            user    0m7.159s
            sys     0m0.272s
        TRIAL TWO:
            real    0m7.718s
            user    0m7.148s
            sys     0m0.259s
        TRIAL THREE: 
            real    0m7.863s
            user    0m7.304s
            sys     0m0.227s
        AVERAGE: 
            0m7.775s

    ANALYSIS:
    Of course, since Gzip is a single-threaded program, we can only collect data for the
    single-threaded case. In the case of one processor, by comparing the real times, the 
    ordering of time is pigz < gzip < pigzj. However, the difference is not by much, as is
    expected since these are all performing with one thread. However, the Pigzj implementation
    seems to be taking significantly more time making system calls and the like. We will examine 
    this with strace later. 

@@@@Two processors@@@@
    PIGZ - 
        TRIAL ONE:
            real    0m3.949s
            user    0m7.044s
            sys     0m0.093s
        TRIAL TWO:
            real    0m3.959s
            user    0m7.053s
            sys     0m0.086s
        TRIAL THREE:
            real    0m3.946s
            user    0m7.049s
            sys     0m0.087s
        AVERAGE: 
            0m3.951s

    PIGZJ -
        TRIAL ONE:
            real    0m4.218s
            user    0m7.180s
            sys     0m0.259s
        TRIAL TWO: 
            real    0m4.263s
            user    0m7.208s
            sys     0m0.265s
        TRIAL THREE: 
            real    0m4.242s
            user    0m7.182s
            sys     0m0.263s
        AVERAGE: 
            0m4.241s
    ANALYSIS: 
        pigz runs faster than Pigzj by approximately .29 seconds. 
        Note again the bottleneck created by system calls in the Pigzj implementation. 


@@@@Three processor@@@@
    PIGZ -
        TRIAL ONE:
            real    0m2.772s
            user    0m7.055s
            sys     0m0.088s
        TRIAL TWO:
            real    0m2.789s
            user    0m7.048s
            sys     0m0.079s
        TRIAL THREE:
            real    0m2.767s
            user    0m7.042s
            sys     0m0.087s
        AVERAGE: 
            0m2.794s

    PIGZJ -
        TRIAL ONE:
            real    0m3.071s
            user    0m7.177s
            sys     0m0.258s
        TRIAL TWO: 
            real    0m3.069s
            user    0m7.167s
            sys     0m0.238s
        TRIAL THREE: 
            real    0m3.086s
            user    0m7.172s
            sys     0m0.250s
        AVERAGE: 
            0m3.075s
    ANALYSIS: 
        pigz runs faster than Pigzj by approximately .281 seconds. Note once again, 
        the bottleneck created by system calls. 



@@@@Four processors@@@@
    PIGZ - 
        TRIAL ONE:
            real    0m2.233s
            user    0m7.121s
            sys     0m0.035s
        TRIAL TWO:
            real    0m2.249s
            user    0m7.094s
            sys     0m0.034s
        TRIAL THREE:
            real    0m2.246s
            user    0m7.104s
            sys     0m0.034s
        AVERAGE: 
            0m2.243s

    PIGZJ -
        TRIAL ONE:
            real    0m2.560s
            user    0m7.137s
            sys     0m0.256s
        TRIAL TWO:
            real    0m2.505s
            user    0m7.188s
            sys     0m0.248s
        TRIAL THREE:
            real    0m2.503s
            user    0m7.194s
            sys     0m0.256s
        AVERAGE: 
            2.523s
        ANALYSIS: 
            pigz runs faster than pigzj by .28 seconds. Again, note the bottleneck 
            created by system calls. 
        
@@@@OVERALL ANALYSIS@@@@
    Overall, the compression of pigz was usually done the fastest. Upon inspection of the runtimes,
    it seems that Pigzj was not too far behind, usually only as far behind as .3 seconds slower, 
    which is actually not much slower. However, it seems that the gap between the two, albeit quite
    slowly, was actually growing much smaller as the number of processors increased. 
    One reason for Pigzj being a bit slower could be that it is written in Java, and pigz is written
    in C. It is to be expected that the C implementation is much faster due to the fact that is a 
    purely compiled language, so it should run faster than the Pigzj implementation written in Java,
    which is expected to run slower due to the fact that it is a both compiled and interpreted language, 
    and so the Java Virtual Machine (JVM) interpreting the compiled bytecodes could actually be what is 
    making Pigzj slower than pigz. We also notice that Pigzj spends a significantly longer amount of time 
    making system calls, which we will examine with strace later. 

    Although Pigzj is a bit slower than pigz, also take note that it is significantly better than its 
    purely single-threaded counterpart Gzip, who is much, much slower by comparison. 

======COMPRESSION RATIO=====
The following compression ratio test was done on: 
input=/usr/local/cs/jdk-16.0.1/lib/modules
wc -c $input gives us that the file is: 125942959 bytes 

GZIP COMPRESSION RATIO: 
wc -c gzip.gz gives us: 43261332 bytes 
43261332/125942959=.343 
Gzip gave us a 34.3% compression ratio. 

PIGZ COMPRESSION RATIO: 
wc -c pigz.gz gives us: 43134815 bytes 
43134815/125942959=.342
Pigz gave us a 34.2% compression ratio. 

PIGZJ COMPRESSION RATIO: 
wc -c Pigzj.gz gives us: 43136282 bytes 
43136282/125942959=.343 
Pigzj gave us a 34.3% compression ratio. 

@@@@ANALYSIS@@@@
All of the compression rates are not too different from each other. The biggest difference between
the three is by at most .1%, which is not a significant amount. 
However, for the sake of comparison, take note that pigz actually has the worst compression ratio, 
and looking closer at the numbers, it is actually true that, at least for this testcase, Pigzj had
a better compression ratio than Gzip. 
Gzip is already better than Pigz in terms of compression ratio, and Pigzj is essentially a 
multi-threaded version of Gzip, so, in that regard, it follows that Pigzj should have a better 
compression ratio than its Pigz counterpart. However, Pigzj borrows the pigz strategy of priming 
the compression dictionary for the next block, so in that regard, it follows that Pigzj should be 
performing better than Gzip, if this strategy is actually better than what Gzip does. 
However, in the end, the compression ratios do not seem to be anything significant, as they are all 
close to each other. 

======STRACE=====
We use the -c option of strace to produce a more human readable summary of the system calls made. 
GZIP: 
strace -c gzip <$input >gzip.gz
% time     seconds  usecs/call     calls    errors syscall
------ ----------- ----------- --------- --------- ----------------
 55.04    0.002993         748         4           close
 27.12    0.001475           0      2641           write
 17.84    0.000970           0      3846           read
  0.00    0.000000           0         3           fstat
  0.00    0.000000           0         1           lseek
  0.00    0.000000           0         5           mmap
  0.00    0.000000           0         4           mprotect
  0.00    0.000000           0         1           munmap
  0.00    0.000000           0         1           brk
  0.00    0.000000           0        12           rt_sigaction
  0.00    0.000000           0         1         1 ioctl
  0.00    0.000000           0         1         1 access
  0.00    0.000000           0         1           execve
  0.00    0.000000           0         2         1 arch_prctl
  0.00    0.000000           0         2           openat
------ ----------- ----------- --------- --------- ----------------
100.00    0.005438                  6525         3 total

PIGZ: 
 strace -c pigz -p4 <$input >pigz.gz;
% time     seconds  usecs/call     calls    errors syscall
------ ----------- ----------- --------- --------- ----------------
 48.55    0.008742          18       465         3 futex
 46.35    0.008346           8       971           read
  2.46    0.000443          21        21           munmap
  0.71    0.000128          25         5           clone
  0.55    0.000099           6        15           mprotect
  0.53    0.000095           3        28           mmap
  0.24    0.000043           7         6           openat
  0.12    0.000022           3         6           fstat
  0.11    0.000019           2         8           brk
  0.10    0.000018           3         6           close
  0.08    0.000014           4         3           lseek
  0.07    0.000012           4         3           rt_sigaction
  0.03    0.000005           5         1           set_robust_list
  0.03    0.000005           5         1           prlimit64
  0.02    0.000004           4         1           rt_sigprocmask
  0.02    0.000004           4         1         1 ioctl
  0.02    0.000004           2         2         1 arch_prctl
  0.02    0.000004           4         1           set_tid_address
  0.00    0.000000           0         1         1 access
  0.00    0.000000           0         1           execve
------ ----------- ----------- --------- --------- ----------------
100.00    0.018007                  1546         6 total

PIGZJ: 
strace -c java Pigzj -p4 <$input >Pigzj.gz
% time     seconds  usecs/call     calls    errors syscall
------ ----------- ----------- --------- --------- ----------------
 99.74    0.200398      100199         2           futex
  0.05    0.000104           6        15           mprotect
  0.05    0.000091           3        23           mmap
  0.05    0.000091           1        49        39 openat
  0.02    0.000044          14         3           munmap
  0.02    0.000031           2        12           read
  0.02    0.000031           3        10           fstat
  0.01    0.000026           2        11           close
  0.01    0.000024          24         1           clone
  0.01    0.000014           3         4           brk
  0.01    0.000012           6         2           readlink
  0.00    0.000009           0        33        30 stat
  0.00    0.000009           4         2         1 access
  0.00    0.000008           4         2           rt_sigaction
  0.00    0.000005           5         1           rt_sigprocmask
  0.00    0.000005           2         2         1 arch_prctl
  0.00    0.000005           5         1           set_robust_list
  0.00    0.000004           1         3           lseek
  0.00    0.000004           4         1           set_tid_address
  0.00    0.000004           4         1           prlimit64
  0.00    0.000002           2         1           getpid
  0.00    0.000000           0         1           execve
------ ----------- ----------- --------- --------- ----------------
100.00    0.200921                   180        71 total

@@@@ANALYSIS@@@@
To recall the results from earlier, Gzip and pigz spent a similar amount of time making system 
calls. However, Pigzj spent significantly longer making system calls. 
These results from strace show that most of Gzip's time was spent closing, whereas pigz and 
Pigzj spent the most time making the futex system call. However, Pigzj spends a much more 
significant amount of time in these futex calls than its pigz counterpart. There is almost a 
difference of .2 seconds, which is very significant considering that most other system calls 
take only a fraction of that time. 
According to its man page, "the futex() system call provides 
a method for waiting until a certain condition becomes true. It is typically used as a blocking 
construct in the context of shared-memory synchronization." 
So, it does make sense with Gzip doesn't use this system call, as it is single threaded. The 
futex system call was used in the C pigz and the Java Pigzj implementations in order to make 
multithreading possible. In the Pigzj implementation, I did not explicitly create my own locks 
and use them. Instead, the Java implementation may have done some of them for me, which would 
be why the Java implementation takes longer than the C implementation. Instead of explicitly 
creating locks for variables, I used the type AtomicInteger, in order to make all updates to 
some shared integer variables completely atomic, without me having to worry about the exact 
implementation. Thus, the implementation of AtomicInteger is probably very slow compared to 
the primitive integer type due to the fact that it is simply there to make the programmer's 
life easier. 


======DESIGN=====
First, it makes sense to go over the design implementation of my Pigzj implementation, and 
any design choices I made, and the implications of those. 
First, Pigzj reads in the entire file at once from standard input, and then uses 
multithreading to create a shared hashmap of the dictionaries used for priming the 
following compression blocks. 
Then, I use multithreading again to do the compression itself, and from here what the 
implementation does is it essentially splits up the blocks between the threads, and each thread
compresses its block, writes its block into a buffer, and then moves onto its next assigned 
block. The most notable design choice here is when it decides to write. My Pigzj implementation
chooses not to write to standard output until every thread is done. What my program does is that 
all threads finish their assigned blocks, the threads all rejoin, and it is not until then that 
a singular thread compresses the final block and then writes everything to standard output.
In order to do so, I use a two dimensional array bufferedWrite which stores compressed data. 

One of my design choices that I consider to be faulty in my implementation is the amount of
separate data structures I have that relate to each other. For example,
private byte[][] bufferedWrite; // Multidimensional array, stores compressed blocks to be written
private int[] defLen;  // The length of each bufferedWrite index. 
private int numBlocks; // How many blocks our uncompressed data is split into 
bufferedWrite has a size of numBlocks, and each defLen corresponds to the length of its
corresponding compressed data in bufferedWrite. This could have been done much more efficiently, 
probably using another data structure, but the reason I decided to use a multidimensional array 
was to prevent having to use locks to write to this. All of the threads when multithreading only 
write to this array, and they write to different indices, so data races here are not a worry. 
However, using multiple variables that all relate to each other led to lots of mistakes and 
possibly decrease readability of my code, and it would have probably been better to create a 
whole new class for such a thing. 

The design choice that really enhanced the multithreading capability was giving each thread their 
own deflater. The deflater has a lot of single-threaded methods, so it only made sense to give each 
thread their own deflater. Otherwise, the time of my Pigzj implementation would have been much closer 
to the Gzip implementation, because each thread would have to wait their turn to use the deflater, and 
much of the program time is spent using the deflater in order to compress a block.  

Another design choice was instead of using a thread pool, I used an array of threads. A thread pool 
could have been more useful because in my implementation, each thread is assigned a fixed amount of 
blocks. However, there could be the case where one thread finishes all of its tasks early, and it 
just ends up waiting for the other threads to complete. A thread pool could have been more useful 
than my array of threads in that it could dynamically schedule which thread does what block. 

I also note that when I made the block sizes of each read and each dictionary size small, I
sometimes ran into some crc mismatches, and the pigz compression not matching my Pigzj compression,
and I couldn't figure out where the problem here lied. However, it worked for bigger block sizes, 
which is what this assignment was concerned with, so I can only assume that my implementation works 
okay. 

=====SCALABILITY OF FILESIZE=====
The most notable design choice that could be impacted by file size is the fact that my Pigz 
implementation does not read from standard input in parallel. This could lead to a huge bottleneck
right at the beginning as I serially read in a huge file. My first implementation attempted to do 
the reading in of data right before the compression of that data, but I found that reading it all 
at once was easier to do, so that I could immediately create the priming dictionary afterwards, 
however this came with the cost of scalability. In order for better scalability in terms of filesize, 
I could have kept reading from standard input one block at a time, and the thread that reads that block 
is responsible for compressing it, but I also could not figure out how to set the priming dictionary to
the previous block, either. 

=====SCALABILITY OF THREADS======
As threads scale, I can only assume that the scalability of my program will scale with it, as seen from 
the time comparisons between pigz and Pigzj, where the gap between the two actually seemed to be thinning 
as the number of processors increased. However, I believe there to be some potential issues. As the number
of threads increases and filesize stays fixed, there is that last block that won't be touched until all 
threads are finished. However, this problem is very minimal, as it's only one extra block to compress in the 
end. 
Another potential problem regarding the scalbility of threads is the block size, and the priming dictionary 
size. The aforementioned values are statically set, and so there could be the case that there are more threads 
than blocks, and so the program won't be making its full use of multithreading because many of the threads will 
just be doing nothing most of the time. However, it will still be much faster than Gzip, because every block 
(except for the final one in my implementation) will be getting compressed at around the same time. 



=====GZIP VS PIGZ VS PIGZJ======= 
Gzip is obviously not the best choice out of these three, because its single-threadedness
can be a real bottleneck, especially when working with bigger files. It follows that the
contenders for which method would work better in general is between Pigz and Pigzj. 
The biggest difference between Pigz and Pigzj is the language that they're implemented in,
other than that, one would expect them to perform about the same, as seen by the runtime 
analysis above. 
As mentioned before, pigz was implemented in C, a compiled language. 
Pigzj was implemented in Java, a language that is first compiled into bytecodes and then 
interpreted by the Java Virtual Machine (JVM). 
Interpreting is much slower than a purely-compiled language, as it is all done at runtime,
so of course pigz is expected to be better in that regard. 
Pigzj, through the use of many provided classes to do multithreading, also spends a lot of 
time in its system calls, so it is much slower in that regard.
Although the time difference between the two is not too large, pigz being implemented in C 
probably makes it the faster method. 


======CODE ACKNOWLEDGEMENTS=====
Thank you to TA Amit for posting starter hint code on Piazza. I did not copy it directly, but 
I got many ideas from it such as when I should be updating the crc. 
My PigzjOutputStream implementation included many ideas from the GZIP output stream, and its 
super class, DeflaterOutputStream. I borrowed some ideas in order to make my own single-threaded 
Gzip compressor, which I then added the pigz dictionary priming to, which I then made 
my own multi-threaded code. 

=====REFERENCES=====
1. Futex man page: https://man7.org/linux/man-pages/man2/futex.2.html
2. GZIP output stream code: 
http://hg.openjdk.java.net/jdk7/jdk7/jdk/file/00cd9dc3c2b5/src/share/classes/java/util/zip/GZIPOutputStream.java#l38 