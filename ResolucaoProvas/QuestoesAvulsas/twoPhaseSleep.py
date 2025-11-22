import threading
import time
import random

NUM_THREADS = 6

class Barrier:
    def __init__(self, size: int) -> None:
        self.count = 0
        self.size = size
        self.mutex = threading.Semaphore(1)
        self.catraca1 = threading.Semaphore(0)
        self.catraca2 = threading.Semaphore(1)

    def wait(self):
        # Fase 1

        self.mutex.acquire()
        self.count += 1

        if self.count == self.size:
            self.catraca2.acquire()
            self.catraca1.release()
        
        self.mutex.release()
        
        self.catraca1.acquire()
        self.catraca1.release()

        # Região crítica aqui!

        # Fase 2

        self.mutex.acquire()
        self.count -= 1

        if self.count == 0:
            self.catraca2.release()
            self.catraca1.acquire()
        
        self.mutex.release()

        self.catraca2.acquire()
        self.catraca2.release()
    

barrier = Barrier(NUM_THREADS)
count = 0
mutex = threading.Semaphore(1)
signaling = threading.Semaphore(0)
timeSleep = [-999 for _ in range(NUM_THREADS)]


def twoPhaseSleep(index):
    global barrier, mutex, count, signaling, timeSleep

    t = random.randint(1,5)
    print(f"{threading.current_thread().name} will sleep {t} on the first phase")
    time.sleep(t)
    barrier.wait()

    s = random.randint(0,10)
    timeSleep[index] = s
    print(f"{threading.current_thread().name} choose the number {s} on the second phase and will save on index: {index}")
    barrier.wait()

    if index == 0:
        st = timeSleep[len(timeSleep) - 1]
    else:
        st = timeSleep[index - 1]
    
    print(f"{threading.current_thread().name} is sleeping {st} on the second phase")
    time.sleep(st)

    with mutex:
        count += 1

        if (count == NUM_THREADS):
            print("Last thread is done!")
            signaling.release()


if __name__ == "__main__":
    threads = []

    for i in range(NUM_THREADS):
        threads.append(threading.Thread(target=twoPhaseSleep, args=(i,), name=f"Thread {i}"))
    
    for thread in threads:
        thread.start()
    
    signaling.acquire()
    print("Main is no more")
    
