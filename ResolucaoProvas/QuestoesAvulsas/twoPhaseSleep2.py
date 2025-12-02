import threading
import random
import time

N = 10

class Barrier:
    def __init__(self, size):
        self.mutex = threading.Semaphore(1)
        self.phase1 = threading.Semaphore(0)
        self.phase2 = threading.Semaphore(1)
        self.count = 0
        self.size = size
    
    def wait(self):
        with self.mutex:
            self.count += 1

            if self.count == self.size:
                self.phase2.acquire()
                self.phase1.release()
        
        self.phase1.acquire()
        self.phase1.release()

        with self.mutex:
            self.count -= 1

            if self.count == 0:
                self.phase1.acquire()
                self.phase2.release()
            
        self.phase2.acquire()
        self.phase2.release()

barrier = Barrier(N)
sleep_time: list = [None for _ in range(N)]

def two_phase_sleep(index):
    t = random.randint(0,5)
    time.sleep(t)
    print(f"{threading.current_thread().name} will sleep {t} on the first phase")
    barrier.wait()
    
    s = random.randint(0,10)
    sleep_time[index] = s
    print(f"{threading.current_thread().name} choose the number {s} on the second phase and will save on index: {index}")
    barrier.wait()

    index = (index - 1) if index != (N - 1) else 0

    print(f"{threading.current_thread().name} is sleeping {sleep_time[index]} on the second phase")
    time.sleep(sleep_time[index])
    barrier.wait()
    print(f"{threading.current_thread().name} is done!")

def create_threads(n):
    threads = []

    for i in range(n):
        threads.append(threading.Thread(target=two_phase_sleep, args=(i,), name=f"Thread {i}"))
    
    for thread in threads:
        thread.start()
    
    for thread in threads:
        thread.join()


if __name__ == "__main__":
    create_threads(N)