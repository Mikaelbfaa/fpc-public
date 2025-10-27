import threading
import time
import random

sem = threading.Semaphore(1)
total = 0


def thread_function() -> None:
    global total

    sleep_time = random.randint(1, 10)
    print(
        f"Thread {threading.current_thread().name} started and will wait for {sleep_time} seconds"
    )
    time.sleep(sleep_time)
    print(f"Thread {threading.current_thread().name} finished waiting")
    sumTime(sleep_time)


def sumTime(time: int) -> None:
    global total

    with sem:
        total += time

    print(f"Thread {threading.current_thread().name} added {time} to total.")


if __name__ == "__main__":
    threads: list[threading.Thread] = []

    for _ in range(5):
        new_thread = threading.Thread(target=thread_function)
        threads.append(new_thread)
        new_thread.start()

    for thread in threads:
        thread.join()

    print(f"The total time is: {total}")
