import threading
import random
import time

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

def handle_post():
    while True:
            get_sem.acquire()
            current_threads.acquire()
            ls.lock(post_sem)
            post()
            ls.unlock(post_sem)
            current_threads.release()
            get_sem.release()

def handle_get():
    while True:
        current_threads.acquire()
        ls.lock(get_sem)
        get()
        ls.unlock(get_sem)
        current_threads.release()

def get():
    print(f"{threading.current_thread().name} is doing as GET")
    time.sleep(0.2)

def post():
    print(f"{threading.current_thread().name} is doing as POST")
    time.sleep(0.2)

if __name__ == "__main__":
    N = 10
    post_sem = threading.Semaphore()
    get_sem = threading.Semaphore()
    current_threads = threading.Semaphore(N)
    catraca = threading.Semaphore()
    ls = LightSwitch()
    threads: list = []
    for i in range(N):
            threads.append(threading.Thread(target=handle_get, name=f"Get Thread {i}"))
    for i in range(N):
            threads.append(threading.Thread(target=handle_post, name=f"Post Thread {i}"))
    
    random.shuffle(threads)
    
    for thread in threads:
            thread.start()