import threading
import random
import time
from threading import Semaphore

class ConfigStore:

    def __init__(self, config_key, database_adress, timeout, flags) -> None:
        self.config_key = config_key
        self.database_adress = database_adress
        self.timeout = timeout
        self.flags = flags

class LightSwitch:
    counter = 0
    mutex = threading.Semaphore(1)

    def lock(self, sem: Semaphore):
        self.mutex.acquire()
        self.counter += 1
        if self.counter == 1:
            sem.acquire()
        self.mutex.release()
    
    def unlock(self, sem: Semaphore):
        self.mutex.acquire()
        self.counter -= 1
        if self.counter == 0:
            sem.release()
        self.mutex.release()

turnstile = threading.Semaphore(1)
isEmpty = threading.Semaphore(1)
ls = LightSwitch()

def safe_lookup():
    turnstile.acquire()
    turnstile.release()

    ls.lock(isEmpty)
    lookup()
    ls.unlock(isEmpty)


def lookup():
    print(f"{threading.current_thread().name} is looking up on db")

def safe_update():
    turnstile.acquire()
    isEmpty.acquire()
    print("Updating, no one should read or write")
    update()
    print("Done")
    isEmpty.release()
    turnstile.release()

def update():
    print("updating")
    time.sleep(2)
    print("done updating")


if __name__ == "__main__":
    readerThreads = []
    writerThreads = []
    allThreads = []

    for i in range(10):
        readerThreads.append(threading.Thread(target=safe_lookup, name=f"Reader {i}"))
    
    for i in range(2):
        writerThreads.append(threading.Thread(target=safe_update, name=f"Writer {i}"))
    
    allThreads = readerThreads + writerThreads
    random.shuffle(allThreads)

    for thread in allThreads:
        thread.start()
    
    for thread in allThreads:
        thread.join()