import java.util.*;
import java.io.*;

class Allocator {

    public static MemorySlot[] memorySlots;
    public static Process[] processes;
    static Comparator<MemorySlot> ascSize;
    static Comparator<MemorySlot> descSize;
    static Comparator<MemorySlot> ascID;
    
    static {
        ascSize = new Comparator<MemorySlot>(){
            @Override
            public int compare(MemorySlot s1, MemorySlot s2){
                Integer i = new Integer(s1.size);
                return i.compareTo(s2.size);
            }
        };
        descSize = new Comparator<MemorySlot>(){
            @Override
            public int compare(MemorySlot s1, MemorySlot s2){
                Integer i = new Integer(s2.size);
                return i.compareTo(s1.size);
            }
        };
        ascID = new Comparator<MemorySlot>(){
            @Override
            public int compare(MemorySlot s1, MemorySlot s2){
                Integer i = new Integer(s1.iD);
                return i.compareTo(s2.iD);
            }
        };
    };
    
    public static void main(String[] args) throws IOException {
        readFile();
        ffAllocate();
        bfAllocate();
        wfAllocate();
    }

    public static void readFile() throws FileNotFoundException {
        Scanner scannerM;
        Scanner scannerP;
        Scanner kb = new Scanner(System.in);
        String mFile, pFile;
        int memSlotCount, addrStart, addrEnd;
        int processCount, iD, size;


        System.out.println("Enter memory file");
        mFile = kb.next();
        scannerM = new Scanner(new File(mFile));

        System.out.println("Enter process file");
        pFile = kb.next();
        scannerP = new Scanner(new File(pFile));


        memSlotCount = scannerM.nextInt();       //initializes free memory spaces
        memorySlots = new MemorySlot[memSlotCount];
        System.out.println("Number of free memory slots: " + memSlotCount);
        
        for(int i=0; i < memSlotCount; i++) {
            addrStart = scannerM.nextInt();
            addrEnd = scannerM.nextInt();
            memorySlots[i] = new MemorySlot(addrStart, addrEnd, i);
            System.out.println("Slot " + i + " has size " + memorySlots[i].size);
        }
        scannerM.close();
//-------------------------------------------------------------------------------------------------

        processCount= scannerP.nextInt();      //initializes processes and adds to queue
        processes = new Process[processCount];
        System.out.println("Number of processes: " + processCount);
        
        Process process;
        for(int i=0; i < processCount; i++) {
            iD = scannerP.nextInt();
            size = scannerP.nextInt();
            process = new Process(iD, size);
            processes[i] = process;
            System.out.println("Process " + iD + " has size " + size);
        }
        scannerP.close();
    }

    public static void ffAllocate() throws IOException {
        Queue<Process> queue = new LinkedList<>();
        MemorySlot[] tempSlots = new MemorySlot[memorySlots.length];
        Process curProcess;
        
        System.out.println();
        System.out.println("FIRST FIT ALLOCATION");
        for(int i=0; i<memorySlots.length; i++){
            tempSlots[i] = new MemorySlot(memorySlots[i].addrStart, memorySlots[i].addrEnd, i);
        }

        for(int i=0; i < processes.length; i++)
            queue.add(processes[i]);

        for(int i=0; i < processes.length; i++) {
            curProcess = queue.remove();
            System.out.println("Allocating process " + curProcess.iD + "...");
            
            for(int index=0; index < tempSlots.length; index++) {   //
                if(tempSlots[index].processAL.size() >= 1){
                    curProcess.addrStart = tempSlots[index].processAL.get(tempSlots[index].processAL.size()-1).addrEnd;
                    tempSlots[index].usedSpace = tempSlots[index].usedSpace + curProcess.size;
                    curProcess.addrEnd = curProcess.size + curProcess.addrStart;
                    tempSlots[index].processAL.add(curProcess);
                    tempSlots[index].size = tempSlots[index].size - curProcess.size;
                    tempSlots[index].processAL.get(tempSlots[index].processAL.size()-1).allocated = true;
                    System.out.println("Process " + curProcess.iD + " is in slot " + tempSlots[index].iD + " with remaining size " + tempSlots[index].size);
                    break;
                }else{
                    curProcess.addrStart = tempSlots[index].addrStart + tempSlots[index].usedSpace;
                    tempSlots[index].usedSpace = tempSlots[index].usedSpace + curProcess.size;
                    curProcess.addrEnd = tempSlots[index].usedSpace + curProcess.addrStart;
                    tempSlots[index].processAL.add(curProcess);
                    tempSlots[index].size = tempSlots[index].size - curProcess.size;
                    tempSlots[index].processAL.get(tempSlots[index].processAL.size()-1).allocated = true;
                    System.out.println("Process " + curProcess.iD + " is in slot " + tempSlots[index].iD + " with remaining size " + tempSlots[index].size);
                    break;
                }
            }
            if(curProcess.allocated == false){
                queue.add(curProcess);
                System.out.println("Process " + curProcess.iD + " could not be allocated");
            }
        }
        outputResults(tempSlots, "FFoutput.data");
    }

