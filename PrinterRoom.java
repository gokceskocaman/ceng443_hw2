import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

public class PrinterRoom
{

    private class Printer implements Runnable
    {
        // TODO: Implement
        // ....

        int id;

        public Printer(int id, IMPMCQueue<PrintItem> roomQueue)
        {


            // TODO: Implement
            this.id = id;
            String s = SyncLogger.FORMAT_PRINTER_LAUNCH.replace("%d", "" + id);
            SyncLogger.Instance().Log(SyncLogger.ThreadType.MAIN_THREAD, 0,  s);
            Thread thread = new Thread(this);
            thread.start();


        }

        //later
        @Override
        public void run() {


            while(true){
                try {

                    lock.lock();
                    PrintItem item = roomQueue.Consume();
                    String s = SyncLogger.FORMAT_PRINT_DONE.replace("%s", item.getPrintType().name() + item.toString()) ;


                    SyncLogger.Instance().Log(SyncLogger.ThreadType.CONSUMER, id, s);
                } catch (QueueIsClosedExecption | InterruptedException e) {
                    String s = "Terminating..." ;
                    SyncLogger.Instance().Log(SyncLogger.ThreadType.CONSUMER, id, s);
                    cls = true;
                    closed.signal();

                    break;

                }

                finally {
                    lock.unlock();
                }
            }



        }
    }

    boolean isClosed = false;

    Lock lock1 = new ReentrantLock();

    private static Lock lock = new ReentrantLock();

    // Create a condition
    private static Condition closed = lock.newCondition();
    boolean cls = false;

    //private IMPMCQueue<PrintItem> roomQueue;
    public IMPMCQueue<PrintItem> roomQueue;
    private final List<Printer> printers;



    public PrinterRoom(int printerCount, int maxElementCount)
    {
        // Instantiating the shared queue
        roomQueue = new PrinterQueue(maxElementCount);

        // Let's try streams
        // Printer creation automatically launches its thread
        printers = Collections.unmodifiableList(IntStream.range(0, printerCount)
                                                         .mapToObj(i -> new Printer(i, roomQueue))
                                                         .collect(Collectors.toList()));
        // Printers are launched using the same queue


    }

    public boolean SubmitPrint(PrintItem item, int producerId)
    {
        // TODO: Implement

        String s = SyncLogger.FORMAT_ADD.replace("%s", item.getPrintType().name()) + item.toString();

        SyncLogger.Instance().Log(SyncLogger.ThreadType.PRODUCER, producerId, s);

        lock1.lock();
        boolean h = true;

        try {

            try {

                roomQueue.Add(item);
                return true;
            } catch (QueueIsClosedExecption | InterruptedException e) {
                String str = SyncLogger.FORMAT_ROOM_CLOSED .replace("%s", item.getPrintType().name() + item.toString()) ;

                SyncLogger.Instance().Log(SyncLogger.ThreadType.PRODUCER, producerId, str);

                h = false;
                return false;

            }


        }

        finally {
            lock1.unlock();

        }




    }

    public void CloseRoom()
    {

        // TODO: Implement

        roomQueue.CloseQueue();
        SyncLogger.Instance().Log(SyncLogger.ThreadType.MAIN_THREAD, 0,
                "Closing Room");



        lock.lock();
        if(cls){

        }
            else{
        try {
            closed.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
      }

        lock.unlock();

    }
}
