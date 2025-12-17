package ResolucaoProvas.QuestoesAvulsas;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

class SimpleBakery {
    volatile int n;
    AtomicIntegerArray tickets;
    AtomicInteger ticketDispenser;

    public SimpleBakery(int n) {
        this.n = n;
        this.tickets = new AtomicIntegerArray(n);
        this.ticketDispenser = new AtomicInteger(0);
    }

    public void lock(int id) throws ArrayIndexOutOfBoundsException {
        try {
            this.tickets.set(id, this.ticketDispenser.incrementAndGet());

            for (int i = 0; i < this.n; i++) {


                while (this.tickets.get(i) != 0 && this.tickets.get(id) > this.tickets.get(i)) {
                    // busy wait
                }
            }
        } catch (Exception e) {
           e.printStackTrace();
        }

    }

    public void unlock(int id) {
        try {
            this.tickets.set(id, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}