    public static void bfAllocate() throws IOException{
        
        Queue<Process> queue = new LinkedList<>();
        MemorySlot[] tempSlots = new MemorySlot[memorySlots.length];
        Process curProcess;
        
        System.out.println();
        System.out.println("BEST FIT ALLOCATION");
        for(int i=0; i<memorySlots.length; i++){
            tempSlots[i] = new MemorySlot(memorySlots[i].addrStart, memorySlots[i].addrEnd, i);
        }
        
        for(int i=0; i < processes.length; i++)
            queue.add(processes[i]);

        int alCount=0;
        while(!queue.isEmpty()){
            Arrays.sort(tempSlots, ascSize);
            curProcess = queue.remove();
            System.out.println("Allocating process " + curProcess.iD + "...");
            
            for(int index=0; index < tempSlots.length; index++){
                if(curProcess.size < tempSlots[index].size){
                    if(tempSlots[index].processAL.size() >= 1){
                        curProcess.addrStart = tempSlots[index].processAL.get(tempSlots[index].processAL.size()-1).addrEnd;
                        tempSlots[index].usedSpace = tempSlots[index].usedSpace + curProcess.size;
                        curProcess.addrEnd = curProcess.size + curProcess.addrStart;
                        tempSlots[index].processAL.add(curProcess);
                        tempSlots[index].size = tempSlots[index].size - curProcess.size;
                        tempSlots[index].processAL.get(tempSlots[index].processAL.size()-1).allocated = true;
                        System.out.println("Process " + curProcess.iD + " is in slot " + tempSlots[index].iD + " with remaining size " + tempSlots[index].size);
                        break;
                    }else{
                        curProcess.addrStart = tempSlots[index].addrStart + tempSlots[index].usedSpace;
                        tempSlots[index].usedSpace = tempSlots[index].usedSpace + curProcess.size;
                        curProcess.addrEnd = tempSlots[index].usedSpace + curProcess.addrStart;
                        tempSlots[index].processAL.add(curProcess);
                        tempSlots[index].size = tempSlots[index].size - curProcess.size;
                        tempSlots[index].processAL.get(tempSlots[index].processAL.size()-1).allocated = true;
                        System.out.println("Process " + curProcess.iD + " is in slot " + tempSlots[index].iD + " with remaining size " + tempSlots[index].size);
                        break;
                    }
                }
            }
            if(curProcess.allocated == false)
                System.out.println("Process " + curProcess.iD + " could not be allocated");
        }
        Arrays.sort(tempSlots, ascID);
        outputResults(tempSlots, "BFoutput.data");
    }

