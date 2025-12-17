from threading import Semaphore, Thread
from time import sleep
from random import shuffle
import uuid

SIZE = 1000
N_PRODUCERS = 4
N_CONSUMERS = 2

class Broker:
    requests = []
    producer = Semaphore(SIZE)
    consumer = Semaphore(0)
    
    def submitRequest(self, r):
        self.producer.acquire()
        self.requests.append(r)
        print(f"Master is appending {r} to requests") 
        self.consumer.release()

    def getWork(self):
        self.consumer.acquire()
        request = self.requests.pop()
        print(f"Worker is processing {request}")
        self.producer.release()

broker = Broker()

def workerHandler():
    global broker
    while (True):
        broker.getWork()
        sleep(1)

def masterHandler():
    global broker
    while (True):
        broker.submitRequest(uuid.uuid1())
        sleep(1)

if __name__ == "__main__":
    threads = []

    for i in range(N_PRODUCERS):
        threads.append(Thread(target=masterHandler, name=f"Master {i}"))
    
    for i in range(N_CONSUMERS):
        threads.append(Thread(target=workerHandler, name=f"Worker {i}"))
    

    shuffle(threads)

    for thread in threads:
        thread.start()
    
    for thread in threads:
        thread.join()