import threading
import random
import time

M = 100
buffer_size = M
buffer = list(range(1,M+1))
mutex = threading.Semaphore(1)
producer = threading.Semaphore(0)
consumer = threading.Semaphore(0)

def compute():
    global buffer_size
    global producer
    global consumer
    while(True):
        mutex.acquire()
        
        if (buffer_size == 0):
            producer.release()
            consumer.acquire()
        
        j = getJob()
        buffer_size -= 1
        mutex.release()

        execute(j)

def getJob() -> int:
    global buffer
    print(f"{threading.current_thread().name} getting a job")
    return buffer.pop()

def execute(j: int):
    print(f"Executing task: {j}")
    time.sleep(1)

def manage():
    global producer
    global consumer
    global M
    global buffer_size
    while(True):
        producer.acquire()
        create_jobs(M)
        buffer_size = M
        consumer.release()

def create_jobs(M: int):
    global buffer
    print("Starting job creation")
    for i in range(1, M+1):
        buffer.append(i)
    time.sleep(2)
    print("Job creating done!")


if __name__ == "__main__":
    consumerN = 8
    consumerThreads = []
    all_threads = []
    producerThread = threading.Thread(target=manage, name="Producer")

    for i in range(consumerN):
        consumerThreads.append(threading.Thread(target=compute, name=f"Consumer {i + 1}"))
    
    all_threads = consumerThreads
    all_threads.append(producerThread)
    random.shuffle(all_threads)

    for thread in all_threads:
        thread.start()
    
    for thread in all_threads:
        thread.join()
