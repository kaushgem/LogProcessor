# Log Processor

Main.java - Starting point of execution.
It calls the processLogs() method of the Singleton class - LogProcessorService (Implemented using Enum)

## LogProcessorService contains 4 ExecutorService for the following purpose

```
1. FileReadTask
2. FileWriteTask
3. LineNumberUpdateTask
4. MemoryMonitor
```

  1. Read Log files and store them in a BlockingQueue (filesToProcess) wrapped as a FileBean
  2. Process Log files - take a fileBean from filesToProcess BlockingQueue append line numbers and modify the fileBean and add it to filesToWrite BlockingQueue
  3. Write Log Files back - take a fileBean from filesToWrite and persist in the path, this will replace the existing log file with the same Name
  4. Monitor Service - monitors the JVM memory and pauses the ReadService from loading the Queue until process and write service processes them and also waits until the gc clears the memory

## How to run the project

  1. Modify the pom.xml parameters
      i.  location of the directory containing the log files
      ii. the number of threads
  2. Run the project using maven (test or package)
  3. This will automatically run the test cases and the project
