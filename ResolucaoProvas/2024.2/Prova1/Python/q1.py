import threading
import random
import time

thread_number = 8
join_counter = 0
mutex = threading.Semaphore(1)
join = threading.Semaphore(0)
sleep_times = []


def foo():
    global join_counter
    global thread_number
    global join
    global mutex

    sleep_time = random.randint(1,5)
    time.sleep(sleep_time)
    print(f"{threading.current_thread().name} slept for {sleep_time} seconds")

    mutex.acquire()

    sleep_times.append(sleep_time)
    join_counter += 1
    if(join_counter == thread_number):
        join.release()

    mutex.release()


if __name__ == "__main__":
    threads = []

    for i in range(thread_number):
        threads.append(threading.Thread(target=foo, name=f"Thread {i+1}"))

    for thread in threads:
        thread.start()
    
    join.acquire()

    print(f"Total wait times: {sleep_times}")

    max_n = max(sleep_times)
    min_n = min(sleep_times)

    print(f"Min + Max = {max_n + min_n}")