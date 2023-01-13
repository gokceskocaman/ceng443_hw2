import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main
{
    static class Producer implements Runnable
    {

        // TODO: You may want to implement this class to test your code
        private PrinterRoom room;
        private List<PrintItem> jobs;
        private List<Integer> sleep;
        private int id;
        public Producer(int id, PrinterRoom room, List<PrintItem> jobs, List<Integer> sleep){
            this.room = room;
            this.jobs = jobs;
            this.sleep = sleep;
            this.id = id;
        }
        public void run()
        {
            /*try {
                Thread.sleep((long)(2 * 1000));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } */

            // TODO: Provide a thread join functionality for the main thread
            try{
                for (int i = 0; i < this.jobs.size(); i++)
                {
                    Thread.sleep(this.sleep.get(i));
                    // Printing and display the elements in ArrayList
                    //SyncLogger.Instance().Log(SyncLogger.ThreadType.PRODUCER, this.id,
                           // String.format(SyncLogger.FORMAT_ADD, this.jobs.get(i)));
                    if(!room.SubmitPrint(this.jobs.get(i), this.id))
                    {
                        //SyncLogger.Instance().Log(SyncLogger.ThreadType.PRODUCER, this.id,
                                //String.format(SyncLogger.FORMAT_ROOM_CLOSED, this.jobs.get(i)));
                    }
                }
            } catch (InterruptedException e) { return; }
            finally {
                System.out.println("Producer " + id + " is terminating...");
            }
        }
    }

    public static void main(String args[]) throws InterruptedException
    {
        PrinterRoom room = new PrinterRoom(2, 2);


        SyncLogger.Instance().Log(SyncLogger.ThreadType.PRODUCER, 0,
                String.format(SyncLogger.FORMAT_PRODUCER_LAUNCH, 0));

        Producer p1 = new Producer(0, room, Arrays.asList(
                new PrintItem(300, PrintItem.PrintType.STUDENT, 0),
                new PrintItem(300, PrintItem.PrintType.STUDENT, 1),
                new PrintItem(300, PrintItem.PrintType.STUDENT, 2),
                new PrintItem(300, PrintItem.PrintType.INSTRUCTOR, 3)),
                Arrays.asList(0, 0, 5000, 0)
        );
        SyncLogger.Instance().Log(SyncLogger.ThreadType.PRODUCER, 1,
                String.format(SyncLogger.FORMAT_PRODUCER_LAUNCH, 1));


        Producer p2 = new Producer(1, room, Arrays.asList(
                new PrintItem(300, PrintItem.PrintType.INSTRUCTOR, 4),
                new PrintItem(300, PrintItem.PrintType.STUDENT, 5)),
                Arrays.asList(0, 0)
        );
        SyncLogger.Instance().Log(SyncLogger.ThreadType.PRODUCER, 2,
                String.format(SyncLogger.FORMAT_PRODUCER_LAUNCH, 2));
        Producer p3 = new Producer(2, room, Arrays.asList(
                new PrintItem(3000, PrintItem.PrintType.STUDENT, 6),
                new PrintItem(3000, PrintItem.PrintType.STUDENT, 7),
                new PrintItem(3000, PrintItem.PrintType.STUDENT, 8),
                new PrintItem(3000, PrintItem.PrintType.INSTRUCTOR, 9)),
                Arrays.asList(0, 0, 0, 0)
        );



        Thread t1 = new Thread(p1);
        Thread t2 = new Thread(p2);
        Thread t3 = new Thread(p3);



        t1.start();

        t2.start();
        t3.start();



        // Wait a little we are doing produce on the same thread that will do the close
        // actual tests won't do this.
        Thread.sleep((long)(2 * 1000));
        // Log before close
        //SyncLogger.Instance().Log(SyncLogger.ThreadType.MAIN_THREAD, 0,
                //"Closing Room");
        room.CloseRoom();
        // This should print only after all elements are closed (here we wait 3 seconds so it should be immediate)


        t1.join();
        t2.join();
        t3.join();


        //SyncLogger.Instance().Log(SyncLogger.ThreadType.MAIN_THREAD, 0,
               // "Room is Closed");


        SyncLogger.Instance().Log(SyncLogger.ThreadType.MAIN_THREAD, 0,
                "Room is Closed");
    }
}


