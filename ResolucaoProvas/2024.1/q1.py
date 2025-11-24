import random
import time
from threading import Semaphore, current_thread, Thread

extern_count = 0
intern_count = 0
extern_request = Semaphore(4)
mutex = Semaphore(1)
intern_request = Semaphore(1)

def externalRoute(req: str):
    global extern_count, mutex, extern_request, intern_request, intern_count
    
    extern_request.acquire()
    
    handle()

    with mutex:
        extern_count += 1
        if extern_count == 4 and intern_count == 1:
            intern_request.release()
            release_externs()
            extern_count = 0
            intern_count = 0
    

def internalRoute(req: str):
    global extern_count, intern_request, extern_request, intern_count

    intern_request.acquire()

    handle()
    
    with mutex:
        intern_count += 1

        if intern_count == 1 and extern_count == 4:
            intern_count = 0
            extern_count = 0
            release_externs()
            intern_request.release()

def release_externs():
    global extern_request
    for _ in range(4):
        extern_request.release()

def handle():
    print(f"{current_thread().name} is handling a request")
    time.sleep(1)


if __name__ == "__main__":
    threads = []
    
    num_external = 20
    num_internal = 5
    
    print("Iniciando sistema com proporção 1:4 (1 interna para cada 4 externas)")
    print()
    
    for i in range(max(num_external, num_internal)):
        if i < num_external:
            t = Thread(target=externalRoute, args=(f"req_ext_{i}",), name=f"External {i}")
            threads.append(t)
        
        if i < num_internal:
            t = Thread(target=internalRoute, args=(f"req_int_{i}",), name=f"Internal {i}")
            threads.append(t)
    
    random.shuffle(threads)
    
    for t in threads:
        t.start()
        time.sleep(0.05)
    
    for t in threads:
        t.join()
    
    print()
    print("Todas as requisições foram processadas")