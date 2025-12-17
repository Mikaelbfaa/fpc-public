import threading
import random
import time

class LightSwitch:
    count = 0
    mutex = threading.Semaphore(1)

    def lock(self, sem):
        self.mutex.acquire()
        self.count += 1

        if self.count == 1:
            sem.acquire()
        
        self.mutex.release()
        
    def unlock(self, sem):
        self.mutex.acquire()
        self.count -= 1

        if self.count == 0:
            sem.release()
        
        self.mutex.release()

catraca = threading.Semaphore(1)
room = threading.Semaphore(1)
ls = LightSwitch()

def wrap_read():
    global ls, catraca, room
    
    catraca.acquire()
    catraca.release()
    
    ls.lock(room)
    n = read()
    ls.unlock(room)

    return n

def read():
    print(f"{threading.current_thread().name} is reading cache")
    time.sleep(1)
    return random.randint(1,100)

def wrap_write(d):
    catraca.acquire()
    room.acquire()
    write(d)
    room.release()
    catraca.release()

def write(d):
    print(f"{threading.current_thread().name} is writing {d} on cache")
    time.sleep(2)

if __name__ == "__main__":
    readers = [threading.Thread(target=wrap_read, name=f"Reader-{i}") for i in range(10)]
    writers = [threading.Thread(target=wrap_write, args=(i*10,), name=f"Writer-{i}") for i in range(4)]

    threads = readers + writers
    random.shuffle(threads)
    
    for t in threads:
        t.start()
    
    for t in threads:
        t.join()
    
    print("Teste concluido")