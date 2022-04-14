
import java.util.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Random;
import java.util.concurrent.atomic.AtomicMarkableReference;

/*

// COARSE LIST
// - locks whole list
// - only one thread can work on it at a time
// - 4 threads, shouldnt have much contention

LOCK-FREE LIST



SERVANT
- 3 actions:
   - 1. Take present from unordered bag.
        Add to chain in correct order
   - 2. Write note thank you note
        Remove gift from chain
   - 3. Minotaur random event
        Servant needs to check contains

ARCHITECTURE
- BAG
    - arraylist
    - checks if empty everytime time servant tries to grabs from bag
- 4 Threads
    - grabs from bag, removes arraylist item
    - removes from linked list




*/




public class P3 {
    public static void main(String[] args) {
    
        // random var
        Random rand = new Random();
    
        // scanner for input
        Scanner sc = new Scanner(System.in);
    
        
        // prompts for number of presents in bag ( main is 500000)
        System.out.println("Enter number of gifts: ");
        int numGifts = sc.nextInt();

        giftBagTracker.numGifts = numGifts;
    
        // creates giftBag with arraylist
        ArrayList<Integer> giftBag = new ArrayList<Integer>(numGifts);
    
        // add entries to giftBag
        for (int i = 1; i <= numGifts; i++)
            giftBag.add(i);
    
        // test if bag added correctly
        // for(int j = 0; j < 10; j++)
        //     //System.out.println(giftBag.get(j));
          
        // shuffles giftBag to be unordered
        Collections.shuffle(giftBag);
    
        // test if bag shuffled correctly
        // for(int j = 0; j < 10; j++)
        //     //System.out.println(giftBag.get(j));
    
        // creates lock free list (linked list)
        LockFreeList<Integer> list = new LockFreeList<Integer>();

        
        // creates servants
    
        Servant [] servs = new Servant[4];
        // create primary guest threads
        for (int i = 0; i < 4; i++)
        {
            
            servs[i] = new Servant(i, giftBag, list);
            servs[i].start();
        }

        ////System.out.println("All threads have died, in main");
        //list.print();

          

    }
}

class giftBagTracker {
    public static int giftsTaken = 0;
    public static int numGifts = 0;
    public static int notesWritten = 0;
}


// class for node
class Node {
    int key;
    Node next;
    
    
    // constructor for node
    Node(int d) {
      key = d;
    }
}

class Servant extends Thread {

    Random rand = new Random();

    // name
    private int threadNumber;
    // gift that thread is looking at, adding or removing
    private int activeGift;
    ArrayList<Integer> giftBag;
    LockFreeList<Integer> list;
    
    
    // constructor
    public Servant (int threadNumber, ArrayList<Integer> giftBag, LockFreeList<Integer> list)
    {
        this.threadNumber = threadNumber;
        this.giftBag = giftBag;
        this.list = list;
    }
    
    @Override
    public void run()
    {

        //System.out.println("Created servant " + threadNumber);

        //while (list.returnFirstEntry() != -2147483648)
        while (giftBagTracker.giftsTaken < giftBagTracker.numGifts)
        {
            // decides if minotaur request a gift check
            int randomNumber = rand.nextInt(10);
        
            // 1/10 chance that minotaur requests check
            if (randomNumber == 5)
            {
                int randomGift = rand.nextInt(giftBagTracker.numGifts);
              // make thread check if list contains gift (id)
                //System.out.println("Servant " + threadNumber + " got requested by the minotaur ");
                if (list.contains(randomGift))
                    System.out.println("contains gift");
                ////else
                //    System.out.println("does not contain gift");
            }
            // grab from bag or remove gift from list (write thank you note)
            else
            {
                // check if bag is empty
                // if not empty, add to linked list in correct order
                //System.out.println("giftsTaken is " + giftBagTracker.giftsTaken + 
                                   //" and numGifts is " + giftBagTracker.numGifts);
                if (giftBagTracker.giftsTaken < giftBagTracker.numGifts)
                {
                    // grabs id for gift
                    activeGift = giftBag.get(giftBagTracker.giftsTaken++);
                    //System.out.println("Servant " + threadNumber + " grabbed gift " + activeGift);
                    // removes gift from bag, may not need this line
                    //giftBag.remove(1);
    
                    // add active gift to linked list
                    list.add(activeGift);
                }
                // if linked list has no gifts
                if (list.returnFirstEntry() == 2147483647)
                {
                     //System.out.println("Chain is empty, moving on");   
                }
                    
                // check if linked list is empty
                else if (list.returnFirstEntry() != -2147483648)
                {
                    //System.out.println("Servant " + threadNumber + " is removing "
                                     //  + list.returnFirstEntry());
                    //if (list.returnFirstEntry())
                    if (list.returnFirstEntry() != 2147483647)
                    {
                        list.remove(list.returnFirstEntry());
                        giftBagTracker.notesWritten++;
                        //System.out.println("notesWritten is " + giftBagTracker.notesWritten);
                        
                    }
                }
                
                // check if linked list is empty
                // remove from linked list
            }
            //System.out.println("Chain looks like: ");
            // method to print lockfreelist, written in lockfreelist class
            list.print();
            //System.out.println();
        }
        System.out.println("Servant " + threadNumber + " has died");
    }
}
  