    public static void wfAllocate() throws IOException{
        
        Queue<Process> queue = new LinkedList<>();
        MemorySlot[] tempSlots = new MemorySlot[memorySlots.length];
        Process curProcess;
        
        System.out.println();
        System.out.println("WORST FIT ALLOCATION");
        for(int i=0; i<memorySlots.length; i++){
            tempSlots[i] = new MemorySlot(memorySlots[i].addrStart, memorySlots[i].addrEnd, i);
        }

        for(int i=0; i < processes.length; i++)
            queue.add(processes[i]);

        while(!queue.isEmpty()){
            Arrays.sort(tempSlots, descSize);
            curProcess = queue.remove();
            System.out.println("Allocating process " + curProcess.iD + "...");
            for(int index=0; index < tempSlots.length; index++){
                if(curProcess.size < tempSlots[index].size){
                    if(tempSlots[index].processAL.size() >= 1){
                        curProcess.addrStart = tempSlots[index].processAL.get(tempSlots[index].processAL.size()-1).addrEnd;
                        tempSlots[index].usedSpace = tempSlots[index].usedSpace + curProcess.size;
                        curProcess.addrEnd = curProcess.size + curProcess.addrStart;
                        tempSlots[index].processAL.add(curProcess);
                        tempSlots[index].size = tempSlots[index].size - curProcess.size;
                        tempSlots[index].processAL.get(tempSlots[index].processAL.size()-1).allocated = true;
                        System.out.println("Process " + curProcess.iD + " is in slot " + tempSlots[index].iD + " with remaining size " + tempSlots[index].size);
                        break;
                    }else{
                        curProcess.addrStart = tempSlots[index].addrStart + tempSlots[index].usedSpace;
                        tempSlots[index].usedSpace = tempSlots[index].usedSpace + curProcess.size;
                        curProcess.addrEnd = tempSlots[index].usedSpace + curProcess.addrStart;
                        tempSlots[index].processAL.add(curProcess);
                        tempSlots[index].size = tempSlots[index].size - curProcess.size;
                        tempSlots[index].processAL.get(tempSlots[index].processAL.size()-1).allocated = true;
                        System.out.println("Process " + curProcess.iD + " is in slot " + tempSlots[index].iD + " with remaining size " + tempSlots[index].size);
                        break;
                    }
                }
            }
            if(curProcess.allocated == false)
                System.out.println("Process " + curProcess.iD + " could not be allocated");
        }
        Arrays.sort(tempSlots, ascID);
        outputResults(tempSlots, "WFoutput.data");
    }

    public static void outputResults(MemorySlot[] targetArr, String filename) throws IOException{
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        boolean allAllocated = true;

        for(int i=0; i<targetArr.length; i++){
            if(targetArr[i].processAL != null){
                for(int j=0; j<targetArr[i].processAL.size(); j++)
                    writer.write(targetArr[i].processAL.get(j).addrStart + " " + targetArr[i].processAL.get(j).addrEnd + " " + targetArr[i].processAL.get(j).iD + '\n');
            }
        }
        for(int i=0; i<targetArr.length; i++){
            for(int j=0; j<targetArr[i].processAL.size(); j++){
                if(targetArr[i].processAL.get(j).allocated == false){
                    writer.write("-" + targetArr[i].processAL.get(j).iD + ",");
                    allAllocated = false;
                }else{
                    allAllocated = true;
                }
            }
        }
        if(allAllocated == true)
            writer.write("-0");

        writer.close();
    }

}

class MemorySlot {
    
    int addrStart, addrEnd, size, iD, usedSpace=0;
    ArrayList<Process> processAL = new ArrayList<Process>();

    public MemorySlot(int addrStart, int addrEnd, int iD) {
        this.addrStart = addrStart;
        this.addrEnd = addrEnd;
        this.iD = iD;
        this.size = (this.addrEnd - this.addrStart);
    }
}

class Process {

    int iD, size, addrStart, addrEnd;
    boolean allocated;

    public Process(int iD, int size) {
        this.iD = iD;
        this.size = size;
        this.allocated = false;
    }
}