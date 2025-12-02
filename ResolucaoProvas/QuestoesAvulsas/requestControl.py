import threading
import uuid
import time
import random

class LightSwitch:
    def __init__(self):
        self.mutex = threading.Semaphore(1)
        self.count = 0
    
    def lock(self, sem: threading.Semaphore):
        with self.mutex:
            self.count += 1

            if self.count == 1:
                sem.acquire()
    
    def unlock(self, sem: threading.Semaphore):
        with self.mutex:
            self.count -= 1

            if self.count == 0:
                sem.release()


def handle(request: int, req_type: int):
    if req_type == 0:
        catraca.acquire()
        catraca.release()

        ls0.lock(mutex_tipo)
        execute(request, req_type)
        ls0.unlock(mutex_tipo)
    elif req_type == 1:
        catraca.acquire()
        catraca.release()

        ls1.lock(mutex_tipo)
        execute(request, req_type)
        ls1.unlock(mutex_tipo)
    

def execute(request, req_type):
    print(f"{threading.current_thread().name} is executing the type {req_type} request: {request}")

if __name__ == "__main__":
    ls0 = LightSwitch()
    ls1 = LightSwitch()
    mutex_tipo = threading.Semaphore()
    catraca = threading.Semaphore(1)
    N = 50
    threads = []

    for i in range(N):
        req = random.randint(1,100000)
        req_type = random.randint(0,1)
        threads.append(threading.Thread(target=handle, args=(req, req_type)))
    
    for thread in threads:
        thread.start()
    
    for thread in threads:
        thread.join()
