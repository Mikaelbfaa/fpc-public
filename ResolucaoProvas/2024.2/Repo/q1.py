import threading
import random

NUM_THREADS = 6
count = 0
mutex = threading.Semaphore(1)
signaling = threading.Semaphore(0)
local_max: list = [None for _ in range(NUM_THREADS)]

def slice_handler(v:list, start_index: int, end_index: int, array_index: int, n: int) -> None:
    global mutex, count, local_max, signaling

    local_max[array_index] = find_max_slice(v, start_index, end_index)
    print(f"{threading.current_thread().name} found the maximum value: {local_max[array_index]}")


    with mutex:
        count += 1

        if count == n:
            signaling.release()


def find_max_slice(v: list, start_index: int, end_index:int) -> int:
    return max(v[start_index:end_index])


def exec(v: list, n: int):
    l = len(v)
    lines_per_thread = l // n
    threads = []

    for i in range(n):
        si = lines_per_thread * i

        if i == (n - 1):
            ei = l
        else:
            ei = lines_per_thread * (i + 1)
        
        threads.append(threading.Thread(target=slice_handler, args=(v, si, ei, i, n), name=f"Thread {i}")) 
    
    for thread in threads:
        thread.start()
    

if __name__ == "__main__":
    M = random.randint(5,50)
    v = [random.randint(1,100) for _ in range(M)]

    exec(v, NUM_THREADS)

    signaling.acquire()

    print(f"max value: {max(local_max)}")