class LockFreeList<T> {

    public Node head;

    public LockFreeList() {
        this.head  = new Node(Integer.MIN_VALUE);
        Node tail = new Node(Integer.MAX_VALUE);
        while (!head.next.compareAndSet(null, tail, false, false));
    }


    public boolean add(T item) {
        int key = item.hashCode();
        boolean splice;
        while (true) {
          // find predecessor and curren entries
            Window window = find(head, key);
            Node pred = window.pred, curr = window.curr;
          // is the key present?
            if (curr.key == key) 
            {
                return false;
            } 
            else {
                // splice in new node
                Node node = new Node(item);
                node.next = new AtomicMarkableReference(curr, false);
                if (pred.next.compareAndSet(curr, node, false, false)) 
                {
                    return true;
                }
            }
        }
    }

    
    public boolean remove(T item) {
        int key = item.hashCode();
        boolean snip;
        while (true) 
        {
            // find predecessor and current entries
            Window window = find(head, key);
            Node pred = window.pred, curr = window.curr;
            // is the key present?
            if (curr.key != key) {
                return false;
            } 
            else
            {
                // snip out matching node
                Node succ = curr.next.getReference();
                snip = curr.next.attemptMark(succ, true);
                if (!snip)
                    continue;
                pred.next.compareAndSet(curr, succ, false, false);
                return true;
            }
        }
    }
    

    public boolean contains(T item) {
        int key = item.hashCode();
        // find predecessor and curren entries
        Window window = find(head, key);
        Node pred = window.pred, curr = window.curr;
        return (curr.key == key);
    }

    public void print() {
        Node temp = head;
        while (temp.next.getReference() != null)
        //while (1 == 1) 
        {
            //System.out.print(temp.key + ", ");
            temp = temp.next.getReference();
        }
    }

    public int returnFirstEntry()
    {
        Node temp = head;
        if (temp.next != null)
        {
            temp = temp.next.getReference();
            //System.out.println("Inside of returnForstEntry() returning " + temp.key);
        }
        return temp.key;
    }
    
    public class Node {
    
        T item;

        int key;

        AtomicMarkableReference<Node> next;

        Node(T item) {      
            this.item = item;
            this.key = item.hashCode();
            this.next = new AtomicMarkableReference<Node>(null, false);
        }

        
        Node(int key) { // sentinel constructor
            this.item = null;
            this.key = key;
            this.next = new AtomicMarkableReference<Node>(null, false);
        }
    }


    class Window {
    
        public Node pred;
    
        public Node curr;
    
        Window(Node pred, Node curr) {
            this.pred = pred; this.curr = curr;
        }
    }


    public Window find(Node head, int key) {
        Node pred = null, curr = null, succ = null;
        boolean[] marked = {false}; // is curr marked?
        boolean snip;
        retry: while (true) {
            pred = head;
            curr = pred.next.getReference();
            while (true) {
                succ = curr.next.get(marked);
                while (marked[0]) {           // replace curr if marked
                snip = pred.next.compareAndSet(curr, succ, false, false);
                if (!snip) continue retry;
                curr = pred.next.getReference();
                succ = curr.next.get(marked);
            }
            if (curr.key >= key)
                return new Window(pred, curr);
            pred = curr;
            curr = succ;
            }
       }
    }
}



// class CoarseList<T> {

//   // class for node
//   class Node {
//     int data;
//     Node next;
  
//     // constructor for node
//     Node(int d) {
//       data = d;
//     }
//   }

//   // declare head
//   private Node head;
//   // create instance of lock
//   private Lock lock = new ReentrantLock();

//   // constructor for coarselist
//   public CoarseList()  {
//     head = new Node(Integer.MIN_VALUE);
//     head.next = new Node(Integer.MAX_VALUE);
//   }

//   public boolean add(T item) {
//     Node pred, curr;
//     int key = item.hashCode();
//     lock.lock();
//     try {
//       pred = head;
//       curr = pred.next;
//       while (curr.key < key) {
//         pred = curr;
//         curr = curr.next;
//       }
//       if (key == curr.key) {
//         return false;
//       }
//       else {
//         Node node = new Node(item);
//         node.next = curr;
//         pred.next = node;
//         return true;
//       }
//     } finally {
//       lock.unlock();
//     }
//   }
// }