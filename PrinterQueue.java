import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class ItemComparator implements Comparator<PrintItem>{

    // Overriding compare()method of Comparator
    // for descending order of cgpa
    public int compare(PrintItem p1, PrintItem p2) {
        if (p1.getPrintType() == PrintItem.PrintType.INSTRUCTOR && p2.getPrintType() != PrintItem.PrintType.INSTRUCTOR)
            return -1;
        else if (p1.getPrintType() != PrintItem.PrintType.INSTRUCTOR && p2.getPrintType() == PrintItem.PrintType.INSTRUCTOR)
            return 1;
        return 0;

    }
}

public class PrinterQueue implements IMPMCQueue<PrintItem>
{
    // TODO: This is all yours

    PriorityQueue<PrintItem> queue ;

    int queueSize;

    private static Lock lock = new ReentrantLock();



    private static Condition newSeat = lock.newCondition();


    private static Condition newObject = lock.newCondition();
    boolean isClosed = false;



    public PrinterQueue(int maxElementCount)
    {
        // TODO: Implement

        queue = new PriorityQueue<PrintItem>(maxElementCount, new ItemComparator());
        queueSize = maxElementCount;


        // You can change this signature but also don't forget to
        // change the instantiation signature on the
        // Printer room
    }

    //later all overrrides
    // InterruptedException is new
    @Override
    public void Add(PrintItem data) throws QueueIsClosedExecption{

        lock.lock();
        try {

            if(isClosed){
                throw new QueueIsClosedExecption();
            }

                if (queue.size() == queueSize) {
                    try {
                        //System.out.println("before");
                        newSeat.await();
                        //System.out.println("after");

                        if(!isClosed) {
                            queue.add(data);

                            newObject.signal();
                        }

                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }

                } else {
                    queue.add(data);
                    //String s = SyncLogger.FORMAT_ADD.replace("%s", data.getPrintType().name()) + data.toString();

                    //SyncLogger.Instance().Log(SyncLogger.ThreadType.PRODUCER, data.getId(), s);
                    newObject.signal();
                }


        }
        finally {
            lock.unlock();
        }





    }

    @Override
    public PrintItem Consume() throws QueueIsClosedExecption{

        lock.lock();

        try {
            if (queue.size() == 0) {

                if(isClosed){
                    throw new QueueIsClosedExecption();
                }

                try {
                    newObject.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                if(isClosed){
                    throw new QueueIsClosedExecption();
                }

                PrintItem x = queue.poll();
               //String s = SyncLogger.FORMAT_PRINT_DONE.replace("%s", x.getPrintType().name() + x.toString()) ;


               //SyncLogger.Instance().Log(SyncLogger.ThreadType.CONSUMER, x.getId(), s);
               //System.out.println(SyncLogger.ThreadType.CONSUMER + x.toString());


                newSeat.signal();
                return x;

            } else {
                PrintItem y = queue.poll();
                newSeat.signal();
                return y;
            }
        }finally {
            lock.unlock();
        }

        }






    @Override
    public int RemainingSize() {
        return queue.size();
    }

    @Override
    public void CloseQueue() {
        lock.lock();
        isClosed = true;
        newObject.signalAll();
        lock.unlock();


    }
}
