import threading
import time
import random

K = 100
n_consumers = 8
count = K
rep = 0
buffer: list = list(range(1,K+1))
mutex = threading.Semaphore(1)
producer = threading.Semaphore(0)
consumer = threading.Semaphore(0)

def receive() -> None:
    global count, producer, consumer, mutex

    while(True):
        with mutex:
            if count == 0:
                print("Buffer is empty, waiting for OS")
                producer.release()
                consumer.acquire()
            count -= 1
            packet = get_data_packet()
        
        print(f"{threading.current_thread().name} received packet {packet}")


def handle():
    global rep, K, count, buffer

    while True:
        producer.acquire()
        print(f"{threading.current_thread().name} is filling Packets...")
        data = read_net_buffer()
        fill_packets(data)
        time.sleep(2)
        count = K
        rep += 1
        consumer.release()

def read_net_buffer():
    return list(range(1+(K * rep), (K * rep) + K + 1))

def fill_packets(data):
    global buffer

    buffer = data

def get_data_packet():
    global buffer
    return buffer.pop()

if __name__ == "__main__":
    threads = []

    for i in range(n_consumers):
        threads.append(threading.Thread(target=receive, name=f"Consumer {i}"))
    
    pd = threading.Thread(target=handle, name="Producer")
    threads.append(pd)

    random.shuffle(threads)

    for thread in threads:
        thread.start()